/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int checkInterval;
    protected BlockPos pos;

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level) {
        super(entityType, level);
    }

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level, BlockPos blockPos) {
        this(entityType, level);
        this.pos = blockPos;
    }

    protected abstract void recalculateBoundingBox();

    @Override
    public void tick() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.checkBelowWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.dropItem(serverLevel, null);
                }
            }
        }
    }

    public abstract boolean survives();

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (!this.level().mayInteract(player, this.pos)) {
                return true;
            }
            return this.hurtOrSimulate(this.damageSources().playerAttack(player), 0.0f);
        }
        return false;
    }

    @Override
    public boolean hurtClient(DamageSource damageSource) {
        return !this.isInvulnerableToBase(damageSource);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && damageSource.getEntity() instanceof Mob) {
            return false;
        }
        if (!this.isRemoved()) {
            this.kill(serverLevel);
            this.markHurt();
            this.dropItem(serverLevel, damageSource.getEntity());
        }
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        Entity entity = explosion.getDirectSourceEntity();
        if (entity != null && entity.isInWater()) {
            return true;
        }
        if (explosion.shouldAffectBlocklikeEntities()) {
            return super.ignoreExplosion(explosion);
        }
        return true;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (!this.isRemoved() && vec3.lengthSqr() > 0.0) {
                this.kill(serverLevel);
                this.dropItem(serverLevel, null);
            }
        }
    }

    @Override
    public void push(double d, double d2, double d3) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (!this.isRemoved() && d * d + d2 * d2 + d3 * d3 > 0.0) {
                this.kill(serverLevel);
                this.dropItem(serverLevel, null);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.store("block_pos", BlockPos.CODEC, this.getPos());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        BlockPos blockPos = valueInput.read("block_pos", BlockPos.CODEC).orElse(null);
        if (blockPos == null || !blockPos.closerThan(this.blockPosition(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", (Object)blockPos);
            return;
        }
        this.pos = blockPos;
    }

    public abstract void dropItem(ServerLevel var1, @Nullable Entity var2);

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setPos(double d, double d2, double d3) {
        this.pos = BlockPos.containing(d, d2, d3);
        this.recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
    }

    @Override
    public void refreshDimensions() {
    }
}

