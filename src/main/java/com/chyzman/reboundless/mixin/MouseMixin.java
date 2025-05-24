package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.InputHandler;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.chyzman.reboundless.Reboundless.*;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void rememberMyKeysPlease$addPressedFromMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1) {
            var input = InputUtil.Type.MOUSE.createFromCode(button);
            InputHandler.onInput(input, true);
        }
    }

    @Inject(method = "onMouseButton", at = @At("RETURN"))
    private void rememberMyKeysPlease$removeUnpressedFromMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 0) {
            var input = InputUtil.Type.MOUSE.createFromCode(button);
            InputHandler.onInput(input, false);
        }
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;"))
    public void makeScrollingWork(long window, double horizontal, double vertical, CallbackInfo ci) {
        InputUtil.Key input = Math.abs(vertical) > Math.abs(horizontal) ? vertical > 0 ? SCROLL_UP : SCROLL_DOWN : horizontal > 0 ? SCROLL_LEFT : SCROLL_RIGHT;
        InputHandler.onInput(input, true);
        KeyBinding.setKeyPressed(input, true);
        KeyBinding.onKeyPressed(input);
    }
}
