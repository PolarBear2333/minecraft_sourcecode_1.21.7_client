/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Snowball
extends ThrowableItemProjectile {
    public Snowball(EntityType<? extends Snowball> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public Snowball(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(EntityType.SNOWBALL, livingEntity, level, itemStack);
    }

    public Snowball(Level level, double d, double d2, double d3, ItemStack itemStack) {
        super(EntityType.SNOWBALL, d, d2, d3, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    private ParticleOptions getParticle() {
        ItemStack itemStack = this.getItem();
        return itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, itemStack);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 3) {
            ParticleOptions particleOptions = this.getParticle();
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(particleOptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        int n = entity instanceof Blaze ? 3 : 0;
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), n);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}

