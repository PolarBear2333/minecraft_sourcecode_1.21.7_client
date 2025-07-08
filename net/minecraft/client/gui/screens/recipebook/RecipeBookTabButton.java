/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

public class RecipeBookTabButton
extends StateSwitchingButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("recipe_book/tab"), ResourceLocation.withDefaultNamespace("recipe_book/tab_selected"));
    private final RecipeBookComponent.TabInfo tabInfo;
    private static final float ANIMATION_TIME = 15.0f;
    private float animationTime;

    public RecipeBookTabButton(RecipeBookComponent.TabInfo tabInfo) {
        super(0, 0, 35, 27, false);
        this.tabInfo = tabInfo;
        this.initTextureValues(SPRITES);
    }

    public void startAnimation(ClientRecipeBook clientRecipeBook, boolean bl) {
        RecipeCollection.CraftableStatus craftableStatus = bl ? RecipeCollection.CraftableStatus.CRAFTABLE : RecipeCollection.CraftableStatus.ANY;
        List<RecipeCollection> list = clientRecipeBook.getCollection(this.tabInfo.category());
        for (RecipeCollection recipeCollection : list) {
            for (RecipeDisplayEntry recipeDisplayEntry : recipeCollection.getSelectedRecipes(craftableStatus)) {
                if (!clientRecipeBook.willHighlight(recipeDisplayEntry.id())) continue;
                this.animationTime = 15.0f;
                return;
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.sprites == null) {
            return;
        }
        if (this.animationTime > 0.0f) {
            float f2 = 1.0f + 0.1f * (float)Math.sin(this.animationTime / 15.0f * (float)Math.PI);
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12));
            guiGraphics.pose().scale(1.0f, f2);
            guiGraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)));
        }
        ResourceLocation resourceLocation = this.sprites.get(true, this.isStateTriggered);
        int n3 = this.getX();
        if (this.isStateTriggered) {
            n3 -= 2;
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n3, this.getY(), this.width, this.height);
        this.renderIcon(guiGraphics);
        if (this.animationTime > 0.0f) {
            guiGraphics.pose().popMatrix();
            this.animationTime -= f;
        }
    }

    private void renderIcon(GuiGraphics guiGraphics) {
        int n;
        int n2 = n = this.isStateTriggered ? -2 : 0;
        if (this.tabInfo.secondaryIcon().isPresent()) {
            guiGraphics.renderFakeItem(this.tabInfo.primaryIcon(), this.getX() + 3 + n, this.getY() + 5);
            guiGraphics.renderFakeItem(this.tabInfo.secondaryIcon().get(), this.getX() + 14 + n, this.getY() + 5);
        } else {
            guiGraphics.renderFakeItem(this.tabInfo.primaryIcon(), this.getX() + 9 + n, this.getY() + 5);
        }
    }

    public ExtendedRecipeBookCategory getCategory() {
        return this.tabInfo.category();
    }

    public boolean updateVisibility(ClientRecipeBook clientRecipeBook) {
        List<RecipeCollection> list = clientRecipeBook.getCollection(this.tabInfo.category());
        this.visible = false;
        for (RecipeCollection recipeCollection : list) {
            if (!recipeCollection.hasAnySelected()) continue;
            this.visible = true;
            break;
        }
        return this.visible;
    }
}

