package com.chyzman.reboundless.screen.widget;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.CategoryMode;
import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.api.SortingMode;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
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
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.screen.Screen.FOOTER_SEPARATOR_TEXTURE;

public class ReBoundlessWidget extends StatefulWidget {
    private final MinecraftClient client;
    private final GameOptions options;

    public ReBoundlessWidget(MinecraftClient client, GameOptions options) {
        this.client = client;
        this.options = options;
        ReBindings.updateInitialProperties();
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class ReBindingScreenState extends SharableState {
        public @Nullable Object selected = null;
        public @Nullable BooleanConsumer updateSelected = null;
    }

    public static class State extends WidgetState<ReBoundlessWidget> {
        CategoryMode categoryMode = CategoryMode.MOD;
        SortingMode sortingMode = SortingMode.ALPHABETICAL;

        @Override
        public Widget build(BuildContext context) {
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
                                new Panel(widget().client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE)
                            ),
                            new Flexible(
                                new Align(
                                    Alignment.CENTER,
                                    new VerticallyScrollable(
                                        new Row(
                                            new Padding(50),
                                            new Flexible(
                                                0.6,
                                                new Padding(
                                                    Insets.all(3).withTop(0),
                                                    new Column(
                                                        ReBindings.allReBindings().stream()
                                                            .collect(Collectors.groupingBy(rebind -> categoryMode.category.apply(rebind)))
                                                            .entrySet().stream()
                                                            .sorted(Map.Entry.comparingByKey(categoryMode.comparator))
                                                            .map(stringListEntry -> new Column(
                                                                new Label(LabelStyle.SHADOW, false, categoryMode.label.apply(stringListEntry.getKey())),
                                                                new Column(
                                                                    stringListEntry.getValue().stream()
                                                                        .sorted(sortingMode.comparator)
                                                                        .map(ReBinding::createConfigWidget)
                                                                        .toList()
                                                                )
                                                            )).toList()
                                                    )
                                                )
                                            ),
                                            new Padding(50)
                                        )
                                    )
                                )
                            ),
                            new Sized(
                                null, 2d,
                                new Panel(widget().client.world == null ? FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE)
                            ),
                            new Padding(
                                Insets.all(5),
                                new Sized(
                                    null, 45d,
                                    new Padding(
                                        Insets.horizontal(50),
                                        new Button(ScreenTexts.DONE, () -> MinecraftClient.getInstance().currentScreen.close())
                                    )
                                )
                            )
                        ),
                        new InputConsumer(
                            key -> {
                                var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                                if (sharedState.selected == null || sharedState.updateSelected == null) return false;
                                sharedState.updateSelected.accept(false);
                                return true;
                            },
                            key -> {
                                if (Reboundless.CURRENTLY_HELD_KEYS.isEmpty()) return false;
                                var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                                if (sharedState.selected == null || sharedState.updateSelected == null) return false;
                                sharedState.updateSelected.accept(true);
                                ReBindings.reCache();
                                SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                    reBindingScreenState.selected = null;
                                    reBindingScreenState.updateSelected = null;
                                });
                                return true;
                            }
                        )
                    )
                )
            );
        }
    }
}
