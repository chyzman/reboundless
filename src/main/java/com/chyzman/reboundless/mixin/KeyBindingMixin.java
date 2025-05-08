package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
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

    @Unique private Set<ReBinding> rebindingsPressingMe = new HashSet<>();

    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void handleOnKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "setKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void handleSetKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        ci.cancel();
        for (ReBinding bind : ReBindings.byKey(key)) bind.onKey(key, pressed);
    }

    @Inject(method = "updatePressedStates", at = @At(value = "HEAD"), cancellable = true)
    private static void handlesUpdatePressedStates(CallbackInfo ci) {
        ci.cancel();
        for (ReBinding reBinding : ReBindings.all()) {
            reBinding.updateKeybindingState();
        }
    }

    @Inject(method = "unpressAll", at = @At(value = "HEAD"), cancellable = true)
    private static void handleUnpressAll(CallbackInfo ci) {
        ci.cancel();
        for (ReBinding reBinding : ReBindings.all()) reBinding.reset();
    }

    @Override
    public Set<ReBinding> reboundless$getRebindingsPressing() {
        return rebindingsPressingMe;
    }

    @Override
    public void reboundless$setRebindingsPressing(ReBinding reBinding, boolean pressed) {
        if (pressed) {
            rebindingsPressingMe.add(reBinding);
        } else {
            rebindingsPressingMe.remove(reBinding);
        }
    }

    @Override
    public void reboundless$updatePressed() {
        ((KeyBindingAccessor) this).reboundless$setPressed(!((KeyBindingDuck) this).reboundless$getRebindingsPressing().isEmpty());
    }
}
