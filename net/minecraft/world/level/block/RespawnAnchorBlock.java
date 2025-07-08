/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RespawnAnchorBlock
extends Block {
    public static final MapCodec<RespawnAnchorBlock> CODEC = RespawnAnchorBlock.simpleCodec(RespawnAnchorBlock::new);
    public static final int MIN_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of((Object)new Vec3i(0, 0, -1), (Object)new Vec3i(-1, 0, 0), (Object)new Vec3i(0, 0, 1), (Object)new Vec3i(1, 0, 0), (Object)new Vec3i(-1, 0, -1), (Object)new Vec3i(1, 0, -1), (Object)new Vec3i(-1, 0, 1), (Object)new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = new ImmutableList.Builder().addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator()).add((Object)new Vec3i(0, 1, 0)).build();

    public MapCodec<RespawnAnchorBlock> codec() {
        return CODEC;
    }

    public RespawnAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(CHARGE, 0));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (RespawnAnchorBlock.isRespawnFuel(itemStack) && RespawnAnchorBlock.canBeCharged(blockState)) {
            RespawnAnchorBlock.charge(player, level, blockPos, blockState);
            itemStack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        if (interactionHand == InteractionHand.MAIN_HAND && RespawnAnchorBlock.isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND)) && RespawnAnchorBlock.canBeCharged(blockState)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (blockState.getValue(CHARGE) == 0) {
            return InteractionResult.PASS;
        }
        if (RespawnAnchorBlock.canSetSpawn(level)) {
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();
                ServerPlayer.RespawnConfig respawnConfig2 = new ServerPlayer.RespawnConfig(level.dimension(), blockPos, 0.0f, false);
                if (respawnConfig == null || !respawnConfig.isSamePosition(respawnConfig2)) {
                    serverPlayer.setRespawnPosition(respawnConfig2, true);
                    level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            this.explode(blockState, level, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isRespawnFuel(ItemStack itemStack) {
        return itemStack.is(Items.GLOWSTONE);
    }

    private static boolean canBeCharged(BlockState blockState) {
        return blockState.getValue(CHARGE) < 4;
    }

    private static boolean isWaterThatWouldFlow(BlockPos blockPos, Level level) {
        FluidState fluidState = level.getFluidState(blockPos);
        if (!fluidState.is(FluidTags.WATER)) {
            return false;
        }
        if (fluidState.isSource()) {
            return true;
        }
        float f = fluidState.getAmount();
        if (f < 2.0f) {
            return false;
        }
        FluidState fluidState2 = level.getFluidState(blockPos.below());
        return !fluidState2.is(FluidTags.WATER);
    }

    private void explode(BlockState blockState, Level level, final BlockPos blockPos2) {
        level.removeBlock(blockPos2, false);
        boolean bl = Direction.Plane.HORIZONTAL.stream().map(blockPos2::relative).anyMatch(blockPos -> RespawnAnchorBlock.isWaterThatWouldFlow(blockPos, level));
        final boolean bl2 = bl || level.getFluidState(blockPos2.above()).is(FluidTags.WATER);
        ExplosionDamageCalculator explosionDamageCalculator = new ExplosionDamageCalculator(this){

            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
                if (blockPos.equals(blockPos2) && bl2) {
                    return Optional.of(Float.valueOf(Blocks.WATER.getExplosionResistance()));
                }
                return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
            }
        };
        Vec3 vec3 = blockPos2.getCenter();
        level.explode(null, level.damageSources().badRespawnPointExplosion(vec3), explosionDamageCalculator, vec3, 5.0f, true, Level.ExplosionInteraction.BLOCK);
    }

    public static boolean canSetSpawn(Level level) {
        return level.dimensionType().respawnAnchorWorks();
    }

    public static void charge(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2 = (BlockState)blockState.setValue(CHARGE, blockState.getValue(CHARGE) + 1);
        level.setBlock(blockPos, blockState2, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
        level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(CHARGE) == 0) {
            return;
        }
        if (randomSource.nextInt(100) == 0) {
            level.playLocalSound(blockPos, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        double d = (double)blockPos.getX() + 0.5 + (0.5 - randomSource.nextDouble());
        double d2 = (double)blockPos.getY() + 1.0;
        double d3 = (double)blockPos.getZ() + 0.5 + (0.5 - randomSource.nextDouble());
        double d4 = (double)randomSource.nextFloat() * 0.04;
        level.addParticle(ParticleTypes.REVERSE_PORTAL, d, d2, d3, 0.0, d4, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    public static int getScaledChargeLevel(BlockState blockState, int n) {
        return Mth.floor((float)(blockState.getValue(CHARGE) - 0) / 4.0f * (float)n);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return RespawnAnchorBlock.getScaledChargeLevel(blockState, 15);
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos) {
        Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(entityType, collisionGetter, blockPos, true);
        if (optional.isPresent()) {
            return optional;
        }
        return RespawnAnchorBlock.findStandUpPosition(entityType, collisionGetter, blockPos, false);
    }

    private static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean bl) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Vec3i vec3i : RESPAWN_OFFSETS) {
            mutableBlockPos.set(blockPos).move(vec3i);
            Vec3 vec3 = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, bl);
            if (vec3 == null) continue;
            return Optional.of(vec3);
        }
        return Optional.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

