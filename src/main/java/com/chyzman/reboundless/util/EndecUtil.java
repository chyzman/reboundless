package com.chyzman.reboundless.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructField;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EndecUtil {
    public static <S,T> StructField<S, T> optionalFieldOfEmptyCheck(String name, Endec<T> endec, Function<S, T> getter, Supplier<@Nullable T> defaultValue) {
        return optionalFieldOfEmptyCheck(name, endec, getter, defaultValue, t -> Objects.equals(t, defaultValue.get()));
    }

    public static <S,T> StructField<S, T> optionalFieldOfEmptyCheck(String name, Endec<T> endec, Function<S, T> getter, Supplier<@Nullable T> defaultValue, Predicate<T> isEmpty) {
        Objects.requireNonNull(defaultValue, "Supplier was found to be null which is not permitted for optionalFieldOf");

        return new StructField<>(name, endec.optionalOf().xmap(optional -> optional.orElseGet(defaultValue), t -> {
            if (isEmpty.test(t)) return Optional.empty();

            return Optional.ofNullable(t);
        }), getter, defaultValue);
    }
}
