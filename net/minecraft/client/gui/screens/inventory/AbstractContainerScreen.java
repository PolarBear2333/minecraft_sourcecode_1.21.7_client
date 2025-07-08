/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 *  org.joml.Vector2i
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

public abstract class AbstractContainerScreen<T extends AbstractContainerMenu>
extends Screen
implements MenuAccess<T> {
    public static final ResourceLocation INVENTORY_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");
    protected static final int BACKGROUND_TEXTURE_WIDTH = 256;
    protected static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private static final float SNAPBACK_SPEED = 100.0f;
    private static final int QUICKDROP_DELAY = 500;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int titleLabelX;
    protected int titleLabelY;
    protected int inventoryLabelX;
    protected int inventoryLabelY;
    private final List<ItemSlotMouseAction> itemSlotMouseActions;
    protected final T menu;
    protected final Component playerInventoryTitle;
    @Nullable
    protected Slot hoveredSlot;
    @Nullable
    private Slot clickedSlot;
    @Nullable
    private Slot quickdropSlot;
    @Nullable
    private Slot lastClickSlot;
    @Nullable
    private SnapbackData snapbackData;
    protected int leftPos;
    protected int topPos;
    private boolean isSplittingStack;
    private ItemStack draggingItem = ItemStack.EMPTY;
    private long quickdropTime;
    protected final Set<Slot> quickCraftSlots = Sets.newHashSet();
    protected boolean isQuickCrafting;
    private int quickCraftingType;
    private int quickCraftingButton;
    private boolean skipNextRelease;
    private int quickCraftingRemainder;
    private long lastClickTime;
    private int lastClickButton;
    private boolean doubleclick;
    private ItemStack lastQuickMoved = ItemStack.EMPTY;

    public AbstractContainerScreen(T t, Inventory inventory, Component component) {
        super(component);
        this.menu = t;
        this.playerInventoryTitle = inventory.getDisplayName();
        this.skipNextRelease = true;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
        this.itemSlotMouseActions = new ArrayList<ItemSlotMouseAction>();
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.itemSlotMouseActions.clear();
        this.addItemSlotMouseAction(new BundleMouseActions(this.minecraft));
    }

    protected void addItemSlotMouseAction(ItemSlotMouseAction itemSlotMouseAction) {
        this.itemSlotMouseActions.add(itemSlotMouseAction);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderContents(guiGraphics, n, n2, f);
        this.renderCarriedItem(guiGraphics, n, n2);
        this.renderSnapbackItem(guiGraphics);
    }

    public void renderContents(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = this.leftPos;
        int n4 = this.topPos;
        super.render(guiGraphics, n, n2, f);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)n3, (float)n4);
        this.renderLabels(guiGraphics, n, n2);
        Slot slot = this.hoveredSlot;
        this.hoveredSlot = this.getHoveredSlot(n, n2);
        this.renderSlotHighlightBack(guiGraphics);
        this.renderSlots(guiGraphics);
        this.renderSlotHighlightFront(guiGraphics);
        if (slot != null && slot != this.hoveredSlot) {
            this.onStopHovering(slot);
        }
        guiGraphics.pose().popMatrix();
    }

    public void renderCarriedItem(GuiGraphics guiGraphics, int n, int n2) {
        ItemStack itemStack;
        ItemStack itemStack2 = itemStack = this.draggingItem.isEmpty() ? ((AbstractContainerMenu)this.menu).getCarried() : this.draggingItem;
        if (!itemStack.isEmpty()) {
            int n3 = 8;
            int n4 = this.draggingItem.isEmpty() ? 8 : 16;
            String string = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemStack = itemStack.copyWithCount(Mth.ceil((float)itemStack.getCount() / 2.0f));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1 && (itemStack = itemStack.copyWithCount(this.quickCraftingRemainder)).isEmpty()) {
                string = String.valueOf(ChatFormatting.YELLOW) + "0";
            }
            guiGraphics.nextStratum();
            this.renderFloatingItem(guiGraphics, itemStack, n - 8, n2 - n4, string);
        }
    }

    public void renderSnapbackItem(GuiGraphics guiGraphics) {
        if (this.snapbackData != null) {
            float f = Mth.clamp((float)(Util.getMillis() - this.snapbackData.time) / 100.0f, 0.0f, 1.0f);
            int n = this.snapbackData.end.x - this.snapbackData.start.x;
            int n2 = this.snapbackData.end.y - this.snapbackData.start.y;
            int n3 = this.snapbackData.start.x + (int)((float)n * f);
            int n4 = this.snapbackData.start.y + (int)((float)n2 * f);
            guiGraphics.nextStratum();
            this.renderFloatingItem(guiGraphics, this.snapbackData.item, n3, n4, null);
            if (f >= 1.0f) {
                this.snapbackData = null;
            }
        }
    }

    protected void renderSlots(GuiGraphics guiGraphics) {
        for (Slot slot : ((AbstractContainerMenu)this.menu).slots) {
            if (!slot.isActive()) continue;
            this.renderSlot(guiGraphics, slot);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
        this.renderBg(guiGraphics, f, n, n2);
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            for (ItemSlotMouseAction itemSlotMouseAction : this.itemSlotMouseActions) {
                if (!itemSlotMouseAction.matches(this.hoveredSlot) || !itemSlotMouseAction.onMouseScrolled(d3, d4, this.hoveredSlot.index, this.hoveredSlot.getItem())) continue;
                return true;
            }
        }
        return false;
    }

    private void renderSlotHighlightBack(GuiGraphics guiGraphics) {
        if (this.hoveredSlot != null && this.hoveredSlot.isHighlightable()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, this.hoveredSlot.x - 4, this.hoveredSlot.y - 4, 24, 24);
        }
    }

    private void renderSlotHighlightFront(GuiGraphics guiGraphics) {
        if (this.hoveredSlot != null && this.hoveredSlot.isHighlightable()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, this.hoveredSlot.x - 4, this.hoveredSlot.y - 4, 24, 24);
        }
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int n, int n2) {
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) {
            return;
        }
        ItemStack itemStack = this.hoveredSlot.getItem();
        if (((AbstractContainerMenu)this.menu).getCarried().isEmpty() || this.showTooltipWithItemInHand(itemStack)) {
            guiGraphics.setTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(itemStack), itemStack.getTooltipImage(), n, n2, itemStack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    private boolean showTooltipWithItemInHand(ItemStack itemStack) {
        return itemStack.getTooltipImage().map(ClientTooltipComponent::create).map(ClientTooltipComponent::showTooltipWithItemInHand).orElse(false);
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
        return AbstractContainerScreen.getTooltipFromItem(this.minecraft, itemStack);
    }

    private void renderFloatingItem(GuiGraphics guiGraphics, ItemStack itemStack, int n, int n2, @Nullable String string) {
        guiGraphics.renderItem(itemStack, n, n2);
        guiGraphics.renderItemDecorations(this.font, itemStack, n, n2 - (this.draggingItem.isEmpty() ? 0 : 8), string);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    protected abstract void renderBg(GuiGraphics var1, float var2, int var3, int var4);

    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        ResourceLocation resourceLocation;
        int n;
        int n2 = slot.x;
        int n3 = slot.y;
        ItemStack itemStack = slot.getItem();
        boolean bl = false;
        boolean bl2 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemStack2 = ((AbstractContainerMenu)this.menu).getCarried();
        String string = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemStack2.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }
            if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack2, true) && ((AbstractContainerMenu)this.menu).canDragTo(slot)) {
                bl = true;
                n = Math.min(itemStack2.getMaxStackSize(), slot.getMaxStackSize(itemStack2));
                int n4 = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int n5 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack2) + n4;
                if (n5 > n) {
                    n5 = n;
                    string = ChatFormatting.YELLOW.toString() + n;
                }
                itemStack = itemStack2.copyWithCount(n5);
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }
        if (itemStack.isEmpty() && slot.isActive() && (resourceLocation = slot.getNoItemIcon()) != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n2, n3, 16, 16);
            bl2 = true;
        }
        if (!bl2) {
            if (bl) {
                guiGraphics.fill(n2, n3, n2 + 16, n3 + 16, -2130706433);
            }
            n = slot.x + slot.y * this.imageWidth;
            if (slot.isFake()) {
                guiGraphics.renderFakeItem(itemStack, n2, n3, n);
            } else {
                guiGraphics.renderItem(itemStack, n2, n3, n);
            }
            guiGraphics.renderItemDecorations(this.font, itemStack, n2, n3, string);
        }
    }

    private void recalculateQuickCraftRemaining() {
        ItemStack itemStack = ((AbstractContainerMenu)this.menu).getCarried();
        if (itemStack.isEmpty() || !this.isQuickCrafting) {
            return;
        }
        if (this.quickCraftingType == 2) {
            this.quickCraftingRemainder = itemStack.getMaxStackSize();
            return;
        }
        this.quickCraftingRemainder = itemStack.getCount();
        for (Slot slot : this.quickCraftSlots) {
            ItemStack itemStack2 = slot.getItem();
            int n = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
            int n2 = Math.min(itemStack.getMaxStackSize(), slot.getMaxStackSize(itemStack));
            int n3 = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack) + n, n2);
            this.quickCraftingRemainder -= n3 - n;
        }
    }

    @Nullable
    private Slot getHoveredSlot(double d, double d2) {
        for (Slot slot : ((AbstractContainerMenu)this.menu).slots) {
            if (!slot.isActive() || !this.isHovering(slot, d, d2)) continue;
            return slot;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (super.mouseClicked(d, d2, n)) {
            return true;
        }
        boolean bl = this.minecraft.options.keyPickItem.matchesMouse(n) && this.minecraft.player.hasInfiniteMaterials();
        Slot slot = this.getHoveredSlot(d, d2);
        long l = Util.getMillis();
        this.doubleclick = this.lastClickSlot == slot && l - this.lastClickTime < 250L && this.lastClickButton == n;
        this.skipNextRelease = false;
        if (n == 0 || n == 1 || bl) {
            int n2 = this.leftPos;
            int n3 = this.topPos;
            boolean bl2 = this.hasClickedOutside(d, d2, n2, n3, n);
            int n4 = -1;
            if (slot != null) {
                n4 = slot.index;
            }
            if (bl2) {
                n4 = -999;
            }
            if (this.minecraft.options.touchscreen().get().booleanValue() && bl2 && ((AbstractContainerMenu)this.menu).getCarried().isEmpty()) {
                this.onClose();
                return true;
            }
            if (n4 != -1) {
                if (this.minecraft.options.touchscreen().get().booleanValue()) {
                    if (slot != null && slot.hasItem()) {
                        this.clickedSlot = slot;
                        this.draggingItem = ItemStack.EMPTY;
                        this.isSplittingStack = n == 1;
                    } else {
                        this.clickedSlot = null;
                    }
                } else if (!this.isQuickCrafting) {
                    if (((AbstractContainerMenu)this.menu).getCarried().isEmpty()) {
                        if (bl) {
                            this.slotClicked(slot, n4, n, ClickType.CLONE);
                        } else {
                            boolean bl3 = n4 != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                            ClickType clickType = ClickType.PICKUP;
                            if (bl3) {
                                this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                                clickType = ClickType.QUICK_MOVE;
                            } else if (n4 == -999) {
                                clickType = ClickType.THROW;
                            }
                            this.slotClicked(slot, n4, n, clickType);
                        }
                        this.skipNextRelease = true;
                    } else {
                        this.isQuickCrafting = true;
                        this.quickCraftingButton = n;
                        this.quickCraftSlots.clear();
                        if (n == 0) {
                            this.quickCraftingType = 0;
                        } else if (n == 1) {
                            this.quickCraftingType = 1;
                        } else if (bl) {
                            this.quickCraftingType = 2;
                        }
                    }
                }
            }
        } else {
            this.checkHotbarMouseClicked(n);
        }
        this.lastClickSlot = slot;
        this.lastClickTime = l;
        this.lastClickButton = n;
        return true;
    }

    private void checkHotbarMouseClicked(int n) {
        if (this.hoveredSlot != null && ((AbstractContainerMenu)this.menu).getCarried().isEmpty()) {
            if (this.minecraft.options.keySwapOffhand.matchesMouse(n)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return;
            }
            for (int i = 0; i < 9; ++i) {
                if (!this.minecraft.options.keyHotbarSlots[i].matchesMouse(n)) continue;
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
            }
        }
    }

    protected boolean hasClickedOutside(double d, double d2, int n, int n2, int n3) {
        return d < (double)n || d2 < (double)n2 || d >= (double)(n + this.imageWidth) || d2 >= (double)(n2 + this.imageHeight);
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        Slot slot = this.getHoveredSlot(d, d2);
        ItemStack itemStack = ((AbstractContainerMenu)this.menu).getCarried();
        if (this.clickedSlot != null && this.minecraft.options.touchscreen().get().booleanValue()) {
            if (n == 0 || n == 1) {
                if (this.draggingItem.isEmpty()) {
                    if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                } else if (this.draggingItem.getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false)) {
                    long l = Util.getMillis();
                    if (this.quickdropSlot == slot) {
                        if (l - this.quickdropTime > 500L) {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.quickdropTime = l + 750L;
                            this.draggingItem.shrink(1);
                        }
                    } else {
                        this.quickdropSlot = slot;
                        this.quickdropTime = l;
                    }
                }
            }
        } else if (this.isQuickCrafting && slot != null && !itemStack.isEmpty() && (itemStack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2) && AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true) && slot.mayPlace(itemStack) && ((AbstractContainerMenu)this.menu).canDragTo(slot)) {
            this.quickCraftSlots.add(slot);
            this.recalculateQuickCraftRemaining();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        Slot slot = this.getHoveredSlot(d, d2);
        int n2 = this.leftPos;
        int n3 = this.topPos;
        boolean bl = this.hasClickedOutside(d, d2, n2, n3, n);
        int n4 = -1;
        if (slot != null) {
            n4 = slot.index;
        }
        if (bl) {
            n4 = -999;
        }
        if (this.doubleclick && slot != null && n == 0 && ((AbstractContainerMenu)this.menu).canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (AbstractContainerScreen.hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for (Slot slot2 : ((AbstractContainerMenu)this.menu).slots) {
                        if (slot2 == null || !slot2.mayPickup(this.minecraft.player) || !slot2.hasItem() || slot2.container != slot.container || !AbstractContainerMenu.canItemQuickReplace(slot2, this.lastQuickMoved, true)) continue;
                        this.slotClicked(slot2, slot2.index, n, ClickType.QUICK_MOVE);
                    }
                }
            } else {
                this.slotClicked(slot, n4, n, ClickType.PICKUP_ALL);
            }
            this.doubleclick = false;
            this.lastClickTime = 0L;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != n) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }
            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }
            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get().booleanValue()) {
                if (n == 0 || n == 1) {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }
                    boolean bl2 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
                    if (n4 != -1 && !this.draggingItem.isEmpty() && bl2) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, n, ClickType.PICKUP);
                        this.slotClicked(slot, n4, 0, ClickType.PICKUP);
                        if (((AbstractContainerMenu)this.menu).getCarried().isEmpty()) {
                            this.snapbackData = null;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, n, ClickType.PICKUP);
                            this.snapbackData = new SnapbackData(this.draggingItem, new Vector2i((int)d, (int)d2), new Vector2i(this.clickedSlot.x + n2, this.clickedSlot.y + n3), Util.getMillis());
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackData = new SnapbackData(this.draggingItem, new Vector2i((int)d, (int)d2), new Vector2i(this.clickedSlot.x + n2, this.clickedSlot.y + n3), Util.getMillis());
                    }
                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);
                for (Slot slot3 : this.quickCraftSlots) {
                    this.slotClicked(slot3, slot3.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }
                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!((AbstractContainerMenu)this.menu).getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.matchesMouse(n)) {
                    this.slotClicked(slot, n4, n, ClickType.CLONE);
                } else {
                    boolean bl3;
                    boolean bl4 = bl3 = n4 != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                    if (bl3) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }
                    this.slotClicked(slot, n4, n, bl3 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }
        if (((AbstractContainerMenu)this.menu).getCarried().isEmpty()) {
            this.lastClickTime = 0L;
        }
        this.isQuickCrafting = false;
        return true;
    }

    public void clearDraggingState() {
        this.draggingItem = ItemStack.EMPTY;
        this.clickedSlot = null;
    }

    private boolean isHovering(Slot slot, double d, double d2) {
        return this.isHovering(slot.x, slot.y, 16, 16, d, d2);
    }

    protected boolean isHovering(int n, int n2, int n3, int n4, double d, double d2) {
        int n5 = this.leftPos;
        int n6 = this.topPos;
        return (d -= (double)n5) >= (double)(n - 1) && d < (double)(n + n3 + 1) && (d2 -= (double)n6) >= (double)(n2 - 1) && d2 < (double)(n2 + n4 + 1);
    }

    private void onStopHovering(Slot slot) {
        if (slot.hasItem()) {
            for (ItemSlotMouseAction itemSlotMouseAction : this.itemSlotMouseActions) {
                if (!itemSlotMouseAction.matches(slot)) continue;
                itemSlotMouseAction.onStopHovering(slot);
            }
        }
    }

    protected void slotClicked(Slot slot, int n, int n2, ClickType clickType) {
        if (slot != null) {
            n = slot.index;
        }
        this.onMouseClickAction(slot, clickType);
        this.minecraft.gameMode.handleInventoryMouseClick(((AbstractContainerMenu)this.menu).containerId, n, n2, clickType, this.minecraft.player);
    }

    void onMouseClickAction(@Nullable Slot slot, ClickType clickType) {
        if (slot != null && slot.hasItem()) {
            for (ItemSlotMouseAction itemSlotMouseAction : this.itemSlotMouseActions) {
                if (!itemSlotMouseAction.matches(slot)) continue;
                itemSlotMouseAction.onSlotClicked(slot, clickType);
            }
        }
    }

    protected void handleSlotStateChanged(int n, int n2, boolean bl) {
        this.minecraft.gameMode.handleSlotStateChanged(n, n2, bl);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        if (this.minecraft.options.keyInventory.matches(n, n2)) {
            this.onClose();
            return true;
        }
        this.checkHotbarKeyPressed(n, n2);
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.minecraft.options.keyPickItem.matches(n, n2)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
            } else if (this.minecraft.options.keyDrop.matches(n, n2)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, AbstractContainerScreen.hasControlDown() ? 1 : 0, ClickType.THROW);
            }
        }
        return true;
    }

    protected boolean checkHotbarKeyPressed(int n, int n2) {
        if (((AbstractContainerMenu)this.menu).getCarried().isEmpty() && this.hoveredSlot != null) {
            if (this.minecraft.options.keySwapOffhand.matches(n, n2)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return true;
            }
            for (int i = 0; i < 9; ++i) {
                if (!this.minecraft.options.keyHotbarSlots[i].matches(n, n2)) continue;
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed() {
        if (this.minecraft.player == null) {
            return;
        }
        ((AbstractContainerMenu)this.menu).removed(this.minecraft.player);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public final void tick() {
        super.tick();
        if (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved()) {
            this.minecraft.player.closeContainer();
        } else {
            this.containerTick();
        }
    }

    protected void containerTick() {
    }

    @Override
    public T getMenu() {
        return this.menu;
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        if (this.hoveredSlot != null) {
            this.onStopHovering(this.hoveredSlot);
        }
        super.onClose();
    }

    static final class SnapbackData
    extends Record {
        final ItemStack item;
        final Vector2i start;
        final Vector2i end;
        final long time;

        SnapbackData(ItemStack itemStack, Vector2i vector2i, Vector2i vector2i2, long l) {
            this.item = itemStack;
            this.start = vector2i;
            this.end = vector2i2;
            this.time = l;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SnapbackData.class, "item;start;end;time", "item", "start", "end", "time"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SnapbackData.class, "item;start;end;time", "item", "start", "end", "time"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SnapbackData.class, "item;start;end;time", "item", "start", "end", "time"}, this, object);
        }

        public ItemStack item() {
            return this.item;
        }

        public Vector2i start() {
            return this.start;
        }

        public Vector2i end() {
            return this.end;
        }

        public long time() {
            return this.time;
        }
    }
}

