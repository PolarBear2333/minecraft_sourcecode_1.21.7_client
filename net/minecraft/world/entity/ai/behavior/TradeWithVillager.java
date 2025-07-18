/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager
extends Behavior<Villager> {
    private Set<Item> trades = ImmutableSet.of();

    public TradeWithVillager() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        return BehaviorUtils.targetIsValid(villager.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return this.checkExtraStartConditions(serverLevel, villager);
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2, 0.5f, 2);
        this.trades = TradeWithVillager.figureOutWhatIAmWillingToTrade(villager, villager2);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (villager.distanceToSqr(villager2) > 5.0) {
            return;
        }
        BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2, 0.5f, 2);
        villager.gossip(serverLevel, villager2, l);
        boolean bl = villager.getVillagerData().profession().is(VillagerProfession.FARMER);
        if (villager.hasExcessFood() && (bl || villager2.wantsMoreFood())) {
            TradeWithVillager.throwHalfStack(villager, Villager.FOOD_POINTS.keySet(), villager2);
        }
        if (bl && villager.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getDefaultMaxStackSize() / 2) {
            TradeWithVillager.throwHalfStack(villager, (Set<Item>)ImmutableSet.of((Object)Items.WHEAT), villager2);
        }
        if (!this.trades.isEmpty() && villager.getInventory().hasAnyOf(this.trades)) {
            TradeWithVillager.throwHalfStack(villager, this.trades, villager2);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private static Set<Item> figureOutWhatIAmWillingToTrade(Villager villager, Villager villager2) {
        ImmutableSet<Item> immutableSet = villager2.getVillagerData().profession().value().requestedItems();
        ImmutableSet<Item> immutableSet2 = villager.getVillagerData().profession().value().requestedItems();
        return immutableSet.stream().filter(item -> !immutableSet2.contains(item)).collect(Collectors.toSet());
    }

    private static void throwHalfStack(Villager villager, Set<Item> set, LivingEntity livingEntity) {
        SimpleContainer simpleContainer = villager.getInventory();
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < simpleContainer.getContainerSize(); ++i) {
            int n;
            Item item;
            ItemStack itemStack2 = simpleContainer.getItem(i);
            if (itemStack2.isEmpty() || !set.contains(item = itemStack2.getItem())) continue;
            if (itemStack2.getCount() > itemStack2.getMaxStackSize() / 2) {
                n = itemStack2.getCount() / 2;
            } else {
                if (itemStack2.getCount() <= 24) continue;
                n = itemStack2.getCount() - 24;
            }
            itemStack2.shrink(n);
            itemStack = new ItemStack(item, n);
            break;
        }
        if (!itemStack.isEmpty()) {
            BehaviorUtils.throwItem(villager, itemStack, livingEntity.position());
        }
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

