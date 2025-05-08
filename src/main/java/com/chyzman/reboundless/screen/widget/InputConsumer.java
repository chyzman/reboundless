package com.chyzman.reboundless.screen.widget;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.KeyboardListener;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.Nullable;

public class InputConsumer extends LeafInstanceWidget {
    @Nullable private final InputStartedCallback startedCallback;
    @Nullable private final InputFinishedCallback finishedCallback;

    public InputConsumer(
        @Nullable InputStartedCallback startedCallback,
        @Nullable InputFinishedCallback finishedCallback
    ) {
        this.startedCallback = startedCallback;
        this.finishedCallback = finishedCallback;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public boolean onStarted(InputUtil.Key key) {
        if (this.startedCallback != null) return this.startedCallback.onStarted(key);
        return false;
    }

    public boolean onFinished(InputUtil.Key key) {
        if (this.finishedCallback != null) return this.finishedCallback.onFinished(key);
        return false;
    }

    @FunctionalInterface
    public interface InputStartedCallback {
        boolean onStarted(InputUtil.Key key);
    }

    @FunctionalInterface
    public interface InputFinishedCallback {
        boolean onFinished(InputUtil.Key key);
    }

    public static class Instance extends LeafWidgetInstance<InputConsumer> implements KeyboardListener, MouseListener {
        public Instance(InputConsumer widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.transform.setSize(constraints.maxSize());
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {}

        @Override
        public boolean onKeyDown(int keyCode, int modifiers) {
            if (this.widget.startedCallback == null) return false;
            return this.widget.onStarted(InputUtil.Type.KEYSYM.createFromCode(keyCode));
        }

        @Override
        public boolean onKeyUp(int keyCode, int modifiers) {
            if (this.widget.finishedCallback == null) return false;
            return this.widget.onFinished(InputUtil.Type.KEYSYM.createFromCode(keyCode));
        }

        @Override
        public boolean onMouseDown(double x, double y, int button) {
            if (this.widget.startedCallback == null) return false;
            return this.widget.onStarted(InputUtil.Type.MOUSE.createFromCode(button));
        }

        @Override
        public boolean onMouseUp(double x, double y, int button) {
            if (this.widget.finishedCallback == null) return false;
            return this.widget.onFinished(InputUtil.Type.MOUSE.createFromCode(button));
        }
    }
}
