/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 */
package net.minecraft.world.entity.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Inventory
implements Container,
Nameable {
    public static final int POP_TIME_DURATION = 5;
    public static final int INVENTORY_SIZE = 36;
    public static final int SELECTION_SIZE = 9;
    public static final int SLOT_OFFHAND = 40;
    public static final int SLOT_BODY_ARMOR = 41;
    public static final int SLOT_SADDLE = 42;
    public static final int NOT_FOUND_INDEX = -1;
    public static final Int2ObjectMap<EquipmentSlot> EQUIPMENT_SLOT_MAPPING = new Int2ObjectArrayMap(Map.of(EquipmentSlot.FEET.getIndex(36), EquipmentSlot.FEET, EquipmentSlot.LEGS.getIndex(36), EquipmentSlot.LEGS, EquipmentSlot.CHEST.getIndex(36), EquipmentSlot.CHEST, EquipmentSlot.HEAD.getIndex(36), EquipmentSlot.HEAD, 40, EquipmentSlot.OFFHAND, 41, EquipmentSlot.BODY, 42, EquipmentSlot.SADDLE));
    private final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
    private int selected;
    public final Player player;
    private final EntityEquipment equipment;
    private int timesChanged;

    public Inventory(Player player, EntityEquipment entityEquipment) {
        this.player = player;
        this.equipment = entityEquipment;
    }

    public int getSelectedSlot() {
        return this.selected;
    }

    public void setSelectedSlot(int n) {
        if (!Inventory.isHotbarSlot(n)) {
            throw new IllegalArgumentException("Invalid selected slot");
        }
        this.selected = n;
    }

    public ItemStack getSelectedItem() {
        return this.items.get(this.selected);
    }

    public ItemStack setSelectedItem(ItemStack itemStack) {
        return this.items.set(this.selected, itemStack);
    }

    public static int getSelectionSize() {
        return 9;
    }

    public NonNullList<ItemStack> getNonEquipmentItems() {
        return this.items;
    }

    private boolean hasRemainingSpaceForItem(ItemStack itemStack, ItemStack itemStack2) {
        return !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2) && itemStack.isStackable() && itemStack.getCount() < this.getMaxStackSize(itemStack);
    }

    public int getFreeSlot() {
        for (int i = 0; i < this.items.size(); ++i) {
            if (!this.items.get(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public void addAndPickItem(ItemStack itemStack) {
        int n;
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        if (!this.items.get(this.selected).isEmpty() && (n = this.getFreeSlot()) != -1) {
            this.items.set(n, this.items.get(this.selected));
        }
        this.items.set(this.selected, itemStack);
    }

    public void pickSlot(int n) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        ItemStack itemStack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(n));
        this.items.set(n, itemStack);
    }

    public static boolean isHotbarSlot(int n) {
        return n >= 0 && n < 9;
    }

    public int findSlotMatchingItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty() || !ItemStack.isSameItemSameComponents(itemStack, this.items.get(i))) continue;
            return i;
        }
        return -1;
    }

    public static boolean isUsableForCrafting(ItemStack itemStack) {
        return !itemStack.isDamaged() && !itemStack.isEnchanted() && !itemStack.has(DataComponents.CUSTOM_NAME);
    }

    public int findSlotMatchingCraftingIngredient(Holder<Item> holder, ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack2 = this.items.get(i);
            if (itemStack2.isEmpty() || !itemStack2.is(holder) || !Inventory.isUsableForCrafting(itemStack2) || !itemStack.isEmpty() && !ItemStack.isSameItemSameComponents(itemStack, itemStack2)) continue;
            return i;
        }
        return -1;
    }

    public int getSuitableHotbarSlot() {
        int n;
        int n2;
        for (n2 = 0; n2 < 9; ++n2) {
            n = (this.selected + n2) % 9;
            if (!this.items.get(n).isEmpty()) continue;
            return n;
        }
        for (n2 = 0; n2 < 9; ++n2) {
            n = (this.selected + n2) % 9;
            if (this.items.get(n).isEnchanted()) continue;
            return n;
        }
        return this.selected;
    }

    public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int n, Container container) {
        int n2 = 0;
        boolean bl = n == 0;
        n2 += ContainerHelper.clearOrCountMatchingItems(this, predicate, n - n2, bl);
        n2 += ContainerHelper.clearOrCountMatchingItems(container, predicate, n - n2, bl);
        ItemStack itemStack = this.player.containerMenu.getCarried();
        n2 += ContainerHelper.clearOrCountMatchingItems(itemStack, predicate, n - n2, bl);
        if (itemStack.isEmpty()) {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        return n2;
    }

    private int addResource(ItemStack itemStack) {
        int n = this.getSlotWithRemainingSpace(itemStack);
        if (n == -1) {
            n = this.getFreeSlot();
        }
        if (n == -1) {
            return itemStack.getCount();
        }
        return this.addResource(n, itemStack);
    }

    private int addResource(int n, ItemStack itemStack) {
        int n2;
        int n3;
        int n4 = itemStack.getCount();
        ItemStack itemStack2 = this.getItem(n);
        if (itemStack2.isEmpty()) {
            itemStack2 = itemStack.copyWithCount(0);
            this.setItem(n, itemStack2);
        }
        if ((n3 = Math.min(n4, n2 = this.getMaxStackSize(itemStack2) - itemStack2.getCount())) == 0) {
            return n4;
        }
        itemStack2.grow(n3);
        itemStack2.setPopTime(5);
        return n4 -= n3;
    }

    public int getSlotWithRemainingSpace(ItemStack itemStack) {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), itemStack)) {
            return this.selected;
        }
        if (this.hasRemainingSpaceForItem(this.getItem(40), itemStack)) {
            return 40;
        }
        for (int i = 0; i < this.items.size(); ++i) {
            if (!this.hasRemainingSpaceForItem(this.items.get(i), itemStack)) continue;
            return i;
        }
        return -1;
    }

    public void tick() {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            itemStack.inventoryTick(this.player.level(), this.player, i == this.selected ? EquipmentSlot.MAINHAND : null);
        }
    }

    public boolean add(ItemStack itemStack) {
        return this.add(-1, itemStack);
    }

    public boolean add(int n, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        try {
            if (!itemStack.isDamaged()) {
                int n2;
                do {
                    n2 = itemStack.getCount();
                    if (n == -1) {
                        itemStack.setCount(this.addResource(itemStack));
                        continue;
                    }
                    itemStack.setCount(this.addResource(n, itemStack));
                } while (!itemStack.isEmpty() && itemStack.getCount() < n2);
                if (itemStack.getCount() == n2 && this.player.hasInfiniteMaterials()) {
                    itemStack.setCount(0);
                    return true;
                }
                return itemStack.getCount() < n2;
            }
            if (n == -1) {
                n = this.getFreeSlot();
            }
            if (n >= 0) {
                this.items.set(n, itemStack.copyAndClear());
                this.items.get(n).setPopTime(5);
                return true;
            }
            if (this.player.hasInfiniteMaterials()) {
                itemStack.setCount(0);
                return true;
            }
            return false;
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Adding item to inventory");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
            crashReportCategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
            crashReportCategory.setDetail("Item data", itemStack.getDamageValue());
            crashReportCategory.setDetail("Item name", () -> itemStack.getHoverName().getString());
            throw new ReportedException(crashReport);
        }
    }

    public void placeItemBackInInventory(ItemStack itemStack) {
        this.placeItemBackInInventory(itemStack, true);
    }

    public void placeItemBackInInventory(ItemStack itemStack, boolean bl) {
        while (!itemStack.isEmpty()) {
            Player player;
            int n = this.getSlotWithRemainingSpace(itemStack);
            if (n == -1) {
                n = this.getFreeSlot();
            }
            if (n == -1) {
                this.player.drop(itemStack, false);
                break;
            }
            int n2 = itemStack.getMaxStackSize() - this.getItem(n).getCount();
            if (!this.add(n, itemStack.split(n2)) || !bl || !((player = this.player) instanceof ServerPlayer)) continue;
            ServerPlayer serverPlayer = (ServerPlayer)player;
            serverPlayer.connection.send(this.createInventoryUpdatePacket(n));
        }
    }

    public ClientboundSetPlayerInventoryPacket createInventoryUpdatePacket(int n) {
        return new ClientboundSetPlayerInventoryPacket(n, this.getItem(n).copy());
    }

    @Override
    public ItemStack removeItem(int n, int n2) {
        ItemStack itemStack;
        if (n < this.items.size()) {
            return ContainerHelper.removeItem(this.items, n, n2);
        }
        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(n);
        if (equipmentSlot != null && !(itemStack = this.equipment.get(equipmentSlot)).isEmpty()) {
            return itemStack.split(n2);
        }
        return ItemStack.EMPTY;
    }

    public void removeItem(ItemStack itemStack) {
        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i) != itemStack) continue;
            this.items.set(i, ItemStack.EMPTY);
            return;
        }
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOT_MAPPING.values()) {
            ItemStack itemStack2 = this.equipment.get(equipmentSlot);
            if (itemStack2 != itemStack) continue;
            this.equipment.set(equipmentSlot, ItemStack.EMPTY);
            return;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int n) {
        if (n < this.items.size()) {
            ItemStack itemStack = this.items.get(n);
            this.items.set(n, ItemStack.EMPTY);
            return itemStack;
        }
        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(n);
        if (equipmentSlot != null) {
            return this.equipment.set(equipmentSlot, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        EquipmentSlot equipmentSlot;
        if (n < this.items.size()) {
            this.items.set(n, itemStack);
        }
        if ((equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(n)) != null) {
            this.equipment.set(equipmentSlot, itemStack);
        }
    }

    public void save(ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.isEmpty()) continue;
            typedOutputList.add(new ItemStackWithSlot(i, itemStack));
        }
    }

    public void load(ValueInput.TypedInputList<ItemStackWithSlot> typedInputList) {
        this.items.clear();
        for (ItemStackWithSlot itemStackWithSlot : typedInputList) {
            if (!itemStackWithSlot.isValidInContainer(this.items.size())) continue;
            this.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + EQUIPMENT_SLOT_MAPPING.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack object : this.items) {
            if (object.isEmpty()) continue;
            return false;
        }
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOT_MAPPING.values()) {
            if (this.equipment.get(equipmentSlot).isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int n) {
        if (n < this.items.size()) {
            return this.items.get(n);
        }
        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(n);
        if (equipmentSlot != null) {
            return this.equipment.get(equipmentSlot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName() {
        return Component.translatable("container.inventory");
    }

    public void dropAll() {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.isEmpty()) continue;
            this.player.drop(itemStack, true, false);
            this.items.set(i, ItemStack.EMPTY);
        }
        this.equipment.dropAll(this.player);
    }

    @Override
    public void setChanged() {
        ++this.timesChanged;
    }

    public int getTimesChanged() {
        return this.timesChanged;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public boolean contains(ItemStack itemStack) {
        for (ItemStack itemStack2 : this) {
            if (itemStack2.isEmpty() || !ItemStack.isSameItemSameComponents(itemStack2, itemStack)) continue;
            return true;
        }
        return false;
    }

    public boolean contains(TagKey<Item> tagKey) {
        for (ItemStack itemStack : this) {
            if (itemStack.isEmpty() || !itemStack.is(tagKey)) continue;
            return true;
        }
        return false;
    }

    public boolean contains(Predicate<ItemStack> predicate) {
        for (ItemStack itemStack : this) {
            if (!predicate.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public void replaceWith(Inventory inventory) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, inventory.getItem(i));
        }
        this.setSelectedSlot(inventory.getSelectedSlot());
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.equipment.clear();
    }

    public void fillStackedContents(StackedItemContents stackedItemContents) {
        for (ItemStack itemStack : this.items) {
            stackedItemContents.accountSimpleStack(itemStack);
        }
    }

    public ItemStack removeFromSelected(boolean bl) {
        ItemStack itemStack = this.getSelectedItem();
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.removeItem(this.selected, bl ? itemStack.getCount() : 1);
    }
}

