package com.chyzman.reboundless.mixin.access;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(KeyBindingRegistryImpl.class)
public interface KeyBindingRegistryImplAccessor {

    @Accessor("MODDED_KEY_BINDINGS")
    static List<KeyBinding> reboundless$getModdedKeys() {
        throw new UnsupportedOperationException("me when the accessor is called");
    }
}
