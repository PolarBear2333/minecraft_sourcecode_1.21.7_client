/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove
extends Behavior<Animal> {
    private static final int BREED_RANGE = 3;
    private static final int MIN_DURATION = 60;
    private static final int MAX_DURATION = 110;
    private final EntityType<? extends Animal> partnerType;
    private final float speedModifier;
    private final int closeEnoughDistance;
    private static final int DEFAULT_CLOSE_ENOUGH_DISTANCE = 2;
    private long spawnChildAtTime;

    public AnimalMakeLove(EntityType<? extends Animal> entityType) {
        this(entityType, 1.0f, 2);
    }

    public AnimalMakeLove(EntityType<? extends Animal> entityType, float f, int n) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 110);
        this.partnerType = entityType;
        this.speedModifier = f;
        this.closeEnoughDistance = n;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Animal animal) {
        return animal.isInLove() && this.findValidBreedPartner(animal).isPresent();
    }

    @Override
    protected void start(ServerLevel serverLevel, Animal animal, long l) {
        Animal animal2 = this.findValidBreedPartner(animal).get();
        animal.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal2);
        animal2.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal);
        BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal2, this.speedModifier, this.closeEnoughDistance);
        int n = 60 + animal.getRandom().nextInt(50);
        this.spawnChildAtTime = l + (long)n;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Animal animal, long l) {
        if (!this.hasBreedTargetOfRightType(animal)) {
            return false;
        }
        Animal animal2 = this.getBreedTarget(animal);
        return animal2.isAlive() && animal.canMate(animal2) && BehaviorUtils.entityIsVisible(animal.getBrain(), animal2) && l <= this.spawnChildAtTime && !animal.isPanicking() && !animal2.isPanicking();
    }

    @Override
    protected void tick(ServerLevel serverLevel, Animal animal, long l) {
        Animal animal2 = this.getBreedTarget(animal);
        BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal2, this.speedModifier, this.closeEnoughDistance);
        if (!animal.closerThan(animal2, 3.0)) {
            return;
        }
        if (l >= this.spawnChildAtTime) {
            animal.spawnChildFromBreeding(serverLevel, animal2);
            animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            animal2.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Animal animal, long l) {
        animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        animal.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        animal.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.spawnChildAtTime = 0L;
    }

    private Animal getBreedTarget(Animal animal) {
        return (Animal)animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
    }

    private boolean hasBreedTargetOfRightType(Animal animal) {
        Brain<AgeableMob> brain = animal.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) && brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
    }

    private Optional<? extends Animal> findValidBreedPartner(Animal animal) {
        return animal.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest(livingEntity -> {
            Animal animal2;
            return livingEntity.getType() == this.partnerType && livingEntity instanceof Animal && animal.canMate(animal2 = (Animal)livingEntity) && !animal2.isPanicking();
        }).map(Animal.class::cast);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Animal)livingEntity, l);
    }
}

