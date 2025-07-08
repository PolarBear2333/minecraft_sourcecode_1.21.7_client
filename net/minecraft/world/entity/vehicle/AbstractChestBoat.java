/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
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
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class AbstractChestBoat
extends AbstractBoat
implements HasCustomInventoryScreen,
ContainerEntity {
    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    public AbstractChestBoat(EntityType<? extends AbstractChestBoat> entityType, Level level, Supplier<Item> supplier) {
        super(entityType, level, supplier);
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15f;
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
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
    public void destroy(ServerLevel serverLevel, DamageSource damageSource) {
        this.destroy(serverLevel, this.getDropItem());
        this.chestVehicleDestroyed(damageSource, serverLevel, this);
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide && removalReason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, (Container)this);
        }
        super.remove(removalReason);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult = super.interact(player, interactionHand);
        if (interactionResult != InteractionResult.PASS) {
            return interactionResult;
        }
        if (!this.canAddPassenger(player) || player.isSecondaryUseActive()) {
            Level level;
            InteractionResult interactionResult2 = this.interactWithContainerVehicle(player);
            if (interactionResult2.consumesAction() && (level = player.level()) instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            }
            return interactionResult2;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        }
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    @Override
    public int getContainerSize() {
        return 27;
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
    @Nullable
    public AbstractContainerMenu createMenu(int n, Inventory inventory, Player player) {
        if (this.lootTable == null || !player.isSpectator()) {
            this.unpackLootTable(inventory.player);
            return ChestMenu.threeRows(n, inventory, this);
        }
        return null;
    }

    public void unpackLootTable(@Nullable Player player) {
        this.unpackChestVehicleLootTable(player);
    }

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

    @Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
    }
}

