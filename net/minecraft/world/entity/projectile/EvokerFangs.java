/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EvokerFangs
extends Entity
implements TraceableEntity {
    public static final int ATTACK_DURATION = 20;
    public static final int LIFE_OFFSET = 2;
    public static final int ATTACK_TRIGGER_TICKS = 14;
    private static final int DEFAULT_WARMUP_DELAY = 0;
    private int warmupDelayTicks = 0;
    private boolean sentSpikeEvent;
    private int lifeTicks = 22;
    private boolean clientSideAttackStarted;
    @Nullable
    private EntityReference<LivingEntity> owner;

    public EvokerFangs(EntityType<? extends EvokerFangs> entityType, Level level) {
        super(entityType, level);
    }

    public EvokerFangs(Level level, double d, double d2, double d3, float f, int n, LivingEntity livingEntity) {
        this((EntityType<? extends EvokerFangs>)EntityType.EVOKER_FANGS, level);
        this.warmupDelayTicks = n;
        this.setOwner(livingEntity);
        this.setYRot(f * 57.295776f);
        this.setPos(d, d2, d3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity != null ? new EntityReference<LivingEntity>(livingEntity) : null;
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        return EntityReference.get(this.owner, this.level(), LivingEntity.class);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.warmupDelayTicks = valueInput.getIntOr("Warmup", 0);
        this.owner = EntityReference.read(valueInput, "Owner");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putInt("Warmup", this.warmupDelayTicks);
        EntityReference.store(this.owner, valueOutput, "Owner");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == 14) {
                    for (int i = 0; i < 12; ++i) {
                        double d = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double d2 = this.getY() + 0.05 + this.random.nextDouble();
                        double d3 = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double d4 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double d5 = 0.3 + this.random.nextDouble() * 0.3;
                        double d6 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.level().addParticle(ParticleTypes.CRIT, d, d2 + 1.0, d3, d4, d5, d6);
                    }
                }
            }
        } else if (--this.warmupDelayTicks < 0) {
            if (this.warmupDelayTicks == -8) {
                List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2, 0.0, 0.2));
                for (LivingEntity livingEntity : list) {
                    this.dealDamageTo(livingEntity);
                }
            }
            if (!this.sentSpikeEvent) {
                this.level().broadcastEntityEvent(this, (byte)4);
                this.sentSpikeEvent = true;
            }
            if (--this.lifeTicks < 0) {
                this.discard();
            }
        }
    }

    private void dealDamageTo(LivingEntity livingEntity) {
        LivingEntity livingEntity2 = this.getOwner();
        if (!livingEntity.isAlive() || livingEntity.isInvulnerable() || livingEntity == livingEntity2) {
            return;
        }
        if (livingEntity2 == null) {
            livingEntity.hurt(this.damageSources().magic(), 6.0f);
        } else {
            ServerLevel serverLevel;
            if (livingEntity2.isAlliedTo(livingEntity)) {
                return;
            }
            DamageSource damageSource = this.damageSources().indirectMagic(this, livingEntity2);
            Level level = this.level();
            if (level instanceof ServerLevel && livingEntity.hurtServer(serverLevel = (ServerLevel)level, damageSource, 6.0f)) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        super.handleEntityEvent(by);
        if (by == 4) {
            this.clientSideAttackStarted = true;
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0f, this.random.nextFloat() * 0.2f + 0.85f, false);
            }
        }
    }

    public float getAnimationProgress(float f) {
        if (!this.clientSideAttackStarted) {
            return 0.0f;
        }
        int n = this.lifeTicks - 2;
        if (n <= 0) {
            return 1.0f;
        }
        return 1.0f - ((float)n - f) / 20.0f;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }

    @Override
    @Nullable
    public /* synthetic */ Entity getOwner() {
        return this.getOwner();
    }
}

