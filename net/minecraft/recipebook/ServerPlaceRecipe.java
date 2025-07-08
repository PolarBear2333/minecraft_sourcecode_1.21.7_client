/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<R extends Recipe<?>> {
    private static final int ITEM_NOT_FOUND = -1;
    private final Inventory inventory;
    private final CraftingMenuAccess<R> menu;
    private final boolean useMaxItems;
    private final int gridWidth;
    private final int gridHeight;
    private final List<Slot> inputGridSlots;
    private final List<Slot> slotsToClear;

    public static <I extends RecipeInput, R extends Recipe<I>> RecipeBookMenu.PostPlaceAction placeRecipe(CraftingMenuAccess<R> craftingMenuAccess, int n, int n2, List<Slot> list, List<Slot> list2, Inventory inventory, RecipeHolder<R> recipeHolder, boolean bl, boolean bl2) {
        ServerPlaceRecipe<R> serverPlaceRecipe = new ServerPlaceRecipe<R>(craftingMenuAccess, inventory, bl, n, n2, list, list2);
        if (!bl2 && !serverPlaceRecipe.testClearGrid()) {
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        }
        StackedItemContents stackedItemContents = new StackedItemContents();
        inventory.fillStackedContents(stackedItemContents);
        craftingMenuAccess.fillCraftSlotsStackedContents(stackedItemContents);
        return serverPlaceRecipe.tryPlaceRecipe(recipeHolder, stackedItemContents);
    }

    private ServerPlaceRecipe(CraftingMenuAccess<R> craftingMenuAccess, Inventory inventory, boolean bl, int n, int n2, List<Slot> list, List<Slot> list2) {
        this.menu = craftingMenuAccess;
        this.inventory = inventory;
        this.useMaxItems = bl;
        this.gridWidth = n;
        this.gridHeight = n2;
        this.inputGridSlots = list;
        this.slotsToClear = list2;
    }

    private RecipeBookMenu.PostPlaceAction tryPlaceRecipe(RecipeHolder<R> recipeHolder, StackedItemContents stackedItemContents) {
        if (stackedItemContents.canCraft((Recipe<?>)recipeHolder.value(), null)) {
            this.placeRecipe(recipeHolder, stackedItemContents);
            this.inventory.setChanged();
            return RecipeBookMenu.PostPlaceAction.NOTHING;
        }
        this.clearGrid();
        this.inventory.setChanged();
        return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
    }

    private void clearGrid() {
        for (Slot slot : this.slotsToClear) {
            ItemStack itemStack = slot.getItem().copy();
            this.inventory.placeItemBackInInventory(itemStack, false);
            slot.set(itemStack);
        }
        this.menu.clearCraftingContent();
    }

    private void placeRecipe(RecipeHolder<R> recipeHolder, StackedItemContents stackedItemContents) {
        boolean bl = this.menu.recipeMatches(recipeHolder);
        int n = stackedItemContents.getBiggestCraftableStack((Recipe<?>)recipeHolder.value(), null);
        if (bl) {
            for (Slot object2 : this.inputGridSlots) {
                ItemStack n32 = object2.getItem();
                if (n32.isEmpty() || Math.min(n, n32.getMaxStackSize()) >= n32.getCount() + 1) continue;
                return;
            }
        }
        int n22 = this.calculateAmountToCraft(n, bl);
        ArrayList<Holder<Item>> arrayList = new ArrayList<Holder<Item>>();
        if (!stackedItemContents.canCraft((Recipe<?>)recipeHolder.value(), n22, arrayList::add)) {
            return;
        }
        int n6 = ServerPlaceRecipe.clampToMaxStackSize(n22, arrayList);
        if (n6 != n22) {
            arrayList.clear();
            if (!stackedItemContents.canCraft((Recipe<?>)recipeHolder.value(), n6, arrayList::add)) {
                return;
            }
        }
        this.clearGrid();
        PlaceRecipeHelper.placeRecipe(this.gridWidth, this.gridHeight, recipeHolder.value(), recipeHolder.value().placementInfo().slotsToIngredientIndex(), (n2, n3, n4, n5) -> {
            if (n2 == -1) {
                return;
            }
            Slot slot = this.inputGridSlots.get(n3);
            Holder holder = (Holder)arrayList.get((int)n2);
            int n6 = n6;
            while (n6 > 0) {
                if ((n6 = this.moveItemToGrid(slot, holder, n6)) != -1) continue;
                return;
            }
        });
    }

    private static int clampToMaxStackSize(int n, List<Holder<Item>> list) {
        for (Holder<Item> holder : list) {
            n = Math.min(n, holder.value().getDefaultMaxStackSize());
        }
        return n;
    }

    private int calculateAmountToCraft(int n, boolean bl) {
        if (this.useMaxItems) {
            return n;
        }
        if (bl) {
            int n2 = Integer.MAX_VALUE;
            for (Slot slot : this.inputGridSlots) {
                ItemStack itemStack = slot.getItem();
                if (itemStack.isEmpty() || n2 <= itemStack.getCount()) continue;
                n2 = itemStack.getCount();
            }
            if (n2 != Integer.MAX_VALUE) {
                ++n2;
            }
            return n2;
        }
        return 1;
    }

    private int moveItemToGrid(Slot slot, Holder<Item> holder, int n) {
        ItemStack itemStack = slot.getItem();
        int n2 = this.inventory.findSlotMatchingCraftingIngredient(holder, itemStack);
        if (n2 == -1) {
            return -1;
        }
        ItemStack itemStack2 = this.inventory.getItem(n2);
        ItemStack itemStack3 = n < itemStack2.getCount() ? this.inventory.removeItem(n2, n) : this.inventory.removeItemNoUpdate(n2);
        int n3 = itemStack3.getCount();
        if (itemStack.isEmpty()) {
            slot.set(itemStack3);
        } else {
            itemStack.grow(n3);
        }
        return n - n3;
    }

    private boolean testClearGrid() {
        ArrayList arrayList = Lists.newArrayList();
        int n = this.getAmountOfFreeSlotsInInventory();
        for (Slot slot : this.inputGridSlots) {
            ItemStack itemStack = slot.getItem().copy();
            if (itemStack.isEmpty()) continue;
            int n2 = this.inventory.getSlotWithRemainingSpace(itemStack);
            if (n2 == -1 && arrayList.size() <= n) {
                for (ItemStack itemStack2 : arrayList) {
                    if (!ItemStack.isSameItem(itemStack2, itemStack) || itemStack2.getCount() == itemStack2.getMaxStackSize() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) continue;
                    itemStack2.grow(itemStack.getCount());
                    itemStack.setCount(0);
                    break;
                }
                if (itemStack.isEmpty()) continue;
                if (arrayList.size() < n) {
                    arrayList.add(itemStack);
                    continue;
                }
                return false;
            }
            if (n2 != -1) continue;
            return false;
        }
        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int n = 0;
        for (ItemStack itemStack : this.inventory.getNonEquipmentItems()) {
            if (!itemStack.isEmpty()) continue;
            ++n;
        }
        return n;
    }

    public static interface CraftingMenuAccess<T extends Recipe<?>> {
        public void fillCraftSlotsStackedContents(StackedItemContents var1);

        public void clearCraftingContent();

        public boolean recipeMatches(RecipeHolder<T> var1);
    }
}

