/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor
extends NearestVisibleLivingEntitySensor {
    public static final float TARGET_DETECTION_DISTANCE = 10.0f;

    @Override
    protected boolean isMatchingEntity(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (!livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && Sensor.isEntityAttackable(serverLevel, livingEntity, livingEntity2) && Frog.canEat(livingEntity2) && !this.isUnreachableAttackTarget(livingEntity, livingEntity2)) {
            return livingEntity2.closerThan(livingEntity, 10.0);
        }
        return false;
    }

    private boolean isUnreachableAttackTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        List list = livingEntity.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
        return list.contains(livingEntity2.getUUID());
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

