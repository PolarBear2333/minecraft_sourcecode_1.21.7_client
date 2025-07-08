/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class OminousItemSpawner
extends Entity {
    private static final int SPAWN_ITEM_DELAY_MIN = 60;
    private static final int SPAWN_ITEM_DELAY_MAX = 120;
    private static final String TAG_SPAWN_ITEM_AFTER_TICKS = "spawn_item_after_ticks";
    private static final String TAG_ITEM = "item";
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(OminousItemSpawner.class, EntityDataSerializers.ITEM_STACK);
    public static final int TICKS_BEFORE_ABOUT_TO_SPAWN_SOUND = 36;
    private long spawnItemAfterTicks;

    public OminousItemSpawner(EntityType<? extends OminousItemSpawner> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public static OminousItemSpawner create(Level level, ItemStack itemStack) {
        OminousItemSpawner ominousItemSpawner = new OminousItemSpawner((EntityType<? extends OminousItemSpawner>)EntityType.OMINOUS_ITEM_SPAWNER, level);
        ominousItemSpawner.spawnItemAfterTicks = level.random.nextIntBetweenInclusive(60, 120);
        ominousItemSpawner.setItem(itemStack);
        return ominousItemSpawner;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.tickServer(serverLevel);
        } else {
            this.tickClient();
        }
    }

    private void tickServer(ServerLevel serverLevel) {
        if ((long)this.tickCount == this.spawnItemAfterTicks - 36L) {
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundSource.NEUTRAL);
        }
        if ((long)this.tickCount >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill(serverLevel);
        }
    }

    private void tickClient() {
        if (this.level().getGameTime() % 5L == 0L) {
            this.addParticles();
        }
    }

    private void spawnItem() {
        Entity entity;
        Object object = this.level();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = this.getItem();
        if (((ItemStack)object).isEmpty()) {
            return;
        }
        Item item = ((ItemStack)object).getItem();
        if (item instanceof ProjectileItem) {
            ProjectileItem projectileItem = (ProjectileItem)((Object)item);
            entity = this.spawnProjectile(serverLevel, projectileItem, (ItemStack)object);
        } else {
            entity = new ItemEntity(serverLevel, this.getX(), this.getY(), this.getZ(), (ItemStack)object);
            serverLevel.addFreshEntity(entity);
        }
        serverLevel.levelEvent(3021, this.blockPosition(), 1);
        serverLevel.gameEvent(entity, GameEvent.ENTITY_PLACE, this.position());
        this.setItem(ItemStack.EMPTY);
    }

    private Entity spawnProjectile(ServerLevel serverLevel, ProjectileItem projectileItem, ItemStack itemStack) {
        ProjectileItem.DispenseConfig dispenseConfig = projectileItem.createDispenseConfig();
        dispenseConfig.overrideDispenseEvent().ifPresent(n -> serverLevel.levelEvent(n, this.blockPosition(), 0));
        Direction direction = Direction.DOWN;
        Projectile projectile = Projectile.spawnProjectileUsingShoot(projectileItem.asProjectile(serverLevel, this.position(), itemStack, direction), serverLevel, itemStack, direction.getStepX(), direction.getStepY(), direction.getStepZ(), dispenseConfig.power(), dispenseConfig.uncertainty());
        projectile.setOwner(this);
        return projectile;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setItem(valueInput.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        this.spawnItemAfterTicks = valueInput.getLongOr(TAG_SPAWN_ITEM_AFTER_TICKS, 0L);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        if (!this.getItem().isEmpty()) {
            valueOutput.store(TAG_ITEM, ItemStack.CODEC, this.getItem());
        }
        valueOutput.putLong(TAG_SPAWN_ITEM_AFTER_TICKS, this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity entity) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public void addParticles() {
        Vec3 vec3 = this.position();
        int n = this.random.nextIntBetweenInclusive(1, 3);
        for (int i = 0; i < n; ++i) {
            double d = 0.4;
            Vec3 vec32 = new Vec3(this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3 vec33 = vec3.vectorTo(vec32);
            this.level().addParticle(ParticleTypes.OMINOUS_SPAWNING, vec3.x(), vec3.y(), vec3.z(), vec33.x(), vec33.y(), vec33.z());
        }
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    private void setItem(ItemStack itemStack) {
        this.getEntityData().set(DATA_ITEM, itemStack);
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }
}

