package com.chyzman.reboundless.binding;

import com.chyzman.reboundless.api.ReBinding;
import io.wispforest.endec.Endec;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.text.Text;

import static com.chyzman.reboundless.binding.BindableType.BINDING_TYPE_REGISTRY;

public abstract class Bindable {
    private final BindableType<?> type;

    public static final Endec<Bindable> ENDEC = Endec.dispatchedStruct(
        type -> type.endec,
        bindable -> bindable.type,
        CodecUtils.toEndec(BINDING_TYPE_REGISTRY.getCodec())
    );

    public Bindable(BindableType<?> type) {
        this.type = type;
    }

    public abstract Text getName();

    public abstract String getCategory();

    public abstract void setPressed(ReBinding reBinding, boolean pressed, int power);

    public void reset() {}

    public ReBinding.Properties generateProperties() {
        return new ReBinding.Properties()
            .binding(this);
    }

    public abstract Widget createWidget();
}
