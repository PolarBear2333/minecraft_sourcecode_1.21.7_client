/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P3
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
    public static final Codec<RootPlacer> CODEC = BuiltInRegistries.ROOT_PLACER_TYPE.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
    protected final IntProvider trunkOffsetY;
    protected final BlockStateProvider rootProvider;
    protected final Optional<AboveRootPlacement> aboveRootPlacement;

    protected static <P extends RootPlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, BlockStateProvider, Optional<AboveRootPlacement>> rootPlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group((App)IntProvider.CODEC.fieldOf("trunk_offset_y").forGetter(rootPlacer -> rootPlacer.trunkOffsetY), (App)BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(rootPlacer -> rootPlacer.rootProvider), (App)AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter(rootPlacer -> rootPlacer.aboveRootPlacement));
    }

    public RootPlacer(IntProvider intProvider, BlockStateProvider blockStateProvider, Optional<AboveRootPlacement> optional) {
        this.trunkOffsetY = intProvider;
        this.rootProvider = blockStateProvider;
        this.aboveRootPlacement = optional;
    }

    protected abstract RootPlacerType<?> type();

    public abstract boolean placeRoots(LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, RandomSource var3, BlockPos var4, BlockPos var5, TreeConfiguration var6);

    protected boolean canPlaceRoot(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.validTreePos(levelSimulatedReader, blockPos);
    }

    protected void placeRoot(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        if (!this.canPlaceRoot(levelSimulatedReader, blockPos)) {
            return;
        }
        biConsumer.accept(blockPos, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos, this.rootProvider.getState(randomSource, blockPos)));
        if (this.aboveRootPlacement.isPresent()) {
            AboveRootPlacement aboveRootPlacement = this.aboveRootPlacement.get();
            BlockPos blockPos2 = blockPos.above();
            if (randomSource.nextFloat() < aboveRootPlacement.aboveRootPlacementChance() && levelSimulatedReader.isStateAtPosition(blockPos2, BlockBehaviour.BlockStateBase::isAir)) {
                biConsumer.accept(blockPos2, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos2, aboveRootPlacement.aboveRootProvider().getState(randomSource, blockPos2)));
            }
        }
    }

    protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean bl = levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.is(FluidTags.WATER));
            return (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, bl);
        }
        return blockState;
    }

    public BlockPos getTrunkOrigin(BlockPos blockPos, RandomSource randomSource) {
        return blockPos.above(this.trunkOffsetY.sample(randomSource));
    }
}

