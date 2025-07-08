/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu
extends AbstractContainerMenu {
    private static final int INVENTORY_SLOTS_PER_ROW = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final int INPUT_SLOT_START = 0;
    protected final ContainerLevelAccess access;
    protected final Player player;
    protected final Container inputSlots;
    protected final ResultContainer resultSlots = new ResultContainer(){

        @Override
        public void setChanged() {
            ItemCombinerMenu.this.slotsChanged(this);
        }
    };
    private final int resultSlotIndex;

    protected boolean mayPickup(Player player, boolean bl) {
        return true;
    }

    protected abstract void onTake(Player var1, ItemStack var2);

    protected abstract boolean isValidBlock(BlockState var1);

    public ItemCombinerMenu(@Nullable MenuType<?> menuType, int n, Inventory inventory, ContainerLevelAccess containerLevelAccess, ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        super(menuType, n);
        this.access = containerLevelAccess;
        this.player = inventory.player;
        this.inputSlots = this.createContainer(itemCombinerMenuSlotDefinition.getNumOfInputSlots());
        this.resultSlotIndex = itemCombinerMenuSlotDefinition.getResultSlotIndex();
        this.createInputSlots(itemCombinerMenuSlotDefinition);
        this.createResultSlot(itemCombinerMenuSlotDefinition);
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    private void createInputSlots(ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        for (final ItemCombinerMenuSlotDefinition.SlotDefinition slotDefinition : itemCombinerMenuSlotDefinition.getSlots()) {
            this.addSlot(new Slot(this, this.inputSlots, slotDefinition.slotIndex(), slotDefinition.x(), slotDefinition.y()){

                @Override
                public boolean mayPlace(ItemStack itemStack) {
                    return slotDefinition.mayPlace().test(itemStack);
                }
            });
        }
    }

    private void createResultSlot(ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        this.addSlot(new Slot(this.resultSlots, itemCombinerMenuSlotDefinition.getResultSlot().slotIndex(), itemCombinerMenuSlotDefinition.getResultSlot().x(), itemCombinerMenuSlotDefinition.getResultSlot().y()){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return ItemCombinerMenu.this.mayPickup(player, this.hasItem());
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                ItemCombinerMenu.this.onTake(player, itemStack);
            }
        });
    }

    public abstract void createResult();

    private SimpleContainer createContainer(int n) {
        return new SimpleContainer(n){

            @Override
            public void setChanged() {
                super.setChanged();
                ItemCombinerMenu.this.slotsChanged(this);
            }
        };
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.inputSlots) {
            this.createResult();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.inputSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) -> {
            if (!this.isValidBlock(level.getBlockState((BlockPos)blockPos))) {
                return false;
            }
            return player.canInteractWithBlock((BlockPos)blockPos, 4.0);
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int n2 = this.getInventorySlotStart();
            int n3 = this.getUseRowEnd();
            if (n == this.getResultSlot()) {
                if (!this.moveItemStackTo(itemStack2, n2, n3, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (n >= 0 && n < this.getResultSlot() ? !this.moveItemStackTo(itemStack2, n2, n3, false) : (this.canMoveIntoInputSlots(itemStack2) && n >= this.getInventorySlotStart() && n < this.getUseRowEnd() ? !this.moveItemStackTo(itemStack2, 0, this.getResultSlot(), false) : (n >= this.getInventorySlotStart() && n < this.getInventorySlotEnd() ? !this.moveItemStackTo(itemStack2, this.getUseRowStart(), this.getUseRowEnd(), false) : n >= this.getUseRowStart() && n < this.getUseRowEnd() && !this.moveItemStackTo(itemStack2, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    protected boolean canMoveIntoInputSlots(ItemStack itemStack) {
        return true;
    }

    public int getResultSlot() {
        return this.resultSlotIndex;
    }

    private int getInventorySlotStart() {
        return this.getResultSlot() + 1;
    }

    private int getInventorySlotEnd() {
        return this.getInventorySlotStart() + 27;
    }

    private int getUseRowStart() {
        return this.getInventorySlotEnd();
    }

    private int getUseRowEnd() {
        return this.getUseRowStart() + 9;
    }
}

