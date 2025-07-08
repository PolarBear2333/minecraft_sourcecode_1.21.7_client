/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class FocusableTextWidget
extends MultiLineTextWidget {
    public static final int DEFAULT_PADDING = 4;
    private final boolean alwaysShowBorder;
    private final boolean fillBackground;
    private final int padding;

    public FocusableTextWidget(int n, Component component, Font font) {
        this(n, component, font, 4);
    }

    public FocusableTextWidget(int n, Component component, Font font, int n2) {
        this(n, component, font, true, true, n2);
    }

    public FocusableTextWidget(int n, Component component, Font font, boolean bl, boolean bl2, int n2) {
        super(component, font);
        this.setMaxWidth(n);
        this.setCentered(true);
        this.active = true;
        this.alwaysShowBorder = bl;
        this.fillBackground = bl2;
        this.padding = n2;
    }

    public void containWithin(int n) {
        this.setMaxWidth(n - this.padding * 4);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = this.getX() - this.padding;
        int n4 = this.getY() - this.padding;
        int n5 = this.getWidth() + this.padding * 2;
        int n6 = this.getHeight() + this.padding * 2;
        int n7 = ARGB.color(this.alpha, this.alwaysShowBorder ? (this.isFocused() ? -1 : -6250336) : -1);
        if (this.fillBackground) {
            guiGraphics.fill(n3 + 1, n4, n3 + n5, n4 + n6, ARGB.color(this.alpha, -16777216));
        }
        if (this.isFocused() || this.alwaysShowBorder) {
            guiGraphics.renderOutline(n3, n4, n5, n6, n7);
        }
        super.renderWidget(guiGraphics, n, n2, f);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }
}

