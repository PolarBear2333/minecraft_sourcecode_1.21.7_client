/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;
    private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public static OneShot<PathfinderMob> stroll(float f) {
        return RandomStroll.stroll(f, true);
    }

    public static OneShot<PathfinderMob> stroll(float f, boolean bl) {
        return RandomStroll.strollFlyOrSwim(f, pathfinderMob -> LandRandomPos.getPos(pathfinderMob, 10, 7), bl ? pathfinderMob -> true : pathfinderMob -> !pathfinderMob.isInWater());
    }

    public static BehaviorControl<PathfinderMob> stroll(float f, int n, int n2) {
        return RandomStroll.strollFlyOrSwim(f, pathfinderMob -> LandRandomPos.getPos(pathfinderMob, n, n2), pathfinderMob -> true);
    }

    public static BehaviorControl<PathfinderMob> fly(float f) {
        return RandomStroll.strollFlyOrSwim(f, pathfinderMob -> RandomStroll.getTargetFlyPos(pathfinderMob, 10, 7), pathfinderMob -> true);
    }

    public static BehaviorControl<PathfinderMob> swim(float f) {
        return RandomStroll.strollFlyOrSwim(f, RandomStroll::getTargetSwimPos, Entity::isInWater);
    }

    private static OneShot<PathfinderMob> strollFlyOrSwim(float f, Function<PathfinderMob, Vec3> function, Predicate<PathfinderMob> predicate) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)instance, memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
            if (!predicate.test((PathfinderMob)pathfinderMob)) {
                return false;
            }
            Optional<Vec3> optional = Optional.ofNullable((Vec3)function.apply((PathfinderMob)pathfinderMob));
            memoryAccessor.setOrErase(optional.map(vec3 -> new WalkTarget((Vec3)vec3, f, 0)));
            return true;
        }));
    }

    @Nullable
    private static Vec3 getTargetSwimPos(PathfinderMob pathfinderMob) {
        Vec3 vec3 = null;
        Vec3 vec32 = null;
        for (int[] nArray : SWIM_XY_DISTANCE_TIERS) {
            vec32 = vec3 == null ? BehaviorUtils.getRandomSwimmablePos(pathfinderMob, nArray[0], nArray[1]) : pathfinderMob.position().add(pathfinderMob.position().vectorTo(vec3).normalize().multiply(nArray[0], nArray[1], nArray[0]));
            if (vec32 == null || pathfinderMob.level().getFluidState(BlockPos.containing(vec32)).isEmpty()) {
                return vec3;
            }
            vec3 = vec32;
        }
        return vec32;
    }

    @Nullable
    private static Vec3 getTargetFlyPos(PathfinderMob pathfinderMob, int n, int n2) {
        Vec3 vec3 = pathfinderMob.getViewVector(0.0f);
        return AirAndWaterRandomPos.getPos(pathfinderMob, n, n2, -2, vec3.x, vec3.z, 1.5707963705062866);
    }
}

