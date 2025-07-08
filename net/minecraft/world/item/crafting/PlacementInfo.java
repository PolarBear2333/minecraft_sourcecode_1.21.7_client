/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.crafting.Ingredient;

public class PlacementInfo {
    public static final int EMPTY_SLOT = -1;
    public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), IntList.of());
    private final List<Ingredient> ingredients;
    private final IntList slotsToIngredientIndex;

    private PlacementInfo(List<Ingredient> list, IntList intList) {
        this.ingredients = list;
        this.slotsToIngredientIndex = intList;
    }

    public static PlacementInfo create(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return NOT_PLACEABLE;
        }
        return new PlacementInfo(List.of(ingredient), IntList.of((int)0));
    }

    public static PlacementInfo createFromOptionals(List<Optional<Ingredient>> list) {
        int n = list.size();
        ArrayList<Ingredient> arrayList = new ArrayList<Ingredient>(n);
        IntArrayList intArrayList = new IntArrayList(n);
        int n2 = 0;
        for (Optional<Ingredient> optional : list) {
            if (optional.isPresent()) {
                Ingredient ingredient = optional.get();
                if (ingredient.isEmpty()) {
                    return NOT_PLACEABLE;
                }
                arrayList.add(ingredient);
                intArrayList.add(n2++);
                continue;
            }
            intArrayList.add(-1);
        }
        return new PlacementInfo(arrayList, (IntList)intArrayList);
    }

    public static PlacementInfo create(List<Ingredient> list) {
        int n = list.size();
        IntArrayList intArrayList = new IntArrayList(n);
        for (int i = 0; i < n; ++i) {
            Ingredient ingredient = list.get(i);
            if (ingredient.isEmpty()) {
                return NOT_PLACEABLE;
            }
            intArrayList.add(i);
        }
        return new PlacementInfo(list, (IntList)intArrayList);
    }

    public IntList slotsToIngredientIndex() {
        return this.slotsToIngredientIndex;
    }

    public List<Ingredient> ingredients() {
        return this.ingredients;
    }

    public boolean isImpossibleToPlace() {
        return this.slotsToIngredientIndex.isEmpty();
    }
}

