/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class StringWidget
extends AbstractStringWidget {
    private float alignX = 0.5f;

    public StringWidget(Component component, Font font) {
        this(0, 0, font.width(component.getVisualOrderText()), font.lineHeight, component, font);
    }

    public StringWidget(int n, int n2, Component component, Font font) {
        this(0, 0, n, n2, component, font);
    }

    public StringWidget(int n, int n2, int n3, int n4, Component component, Font font) {
        super(n, n2, n3, n4, component, font);
        this.active = false;
    }

    @Override
    public StringWidget setColor(int n) {
        super.setColor(n);
        return this;
    }

    private StringWidget horizontalAlignment(float f) {
        this.alignX = f;
        return this;
    }

    public StringWidget alignLeft() {
        return this.horizontalAlignment(0.0f);
    }

    public StringWidget alignCenter() {
        return this.horizontalAlignment(0.5f);
    }

    public StringWidget alignRight() {
        return this.horizontalAlignment(1.0f);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int n3 = this.getWidth();
        int n4 = font.width(component);
        int n5 = this.getX() + Math.round(this.alignX * (float)(n3 - n4));
        int n6 = this.getY() + (this.getHeight() - font.lineHeight) / 2;
        FormattedCharSequence formattedCharSequence = n4 > n3 ? this.clipText(component, n3) : component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, n5, n6, this.getColor());
    }

    private FormattedCharSequence clipText(Component component, int n) {
        Font font = this.getFont();
        FormattedText formattedText = font.substrByWidth(component, n - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedText, CommonComponents.ELLIPSIS));
    }

    @Override
    public /* synthetic */ AbstractStringWidget setColor(int n) {
        return this.setColor(n);
    }
}

