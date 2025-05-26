package com.chyzman.reboundless.api.binding;

import com.chyzman.reboundless.api.ReBinding;
import io.wispforest.endec.Endec;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import static com.chyzman.reboundless.api.binding.BindableType.BINDING_TYPE_REGISTRY;

public abstract class Bindable {
    public final BindableType<?> type;

    public static final Endec<Bindable> ENDEC = Endec.dispatchedStruct(
        type -> type.endec,
        bindable -> bindable.type,
        MinecraftEndecs.ofRegistry(BINDING_TYPE_REGISTRY)
    );

    public Bindable(BindableType<?> type) {
        this.type = type;
    }

    public abstract Text getName();

    public abstract @Nullable String getCategoryKey();

    public abstract void setPressed(ReBinding reBinding, boolean pressed, int power);

    public void reset() {}

    public ReBinding.Properties generateProperties() {
        return new ReBinding.Properties()
            .binding(this);
    }

    public abstract Widget createWidget();
}
