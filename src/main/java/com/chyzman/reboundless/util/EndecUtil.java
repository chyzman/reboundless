package com.chyzman.reboundless.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructField;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EndecUtil {
    public static <S, T> StructField<S, T> optionalFieldOfEmptyCheck(String name, Endec<T> endec, Function<S, T> getter, Supplier<@Nullable T> defaultValue) {
        return optionalFieldOfEmptyCheck(name, endec, getter, defaultValue, t -> Objects.equals(t, defaultValue.get()));
    }

    public static <S, T> StructField<S, T> optionalFieldOfEmptyCheck(String name, Endec<T> endec, Function<S, T> getter, Supplier<@Nullable T> defaultValue, Predicate<T> isEmpty) {
        Objects.requireNonNull(defaultValue, "Optional default value is null for field: " + name);

        return new StructField<>(
            name,
            endec.optionalOf()
                .xmap(
                    optional -> optional.orElseGet(defaultValue),
                    t -> {
                        if (isEmpty.test(t)) return Optional.empty();

                        return Optional.ofNullable(t);
                    }
                ),
            getter,
            defaultValue
        );
    }

    public static <T extends Enum<T>> Endec<T> enumEndec(Class<T> enumClass, T defaultValue) {
        return Endec.STRING.xmap(string -> Enum.valueOf(enumClass, string), t -> t.name().toLowerCase(Locale.ROOT)).catchErrors((ctx, serializer, exception) -> defaultValue);
    }
}
