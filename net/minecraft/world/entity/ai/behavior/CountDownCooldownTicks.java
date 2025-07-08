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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownCooldownTicks
extends Behavior<LivingEntity> {
    private final MemoryModuleType<Integer> cooldownTicks;

    public CountDownCooldownTicks(MemoryModuleType<Integer> memoryModuleType) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(memoryModuleType, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        this.cooldownTicks = memoryModuleType;
    }

    private Optional<Integer> getCooldownTickMemory(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(this.cooldownTicks);
    }

    @Override
    protected boolean timedOut(long l) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        Optional<Integer> optional = this.getCooldownTickMemory(livingEntity);
        return optional.isPresent() && optional.get() > 0;
    }

    @Override
    protected void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        Optional<Integer> optional = this.getCooldownTickMemory(livingEntity);
        livingEntity.getBrain().setMemory(this.cooldownTicks, optional.get() - 1);
    }

    @Override
    protected void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        livingEntity.getBrain().eraseMemory(this.cooldownTicks);
    }
}

