/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.math.DoubleMath
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.phys.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.OffsetDoubleList;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.SliceShape;

public abstract class VoxelShape {
    protected final DiscreteVoxelShape shape;
    @Nullable
    private VoxelShape[] faces;

    protected VoxelShape(DiscreteVoxelShape discreteVoxelShape) {
        this.shape = discreteVoxelShape;
    }

    public double min(Direction.Axis axis) {
        int n = this.shape.firstFull(axis);
        if (n >= this.shape.getSize(axis)) {
            return Double.POSITIVE_INFINITY;
        }
        return this.get(axis, n);
    }

    public double max(Direction.Axis axis) {
        int n = this.shape.lastFull(axis);
        if (n <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.get(axis, n);
    }

    public AABB bounds() {
        if (this.isEmpty()) {
            throw Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
        }
        return new AABB(this.min(Direction.Axis.X), this.min(Direction.Axis.Y), this.min(Direction.Axis.Z), this.max(Direction.Axis.X), this.max(Direction.Axis.Y), this.max(Direction.Axis.Z));
    }

    public VoxelShape singleEncompassing() {
        if (this.isEmpty()) {
            return Shapes.empty();
        }
        return Shapes.box(this.min(Direction.Axis.X), this.min(Direction.Axis.Y), this.min(Direction.Axis.Z), this.max(Direction.Axis.X), this.max(Direction.Axis.Y), this.max(Direction.Axis.Z));
    }

    protected double get(Direction.Axis axis, int n) {
        return this.getCoords(axis).getDouble(n);
    }

    public abstract DoubleList getCoords(Direction.Axis var1);

    public boolean isEmpty() {
        return this.shape.isEmpty();
    }

    public VoxelShape move(Vec3 vec3) {
        return this.move(vec3.x, vec3.y, vec3.z);
    }

    public VoxelShape move(Vec3i vec3i) {
        return this.move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public VoxelShape move(double d, double d2, double d3) {
        if (this.isEmpty()) {
            return Shapes.empty();
        }
        return new ArrayVoxelShape(this.shape, (DoubleList)new OffsetDoubleList(this.getCoords(Direction.Axis.X), d), (DoubleList)new OffsetDoubleList(this.getCoords(Direction.Axis.Y), d2), (DoubleList)new OffsetDoubleList(this.getCoords(Direction.Axis.Z), d3));
    }

    public VoxelShape optimize() {
        VoxelShape[] voxelShapeArray = new VoxelShape[]{Shapes.empty()};
        this.forAllBoxes((d, d2, d3, d4, d5, d6) -> {
            voxelShapeArray[0] = Shapes.joinUnoptimized(voxelShapeArray[0], Shapes.box(d, d2, d3, d4, d5, d6), BooleanOp.OR);
        });
        return voxelShapeArray[0];
    }

    public void forAllEdges(Shapes.DoubleLineConsumer doubleLineConsumer) {
        this.shape.forAllEdges((n, n2, n3, n4, n5, n6) -> doubleLineConsumer.consume(this.get(Direction.Axis.X, n), this.get(Direction.Axis.Y, n2), this.get(Direction.Axis.Z, n3), this.get(Direction.Axis.X, n4), this.get(Direction.Axis.Y, n5), this.get(Direction.Axis.Z, n6)), true);
    }

    public void forAllBoxes(Shapes.DoubleLineConsumer doubleLineConsumer) {
        DoubleList doubleList = this.getCoords(Direction.Axis.X);
        DoubleList doubleList2 = this.getCoords(Direction.Axis.Y);
        DoubleList doubleList3 = this.getCoords(Direction.Axis.Z);
        this.shape.forAllBoxes((n, n2, n3, n4, n5, n6) -> doubleLineConsumer.consume(doubleList.getDouble(n), doubleList2.getDouble(n2), doubleList3.getDouble(n3), doubleList.getDouble(n4), doubleList2.getDouble(n5), doubleList3.getDouble(n6)), true);
    }

    public List<AABB> toAabbs() {
        ArrayList arrayList = Lists.newArrayList();
        this.forAllBoxes((d, d2, d3, d4, d5, d6) -> arrayList.add(new AABB(d, d2, d3, d4, d5, d6)));
        return arrayList;
    }

    public double min(Direction.Axis axis, double d, double d2) {
        int n;
        Direction.Axis axis2 = AxisCycle.FORWARD.cycle(axis);
        Direction.Axis axis3 = AxisCycle.BACKWARD.cycle(axis);
        int n2 = this.findIndex(axis2, d);
        int n3 = this.shape.firstFull(axis, n2, n = this.findIndex(axis3, d2));
        if (n3 >= this.shape.getSize(axis)) {
            return Double.POSITIVE_INFINITY;
        }
        return this.get(axis, n3);
    }

    public double max(Direction.Axis axis, double d, double d2) {
        int n;
        Direction.Axis axis2 = AxisCycle.FORWARD.cycle(axis);
        Direction.Axis axis3 = AxisCycle.BACKWARD.cycle(axis);
        int n2 = this.findIndex(axis2, d);
        int n3 = this.shape.lastFull(axis, n2, n = this.findIndex(axis3, d2));
        if (n3 <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.get(axis, n3);
    }

    protected int findIndex(Direction.Axis axis, double d) {
        return Mth.binarySearch(0, this.shape.getSize(axis) + 1, n -> d < this.get(axis, n)) - 1;
    }

    @Nullable
    public BlockHitResult clip(Vec3 vec3, Vec3 vec32, BlockPos blockPos) {
        if (this.isEmpty()) {
            return null;
        }
        Vec3 vec33 = vec32.subtract(vec3);
        if (vec33.lengthSqr() < 1.0E-7) {
            return null;
        }
        Vec3 vec34 = vec3.add(vec33.scale(0.001));
        if (this.shape.isFullWide(this.findIndex(Direction.Axis.X, vec34.x - (double)blockPos.getX()), this.findIndex(Direction.Axis.Y, vec34.y - (double)blockPos.getY()), this.findIndex(Direction.Axis.Z, vec34.z - (double)blockPos.getZ()))) {
            return new BlockHitResult(vec34, Direction.getApproximateNearest(vec33.x, vec33.y, vec33.z).getOpposite(), blockPos, true);
        }
        return AABB.clip(this.toAabbs(), vec3, vec32, blockPos);
    }

    public Optional<Vec3> closestPointTo(Vec3 vec3) {
        if (this.isEmpty()) {
            return Optional.empty();
        }
        Vec3[] vec3Array = new Vec3[1];
        this.forAllBoxes((d, d2, d3, d4, d5, d6) -> {
            double d7 = Mth.clamp(vec3.x(), d, d4);
            double d8 = Mth.clamp(vec3.y(), d2, d5);
            double d9 = Mth.clamp(vec3.z(), d3, d6);
            if (vec3Array[0] == null || vec3.distanceToSqr(d7, d8, d9) < vec3.distanceToSqr(vec3Array[0])) {
                vec3Array[0] = new Vec3(d7, d8, d9);
            }
        });
        return Optional.of(vec3Array[0]);
    }

    public VoxelShape getFaceShape(Direction direction) {
        VoxelShape voxelShape;
        if (this.isEmpty() || this == Shapes.block()) {
            return this;
        }
        if (this.faces != null) {
            voxelShape = this.faces[direction.ordinal()];
            if (voxelShape != null) {
                return voxelShape;
            }
        } else {
            this.faces = new VoxelShape[6];
        }
        this.faces[direction.ordinal()] = voxelShape = this.calculateFace(direction);
        return voxelShape;
    }

    private VoxelShape calculateFace(Direction direction) {
        Direction.Axis axis = direction.getAxis();
        if (this.isCubeLikeAlong(axis)) {
            return this;
        }
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        int n = this.findIndex(axis, axisDirection == Direction.AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
        SliceShape sliceShape = new SliceShape(this, axis, n);
        if (sliceShape.isEmpty()) {
            return Shapes.empty();
        }
        if (sliceShape.isCubeLike()) {
            return Shapes.block();
        }
        return sliceShape;
    }

    protected boolean isCubeLike() {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            if (this.isCubeLikeAlong(axis)) continue;
            return false;
        }
        return true;
    }

    private boolean isCubeLikeAlong(Direction.Axis axis) {
        DoubleList doubleList = this.getCoords(axis);
        return doubleList.size() == 2 && DoubleMath.fuzzyEquals((double)doubleList.getDouble(0), (double)0.0, (double)1.0E-7) && DoubleMath.fuzzyEquals((double)doubleList.getDouble(1), (double)1.0, (double)1.0E-7);
    }

    public double collide(Direction.Axis axis, AABB aABB, double d) {
        return this.collideX(AxisCycle.between(axis, Direction.Axis.X), aABB, d);
    }

    protected double collideX(AxisCycle axisCycle, AABB aABB, double d) {
        block11: {
            int n;
            int n2;
            double d2;
            Direction.Axis axis;
            AxisCycle axisCycle2;
            block10: {
                if (this.isEmpty()) {
                    return d;
                }
                if (Math.abs(d) < 1.0E-7) {
                    return 0.0;
                }
                axisCycle2 = axisCycle.inverse();
                axis = axisCycle2.cycle(Direction.Axis.X);
                Direction.Axis axis2 = axisCycle2.cycle(Direction.Axis.Y);
                Direction.Axis axis3 = axisCycle2.cycle(Direction.Axis.Z);
                double d3 = aABB.max(axis);
                d2 = aABB.min(axis);
                int n3 = this.findIndex(axis, d2 + 1.0E-7);
                int n4 = this.findIndex(axis, d3 - 1.0E-7);
                int n5 = Math.max(0, this.findIndex(axis2, aABB.min(axis2) + 1.0E-7));
                n2 = Math.min(this.shape.getSize(axis2), this.findIndex(axis2, aABB.max(axis2) - 1.0E-7) + 1);
                int n6 = Math.max(0, this.findIndex(axis3, aABB.min(axis3) + 1.0E-7));
                n = Math.min(this.shape.getSize(axis3), this.findIndex(axis3, aABB.max(axis3) - 1.0E-7) + 1);
                int n7 = this.shape.getSize(axis);
                if (!(d > 0.0)) break block10;
                for (int i = n4 + 1; i < n7; ++i) {
                    for (int j = n5; j < n2; ++j) {
                        for (int k = n6; k < n; ++k) {
                            if (!this.shape.isFullWide(axisCycle2, i, j, k)) continue;
                            double d4 = this.get(axis, i) - d3;
                            if (d4 >= -1.0E-7) {
                                d = Math.min(d, d4);
                            }
                            return d;
                        }
                    }
                }
                break block11;
            }
            if (!(d < 0.0)) break block11;
            for (int i = n3 - 1; i >= 0; --i) {
                for (int j = n5; j < n2; ++j) {
                    for (int k = n6; k < n; ++k) {
                        if (!this.shape.isFullWide(axisCycle2, i, j, k)) continue;
                        double d5 = this.get(axis, i + 1) - d2;
                        if (d5 <= 1.0E-7) {
                            d = Math.max(d, d5);
                        }
                        return d;
                    }
                }
            }
        }
        return d;
    }

    public boolean equals(Object object) {
        return super.equals(object);
    }

    public String toString() {
        return this.isEmpty() ? "EMPTY" : "VoxelShape[" + String.valueOf(this.bounds()) + "]";
    }
}

