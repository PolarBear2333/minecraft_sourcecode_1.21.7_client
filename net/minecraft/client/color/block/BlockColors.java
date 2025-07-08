/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

public class BlockColors {
    private static final int DEFAULT = -1;
    public static final int LILY_PAD_IN_WORLD = -14647248;
    public static final int LILY_PAD_DEFAULT = -9321636;
    private final IdMapper<BlockColor> blockColors = new IdMapper(32);
    private final Map<Block, Set<Property<?>>> coloringStates = Maps.newHashMap();

    public static BlockColors createDefault() {
        BlockColors blockColors = new BlockColors();
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return GrassColor.getDefaultColor();
            }
            return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? blockPos.below() : blockPos);
        }, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        blockColors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return GrassColor.getDefaultColor();
            }
            return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos);
        }, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.SHORT_GRASS, Blocks.POTTED_FERN, Blocks.BUSH);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (n != 0) {
                if (blockAndTintGetter == null || blockPos == null) {
                    return GrassColor.getDefaultColor();
                }
                return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos);
            }
            return -1;
        }, Blocks.PINK_PETALS, Blocks.WILDFLOWERS);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> -10380959, Blocks.SPRUCE_LEAVES);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> -8345771, Blocks.BIRCH_LEAVES);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -12012264;
            }
            return BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos);
        }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE, Blocks.MANGROVE_LEAVES);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -10732494;
            }
            return BiomeColors.getAverageDryFoliageColor(blockAndTintGetter, blockPos);
        }, Blocks.LEAF_LITTER);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -1;
            }
            return BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> RedStoneWireBlock.getColorForPower(blockState.getValue(RedStoneWireBlock.POWER)), Blocks.REDSTONE_WIRE);
        blockColors.addColoringState(RedStoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -1;
            }
            return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos);
        }, Blocks.SUGAR_CANE);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> -2046180, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            int n2 = blockState.getValue(StemBlock.AGE);
            return ARGB.color(n2 * 32, 255 - n2 * 8, n2 * 4);
        }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        blockColors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        blockColors.register((blockState, blockAndTintGetter, blockPos, n) -> {
            if (blockAndTintGetter == null || blockPos == null) {
                return -9321636;
            }
            return -14647248;
        }, Blocks.LILY_PAD);
        return blockColors;
    }

    public int getColor(BlockState blockState, Level level, BlockPos blockPos) {
        BlockColor blockColor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(blockState.getBlock()));
        if (blockColor != null) {
            return blockColor.getColor(blockState, null, null, 0);
        }
        MapColor mapColor = blockState.getMapColor(level, blockPos);
        return mapColor != null ? mapColor.col : -1;
    }

    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int n) {
        BlockColor blockColor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(blockState.getBlock()));
        return blockColor == null ? -1 : blockColor.getColor(blockState, blockAndTintGetter, blockPos, n);
    }

    public void register(BlockColor blockColor, Block ... blockArray) {
        for (Block block : blockArray) {
            this.blockColors.addMapping(blockColor, BuiltInRegistries.BLOCK.getId(block));
        }
    }

    private void addColoringStates(Set<Property<?>> set, Block ... blockArray) {
        for (Block block : blockArray) {
            this.coloringStates.put(block, set);
        }
    }

    private void addColoringState(Property<?> property, Block ... blockArray) {
        this.addColoringStates((Set<Property<?>>)ImmutableSet.of(property), blockArray);
    }

    public Set<Property<?>> getColoringProperties(Block block) {
        return (Set)this.coloringStates.getOrDefault(block, (Set<Property<?>>)ImmutableSet.of());
    }
}

