/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class BlockIgnoreProcessor
extends StructureProcessor {
    public static final MapCodec<BlockIgnoreProcessor> CODEC = BlockState.CODEC.xmap(BlockBehaviour.BlockStateBase::getBlock, Block::defaultBlockState).listOf().fieldOf("blocks").xmap(BlockIgnoreProcessor::new, blockIgnoreProcessor -> blockIgnoreProcessor.toIgnore);
    public static final BlockIgnoreProcessor STRUCTURE_BLOCK = new BlockIgnoreProcessor((List<Block>)ImmutableList.of((Object)Blocks.STRUCTURE_BLOCK));
    public static final BlockIgnoreProcessor AIR = new BlockIgnoreProcessor((List<Block>)ImmutableList.of((Object)Blocks.AIR));
    public static final BlockIgnoreProcessor STRUCTURE_AND_AIR = new BlockIgnoreProcessor((List<Block>)ImmutableList.of((Object)Blocks.AIR, (Object)Blocks.STRUCTURE_BLOCK));
    private final ImmutableList<Block> toIgnore;

    public BlockIgnoreProcessor(List<Block> list) {
        this.toIgnore = ImmutableList.copyOf(list);
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        if (this.toIgnore.contains((Object)structureBlockInfo2.state().getBlock())) {
            return null;
        }
        return structureBlockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_IGNORE;
    }
}

