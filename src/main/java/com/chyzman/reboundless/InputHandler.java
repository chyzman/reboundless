package com.chyzman.reboundless;


import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.api.action.Condition;
import com.chyzman.reboundless.api.action.impl.KeyCondition;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InputHandler {
    public static final List<InputUtil.Key> CURRENTLY_HELD_KEYS = new ArrayList<>();

    public static void onInput(InputUtil.Key key, boolean pressed) {
        Condition.PREVIOUS_STATES.put(KeyCondition.TYPE, new HashSet<>(CURRENTLY_HELD_KEYS));
        if (CURRENTLY_HELD_KEYS.contains(key) != pressed) if (pressed) CURRENTLY_HELD_KEYS.add(key); else CURRENTLY_HELD_KEYS.remove(key);
        Condition.CURRENT_STATES.put(KeyCondition.TYPE, new HashSet<>(CURRENTLY_HELD_KEYS));
        ReBindings.stepAll();
    }
}
