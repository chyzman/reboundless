package com.chyzman.reboundless.screen.widget;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.sharedstate.SharableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.screen.Screen.FOOTER_SEPARATOR_TEXTURE;

public class ReBoundlessWidget extends StatelessWidget {
    private final MinecraftClient client;
    private final GameOptions options;

    public ReBoundlessWidget(MinecraftClient client, GameOptions options) {
        this.client = client;
        this.options = options;
    }

    public static class ReBindingScreenState extends SharableState {
        public @Nullable ReBinding selectedRebind = null;
        public @Nullable Runnable updateSelected = null;
    }

    @Override
    public Widget build(BuildContext buildContext) {
        return new SharedState<>(
            ReBindingScreenState::new,
            new Builder(
                ctx -> new Stack(
                    new Column(
                        new Sized(
                            null, 31d,
                            new Align(
                                Alignment.CENTER,
                                new Label(LabelStyle.SHADOW, false, Text.translatable("controls.keybinds.title"))
                            )
                        ),
                        new Sized(
                            null, 2d,
                            new Panel(client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE)
                        ),
                        new Flexible(
                            new Align(
                                Alignment.CENTER,
                                new Row(
                                    new Flexible(0.2, new Label(Text.literal("FUCKING WORK"))),
                                    new Flexible(
                                        0.6,
                                        new VerticallyScrollable(
                                            new Column(
                                                ReBindings.allReBindings().stream().map(reBinding -> (Widget) new ReBindConfigurationWidget(reBinding)).toList()
                                            )
                                        )
                                    ),
                                    new Flexible(0.2, new Label(Text.literal("FUCKING WORK")))
                                )
                            )
                        ),
                        new Sized(
                            null, 2d,
                            new Panel(this.client.world == null ? FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE)
                        ),
                        new Sized(
                            null, 45d,
                            new Row(
                                new Button(ScreenTexts.DONE, () -> MinecraftClient.getInstance().currentScreen.close())
                            )
                        )
                    ),
                    new InputConsumer(
                        key -> {
                            var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                            if (sharedState.selectedRebind == null || sharedState.updateSelected == null) return false;
                            sharedState.updateSelected.run();
                            return true;
                        },
                        key -> {
                            if (Reboundless.CURRENTLY_HELD_KEYS.isEmpty()) return false;
                            var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                            if (sharedState.selectedRebind == null || sharedState.updateSelected == null) return false;
                            var rebinding = sharedState.selectedRebind;
//                            var currentlyHeldKeys = new ArrayList<>(Reboundless.CURRENTLY_HELD_KEYS).reversed();
//                            rebinding.key(currentlyHeldKeys.getFirst());
//                            currentlyHeldKeys.removeFirst();
//                            rebinding.modifiers().clear();
//                            currentlyHeldKeys.forEach(rebinding.modifiers()::add);
                            ReBindings.reCache();
                            sharedState.updateSelected.run();
                            SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                reBindingScreenState.selectedRebind = null;
                                reBindingScreenState.updateSelected = null;
                            });
                            return true;
                        }
                    )
                )
            )
        );
    }

    public static class ReBindConfigurationWidget extends StatefulWidget {
        final ReBinding rebinding;

        public ReBindConfigurationWidget(ReBinding rebinding) {
            this.rebinding = rebinding;
        }

        @Override
        public WidgetState<?> createState() {
            return new State();
        }

        public class State extends WidgetState<ReBindConfigurationWidget> {
            private boolean expanded = false;

            @Override
            public Widget build(BuildContext buildContext) {
                var rebinding = this.widget().rebinding;
                List<Widget> columnContents = new ArrayList<>();
                columnContents.add(
                    new Sized(
                        null, 20,
                        new Row(
                            new Padding(Insets.right(3)),
                            List.of(
                                new Flexible(
                                    new Align(
                                        Alignment.LEFT,
                                        new Label(
                                            LabelStyle.SHADOW,
                                            false,
                                            Text.translatable(rebinding.keybinding().getTranslationKey())
                                        )
                                    )
                                ),
                                new Constrain(
                                    Constraints.ofMinWidth(75),
                                    new ReBindButton(rebinding)
                                ),
                                new Row(
                                    new Padding(Insets.right(1)),
                                    List.of(
                                        new Sized(
                                            20, 20,
                                            new ConfirmingButton(
                                                Text.translatable("controls.reboundless.keybinds.keybind.reset"),
                                                Text.translatable("controls.reboundless.keybinds.keybind.reset.tooltip"),
                                                Text.translatable("controls.reboundless.keybinds.keybind.reset.tooltip.confirm"),
                                                () -> {
                                                    var player = MinecraftClient.getInstance().player;
                                                    if (player != null) player.sendMessage(Text.literal("pretend it was reset here"), false);
                                                }
                                            )
                                        ),
                                        new Sized(
                                            20, 20,
                                            new ConfirmingButton(
                                                Text.translatable("controls.reboundless.keybinds.keybind.clear"),
                                                Text.translatable("controls.reboundless.keybinds.keybind.clear.tooltip"),
                                                Text.translatable("controls.reboundless.keybinds.keybind.clear.tooltip.confirm"),
                                                () -> {
                                                    var player = MinecraftClient.getInstance().player;
                                                    if (player != null) player.sendMessage(Text.literal("pretend it was cleared here"), false);
                                                }
                                            )
                                        ),
                                        new Sized(
                                            20, 20,
                                            new Button(
                                                Text.translatable("controls.reboundless.keybinds.keybind.settings"),
                                                () -> this.setState(() -> this.expanded = !this.expanded)
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                );
                if (expanded) {
                    columnContents.add(
                        new Padding(
                            Insets.left(10),
                            new Column(
                                new Row(
                                    new Flexible(
                                        new ToggleButton(
                                            Text.translatable("controls.reboundless.keybinds.keybind.sticky", Text.translatable("options.key.toggle")),
                                            Text.translatable("controls.reboundless.keybinds.keybind.sticky", Text.translatable("options.key.hold")),
//                                            rebinding.sticky(),
//                                            enabled -> this.setState(() -> rebinding.sticky(enabled))
                                            false,
                                            enabled -> this.setState(() -> {})
                                        )
                                    ),
                                    new Flexible(
                                        new ToggleButton(
                                            Text.translatable("controls.reboundless.keybinds.keybind.inverted", Text.translatable("options.true")),
                                            Text.translatable("controls.reboundless.keybinds.keybind.inverted", Text.translatable("options.false")),
//                                            rebinding.inverted(),
//                                            enabled -> this.setState(() -> rebinding.inverted(enabled))
                                            false,
                                            enabled -> this.setState(() -> {})
                                        )
                                    )
                                )
                            )
                        )
                    );
                }
                return new Column(columnContents);
            }
        }

        public static class ReBindButton extends StatefulWidget {
            private final ReBinding rebinding;

            public ReBindButton(ReBinding rebinding) {
                this.rebinding = rebinding;
            }

            @Override
            public WidgetState<?> createState() {
                return new State();
            }

            public static class State extends WidgetState<ReBindButton> {
                @Override
                public Widget build(BuildContext ctx) {
                    var state = SharedState.get(ctx, ReBindingScreenState.class);
                    var rebinding = this.widget().rebinding;
                    return new Column(
                        new Sized(
                            null, 20,
                            new Button(
//                                state.selectedRebind == rebinding ? rebinding.getEditingText() : rebinding.getBoundText(),
                                Text.literal("broken atm"),
                                state.selectedRebind == null || state.selectedRebind == rebinding ? () -> SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                    Reboundless.CURRENTLY_HELD_KEYS.clear();
                                    reBindingScreenState.selectedRebind = rebinding;
                                    reBindingScreenState.updateSelected = () -> this.setState(() -> {});
                                }) : null
                            )
                        )
                    );
                }
            }
        }

        public class ConfirmingButton extends StatefulWidget {
            public final Text label;
            public final Text confirmLabel;
            public final Text tooltip;
            public final Text confirmTooltip;

            public final Runnable onClick;

            public ConfirmingButton(Text label, Text confirmLabel, Text tooltip, Text confirmTooltip, Runnable onClick) {
                this.label = label;
                this.confirmLabel = confirmLabel;
                this.tooltip = tooltip;
                this.confirmTooltip = confirmTooltip;
                this.onClick = onClick;
            }

            public ConfirmingButton(Text label, Text tooltip, Text confirmTooltip, Runnable onClick) {
                this(label, label.copy().formatted(Formatting.RED), tooltip, confirmTooltip, onClick);
            }


            @Override
            public WidgetState<ConfirmingButton> createState() {
                return new State();
            }

            public static class State extends WidgetState<ConfirmingButton> {
                private boolean confirming = false;

                @Override
                public Widget build(BuildContext context) {
                    return new MouseArea(
                        widget -> widget.exitCallback(() -> this.setState(() -> this.confirming = false)),
                        new Tooltip(
                            this.confirming ? this.widget().confirmTooltip : this.widget().tooltip,
                            new Button(
                                this.confirming ? this.widget().confirmLabel : this.widget().label,
                                () -> {
                                    if (this.confirming) this.widget().onClick.run();
                                    this.setState(() -> this.confirming = !this.confirming);
                                }
                            )
                        )
                    );
                }
            }
        }
    }
}
