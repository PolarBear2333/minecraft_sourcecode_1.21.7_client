/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;

public class VillagerHostilesSensor
extends NearestVisibleLivingEntitySensor {
    private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.builder().put(EntityType.DROWNED, (Object)Float.valueOf(8.0f)).put(EntityType.EVOKER, (Object)Float.valueOf(12.0f)).put(EntityType.HUSK, (Object)Float.valueOf(8.0f)).put(EntityType.ILLUSIONER, (Object)Float.valueOf(12.0f)).put(EntityType.PILLAGER, (Object)Float.valueOf(15.0f)).put(EntityType.RAVAGER, (Object)Float.valueOf(12.0f)).put(EntityType.VEX, (Object)Float.valueOf(8.0f)).put(EntityType.VINDICATOR, (Object)Float.valueOf(10.0f)).put(EntityType.ZOGLIN, (Object)Float.valueOf(10.0f)).put(EntityType.ZOMBIE, (Object)Float.valueOf(8.0f)).put(EntityType.ZOMBIE_VILLAGER, (Object)Float.valueOf(8.0f)).build();

    @Override
    protected boolean isMatchingEntity(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return this.isHostile(livingEntity2) && this.isClose(livingEntity, livingEntity2);
    }

    private boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
        float f = ((Float)ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingEntity2.getType())).floatValue();
        return livingEntity2.distanceToSqr(livingEntity) <= (double)(f * f);
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_HOSTILE;
    }

    private boolean isHostile(LivingEntity livingEntity) {
        return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingEntity.getType());
    }
}

