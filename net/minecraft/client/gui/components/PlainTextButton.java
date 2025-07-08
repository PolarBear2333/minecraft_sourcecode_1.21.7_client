/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class PlainTextButton
extends Button {
    private final Font font;
    private final Component message;
    private final Component underlinedMessage;

    public PlainTextButton(int n, int n2, int n3, int n4, Component component, Button.OnPress onPress, Font font) {
        super(n, n2, n3, n4, component, onPress, DEFAULT_NARRATION);
        this.font = font;
        this.message = component;
        this.underlinedMessage = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Component component = this.isHoveredOrFocused() ? this.underlinedMessage : this.message;
        guiGraphics.drawString(this.font, component, this.getX(), this.getY(), 0xFFFFFF | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}

