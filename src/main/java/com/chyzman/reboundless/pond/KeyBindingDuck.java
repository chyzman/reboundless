package com.chyzman.reboundless.pond;

import com.chyzman.reboundless.api.ReBinding;

public interface KeyBindingDuck {

    default void reboundless$setPressed(ReBinding reBinding, boolean pressed) {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

    default void reboundless$updateState() {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

    default void reboundless$resetState() {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

}
