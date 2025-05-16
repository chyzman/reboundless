package com.chyzman.reboundless.api;

import java.util.Comparator;

public enum SortingMode {
    ALPHABETICAL(Comparator.comparing(bind -> bind.getDisplayedName().getString()));

    public final Comparator<ReBinding> comparator;

    SortingMode(Comparator<ReBinding> comparator) {
        this.comparator = comparator;

    }
}
