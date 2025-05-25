package com.chyzman.reboundless.api.action;

import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.chyzman.reboundless.api.action.ConditionType.CONDITION_TYPE_REGISTRY;

public abstract class Condition<T> implements ConvertableToActionStep {
    public static final Map<Type<?>, Object> CURRENT_STATES = new HashMap<>();
    public static final Map<Type<?>, Object> PREVIOUS_STATES = new HashMap<>();

    private final ConditionType<?> type;

    //region ENDEC

    public static final Endec<Condition<?>> ENDEC = Endec.dispatchedStruct(
        type -> type.endec,
        condition -> condition.type,
        MinecraftEndecs.ofRegistry(CONDITION_TYPE_REGISTRY)
    );

    //endregion

    public Condition(ConditionType<?> type) {
        this.type = type;
    }

    public abstract boolean test(T state);

    public abstract Text getDisplayText();

    public abstract Type<T> getType();

    public record Type<T>(Supplier<T> getter) {}

    @Override
    public ActionStep toActionStep() {
        return new ActionStep(this);
    }
}
