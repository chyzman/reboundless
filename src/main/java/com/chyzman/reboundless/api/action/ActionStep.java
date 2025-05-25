package com.chyzman.reboundless.api.action;

import com.chyzman.reboundless.util.EndecUtil;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ActionStep implements ConvertableToActionStep {
    public final List<Condition<?>> conditions;

    //region ENDEC

    public static final Endec<ActionStep> ENDEC = StructEndecBuilder.of(
        EndecUtil.optionalFieldOfEmptyCheck(
            "conditions",
            Condition.ENDEC.listOf(),
            o -> o.conditions,
            List::of
        ),
        ActionStep::new
    );

    //endregion

    //region CONSTRUCTORS

    public ActionStep(List<? extends Condition<?>> conditions) {
        this.conditions = new ArrayList<>(conditions);
    }

    public ActionStep(Condition<?>... conditions) {
        this.conditions = new ArrayList<>(List.of(conditions));
    }

    //endregion

    @SuppressWarnings({"unchecked", "rawtypes"})
    public StepResult test() {
        if (this.conditions.isEmpty()) return StepResult.IGNORE;
        Condition mainCondition = this.conditions.getLast();
        var mainType = mainCondition.getType();
        if (mainCondition.test(Condition.CURRENT_STATES.get(mainType)) == mainCondition.test(Condition.PREVIOUS_STATES.get(mainType))) return StepResult.IGNORE;
        for (Condition precondition : this.conditions) if (!precondition.test(Condition.CURRENT_STATES.get(precondition.getType()))) return StepResult.FAILURE;
        return StepResult.SUCCESS;
    }

    public Text getDisplayText() {
        return Texts.join(
            this.conditions.stream().map(Condition::getDisplayText).toList(),
            Text.literal(" + ")
        );
    }

    @Override
    public ActionStep toActionStep() {
        return this;
    }

    public enum StepResult {
        SUCCESS,
        FAILURE,
        IGNORE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActionStep that)) return false;
        return Objects.equals(conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(conditions);
    }
}
