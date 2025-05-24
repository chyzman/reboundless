package com.chyzman.reboundless.api.action;

import com.chyzman.reboundless.Reboundless;
import com.mojang.serialization.Lifecycle;
import io.wispforest.endec.StructEndec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ConditionType<T extends Condition<?>> {
    public static final RegistryKey<Registry<ConditionType<?>>> CONDITION_TYPE_KEY = RegistryKey.ofRegistry(Reboundless.id("condition_type"));
    public static final SimpleRegistry<ConditionType<?>> CONDITION_TYPE_REGISTRY = new SimpleRegistry<>(CONDITION_TYPE_KEY, Lifecycle.stable(), false);

    public final StructEndec<T> endec;
    public final String translationKey;

    public ConditionType(
            StructEndec<T> endec,
            String translationKey
    ) {
        this.endec = endec;
        this.translationKey = translationKey;
    }

    public static <T extends Condition<?>> ConditionType<T> register(RegistryKey<ConditionType<?>> key, StructEndec<T> endec) {
        return Registry.register(CONDITION_TYPE_REGISTRY, key, new ConditionType<>(endec, Util.createTranslationKey("condition", key.getValue())));
    }

    public static <T extends Condition<?>> ConditionType<T> register(Identifier id, StructEndec<T> endec) {
        var key = RegistryKey.of(CONDITION_TYPE_KEY, id);
        return register(key, endec);
    }
}
