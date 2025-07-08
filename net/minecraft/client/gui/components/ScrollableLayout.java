/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;

public class ScrollableLayout
implements Layout {
    private static final int SCROLLBAR_SPACING = 4;
    private static final int SCROLLBAR_RESERVE = 10;
    final Layout content;
    private final Container container;
    private int minWidth;
    private int maxHeight;

    public ScrollableLayout(Minecraft minecraft, Layout layout, int n) {
        this.content = layout;
        this.container = new Container(minecraft, 0, n);
    }

    public void setMinWidth(int n) {
        this.minWidth = n;
        this.container.setWidth(Math.max(this.content.getWidth(), n));
    }

    public void setMaxHeight(int n) {
        this.maxHeight = n;
        this.container.setHeight(Math.min(this.content.getHeight(), n));
        this.container.refreshScrollAmount();
    }

    @Override
    public void arrangeElements() {
        this.content.arrangeElements();
        int n = this.content.getWidth();
        this.container.setWidth(Math.max(n + 20, this.minWidth));
        this.container.setHeight(Math.min(this.content.getHeight(), this.maxHeight));
        this.container.refreshScrollAmount();
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(this.container);
    }

    @Override
    public void setX(int n) {
        this.container.setX(n);
    }

    @Override
    public void setY(int n) {
        this.container.setY(n);
    }

    @Override
    public int getX() {
        return this.container.getX();
    }

    @Override
    public int getY() {
        return this.container.getY();
    }

    @Override
    public int getWidth() {
        return this.container.getWidth();
    }

    @Override
    public int getHeight() {
        return this.container.getHeight();
    }

    class Container
    extends AbstractContainerWidget {
        private final Minecraft minecraft;
        private final List<AbstractWidget> children;

        public Container(Minecraft minecraft, int n, int n2) {
            super(0, 0, n, n2, CommonComponents.EMPTY);
            this.children = new ArrayList<AbstractWidget>();
            this.minecraft = minecraft;
            ScrollableLayout.this.content.visitWidgets(this.children::add);
        }

        @Override
        protected int contentHeight() {
            return ScrollableLayout.this.content.getHeight();
        }

        @Override
        protected double scrollRate() {
            return 10.0;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            guiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
            for (AbstractWidget abstractWidget : this.children) {
                abstractWidget.render(guiGraphics, n, n2, f);
            }
            guiGraphics.disableScissor();
            this.renderScrollbar(guiGraphics);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
            return new ScreenRectangle(this.getX(), this.getY(), this.width, this.contentHeight());
        }

        @Override
        public void setFocused(@Nullable GuiEventListener guiEventListener) {
            super.setFocused(guiEventListener);
            if (guiEventListener == null || !this.minecraft.getLastInputType().isKeyboard()) {
                return;
            }
            ScreenRectangle screenRectangle = this.getRectangle();
            ScreenRectangle screenRectangle2 = guiEventListener.getRectangle();
            int n = screenRectangle2.top() - screenRectangle.top();
            int n2 = screenRectangle2.bottom() - screenRectangle.bottom();
            if (n < 0) {
                this.setScrollAmount(this.scrollAmount() + (double)n - 14.0);
            } else if (n2 > 0) {
                this.setScrollAmount(this.scrollAmount() + (double)n2 + 14.0);
            }
        }

        @Override
        public void setX(int n) {
            super.setX(n);
            ScrollableLayout.this.content.setX(n + 10);
        }

        @Override
        public void setY(int n) {
            super.setY(n);
            ScrollableLayout.this.content.setY(n - (int)this.scrollAmount());
        }

        @Override
        public void setScrollAmount(double d) {
            super.setScrollAmount(d);
            ScrollableLayout.this.content.setY(this.getRectangle().top() - (int)this.scrollAmount());
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public Collection<? extends NarratableEntry> getNarratables() {
            return this.children;
        }
    }
}

