/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class WoodlandMansionPieces {
    public static void generateMansion(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, List<WoodlandMansionPiece> list, RandomSource randomSource) {
        MansionGrid mansionGrid = new MansionGrid(randomSource);
        MansionPiecePlacer mansionPiecePlacer = new MansionPiecePlacer(structureTemplateManager, randomSource);
        mansionPiecePlacer.createMansion(blockPos, rotation, list, mansionGrid);
    }

    static class MansionGrid {
        private static final int DEFAULT_SIZE = 11;
        private static final int CLEAR = 0;
        private static final int CORRIDOR = 1;
        private static final int ROOM = 2;
        private static final int START_ROOM = 3;
        private static final int TEST_ROOM = 4;
        private static final int BLOCKED = 5;
        private static final int ROOM_1x1 = 65536;
        private static final int ROOM_1x2 = 131072;
        private static final int ROOM_2x2 = 262144;
        private static final int ROOM_ORIGIN_FLAG = 0x100000;
        private static final int ROOM_DOOR_FLAG = 0x200000;
        private static final int ROOM_STAIRS_FLAG = 0x400000;
        private static final int ROOM_CORRIDOR_FLAG = 0x800000;
        private static final int ROOM_TYPE_MASK = 983040;
        private static final int ROOM_ID_MASK = 65535;
        private final RandomSource random;
        final SimpleGrid baseGrid;
        final SimpleGrid thirdFloorGrid;
        final SimpleGrid[] floorRooms;
        final int entranceX;
        final int entranceY;

        public MansionGrid(RandomSource randomSource) {
            this.random = randomSource;
            int n = 11;
            this.entranceX = 7;
            this.entranceY = 4;
            this.baseGrid = new SimpleGrid(11, 11, 5);
            this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
            this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
            this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
            this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
            this.baseGrid.set(0, 0, 11, 1, 5);
            this.baseGrid.set(0, 9, 11, 11, 5);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);
            while (this.cleanEdges(this.baseGrid)) {
            }
            this.floorRooms = new SimpleGrid[3];
            this.floorRooms[0] = new SimpleGrid(11, 11, 5);
            this.floorRooms[1] = new SimpleGrid(11, 11, 5);
            this.floorRooms[2] = new SimpleGrid(11, 11, 5);
            this.identifyRooms(this.baseGrid, this.floorRooms[0]);
            this.identifyRooms(this.baseGrid, this.floorRooms[1]);
            this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 0x800000);
            this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 0x800000);
            this.thirdFloorGrid = new SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
            this.setupThirdFloor();
            this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
        }

        public static boolean isHouse(SimpleGrid simpleGrid, int n, int n2) {
            int n3 = simpleGrid.get(n, n2);
            return n3 == 1 || n3 == 2 || n3 == 3 || n3 == 4;
        }

        public boolean isRoomId(SimpleGrid simpleGrid, int n, int n2, int n3, int n4) {
            return (this.floorRooms[n3].get(n, n2) & 0xFFFF) == n4;
        }

        @Nullable
        public Direction get1x2RoomDirection(SimpleGrid simpleGrid, int n, int n2, int n3, int n4) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (!this.isRoomId(simpleGrid, n + direction.getStepX(), n2 + direction.getStepZ(), n3, n4)) continue;
                return direction;
            }
            return null;
        }

        private void recursiveCorridor(SimpleGrid simpleGrid, int n, int n2, Direction direction, int n3) {
            Direction direction2;
            if (n3 <= 0) {
                return;
            }
            simpleGrid.set(n, n2, 1);
            simpleGrid.setif(n + direction.getStepX(), n2 + direction.getStepZ(), 0, 1);
            for (int i = 0; i < 8; ++i) {
                direction2 = Direction.from2DDataValue(this.random.nextInt(4));
                if (direction2 == direction.getOpposite() || direction2 == Direction.EAST && this.random.nextBoolean()) continue;
                int n4 = n + direction.getStepX();
                int n5 = n2 + direction.getStepZ();
                if (simpleGrid.get(n4 + direction2.getStepX(), n5 + direction2.getStepZ()) != 0 || simpleGrid.get(n4 + direction2.getStepX() * 2, n5 + direction2.getStepZ() * 2) != 0) continue;
                this.recursiveCorridor(simpleGrid, n + direction.getStepX() + direction2.getStepX(), n2 + direction.getStepZ() + direction2.getStepZ(), direction2, n3 - 1);
                break;
            }
            Direction direction3 = direction.getClockWise();
            direction2 = direction.getCounterClockWise();
            simpleGrid.setif(n + direction3.getStepX(), n2 + direction3.getStepZ(), 0, 2);
            simpleGrid.setif(n + direction2.getStepX(), n2 + direction2.getStepZ(), 0, 2);
            simpleGrid.setif(n + direction.getStepX() + direction3.getStepX(), n2 + direction.getStepZ() + direction3.getStepZ(), 0, 2);
            simpleGrid.setif(n + direction.getStepX() + direction2.getStepX(), n2 + direction.getStepZ() + direction2.getStepZ(), 0, 2);
            simpleGrid.setif(n + direction.getStepX() * 2, n2 + direction.getStepZ() * 2, 0, 2);
            simpleGrid.setif(n + direction3.getStepX() * 2, n2 + direction3.getStepZ() * 2, 0, 2);
            simpleGrid.setif(n + direction2.getStepX() * 2, n2 + direction2.getStepZ() * 2, 0, 2);
        }

        private boolean cleanEdges(SimpleGrid simpleGrid) {
            boolean bl = false;
            for (int i = 0; i < simpleGrid.height; ++i) {
                for (int j = 0; j < simpleGrid.width; ++j) {
                    if (simpleGrid.get(j, i) != 0) continue;
                    int n = 0;
                    n += MansionGrid.isHouse(simpleGrid, j + 1, i) ? 1 : 0;
                    n += MansionGrid.isHouse(simpleGrid, j - 1, i) ? 1 : 0;
                    n += MansionGrid.isHouse(simpleGrid, j, i + 1) ? 1 : 0;
                    if ((n += MansionGrid.isHouse(simpleGrid, j, i - 1) ? 1 : 0) >= 3) {
                        simpleGrid.set(j, i, 2);
                        bl = true;
                        continue;
                    }
                    if (n != 2) continue;
                    int n2 = 0;
                    n2 += MansionGrid.isHouse(simpleGrid, j + 1, i + 1) ? 1 : 0;
                    n2 += MansionGrid.isHouse(simpleGrid, j - 1, i + 1) ? 1 : 0;
                    n2 += MansionGrid.isHouse(simpleGrid, j + 1, i - 1) ? 1 : 0;
                    if ((n2 += MansionGrid.isHouse(simpleGrid, j - 1, i - 1) ? 1 : 0) > 1) continue;
                    simpleGrid.set(j, i, 2);
                    bl = true;
                }
            }
            return bl;
        }

        private void setupThirdFloor() {
            int n;
            int n2;
            ArrayList arrayList = Lists.newArrayList();
            SimpleGrid simpleGrid = this.floorRooms[1];
            for (int i = 0; i < this.thirdFloorGrid.height; ++i) {
                for (n2 = 0; n2 < this.thirdFloorGrid.width; ++n2) {
                    int n3 = simpleGrid.get(n2, i);
                    n = n3 & 0xF0000;
                    if (n != 131072 || (n3 & 0x200000) != 0x200000) continue;
                    arrayList.add(new Tuple<Integer, Integer>(n2, i));
                }
            }
            if (arrayList.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                return;
            }
            Tuple tuple = (Tuple)arrayList.get(this.random.nextInt(arrayList.size()));
            n2 = simpleGrid.get((Integer)tuple.getA(), (Integer)tuple.getB());
            simpleGrid.set((Integer)tuple.getA(), (Integer)tuple.getB(), n2 | 0x400000);
            Direction direction = this.get1x2RoomDirection(this.baseGrid, (Integer)tuple.getA(), (Integer)tuple.getB(), 1, n2 & 0xFFFF);
            n = (Integer)tuple.getA() + direction.getStepX();
            int n4 = (Integer)tuple.getB() + direction.getStepZ();
            for (int i = 0; i < this.thirdFloorGrid.height; ++i) {
                for (int j = 0; j < this.thirdFloorGrid.width; ++j) {
                    if (!MansionGrid.isHouse(this.baseGrid, j, i)) {
                        this.thirdFloorGrid.set(j, i, 5);
                        continue;
                    }
                    if (j == (Integer)tuple.getA() && i == (Integer)tuple.getB()) {
                        this.thirdFloorGrid.set(j, i, 3);
                        continue;
                    }
                    if (j != n || i != n4) continue;
                    this.thirdFloorGrid.set(j, i, 3);
                    this.floorRooms[2].set(j, i, 0x800000);
                }
            }
            ArrayList arrayList2 = Lists.newArrayList();
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                if (this.thirdFloorGrid.get(n + direction2.getStepX(), n4 + direction2.getStepZ()) != 0) continue;
                arrayList2.add(direction2);
            }
            if (arrayList2.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                simpleGrid.set((Integer)tuple.getA(), (Integer)tuple.getB(), n2);
                return;
            }
            Direction direction3 = (Direction)arrayList2.get(this.random.nextInt(arrayList2.size()));
            this.recursiveCorridor(this.thirdFloorGrid, n + direction3.getStepX(), n4 + direction3.getStepZ(), direction3, 4);
            while (this.cleanEdges(this.thirdFloorGrid)) {
            }
        }

        private void identifyRooms(SimpleGrid simpleGrid, SimpleGrid simpleGrid2) {
            int n;
            ObjectArrayList objectArrayList = new ObjectArrayList();
            for (n = 0; n < simpleGrid.height; ++n) {
                for (int i = 0; i < simpleGrid.width; ++i) {
                    if (simpleGrid.get(i, n) != 2) continue;
                    objectArrayList.add(new Tuple<Integer, Integer>(i, n));
                }
            }
            Util.shuffle(objectArrayList, this.random);
            n = 10;
            for (Tuple tuple : objectArrayList) {
                int n2;
                int n3 = (Integer)tuple.getA();
                if (simpleGrid2.get(n3, n2 = ((Integer)tuple.getB()).intValue()) != 0) continue;
                int n4 = n3;
                int n5 = n3;
                int n6 = n2;
                int n7 = n2;
                int n8 = 65536;
                if (simpleGrid2.get(n3 + 1, n2) == 0 && simpleGrid2.get(n3, n2 + 1) == 0 && simpleGrid2.get(n3 + 1, n2 + 1) == 0 && simpleGrid.get(n3 + 1, n2) == 2 && simpleGrid.get(n3, n2 + 1) == 2 && simpleGrid.get(n3 + 1, n2 + 1) == 2) {
                    ++n5;
                    ++n7;
                    n8 = 262144;
                } else if (simpleGrid2.get(n3 - 1, n2) == 0 && simpleGrid2.get(n3, n2 + 1) == 0 && simpleGrid2.get(n3 - 1, n2 + 1) == 0 && simpleGrid.get(n3 - 1, n2) == 2 && simpleGrid.get(n3, n2 + 1) == 2 && simpleGrid.get(n3 - 1, n2 + 1) == 2) {
                    --n4;
                    ++n7;
                    n8 = 262144;
                } else if (simpleGrid2.get(n3 - 1, n2) == 0 && simpleGrid2.get(n3, n2 - 1) == 0 && simpleGrid2.get(n3 - 1, n2 - 1) == 0 && simpleGrid.get(n3 - 1, n2) == 2 && simpleGrid.get(n3, n2 - 1) == 2 && simpleGrid.get(n3 - 1, n2 - 1) == 2) {
                    --n4;
                    --n6;
                    n8 = 262144;
                } else if (simpleGrid2.get(n3 + 1, n2) == 0 && simpleGrid.get(n3 + 1, n2) == 2) {
                    ++n5;
                    n8 = 131072;
                } else if (simpleGrid2.get(n3, n2 + 1) == 0 && simpleGrid.get(n3, n2 + 1) == 2) {
                    ++n7;
                    n8 = 131072;
                } else if (simpleGrid2.get(n3 - 1, n2) == 0 && simpleGrid.get(n3 - 1, n2) == 2) {
                    --n4;
                    n8 = 131072;
                } else if (simpleGrid2.get(n3, n2 - 1) == 0 && simpleGrid.get(n3, n2 - 1) == 2) {
                    --n6;
                    n8 = 131072;
                }
                int n9 = this.random.nextBoolean() ? n4 : n5;
                int n10 = this.random.nextBoolean() ? n6 : n7;
                int n11 = 0x200000;
                if (!simpleGrid.edgesTo(n9, n10, 1)) {
                    n9 = n9 == n4 ? n5 : n4;
                    int n12 = n10 = n10 == n6 ? n7 : n6;
                    if (!simpleGrid.edgesTo(n9, n10, 1)) {
                        int n13 = n10 = n10 == n6 ? n7 : n6;
                        if (!simpleGrid.edgesTo(n9, n10, 1)) {
                            n9 = n9 == n4 ? n5 : n4;
                            int n14 = n10 = n10 == n6 ? n7 : n6;
                            if (!simpleGrid.edgesTo(n9, n10, 1)) {
                                n11 = 0;
                                n9 = n4;
                                n10 = n6;
                            }
                        }
                    }
                }
                for (int i = n6; i <= n7; ++i) {
                    for (int j = n4; j <= n5; ++j) {
                        if (j == n9 && i == n10) {
                            simpleGrid2.set(j, i, 0x100000 | n11 | n8 | n);
                            continue;
                        }
                        simpleGrid2.set(j, i, n8 | n);
                    }
                }
                ++n;
            }
        }
    }

    static class MansionPiecePlacer {
        private final StructureTemplateManager structureTemplateManager;
        private final RandomSource random;
        private int startX;
        private int startY;

        public MansionPiecePlacer(StructureTemplateManager structureTemplateManager, RandomSource randomSource) {
            this.structureTemplateManager = structureTemplateManager;
            this.random = randomSource;
        }

        public void createMansion(BlockPos blockPos, Rotation rotation, List<WoodlandMansionPiece> list, MansionGrid mansionGrid) {
            int n;
            PlacementData placementData = new PlacementData();
            placementData.position = blockPos;
            placementData.rotation = rotation;
            placementData.wallType = "wall_flat";
            PlacementData placementData2 = new PlacementData();
            this.entrance(list, placementData);
            placementData2.position = placementData.position.above(8);
            placementData2.rotation = placementData.rotation;
            placementData2.wallType = "wall_window";
            if (!list.isEmpty()) {
                // empty if block
            }
            SimpleGrid simpleGrid = mansionGrid.baseGrid;
            SimpleGrid simpleGrid2 = mansionGrid.thirdFloorGrid;
            this.startX = mansionGrid.entranceX + 1;
            this.startY = mansionGrid.entranceY + 1;
            int n2 = mansionGrid.entranceX + 1;
            int n3 = mansionGrid.entranceY;
            this.traverseOuterWalls(list, placementData, simpleGrid, Direction.SOUTH, this.startX, this.startY, n2, n3);
            this.traverseOuterWalls(list, placementData2, simpleGrid, Direction.SOUTH, this.startX, this.startY, n2, n3);
            PlacementData placementData3 = new PlacementData();
            placementData3.position = placementData.position.above(19);
            placementData3.rotation = placementData.rotation;
            placementData3.wallType = "wall_window";
            boolean bl = false;
            for (int i = 0; i < simpleGrid2.height && !bl; ++i) {
                for (n = simpleGrid2.width - 1; n >= 0 && !bl; --n) {
                    if (!MansionGrid.isHouse(simpleGrid2, n, i)) continue;
                    placementData3.position = placementData3.position.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                    placementData3.position = placementData3.position.relative(rotation.rotate(Direction.EAST), (n - this.startX) * 8);
                    this.traverseWallPiece(list, placementData3);
                    this.traverseOuterWalls(list, placementData3, simpleGrid2, Direction.SOUTH, n, i, n, i);
                    bl = true;
                }
            }
            this.createRoof(list, blockPos.above(16), rotation, simpleGrid, simpleGrid2);
            this.createRoof(list, blockPos.above(27), rotation, simpleGrid2, null);
            if (!list.isEmpty()) {
                // empty if block
            }
            FloorRoomCollection[] floorRoomCollectionArray = new FloorRoomCollection[]{new FirstFloorRoomCollection(), new SecondFloorRoomCollection(), new ThirdFloorRoomCollection()};
            for (n = 0; n < 3; ++n) {
                Object object;
                BlockPos blockPos2 = blockPos.above(8 * n + (n == 2 ? 3 : 0));
                SimpleGrid simpleGrid3 = mansionGrid.floorRooms[n];
                SimpleGrid simpleGrid4 = n == 2 ? simpleGrid2 : simpleGrid;
                String string = n == 0 ? "carpet_south_1" : "carpet_south_2";
                String string2 = n == 0 ? "carpet_west_1" : "carpet_west_2";
                for (int i = 0; i < simpleGrid4.height; ++i) {
                    for (int j = 0; j < simpleGrid4.width; ++j) {
                        if (simpleGrid4.get(j, i) != 1) continue;
                        object = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                        object = ((BlockPos)object).relative(rotation.rotate(Direction.EAST), (j - this.startX) * 8);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "corridor_floor", (BlockPos)object, rotation));
                        if (simpleGrid4.get(j, i - 1) == 1 || (simpleGrid3.get(j, i - 1) & 0x800000) == 0x800000) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "carpet_north", ((BlockPos)object).relative(rotation.rotate(Direction.EAST), 1).above(), rotation));
                        }
                        if (simpleGrid4.get(j + 1, i) == 1 || (simpleGrid3.get(j + 1, i) & 0x800000) == 0x800000) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "carpet_east", ((BlockPos)object).relative(rotation.rotate(Direction.SOUTH), 1).relative(rotation.rotate(Direction.EAST), 5).above(), rotation));
                        }
                        if (simpleGrid4.get(j, i + 1) == 1 || (simpleGrid3.get(j, i + 1) & 0x800000) == 0x800000) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, string, ((BlockPos)object).relative(rotation.rotate(Direction.SOUTH), 5).relative(rotation.rotate(Direction.WEST), 1), rotation));
                        }
                        if (simpleGrid4.get(j - 1, i) != 1 && (simpleGrid3.get(j - 1, i) & 0x800000) != 0x800000) continue;
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, string2, ((BlockPos)object).relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.NORTH), 1), rotation));
                    }
                }
                String string3 = n == 0 ? "indoors_wall_1" : "indoors_wall_2";
                String string4 = n == 0 ? "indoors_door_1" : "indoors_door_2";
                object = Lists.newArrayList();
                for (int i = 0; i < simpleGrid4.height; ++i) {
                    for (int j = 0; j < simpleGrid4.width; ++j) {
                        Object object2;
                        Object object32;
                        boolean bl2;
                        boolean bl3 = bl2 = n == 2 && simpleGrid4.get(j, i) == 3;
                        if (simpleGrid4.get(j, i) != 2 && !bl2) continue;
                        int n4 = simpleGrid3.get(j, i);
                        int n5 = n4 & 0xF0000;
                        int n6 = n4 & 0xFFFF;
                        bl2 = bl2 && (n4 & 0x800000) == 0x800000;
                        object.clear();
                        if ((n4 & 0x200000) == 0x200000) {
                            for (Object object32 : Direction.Plane.HORIZONTAL) {
                                if (simpleGrid4.get(j + ((Direction)object32).getStepX(), i + ((Direction)object32).getStepZ()) != 1) continue;
                                object.add(object32);
                            }
                        }
                        Object object4 = null;
                        if (!object.isEmpty()) {
                            object4 = (Direction)object.get(this.random.nextInt(object.size()));
                        } else if ((n4 & 0x100000) == 0x100000) {
                            object4 = Direction.UP;
                        }
                        object32 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                        object32 = ((BlockPos)object32).relative(rotation.rotate(Direction.EAST), -1 + (j - this.startX) * 8);
                        if (MansionGrid.isHouse(simpleGrid4, j - 1, i) && !mansionGrid.isRoomId(simpleGrid4, j - 1, i, n, n6)) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, object4 == Direction.WEST ? string4 : string3, (BlockPos)object32, rotation));
                        }
                        if (simpleGrid4.get(j + 1, i) == 1 && !bl2) {
                            object2 = ((BlockPos)object32).relative(rotation.rotate(Direction.EAST), 8);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, object4 == Direction.EAST ? string4 : string3, (BlockPos)object2, rotation));
                        }
                        if (MansionGrid.isHouse(simpleGrid4, j, i + 1) && !mansionGrid.isRoomId(simpleGrid4, j, i + 1, n, n6)) {
                            object2 = ((BlockPos)object32).relative(rotation.rotate(Direction.SOUTH), 7);
                            object2 = ((BlockPos)object2).relative(rotation.rotate(Direction.EAST), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, object4 == Direction.SOUTH ? string4 : string3, (BlockPos)object2, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                        if (simpleGrid4.get(j, i - 1) == 1 && !bl2) {
                            object2 = ((BlockPos)object32).relative(rotation.rotate(Direction.NORTH), 1);
                            object2 = ((BlockPos)object2).relative(rotation.rotate(Direction.EAST), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, object4 == Direction.NORTH ? string4 : string3, (BlockPos)object2, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                        if (n5 == 65536) {
                            this.addRoom1x1(list, (BlockPos)object32, rotation, (Direction)object4, floorRoomCollectionArray[n]);
                            continue;
                        }
                        if (n5 == 131072 && object4 != null) {
                            object2 = mansionGrid.get1x2RoomDirection(simpleGrid4, j, i, n, n6);
                            boolean bl4 = (n4 & 0x400000) == 0x400000;
                            this.addRoom1x2(list, (BlockPos)object32, rotation, (Direction)object2, (Direction)object4, floorRoomCollectionArray[n], bl4);
                            continue;
                        }
                        if (n5 == 262144 && object4 != null && object4 != Direction.UP) {
                            object2 = ((Direction)object4).getClockWise();
                            if (!mansionGrid.isRoomId(simpleGrid4, j + ((Direction)object2).getStepX(), i + ((Direction)object2).getStepZ(), n, n6)) {
                                object2 = ((Direction)object2).getOpposite();
                            }
                            this.addRoom2x2(list, (BlockPos)object32, rotation, (Direction)object2, (Direction)object4, floorRoomCollectionArray[n]);
                            continue;
                        }
                        if (n5 != 262144 || object4 != Direction.UP) continue;
                        this.addRoom2x2Secret(list, (BlockPos)object32, rotation, floorRoomCollectionArray[n]);
                    }
                }
            }
        }

        private void traverseOuterWalls(List<WoodlandMansionPiece> list, PlacementData placementData, SimpleGrid simpleGrid, Direction direction, int n, int n2, int n3, int n4) {
            int n5 = n;
            int n6 = n2;
            Direction direction2 = direction;
            do {
                if (!MansionGrid.isHouse(simpleGrid, n5 + direction.getStepX(), n6 + direction.getStepZ())) {
                    this.traverseTurn(list, placementData);
                    direction = direction.getClockWise();
                    if (n5 == n3 && n6 == n4 && direction2 == direction) continue;
                    this.traverseWallPiece(list, placementData);
                    continue;
                }
                if (MansionGrid.isHouse(simpleGrid, n5 + direction.getStepX(), n6 + direction.getStepZ()) && MansionGrid.isHouse(simpleGrid, n5 + direction.getStepX() + direction.getCounterClockWise().getStepX(), n6 + direction.getStepZ() + direction.getCounterClockWise().getStepZ())) {
                    this.traverseInnerTurn(list, placementData);
                    n5 += direction.getStepX();
                    n6 += direction.getStepZ();
                    direction = direction.getCounterClockWise();
                    continue;
                }
                if ((n5 += direction.getStepX()) == n3 && (n6 += direction.getStepZ()) == n4 && direction2 == direction) continue;
                this.traverseWallPiece(list, placementData);
            } while (n5 != n3 || n6 != n4 || direction2 != direction);
        }

        private void createRoof(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, SimpleGrid simpleGrid, @Nullable SimpleGrid simpleGrid2) {
            BlockPos blockPos2;
            boolean bl;
            BlockPos blockPos3;
            int n;
            int n2;
            for (n2 = 0; n2 < simpleGrid.height; ++n2) {
                for (n = 0; n < simpleGrid.width; ++n) {
                    blockPos3 = blockPos;
                    blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 8 + (n2 - this.startY) * 8);
                    blockPos3 = blockPos3.relative(rotation.rotate(Direction.EAST), (n - this.startX) * 8);
                    boolean bl2 = bl = simpleGrid2 != null && MansionGrid.isHouse(simpleGrid2, n, n2);
                    if (!MansionGrid.isHouse(simpleGrid, n, n2) || bl) continue;
                    list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof", blockPos3.above(3), rotation));
                    if (!MansionGrid.isHouse(simpleGrid, n + 1, n2)) {
                        blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 6);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos2, rotation));
                    }
                    if (!MansionGrid.isHouse(simpleGrid, n - 1, n2)) {
                        blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 0);
                        blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 7);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_180)));
                    }
                    if (!MansionGrid.isHouse(simpleGrid, n, n2 - 1)) {
                        blockPos2 = blockPos3.relative(rotation.rotate(Direction.WEST), 1);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos2, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                    }
                    if (MansionGrid.isHouse(simpleGrid, n, n2 + 1)) continue;
                    blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 6);
                    blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                    list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90)));
                }
            }
            if (simpleGrid2 != null) {
                for (n2 = 0; n2 < simpleGrid.height; ++n2) {
                    for (n = 0; n < simpleGrid.width; ++n) {
                        blockPos3 = blockPos;
                        blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 8 + (n2 - this.startY) * 8);
                        blockPos3 = blockPos3.relative(rotation.rotate(Direction.EAST), (n - this.startX) * 8);
                        bl = MansionGrid.isHouse(simpleGrid2, n, n2);
                        if (!MansionGrid.isHouse(simpleGrid, n, n2) || !bl) continue;
                        if (!MansionGrid.isHouse(simpleGrid, n + 1, n2)) {
                            blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos2, rotation));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, n - 1, n2)) {
                            blockPos2 = blockPos3.relative(rotation.rotate(Direction.WEST), 1);
                            blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_180)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, n, n2 - 1)) {
                            blockPos2 = blockPos3.relative(rotation.rotate(Direction.WEST), 0);
                            blockPos2 = blockPos2.relative(rotation.rotate(Direction.NORTH), 1);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos2, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, n, n2 + 1)) {
                            blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 6);
                            blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, n + 1, n2)) {
                            if (!MansionGrid.isHouse(simpleGrid, n, n2 - 1)) {
                                blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 7);
                                blockPos2 = blockPos2.relative(rotation.rotate(Direction.NORTH), 2);
                                list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos2, rotation));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, n, n2 + 1)) {
                                blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 8);
                                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 7);
                                list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90)));
                            }
                        }
                        if (MansionGrid.isHouse(simpleGrid, n - 1, n2)) continue;
                        if (!MansionGrid.isHouse(simpleGrid, n, n2 - 1)) {
                            blockPos2 = blockPos3.relative(rotation.rotate(Direction.WEST), 2);
                            blockPos2 = blockPos2.relative(rotation.rotate(Direction.NORTH), 1);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos2, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }
                        if (MansionGrid.isHouse(simpleGrid, n, n2 + 1)) continue;
                        blockPos2 = blockPos3.relative(rotation.rotate(Direction.WEST), 1);
                        blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_180)));
                    }
                }
            }
            for (n2 = 0; n2 < simpleGrid.height; ++n2) {
                for (n = 0; n < simpleGrid.width; ++n) {
                    BlockPos blockPos4;
                    blockPos3 = blockPos;
                    blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 8 + (n2 - this.startY) * 8);
                    blockPos3 = blockPos3.relative(rotation.rotate(Direction.EAST), (n - this.startX) * 8);
                    boolean bl3 = bl = simpleGrid2 != null && MansionGrid.isHouse(simpleGrid2, n, n2);
                    if (!MansionGrid.isHouse(simpleGrid, n, n2) || bl) continue;
                    if (!MansionGrid.isHouse(simpleGrid, n + 1, n2)) {
                        blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 6);
                        if (!MansionGrid.isHouse(simpleGrid, n, n2 + 1)) {
                            blockPos4 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos4, rotation));
                        } else if (MansionGrid.isHouse(simpleGrid, n + 1, n2 + 1)) {
                            blockPos4 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 5);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, n, n2 - 1)) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos2, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        } else if (MansionGrid.isHouse(simpleGrid, n + 1, n2 - 1)) {
                            blockPos4 = blockPos3.relative(rotation.rotate(Direction.EAST), 9);
                            blockPos4 = blockPos4.relative(rotation.rotate(Direction.NORTH), 2);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                    }
                    if (MansionGrid.isHouse(simpleGrid, n - 1, n2)) continue;
                    blockPos2 = blockPos3.relative(rotation.rotate(Direction.EAST), 0);
                    blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 0);
                    if (!MansionGrid.isHouse(simpleGrid, n, n2 + 1)) {
                        blockPos4 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos4, rotation.getRotated(Rotation.CLOCKWISE_90)));
                    } else if (MansionGrid.isHouse(simpleGrid, n - 1, n2 + 1)) {
                        blockPos4 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8);
                        blockPos4 = blockPos4.relative(rotation.rotate(Direction.WEST), 3);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                    }
                    if (!MansionGrid.isHouse(simpleGrid, n, n2 - 1)) {
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos2, rotation.getRotated(Rotation.CLOCKWISE_180)));
                        continue;
                    }
                    if (!MansionGrid.isHouse(simpleGrid, n - 1, n2 - 1)) continue;
                    blockPos4 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 1);
                    list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation.getRotated(Rotation.CLOCKWISE_180)));
                }
            }
        }

        private void entrance(List<WoodlandMansionPiece> list, PlacementData placementData) {
            Direction direction = placementData.rotation.rotate(Direction.WEST);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "entrance", placementData.position.relative(direction, 9), placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 16);
        }

        private void traverseWallPiece(List<WoodlandMansionPiece> list, PlacementData placementData) {
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, placementData.wallType, placementData.position.relative(placementData.rotation.rotate(Direction.EAST), 7), placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 8);
        }

        private void traverseTurn(List<WoodlandMansionPiece> list, PlacementData placementData) {
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), -1);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "wall_corner", placementData.position, placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), -7);
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.WEST), -6);
            placementData.rotation = placementData.rotation.getRotated(Rotation.CLOCKWISE_90);
        }

        private void traverseInnerTurn(List<WoodlandMansionPiece> list, PlacementData placementData) {
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 6);
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.EAST), 8);
            placementData.rotation = placementData.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
        }

        private void addRoom1x1(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, FloorRoomCollection floorRoomCollection) {
            Rotation rotation2 = Rotation.NONE;
            String string = floorRoomCollection.get1x1(this.random);
            if (direction != Direction.EAST) {
                if (direction == Direction.NORTH) {
                    rotation2 = rotation2.getRotated(Rotation.COUNTERCLOCKWISE_90);
                } else if (direction == Direction.WEST) {
                    rotation2 = rotation2.getRotated(Rotation.CLOCKWISE_180);
                } else if (direction == Direction.SOUTH) {
                    rotation2 = rotation2.getRotated(Rotation.CLOCKWISE_90);
                } else {
                    string = floorRoomCollection.get1x1Secret(this.random);
                }
            }
            BlockPos blockPos2 = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation2, 7, 7);
            rotation2 = rotation2.getRotated(rotation);
            blockPos2 = blockPos2.rotate(rotation);
            BlockPos blockPos3 = blockPos.offset(blockPos2.getX(), 0, blockPos2.getZ());
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, string, blockPos3, rotation2));
        }

        private void addRoom1x2(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, Direction direction2, FloorRoomCollection floorRoomCollection, boolean bl) {
            if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation));
            } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
                BlockPos blockPos3 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos3, rotation, Mirror.LEFT_RIGHT));
            } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
                BlockPos blockPos4 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                blockPos4 = blockPos4.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos4, rotation.getRotated(Rotation.CLOCKWISE_180)));
            } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
                BlockPos blockPos5 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos5, rotation, Mirror.FRONT_BACK));
            } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
                BlockPos blockPos6 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos6, rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
            } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
                BlockPos blockPos7 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos7, rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
                BlockPos blockPos8 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                blockPos8 = blockPos8.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos8, rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
            } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
                BlockPos blockPos9 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos9 = blockPos9.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos9, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
            } else if (direction2 == Direction.SOUTH && direction == Direction.NORTH) {
                BlockPos blockPos10 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos10 = blockPos10.relative(rotation.rotate(Direction.NORTH), 8);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos10, rotation));
            } else if (direction2 == Direction.NORTH && direction == Direction.SOUTH) {
                BlockPos blockPos11 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                blockPos11 = blockPos11.relative(rotation.rotate(Direction.SOUTH), 14);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos11, rotation.getRotated(Rotation.CLOCKWISE_180)));
            } else if (direction2 == Direction.WEST && direction == Direction.EAST) {
                BlockPos blockPos12 = blockPos.relative(rotation.rotate(Direction.EAST), 15);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos12, rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.EAST && direction == Direction.WEST) {
                BlockPos blockPos13 = blockPos.relative(rotation.rotate(Direction.WEST), 7);
                blockPos13 = blockPos13.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos13, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
            } else if (direction2 == Direction.UP && direction == Direction.EAST) {
                BlockPos blockPos14 = blockPos.relative(rotation.rotate(Direction.EAST), 15);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2Secret(this.random), blockPos14, rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.UP && direction == Direction.SOUTH) {
                BlockPos blockPos15 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos15 = blockPos15.relative(rotation.rotate(Direction.NORTH), 0);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2Secret(this.random), blockPos15, rotation));
            }
        }

        private void addRoom2x2(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, Direction direction2, FloorRoomCollection floorRoomCollection) {
            int n = 0;
            int n2 = 0;
            Rotation rotation2 = rotation;
            Mirror mirror = Mirror.NONE;
            if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
                n = -7;
            } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
                n = -7;
                n2 = 6;
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
                n = 1;
                n2 = 14;
                rotation2 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
                n = 7;
                n2 = 14;
                rotation2 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
                n = 7;
                n2 = -8;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_90);
            } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
                n = 1;
                n2 = -8;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
                n = 15;
                n2 = 6;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_180);
            } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
                n = 15;
                mirror = Mirror.FRONT_BACK;
            }
            BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), n);
            blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), n2);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get2x2(this.random), blockPos2, rotation2, mirror));
        }

        private void addRoom2x2Secret(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, FloorRoomCollection floorRoomCollection) {
            BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get2x2Secret(this.random), blockPos2, rotation, Mirror.NONE));
        }
    }

    static class ThirdFloorRoomCollection
    extends SecondFloorRoomCollection {
        ThirdFloorRoomCollection() {
        }
    }

    static class SecondFloorRoomCollection
    extends FloorRoomCollection {
        SecondFloorRoomCollection() {
        }

        @Override
        public String get1x1(RandomSource randomSource) {
            return "1x1_b" + (randomSource.nextInt(5) + 1);
        }

        @Override
        public String get1x1Secret(RandomSource randomSource) {
            return "1x1_as" + (randomSource.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(RandomSource randomSource, boolean bl) {
            if (bl) {
                return "1x2_c_stairs";
            }
            return "1x2_c" + (randomSource.nextInt(4) + 1);
        }

        @Override
        public String get1x2FrontEntrance(RandomSource randomSource, boolean bl) {
            if (bl) {
                return "1x2_d_stairs";
            }
            return "1x2_d" + (randomSource.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(RandomSource randomSource) {
            return "1x2_se" + (randomSource.nextInt(1) + 1);
        }

        @Override
        public String get2x2(RandomSource randomSource) {
            return "2x2_b" + (randomSource.nextInt(5) + 1);
        }

        @Override
        public String get2x2Secret(RandomSource randomSource) {
            return "2x2_s1";
        }
    }

    static class FirstFloorRoomCollection
    extends FloorRoomCollection {
        FirstFloorRoomCollection() {
        }

        @Override
        public String get1x1(RandomSource randomSource) {
            return "1x1_a" + (randomSource.nextInt(5) + 1);
        }

        @Override
        public String get1x1Secret(RandomSource randomSource) {
            return "1x1_as" + (randomSource.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(RandomSource randomSource, boolean bl) {
            return "1x2_a" + (randomSource.nextInt(9) + 1);
        }

        @Override
        public String get1x2FrontEntrance(RandomSource randomSource, boolean bl) {
            return "1x2_b" + (randomSource.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(RandomSource randomSource) {
            return "1x2_s" + (randomSource.nextInt(2) + 1);
        }

        @Override
        public String get2x2(RandomSource randomSource) {
            return "2x2_a" + (randomSource.nextInt(4) + 1);
        }

        @Override
        public String get2x2Secret(RandomSource randomSource) {
            return "2x2_s1";
        }
    }

    static abstract class FloorRoomCollection {
        FloorRoomCollection() {
        }

        public abstract String get1x1(RandomSource var1);

        public abstract String get1x1Secret(RandomSource var1);

        public abstract String get1x2SideEntrance(RandomSource var1, boolean var2);

        public abstract String get1x2FrontEntrance(RandomSource var1, boolean var2);

        public abstract String get1x2Secret(RandomSource var1);

        public abstract String get2x2(RandomSource var1);

        public abstract String get2x2Secret(RandomSource var1);
    }

    static class SimpleGrid {
        private final int[][] grid;
        final int width;
        final int height;
        private final int valueIfOutside;

        public SimpleGrid(int n, int n2, int n3) {
            this.width = n;
            this.height = n2;
            this.valueIfOutside = n3;
            this.grid = new int[n][n2];
        }

        public void set(int n, int n2, int n3) {
            if (n >= 0 && n < this.width && n2 >= 0 && n2 < this.height) {
                this.grid[n][n2] = n3;
            }
        }

        public void set(int n, int n2, int n3, int n4, int n5) {
            for (int i = n2; i <= n4; ++i) {
                for (int j = n; j <= n3; ++j) {
                    this.set(j, i, n5);
                }
            }
        }

        public int get(int n, int n2) {
            if (n >= 0 && n < this.width && n2 >= 0 && n2 < this.height) {
                return this.grid[n][n2];
            }
            return this.valueIfOutside;
        }

        public void setif(int n, int n2, int n3, int n4) {
            if (this.get(n, n2) == n3) {
                this.set(n, n2, n4);
            }
        }

        public boolean edgesTo(int n, int n2, int n3) {
            return this.get(n - 1, n2) == n3 || this.get(n + 1, n2) == n3 || this.get(n, n2 + 1) == n3 || this.get(n, n2 - 1) == n3;
        }
    }

    static class PlacementData {
        public Rotation rotation;
        public BlockPos position;
        public String wallType;

        PlacementData() {
        }
    }

    public static class WoodlandMansionPiece
    extends TemplateStructurePiece {
        public WoodlandMansionPiece(StructureTemplateManager structureTemplateManager, String string, BlockPos blockPos, Rotation rotation) {
            this(structureTemplateManager, string, blockPos, rotation, Mirror.NONE);
        }

        public WoodlandMansionPiece(StructureTemplateManager structureTemplateManager, String string, BlockPos blockPos, Rotation rotation, Mirror mirror) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, 0, structureTemplateManager, WoodlandMansionPiece.makeLocation(string), string, WoodlandMansionPiece.makeSettings(mirror, rotation), blockPos);
        }

        public WoodlandMansionPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, compoundTag, structureTemplateManager, (ResourceLocation resourceLocation) -> WoodlandMansionPiece.makeSettings(compoundTag.read("Mi", Mirror.LEGACY_CODEC).orElseThrow(), compoundTag.read("Rot", Rotation.LEGACY_CODEC).orElseThrow()));
        }

        @Override
        protected ResourceLocation makeTemplateLocation() {
            return WoodlandMansionPiece.makeLocation(this.templateName);
        }

        private static ResourceLocation makeLocation(String string) {
            return ResourceLocation.withDefaultNamespace("woodland_mansion/" + string);
        }

        private static StructurePlaceSettings makeSettings(Mirror mirror, Rotation rotation) {
            return new StructurePlaceSettings().setIgnoreEntities(true).setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
            compoundTag.store("Mi", Mirror.LEGACY_CODEC, this.placeSettings.getMirror());
        }

        @Override
        protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
            if (string.startsWith("Chest")) {
                Rotation rotation = this.placeSettings.getRotation();
                BlockState blockState = Blocks.CHEST.defaultBlockState();
                if ("ChestWest".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.WEST));
                } else if ("ChestEast".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.EAST));
                } else if ("ChestSouth".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.SOUTH));
                } else if ("ChestNorth".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.NORTH));
                }
                this.createChest(serverLevelAccessor, boundingBox, randomSource, blockPos, BuiltInLootTables.WOODLAND_MANSION, blockState);
            } else {
                ArrayList<Mob> arrayList = new ArrayList<Mob>();
                switch (string) {
                    case "Mage": {
                        arrayList.add(EntityType.EVOKER.create(serverLevelAccessor.getLevel(), EntitySpawnReason.STRUCTURE));
                        break;
                    }
                    case "Warrior": {
                        arrayList.add(EntityType.VINDICATOR.create(serverLevelAccessor.getLevel(), EntitySpawnReason.STRUCTURE));
                        break;
                    }
                    case "Group of Allays": {
                        int n = serverLevelAccessor.getRandom().nextInt(3) + 1;
                        for (int i = 0; i < n; ++i) {
                            arrayList.add(EntityType.ALLAY.create(serverLevelAccessor.getLevel(), EntitySpawnReason.STRUCTURE));
                        }
                        break;
                    }
                    default: {
                        return;
                    }
                }
                for (Mob mob : arrayList) {
                    if (mob == null) continue;
                    mob.setPersistenceRequired();
                    mob.snapTo(blockPos, 0.0f, 0.0f);
                    mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.STRUCTURE, null);
                    serverLevelAccessor.addFreshEntityWithPassengers(mob);
                    serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }
}

