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
import net.minecraft.world.inventory.ChestMenu;

public class ContainerScreen
extends AbstractContainerScreen<ChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private final int containerRows;

    public ContainerScreen(ChestMenu chestMenu, Inventory inventory, Component component) {
        super(chestMenu, inventory, component);
        int n = 222;
        int n2 = 114;
        this.containerRows = chestMenu.getRowCount();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, n3, n4, 0.0f, 0.0f, this.imageWidth, this.containerRows * 18 + 17, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, n3, n4 + this.containerRows * 18 + 17, 0.0f, 126.0f, this.imageWidth, 96, 256, 256);
    }
}

