//package com.chyzman.reboundless.api.action.impl;
//
//import com.chyzman.reboundless.api.action.Condition;
//import com.chyzman.reboundless.registry.ConditionRegistry;
//import com.chyzman.reboundless.util.EndecUtil;
//import com.chyzman.reboundless.util.ReboundlessEndecs;
//import io.wispforest.endec.Endec;
//import io.wispforest.endec.StructEndec;
//import io.wispforest.endec.impl.StructEndecBuilder;
//import net.minecraft.client.util.InputUtil;
//import net.minecraft.text.Text;
//import net.minecraft.text.Texts;
//
//import java.util.List;
//import java.util.Set;
//
//public class OrCondition extends Condition<?> {
//    public List<Condition<?>> conditions;
//
//    public static final Type<Set<InputUtil.Key>> TYPE = new Type<>(OrCondition::currentState);
//
//    //region ENDEC
//
//    public static final StructEndec<OrCondition> ENDEC = StructEndecBuilder.of(
//        EndecUtil.optionalFieldOfEmptyCheck(
//            "conditions",
//            Condition.ENDEC.listOf(),
//            o -> o.conditions,
//            List::of
//        ),
//        OrCondition::new
//    );
//
//    //endregion
//
//    public OrCondition(List<Condition<?>> conditions) {
//        super(ConditionRegistry.KEY);
//        this.conditions = conditions;
//    }
//
//    public OrCondition(Condition<?>... conditions) {
//        this(List.of(conditions));
//    }
//
//    @Override
//    protected boolean test(Object state) {
//        return conditions.stream().anyMatch(condition -> condition.test(condition.getType().getter().get()));
//    }
//
//    @Override
//    public Text getDisplayText() {
//        return Text.literal("( ")
//            .append(
//                Texts.join(
//                    this.conditions.stream().map(Condition::getDisplayText).toList(),
//                    Text.literal(" + ")
//                )
//            )
//            .append(" )");
//    }
//
//    @SuppressWarnings("unchecked")
//    public static Set<InputUtil.Key> currentState() {
//        return (Set<InputUtil.Key>) CURRENT_STATES.get(TYPE);
//    }
//
//    @Override
//    public Type<Set<InputUtil.Key>> getType() {
//        return TYPE;
//    }
//}
