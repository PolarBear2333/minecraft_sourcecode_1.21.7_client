/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAtTargetSink
extends Behavior<Mob> {
    public LookAtTargetSink(int n, int n2) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), n, n2);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        return mob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter(positionTracker -> positionTracker.isVisibleBy(mob)).isPresent();
    }

    @Override
    protected void stop(ServerLevel serverLevel, Mob mob, long l) {
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Mob mob, long l) {
        mob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent(positionTracker -> mob.getLookControl().setLookAt(positionTracker.currentPosition()));
    }
}

