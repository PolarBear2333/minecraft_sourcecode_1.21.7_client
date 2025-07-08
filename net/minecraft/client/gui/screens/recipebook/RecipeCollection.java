/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.recipebook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public class RecipeCollection {
    public static final RecipeCollection EMPTY = new RecipeCollection(List.of());
    private final List<RecipeDisplayEntry> entries;
    private final Set<RecipeDisplayId> craftable = new HashSet<RecipeDisplayId>();
    private final Set<RecipeDisplayId> selected = new HashSet<RecipeDisplayId>();

    public RecipeCollection(List<RecipeDisplayEntry> list) {
        this.entries = list;
    }

    public void selectRecipes(StackedItemContents stackedItemContents, Predicate<RecipeDisplay> predicate) {
        for (RecipeDisplayEntry recipeDisplayEntry : this.entries) {
            boolean bl = predicate.test(recipeDisplayEntry.display());
            if (bl) {
                this.selected.add(recipeDisplayEntry.id());
            } else {
                this.selected.remove(recipeDisplayEntry.id());
            }
            if (bl && recipeDisplayEntry.canCraft(stackedItemContents)) {
                this.craftable.add(recipeDisplayEntry.id());
                continue;
            }
            this.craftable.remove(recipeDisplayEntry.id());
        }
    }

    public boolean isCraftable(RecipeDisplayId recipeDisplayId) {
        return this.craftable.contains(recipeDisplayId);
    }

    public boolean hasCraftable() {
        return !this.craftable.isEmpty();
    }

    public boolean hasAnySelected() {
        return !this.selected.isEmpty();
    }

    public List<RecipeDisplayEntry> getRecipes() {
        return this.entries;
    }

    public List<RecipeDisplayEntry> getSelectedRecipes(CraftableStatus craftableStatus) {
        Predicate<RecipeDisplayId> predicate = switch (craftableStatus.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.selected::contains;
            case 1 -> this.craftable::contains;
            case 2 -> recipeDisplayId -> this.selected.contains(recipeDisplayId) && !this.craftable.contains(recipeDisplayId);
        };
        ArrayList<RecipeDisplayEntry> arrayList = new ArrayList<RecipeDisplayEntry>();
        for (RecipeDisplayEntry recipeDisplayEntry : this.entries) {
            if (!predicate.test(recipeDisplayEntry.id())) continue;
            arrayList.add(recipeDisplayEntry);
        }
        return arrayList;
    }

    public static enum CraftableStatus {
        ANY,
        CRAFTABLE,
        NOT_CRAFTABLE;

    }
}

