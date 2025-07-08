/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecartContainer
extends AbstractMinecart
implements ContainerEntity {
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    protected AbstractMinecartContainer(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void destroy(ServerLevel serverLevel, DamageSource damageSource) {
        super.destroy(serverLevel, damageSource);
        this.chestVehicleDestroyed(damageSource, serverLevel, this);
    }

    @Override
    public ItemStack getItem(int n) {
        return this.getChestVehicleItem(n);
    }

    @Override
    public ItemStack removeItem(int n, int n2) {
        return this.removeChestVehicleItem(n, n2);
    }

    @Override
    public ItemStack removeItemNoUpdate(int n) {
        return this.removeChestVehicleItemNoUpdate(n);
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        this.setChestVehicleItem(n, itemStack);
    }

    @Override
    public SlotAccess getSlot(int n) {
        return this.getChestVehicleSlot(n);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isChestVehicleStillValid(player);
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide && removalReason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, (Container)this);
        }
        super.remove(removalReason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.addChestVehicleSaveData(valueOutput);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readChestVehicleSaveData(valueInput);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        return this.interactWithContainerVehicle(player);
    }

    @Override
    protected Vec3 applyNaturalSlowdown(Vec3 vec3) {
        float f = 0.98f;
        if (this.lootTable == null) {
            int n = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
            f += (float)n * 0.001f;
        }
        if (this.isInWater()) {
            f *= 0.95f;
        }
        return vec3.multiply(f, 0.0, f);
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    public void setLootTable(ResourceKey<LootTable> resourceKey, long l) {
        this.lootTable = resourceKey;
        this.lootTableSeed = l;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int n, Inventory inventory, Player player) {
        if (this.lootTable == null || !player.isSpectator()) {
            this.unpackChestVehicleLootTable(inventory.player);
            return this.createMenu(n, inventory);
        }
        return null;
    }

    protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);

    @Override
    @Nullable
    public ResourceKey<LootTable> getContainerLootTable() {
        return this.lootTable;
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
        this.lootTable = resourceKey;
    }

    @Override
    public long getContainerLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setContainerLootTableSeed(long l) {
        this.lootTableSeed = l;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }
}

