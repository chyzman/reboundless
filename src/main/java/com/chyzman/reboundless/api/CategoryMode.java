package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.access.KeyBindingRegistryImplAccessor;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Function;

import static com.chyzman.reboundless.Reboundless.MOD_NAME_MAP;
import static com.chyzman.reboundless.Reboundless.UNKNOWN_CATEGORY;

@SuppressWarnings("UnstableApiUsage")
public enum CategoryMode {
    VANILLA(
        bind -> bind.properties.getCategory(),
        (cat, other) -> {
            var categoryMap = KeyBindingAccessor.fabric_getCategoryMap();

            Comparator<String> mapComparator = Comparator.comparing(categoryMap::get, Comparator.nullsLast(Comparator.naturalOrder()));

            return mapComparator
                .thenComparing((category, otherCategory) -> {
                    boolean categoryInMap = categoryMap.containsKey(category);
                    boolean otherCategoryInMap = categoryMap.containsKey(otherCategory);

                    if (categoryInMap != otherCategoryInMap) return categoryInMap ? -1 : 1;
                    return 0;
                })
                .thenComparing(mapComparator)
                .thenComparing(Comparator.naturalOrder())
                .compare(cat, other);
        }
    ),
    MOD(
        bind -> {
            if (!(KeyBindingRegistryImplAccessor.reboundless$getModdedKeys().contains(bind.properties.keybinding()))) return "minecraft";
            var pattern = Reboundless.TRADITIONAL_KEYBIND_KEY_PATTERN.matcher(bind.properties.keybinding().getTranslationKey());
            if (pattern.find() && MOD_NAME_MAP.containsKey(pattern.group(1))) return pattern.group(1);
            return "";
        },
        Comparator.comparing(
            s -> s.equals("minecraft") ? null : s,
            Comparator.nullsFirst(
                Comparator.comparing(
                    MOD_NAME_MAP::get,
                    Comparator.nullsLast(
                        Comparator.naturalOrder()
                    )
                )
            )
        ),
        name -> {
            var mappedName = Reboundless.MOD_NAME_MAP.get(name);
            if (mappedName != null) return Text.literal(mappedName);
            return Text.translatable(UNKNOWN_CATEGORY);
        }
    ),
    NONE(null);

    private final Function<ReBinding, String> category;
    private final Comparator<String> comparator;
    private final Function<String, Text> label;

    //region CONSTRUCTORS

    CategoryMode(
        Function<ReBinding, String> category,
        Comparator<String> comparator,
        Function<String, Text> label
    ) {
        this.category = category;
        this.comparator = comparator;
        this.label = label;
    }

    CategoryMode(
        Function<ReBinding, String> category,
        Comparator<String> comparator
    ) {
        this(
            category,
            comparator,
            Text::translatable
        );
    }

    CategoryMode(
        Function<ReBinding, String> category,
        Function<String, Text> label
    ) {
        this(
            category,
            Comparator.naturalOrder(),
            label
        );
    }


    CategoryMode(
        Function<ReBinding, String> category
    ) {
        this(
            category,
            Comparator.naturalOrder(),
            Text::translatable
        );
    }

    //endregion

    private static final Map<String, Integer> SPECIALS = new HashMap<>(
        Map.of(
            UNKNOWN_CATEGORY, 0
        )
    );

    public String getCategory(ReBinding binding) {
        return category.apply(binding);
    }

    public Comparator<String> getComparator() {
        var mapComparator = comparator.thenComparing(SPECIALS::get, Comparator.nullsFirst(Comparator.naturalOrder()));


        return mapComparator
            .thenComparing((cat, other) -> {
                var categoryIsSpecial = SPECIALS.containsKey(cat);
                var otherIsSpecial = SPECIALS.containsKey(other);

                if (categoryIsSpecial != otherIsSpecial) return categoryIsSpecial ? -1 : 1;
                return 0;
            })
            .thenComparing(mapComparator)
            .thenComparing(Comparator.naturalOrder());
    }

    public Text getLabel(String name) {
        return label.apply(name);
    }
}
