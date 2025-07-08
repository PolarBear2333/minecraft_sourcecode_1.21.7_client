/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;

public class NearestLivingEntitySensor<T extends LivingEntity>
extends Sensor<T> {
    @Override
    protected void doTick(ServerLevel serverLevel, T t) {
        double d = ((LivingEntity)t).getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aABB = ((Entity)t).getBoundingBox().inflate(d, d, d);
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, aABB, livingEntity2 -> livingEntity2 != t && livingEntity2.isAlive());
        list.sort(Comparator.comparingDouble(arg_0 -> t.distanceToSqr(arg_0)));
        Brain<?> brain = ((LivingEntity)t).getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, list);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(serverLevel, (LivingEntity)t, list));
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}

