/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class MobBucketItem
extends BucketItem {
    private final EntityType<? extends Mob> type;
    private final SoundEvent emptySound;

    public MobBucketItem(EntityType<? extends Mob> entityType, Fluid fluid, SoundEvent soundEvent, Item.Properties properties) {
        super(fluid, properties);
        this.type = entityType;
        this.emptySound = soundEvent;
    }

    @Override
    public void checkExtraContent(@Nullable LivingEntity livingEntity, Level level, ItemStack itemStack, BlockPos blockPos) {
        if (level instanceof ServerLevel) {
            this.spawn((ServerLevel)level, itemStack, blockPos);
            level.gameEvent((Entity)livingEntity, GameEvent.ENTITY_PLACE, blockPos);
        }
    }

    @Override
    protected void playEmptySound(@Nullable LivingEntity livingEntity, LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.playSound(livingEntity, blockPos, this.emptySound, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    private void spawn(ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        Mob mob = this.type.create(serverLevel, EntityType.createDefaultStackConfig(serverLevel, itemStack, null), blockPos, EntitySpawnReason.BUCKET, true, false);
        if (mob instanceof Bucketable) {
            Bucketable bucketable = (Bucketable)((Object)mob);
            CustomData customData = itemStack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            bucketable.loadFromBucketTag(customData.copyTag());
            bucketable.setFromBucket(true);
        }
        if (mob != null) {
            serverLevel.addFreshEntityWithPassengers(mob);
            mob.playAmbientSound();
        }
    }
}

