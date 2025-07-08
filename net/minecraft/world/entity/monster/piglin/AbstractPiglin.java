/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractPiglin
extends Monster {
    protected static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(AbstractPiglin.class, EntityDataSerializers.BOOLEAN);
    public static final int CONVERSION_TIME = 300;
    private static final boolean DEFAULT_IMMUNE_TO_ZOMBIFICATION = false;
    private static final boolean DEFAULT_PICK_UP_LOOT = true;
    private static final int DEFAULT_TIME_IN_OVERWORLD = 0;
    protected int timeInOverworld = 0;

    public AbstractPiglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.setCanPickUpLoot(true);
        this.applyOpenDoorsAbility();
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
    }

    private void applyOpenDoorsAbility() {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            this.getNavigation().setCanOpenDoors(true);
        }
    }

    protected abstract boolean canHunt();

    public void setImmuneToZombification(boolean bl) {
        this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, bl);
    }

    protected boolean isImmuneToZombification() {
        return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("IsImmuneToZombification", this.isImmuneToZombification());
        valueOutput.putInt("TimeInOverworld", this.timeInOverworld);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setCanPickUpLoot(valueInput.getBooleanOr("CanPickUpLoot", true));
        this.setImmuneToZombification(valueInput.getBooleanOr("IsImmuneToZombification", false));
        this.timeInOverworld = valueInput.getIntOr("TimeInOverworld", 0);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        this.timeInOverworld = this.isConverting() ? ++this.timeInOverworld : 0;
        if (this.timeInOverworld > 300) {
            this.playConvertedSound();
            this.finishConversion(serverLevel);
        }
    }

    @VisibleForTesting
    public void setTimeInOverworld(int n) {
        this.timeInOverworld = n;
    }

    public boolean isConverting() {
        return !this.level().dimensionType().piglinSafe() && !this.isImmuneToZombification() && !this.isNoAi();
    }

    protected void finishConversion(ServerLevel serverLevel) {
        this.convertTo(EntityType.ZOMBIFIED_PIGLIN, ConversionParams.single(this, true, true), zombifiedPiglin -> zombifiedPiglin.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)));
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    public abstract PiglinArmPose getArmPose();

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    protected boolean isHoldingMeleeWeapon() {
        return this.getMainHandItem().has(DataComponents.TOOL);
    }

    @Override
    public void playAmbientSound() {
        if (PiglinAi.isIdle(this)) {
            super.playAmbientSound();
        }
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    protected abstract void playConvertedSound();
}

