package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.access.KeyBindingRegistryImplAccessor;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public enum CategoryMode {
    VANILLA(
        bind -> bind.properties.category(),
        Text::translatable,
        (cat, other) -> {
            var categoryMap = KeyBindingAccessor.fabric_getCategoryMap();

            Comparator<String> mapComparator = Comparator.comparing(categoryMap::get, Comparator.nullsLast(Comparator.naturalOrder()));

            return mapComparator
                .thenComparing((category, otherCategory) -> {
                    boolean categoryInMap = categoryMap.containsKey(category);
                    boolean otherCategoryInMap = categoryMap.containsKey(otherCategory);

                    if (categoryInMap && !otherCategoryInMap) {
                        return -1;
                    } else if (!categoryInMap && otherCategoryInMap) {
                        return 1;
                    } else {
                        return 0;
                    }
                })
                .thenComparing(mapComparator)
                .thenComparing(Comparator.naturalOrder())
                .compare(cat, other);
        }
    ),
    MOD(bind -> {
        if (!(KeyBindingRegistryImplAccessor.reboundless$getModdedKeys().contains(bind.properties.keybinding()))) return "minecraft";
        var pattern = Reboundless.TRADITIONAL_KEYBIND_KEY_PATTERN.matcher(bind.properties.keybinding().getTranslationKey());
        if (pattern.find()) return pattern.group(1);
        return bind.properties.keybinding().getTranslationKey();
    }, name -> {
        if ((Reboundless.MOD_NAME_MAP.containsKey(name))) return Text.translatable(Reboundless.MOD_NAME_MAP.get(name));
        return Text.translatable(name);
    }),
    NONE(keyBinding -> null, s -> null);


    public final Function<ReBinding, String> category;
    public final Function<String, Text> label;
    public final Comparator<String> comparator;

    CategoryMode(
        Function<ReBinding, String> category,
        Function<String, Text> label,
        Comparator<String> comparator
    ) {
        this.category = category;
        this.label = label;
        this.comparator = comparator;
    }

    CategoryMode(
        Function<ReBinding, String> category,
        Function<String, Text> label
    ) {
        this(category, label, Comparator.naturalOrder());
    }
}
