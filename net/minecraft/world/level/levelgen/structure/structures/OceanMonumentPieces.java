/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OceanMonumentPieces {
    private OceanMonumentPieces() {
    }

    static class FitDoubleYZRoom
    implements MonumentRoomFitter {
        FitDoubleYZRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed && roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed) {
                RoomDefinition roomDefinition2 = roomDefinition.connections[Direction.NORTH.get3DDataValue()];
                return roomDefinition2.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition2.connections[Direction.UP.get3DDataValue()].claimed;
            }
            return false;
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleYZRoom(direction, roomDefinition);
        }
    }

    static class FitDoubleXYRoom
    implements MonumentRoomFitter {
        FitDoubleXYRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] && !roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed && roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed) {
                RoomDefinition roomDefinition2 = roomDefinition.connections[Direction.EAST.get3DDataValue()];
                return roomDefinition2.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition2.connections[Direction.UP.get3DDataValue()].claimed;
            }
            return false;
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
            roomDefinition.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleXYRoom(direction, roomDefinition);
        }
    }

    static class FitDoubleZRoom
    implements MonumentRoomFitter {
        FitDoubleZRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            return roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed;
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            RoomDefinition roomDefinition2 = roomDefinition;
            if (!roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] || roomDefinition.connections[Direction.NORTH.get3DDataValue()].claimed) {
                roomDefinition2 = roomDefinition.connections[Direction.SOUTH.get3DDataValue()];
            }
            roomDefinition2.claimed = true;
            roomDefinition2.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleZRoom(direction, roomDefinition2);
        }
    }

    static class FitDoubleXRoom
    implements MonumentRoomFitter {
        FitDoubleXRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            return roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] && !roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed;
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleXRoom(direction, roomDefinition);
        }
    }

    static class FitDoubleYRoom
    implements MonumentRoomFitter {
        FitDoubleYRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            return roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && !roomDefinition.connections[Direction.UP.get3DDataValue()].claimed;
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            roomDefinition.claimed = true;
            roomDefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentDoubleYRoom(direction, roomDefinition);
        }
    }

    static class FitSimpleTopRoom
    implements MonumentRoomFitter {
        FitSimpleTopRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            return !roomDefinition.hasOpening[Direction.WEST.get3DDataValue()] && !roomDefinition.hasOpening[Direction.EAST.get3DDataValue()] && !roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()] && !roomDefinition.hasOpening[Direction.UP.get3DDataValue()];
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            roomDefinition.claimed = true;
            return new OceanMonumentSimpleTopRoom(direction, roomDefinition);
        }
    }

    static class FitSimpleRoom
    implements MonumentRoomFitter {
        FitSimpleRoom() {
        }

        @Override
        public boolean fits(RoomDefinition roomDefinition) {
            return true;
        }

        @Override
        public OceanMonumentPiece create(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            roomDefinition.claimed = true;
            return new OceanMonumentSimpleRoom(direction, roomDefinition, randomSource);
        }
    }

    static interface MonumentRoomFitter {
        public boolean fits(RoomDefinition var1);

        public OceanMonumentPiece create(Direction var1, RoomDefinition var2, RandomSource var3);
    }

    static class RoomDefinition {
        final int index;
        final RoomDefinition[] connections = new RoomDefinition[6];
        final boolean[] hasOpening = new boolean[6];
        boolean claimed;
        boolean isSource;
        private int scanIndex;

        public RoomDefinition(int n) {
            this.index = n;
        }

        public void setConnection(Direction direction, RoomDefinition roomDefinition) {
            this.connections[direction.get3DDataValue()] = roomDefinition;
            roomDefinition.connections[direction.getOpposite().get3DDataValue()] = this;
        }

        public void updateOpenings() {
            for (int i = 0; i < 6; ++i) {
                this.hasOpening[i] = this.connections[i] != null;
            }
        }

        public boolean findSource(int n) {
            if (this.isSource) {
                return true;
            }
            this.scanIndex = n;
            for (int i = 0; i < 6; ++i) {
                if (this.connections[i] == null || !this.hasOpening[i] || this.connections[i].scanIndex == n || !this.connections[i].findSource(n)) continue;
                return true;
            }
            return false;
        }

        public boolean isSpecial() {
            return this.index >= 75;
        }

        public int countOpenings() {
            int n = 0;
            for (int i = 0; i < 6; ++i) {
                if (!this.hasOpening[i]) continue;
                ++n;
            }
            return n;
        }
    }

    public static class OceanMonumentPenthouse
    extends OceanMonumentPiece {
        public OceanMonumentPenthouse(Direction direction, BoundingBox boundingBox) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, direction, 1, boundingBox);
        }

        public OceanMonumentPenthouse(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n;
            this.generateBox(worldGenLevel, boundingBox, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            for (n = 2; n <= 11; n += 3) {
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 0, 0, n, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 13, 0, n, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, n, 0, 0, boundingBox);
            }
            this.generateBox(worldGenLevel, boundingBox, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 5, 0, 8, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 8, 0, 8, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 10, 0, 10, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 3, 0, 10, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
            n = 3;
            for (int i = 0; i < 2; ++i) {
                for (int j = 2; j <= 8; j += 3) {
                    this.generateBox(worldGenLevel, boundingBox, n, 0, j, n, 2, j, BASE_LIGHT, BASE_LIGHT, false);
                }
                n = 10;
            }
            this.generateBox(worldGenLevel, boundingBox, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
            this.generateWaterBox(worldGenLevel, boundingBox, 6, -1, 3, 7, -1, 4);
            this.spawnElder(worldGenLevel, boundingBox, 6, 1, 6);
        }
    }

    public static class OceanMonumentWingRoom
    extends OceanMonumentPiece {
        private int mainDesign;

        public OceanMonumentWingRoom(Direction direction, BoundingBox boundingBox, int n) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, direction, 1, boundingBox);
            this.mainDesign = n & 1;
        }

        public OceanMonumentWingRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.mainDesign == 0) {
                int n;
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, 10 - n, 3 - n, 20 - n, 12 + n, 3 - n, 20, BASE_LIGHT, BASE_LIGHT, false);
                }
                this.generateBox(worldGenLevel, boundingBox, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);
                for (n = 18; n >= 7; n -= 3) {
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 3, n, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, 16, 3, n, boundingBox);
                }
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 0, 10, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 12, 0, 10, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 0, 12, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 12, 0, 12, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 8, 3, 6, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 14, 3, 6, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 2, 4, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 4, 1, 4, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 0, 4, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 2, 4, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 18, 1, 4, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 0, 4, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 2, 18, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 4, 1, 18, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 4, 0, 18, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 2, 18, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 18, 1, 18, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 18, 0, 18, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 9, 7, 20, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 13, 7, 20, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                this.spawnElder(worldGenLevel, boundingBox, 11, 2, 16);
            } else if (this.mainDesign == 1) {
                int n;
                this.generateBox(worldGenLevel, boundingBox, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
                int n2 = 9;
                int n3 = 20;
                int n4 = 5;
                for (n = 0; n < 2; ++n) {
                    this.placeBlock(worldGenLevel, BASE_LIGHT, n2, 6, 20, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, n2, 5, 20, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_LIGHT, n2, 4, 20, boundingBox);
                    n2 = 13;
                }
                this.generateBox(worldGenLevel, boundingBox, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
                n2 = 10;
                for (n = 0; n < 2; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n2, 0, 10, n2, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, 0, 12, n2, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, n2, 0, 10, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, n2, 0, 12, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, n2, 4, 10, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, n2, 4, 12, boundingBox);
                    n2 = 12;
                }
                n2 = 8;
                for (n = 0; n < 2; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n2, 0, 7, n2, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, 0, 14, n2, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
                    n2 = 14;
                }
                this.generateBox(worldGenLevel, boundingBox, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
                this.spawnElder(worldGenLevel, boundingBox, 11, 5, 13);
            }
        }
    }

    public static class OceanMonumentCoreRoom
    extends OceanMonumentPiece {
        public OceanMonumentCoreRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, direction, roomDefinition, 2, 2, 2);
        }

        public OceanMonumentCoreRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 0, 14, 8, 14, BASE_GRAY);
            int n = 7;
            BlockState blockState = BASE_LIGHT;
            this.generateBox(worldGenLevel, boundingBox, 0, 7, 0, 0, 7, 15, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 15, 7, 0, 15, 7, 15, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 7, 0, 15, 7, 0, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 7, 15, 14, 7, 15, blockState, blockState, false);
            for (n = 1; n <= 6; ++n) {
                blockState = BASE_LIGHT;
                if (n == 2 || n == 6) {
                    blockState = BASE_GRAY;
                }
                for (int i = 0; i <= 15; i += 15) {
                    this.generateBox(worldGenLevel, boundingBox, i, n, 0, i, n, 1, blockState, blockState, false);
                    this.generateBox(worldGenLevel, boundingBox, i, n, 6, i, n, 9, blockState, blockState, false);
                    this.generateBox(worldGenLevel, boundingBox, i, n, 14, i, n, 15, blockState, blockState, false);
                }
                this.generateBox(worldGenLevel, boundingBox, 1, n, 0, 1, n, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 6, n, 0, 9, n, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 14, n, 0, 14, n, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 1, n, 15, 14, n, 15, blockState, blockState, false);
            }
            this.generateBox(worldGenLevel, boundingBox, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);
            for (n = 3; n <= 6; n += 3) {
                for (int i = 6; i <= 9; i += 3) {
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, i, n, 6, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, i, n, 9, boundingBox);
                }
            }
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
        }
    }

    public static class OceanMonumentDoubleYZRoom
    extends OceanMonumentPiece {
        public OceanMonumentDoubleYZRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, direction, roomDefinition, 1, 2, 2);
        }

        public OceanMonumentDoubleYZRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockState blockState;
            int n;
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            RoomDefinition roomDefinition3 = roomDefinition.connections[Direction.UP.get3DDataValue()];
            RoomDefinition roomDefinition4 = roomDefinition2.connections[Direction.UP.get3DDataValue()];
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 8, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition4.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 6, 8, 7, BASE_GRAY);
            }
            if (roomDefinition3.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 8, 6, 8, 14, BASE_GRAY);
            }
            for (n = 1; n <= 7; ++n) {
                blockState = BASE_LIGHT;
                if (n == 2 || n == 6) {
                    blockState = BASE_GRAY;
                }
                this.generateBox(worldGenLevel, boundingBox, 0, n, 0, 0, n, 15, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 7, n, 0, 7, n, 15, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 1, n, 0, 6, n, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 1, n, 15, 6, n, 15, blockState, blockState, false);
            }
            for (n = 1; n <= 7; ++n) {
                blockState = BASE_BLACK;
                if (n == 2 || n == 6) {
                    blockState = LAMP_BLOCK;
                }
                this.generateBox(worldGenLevel, boundingBox, 3, n, 7, 4, n, 8, blockState, blockState, false);
            }
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 15, 4, 2, 15);
            }
            if (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 11, 0, 2, 12);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 11, 7, 2, 12);
            }
            if (roomDefinition4.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 0, 4, 6, 0);
            }
            if (roomDefinition4.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 5, 3, 7, 6, 4);
                this.generateBox(worldGenLevel, boundingBox, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            }
            if (roomDefinition4.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 6, 4);
                this.generateBox(worldGenLevel, boundingBox, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            }
            if (roomDefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 15, 4, 6, 15);
            }
            if (roomDefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 5, 11, 0, 6, 12);
                this.generateBox(worldGenLevel, boundingBox, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
            }
            if (roomDefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 5, 11, 7, 6, 12);
                this.generateBox(worldGenLevel, boundingBox, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
            }
        }
    }

    public static class OceanMonumentDoubleXYRoom
    extends OceanMonumentPiece {
        public OceanMonumentDoubleXYRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, direction, roomDefinition, 2, 2, 1);
        }

        public OceanMonumentDoubleXYRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            RoomDefinition roomDefinition3 = roomDefinition2.connections[Direction.UP.get3DDataValue()];
            RoomDefinition roomDefinition4 = roomDefinition.connections[Direction.UP.get3DDataValue()];
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 8, 0, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition3.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 7, 8, 6, BASE_GRAY);
            }
            if (roomDefinition4.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 8, 8, 1, 14, 8, 6, BASE_GRAY);
            }
            for (int i = 1; i <= 7; ++i) {
                BlockState blockState = BASE_LIGHT;
                if (i == 2 || i == 6) {
                    blockState = BASE_GRAY;
                }
                this.generateBox(worldGenLevel, boundingBox, 0, i, 0, 0, i, 7, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 15, i, 0, 15, i, 7, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 1, i, 0, 15, i, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 1, i, 7, 14, i, 7, blockState, blockState, false);
            }
            this.generateBox(worldGenLevel, boundingBox, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 6, 6, 2, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 9, 6, 2, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 6, 6, 5, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 9, 6, 5, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 4, 2, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 4, 5, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 4, 2, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 10, 4, 5, boundingBox);
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 0, 12, 2, 0);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 7, 12, 2, 7);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 15, 1, 3, 15, 2, 4);
            }
            if (roomDefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 0, 4, 6, 0);
            }
            if (roomDefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 5, 7, 4, 6, 7);
            }
            if (roomDefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 6, 4);
            }
            if (roomDefinition4.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 6, 0);
            }
            if (roomDefinition4.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 11, 5, 7, 12, 6, 7);
            }
            if (roomDefinition4.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 15, 5, 3, 15, 6, 4);
            }
        }
    }

    public static class OceanMonumentDoubleZRoom
    extends OceanMonumentPiece {
        public OceanMonumentDoubleZRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, direction, roomDefinition, 1, 1, 2);
        }

        public OceanMonumentDoubleZRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 8, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition2.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 7, BASE_GRAY);
            }
            if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 8, 6, 4, 14, BASE_GRAY);
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 5, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 5, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 10, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 10, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 2, 3, 5, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 5, 3, 5, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 2, 3, 10, boundingBox);
            this.placeBlock(worldGenLevel, BASE_LIGHT, 5, 3, 10, boundingBox);
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 15, 4, 2, 15);
            }
            if (roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 11, 0, 2, 12);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 11, 7, 2, 12);
            }
        }
    }

    public static class OceanMonumentDoubleXRoom
    extends OceanMonumentPiece {
        public OceanMonumentDoubleXRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, direction, roomDefinition, 2, 1, 1);
        }

        public OceanMonumentDoubleXRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
            RoomDefinition roomDefinition2 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 8, 0, roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, roomDefinition2.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (roomDefinition2.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 7, 4, 6, BASE_GRAY);
            }
            if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 8, 4, 1, 14, 4, 6, BASE_GRAY);
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 3, boundingBox);
            this.placeBlock(worldGenLevel, LAMP_BLOCK, 9, 2, 3, boundingBox);
            if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
            if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
            }
            if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
            }
            if (roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 0, 12, 2, 0);
            }
            if (roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 11, 1, 7, 12, 2, 7);
            }
            if (roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 15, 1, 3, 15, 2, 4);
            }
        }
    }

    public static class OceanMonumentDoubleYRoom
    extends OceanMonumentPiece {
        public OceanMonumentDoubleYRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, direction, roomDefinition, 1, 2, 1);
        }

        public OceanMonumentDoubleYRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            RoomDefinition roomDefinition = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
            if (roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 8, 1, 6, 8, 6, BASE_GRAY);
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            RoomDefinition roomDefinition2 = this.roomDefinition;
            for (int i = 1; i <= 5; i += 4) {
                int n = 0;
                if (roomDefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 2, i, n, 2, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 5, i, n, 5, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 3, i + 2, n, 4, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, 0, i, n, 7, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 0, i + 1, n, 7, i + 1, n, BASE_GRAY, BASE_GRAY, false);
                }
                n = 7;
                if (roomDefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 2, i, n, 2, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 5, i, n, 5, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 3, i + 2, n, 4, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, 0, i, n, 7, i + 2, n, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 0, i + 1, n, 7, i + 1, n, BASE_GRAY, BASE_GRAY, false);
                }
                int n2 = 0;
                if (roomDefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, n2, i, 2, n2, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, i, 5, n2, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, i + 2, 3, n2, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, n2, i, 0, n2, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, i + 1, 0, n2, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }
                n2 = 7;
                if (roomDefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, n2, i, 2, n2, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, i, 5, n2, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, i + 2, 3, n2, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, n2, i, 0, n2, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n2, i + 1, 0, n2, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }
                roomDefinition2 = roomDefinition;
            }
        }
    }

    public static class OceanMonumentSimpleTopRoom
    extends OceanMonumentPiece {
        public OceanMonumentSimpleTopRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, direction, roomDefinition, 1, 1, 1);
        }

        public OceanMonumentSimpleTopRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
            }
            for (int i = 1; i <= 6; ++i) {
                for (int j = 1; j <= 6; ++j) {
                    if (randomSource.nextInt(3) == 0) continue;
                    int n = 2 + (randomSource.nextInt(4) == 0 ? 0 : 1);
                    BlockState blockState = Blocks.WET_SPONGE.defaultBlockState();
                    this.generateBox(worldGenLevel, boundingBox, i, n, j, i, 3, j, blockState, blockState, false);
                }
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
            }
        }
    }

    public static class OceanMonumentSimpleRoom
    extends OceanMonumentPiece {
        private int mainDesign;

        public OceanMonumentSimpleRoom(Direction direction, RoomDefinition roomDefinition, RandomSource randomSource) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, direction, roomDefinition, 1, 1, 1);
            this.mainDesign = randomSource.nextInt(3);
        }

        public OceanMonumentSimpleRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            boolean bl;
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(worldGenLevel, boundingBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }
            if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(worldGenLevel, boundingBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
            }
            boolean bl2 = bl = this.mainDesign != 0 && randomSource.nextBoolean() && !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()] && !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && this.roomDefinition.countOpenings() > 1;
            if (this.mainDesign == 0) {
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 1, 2, 1, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 1, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 1, 2, 6, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 6, 2, 6, boundingBox);
                if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(worldGenLevel, boundingBox, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                }
            } else if (this.mainDesign == 1) {
                this.generateBox(worldGenLevel, boundingBox, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 2, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 2, 2, 5, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 5, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 5, 2, 2, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(worldGenLevel, BASE_GRAY, 1, 2, 0, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 0, 2, 1, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 1, 2, 7, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 0, 2, 6, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 6, 2, 7, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 7, 2, 6, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 6, 2, 0, boundingBox);
                this.placeBlock(worldGenLevel, BASE_GRAY, 7, 2, 1, boundingBox);
                if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
                }
                if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateBox(worldGenLevel, boundingBox, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(worldGenLevel, boundingBox, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
                }
            } else if (this.mainDesign == 2) {
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
                if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 0, 4, 2, 0);
                }
                if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
                }
                if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 0, 2, 4);
                }
                if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 3, 7, 2, 4);
                }
            }
            if (bl) {
                this.generateBox(worldGenLevel, boundingBox, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            }
        }
    }

    public static class OceanMonumentEntryRoom
    extends OceanMonumentPiece {
        public OceanMonumentEntryRoom(Direction direction, RoomDefinition roomDefinition) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, direction, roomDefinition, 1, 1, 1);
        }

        public OceanMonumentEntryRoom(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, compoundTag);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 3, 1, 7, 4, 2, 7);
            }
            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 3, 1, 2, 4);
            }
            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(worldGenLevel, boundingBox, 6, 1, 3, 7, 2, 4);
            }
        }
    }

    public static class MonumentBuilding
    extends OceanMonumentPiece {
        private static final int WIDTH = 58;
        private static final int HEIGHT = 22;
        private static final int DEPTH = 58;
        public static final int BIOME_RANGE_CHECK = 29;
        private static final int TOP_POSITION = 61;
        private RoomDefinition sourceRoom;
        private RoomDefinition coreRoom;
        private final List<OceanMonumentPiece> childPieces = Lists.newArrayList();

        public MonumentBuilding(RandomSource randomSource, int n, int n2, Direction direction) {
            super(StructurePieceType.OCEAN_MONUMENT_BUILDING, direction, 0, MonumentBuilding.makeBoundingBox(n, 39, n2, direction, 58, 23, 58));
            Object object2;
            this.setOrientation(direction);
            List<RoomDefinition> list = this.generateRoomGraph(randomSource);
            this.sourceRoom.claimed = true;
            this.childPieces.add(new OceanMonumentEntryRoom(direction, this.sourceRoom));
            this.childPieces.add(new OceanMonumentCoreRoom(direction, this.coreRoom));
            ArrayList arrayList = Lists.newArrayList();
            arrayList.add(new FitDoubleXYRoom());
            arrayList.add(new FitDoubleYZRoom());
            arrayList.add(new FitDoubleZRoom());
            arrayList.add(new FitDoubleXRoom());
            arrayList.add(new FitDoubleYRoom());
            arrayList.add(new FitSimpleTopRoom());
            arrayList.add(new FitSimpleRoom());
            block0: for (RoomDefinition object32 : list) {
                if (object32.claimed || object32.isSpecial()) continue;
                for (Object object2 : arrayList) {
                    if (!object2.fits(object32)) continue;
                    this.childPieces.add(object2.create(direction, object32, randomSource));
                    continue block0;
                }
            }
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(9, 0, 22);
            for (OceanMonumentPiece oceanMonumentPiece : this.childPieces) {
                oceanMonumentPiece.getBoundingBox().move(mutableBlockPos);
            }
            BoundingBox boundingBox = BoundingBox.fromCorners(this.getWorldPos(1, 1, 1), this.getWorldPos(23, 8, 21));
            BoundingBox boundingBox2 = BoundingBox.fromCorners(this.getWorldPos(34, 1, 1), this.getWorldPos(56, 8, 21));
            object2 = BoundingBox.fromCorners(this.getWorldPos(22, 13, 22), this.getWorldPos(35, 17, 35));
            int n3 = randomSource.nextInt();
            this.childPieces.add(new OceanMonumentWingRoom(direction, boundingBox, n3++));
            this.childPieces.add(new OceanMonumentWingRoom(direction, boundingBox2, n3++));
            this.childPieces.add(new OceanMonumentPenthouse(direction, (BoundingBox)object2));
        }

        public MonumentBuilding(CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_MONUMENT_BUILDING, compoundTag);
        }

        private List<RoomDefinition> generateRoomGraph(RandomSource randomSource) {
            int n;
            int n2;
            int n3;
            int n4;
            int n5;
            int n6;
            int n7;
            RoomDefinition[] roomDefinitionArray = new RoomDefinition[75];
            for (n7 = 0; n7 < 5; ++n7) {
                for (n6 = 0; n6 < 4; ++n6) {
                    n5 = 0;
                    n4 = MonumentBuilding.getRoomIndex(n7, 0, n6);
                    roomDefinitionArray[n4] = new RoomDefinition(n4);
                }
            }
            for (n7 = 0; n7 < 5; ++n7) {
                for (n6 = 0; n6 < 4; ++n6) {
                    n5 = 1;
                    n4 = MonumentBuilding.getRoomIndex(n7, 1, n6);
                    roomDefinitionArray[n4] = new RoomDefinition(n4);
                }
            }
            for (n7 = 1; n7 < 4; ++n7) {
                for (n6 = 0; n6 < 2; ++n6) {
                    n5 = 2;
                    n4 = MonumentBuilding.getRoomIndex(n7, 2, n6);
                    roomDefinitionArray[n4] = new RoomDefinition(n4);
                }
            }
            this.sourceRoom = roomDefinitionArray[GRIDROOM_SOURCE_INDEX];
            for (n7 = 0; n7 < 5; ++n7) {
                for (n6 = 0; n6 < 5; ++n6) {
                    for (n5 = 0; n5 < 3; ++n5) {
                        n4 = MonumentBuilding.getRoomIndex(n7, n5, n6);
                        if (roomDefinitionArray[n4] == null) continue;
                        for (Direction object : Direction.values()) {
                            int n8;
                            n3 = n7 + object.getStepX();
                            n2 = n5 + object.getStepY();
                            n = n6 + object.getStepZ();
                            if (n3 < 0 || n3 >= 5 || n < 0 || n >= 5 || n2 < 0 || n2 >= 3 || roomDefinitionArray[n8 = MonumentBuilding.getRoomIndex(n3, n2, n)] == null) continue;
                            if (n == n6) {
                                roomDefinitionArray[n4].setConnection(object, roomDefinitionArray[n8]);
                                continue;
                            }
                            roomDefinitionArray[n4].setConnection(object.getOpposite(), roomDefinitionArray[n8]);
                        }
                    }
                }
            }
            RoomDefinition roomDefinition = new RoomDefinition(1003);
            RoomDefinition roomDefinition2 = new RoomDefinition(1001);
            RoomDefinition roomDefinition3 = new RoomDefinition(1002);
            roomDefinitionArray[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, roomDefinition);
            roomDefinitionArray[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, roomDefinition2);
            roomDefinitionArray[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, roomDefinition3);
            roomDefinition.claimed = true;
            roomDefinition2.claimed = true;
            roomDefinition3.claimed = true;
            this.sourceRoom.isSource = true;
            this.coreRoom = roomDefinitionArray[MonumentBuilding.getRoomIndex(randomSource.nextInt(4), 0, 2)];
            this.coreRoom.claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            ObjectArrayList objectArrayList = new ObjectArrayList();
            for (RoomDefinition roomDefinition4 : roomDefinitionArray) {
                if (roomDefinition4 == null) continue;
                roomDefinition4.updateOpenings();
                objectArrayList.add((Object)roomDefinition4);
            }
            roomDefinition.updateOpenings();
            Util.shuffle(objectArrayList, randomSource);
            int n9 = 1;
            for (RoomDefinition roomDefinition4 : objectArrayList) {
                int n10 = 0;
                for (n3 = 0; n10 < 2 && n3 < 5; ++n3) {
                    n2 = randomSource.nextInt(6);
                    if (!roomDefinition4.hasOpening[n2]) continue;
                    n = Direction.from3DDataValue(n2).getOpposite().get3DDataValue();
                    roomDefinition4.hasOpening[n2] = false;
                    roomDefinition4.connections[n2].hasOpening[n] = false;
                    if (roomDefinition4.findSource(n9++) && roomDefinition4.connections[n2].findSource(n9++)) {
                        ++n10;
                        continue;
                    }
                    roomDefinition4.hasOpening[n2] = true;
                    roomDefinition4.connections[n2].hasOpening[n] = true;
                }
            }
            objectArrayList.add((Object)roomDefinition);
            objectArrayList.add((Object)roomDefinition2);
            objectArrayList.add((Object)roomDefinition3);
            return objectArrayList;
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n;
            int n2 = Math.max(worldGenLevel.getSeaLevel(), 64) - this.boundingBox.minY();
            this.generateWaterBox(worldGenLevel, boundingBox, 0, 0, 0, 58, n2, 58);
            this.generateWing(false, 0, worldGenLevel, randomSource, boundingBox);
            this.generateWing(true, 33, worldGenLevel, randomSource, boundingBox);
            this.generateEntranceArchs(worldGenLevel, randomSource, boundingBox);
            this.generateEntranceWall(worldGenLevel, randomSource, boundingBox);
            this.generateRoofPiece(worldGenLevel, randomSource, boundingBox);
            this.generateLowerWall(worldGenLevel, randomSource, boundingBox);
            this.generateMiddleWall(worldGenLevel, randomSource, boundingBox);
            this.generateUpperWall(worldGenLevel, randomSource, boundingBox);
            for (n = 0; n < 7; ++n) {
                int n3 = 0;
                while (n3 < 7) {
                    if (n3 == 0 && n == 3) {
                        n3 = 6;
                    }
                    int n4 = n * 9;
                    int n5 = n3 * 9;
                    for (int i = 0; i < 4; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            this.placeBlock(worldGenLevel, BASE_LIGHT, n4 + i, 0, n5 + j, boundingBox);
                            this.fillColumnDown(worldGenLevel, BASE_LIGHT, n4 + i, -1, n5 + j, boundingBox);
                        }
                    }
                    if (n == 0 || n == 6) {
                        ++n3;
                        continue;
                    }
                    n3 += 6;
                }
            }
            for (n = 0; n < 5; ++n) {
                this.generateWaterBox(worldGenLevel, boundingBox, -1 - n, 0 + n * 2, -1 - n, -1 - n, 23, 58 + n);
                this.generateWaterBox(worldGenLevel, boundingBox, 58 + n, 0 + n * 2, -1 - n, 58 + n, 23, 58 + n);
                this.generateWaterBox(worldGenLevel, boundingBox, 0 - n, 0 + n * 2, -1 - n, 57 + n, 23, -1 - n);
                this.generateWaterBox(worldGenLevel, boundingBox, 0 - n, 0 + n * 2, 58 + n, 57 + n, 23, 58 + n);
            }
            for (OceanMonumentPiece oceanMonumentPiece : this.childPieces) {
                if (!oceanMonumentPiece.getBoundingBox().intersects(boundingBox)) continue;
                oceanMonumentPiece.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
            }
        }

        private void generateWing(boolean bl, int n, WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            int n2 = 24;
            if (this.chunkIntersects(boundingBox, n, 0, n + 23, 20)) {
                int n3;
                int n4;
                this.generateBox(worldGenLevel, boundingBox, n + 0, 0, 0, n + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, n + 0, 1, 0, n + 24, 10, 20);
                for (n4 = 0; n4 < 4; ++n4) {
                    this.generateBox(worldGenLevel, boundingBox, n + n4, n4 + 1, n4, n + n4, n4 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n + n4 + 7, n4 + 5, n4 + 7, n + n4 + 7, n4 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n + 17 - n4, n4 + 5, n4 + 7, n + 17 - n4, n4 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n + 24 - n4, n4 + 1, n4, n + 24 - n4, n4 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n + n4 + 1, n4 + 1, n4, n + 23 - n4, n4 + 1, n4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, n + n4 + 8, n4 + 5, n4 + 7, n + 16 - n4, n4 + 5, n4 + 7, BASE_LIGHT, BASE_LIGHT, false);
                }
                this.generateBox(worldGenLevel, boundingBox, n + 4, 4, 4, n + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 7, 4, 4, n + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 18, 4, 4, n + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 11, 8, 11, n + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(worldGenLevel, DOT_DECO_DATA, n + 12, 9, 12, boundingBox);
                this.placeBlock(worldGenLevel, DOT_DECO_DATA, n + 12, 9, 15, boundingBox);
                this.placeBlock(worldGenLevel, DOT_DECO_DATA, n + 12, 9, 18, boundingBox);
                n4 = n + (bl ? 19 : 5);
                int n5 = n + (bl ? 5 : 19);
                for (n3 = 20; n3 >= 5; n3 -= 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n4, 5, n3, boundingBox);
                }
                for (n3 = 19; n3 >= 7; n3 -= 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n5, 5, n3, boundingBox);
                }
                for (n3 = 0; n3 < 4; ++n3) {
                    int n6 = bl ? n + 24 - (17 - n3 * 3) : n + 17 - n3 * 3;
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n6, 5, 5, boundingBox);
                }
                this.placeBlock(worldGenLevel, DOT_DECO_DATA, n5, 5, 5, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, n + 11, 1, 12, n + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 12, 1, 11, n + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
            }
        }

        private void generateEntranceArchs(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            if (this.chunkIntersects(boundingBox, 22, 5, 35, 17)) {
                this.generateWaterBox(worldGenLevel, boundingBox, 25, 0, 0, 32, 8, 20);
                for (int i = 0; i < 4; ++i) {
                    this.generateBox(worldGenLevel, boundingBox, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.placeBlock(worldGenLevel, BASE_LIGHT, 25, 5, 5 + i * 4, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 6, 5 + i * 4, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, 26, 5, 5 + i * 4, boundingBox);
                    this.generateBox(worldGenLevel, boundingBox, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.placeBlock(worldGenLevel, BASE_LIGHT, 32, 5, 5 + i * 4, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 6, 5 + i * 4, boundingBox);
                    this.placeBlock(worldGenLevel, LAMP_BLOCK, 31, 5, 5 + i * 4, boundingBox);
                    this.generateBox(worldGenLevel, boundingBox, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, BASE_GRAY, BASE_GRAY, false);
                }
            }
        }

        private void generateEntranceWall(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            if (this.chunkIntersects(boundingBox, 15, 20, 42, 21)) {
                int n;
                this.generateBox(worldGenLevel, boundingBox, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 26, 1, 21, 31, 3, 21);
                this.generateBox(worldGenLevel, boundingBox, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 27, 3, 21, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 30, 3, 21, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 2, 21, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 2, 21, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 25, 1, 21, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 32, 1, 21, boundingBox);
                for (n = 0; n < 7; ++n) {
                    this.placeBlock(worldGenLevel, BASE_BLACK, 28 - n, 6 + n, 21, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_BLACK, 29 + n, 6 + n, 21, boundingBox);
                }
                for (n = 0; n < 4; ++n) {
                    this.placeBlock(worldGenLevel, BASE_BLACK, 28 - n, 9 + n, 21, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_BLACK, 29 + n, 9 + n, 21, boundingBox);
                }
                this.placeBlock(worldGenLevel, BASE_BLACK, 28, 12, 21, boundingBox);
                this.placeBlock(worldGenLevel, BASE_BLACK, 29, 12, 21, boundingBox);
                for (n = 0; n < 3; ++n) {
                    this.placeBlock(worldGenLevel, BASE_BLACK, 22 - n * 2, 8, 21, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_BLACK, 22 - n * 2, 9, 21, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_BLACK, 35 + n * 2, 8, 21, boundingBox);
                    this.placeBlock(worldGenLevel, BASE_BLACK, 35 + n * 2, 9, 21, boundingBox);
                }
                this.generateWaterBox(worldGenLevel, boundingBox, 15, 13, 21, 42, 15, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 15, 1, 21, 15, 6, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 16, 1, 21, 16, 5, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 17, 1, 21, 20, 4, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 21, 1, 21, 21, 3, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 22, 1, 21, 22, 2, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 23, 1, 21, 24, 1, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 42, 1, 21, 42, 6, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 41, 1, 21, 41, 5, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 37, 1, 21, 40, 4, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 36, 1, 21, 36, 3, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 33, 1, 21, 34, 1, 21);
                this.generateWaterBox(worldGenLevel, boundingBox, 35, 1, 21, 35, 2, 21);
            }
        }

        private void generateRoofPiece(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            if (this.chunkIntersects(boundingBox, 21, 21, 36, 36)) {
                this.generateBox(worldGenLevel, boundingBox, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 21, 1, 22, 36, 23, 36);
                for (int i = 0; i < 4; ++i) {
                    this.generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(worldGenLevel, boundingBox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
                }
                this.generateBox(worldGenLevel, boundingBox, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 20, 26, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 27, 21, 27, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 27, 20, 27, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 26, 20, 31, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 27, 21, 30, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 27, 20, 30, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 20, 31, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 30, 21, 30, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 30, 20, 30, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 31, 20, 26, boundingBox);
                this.placeBlock(worldGenLevel, BASE_LIGHT, 30, 21, 27, boundingBox);
                this.placeBlock(worldGenLevel, LAMP_BLOCK, 30, 20, 27, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
            }
        }

        private void generateLowerWall(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            int n;
            if (this.chunkIntersects(boundingBox, 0, 21, 6, 58)) {
                this.generateBox(worldGenLevel, boundingBox, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 0, 1, 21, 6, 7, 57);
                this.generateBox(worldGenLevel, boundingBox, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n, n + 1, 21, n, n + 1, 57 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 23; n < 53; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, 5, 5, n, boundingBox);
                }
                this.placeBlock(worldGenLevel, DOT_DECO_DATA, 5, 5, 52, boundingBox);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n, n + 1, 21, n, n + 1, 57 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
                this.generateBox(worldGenLevel, boundingBox, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
            }
            if (this.chunkIntersects(boundingBox, 51, 21, 58, 58)) {
                this.generateBox(worldGenLevel, boundingBox, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 51, 1, 21, 57, 7, 57);
                this.generateBox(worldGenLevel, boundingBox, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, 57 - n, n + 1, 21, 57 - n, n + 1, 57 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 23; n < 53; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, 52, 5, n, boundingBox);
                }
                this.placeBlock(worldGenLevel, DOT_DECO_DATA, 52, 5, 52, boundingBox);
                this.generateBox(worldGenLevel, boundingBox, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
            }
            if (this.chunkIntersects(boundingBox, 0, 51, 57, 57)) {
                this.generateBox(worldGenLevel, boundingBox, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 51, 50, 10, 57);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n + 1, n + 1, 57 - n, 56 - n, n + 1, 57 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
            }
        }

        private void generateMiddleWall(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            int n;
            if (this.chunkIntersects(boundingBox, 7, 21, 13, 50)) {
                this.generateBox(worldGenLevel, boundingBox, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 7, 1, 21, 13, 10, 50);
                this.generateBox(worldGenLevel, boundingBox, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n + 7, n + 5, 21, n + 7, n + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 21; n <= 45; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, 12, 9, n, boundingBox);
                }
            }
            if (this.chunkIntersects(boundingBox, 44, 21, 50, 54)) {
                this.generateBox(worldGenLevel, boundingBox, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 44, 1, 21, 50, 10, 50);
                this.generateBox(worldGenLevel, boundingBox, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, 50 - n, n + 5, 21, 50 - n, n + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 21; n <= 45; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, 45, 9, n, boundingBox);
                }
            }
            if (this.chunkIntersects(boundingBox, 8, 44, 49, 54)) {
                this.generateBox(worldGenLevel, boundingBox, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 14, 1, 44, 43, 10, 50);
                for (n = 12; n <= 45; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 9, 45, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 9, 52, boundingBox);
                    if (n != 12 && n != 18 && n != 24 && n != 33 && n != 39 && n != 45) continue;
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 9, 47, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 9, 50, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 10, 45, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 10, 46, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 10, 51, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 10, 52, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 11, 47, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 11, 50, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 12, 48, boundingBox);
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 12, 49, boundingBox);
                }
                for (n = 0; n < 3; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, 8 + n, 5 + n, 54, 49 - n, 5 + n, 54, BASE_GRAY, BASE_GRAY, false);
                }
                this.generateBox(worldGenLevel, boundingBox, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
            }
        }

        private void generateUpperWall(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox) {
            int n;
            if (this.chunkIntersects(boundingBox, 14, 21, 20, 43)) {
                this.generateBox(worldGenLevel, boundingBox, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 14, 1, 22, 20, 14, 43);
                this.generateBox(worldGenLevel, boundingBox, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, n + 14, n + 9, 21, n + 14, n + 9, 43 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 23; n <= 39; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, 19, 13, n, boundingBox);
                }
            }
            if (this.chunkIntersects(boundingBox, 37, 21, 43, 43)) {
                this.generateBox(worldGenLevel, boundingBox, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 37, 1, 22, 43, 14, 43);
                this.generateBox(worldGenLevel, boundingBox, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, 43 - n, n + 9, 21, 43 - n, n + 9, 43 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 23; n <= 39; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, 38, 13, n, boundingBox);
                }
            }
            if (this.chunkIntersects(boundingBox, 15, 37, 42, 43)) {
                this.generateBox(worldGenLevel, boundingBox, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(worldGenLevel, boundingBox, 21, 1, 37, 36, 14, 43);
                this.generateBox(worldGenLevel, boundingBox, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);
                for (n = 0; n < 4; ++n) {
                    this.generateBox(worldGenLevel, boundingBox, 15 + n, n + 9, 43 - n, 42 - n, n + 9, 43 - n, BASE_LIGHT, BASE_LIGHT, false);
                }
                for (n = 21; n <= 36; n += 3) {
                    this.placeBlock(worldGenLevel, DOT_DECO_DATA, n, 13, 38, boundingBox);
                }
            }
        }
    }

    protected static abstract class OceanMonumentPiece
    extends StructurePiece {
        protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
        protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
        protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
        protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
        protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
        protected static final boolean DO_FILL = true;
        protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
        protected static final Set<Block> FILL_KEEP = ImmutableSet.builder().add((Object)Blocks.ICE).add((Object)Blocks.PACKED_ICE).add((Object)Blocks.BLUE_ICE).add((Object)FILL_BLOCK.getBlock()).build();
        protected static final int GRIDROOM_WIDTH = 8;
        protected static final int GRIDROOM_DEPTH = 8;
        protected static final int GRIDROOM_HEIGHT = 4;
        protected static final int GRID_WIDTH = 5;
        protected static final int GRID_DEPTH = 5;
        protected static final int GRID_HEIGHT = 3;
        protected static final int GRID_FLOOR_COUNT = 25;
        protected static final int GRID_SIZE = 75;
        protected static final int GRIDROOM_SOURCE_INDEX = OceanMonumentPiece.getRoomIndex(2, 0, 0);
        protected static final int GRIDROOM_TOP_CONNECT_INDEX = OceanMonumentPiece.getRoomIndex(2, 2, 0);
        protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = OceanMonumentPiece.getRoomIndex(0, 1, 0);
        protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = OceanMonumentPiece.getRoomIndex(4, 1, 0);
        protected static final int LEFTWING_INDEX = 1001;
        protected static final int RIGHTWING_INDEX = 1002;
        protected static final int PENTHOUSE_INDEX = 1003;
        protected RoomDefinition roomDefinition;

        protected static int getRoomIndex(int n, int n2, int n3) {
            return n2 * 25 + n3 * 5 + n;
        }

        public OceanMonumentPiece(StructurePieceType structurePieceType, Direction direction, int n, BoundingBox boundingBox) {
            super(structurePieceType, n, boundingBox);
            this.setOrientation(direction);
        }

        protected OceanMonumentPiece(StructurePieceType structurePieceType, int n, Direction direction, RoomDefinition roomDefinition, int n2, int n3, int n4) {
            super(structurePieceType, n, OceanMonumentPiece.makeBoundingBox(direction, roomDefinition, n2, n3, n4));
            this.setOrientation(direction);
            this.roomDefinition = roomDefinition;
        }

        private static BoundingBox makeBoundingBox(Direction direction, RoomDefinition roomDefinition, int n, int n2, int n3) {
            int n4 = roomDefinition.index;
            int n5 = n4 % 5;
            int n6 = n4 / 5 % 5;
            int n7 = n4 / 25;
            BoundingBox boundingBox = OceanMonumentPiece.makeBoundingBox(0, 0, 0, direction, n * 8, n2 * 4, n3 * 8);
            switch (direction) {
                case NORTH: {
                    boundingBox.move(n5 * 8, n7 * 4, -(n6 + n3) * 8 + 1);
                    break;
                }
                case SOUTH: {
                    boundingBox.move(n5 * 8, n7 * 4, n6 * 8);
                    break;
                }
                case WEST: {
                    boundingBox.move(-(n6 + n3) * 8 + 1, n7 * 4, n5 * 8);
                    break;
                }
                default: {
                    boundingBox.move(n6 * 8, n7 * 4, n5 * 8);
                }
            }
            return boundingBox;
        }

        public OceanMonumentPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        }

        protected void generateWaterBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, int n6) {
            for (int i = n2; i <= n5; ++i) {
                for (int j = n; j <= n4; ++j) {
                    for (int k = n3; k <= n6; ++k) {
                        BlockState blockState = this.getBlock(worldGenLevel, j, i, k, boundingBox);
                        if (FILL_KEEP.contains(blockState.getBlock())) continue;
                        if (this.getWorldY(i) >= worldGenLevel.getSeaLevel() && blockState != FILL_BLOCK) {
                            this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), j, i, k, boundingBox);
                            continue;
                        }
                        this.placeBlock(worldGenLevel, FILL_BLOCK, j, i, k, boundingBox);
                    }
                }
            }
        }

        protected void generateDefaultFloor(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, boolean bl) {
            if (bl) {
                this.generateBox(worldGenLevel, boundingBox, n + 0, 0, n2 + 0, n + 2, 0, n2 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 5, 0, n2 + 0, n + 8 - 1, 0, n2 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 3, 0, n2 + 0, n + 4, 0, n2 + 2, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 3, 0, n2 + 5, n + 4, 0, n2 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(worldGenLevel, boundingBox, n + 3, 0, n2 + 2, n + 4, 0, n2 + 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, n + 3, 0, n2 + 5, n + 4, 0, n2 + 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, n + 2, 0, n2 + 3, n + 2, 0, n2 + 4, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(worldGenLevel, boundingBox, n + 5, 0, n2 + 3, n + 5, 0, n2 + 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, n + 0, 0, n2 + 0, n + 8 - 1, 0, n2 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            }
        }

        protected void generateBoxOnFillOnly(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, int n6, BlockState blockState) {
            for (int i = n2; i <= n5; ++i) {
                for (int j = n; j <= n4; ++j) {
                    for (int k = n3; k <= n6; ++k) {
                        if (this.getBlock(worldGenLevel, j, i, k, boundingBox) != FILL_BLOCK) continue;
                        this.placeBlock(worldGenLevel, blockState, j, i, k, boundingBox);
                    }
                }
            }
        }

        protected boolean chunkIntersects(BoundingBox boundingBox, int n, int n2, int n3, int n4) {
            int n5 = this.getWorldX(n, n2);
            int n6 = this.getWorldZ(n, n2);
            int n7 = this.getWorldX(n3, n4);
            int n8 = this.getWorldZ(n3, n4);
            return boundingBox.intersects(Math.min(n5, n7), Math.min(n6, n8), Math.max(n5, n7), Math.max(n6, n8));
        }

        protected void spawnElder(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3) {
            ElderGuardian elderGuardian;
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            if (boundingBox.isInside(mutableBlockPos) && (elderGuardian = EntityType.ELDER_GUARDIAN.create(worldGenLevel.getLevel(), EntitySpawnReason.STRUCTURE)) != null) {
                elderGuardian.heal(elderGuardian.getMaxHealth());
                elderGuardian.snapTo((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5, 0.0f, 0.0f);
                elderGuardian.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(elderGuardian.blockPosition()), EntitySpawnReason.STRUCTURE, null);
                worldGenLevel.addFreshEntityWithPassengers(elderGuardian);
            }
        }
    }
}

