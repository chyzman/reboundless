package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.ReBindings;
import com.google.gson.Gson;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Shadow @Final static Gson GSON;
    @Shadow @Final public KeyBinding[] allKeys;

    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/sound/SoundCategory;values()[Lnet/minecraft/sound/SoundCategory;"))
    private void loadReBindings(GameOptions.Visitor visitor, CallbackInfo ci) {
        ReBindings.load(visitor, GSON, allKeys);
    }
}
