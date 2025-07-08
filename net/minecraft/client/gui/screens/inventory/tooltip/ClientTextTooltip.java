/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;

public class ClientTextTooltip
implements ClientTooltipComponent {
    private final FormattedCharSequence text;

    public ClientTextTooltip(FormattedCharSequence formattedCharSequence) {
        this.text = formattedCharSequence;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(this.text);
    }

    @Override
    public int getHeight(Font font) {
        return 10;
    }

    @Override
    public void renderText(GuiGraphics guiGraphics, Font font, int n, int n2) {
        guiGraphics.drawString(font, this.text, n, n2, -1, true);
    }
}

