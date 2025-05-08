package com.chyzman.reboundless.util;

import net.minecraft.client.option.SimpleOption;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenModificationUtil {
    public static SimpleOption<?>[] removeToggleButtonsFrom(int start, int finish, SimpleOption<?>[] options) {
        var list = new ArrayList<>(Arrays.asList(options));
        var toRemove = list.subList(start, finish);
        list.removeAll(toRemove);
        return list.toArray(new SimpleOption[0]);
    }
}
