/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;

public class EnchantmentMenu
extends AbstractContainerMenu {
    static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("container/slot/lapis_lazuli");
    private final Container enchantSlots = new SimpleContainer(2){

        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;
    private final RandomSource random = RandomSource.create();
    private final DataSlot enchantmentSeed = DataSlot.standalone();
    public final int[] costs = new int[3];
    public final int[] enchantClue = new int[]{-1, -1, -1};
    public final int[] levelClue = new int[]{-1, -1, -1};

    public EnchantmentMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public EnchantmentMenu(int n, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(MenuType.ENCHANTMENT, n);
        this.access = containerLevelAccess;
        this.addSlot(new Slot(this, this.enchantSlots, 0, 15, 47){

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this, this.enchantSlots, 1, 35, 47){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public ResourceLocation getNoItemIcon() {
                return EMPTY_SLOT_LAPIS_LAZULI;
            }
        });
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
        this.addDataSlot(this.enchantmentSeed).set(inventory.player.getEnchantmentSeed());
        this.addDataSlot(DataSlot.shared(this.enchantClue, 0));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 1));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 2));
        this.addDataSlot(DataSlot.shared(this.levelClue, 0));
        this.addDataSlot(DataSlot.shared(this.levelClue, 1));
        this.addDataSlot(DataSlot.shared(this.levelClue, 2));
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack itemStack = container.getItem(0);
            if (itemStack.isEmpty() || !itemStack.isEnchantable()) {
                for (int i = 0; i < 3; ++i) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
            } else {
                this.access.execute((level, blockPos) -> {
                    int n;
                    IdMap<Holder<Holder<Enchantment>>> idMap = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                    int n2 = 0;
                    for (BlockPos object : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                        if (!EnchantingTableBlock.isValidBookShelf(level, blockPos, object)) continue;
                        ++n2;
                    }
                    this.random.setSeed(this.enchantmentSeed.get());
                    for (n = 0; n < 3; ++n) {
                        this.costs[n] = EnchantmentHelper.getEnchantmentCost(this.random, n, n2, itemStack);
                        this.enchantClue[n] = -1;
                        this.levelClue[n] = -1;
                        if (this.costs[n] >= n + 1) continue;
                        this.costs[n] = 0;
                    }
                    for (n = 0; n < 3; ++n) {
                        List<EnchantmentInstance> list;
                        if (this.costs[n] <= 0 || (list = this.getEnchantmentList(level.registryAccess(), itemStack, n, this.costs[n])) == null || list.isEmpty()) continue;
                        EnchantmentInstance enchantmentInstance = list.get(this.random.nextInt(list.size()));
                        this.enchantClue[n] = idMap.getId(enchantmentInstance.enchantment());
                        this.levelClue[n] = enchantmentInstance.level();
                    }
                    this.broadcastChanges();
                });
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int n) {
        if (n < 0 || n >= this.costs.length) {
            Util.logAndPauseIfInIde(String.valueOf(player.getName()) + " pressed invalid button id: " + n);
            return false;
        }
        ItemStack itemStack = this.enchantSlots.getItem(0);
        ItemStack itemStack2 = this.enchantSlots.getItem(1);
        int n2 = n + 1;
        if ((itemStack2.isEmpty() || itemStack2.getCount() < n2) && !player.hasInfiniteMaterials()) {
            return false;
        }
        if (this.costs[n] > 0 && !itemStack.isEmpty() && (player.experienceLevel >= n2 && player.experienceLevel >= this.costs[n] || player.hasInfiniteMaterials())) {
            this.access.execute((level, blockPos) -> {
                ItemStack itemStack3 = itemStack;
                List<EnchantmentInstance> list = this.getEnchantmentList(level.registryAccess(), itemStack3, n, this.costs[n]);
                if (!list.isEmpty()) {
                    player.onEnchantmentPerformed(itemStack3, n2);
                    if (itemStack3.is(Items.BOOK)) {
                        itemStack3 = itemStack.transmuteCopy(Items.ENCHANTED_BOOK);
                        this.enchantSlots.setItem(0, itemStack3);
                    }
                    for (EnchantmentInstance enchantmentInstance : list) {
                        itemStack3.enchant(enchantmentInstance.enchantment(), enchantmentInstance.level());
                    }
                    itemStack2.consume(n2, player);
                    if (itemStack2.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                    player.awardStat(Stats.ENCHANT_ITEM);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, itemStack3, n2);
                    }
                    this.enchantSlots.setChanged();
                    this.enchantmentSeed.set(player.getEnchantmentSeed());
                    this.slotsChanged(this.enchantSlots);
                    level.playSound(null, (BlockPos)blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.1f + 0.9f);
                }
            });
            return true;
        }
        return false;
    }

    private List<EnchantmentInstance> getEnchantmentList(RegistryAccess registryAccess, ItemStack itemStack, int n, int n2) {
        this.random.setSeed(this.enchantmentSeed.get() + n);
        Optional optional = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (optional.isEmpty()) {
            return List.of();
        }
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(this.random, itemStack, n2, ((HolderSet.Named)optional.get()).stream());
        if (itemStack.is(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }
        return list;
    }

    public int getGoldCount() {
        ItemStack itemStack = this.enchantSlots.getItem(1);
        if (itemStack.isEmpty()) {
            return 0;
        }
        return itemStack.getCount();
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.enchantSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return EnchantmentMenu.stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int n) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(n);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (n == 0) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (n == 1) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!((Slot)this.slots.get(0)).hasItem() && ((Slot)this.slots.get(0)).mayPlace(itemStack2)) {
                ItemStack itemStack3 = itemStack2.copyWithCount(1);
                itemStack2.shrink(1);
                ((Slot)this.slots.get(0)).setByPlayer(itemStack3);
            } else {
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
}

