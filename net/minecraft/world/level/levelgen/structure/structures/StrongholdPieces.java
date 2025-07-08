/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class StrongholdPieces {
    private static final int SMALL_DOOR_WIDTH = 3;
    private static final int SMALL_DOOR_HEIGHT = 3;
    private static final int MAX_DEPTH = 50;
    private static final int LOWEST_Y_POSITION = 10;
    private static final boolean CHECK_AIR = true;
    public static final int MAGIC_START_Y = 64;
    private static final PieceWeight[] STRONGHOLD_PIECE_WEIGHTS = new PieceWeight[]{new PieceWeight(Straight.class, 40, 0), new PieceWeight(PrisonHall.class, 5, 5), new PieceWeight(LeftTurn.class, 20, 0), new PieceWeight(RightTurn.class, 20, 0), new PieceWeight(RoomCrossing.class, 10, 6), new PieceWeight(StraightStairsDown.class, 5, 5), new PieceWeight(StairsDown.class, 5, 5), new PieceWeight(FiveCrossing.class, 5, 4), new PieceWeight(ChestCorridor.class, 5, 4), new PieceWeight(Library.class, 10, 2){

        @Override
        public boolean doPlace(int n) {
            return super.doPlace(n) && n > 4;
        }
    }, new PieceWeight(PortalRoom.class, 20, 1){

        @Override
        public boolean doPlace(int n) {
            return super.doPlace(n) && n > 5;
        }
    }};
    private static List<PieceWeight> currentPieces;
    static Class<? extends StrongholdPiece> imposedPiece;
    private static int totalWeight;
    static final SmoothStoneSelector SMOOTH_STONE_SELECTOR;

    public static void resetPieces() {
        currentPieces = Lists.newArrayList();
        for (PieceWeight pieceWeight : STRONGHOLD_PIECE_WEIGHTS) {
            pieceWeight.placeCount = 0;
            currentPieces.add(pieceWeight);
        }
        imposedPiece = null;
    }

    private static boolean updatePieceWeight() {
        boolean bl = false;
        totalWeight = 0;
        for (PieceWeight pieceWeight : currentPieces) {
            if (pieceWeight.maxPlaceCount > 0 && pieceWeight.placeCount < pieceWeight.maxPlaceCount) {
                bl = true;
            }
            totalWeight += pieceWeight.weight;
        }
        return bl;
    }

    private static StrongholdPiece findAndCreatePieceFactory(Class<? extends StrongholdPiece> clazz, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, @Nullable Direction direction, int n4) {
        StrongholdPiece strongholdPiece = null;
        if (clazz == Straight.class) {
            strongholdPiece = Straight.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == PrisonHall.class) {
            strongholdPiece = PrisonHall.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == LeftTurn.class) {
            strongholdPiece = LeftTurn.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == RightTurn.class) {
            strongholdPiece = RightTurn.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == RoomCrossing.class) {
            strongholdPiece = RoomCrossing.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == StraightStairsDown.class) {
            strongholdPiece = StraightStairsDown.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == StairsDown.class) {
            strongholdPiece = StairsDown.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == FiveCrossing.class) {
            strongholdPiece = FiveCrossing.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == ChestCorridor.class) {
            strongholdPiece = ChestCorridor.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == Library.class) {
            strongholdPiece = Library.createPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
        } else if (clazz == PortalRoom.class) {
            strongholdPiece = PortalRoom.createPiece(structurePieceAccessor, n, n2, n3, direction, n4);
        }
        return strongholdPiece;
    }

    private static StrongholdPiece generatePieceFromSmallDoor(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
        if (!StrongholdPieces.updatePieceWeight()) {
            return null;
        }
        if (imposedPiece != null) {
            StrongholdPiece strongholdPiece = StrongholdPieces.findAndCreatePieceFactory(imposedPiece, structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
            imposedPiece = null;
            if (strongholdPiece != null) {
                return strongholdPiece;
            }
        }
        int n5 = 0;
        block0: while (n5 < 5) {
            ++n5;
            int n6 = randomSource.nextInt(totalWeight);
            for (PieceWeight pieceWeight : currentPieces) {
                if ((n6 -= pieceWeight.weight) >= 0) continue;
                if (!pieceWeight.doPlace(n4) || pieceWeight == startPiece.previousPiece) continue block0;
                StrongholdPiece strongholdPiece = StrongholdPieces.findAndCreatePieceFactory(pieceWeight.pieceClass, structurePieceAccessor, randomSource, n, n2, n3, direction, n4);
                if (strongholdPiece == null) continue;
                ++pieceWeight.placeCount;
                startPiece.previousPiece = pieceWeight;
                if (!pieceWeight.isValid()) {
                    currentPieces.remove(pieceWeight);
                }
                return strongholdPiece;
            }
        }
        BoundingBox boundingBox = FillerCorridor.findPieceBox(structurePieceAccessor, randomSource, n, n2, n3, direction);
        if (boundingBox != null && boundingBox.minY() > 1) {
            return new FillerCorridor(n4, boundingBox, direction);
        }
        return null;
    }

    static StructurePiece generateAndAddPiece(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, @Nullable Direction direction, int n4) {
        if (n4 > 50) {
            return null;
        }
        if (Math.abs(n - startPiece.getBoundingBox().minX()) > 112 || Math.abs(n3 - startPiece.getBoundingBox().minZ()) > 112) {
            return null;
        }
        StrongholdPiece strongholdPiece = StrongholdPieces.generatePieceFromSmallDoor(startPiece, structurePieceAccessor, randomSource, n, n2, n3, direction, n4 + 1);
        if (strongholdPiece != null) {
            structurePieceAccessor.addPiece(strongholdPiece);
            startPiece.pendingChildren.add(strongholdPiece);
        }
        return strongholdPiece;
    }

    static {
        SMOOTH_STONE_SELECTOR = new SmoothStoneSelector();
    }

    static class PieceWeight {
        public final Class<? extends StrongholdPiece> pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;

        public PieceWeight(Class<? extends StrongholdPiece> clazz, int n, int n2) {
            this.pieceClass = clazz;
            this.weight = n;
            this.maxPlaceCount = n2;
        }

        public boolean doPlace(int n) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    public static class Straight
    extends StrongholdPiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 5;
        private static final int DEPTH = 7;
        private final boolean leftChild;
        private final boolean rightChild;

        public Straight(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
            this.leftChild = randomSource.nextInt(2) == 0;
            this.rightChild = randomSource.nextInt(2) == 0;
        }

        public Straight(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT, compoundTag);
            this.leftChild = compoundTag.getBooleanOr("Left", false);
            this.rightChild = compoundTag.getBooleanOr("Right", false);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Left", this.leftChild);
            compoundTag.putBoolean("Right", this.rightChild);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
            if (this.leftChild) {
                this.generateSmallDoorChildLeft((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 2);
            }
            if (this.rightChild) {
                this.generateSmallDoorChildRight((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 2);
            }
        }

        public static Straight createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, 7, direction);
            if (!Straight.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new Straight(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 6, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 1, 0);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
            BlockState blockState = (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
            BlockState blockState2 = (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
            this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, 0.1f, 1, 2, 1, blockState);
            this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, 0.1f, 3, 2, 1, blockState2);
            this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, 0.1f, 1, 2, 5, blockState);
            this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, 0.1f, 3, 2, 5, blockState2);
            if (this.leftChild) {
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 2, 0, 3, 4, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.rightChild) {
                this.generateBox(worldGenLevel, boundingBox, 4, 1, 2, 4, 3, 4, CAVE_AIR, CAVE_AIR, false);
            }
        }
    }

    public static class PrisonHall
    extends StrongholdPiece {
        protected static final int WIDTH = 9;
        protected static final int HEIGHT = 5;
        protected static final int DEPTH = 11;

        public PrisonHall(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_PRISON_HALL, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
        }

        public PrisonHall(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_PRISON_HALL, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
        }

        public static PrisonHall createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 9, 5, 11, direction);
            if (!PrisonHall.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new PrisonHall(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 8, 4, 10, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 1, 0);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 10, 3, 3, 10, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 1, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 3, 4, 3, 3, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 7, 4, 3, 7, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 9, 4, 3, 9, false, randomSource, SMOOTH_STONE_SELECTOR);
            for (int i = 1; i <= 3; ++i) {
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, i, 4, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true)).setValue(IronBarsBlock.EAST, true), 4, i, 5, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, i, 6, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true), 5, i, 5, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true), 6, i, 5, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true), 7, i, 5, boundingBox);
            }
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, 3, 2, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true), 4, 3, 8, boundingBox);
            BlockState blockState = (BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST)).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
            this.placeBlock(worldGenLevel, blockState, 4, 1, 2, boundingBox);
            this.placeBlock(worldGenLevel, blockState2, 4, 2, 2, boundingBox);
            this.placeBlock(worldGenLevel, blockState, 4, 1, 8, boundingBox);
            this.placeBlock(worldGenLevel, blockState2, 4, 2, 8, boundingBox);
        }
    }

    public static class LeftTurn
    extends Turn {
        public LeftTurn(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_LEFT_TURN, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
        }

        public LeftTurn(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_LEFT_TURN, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            Direction direction = this.getOrientation();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.generateSmallDoorChildLeft((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
            } else {
                this.generateSmallDoorChildRight((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
            }
        }

        public static LeftTurn createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, 5, direction);
            if (!LeftTurn.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new LeftTurn(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 4, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 1, 0);
            Direction direction = this.getOrientation();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
            }
        }
    }

    public static class RightTurn
    extends Turn {
        public RightTurn(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_RIGHT_TURN, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
        }

        public RightTurn(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_RIGHT_TURN, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            Direction direction = this.getOrientation();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.generateSmallDoorChildRight((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
            } else {
                this.generateSmallDoorChildLeft((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
            }
        }

        public static RightTurn createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, 5, direction);
            if (!RightTurn.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new RightTurn(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 4, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 1, 0);
            Direction direction = this.getOrientation();
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
            }
        }
    }

    public static class RoomCrossing
    extends StrongholdPiece {
        protected static final int WIDTH = 11;
        protected static final int HEIGHT = 7;
        protected static final int DEPTH = 11;
        protected final int type;

        public RoomCrossing(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
            this.type = randomSource.nextInt(5);
        }

        public RoomCrossing(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, compoundTag);
            this.type = compoundTag.getIntOr("Type", 0);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putInt("Type", this.type);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 4, 1);
            this.generateSmallDoorChildLeft((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 4);
            this.generateSmallDoorChildRight((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 4);
        }

        public static RoomCrossing createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -4, -1, 0, 11, 7, 11, direction);
            if (!RoomCrossing.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new RoomCrossing(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 10, 6, 10, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 4, 1, 0);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 10, 6, 3, 10, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 4, 0, 3, 6, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, 10, 1, 4, 10, 3, 6, CAVE_AIR, CAVE_AIR, false);
            switch (this.type) {
                default: {
                    break;
                }
                case 0: {
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, boundingBox);
                    break;
                }
                case 1: {
                    for (int i = 0; i < 5; ++i) {
                        this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + i, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + i, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 3, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 7, boundingBox);
                    }
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.WATER.defaultBlockState(), 5, 4, 5, boundingBox);
                    break;
                }
                case 2: {
                    int n;
                    for (n = 1; n <= 9; ++n) {
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, n, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, n, boundingBox);
                    }
                    for (n = 1; n <= 9; ++n) {
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), n, 3, 1, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), n, 3, 9, boundingBox);
                    }
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, boundingBox);
                    for (n = 1; n <= 3; ++n) {
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, n, 4, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, n, 4, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, n, 6, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, n, 6, boundingBox);
                    }
                    this.placeBlock(worldGenLevel, Blocks.WALL_TORCH.defaultBlockState(), 5, 3, 5, boundingBox);
                    for (n = 2; n <= 8; ++n) {
                        this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, n, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, n, boundingBox);
                        if (n <= 3 || n >= 7) {
                            this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, n, boundingBox);
                            this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, n, boundingBox);
                            this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, n, boundingBox);
                        }
                        this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, n, boundingBox);
                        this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, n, boundingBox);
                    }
                    BlockState blockState = (BlockState)Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
                    this.placeBlock(worldGenLevel, blockState, 9, 1, 3, boundingBox);
                    this.placeBlock(worldGenLevel, blockState, 9, 2, 3, boundingBox);
                    this.placeBlock(worldGenLevel, blockState, 9, 3, 3, boundingBox);
                    this.createChest(worldGenLevel, boundingBox, randomSource, 3, 4, 8, BuiltInLootTables.STRONGHOLD_CROSSING);
                }
            }
        }
    }

    public static class StraightStairsDown
    extends StrongholdPiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 11;
        private static final int DEPTH = 8;

        public StraightStairsDown(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
        }

        public StraightStairsDown(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
        }

        public static StraightStairsDown createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -7, 0, 5, 11, 8, direction);
            if (!StraightStairsDown.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new StraightStairsDown(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 10, 7, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 7, 0);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 7);
            BlockState blockState = (BlockState)Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            for (int i = 0; i < 6; ++i) {
                this.placeBlock(worldGenLevel, blockState, 1, 6 - i, 1 + i, boundingBox);
                this.placeBlock(worldGenLevel, blockState, 2, 6 - i, 1 + i, boundingBox);
                this.placeBlock(worldGenLevel, blockState, 3, 6 - i, 1 + i, boundingBox);
                if (i >= 5) continue;
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - i, 1 + i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - i, 1 + i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - i, 1 + i, boundingBox);
            }
        }
    }

    public static class StairsDown
    extends StrongholdPiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 11;
        private static final int DEPTH = 5;
        private final boolean isSource;

        public StairsDown(StructurePieceType structurePieceType, int n, int n2, int n3, Direction direction) {
            super(structurePieceType, n, StairsDown.makeBoundingBox(n2, 64, n3, direction, 5, 11, 5));
            this.isSource = true;
            this.setOrientation(direction);
            this.entryDoor = StrongholdPiece.SmallDoorType.OPENING;
        }

        public StairsDown(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_STAIRS_DOWN, n, boundingBox);
            this.isSource = false;
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
        }

        public StairsDown(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.isSource = compoundTag.getBooleanOr("Source", false);
        }

        public StairsDown(CompoundTag compoundTag) {
            this(StructurePieceType.STRONGHOLD_STAIRS_DOWN, compoundTag);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Source", this.isSource);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            if (this.isSource) {
                imposedPiece = FiveCrossing.class;
            }
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
        }

        public static StairsDown createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -7, 0, 5, 11, 5, direction);
            if (!StairsDown.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new StairsDown(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 10, 4, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 7, 0);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 4);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, boundingBox);
        }
    }

    public static class FiveCrossing
    extends StrongholdPiece {
        protected static final int WIDTH = 10;
        protected static final int HEIGHT = 9;
        protected static final int DEPTH = 11;
        private final boolean leftLow;
        private final boolean leftHigh;
        private final boolean rightLow;
        private final boolean rightHigh;

        public FiveCrossing(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
            this.leftLow = randomSource.nextBoolean();
            this.leftHigh = randomSource.nextBoolean();
            this.rightLow = randomSource.nextBoolean();
            this.rightHigh = randomSource.nextInt(3) > 0;
        }

        public FiveCrossing(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, compoundTag);
            this.leftLow = compoundTag.getBooleanOr("leftLow", false);
            this.leftHigh = compoundTag.getBooleanOr("leftHigh", false);
            this.rightLow = compoundTag.getBooleanOr("rightLow", false);
            this.rightHigh = compoundTag.getBooleanOr("rightHigh", false);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("leftLow", this.leftLow);
            compoundTag.putBoolean("leftHigh", this.leftHigh);
            compoundTag.putBoolean("rightLow", this.rightLow);
            compoundTag.putBoolean("rightHigh", this.rightHigh);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            int n = 3;
            int n2 = 5;
            Direction direction = this.getOrientation();
            if (direction == Direction.WEST || direction == Direction.NORTH) {
                n = 8 - n;
                n2 = 8 - n2;
            }
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 5, 1);
            if (this.leftLow) {
                this.generateSmallDoorChildLeft((StartPiece)structurePiece, structurePieceAccessor, randomSource, n, 1);
            }
            if (this.leftHigh) {
                this.generateSmallDoorChildLeft((StartPiece)structurePiece, structurePieceAccessor, randomSource, n2, 7);
            }
            if (this.rightLow) {
                this.generateSmallDoorChildRight((StartPiece)structurePiece, structurePieceAccessor, randomSource, n, 1);
            }
            if (this.rightHigh) {
                this.generateSmallDoorChildRight((StartPiece)structurePiece, structurePieceAccessor, randomSource, n2, 7);
            }
        }

        public static FiveCrossing createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -4, -3, 0, 10, 9, 11, direction);
            if (!FiveCrossing.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new FiveCrossing(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 9, 8, 10, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 4, 3, 0);
            if (this.leftLow) {
                this.generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 5, 3, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.rightLow) {
                this.generateBox(worldGenLevel, boundingBox, 9, 3, 1, 9, 5, 3, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.leftHigh) {
                this.generateBox(worldGenLevel, boundingBox, 0, 5, 7, 0, 7, 9, CAVE_AIR, CAVE_AIR, false);
            }
            if (this.rightHigh) {
                this.generateBox(worldGenLevel, boundingBox, 9, 5, 7, 9, 7, 9, CAVE_AIR, CAVE_AIR, false);
            }
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 10, 7, 3, 10, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 1, 8, 2, 6, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 5, 4, 4, 9, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 8, 1, 5, 8, 4, 9, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 7, 3, 4, 9, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 5, 3, 3, 6, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 7, 7, 1, 8, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 5, 7, 7, 5, 9, (BlockState)Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), (BlockState)Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), false);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, boundingBox);
        }
    }

    public static class ChestCorridor
    extends StrongholdPiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 5;
        private static final int DEPTH = 7;
        private boolean hasPlacedChest;

        public ChestCorridor(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
        }

        public ChestCorridor(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, compoundTag);
            this.hasPlacedChest = compoundTag.getBooleanOr("Chest", false);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Chest", this.hasPlacedChest);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            this.generateSmallDoorChildForward((StartPiece)structurePiece, structurePieceAccessor, randomSource, 1, 1);
        }

        public static ChestCorridor createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, 7, direction);
            if (!ChestCorridor.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new ChestCorridor(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 4, 6, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 1, 1, 0);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, boundingBox);
            for (int i = 2; i <= 4; ++i) {
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, i, boundingBox);
            }
            if (!this.hasPlacedChest && boundingBox.isInside(this.getWorldPos(3, 2, 3))) {
                this.hasPlacedChest = true;
                this.createChest(worldGenLevel, boundingBox, randomSource, 3, 2, 3, BuiltInLootTables.STRONGHOLD_CORRIDOR);
            }
        }
    }

    public static class Library
    extends StrongholdPiece {
        protected static final int WIDTH = 14;
        protected static final int HEIGHT = 6;
        protected static final int TALL_HEIGHT = 11;
        protected static final int DEPTH = 15;
        private final boolean isTall;

        public Library(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_LIBRARY, n, boundingBox);
            this.setOrientation(direction);
            this.entryDoor = this.randomSmallDoor(randomSource);
            this.isTall = boundingBox.getYSpan() > 6;
        }

        public Library(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_LIBRARY, compoundTag);
            this.isTall = compoundTag.getBooleanOr("Tall", false);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Tall", this.isTall);
        }

        public static Library createPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -4, -1, 0, 14, 11, 15, direction);
            if (!(Library.isOkBox(boundingBox) && structurePieceAccessor.findCollisionPiece(boundingBox) == null || Library.isOkBox(boundingBox = BoundingBox.orientBox(n, n2, n3, -4, -1, 0, 14, 6, 15, direction)) && structurePieceAccessor.findCollisionPiece(boundingBox) == null)) {
                return null;
            }
            return new Library(n4, randomSource, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n;
            int n2 = 11;
            if (!this.isTall) {
                n2 = 6;
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 13, n2 - 1, 14, true, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, this.entryDoor, 4, 1, 0);
            this.generateMaybeBox(worldGenLevel, boundingBox, randomSource, 0.07f, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false);
            boolean bl = true;
            int n3 = 12;
            for (n = 1; n <= 13; ++n) {
                if ((n - 1) % 4 == 0) {
                    this.generateBox(worldGenLevel, boundingBox, 1, 1, n, 1, 4, n, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    this.generateBox(worldGenLevel, boundingBox, 12, 1, n, 12, 4, n, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2, 3, n, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 11, 3, n, boundingBox);
                    if (!this.isTall) continue;
                    this.generateBox(worldGenLevel, boundingBox, 1, 6, n, 1, 9, n, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    this.generateBox(worldGenLevel, boundingBox, 12, 6, n, 12, 9, n, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                    continue;
                }
                this.generateBox(worldGenLevel, boundingBox, 1, 1, n, 1, 4, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 12, 1, n, 12, 4, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                if (!this.isTall) continue;
                this.generateBox(worldGenLevel, boundingBox, 1, 6, n, 1, 9, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 12, 6, n, 12, 9, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            }
            for (n = 3; n < 12; n += 2) {
                this.generateBox(worldGenLevel, boundingBox, 3, 1, n, 4, 3, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 6, 1, n, 7, 3, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 9, 1, n, 10, 3, n, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            }
            if (this.isTall) {
                this.generateBox(worldGenLevel, boundingBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, boundingBox);
                BlockState blockState = (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
                BlockState blockState2 = (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
                this.generateBox(worldGenLevel, boundingBox, 3, 6, 3, 3, 6, 11, blockState2, blockState2, false);
                this.generateBox(worldGenLevel, boundingBox, 10, 6, 3, 10, 6, 9, blockState2, blockState2, false);
                this.generateBox(worldGenLevel, boundingBox, 4, 6, 2, 9, 6, 2, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 4, 6, 12, 7, 6, 12, blockState, blockState, false);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 3, 6, 2, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.EAST, true), 3, 6, 12, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.WEST, true), 10, 6, 2, boundingBox);
                for (int i = 0; i <= 2; ++i) {
                    this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.WEST, true), 8 + i, 6, 12 - i, boundingBox);
                    if (i == 2) continue;
                    this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 8 + i, 6, 11 - i, boundingBox);
                }
                BlockState blockState3 = (BlockState)Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
                this.placeBlock(worldGenLevel, blockState3, 10, 1, 13, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, 10, 2, 13, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, 10, 3, 13, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, 10, 4, 13, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, 10, 5, 13, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, 10, 6, 13, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, 10, 7, 13, boundingBox);
                int n4 = 7;
                int n5 = 7;
                BlockState blockState4 = (BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true);
                this.placeBlock(worldGenLevel, blockState4, 6, 9, 7, boundingBox);
                BlockState blockState5 = (BlockState)Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true);
                this.placeBlock(worldGenLevel, blockState5, 7, 9, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState4, 6, 8, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState5, 7, 8, 7, boundingBox);
                BlockState blockState6 = (BlockState)((BlockState)blockState2.setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
                this.placeBlock(worldGenLevel, blockState6, 6, 7, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState6, 7, 7, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState4, 5, 7, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState5, 8, 7, 7, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(FenceBlock.NORTH, true), 6, 7, 6, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(FenceBlock.SOUTH, true), 6, 7, 8, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)blockState5.setValue(FenceBlock.NORTH, true), 7, 7, 6, boundingBox);
                this.placeBlock(worldGenLevel, (BlockState)blockState5.setValue(FenceBlock.SOUTH, true), 7, 7, 8, boundingBox);
                BlockState blockState7 = Blocks.TORCH.defaultBlockState();
                this.placeBlock(worldGenLevel, blockState7, 5, 8, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState7, 8, 8, 7, boundingBox);
                this.placeBlock(worldGenLevel, blockState7, 6, 8, 6, boundingBox);
                this.placeBlock(worldGenLevel, blockState7, 6, 8, 8, boundingBox);
                this.placeBlock(worldGenLevel, blockState7, 7, 8, 6, boundingBox);
                this.placeBlock(worldGenLevel, blockState7, 7, 8, 8, boundingBox);
            }
            this.createChest(worldGenLevel, boundingBox, randomSource, 3, 3, 5, BuiltInLootTables.STRONGHOLD_LIBRARY);
            if (this.isTall) {
                this.placeBlock(worldGenLevel, CAVE_AIR, 12, 9, 1, boundingBox);
                this.createChest(worldGenLevel, boundingBox, randomSource, 12, 8, 1, BuiltInLootTables.STRONGHOLD_LIBRARY);
            }
        }
    }

    public static class PortalRoom
    extends StrongholdPiece {
        protected static final int WIDTH = 11;
        protected static final int HEIGHT = 8;
        protected static final int DEPTH = 16;
        private boolean hasPlacedSpawner;

        public PortalRoom(int n, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, n, boundingBox);
            this.setOrientation(direction);
        }

        public PortalRoom(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, compoundTag);
            this.hasPlacedSpawner = compoundTag.getBooleanOr("Mob", false);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Mob", this.hasPlacedSpawner);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            if (structurePiece != null) {
                ((StartPiece)structurePiece).portalRoomPiece = this;
            }
        }

        public static PortalRoom createPiece(StructurePieceAccessor structurePieceAccessor, int n, int n2, int n3, Direction direction, int n4) {
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -4, -1, 0, 11, 8, 16, direction);
            if (!PortalRoom.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new PortalRoom(n4, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockPos.MutableBlockPos mutableBlockPos;
            int n;
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 10, 7, 15, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateSmallDoor(worldGenLevel, randomSource, boundingBox, StrongholdPiece.SmallDoorType.GRATES, 4, 1, 0);
            int n2 = 6;
            this.generateBox(worldGenLevel, boundingBox, 1, 6, 1, 1, 6, 14, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 9, 6, 1, 9, 6, 14, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 2, 6, 1, 8, 6, 2, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 2, 6, 14, 8, 6, 14, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 2, 1, 4, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 8, 1, 1, 9, 1, 4, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 3, 1, 8, 7, 1, 12, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, true)).setValue(IronBarsBlock.SOUTH, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true)).setValue(IronBarsBlock.EAST, true);
            for (n = 3; n < 14; n += 2) {
                this.generateBox(worldGenLevel, boundingBox, 0, 3, n, 0, 4, n, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 10, 3, n, 10, 4, n, blockState, blockState, false);
            }
            for (n = 2; n < 9; n += 2) {
                this.generateBox(worldGenLevel, boundingBox, n, 3, 15, n, 4, 15, blockState2, blockState2, false);
            }
            BlockState blockState3 = (BlockState)Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 5, 6, 1, 7, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 6, 6, 2, 7, false, randomSource, SMOOTH_STONE_SELECTOR);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 7, 6, 3, 7, false, randomSource, SMOOTH_STONE_SELECTOR);
            for (int i = 4; i <= 6; ++i) {
                this.placeBlock(worldGenLevel, blockState3, i, 1, 4, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, i, 2, 5, boundingBox);
                this.placeBlock(worldGenLevel, blockState3, i, 3, 6, boundingBox);
            }
            BlockState blockState4 = (BlockState)Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
            BlockState blockState5 = (BlockState)Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
            BlockState blockState6 = (BlockState)Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
            BlockState blockState7 = (BlockState)Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
            boolean bl = true;
            boolean[] blArray = new boolean[12];
            for (int i = 0; i < blArray.length; ++i) {
                blArray[i] = randomSource.nextFloat() > 0.9f;
                bl &= blArray[i];
            }
            this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(EndPortalFrameBlock.HAS_EYE, blArray[0]), 4, 3, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(EndPortalFrameBlock.HAS_EYE, blArray[1]), 5, 3, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(EndPortalFrameBlock.HAS_EYE, blArray[2]), 6, 3, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState5.setValue(EndPortalFrameBlock.HAS_EYE, blArray[3]), 4, 3, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState5.setValue(EndPortalFrameBlock.HAS_EYE, blArray[4]), 5, 3, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState5.setValue(EndPortalFrameBlock.HAS_EYE, blArray[5]), 6, 3, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState6.setValue(EndPortalFrameBlock.HAS_EYE, blArray[6]), 3, 3, 9, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState6.setValue(EndPortalFrameBlock.HAS_EYE, blArray[7]), 3, 3, 10, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState6.setValue(EndPortalFrameBlock.HAS_EYE, blArray[8]), 3, 3, 11, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState7.setValue(EndPortalFrameBlock.HAS_EYE, blArray[9]), 7, 3, 9, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState7.setValue(EndPortalFrameBlock.HAS_EYE, blArray[10]), 7, 3, 10, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)blockState7.setValue(EndPortalFrameBlock.HAS_EYE, blArray[11]), 7, 3, 11, boundingBox);
            if (bl) {
                BlockState blockState8 = Blocks.END_PORTAL.defaultBlockState();
                this.placeBlock(worldGenLevel, blockState8, 4, 3, 9, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 5, 3, 9, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 6, 3, 9, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 4, 3, 10, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 5, 3, 10, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 6, 3, 10, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 4, 3, 11, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 5, 3, 11, boundingBox);
                this.placeBlock(worldGenLevel, blockState8, 6, 3, 11, boundingBox);
            }
            if (!this.hasPlacedSpawner && boundingBox.isInside(mutableBlockPos = this.getWorldPos(5, 3, 6))) {
                this.hasPlacedSpawner = true;
                worldGenLevel.setBlock(mutableBlockPos, Blocks.SPAWNER.defaultBlockState(), 2);
                BlockEntity blockEntity = worldGenLevel.getBlockEntity(mutableBlockPos);
                if (blockEntity instanceof SpawnerBlockEntity) {
                    SpawnerBlockEntity spawnerBlockEntity = (SpawnerBlockEntity)blockEntity;
                    spawnerBlockEntity.setEntityId(EntityType.SILVERFISH, randomSource);
                }
            }
        }
    }

    static abstract class StrongholdPiece
    extends StructurePiece {
        protected SmallDoorType entryDoor = SmallDoorType.OPENING;

        protected StrongholdPiece(StructurePieceType structurePieceType, int n, BoundingBox boundingBox) {
            super(structurePieceType, n, boundingBox);
        }

        public StrongholdPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.entryDoor = compoundTag.read("EntryDoor", SmallDoorType.LEGACY_CODEC).orElseThrow();
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            compoundTag.store("EntryDoor", SmallDoorType.LEGACY_CODEC, this.entryDoor);
        }

        protected void generateSmallDoor(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox, SmallDoorType smallDoorType, int n, int n2, int n3) {
            switch (smallDoorType.ordinal()) {
                case 0: {
                    this.generateBox(worldGenLevel, boundingBox, n, n2, n3, n + 3 - 1, n2 + 3 - 1, n3, CAVE_AIR, CAVE_AIR, false);
                    break;
                }
                case 1: {
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 1, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 2, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 2, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 2, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.OAK_DOOR.defaultBlockState(), n + 1, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), n + 1, n2 + 1, n3, boundingBox);
                    break;
                }
                case 2: {
                    this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), n + 1, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), n + 1, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true), n, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, true), n, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true)).setValue(IronBarsBlock.WEST, true), n, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true)).setValue(IronBarsBlock.WEST, true), n + 1, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true)).setValue(IronBarsBlock.WEST, true), n + 2, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true), n + 2, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, true), n + 2, n2, n3, boundingBox);
                    break;
                }
                case 3: {
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 1, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 2, n2 + 2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 2, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), n + 2, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.IRON_DOOR.defaultBlockState(), n + 1, n2, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), n + 1, n2 + 1, n3, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH), n + 2, n2 + 1, n3 + 1, boundingBox);
                    this.placeBlock(worldGenLevel, (BlockState)Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH), n + 2, n2 + 1, n3 - 1, boundingBox);
                }
            }
        }

        protected SmallDoorType randomSmallDoor(RandomSource randomSource) {
            int n = randomSource.nextInt(5);
            switch (n) {
                default: {
                    return SmallDoorType.OPENING;
                }
                case 2: {
                    return SmallDoorType.WOOD_DOOR;
                }
                case 3: {
                    return SmallDoorType.GRATES;
                }
                case 4: 
            }
            return SmallDoorType.IRON_DOOR;
        }

        @Nullable
        protected StructurePiece generateSmallDoorChildForward(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2) {
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n, this.boundingBox.minY() + n2, this.boundingBox.minZ() - 1, direction, this.getGenDepth());
                    }
                    case SOUTH: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n, this.boundingBox.minY() + n2, this.boundingBox.maxZ() + 1, direction, this.getGenDepth());
                    }
                    case WEST: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() + n2, this.boundingBox.minZ() + n, direction, this.getGenDepth());
                    }
                    case EAST: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + n2, this.boundingBox.minZ() + n, direction, this.getGenDepth());
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateSmallDoorChildLeft(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2) {
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() + n, this.boundingBox.minZ() + n2, Direction.WEST, this.getGenDepth());
                    }
                    case SOUTH: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() + n, this.boundingBox.minZ() + n2, Direction.WEST, this.getGenDepth());
                    }
                    case WEST: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n2, this.boundingBox.minY() + n, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth());
                    }
                    case EAST: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n2, this.boundingBox.minY() + n, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth());
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateSmallDoorChildRight(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2) {
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + n, this.boundingBox.minZ() + n2, Direction.EAST, this.getGenDepth());
                    }
                    case SOUTH: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + n, this.boundingBox.minZ() + n2, Direction.EAST, this.getGenDepth());
                    }
                    case WEST: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n2, this.boundingBox.minY() + n, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
                    }
                    case EAST: {
                        return StrongholdPieces.generateAndAddPiece(startPiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n2, this.boundingBox.minY() + n, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
                    }
                }
            }
            return null;
        }

        protected static boolean isOkBox(BoundingBox boundingBox) {
            return boundingBox != null && boundingBox.minY() > 10;
        }

        protected static enum SmallDoorType {
            OPENING,
            WOOD_DOOR,
            GRATES,
            IRON_DOOR;

            @Deprecated
            public static final Codec<SmallDoorType> LEGACY_CODEC;

            static {
                LEGACY_CODEC = ExtraCodecs.legacyEnum(SmallDoorType::valueOf);
            }
        }
    }

    public static class StartPiece
    extends StairsDown {
        public PieceWeight previousPiece;
        @Nullable
        public PortalRoom portalRoomPiece;
        public final List<StructurePiece> pendingChildren = Lists.newArrayList();

        public StartPiece(RandomSource randomSource, int n, int n2) {
            super(StructurePieceType.STRONGHOLD_START, 0, n, n2, StartPiece.getRandomHorizontalDirection(randomSource));
        }

        public StartPiece(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_START, compoundTag);
        }

        @Override
        public BlockPos getLocatorPosition() {
            if (this.portalRoomPiece != null) {
                return this.portalRoomPiece.getLocatorPosition();
            }
            return super.getLocatorPosition();
        }
    }

    public static class FillerCorridor
    extends StrongholdPiece {
        private final int steps;

        public FillerCorridor(int n, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, n, boundingBox);
            this.setOrientation(direction);
            this.steps = direction == Direction.NORTH || direction == Direction.SOUTH ? boundingBox.getZSpan() : boundingBox.getXSpan();
        }

        public FillerCorridor(CompoundTag compoundTag) {
            super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, compoundTag);
            this.steps = compoundTag.getIntOr("Steps", 0);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putInt("Steps", this.steps);
        }

        public static BoundingBox findPieceBox(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction) {
            int n4 = 3;
            BoundingBox boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, 4, direction);
            StructurePiece structurePiece = structurePieceAccessor.findCollisionPiece(boundingBox);
            if (structurePiece == null) {
                return null;
            }
            if (structurePiece.getBoundingBox().minY() == boundingBox.minY()) {
                for (int i = 2; i >= 1; --i) {
                    boundingBox = BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, i, direction);
                    if (structurePiece.getBoundingBox().intersects(boundingBox)) continue;
                    return BoundingBox.orientBox(n, n2, n3, -1, -1, 0, 5, 5, i + 1, direction);
                }
            }
            return null;
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            for (int i = 0; i < this.steps; ++i) {
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, i, boundingBox);
                for (int j = 1; j <= 3; ++j) {
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, j, i, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 1, j, i, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 2, j, i, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.CAVE_AIR.defaultBlockState(), 3, j, i, boundingBox);
                    this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, j, i, boundingBox);
                }
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, i, boundingBox);
            }
        }
    }

    static class SmoothStoneSelector
    extends StructurePiece.BlockSelector {
        SmoothStoneSelector() {
        }

        @Override
        public void next(RandomSource randomSource, int n, int n2, int n3, boolean bl) {
            float f;
            this.next = bl ? ((f = randomSource.nextFloat()) < 0.2f ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : (f < 0.5f ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : (f < 0.55f ? Blocks.INFESTED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState()))) : Blocks.CAVE_AIR.defaultBlockState();
        }
    }

    public static abstract class Turn
    extends StrongholdPiece {
        protected static final int WIDTH = 5;
        protected static final int HEIGHT = 5;
        protected static final int DEPTH = 5;

        protected Turn(StructurePieceType structurePieceType, int n, BoundingBox boundingBox) {
            super(structurePieceType, n, boundingBox);
        }

        public Turn(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }
    }
}

