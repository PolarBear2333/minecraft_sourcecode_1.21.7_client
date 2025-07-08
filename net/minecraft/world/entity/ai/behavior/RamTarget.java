/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public class RamTarget
extends Behavior<Goat> {
    public static final int TIME_OUT_DURATION = 200;
    public static final float RAM_SPEED_FORCE_FACTOR = 1.65f;
    private final Function<Goat, UniformInt> getTimeBetweenRams;
    private final TargetingConditions ramTargeting;
    private final float speed;
    private final ToDoubleFunction<Goat> getKnockbackForce;
    private Vec3 ramDirection;
    private final Function<Goat, SoundEvent> getImpactSound;
    private final Function<Goat, SoundEvent> getHornBreakSound;

    public RamTarget(Function<Goat, UniformInt> function, TargetingConditions targetingConditions, float f, ToDoubleFunction<Goat> toDoubleFunction, Function<Goat, SoundEvent> function2, Function<Goat, SoundEvent> function3) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.RAM_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 200);
        this.getTimeBetweenRams = function;
        this.ramTargeting = targetingConditions;
        this.speed = f;
        this.getKnockbackForce = toDoubleFunction;
        this.getImpactSound = function2;
        this.getHornBreakSound = function3;
        this.ramDirection = Vec3.ZERO;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Goat goat) {
        return goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Goat goat, long l) {
        return goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected void start(ServerLevel serverLevel, Goat goat, long l) {
        BlockPos blockPos = goat.blockPosition();
        Brain<Goat> brain = goat.getBrain();
        Vec3 vec3 = brain.getMemory(MemoryModuleType.RAM_TARGET).get();
        this.ramDirection = new Vec3((double)blockPos.getX() - vec3.x(), 0.0, (double)blockPos.getZ() - vec3.z()).normalize();
        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speed, 0));
    }

    @Override
    protected void tick(ServerLevel serverLevel, Goat goat, long l) {
        List<LivingEntity> list = serverLevel.getNearbyEntities(LivingEntity.class, this.ramTargeting, goat, goat.getBoundingBox());
        Brain<Goat> brain = goat.getBrain();
        if (!list.isEmpty()) {
            float f;
            DamageSource damageSource;
            LivingEntity livingEntity = list.get(0);
            if (livingEntity.hurtServer(serverLevel, damageSource = serverLevel.damageSources().noAggroMobAttack(goat), f = (float)goat.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
            }
            int n = goat.hasEffect(MobEffects.SPEED) ? goat.getEffect(MobEffects.SPEED).getAmplifier() + 1 : 0;
            int n2 = goat.hasEffect(MobEffects.SLOWNESS) ? goat.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1 : 0;
            float f2 = 0.25f * (float)(n - n2);
            float f3 = Mth.clamp(goat.getSpeed() * 1.65f, 0.2f, 3.0f) + f2;
            DamageSource damageSource2 = serverLevel.damageSources().mobAttack(goat);
            float f4 = livingEntity.applyItemBlocking(serverLevel, damageSource2, f);
            float f5 = f4 > 0.0f ? 0.5f : 1.0f;
            livingEntity.knockback((double)(f5 * f3) * this.getKnockbackForce.applyAsDouble(goat), this.ramDirection.x(), this.ramDirection.z());
            this.finishRam(serverLevel, goat);
            serverLevel.playSound(null, goat, this.getImpactSound.apply(goat), SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else if (this.hasRammedHornBreakingBlock(serverLevel, goat)) {
            serverLevel.playSound(null, goat, this.getImpactSound.apply(goat), SoundSource.NEUTRAL, 1.0f, 1.0f);
            boolean bl = goat.dropHorn();
            if (bl) {
                serverLevel.playSound(null, goat, this.getHornBreakSound.apply(goat), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            this.finishRam(serverLevel, goat);
        } else {
            boolean bl;
            Optional<WalkTarget> optional = brain.getMemory(MemoryModuleType.WALK_TARGET);
            Optional<Vec3> optional2 = brain.getMemory(MemoryModuleType.RAM_TARGET);
            boolean bl2 = bl = optional.isEmpty() || optional2.isEmpty() || optional.get().getTarget().currentPosition().closerThan(optional2.get(), 0.25);
            if (bl) {
                this.finishRam(serverLevel, goat);
            }
        }
    }

    private boolean hasRammedHornBreakingBlock(ServerLevel serverLevel, Goat goat) {
        Vec3 vec3 = goat.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
        BlockPos blockPos = BlockPos.containing(goat.position().add(vec3));
        return serverLevel.getBlockState(blockPos).is(BlockTags.SNAPS_GOAT_HORN) || serverLevel.getBlockState(blockPos.above()).is(BlockTags.SNAPS_GOAT_HORN);
    }

    protected void finishRam(ServerLevel serverLevel, Goat goat) {
        serverLevel.broadcastEntityEvent(goat, (byte)59);
        goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getTimeBetweenRams.apply(goat).sample(serverLevel.random));
        goat.getBrain().eraseMemory(MemoryModuleType.RAM_TARGET);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Goat)livingEntity, l);
    }
}

