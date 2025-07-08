/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DispenserMenu
extends AbstractContainerMenu {
    private static final int SLOT_COUNT = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;
    private final Container dispenser;

    public DispenserMenu(int n, Inventory inventory) {
        this(n, inventory, new SimpleContainer(9));
    }

    public DispenserMenu(int n, Inventory inventory, Container container) {
        super(MenuType.GENERIC_3x3, n);
        DispenserMenu.checkContainerSize(container, 9);
        this.dispenser = container;
        container.startOpen(inventory.player);
        this.add3x3GridSlots(container, 62, 17);
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    protected void add3x3GridSlots(Container container, int n, int n2) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int n3 = j + i * 3;
                this.addSlot(new Slot(container, n3, n + j * 18, n2 + i * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.dispenser.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n < 9 ? !this.moveItemStackTo(itemStack2, 9, 45, true) : !this.moveItemStackTo(itemStack2, 0, 9, false)) {
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.dispenser.stopOpen(player);
    }
}

