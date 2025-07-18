/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class HoglinSpecificSensor
extends Sensor<Hoglin> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, (Object[])new MemoryModuleType[0]);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Hoglin hoglin) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(serverLevel, hoglin));
        Optional<Object> optional = Optional.empty();
        int n = 0;
        ArrayList arrayList = Lists.newArrayList();
        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for (LivingEntity livingEntity2 : nearestVisibleLivingEntities.findAll(livingEntity -> !livingEntity.isBaby() && (livingEntity instanceof Piglin || livingEntity instanceof Hoglin))) {
            PathfinderMob pathfinderMob;
            if (livingEntity2 instanceof Piglin) {
                pathfinderMob = (Piglin)livingEntity2;
                ++n;
                if (optional.isEmpty()) {
                    optional = Optional.of(pathfinderMob);
                }
            }
            if (!(livingEntity2 instanceof Hoglin)) continue;
            pathfinderMob = (Hoglin)livingEntity2;
            arrayList.add(pathfinderMob);
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, arrayList);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, n);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, arrayList.size());
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, Hoglin hoglin) {
        return BlockPos.findClosestMatch(hoglin.blockPosition(), 8, 4, blockPos -> serverLevel.getBlockState((BlockPos)blockPos).is(BlockTags.HOGLIN_REPELLENTS));
    }
}

