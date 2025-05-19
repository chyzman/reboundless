package com.chyzman.reboundless.screen.widget;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
