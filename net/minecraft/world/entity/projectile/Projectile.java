/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile
extends Entity
implements TraceableEntity {
    private static final boolean DEFAULT_LEFT_OWNER = false;
    private static final boolean DEFAULT_HAS_BEEN_SHOT = false;
    @Nullable
    protected EntityReference<Entity> owner;
    private boolean leftOwner = false;
    private boolean hasBeenShot = false;
    @Nullable
    private Entity lastDeflectedBy;

    Projectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    protected void setOwner(@Nullable EntityReference<Entity> entityReference) {
        this.owner = entityReference;
    }

    public void setOwner(@Nullable Entity entity) {
        this.setOwner(entity != null ? new EntityReference<Entity>(entity) : null);
    }

    @Override
    @Nullable
    public Entity getOwner() {
        return EntityReference.get(this.owner, this.level(), Entity.class);
    }

    public Entity getEffectSource() {
        return (Entity)MoreObjects.firstNonNull((Object)this.getOwner(), (Object)this);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        EntityReference.store(this.owner, valueOutput, "Owner");
        if (this.leftOwner) {
            valueOutput.putBoolean("LeftOwner", true);
        }
        valueOutput.putBoolean("HasBeenShot", this.hasBeenShot);
    }

    protected boolean ownedBy(Entity entity) {
        return this.owner != null && this.owner.matches(entity);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setOwner(EntityReference.read(valueInput, "Owner"));
        this.leftOwner = valueInput.getBooleanOr("LeftOwner", false);
        this.hasBeenShot = valueInput.getBooleanOr("HasBeenShot", false);
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            this.owner = projectile.owner;
        }
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        super.tick();
    }

    private boolean checkLeftOwner() {
        Entity entity2 = this.getOwner();
        if (entity2 != null) {
            AABB aABB = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
            return entity2.getRootVehicle().getSelfAndPassengers().filter(EntitySelector.CAN_BE_PICKED).noneMatch(entity -> aABB.intersects(entity.getBoundingBox()));
        }
        return true;
    }

    public Vec3 getMovementToShoot(double d, double d2, double d3, float f, float f2) {
        return new Vec3(d, d2, d3).normalize().add(this.random.triangle(0.0, 0.0172275 * (double)f2), this.random.triangle(0.0, 0.0172275 * (double)f2), this.random.triangle(0.0, 0.0172275 * (double)f2)).scale(f);
    }

    public void shoot(double d, double d2, double d3, float f, float f2) {
        Vec3 vec3 = this.getMovementToShoot(d, d2, d3, f, f2);
        this.setDeltaMovement(vec3);
        this.hasImpulse = true;
        double d4 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(vec3.y, d4) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity entity, float f, float f2, float f3, float f4, float f5) {
        float f6 = -Mth.sin(f2 * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        float f7 = -Mth.sin((f + f3) * ((float)Math.PI / 180));
        float f8 = Mth.cos(f2 * ((float)Math.PI / 180)) * Mth.cos(f * ((float)Math.PI / 180));
        this.shoot(f6, f7, f8, f4, f5);
        Vec3 vec3 = entity.getKnownMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.onGround() ? 0.0 : vec3.y, vec3.z));
    }

    @Override
    public void onAboveBubbleColumn(boolean bl, BlockPos blockPos) {
        double d = bl ? -0.03 : 0.1;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, d, 0.0));
        Projectile.sendBubbleColumnParticles(this.level(), blockPos);
    }

    @Override
    public void onInsideBubbleColumn(boolean bl) {
        double d = bl ? -0.03 : 0.06;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, d, 0.0));
        this.resetFallDistance();
    }

    public static <T extends Projectile> T spawnProjectileFromRotation(ProjectileFactory<T> projectileFactory, ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, float f, float f2, float f3) {
        return (T)Projectile.spawnProjectile(projectileFactory.create(serverLevel, livingEntity, itemStack), serverLevel, itemStack, projectile -> projectile.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), f, f2, f3));
    }

    public static <T extends Projectile> T spawnProjectileUsingShoot(ProjectileFactory<T> projectileFactory, ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, double d, double d2, double d3, float f, float f2) {
        return (T)Projectile.spawnProjectile(projectileFactory.create(serverLevel, livingEntity, itemStack), serverLevel, itemStack, projectile -> projectile.shoot(d, d2, d3, f, f2));
    }

    public static <T extends Projectile> T spawnProjectileUsingShoot(T t, ServerLevel serverLevel, ItemStack itemStack, double d, double d2, double d3, float f, float f2) {
        return (T)Projectile.spawnProjectile(t, serverLevel, itemStack, projectile2 -> t.shoot(d, d2, d3, f, f2));
    }

    public static <T extends Projectile> T spawnProjectile(T t, ServerLevel serverLevel, ItemStack itemStack) {
        return (T)Projectile.spawnProjectile(t, serverLevel, itemStack, projectile -> {});
    }

    public static <T extends Projectile> T spawnProjectile(T t, ServerLevel serverLevel, ItemStack itemStack, Consumer<T> consumer) {
        consumer.accept(t);
        serverLevel.addFreshEntity(t);
        t.applyOnProjectileSpawned(serverLevel, itemStack);
        return t;
    }

    public void applyOnProjectileSpawned(ServerLevel serverLevel, ItemStack itemStack) {
        AbstractArrow abstractArrow;
        EnchantmentHelper.onProjectileSpawned(serverLevel, itemStack, this, item -> {});
        DataComponentGetter dataComponentGetter = this;
        if (dataComponentGetter instanceof AbstractArrow && (dataComponentGetter = (abstractArrow = (AbstractArrow)dataComponentGetter).getWeaponItem()) != null && !((ItemStack)dataComponentGetter).isEmpty() && !itemStack.getItem().equals(((ItemStack)dataComponentGetter).getItem())) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, (ItemStack)dataComponentGetter, this, abstractArrow::onItemBreak);
        }
    }

    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult) {
        ProjectileDeflection projectileDeflection;
        BlockHitResult blockHitResult;
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            ProjectileDeflection projectileDeflection2 = entity.deflection(this);
            if (projectileDeflection2 != ProjectileDeflection.NONE) {
                if (entity != this.lastDeflectedBy && this.deflect(projectileDeflection2, entity, this.getOwner(), false)) {
                    this.lastDeflectedBy = entity;
                }
                return projectileDeflection2;
            }
        } else if (this.shouldBounceOnWorldBorder() && hitResult instanceof BlockHitResult && (blockHitResult = (BlockHitResult)hitResult).isWorldBorderHit() && this.deflect(projectileDeflection = ProjectileDeflection.REVERSE, null, this.getOwner(), false)) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            return projectileDeflection;
        }
        this.onHit(hitResult);
        return ProjectileDeflection.NONE;
    }

    protected boolean shouldBounceOnWorldBorder() {
        return false;
    }

    public boolean deflect(ProjectileDeflection projectileDeflection, @Nullable Entity entity, @Nullable Entity entity2, boolean bl) {
        projectileDeflection.deflect(this, entity, this.random);
        if (!this.level().isClientSide) {
            this.setOwner(entity2);
            this.onDeflection(entity, bl);
        }
        return true;
    }

    protected void onDeflection(@Nullable Entity entity, boolean bl) {
    }

    protected void onItemBreak(Item item) {
    }

    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile) {
                Projectile projectile = (Projectile)entity;
                projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
            }
            this.onHitEntity(entityHitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            this.onHitBlock(blockHitResult);
            BlockPos blockPos = blockHitResult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.level(), blockState, blockHitResult, this);
    }

    protected boolean canHitEntity(Entity entity) {
        if (!entity.canBeHitByProjectile()) {
            return false;
        }
        Entity entity2 = this.getOwner();
        return entity2 == null || this.leftOwner || !entity2.isPassengerOfSameVehicle(entity);
    }

    protected void updateRotation() {
        Vec3 vec3 = this.getDeltaMovement();
        double d = vec3.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875)));
    }

    protected static float lerpRotation(float f, float f2) {
        while (f2 - f < -180.0f) {
            f -= 360.0f;
        }
        while (f2 - f >= 180.0f) {
            f += 360.0f;
        }
        return Mth.lerp(0.2f, f, f2);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket((Entity)this, serverEntity, entity == null ? 0 : entity.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        Entity entity = this.level().getEntity(clientboundAddEntityPacket.getData());
        if (entity != null) {
            this.setOwner(entity);
        }
    }

    @Override
    public boolean mayInteract(ServerLevel serverLevel, BlockPos blockPos) {
        Entity entity = this.getOwner();
        if (entity instanceof Player) {
            return entity.mayInteract(serverLevel, blockPos);
        }
        return entity == null || serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public boolean mayBreak(ServerLevel serverLevel) {
        return this.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && serverLevel.getGameRules().getBoolean(GameRules.RULE_PROJECTILESCANBREAKBLOCKS);
    }

    @Override
    public boolean isPickable() {
        return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    @Override
    public float getPickRadius() {
        return this.isPickable() ? 1.0f : 0.0f;
    }

    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
        double d = this.getDeltaMovement().x;
        double d2 = this.getDeltaMovement().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)d2);
    }

    @Override
    public int getDimensionChangingDelay() {
        return 2;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (!this.isInvulnerableToBase(damageSource)) {
            this.markHurt();
        }
        return false;
    }

    @FunctionalInterface
    public static interface ProjectileFactory<T extends Projectile> {
        public T create(ServerLevel var1, LivingEntity var2, ItemStack var3);
    }
}

