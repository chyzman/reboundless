package com.chyzman.reboundless.api;

import com.chyzman.reboundless.api.action.ActionStep;
import com.chyzman.reboundless.api.action.ConvertableToActionStep;
import com.chyzman.reboundless.api.action.impl.KeyCondition;
import com.chyzman.reboundless.api.binding.Bindable;
import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.util.EndecUtil;
import eu.pb4.placeholders.api.parsers.TagParser;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.chyzman.reboundless.Reboundless.UNKNOWN_CATEGORY;

public class ReBinding {

    public final Properties properties;

    private final Properties initialProperties;

    private boolean active;

    //TODO: make this private
    public int nextStep = 0;

    private long lastUpdated;


    //region ENDEC

    public static final Endec<ReBinding> ENDEC = Properties.ENDEC.xmap(ReBinding::new, bind -> bind.properties);

    //endregion

    //region CONSTRUCTORS

    private ReBinding(
        Properties properties,
        Properties initialProperties
    ) {
        this.properties = properties;
        this.initialProperties = initialProperties;

        this.active = false;
        this.lastUpdated = System.currentTimeMillis();
    }

    public ReBinding(Properties properties) {
        this(properties, new Properties(properties));
    }

    public ReBinding(
        String name,
        List<ActionStep> activationSteps,
        List<ActionStep> deactivationSteps,
        boolean inverted,
        int debounce,
        int pressPower,
        @Nullable Bindable keybinding
    ) {
        this(new Properties(
            name,
            activationSteps,
            deactivationSteps,
            inverted,
            debounce,
            pressPower,
            keybinding
        ));
    }

    //endregion

    public void step() {
        var now = System.currentTimeMillis();

        if (properties.debounce > 0 && now - lastUpdated > properties.debounce) return;

        var targetSequence = active ? properties.deactivationSteps : properties.activationSteps;

        if (targetSequence.size() <= nextStep) {
            nextStep = 0;
            return;
        }

        var test = targetSequence.get(nextStep).test();

        if (test == ActionStep.StepResult.FAILURE) nextStep = 0;
        if (test != ActionStep.StepResult.SUCCESS) return;

        nextStep++;

        if (nextStep < targetSequence.size()) return;

        nextStep = 0;
        lastUpdated = now;

        var nextPressed = !active;

        if (setPressed(nextPressed) && properties.binding != null) properties.binding.setPressed(this, nextPressed, properties.pressPower);
    }

    public boolean isPressed() {
        return active != properties.inverted;
    }

    public boolean setPressed(boolean pressed) {
        var updated = pressed != this.active;
        this.active = pressed;
        return updated;
    }

    public void updateState() {
        if (properties.binding != null && properties.binding instanceof KeyBindBinding keyBindBinding) ((KeyBindingDuck) keyBindBinding.keyBinding).reboundless$updateState();
    }

    public void resetState() {
        active = false;
        lastUpdated = System.currentTimeMillis();
        updateState();
    }

    public void updateInitialProperties() {
        initialProperties.apply(properties);
    }

    public boolean isInitial() {
        return properties.equals(initialProperties);
    }

    public void revertToInitial() {
        properties.apply(initialProperties);
        updateState();
    }

    public boolean isDefault() {
        return properties.binding == null || properties.equals(properties.binding.generateProperties());
    }

    public void revertToDefault() {
        if (properties.binding == null) return;
        properties.apply(properties.binding.generateProperties());
        updateState();
    }

    public Text getDisplayedName() {
        return properties.name.isBlank() && properties.binding != null ? properties.binding.getName() : TagParser.DEFAULT_SAFE.parseNode(properties.name).toText();
    }

    public static class Properties {

        public static final Properties EMPTY = new Properties(
            "",
            List.of(),
            List.of(),
            false,
            0,
            1,
            null
        );

        //region PROPERTIES

        private String name;

        public List<ActionStep> activationSteps;
        public List<ActionStep> deactivationSteps;

        private boolean inverted;

        private int debounce;

        private int pressPower;

        @Nullable
        private Bindable binding;

        //endregion

        //region ENDEC

        public static final Endec<Properties> ENDEC = StructEndecBuilder.of(
            EndecUtil.optionalFieldOfEmptyCheck("name", Endec.STRING, o -> o.name, () -> EMPTY.name),
            EndecUtil.optionalFieldOfEmptyCheck("activationSteps", ActionStep.ENDEC.listOf(), o -> o.activationSteps, () -> new ArrayList<>(EMPTY.activationSteps)),
            EndecUtil.optionalFieldOfEmptyCheck("deactivationSteps", ActionStep.ENDEC.listOf(), o -> o.deactivationSteps, () -> new ArrayList<>(EMPTY.deactivationSteps)),
            EndecUtil.optionalFieldOfEmptyCheck("inverted", Endec.BOOLEAN, o -> o.inverted, () -> EMPTY.inverted),
            EndecUtil.optionalFieldOfEmptyCheck("debounce", Endec.INT, o -> o.debounce, () -> EMPTY.debounce),
            EndecUtil.optionalFieldOfEmptyCheck("pressPower", Endec.INT, o -> o.pressPower, () -> EMPTY.pressPower),
            EndecUtil.optionalFieldOfEmptyCheck("binding", Bindable.ENDEC, o -> o.binding, () -> EMPTY.binding),
            Properties::new
        );

