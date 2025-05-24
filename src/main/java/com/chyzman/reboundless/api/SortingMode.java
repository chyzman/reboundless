package com.chyzman.reboundless.api;

import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import net.minecraft.text.Text;

import java.util.Comparator;

public enum SortingMode {
    VANILLA(Comparator.comparing(binding -> binding.properties.binding() instanceof KeyBindBinding keyBindBinding ? Text.translatable(keyBindBinding.keyBinding.getTranslationKey()).getString() : null, Comparator.nullsLast(Comparator.naturalOrder()))),
    ALPHABETICAL(Comparator.comparing(bind -> bind.getDisplayedName().getString())),
    REGISTRY((bind, other) -> {
        //TODO: sort vanilla alphabetically then use fapi modded keys list
        var keys = KeyBindingAccessor.reboundless$getKeysById().values().stream().toList();
        var comparator = Comparator.comparing(
            (ReBinding binding) -> binding.properties.binding() == null ? null : keys.indexOf(binding.properties.binding()),
            Comparator.nullsLast(Comparator.naturalOrder())
        );

        return comparator
            .thenComparing(binding -> binding.getDisplayedName().getString())
            .thenComparing(comparator)
            .compare(bind, other);
    });

    public final Comparator<ReBinding> comparator;

    SortingMode(Comparator<ReBinding> comparator) {
        this.comparator = comparator;

    }
}
