/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class CaveSpider
extends Spider {
    public CaveSpider(EntityType<? extends CaveSpider> entityType, Level level) {
        super((EntityType<? extends Spider>)entityType, level);
    }

    public static AttributeSupplier.Builder createCaveSpider() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 12.0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        if (super.doHurtTarget(serverLevel, entity)) {
            if (entity instanceof LivingEntity) {
                int n = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    n = 7;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    n = 15;
                }
                if (n > 0) {
                    ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, n * 20, 0), this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        return spawnGroupData;
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity entity) {
        if (entity.getBbWidth() <= this.getBbWidth()) {
            return new Vec3(0.0, 0.21875 * (double)this.getScale(), 0.0);
        }
        return super.getVehicleAttachmentPoint(entity);
    }
}

