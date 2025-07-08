/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractTextAreaWidget
extends AbstractScrollArea {
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted"));
    private static final int INNER_PADDING = 4;
    public static final int DEFAULT_TOTAL_PADDING = 8;
    private boolean showBackground = true;
    private boolean showDecorations = true;

    public AbstractTextAreaWidget(int n, int n2, int n3, int n4, Component component) {
        super(n, n2, n3, n4, component);
    }

    public AbstractTextAreaWidget(int n, int n2, int n3, int n4, Component component, boolean bl, boolean bl2) {
        this(n, n2, n3, n4, component);
        this.showBackground = bl;
        this.showDecorations = bl2;
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        boolean bl = this.updateScrolling(d, d2, n);
        return super.mouseClicked(d, d2, n) || bl;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        boolean bl;
        boolean bl2 = n == 265;
        boolean bl3 = bl = n == 264;
        if (bl2 || bl) {
            double d = this.scrollAmount();
            this.setScrollAmount(this.scrollAmount() + (double)(bl2 ? -1 : 1) * this.scrollRate());
            if (d != this.scrollAmount()) {
                return true;
            }
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (!this.visible) {
            return;
        }
        if (this.showBackground) {
            this.renderBackground(guiGraphics);
        }
        guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0.0f, (float)(-this.scrollAmount()));
        this.renderContents(guiGraphics, n, n2, f);
        guiGraphics.pose().popMatrix();
        guiGraphics.disableScissor();
        this.renderScrollbar(guiGraphics);
        if (this.showDecorations) {
            this.renderDecorations(guiGraphics);
        }
    }

    protected void renderDecorations(GuiGraphics guiGraphics) {
    }

    protected int innerPadding() {
        return 4;
    }

    protected int totalInnerPadding() {
        return this.innerPadding() * 2;
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return this.active && this.visible && d >= (double)this.getX() && d2 >= (double)this.getY() && d < (double)(this.getRight() + 6) && d2 < (double)this.getBottom();
    }

    @Override
    protected int scrollBarX() {
        return this.getRight();
    }

    @Override
    protected int contentHeight() {
        return this.getInnerHeight() + this.totalInnerPadding();
    }

    protected void renderBackground(GuiGraphics guiGraphics) {
        this.renderBorder(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    protected void renderBorder(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        ResourceLocation resourceLocation = BACKGROUND_SPRITES.get(this.isActive(), this.isFocused());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n2, n3, n4);
    }

    protected boolean withinContentAreaTopBottom(int n, int n2) {
        return (double)n2 - this.scrollAmount() >= (double)this.getY() && (double)n - this.scrollAmount() <= (double)(this.getY() + this.height);
    }

    protected abstract int getInnerHeight();

    protected abstract void renderContents(GuiGraphics var1, int var2, int var3, float var4);

    protected int getInnerLeft() {
        return this.getX() + this.innerPadding();
    }

    protected int getInnerTop() {
        return this.getY() + this.innerPadding();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }
}

