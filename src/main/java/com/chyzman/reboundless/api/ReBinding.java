package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReBinding {
    private static final ReBinding EMPTY = new ReBinding(
        "",
        new ArrayList<>(),
        1,
        0,
        1,
        350,
        null
    );

    //region PROPERTIES

    private String name;

    private final List<ReCombination> combinations;

    private int pressPower;

    private int debounce;

    private int pressesRequired;
    private int timeBetweenPresses;

    @Nullable
    private KeyBinding keybinding;

    //endregion

    //region INTERNAL STATE

    private boolean pressed;

    private Set<ReCombination> activeCombos = new HashSet<>();

    //endregion

    //region ENDEC STUFF

    public static final Endec<ReBinding> ENDEC = StructEndecBuilder.of(
        EndecUtil.optionalFieldOfEmptyCheck("name", Endec.STRING, ReBinding::name, () -> EMPTY.name),
        EndecUtil.optionalFieldOfEmptyCheck("combos",ReCombination.ENDEC.listOf(), ReBinding::combinations, () -> EMPTY.combinations),
        EndecUtil.optionalFieldOfEmptyCheck("pressPower", Endec.INT, ReBinding::pressPower, () -> EMPTY.pressPower),
        EndecUtil.optionalFieldOfEmptyCheck("debounce", Endec.INT, ReBinding::debounce, () -> EMPTY.debounce),
        EndecUtil.optionalFieldOfEmptyCheck("pressesRequired", Endec.INT, ReBinding::pressesRequired, () -> EMPTY.pressesRequired),
        EndecUtil.optionalFieldOfEmptyCheck("timeBetweenPresses", Endec.INT, ReBinding::timeBetweenPresses, () -> EMPTY.timeBetweenPresses),
        EndecUtil.optionalFieldOfEmptyCheck("keybinding", ReboundlessEndecs.KEYBINDING, ReBinding::keybinding, () -> EMPTY.keybinding),
        ReBinding::new
    );

    //endregion

    //region CONSTRUCTORS

    private ReBinding(
        String name,
        List<ReCombination> combinations,
        int pressPower,
        int debounce,
        int pressesRequired,
        int timeBetweenPresses,
        @Nullable KeyBinding keybinding
    ) {
        this.name = name;
        this.combinations = combinations;
        this.pressPower = pressPower;
        this.debounce = debounce;
        this.pressesRequired = pressesRequired;
        this.timeBetweenPresses = timeBetweenPresses;
        this.keybinding = keybinding;
    }

    public ReBinding(
        Text name,
        ReCombination combination,
        @Nullable KeyBinding keybinding
    ) {
        this(
            name.getString(),
            new ArrayList<>(List.of(combination)),
            EMPTY.pressPower(),
            EMPTY.debounce(),
            EMPTY.pressesRequired(),
            EMPTY.timeBetweenPresses(),
            keybinding
        );
    }

    public static ReBinding fromKeyBinding(KeyBinding keybinding) {
        return new ReBinding(
            Text.empty(),
            ReCombination.fromKeyBinding(keybinding),
            keybinding
        );
    }

    //endregion

    //region GETTERS AND SETTERS

    public String name() {
        return name;
    }

    public ReBinding name(String name) {
        this.name = name;
        return this;
    }

    public List<ReCombination> combinations() {
        return combinations;
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

    public String getDisplayedName() {
        return name.isBlank() && keybinding != null ? Text.translatable(keybinding.getTranslationKey()).getString() : name;
    }

    public boolean isPressed() {
        return !activeCombos.isEmpty();
    }

    public void setPressed(ReCombination combo, boolean pressed) {
        var update = false;
        if (pressed) {
            update = activeCombos.add(combo);
        } else {
            update = activeCombos.remove(combo);
        }
        if (update) {
            if (keybinding != null) ((KeyBindingDuck) keybinding).reboundless$setPressed(this, pressed);
            updateState();
        }
    }

    public void updateState() {
        pressed = !activeCombos.isEmpty();
        if (keybinding != null) ((KeyBindingDuck) keybinding).reboundless$updateState();
    }

    public void resetState() {
        activeCombos.clear();
        for (ReCombination combination : combinations) combination.resetState();
        updateState();
    }

    public void resetToDefault() {
        //TODO: implement this
    }
}
