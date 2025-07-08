/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 */
package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractThrownPotion
extends ThrowableItemProjectile {
    public static final double SPLASH_RANGE = 4.0;
    protected static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = livingEntity -> livingEntity.isSensitiveToWater() || livingEntity.isOnFire();

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> entityType, Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(entityType, livingEntity, level, itemStack);
    }

    public AbstractThrownPotion(EntityType<? extends AbstractThrownPotion> entityType, Level level, double d, double d2, double d3, ItemStack itemStack) {
        super(entityType, d, d2, d3, level, itemStack);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level().isClientSide) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockPos blockPos2 = blockPos.relative(direction);
        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potionContents.is(Potions.WATER)) {
            this.dowseFire(blockPos2);
            this.dowseFire(blockPos2.relative(direction.getOpposite()));
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                this.dowseFire(blockPos2.relative(direction2));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        Object object = this.level();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = this.getItem();
        PotionContents potionContents = object.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (potionContents.is(Potions.WATER)) {
            this.onHitAsWater(serverLevel);
        } else if (potionContents.hasEffects()) {
            this.onHitAsPotion(serverLevel, (ItemStack)object, hitResult);
        }
        int n = potionContents.potion().isPresent() && potionContents.potion().get().value().hasInstantEffects() ? 2007 : 2002;
        serverLevel.levelEvent(n, this.blockPosition(), potionContents.getColor());
        this.discard();
    }

    private void onHitAsWater(ServerLevel serverLevel) {
        AABB aABB = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aABB, WATER_SENSITIVE_OR_ON_FIRE);
        for (LivingEntity object2 : list) {
            double axolotl = this.distanceToSqr(object2);
            if (!(axolotl < 16.0)) continue;
            if (object2.isSensitiveToWater()) {
                object2.hurtServer(serverLevel, this.damageSources().indirectMagic(this, this.getOwner()), 1.0f);
            }
            if (!object2.isOnFire() || !object2.isAlive()) continue;
            object2.extinguishFire();
        }
        List<Axolotl> list2 = this.level().getEntitiesOfClass(Axolotl.class, aABB);
        Iterator iterator = list2.iterator();
        while (iterator.hasNext()) {
            Axolotl axolotl = (Axolotl)iterator.next();
            axolotl.rehydrate();
        }
    }

    protected abstract void onHitAsPotion(ServerLevel var1, ItemStack var2, HitResult var3);

    private void dowseFire(BlockPos blockPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.is(BlockTags.FIRE)) {
            this.level().destroyBlock(blockPos, false, this);
        } else if (AbstractCandleBlock.isLit(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, this.level(), blockPos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.level().levelEvent(null, 1009, blockPos, 0);
            CampfireBlock.dowse(this.getOwner(), this.level(), blockPos, blockState);
            this.level().setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(CampfireBlock.LIT, false));
        }
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
        double d = livingEntity.position().x - this.position().x;
        double d2 = livingEntity.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)d2);
    }
}

