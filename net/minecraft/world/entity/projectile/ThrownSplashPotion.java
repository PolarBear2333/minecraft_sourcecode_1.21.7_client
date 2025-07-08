/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class ThrownSplashPotion
extends AbstractThrownPotion {
    public ThrownSplashPotion(EntityType<? extends ThrownSplashPotion> entityType, Level level) {
        super((EntityType<? extends AbstractThrownPotion>)entityType, level);
    }

    public ThrownSplashPotion(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(EntityType.SPLASH_POTION, level, livingEntity, itemStack);
    }

    public ThrownSplashPotion(Level level, double d, double d2, double d3, ItemStack itemStack) {
        super(EntityType.SPLASH_POTION, level, d, d2, d3, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    public void onHitAsPotion(ServerLevel serverLevel, ItemStack itemStack, HitResult hitResult) {
        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        float f = itemStack.getOrDefault(DataComponents.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue();
        Iterable<MobEffectInstance> iterable = potionContents.getAllEffects();
        AABB aABB = this.getBoundingBox().move(hitResult.getLocation().subtract(this.position()));
        AABB aABB2 = aABB.inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aABB2);
        float f2 = ProjectileUtil.computeMargin(this);
        if (!list.isEmpty()) {
            Entity entity = this.getEffectSource();
            for (LivingEntity livingEntity : list) {
                double d;
                if (!livingEntity.isAffectedByPotions() || !((d = aABB.distanceToSqr(livingEntity.getBoundingBox().inflate(f2))) < 16.0)) continue;
                double d2 = 1.0 - Math.sqrt(d) / 4.0;
                for (MobEffectInstance mobEffectInstance : iterable) {
                    Holder<MobEffect> holder = mobEffectInstance.getEffect();
                    if (holder.value().isInstantenous()) {
                        holder.value().applyInstantenousEffect(serverLevel, this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), d2);
                        continue;
                    }
                    int n2 = mobEffectInstance.mapDuration(n -> (int)(d2 * (double)n * (double)f + 0.5));
                    MobEffectInstance mobEffectInstance2 = new MobEffectInstance(holder, n2, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible());
                    if (mobEffectInstance2.endsWithin(20)) continue;
                    livingEntity.addEffect(mobEffectInstance2, entity);
                }
            }
        }
    }
}

