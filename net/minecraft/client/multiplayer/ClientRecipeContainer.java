/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class ClientRecipeContainer
implements RecipeAccess {
    private final Map<ResourceKey<RecipePropertySet>, RecipePropertySet> itemSets;
    private final SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes;

    public ClientRecipeContainer(Map<ResourceKey<RecipePropertySet>, RecipePropertySet> map, SelectableRecipe.SingleInputSet<StonecutterRecipe> singleInputSet) {
        this.itemSets = map;
        this.stonecutterRecipes = singleInputSet;
    }

    @Override
    public RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourceKey) {
        return this.itemSets.getOrDefault(resourceKey, RecipePropertySet.EMPTY);
    }

    @Override
    public SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes() {
        return this.stonecutterRecipes;
    }
}

