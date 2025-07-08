/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Roar
extends Behavior<Warden> {
    private static final int TICKS_BEFORE_PLAYING_ROAR_SOUND = 25;
    private static final int ROAR_ANGER_INCREASE = 20;

    public Roar() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.ROAR_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.ROAR_SOUND_COOLDOWN, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.ROAR_SOUND_DELAY, (Object)((Object)MemoryStatus.REGISTERED)), WardenAi.ROAR_DURATION);
    }

    @Override
    protected void start(ServerLevel serverLevel, Warden warden, long l) {
        Brain<Warden> brain = warden.getBrain();
        brain.setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_DELAY, Unit.INSTANCE, 25L);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        LivingEntity livingEntity = warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).get();
        BehaviorUtils.lookAtEntity(warden, livingEntity);
        warden.setPose(Pose.ROARING);
        warden.increaseAngerAt(livingEntity, 20, false);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Warden warden, long l) {
        return true;
    }

    @Override
    protected void tick(ServerLevel serverLevel, Warden warden, long l) {
        if (warden.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_DELAY) || warden.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_COOLDOWN)) {
            return;
        }
        warden.getBrain().setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_COOLDOWN, Unit.INSTANCE, WardenAi.ROAR_DURATION - 25);
        warden.playSound(SoundEvents.WARDEN_ROAR, 3.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel serverLevel, Warden warden, long l) {
        if (warden.hasPose(Pose.ROARING)) {
            warden.setPose(Pose.STANDING);
        }
        warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).ifPresent(warden::setAttackTarget);
        warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Warden)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Warden)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Warden)livingEntity, l);
    }
}

