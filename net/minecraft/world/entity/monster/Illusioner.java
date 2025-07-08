/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class Illusioner
extends SpellcasterIllager
implements RangedAttackMob {
    private static final int NUM_ILLUSIONS = 4;
    private static final int ILLUSION_TRANSITION_TICKS = 3;
    public static final int ILLUSION_SPREAD = 3;
    private int clientSideIllusionTicks;
    private final Vec3[][] clientSideIllusionOffsets;

    public Illusioner(EntityType<? extends Illusioner> entityType, Level level) {
        super((EntityType<? extends SpellcasterIllager>)entityType, level);
        this.xpReward = 5;
        this.clientSideIllusionOffsets = new Vec3[2][4];
        for (int i = 0; i < 4; ++i) {
            this.clientSideIllusionOffsets[0][i] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][i] = Vec3.ZERO;
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Creaking>(this, Creaking.class, 8.0f, 1.0, 1.2));
        this.goalSelector.addGoal(4, new IllusionerMirrorSpellGoal());
        this.goalSelector.addGoal(5, new IllusionerBlindnessSpellGoal());
        this.goalSelector.addGoal(6, new RangedBowAttackGoal<Illusioner>(this, 0.5, 20, 15.0f));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, false).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.MAX_HEALTH, 32.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide && this.isInvisible()) {
            --this.clientSideIllusionTicks;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }
            if (this.hurtTime == 1 || this.tickCount % 1200 == 0) {
                int n;
                this.clientSideIllusionTicks = 3;
                float f = -6.0f;
                int n2 = 13;
                for (n = 0; n < 4; ++n) {
                    this.clientSideIllusionOffsets[0][n] = this.clientSideIllusionOffsets[1][n];
                    this.clientSideIllusionOffsets[1][n] = new Vec3((double)(-6.0f + (float)this.random.nextInt(13)) * 0.5, Math.max(0, this.random.nextInt(6) - 4), (double)(-6.0f + (float)this.random.nextInt(13)) * 0.5);
                }
                for (n = 0; n < 16; ++n) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5), this.getRandomY(), this.getZ(0.5), 0.0, 0.0, 0.0);
                }
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0f, 1.0f, false);
            } else if (this.hurtTime == this.hurtDuration - 1) {
                this.clientSideIllusionTicks = 3;
                for (int i = 0; i < 4; ++i) {
                    this.clientSideIllusionOffsets[0][i] = this.clientSideIllusionOffsets[1][i];
                    this.clientSideIllusionOffsets[1][i] = new Vec3(0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    public Vec3[] getIllusionOffsets(float f) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        }
        double d = ((float)this.clientSideIllusionTicks - f) / 3.0f;
        d = Math.pow(d, 0.25);
        Vec3[] vec3Array = new Vec3[4];
        for (int i = 0; i < 4; ++i) {
            vec3Array[i] = this.clientSideIllusionOffsets[1][i].scale(1.0 - d).add(this.clientSideIllusionOffsets[0][i].scale(d));
        }
        return vec3Array;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.ILLUSIONER_CAST_SPELL;
    }

    @Override
    public void applyRaidBuffs(ServerLevel serverLevel, int n, boolean bl) {
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        ItemStack itemStack2 = this.getProjectile(itemStack);
        AbstractArrow abstractArrow = ProjectileUtil.getMobArrow(this, itemStack2, f, itemStack);
        double d = livingEntity.getX() - this.getX();
        double d2 = livingEntity.getY(0.3333333333333333) - abstractArrow.getY();
        double d3 = livingEntity.getZ() - this.getZ();
        double d4 = Math.sqrt(d * d + d3 * d3);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Projectile.spawnProjectileUsingShoot(abstractArrow, serverLevel, itemStack2, d, d2 + d4 * (double)0.2f, d3, 1.6f, 14 - serverLevel.getDifficulty().getId() * 4);
        }
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        if (this.isAggressive()) {
            return AbstractIllager.IllagerArmPose.BOW_AND_ARROW;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    class IllusionerMirrorSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        IllusionerMirrorSpellGoal() {
            super(Illusioner.this);
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            return !Illusioner.this.hasEffect(MobEffects.INVISIBILITY);
        }

        @Override
        protected int getCastingTime() {
            return 20;
        }

        @Override
        protected int getCastingInterval() {
            return 340;
        }

        @Override
        protected void performSpellCasting() {
            Illusioner.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
        }

        @Override
        @Nullable
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.DISAPPEAR;
        }
    }

    class IllusionerBlindnessSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private int lastTargetId;

        IllusionerBlindnessSpellGoal() {
            super(Illusioner.this);
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            if (Illusioner.this.getTarget() == null) {
                return false;
            }
            if (Illusioner.this.getTarget().getId() == this.lastTargetId) {
                return false;
            }
            return Illusioner.this.level().getCurrentDifficultyAt(Illusioner.this.blockPosition()).isHarderThan(Difficulty.NORMAL.ordinal());
        }

        @Override
        public void start() {
            super.start();
            LivingEntity livingEntity = Illusioner.this.getTarget();
            if (livingEntity != null) {
                this.lastTargetId = livingEntity.getId();
            }
        }

        @Override
        protected int getCastingTime() {
            return 20;
        }

        @Override
        protected int getCastingInterval() {
            return 180;
        }

        @Override
        protected void performSpellCasting() {
            Illusioner.this.getTarget().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400), Illusioner.this);
        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_BLINDNESS;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.BLINDNESS;
        }
    }
}

