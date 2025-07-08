/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemDisplayWidget
extends AbstractWidget {
    private final Minecraft minecraft;
    private final int offsetX;
    private final int offsetY;
    private final ItemStack itemStack;
    private final boolean decorations;
    private final boolean tooltip;

    public ItemDisplayWidget(Minecraft minecraft, int n, int n2, int n3, int n4, Component component, ItemStack itemStack, boolean bl, boolean bl2) {
        super(0, 0, n3, n4, component);
        this.minecraft = minecraft;
        this.offsetX = n;
        this.offsetY = n2;
        this.itemStack = itemStack;
        this.decorations = bl;
        this.tooltip = bl2;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        guiGraphics.renderItem(this.itemStack, this.getX() + this.offsetX, this.getY() + this.offsetY, 0);
        if (this.decorations) {
            guiGraphics.renderItemDecorations(this.minecraft.font, this.itemStack, this.getX() + this.offsetX, this.getY() + this.offsetY, null);
        }
        if (this.isFocused()) {
            guiGraphics.renderOutline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), -1);
        }
        if (this.tooltip && this.isHoveredOrFocused()) {
            guiGraphics.setTooltipForNextFrame(this.minecraft.font, this.itemStack, n, n2);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narration.item", this.itemStack.getHoverName()));
    }
}

