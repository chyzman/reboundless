package com.chyzman.reboundless.api.binding.impl;

import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.binding.Bindable;
import com.chyzman.reboundless.registry.BindingRegistry;
import com.chyzman.reboundless.util.EndecUtil;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.cycle.EnumCyclingButton;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;

public class MacroBinding extends Bindable {

    public String macro;
    public Type macroType;

    public static final StructEndec<MacroBinding> ENDEC = StructEndecBuilder.of(
        Endec.STRING.fieldOf("macro", binding -> binding.macro),
        EndecUtil.optionalFieldOfEmptyCheck("macroType", Endec.STRING.xmap(Type::valueOf, Enum::name), binding -> binding.macroType, () -> Type.COMMAND),
        MacroBinding::new
    );

    public MacroBinding(String macro, Type macroType) {
        super(BindingRegistry.MACRO);
        this.macro = macro;
        this.macroType = macroType;
    }

    @Override
    public Text getName() {
        return Text.translatable(this.type.translationKey + "." + this.macroType.name().toLowerCase(Locale.ROOT), macro);
    }

    @Override
    public void setPressed(ReBinding reBinding, boolean pressed, int power) {
        var player = MinecraftClient.getInstance().player;
        if (pressed && power > 0 && player != null) this.macroType.action.accept(MinecraftClient.getInstance(), this.macro);
    }

    @Override
    public Widget createWidget() {
        return new ConfigWidget(this);
    }

    public enum Type {
        COMMAND((client, macro) -> client.player.networkHandler.sendCommand(macro.startsWith("/") ? macro.substring(1) : macro)),
        MESSAGE((client, macro) -> client.player.networkHandler.sendChatMessage(macro)),
        CHAT((client, macro) -> {
            KeyBinding.unpressAll();
            client.setScreen(new ChatScreen(macro));
        });

        public final BiConsumer<MinecraftClient, String> action;

        Type(BiConsumer<MinecraftClient, String> action) {
            this.action = action;
        }

    }

    public static class ConfigWidget extends StatefulWidget {
        private final MacroBinding binding;

        public ConfigWidget(MacroBinding binding) {
            this.binding = binding;
        }

        @Override
        public WidgetState<MacroBinding.ConfigWidget> createState() {
            return new State();
        }

        public static class State extends WidgetState<MacroBinding.ConfigWidget> {
            private MacroBinding binding;
            private TextEditingController textInputController;

            @Override
            public void init() {
                this.binding = widget().binding;
                this.textInputController = new TextEditingController(this.binding.macro);
                textInputController.addListener(() -> setState(() -> this.binding.macro = textInputController.text()));
            }

            @Override
            public Widget build(BuildContext context) {
                return new Row(
                    new TextBox(
                        textInputController,
                        false,
                        false
                    ),
                    new EnumCyclingButton<>(
                        this.binding.macroType,
                        binding -> Text.translatable("binding.reboundless.macro.type." + binding.name().toLowerCase(Locale.ROOT)),
                        bind -> this.setState(() -> this.binding.macroType = bind)
                    )
                );
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MacroBinding that)) return false;
        return Objects.equals(macro, that.macro) && macroType == that.macroType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(macro, macroType);
    }
}
