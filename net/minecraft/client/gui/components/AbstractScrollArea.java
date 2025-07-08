/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class AbstractScrollArea
extends AbstractWidget {
    public static final int SCROLLBAR_WIDTH = 6;
    private double scrollAmount;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    private boolean scrolling;

    public AbstractScrollArea(int n, int n2, int n3, int n4, Component component) {
        super(n, n2, n3, n4, component);
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (!this.visible) {
            return false;
        }
        this.setScrollAmount(this.scrollAmount() - d4 * this.scrollRate());
        return true;
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.scrolling) {
            if (d2 < (double)this.getY()) {
                this.setScrollAmount(0.0);
            } else if (d2 > (double)this.getBottom()) {
                this.setScrollAmount(this.maxScrollAmount());
            } else {
                double d5 = Math.max(1, this.maxScrollAmount());
                int n2 = this.scrollerHeight();
                double d6 = Math.max(1.0, d5 / (double)(this.height - n2));
                this.setScrollAmount(this.scrollAmount() + d4 * d6);
            }
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    @Override
    public void onRelease(double d, double d2) {
        this.scrolling = false;
    }

    public double scrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double d) {
        this.scrollAmount = Mth.clamp(d, 0.0, (double)this.maxScrollAmount());
    }

    public boolean updateScrolling(double d, double d2, int n) {
        this.scrolling = this.scrollbarVisible() && this.isValidClickButton(n) && d >= (double)this.scrollBarX() && d <= (double)(this.scrollBarX() + 6) && d2 >= (double)this.getY() && d2 < (double)this.getBottom();
        return this.scrolling;
    }

    public void refreshScrollAmount() {
        this.setScrollAmount(this.scrollAmount);
    }

    public int maxScrollAmount() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    protected boolean scrollbarVisible() {
        return this.maxScrollAmount() > 0;
    }

    protected int scrollerHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.contentHeight()), 32, this.height - 8);
    }

    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    protected int scrollBarY() {
        return Math.max(this.getY(), (int)this.scrollAmount * (this.height - this.scrollerHeight()) / this.maxScrollAmount() + this.getY());
    }

    protected void renderScrollbar(GuiGraphics guiGraphics) {
        if (this.scrollbarVisible()) {
            int n = this.scrollBarX();
            int n2 = this.scrollerHeight();
            int n3 = this.scrollBarY();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_SPRITE, n, this.getY(), 6, this.getHeight());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, n, n3, 6, n2);
        }
    }

    protected abstract int contentHeight();

    protected abstract double scrollRate();
}

