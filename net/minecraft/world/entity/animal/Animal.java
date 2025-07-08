/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class Animal
extends AgeableMob {
    protected static final int PARENT_AGE_AFTER_BREEDING = 6000;
    private static final int DEFAULT_IN_LOVE_TIME = 0;
    private int inLove = 0;
    @Nullable
    private EntityReference<ServerPlayer> loveCause;

    protected Animal(EntityType<? extends Animal> entityType, Level level) {
        super((EntityType<? extends AgeableMob>)entityType, level);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
    }

    public static AttributeSupplier.Builder createAnimalAttributes() {
        return Mob.createMobAttributes().add(Attributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        if (this.getAge() != 0) {
            this.inLove = 0;
        }
        super.customServerAiStep(serverLevel);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getAge() != 0) {
            this.inLove = 0;
        }
        if (this.inLove > 0) {
            --this.inLove;
            if (this.inLove % 10 == 0) {
                double d = this.random.nextGaussian() * 0.02;
                double d2 = this.random.nextGaussian() * 0.02;
                double d3 = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, d2, d3);
            }
        }
    }

    @Override
    protected void actuallyHurt(ServerLevel serverLevel, DamageSource damageSource, float f) {
        this.resetLove();
        super.actuallyHurt(serverLevel, damageSource, f);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK)) {
            return 10.0f;
        }
        return levelReader.getPathfindingCostFromLightLevels(blockPos);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("InLove", this.inLove);
        EntityReference.store(this.loveCause, valueOutput, "LoveCause");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.inLove = valueInput.getIntOr("InLove", 0);
        this.loveCause = EntityReference.read(valueInput, "LoveCause");
    }

    public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        boolean bl = EntitySpawnReason.ignoresLightRequirements(entitySpawnReason) || Animal.isBrightEnoughToSpawn(levelAccessor, blockPos);
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && bl;
    }

    protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        return blockAndTintGetter.getRawBrightness(blockPos, 0) > 8;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel serverLevel) {
        return 1 + this.random.nextInt(3);
    }

    public abstract boolean isFood(ItemStack var1);

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (this.isFood(itemStack)) {
            int n = this.getAge();
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                if (n == 0 && this.canFallInLove()) {
                    this.usePlayerItem(player, interactionHand, itemStack);
                    this.setInLove(serverPlayer);
                    this.playEatingSound();
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
            if (this.isBaby()) {
                this.usePlayerItem(player, interactionHand, itemStack);
                this.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-n), true);
                this.playEatingSound();
                return InteractionResult.SUCCESS;
            }
            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }
        }
        return super.mobInteract(player, interactionHand);
    }

    protected void playEatingSound() {
    }

    protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
        int n = itemStack.getCount();
        UseRemainder useRemainder = itemStack.get(DataComponents.USE_REMAINDER);
        itemStack.consume(1, player);
        if (useRemainder != null) {
            ItemStack itemStack2 = useRemainder.convertIntoRemainder(itemStack, n, player.hasInfiniteMaterials(), player::handleExtraItemsCreatedOnUse);
            player.setItemInHand(interactionHand, itemStack2);
        }
    }

    public boolean canFallInLove() {
        return this.inLove <= 0;
    }

    public void setInLove(@Nullable Player player) {
        this.inLove = 600;
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            this.loveCause = new EntityReference<ServerPlayer>(serverPlayer);
        }
        this.level().broadcastEntityEvent(this, (byte)18);
    }

    public void setInLoveTime(int n) {
        this.inLove = n;
    }

    public int getInLoveTime() {
        return this.inLove;
    }

    @Nullable
    public ServerPlayer getLoveCause() {
        return EntityReference.get(this.loveCause, this.level()::getPlayerByUUID, ServerPlayer.class);
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public void resetLove() {
        this.inLove = 0;
    }

    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (animal.getClass() != this.getClass()) {
            return false;
        }
        return this.isInLove() && animal.isInLove();
    }

    public void spawnChildFromBreeding(ServerLevel serverLevel, Animal animal) {
        AgeableMob ageableMob = this.getBreedOffspring(serverLevel, animal);
        if (ageableMob == null) {
            return;
        }
        ageableMob.setBaby(true);
        ageableMob.snapTo(this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f);
        this.finalizeSpawnChildFromBreeding(serverLevel, animal, ageableMob);
        serverLevel.addFreshEntityWithPassengers(ageableMob);
    }

    public void finalizeSpawnChildFromBreeding(ServerLevel serverLevel, Animal animal, @Nullable AgeableMob ageableMob) {
        Optional.ofNullable(this.getLoveCause()).or(() -> Optional.ofNullable(animal.getLoveCause())).ifPresent(serverPlayer -> {
            serverPlayer.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger((ServerPlayer)serverPlayer, this, animal, ageableMob);
        });
        this.setAge(6000);
        animal.setAge(6000);
        this.resetLove();
        animal.resetLove();
        serverLevel.broadcastEntityEvent(this, (byte)18);
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 18) {
            for (int i = 0; i < 7; ++i) {
                double d = this.random.nextGaussian() * 0.02;
                double d2 = this.random.nextGaussian() * 0.02;
                double d3 = this.random.nextGaussian() * 0.02;
                this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, d2, d3);
            }
        } else {
            super.handleEntityEvent(by);
        }
    }
}

