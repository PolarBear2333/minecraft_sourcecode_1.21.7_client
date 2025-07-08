/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class ZombieHorse
extends AbstractHorse {
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIE_HORSE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.ZOMBIE_HORSE.getHeight() - 0.03125f, 0.0f)).scale(0.5f);

    public ZombieHorse(EntityType<? extends ZombieHorse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZombieHorse.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2f);
    }

    public static boolean checkZombieHorseSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        if (EntitySpawnReason.isSpawner(entitySpawnReason)) {
            return EntitySpawnReason.ignoresLightRequirements(entitySpawnReason) || ZombieHorse.isBrightEnoughToSpawn(levelAccessor, blockPos);
        }
        return Animal.checkAnimalSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(ZombieHorse.generateJumpStrength(randomSource::nextDouble));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.ZOMBIE_HORSE.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (!this.isTamed()) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    protected void addBehaviourGoals() {
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }
}

