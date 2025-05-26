package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.mixin.access.KeyBindingRegistryImplAccessor;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.chyzman.reboundless.Reboundless.MOD_NAME_MAP;
import static com.chyzman.reboundless.Reboundless.UNKNOWN_CATEGORY;
import static java.util.Comparator.*;
import static net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor.fabric_getCategoryMap;

@SuppressWarnings("UnstableApiUsage")
public enum GroupMode {
    CATEGORY(
        bind -> bind.properties.getCategory(),
        comparingInt(v -> fabric_getCategoryMap().getOrDefault(v, Integer.MAX_VALUE))
    ),
    MOD(
        bind -> {
            if (bind.properties.binding() == null) return null;
            if (!(bind.properties.binding() instanceof KeyBindBinding keyBindBinding)) return bind.properties.binding().type.translationKey;
            var keyBinding = keyBindBinding.keyBinding;
            if (!(KeyBindingRegistryImplAccessor.reboundless$getModdedKeys().contains(keyBinding))) return "minecraft";
            var pattern = Reboundless.TRADITIONAL_KEYBIND_KEY_PATTERN.matcher(keyBinding.getTranslationKey());
            if (pattern.find() && MOD_NAME_MAP.containsKey(pattern.group(1))) return pattern.group(1);
            return null;
        },
        comparing(
            s -> s.equals("minecraft") ? null : s,
            nullsFirst(
                comparing(
                    MOD_NAME_MAP::get,
                    nullsLast(
                        naturalOrder()
                    )
                )
            )
        ),
        name -> {
            var mappedName = Reboundless.MOD_NAME_MAP.get(name);
            if (mappedName != null) return Text.literal(mappedName);
            return Text.translatable(name);
        }
    ),
    TYPE(bind -> bind.properties.binding().type.translationKey),
    NONE(binding -> null);

    private final @NotNull Function<ReBinding, @Nullable String> category;
    private final @NotNull Comparator<@Nullable String> comparator;
    private final @NotNull Function<String, Text> label;

    //region CONSTRUCTORS

    GroupMode(
        @NotNull Function<ReBinding, String> category,
        @NotNull Comparator<String> comparator,
        @NotNull Function<String, Text> label
    ) {
        this.category = category;
        this.comparator = comparator;
        this.label = label;
    }

    GroupMode(
        @NotNull Function<ReBinding, String> category,
        @NotNull Comparator<String> comparator
    ) {
        this(
            category,
            comparator,
            Text::translatable
        );
    }

    GroupMode(
        @NotNull Function<ReBinding, String> category,
        @NotNull Function<String, Text> label
    ) {
        this(
            category,
            naturalOrder(),
            label
        );
    }

    GroupMode(
        @NotNull Function<ReBinding, String> category
    ) {
        this(
            category,
            naturalOrder(),
            Text::translatable
        );
    }

    //endregion

    public String getGroup(ReBinding binding) {
        return Objects.requireNonNullElse(category.apply(binding),UNKNOWN_CATEGORY);
    }

    public Comparator<@Nullable String> comparator() {
        return comparator.thenComparing(label.andThen(Text::getString), comparator);
    }

    public Text getLabel(String name) {
        return label.apply(name);
    }

    //TODO: you were revamping this, then you were going to do the same for sortmode, also you were taking a break to add more features to nested lang
}
