/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ResultContainer
implements Container,
RecipeCraftingHolder {
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
    @Nullable
    private RecipeHolder<?> recipeUsed;

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.itemStacks) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int n) {
        return this.itemStacks.get(0);
    }

    @Override
    public ItemStack removeItem(int n, int n2) {
        return ContainerHelper.takeItem(this.itemStacks, 0);
    }

    @Override
    public ItemStack removeItemNoUpdate(int n) {
        return ContainerHelper.takeItem(this.itemStacks, 0);
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        this.itemStacks.set(0, itemStack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
        this.recipeUsed = recipeHolder;
    }

    @Override
    @Nullable
    public RecipeHolder<?> getRecipeUsed() {
        return this.recipeUsed;
    }
}

