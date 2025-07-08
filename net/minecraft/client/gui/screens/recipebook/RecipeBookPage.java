/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class RecipeBookPage {
    public static final int ITEMS_PER_PAGE = 20;
    private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("recipe_book/page_forward"), ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted"));
    private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("recipe_book/page_backward"), ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    private final List<RecipeButton> buttons = Lists.newArrayListWithCapacity((int)20);
    @Nullable
    private RecipeButton hoveredButton;
    private final OverlayRecipeComponent overlay;
    private Minecraft minecraft;
    private final RecipeBookComponent<?> parent;
    private List<RecipeCollection> recipeCollections = ImmutableList.of();
    private StateSwitchingButton forwardButton;
    private StateSwitchingButton backButton;
    private int totalPages;
    private int currentPage;
    private ClientRecipeBook recipeBook;
    @Nullable
    private RecipeDisplayId lastClickedRecipe;
    @Nullable
    private RecipeCollection lastClickedRecipeCollection;
    private boolean isFiltering;

    public RecipeBookPage(RecipeBookComponent<?> recipeBookComponent, SlotSelectTime slotSelectTime, boolean bl) {
        this.parent = recipeBookComponent;
        this.overlay = new OverlayRecipeComponent(slotSelectTime, bl);
        for (int i = 0; i < 20; ++i) {
            this.buttons.add(new RecipeButton(slotSelectTime));
        }
    }

    public void init(Minecraft minecraft, int n, int n2) {
        this.minecraft = minecraft;
        this.recipeBook = minecraft.player.getRecipeBook();
        for (int i = 0; i < this.buttons.size(); ++i) {
            this.buttons.get(i).setPosition(n + 11 + 25 * (i % 5), n2 + 31 + 25 * (i / 5));
        }
        this.forwardButton = new StateSwitchingButton(n + 93, n2 + 137, 12, 17, false);
        this.forwardButton.initTextureValues(PAGE_FORWARD_SPRITES);
        this.backButton = new StateSwitchingButton(n + 38, n2 + 137, 12, 17, true);
        this.backButton.initTextureValues(PAGE_BACKWARD_SPRITES);
    }

    public void updateCollections(List<RecipeCollection> list, boolean bl, boolean bl2) {
        this.recipeCollections = list;
        this.isFiltering = bl2;
        this.totalPages = (int)Math.ceil((double)list.size() / 20.0);
        if (this.totalPages <= this.currentPage || bl) {
            this.currentPage = 0;
        }
        this.updateButtonsForPage();
    }

    private void updateButtonsForPage() {
        int n = 20 * this.currentPage;
        ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);
        for (int i = 0; i < this.buttons.size(); ++i) {
            RecipeButton recipeButton = this.buttons.get(i);
            if (n + i < this.recipeCollections.size()) {
                RecipeCollection recipeCollection = this.recipeCollections.get(n + i);
                recipeButton.init(recipeCollection, this.isFiltering, this, contextMap);
                recipeButton.visible = true;
                continue;
            }
            recipeButton.visible = false;
        }
        this.updateArrowButtons();
    }

    private void updateArrowButtons() {
        this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
        this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, float f) {
        if (this.totalPages > 1) {
            MutableComponent mutableComponent = Component.translatable("gui.recipebook.page", this.currentPage + 1, this.totalPages);
            int n5 = this.minecraft.font.width(mutableComponent);
            guiGraphics.drawString(this.minecraft.font, mutableComponent, n - n5 / 2 + 73, n2 + 141, -1);
        }
        this.hoveredButton = null;
        for (RecipeButton recipeButton : this.buttons) {
            recipeButton.render(guiGraphics, n3, n4, f);
            if (!recipeButton.visible || !recipeButton.isHoveredOrFocused()) continue;
            this.hoveredButton = recipeButton;
        }
        this.backButton.render(guiGraphics, n3, n4, f);
        this.forwardButton.render(guiGraphics, n3, n4, f);
        guiGraphics.nextStratum();
        this.overlay.render(guiGraphics, n3, n4, f);
    }

    public void renderTooltip(GuiGraphics guiGraphics, int n, int n2) {
        if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
            ItemStack itemStack = this.hoveredButton.getDisplayStack();
            ResourceLocation resourceLocation = itemStack.get(DataComponents.TOOLTIP_STYLE);
            guiGraphics.setComponentTooltipForNextFrame(this.minecraft.font, this.hoveredButton.getTooltipText(itemStack), n, n2, resourceLocation);
        }
    }

    @Nullable
    public RecipeDisplayId getLastClickedRecipe() {
        return this.lastClickedRecipe;
    }

    @Nullable
    public RecipeCollection getLastClickedRecipeCollection() {
        return this.lastClickedRecipeCollection;
    }

    public void setInvisible() {
        this.overlay.setVisible(false);
    }

    public boolean mouseClicked(double d, double d2, int n, int n2, int n3, int n4, int n5) {
        this.lastClickedRecipe = null;
        this.lastClickedRecipeCollection = null;
        if (this.overlay.isVisible()) {
            if (this.overlay.mouseClicked(d, d2, n)) {
                this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
                this.lastClickedRecipeCollection = this.overlay.getRecipeCollection();
            } else {
                this.overlay.setVisible(false);
            }
            return true;
        }
        if (this.forwardButton.mouseClicked(d, d2, n)) {
            ++this.currentPage;
            this.updateButtonsForPage();
            return true;
        }
        if (this.backButton.mouseClicked(d, d2, n)) {
            --this.currentPage;
            this.updateButtonsForPage();
            return true;
        }
        ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);
        for (RecipeButton recipeButton : this.buttons) {
            if (!recipeButton.mouseClicked(d, d2, n)) continue;
            if (n == 0) {
                this.lastClickedRecipe = recipeButton.getCurrentRecipe();
                this.lastClickedRecipeCollection = recipeButton.getCollection();
            } else if (n == 1 && !this.overlay.isVisible() && !recipeButton.isOnlyOption()) {
                this.overlay.init(recipeButton.getCollection(), contextMap, this.isFiltering, recipeButton.getX(), recipeButton.getY(), n2 + n4 / 2, n3 + 13 + n5 / 2, recipeButton.getWidth());
            }
            return true;
        }
        return false;
    }

    public void recipeShown(RecipeDisplayId recipeDisplayId) {
        this.parent.recipeShown(recipeDisplayId);
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    protected void listButtons(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.forwardButton);
        consumer.accept(this.backButton);
        this.buttons.forEach(consumer);
    }
}

