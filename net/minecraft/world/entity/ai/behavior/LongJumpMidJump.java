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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LongJumpMidJump
extends Behavior<Mob> {
    public static final int TIME_OUT_DURATION = 100;
    private final UniformInt timeBetweenLongJumps;
    private final SoundEvent landingSound;

    public LongJumpMidJump(UniformInt uniformInt, SoundEvent soundEvent) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 100);
        this.timeBetweenLongJumps = uniformInt;
        this.landingSound = soundEvent;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        return !mob.onGround();
    }

    @Override
    protected void start(ServerLevel serverLevel, Mob mob, long l) {
        mob.setDiscardFriction(true);
        mob.setPose(Pose.LONG_JUMPING);
    }

    @Override
    protected void stop(ServerLevel serverLevel, Mob mob, long l) {
        if (mob.onGround()) {
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.1f, 1.0, 0.1f));
            serverLevel.playSound(null, mob, this.landingSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
        }
        mob.setDiscardFriction(false);
        mob.setPose(Pose.STANDING);
        mob.getBrain().eraseMemory(MemoryModuleType.LONG_JUMP_MID_JUMP);
        mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random));
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Mob)livingEntity, l);
    }
}

