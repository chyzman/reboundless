package com.chyzman.reboundless.util;

import io.wispforest.endec.Endec;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class ReboundlessEndecs {

    public static final Endec<InputUtil.Key> KEY = Endec.STRING
        .xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey);

    public static final Endec<KeyBinding> KEYBINDING = Endec.STRING
        .xmap(KeyBinding::byId, KeyBinding::getTranslationKey);
}
