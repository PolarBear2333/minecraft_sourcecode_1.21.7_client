/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Iterables
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor
extends NearestLivingEntitySensor<Warden> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.copyOf((Iterable)Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    @Override
    protected void doTick(ServerLevel serverLevel, Warden warden) {
        super.doTick(serverLevel, warden);
        WardenEntitySensor.getClosest(warden, livingEntity -> livingEntity.getType() == EntityType.PLAYER).or(() -> WardenEntitySensor.getClosest(warden, livingEntity -> livingEntity.getType() != EntityType.PLAYER)).ifPresentOrElse(livingEntity -> warden.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, livingEntity), () -> warden.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE));
    }

    private static Optional<LivingEntity> getClosest(Warden warden, Predicate<LivingEntity> predicate) {
        return warden.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter(warden::canTargetEntity).filter(predicate).findFirst();
    }
}

