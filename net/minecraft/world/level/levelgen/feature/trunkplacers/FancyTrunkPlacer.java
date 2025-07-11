/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class FancyTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> FancyTrunkPlacer.trunkPlacerParts(instance).apply((Applicative)instance, FancyTrunkPlacer::new));
    private static final double TRUNK_HEIGHT_SCALE = 0.618;
    private static final double CLUSTER_DENSITY_MAGIC = 1.382;
    private static final double BRANCH_SLOPE = 0.381;
    private static final double BRANCH_LENGTH_MAGIC = 0.328;

    public FancyTrunkPlacer(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FANCY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        int n2;
        int n3 = 5;
        int n4 = n + 2;
        int n5 = Mth.floor((double)n4 * 0.618);
        FancyTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos.below(), treeConfiguration);
        double d = 1.0;
        int n6 = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * (double)n4 / 13.0, 2.0)));
        int n7 = blockPos.getY() + n5;
        ArrayList arrayList = Lists.newArrayList();
        arrayList.add(new FoliageCoords(blockPos.above(n2), n7));
        for (n2 = n4 - 5; n2 >= 0; --n2) {
            float f = FancyTrunkPlacer.treeShape(n4, n2);
            if (f < 0.0f) continue;
            for (int i = 0; i < n6; ++i) {
                BlockPos blockPos2;
                double d2 = 1.0;
                double d3 = 1.0 * (double)f * ((double)randomSource.nextFloat() + 0.328);
                double d4 = (double)(randomSource.nextFloat() * 2.0f) * Math.PI;
                double d5 = d3 * Math.sin(d4) + 0.5;
                double d6 = d3 * Math.cos(d4) + 0.5;
                BlockPos blockPos3 = blockPos.offset(Mth.floor(d5), n2 - 1, Mth.floor(d6));
                if (!this.makeLimb(levelSimulatedReader, biConsumer, randomSource, blockPos3, blockPos2 = blockPos3.above(5), false, treeConfiguration)) continue;
                int n8 = blockPos.getX() - blockPos3.getX();
                int n9 = blockPos.getZ() - blockPos3.getZ();
                double d7 = (double)blockPos3.getY() - Math.sqrt(n8 * n8 + n9 * n9) * 0.381;
                int n10 = d7 > (double)n7 ? n7 : (int)d7;
                BlockPos blockPos4 = new BlockPos(blockPos.getX(), n10, blockPos.getZ());
                if (!this.makeLimb(levelSimulatedReader, biConsumer, randomSource, blockPos4, blockPos3, false, treeConfiguration)) continue;
                arrayList.add(new FoliageCoords(blockPos3, blockPos4.getY()));
            }
        }
        this.makeLimb(levelSimulatedReader, biConsumer, randomSource, blockPos, blockPos.above(n5), true, treeConfiguration);
        this.makeBranches(levelSimulatedReader, biConsumer, randomSource, n4, blockPos, arrayList, treeConfiguration);
        ArrayList arrayList2 = Lists.newArrayList();
        for (FoliageCoords foliageCoords : arrayList) {
            if (!this.trimBranches(n4, foliageCoords.getBranchBase() - blockPos.getY())) continue;
            arrayList2.add(foliageCoords.attachment);
        }
        return arrayList2;
    }

    private boolean makeLimb(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, BlockPos blockPos2, boolean bl, TreeConfiguration treeConfiguration) {
        if (!bl && Objects.equals(blockPos, blockPos2)) {
            return true;
        }
        BlockPos blockPos3 = blockPos2.offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
        int n = this.getSteps(blockPos3);
        float f = (float)blockPos3.getX() / (float)n;
        float f2 = (float)blockPos3.getY() / (float)n;
        float f3 = (float)blockPos3.getZ() / (float)n;
        for (int i = 0; i <= n; ++i) {
            BlockPos blockPos4 = blockPos.offset(Mth.floor(0.5f + (float)i * f), Mth.floor(0.5f + (float)i * f2), Mth.floor(0.5f + (float)i * f3));
            if (bl) {
                this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos4, treeConfiguration, blockState -> (BlockState)blockState.trySetValue(RotatedPillarBlock.AXIS, this.getLogAxis(blockPos, blockPos4)));
                continue;
            }
            if (this.isFree(levelSimulatedReader, blockPos4)) continue;
            return false;
        }
        return true;
    }

    private int getSteps(BlockPos blockPos) {
        int n = Mth.abs(blockPos.getX());
        int n2 = Mth.abs(blockPos.getY());
        int n3 = Mth.abs(blockPos.getZ());
        return Math.max(n, Math.max(n2, n3));
    }

    private Direction.Axis getLogAxis(BlockPos blockPos, BlockPos blockPos2) {
        int n;
        Direction.Axis axis = Direction.Axis.Y;
        int n2 = Math.abs(blockPos2.getX() - blockPos.getX());
        int n3 = Math.max(n2, n = Math.abs(blockPos2.getZ() - blockPos.getZ()));
        if (n3 > 0) {
            axis = n2 == n3 ? Direction.Axis.X : Direction.Axis.Z;
        }
        return axis;
    }

    private boolean trimBranches(int n, int n2) {
        return (double)n2 >= (double)n * 0.2;
    }

    private void makeBranches(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, List<FoliageCoords> list, TreeConfiguration treeConfiguration) {
        for (FoliageCoords foliageCoords : list) {
            int n2 = foliageCoords.getBranchBase();
            BlockPos blockPos2 = new BlockPos(blockPos.getX(), n2, blockPos.getZ());
            if (blockPos2.equals(foliageCoords.attachment.pos()) || !this.trimBranches(n, n2 - blockPos.getY())) continue;
            this.makeLimb(levelSimulatedReader, biConsumer, randomSource, blockPos2, foliageCoords.attachment.pos(), true, treeConfiguration);
        }
    }

    private static float treeShape(int n, int n2) {
        if ((float)n2 < (float)n * 0.3f) {
            return -1.0f;
        }
        float f = (float)n / 2.0f;
        float f2 = f - (float)n2;
        float f3 = Mth.sqrt(f * f - f2 * f2);
        if (f2 == 0.0f) {
            f3 = f;
        } else if (Math.abs(f2) >= f) {
            return 0.0f;
        }
        return f3 * 0.5f;
    }

    static class FoliageCoords {
        final FoliagePlacer.FoliageAttachment attachment;
        private final int branchBase;

        public FoliageCoords(BlockPos blockPos, int n) {
            this.attachment = new FoliagePlacer.FoliageAttachment(blockPos, 0, false);
            this.branchBase = n;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}

