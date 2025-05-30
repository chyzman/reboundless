package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.InputHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    //TODO make assigning keys that work in the keybinds screen not immediatealy activate when assigned to something
//    @Shadow @Final private MinecraftClient client;
//
//    @Definition(id = "client", field = "Lnet/minecraft/client/Keyboard;client:Lnet/minecraft/client/MinecraftClient;")
//    @Definition(id = "currentScreen", field = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;")
//    @Definition(id = "KeybindsScreen", type = KeybindsScreen.class)
//    @Expression("this.client.currentScreen instanceof KeybindsScreen")
//    @ModifyExpressionValue(method = "onKey", at = @At("MIXINEXTRAS:EXPRESSION"))
//    private boolean respectMyKeybindScreenDamnit(boolean original) {
//        return original || !(client.currentScreen instanceof KeybindingScreen);
//    }

//    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;"))
//    private void rememberMyKeysPlease$addPressedFromKeyboard(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
//        if (action == 1) InputHandler.onInput(InputUtil.fromKeyCode(key, scancode), true);
//    }
//
//    @Inject(method = "onKey", at = @At("RETURN"))
//    private void rememberMyKeysPlease$removeUnpressedFromKeyboard(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
//        if (action == 0) InputHandler.onInput(InputUtil.fromKeyCode(key, scancode), false);
//    }
}
