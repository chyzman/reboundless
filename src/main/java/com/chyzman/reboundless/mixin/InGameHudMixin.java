package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.google.common.base.Strings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static com.chyzman.reboundless.Reboundless.CURRENTLY_HELD_KEYS;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow public abstract TextRenderer getTextRenderer();

    @Shadow @Final private DebugHud debugHud;

    @Inject(method = "method_55807", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;shouldShowDebugHud()Z"))
    private void renderReboundlessDebug(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        if (debugHud.shouldShowDebugHud()) return;
        drawText(context, CURRENTLY_HELD_KEYS.stream().map(InputUtil.Key::getLocalizedText).map(Text::getString).toList(), true);
        drawText(context, ReBindings.all().stream().filter(ReBinding::isPressed).map(reBinding -> Text.translatable(reBinding.name()).getString() + " " + ((KeyBindingAccessor)reBinding.keybinding()).reboundless$getTimesPressed()).toList(), false);
    }

    @Unique
    private void drawText(DrawContext context, List<String> text, boolean left) {
        int i = 9;

        for (int j = 0; j < text.size(); j++) {
            String string = text.get(j);
            if (!Strings.isNullOrEmpty(string)) {
                int k = getTextRenderer().getWidth(string);
                int l = left ? 2 : context.getScaledWindowWidth() - 2 - k;
                int m = 2 + i * j;
                context.fill(l - 1, m - 1, l + k + 1, m + i - 1, -1873784752);
            }
        }

        for (int jx = 0; jx < text.size(); jx++) {
            String string = text.get(jx);
            if (!Strings.isNullOrEmpty(string)) {
                int k = getTextRenderer().getWidth(string);
                int l = left ? 2 : context.getScaledWindowWidth() - 2 - k;
                int m = 2 + i * jx;
                context.drawText(getTextRenderer(), string, l, m, 14737632, false);
            }
        }
    }
}
