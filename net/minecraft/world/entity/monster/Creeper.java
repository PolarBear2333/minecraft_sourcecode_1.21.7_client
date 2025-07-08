/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Creeper
extends Monster {
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_POWERED = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_IGNITED = false;
    private static final boolean DEFAULT_POWERED = false;
    private static final short DEFAULT_MAX_SWELL = 30;
    private static final byte DEFAULT_EXPLOSION_RADIUS = 3;
    private int oldSwell;
    private int swell;
    private int maxSwell = 30;
    private int explosionRadius = 3;
    private int droppedSkulls;

    public Creeper(EntityType<? extends Creeper> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SwellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Ocelot>(this, Ocelot.class, 6.0f, 1.0, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Cat>(this, Cat.class, 6.0f, 1.0, 1.2));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return this.getComfortableFallDistance(0.0f);
        }
        return this.getComfortableFallDistance(this.getHealth() - 1.0f);
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        boolean bl = super.causeFallDamage(d, f, damageSource);
        this.swell += (int)(d * 1.5);
        if (this.swell > this.maxSwell - 5) {
            this.swell = this.maxSwell - 5;
        }
        return bl;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
        builder.define(DATA_IS_POWERED, false);
        builder.define(DATA_IS_IGNITED, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("powered", this.isPowered());
        valueOutput.putShort("Fuse", (short)this.maxSwell);
        valueOutput.putByte("ExplosionRadius", (byte)this.explosionRadius);
        valueOutput.putBoolean("ignited", this.isIgnited());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.entityData.set(DATA_IS_POWERED, valueInput.getBooleanOr("powered", false));
        this.maxSwell = valueInput.getShortOr("Fuse", (short)30);
        this.explosionRadius = valueInput.getByteOr("ExplosionRadius", (byte)3);
        if (valueInput.getBooleanOr("ignited", false)) {
            this.ignite();
        }
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            int n;
            this.oldSwell = this.swell;
            if (this.isIgnited()) {
                this.setSwellDir(1);
            }
            if ((n = this.getSwellDir()) > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
                this.gameEvent(GameEvent.PRIME_FUSE);
            }
            this.swell += n;
            if (this.swell < 0) {
                this.swell = 0;
            }
            if (this.swell >= this.maxSwell) {
                this.swell = this.maxSwell;
                this.explodeCreeper();
            }
        }
        super.tick();
    }

    @Override
    public void setTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity instanceof Goat) {
            return;
        }
        super.setTarget(livingEntity);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        Creeper creeper;
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
        Entity entity = damageSource.getEntity();
        if (entity != this && entity instanceof Creeper && (creeper = (Creeper)entity).canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(serverLevel, Items.CREEPER_HEAD);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        return true;
    }

    public boolean isPowered() {
        return this.entityData.get(DATA_IS_POWERED);
    }

    public float getSwelling(float f) {
        return Mth.lerp(f, this.oldSwell, this.swell) / (float)(this.maxSwell - 2);
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int n) {
        this.entityData.set(DATA_SWELL_DIR, n);
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        super.thunderHit(serverLevel, lightningBolt);
        this.entityData.set(DATA_IS_POWERED, true);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(ItemTags.CREEPER_IGNITERS)) {
            SoundEvent soundEvent = itemStack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
            this.level().playSound((Entity)player, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0f, this.random.nextFloat() * 0.4f + 0.8f);
            if (!this.level().isClientSide) {
                this.ignite();
                if (!itemStack.isDamageableItem()) {
                    itemStack.shrink(1);
                } else {
                    itemStack.hurtAndBreak(1, (LivingEntity)player, Creeper.getSlotForHand(interactionHand));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    private void explodeCreeper() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            float f = this.isPowered() ? 2.0f : 1.0f;
            this.dead = true;
            serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, Level.ExplosionInteraction.MOB);
            this.spawnLingeringCloud();
            this.triggerOnDeathMobEffects(serverLevel, Entity.RemovalReason.KILLED);
            this.discard();
        }
    }

    private void spawnLingeringCloud() {
        Collection<MobEffectInstance> collection = this.getActiveEffects();
        if (!collection.isEmpty()) {
            AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            areaEffectCloud.setRadius(2.5f);
            areaEffectCloud.setRadiusOnUse(-0.5f);
            areaEffectCloud.setWaitTime(10);
            areaEffectCloud.setDuration(300);
            areaEffectCloud.setPotionDurationScale(0.25f);
            areaEffectCloud.setRadiusPerTick(-areaEffectCloud.getRadius() / (float)areaEffectCloud.getDuration());
            for (MobEffectInstance mobEffectInstance : collection) {
                areaEffectCloud.addEffect(new MobEffectInstance(mobEffectInstance));
            }
            this.level().addFreshEntity(areaEffectCloud);
        }
    }

    public boolean isIgnited() {
        return this.entityData.get(DATA_IS_IGNITED);
    }

    public void ignite() {
        this.entityData.set(DATA_IS_IGNITED, true);
    }

    public boolean canDropMobsSkull() {
        return this.isPowered() && this.droppedSkulls < 1;
    }

    public void increaseDroppedSkulls() {
        ++this.droppedSkulls;
    }
}

