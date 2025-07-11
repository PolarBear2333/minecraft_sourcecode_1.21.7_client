/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class IcebergFeature
extends Feature<BlockStateConfiguration> {
    public IcebergFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> featurePlaceContext) {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        blockPos = new BlockPos(blockPos.getX(), featurePlaceContext.chunkGenerator().getSeaLevel(), blockPos.getZ());
        RandomSource randomSource = featurePlaceContext.random();
        boolean bl = randomSource.nextDouble() > 0.7;
        BlockState blockState = featurePlaceContext.config().state;
        double d = randomSource.nextDouble() * 2.0 * Math.PI;
        int n6 = 11 - randomSource.nextInt(5);
        int n7 = 3 + randomSource.nextInt(3);
        boolean bl2 = randomSource.nextDouble() > 0.7;
        int n8 = 11;
        int n9 = n5 = bl2 ? randomSource.nextInt(6) + 6 : randomSource.nextInt(15) + 3;
        if (!bl2 && randomSource.nextDouble() > 0.9) {
            n5 += randomSource.nextInt(19) + 7;
        }
        int n10 = Math.min(n5 + randomSource.nextInt(11), 18);
        int n11 = Math.min(n5 + randomSource.nextInt(7) - randomSource.nextInt(5), 11);
        int n12 = bl2 ? n6 : 11;
        for (n4 = -n12; n4 < n12; ++n4) {
            for (n3 = -n12; n3 < n12; ++n3) {
                for (n2 = 0; n2 < n5; ++n2) {
                    int n13 = n = bl2 ? this.heightDependentRadiusEllipse(n2, n5, n11) : this.heightDependentRadiusRound(randomSource, n2, n5, n11);
                    if (!bl2 && n4 >= n) continue;
                    this.generateIcebergBlock(worldGenLevel, randomSource, blockPos, n5, n4, n2, n3, n, n12, bl2, n7, d, bl, blockState);
                }
            }
        }
        this.smooth(worldGenLevel, blockPos, n11, n5, bl2, n6);
        for (n4 = -n12; n4 < n12; ++n4) {
            for (n3 = -n12; n3 < n12; ++n3) {
                for (n2 = -1; n2 > -n10; --n2) {
                    n = bl2 ? Mth.ceil((float)n12 * (1.0f - (float)Math.pow(n2, 2.0) / ((float)n10 * 8.0f))) : n12;
                    int n14 = this.heightDependentRadiusSteep(randomSource, -n2, n10, n11);
                    if (n4 >= n14) continue;
                    this.generateIcebergBlock(worldGenLevel, randomSource, blockPos, n10, n4, n2, n3, n14, n, bl2, n7, d, bl, blockState);
                }
            }
        }
        int n15 = bl2 ? (randomSource.nextDouble() > 0.1 ? 1 : 0) : (n4 = randomSource.nextDouble() > 0.7 ? 1 : 0);
        if (n4 != 0) {
            this.generateCutOut(randomSource, worldGenLevel, n11, n5, blockPos, bl2, n6, d, n7);
        }
        return true;
    }

    private void generateCutOut(RandomSource randomSource, LevelAccessor levelAccessor, int n, int n2, BlockPos blockPos, boolean bl, int n3, double d, int n4) {
        int n5;
        int n6;
        int n7 = randomSource.nextBoolean() ? -1 : 1;
        int n8 = randomSource.nextBoolean() ? -1 : 1;
        int n9 = randomSource.nextInt(Math.max(n / 2 - 2, 1));
        if (randomSource.nextBoolean()) {
            n9 = n / 2 + 1 - randomSource.nextInt(Math.max(n - n / 2 - 1, 1));
        }
        int n10 = randomSource.nextInt(Math.max(n / 2 - 2, 1));
        if (randomSource.nextBoolean()) {
            n10 = n / 2 + 1 - randomSource.nextInt(Math.max(n - n / 2 - 1, 1));
        }
        if (bl) {
            n9 = n10 = randomSource.nextInt(Math.max(n3 - 5, 1));
        }
        BlockPos blockPos2 = new BlockPos(n7 * n9, 0, n8 * n10);
        double d2 = bl ? d + 1.5707963267948966 : randomSource.nextDouble() * 2.0 * Math.PI;
        for (n6 = 0; n6 < n2 - 3; ++n6) {
            n5 = this.heightDependentRadiusRound(randomSource, n6, n2, n);
            this.carve(n5, n6, blockPos, levelAccessor, false, d2, blockPos2, n3, n4);
        }
        for (n6 = -1; n6 > -n2 + randomSource.nextInt(5); --n6) {
            n5 = this.heightDependentRadiusSteep(randomSource, -n6, n2, n);
            this.carve(n5, n6, blockPos, levelAccessor, true, d2, blockPos2, n3, n4);
        }
    }

    private void carve(int n, int n2, BlockPos blockPos, LevelAccessor levelAccessor, boolean bl, double d, BlockPos blockPos2, int n3, int n4) {
        int n5 = n + 1 + n3 / 3;
        int n6 = Math.min(n - 3, 3) + n4 / 2 - 1;
        for (int i = -n5; i < n5; ++i) {
            for (int j = -n5; j < n5; ++j) {
                BlockPos blockPos3;
                BlockState blockState;
                double d2 = this.signedDistanceEllipse(i, j, blockPos2, n5, n6, d);
                if (!(d2 < 0.0) || !IcebergFeature.isIcebergState(blockState = levelAccessor.getBlockState(blockPos3 = blockPos.offset(i, n2, j))) && !blockState.is(Blocks.SNOW_BLOCK)) continue;
                if (bl) {
                    this.setBlock(levelAccessor, blockPos3, Blocks.WATER.defaultBlockState());
                    continue;
                }
                this.setBlock(levelAccessor, blockPos3, Blocks.AIR.defaultBlockState());
                this.removeFloatingSnowLayer(levelAccessor, blockPos3);
            }
        }
    }

    private void removeFloatingSnowLayer(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (levelAccessor.getBlockState(blockPos.above()).is(Blocks.SNOW)) {
            this.setBlock(levelAccessor, blockPos.above(), Blocks.AIR.defaultBlockState());
        }
    }

    private void generateIcebergBlock(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, int n, int n2, int n3, int n4, int n5, int n6, boolean bl, int n7, double d, boolean bl2, BlockState blockState) {
        double d2;
        double d3 = d2 = bl ? this.signedDistanceEllipse(n2, n4, BlockPos.ZERO, n6, this.getEllipseC(n3, n, n7), d) : this.signedDistanceCircle(n2, n4, BlockPos.ZERO, n5, randomSource);
        if (d2 < 0.0) {
            double d4;
            BlockPos blockPos2 = blockPos.offset(n2, n3, n4);
            double d5 = d4 = bl ? -0.5 : (double)(-6 - randomSource.nextInt(3));
            if (d2 > d4 && randomSource.nextDouble() > 0.9) {
                return;
            }
            this.setIcebergBlock(blockPos2, levelAccessor, randomSource, n - n3, n, bl, bl2, blockState);
        }
    }

    private void setIcebergBlock(BlockPos blockPos, LevelAccessor levelAccessor, RandomSource randomSource, int n, int n2, boolean bl, boolean bl2, BlockState blockState) {
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if (blockState2.isAir() || blockState2.is(Blocks.SNOW_BLOCK) || blockState2.is(Blocks.ICE) || blockState2.is(Blocks.WATER)) {
            int n3;
            boolean bl3 = !bl || randomSource.nextDouble() > 0.05;
            int n4 = n3 = bl ? 3 : 2;
            if (bl2 && !blockState2.is(Blocks.WATER) && (double)n <= (double)randomSource.nextInt(Math.max(1, n2 / n3)) + (double)n2 * 0.6 && bl3) {
                this.setBlock(levelAccessor, blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
            } else {
                this.setBlock(levelAccessor, blockPos, blockState);
            }
        }
    }

    private int getEllipseC(int n, int n2, int n3) {
        int n4 = n3;
        if (n > 0 && n2 - n <= 3) {
            n4 -= 4 - (n2 - n);
        }
        return n4;
    }

    private double signedDistanceCircle(int n, int n2, BlockPos blockPos, int n3, RandomSource randomSource) {
        float f = 10.0f * Mth.clamp(randomSource.nextFloat(), 0.2f, 0.8f) / (float)n3;
        return (double)f + Math.pow(n - blockPos.getX(), 2.0) + Math.pow(n2 - blockPos.getZ(), 2.0) - Math.pow(n3, 2.0);
    }

    private double signedDistanceEllipse(int n, int n2, BlockPos blockPos, int n3, int n4, double d) {
        return Math.pow(((double)(n - blockPos.getX()) * Math.cos(d) - (double)(n2 - blockPos.getZ()) * Math.sin(d)) / (double)n3, 2.0) + Math.pow(((double)(n - blockPos.getX()) * Math.sin(d) + (double)(n2 - blockPos.getZ()) * Math.cos(d)) / (double)n4, 2.0) - 1.0;
    }

    private int heightDependentRadiusRound(RandomSource randomSource, int n, int n2, int n3) {
        float f = 3.5f - randomSource.nextFloat();
        float f2 = (1.0f - (float)Math.pow(n, 2.0) / ((float)n2 * f)) * (float)n3;
        if (n2 > 15 + randomSource.nextInt(5)) {
            int n4 = n < 3 + randomSource.nextInt(6) ? n / 2 : n;
            f2 = (1.0f - (float)n4 / ((float)n2 * f * 0.4f)) * (float)n3;
        }
        return Mth.ceil(f2 / 2.0f);
    }

    private int heightDependentRadiusEllipse(int n, int n2, int n3) {
        float f = 1.0f;
        float f2 = (1.0f - (float)Math.pow(n, 2.0) / ((float)n2 * 1.0f)) * (float)n3;
        return Mth.ceil(f2 / 2.0f);
    }

    private int heightDependentRadiusSteep(RandomSource randomSource, int n, int n2, int n3) {
        float f = 1.0f + randomSource.nextFloat() / 2.0f;
        float f2 = (1.0f - (float)n / ((float)n2 * f)) * (float)n3;
        return Mth.ceil(f2 / 2.0f);
    }

    private static boolean isIcebergState(BlockState blockState) {
        return blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.BLUE_ICE);
    }

    private boolean belowIsAir(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos.below()).isAir();
    }

    private void smooth(LevelAccessor levelAccessor, BlockPos blockPos, int n, int n2, boolean bl, int n3) {
        int n4 = bl ? n3 : n / 2;
        for (int i = -n4; i <= n4; ++i) {
            for (int j = -n4; j <= n4; ++j) {
                for (int k = 0; k <= n2; ++k) {
                    BlockPos blockPos2 = blockPos.offset(i, k, j);
                    BlockState blockState = levelAccessor.getBlockState(blockPos2);
                    if (!IcebergFeature.isIcebergState(blockState) && !blockState.is(Blocks.SNOW)) continue;
                    if (this.belowIsAir(levelAccessor, blockPos2)) {
                        this.setBlock(levelAccessor, blockPos2, Blocks.AIR.defaultBlockState());
                        this.setBlock(levelAccessor, blockPos2.above(), Blocks.AIR.defaultBlockState());
                        continue;
                    }
                    if (!IcebergFeature.isIcebergState(blockState)) continue;
                    BlockState[] blockStateArray = new BlockState[]{levelAccessor.getBlockState(blockPos2.west()), levelAccessor.getBlockState(blockPos2.east()), levelAccessor.getBlockState(blockPos2.north()), levelAccessor.getBlockState(blockPos2.south())};
                    int n5 = 0;
                    for (BlockState blockState2 : blockStateArray) {
                        if (IcebergFeature.isIcebergState(blockState2)) continue;
                        ++n5;
                    }
                    if (n5 < 3) continue;
                    this.setBlock(levelAccessor, blockPos2, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}

