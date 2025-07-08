/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;

public class CraftingScreen
extends AbstractRecipeBookScreen<CraftingMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public CraftingScreen(CraftingMenu craftingMenu, Inventory inventory, Component component) {
        super(craftingMenu, new CraftingRecipeBookComponent(craftingMenu), inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 29;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = this.leftPos;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CRAFTING_TABLE_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }
}

