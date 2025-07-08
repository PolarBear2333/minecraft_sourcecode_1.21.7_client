/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ContainerEntity
extends Container,
MenuProvider {
    public Vec3 position();

    public AABB getBoundingBox();

    @Nullable
    public ResourceKey<LootTable> getContainerLootTable();

    public void setContainerLootTable(@Nullable ResourceKey<LootTable> var1);

    public long getContainerLootTableSeed();

    public void setContainerLootTableSeed(long var1);

    public NonNullList<ItemStack> getItemStacks();

    public void clearItemStacks();

    public Level level();

    public boolean isRemoved();

    @Override
    default public boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    default public void addChestVehicleSaveData(ValueOutput valueOutput) {
        if (this.getContainerLootTable() != null) {
            valueOutput.putString("LootTable", this.getContainerLootTable().location().toString());
            if (this.getContainerLootTableSeed() != 0L) {
                valueOutput.putLong("LootTableSeed", this.getContainerLootTableSeed());
            }
        } else {
            ContainerHelper.saveAllItems(valueOutput, this.getItemStacks());
        }
    }

    default public void readChestVehicleSaveData(ValueInput valueInput) {
        this.clearItemStacks();
        ResourceKey resourceKey = valueInput.read("LootTable", LootTable.KEY_CODEC).orElse(null);
        this.setContainerLootTable(resourceKey);
        this.setContainerLootTableSeed(valueInput.getLongOr("LootTableSeed", 0L));
        if (resourceKey == null) {
            ContainerHelper.loadAllItems(valueInput, this.getItemStacks());
        }
    }

    default public void chestVehicleDestroyed(DamageSource damageSource, ServerLevel serverLevel, Entity entity) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        Containers.dropContents((Level)serverLevel, entity, (Container)this);
        Entity entity2 = damageSource.getDirectEntity();
        if (entity2 != null && entity2.getType() == EntityType.PLAYER) {
            PiglinAi.angerNearbyPiglins(serverLevel, (Player)entity2, true);
        }
    }

    default public InteractionResult interactWithContainerVehicle(Player player) {
        player.openMenu(this);
        return InteractionResult.SUCCESS;
    }

    default public void unpackChestVehicleLootTable(@Nullable Player player) {
        MinecraftServer minecraftServer = this.level().getServer();
        if (this.getContainerLootTable() != null && minecraftServer != null) {
            LootTable lootTable = minecraftServer.reloadableRegistries().getLootTable(this.getContainerLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getContainerLootTable());
            }
            this.setContainerLootTable(null);
            LootParams.Builder builder = new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.ORIGIN, this.position());
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getContainerLootTableSeed());
        }
    }

    default public void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().clear();
    }

    default public boolean isChestVehicleEmpty() {
        for (ItemStack itemStack : this.getItemStacks()) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public ItemStack removeChestVehicleItemNoUpdate(int n) {
        this.unpackChestVehicleLootTable(null);
        ItemStack itemStack = this.getItemStacks().get(n);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.getItemStacks().set(n, ItemStack.EMPTY);
        return itemStack;
    }

    default public ItemStack getChestVehicleItem(int n) {
        this.unpackChestVehicleLootTable(null);
        return this.getItemStacks().get(n);
    }

    default public ItemStack removeChestVehicleItem(int n, int n2) {
        this.unpackChestVehicleLootTable(null);
        return ContainerHelper.removeItem(this.getItemStacks(), n, n2);
    }

    default public void setChestVehicleItem(int n, ItemStack itemStack) {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().set(n, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
    }

    default public SlotAccess getChestVehicleSlot(final int n) {
        if (n >= 0 && n < this.getContainerSize()) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return ContainerEntity.this.getChestVehicleItem(n);
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    ContainerEntity.this.setChestVehicleItem(n, itemStack);
                    return true;
                }
            };
        }
        return SlotAccess.NULL;
    }

    default public boolean isChestVehicleStillValid(Player player) {
        return !this.isRemoved() && player.canInteractWithEntity(this.getBoundingBox(), 4.0);
    }
}

