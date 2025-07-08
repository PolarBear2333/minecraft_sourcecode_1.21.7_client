/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GrassBlock
extends SpreadingSnowyDirtBlock
implements BonemealableBlock {
    public static final MapCodec<GrassBlock> CODEC = GrassBlock.simpleCodec(GrassBlock::new);

    public MapCodec<GrassBlock> codec() {
        return CODEC;
    }

    public GrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return levelReader.getBlockState(blockPos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState2 = Blocks.SHORT_GRASS.defaultBlockState();
        Optional optional = serverLevel.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(VegetationPlacements.GRASS_BONEMEAL);
        block0: for (int i = 0; i < 128; ++i) {
            Holder<PlacedFeature> holder;
            BlockPos blockPos3 = blockPos2;
            for (int j = 0; j < i / 16; ++j) {
                if (!serverLevel.getBlockState((blockPos3 = blockPos3.offset(randomSource.nextInt(3) - 1, (randomSource.nextInt(3) - 1) * randomSource.nextInt(3) / 2, randomSource.nextInt(3) - 1)).below()).is(this) || serverLevel.getBlockState(blockPos3).isCollisionShapeFullBlock(serverLevel, blockPos3)) continue block0;
            }
            BlockState blockState3 = serverLevel.getBlockState(blockPos3);
            if (blockState3.is(blockState2.getBlock()) && randomSource.nextInt(10) == 0 && (holder = (BonemealableBlock)((Object)blockState2.getBlock())).isValidBonemealTarget(serverLevel, blockPos3, blockState3)) {
                holder.performBonemeal(serverLevel, randomSource, blockPos3, blockState3);
            }
            if (!blockState3.isAir()) continue;
            if (randomSource.nextInt(8) == 0) {
                List<ConfiguredFeature<?, ?>> list = serverLevel.getBiome(blockPos3).value().getGenerationSettings().getFlowerFeatures();
                if (list.isEmpty()) continue;
                int n = randomSource.nextInt(list.size());
                holder = ((RandomPatchConfiguration)list.get(n).config()).feature();
            } else {
                if (!optional.isPresent()) continue;
                holder = (Holder)optional.get();
            }
            ((PlacedFeature)holder.value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos3);
        }
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}

