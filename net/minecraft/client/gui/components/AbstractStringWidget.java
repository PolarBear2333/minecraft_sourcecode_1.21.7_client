/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public abstract class AbstractStringWidget
extends AbstractWidget {
    private final Font font;
    private int color = -1;

    public AbstractStringWidget(int n, int n2, int n3, int n4, Component component, Font font) {
        super(n, n2, n3, n4, component);
        this.font = font;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public AbstractStringWidget setColor(int n) {
        this.color = n;
        return this;
    }

    protected final Font getFont() {
        return this.font;
    }

    protected final int getColor() {
        return ARGB.color(this.alpha, this.color);
    }
}

