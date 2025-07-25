/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MushroomBlock
extends VegetationBlock
implements BonemealableBlock {
    public static final MapCodec<MushroomBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(mushroomBlock -> mushroomBlock.feature), MushroomBlock.propertiesCodec()).apply((Applicative)instance, MushroomBlock::new));
    private static final VoxelShape SHAPE = Block.column(6.0, 0.0, 6.0);
    private final ResourceKey<ConfiguredFeature<?, ?>> feature;

    public MapCodec<MushroomBlock> codec() {
        return CODEC;
    }

    public MushroomBlock(ResourceKey<ConfiguredFeature<?, ?>> resourceKey, BlockBehaviour.Properties properties) {
        super(properties);
        this.feature = resourceKey;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos object, RandomSource randomSource) {
        if (randomSource.nextInt(25) == 0) {
            int n = 5;
            int n2 = 4;
            for (BlockPos blockPos : BlockPos.betweenClosed(((BlockPos)object).offset(-4, -1, -4), ((BlockPos)object).offset(4, 1, 4))) {
                if (!serverLevel.getBlockState(blockPos).is(this) || --n > 0) continue;
                return;
            }
            Object object2 = ((BlockPos)object).offset(randomSource.nextInt(3) - 1, randomSource.nextInt(2) - randomSource.nextInt(2), randomSource.nextInt(3) - 1);
            for (int i = 0; i < 4; ++i) {
                if (serverLevel.isEmptyBlock((BlockPos)object2) && blockState.canSurvive(serverLevel, (BlockPos)object2)) {
                    object = object2;
                }
                object2 = ((BlockPos)object).offset(randomSource.nextInt(3) - 1, randomSource.nextInt(2) - randomSource.nextInt(2), randomSource.nextInt(3) - 1);
            }
            if (serverLevel.isEmptyBlock((BlockPos)object2) && blockState.canSurvive(serverLevel, (BlockPos)object2)) {
                serverLevel.setBlock((BlockPos)object2, blockState, 2);
            }
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.isSolidRender();
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        if (blockState2.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return true;
        }
        return levelReader.getRawBrightness(blockPos, 0) < 13 && this.mayPlaceOn(blockState2, levelReader, blockPos2);
    }

    public boolean growMushroom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
        Optional optional = serverLevel.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(this.feature);
        if (optional.isEmpty()) {
            return false;
        }
        serverLevel.removeBlock(blockPos, false);
        if (((ConfiguredFeature)((Holder)optional.get()).value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos)) {
            return true;
        }
        serverLevel.setBlock(blockPos, blockState, 3);
        return false;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return (double)randomSource.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        this.growMushroom(serverLevel, blockPos, blockState, randomSource);
    }
}

