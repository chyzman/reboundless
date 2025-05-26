package com.chyzman.reboundless;

import com.chyzman.reboundless.api.GroupMode;
import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.SortMode;
import com.chyzman.reboundless.api.action.ActionStep;
import com.chyzman.reboundless.api.action.impl.KeyCondition;
import com.chyzman.reboundless.api.binding.impl.KeyBindBinding;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.mixin.access.StickyKeyBindingAccessor;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.ListUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonSerializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.chyzman.reboundless.Reboundless.MODID;
import static com.chyzman.reboundless.util.EndecUtil.*;

public class ReboundlessConfig {
    public static final List<ReBinding> REBINDINGS = new ArrayList<>();
    public static final Multimap<KeyBinding, ReBinding> KEYBINDING_TO_REBINDING = HashMultimap.create();
//    public static final Multimap<InputUtil.Key, ReBinding> KEY_TO_REBINDING = HashMultimap.create();

    public static final List<KeyBinding> UNBOUND_KEYBINDINGS = new ArrayList<>();

    public static ScreenState SCREEN_STATE = new ScreenState();

    //region ENDEC

    public static final Endec<List<ReBinding>> REBINDINGS_ENDEC = ReBinding.ENDEC.listOf();

    public static final Endec<List<KeyBinding>> UNBOUND_KEYBINDINGS_ENDEC = ReboundlessEndecs.KEYBINDING.listOf();

    //endregion

    //region SAVE/LOAD

    public static void load(GameOptions.Visitor visitor, Gson gson, KeyBinding[] allKeys) {
        ListUtil.replace(
            REBINDINGS,
            visitor.visitObject(
                MODID + ".binds",
                List.copyOf(REBINDINGS),
                string -> REBINDINGS_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
                reBindingList -> REBINDINGS_ENDEC.encodeFully(GsonSerializer::of, reBindingList).toString()
            )
        );
        ListUtil.replace(
            UNBOUND_KEYBINDINGS,
            visitor.visitObject(
                MODID + ".unbound_keys",
                List.copyOf(UNBOUND_KEYBINDINGS),
                string -> UNBOUND_KEYBINDINGS_ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
                keyBindingList -> UNBOUND_KEYBINDINGS_ENDEC.encodeFully(GsonSerializer::of, keyBindingList).toString()
            )
        );
        visitor.visitObject(
            MODID + ".screen_state",
            SCREEN_STATE,
            string -> ScreenState.ENDEC.decodeFully(GsonDeserializer::of, gson.fromJson(string, JsonElement.class)),
            screenState -> ScreenState.ENDEC.encodeFully(GsonSerializer::of, screenState).toString()
        );
        generateDefaultBindings(allKeys);
    }

    public static void generateDefaultBindings(KeyBinding[] allKeys) {
        var usedKeyBindings = REBINDINGS.stream().map(binding -> binding.properties.binding()).filter(binding -> binding instanceof KeyBindBinding).map(bindable -> ((KeyBindBinding) bindable).keyBinding).toList();
        var unused = Arrays.stream(allKeys).filter(keyBinding -> !usedKeyBindings.contains(keyBinding) && !UNBOUND_KEYBINDINGS.contains(keyBinding)).toList();
        for (KeyBinding keyBinding : unused) {
            var reBinding = new ReBinding(new KeyBindBinding(keyBinding).generateProperties());
            reBinding.properties
                .replaceActivationSteps(new ActionStep(new KeyCondition(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey())))
                .replaceDeactivationSteps(new ActionStep(new KeyCondition(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey(), keyBinding instanceof StickyKeyBinding sticky && ((StickyKeyBindingAccessor) sticky).reboundless$getBooleanSupplier().getAsBoolean())));
            REBINDINGS.add(reBinding);
        }
    }

    //endregion

    public static void reCache() {
        KEYBINDING_TO_REBINDING.clear();
//        KEY_TO_REBINDING.clear();
        for (ReBinding reBinding : REBINDINGS) {
            if (reBinding.properties.binding() != null && reBinding.properties.binding() instanceof KeyBindBinding keyBindBinding) {
                KEYBINDING_TO_REBINDING.put(keyBindBinding.keyBinding, reBinding);
            }
        }
    }

    public static class ScreenState {
        private static final ScreenState DEFAULT = new ScreenState(
            SortMode.VANILLA, false,
            GroupMode.CATEGORY, false
        );

        public SortMode sortMode;
        public boolean sortReversed;

        public GroupMode groupMode;
        public boolean groupReversed;


        public static final Endec<ScreenState> ENDEC = StructEndecBuilder.of(
            optionalFieldOfEmptyCheck(
                "sortMode", EndecUtil.enumEndec(SortMode.class, DEFAULT.sortMode),
                o -> o.sortMode, () -> DEFAULT.sortMode
            ),
            optionalFieldOfEmptyCheck(
                "sortReversed", Endec.BOOLEAN,
                o -> o.sortReversed, () -> DEFAULT.sortReversed
            ),
            optionalFieldOfEmptyCheck(
                "groupMode", EndecUtil.enumEndec(GroupMode.class, GroupMode.CATEGORY),
                o -> o.groupMode, () -> GroupMode.CATEGORY
            ),
            optionalFieldOfEmptyCheck(
                "groupReversed", Endec.BOOLEAN,
                o -> o.groupReversed, () -> DEFAULT.groupReversed
            ),
            ScreenState::new
        );

        public ScreenState(
            SortMode sortMode,
            boolean sortReversed,
            GroupMode groupMode,
            boolean groupReversed
        ) {
            this.sortMode = sortMode;
            this.sortReversed = sortReversed;
            this.groupMode = groupMode;
            this.groupReversed = groupReversed;
        }

        public ScreenState() {
            this.sortMode = DEFAULT.sortMode;
            this.sortReversed = DEFAULT.sortReversed;
            this.groupMode = DEFAULT.groupMode;
            this.groupReversed = DEFAULT.groupReversed;
        }
    }
}
