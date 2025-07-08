/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.structures;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidPiece
extends ScatteredFeaturePiece {
    public static final int WIDTH = 21;
    public static final int DEPTH = 21;
    private final boolean[] hasPlacedChest = new boolean[4];
    private final List<BlockPos> potentialSuspiciousSandWorldPositions = new ArrayList<BlockPos>();
    private BlockPos randomCollapsedRoofPos = BlockPos.ZERO;

    public DesertPyramidPiece(RandomSource randomSource, int n, int n2) {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, n, 64, n2, 21, 15, 21, DesertPyramidPiece.getRandomHorizontalDirection(randomSource));
    }

    public DesertPyramidPiece(CompoundTag compoundTag) {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, compoundTag);
        this.hasPlacedChest[0] = compoundTag.getBooleanOr("hasPlacedChest0", false);
        this.hasPlacedChest[1] = compoundTag.getBooleanOr("hasPlacedChest1", false);
        this.hasPlacedChest[2] = compoundTag.getBooleanOr("hasPlacedChest2", false);
        this.hasPlacedChest[3] = compoundTag.getBooleanOr("hasPlacedChest3", false);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
        compoundTag.putBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
        compoundTag.putBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
        compoundTag.putBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
        compoundTag.putBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int n;
        int n2;
        if (!this.updateHeightPositionToLowestGroundHeight(worldGenLevel, -randomSource.nextInt(3))) {
            return;
        }
        this.generateBox(worldGenLevel, boundingBox, 0, -4, 0, this.width - 1, 0, this.depth - 1, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        for (n2 = 1; n2 <= 9; ++n2) {
            this.generateBox(worldGenLevel, boundingBox, n2, n2, n2, this.width - 1 - n2, n2, this.depth - 1 - n2, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, n2 + 1, n2, n2 + 1, this.width - 2 - n2, n2, this.depth - 2 - n2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        }
        for (n2 = 0; n2 < this.width; ++n2) {
            for (int i = 0; i < this.depth; ++i) {
                int n3 = -5;
                this.fillColumnDown(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), n2, -5, i, boundingBox);
            }
        }
        BlockState blockState = (BlockState)Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState blockState2 = (BlockState)Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        BlockState blockState3 = (BlockState)Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState blockState4 = (BlockState)Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
        this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, blockState, 2, 10, 0, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, 2, 10, 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, 0, 10, 2, boundingBox);
        this.placeBlock(worldGenLevel, blockState4, 4, 10, 2, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, this.width - 5, 0, 0, this.width - 1, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 4, 10, 1, this.width - 2, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, blockState, this.width - 3, 10, 0, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, this.width - 3, 10, 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, this.width - 5, 10, 2, boundingBox);
        this.placeBlock(worldGenLevel, blockState4, this.width - 1, 10, 2, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 9, 1, 0, 11, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 1, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 2, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 3, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, 3, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 3, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 2, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 1, 1, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 4, 1, 2, 8, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 12, 1, 2, 16, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 5, 4, 5, this.width - 6, 4, this.depth - 6, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 9, 4, 9, 11, 4, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 8, 1, 8, 8, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 12, 1, 8, 12, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 8, 1, 12, 8, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 12, 1, 12, 12, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 5, 1, 5, this.width - 2, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 7, 7, 9, this.width - 7, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 5, 5, 9, 5, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 6, 5, 9, this.width - 6, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 5, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 6, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 6, 6, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 6, 5, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 6, 6, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), this.width - 7, 6, 10, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 2, 4, 4, 2, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 3, 4, 4, this.width - 3, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, blockState, 2, 4, 5, boundingBox);
        this.placeBlock(worldGenLevel, blockState, 2, 3, 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState, this.width - 3, 4, 5, boundingBox);
        this.placeBlock(worldGenLevel, blockState, this.width - 3, 3, 4, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 3, 1, 3, this.width - 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), 1, 1, 2, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.SANDSTONE.defaultBlockState(), this.width - 2, 1, 2, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), 1, 2, 2, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), this.width - 2, 2, 2, boundingBox);
        this.placeBlock(worldGenLevel, blockState4, 2, 1, 2, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, this.width - 3, 1, 2, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 4, 3, 5, 4, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 5, 3, 5, this.width - 5, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 3, 1, 5, 4, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, this.width - 6, 1, 5, this.width - 5, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        for (n = 5; n <= 17; n += 2) {
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 4, 1, n, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 4, 2, n, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), this.width - 5, 1, n, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), this.width - 5, 2, n, boundingBox);
        }
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 7, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 8, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 9, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 9, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 8, 0, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12, 0, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 7, 0, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 13, 0, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 11, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 11, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 12, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 13, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.BLUE_TERRACOTTA.defaultBlockState(), 10, 0, 10, boundingBox);
        for (n = 0; n <= this.width - 1; n += this.width - 1) {
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 2, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 2, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 2, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 3, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 3, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 3, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 4, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), n, 4, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 4, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 5, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 5, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 5, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 6, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), n, 6, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 6, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 7, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 7, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 7, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 8, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 8, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 8, 3, boundingBox);
        }
        for (n = 2; n <= this.width - 3; n += this.width - 3 - 2) {
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n - 1, 2, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 2, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n + 1, 2, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n - 1, 3, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 3, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n + 1, 3, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n - 1, 4, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), n, 4, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n + 1, 4, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n - 1, 5, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 5, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n + 1, 5, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n - 1, 6, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), n, 6, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n + 1, 6, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n - 1, 7, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n, 7, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), n + 1, 7, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n - 1, 8, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n, 8, 0, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), n + 1, 8, 0, boundingBox);
        }
        this.generateBox(worldGenLevel, boundingBox, 8, 4, 0, 12, 6, 0, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, 6, 0, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, 6, 0, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 5, 0, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, 5, 0, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 5, 0, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 8, -14, 8, 12, -11, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 8, -10, 8, 12, -10, 12, Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 8, -9, 8, 12, -9, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 9, -11, 9, 11, -1, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 10, -11, 10, boundingBox);
        this.generateBox(worldGenLevel, boundingBox, 9, -13, 9, 11, -13, 11, Blocks.TNT.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, -11, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 8, -10, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 7, -10, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 7, -11, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, -11, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 12, -10, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 13, -10, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 13, -11, 10, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -11, 8, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -10, 8, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 7, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 7, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -11, 12, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 10, -10, 12, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 13, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 13, boundingBox);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.hasPlacedChest[direction.get2DDataValue()]) continue;
            int n4 = direction.getStepX() * 2;
            int n5 = direction.getStepZ() * 2;
            this.hasPlacedChest[direction.get2DDataValue()] = this.createChest(worldGenLevel, boundingBox, randomSource, 10 + n4, -11, 10 + n5, BuiltInLootTables.DESERT_PYRAMID);
        }
        this.addCellar(worldGenLevel, boundingBox);
    }

    private void addCellar(WorldGenLevel worldGenLevel, BoundingBox boundingBox) {
        BlockPos blockPos = new BlockPos(16, -4, 13);
        this.addCellarStairs(blockPos, worldGenLevel, boundingBox);
        this.addCellarRoom(blockPos, worldGenLevel, boundingBox);
    }

    private void addCellarStairs(BlockPos blockPos, WorldGenLevel worldGenLevel, BoundingBox boundingBox) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        BlockState blockState = Blocks.SANDSTONE_STAIRS.defaultBlockState();
        this.placeBlock(worldGenLevel, blockState.rotate(Rotation.COUNTERCLOCKWISE_90), 13, -1, 17, boundingBox);
        this.placeBlock(worldGenLevel, blockState.rotate(Rotation.COUNTERCLOCKWISE_90), 14, -2, 17, boundingBox);
        this.placeBlock(worldGenLevel, blockState.rotate(Rotation.COUNTERCLOCKWISE_90), 15, -3, 17, boundingBox);
        BlockState blockState2 = Blocks.SAND.defaultBlockState();
        BlockState blockState3 = Blocks.SANDSTONE.defaultBlockState();
        boolean bl = worldGenLevel.getRandom().nextBoolean();
        this.placeBlock(worldGenLevel, blockState2, n - 4, n2 + 4, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n - 3, n2 + 4, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n - 2, n2 + 4, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n - 1, n2 + 4, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n, n2 + 4, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n - 2, n2 + 3, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, bl ? blockState2 : blockState3, n - 1, n2 + 3, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, !bl ? blockState2 : blockState3, n, n2 + 3, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n - 1, n2 + 2, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n, n2 + 2, n3 + 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n, n2 + 1, n3 + 4, boundingBox);
    }

    private void addCellarRoom(BlockPos blockPos, WorldGenLevel worldGenLevel, BoundingBox boundingBox) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        BlockState blockState = Blocks.CUT_SANDSTONE.defaultBlockState();
        BlockState blockState2 = Blocks.CHISELED_SANDSTONE.defaultBlockState();
        this.generateBox(worldGenLevel, boundingBox, n - 3, n2 + 1, n3 - 3, n - 3, n2 + 1, n3 + 2, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n + 3, n2 + 1, n3 - 3, n + 3, n2 + 1, n3 + 2, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, n2 + 1, n3 - 3, n + 3, n2 + 1, n3 - 2, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, n2 + 1, n3 + 3, n + 3, n2 + 1, n3 + 3, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, n2 + 2, n3 - 3, n - 3, n2 + 2, n3 + 2, blockState2, blockState2, true);
        this.generateBox(worldGenLevel, boundingBox, n + 3, n2 + 2, n3 - 3, n + 3, n2 + 2, n3 + 2, blockState2, blockState2, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, n2 + 2, n3 - 3, n + 3, n2 + 2, n3 - 2, blockState2, blockState2, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, n2 + 2, n3 + 3, n + 3, n2 + 2, n3 + 3, blockState2, blockState2, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, -1, n3 - 3, n - 3, -1, n3 + 2, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n + 3, -1, n3 - 3, n + 3, -1, n3 + 2, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, -1, n3 - 3, n + 3, -1, n3 - 2, blockState, blockState, true);
        this.generateBox(worldGenLevel, boundingBox, n - 3, -1, n3 + 3, n + 3, -1, n3 + 3, blockState, blockState, true);
        this.placeSandBox(n - 2, n2 + 1, n3 - 2, n + 2, n2 + 3, n3 + 2);
        this.placeCollapsedRoof(worldGenLevel, boundingBox, n - 2, n2 + 4, n3 - 2, n + 2, n3 + 2);
        BlockState blockState3 = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
        BlockState blockState4 = Blocks.BLUE_TERRACOTTA.defaultBlockState();
        this.placeBlock(worldGenLevel, blockState4, n, n2, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n + 1, n2, n3 - 1, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n + 1, n2, n3 + 1, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n - 1, n2, n3 - 1, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n - 1, n2, n3 + 1, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n + 2, n2, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n - 2, n2, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n, n2, n3 + 2, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n, n2, n3 - 2, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n + 3, n2, n3, boundingBox);
        this.placeSand(n + 3, n2 + 1, n3);
        this.placeSand(n + 3, n2 + 2, n3);
        this.placeBlock(worldGenLevel, blockState, n + 4, n2 + 1, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n + 4, n2 + 2, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n - 3, n2, n3, boundingBox);
        this.placeSand(n - 3, n2 + 1, n3);
        this.placeSand(n - 3, n2 + 2, n3);
        this.placeBlock(worldGenLevel, blockState, n - 4, n2 + 1, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n - 4, n2 + 2, n3, boundingBox);
        this.placeBlock(worldGenLevel, blockState3, n, n2, n3 + 3, boundingBox);
        this.placeSand(n, n2 + 1, n3 + 3);
        this.placeSand(n, n2 + 2, n3 + 3);
        this.placeBlock(worldGenLevel, blockState3, n, n2, n3 - 3, boundingBox);
        this.placeSand(n, n2 + 1, n3 - 3);
        this.placeSand(n, n2 + 2, n3 - 3);
        this.placeBlock(worldGenLevel, blockState, n, n2 + 1, n3 - 4, boundingBox);
        this.placeBlock(worldGenLevel, blockState2, n, -2, n3 - 4, boundingBox);
    }

    private void placeSand(int n, int n2, int n3) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
        this.potentialSuspiciousSandWorldPositions.add(mutableBlockPos);
    }

    private void placeSandBox(int n, int n2, int n3, int n4, int n5, int n6) {
        for (int i = n2; i <= n5; ++i) {
            for (int j = n; j <= n4; ++j) {
                for (int k = n3; k <= n6; ++k) {
                    this.placeSand(j, i, k);
                }
            }
        }
    }

    private void placeCollapsedRoofPiece(WorldGenLevel worldGenLevel, int n, int n2, int n3, BoundingBox boundingBox) {
        if (worldGenLevel.getRandom().nextFloat() < 0.33f) {
            BlockState blockState = Blocks.SANDSTONE.defaultBlockState();
            this.placeBlock(worldGenLevel, blockState, n, n2, n3, boundingBox);
        } else {
            BlockState blockState = Blocks.SAND.defaultBlockState();
            this.placeBlock(worldGenLevel, blockState, n, n2, n3, boundingBox);
        }
    }

    private void placeCollapsedRoof(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5) {
        int n6;
        for (int i = n; i <= n4; ++i) {
            for (n6 = n3; n6 <= n5; ++n6) {
                this.placeCollapsedRoofPiece(worldGenLevel, i, n2, n6, boundingBox);
            }
        }
        RandomSource randomSource = RandomSource.create(worldGenLevel.getSeed()).forkPositional().at(this.getWorldPos(n, n2, n3));
        n6 = randomSource.nextIntBetweenInclusive(n, n4);
        int n7 = randomSource.nextIntBetweenInclusive(n3, n5);
        this.randomCollapsedRoofPos = new BlockPos(this.getWorldX(n6, n7), this.getWorldY(n2), this.getWorldZ(n6, n7));
    }

    public List<BlockPos> getPotentialSuspiciousSandWorldPositions() {
        return this.potentialSuspiciousSandWorldPositions;
    }

    public BlockPos getRandomCollapsedRoofPos() {
        return this.randomCollapsedRoofPos;
    }
}

