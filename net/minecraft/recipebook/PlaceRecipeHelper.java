/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipeHelper {
    public static <T> void placeRecipe(int n, int n2, Recipe<?> recipe, Iterable<T> iterable, Output<T> output) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            PlaceRecipeHelper.placeRecipe(n, n2, shapedRecipe.getWidth(), shapedRecipe.getHeight(), iterable, output);
        } else {
            PlaceRecipeHelper.placeRecipe(n, n2, n, n2, iterable, output);
        }
    }

    public static <T> void placeRecipe(int n, int n2, int n3, int n4, Iterable<T> iterable, Output<T> output) {
        Iterator<T> iterator = iterable.iterator();
        int n5 = 0;
        block0: for (int i = 0; i < n2; ++i) {
            boolean bl = (float)n4 < (float)n2 / 2.0f;
            int n6 = Mth.floor((float)n2 / 2.0f - (float)n4 / 2.0f);
            if (bl && n6 > i) {
                n5 += n;
                ++i;
            }
            for (int j = 0; j < n; ++j) {
                boolean bl2;
                if (!iterator.hasNext()) {
                    return;
                }
                bl = (float)n3 < (float)n / 2.0f;
                n6 = Mth.floor((float)n / 2.0f - (float)n3 / 2.0f);
                int n7 = n3;
                boolean bl3 = bl2 = j < n3;
                if (bl) {
                    n7 = n6 + n3;
                    boolean bl4 = bl2 = n6 <= j && j < n6 + n3;
                }
                if (bl2) {
                    output.addItemToSlot(iterator.next(), n5, j, i);
                } else if (n7 == j) {
                    n5 += n - j;
                    continue block0;
                }
                ++n5;
            }
        }
    }

    @FunctionalInterface
    public static interface Output<T> {
        public void addItemToSlot(T var1, int var2, int var3, int var4);
    }
}

