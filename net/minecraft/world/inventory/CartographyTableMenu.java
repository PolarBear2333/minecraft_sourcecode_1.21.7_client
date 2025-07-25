/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu
extends AbstractContainerMenu {
    public static final int MAP_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final ContainerLevelAccess access;
    long lastSoundTime;
    public final Container container = new SimpleContainer(2){

        @Override
        public void setChanged() {
            CartographyTableMenu.this.slotsChanged(this);
            super.setChanged();
        }
    };
    private final ResultContainer resultContainer = new ResultContainer(){

        @Override
        public void setChanged() {
            CartographyTableMenu.this.slotsChanged(this);
            super.setChanged();
        }
    };

    public CartographyTableMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public CartographyTableMenu(int n, Inventory inventory, final ContainerLevelAccess containerLevelAccess) {
        super(MenuType.CARTOGRAPHY_TABLE, n);
        this.access = containerLevelAccess;
        this.addSlot(new Slot(this, this.container, 0, 15, 15){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.has(DataComponents.MAP_ID);
            }
        });
        this.addSlot(new Slot(this, this.container, 1, 15, 52){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(Items.PAPER) || itemStack.is(Items.MAP) || itemStack.is(Items.GLASS_PANE);
            }
        });
        this.addSlot(new Slot(this.resultContainer, 2, 145, 39){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                ((Slot)CartographyTableMenu.this.slots.get(0)).remove(1);
                ((Slot)CartographyTableMenu.this.slots.get(1)).remove(1);
                itemStack.getItem().onCraftedBy(itemStack, player);
                containerLevelAccess.execute((level, blockPos) -> {
                    long l = level.getGameTime();
                    if (CartographyTableMenu.this.lastSoundTime != l) {
                        level.playSound(null, (BlockPos)blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0f, 1.0f);
                        CartographyTableMenu.this.lastSoundTime = l;
                    }
                });
                super.onTake(player, itemStack);
            }
        });
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return CartographyTableMenu.stillValid(this.access, player, Blocks.CARTOGRAPHY_TABLE);
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack itemStack = this.container.getItem(0);
        ItemStack itemStack2 = this.container.getItem(1);
        ItemStack itemStack3 = this.resultContainer.getItem(2);
        if (!itemStack3.isEmpty() && (itemStack.isEmpty() || itemStack2.isEmpty())) {
            this.resultContainer.removeItemNoUpdate(2);
        } else if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            this.setupResultSlot(itemStack, itemStack2, itemStack3);
        }
    }

    private void setupResultSlot(ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3) {
        this.access.execute((level, blockPos) -> {
            ItemStack itemStack4;
            MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
            if (mapItemSavedData == null) {
                return;
            }
            if (itemStack2.is(Items.PAPER) && !mapItemSavedData.locked && mapItemSavedData.scale < 4) {
                itemStack4 = itemStack.copyWithCount(1);
                itemStack4.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
                this.broadcastChanges();
            } else if (itemStack2.is(Items.GLASS_PANE) && !mapItemSavedData.locked) {
                itemStack4 = itemStack.copyWithCount(1);
                itemStack4.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.LOCK);
                this.broadcastChanges();
            } else if (itemStack2.is(Items.MAP)) {
                itemStack4 = itemStack.copyWithCount(2);
                this.broadcastChanges();
            } else {
                this.resultContainer.removeItemNoUpdate(2);
                this.broadcastChanges();
                return;
            }
            if (!ItemStack.matches(itemStack4, itemStack3)) {
                this.resultContainer.setItem(2, itemStack4);
                this.broadcastChanges();
            }
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(itemStack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n == 2) {
                itemStack2.getItem().onCraftedBy(itemStack2, player);
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (n == 1 || n == 0 ? !this.moveItemStackTo(itemStack2, 3, 39, false) : (itemStack2.has(DataComponents.MAP_ID) ? !this.moveItemStackTo(itemStack2, 0, 1, false) : (itemStack2.is(Items.PAPER) || itemStack2.is(Items.MAP) || itemStack2.is(Items.GLASS_PANE) ? !this.moveItemStackTo(itemStack2, 1, 2, false) : (n >= 3 && n < 30 ? !this.moveItemStackTo(itemStack2, 30, 39, false) : n >= 30 && n < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false))))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            slot.setChanged();
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
            this.broadcastChanges();
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.removeItemNoUpdate(2);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.container));
    }
}

