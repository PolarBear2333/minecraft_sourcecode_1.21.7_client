/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownExperienceBottle
extends ThrowableItemProjectile {
    public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownExperienceBottle(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(EntityType.EXPERIENCE_BOTTLE, livingEntity, level, itemStack);
    }

    public ThrownExperienceBottle(Level level, double d, double d2, double d3, ItemStack itemStack) {
        super(EntityType.EXPERIENCE_BOTTLE, d, d2, d3, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.levelEvent(2002, this.blockPosition(), -13083194);
            int n = 3 + serverLevel.random.nextInt(5) + serverLevel.random.nextInt(5);
            if (hitResult instanceof BlockHitResult) {
                BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                Vec3 vec3 = blockHitResult.getDirection().getUnitVec3();
                ExperienceOrb.awardWithDirection(serverLevel, hitResult.getLocation(), vec3, n);
            } else {
                ExperienceOrb.awardWithDirection(serverLevel, hitResult.getLocation(), this.getDeltaMovement().scale(-1.0), n);
            }
            this.discard();
        }
    }
}

