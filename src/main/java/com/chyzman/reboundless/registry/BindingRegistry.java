package com.chyzman.reboundless.registry;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.binding.BindableType;
import com.chyzman.reboundless.binding.KeyBindBinding;
import com.chyzman.reboundless.binding.MacroBinding;
import io.wispforest.endec.Endec;
import net.minecraft.client.option.KeyBinding;

public class BindingRegistry {

    public static final BindableType<KeyBindBinding> KEYBINDING = BindableType.register(
        Reboundless.id("keybind"),
        KeyBindBinding.ENDEC
    );

    public static final BindableType<MacroBinding> MACRO = BindableType.register(
        Reboundless.id("macro"),
        MacroBinding.ENDEC
    );

    public static void init() {
    }
}
