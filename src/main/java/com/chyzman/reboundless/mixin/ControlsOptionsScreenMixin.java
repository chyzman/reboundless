package com.chyzman.reboundless.mixin;

import com.chyzman.reboundless.screen.ReBindingScreen;
import com.chyzman.reboundless.screen.widget.ReBoundlessWidget;
import com.chyzman.reboundless.util.ScreenModificationUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

@Mixin(ControlsOptionsScreen.class)
public abstract class ControlsOptionsScreenMixin extends GameOptionsScreen {

    public ControlsOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @ModifyArg(method = "method_60340", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private Screen replaceKeyBindsScreen(@Nullable Screen screen) {
            return new ReBindingScreen(this, this.gameOptions, new ReBoundlessWidget(this.client, this.gameOptions));
    }

    @Inject(method = "addOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/OptionListWidget;addWidgetEntry(Lnet/minecraft/client/gui/widget/ClickableWidget;Lnet/minecraft/client/gui/widget/ClickableWidget;)V", shift = At.Shift.AFTER))
    private void addRebindButton(CallbackInfo ci) {
        this.body.addAll(List.of(ButtonWidget.builder(Text.literal("vanilla controls"), buttonWidget -> this.client.setScreen(new KeybindsScreen(this, this.gameOptions))).build()));
    }

    @Inject(method = "getOptions", at = @At("RETURN"), cancellable = true, order = 0)
    private static void removeToggleKeyBindConfigs(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        try {
            cir.setReturnValue(ScreenModificationUtil.removeToggleButtonsFrom(
                0,
                Arrays.asList(cir.getReturnValue()).indexOf(gameOptions.getAutoJump()),
                cir.getReturnValue()
            ));
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to remove options from ControlsOptionsScreen", e);
        }
    }

}
