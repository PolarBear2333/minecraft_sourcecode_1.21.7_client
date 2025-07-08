/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerDuplicateRecipe
extends CustomRecipe {
    public BannerDuplicateRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != 2) {
            return false;
        }
        DyeColor dyeColor = null;
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            Item item = itemStack.getItem();
            if (item instanceof BannerItem) {
                BannerItem bannerItem = (BannerItem)item;
                if (dyeColor == null) {
                    dyeColor = bannerItem.getColor();
                } else if (dyeColor != bannerItem.getColor()) {
                    return false;
                }
            } else {
                return false;
            }
            int n = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size();
            if (n > 6) {
                return false;
            }
            if (n > 0) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            if (bl) {
                return false;
            }
            bl = true;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        for (int i = 0; i < craftingInput.size(); ++i) {
            int n;
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty() || (n = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size()) <= 0 || n > 6) continue;
            return itemStack.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            ItemStack itemStack2 = itemStack.getItem().getCraftingRemainder();
            if (!itemStack2.isEmpty()) {
                nonNullList.set(i, itemStack2);
                continue;
            }
            if (itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty()) continue;
            nonNullList.set(i, itemStack.copyWithCount(1));
        }
        return nonNullList;
    }

    @Override
    public RecipeSerializer<BannerDuplicateRecipe> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }
}

