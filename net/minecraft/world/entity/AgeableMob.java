/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AgeableMob
extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgeableMob.class, EntityDataSerializers.BOOLEAN);
    public static final int BABY_START_AGE = -24000;
    private static final int FORCED_AGE_PARTICLE_TICKS = 40;
    protected static final int DEFAULT_AGE = 0;
    protected static final int DEFAULT_FORCED_AGE = 0;
    protected int age = 0;
    protected int forcedAge = 0;
    protected int forcedAgeTimer;

    protected AgeableMob(EntityType<? extends AgeableMob> entityType, Level level) {
        super((EntityType<? extends PathfinderMob>)entityType, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        AgeableMobGroupData ageableMobGroupData;
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMobGroupData(true);
        }
        if ((ageableMobGroupData = (AgeableMobGroupData)spawnGroupData).isShouldSpawnBaby() && ageableMobGroupData.getGroupSize() > 0 && serverLevelAccessor.getRandom().nextFloat() <= ageableMobGroupData.getBabySpawnChance()) {
            this.setAge(-24000);
        }
        ageableMobGroupData.increaseGroupSizeByOne();
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Nullable
    public abstract AgeableMob getBreedOffspring(ServerLevel var1, AgeableMob var2);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BABY_ID, false);
    }

    public boolean canBreed() {
        return false;
    }

    public int getAge() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_BABY_ID) != false ? -1 : 1;
        }
        return this.age;
    }

    public void ageUp(int n, boolean bl) {
        int n2;
        int n3 = n2 = this.getAge();
        if ((n2 += n * 20) > 0) {
            n2 = 0;
        }
        int n4 = n2 - n3;
        this.setAge(n2);
        if (bl) {
            this.forcedAge += n4;
            if (this.forcedAgeTimer == 0) {
                this.forcedAgeTimer = 40;
            }
        }
        if (this.getAge() == 0) {
            this.setAge(this.forcedAge);
        }
    }

    public void ageUp(int n) {
        this.ageUp(n, false);
    }

    public void setAge(int n) {
        int n2 = this.getAge();
        this.age = n;
        if (n2 < 0 && n >= 0 || n2 >= 0 && n < 0) {
            this.entityData.set(DATA_BABY_ID, n < 0);
            this.ageBoundaryReached();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Age", this.getAge());
        valueOutput.putInt("ForcedAge", this.forcedAge);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setAge(valueInput.getIntOr("Age", 0));
        this.forcedAge = valueInput.getIntOr("ForcedAge", 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (this.forcedAgeTimer > 0) {
                if (this.forcedAgeTimer % 4 == 0) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
                }
                --this.forcedAgeTimer;
            }
        } else if (this.isAlive()) {
            int n = this.getAge();
            if (n < 0) {
                this.setAge(++n);
            } else if (n > 0) {
                this.setAge(--n);
            }
        }
    }

    protected void ageBoundaryReached() {
        AbstractBoat abstractBoat;
        Entity entity;
        if (!this.isBaby() && this.isPassenger() && (entity = this.getVehicle()) instanceof AbstractBoat && !(abstractBoat = (AbstractBoat)entity).hasEnoughSpaceFor(this)) {
            this.stopRiding();
        }
    }

    @Override
    public boolean isBaby() {
        return this.getAge() < 0;
    }

    @Override
    public void setBaby(boolean bl) {
        this.setAge(bl ? -24000 : 0);
    }

    public static int getSpeedUpSecondsWhenFeeding(int n) {
        return (int)((float)(n / 20) * 0.1f);
    }

    @VisibleForTesting
    public int getForcedAge() {
        return this.forcedAge;
    }

    @VisibleForTesting
    public int getForcedAgeTimer() {
        return this.forcedAgeTimer;
    }

    public static class AgeableMobGroupData
    implements SpawnGroupData {
        private int groupSize;
        private final boolean shouldSpawnBaby;
        private final float babySpawnChance;

        public AgeableMobGroupData(boolean bl, float f) {
            this.shouldSpawnBaby = bl;
            this.babySpawnChance = f;
        }

        public AgeableMobGroupData(boolean bl) {
            this(bl, 0.05f);
        }

        public AgeableMobGroupData(float f) {
            this(true, f);
        }

        public int getGroupSize() {
            return this.groupSize;
        }

        public void increaseGroupSizeByOne() {
            ++this.groupSize;
        }

        public boolean isShouldSpawnBaby() {
            return this.shouldSpawnBaby;
        }

        public float getBabySpawnChance() {
            return this.babySpawnChance;
        }
    }
}

