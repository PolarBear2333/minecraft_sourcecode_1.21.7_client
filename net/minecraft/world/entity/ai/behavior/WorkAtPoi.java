/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;

public class WorkAtPoi
extends Behavior<Villager> {
    private static final int CHECK_COOLDOWN = 300;
    private static final double DISTANCE = 1.73;
    private long lastCheck;

    public WorkAtPoi() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.JOB_SITE, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED)));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        if (serverLevel.getGameTime() - this.lastCheck < 300L) {
            return false;
        }
        if (serverLevel.random.nextInt(2) != 0) {
            return false;
        }
        this.lastCheck = serverLevel.getGameTime();
        GlobalPos globalPos = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
        return globalPos.dimension() == serverLevel.dimension() && globalPos.pos().closerToCenterThan(villager.position(), 1.73);
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        Brain<Villager> brain = villager.getBrain();
        brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, l);
        brain.getMemory(MemoryModuleType.JOB_SITE).ifPresent(globalPos -> brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(globalPos.pos())));
        villager.playWorkSound();
        this.useWorkstation(serverLevel, villager);
        if (villager.shouldRestock()) {
            villager.restock();
        }
    }

    protected void useWorkstation(ServerLevel serverLevel, Villager villager) {
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (optional.isEmpty()) {
            return false;
        }
        GlobalPos globalPos = optional.get();
        return globalPos.dimension() == serverLevel.dimension() && globalPos.pos().closerToCenterThan(villager.position(), 1.73);
    }

    @Override
    protected /* synthetic */ boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        return this.checkExtraStartConditions(serverLevel, (Villager)livingEntity);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

