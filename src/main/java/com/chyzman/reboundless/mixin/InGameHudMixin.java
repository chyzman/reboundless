package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.api.ReCombination;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.google.common.base.Strings;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
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

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow @Final private DebugHud debugHud;

    @Inject(method = "method_55807", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;shouldShowDebugHud()Z"))
    private void renderReboundlessDebug(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        if (debugHud.shouldShowDebugHud()) return;
        var text = new ArrayList<String>();
        text.add("Reboundless Debug:");
        text.add("Pressed Keys (In Order):");
        text.add(CURRENTLY_HELD_KEYS.stream()
                     .map(InputUtil.Key::getLocalizedText)
                     .map(Text::getString)
                     .toList() + "");
        text.add("Active KeyBindings (In no particular order):");
        text.add(Arrays.stream(MinecraftClient.getInstance().options.allKeys)
                     .filter(KeyBinding::isPressed)
                     .map(bind -> Text.translatable(bind.getTranslationKey()).getString() + " " + ((KeyBindingAccessor) bind).reboundless$getTimesPressed())
                     .toList() + "");
        text.add("Active ReBindings (In no particular order):");
        text.add(ReBindings.allReBindings().stream()
                     .filter(ReBinding::isPressed)
                     .map(ReBinding::getDisplayedName)
                     .toList() + "");
        text.add("Active ReCombos (In no particular order):");
        text.add(ReBindings.RECOMBINATIONS.stream()
                     .filter(ReCombination::isPressed)
                     .map(ReCombination::getBoundText)
                     .map(Text::getString)
                     .toList() + "");
        drawText(context, text, true);
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
