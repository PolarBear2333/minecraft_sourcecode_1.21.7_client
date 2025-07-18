/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnifferEggBlock
extends Block {
    public static final MapCodec<SnifferEggBlock> CODEC = SnifferEggBlock.simpleCodec(SnifferEggBlock::new);
    public static final int MAX_HATCH_LEVEL = 2;
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    private static final int REGULAR_HATCH_TIME_TICKS = 24000;
    private static final int BOOSTED_HATCH_TIME_TICKS = 12000;
    private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
    private static final VoxelShape SHAPE = Block.column(14.0, 12.0, 0.0, 16.0);

    public MapCodec<SnifferEggBlock> codec() {
        return CODEC;
    }

    public SnifferEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(HATCH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    public int getHatchLevel(BlockState blockState) {
        return blockState.getValue(HATCH);
    }

    private boolean isReadyToHatch(BlockState blockState) {
        return this.getHatchLevel(blockState) == 2;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!this.isReadyToHatch(blockState)) {
            serverLevel.playSound(null, blockPos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7f, 0.9f + randomSource.nextFloat() * 0.2f);
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(HATCH, this.getHatchLevel(blockState) + 1), 2);
            return;
        }
        serverLevel.playSound(null, blockPos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7f, 0.9f + randomSource.nextFloat() * 0.2f);
        serverLevel.destroyBlock(blockPos, false);
        Sniffer sniffer = EntityType.SNIFFER.create(serverLevel, EntitySpawnReason.BREEDING);
        if (sniffer != null) {
            Vec3 vec3 = blockPos.getCenter();
            sniffer.setBaby(true);
            sniffer.snapTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0f), 0.0f);
            serverLevel.addFreshEntity(sniffer);
        }
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        boolean bl2 = SnifferEggBlock.hatchBoost(level, blockPos);
        if (!level.isClientSide() && bl2) {
            level.levelEvent(3009, blockPos, 0);
        }
        int n = bl2 ? 12000 : 24000;
        int n2 = n / 3;
        level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(blockState));
        level.scheduleTick(blockPos, this, n2 + level.random.nextInt(300));
    }

    @Override
    public boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    public static boolean hatchBoost(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos.below()).is(BlockTags.SNIFFER_EGG_HATCH_BOOST);
    }
}

