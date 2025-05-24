package com.chyzman.reboundless.api;

import com.chyzman.reboundless.api.action.ActionStep;
import com.chyzman.reboundless.api.action.impl.KeyCondition;
import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.mixin.access.StickyKeyBindingAccessor;
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
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.*;

import static com.chyzman.reboundless.Reboundless.MODID;

public class ReBindings {
    public static final List<ReBinding> REBINDINGS = new ArrayList<>();

    public static final Multimap<InputUtil.Key, ReBinding> KEY_TO_REBINDING = HashMultimap.create();

    public static final Multimap<KeyBinding, ReBinding> KEYBINDING_TO_REBINDING = HashMultimap.create();

    public static final List<KeyBinding> INTENTIONALLY_UNBOUND_KEYBINDINGS = new ArrayList<>();

    //region ENDEC

    private static final Endec<List<ReBinding>> REBINDING_LIST_ENDEC = ReBinding.ENDEC.listOf();

    private static final Endec<List<KeyBinding>> KEYBINDING_LIST_ENDEC = ReboundlessEndecs.KEYBINDING.listOf();

    //endregion

    public static List<ReBinding> allReBindings() {
        return REBINDINGS;
    }

    public static List<ReBinding> reBindingsByKey(InputUtil.Key key) {
        return new ArrayList<>(KEY_TO_REBINDING.get(key));
    }

    public static List<ReBinding> reBindingsByKeyBinding(KeyBinding keyBinding) {
        return new ArrayList<>(KEYBINDING_TO_REBINDING.get(keyBinding));
    }

    public static List<KeyBinding> unboundKeys() {
        return INTENTIONALLY_UNBOUND_KEYBINDINGS;
    }

    public static void reCache() {
        KEY_TO_REBINDING.clear();
        KEYBINDING_TO_REBINDING.clear();
        for (ReBinding reBinding : REBINDINGS) {
//            for (InputUtil.Key key : reBinding.properties.relevantKeys()) KEY_TO_REBINDING.put(key, reBinding);
            if (reBinding.properties.binding() != null && reBinding.properties.binding() instanceof KeyBindBinding keyBindBinding) KEYBINDING_TO_REBINDING.put(keyBindBinding.keyBinding, reBinding);
        }
    }

    public static void load(GameOptions.Visitor visitor, Gson gson, KeyBinding[] allKeys) {
        var rebindings = visitor.visitObject(
            MODID + ".binds",
            List.copyOf(allReBindings()),
            string -> REBINDING_LIST_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            reBindingList -> REBINDING_LIST_ENDEC.encodeFully(GsonSerializer::of, reBindingList).toString()
        );
        allReBindings().clear();
        allReBindings().addAll(rebindings);
        var unbound = visitor.visitObject(
            MODID + ".unbound",
            List.copyOf(unboundKeys()),
            string -> KEYBINDING_LIST_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            keyBindingsList -> KEYBINDING_LIST_ENDEC.encodeFully(GsonSerializer::of, keyBindingsList).toString()
        );
        var usedKeyBindings = allReBindings().stream().map(binding -> binding.properties.binding()).filter(binding -> binding instanceof KeyBindBinding).map(bindable -> ((KeyBindBinding) bindable).keyBinding).toList();
        var unused = Arrays.stream(allKeys).filter(keyBinding -> !usedKeyBindings.contains(keyBinding) && !unbound.contains(keyBinding)).toList();
        for (KeyBinding keyBinding : unused) {
            var reBinding = new ReBinding(new KeyBindBinding(keyBinding).generateProperties());
            reBinding.properties
                .replaceActivationSteps(new ActionStep(new KeyCondition(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey())))
                .replaceDeactivationSteps(new ActionStep(new KeyCondition(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey(), keyBinding instanceof StickyKeyBinding sticky && ((StickyKeyBindingAccessor) sticky).reboundless$getBooleanSupplier().getAsBoolean())));
            allReBindings().add(reBinding);
        }
        reCache();
    }

    public static void stepAll() {
        REBINDINGS.forEach(ReBinding::step);
    }

    public static void updateInitialProperties() {
        for (ReBinding reBinding : REBINDINGS) {
            reBinding.updateInitialProperties();
        }
    }
}
