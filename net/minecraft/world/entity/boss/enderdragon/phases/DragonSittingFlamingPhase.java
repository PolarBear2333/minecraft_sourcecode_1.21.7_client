/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;

public class DragonSittingFlamingPhase
extends AbstractDragonSittingPhase {
    private static final int FLAME_DURATION = 200;
    private static final int SITTING_FLAME_ATTACKS_COUNT = 4;
    private static final int WARMUP_TIME = 10;
    private int flameTicks;
    private int flameCount;
    @Nullable
    private AreaEffectCloud flame;

    public DragonSittingFlamingPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doClientTick() {
        ++this.flameTicks;
        if (this.flameTicks % 2 == 0 && this.flameTicks < 10) {
            Vec3 vec3 = this.dragon.getHeadLookVector(1.0f).normalize();
            vec3.yRot(-0.7853982f);
            double d = this.dragon.head.getX();
            double d2 = this.dragon.head.getY(0.5);
            double d3 = this.dragon.head.getZ();
            for (int i = 0; i < 8; ++i) {
                double d4 = d + this.dragon.getRandom().nextGaussian() / 2.0;
                double d5 = d2 + this.dragon.getRandom().nextGaussian() / 2.0;
                double d6 = d3 + this.dragon.getRandom().nextGaussian() / 2.0;
                for (int j = 0; j < 6; ++j) {
                    this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, d4, d5, d6, -vec3.x * (double)0.08f * (double)j, -vec3.y * (double)0.6f, -vec3.z * (double)0.08f * (double)j);
                }
                vec3.yRot(0.19634955f);
            }
        }
    }

    @Override
    public void doServerTick(ServerLevel serverLevel) {
        ++this.flameTicks;
        if (this.flameTicks >= 200) {
            if (this.flameCount >= 4) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            } else {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
            }
        } else if (this.flameTicks == 10) {
            double d;
            Vec3 vec3 = new Vec3(this.dragon.head.getX() - this.dragon.getX(), 0.0, this.dragon.head.getZ() - this.dragon.getZ()).normalize();
            float f = 5.0f;
            double d2 = this.dragon.head.getX() + vec3.x * 5.0 / 2.0;
            double d3 = this.dragon.head.getZ() + vec3.z * 5.0 / 2.0;
            double d4 = d = this.dragon.head.getY(0.5);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(d2, d4, d3);
            while (serverLevel.isEmptyBlock(mutableBlockPos)) {
                if ((d4 -= 1.0) < 0.0) {
                    d4 = d;
                    break;
                }
                mutableBlockPos.set(d2, d4, d3);
            }
            d4 = Mth.floor(d4) + 1;
            this.flame = new AreaEffectCloud(serverLevel, d2, d4, d3);
            this.flame.setOwner(this.dragon);
            this.flame.setRadius(5.0f);
            this.flame.setDuration(200);
            this.flame.setCustomParticle(ParticleTypes.DRAGON_BREATH);
            this.flame.setPotionDurationScale(0.25f);
            this.flame.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE));
            serverLevel.addFreshEntity(this.flame);
        }
    }

    @Override
    public void begin() {
        this.flameTicks = 0;
        ++this.flameCount;
    }

    @Override
    public void end() {
        if (this.flame != null) {
            this.flame.discard();
            this.flame = null;
        }
    }

    public EnderDragonPhase<DragonSittingFlamingPhase> getPhase() {
        return EnderDragonPhase.SITTING_FLAMING;
    }

    public void resetFlameCount() {
        this.flameCount = 0;
    }
}