        //endregion

        //region CONSTRUCTORS

        public Properties(
            String name,
            List<ActionStep> activationSteps,
            List<ActionStep> deactivationSteps,
            boolean inverted,
            int debounce,
            int pressPower,
            @Nullable Bindable binding
        ) {
            this.name = name;
            this.activationSteps = activationSteps;
            this.deactivationSteps = deactivationSteps;
            this.inverted = inverted;
            this.debounce = debounce;
            this.pressPower = pressPower;
            this.binding = binding;
        }

        public Properties() {
            this(
                EMPTY.name,
                new ArrayList<>(EMPTY.activationSteps),
                new ArrayList<>(EMPTY.deactivationSteps),
                EMPTY.inverted,
                EMPTY.debounce,
                EMPTY.pressPower,
                EMPTY.binding
            );
        }

        public Properties(Properties properties) {
            this(
                properties.name,
                new ArrayList<>(properties.activationSteps),
                new ArrayList<>(properties.deactivationSteps),
                properties.inverted,
                properties.debounce,
                properties.pressPower,
                properties.binding
            );
        }

        public void apply(Properties that) {
            this.name = that.name;
            this.replaceActivationSteps(that.activationSteps);
            this.replaceDeactivationSteps(that.deactivationSteps);
            this.inverted = that.inverted;
            this.debounce = that.debounce;
            this.pressPower = that.pressPower;
            this.binding = that.binding;
        }

        //endregion

        //region GETTERS AND SETTERS

        //name

        public String name() {
            return name;
        }

        public Properties name(String name) {
            this.name = name;
            return this;
        }

        //activation Steps

        public Properties replaceActivationSteps(Collection<? extends ConvertableToActionStep> activationSteps) {
            this.activationSteps.clear();
            this.activationSteps.addAll(activationSteps.stream().map(ConvertableToActionStep::toActionStep).toList());
            return this;
        }

        public Properties replaceActivationSteps(ConvertableToActionStep... activationSteps) {
            return this.replaceActivationSteps(List.of(activationSteps));
        }

        //deactivation Steps

        public Properties replaceDeactivationSteps(Collection<? extends ConvertableToActionStep> deactivationSteps) {
            this.deactivationSteps.clear();
            this.deactivationSteps.addAll(deactivationSteps.stream().map(ConvertableToActionStep::toActionStep).toList());
            return this;
        }

        public Properties replaceDeactivationSteps(ConvertableToActionStep... deactivationSteps) {
            return this.replaceDeactivationSteps(List.of(deactivationSteps));
        }

        //inverted

        public boolean inverted() {
            return inverted;
        }

        public Properties inverted(boolean inverted) {
            this.inverted = inverted;
            return this;
        }

        //debounce

        public int debounce() {
            return debounce;
        }

        public Properties debounce(int debounce) {
            this.debounce = debounce;
            return this;
        }

        //pressPower

        public int pressPower() {
            return pressPower;
        }

        public Properties pressPower(int pressPower) {
            this.pressPower = pressPower;
            return this;
        }

        //keybinding

        public Bindable binding() {
            return binding;
        }

        public Properties binding(Bindable binding) {
            this.binding = binding;
            return this;
        }

        //categorization

        public String getCategory() {
            if (binding == null || binding.getVanillaCategoryKey() == null) return UNKNOWN_CATEGORY;
            return binding.getVanillaCategoryKey();
        }

        //endregion

        //TODO: make this better
        public void rebind(List<InputUtil.Key> keys) {
            this.replaceActivationSteps(new ActionStep(keys.stream().map(KeyCondition::new).toList()));
            if (keys.isEmpty()) {
                this.replaceDeactivationSteps(List.of());
            } else {
                this.replaceDeactivationSteps(new KeyCondition(keys.getLast(), false));
            }
        }

        public Text getDisplayText() {
            return Text.empty().append(
                    Texts.join(
                        activationSteps.stream().map(ActionStep::getDisplayText).toList(),
                        Text.literal(" -> ")
                    ).copy().formatted(Formatting.GREEN))
                .append("\n")
                .append(
                    Texts.join(
                        deactivationSteps.stream().map(ActionStep::getDisplayText).toList(),
                        Text.literal(" -> ")
                    ).copy().formatted(Formatting.RED));
        }

        public boolean unpressable() {
            if (this.binding == null) return true;
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Properties that)) return false;
            return inverted == that.inverted &&
                   debounce == that.debounce &&
                   pressPower == that.pressPower &&
                   Objects.equals(name, that.name) &&
                   Objects.equals(activationSteps, that.activationSteps) &&
                   Objects.equals(deactivationSteps, that.deactivationSteps) &&
                   Objects.equals(binding, that.binding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                name,
                activationSteps,
                deactivationSteps,
                inverted,
                debounce,
                pressPower,
                binding
            );
        }
    }
}
