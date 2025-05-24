package com.chyzman.reboundless;

import com.chyzman.reboundless.registry.BindingRegistry;
import com.chyzman.reboundless.registry.ConditionRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.chyzman.reboundless.InputHandler.CURRENTLY_HELD_KEYS;

public class Reboundless implements ClientModInitializer {
    public static final String MODID = "reboundless";

    public static final String UNKNOWN_CATEGORY = "key.categories.unknown";
    public static final String MACRO_CATEGORY = "key.categories.macro";

    public static final InputUtil.Key SCROLL_UP = InputUtil.Type.MOUSE.createFromCode(100);
    public static final InputUtil.Key SCROLL_DOWN = InputUtil.Type.MOUSE.createFromCode(101);
    public static final InputUtil.Key SCROLL_LEFT = InputUtil.Type.MOUSE.createFromCode(102);
    public static final InputUtil.Key SCROLL_RIGHT = InputUtil.Type.MOUSE.createFromCode(103);

    public static final List<InputUtil.Key> SCROLL_KEYS = List.of(SCROLL_UP, SCROLL_DOWN, SCROLL_LEFT, SCROLL_RIGHT);

    //TODO: decide if still needed
    public static final Pattern TRADITIONAL_KEYBIND_KEY_PATTERN = Pattern.compile("key\\.(.+)\\.");

    //TODO: decide if still needed
    public static Map<String, String> MOD_NAME_MAP = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ConditionRegistry.init();
        BindingRegistry.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SCROLL_KEYS.forEach(key -> {
                KeyBinding.setKeyPressed(key, false);
                CURRENTLY_HELD_KEYS.remove(key);
            });
        });

        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            MOD_NAME_MAP.put(modContainer.getMetadata().getId(), modContainer.getMetadata().getName());
        }
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}
