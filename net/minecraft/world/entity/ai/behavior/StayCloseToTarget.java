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
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
    public static BehaviorControl<LivingEntity> create(Function<LivingEntity, Optional<PositionTracker>> function, Predicate<LivingEntity> predicate, int n, int n2, float f) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.LOOK_TARGET), instance.registered(MemoryModuleType.WALK_TARGET)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            Optional optional = (Optional)function.apply(livingEntity);
            if (optional.isEmpty() || !predicate.test(livingEntity)) {
                return false;
            }
            PositionTracker positionTracker = (PositionTracker)optional.get();
            if (livingEntity.position().closerThan(positionTracker.currentPosition(), n2)) {
                return false;
            }
            PositionTracker positionTracker2 = (PositionTracker)optional.get();
            memoryAccessor.set(positionTracker2);
            memoryAccessor2.set(new WalkTarget(positionTracker2, f, n));
            return true;
        }));
    }
}

