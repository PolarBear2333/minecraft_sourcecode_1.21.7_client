/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.toasts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class RecipeToast
implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/recipe");
    private static final long DISPLAY_TIME = 5000L;
    private static final Component TITLE_TEXT = Component.translatable("recipe.toast.title");
    private static final Component DESCRIPTION_TEXT = Component.translatable("recipe.toast.description");
    private final List<Entry> recipeItems = new ArrayList<Entry>();
    private long lastChanged;
    private boolean changed;
    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;
    private int displayedRecipeIndex;

    private RecipeToast() {
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager toastManager, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        this.wantedVisibility = this.recipeItems.isEmpty() ? Toast.Visibility.HIDE : ((double)(l - this.lastChanged) >= 5000.0 * toastManager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW);
        this.displayedRecipeIndex = (int)((double)l / Math.max(1.0, 5000.0 * toastManager.getNotificationDisplayTimeMultiplier() / (double)this.recipeItems.size()) % (double)this.recipeItems.size());
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long l) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        guiGraphics.drawString(font, TITLE_TEXT, 30, 7, -11534256, false);
        guiGraphics.drawString(font, DESCRIPTION_TEXT, 30, 18, -16777216, false);
        Entry entry = this.recipeItems.get(this.displayedRecipeIndex);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(0.6f, 0.6f);
        guiGraphics.renderFakeItem(entry.categoryItem(), 3, 3);
        guiGraphics.pose().popMatrix();
        guiGraphics.renderFakeItem(entry.unlockedItem(), 8, 8);
    }

    private void addItem(ItemStack itemStack, ItemStack itemStack2) {
        this.recipeItems.add(new Entry(itemStack, itemStack2));
        this.changed = true;
    }

    public static void addOrUpdate(ToastManager toastManager, RecipeDisplay recipeDisplay) {
        RecipeToast recipeToast = toastManager.getToast(RecipeToast.class, NO_TOKEN);
        if (recipeToast == null) {
            recipeToast = new RecipeToast();
            toastManager.addToast(recipeToast);
        }
        ContextMap contextMap = SlotDisplayContext.fromLevel(toastManager.getMinecraft().level);
        ItemStack itemStack = recipeDisplay.craftingStation().resolveForFirstStack(contextMap);
        ItemStack itemStack2 = recipeDisplay.result().resolveForFirstStack(contextMap);
        recipeToast.addItem(itemStack, itemStack2);
    }

    record Entry(ItemStack categoryItem, ItemStack unlockedItem) {
    }
}

