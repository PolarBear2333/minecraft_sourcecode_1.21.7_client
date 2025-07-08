/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.FurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;

public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceMenu>
extends AbstractRecipeBookScreen<T> {
    private final ResourceLocation texture;
    private final ResourceLocation litProgressSprite;
    private final ResourceLocation burnProgressSprite;

    public AbstractFurnaceScreen(T t, Inventory inventory, Component component, Component component2, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, List<RecipeBookComponent.TabInfo> list) {
        super(t, new FurnaceRecipeBookComponent((AbstractFurnaceMenu)t, component2, list), inventory, component);
        this.texture = resourceLocation;
        this.litProgressSprite = resourceLocation2;
        this.burnProgressSprite = resourceLocation3;
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 20, this.height / 2 - 49);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3;
        int n4;
        int n5 = this.leftPos;
        int n6 = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, n5, n6, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        if (((AbstractFurnaceMenu)this.menu).isLit()) {
            n4 = 14;
            n3 = Mth.ceil(((AbstractFurnaceMenu)this.menu).getLitProgress() * 13.0f) + 1;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.litProgressSprite, 14, 14, 0, 14 - n3, n5 + 56, n6 + 36 + 14 - n3, 14, n3);
        }
        n4 = 24;
        n3 = Mth.ceil(((AbstractFurnaceMenu)this.menu).getBurnProgress() * 24.0f);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.burnProgressSprite, 24, 16, 0, 0, n5 + 79, n6 + 34, n3, 16);
    }
}

