/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock
extends Block {
    public static final MapCodec<TargetBlock> CODEC = TargetBlock.simpleCodec(TargetBlock::new);
    private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;
    private static final int ACTIVATION_TICKS_ARROWS = 20;
    private static final int ACTIVATION_TICKS_OTHER = 8;

    public MapCodec<TargetBlock> codec() {
        return CODEC;
    }

    public TargetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(OUTPUT_POWER, 0));
    }

    @Override
    protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        int n = TargetBlock.updateRedstoneOutput(level, blockState, blockHitResult, projectile);
        Entity entity = projectile.getOwner();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.awardStat(Stats.TARGET_HIT);
            CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, projectile, blockHitResult.getLocation(), n);
        }
    }

    private static int updateRedstoneOutput(LevelAccessor levelAccessor, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
        int n;
        int n2 = TargetBlock.getRedstoneStrength(blockHitResult, blockHitResult.getLocation());
        int n3 = n = entity instanceof AbstractArrow ? 20 : 8;
        if (!levelAccessor.getBlockTicks().hasScheduledTick(blockHitResult.getBlockPos(), blockState.getBlock())) {
            TargetBlock.setOutputPower(levelAccessor, blockState, n2, blockHitResult.getBlockPos(), n);
        }
        return n2;
    }

    private static int getRedstoneStrength(BlockHitResult blockHitResult, Vec3 vec3) {
        Direction direction = blockHitResult.getDirection();
        double d = Math.abs(Mth.frac(vec3.x) - 0.5);
        double d2 = Math.abs(Mth.frac(vec3.y) - 0.5);
        double d3 = Math.abs(Mth.frac(vec3.z) - 0.5);
        Direction.Axis axis = direction.getAxis();
        double d4 = axis == Direction.Axis.Y ? Math.max(d, d3) : (axis == Direction.Axis.Z ? Math.max(d, d2) : Math.max(d2, d3));
        return Math.max(1, Mth.ceil(15.0 * Mth.clamp((0.5 - d4) / 0.5, 0.0, 1.0)));
    }

    private static void setOutputPower(LevelAccessor levelAccessor, BlockState blockState, int n, BlockPos blockPos, int n2) {
        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(OUTPUT_POWER, n), 3);
        levelAccessor.scheduleTick(blockPos, blockState.getBlock(), n2);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(OUTPUT_POWER) != 0) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(OUTPUT_POWER, 0), 3);
        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(OUTPUT_POWER);
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OUTPUT_POWER);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (level.isClientSide() || blockState.is(blockState2.getBlock())) {
            return;
        }
        if (blockState.getValue(OUTPUT_POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(OUTPUT_POWER, 0), 18);
        }
    }
}

