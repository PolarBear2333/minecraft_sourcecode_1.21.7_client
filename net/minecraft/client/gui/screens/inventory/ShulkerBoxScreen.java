/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;

public class ShulkerBoxScreen
extends AbstractContainerScreen<ShulkerBoxMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png");

    public ShulkerBoxScreen(ShulkerBoxMenu shulkerBoxMenu, Inventory inventory, Component component) {
        super(shulkerBoxMenu, inventory, component);
        ++this.imageHeight;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }
}

