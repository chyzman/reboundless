package com.chyzman.reboundless.mixin.access;

import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BooleanSupplier;

@Mixin(StickyKeyBinding.class)
public interface StickyKeyBindingAccessor {

    @Accessor("toggleGetter")
    BooleanSupplier reboundless$getBooleanSupplier();

}
