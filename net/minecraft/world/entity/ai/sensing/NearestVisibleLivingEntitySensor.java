/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

public abstract class NearestVisibleLivingEntitySensor
extends Sensor<LivingEntity> {
    protected abstract boolean isMatchingEntity(ServerLevel var1, LivingEntity var2, LivingEntity var3);

    protected abstract MemoryModuleType<LivingEntity> getMemory();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(this.getMemory());
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        livingEntity.getBrain().setMemory(this.getMemory(), this.getNearestEntity(serverLevel, livingEntity));
    }

    private Optional<LivingEntity> getNearestEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
        return this.getVisibleEntities(livingEntity).flatMap(nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findClosest(livingEntity2 -> this.isMatchingEntity(serverLevel, livingEntity, (LivingEntity)livingEntity2)));
    }

    protected Optional<NearestVisibleLivingEntities> getVisibleEntities(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

