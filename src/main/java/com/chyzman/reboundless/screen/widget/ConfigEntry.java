package com.chyzman.reboundless.screen.widget;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.text.Text;

public class ConfigEntry extends StatefulWidget {
    private final Text label;
    private final Widget widget;

    public ConfigEntry(Text label, Widget widget) {
        this.label = label;
        this.widget = widget;
    }

    @Override
    public WidgetState<ConfigEntry> createState() {
        return new State();
    }

    public static class State extends WidgetState<ConfigEntry> {
        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                null, 20,
                new Row(
                    new Label(widget().label),
                    widget().widget
                )
            );
        }
    }
}
