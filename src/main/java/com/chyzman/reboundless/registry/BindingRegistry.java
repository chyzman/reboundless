package com.chyzman.reboundless.registry;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.binding.BindableType;
import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.api.binding.impl.MacroBinding;

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
