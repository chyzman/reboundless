package com.chyzman.reboundless;

import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import com.google.gson.Gson;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.List;

public class ReboundlessConfig {
    public static final List<ReBinding> REBINDINGS = new ArrayList<>();

    public static final List<KeyBinding> UNBOUND_KEYBINDINGS = new ArrayList<>();

    //region ENDEC

    public static final StructEndec<Void> ENDEC = StructEndecBuilder.of(
        EndecUtil.optionalFieldOfEmptyCheck(
            "binds", ReBinding.ENDEC.listOf(),
            o -> REBINDINGS, List::of
        ),
        EndecUtil.optionalFieldOfEmptyCheck(
            "unbound", ReboundlessEndecs.KEYBINDING.listOf(),
            o -> UNBOUND_KEYBINDINGS, List::of
        ),
        (reBindingList, keyBindings) -> {
            REBINDINGS.clear();
            REBINDINGS.addAll(reBindingList);
            UNBOUND_KEYBINDINGS.clear();
            UNBOUND_KEYBINDINGS.addAll(keyBindings);
            return null;
        }
    );

    //endregion

    public static void load(GameOptions.Visitor visitor, Gson gson, KeyBinding[] allKeys) {

    }
}
