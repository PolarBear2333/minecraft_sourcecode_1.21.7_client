/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.google.common.math.DoubleMath
 *  com.google.common.math.IntMath
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CubePointRange;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteCubeMerger;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.IdenticalMerger;
import net.minecraft.world.phys.shapes.IndexMerger;
import net.minecraft.world.phys.shapes.IndirectMerger;
import net.minecraft.world.phys.shapes.NonOverlappingMerger;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Shapes {
    public static final double EPSILON = 1.0E-7;
    public static final double BIG_EPSILON = 1.0E-6;
    private static final VoxelShape BLOCK = Util.make(() -> {
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(1, 1, 1);
        ((DiscreteVoxelShape)bitSetDiscreteVoxelShape).fill(0, 0, 0);
        return new CubeVoxelShape(bitSetDiscreteVoxelShape);
    });
    private static final Vec3 BLOCK_CENTER = new Vec3(0.5, 0.5, 0.5);
    public static final VoxelShape INFINITY = Shapes.box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private static final VoxelShape EMPTY = new ArrayVoxelShape((DiscreteVoxelShape)new BitSetDiscreteVoxelShape(0, 0, 0), (DoubleList)new DoubleArrayList(new double[]{0.0}), (DoubleList)new DoubleArrayList(new double[]{0.0}), (DoubleList)new DoubleArrayList(new double[]{0.0}));

    public static VoxelShape empty() {
        return EMPTY;
    }

    public static VoxelShape block() {
        return BLOCK;
    }

    public static VoxelShape box(double d, double d2, double d3, double d4, double d5, double d6) {
        if (d > d4 || d2 > d5 || d3 > d6) {
            throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
        }
        return Shapes.create(d, d2, d3, d4, d5, d6);
    }

    public static VoxelShape create(double d, double d2, double d3, double d4, double d5, double d6) {
        if (d4 - d < 1.0E-7 || d5 - d2 < 1.0E-7 || d6 - d3 < 1.0E-7) {
            return Shapes.empty();
        }
        int n = Shapes.findBits(d, d4);
        int n2 = Shapes.findBits(d2, d5);
        int n3 = Shapes.findBits(d3, d6);
        if (n < 0 || n2 < 0 || n3 < 0) {
            return new ArrayVoxelShape(Shapes.BLOCK.shape, (DoubleList)DoubleArrayList.wrap((double[])new double[]{d, d4}), (DoubleList)DoubleArrayList.wrap((double[])new double[]{d2, d5}), (DoubleList)DoubleArrayList.wrap((double[])new double[]{d3, d6}));
        }
        if (n == 0 && n2 == 0 && n3 == 0) {
            return Shapes.block();
        }
        int n4 = 1 << n;
        int n5 = 1 << n2;
        int n6 = 1 << n3;
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.withFilledBounds(n4, n5, n6, (int)Math.round(d * (double)n4), (int)Math.round(d2 * (double)n5), (int)Math.round(d3 * (double)n6), (int)Math.round(d4 * (double)n4), (int)Math.round(d5 * (double)n5), (int)Math.round(d6 * (double)n6));
        return new CubeVoxelShape(bitSetDiscreteVoxelShape);
    }

    public static VoxelShape create(AABB aABB) {
        return Shapes.create(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
    }

    @VisibleForTesting
    protected static int findBits(double d, double d2) {
        if (d < -1.0E-7 || d2 > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            boolean bl;
            int n = 1 << i;
            double d3 = d * (double)n;
            double d4 = d2 * (double)n;
            boolean bl2 = Math.abs(d3 - (double)Math.round(d3)) < 1.0E-7 * (double)n;
            boolean bl3 = bl = Math.abs(d4 - (double)Math.round(d4)) < 1.0E-7 * (double)n;
            if (!bl2 || !bl) continue;
            return i;
        }
        return -1;
    }

    protected static long lcm(int n, int n2) {
        return (long)n * (long)(n2 / IntMath.gcd((int)n, (int)n2));
    }

    public static VoxelShape or(VoxelShape voxelShape, VoxelShape voxelShape2) {
        return Shapes.join(voxelShape, voxelShape2, BooleanOp.OR);
    }

    public static VoxelShape or(VoxelShape voxelShape, VoxelShape ... voxelShapeArray) {
        return Arrays.stream(voxelShapeArray).reduce(voxelShape, Shapes::or);
    }

    public static VoxelShape join(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        return Shapes.joinUnoptimized(voxelShape, voxelShape2, booleanOp).optimize();
    }

    public static VoxelShape joinUnoptimized(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        if (booleanOp.apply(false, false)) {
            throw Util.pauseInIde(new IllegalArgumentException());
        }
        if (voxelShape == voxelShape2) {
            return booleanOp.apply(true, true) ? voxelShape : Shapes.empty();
        }
        boolean bl = booleanOp.apply(true, false);
        boolean bl2 = booleanOp.apply(false, true);
        if (voxelShape.isEmpty()) {
            return bl2 ? voxelShape2 : Shapes.empty();
        }
        if (voxelShape2.isEmpty()) {
            return bl ? voxelShape : Shapes.empty();
        }
        IndexMerger indexMerger = Shapes.createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl, bl2);
        IndexMerger indexMerger2 = Shapes.createIndexMerger(indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl, bl2);
        IndexMerger indexMerger3 = Shapes.createIndexMerger((indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl, bl2);
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.join(voxelShape.shape, voxelShape2.shape, indexMerger, indexMerger2, indexMerger3, booleanOp);
        if (indexMerger instanceof DiscreteCubeMerger && indexMerger2 instanceof DiscreteCubeMerger && indexMerger3 instanceof DiscreteCubeMerger) {
            return new CubeVoxelShape(bitSetDiscreteVoxelShape);
        }
        return new ArrayVoxelShape((DiscreteVoxelShape)bitSetDiscreteVoxelShape, indexMerger.getList(), indexMerger2.getList(), indexMerger3.getList());
    }

    public static boolean joinIsNotEmpty(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        if (booleanOp.apply(false, false)) {
            throw Util.pauseInIde(new IllegalArgumentException());
        }
        boolean bl = voxelShape.isEmpty();
        boolean bl2 = voxelShape2.isEmpty();
        if (bl || bl2) {
            return booleanOp.apply(!bl, !bl2);
        }
        if (voxelShape == voxelShape2) {
            return booleanOp.apply(true, true);
        }
        boolean bl3 = booleanOp.apply(true, false);
        boolean bl4 = booleanOp.apply(false, true);
        for (Direction.Axis axis : AxisCycle.AXIS_VALUES) {
            if (voxelShape.max(axis) < voxelShape2.min(axis) - 1.0E-7) {
                return bl3 || bl4;
            }
            if (!(voxelShape2.max(axis) < voxelShape.min(axis) - 1.0E-7)) continue;
            return bl3 || bl4;
        }
        IndexMerger indexMerger = Shapes.createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl3, bl4);
        IndexMerger indexMerger2 = Shapes.createIndexMerger(indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl3, bl4);
        IndexMerger indexMerger3 = Shapes.createIndexMerger((indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl3, bl4);
        return Shapes.joinIsNotEmpty(indexMerger, indexMerger2, indexMerger3, voxelShape.shape, voxelShape2.shape, booleanOp);
    }

    private static boolean joinIsNotEmpty(IndexMerger indexMerger, IndexMerger indexMerger2, IndexMerger indexMerger3, DiscreteVoxelShape discreteVoxelShape, DiscreteVoxelShape discreteVoxelShape2, BooleanOp booleanOp) {
        return !indexMerger.forMergedIndexes((n, n2, n5) -> indexMerger2.forMergedIndexes((n3, n4, n8) -> indexMerger3.forMergedIndexes((n5, n6, n7) -> !booleanOp.apply(discreteVoxelShape.isFullWide(n, n3, n5), discreteVoxelShape2.isFullWide(n2, n4, n6)))));
    }

    public static double collide(Direction.Axis axis, AABB aABB, Iterable<VoxelShape> iterable, double d) {
        for (VoxelShape voxelShape : iterable) {
            if (Math.abs(d) < 1.0E-7) {
                return 0.0;
            }
            d = voxelShape.collide(axis, aABB, d);
        }
        return d;
    }

    public static boolean blockOccludes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
        if (voxelShape == Shapes.block() && voxelShape2 == Shapes.block()) {
            return true;
        }
        if (voxelShape2.isEmpty()) {
            return false;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
        VoxelShape voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
        BooleanOp booleanOp = axisDirection == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
        return DoubleMath.fuzzyEquals((double)voxelShape3.max(axis), (double)1.0, (double)1.0E-7) && DoubleMath.fuzzyEquals((double)voxelShape4.min(axis), (double)0.0, (double)1.0E-7) && !Shapes.joinIsNotEmpty(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), booleanOp);
    }

    public static boolean mergedFaceOccludes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
        VoxelShape voxelShape3;
        if (voxelShape == Shapes.block() || voxelShape2 == Shapes.block()) {
            return true;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        VoxelShape voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
        VoxelShape voxelShape5 = voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
        if (!DoubleMath.fuzzyEquals((double)voxelShape4.max(axis), (double)1.0, (double)1.0E-7)) {
            voxelShape4 = Shapes.empty();
        }
        if (!DoubleMath.fuzzyEquals((double)voxelShape3.min(axis), (double)0.0, (double)1.0E-7)) {
            voxelShape3 = Shapes.empty();
        }
        return !Shapes.joinIsNotEmpty(Shapes.block(), Shapes.joinUnoptimized(new SliceShape(voxelShape4, axis, voxelShape4.shape.getSize(axis) - 1), new SliceShape(voxelShape3, axis, 0), BooleanOp.OR), BooleanOp.ONLY_FIRST);
    }

    public static boolean faceShapeOccludes(VoxelShape voxelShape, VoxelShape voxelShape2) {
        if (voxelShape == Shapes.block() || voxelShape2 == Shapes.block()) {
            return true;
        }
        if (voxelShape.isEmpty() && voxelShape2.isEmpty()) {
            return false;
        }
        return !Shapes.joinIsNotEmpty(Shapes.block(), Shapes.joinUnoptimized(voxelShape, voxelShape2, BooleanOp.OR), BooleanOp.ONLY_FIRST);
    }

    @VisibleForTesting
    protected static IndexMerger createIndexMerger(int n, DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
        long l;
        int n2 = doubleList.size() - 1;
        int n3 = doubleList2.size() - 1;
        if (doubleList instanceof CubePointRange && doubleList2 instanceof CubePointRange && (long)n * (l = Shapes.lcm(n2, n3)) <= 256L) {
            return new DiscreteCubeMerger(n2, n3);
        }
        if (doubleList.getDouble(n2) < doubleList2.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(doubleList, doubleList2, false);
        }
        if (doubleList2.getDouble(n3) < doubleList.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(doubleList2, doubleList, true);
        }
        if (n2 == n3 && Objects.equals(doubleList, doubleList2)) {
            return new IdenticalMerger(doubleList);
        }
        return new IndirectMerger(doubleList, doubleList2, bl, bl2);
    }

    public static VoxelShape rotate(VoxelShape voxelShape, OctahedralGroup octahedralGroup) {
        return Shapes.rotate(voxelShape, octahedralGroup, BLOCK_CENTER);
    }

    public static VoxelShape rotate(VoxelShape voxelShape, OctahedralGroup octahedralGroup, Vec3 vec3) {
        if (octahedralGroup == OctahedralGroup.IDENTITY) {
            return voxelShape;
        }
        DiscreteVoxelShape discreteVoxelShape = voxelShape.shape.rotate(octahedralGroup);
        if (voxelShape instanceof CubeVoxelShape && BLOCK_CENTER.equals(vec3)) {
            return new CubeVoxelShape(discreteVoxelShape);
        }
        Direction.Axis axis = octahedralGroup.permute(Direction.Axis.X);
        Direction.Axis axis2 = octahedralGroup.permute(Direction.Axis.Y);
        Direction.Axis axis3 = octahedralGroup.permute(Direction.Axis.Z);
        DoubleList doubleList = voxelShape.getCoords(axis);
        DoubleList doubleList2 = voxelShape.getCoords(axis2);
        DoubleList doubleList3 = voxelShape.getCoords(axis3);
        boolean bl = octahedralGroup.inverts(axis);
        boolean bl2 = octahedralGroup.inverts(axis2);
        boolean bl3 = octahedralGroup.inverts(axis3);
        boolean bl4 = axis.choose(bl, bl2, bl3);
        boolean bl5 = axis2.choose(bl, bl2, bl3);
        boolean bl6 = axis3.choose(bl, bl2, bl3);
        return new ArrayVoxelShape(discreteVoxelShape, Shapes.makeAxis(doubleList, bl4, vec3.get(axis), vec3.x), Shapes.makeAxis(doubleList2, bl5, vec3.get(axis2), vec3.y), Shapes.makeAxis(doubleList3, bl6, vec3.get(axis3), vec3.z));
    }

    @VisibleForTesting
    static DoubleList makeAxis(DoubleList doubleList, boolean bl, double d, double d2) {
        int n;
        if (!bl && d == d2) {
            return doubleList;
        }
        int n2 = doubleList.size();
        DoubleArrayList doubleArrayList = new DoubleArrayList(n2);
        int n3 = bl ? -1 : 1;
        int n4 = n = bl ? n2 - 1 : 0;
        while (n >= 0 && n < n2) {
            doubleArrayList.add(d2 + (double)n3 * (doubleList.getDouble(n) - d));
            n += n3;
        }
        return doubleArrayList;
    }

    public static boolean equal(VoxelShape voxelShape, VoxelShape voxelShape2) {
        return !Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.NOT_SAME);
    }

    public static Map<Direction.Axis, VoxelShape> rotateHorizontalAxis(VoxelShape voxelShape) {
        return Shapes.rotateHorizontalAxis(voxelShape, BLOCK_CENTER);
    }

    public static Map<Direction.Axis, VoxelShape> rotateHorizontalAxis(VoxelShape voxelShape, Vec3 vec3) {
        return Maps.newEnumMap(Map.of(Direction.Axis.Z, voxelShape, Direction.Axis.X, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R90), vec3)));
    }

    public static Map<Direction.Axis, VoxelShape> rotateAllAxis(VoxelShape voxelShape) {
        return Shapes.rotateAllAxis(voxelShape, BLOCK_CENTER);
    }

    public static Map<Direction.Axis, VoxelShape> rotateAllAxis(VoxelShape voxelShape, Vec3 vec3) {
        return Maps.newEnumMap(Map.of(Direction.Axis.Z, voxelShape, Direction.Axis.X, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R90), vec3), Direction.Axis.Y, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R90, Quadrant.R0), vec3)));
    }

    public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape voxelShape) {
        return Shapes.rotateHorizontal(voxelShape, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape voxelShape, Vec3 vec3) {
        return Maps.newEnumMap(Map.of(Direction.NORTH, voxelShape, Direction.EAST, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R90), vec3), Direction.SOUTH, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R180), vec3), Direction.WEST, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R270), vec3)));
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape voxelShape) {
        return Shapes.rotateAll(voxelShape, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape voxelShape, Vec3 vec3) {
        return Maps.newEnumMap(Map.of(Direction.NORTH, voxelShape, Direction.EAST, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R90), vec3), Direction.SOUTH, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R180), vec3), Direction.WEST, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R270), vec3), Direction.UP, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R270, Quadrant.R0), vec3), Direction.DOWN, Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R90, Quadrant.R0), vec3)));
    }

    public static Map<AttachFace, Map<Direction, VoxelShape>> rotateAttachFace(VoxelShape voxelShape) {
        return Map.of(AttachFace.WALL, Shapes.rotateHorizontal(voxelShape), AttachFace.FLOOR, Shapes.rotateHorizontal(Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R270, Quadrant.R0))), AttachFace.CEILING, Shapes.rotateHorizontal(Shapes.rotate(voxelShape, OctahedralGroup.fromXYAngles(Quadrant.R90, Quadrant.R180))));
    }

    public static interface DoubleLineConsumer {
        public void consume(double var1, double var3, double var5, double var7, double var9, double var11);
    }
}

