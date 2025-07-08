/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.player;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;

public class StackedItemContents {
    private final StackedContents<Holder<Item>> raw = new StackedContents();

    public void accountSimpleStack(ItemStack itemStack) {
        if (Inventory.isUsableForCrafting(itemStack)) {
            this.accountStack(itemStack);
        }
    }

    public void accountStack(ItemStack itemStack) {
        this.accountStack(itemStack, itemStack.getMaxStackSize());
    }

    public void accountStack(ItemStack itemStack, int n) {
        if (!itemStack.isEmpty()) {
            int n2 = Math.min(n, itemStack.getCount());
            this.raw.account(itemStack.getItemHolder(), n2);
        }
    }

    public boolean canCraft(Recipe<?> recipe, @Nullable StackedContents.Output<Holder<Item>> output) {
        return this.canCraft(recipe, 1, output);
    }

    public boolean canCraft(Recipe<?> recipe, int n, @Nullable StackedContents.Output<Holder<Item>> output) {
        PlacementInfo placementInfo = recipe.placementInfo();
        if (placementInfo.isImpossibleToPlace()) {
            return false;
        }
        return this.canCraft(placementInfo.ingredients(), n, output);
    }

    public boolean canCraft(List<? extends StackedContents.IngredientInfo<Holder<Item>>> list, @Nullable StackedContents.Output<Holder<Item>> output) {
        return this.canCraft(list, 1, output);
    }

    private boolean canCraft(List<? extends StackedContents.IngredientInfo<Holder<Item>>> list, int n, @Nullable StackedContents.Output<Holder<Item>> output) {
        return this.raw.tryPick(list, n, output);
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, @Nullable StackedContents.Output<Holder<Item>> output) {
        return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, output);
    }

    public int getBiggestCraftableStack(Recipe<?> recipe, int n, @Nullable StackedContents.Output<Holder<Item>> output) {
        return this.raw.tryPickAll(recipe.placementInfo().ingredients(), n, output);
    }

    public void clear() {
        this.raw.clear();
    }
}

