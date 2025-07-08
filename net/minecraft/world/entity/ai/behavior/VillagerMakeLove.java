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
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class VillagerMakeLove
extends Behavior<Villager> {
    private long birthTimestamp;

    public VillagerMakeLove() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 350, 350);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        return this.isBreedingPossible(villager);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return l <= this.birthTimestamp && this.isBreedingPossible(villager);
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        AgeableMob ageableMob = villager.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther(villager, ageableMob, 0.5f, 2);
        serverLevel.broadcastEntityEvent(ageableMob, (byte)18);
        serverLevel.broadcastEntityEvent(villager, (byte)18);
        int n = 275 + villager.getRandom().nextInt(50);
        this.birthTimestamp = l + (long)n;
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        Villager villager2 = (Villager)villager.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        if (villager.distanceToSqr(villager2) > 5.0) {
            return;
        }
        BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager2, 0.5f, 2);
        if (l >= this.birthTimestamp) {
            villager.eatAndDigestFood();
            villager2.eatAndDigestFood();
            this.tryToGiveBirth(serverLevel, villager, villager2);
        } else if (villager.getRandom().nextInt(35) == 0) {
            serverLevel.broadcastEntityEvent(villager2, (byte)12);
            serverLevel.broadcastEntityEvent(villager, (byte)12);
        }
    }

    private void tryToGiveBirth(ServerLevel serverLevel, Villager villager, Villager villager2) {
        Optional<BlockPos> optional = this.takeVacantBed(serverLevel, villager);
        if (optional.isEmpty()) {
            serverLevel.broadcastEntityEvent(villager2, (byte)13);
            serverLevel.broadcastEntityEvent(villager, (byte)13);
        } else {
            Optional<Villager> optional2 = this.breed(serverLevel, villager, villager2);
            if (optional2.isPresent()) {
                this.giveBedToChild(serverLevel, optional2.get(), optional.get());
            } else {
                serverLevel.getPoiManager().release(optional.get());
                DebugPackets.sendPoiTicketCountPacket(serverLevel, optional.get());
            }
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        villager.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
    }

    private boolean isBreedingPossible(Villager villager) {
        Brain<Villager> brain = villager.getBrain();
        Optional<AgeableMob> optional = brain.getMemory(MemoryModuleType.BREED_TARGET).filter(ageableMob -> ageableMob.getType() == EntityType.VILLAGER);
        if (optional.isEmpty()) {
            return false;
        }
        return BehaviorUtils.targetIsValid(brain, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && villager.canBreed() && optional.get().canBreed();
    }

    private Optional<BlockPos> takeVacantBed(ServerLevel serverLevel, Villager villager) {
        return serverLevel.getPoiManager().take(holder -> holder.is(PoiTypes.HOME), (holder, blockPos) -> this.canReach(villager, (BlockPos)blockPos, (Holder<PoiType>)holder), villager.blockPosition(), 48);
    }

    private boolean canReach(Villager villager, BlockPos blockPos, Holder<PoiType> holder) {
        Path path = villager.getNavigation().createPath(blockPos, holder.value().validRange());
        return path != null && path.canReach();
    }

    private Optional<Villager> breed(ServerLevel serverLevel, Villager villager, Villager villager2) {
        Villager villager3 = villager.getBreedOffspring(serverLevel, villager2);
        if (villager3 == null) {
            return Optional.empty();
        }
        villager.setAge(6000);
        villager2.setAge(6000);
        villager3.setAge(-24000);
        villager3.snapTo(villager.getX(), villager.getY(), villager.getZ(), 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(villager3);
        serverLevel.broadcastEntityEvent(villager3, (byte)12);
        return Optional.of(villager3);
    }

    private void giveBedToChild(ServerLevel serverLevel, Villager villager, BlockPos blockPos) {
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), blockPos);
        villager.getBrain().setMemory(MemoryModuleType.HOME, globalPos);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

