package com.chyzman.reboundless.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonSerializer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.chyzman.reboundless.Reboundless.MODID;

public class ReBindings {
    public static final List<ReBinding> REBINDINGS = new ArrayList<>();
    public static final Multimap<KeyBinding, ReBinding> KEYBINDING_TO_REBINDING = HashMultimap.create();
    public static final Multimap<InputUtil.Key, ReBinding> KEY_TO_REBINDING = HashMultimap.create();

    public static final List<KeyBinding> INTENTIONALLY_UNBOUND_KEYBINDINGS = new ArrayList<>();

    //region ENDEC STUFF

    private static final Endec<List<ReBinding>> REBBINGING_LIST_ENDEC = ReBinding.ENDEC.listOf();

    private static final Endec<List<KeyBinding>> KEYBINDING_LIST_ENDEC = Endec.STRING.xmap(KeyBinding::byId, KeyBinding::getTranslationKey).listOf();

    //endregion

    public static List<ReBinding> all() {
        return REBINDINGS;
    }

    public static List<ReBinding> byKeyBinding(KeyBinding keyBinding) {
        return new ArrayList<>(KEYBINDING_TO_REBINDING.get(keyBinding));
    }

    public static List<ReBinding> byKey(InputUtil.Key key) {
        return new ArrayList<>(KEY_TO_REBINDING.get(key));
    }

    public static List<KeyBinding> unboundKeys() {
        return INTENTIONALLY_UNBOUND_KEYBINDINGS;
    }

    public static void refresh() {
        KEYBINDING_TO_REBINDING.clear();
        KEY_TO_REBINDING.clear();
        for (ReBinding reBinding : REBINDINGS) {
            if (reBinding.keybinding() != null) {
                KEYBINDING_TO_REBINDING.put(reBinding.keybinding(), reBinding);
            }
            KEY_TO_REBINDING.put(reBinding.key(), reBinding);
            for (InputUtil.Key key : reBinding.modifiers()) {
                KEY_TO_REBINDING.put(key, reBinding);
            }
        }
    }

    public static void load(GameOptions.Visitor visitor, Gson gson, KeyBinding[] allKeys) {
        var rebindings = visitor.visitObject(
            MODID + ".rebindings",
            List.copyOf(ReBindings.all()),
            string -> REBBINGING_LIST_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            reBindingList -> REBBINGING_LIST_ENDEC.encodeFully(GsonSerializer::of, reBindingList).toString()
        );
        ReBindings.all().clear();
        ReBindings.all().addAll(rebindings);
        var unbound = visitor.visitObject(
            MODID + ".unbound",
            List.copyOf(ReBindings.unboundKeys()),
            string -> KEYBINDING_LIST_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            keyBindingsList -> KEYBINDING_LIST_ENDEC.encodeFully(GsonSerializer::of, keyBindingsList).toString()
        );
        var usedKeyBindings = ReBindings.all().stream().map(ReBinding::keybinding).toList();
        var unused = Arrays.stream(allKeys).filter(keyBinding -> !usedKeyBindings.contains(keyBinding) && !unbound.contains(keyBinding)).toList();
        for (KeyBinding keyBinding : unused) {
            ReBindings.all().add(ReBinding.fromKeyBinding(keyBinding));
        }
        refresh();
    }
}
