package com.chyzman.reboundless.screen.widget;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.api.CategoryMode;
import com.chyzman.reboundless.api.ReBinding;
import com.chyzman.reboundless.api.ReBindings;
import com.chyzman.reboundless.api.SortingMode;
import com.chyzman.reboundless.util.KeyUtil;
import com.google.common.collect.HashMultimap;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.Stack;
import io.wispforest.owo.braid.widgets.button.RawButton;
import io.wispforest.owo.braid.widgets.cycle.EnumCyclingButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.ui.core.Color;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static class ReBindingScreenState extends ShareableState {
        public @Nullable Object selected = null;
        public @Nullable BooleanConsumer updateSelected = null;

        public SortingMode sortingMode = SortingMode.VANILLA;
        public CategoryMode categoryMode = CategoryMode.VANILLA;

        private String search = "";
        private TextEditingController searchController = new TextEditingController(search);

        @Nullable
        private ReBinding overlapFilter = null;
        @Nullable
        private ReBindingConfig.Overlap overlapFilterType = null;

        public boolean searchActive() {
            return !search.isBlank();
        }

        public boolean matchesFilters(ReBinding binding) {
            if (overlapFilter != null) {
                if (overlapFilter == binding) return true;
                var overlaps = ReBindingConfig.Overlap.findOverlaps(overlapFilter);
                return overlapFilterType == null ? overlaps.containsValue(binding) : overlaps.get(overlapFilterType).contains(binding);
            }
            return true;
        }

        public boolean isFilterEmpty() {
            return overlapFilter == null && overlapFilterType == null;
        }
    }

    public static class State extends WidgetState<ReBoundlessWidget> {
        @Override
        public Widget build(BuildContext context) {
            return new SharedState<>(
                ReBindingScreenState::new,
                new Builder(
                    ctx -> {
                        var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                        sharedState.searchController.addListener(() -> SharedState.set(ctx, ReBindingScreenState.class, state -> state.search = sharedState.searchController.text()));
                        return new Stack(
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
                                    new TextureWidget(
                                        widget().client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE,
                                        0, 0,
                                        16, 2,
                                        16, 2,
                                        false
                                    )
                                ),
                                new Flexible(
                                    new Align(
                                        Alignment.CENTER,
                                        new TextureWidget(
                                            widget().client.world == null ? EntryListWidget.MENU_LIST_BACKGROUND_TEXTURE : EntryListWidget.INWORLD_MENU_LIST_BACKGROUND_TEXTURE,
                                            0, 0,
                                            16, 16,
                                            16, 16,
                                            false,
                                            new VerticallyScrollable(
                                                new Row(
                                                    sharedState.isFilterEmpty() ?
                                                        new Padding(50) :
                                                        new Sized(
                                                            50, null,
                                                            new Column(
                                                                new Padding(3),
                                                                Util.make(new ArrayList<>(), list -> {
                                                                    if (sharedState.overlapFilter != null) {
                                                                        list.add(new Button(
                                                                            sharedState.overlapFilter.getDisplayedName(),
                                                                            () -> SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                                                                reBindingScreenState.overlapFilter = null;
                                                                                reBindingScreenState.overlapFilterType = null;
                                                                            })
                                                                        ));
                                                                    }
                                                                    if (sharedState.overlapFilterType != null) {
                                                                        list.add(new Button(
                                                                                     Text.translatable("controls.reboundless.keybind.overlap." + sharedState.overlapFilterType.name().toLowerCase(Locale.ROOT)),
                                                                                     () -> SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> reBindingScreenState.overlapFilterType = null)
                                                                                 )
                                                                        );
                                                                    }
                                                                })
                                                            )
                                                        ),
                                                    new Flexible(
                                                        1,
                                                        new Padding(
                                                            Insets.all(3).withTop(0),
                                                            new ReBindingList()
                                                        )
                                                    ),
                                                    new Padding(50)
                                                )
                                            )
                                        )
                                    )
                                ),
                                new Sized(
                                    null, 2d,
                                    new TextureWidget(
                                        widget().client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE,
                                        0, 0,
                                        16, 2,
                                        16, 2,
                                        false
                                    )
                                ),
                                new Padding(
                                    Insets.all(5),
                                    new Sized(
                                        null, 45d,
                                        new Padding(
                                            Insets.horizontal(50),
                                            new Row(
                                                new Padding(5),
                                                List.of(
                                                    new Flexible(
                                                        6,
                                                        new Column(
                                                            new Padding(5),
                                                            List.of(
                                                                new Sized(
                                                                    null, 20,
                                                                    new Row(
                                                                        new Padding(5),
                                                                        List.of(
                                                                            new Flexible(
                                                                                sharedState.searchActive() ?
                                                                                    new Button(Text.translatable("controls.reboundless.keybinds.sortMode", Text.translatable("controls.reboundless.keybinds.sorting.none")), null) :
                                                                                    new EnumCyclingButton<>(
                                                                                        sharedState.sortingMode,
                                                                                        mode -> Text.translatable("controls.reboundless.keybinds.sortMode", Text.translatable("controls.reboundless.keybinds.sorting." + mode.toString().toLowerCase(Locale.ROOT))),
                                                                                        mode -> SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> reBindingScreenState.sortingMode = mode)
                                                                                    )
                                                                            ),
                                                                            new Flexible(
                                                                                sharedState.searchActive() ?
                                                                                    new Button(Text.translatable("controls.reboundless.keybinds.categoryMode", Text.translatable("controls.reboundless.keybinds.sorting.search")), null) :
                                                                                    new EnumCyclingButton<>(
                                                                                        sharedState.categoryMode,
                                                                                        mode -> Text.translatable("controls.reboundless.keybinds.categoryMode", Text.translatable("controls.reboundless.keybinds.sorting." + mode.toString().toLowerCase(Locale.ROOT))),
                                                                                        mode -> SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> reBindingScreenState.categoryMode = mode)
                                                                                    )
                                                                            )
                                                                        )
                                                                    )
                                                                ),
                                                                new Sized(
                                                                    null, 20,
                                                                    new TextBox(
                                                                        sharedState.searchController,
                                                                        false,
                                                                        false
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    ),
                                                    new Flexible(
                                                        4,
                                                        new Column(
                                                            new Padding(5),
                                                            List.of(
                                                                new Sized(
                                                                    null, 20,
                                                                    new Button(Text.literal("make this revert/reset later"), () -> MinecraftClient.getInstance().currentScreen.close())
                                                                ),
                                                                new Sized(
                                                                    null, 20,
                                                                    new Button(ScreenTexts.DONE, () -> MinecraftClient.getInstance().currentScreen.close())
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            new InputConsumer(
                                key -> {
                                    if (sharedState.selected == null || sharedState.updateSelected == null) return false;
                                    sharedState.updateSelected.accept(false);
                                    return true;
                                },
                                key -> {
                                    if (Reboundless.CURRENTLY_HELD_KEYS.isEmpty()) return false;
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
                        );
                    }
                )
            );
        }
    }

    public static class ReBindingList extends StatefulWidget {
        @Override
        public WidgetState<ReBindingList> createState() {
            return new State();
        }

        public static class State extends WidgetState<ReBindingList> {
            @Override
            public Widget build(BuildContext ctx) {
                var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                var bindings = ReBindings.allReBindings().stream().filter(sharedState::matchesFilters).toList();
                return sharedState.categoryMode.equals(CategoryMode.NONE) || sharedState.searchActive() ?
                    listReBindings(ctx, bindings) :
                    new Column(
                        bindings.stream()
                            .collect(Collectors.groupingBy(sharedState.categoryMode::getCategory))
                            .entrySet().stream()
                            .sorted(Map.Entry.comparingByKey(sharedState.categoryMode.getComparator()))
                            .map(stringListEntry -> new Column(
                                new Label(LabelStyle.SHADOW, false, sharedState.categoryMode.getLabel(stringListEntry.getKey())),
                                listReBindings(ctx, stringListEntry.getValue())
                            )).toList()
                    );
            }

            private Widget listReBindings(BuildContext ctx, List<ReBinding> reBindings) {
                var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                Stream<ReBinding> streamed;
                if (!sharedState.search.isBlank()) {
                    streamed = FuzzySearch.extractAll(sharedState.search, reBindings, bind -> bind.getDisplayedName().getString())
                        .stream()
                        .sorted(Comparator.reverseOrder())
                        .map(BoundExtractedResult::getReferent);
                } else {
                    streamed = reBindings.stream().sorted(sharedState.sortingMode.comparator);
                }
                return new Column(
                    streamed
                        .map(binding -> new ReBindingConfig(binding, () -> this.setState(() -> {})))
                        .toList()
                );
            }
        }
    }

    public static class ReBindingConfig extends StatefulWidget {
        private final ReBinding binding;
        private final Runnable onChanged;

        public ReBindingConfig(ReBinding binding, Runnable onChanged) {
            this.binding = binding;
            this.onChanged = onChanged;
        }

        @Override
        public WidgetState<?> createState() {
            return new ConfigState();
        }

        public static class ConfigState extends WidgetState<ReBindingConfig> {
            private TextEditingController textInputController;
            private boolean expanded = false;

            @Override
            public void init() {
                var binding = widget().binding;
                this.textInputController = new TextEditingController(binding.properties.name());
                textInputController.addListener(() -> setStateAndUpdate(() -> binding.properties.name(textInputController.text())));
            }

            @Override
            public Widget build(BuildContext ctx) {
                var binding = widget().binding;
                var properties = binding.properties;
                var overlaps = Overlap.findOverlaps(binding);
                List<Widget> columnContents = new ArrayList<>();
                var sharedState = SharedState.get(ctx, ReBindingScreenState.class);
                columnContents.add(
                    new Sized(
                        null, 20,
                        new Row(
                            new Padding(2),
                            List.of(
                                new Flexible(
                                    new Align(
                                        Alignment.LEFT,
                                        expanded ?
                                            new TextBox(textInputController, false, false) :
                                            new Padding(
                                                Insets.all(3).withTop(4),
                                                new Label(
                                                    LabelStyle.SHADOW,
                                                    false,
                                                    binding.getDisplayedName()
                                                )
                                            )
                                    )
                                ),
                                new MouseArea(
                                    widget -> {
                                        if (Objects.equals(sharedState.overlapFilter, binding)) return;
                                        widget.clickCallback((x, y, button) -> {
                                            if (button != 0) return;
                                            SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                                reBindingScreenState.overlapFilter = binding;
                                                reBindingScreenState.overlapFilterType = null;
                                            });
                                        });
                                        widget.cursorStyle(CursorStyle.HAND);
                                    },
                                    new Row(
                                        new Padding(Insets.right(3)),
                                        Arrays.stream(Overlap.values())
                                            .filter(overlap -> !overlaps.get(overlap).isEmpty())
                                            .map(overlap -> new Padding(
                                                Insets.vertical(1),
                                                new Tooltip(
                                                    Text.translatable(
                                                        "controls.reboundless.keybind.overlap." + overlap.name().toLowerCase(Locale.ROOT) + ".tooltip",
                                                        Texts.join(
                                                            overlaps.get(overlap).stream()
                                                                .filter(Objects::nonNull)
                                                                .sorted(SharedState.get(ctx, ReBoundlessWidget.ReBindingScreenState.class).sortingMode.comparator)
                                                                .map(ReBinding::getDisplayedName)
                                                                .toList(),
                                                            Text.literal("\n")
                                                        )
                                                    ),
                                                    new Sized(
                                                        3, 18,
                                                        new MouseArea(
                                                            widget -> {
                                                                if (!Objects.equals(sharedState.overlapFilter, binding)) return;
                                                                widget.clickCallback((x, y, button) -> {
                                                                    if (button != 0) return;
                                                                    SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> reBindingScreenState.overlapFilterType = overlap);
                                                                });
                                                                widget.cursorStyle(CursorStyle.HAND);
                                                            },
                                                            new Box(Color.ofRgb(overlap.color))
                                                        )
                                                    )
                                                )
                                            )).toList()
                                    )
                                ),
                                new Constrain(
                                    Constraints.ofMinWidth(75),
                                    new Column(
                                        new Sized(
                                            null, 20,
                                            new RawButton(
                                                getBindButtonLabel(sharedState.selected == binding),
                                                sharedState.selected == null || sharedState.selected == binding ? button -> {
                                                    if (button == 0) {
                                                        SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                                            Reboundless.CURRENTLY_HELD_KEYS.clear();
                                                            reBindingScreenState.selected = binding;
                                                            reBindingScreenState.updateSelected = b -> {
                                                                if (b) properties.replaceKeys(Reboundless.CURRENTLY_HELD_KEYS);
                                                                widget().onChanged.run();
                                                            };
                                                        });
                                                    } else if (button == 1) {
                                                        SharedState.set(ctx, ReBindingScreenState.class, reBindingScreenState -> {
                                                            properties.replaceKeys(List.of());
                                                            widget().onChanged.run();
                                                        });
                                                    }
                                                } : null
                                            )
                                        )
                                    )
                                ),
                                new Sized(
                                    20, 20,
                                    new ConfirmingButton(
                                        "controls.reboundless.keybind." + (binding.isInitial() ? "reset" : "revert"),
                                        binding.isInitial() ? (binding.isDefault() ? null : () -> setStateAndUpdate(binding::revertToDefault)) : () -> setStateAndUpdate(binding::revertToInitial)
                                    )
                                ),
                                new Sized(
                                    20, 20,
                                    new Button(
                                        Text.translatable("controls.reboundless.keybind.settings"),
                                        () -> this.setStateAndUpdate(() -> this.expanded = !this.expanded)
                                    )
                                )
                            )
                        )
                    )
                );
                if (expanded) {
                    columnContents.add(
                        new Padding(
                            Insets.vertical(3).withLeft(10),
                            new Column(
                                new Padding(2),
                                List.of(
                                    new ConfigEntry(
                                        Text.translatable("controls.reboundless.keybind.sticky"),
                                        new ToggleButton(
                                             Text.translatable("options.key.toggle"),
                                             Text.translatable("options.key.hold"),
                                            properties.sticky(),
                                            enabled -> this.setStateAndUpdate(() -> properties.sticky(enabled))
                                        )
                                    ),
                                    new ConfigEntry(
                                        Text.translatable("controls.reboundless.keybind.inverted"),
                                        new ToggleButton(
                                            Text.translatable("options.true"),
                                            Text.translatable("options.false"),
                                            properties.inverted(),
                                            enabled -> this.setStateAndUpdate(() -> properties.inverted(enabled))
                                        )
                                    ),
                                    properties.binding().createWidget()
                                )
                            )
                        )
                    );
                }
                return new Column(columnContents);
            }

            private void setStateAndUpdate(Runnable fn) {
                fn.run();
                widget().onChanged.run();
            }

            private Text getBindButtonLabel(boolean editing) {
                var text = Text.empty();
                var useHeld = editing && !Reboundless.CURRENTLY_HELD_KEYS.isEmpty();

                text.append(Text.literal("> "));

                var center = Text.empty().append(KeyUtil.boundText(useHeld ? Reboundless.CURRENTLY_HELD_KEYS : widget().binding.properties.relevantKeys()));
                if (useHeld) center.append(KeyUtil.BOUND_SEPARATOR).append(Text.literal("..."));

                text.append(center.copy().formatted(Formatting.WHITE, Formatting.UNDERLINE));

                text.append(Text.literal(" <"));

                return editing ? text.formatted(Formatting.YELLOW) : center;
            }
        }

        public enum Overlap {
            GUARANTEED(DyeColor.RED.getFireworkColor()),
            LIKELY(DyeColor.ORANGE.getFireworkColor()),
            POSSIBLE(DyeColor.YELLOW.getFireworkColor()),
            IMPOSSIBLE(DyeColor.LIME.getFireworkColor()),
            SAME_FUNCTION(DyeColor.BLUE.getFireworkColor());

            public final int color;

            Overlap(int color) {
                this.color = color;
            }

            public static HashMultimap<Overlap, ReBinding> findOverlaps(ReBinding bind) {
                var overlaps = HashMultimap.<Overlap, ReBinding>create();
                var a = bind.properties;
                for (ReBinding other : ReBindings.allReBindings()) {
                    if (other == bind) continue;
                    var b = other.properties;
                    if (a.unpressable() || b.unpressable()) continue;
                    if (a.binding() != null && a.binding().equals(b.binding())) overlaps.put(SAME_FUNCTION, other);
                    if (!a.exceptionsMatch(b.relevantKeys()) || !b.exceptionsMatch(a.relevantKeys())) {
                        overlaps.put(IMPOSSIBLE, other);
                        continue;
                    }
                    var guaranteed = a.relevantKeys().equals(b.relevantKeys());
                    if (guaranteed) {
                        overlaps.put(GUARANTEED, other);
                    } else if (a.keysMatch(b.relevantKeys()) || b.keysMatch(a.relevantKeys())) {
                        overlaps.put(POSSIBLE, other);
                    }
                }
                return overlaps;
            }
        }
    }
}
