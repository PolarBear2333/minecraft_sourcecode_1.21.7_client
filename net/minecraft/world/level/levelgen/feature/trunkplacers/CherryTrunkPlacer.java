/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class CherryTrunkPlacer
extends TrunkPlacer {
    private static final Codec<UniformInt> BRANCH_START_CODEC = UniformInt.CODEC.codec().validate(uniformInt -> {
        if (uniformInt.getMaxValue() - uniformInt.getMinValue() < 1) {
            return DataResult.error(() -> "Need at least 2 blocks variation for the branch starts to fit both branches");
        }
        return DataResult.success((Object)uniformInt);
    });
    public static final MapCodec<CherryTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> CherryTrunkPlacer.trunkPlacerParts(instance).and(instance.group((App)IntProvider.codec(1, 3).fieldOf("branch_count").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchCount), (App)IntProvider.codec(2, 16).fieldOf("branch_horizontal_length").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchHorizontalLength), (App)IntProvider.validateCodec(-16, 0, BRANCH_START_CODEC).fieldOf("branch_start_offset_from_top").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchStartOffsetFromTop), (App)IntProvider.codec(-16, 16).fieldOf("branch_end_offset_from_top").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchEndOffsetFromTop))).apply((Applicative)instance, CherryTrunkPlacer::new));
    private final IntProvider branchCount;
    private final IntProvider branchHorizontalLength;
    private final UniformInt branchStartOffsetFromTop;
    private final UniformInt secondBranchStartOffsetFromTop;
    private final IntProvider branchEndOffsetFromTop;

    public CherryTrunkPlacer(int n, int n2, int n3, IntProvider intProvider, IntProvider intProvider2, UniformInt uniformInt, IntProvider intProvider3) {
        super(n, n2, n3);
        this.branchCount = intProvider;
        this.branchHorizontalLength = intProvider2;
        this.branchStartOffsetFromTop = uniformInt;
        this.secondBranchStartOffsetFromTop = UniformInt.of(uniformInt.getMinValue(), uniformInt.getMaxValue() - 1);
        this.branchEndOffsetFromTop = intProvider3;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.CHERRY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        boolean bl;
        int n2;
        CherryTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos.below(), treeConfiguration);
        int n3 = Math.max(0, n - 1 + this.branchStartOffsetFromTop.sample(randomSource));
        int n4 = Math.max(0, n - 1 + this.secondBranchStartOffsetFromTop.sample(randomSource));
        if (n4 >= n3) {
            ++n4;
        }
        boolean bl2 = (n2 = this.branchCount.sample(randomSource)) == 3;
        boolean bl3 = bl = n2 >= 2;
        int n5 = bl2 ? n : (bl ? Math.max(n3, n4) + 1 : n3 + 1);
        for (int i = 0; i < n5; ++i) {
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos.above(i), treeConfiguration);
        }
        ArrayList<FoliagePlacer.FoliageAttachment> arrayList = new ArrayList<FoliagePlacer.FoliageAttachment>();
        if (bl2) {
            arrayList.add(new FoliagePlacer.FoliageAttachment(blockPos.above(n5), 0, false));
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        Function<BlockState, BlockState> function = blockState -> (BlockState)blockState.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis());
        arrayList.add(this.generateBranch(levelSimulatedReader, biConsumer, randomSource, n, blockPos, treeConfiguration, function, direction, n3, n3 < n5 - 1, mutableBlockPos));
        if (bl) {
            arrayList.add(this.generateBranch(levelSimulatedReader, biConsumer, randomSource, n, blockPos, treeConfiguration, function, direction.getOpposite(), n4, n4 < n5 - 1, mutableBlockPos));
        }
        return arrayList;
    }

    private FoliagePlacer.FoliageAttachment generateBranch(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int n, BlockPos blockPos, TreeConfiguration treeConfiguration, Function<BlockState, BlockState> function, Direction direction, int n2, boolean bl, BlockPos.MutableBlockPos mutableBlockPos) {
        int n3;
        Direction direction2;
        mutableBlockPos.set(blockPos).move(Direction.UP, n2);
        int n4 = n - 1 + this.branchEndOffsetFromTop.sample(randomSource);
        boolean bl2 = bl || n4 < n2;
        int n5 = this.branchHorizontalLength.sample(randomSource) + (bl2 ? 1 : 0);
        BlockPos blockPos2 = blockPos.relative(direction, n5).above(n4);
        int n6 = bl2 ? 2 : 1;
        for (int i = 0; i < n6; ++i) {
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.move(direction), treeConfiguration, function);
        }
        Direction direction3 = direction2 = blockPos2.getY() > mutableBlockPos.getY() ? Direction.UP : Direction.DOWN;
        while ((n3 = mutableBlockPos.distManhattan(blockPos2)) != 0) {
            float f = (float)Math.abs(blockPos2.getY() - mutableBlockPos.getY()) / (float)n3;
            boolean bl3 = randomSource.nextFloat() < f;
            mutableBlockPos.move(bl3 ? direction2 : direction);
            this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, bl3 ? Function.identity() : function);
        }
        return new FoliagePlacer.FoliageAttachment(blockPos2.above(), 0, false);
    }
}

