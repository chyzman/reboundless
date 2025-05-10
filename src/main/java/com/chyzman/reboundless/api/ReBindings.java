package com.chyzman.reboundless.api;

import com.chyzman.reboundless.util.ReboundlessEndecs;
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

import java.util.*;

import static com.chyzman.reboundless.Reboundless.MODID;

public class ReBindings {
    public static final List<ReBinding> REBINDINGS = new ArrayList<>();

    public static final List<ReCombination> RECOMBINATIONS = new ArrayList<>();

    public static final Multimap<InputUtil.Key, ReBinding> KEY_TO_REBINDING = HashMultimap.create();

    public static final Multimap<InputUtil.Key, ReCombination> KEY_TO_RECOMBINATION = HashMultimap.create();

    public static final Map<ReCombination, ReBinding> RECOMBINATION_TO_REBINDING = new HashMap<>();

    public static final Multimap<KeyBinding, ReBinding> KEYBINDING_TO_REBINDING = HashMultimap.create();

    public static final List<KeyBinding> INTENTIONALLY_UNBOUND_KEYBINDINGS = new ArrayList<>();

    //region ENDEC STUFF

    private static final Endec<List<ReBinding>> REBINGING_LIST_ENDEC = ReBinding.ENDEC.listOf();

    private static final Endec<List<KeyBinding>> KEYBINDING_LIST_ENDEC = ReboundlessEndecs.KEYBINDING.listOf();

    //endregion

    public static List<ReBinding> allReBindings() {
        return REBINDINGS;
    }

    public static List<ReBinding> reBindingsByKey(InputUtil.Key key) {
        return new ArrayList<>(KEY_TO_REBINDING.get(key));
    }

    public static List<ReCombination> combinationsByKey(InputUtil.Key key) {
        return new ArrayList<>(KEY_TO_RECOMBINATION.get(key));
    }

    public static ReBinding reBindingByCombination(ReCombination reCombination) {
        return RECOMBINATION_TO_REBINDING.get(reCombination);
    }

    public static List<ReBinding> reBindingsByKeyBinding(KeyBinding keyBinding) {
        return new ArrayList<>(KEYBINDING_TO_REBINDING.get(keyBinding));
    }

    public static List<KeyBinding> unboundKeys() {
        return INTENTIONALLY_UNBOUND_KEYBINDINGS;
    }

    public static void reCache() {
        RECOMBINATIONS.clear();
        KEY_TO_REBINDING.clear();
        KEYBINDING_TO_REBINDING.clear();
        KEY_TO_RECOMBINATION.clear();
        RECOMBINATION_TO_REBINDING.clear();
        for (ReBinding reBinding : REBINDINGS) {
            for (ReCombination reCombo : reBinding.combinations()) {
                RECOMBINATIONS.add(reCombo);
                KEY_TO_REBINDING.put(reCombo.key(), reBinding);
                KEY_TO_RECOMBINATION.put(reCombo.key(), reCombo);;
                for (InputUtil.Key key : reCombo.modifiers()) {
                    KEY_TO_REBINDING.put(key, reBinding);
                    KEY_TO_RECOMBINATION.put(key, reCombo);
                }
                RECOMBINATION_TO_REBINDING.put(reCombo, reBinding);
            }
            if (reBinding.keybinding() != null) KEYBINDING_TO_REBINDING.put(reBinding.keybinding(), reBinding);
        }
    }

    public static void load(GameOptions.Visitor visitor, Gson gson, KeyBinding[] allKeys) {
        var rebindings = visitor.visitObject(
            MODID + ".rebindings",
            List.copyOf(allReBindings()),
            string -> REBINGING_LIST_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            reBindingList -> REBINGING_LIST_ENDEC.encodeFully(GsonSerializer::of, reBindingList).toString()
        );
        allReBindings().clear();
        allReBindings().addAll(rebindings);
        var unbound = visitor.visitObject(
            MODID + ".unbound",
            List.copyOf(unboundKeys()),
            string -> KEYBINDING_LIST_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            keyBindingsList -> KEYBINDING_LIST_ENDEC.encodeFully(GsonSerializer::of, keyBindingsList).toString()
        );
        var usedKeyBindings = allReBindings().stream().map(ReBinding::keybinding).toList();
        var unused = Arrays.stream(allKeys).filter(keyBinding -> !usedKeyBindings.contains(keyBinding) && !unbound.contains(keyBinding)).toList();
        for (KeyBinding keyBinding : unused) {
            allReBindings().add(ReBinding.fromKeyBinding(keyBinding));
        }
        reCache();
    }
}
