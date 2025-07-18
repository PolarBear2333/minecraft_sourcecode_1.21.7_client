/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.world.entity.npc;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class WanderingTrader
extends AbstractVillager
implements Consumable.OverrideConsumeSound {
    private static final int DEFAULT_DESPAWN_DELAY = 0;
    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay = 0;

    public WanderingTrader(EntityType<? extends WanderingTrader> entityType, Level level) {
        super((EntityType<? extends AbstractVillager>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new UseItemGoal<WanderingTrader>(this, PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, wanderingTrader -> this.level().isDarkOutside() && !wanderingTrader.isInvisible()));
        this.goalSelector.addGoal(0, new UseItemGoal<WanderingTrader>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, wanderingTrader -> this.level().isBrightOutside() && wanderingTrader.isInvisible()));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zombie>(this, Zombie.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Evoker>(this, Evoker.class, 12.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vindicator>(this, Vindicator.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Vex>(this, Vex.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Pillager>(this, Pillager.class, 15.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Illusioner>(this, Illusioner.class, 12.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<Zoglin>(this, Zoglin.class, 10.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderToPositionGoal(this, 2.0, 0.35));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (interactionHand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!this.level().isClientSide) {
                if (this.getOffers().isEmpty()) {
                    return InteractionResult.CONSUME;
                }
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    protected void updateTrades() {
        MerchantOffers merchantOffers = this.getOffers();
        for (Pair<VillagerTrades.ItemListing[], Integer> pair : VillagerTrades.WANDERING_TRADER_TRADES) {
            VillagerTrades.ItemListing[] itemListingArray = (VillagerTrades.ItemListing[])pair.getLeft();
            this.addOffersFromItemListings(merchantOffers, itemListingArray, (Integer)pair.getRight());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("DespawnDelay", this.despawnDelay);
        valueOutput.storeNullable("wander_target", BlockPos.CODEC, this.wanderTarget);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.despawnDelay = valueInput.getIntOr("DespawnDelay", 0);
        this.wanderTarget = valueInput.read("wander_target", BlockPos.CODEC).orElse(null);
        this.setAge(Math.max(0, this.getAge()));
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer merchantOffer) {
        if (merchantOffer.shouldRewardExp()) {
            int n = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), n));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isTrading()) {
            return SoundEvents.WANDERING_TRADER_TRADE;
        }
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    public SoundEvent getConsumeSound(ItemStack itemStack) {
        if (itemStack.is(Items.MILK_BUCKET)) {
            return SoundEvents.WANDERING_TRADER_DRINK_MILK;
        }
        return SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean bl) {
        return bl ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int n) {
        this.despawnDelay = n;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.discard();
        }
    }

    public void setWanderTarget(@Nullable BlockPos blockPos) {
        this.wanderTarget = blockPos;
    }

    @Nullable
    BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    class WanderToPositionGoal
    extends Goal {
        final WanderingTrader trader;
        final double stopDistance;
        final double speedModifier;

        WanderToPositionGoal(WanderingTrader wanderingTrader2, double d, double d2) {
            this.trader = wanderingTrader2;
            this.stopDistance = d;
            this.speedModifier = d2;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void stop() {
            this.trader.setWanderTarget(null);
            WanderingTrader.this.navigation.stop();
        }

        @Override
        public boolean canUse() {
            BlockPos blockPos = this.trader.getWanderTarget();
            return blockPos != null && this.isTooFarAway(blockPos, this.stopDistance);
        }

        @Override
        public void tick() {
            BlockPos blockPos = this.trader.getWanderTarget();
            if (blockPos != null && WanderingTrader.this.navigation.isDone()) {
                if (this.isTooFarAway(blockPos, 10.0)) {
                    Vec3 vec3 = new Vec3((double)blockPos.getX() - this.trader.getX(), (double)blockPos.getY() - this.trader.getY(), (double)blockPos.getZ() - this.trader.getZ()).normalize();
                    Vec3 vec32 = vec3.scale(10.0).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    WanderingTrader.this.navigation.moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
                } else {
                    WanderingTrader.this.navigation.moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.speedModifier);
                }
            }
        }

        private boolean isTooFarAway(BlockPos blockPos, double d) {
            return !blockPos.closerToCenterThan(this.trader.position(), d);
        }
    }
}

