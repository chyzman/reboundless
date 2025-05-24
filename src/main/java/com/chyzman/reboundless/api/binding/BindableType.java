package com.chyzman.reboundless.api.binding;

import com.chyzman.reboundless.Reboundless;
import com.mojang.serialization.Lifecycle;
import io.wispforest.endec.StructEndec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class BindableType<T extends Bindable> {
    public static final RegistryKey<Registry<BindableType<?>>> BINDABLE_TYPE_KEY = RegistryKey.ofRegistry(Reboundless.id("binding_type"));
    public static final SimpleRegistry<BindableType<?>> BINDING_TYPE_REGISTRY = new SimpleRegistry<>(BINDABLE_TYPE_KEY, Lifecycle.stable(), false);

    public final StructEndec<T> endec;
    public final String translationKey;

    public BindableType(
            StructEndec<T> endec,
            String translationKey
    ) {
        this.endec = endec;
        this.translationKey = translationKey;
    }

    public static <T extends Bindable> BindableType<T> register(RegistryKey<BindableType<?>> key, StructEndec<T> endec) {
        return Registry.register(BINDING_TYPE_REGISTRY, key, new BindableType<>(endec, Util.createTranslationKey("binding", key.getValue())));
    }

    public static <T extends Bindable> BindableType<T> register(Identifier id, StructEndec<T> endec) {
        var key = RegistryKey.of(BINDABLE_TYPE_KEY, id);
        return register(key, endec);
    }
}
