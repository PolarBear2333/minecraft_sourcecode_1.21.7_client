/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.Mutable
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class PaleMossDecorator
extends TreeDecorator {
    public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("leaves_probability").forGetter(paleMossDecorator -> Float.valueOf(paleMossDecorator.leavesProbability)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("trunk_probability").forGetter(paleMossDecorator -> Float.valueOf(paleMossDecorator.trunkProbability)), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("ground_probability").forGetter(paleMossDecorator -> Float.valueOf(paleMossDecorator.groundProbability))).apply((Applicative)instance, PaleMossDecorator::new));
    private final float leavesProbability;
    private final float trunkProbability;
    private final float groundProbability;

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.PALE_MOSS;
    }

    public PaleMossDecorator(float f, float f2, float f3) {
        this.leavesProbability = f;
        this.trunkProbability = f2;
        this.groundProbability = f3;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        WorldGenLevel worldGenLevel = (WorldGenLevel)context.level();
        List<BlockPos> list = Util.shuffledCopy(context.logs(), randomSource);
        if (list.isEmpty()) {
            return;
        }
        MutableObject mutableObject = new MutableObject((Object)list.getFirst());
        list.forEach(arg_0 -> PaleMossDecorator.lambda$place$4((Mutable)mutableObject, arg_0));
        BlockPos blockPos2 = (BlockPos)mutableObject.getValue();
        if (randomSource.nextFloat() < this.groundProbability) {
            worldGenLevel.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap(registry -> registry.get(VegetationFeatures.PALE_MOSS_PATCH)).ifPresent(reference -> ((ConfiguredFeature)reference.value()).place(worldGenLevel, worldGenLevel.getLevel().getChunkSource().getGenerator(), randomSource, blockPos2.above()));
        }
        context.logs().forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextFloat() < this.trunkProbability && context.isAir(blockPos2 = blockPos.below())) {
                PaleMossDecorator.addMossHanger(blockPos2, context);
            }
        });
        context.leaves().forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextFloat() < this.leavesProbability && context.isAir(blockPos2 = blockPos.below())) {
                PaleMossDecorator.addMossHanger(blockPos2, context);
            }
        });
    }

    private static void addMossHanger(BlockPos blockPos, TreeDecorator.Context context) {
        while (context.isAir(blockPos.below()) && !((double)context.random().nextFloat() < 0.5)) {
            context.setBlock(blockPos, (BlockState)Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, false));
            blockPos = blockPos.below();
        }
        context.setBlock(blockPos, (BlockState)Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, true));
    }

    private static /* synthetic */ void lambda$place$4(Mutable mutable, BlockPos blockPos) {
        if (blockPos.getY() < ((BlockPos)mutable.getValue()).getY()) {
            mutable.setValue((Object)blockPos);
        }
    }
}

