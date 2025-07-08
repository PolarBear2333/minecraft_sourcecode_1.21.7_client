/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {

    public static class BuriedTreasurePiece
    extends StructurePiece {
        public BuriedTreasurePiece(BlockPos blockPos) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, 0, new BoundingBox(blockPos));
        }

        public BuriedTreasurePiece(CompoundTag compoundTag) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, compoundTag);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.minX(), this.boundingBox.minZ());
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.boundingBox.minX(), n, this.boundingBox.minZ());
            while (mutableBlockPos.getY() > worldGenLevel.getMinY()) {
                BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
                BlockState blockState2 = worldGenLevel.getBlockState((BlockPos)mutableBlockPos.below());
                if (blockState2 == Blocks.SANDSTONE.defaultBlockState() || blockState2 == Blocks.STONE.defaultBlockState() || blockState2 == Blocks.ANDESITE.defaultBlockState() || blockState2 == Blocks.GRANITE.defaultBlockState() || blockState2 == Blocks.DIORITE.defaultBlockState()) {
                    BlockState blockState3 = blockState.isAir() || this.isLiquid(blockState) ? Blocks.SAND.defaultBlockState() : blockState;
                    for (Direction direction : Direction.values()) {
                        Vec3i vec3i = mutableBlockPos.relative(direction);
                        BlockState blockState4 = worldGenLevel.getBlockState((BlockPos)vec3i);
                        if (!blockState4.isAir() && !this.isLiquid(blockState4)) continue;
                        BlockPos blockPos2 = ((BlockPos)vec3i).below();
                        BlockState blockState5 = worldGenLevel.getBlockState(blockPos2);
                        if ((blockState5.isAir() || this.isLiquid(blockState5)) && direction != Direction.UP) {
                            worldGenLevel.setBlock((BlockPos)vec3i, blockState2, 3);
                            continue;
                        }
                        worldGenLevel.setBlock((BlockPos)vec3i, blockState3, 3);
                    }
                    this.boundingBox = new BoundingBox(mutableBlockPos);
                    this.createChest(worldGenLevel, boundingBox, randomSource, mutableBlockPos, BuiltInLootTables.BURIED_TREASURE, null);
                    return;
                }
                mutableBlockPos.move(0, -1, 0);
            }
        }

        private boolean isLiquid(BlockState blockState) {
            return blockState == Blocks.WATER.defaultBlockState() || blockState == Blocks.LAVA.defaultBlockState();
        }
    }
}

