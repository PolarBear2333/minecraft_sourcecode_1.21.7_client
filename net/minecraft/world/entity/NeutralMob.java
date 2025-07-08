/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface NeutralMob {
    public static final String TAG_ANGER_TIME = "AngerTime";
    public static final String TAG_ANGRY_AT = "AngryAt";

    public int getRemainingPersistentAngerTime();

    public void setRemainingPersistentAngerTime(int var1);

    @Nullable
    public UUID getPersistentAngerTarget();

    public void setPersistentAngerTarget(@Nullable UUID var1);

    public void startPersistentAngerTimer();

    default public void addPersistentAngerSaveData(ValueOutput valueOutput) {
        valueOutput.putInt(TAG_ANGER_TIME, this.getRemainingPersistentAngerTime());
        valueOutput.storeNullable(TAG_ANGRY_AT, UUIDUtil.CODEC, this.getPersistentAngerTarget());
    }

    default public void readPersistentAngerSaveData(Level level, ValueInput valueInput) {
        Entity entity;
        this.setRemainingPersistentAngerTime(valueInput.getIntOr(TAG_ANGER_TIME, 0));
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        UUID uUID = valueInput.read(TAG_ANGRY_AT, UUIDUtil.CODEC).orElse(null);
        this.setPersistentAngerTarget(uUID);
        Entity entity2 = entity = uUID != null ? serverLevel.getEntity(uUID) : null;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            this.setTarget(livingEntity);
        }
    }

    default public void updatePersistentAnger(ServerLevel serverLevel, boolean bl) {
        LivingEntity livingEntity = this.getTarget();
        UUID uUID = this.getPersistentAngerTarget();
        if ((livingEntity == null || livingEntity.isDeadOrDying()) && uUID != null && serverLevel.getEntity(uUID) instanceof Mob) {
            this.stopBeingAngry();
            return;
        }
        if (livingEntity != null && !Objects.equals(uUID, livingEntity.getUUID())) {
            this.setPersistentAngerTarget(livingEntity.getUUID());
            this.startPersistentAngerTimer();
        }
        if (!(this.getRemainingPersistentAngerTime() <= 0 || livingEntity != null && livingEntity.getType() == EntityType.PLAYER && bl)) {
            this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
            if (this.getRemainingPersistentAngerTime() == 0) {
                this.stopBeingAngry();
            }
        }
    }

    default public boolean isAngryAt(LivingEntity livingEntity, ServerLevel serverLevel) {
        if (!this.canAttack(livingEntity)) {
            return false;
        }
        if (livingEntity.getType() == EntityType.PLAYER && this.isAngryAtAllPlayers(serverLevel)) {
            return true;
        }
        return livingEntity.getUUID().equals(this.getPersistentAngerTarget());
    }

    default public boolean isAngryAtAllPlayers(ServerLevel serverLevel) {
        return serverLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
    }

    default public boolean isAngry() {
        return this.getRemainingPersistentAngerTime() > 0;
    }

    default public void playerDied(ServerLevel serverLevel, Player player) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            return;
        }
        if (!player.getUUID().equals(this.getPersistentAngerTarget())) {
            return;
        }
        this.stopBeingAngry();
    }

    default public void forgetCurrentTargetAndRefreshUniversalAnger() {
        this.stopBeingAngry();
        this.startPersistentAngerTimer();
    }

    default public void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
        this.setRemainingPersistentAngerTime(0);
    }

    @Nullable
    public LivingEntity getLastHurtByMob();

    public void setLastHurtByMob(@Nullable LivingEntity var1);

    public void setTarget(@Nullable LivingEntity var1);

    public boolean canAttack(LivingEntity var1);

    @Nullable
    public LivingEntity getTarget();
}

