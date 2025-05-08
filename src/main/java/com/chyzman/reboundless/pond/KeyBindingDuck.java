package com.chyzman.reboundless.pond;

import com.chyzman.reboundless.api.ReBinding;

import java.util.Map;
import java.util.Set;

public interface KeyBindingDuck {

    default Set<ReBinding> reboundless$getRebindingsPressing() {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

    default void reboundless$setRebindingsPressing(ReBinding reBinding, boolean pressed) {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

    default void reboundless$updatePressed() {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

}
