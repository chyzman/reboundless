package com.chyzman.reboundless.util;

import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;

public class KeyUtil {
    public static final Text BOUND_SEPARATOR = Text.literal(" + ");

    public static boolean isValid(InputUtil.Key key) {
        if (key == null) return false;
        if (key == InputUtil.UNKNOWN_KEY) return false;
        return true;
    }

    public static List<Text> getKeyTexts(Collection<InputUtil.Key> keys) {
        return keys.stream()
            .map(InputUtil.Key::getLocalizedText)
            .toList();
    }

    public static Text boundText(Collection<InputUtil.Key> keys) {
        if (keys.isEmpty()) return InputUtil.UNKNOWN_KEY.getLocalizedText();
        return Texts.join(getKeyTexts(keys), BOUND_SEPARATOR);
    }
}
