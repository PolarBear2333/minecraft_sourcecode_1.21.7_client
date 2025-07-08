/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.kinds.OptionalBox$Mu
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.OptionalBox;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class InteractWithDoor {
    private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
    private static final double SKIP_CLOSING_DOOR_IF_FURTHER_AWAY_THAN = 3.0;
    private static final double MAX_DISTANCE_TO_HOLD_DOOR_OPEN_FOR_OTHER_MOBS = 2.0;

    public static BehaviorControl<LivingEntity> create() {
        MutableObject mutableObject = new MutableObject(null);
        MutableInt mutableInt = new MutableInt(0);
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.PATH), instance.registered(MemoryModuleType.DOORS_TO_CLOSE), instance.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            DoorBlock doorBlock;
            BlockState blockState;
            Object object;
            Path path = (Path)instance.get(memoryAccessor);
            Optional<Set<GlobalPos>> optional = instance.tryGet(memoryAccessor2);
            if (path.notStarted() || path.isDone()) {
                return false;
            }
            if (Objects.equals(mutableObject.getValue(), path.getNextNode())) {
                mutableInt.setValue(20);
            } else if (mutableInt.decrementAndGet() > 0) {
                return false;
            }
            mutableObject.setValue((Object)path.getNextNode());
            Node node = path.getPreviousNode();
            Node node2 = path.getNextNode();
            BlockPos blockPos = node.asBlockPos();
            BlockState blockState2 = serverLevel.getBlockState(blockPos);
            if (blockState2.is(BlockTags.MOB_INTERACTABLE_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
                object = (DoorBlock)blockState2.getBlock();
                if (!((DoorBlock)object).isOpen(blockState2)) {
                    ((DoorBlock)object).setOpen(livingEntity, serverLevel, blockState2, blockPos, true);
                }
                optional = InteractWithDoor.rememberDoorToClose(memoryAccessor2, optional, serverLevel, blockPos);
            }
            if ((blockState = serverLevel.getBlockState((BlockPos)(object = node2.asBlockPos()))).is(BlockTags.MOB_INTERACTABLE_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock) && !(doorBlock = (DoorBlock)blockState.getBlock()).isOpen(blockState)) {
                doorBlock.setOpen(livingEntity, serverLevel, blockState, (BlockPos)object, true);
                optional = InteractWithDoor.rememberDoorToClose(memoryAccessor2, optional, serverLevel, (BlockPos)object);
            }
            optional.ifPresent(set -> InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough(serverLevel, livingEntity, node, node2, set, instance.tryGet(memoryAccessor3)));
            return true;
        }));
    }

    public static void closeDoorsThatIHaveOpenedOrPassedThrough(ServerLevel serverLevel, LivingEntity livingEntity, @Nullable Node node, @Nullable Node node2, Set<GlobalPos> set, Optional<List<LivingEntity>> optional) {
        Iterator<GlobalPos> iterator = set.iterator();
        while (iterator.hasNext()) {
            GlobalPos globalPos = iterator.next();
            BlockPos blockPos = globalPos.pos();
            if (node != null && node.asBlockPos().equals(blockPos) || node2 != null && node2.asBlockPos().equals(blockPos)) continue;
            if (InteractWithDoor.isDoorTooFarAway(serverLevel, livingEntity, globalPos)) {
                iterator.remove();
                continue;
            }
            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (!blockState.is(BlockTags.MOB_INTERACTABLE_DOORS, blockStateBase -> blockStateBase.getBlock() instanceof DoorBlock)) {
                iterator.remove();
                continue;
            }
            DoorBlock doorBlock = (DoorBlock)blockState.getBlock();
            if (!doorBlock.isOpen(blockState)) {
                iterator.remove();
                continue;
            }
            if (InteractWithDoor.areOtherMobsComingThroughDoor(livingEntity, blockPos, optional)) {
                iterator.remove();
                continue;
            }
            doorBlock.setOpen(livingEntity, serverLevel, blockState, blockPos, false);
            iterator.remove();
        }
    }

    private static boolean areOtherMobsComingThroughDoor(LivingEntity livingEntity3, BlockPos blockPos, Optional<List<LivingEntity>> optional) {
        if (optional.isEmpty()) {
            return false;
        }
        return optional.get().stream().filter(livingEntity2 -> livingEntity2.getType() == livingEntity3.getType()).filter(livingEntity -> blockPos.closerToCenterThan(livingEntity.position(), 2.0)).anyMatch(livingEntity -> InteractWithDoor.isMobComingThroughDoor(livingEntity.getBrain(), blockPos));
    }

    private static boolean isMobComingThroughDoor(Brain<?> brain, BlockPos blockPos) {
        if (!brain.hasMemoryValue(MemoryModuleType.PATH)) {
            return false;
        }
        Path path = brain.getMemory(MemoryModuleType.PATH).get();
        if (path.isDone()) {
            return false;
        }
        Node node = path.getPreviousNode();
        if (node == null) {
            return false;
        }
        Node node2 = path.getNextNode();
        return blockPos.equals(node.asBlockPos()) || blockPos.equals(node2.asBlockPos());
    }

    private static boolean isDoorTooFarAway(ServerLevel serverLevel, LivingEntity livingEntity, GlobalPos globalPos) {
        return globalPos.dimension() != serverLevel.dimension() || !globalPos.pos().closerToCenterThan(livingEntity.position(), 3.0);
    }

    private static Optional<Set<GlobalPos>> rememberDoorToClose(MemoryAccessor<OptionalBox.Mu, Set<GlobalPos>> memoryAccessor, Optional<Set<GlobalPos>> optional, ServerLevel serverLevel, BlockPos blockPos) {
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), blockPos);
        return Optional.of(optional.map(set -> {
            set.add(globalPos);
            return set;
        }).orElseGet(() -> {
            HashSet hashSet = Sets.newHashSet((Object[])new GlobalPos[]{globalPos});
            memoryAccessor.set(hashSet);
            return hashSet;
        }));
    }
}

