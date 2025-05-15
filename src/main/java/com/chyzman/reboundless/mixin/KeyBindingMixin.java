package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements KeyBindingDuck {

    @Shadow @Final private static Map<String, KeyBinding> KEYS_BY_ID;

    @Shadow private int timesPressed;
    @Shadow private boolean pressed;
    @Unique private final Set<ReBinding> activeRebinds = new HashSet<>();

    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void handleOnKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "setKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void handleSetKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        ci.cancel();
        for (ReBinding bind : ReBindings.reBindingsByKey(key)) bind.onKey(key, pressed);
    }

    @Inject(method = "updatePressedStates", at = @At(value = "HEAD"), cancellable = true)
    private static void handlesUpdatePressedStates(CallbackInfo ci) {
        ci.cancel();
        for (ReBinding reBinding : ReBindings.allReBindings()) reBinding.updateState();
    }

    @Inject(method = "unpressAll", at = @At(value = "HEAD"), cancellable = true)
    private static void handleUnpressAll(CallbackInfo ci) {
        ci.cancel();
        for (KeyBinding binding : KEYS_BY_ID.values()) ((KeyBindingDuck) binding).reboundless$resetState();
    }

    @Override
    public void reboundless$setPressed(ReBinding reBinding, boolean pressed, int power) {
        var update = false;
        if (pressed) {
            update = activeRebinds.add(reBinding);
        } else {
            update = activeRebinds.remove(reBinding);
        }
        if (update) {
            if (pressed) timesPressed += power;
            reboundless$updateState();
        }
    }

    @Override
    public void reboundless$updateState() {
        pressed = !activeRebinds.isEmpty();
    }

    @Override
    public void reboundless$resetState() {
        activeRebinds.clear();
        for (ReBinding reBinding : ReBindings.reBindingsByKeyBinding(((KeyBinding) (Object) this))) reBinding.resetState();
        reboundless$updateState();
    }
}
