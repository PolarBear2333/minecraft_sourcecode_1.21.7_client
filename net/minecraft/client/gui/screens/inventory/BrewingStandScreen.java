/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;

public class BrewingStandScreen
extends AbstractContainerScreen<BrewingStandMenu> {
    private static final ResourceLocation FUEL_LENGTH_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length");
    private static final ResourceLocation BREW_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final ResourceLocation BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final ResourceLocation BREWING_STAND_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public BrewingStandScreen(BrewingStandMenu brewingStandMenu, Inventory inventory, Component component) {
        super(brewingStandMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3;
        int n4 = (this.width - this.imageWidth) / 2;
        int n5 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BREWING_STAND_LOCATION, n4, n5, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        int n6 = ((BrewingStandMenu)this.menu).getFuel();
        int n7 = Mth.clamp((18 * n6 + 20 - 1) / 20, 0, 18);
        if (n7 > 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, FUEL_LENGTH_SPRITE, 18, 4, 0, 0, n4 + 60, n5 + 44, n7, 4);
        }
        if ((n3 = ((BrewingStandMenu)this.menu).getBrewingTicks()) > 0) {
            int n8 = (int)(28.0f * (1.0f - (float)n3 / 400.0f));
            if (n8 > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BREW_PROGRESS_SPRITE, 9, 28, 0, 0, n4 + 97, n5 + 16, 9, n8);
            }
            if ((n8 = BUBBLELENGTHS[n3 / 2 % 7]) > 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUBBLES_SPRITE, 12, 29, 0, 29 - n8, n4 + 63, n5 + 14 + 29 - n8, 12, n8);
            }
        }
    }
}

