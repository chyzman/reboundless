package com.chyzman.reboundless.screen.widget;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.UISounds;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends StatefulWidget {
    public final Text enabledText;
    public final Text disabledText;
    public final boolean enabled;
    public final @Nullable BooleanConsumer onClick;

    public ToggleButton(Text enabledText, Text disabledText, boolean enabled, @Nullable BooleanConsumer onClick) {
        this.enabledText = enabledText;
        this.disabledText = disabledText;
        this.enabled = enabled;
        this.onClick = onClick;
    }

    @Override
    public WidgetState<ToggleButton> createState() {
        return new State();
    }

    public static class State extends WidgetState<ToggleButton> {
        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            boolean active = (this.widget()).onClick != null;
            return new MouseArea((widget) -> {
                widget.clickCallback((x, y, button) -> {
                        if (active) {
                            if (button == 0) {
                                this.widget().onClick.accept(!this.widget().enabled);
                                UISounds.playButtonSound();
                            }
                        }
                    })
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false))
                    .cursorStyle(active ? CursorStyle.HAND : null);
            }, new Panel(
                active ? (this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE) : ButtonComponent.DISABLED_TEXTURE,
                new Padding(
                    Insets.all(5.0),
                    new Label(
                        active ?
                            LabelStyle.SHADOW :
                            new LabelStyle(
                                null,
                                Color.ofFormatting(Formatting.GRAY),
                                null,
                                false
                            ),
                        true,
                        this.widget().enabled ? this.widget().enabledText : this.widget().disabledText
                    )
                )
            ));
        }
    }
}
