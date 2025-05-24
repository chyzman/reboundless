package com.chyzman.reboundless.api.action.impl;

import com.chyzman.reboundless.api.action.Condition;
import com.chyzman.reboundless.registry.ConditionRegistry;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.Set;

public class KeyCondition extends Condition<Set<InputUtil.Key>> {
    public InputUtil.Key key;
    public boolean pressed;

    public static final Condition.Type<Set<InputUtil.Key>> TYPE = new Condition.Type<>(KeyCondition::currentState);

    //region ENDEC

    public static final StructEndec<KeyCondition> ENDEC = StructEndecBuilder.of(
        ReboundlessEndecs.KEY.fieldOf("key", o -> o.key),
        EndecUtil.optionalFieldOfEmptyCheck("pressed", Endec.BOOLEAN, o -> o.pressed, () -> true),
        KeyCondition::new
    );

    //endregion

    public KeyCondition(InputUtil.Key key, boolean pressed) {
        super(ConditionRegistry.KEY);
        this.key = key;
        this.pressed = pressed;
    }

    public KeyCondition(InputUtil.Key key) {
        this(key, true);
    }

    @Override
    protected boolean test(Set<InputUtil.Key> state) {
        return state.contains(key) == pressed;
    }

    @Override
    public Text getDisplayText() {
        return Text.literal(pressed ? "": "-").append(key.getLocalizedText());
    }

    @SuppressWarnings("unchecked")
    public static Set<InputUtil.Key> currentState() {
        return (Set<InputUtil.Key>) CURRENT_STATES.get(TYPE);
    }

    @Override
    public Type<Set<InputUtil.Key>> getType() {
        return TYPE;
    }
}
