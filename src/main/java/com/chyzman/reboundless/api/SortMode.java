package com.chyzman.reboundless.api;

import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import net.minecraft.text.Text;

import java.util.Comparator;

import static java.util.Comparator.*;

public enum SortMode {
    VANILLA(
        comparing(GroupMode.CATEGORY::getGroup, GroupMode.CATEGORY.comparator())
            .thenComparing(
                binding -> binding.properties.binding() instanceof KeyBindBinding keyBindBinding ? Text.translatable(keyBindBinding.keyBinding.getTranslationKey()).getString() : null,
                nullsLast(naturalOrder())
            )
    ),
    ALPHABETICAL(
        comparing(bind -> bind.getDisplayedName().getString())
//    ),
//    REGISTRY(
//        (bind, other) -> {
//            //TODO: sort vanilla alphabetically then use fapi modded keys list
//            var keys = KeyBindingAccessor.reboundless$getKeysById().values().stream().toList();
//            var comparator = comparing(
//                (ReBinding binding) -> binding.properties.binding() == null ? null : keys.indexOf(binding.properties.binding()),
//                nullsLast(naturalOrder())
//            );
//
//            return comparator
//                .thenComparing(binding -> binding.getDisplayedName().getString())
//                .thenComparing(comparator)
//                .compare(bind, other);
//        }
    );

    private final Comparator<ReBinding> comparator;

    SortMode(Comparator<ReBinding> comparator) {
        this.comparator = comparator;
    }

    public Comparator<ReBinding> comparator() {
        return this.comparator.thenComparing(bind -> bind.getDisplayedName().getString());
    }
}
