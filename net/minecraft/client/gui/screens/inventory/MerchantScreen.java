/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantScreen
extends AbstractContainerScreen<MerchantMenu> {
    private static final ResourceLocation OUT_OF_STOCK_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/out_of_stock");
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_CURRENT_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/experience_bar_current");
    private static final ResourceLocation EXPERIENCE_BAR_RESULT_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/experience_bar_result");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller_disabled");
    private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
    private static final ResourceLocation TRADE_ARROW_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow");
    private static final ResourceLocation DISCOUNT_STRIKETHRUOGH_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/discount_strikethrough");
    private static final ResourceLocation VILLAGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/villager.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int MERCHANT_MENU_PART_X = 99;
    private static final int PROGRESS_BAR_X = 136;
    private static final int PROGRESS_BAR_Y = 16;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_OFFER_BUTTONS = 7;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_HEIGHT = 20;
    private static final int TRADE_BUTTON_WIDTH = 88;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = 139;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 94;
    private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
    private static final Component DEPRECATED_TOOLTIP = Component.translatable("merchant.deprecated");
    private int shopItem;
    private final TradeOfferButton[] tradeOfferButtons = new TradeOfferButton[7];
    int scrollOff;
    private boolean isDragging;

    public MerchantScreen(MerchantMenu merchantMenu, Inventory inventory, Component component) {
        super(merchantMenu, inventory, component);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
    }

    private void postButtonClick() {
        ((MerchantMenu)this.menu).setSelectionHint(this.shopItem);
        ((MerchantMenu)this.menu).tryMoveItems(this.shopItem);
        this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
    }

    @Override
    protected void init() {
        super.init();
        int n = (this.width - this.imageWidth) / 2;
        int n2 = (this.height - this.imageHeight) / 2;
        int n3 = n2 + 16 + 2;
        for (int i = 0; i < 7; ++i) {
            this.tradeOfferButtons[i] = this.addRenderableWidget(new TradeOfferButton(n + 5, n3, i, button -> {
                if (button instanceof TradeOfferButton) {
                    this.shopItem = ((TradeOfferButton)button).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }
            }));
            n3 += 20;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        int n3 = ((MerchantMenu)this.menu).getTraderLevel();
        if (n3 > 0 && n3 <= 5 && ((MerchantMenu)this.menu).showProgressBar()) {
            MutableComponent mutableComponent = Component.translatable("merchant.title", this.title, Component.translatable("merchant.level." + n3));
            int n4 = this.font.width(mutableComponent);
            int n5 = 49 + this.imageWidth / 2 - n4 / 2;
            guiGraphics.drawString(this.font, mutableComponent, n5, 6, -12566464, false);
        } else {
            guiGraphics.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, 6, -12566464, false);
        }
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
        int n6 = this.font.width(TRADES_LABEL);
        guiGraphics.drawString(this.font, TRADES_LABEL, 5 - n6 / 2 + 48, 6, -12566464, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, VILLAGER_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 512, 256);
        MerchantOffers merchantOffers = ((MerchantMenu)this.menu).getOffers();
        if (!merchantOffers.isEmpty()) {
            int n5 = this.shopItem;
            if (n5 < 0 || n5 >= merchantOffers.size()) {
                return;
            }
            MerchantOffer merchantOffer = (MerchantOffer)merchantOffers.get(n5);
            if (merchantOffer.isOutOfStock()) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, OUT_OF_STOCK_SPRITE, this.leftPos + 83 + 99, this.topPos + 35, 28, 21);
            }
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int n, int n2, MerchantOffer merchantOffer) {
        int n3 = ((MerchantMenu)this.menu).getTraderLevel();
        int n4 = ((MerchantMenu)this.menu).getTraderXp();
        if (n3 >= 5) {
            return;
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND_SPRITE, n + 136, n2 + 16, 102, 5);
        int n5 = VillagerData.getMinXpPerLevel(n3);
        if (n4 < n5 || !VillagerData.canLevelUp(n3)) {
            return;
        }
        int n6 = 102;
        float f = 102.0f / (float)(VillagerData.getMaxXpPerLevel(n3) - n5);
        int n7 = Math.min(Mth.floor(f * (float)(n4 - n5)), 102);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_CURRENT_SPRITE, 102, 5, 0, 0, n + 136, n2 + 16, n7, 5);
        int n8 = ((MerchantMenu)this.menu).getFutureTraderXp();
        if (n8 > 0) {
            int n9 = Math.min(Mth.floor((float)n8 * f), 102 - n7);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_RESULT_SPRITE, 102, 5, n7, 0, n + 136 + n7, n2 + 16, n9, 5);
        }
    }

    private void renderScroller(GuiGraphics guiGraphics, int n, int n2, MerchantOffers merchantOffers) {
        int n3 = merchantOffers.size() + 1 - 7;
        if (n3 > 1) {
            int n4 = 139 - (27 + (n3 - 1) * 139 / n3);
            int n5 = 1 + n4 / n3 + 139 / n3;
            int n6 = 113;
            int n7 = Math.min(113, this.scrollOff * n5);
            if (this.scrollOff == n3 - 1) {
                n7 = 113;
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, n + 94, n2 + 18 + n7, 6, 27);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE, n + 94, n2 + 18, 6, 27);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        MerchantOffers merchantOffers = ((MerchantMenu)this.menu).getOffers();
        if (!merchantOffers.isEmpty()) {
            MerchantOffer merchantOffer22;
            int n3 = (this.width - this.imageWidth) / 2;
            int n4 = (this.height - this.imageHeight) / 2;
            int n5 = n4 + 16 + 1;
            int n6 = n3 + 5 + 5;
            this.renderScroller(guiGraphics, n3, n4, merchantOffers);
            int n7 = 0;
            for (MerchantOffer merchantOffer22 : merchantOffers) {
                if (this.canScroll(merchantOffers.size()) && (n7 < this.scrollOff || n7 >= 7 + this.scrollOff)) {
                    ++n7;
                    continue;
                }
                TradeOfferButton[] tradeOfferButtonArray = merchantOffer22.getBaseCostA();
                ItemStack itemStack = merchantOffer22.getCostA();
                ItemStack itemStack2 = merchantOffer22.getCostB();
                ItemStack object = merchantOffer22.getResult();
                int n8 = n5 + 2;
                this.renderAndDecorateCostA(guiGraphics, itemStack, (ItemStack)tradeOfferButtonArray, n6, n8);
                if (!itemStack2.isEmpty()) {
                    guiGraphics.renderFakeItem(itemStack2, n3 + 5 + 35, n8);
                    guiGraphics.renderItemDecorations(this.font, itemStack2, n3 + 5 + 35, n8);
                }
                this.renderButtonArrows(guiGraphics, merchantOffer22, n3, n8);
                guiGraphics.renderFakeItem(object, n3 + 5 + 68, n8);
                guiGraphics.renderItemDecorations(this.font, object, n3 + 5 + 68, n8);
                n5 += 20;
                ++n7;
            }
            int n9 = this.shopItem;
            merchantOffer22 = (MerchantOffer)merchantOffers.get(n9);
            if (((MerchantMenu)this.menu).showProgressBar()) {
                this.renderProgressBar(guiGraphics, n3, n4, merchantOffer22);
            }
            if (merchantOffer22.isOutOfStock() && this.isHovering(186, 35, 22, 21, n, n2) && ((MerchantMenu)this.menu).canRestock()) {
                guiGraphics.setTooltipForNextFrame(this.font, DEPRECATED_TOOLTIP, n, n2);
            }
            for (TradeOfferButton tradeOfferButton : this.tradeOfferButtons) {
                if (tradeOfferButton.isHoveredOrFocused()) {
                    tradeOfferButton.renderToolTip(guiGraphics, n, n2);
                }
                tradeOfferButton.visible = tradeOfferButton.index < ((MerchantMenu)this.menu).getOffers().size();
            }
        }
        this.renderTooltip(guiGraphics, n, n2);
    }

    private void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer merchantOffer, int n, int n2) {
        if (merchantOffer.isOutOfStock()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRADE_ARROW_OUT_OF_STOCK_SPRITE, n + 5 + 35 + 20, n2 + 3, 10, 9);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRADE_ARROW_SPRITE, n + 5 + 35 + 20, n2 + 3, 10, 9);
        }
    }

    private void renderAndDecorateCostA(GuiGraphics guiGraphics, ItemStack itemStack, ItemStack itemStack2, int n, int n2) {
        guiGraphics.renderFakeItem(itemStack, n, n2);
        if (itemStack2.getCount() == itemStack.getCount()) {
            guiGraphics.renderItemDecorations(this.font, itemStack, n, n2);
        } else {
            guiGraphics.renderItemDecorations(this.font, itemStack2, n, n2, itemStack2.getCount() == 1 ? "1" : null);
            guiGraphics.renderItemDecorations(this.font, itemStack, n + 14, n2, itemStack.getCount() == 1 ? "1" : null);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISCOUNT_STRIKETHRUOGH_SPRITE, n + 7, n2 + 12, 9, 2);
        }
    }

    private boolean canScroll(int n) {
        return n > 7;
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (super.mouseScrolled(d, d2, d3, d4)) {
            return true;
        }
        int n = ((MerchantMenu)this.menu).getOffers().size();
        if (this.canScroll(n)) {
            int n2 = n - 7;
            this.scrollOff = Mth.clamp((int)((double)this.scrollOff - d4), 0, n2);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        int n2 = ((MerchantMenu)this.menu).getOffers().size();
        if (this.isDragging) {
            int n3 = this.topPos + 18;
            int n4 = n3 + 139;
            int n5 = n2 - 7;
            float f = ((float)d2 - (float)n3 - 13.5f) / ((float)(n4 - n3) - 27.0f);
            f = f * (float)n5 + 0.5f;
            this.scrollOff = Mth.clamp((int)f, 0, n5);
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        this.isDragging = false;
        int n2 = (this.width - this.imageWidth) / 2;
        int n3 = (this.height - this.imageHeight) / 2;
        if (this.canScroll(((MerchantMenu)this.menu).getOffers().size()) && d > (double)(n2 + 94) && d < (double)(n2 + 94 + 6) && d2 > (double)(n3 + 18) && d2 <= (double)(n3 + 18 + 139 + 1)) {
            this.isDragging = true;
        }
        return super.mouseClicked(d, d2, n);
    }

    class TradeOfferButton
    extends Button {
        final int index;

        public TradeOfferButton(int n, int n2, int n3, Button.OnPress onPress) {
            super(n, n2, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = n3;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics guiGraphics, int n, int n2) {
            if (this.isHovered && ((MerchantMenu)MerchantScreen.this.menu).getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
                if (n < this.getX() + 20) {
                    ItemStack itemStack = ((MerchantOffer)((MerchantMenu)MerchantScreen.this.menu).getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostA();
                    guiGraphics.setTooltipForNextFrame(MerchantScreen.this.font, itemStack, n, n2);
                } else if (n < this.getX() + 50 && n > this.getX() + 30) {
                    ItemStack itemStack = ((MerchantOffer)((MerchantMenu)MerchantScreen.this.menu).getOffers().get(this.index + MerchantScreen.this.scrollOff)).getCostB();
                    if (!itemStack.isEmpty()) {
                        guiGraphics.setTooltipForNextFrame(MerchantScreen.this.font, itemStack, n, n2);
                    }
                } else if (n > this.getX() + 65) {
                    ItemStack itemStack = ((MerchantOffer)((MerchantMenu)MerchantScreen.this.menu).getOffers().get(this.index + MerchantScreen.this.scrollOff)).getResult();
                    guiGraphics.setTooltipForNextFrame(MerchantScreen.this.font, itemStack, n, n2);
                }
            }
        }
    }
}

