package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.binding.Bindable;
import com.chyzman.reboundless.binding.KeyBindBinding;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.KeyUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import eu.pb4.placeholders.api.parsers.TagParser;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.chyzman.reboundless.Reboundless.UNKNOWN_CATEGORY;

public class ReBinding {

    public final Properties properties;

    private final Properties initialProperties;

    private boolean active;

    private long lastUpdated;

    private int presses;

    //region ENDEC STUFF

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
        this.presses = 0;
    }

    public ReBinding(Properties properties) {
        this(properties, new Properties(properties));
    }

    public ReBinding(
        String name,
        List<InputUtil.Key> keys,
        boolean ordered,
        Set<InputUtil.Key> exceptions,
        boolean isWhitelist,
        boolean sticky,
        boolean inverted,
        int debounce,
        int pressesRequired,
        int pressPower,
        @Nullable Bindable keybinding
    ) {
        this(new Properties(
            name,
            keys,
            ordered,
            exceptions,
            isWhitelist,
            sticky,
            inverted,
            debounce,
            pressesRequired,
            pressPower,
            keybinding
        ));
    }

    //endregion

    public void onKey(InputUtil.Key key, boolean pressed) {
        if (!Objects.equals(properties.key(), key)) return;

        var now = System.currentTimeMillis();

        if (properties.debounce > 0) {
            if (now - lastUpdated > properties.debounce) return;
        }

        if (pressed && !properties.keysMatch(Reboundless.CURRENTLY_HELD_KEYS) || !properties.exceptionsMatch(Reboundless.CURRENTLY_HELD_KEYS)) return;

        if (setPressed(pressed) && properties.binding != null) {
            properties.binding.setPressed(this, pressed, properties.pressPower);
        }
    }

    public boolean isPressed() {
        return active != properties.inverted;
    }

    public boolean setPressed(boolean pressed) {
        var updated = false;
        if (properties.sticky) {
            if (pressed) {
                this.active = !this.active;
                updated = true;
            }
        } else {
            updated = pressed != this.active;
            this.active = pressed;
        }
        return updated;
    }

    public void updateState() {
        if (properties.binding != null && properties.binding instanceof KeyBindBinding keyBindBinding) ((KeyBindingDuck) keyBindBinding.keyBinding).reboundless$updateState();
    }

    public void resetState() {
        active = false;
        presses = 0;
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
            true,
            Set.of(),
            false,
            false,
            false,
            0,
            1,
            1,
            null
        );

        //region PROPERTIES

        private String name;

        private final ArrayList<InputUtil.Key> keys;
        private boolean ordered;

        private final HashSet<InputUtil.Key> exceptions;
        private boolean isWhitelist;

        private boolean sticky;
        private boolean inverted;

        private int debounce;

        //TODO: implement globally stored timeBetweenPresses for pressesRequired
        private int pressesRequired;

        private int pressPower;

        @Nullable
        private Bindable binding;

        //endregion

        //region ENDEC STUFF

        public static final Endec<Properties> ENDEC = StructEndecBuilder.of(
            EndecUtil.optionalFieldOfEmptyCheck("name", Endec.STRING, o -> o.name, () -> EMPTY.name),
            EndecUtil.optionalFieldOfEmptyCheck("keys", ReboundlessEndecs.KEY.listOf(), o -> o.keys, () -> new ArrayList<>(EMPTY.keys)),
            EndecUtil.optionalFieldOfEmptyCheck("ordered", Endec.BOOLEAN, o -> o.ordered, () -> EMPTY.ordered),
            EndecUtil.optionalFieldOfEmptyCheck("exceptions", ReboundlessEndecs.KEY.setOf(), o -> o.exceptions, () -> new HashSet<>(EMPTY.exceptions)),
            EndecUtil.optionalFieldOfEmptyCheck("whitelist", Endec.BOOLEAN, o -> o.isWhitelist, () -> EMPTY.isWhitelist),
            EndecUtil.optionalFieldOfEmptyCheck("sticky", Endec.BOOLEAN, o -> o.sticky, () -> EMPTY.sticky),
            EndecUtil.optionalFieldOfEmptyCheck("inverted", Endec.BOOLEAN, o -> o.inverted, () -> EMPTY.inverted),
            EndecUtil.optionalFieldOfEmptyCheck("debounce", Endec.INT, o -> o.debounce, () -> EMPTY.debounce),
            EndecUtil.optionalFieldOfEmptyCheck("pressesRequired", Endec.INT, o -> o.pressesRequired, () -> EMPTY.pressesRequired),
            EndecUtil.optionalFieldOfEmptyCheck("pressPower", Endec.INT, o -> o.pressPower, () -> EMPTY.pressPower),
            EndecUtil.optionalFieldOfEmptyCheck("binding", Bindable.ENDEC, o -> o.binding, () -> EMPTY.binding),
            Properties::new
        );

        //endregion

        //region CONSTRUCTORS

        public Properties(
            String name,
            Collection<InputUtil.Key> keys,
            boolean ordered,
            Set<InputUtil.Key> exceptions,
            boolean isWhitelist,
            boolean sticky,
            boolean inverted,
            int debounce,
            int pressesRequired,
            int pressPower,
            @Nullable Bindable binding
        ) {
            this.name = name;
            this.keys = new ArrayList<>(keys);
            this.ordered = ordered;
            this.exceptions = new HashSet<>(exceptions);
            this.isWhitelist = isWhitelist;
            this.sticky = sticky;
            this.inverted = inverted;
            this.debounce = debounce;
            this.pressesRequired = pressesRequired;
            this.pressPower = pressPower;
            this.binding = binding;
            this.sanitizeKeys();
            this.sanitizeExceptions();
        }

        public Properties() {
            this(
                EMPTY.name,
                new ArrayList<>(EMPTY.keys),
                EMPTY.ordered,
                new HashSet<>(EMPTY.exceptions),
                EMPTY.isWhitelist,
                EMPTY.sticky,
                EMPTY.inverted,
                EMPTY.debounce,
                EMPTY.pressesRequired,
                EMPTY.pressPower,
                EMPTY.binding
            );
        }

        public Properties(Properties properties) {
            this(
                properties.name,
                new ArrayList<>(properties.keys),
                properties.ordered,
                new HashSet<>(properties.exceptions),
                properties.isWhitelist,
                properties.sticky,
                properties.inverted,
                properties.debounce,
                properties.pressesRequired,
                properties.pressPower,
                properties.binding
            );
        }

        public void apply(Properties that) {
            this.name = that.name;
            this.replaceKeys(that.keys);
            this.ordered = that.ordered;
            this.exceptions.clear();
            this.exceptions.addAll(that.exceptions);
            this.isWhitelist = that.isWhitelist;
            this.sticky = that.sticky;
            this.inverted = that.inverted;
            this.debounce = that.debounce;
            this.pressesRequired = that.pressesRequired;
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

        //key

        public @Nullable InputUtil.Key key() {
            return keys.isEmpty() ? null : keys.getLast();
        }

        public List<InputUtil.Key> modifiers() {
            return new ArrayList<>(keys).subList(0, keys.size() - 1);
        }

        public List<InputUtil.Key> relevantKeys() {
            return new ArrayList<>(keys);
        }

        public Properties replaceKeys(Collection<InputUtil.Key> keys) {
            this.keys.clear();
            this.keys.addAll(keys);
            sanitizeKeys();
            sanitizeExceptions();
            return this;
        }

        public void sanitizeKeys() {
            var sanitized = this.keys.reversed().stream().distinct().filter(KeyUtil::isValid).toList().reversed();
            this.keys.clear();
            this.keys.addAll(sanitized);
        }

        //ordered

        public boolean ordered() {
            return ordered;
        }

        public Properties ordered(boolean ordered) {
            this.ordered = ordered;
            return this;
        }

        //exceptions

        public Set<InputUtil.Key> exceptions() {
            return new HashSet<>(exceptions);
        }

        public boolean addException(InputUtil.Key exception) {
            this.exceptions.add(exception);
            sanitizeExceptions();
            return this.exceptions.contains(exception);
        }

        public boolean removeException(InputUtil.Key exception) {
            return this.exceptions.remove(exception);
        }

        public Properties replaceExceptions(Collection<InputUtil.Key> exceptions) {
            this.exceptions.clear();
            this.exceptions.addAll(exceptions);
            sanitizeExceptions();
            return this;
        }

        public void sanitizeExceptions() {
            this.exceptions.removeIf(exception -> !KeyUtil.isValid(exception) || relevantKeys().contains(exception));
        }

        //isWhitelist

        public boolean isWhitelist() {
            return isWhitelist;
        }

        public Properties isWhitelist(boolean isWhitelist) {
            this.isWhitelist = isWhitelist;
            return this;
        }

        //sticky

        public boolean sticky() {
            return sticky;
        }

        public Properties sticky(boolean sticky) {
            this.sticky = sticky;
            return this;
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

        //pressesRequired

        public int pressesRequired() {
            return pressesRequired;
        }

        public Properties pressesRequired(int pressesRequired) {
            this.pressesRequired = pressesRequired;
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
            if (binding == null || binding.getCategory() == null) return UNKNOWN_CATEGORY;
            return binding.getCategory();
        }

        //endregion

        public boolean unpressable() {
            if (this.binding == null) return true;
            if (!KeyUtil.isValid(this.key())) return true;
            return false;
        }

        public boolean keysMatch(List<InputUtil.Key> keys) {
            if (keys.isEmpty()) return true;
            if (keys.size() < relevantKeys().size()) return false;
            if (ordered) {
                var important = new ArrayList<>(keys);
                important.retainAll(relevantKeys());
                return important.equals(relevantKeys());
            }
            return new HashSet<>(keys).containsAll(relevantKeys());
        }

        public boolean exceptionsMatch(List<InputUtil.Key> keys) {
            if (!isWhitelist && exceptions.isEmpty()) return true;

            var stripped = new HashSet<>(keys);
            stripped.removeIf(relevantKeys()::contains);

            if (isWhitelist) {
                return exceptions.containsAll(stripped);
            } else {
                return exceptions.stream().noneMatch(stripped::contains);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (!(o instanceof Properties that)) return false;
            return ordered == that.ordered &&
                   isWhitelist == that.isWhitelist &&
                   sticky == that.sticky &&
                   inverted == that.inverted &&
                   debounce == that.debounce &&
                   pressesRequired == that.pressesRequired &&
                   pressPower == that.pressPower &&
                   Objects.equals(name, that.name) &&
                   Objects.equals(keys, that.keys) &&
                   Objects.equals(exceptions, that.exceptions) &&
                   Objects.equals(binding, that.binding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                name,
                keys,
                ordered,
                exceptions,
                isWhitelist,
                sticky,
                inverted,
                debounce,
                pressesRequired,
                pressPower,
                binding
            );
        }
    }
}
