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
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxMenu
extends AbstractContainerMenu {
    private static final int CONTAINER_SIZE = 27;
    private final Container container;

    public ShulkerBoxMenu(int n, Inventory inventory) {
        this(n, inventory, new SimpleContainer(27));
    }

    public ShulkerBoxMenu(int n, Inventory inventory, Container container) {
        super(MenuType.SHULKER_BOX, n);
        ShulkerBoxMenu.checkContainerSize(container, 27);
        this.container = container;
        container.startOpen(inventory.player);
        int n2 = 3;
        int n3 = 9;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new ShulkerBoxSlot(container, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
        this.addStandardInventorySlots(inventory, 8, 84);
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
            if (n < this.container.getContainerSize() ? !this.moveItemStackTo(itemStack2, this.container.getContainerSize(), this.slots.size(), true) : !this.moveItemStackTo(itemStack2, 0, this.container.getContainerSize(), false)) {
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
}

