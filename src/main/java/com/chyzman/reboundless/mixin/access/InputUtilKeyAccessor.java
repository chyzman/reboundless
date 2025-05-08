package com.chyzman.reboundless.mixin.access;

import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(InputUtil.Key.class)
public interface InputUtilKeyAccessor {

    @Accessor("KEYS")
    static Map<String, InputUtil.Key> reboundless$getKeysMap() {
        throw new UnsupportedOperationException("me when the accessor is called");
    }

}
