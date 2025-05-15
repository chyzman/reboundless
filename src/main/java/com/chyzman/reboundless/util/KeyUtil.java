package com.chyzman.reboundless.util;

import net.minecraft.client.util.InputUtil;

public class KeyUtil {
    public static boolean isValid(InputUtil.Key key) {
        if (key == null) return false;
        if (key == InputUtil.UNKNOWN_KEY) return false;
        return true;
    }
}
