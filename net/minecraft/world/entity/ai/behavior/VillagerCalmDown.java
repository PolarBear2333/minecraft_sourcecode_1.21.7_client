/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerCalmDown {
    private static final int SAFE_DISTANCE_FROM_DANGER = 36;

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.HURT_BY), instance.registered(MemoryModuleType.HURT_BY_ENTITY), instance.registered(MemoryModuleType.NEAREST_HOSTILE)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            boolean bl;
            boolean bl2 = bl = instance.tryGet(memoryAccessor).isPresent() || instance.tryGet(memoryAccessor3).isPresent() || instance.tryGet(memoryAccessor2).filter(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= 36.0).isPresent();
            if (!bl) {
                memoryAccessor.erase();
                memoryAccessor2.erase();
                livingEntity.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
            }
            return true;
        }));
    }
}

