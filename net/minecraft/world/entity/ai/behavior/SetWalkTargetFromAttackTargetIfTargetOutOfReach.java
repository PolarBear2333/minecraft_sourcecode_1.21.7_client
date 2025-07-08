/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach {
    private static final int PROJECTILE_ATTACK_RANGE_BUFFER = 1;

    public static BehaviorControl<Mob> create(float f) {
        return SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(livingEntity -> Float.valueOf(f));
    }

    public static BehaviorControl<Mob> create(Function<LivingEntity, Float> function) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, mob, l) -> {
            LivingEntity livingEntity = (LivingEntity)instance.get(memoryAccessor3);
            Optional optional = instance.tryGet(memoryAccessor4);
            if (optional.isPresent() && ((NearestVisibleLivingEntities)optional.get()).contains(livingEntity) && BehaviorUtils.isWithinAttackRange(mob, livingEntity, 1)) {
                memoryAccessor.erase();
            } else {
                memoryAccessor2.set(new EntityTracker(livingEntity, true));
                memoryAccessor.set(new WalkTarget(new EntityTracker(livingEntity, false), ((Float)function.apply(mob)).floatValue(), 0));
            }
            return true;
        }));
    }
}

