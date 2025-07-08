/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Vex
extends Monster
implements TraceableEntity {
    public static final float FLAP_DEGREES_PER_TICK = 45.836624f;
    public static final int TICKS_PER_FLAP = Mth.ceil(3.9269907f);
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Vex.class, EntityDataSerializers.BYTE);
    private static final int FLAG_IS_CHARGING = 1;
    @Nullable
    private EntityReference<Mob> owner;
    @Nullable
    private BlockPos boundOrigin;
    private boolean hasLimitedLife;
    private int limitedLifeTicks;

    public Vex(EntityType<? extends Vex> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.moveControl = new VexMoveControl(this);
        this.xpReward = 3;
    }

    @Override
    public boolean isFlapping() {
        return this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    protected boolean isAffectedByBlocks() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20;
            this.hurt(this.damageSources().starve(), 1.0f);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new VexChargeAttackGoal());
        this.goalSelector.addGoal(8, new VexRandomMoveGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new VexCopyOwnerTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.boundOrigin = valueInput.read("bound_pos", BlockPos.CODEC).orElse(null);
        valueInput.getInt("life_ticks").ifPresentOrElse(this::setLimitedLife, () -> {
            this.hasLimitedLife = false;
        });
        this.owner = EntityReference.read(valueInput, "owner");
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof Vex) {
            Vex vex = (Vex)entity;
            this.owner = vex.owner;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.storeNullable("bound_pos", BlockPos.CODEC, this.boundOrigin);
        if (this.hasLimitedLife) {
            valueOutput.putInt("life_ticks", this.limitedLifeTicks);
        }
        EntityReference.store(this.owner, valueOutput, "owner");
    }

    @Override
    @Nullable
    public Mob getOwner() {
        return EntityReference.get(this.owner, this.level(), Mob.class);
    }

    @Nullable
    public BlockPos getBoundOrigin() {
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos blockPos) {
        this.boundOrigin = blockPos;
    }

    private boolean getVexFlag(int n) {
        byte by = this.entityData.get(DATA_FLAGS_ID);
        return (by & n) != 0;
    }

    private void setVexFlag(int n, boolean bl) {
        int n2 = this.entityData.get(DATA_FLAGS_ID).byteValue();
        n2 = bl ? (n2 |= n) : (n2 &= ~n);
        this.entityData.set(DATA_FLAGS_ID, (byte)(n2 & 0xFF));
    }

    public boolean isCharging() {
        return this.getVexFlag(1);
    }

    public void setIsCharging(boolean bl) {
        this.setVexFlag(1, bl);
    }

    public void setOwner(Mob mob) {
        this.owner = new EntityReference<Mob>(mob);
    }

    public void setLimitedLife(int n) {
        this.hasLimitedLife = true;
        this.limitedLifeTicks = n;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        this.populateDefaultEquipmentEnchantments(serverLevelAccessor, randomSource, difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    @Override
    @Nullable
    public /* synthetic */ Entity getOwner() {
        return this.getOwner();
    }

    class VexMoveControl
    extends MoveControl {
        public VexMoveControl(Vex vex2) {
            super(vex2);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                return;
            }
            Vec3 vec3 = new Vec3(this.wantedX - Vex.this.getX(), this.wantedY - Vex.this.getY(), this.wantedZ - Vex.this.getZ());
            double d = vec3.length();
            if (d < Vex.this.getBoundingBox().getSize()) {
                this.operation = MoveControl.Operation.WAIT;
                Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().scale(0.5));
            } else {
                Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05 / d)));
                if (Vex.this.getTarget() == null) {
                    Vec3 vec32 = Vex.this.getDeltaMovement();
                    Vex.this.setYRot(-((float)Mth.atan2(vec32.x, vec32.z)) * 57.295776f);
                    Vex.this.yBodyRot = Vex.this.getYRot();
                } else {
                    double d2 = Vex.this.getTarget().getX() - Vex.this.getX();
                    double d3 = Vex.this.getTarget().getZ() - Vex.this.getZ();
                    Vex.this.setYRot(-((float)Mth.atan2(d2, d3)) * 57.295776f);
                    Vex.this.yBodyRot = Vex.this.getYRot();
                }
            }
        }
    }

    class VexChargeAttackGoal
    extends Goal {
        public VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = Vex.this.getTarget();
            if (livingEntity != null && livingEntity.isAlive() && !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(VexChargeAttackGoal.reducedTickDelay(7)) == 0) {
                return Vex.this.distanceToSqr(livingEntity) > 4.0;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return Vex.this.getMoveControl().hasWanted() && Vex.this.isCharging() && Vex.this.getTarget() != null && Vex.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity livingEntity = Vex.this.getTarget();
            if (livingEntity != null) {
                Vec3 vec3 = livingEntity.getEyePosition();
                Vex.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0);
            }
            Vex.this.setIsCharging(true);
            Vex.this.playSound(SoundEvents.VEX_CHARGE, 1.0f, 1.0f);
        }

        @Override
        public void stop() {
            Vex.this.setIsCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = Vex.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            if (Vex.this.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
                Vex.this.doHurtTarget(VexChargeAttackGoal.getServerLevel(Vex.this.level()), livingEntity);
                Vex.this.setIsCharging(false);
            } else {
                double d = Vex.this.distanceToSqr(livingEntity);
                if (d < 9.0) {
                    Vec3 vec3 = livingEntity.getEyePosition();
                    Vex.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0);
                }
            }
        }
    }

    class VexRandomMoveGoal
    extends Goal {
        public VexRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(VexRandomMoveGoal.reducedTickDelay(7)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos blockPos = Vex.this.getBoundOrigin();
            if (blockPos == null) {
                blockPos = Vex.this.blockPosition();
            }
            for (int i = 0; i < 3; ++i) {
                BlockPos blockPos2 = blockPos.offset(Vex.this.random.nextInt(15) - 7, Vex.this.random.nextInt(11) - 5, Vex.this.random.nextInt(15) - 7);
                if (!Vex.this.level().isEmptyBlock(blockPos2)) continue;
                Vex.this.moveControl.setWantedPosition((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5, 0.25);
                if (Vex.this.getTarget() != null) break;
                Vex.this.getLookControl().setLookAt((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5, 180.0f, 20.0f);
                break;
            }
        }
    }

    class VexCopyOwnerTargetGoal
    extends TargetGoal {
        private final TargetingConditions copyOwnerTargeting;

        public VexCopyOwnerTargetGoal(PathfinderMob pathfinderMob) {
            super(pathfinderMob, false);
            this.copyOwnerTargeting = TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
        }

        @Override
        public boolean canUse() {
            Mob mob = Vex.this.getOwner();
            return mob != null && mob.getTarget() != null && this.canAttack(mob.getTarget(), this.copyOwnerTargeting);
        }

        @Override
        public void start() {
            Mob mob = Vex.this.getOwner();
            Vex.this.setTarget(mob != null ? mob.getTarget() : null);
            super.start();
        }
    }
}

