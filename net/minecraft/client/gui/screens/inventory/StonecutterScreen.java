/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class StonecutterScreen
extends AbstractContainerScreen<StonecutterMenu> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final ResourceLocation BG_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/stonecutter.png");
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int RECIPES_COLUMNS = 4;
    private static final int RECIPES_ROWS = 3;
    private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
    private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
    private static final int SCROLLER_FULL_HEIGHT = 54;
    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 14;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    public StonecutterScreen(StonecutterMenu stonecutterMenu, Inventory inventory, Component component) {
        super(stonecutterMenu, inventory, component);
        stonecutterMenu.registerUpdateListener(this::containerChanged);
        --this.titleLabelY;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = this.leftPos;
        int n4 = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        int n5 = (int)(41.0f * this.scrollOffs);
        ResourceLocation resourceLocation = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n3 + 119, n4 + 15 + n5, 12, 15);
        int n6 = this.leftPos + 52;
        int n7 = this.topPos + 14;
        int n8 = this.startIndex + 12;
        this.renderButtons(guiGraphics, n, n2, n6, n7, n8);
        this.renderRecipes(guiGraphics, n6, n7, n8);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int n, int n2) {
        super.renderTooltip(guiGraphics, n, n2);
        if (this.displayRecipes) {
            int n3 = this.leftPos + 52;
            int n4 = this.topPos + 14;
            int n5 = this.startIndex + 12;
            SelectableRecipe.SingleInputSet<StonecutterRecipe> singleInputSet = ((StonecutterMenu)this.menu).getVisibleRecipes();
            for (int i = this.startIndex; i < n5 && i < singleInputSet.size(); ++i) {
                int n6 = i - this.startIndex;
                int n7 = n3 + n6 % 4 * 16;
                int n8 = n4 + n6 / 4 * 18 + 2;
                if (n < n7 || n >= n7 + 16 || n2 < n8 || n2 >= n8 + 18) continue;
                ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);
                SlotDisplay slotDisplay = singleInputSet.entries().get(i).recipe().optionDisplay();
                guiGraphics.setTooltipForNextFrame(this.font, slotDisplay.resolveForFirstStack(contextMap), n, n2);
            }
        }
    }

    private void renderButtons(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5) {
        for (int i = this.startIndex; i < n5 && i < ((StonecutterMenu)this.menu).getNumberOfVisibleRecipes(); ++i) {
            int n6 = i - this.startIndex;
            int n7 = n3 + n6 % 4 * 16;
            int n8 = n6 / 4;
            int n9 = n4 + n8 * 18 + 2;
            ResourceLocation resourceLocation = i == ((StonecutterMenu)this.menu).getSelectedRecipeIndex() ? RECIPE_SELECTED_SPRITE : (n >= n7 && n2 >= n9 && n < n7 + 16 && n2 < n9 + 18 ? RECIPE_HIGHLIGHTED_SPRITE : RECIPE_SPRITE);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n7, n9 - 1, 16, 18);
        }
    }

    private void renderRecipes(GuiGraphics guiGraphics, int n, int n2, int n3) {
        SelectableRecipe.SingleInputSet<StonecutterRecipe> singleInputSet = ((StonecutterMenu)this.menu).getVisibleRecipes();
        ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);
        for (int i = this.startIndex; i < n3 && i < singleInputSet.size(); ++i) {
            int n4 = i - this.startIndex;
            int n5 = n + n4 % 4 * 16;
            int n6 = n4 / 4;
            int n7 = n2 + n6 * 18 + 2;
            SlotDisplay slotDisplay = singleInputSet.entries().get(i).recipe().optionDisplay();
            guiGraphics.renderItem(slotDisplay.resolveForFirstStack(contextMap), n5, n7);
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int n2 = this.leftPos + 52;
            int n3 = this.topPos + 14;
            int n4 = this.startIndex + 12;
            for (int i = this.startIndex; i < n4; ++i) {
                int n5 = i - this.startIndex;
                double d3 = d - (double)(n2 + n5 % 4 * 16);
                double d4 = d2 - (double)(n3 + n5 / 4 * 18);
                if (!(d3 >= 0.0) || !(d4 >= 0.0) || !(d3 < 16.0) || !(d4 < 18.0) || !((StonecutterMenu)this.menu).clickMenuButton(this.minecraft.player, i)) continue;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0f));
                this.minecraft.gameMode.handleInventoryButtonClick(((StonecutterMenu)this.menu).containerId, i);
                return true;
            }
            n2 = this.leftPos + 119;
            n3 = this.topPos + 9;
            if (d >= (double)n2 && d < (double)(n2 + 12) && d2 >= (double)n3 && d2 < (double)(n3 + 54)) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.scrolling && this.isScrollBarActive()) {
            int n2 = this.topPos + 14;
            int n3 = n2 + 54;
            this.scrollOffs = ((float)d2 - (float)n2 - 7.5f) / ((float)(n3 - n2) - 15.0f);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (super.mouseScrolled(d, d2, d3, d4)) {
            return true;
        }
        if (this.isScrollBarActive()) {
            int n = this.getOffscreenRows();
            float f = (float)d4 / (float)n;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0f, 1.0f);
            this.startIndex = (int)((double)(this.scrollOffs * (float)n) + 0.5) * 4;
        }
        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && ((StonecutterMenu)this.menu).getNumberOfVisibleRecipes() > 12;
    }

    protected int getOffscreenRows() {
        return (((StonecutterMenu)this.menu).getNumberOfVisibleRecipes() + 4 - 1) / 4 - 3;
    }

    private void containerChanged() {
        this.displayRecipes = ((StonecutterMenu)this.menu).hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0f;
            this.startIndex = 0;
        }
    }
}

