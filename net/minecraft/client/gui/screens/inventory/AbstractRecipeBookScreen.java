/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public abstract class AbstractRecipeBookScreen<T extends RecipeBookMenu>
extends AbstractContainerScreen<T>
implements RecipeUpdateListener {
    private final RecipeBookComponent<?> recipeBookComponent;
    private boolean widthTooNarrow;

    public AbstractRecipeBookScreen(T t, RecipeBookComponent<?> recipeBookComponent, Inventory inventory, Component component) {
        super(t, inventory, component);
        this.recipeBookComponent = recipeBookComponent;
    }

    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.initButton();
    }

    protected abstract ScreenPosition getRecipeBookButtonPosition();

    private void initButton() {
        ScreenPosition screenPosition = this.getRecipeBookButtonPosition();
        this.addRenderableWidget(new ImageButton(screenPosition.x(), screenPosition.y(), 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ScreenPosition screenPosition = this.getRecipeBookButtonPosition();
            button.setPosition(screenPosition.x(), screenPosition.y());
            this.onRecipeBookButtonClick();
        }));
        this.addWidget(this.recipeBookComponent);
    }

    protected void onRecipeBookButtonClick() {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(guiGraphics, n, n2, f);
        } else {
            super.renderContents(guiGraphics, n, n2, f);
        }
        guiGraphics.nextStratum();
        this.recipeBookComponent.render(guiGraphics, n, n2, f);
        guiGraphics.nextStratum();
        this.renderCarriedItem(guiGraphics, n, n2);
        this.renderSnapbackItem(guiGraphics);
        this.renderTooltip(guiGraphics, n, n2);
        this.recipeBookComponent.renderTooltip(guiGraphics, n, n2, this.hoveredSlot);
    }

    @Override
    protected void renderSlots(GuiGraphics guiGraphics) {
        super.renderSlots(guiGraphics);
        this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.isBiggerResultSlot());
    }

    protected boolean isBiggerResultSlot() {
        return true;
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (this.recipeBookComponent.charTyped(c, n)) {
            return true;
        }
        return super.charTyped(c, n);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.recipeBookComponent.keyPressed(n, n2, n3)) {
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.recipeBookComponent.mouseClicked(d, d2, n)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        if (this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    protected boolean isHovering(int n, int n2, int n3, int n4, double d, double d2) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(n, n2, n3, n4, d, d2);
    }

    @Override
    protected boolean hasClickedOutside(double d, double d2, int n, int n2, int n3) {
        boolean bl = d < (double)n || d2 < (double)n2 || d >= (double)(n + this.imageWidth) || d2 >= (double)(n2 + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(d, d2, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, n3) && bl;
    }

    @Override
    protected void slotClicked(Slot slot, int n, int n2, ClickType clickType) {
        super.slotClicked(slot, n, n2, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay recipeDisplay) {
        this.recipeBookComponent.fillGhostRecipe(recipeDisplay);
    }
}

