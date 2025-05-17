package com.chyzman.reboundless.mixin.access;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {

    @Accessor("KEYS_BY_ID")
    static Map<String, KeyBinding> reboundless$getKeysById() {
        throw new UnsupportedOperationException("Implemented by Mixin");
    }

    @Accessor("timesPressed")
    int reboundless$getTimesPressed();
    @Accessor("timesPressed")
    void reboundless$setTimesPressed(int value);

    @Accessor("pressed")
    boolean reboundless$isPressed();
    @Accessor("pressed")
    void reboundless$setPressed(boolean pressed);


    @Invoker("reset")
    void reboundless$reset();

    @Accessor("boundKey")
    InputUtil.Key reboundless$getBoundKey();

}
