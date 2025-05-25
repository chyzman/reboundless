package com.chyzman.reboundless.util;

import java.util.Collection;
import java.util.List;

public class ListUtil {
    public static <T> void replace(List<T> list, List<T> newList) {
        list.clear();
        list.addAll(newList);
    }
}
