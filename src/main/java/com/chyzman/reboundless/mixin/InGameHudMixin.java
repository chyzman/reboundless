package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.InputHandler;
import com.chyzman.reboundless.ReboundlessConfig;
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
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow @Final private DebugHud debugHud;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "method_55807", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;shouldShowDebugHud()Z"))
    private void renderReboundlessDebug(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        if (debugHud.shouldShowDebugHud()) return;
        var text = new ArrayList<String>();
        text.add("Reboundless Debug:");
        text.add("Pressed Keys (In Order):");
        var pressedKeys = new ArrayList<>(InputHandler.CURRENTLY_HELD_KEYS);
        var keyGroups = pressedKeys.stream().collect(Collectors.groupingBy(key -> pressedKeys.indexOf(key) / MathHelper.ceil(pressedKeys.size() / 5d))).values();
        text.addAll(
            keyGroups.stream()
                .map(group -> group.stream()
                    .map(InputUtil.Key::getLocalizedText)
                    .reduce((thisText, thatText) -> thisText.copy().append(", ").append(thatText))
                    .orElse(Text.empty())
                    .getString())
                .toList());
        for (int i = 0; i < 5 - keyGroups.size(); i++) text.add("");
        text.add("");
        text.add("Active KeyBindings (In no particular order):");
        var pressedKeyBinds = Arrays.stream(client.options.allKeys).filter(KeyBinding::isPressed).toList();
        var keyBindGroups = pressedKeyBinds.stream().collect(Collectors.groupingBy(key -> pressedKeyBinds.indexOf(key) / MathHelper.ceil(pressedKeyBinds.size() / 5d))).values();
        text.addAll(
            keyBindGroups.stream()
                .map(group -> group.stream()
                    .map(bind -> Text.translatable(bind.getTranslationKey()).append(" " + ((KeyBindingAccessor) bind).reboundless$getTimesPressed()))
                    .reduce((thisText, thatText) -> thisText.append(", ").append(thatText))
                    .orElse(Text.empty())
                    .getString())
                .toList());
        for (int i = 0; i < 5 - keyBindGroups.size(); i++) text.add("");
        text.add("");
        text.add("Active ReBindings (In no particular order):");
        var pressedReBindings = ReboundlessConfig.REBINDINGS.stream().filter(binding -> binding.isPressed() || binding.nextStep != 0).toList();
        var reBindingGroups = pressedReBindings.stream().collect(Collectors.groupingBy(key -> pressedReBindings.indexOf(key) / MathHelper.ceil(pressedReBindings.size() / 5d))).values();
        text.addAll(
            reBindingGroups.stream()
                .map(group -> group.stream()
                    .map(binding -> Text.empty().append(binding.getDisplayedName()).append(" " + binding.nextStep))
                    .reduce((thisText, thatText) -> thisText.copy().append(", ").append(thatText))
                    .orElse(Text.empty())
                    .getString())
                .toList());
        for (int i = 0; i < 5 - reBindingGroups.size(); i++) text.add("");
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
