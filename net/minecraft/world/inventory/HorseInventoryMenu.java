/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class HorseInventoryMenu
extends AbstractContainerMenu {
    private static final ResourceLocation SADDLE_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/saddle");
    private static final ResourceLocation LLAMA_ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/llama_armor");
    private static final ResourceLocation ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/horse_armor");
    private final Container horseContainer;
    private final AbstractHorse horse;
    private static final int SLOT_SADDLE = 0;
    private static final int SLOT_BODY_ARMOR = 1;
    private static final int SLOT_HORSE_INVENTORY_START = 2;

    public HorseInventoryMenu(int n, Inventory inventory, Container container, final AbstractHorse abstractHorse, int n2) {
        super(null, n);
        this.horseContainer = container;
        this.horse = abstractHorse;
        container.startOpen(inventory.player);
        Container container2 = abstractHorse.createEquipmentSlotContainer(EquipmentSlot.SADDLE);
        this.addSlot(new ArmorSlot(this, container2, abstractHorse, EquipmentSlot.SADDLE, 0, 8, 18, SADDLE_SLOT_SPRITE){

            @Override
            public boolean isActive() {
                return abstractHorse.canUseSlot(EquipmentSlot.SADDLE) && abstractHorse.getType().is(EntityTypeTags.CAN_EQUIP_SADDLE);
            }
        });
        final boolean bl = abstractHorse instanceof Llama;
        ResourceLocation resourceLocation = bl ? LLAMA_ARMOR_SLOT_SPRITE : ARMOR_SLOT_SPRITE;
        Container container3 = abstractHorse.createEquipmentSlotContainer(EquipmentSlot.BODY);
        this.addSlot(new ArmorSlot(this, container3, abstractHorse, EquipmentSlot.BODY, 0, 8, 36, resourceLocation){

            @Override
            public boolean isActive() {
                return abstractHorse.canUseSlot(EquipmentSlot.BODY) && (abstractHorse.getType().is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || bl);
            }
        });
        if (n2 > 0) {
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < n2; ++j) {
                    this.addSlot(new Slot(container, j + i * n2, 80 + j * 18, 18 + i * 18));
                }
            }
        }
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return !this.horse.hasInventoryChanged(this.horseContainer) && this.horseContainer.stillValid(player) && this.horse.isAlive() && player.canInteractWithEntity(this.horse, 4.0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int n2 = 2 + this.horseContainer.getContainerSize();
            if (n < n2) {
                if (!this.moveItemStackTo(itemStack2, n2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemStack2) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemStack2) && !this.getSlot(0).hasItem()) {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.horseContainer.getContainerSize() == 0 || !this.moveItemStackTo(itemStack2, 2, n2, false)) {
                int n3;
                int n4 = n3 = n2 + 27;
                int n5 = n4 + 9;
                if (n >= n4 && n < n5 ? !this.moveItemStackTo(itemStack2, n2, n3, false) : (n >= n2 && n < n3 ? !this.moveItemStackTo(itemStack2, n4, n5, false) : !this.moveItemStackTo(itemStack2, n4, n3, false))) {
                    return ItemStack.EMPTY;
                }
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
        this.horseContainer.stopOpen(player);
    }
}

