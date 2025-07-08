/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class BackUpIfTooClose {
    public static OneShot<Mob> create(int n, float f) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.ATTACK_TARGET), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, mob, l) -> {
            LivingEntity livingEntity = (LivingEntity)instance.get(memoryAccessor3);
            if (livingEntity.closerThan(mob, n) && ((NearestVisibleLivingEntities)instance.get(memoryAccessor4)).contains(livingEntity)) {
                memoryAccessor2.set(new EntityTracker(livingEntity, true));
                mob.getMoveControl().strafe(-f, 0.0f);
                mob.setYRot(Mth.rotateIfNecessary(mob.getYRot(), mob.yHeadRot, 0.0f));
                return true;
            }
            return false;
        }));
    }
}

