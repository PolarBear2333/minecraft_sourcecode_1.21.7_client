/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class GolemSensor
extends Sensor<LivingEntity> {
    private static final int GOLEM_SCAN_RATE = 200;
    private static final int MEMORY_TIME_TO_LIVE = 599;

    public GolemSensor() {
        this(200);
    }

    public GolemSensor(int n) {
        super(n);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        GolemSensor.checkForNearbyGolem(livingEntity);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    public static void checkForNearbyGolem(LivingEntity livingEntity2) {
        Optional<List<LivingEntity>> optional = livingEntity2.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (optional.isEmpty()) {
            return;
        }
        boolean bl = optional.get().stream().anyMatch(livingEntity -> livingEntity.getType().equals(EntityType.IRON_GOLEM));
        if (bl) {
            GolemSensor.golemDetected(livingEntity2);
        }
    }

    public static void golemDetected(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 599L);
    }
}

