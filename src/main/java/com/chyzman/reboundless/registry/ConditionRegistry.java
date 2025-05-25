package com.chyzman.reboundless.registry;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.action.ConditionType;
import com.chyzman.reboundless.api.action.impl.KeyCondition;

public class ConditionRegistry {

    public static final ConditionType<KeyCondition> KEY = ConditionType.register(
        Reboundless.id("key"),
        KeyCondition.ENDEC
    );

    public static void init() {
    }
}
