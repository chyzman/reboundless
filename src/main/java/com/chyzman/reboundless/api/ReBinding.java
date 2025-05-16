package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.access.StickyKeyBindingAccessor;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.screen.widget.ReBoundlessWidget;
import com.chyzman.reboundless.screen.widget.ToggleButton;
import com.chyzman.reboundless.util.EndecUtil;
import com.chyzman.reboundless.util.KeyUtil;
import com.chyzman.reboundless.util.ReboundlessEndecs;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.TagParser;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
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
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ReBinding {

    public final Properties properties;

    private final Properties initialProperties;

    private boolean active;

    private long lastUpdated;

    private int presses;

    //region ENDEC STUFF

    public static final Endec<ReBinding> ENDEC = Properties.ENDEC.xmap(ReBinding::new, bind -> bind.properties);

    //endregion

    //region CONSTRUCTORS

    private ReBinding(
        Properties properties,
        Properties initialProperties
    ) {
        this.properties = properties;
        this.initialProperties = initialProperties;

        this.active = false;
        this.lastUpdated = System.currentTimeMillis();
        this.presses = 0;
    }

    public ReBinding(Properties properties) {
        this(properties, new Properties(properties));
    }

    public ReBinding(
        String name,
        String category,
        List<InputUtil.Key> keys,
        boolean ordered,
        Set<InputUtil.Key> exceptions,
        boolean isWhitelist,
        boolean sticky,
        boolean inverted,
        int debounce,
        int pressesRequired,
        int pressPower,
        @Nullable KeyBinding keybinding
    ) {
        this(new Properties(
            name,
            category,
            keys,
            ordered,
            exceptions,
            isWhitelist,
            sticky,
            inverted,
            debounce,
            pressesRequired,
            pressPower,
            keybinding
        ));
    }

    public ReBinding(KeyBinding key) {
        this(Properties.fromKeyBinding(key));
    }

    //endregion

    public void onKey(InputUtil.Key key, boolean pressed) {
        if (!Objects.equals(properties.key(), key)) return;

        var now = System.currentTimeMillis();

        if (properties.debounce > 0) {
            if (now - lastUpdated > properties.debounce) return;
        }

        if (pressed && !properties.keysMatch(Reboundless.CURRENTLY_HELD_KEYS) || !properties.exceptionsMatch(Reboundless.CURRENTLY_HELD_KEYS)) return;

        if (setPressed(pressed) && properties.keybinding != null) {
            ((KeyBindingDuck) properties.keybinding).reboundless$setPressed(this, pressed, properties.pressPower);
        }
    }

    public boolean isPressed() {
        return active != properties.inverted;
    }

    public boolean setPressed(boolean pressed) {
        var updated = false;
        if (properties.sticky) {
            if (pressed) {
                this.active = !this.active;
                updated = true;
            }
        } else {
            updated = pressed != this.active;
            this.active = pressed;
        }
        return updated;
    }

    public void updateState() {
        if (properties.keybinding != null) ((KeyBindingDuck) properties.keybinding).reboundless$updateState();
    }

    public void resetState() {
        active = false;
        presses = 0;
        lastUpdated = System.currentTimeMillis();
        updateState();
    }

    public void updateInitialProperties() {
        initialProperties.apply(properties);
    }

    public boolean isInitial() {
        return properties.equals(initialProperties);
    }

    public void revertToInitial() {
        properties.apply(initialProperties);
        updateState();
    }

    public boolean isDefault() {
        return properties.equals(Properties.fromKeyBinding(properties.keybinding));
    }

    public void revertToDefault() {
        properties.apply(Properties.fromKeyBinding(properties.keybinding));
        updateState();
    }

    public Text getDisplayedName() {
        return properties.name.isBlank() && properties.keybinding != null ? Text.translatable(properties.keybinding.getTranslationKey()) : TagParser.DEFAULT_SAFE.parseNode(properties.name).toText();
    }

    public ConfigWidget createConfigWidget() {
        return new ConfigWidget();
    }

    public static class Properties {

        public static final Properties EMPTY = new Properties(
            "",
            "",
            List.of(),
            false,
            Set.of(),
            false,
            false,
            false,
            0,
            1,
            1,
            null
        );

        //region PROPERTIES

        private String name;

        private String category;

        private final ArrayList<InputUtil.Key> keys;
        private boolean ordered;

        private final HashSet<InputUtil.Key> exceptions;
        private boolean isWhitelist;

        private boolean sticky;
        private boolean inverted;

        private int debounce;

        //TODO: implement globally stored timeBetweenPresses for pressesRequired
        private int pressesRequired;

        private int pressPower;

        @Nullable
        private KeyBinding keybinding;

        //endregion

        //region ENDEC STUFF

        public static final Endec<Properties> ENDEC = StructEndecBuilder.of(
            EndecUtil.optionalFieldOfEmptyCheck("name", Endec.STRING, o -> o.name, () -> EMPTY.name),
            EndecUtil.optionalFieldOfEmptyCheck("category", Endec.STRING, o -> o.category, () -> EMPTY.category),
            EndecUtil.optionalFieldOfEmptyCheck("keys", ReboundlessEndecs.KEY.listOf(), o -> o.keys, () -> new ArrayList<>(EMPTY.keys)),
            EndecUtil.optionalFieldOfEmptyCheck("ordered", Endec.BOOLEAN, o -> o.ordered, () -> EMPTY.ordered),
            EndecUtil.optionalFieldOfEmptyCheck("exceptions", ReboundlessEndecs.KEY.setOf(), o -> o.exceptions, () -> new HashSet<>(EMPTY.exceptions)),
            EndecUtil.optionalFieldOfEmptyCheck("whitelist", Endec.BOOLEAN, o -> o.isWhitelist, () -> EMPTY.isWhitelist),
            EndecUtil.optionalFieldOfEmptyCheck("sticky", Endec.BOOLEAN, o -> o.sticky, () -> EMPTY.sticky),
            EndecUtil.optionalFieldOfEmptyCheck("inverted", Endec.BOOLEAN, o -> o.inverted, () -> EMPTY.inverted),
            EndecUtil.optionalFieldOfEmptyCheck("debounce", Endec.INT, o -> o.debounce, () -> EMPTY.debounce),
            EndecUtil.optionalFieldOfEmptyCheck("pressesRequired", Endec.INT, o -> o.pressesRequired, () -> EMPTY.pressesRequired),
            EndecUtil.optionalFieldOfEmptyCheck("pressPower", Endec.INT, o -> o.pressPower, () -> EMPTY.pressPower),
            EndecUtil.optionalFieldOfEmptyCheck("keybinding", ReboundlessEndecs.KEYBINDING, o -> o.keybinding, () -> EMPTY.keybinding),
            Properties::new
        );

        //endregion

        //region CONSTRUCTORS

        public Properties(
            String name,
            String category,
            Collection<InputUtil.Key> keys,
            boolean ordered,
            Set<InputUtil.Key> exceptions,
            boolean isWhitelist,
            boolean sticky,
            boolean inverted,
            int debounce,
            int pressesRequired,
            int pressPower,
            @Nullable KeyBinding keybinding
        ) {
            this.name = name;
            this.category = category;
            this.keys = new ArrayList<>(keys);
            this.ordered = ordered;
            this.exceptions = new HashSet<>(exceptions);
            this.isWhitelist = isWhitelist;
            this.sticky = sticky;
            this.inverted = inverted;
            this.debounce = debounce;
            this.pressesRequired = pressesRequired;
            this.pressPower = pressPower;
            this.keybinding = keybinding;
            this.sanitizeKeys();
            this.sanitizeExceptions();
        }

        public Properties(Properties properties) {
            this(
                properties.name,
                properties.category,
                new ArrayList<>(properties.keys),
                properties.ordered,
                new HashSet<>(properties.exceptions),
                properties.isWhitelist,
                properties.sticky,
                properties.inverted,
                properties.debounce,
                properties.pressesRequired,
                properties.pressPower,
                properties.keybinding
            );
        }

        public static Properties fromKeyBinding(KeyBinding keyBinding) {
            return new Properties(
                "",
                keyBinding.getCategory(),
                new ArrayList<>(List.of(keyBinding.getDefaultKey())),
                EMPTY.ordered,
                new HashSet<>(EMPTY.exceptions),
                EMPTY.isWhitelist,
                keyBinding instanceof StickyKeyBinding sticky && ((StickyKeyBindingAccessor) sticky).reboundless$getBooleanSupplier().getAsBoolean(),
                EMPTY.inverted,
                EMPTY.debounce,
                EMPTY.pressesRequired,
                EMPTY.pressPower,
                keyBinding
            );
        }

        public void apply(Properties that) {
            this.name = that.name;
            this.category = that.category;
            this.replaceKeys(that.keys);
            this.ordered = that.ordered;
            this.exceptions.clear();
            this.exceptions.addAll(that.exceptions);
            this.isWhitelist = that.isWhitelist;
            this.sticky = that.sticky;
            this.inverted = that.inverted;
            this.debounce = that.debounce;
            this.pressesRequired = that.pressesRequired;
            this.pressPower = that.pressPower;
            this.keybinding = that.keybinding;
        }

        //endregion

        //region GETTERS AND SETTERS

        //name

        public String name() {
            return name;
        }

        public Properties name(String name) {
            this.name = name;
            return this;
        }

        //category

        public String category() {
            return category;
        }

        public Properties category(String category) {
            this.category = category;
            return this;
        }

        //key

        public @Nullable InputUtil.Key key() {
            return keys.isEmpty() ? null : keys.getLast();
        }

        public List<InputUtil.Key> modifiers() {
            return new ArrayList<>(keys).subList(0, keys.size() - 1);
        }

        public List<InputUtil.Key> relevantKeys() {
            return new ArrayList<>(keys);
        }

        public Properties replaceKeys(Collection<InputUtil.Key> keys) {
            this.keys.clear();
            this.keys.addAll(keys);
            sanitizeKeys();
            sanitizeExceptions();
            return this;
        }

        public void sanitizeKeys() {
            var sanitized = this.keys.reversed().stream().distinct().filter(KeyUtil::isValid).toList().reversed();
            this.keys.clear();
            this.keys.addAll(sanitized);
        }

        //ordered

        public boolean ordered() {
            return ordered;
        }

        public Properties ordered(boolean ordered) {
            this.ordered = ordered;
            return this;
        }

        //exceptions

        public Set<InputUtil.Key> exceptions() {
            return new HashSet<>(exceptions);
        }

        public boolean addException(InputUtil.Key exception) {
            this.exceptions.add(exception);
            sanitizeExceptions();
            return this.exceptions.contains(exception);
        }

        public boolean removeException(InputUtil.Key exception) {
            return this.exceptions.remove(exception);
        }

        public Properties replaceExceptions(Collection<InputUtil.Key> exceptions) {
            this.exceptions.clear();
            this.exceptions.addAll(exceptions);
            sanitizeExceptions();
            return this;
        }

        public void sanitizeExceptions() {
            this.exceptions.removeIf(exception -> !KeyUtil.isValid(exception) || relevantKeys().contains(exception));
        }

        //isWhitelist

        public boolean isWhitelist() {
            return isWhitelist;
        }

        public Properties isWhitelist(boolean isWhitelist) {
            this.isWhitelist = isWhitelist;
            return this;
        }

        //sticky

        public boolean sticky() {
            return sticky;
        }

        public Properties sticky(boolean sticky) {
            this.sticky = sticky;
            return this;
        }

        //inverted

        public boolean inverted() {
            return inverted;
        }

        public Properties inverted(boolean inverted) {
            this.inverted = inverted;
            return this;
        }

        //debounce

        public int debounce() {
            return debounce;
        }

        public Properties debounce(int debounce) {
            this.debounce = debounce;
            return this;
        }

        //pressesRequired

        public int pressesRequired() {
            return pressesRequired;
        }

        public Properties pressesRequired(int pressesRequired) {
            this.pressesRequired = pressesRequired;
            return this;
        }

        //pressPower

        public int pressPower() {
            return pressPower;
        }

        public Properties pressPower(int pressPower) {
            this.pressPower = pressPower;
            return this;
        }

        //keybinding

        public KeyBinding keybinding() {
            return keybinding;
        }

        public Properties keybinding(KeyBinding keybinding) {
            this.keybinding = keybinding;
            return this;
        }

        //endregion

        @SuppressWarnings("SpellCheckingInspection")
        public boolean unpressable() {
            if (this.keybinding == null) return true;
            if (!KeyUtil.isValid(this.key())) return true;
            return false;
        }

        public boolean keysMatch(List<InputUtil.Key> keys) {
            if (keys.isEmpty()) return true;
            if (keys.size() < relevantKeys().size()) return false;
            if (ordered) {
                var important = new ArrayList<>(keys);
                important.retainAll(relevantKeys());
                return important.equals(relevantKeys());
            }
            return new HashSet<>(keys).containsAll(keys);
        }

        public boolean exceptionsMatch(List<InputUtil.Key> keys) {
            if (!isWhitelist && exceptions.isEmpty()) return true;

            var stripped = new HashSet<>(keys);
            stripped.removeIf(relevantKeys()::contains);

            if (isWhitelist) {
                return exceptions.containsAll(stripped);
            } else {
                return exceptions.stream().noneMatch(stripped::contains);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (!(o instanceof Properties that)) return false;
            return ordered == that.ordered &&
                   isWhitelist == that.isWhitelist &&
                   sticky == that.sticky &&
                   inverted == that.inverted &&
                   debounce == that.debounce &&
                   pressesRequired == that.pressesRequired &&
                   pressPower == that.pressPower &&
                   Objects.equals(name, that.name) &&
                   Objects.equals(keys, that.keys) &&
                   Objects.equals(exceptions, that.exceptions) &&
                   Objects.equals(keybinding, that.keybinding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                name,
                keys,
                ordered,
                exceptions,
                isWhitelist,
                sticky,
                inverted,
                debounce,
                pressesRequired,
                pressPower,
                keybinding
            );
        }
    }

    public class ConfigWidget extends StatefulWidget {
        @Override
        public WidgetState<?> createState() {
            return new ConfigState();
        }

        public class ConfigState extends WidgetState<ConfigWidget> {
            private TextEditingController textInputController;
            private boolean expanded = false;

            private Multimap<Overlap, ReBinding> overlaps;

            @Override
            public void init() {
                this.textInputController = new TextEditingController(properties.name);
                textInputController.addListener(() -> setState(() -> properties.name = textInputController.text()));

                this.overlaps = Overlap.findOverlaps(ReBinding.this);
            }

            @Override
            public Widget build(BuildContext buildContext) {
                List<Widget> columnContents = new ArrayList<>();
                columnContents.add(
                    new Sized(
                        null, 20,
                        new Row(
                            new Padding(3),
                            List.of(
                                new Flexible(
                                    new Align(
                                        Alignment.LEFT,
                                        expanded ?
                                            new TextBox(
                                                textInputController,
                                                false,
                                                false
                                            ) :
                                            new Padding(
                                                Insets.all(3).withTop(4),
                                                new Label(
                                                    LabelStyle.SHADOW,
                                                    false,
                                                    getDisplayedName()
                                                )
                                            )
                                    )
                                ),
                                new OverlapsWidget(),
                                new Constrain(
                                    Constraints.ofMinWidth(75),
                                    new ReBindButton()
                                ),
                                new Row(
                                    new Padding(1),
                                    List.of(
                                        new Sized(
                                            20, 20,
                                            new ConfirmingButton(
                                                "controls.reboundless.keybind.revert",
                                                isInitial() ? null : () -> setState(ReBinding.this::revertToInitial)
                                            )
                                        ),
                                        new Sized(
                                            20, 20,
                                            new ConfirmingButton(
                                                "controls.reboundless.keybind.reset",
                                                isDefault() ? null : () -> setState(ReBinding.this::revertToDefault)

                                            )
                                        ),
                                        new Sized(
                                            20, 20,
                                            new Button(
                                                Text.translatable("controls.reboundless.keybind.settings"),
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
                                            Text.translatable("controls.reboundless.keybind.sticky", Text.translatable("options.key.toggle")),
                                            Text.translatable("controls.reboundless.keybind.sticky", Text.translatable("options.key.hold")),
                                            properties.sticky,
                                            enabled -> this.setState(() -> properties.sticky = enabled)
                                        )
                                    ),
                                    new Flexible(
                                        new ToggleButton(
                                            Text.translatable("controls.reboundless.keybind.inverted", Text.translatable("options.true")),
                                            Text.translatable("controls.reboundless.keybind.inverted", Text.translatable("options.false")),
                                            properties.inverted,
                                            enabled -> this.setState(() -> properties.inverted = enabled)
                                        )
                                    )
                                )
                            )
                        )
                    );
                }
                return new Column(columnContents);
            }

            public class OverlapsWidget extends StatelessWidget {
                @Override
                public Widget build(BuildContext context) {
                    return new Row(
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
                                                .map(ReBinding::getDisplayedName)
                                                .toList(),
                                            Text.literal("\n")
                                        )
                                    ),
                                    new Sized(
                                        3, 18,
                                        new Box(Color.ofRgb(overlap.color))
                                    )
                                )
                            )).toList()
                    );
                }
            }

            public enum Overlap {
                GUARANTEED(DyeColor.RED.getFireworkColor()),
                LIKELY(DyeColor.ORANGE.getFireworkColor()),
                POSSIBLE(DyeColor.YELLOW.getFireworkColor()),
                IMPOSSIBLE(DyeColor.LIME.getFireworkColor());

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
                        if (!a.exceptionsMatch(b.relevantKeys()) || !b.exceptionsMatch(a.relevantKeys())) {
                            overlaps.put(IMPOSSIBLE, other);
                        }
                        //possibly garunteed logic
//                        if (!a.key.equals(b.key)) return false;
//                        var larger = a.modifiers.size() > b.modifiers.size() ? a : b;
//                        var smaller = larger == a ? b : a;

                    }
                    return overlaps;
                }
            }
        }

        public class ReBindButton extends StatefulWidget {

            @Override
            public WidgetState<?> createState() {
                return new State();
            }

            public class State extends WidgetState<ReBindButton> {
                @Override
                public Widget build(BuildContext ctx) {
                    var state = SharedState.get(ctx, ReBoundlessWidget.ReBindingScreenState.class);
                    return new Column(
                        new Sized(
                            null, 20,
                            new Button(
                                getLabel(state.selected == ReBinding.this),
                                state.selected == null || state.selected == ReBinding.this ? () -> SharedState.set(ctx, ReBoundlessWidget.ReBindingScreenState.class, reBindingScreenState -> {
                                    Reboundless.CURRENTLY_HELD_KEYS.clear();
                                    reBindingScreenState.selected = ReBinding.this;
                                    reBindingScreenState.updateSelected = b -> {
                                        if (b) properties.replaceKeys(Reboundless.CURRENTLY_HELD_KEYS);
                                        this.setState(() -> {});
                                    };
                                }) : null
                            )
                        )
                    );
                }
            }

            private Text getLabel(boolean editing) {
                var text = Text.empty();
                var useHeld = editing && !Reboundless.CURRENTLY_HELD_KEYS.isEmpty();

                text.append(Text.literal("> "));

                var center = Text.empty().append(KeyUtil.boundText(useHeld ? Reboundless.CURRENTLY_HELD_KEYS : properties.relevantKeys()));
                if (useHeld) center.append(KeyUtil.BOUND_SEPARATOR).append(Text.literal("..."));

                text.append(center.copy().formatted(Formatting.WHITE, Formatting.UNDERLINE));

                text.append(Text.literal(" <"));

                return editing ? text.formatted(Formatting.YELLOW) : center;
            }
        }

        public static class ConfirmingButton extends StatefulWidget {
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

            public ConfirmingButton(String key, Runnable onClick) {
                this(Text.translatable(key), Text.translatable(key + ".tooltip"), Text.translatable(key + ".tooltip.confirm"), onClick);
            }


            @Override
            public WidgetState<ConfirmingButton> createState() {
                return new State();
            }

            public static class State extends WidgetState<ConfirmingButton> {
                private boolean confirming = false;

                @Override
                public Widget build(BuildContext context) {
                    Widget button = new Button(
                        this.confirming ? this.widget().confirmLabel : this.widget().label,
                        this.widget().onClick == null ? null : () -> {
                            if (this.confirming) this.widget().onClick.run();
                            this.setState(() -> this.confirming = !this.confirming);
                        }
                    );
                    if (this.widget().onClick != null) button = new Tooltip(this.confirming ? this.widget().confirmTooltip : this.widget().tooltip, button);
                    return new MouseArea(widget -> widget.exitCallback(() -> this.setState(() -> this.confirming = false)), button);
                }
            }
        }
    }
}
