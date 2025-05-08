package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.mixin.access.StickyKeyBindingAccessor;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ReBinding {
    //region PROPERTIES

    private String name = "";

    private InputUtil.Key key = InputUtil.UNKNOWN_KEY;

    private List<InputUtil.Key> modifiers = new ArrayList<>();
    private boolean ordered = false;

    private boolean sticky = false;
    private boolean inverted = false;

    private List<InputUtil.Key> exceptions = new ArrayList<>();
    private boolean isWhitelist = false;

    private int pressPower = 1;

    private int debounce = 0;

    private int pressesRequired = 1;
    private int timeBetweenPresses = 350;

    @Nullable
    private KeyBinding keybinding = null;

    //endregion

    //region INTERNAL STATE

    private boolean pressed = false;

    //endregion

    //region ENDEC STUFF

    public static final Endec<ReBinding> ENDEC = StructEndecBuilder.of(
        Endec.STRING.optionalFieldOf("name", ReBinding::name, () -> null),
        ReboundlessEndecs.KEY.optionalFieldOf("key", ReBinding::key, () -> null),
        ReboundlessEndecs.KEY.listOf().optionalFieldOf("modifiers", ReBinding::modifiers, () -> null),
        Endec.BOOLEAN.optionalFieldOf("sticky", ReBinding::sticky, () -> null),
        Endec.BOOLEAN.optionalFieldOf("inverted", ReBinding::inverted, () -> null),
        ReboundlessEndecs.KEY.listOf().optionalFieldOf("exceptions", ReBinding::exceptions, () -> null),
        Endec.BOOLEAN.optionalFieldOf("isWhitelist", ReBinding::isWhitelist, () -> null),
        Endec.INT.optionalFieldOf("pressPower", ReBinding::pressPower, () -> null),
        Endec.INT.optionalFieldOf("debounce", ReBinding::debounce, () -> null),
        Endec.INT.optionalFieldOf("pressesRequired", ReBinding::pressesRequired, () -> null),
        Endec.INT.optionalFieldOf("timeBetweenPresses", ReBinding::timeBetweenPresses, () -> null),
        ReboundlessEndecs.KEYBINDING.optionalFieldOf("keybinding", ReBinding::keybinding, () -> null),
        ReBinding::new
    );

    //endregion

    private ReBinding(
        @Nullable String name,
        @Nullable InputUtil.Key key,
        @Nullable List<InputUtil.Key> modifiers,
        @Nullable Boolean sticky,
        @Nullable Boolean inverted,
        @Nullable List<InputUtil.Key> exceptions,
        @Nullable Boolean isWhitelist,
        @Nullable Integer pressPower,
        @Nullable Integer debounce,
        @Nullable Integer pressesRequired,
        @Nullable Integer timeBetweenPresses,
        @Nullable KeyBinding keybinding
    ) {
        if (name != null) this.name = name;
        if (key != null) this.key = key;
        if (modifiers != null) this.modifiers = modifiers;
        if (sticky != null) this.sticky = sticky;
        if (inverted != null) this.inverted = inverted;
        if (exceptions != null) this.exceptions = exceptions;
        if (isWhitelist != null) this.isWhitelist = isWhitelist;
        if (pressPower != null) this.pressPower = pressPower;
        if (debounce != null) this.debounce = debounce;
        if (pressesRequired != null) this.pressesRequired = pressesRequired;
        if (timeBetweenPresses != null) this.timeBetweenPresses = timeBetweenPresses;
        if (keybinding != null) this.keybinding = keybinding;
    }

    public ReBinding() {}

    public static ReBinding fromKeyBinding(KeyBinding keybinding) {
        var accessor = (KeyBindingAccessor) keybinding;
        return new ReBinding(
            keybinding.getTranslationKey(),
            accessor.reboundless$getBoundKey(),
            new ArrayList<>(),
            keybinding instanceof StickyKeyBinding sticky && ((StickyKeyBindingAccessor) sticky).reboundless$getBooleanSupplier().getAsBoolean(),
            false,
            new ArrayList<>(),
            false,
            1,
            0,
            1,
            350,
            keybinding
        );
    }

    //region GETTERS AND SETTERS

    public String name() {
        return name;
    }

    public ReBinding name(String name) {
        this.name = name;
        return this;
    }

    public InputUtil.Key key() {
        return key;
    }

    public ReBinding key(InputUtil.Key key) {
        this.key = key;
        return this;
    }

    public List<InputUtil.Key> modifiers() {
        return modifiers;
    }

    public boolean sticky() {
        return sticky;
    }

    public ReBinding sticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    public boolean inverted() {
        return inverted;
    }

    public ReBinding inverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    public List<InputUtil.Key> exceptions() {
        return exceptions;
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }

    public ReBinding isWhitelist(boolean isWhitelist) {
        this.isWhitelist = isWhitelist;
        return this;
    }

    public int pressPower() {
        return pressPower;
    }

    public ReBinding pressPower(int pressPower) {
        this.pressPower = pressPower;
        return this;
    }

    public int debounce() {
        return debounce;
    }

    public ReBinding debounce(int debounce) {
        this.debounce = debounce;
        return this;
    }

    public int pressesRequired() {
        return pressesRequired;
    }

    public ReBinding pressesRequired(int pressesRequired) {
        this.pressesRequired = pressesRequired;
        return this;
    }

    public int timeBetweenPresses() {
        return timeBetweenPresses;
    }

    public ReBinding timeBetweenPresses(int timeBetweenPresses) {
        this.timeBetweenPresses = timeBetweenPresses;
        return this;
    }

    public KeyBinding keybinding() {
        return keybinding;
    }

    public ReBinding keybinding(KeyBinding keybinding) {
        this.keybinding = keybinding;
        return this;
    }

    //endregion

    public void onKey(InputUtil.Key key, boolean pressed) {
        //why did you even ask if this isn't the key
        if (key != this.key) return;

        var currentlyHeldKeys = Reboundless.CURRENTLY_HELD_KEYS.reversed();

        //make sure its releasing or the modifiers and exceptions match
        if (pressed) {
            if (!modifiersMatch(currentlyHeldKeys)) return;
            if (!exceptionsMatch(currentlyHeldKeys)) return;
        }

        if (setPressed(pressed)) {
            if (keybinding != null) {
                if (isPressed()) ((KeyBindingAccessor) keybinding).reboundless$setTimesPressed(((KeyBindingAccessor) keybinding).reboundless$getTimesPressed() + pressPower);
                updateKeybindingState();
            }
        }
    }

    public boolean modifiersMatch(List<InputUtil.Key> keys) {
        if (modifiers.isEmpty()) return true;
        if (keys.size() < modifiers.size()) return false;
        if (ordered) {
            var important = new ArrayList<>(keys);
            important.retainAll(modifiers);
            return important.equals(modifiers);
        }
        return new HashSet<>(keys).containsAll(modifiers);
    }

    public boolean exceptionsMatch(List<InputUtil.Key> keys) {
        //empty blacklist means it's all good
        if (!isWhitelist && exceptions.isEmpty()) return true;

        //remove the key and modifiers from the set
        var stripped = new HashSet<>(keys);
        stripped.remove(this.key);
        modifiers.forEach(stripped::remove);

        if (isWhitelist) {
            //if whitelist then exceptions are the only keys that can be pressed
            return new HashSet<>(exceptions).containsAll(stripped);
        } else {
            //if blacklist then exceptions are the only keys that can't be pressed
            return exceptions.stream().noneMatch(stripped::contains);
        }
    }

    public boolean setPressed(boolean pressed) {
        var updated = false;
        if (sticky) {
            if (pressed) {
                this.pressed = !this.pressed;
                updated = true;
            }
        } else {
            updated = pressed != this.pressed;
            this.pressed = pressed;
        }
        return updated;
    }

    public boolean isPressed() {
        return inverted != pressed;
    }

    //TODO: maybe rethink this, vanilla will always call setpressed before incrementing the times pressed, as im writing this my gut says its a bit iffy idk why
    public void updateKeybindingState() {
        if (keybinding == null) return;
        ((KeyBindingDuck) keybinding).reboundless$setRebindingsPressing(this, isPressed());
        ((KeyBindingDuck) keybinding).reboundless$updatePressed();
    }

    public void reset() {
        if (keybinding != null) ((KeyBindingAccessor) keybinding).reboundless$reset();
        pressed = false;
        updateKeybindingState();
    }

    public Text getBoundText() {
        var text = Text.empty();
        if (!modifiers.isEmpty()) {
            for (InputUtil.Key modifier : modifiers) {
                text.append(modifier.getLocalizedText());
                text.append(" + ");
            }
        }
        text.append(key.getLocalizedText());
        return text;
    }

    public Text getEditingText() {
        var text = Text.empty();
        var useBinding = Reboundless.CURRENTLY_HELD_KEYS.isEmpty();

        text.append(Text.literal("> "));

        var center = Text.empty();
        if (useBinding) {
            center.append(getBoundText());
        } else {
            for (InputUtil.Key heldKey : Reboundless.CURRENTLY_HELD_KEYS) {
                center.append(heldKey.getLocalizedText())
                    .append(Text.literal(" + "));
            }
            center.append(Text.literal("..."));
        }
        text.append(center.formatted(Formatting.WHITE, Formatting.UNDERLINE));

        text.append(Text.literal(" <"));

        return text.formatted(Formatting.YELLOW);
    }
}
