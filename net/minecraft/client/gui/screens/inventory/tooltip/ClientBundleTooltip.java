/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.math.Fraction
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

public class ClientBundleTooltip
implements ClientTooltipComponent {
    private static final ResourceLocation PROGRESSBAR_BORDER_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final ResourceLocation PROGRESSBAR_FILL_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final ResourceLocation PROGRESSBAR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final ResourceLocation SLOT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_background");
    private static final int SLOT_MARGIN = 4;
    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PROGRESSBAR_HEIGHT = 13;
    private static final int PROGRESSBAR_WIDTH = 96;
    private static final int PROGRESSBAR_BORDER = 1;
    private static final int PROGRESSBAR_FILL_MAX = 94;
    private static final int PROGRESSBAR_MARGIN_Y = 4;
    private static final Component BUNDLE_FULL_TEXT = Component.translatable("item.minecraft.bundle.full");
    private static final Component BUNDLE_EMPTY_TEXT = Component.translatable("item.minecraft.bundle.empty");
    private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item.minecraft.bundle.empty.description");
    private final BundleContents contents;

    public ClientBundleTooltip(BundleContents bundleContents) {
        this.contents = bundleContents;
    }

    @Override
    public int getHeight(Font font) {
        return this.contents.isEmpty() ? ClientBundleTooltip.getEmptyBundleBackgroundHeight(font) : this.backgroundHeight();
    }

    @Override
    public int getWidth(Font font) {
        return 96;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    private static int getEmptyBundleBackgroundHeight(Font font) {
        return ClientBundleTooltip.getEmptyBundleDescriptionTextHeight(font) + 13 + 8;
    }

    private int backgroundHeight() {
        return this.itemGridHeight() + 13 + 8;
    }

    private int itemGridHeight() {
        return this.gridSizeY() * 24;
    }

    private int getContentXOffset(int n) {
        return (n - 96) / 2;
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(this.slotCount(), 4);
    }

    private int slotCount() {
        return Math.min(12, this.contents.size());
    }

    @Override
    public void renderImage(Font font, int n, int n2, int n3, int n4, GuiGraphics guiGraphics) {
        if (this.contents.isEmpty()) {
            this.renderEmptyBundleTooltip(font, n, n2, n3, n4, guiGraphics);
        } else {
            this.renderBundleWithItemsTooltip(font, n, n2, n3, n4, guiGraphics);
        }
    }

    private void renderEmptyBundleTooltip(Font font, int n, int n2, int n3, int n4, GuiGraphics guiGraphics) {
        ClientBundleTooltip.drawEmptyBundleDescriptionText(n + this.getContentXOffset(n3), n2, font, guiGraphics);
        this.drawProgressbar(n + this.getContentXOffset(n3), n2 + ClientBundleTooltip.getEmptyBundleDescriptionTextHeight(font) + 4, font, guiGraphics);
    }

    private void renderBundleWithItemsTooltip(Font font, int n, int n2, int n3, int n4, GuiGraphics guiGraphics) {
        boolean bl = this.contents.size() > 12;
        List<ItemStack> list = this.getShownItems(this.contents.getNumberOfItemsToShow());
        int n5 = n + this.getContentXOffset(n3) + 96;
        int n6 = n2 + this.gridSizeY() * 24;
        int n7 = 1;
        for (int i = 1; i <= this.gridSizeY(); ++i) {
            for (int j = 1; j <= 4; ++j) {
                int n8 = n5 - j * 24;
                int n9 = n6 - i * 24;
                if (ClientBundleTooltip.shouldRenderSurplusText(bl, j, i)) {
                    ClientBundleTooltip.renderCount(n8, n9, this.getAmountOfHiddenItems(list), font, guiGraphics);
                    continue;
                }
                if (!ClientBundleTooltip.shouldRenderItemSlot(list, n7)) continue;
                this.renderSlot(n7, n8, n9, list, n7, font, guiGraphics);
                ++n7;
            }
        }
        this.drawSelectedItemTooltip(font, guiGraphics, n, n2, n3);
        this.drawProgressbar(n + this.getContentXOffset(n3), n2 + this.itemGridHeight() + 4, font, guiGraphics);
    }

    private List<ItemStack> getShownItems(int n) {
        int n2 = Math.min(this.contents.size(), n);
        return this.contents.itemCopyStream().toList().subList(0, n2);
    }

    private static boolean shouldRenderSurplusText(boolean bl, int n, int n2) {
        return bl && n * n2 == 1;
    }

    private static boolean shouldRenderItemSlot(List<ItemStack> list, int n) {
        return list.size() >= n;
    }

    private int getAmountOfHiddenItems(List<ItemStack> list) {
        return this.contents.itemCopyStream().skip(list.size()).mapToInt(ItemStack::getCount).sum();
    }

    private void renderSlot(int n, int n2, int n3, List<ItemStack> list, int n4, Font font, GuiGraphics guiGraphics) {
        int n5 = list.size() - n;
        boolean bl = n5 == this.contents.getSelectedItem();
        ItemStack itemStack = list.get(n5);
        if (bl) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, n2, n3, 24, 24);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, n2, n3, 24, 24);
        }
        guiGraphics.renderItem(itemStack, n2 + 4, n3 + 4, n4);
        guiGraphics.renderItemDecorations(font, itemStack, n2 + 4, n3 + 4);
        if (bl) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, n2, n3, 24, 24);
        }
    }

    private static void renderCount(int n, int n2, int n3, Font font, GuiGraphics guiGraphics) {
        guiGraphics.drawCenteredString(font, "+" + n3, n + 12, n2 + 10, -1);
    }

    private void drawSelectedItemTooltip(Font font, GuiGraphics guiGraphics, int n, int n2, int n3) {
        if (this.contents.hasSelectedItem()) {
            ItemStack itemStack = this.contents.getItemUnsafe(this.contents.getSelectedItem());
            Component component = itemStack.getStyledHoverName();
            int n4 = font.width(component.getVisualOrderText());
            int n5 = n + n3 / 2 - 12;
            ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(component.getVisualOrderText());
            guiGraphics.renderTooltip(font, List.of(clientTooltipComponent), n5 - n4 / 2, n2 - 15, DefaultTooltipPositioner.INSTANCE, itemStack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    private void drawProgressbar(int n, int n2, Font font, GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getProgressBarTexture(), n + 1, n2, this.getProgressBarFill(), 13);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, n, n2, 96, 13);
        Component component = this.getProgressBarFillText();
        if (component != null) {
            guiGraphics.drawCenteredString(font, component, n + 48, n2 + 3, -1);
        }
    }

    private static void drawEmptyBundleDescriptionText(int n, int n2, Font font, GuiGraphics guiGraphics) {
        guiGraphics.drawWordWrap(font, BUNDLE_EMPTY_DESCRIPTION, n, n2, 96, -5592406);
    }

    private static int getEmptyBundleDescriptionTextHeight(Font font) {
        return font.split(BUNDLE_EMPTY_DESCRIPTION, 96).size() * font.lineHeight;
    }

    private int getProgressBarFill() {
        return Mth.clamp(Mth.mulAndTruncate(this.contents.weight(), 94), 0, 94);
    }

    private ResourceLocation getProgressBarTexture() {
        return this.contents.weight().compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
    }

    @Nullable
    private Component getProgressBarFillText() {
        if (this.contents.isEmpty()) {
            return BUNDLE_EMPTY_TEXT;
        }
        if (this.contents.weight().compareTo(Fraction.ONE) >= 0) {
            return BUNDLE_FULL_TEXT;
        }
        return null;
    }
}

