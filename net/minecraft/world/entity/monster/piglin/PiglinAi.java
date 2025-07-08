/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CopyMemoryWithExpiry;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DismountOrSkipMounting;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.Mount;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.monster.piglin.StartHuntingHoglin;
import net.minecraft.world.entity.monster.piglin.StopAdmiringIfItemTooFarAway;
import net.minecraft.world.entity.monster.piglin.StopAdmiringIfTiredOfTryingToReachItem;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
    public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
    public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
    public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
    private static final int PLAYER_ANGER_RANGE = 16;
    private static final int ANGER_DURATION = 600;
    private static final int ADMIRE_DURATION = 119;
    private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
    private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
    private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
    private static final int CELEBRATION_TIME = 300;
    protected static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
    private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
    private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
    private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
    private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final int EAT_COOLDOWN = 200;
    private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    private static final int MAX_LOOK_DIST = 8;
    private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
    private static final int INTERACTION_RANGE = 8;
    private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
    private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75f;
    private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1f;
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8f;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6f;

    protected static Brain<?> makeBrain(Piglin piglin, Brain<Piglin> brain) {
        PiglinAi.initCoreActivity(brain);
        PiglinAi.initIdleActivity(brain);
        PiglinAi.initAdmireItemActivity(brain);
        PiglinAi.initFightActivity(piglin, brain);
        PiglinAi.initCelebrateActivity(brain);
        PiglinAi.initRetreatActivity(brain);
        PiglinAi.initRideHoglinActivity(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    protected static void initMemories(Piglin piglin, RandomSource randomSource) {
        int n = TIME_BETWEEN_HUNTS.sample(randomSource);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, n);
    }

    private static void initCoreActivity(Brain<Piglin> brain) {
        brain.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of((Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), InteractWithDoor.create(), PiglinAi.babyAvoidNemesis(), PiglinAi.avoidZombified(), StopHoldingItemIfNoLongerAdmiring.create(), StartAdmiringItemIfSeen.create(119), StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance), StopBeingAngryIfTargetDead.create()));
    }

    private static void initIdleActivity(Brain<Piglin> brain) {
        brain.addActivity(Activity.IDLE, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0f), StartAttacking.create((serverLevel, piglin) -> piglin.isAdult(), PiglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()), PiglinAi.avoidRepellent(), PiglinAi.babySometimesRideBabyHoglin(), PiglinAi.createIdleLookBehaviors(), PiglinAi.createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(Piglin piglin, Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(StopAttackingIfTargetInvalid.create((serverLevel, livingEntity) -> !PiglinAi.isNearestValidAttackTarget(serverLevel, piglin, livingEntity)), BehaviorBuilder.triggerIf(PiglinAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75f)), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0f), MeleeAttack.create(20), new CrossbowAttack(), RememberIfHoglinWasKilled.create(), EraseMemoryIf.create(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(PiglinAi.avoidRepellent(), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0f), StartAttacking.create((serverLevel, piglin) -> piglin.isAdult(), PiglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(piglin -> !piglin.isDancing(), GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0f)), BehaviorBuilder.triggerIf(Piglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6f)), new RunOne(ImmutableList.of((Object)Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0f), (Object)1), (Object)Pair.of(RandomStroll.stroll(0.6f, 2, 1), (Object)1), (Object)Pair.of((Object)new DoNothing(10, 20), (Object)1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void initAdmireItemActivity(Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(GoToWantedItem.create(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0f, true, 9), StopAdmiringIfItemTooFarAway.create(9), StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
    }

    private static void initRetreatActivity(Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0f, 12, true), PiglinAi.createIdleLookBehaviors(), PiglinAi.createIdleMovementBehaviors(), EraseMemoryIf.create(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static void initRideHoglinActivity(Brain<Piglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(Mount.create(0.8f), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 8.0f), BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(ImmutableList.builder().addAll(PiglinAi.createLookBehaviors()).add((Object)Pair.of(BehaviorBuilder.triggerIf(piglin -> true), (Object)1)).build())), DismountOrSkipMounting.create(8, PiglinAi::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
    }

    private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of((Object)Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(8.0f), (Object)1));
    }

    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<LivingEntity>((List<Pair<BehaviorControl<LivingEntity>, Integer>>)ImmutableList.builder().addAll(PiglinAi.createLookBehaviors()).add((Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)).build());
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<Piglin>((List<Pair<BehaviorControl<Piglin>, Integer>>)ImmutableList.of((Object)Pair.of(RandomStroll.stroll(0.6f), (Object)2), (Object)Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), (Object)2), (Object)Pair.of(BehaviorBuilder.triggerIf(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6f, 3)), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)));
    }

    private static BehaviorControl<PathfinderMob> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, false);
    }

    private static BehaviorControl<Piglin> babyAvoidNemesis() {
        return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static BehaviorControl<Piglin> avoidZombified() {
        return CopyMemoryWithExpiry.create(PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
    }

    protected static void updateActivity(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.ADMIRE_ITEM, (Object)Activity.FIGHT, (Object)Activity.AVOID, (Object)Activity.CELEBRATE, (Object)Activity.RIDE, (Object)Activity.IDLE));
        Activity activity2 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity2) {
            PiglinAi.getSoundForCurrentActivity(piglin).ifPresent(piglin::makeSound);
        }
        piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && PiglinAi.isBabyRidingBaby(piglin)) {
            piglin.stopRiding();
        }
        if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
            brain.eraseMemory(MemoryModuleType.DANCING);
        }
        piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
    }

    private static boolean isBabyRidingBaby(Piglin piglin) {
        if (!piglin.isBaby()) {
            return false;
        }
        Entity entity = piglin.getVehicle();
        return entity instanceof Piglin && ((Piglin)entity).isBaby() || entity instanceof Hoglin && ((Hoglin)entity).isBaby();
    }

    protected static void pickUpItem(ServerLevel serverLevel, Piglin piglin, ItemEntity itemEntity) {
        boolean bl;
        ItemStack itemStack;
        PiglinAi.stopWalking(piglin);
        if (itemEntity.getItem().is(Items.GOLD_NUGGET)) {
            piglin.take(itemEntity, itemEntity.getItem().getCount());
            itemStack = itemEntity.getItem();
            itemEntity.discard();
        } else {
            piglin.take(itemEntity, 1);
            itemStack = PiglinAi.removeOneItemFromItemEntity(itemEntity);
        }
        if (PiglinAi.isLovedItem(itemStack)) {
            piglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            PiglinAi.holdInOffhand(serverLevel, piglin, itemStack);
            PiglinAi.admireGoldItem(piglin);
            return;
        }
        if (PiglinAi.isFood(itemStack) && !PiglinAi.hasEatenRecently(piglin)) {
            PiglinAi.eat(piglin);
            return;
        }
        boolean bl2 = bl = !piglin.equipItemIfPossible(serverLevel, itemStack).equals(ItemStack.EMPTY);
        if (bl) {
            return;
        }
        PiglinAi.putInInventory(piglin, itemStack);
    }

    private static void holdInOffhand(ServerLevel serverLevel, Piglin piglin, ItemStack itemStack) {
        if (PiglinAi.isHoldingItemInOffHand(piglin)) {
            piglin.spawnAtLocation(serverLevel, piglin.getItemInHand(InteractionHand.OFF_HAND));
        }
        piglin.holdInOffHand(itemStack);
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack itemStack2 = itemStack.split(1);
        if (itemStack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(itemStack);
        }
        return itemStack2;
    }

    protected static void stopHoldingOffHandItem(ServerLevel serverLevel, Piglin piglin, boolean bl) {
        ItemStack itemStack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (piglin.isAdult()) {
            boolean bl2 = PiglinAi.isBarterCurrency(itemStack);
            if (bl && bl2) {
                PiglinAi.throwItems(piglin, PiglinAi.getBarterResponseItems(piglin));
            } else if (!bl2) {
                boolean bl3;
                boolean bl4 = bl3 = !piglin.equipItemIfPossible(serverLevel, itemStack).isEmpty();
                if (!bl3) {
                    PiglinAi.putInInventory(piglin, itemStack);
                }
            }
        } else {
            boolean bl5;
            boolean bl6 = bl5 = !piglin.equipItemIfPossible(serverLevel, itemStack).isEmpty();
            if (!bl5) {
                ItemStack itemStack2 = piglin.getMainHandItem();
                if (PiglinAi.isLovedItem(itemStack2)) {
                    PiglinAi.putInInventory(piglin, itemStack2);
                } else {
                    PiglinAi.throwItems(piglin, Collections.singletonList(itemStack2));
                }
                piglin.holdInMainHand(itemStack);
            }
        }
    }

    protected static void cancelAdmiring(ServerLevel serverLevel, Piglin piglin) {
        if (PiglinAi.isAdmiringItem(piglin) && !piglin.getOffhandItem().isEmpty()) {
            piglin.spawnAtLocation(serverLevel, piglin.getOffhandItem());
            piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    private static void putInInventory(Piglin piglin, ItemStack itemStack) {
        ItemStack itemStack2 = piglin.addToInventory(itemStack);
        PiglinAi.throwItemsTowardRandomPos(piglin, Collections.singletonList(itemStack2));
    }

    private static void throwItems(Piglin piglin, List<ItemStack> list) {
        Optional<Player> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (optional.isPresent()) {
            PiglinAi.throwItemsTowardPlayer(piglin, optional.get(), list);
        } else {
            PiglinAi.throwItemsTowardRandomPos(piglin, list);
        }
    }

    private static void throwItemsTowardRandomPos(Piglin piglin, List<ItemStack> list) {
        PiglinAi.throwItemsTowardPos(piglin, list, PiglinAi.getRandomNearbyPos(piglin));
    }

    private static void throwItemsTowardPlayer(Piglin piglin, Player player, List<ItemStack> list) {
        PiglinAi.throwItemsTowardPos(piglin, list, player.position());
    }

    private static void throwItemsTowardPos(Piglin piglin, List<ItemStack> list, Vec3 vec3) {
        if (!list.isEmpty()) {
            piglin.swing(InteractionHand.OFF_HAND);
            for (ItemStack itemStack : list) {
                BehaviorUtils.throwItem(piglin, itemStack, vec3.add(0.0, 1.0, 0.0));
            }
        }
    }

    private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
        LootTable lootTable = piglin.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(new LootParams.Builder((ServerLevel)piglin.level()).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER));
        return objectArrayList;
    }

    private static boolean wantsToDance(LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity2.getType() != EntityType.HOGLIN) {
            return false;
        }
        return RandomSource.create(livingEntity.level().getGameTime()).nextFloat() < 0.1f;
    }

    protected static boolean wantsToPickup(Piglin piglin, ItemStack itemStack) {
        if (piglin.isBaby() && itemStack.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        }
        if (itemStack.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        }
        if (PiglinAi.isAdmiringDisabled(piglin) && piglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (PiglinAi.isBarterCurrency(itemStack)) {
            return PiglinAi.isNotHoldingLovedItemInOffHand(piglin);
        }
        boolean bl = piglin.canAddToInventory(itemStack);
        if (itemStack.is(Items.GOLD_NUGGET)) {
            return bl;
        }
        if (PiglinAi.isFood(itemStack)) {
            return !PiglinAi.hasEatenRecently(piglin) && bl;
        }
        if (PiglinAi.isLovedItem(itemStack)) {
            return PiglinAi.isNotHoldingLovedItemInOffHand(piglin) && bl;
        }
        return piglin.canReplaceCurrentItem(itemStack);
    }

    protected static boolean isLovedItem(ItemStack itemStack) {
        return itemStack.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean wantsToStopRiding(Piglin piglin, Entity entity) {
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return !mob.isBaby() || !mob.isAlive() || PiglinAi.wasHurtRecently(piglin) || PiglinAi.wasHurtRecently(mob) || mob instanceof Piglin && mob.getVehicle() == null;
        }
        return false;
    }

    private static boolean isNearestValidAttackTarget(ServerLevel serverLevel, Piglin piglin, LivingEntity livingEntity) {
        return PiglinAi.findNearestValidAttackTarget(serverLevel, piglin).filter(livingEntity2 -> livingEntity2 == livingEntity).isPresent();
    }

    private static boolean isNearZombified(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity livingEntity = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return piglin.closerThan(livingEntity, 6.0);
        }
        return false;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel serverLevel, Piglin piglin) {
        Optional<LivingEntity> optional;
        Brain<Piglin> brain = piglin.getBrain();
        if (PiglinAi.isNearZombified(piglin)) {
            return Optional.empty();
        }
        Optional<LivingEntity> optional2 = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
        if (optional2.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(serverLevel, piglin, optional2.get())) {
            return optional2;
        }
        if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER) && (optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)).isPresent()) {
            return optional;
        }
        optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        if (optional.isPresent()) {
            return optional;
        }
        Optional<Player> optional3 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
        if (optional3.isPresent() && Sensor.isEntityAttackable(serverLevel, piglin, optional3.get())) {
            return optional3;
        }
        return Optional.empty();
    }

    public static void angerNearbyPiglins(ServerLevel serverLevel, Player player, boolean bl) {
        List<Piglin> list = player.level().getEntitiesOfClass(Piglin.class, player.getBoundingBox().inflate(16.0));
        list.stream().filter(PiglinAi::isIdle).filter(piglin -> !bl || BehaviorUtils.canSee(piglin, player)).forEach(piglin -> {
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                PiglinAi.setAngerTargetToNearestTargetablePlayerIfFound(serverLevel, piglin, player);
            } else {
                PiglinAi.setAngerTarget(serverLevel, piglin, player);
            }
        });
    }

    public static InteractionResult mobInteract(ServerLevel serverLevel, Piglin piglin, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (PiglinAi.canAdmire(piglin, itemStack)) {
            ItemStack itemStack2 = itemStack.consumeAndReturn(1, player);
            PiglinAi.holdInOffhand(serverLevel, piglin, itemStack2);
            PiglinAi.admireGoldItem(piglin);
            PiglinAi.stopWalking(piglin);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected static boolean canAdmire(Piglin piglin, ItemStack itemStack) {
        return !PiglinAi.isAdmiringDisabled(piglin) && !PiglinAi.isAdmiringItem(piglin) && piglin.isAdult() && PiglinAi.isBarterCurrency(itemStack);
    }

    protected static void wasHurtBy(ServerLevel serverLevel, Piglin piglin, LivingEntity livingEntity) {
        if (livingEntity instanceof Piglin) {
            return;
        }
        if (PiglinAi.isHoldingItemInOffHand(piglin)) {
            PiglinAi.stopHoldingOffHandItem(serverLevel, piglin, false);
        }
        Brain<Piglin> brain = piglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        brain.eraseMemory(MemoryModuleType.DANCING);
        brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        if (livingEntity instanceof Player) {
            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
        }
        PiglinAi.getAvoidTarget(piglin).ifPresent(livingEntity2 -> {
            if (livingEntity2.getType() != livingEntity.getType()) {
                brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
            }
        });
        if (piglin.isBaby()) {
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, 100L);
            if (Sensor.isEntityAttackableIgnoringLineOfSight(serverLevel, piglin, livingEntity)) {
                PiglinAi.broadcastAngerTarget(serverLevel, piglin, livingEntity);
            }
            return;
        }
        if (livingEntity.getType() == EntityType.HOGLIN && PiglinAi.hoglinsOutnumberPiglins(piglin)) {
            PiglinAi.setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity);
            PiglinAi.broadcastRetreat(piglin, livingEntity);
            return;
        }
        PiglinAi.maybeRetaliate(serverLevel, piglin, livingEntity);
    }

    protected static void maybeRetaliate(ServerLevel serverLevel, AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        if (abstractPiglin.getBrain().isActive(Activity.AVOID)) {
            return;
        }
        if (!Sensor.isEntityAttackableIgnoringLineOfSight(serverLevel, abstractPiglin, livingEntity)) {
            return;
        }
        if (BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(abstractPiglin, livingEntity, 4.0)) {
            return;
        }
        if (livingEntity.getType() == EntityType.PLAYER && serverLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            PiglinAi.setAngerTargetToNearestTargetablePlayerIfFound(serverLevel, abstractPiglin, livingEntity);
            PiglinAi.broadcastUniversalAnger(serverLevel, abstractPiglin);
        } else {
            PiglinAi.setAngerTarget(serverLevel, abstractPiglin, livingEntity);
            PiglinAi.broadcastAngerTarget(serverLevel, abstractPiglin, livingEntity);
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin piglin) {
        return piglin.getBrain().getActiveNonCoreActivity().map(activity -> PiglinAi.getSoundForActivity(piglin, activity));
    }

    private static SoundEvent getSoundForActivity(Piglin piglin, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        }
        if (piglin.isConverting()) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        if (activity == Activity.AVOID && PiglinAi.isNearAvoidTarget(piglin)) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        if (activity == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        }
        if (activity == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        }
        if (PiglinAi.seesPlayerHoldingLovedItem(piglin)) {
            return SoundEvents.PIGLIN_JEALOUS;
        }
        if (PiglinAi.isNearRepellent(piglin)) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        return SoundEvents.PIGLIN_AMBIENT;
    }

    private static boolean isNearAvoidTarget(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return false;
        }
        return brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(piglin, 12.0);
    }

    protected static List<AbstractPiglin> getVisibleAdultPiglins(Piglin piglin) {
        return piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse((List<AbstractPiglin>)ImmutableList.of());
    }

    private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin abstractPiglin) {
        return abstractPiglin.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse((List<AbstractPiglin>)ImmutableList.of());
    }

    public static boolean isWearingSafeArmor(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
            if (!livingEntity.getItemBySlot(equipmentSlot).is(ItemTags.PIGLIN_SAFE_ARMOR)) continue;
            return true;
        }
        return false;
    }

    private static void stopWalking(Piglin piglin) {
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getNavigation().stop();
    }

    private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.Ticker ticker = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
        return CopyMemoryWithExpiry.create(livingEntity -> livingEntity.isBaby() && ticker.tickDownAndCheck(livingEntity.level().random), MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION);
    }

    protected static void broadcastAngerTarget(ServerLevel serverLevel, AbstractPiglin abstractPiglin2, LivingEntity livingEntity) {
        PiglinAi.getAdultPiglins(abstractPiglin2).forEach(abstractPiglin -> {
            if (!(livingEntity.getType() != EntityType.HOGLIN || abstractPiglin.canHunt() && ((Hoglin)livingEntity).canBeHunted())) {
                return;
            }
            PiglinAi.setAngerTargetIfCloserThanCurrent(serverLevel, abstractPiglin, livingEntity);
        });
    }

    protected static void broadcastUniversalAnger(ServerLevel serverLevel, AbstractPiglin abstractPiglin2) {
        PiglinAi.getAdultPiglins(abstractPiglin2).forEach(abstractPiglin -> PiglinAi.getNearestVisibleTargetablePlayer(abstractPiglin).ifPresent(player -> PiglinAi.setAngerTarget(serverLevel, abstractPiglin, player)));
    }

    protected static void setAngerTarget(ServerLevel serverLevel, AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        if (!Sensor.isEntityAttackableIgnoringLineOfSight(serverLevel, abstractPiglin, livingEntity)) {
            return;
        }
        abstractPiglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, livingEntity.getUUID(), 600L);
        if (livingEntity.getType() == EntityType.HOGLIN && abstractPiglin.canHunt()) {
            PiglinAi.dontKillAnyMoreHoglinsForAWhile(abstractPiglin);
        }
        if (livingEntity.getType() == EntityType.PLAYER && serverLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(ServerLevel serverLevel, AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        Optional<Player> optional = PiglinAi.getNearestVisibleTargetablePlayer(abstractPiglin);
        if (optional.isPresent()) {
            PiglinAi.setAngerTarget(serverLevel, abstractPiglin, optional.get());
        } else {
            PiglinAi.setAngerTarget(serverLevel, abstractPiglin, livingEntity);
        }
    }

    private static void setAngerTargetIfCloserThanCurrent(ServerLevel serverLevel, AbstractPiglin abstractPiglin, LivingEntity livingEntity) {
        Optional<LivingEntity> optional = PiglinAi.getAngerTarget(abstractPiglin);
        LivingEntity livingEntity2 = BehaviorUtils.getNearestTarget(abstractPiglin, optional, livingEntity);
        if (optional.isPresent() && optional.get() == livingEntity2) {
            return;
        }
        PiglinAi.setAngerTarget(serverLevel, abstractPiglin, livingEntity2);
    }

    private static Optional<LivingEntity> getAngerTarget(AbstractPiglin abstractPiglin) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(abstractPiglin, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoidTarget(Piglin piglin) {
        if (piglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return piglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET);
        }
        return Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin abstractPiglin) {
        if (abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)) {
            return abstractPiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
        }
        return Optional.empty();
    }

    private static void broadcastRetreat(Piglin piglin, LivingEntity livingEntity) {
        PiglinAi.getVisibleAdultPiglins(piglin).stream().filter(abstractPiglin -> abstractPiglin instanceof Piglin).forEach(abstractPiglin -> PiglinAi.retreatFromNearestTarget((Piglin)abstractPiglin, livingEntity));
    }

    private static void retreatFromNearestTarget(Piglin piglin, LivingEntity livingEntity) {
        Brain<Piglin> brain = piglin.getBrain();
        LivingEntity livingEntity2 = livingEntity;
        livingEntity2 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingEntity2);
        livingEntity2 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), livingEntity2);
        PiglinAi.setAvoidTargetAndDontHuntForAWhile(piglin, livingEntity2);
    }

    private static boolean wantsToStopFleeing(Piglin piglin) {
        Brain<Piglin> brain = piglin.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        }
        LivingEntity livingEntity = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
        EntityType<?> entityType = livingEntity.getType();
        if (entityType == EntityType.HOGLIN) {
            return PiglinAi.piglinsEqualOrOutnumberHoglins(piglin);
        }
        if (PiglinAi.isZombified(entityType)) {
            return !brain.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, livingEntity);
        }
        return false;
    }

    private static boolean piglinsEqualOrOutnumberHoglins(Piglin piglin) {
        return !PiglinAi.hoglinsOutnumberPiglins(piglin);
    }

    private static boolean hoglinsOutnumberPiglins(Piglin piglin) {
        int n = piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int n2 = piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return n2 > n;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(Piglin piglin, LivingEntity livingEntity) {
        piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingEntity, RETREAT_DURATION.sample(piglin.level().random));
        PiglinAi.dontKillAnyMoreHoglinsForAWhile(piglin);
    }

    protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin abstractPiglin) {
        abstractPiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, TIME_BETWEEN_HUNTS.sample(abstractPiglin.level().random));
    }

    private static void eat(Piglin piglin) {
        piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    private static Vec3 getRandomNearbyPos(Piglin piglin) {
        Vec3 vec3 = LandRandomPos.getPos(piglin, 4, 2);
        return vec3 == null ? piglin.position() : vec3;
    }

    private static boolean hasEatenRecently(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    protected static boolean isIdle(AbstractPiglin abstractPiglin) {
        return abstractPiglin.getBrain().isActive(Activity.IDLE);
    }

    private static boolean hasCrossbow(LivingEntity livingEntity) {
        return livingEntity.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 119L);
    }

    private static boolean isAdmiringItem(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    private static boolean isBarterCurrency(ItemStack itemStack) {
        return itemStack.is(BARTERING_ITEM);
    }

    private static boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.PIGLIN_FOOD);
    }

    private static boolean isNearRepellent(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean seesPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return !PiglinAi.seesPlayerHoldingLovedItem(livingEntity);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity livingEntity) {
        return livingEntity.getType() == EntityType.PLAYER && livingEntity.isHolding(PiglinAi::isLovedItem);
    }

    private static boolean isAdmiringDisabled(Piglin piglin) {
        return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(Piglin piglin) {
        return !piglin.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(Piglin piglin) {
        return piglin.getOffhandItem().isEmpty() || !PiglinAi.isLovedItem(piglin.getOffhandItem());
    }

    public static boolean isZombified(EntityType<?> entityType) {
        return entityType == EntityType.ZOMBIFIED_PIGLIN || entityType == EntityType.ZOGLIN;
    }
}

