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
        InputUtil.Key key,
        List<InputUtil.Key> modifiers,
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
        this(new Properties(name, key, modifiers, ordered, exceptions, isWhitelist, sticky, inverted, debounce, pressesRequired, pressPower, keybinding));
    }

    public ReBinding(KeyBinding key) {
        this(Properties.fromKeyBinding(key));
    }

    //endregion

    public void onKey(InputUtil.Key key, boolean pressed) {
        if (!properties.key.equals(key)) return;

        var now = System.currentTimeMillis();

        if (properties.debounce > 0) {
            if (now - lastUpdated > properties.debounce) return;
        }

        var currentlyHeld = new ArrayList<>(Reboundless.CURRENTLY_HELD_KEYS);

        if (pressed) {
            if (!properties.modifiersMatch(currentlyHeld)) return;
            if (!properties.exceptionsMatch(currentlyHeld)) return;
        }

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
        return properties.name.isBlank() && properties.keybinding != null ? Text.translatable(properties.keybinding.getTranslationKey()) : Text.translatable(properties.name);
    }

    public Text getBoundText() {
        var text = Text.empty();
        if (!properties.modifiers.isEmpty()) {
            for (InputUtil.Key modifier : properties.modifiers.reversed()) {
                text.append(modifier.getLocalizedText());
                text.append(" + ");
            }
        }
        text.append(properties.key.getLocalizedText());
        return text;
    }

    public Text getEditingText() {
        var text = Text.empty();
        var useBinding = Reboundless.CURRENTLY_HELD_KEYS.isEmpty();

        text.append(Text.literal("> "));

        var center = Text.empty();
        if (useBinding) {
            center.append(getBoundText());
        } else {
            for (InputUtil.Key heldKey : Reboundless.CURRENTLY_HELD_KEYS) {
                center.append(heldKey.getLocalizedText())
                    .append(Text.literal(" + "));
            }
            center.append(Text.literal("..."));
        }
        text.append(center.formatted(Formatting.WHITE, Formatting.UNDERLINE));

        text.append(Text.literal(" <"));

        return text.formatted(Formatting.YELLOW);
    }

    public ConfigWidget createConfigWidget() {
        return new ConfigWidget();
    }

    public static class Properties {

        public static final Properties EMPTY = new Properties(
            "",
            InputUtil.UNKNOWN_KEY,
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

        private InputUtil.Key key;

        private final List<InputUtil.Key> modifiers;
        private boolean ordered;

        private final Set<InputUtil.Key> exceptions;
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
            EndecUtil.optionalFieldOfEmptyCheck("key", ReboundlessEndecs.KEY, o -> o.key, () -> EMPTY.key),
            EndecUtil.optionalFieldOfEmptyCheck("modifiers", ReboundlessEndecs.KEY.listOf(), o -> o.modifiers, () -> new ArrayList<>(EMPTY.modifiers)),
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
            InputUtil.Key key,
            List<InputUtil.Key> modifiers,
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
            this.key = key;
            this.modifiers = modifiers;
            this.ordered = ordered;
            this.exceptions = exceptions;
            this.isWhitelist = isWhitelist;
            this.sticky = sticky;
            this.inverted = inverted;
            this.debounce = debounce;
            this.pressesRequired = pressesRequired;
            this.pressPower = pressPower;
            this.keybinding = keybinding;
        }

        public Properties(Properties properties) {
            this(
                properties.name,
                properties.key,
                new ArrayList<>(properties.modifiers),
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
                keyBinding.getDefaultKey(),
                new ArrayList<>(EMPTY.modifiers),
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
            this.key = that.key;
            this.modifiers.clear();
            this.modifiers.addAll(that.modifiers);
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

        //key

        public InputUtil.Key key() {
            return key;
        }

        public Properties key(InputUtil.Key key) {
            this.key = key;
            sanitizeModifiers();
            sanitizeExceptions();
            return this;
        }

        //modifiers

        public List<InputUtil.Key> modifiers() {
            return new ArrayList<>(modifiers);
        }

        public boolean addModifier(InputUtil.Key modifier) {
            this.modifiers.add(modifier);
            sanitizeModifiers();
            sanitizeExceptions();
            return this.modifiers.contains(modifier);
        }

        public boolean removeModifier(InputUtil.Key modifier) {
            return this.modifiers.remove(modifier);
        }

        public Properties replaceModifiers(Collection<InputUtil.Key> modifiers) {
            this.modifiers.clear();
            this.modifiers.addAll(modifiers);
            sanitizeModifiers();
            return this;
        }

        public void sanitizeModifiers() {
            this.modifiers.removeIf(modifier -> !KeyUtil.isValid(modifier) || modifier.equals(key));
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
            this.exceptions.removeIf(exception -> !KeyUtil.isValid(exception) || getRelevantKeys().contains(exception));
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

        public List<InputUtil.Key> getRelevantKeys() {
            var keys = new ArrayList<>(this.modifiers);
            keys.add(this.key);
            return keys;
        }

        public boolean canBePressed() {
            if (this.key == InputUtil.UNKNOWN_KEY) return false;
            if (this.keybinding == null) return false;
            return true;
        }

        public boolean modifiersMatch(List<InputUtil.Key> keys) {
            if (modifiers.isEmpty()) return true;
            if (keys.size() < modifiers.size()) return false;
            if (ordered) {
                var important = new ArrayList<>(keys);
                important.retainAll(modifiers);
                return important.equals(modifiers);
            }
            return new HashSet<>(keys).containsAll(modifiers);
        }

        public boolean exceptionsMatch(List<InputUtil.Key> keys) {
            if (!isWhitelist && exceptions.isEmpty()) return true;

            var stripped = new HashSet<>(keys);
            stripped.remove(key);
            modifiers.forEach(stripped::remove);

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
                   Objects.equals(key, that.key) &&
                   Objects.equals(modifiers, that.modifiers) &&
                   Objects.equals(exceptions, that.exceptions) &&
                   Objects.equals(keybinding, that.keybinding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                name,
                key,
                modifiers,
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
                            new Padding(Insets.right(3)),
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
                                    new Padding(Insets.right(1)),
                                    List.of(
                                        new Sized(
                                            20, 20,
                                            new ConfirmingButton(
                                                "controls.reboundless.keybinds.keybind.revert",
                                                isInitial() ? null : () -> setState(ReBinding.this::revertToInitial)
                                            )
                                        ),
                                        new Sized(
                                            20, 20,
                                            new ConfirmingButton(
                                                "controls.reboundless.keybinds.keybind.reset",
                                                isDefault() ? null : () -> setState(ReBinding.this::revertToDefault)

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
                                            properties.sticky,
                                            enabled -> this.setState(() -> properties.sticky = enabled)
                                        )
                                    ),
                                    new Flexible(
                                        new ToggleButton(
                                            Text.translatable("controls.reboundless.keybinds.keybind.inverted", Text.translatable("options.true")),
                                            Text.translatable("controls.reboundless.keybinds.keybind.inverted", Text.translatable("options.false")),
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
                            .map(overlap -> new OverLapWidget(overlap, overlaps.get(overlap).stream().toList())).toList()
                    );
                }

                public class OverLapWidget extends StatelessWidget {
                    private final Overlap overlap;
                    private final List<ReBinding> binds;

                    public OverLapWidget(Overlap overlap, List<ReBinding> binds) {
                        this.overlap = overlap;
                        this.binds = binds;
                    }

                    @Override
                    public Widget build(BuildContext context) {
                        return new Padding(
                            Insets.vertical(1),
                            new Tooltip(
                                Text.literal("placeholder for " + overlap.name() + " tooltip")
                                    .append("\n")
                                    .append(binds.stream().map(reBinding -> reBinding == null ? "null" : reBinding.getDisplayedName().getString()).toList().toString()),
                                new Sized(
                                    3, 18,
                                    new Box(Color.ofRgb(overlap.color))
                                )
                            )
                        );
                    }
                }
            }

            public enum Overlap {
                GUARANTEED(DyeColor.RED.getFireworkColor()),
                LIKELY(DyeColor.ORANGE.getFireworkColor()),
                POSSIBLE(DyeColor.YELLOW.getFireworkColor()),
                IMPOSSIBLE(DyeColor.LIME.getFireworkColor()),
                UNPRESSABLE(DyeColor.GRAY.getFireworkColor());

                public final int color;

                Overlap(int color) {
                    this.color = color;
                }

                public static HashMultimap<Overlap, ReBinding> findOverlaps(ReBinding bind) {
                    var overlaps = HashMultimap.<Overlap, ReBinding>create();
                    var a = bind.properties;
                    if (!a.canBePressed()) {
                        overlaps.put(UNPRESSABLE, null);
                        return overlaps;
                    }
                    var random = new Random();
                    for (ReBinding otherBind : ReBindings.allReBindings()) {
                        if (otherBind == bind) continue;
                        var b = otherBind.properties;
                        if (!b.canBePressed() || !b.canBePressed()) return overlaps;
                        if (!a.exceptionsMatch(b.getRelevantKeys()) || !b.exceptionsMatch(a.getRelevantKeys())) {
                            overlaps.put(IMPOSSIBLE, otherBind);
                            continue;
                        }
                        for (Overlap value : Overlap.values()) {
                            if (random.nextInt(10) == 0) {
                                overlaps.put(value, otherBind);
                            }
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
                                state.selected == ReBinding.this ? getEditingText() : getBoundText(),
                                state.selected == null || state.selected == ReBinding.this ? () -> SharedState.set(ctx, ReBoundlessWidget.ReBindingScreenState.class, reBindingScreenState -> {
                                    Reboundless.CURRENTLY_HELD_KEYS.clear();
                                    reBindingScreenState.selected = ReBinding.this;
                                    reBindingScreenState.updateSelected = b -> {
                                        if (b) {
                                            var currentlyHeldKeys = new ArrayList<>(Reboundless.CURRENTLY_HELD_KEYS).reversed();
                                            properties.key(currentlyHeldKeys.getFirst());
                                            currentlyHeldKeys.removeFirst();
                                            properties.replaceModifiers(currentlyHeldKeys);
                                        }
                                        this.setState(() -> {});
                                    };
                                }) : null
                            )
                        )
                    );
                }
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
