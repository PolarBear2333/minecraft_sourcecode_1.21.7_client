/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderpearl
extends ThrowableItemProjectile {
    private long ticketTimer = 0L;

    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownEnderpearl(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(EntityType.ENDER_PEARL, livingEntity, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void setOwner(@Nullable EntityReference<Entity> entityReference) {
        this.deregisterFromCurrentOwner();
        super.setOwner(entityReference);
        this.registerToCurrentOwner();
    }

    private void deregisterFromCurrentOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.deregisterEnderPearl(this);
        }
    }

    private void registerToCurrentOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.registerEnderPearl(this);
        }
    }

    @Override
    @Nullable
    public Entity getOwner() {
        Level level;
        if (this.owner == null || !((level = this.level()) instanceof ServerLevel)) {
            return super.getOwner();
        }
        ServerLevel serverLevel = (ServerLevel)level;
        return this.owner.getEntity(uUID -> ThrownEnderpearl.findOwnerInAnyDimension(serverLevel, uUID), Entity.class);
    }

    @Nullable
    private static Entity findOwnerInAnyDimension(ServerLevel serverLevel, UUID uUID) {
        Entity entity = serverLevel.getEntity(uUID);
        if (entity != null) {
            return entity;
        }
        for (ServerLevel serverLevel2 : serverLevel.getServer().getAllLevels()) {
            if (serverLevel2 == serverLevel || (entity = serverLevel2.getEntity(uUID)) == null) continue;
            return entity;
        }
        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        ServerLevel serverLevel;
        Object object;
        block14: {
            block13: {
                super.onHit(hitResult);
                for (int i = 0; i < 32; ++i) {
                    this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
                }
                object = this.level();
                if (!(object instanceof ServerLevel)) break block13;
                serverLevel = (ServerLevel)object;
                if (!this.isRemoved()) break block14;
            }
            return;
        }
        object = this.getOwner();
        if (object == null || !ThrownEnderpearl.isAllowedToTeleportOwner((Entity)object, serverLevel)) {
            this.discard();
            return;
        }
        Vec3 vec3 = this.oldPosition();
        if (object instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)object;
            if (serverPlayer.connection.isAcceptingMessages()) {
                LivingEntity livingEntity;
                if (this.random.nextFloat() < 0.05f && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && (livingEntity = EntityType.ENDERMITE.create(serverLevel, EntitySpawnReason.TRIGGERED)) != null) {
                    livingEntity.snapTo(((Entity)object).getX(), ((Entity)object).getY(), ((Entity)object).getZ(), ((Entity)object).getYRot(), ((Entity)object).getXRot());
                    serverLevel.addFreshEntity(livingEntity);
                }
                if (this.isOnPortalCooldown()) {
                    ((Entity)object).setPortalCooldown();
                }
                if ((livingEntity = serverPlayer.teleport(new TeleportTransition(serverLevel, vec3, Vec3.ZERO, 0.0f, 0.0f, Relative.union(Relative.ROTATION, Relative.DELTA), TeleportTransition.DO_NOTHING))) != null) {
                    ((ServerPlayer)livingEntity).resetFallDistance();
                    ((Player)livingEntity).resetCurrentImpulseContext();
                    ((ServerPlayer)livingEntity).hurtServer(serverPlayer.level(), this.damageSources().enderPearl(), 5.0f);
                }
                this.playSound(serverLevel, vec3);
            }
        } else {
            Entity entity = ((Entity)object).teleport(new TeleportTransition(serverLevel, vec3, ((Entity)object).getDeltaMovement(), ((Entity)object).getYRot(), ((Entity)object).getXRot(), TeleportTransition.DO_NOTHING));
            if (entity != null) {
                entity.resetFallDistance();
            }
            this.playSound(serverLevel, vec3);
        }
        this.discard();
    }

    private static boolean isAllowedToTeleportOwner(Entity entity, Level level) {
        if (entity.level().dimension() == level.dimension()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                return livingEntity.isAlive() && !livingEntity.isSleeping();
            }
            return entity.isAlive();
        }
        return entity.canUsePortal(true);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void tick() {
        var1_1 = SectionPos.blockToSectionCoord(this.position().x());
        var2_2 = SectionPos.blockToSectionCoord(this.position().z());
        var3_3 = this.getOwner();
        if (!(var3_3 instanceof ServerPlayer)) ** GOTO lbl-1000
        var4_4 = (ServerPlayer)var3_3;
        if (!var3_3.isAlive() && var4_4.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else lbl-1000:
        // 2 sources

        {
            super.tick();
        }
        if (!this.isAlive()) {
            return;
        }
        var4_4 = BlockPos.containing(this.position());
        if ((--this.ticketTimer <= 0L || var1_1 != SectionPos.blockToSectionCoord(var4_4.getX()) || var2_2 != SectionPos.blockToSectionCoord(var4_4.getZ())) && var3_3 instanceof ServerPlayer) {
            var5_5 = (ServerPlayer)var3_3;
            this.ticketTimer = var5_5.registerAndUpdateEnderPearlTicket(this);
        }
    }

    private void playSound(Level level, Vec3 vec3) {
        level.playSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
    }

    @Override
    @Nullable
    public Entity teleport(TeleportTransition teleportTransition) {
        Entity entity = super.teleport(teleportTransition);
        if (entity != null) {
            entity.placePortalTicket(BlockPos.containing(entity.position()));
        }
        return entity;
    }

    @Override
    public boolean canTeleport(Level level, Level level2) {
        Entity entity;
        if (level.dimension() == Level.END && level2.dimension() == Level.OVERWORLD && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return super.canTeleport(level, level2) && serverPlayer.seenCredits;
        }
        return super.canTeleport(level, level2);
    }

    @Override
    protected void onInsideBlock(BlockState blockState) {
        Entity entity;
        super.onInsideBlock(blockState);
        if (blockState.is(Blocks.END_GATEWAY) && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.onInsideBlock(blockState);
        }
    }

    @Override
    public void onRemoval(Entity.RemovalReason removalReason) {
        if (removalReason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            this.deregisterFromCurrentOwner();
        }
        super.onRemoval(removalReason);
    }

    @Override
    public void onAboveBubbleColumn(boolean bl, BlockPos blockPos) {
        Entity.handleOnAboveBubbleColumn(this, bl, blockPos);
    }

    @Override
    public void onInsideBubbleColumn(boolean bl) {
        Entity.handleOnInsideBubbleColumn(this, bl);
    }
}

