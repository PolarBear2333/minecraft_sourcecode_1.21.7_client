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

public class ChestMenu
extends AbstractContainerMenu {
    private final Container container;
    private final int containerRows;

    private ChestMenu(MenuType<?> menuType, int n, Inventory inventory, int n2) {
        this(menuType, n, inventory, new SimpleContainer(9 * n2), n2);
    }

    public static ChestMenu oneRow(int n, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, n, inventory, 1);
    }

    public static ChestMenu twoRows(int n, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x2, n, inventory, 2);
    }

    public static ChestMenu threeRows(int n, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x3, n, inventory, 3);
    }

    public static ChestMenu fourRows(int n, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x4, n, inventory, 4);
    }

    public static ChestMenu fiveRows(int n, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x5, n, inventory, 5);
    }

    public static ChestMenu sixRows(int n, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x6, n, inventory, 6);
    }

    public static ChestMenu threeRows(int n, Inventory inventory, Container container) {
        return new ChestMenu(MenuType.GENERIC_9x3, n, inventory, container, 3);
    }

    public static ChestMenu sixRows(int n, Inventory inventory, Container container) {
        return new ChestMenu(MenuType.GENERIC_9x6, n, inventory, container, 6);
    }

    public ChestMenu(MenuType<?> menuType, int n, Inventory inventory, Container container, int n2) {
        super(menuType, n);
        ChestMenu.checkContainerSize(container, n2 * 9);
        this.container = container;
        this.containerRows = n2;
        container.startOpen(inventory.player);
        int n3 = 18;
        this.addChestGrid(container, 8, 18);
        int n4 = 18 + this.containerRows * 18 + 13;
        this.addStandardInventorySlots(inventory, 8, n4);
    }

    private void addChestGrid(Container container, int n, int n2) {
        for (int i = 0; i < this.containerRows; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(container, j + i * 9, n + j * 18, n2 + i * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n < this.containerRows * 9 ? !this.moveItemStackTo(itemStack2, this.containerRows * 9, this.slots.size(), true) : !this.moveItemStackTo(itemStack2, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return this.container;
    }

    public int getRowCount() {
        return this.containerRows;
    }
}

