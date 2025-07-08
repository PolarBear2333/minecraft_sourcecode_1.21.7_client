/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

public class TryLaySpawnOnWaterNearLand {
    public static BehaviorControl<LivingEntity> create(Block block) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.present(MemoryModuleType.WALK_TARGET), instance.present(MemoryModuleType.IS_PREGNANT)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            if (livingEntity.isInWater() || !livingEntity.onGround()) {
                return false;
            }
            BlockPos blockPos = livingEntity.blockPosition().below();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos2;
                BlockPos blockPos3 = blockPos.relative(direction);
                if (!serverLevel.getBlockState(blockPos3).getCollisionShape(serverLevel, blockPos3).getFaceShape(Direction.UP).isEmpty() || !serverLevel.getFluidState(blockPos3).is(Fluids.WATER) || !serverLevel.getBlockState(blockPos2 = blockPos3.above()).isAir()) continue;
                BlockState blockState = block.defaultBlockState();
                serverLevel.setBlock(blockPos2, blockState, 3);
                serverLevel.gameEvent(GameEvent.BLOCK_PLACE, blockPos2, GameEvent.Context.of(livingEntity, blockState));
                serverLevel.playSound(null, livingEntity, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
                memoryAccessor3.erase();
                return true;
            }
            return true;
        }));
    }
}

