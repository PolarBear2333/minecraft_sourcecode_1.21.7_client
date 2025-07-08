/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseFireBlock
extends Block {
    private static final int SECONDS_ON_FIRE = 8;
    private static final int MIN_FIRE_TICKS_TO_ADD = 1;
    private static final int MAX_FIRE_TICKS_TO_ADD = 3;
    private final float fireDamage;
    protected static final VoxelShape SHAPE = Block.column(16.0, 0.0, 1.0);

    public BaseFireBlock(BlockBehaviour.Properties properties, float f) {
        super(properties);
        this.fireDamage = f;
    }

    protected abstract MapCodec<? extends BaseFireBlock> codec();

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return BaseFireBlock.getState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    public static BlockState getState(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (SoulFireBlock.canSurviveOnBlock(blockState)) {
            return Blocks.SOUL_FIRE.defaultBlockState();
        }
        return ((FireBlock)Blocks.FIRE).getStateForPlacement(blockGetter, blockPos);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        block12: {
            double d;
            double d2;
            double d3;
            int n;
            block11: {
                BlockPos blockPos2;
                BlockState blockState2;
                if (randomSource.nextInt(24) == 0) {
                    level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0f + randomSource.nextFloat(), randomSource.nextFloat() * 0.7f + 0.3f, false);
                }
                if (!this.canBurn(blockState2 = level.getBlockState(blockPos2 = blockPos.below())) && !blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) break block11;
                for (int i = 0; i < 3; ++i) {
                    double d4 = (double)blockPos.getX() + randomSource.nextDouble();
                    double d5 = (double)blockPos.getY() + randomSource.nextDouble() * 0.5 + 0.5;
                    double d6 = (double)blockPos.getZ() + randomSource.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d4, d5, d6, 0.0, 0.0, 0.0);
                }
                break block12;
            }
            if (this.canBurn(level.getBlockState(blockPos.west()))) {
                for (n = 0; n < 2; ++n) {
                    d3 = (double)blockPos.getX() + randomSource.nextDouble() * (double)0.1f;
                    d2 = (double)blockPos.getY() + randomSource.nextDouble();
                    d = (double)blockPos.getZ() + randomSource.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d3, d2, d, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.east()))) {
                for (n = 0; n < 2; ++n) {
                    d3 = (double)(blockPos.getX() + 1) - randomSource.nextDouble() * (double)0.1f;
                    d2 = (double)blockPos.getY() + randomSource.nextDouble();
                    d = (double)blockPos.getZ() + randomSource.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d3, d2, d, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.north()))) {
                for (n = 0; n < 2; ++n) {
                    d3 = (double)blockPos.getX() + randomSource.nextDouble();
                    d2 = (double)blockPos.getY() + randomSource.nextDouble();
                    d = (double)blockPos.getZ() + randomSource.nextDouble() * (double)0.1f;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d3, d2, d, 0.0, 0.0, 0.0);
                }
            }
            if (this.canBurn(level.getBlockState(blockPos.south()))) {
                for (n = 0; n < 2; ++n) {
                    d3 = (double)blockPos.getX() + randomSource.nextDouble();
                    d2 = (double)blockPos.getY() + randomSource.nextDouble();
                    d = (double)(blockPos.getZ() + 1) - randomSource.nextDouble() * (double)0.1f;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d3, d2, d, 0.0, 0.0, 0.0);
                }
            }
            if (!this.canBurn(level.getBlockState(blockPos.above()))) break block12;
            for (n = 0; n < 2; ++n) {
                d3 = (double)blockPos.getX() + randomSource.nextDouble();
                d2 = (double)(blockPos.getY() + 1) - randomSource.nextDouble() * (double)0.1f;
                d = (double)blockPos.getZ() + randomSource.nextDouble();
                level.addParticle(ParticleTypes.LARGE_SMOKE, d3, d2, d, 0.0, 0.0, 0.0);
            }
        }
    }

    protected abstract boolean canBurn(BlockState var1);

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity2, InsideBlockEffectApplier insideBlockEffectApplier) {
        insideBlockEffectApplier.apply(InsideBlockEffectType.FIRE_IGNITE);
        insideBlockEffectApplier.runAfter(InsideBlockEffectType.FIRE_IGNITE, entity -> entity.hurt(entity.level().damageSources().inFire(), this.fireDamage));
    }

    public static void fireIgnite(Entity entity) {
        if (!entity.fireImmune()) {
            if (entity.getRemainingFireTicks() < 0) {
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            } else if (entity instanceof ServerPlayer) {
                int n = entity.level().getRandom().nextInt(1, 3);
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + n);
            }
            if (entity.getRemainingFireTicks() >= 0) {
                entity.igniteForSeconds(8.0f);
            }
        }
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        Optional<PortalShape> optional;
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (BaseFireBlock.inPortalDimension(level) && (optional = PortalShape.findEmptyPortalShape(level, blockPos, Direction.Axis.X)).isPresent()) {
            optional.get().createPortalBlocks(level);
            return;
        }
        if (!blockState.canSurvive(level, blockPos)) {
            level.removeBlock(blockPos, false);
        }
    }

    private static boolean inPortalDimension(Level level) {
        return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide()) {
            level.levelEvent(null, 1009, blockPos, 0);
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    public static boolean canBePlacedAt(Level level, BlockPos blockPos, Direction direction) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.isAir()) {
            return false;
        }
        return BaseFireBlock.getState(level, blockPos).canSurvive(level, blockPos) || BaseFireBlock.isPortal(level, blockPos, direction);
    }

    private static boolean isPortal(Level level, BlockPos blockPos, Direction direction) {
        if (!BaseFireBlock.inPortalDimension(level)) {
            return false;
        }
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        boolean bl = false;
        for (Direction direction2 : Direction.values()) {
            if (!level.getBlockState(mutableBlockPos.set(blockPos).move(direction2)).is(Blocks.OBSIDIAN)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        Direction.Axis axis = direction.getAxis().isHorizontal() ? direction.getCounterClockWise().getAxis() : Direction.Plane.HORIZONTAL.getRandomAxis(level.random);
        return PortalShape.findEmptyPortalShape(level, blockPos, axis).isPresent();
    }
}

