/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.recipebook;

import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class CraftingRecipeBookComponent
extends RecipeBookComponent<AbstractCraftingMenu> {
    private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"), ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled"), ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted"), ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
    private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final List<RecipeBookComponent.TabInfo> TABS = List.of(new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING), new RecipeBookComponent.TabInfo(Items.IRON_AXE, Items.GOLDEN_SWORD, RecipeBookCategories.CRAFTING_EQUIPMENT), new RecipeBookComponent.TabInfo(Items.BRICKS, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS), new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.APPLE, RecipeBookCategories.CRAFTING_MISC), new RecipeBookComponent.TabInfo(Items.REDSTONE, RecipeBookCategories.CRAFTING_REDSTONE));

    public CraftingRecipeBookComponent(AbstractCraftingMenu abstractCraftingMenu) {
        super(abstractCraftingMenu, TABS);
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return ((AbstractCraftingMenu)this.menu).getResultSlot() == slot || ((AbstractCraftingMenu)this.menu).getInputGridSlots().contains(slot);
    }

    private boolean canDisplay(RecipeDisplay recipeDisplay) {
        int n = ((AbstractCraftingMenu)this.menu).getGridWidth();
        int n2 = ((AbstractCraftingMenu)this.menu).getGridHeight();
        RecipeDisplay recipeDisplay2 = recipeDisplay;
        Objects.requireNonNull(recipeDisplay2);
        RecipeDisplay recipeDisplay3 = recipeDisplay2;
        int n3 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay3, n3)) {
            case 0 -> {
                ShapedCraftingRecipeDisplay var6_6 = (ShapedCraftingRecipeDisplay)recipeDisplay3;
                if (n >= var6_6.width() && n2 >= var6_6.height()) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                ShapelessCraftingRecipeDisplay var7_7 = (ShapelessCraftingRecipeDisplay)recipeDisplay3;
                if (n * n2 >= var7_7.ingredients().size()) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
        ghostSlots.setResult(((AbstractCraftingMenu)this.menu).getResultSlot(), contextMap, recipeDisplay.result());
        RecipeDisplay recipeDisplay2 = recipeDisplay;
        Objects.requireNonNull(recipeDisplay2);
        RecipeDisplay recipeDisplay3 = recipeDisplay2;
        int n4 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay3, n4)) {
            case 0: {
                ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay = (ShapedCraftingRecipeDisplay)recipeDisplay3;
                List<Slot> list = ((AbstractCraftingMenu)this.menu).getInputGridSlots();
                PlaceRecipeHelper.placeRecipe(((AbstractCraftingMenu)this.menu).getGridWidth(), ((AbstractCraftingMenu)this.menu).getGridHeight(), shapedCraftingRecipeDisplay.width(), shapedCraftingRecipeDisplay.height(), shapedCraftingRecipeDisplay.ingredients(), (slotDisplay, n, n2, n3) -> {
                    Slot slot = (Slot)list.get(n);
                    ghostSlots.setInput(slot, contextMap, (SlotDisplay)slotDisplay);
                });
                break;
            }
            case 1: {
                ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay = (ShapelessCraftingRecipeDisplay)recipeDisplay3;
                List<Slot> list = ((AbstractCraftingMenu)this.menu).getInputGridSlots();
                int n5 = Math.min(shapelessCraftingRecipeDisplay.ingredients().size(), list.size());
                for (int i = 0; i < n5; ++i) {
                    ghostSlots.setInput(list.get(i), contextMap, shapelessCraftingRecipeDisplay.ingredients().get(i));
                }
                break;
            }
        }
    }

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(FILTER_BUTTON_SPRITES);
    }

    @Override
    protected Component getRecipeFilterName() {
        return ONLY_CRAFTABLES_TOOLTIP;
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
        recipeCollection.selectRecipes(stackedItemContents, this::canDisplay);
    }
}

