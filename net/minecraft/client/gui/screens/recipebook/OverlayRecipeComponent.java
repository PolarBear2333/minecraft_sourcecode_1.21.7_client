/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class OverlayRecipeComponent
implements Renderable,
GuiEventListener {
    private static final ResourceLocation OVERLAY_RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe");
    private static final int MAX_ROW = 4;
    private static final int MAX_ROW_LARGE = 5;
    private static final float ITEM_RENDER_SCALE = 0.375f;
    public static final int BUTTON_SIZE = 25;
    private final List<OverlayRecipeButton> recipeButtons = Lists.newArrayList();
    private boolean isVisible;
    private int x;
    private int y;
    private RecipeCollection collection = RecipeCollection.EMPTY;
    @Nullable
    private RecipeDisplayId lastRecipeClicked;
    final SlotSelectTime slotSelectTime;
    private final boolean isFurnaceMenu;

    public OverlayRecipeComponent(SlotSelectTime slotSelectTime, boolean bl) {
        this.slotSelectTime = slotSelectTime;
        this.isFurnaceMenu = bl;
    }

    public void init(RecipeCollection recipeCollection, ContextMap contextMap, boolean bl, int n, int n2, int n3, int n4, float f) {
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        this.collection = recipeCollection;
        List<RecipeDisplayEntry> list = recipeCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.CRAFTABLE);
        List list2 = bl ? Collections.emptyList() : recipeCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.NOT_CRAFTABLE);
        int n5 = list.size();
        int n6 = n5 + list2.size();
        int n7 = n6 <= 16 ? 4 : 5;
        int n8 = (int)Math.ceil((float)n6 / (float)n7);
        this.x = n;
        this.y = n2;
        float f7 = this.x + Math.min(n6, n7) * 25;
        if (f7 > (f6 = (float)(n3 + 50))) {
            this.x = (int)((float)this.x - f * (float)((int)((f7 - f6) / f)));
        }
        if ((f5 = (float)(this.y + n8 * 25)) > (f4 = (float)(n4 + 50))) {
            this.y = (int)((float)this.y - f * (float)Mth.ceil((f5 - f4) / f));
        }
        if ((f3 = (float)this.y) < (f2 = (float)(n4 - 100))) {
            this.y = (int)((float)this.y - f * (float)Mth.ceil((f3 - f2) / f));
        }
        this.isVisible = true;
        this.recipeButtons.clear();
        for (int i = 0; i < n6; ++i) {
            boolean bl2 = i < n5;
            RecipeDisplayEntry recipeDisplayEntry = bl2 ? list.get(i) : (RecipeDisplayEntry)list2.get(i - n5);
            int n9 = this.x + 4 + 25 * (i % n7);
            int n10 = this.y + 5 + 25 * (i / n7);
            if (this.isFurnaceMenu) {
                this.recipeButtons.add(new OverlaySmeltingRecipeButton(this, n9, n10, recipeDisplayEntry.id(), recipeDisplayEntry.display(), contextMap, bl2));
                continue;
            }
            this.recipeButtons.add(new OverlayCraftingRecipeButton(this, n9, n10, recipeDisplayEntry.id(), recipeDisplayEntry.display(), contextMap, bl2));
        }
        this.lastRecipeClicked = null;
    }

    public RecipeCollection getRecipeCollection() {
        return this.collection;
    }

    @Nullable
    public RecipeDisplayId getLastRecipeClicked() {
        return this.lastRecipeClicked;
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (n != 0) {
            return false;
        }
        for (OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
            if (!overlayRecipeButton.mouseClicked(d, d2, n)) continue;
            this.lastRecipeClicked = overlayRecipeButton.recipe;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (!this.isVisible) {
            return;
        }
        int n3 = this.recipeButtons.size() <= 16 ? 4 : 5;
        int n4 = Math.min(this.recipeButtons.size(), n3);
        int n5 = Mth.ceil((float)this.recipeButtons.size() / (float)n3);
        int n6 = 4;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, OVERLAY_RECIPE_SPRITE, this.x, this.y, n4 * 25 + 8, n5 * 25 + 8);
        for (OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
            overlayRecipeButton.render(guiGraphics, n, n2, f);
        }
    }

    public void setVisible(boolean bl) {
        this.isVisible = bl;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public void setFocused(boolean bl) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    class OverlaySmeltingRecipeButton
    extends OverlayRecipeButton {
        private static final ResourceLocation ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay");
        private static final ResourceLocation HIGHLIGHTED_ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay_highlighted");
        private static final ResourceLocation DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay_disabled");
        private static final ResourceLocation HIGHLIGHTED_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay_disabled_highlighted");

        public OverlaySmeltingRecipeButton(OverlayRecipeComponent overlayRecipeComponent, int n, int n2, RecipeDisplayId recipeDisplayId, RecipeDisplay recipeDisplay, ContextMap contextMap, boolean bl) {
            super(n, n2, recipeDisplayId, bl, OverlaySmeltingRecipeButton.calculateIngredientsPositions(recipeDisplay, contextMap));
        }

        private static List<OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeDisplay recipeDisplay, ContextMap contextMap) {
            FurnaceRecipeDisplay furnaceRecipeDisplay;
            List<ItemStack> list;
            if (recipeDisplay instanceof FurnaceRecipeDisplay && !(list = (furnaceRecipeDisplay = (FurnaceRecipeDisplay)recipeDisplay).ingredient().resolveForStacks(contextMap)).isEmpty()) {
                return List.of(OverlaySmeltingRecipeButton.createGridPos(1, 1, list));
            }
            return List.of();
        }

        @Override
        protected ResourceLocation getSprite(boolean bl) {
            if (bl) {
                return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
            }
            return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
        }
    }

    class OverlayCraftingRecipeButton
    extends OverlayRecipeButton {
        private static final ResourceLocation ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay");
        private static final ResourceLocation HIGHLIGHTED_ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay_highlighted");
        private static final ResourceLocation DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay_disabled");
        private static final ResourceLocation HIGHLIGHTED_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay_disabled_highlighted");
        private static final int GRID_WIDTH = 3;
        private static final int GRID_HEIGHT = 3;

        public OverlayCraftingRecipeButton(OverlayRecipeComponent overlayRecipeComponent, int n, int n2, RecipeDisplayId recipeDisplayId, RecipeDisplay recipeDisplay, ContextMap contextMap, boolean bl) {
            super(n, n2, recipeDisplayId, bl, OverlayCraftingRecipeButton.calculateIngredientsPositions(recipeDisplay, contextMap));
        }

        private static List<OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeDisplay recipeDisplay, ContextMap contextMap) {
            ArrayList<OverlayRecipeButton.Pos> arrayList = new ArrayList<OverlayRecipeButton.Pos>();
            RecipeDisplay recipeDisplay2 = recipeDisplay;
            Objects.requireNonNull(recipeDisplay2);
            RecipeDisplay recipeDisplay3 = recipeDisplay2;
            int n4 = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay3, n4)) {
                case 0: {
                    ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay = (ShapedCraftingRecipeDisplay)recipeDisplay3;
                    PlaceRecipeHelper.placeRecipe(3, 3, shapedCraftingRecipeDisplay.width(), shapedCraftingRecipeDisplay.height(), shapedCraftingRecipeDisplay.ingredients(), (slotDisplay, n, n2, n3) -> {
                        List<ItemStack> list2 = slotDisplay.resolveForStacks(contextMap);
                        if (!list2.isEmpty()) {
                            arrayList.add(OverlayCraftingRecipeButton.createGridPos(n2, n3, list2));
                        }
                    });
                    break;
                }
                case 1: {
                    ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay = (ShapelessCraftingRecipeDisplay)recipeDisplay3;
                    List<SlotDisplay> list = shapelessCraftingRecipeDisplay.ingredients();
                    for (int i = 0; i < list.size(); ++i) {
                        List<ItemStack> list2 = list.get(i).resolveForStacks(contextMap);
                        if (list2.isEmpty()) continue;
                        arrayList.add(OverlayCraftingRecipeButton.createGridPos(i % 3, i / 3, list2));
                    }
                    break;
                }
            }
            return arrayList;
        }

        @Override
        protected ResourceLocation getSprite(boolean bl) {
            if (bl) {
                return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
            }
            return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
        }
    }

    abstract class OverlayRecipeButton
    extends AbstractWidget {
        final RecipeDisplayId recipe;
        private final boolean isCraftable;
        private final List<Pos> slots;

        public OverlayRecipeButton(int n, int n2, RecipeDisplayId recipeDisplayId, boolean bl, List<Pos> list) {
            super(n, n2, 24, 24, CommonComponents.EMPTY);
            this.slots = list;
            this.recipe = recipeDisplayId;
            this.isCraftable = bl;
        }

        protected static Pos createGridPos(int n, int n2, List<ItemStack> list) {
            return new Pos(3 + n * 7, 3 + n2 * 7, list);
        }

        protected abstract ResourceLocation getSprite(boolean var1);

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(this.isCraftable), this.getX(), this.getY(), this.width, this.height);
            float f2 = this.getX() + 2;
            float f3 = this.getY() + 2;
            for (Pos pos : this.slots) {
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(f2 + (float)pos.x, f3 + (float)pos.y);
                guiGraphics.pose().scale(0.375f, 0.375f);
                guiGraphics.pose().translate(-8.0f, -8.0f);
                guiGraphics.renderItem(pos.selectIngredient(OverlayRecipeComponent.this.slotSelectTime.currentIndex()), 0, 0);
                guiGraphics.pose().popMatrix();
            }
        }

        protected static final class Pos
        extends Record {
            final int x;
            final int y;
            private final List<ItemStack> ingredients;

            public Pos(int n, int n2, List<ItemStack> list) {
                if (list.isEmpty()) {
                    throw new IllegalArgumentException("Ingredient list must be non-empty");
                }
                this.x = n;
                this.y = n2;
                this.ingredients = list;
            }

            public ItemStack selectIngredient(int n) {
                return this.ingredients.get(n % this.ingredients.size());
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Pos.class, "x;y;ingredients", "x", "y", "ingredients"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Pos.class, "x;y;ingredients", "x", "y", "ingredients"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Pos.class, "x;y;ingredients", "x", "y", "ingredients"}, this, object);
            }

            public int x() {
                return this.x;
            }

            public int y() {
                return this.y;
            }

            public List<ItemStack> ingredients() {
                return this.ingredients;
            }
        }
    }
}

