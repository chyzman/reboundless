package com.chyzman.reboundless.binding;

import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.mixin.access.KeyBindingAccessor;
import com.chyzman.reboundless.mixin.access.StickyKeyBindingAccessor;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.registry.BindingRegistry;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.cycle.CyclingButton;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.text.Text;

import java.util.List;

public class KeyBindBinding extends Bindable {
    public KeyBinding keyBinding;

    public static final StructEndec<KeyBindBinding> ENDEC = StructEndecBuilder.of(
        ReboundlessEndecs.KEYBINDING.fieldOf("key", keyBindBinding -> keyBindBinding.keyBinding),
        KeyBindBinding::new
    );

    public KeyBindBinding(KeyBinding keyBinding) {
        super(BindingRegistry.KEYBINDING);
        this.keyBinding = keyBinding;
    }

    @Override
    public Text getName() {
        return Text.translatable(keyBinding.getTranslationKey());
    }

    @Override
    public String getCategory() {
        return keyBinding.getCategory();
    }

    @Override
    public void setPressed(ReBinding reBinding, boolean pressed, int power) {
        ((KeyBindingDuck) keyBinding).reboundless$setPressed(reBinding, pressed, power);
    }

    @Override
    public void reset() {
        ((KeyBindingDuck) keyBinding).reboundless$resetState();
    }

    @Override
    public ReBinding.Properties generateProperties() {
        return super.generateProperties()
            .replaceKeys(List.of(keyBinding.getDefaultKey()))
            .sticky(keyBinding instanceof StickyKeyBinding sticky && ((StickyKeyBindingAccessor) sticky).reboundless$getBooleanSupplier().getAsBoolean());
    }

    @Override
    public Widget createWidget() {
        return new ConfigWidget(this);
    }

    public static class ConfigWidget extends StatefulWidget {
        private final KeyBindBinding binding;

        public ConfigWidget(KeyBindBinding binding) {
            this.binding = binding;
        }

        @Override
        public WidgetState<ConfigWidget> createState() {
            return new State();
        }

        public static class State extends WidgetState<ConfigWidget> {
            private KeyBindBinding binding;

            @Override
            public void init() {
                this.binding = widget().binding;
            }

            @Override
            public Widget build(BuildContext context) {
                return new CyclingButton<>(
                    this.binding.keyBinding,
                    KeyBindingAccessor.reboundless$getKeysById().values().stream().toList(),
                    binding -> Text.translatable(binding.getTranslationKey()),
                    bind -> this.setState(() -> this.binding.keyBinding = bind)
                );
            }
        }
    }
}
