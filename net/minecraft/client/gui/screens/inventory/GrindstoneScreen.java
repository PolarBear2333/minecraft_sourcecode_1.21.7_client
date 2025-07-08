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
import net.minecraft.world.inventory.GrindstoneMenu;

public class GrindstoneScreen
extends AbstractContainerScreen<GrindstoneMenu> {
    private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/grindstone/error");
    private static final ResourceLocation GRINDSTONE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/grindstone.png");

    public GrindstoneScreen(GrindstoneMenu grindstoneMenu, Inventory inventory, Component component) {
        super(grindstoneMenu, inventory, component);
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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GRINDSTONE_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        if ((((GrindstoneMenu)this.menu).getSlot(0).hasItem() || ((GrindstoneMenu)this.menu).getSlot(1).hasItem()) && !((GrindstoneMenu)this.menu).getSlot(2).hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, n3 + 92, n4 + 31, 28, 21);
        }
    }
}

