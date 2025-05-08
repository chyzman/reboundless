package com.chyzman.reboundless.screen;

import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ReBindingScreen extends BraidScreen {
    @Nullable
    private Screen parent;
    private GameOptions gameOptions;

    public ReBindingScreen(@Nullable Screen parent, GameOptions gameOptions, Widget rootWidget) {
        super(rootWidget);
        this.parent = parent;
        this.gameOptions = gameOptions;
    }

    @Override
    public void removed() {
        gameOptions.write();
    }

    @Override
    public void close() {
        if (this.client == null) return;
        this.client.setScreen(this.parent);
    }
}
