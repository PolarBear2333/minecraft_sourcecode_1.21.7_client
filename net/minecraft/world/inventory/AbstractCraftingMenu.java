/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class AbstractCraftingMenu
extends RecipeBookMenu {
    private final int width;
    private final int height;
    protected final CraftingContainer craftSlots;
    protected final ResultContainer resultSlots = new ResultContainer();

    public AbstractCraftingMenu(MenuType<?> menuType, int n, int n2, int n3) {
        super(menuType, n);
        this.width = n2;
        this.height = n3;
        this.craftSlots = new TransientCraftingContainer(this, n2, n3);
    }

    protected Slot addResultSlot(Player player, int n, int n2) {
        return this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, n, n2));
    }

    protected void addCraftingGridSlots(int n, int n2) {
        for (int i = 0; i < this.width; ++i) {
            for (int j = 0; j < this.height; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * this.width, n + j * 18, n2 + i * 18));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean bl, boolean bl2, RecipeHolder<?> recipeHolder, ServerLevel serverLevel, Inventory inventory) {
        RecipeHolder<CraftingRecipe> recipeHolder2 = recipeHolder;
        this.beginPlacingRecipe();
        try {
            List<Slot> list = this.getInputGridSlots();
            RecipeBookMenu.PostPlaceAction postPlaceAction = ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<CraftingRecipe>(){

                @Override
                public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
                    AbstractCraftingMenu.this.fillCraftSlotsStackedContents(stackedItemContents);
                }

                @Override
                public void clearCraftingContent() {
                    AbstractCraftingMenu.this.resultSlots.clearContent();
                    AbstractCraftingMenu.this.craftSlots.clearContent();
                }

                @Override
                public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipeHolder) {
                    return recipeHolder.value().matches(AbstractCraftingMenu.this.craftSlots.asCraftInput(), AbstractCraftingMenu.this.owner().level());
                }
            }, this.width, this.height, list, list, inventory, recipeHolder2, bl, bl2);
            return postPlaceAction;
        }
        finally {
            this.finishPlacingRecipe(serverLevel, recipeHolder2);
        }
    }

    protected void beginPlacingRecipe() {
    }

    protected void finishPlacingRecipe(ServerLevel serverLevel, RecipeHolder<CraftingRecipe> recipeHolder) {
    }

    public abstract Slot getResultSlot();

    public abstract List<Slot> getInputGridSlots();

    public int getGridWidth() {
        return this.width;
    }

    public int getGridHeight() {
        return this.height;
    }

    protected abstract Player owner();

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
        this.craftSlots.fillStackedContents(stackedItemContents);
    }
}

