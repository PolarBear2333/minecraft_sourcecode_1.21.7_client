/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FittingMultiLineTextWidget
extends AbstractTextAreaWidget {
    private final Font font;
    private final MultiLineTextWidget multilineWidget;

    public FittingMultiLineTextWidget(int n, int n2, int n3, int n4, Component component, Font font) {
        super(n, n2, n3, n4, component);
        this.font = font;
        this.multilineWidget = new MultiLineTextWidget(component, font).setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    public FittingMultiLineTextWidget setColor(int n) {
        this.multilineWidget.setColor(n);
        return this;
    }

    @Override
    public void setWidth(int n) {
        super.setWidth(n);
        this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    @Override
    protected int getInnerHeight() {
        return this.multilineWidget.getHeight();
    }

    @Override
    protected double scrollRate() {
        return this.font.lineHeight;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    public boolean showingScrollBar() {
        return super.scrollbarVisible();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int n, int n2, float f) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)this.getInnerLeft(), (float)this.getInnerTop());
        this.multilineWidget.render(guiGraphics, n, n2, f);
        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void setMessage(Component component) {
        super.setMessage(component);
        this.multilineWidget.setMessage(component);
    }
}

