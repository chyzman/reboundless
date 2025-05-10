package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class ReCombination {
    public static final ReCombination EMPTY = new ReCombination(
        InputUtil.UNKNOWN_KEY,
        new ArrayList<>(), false,
        new HashSet<>(), false,
        false, false
    );

    private InputUtil.Key key;

    private final List<InputUtil.Key> modifiers;
    private boolean ordered;

    private final Set<InputUtil.Key> exceptions;
    private boolean isWhitelist;

    private boolean sticky;
    private boolean inverted;

    //region INTERNAL STATE

    private boolean pressed;

    //endregion

    //region ENDEC STUFF

    public static final Endec<ReCombination> ENDEC = StructEndecBuilder.of(
        EndecUtil.optionalFieldOfEmptyCheck("key", ReboundlessEndecs.KEY, ReCombination::key, () -> EMPTY.key),
        EndecUtil.optionalFieldOfEmptyCheck("modifiers", ReboundlessEndecs.KEY.listOf(), ReCombination::modifiers, () -> EMPTY.modifiers),
        EndecUtil.optionalFieldOfEmptyCheck("ordered", Endec.BOOLEAN, ReCombination::ordered, () -> EMPTY.ordered),
        EndecUtil.optionalFieldOfEmptyCheck("exceptions", ReboundlessEndecs.KEY.setOf(), ReCombination::exceptions, () -> EMPTY.exceptions),
        EndecUtil.optionalFieldOfEmptyCheck("whitelist", Endec.BOOLEAN, ReCombination::isWhitelist, () -> EMPTY.isWhitelist),
        EndecUtil.optionalFieldOfEmptyCheck("sticky", Endec.BOOLEAN, ReCombination::sticky, () -> EMPTY.sticky),
        EndecUtil.optionalFieldOfEmptyCheck("inverted", Endec.BOOLEAN, ReCombination::inverted, () -> EMPTY.inverted),
        ReCombination::new
    );

    //endregion

    //region CONSTRUCTORS

    public ReCombination(
        InputUtil.Key key,
        List<InputUtil.Key> modifiers,
        boolean ordered,
        Set<InputUtil.Key> exceptions,
        boolean isWhitelist,
        boolean sticky,
        boolean inverted
    ) {
        this.key = key;
        this.modifiers = modifiers;
        this.ordered = ordered;
        this.exceptions = exceptions;
        this.isWhitelist = isWhitelist;
        this.sticky = sticky;
        this.inverted = inverted;

        this.pressed = !inverted;
    }

    public ReCombination(
        InputUtil.Key key,
        List<InputUtil.Key> modifiers,
        boolean ordered
    ) {
        this(key, modifiers, ordered, new HashSet<>(), false, false, false);
    }

    public ReCombination(
        InputUtil.Key key,
        List<InputUtil.Key> modifiers
    ) {
        this(key, modifiers, false);
    }

    public ReCombination(
        InputUtil.Key key
    ) {
        this(key, new ArrayList<>());
    }

    public static ReCombination fromKeyBinding(KeyBinding keybinding) {
        return new ReCombination(((KeyBindingAccessor) keybinding).reboundless$getBoundKey());
    }

    //endregion

    //region GETTERS AND SETTERS

    public void key(InputUtil.Key key) {
        this.key = key;
    }

    public InputUtil.Key key() {
        return key;
    }

    public List<InputUtil.Key> modifiers() {
        return modifiers;
    }

    public void ordered(boolean ordered) {
        this.ordered = ordered;
    }

    public boolean ordered() {
        return ordered;
    }

    public Set<InputUtil.Key> exceptions() {
        return exceptions;
    }

    public void isWhitelist(boolean isWhitelist) {
        this.isWhitelist = isWhitelist;
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }

    public boolean sticky() {
        return sticky;
    }

    public void sticky(boolean sticky) {
        this.sticky = sticky;
    }

    public boolean inverted() {
        return inverted;
    }

    public void inverted(boolean inverted) {
        this.inverted = inverted;
    }

    //endregion

    public void onKey(InputUtil.Key key, boolean pressed) {
        if (!key.equals(this.key)) return;

        var currentlyHeld = new ArrayList<>(Reboundless.CURRENTLY_HELD_KEYS);

        if (pressed) {
            if (!modifiersMatch(currentlyHeld)) return;
            if (!exceptionsMatch(currentlyHeld)) return;
        }

        if (setPressed(pressed)) {
            var reBinding = ReBindings.reBindingByCombination(this);
            if (reBinding == null) return;
            reBinding.setPressed(this, isPressed());
        }
    }

    public boolean isPressed() {
        return pressed != inverted;
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

    public void resetState() {
        pressed = inverted;
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
        if (!isWhitelist && exceptions.isEmpty()) return true;

        var stripped = new HashSet<>(keys);
        stripped.remove(this.key);
        modifiers.forEach(stripped::remove);

        if (isWhitelist) {
            return exceptions.containsAll(stripped);
        } else {
            return exceptions.stream().noneMatch(stripped::contains);
        }
    }

    public Text getBoundText() {
        var text = Text.empty();
        if (!modifiers.isEmpty()) {
            for (InputUtil.Key modifier : modifiers.reversed()) {
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
