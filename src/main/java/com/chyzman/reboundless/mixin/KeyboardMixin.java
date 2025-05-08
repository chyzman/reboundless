package com.chyzman.reboundless.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.chyzman.reboundless.Reboundless.CURRENTLY_HELD_KEYS;

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

    @Inject(method = "onKey", at = @At("HEAD"))
    private void rememberMyKeysPlease$addPressedFromKeyboard(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1) {
            var keyInput = InputUtil.fromKeyCode(key, scancode);
            if (!CURRENTLY_HELD_KEYS.contains(keyInput)) CURRENTLY_HELD_KEYS.add(InputUtil.fromKeyCode(key, scancode));
        }
    }

    @Inject(method = "onKey", at = @At("RETURN"))
    private void rememberMyKeysPlease$removeUnpressedFromKeyboard(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 0) CURRENTLY_HELD_KEYS.remove(InputUtil.fromKeyCode(key, scancode));
    }
}
