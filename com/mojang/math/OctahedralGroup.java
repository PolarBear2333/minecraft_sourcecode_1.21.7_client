/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.booleans.BooleanArrayList
 *  it.unimi.dsi.fastutil.booleans.BooleanList
 *  javax.annotation.Nullable
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 */
package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import com.mojang.math.SymmetricGroup3;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;

public enum OctahedralGroup implements StringRepresentable
{
    IDENTITY("identity", SymmetricGroup3.P123, false, false, false),
    ROT_180_FACE_XY("rot_180_face_xy", SymmetricGroup3.P123, true, true, false),
    ROT_180_FACE_XZ("rot_180_face_xz", SymmetricGroup3.P123, true, false, true),
    ROT_180_FACE_YZ("rot_180_face_yz", SymmetricGroup3.P123, false, true, true),
    ROT_120_NNN("rot_120_nnn", SymmetricGroup3.P231, false, false, false),
    ROT_120_NNP("rot_120_nnp", SymmetricGroup3.P312, true, false, true),
    ROT_120_NPN("rot_120_npn", SymmetricGroup3.P312, false, true, true),
    ROT_120_NPP("rot_120_npp", SymmetricGroup3.P231, true, false, true),
    ROT_120_PNN("rot_120_pnn", SymmetricGroup3.P312, true, true, false),
    ROT_120_PNP("rot_120_pnp", SymmetricGroup3.P231, true, true, false),
    ROT_120_PPN("rot_120_ppn", SymmetricGroup3.P231, false, true, true),
    ROT_120_PPP("rot_120_ppp", SymmetricGroup3.P312, false, false, false),
    ROT_180_EDGE_XY_NEG("rot_180_edge_xy_neg", SymmetricGroup3.P213, true, true, true),
    ROT_180_EDGE_XY_POS("rot_180_edge_xy_pos", SymmetricGroup3.P213, false, false, true),
    ROT_180_EDGE_XZ_NEG("rot_180_edge_xz_neg", SymmetricGroup3.P321, true, true, true),
    ROT_180_EDGE_XZ_POS("rot_180_edge_xz_pos", SymmetricGroup3.P321, false, true, false),
    ROT_180_EDGE_YZ_NEG("rot_180_edge_yz_neg", SymmetricGroup3.P132, true, true, true),
    ROT_180_EDGE_YZ_POS("rot_180_edge_yz_pos", SymmetricGroup3.P132, true, false, false),
    ROT_90_X_NEG("rot_90_x_neg", SymmetricGroup3.P132, false, false, true),
    ROT_90_X_POS("rot_90_x_pos", SymmetricGroup3.P132, false, true, false),
    ROT_90_Y_NEG("rot_90_y_neg", SymmetricGroup3.P321, true, false, false),
    ROT_90_Y_POS("rot_90_y_pos", SymmetricGroup3.P321, false, false, true),
    ROT_90_Z_NEG("rot_90_z_neg", SymmetricGroup3.P213, false, true, false),
    ROT_90_Z_POS("rot_90_z_pos", SymmetricGroup3.P213, true, false, false),
    INVERSION("inversion", SymmetricGroup3.P123, true, true, true),
    INVERT_X("invert_x", SymmetricGroup3.P123, true, false, false),
    INVERT_Y("invert_y", SymmetricGroup3.P123, false, true, false),
    INVERT_Z("invert_z", SymmetricGroup3.P123, false, false, true),
    ROT_60_REF_NNN("rot_60_ref_nnn", SymmetricGroup3.P312, true, true, true),
    ROT_60_REF_NNP("rot_60_ref_nnp", SymmetricGroup3.P231, true, false, false),
    ROT_60_REF_NPN("rot_60_ref_npn", SymmetricGroup3.P231, false, false, true),
    ROT_60_REF_NPP("rot_60_ref_npp", SymmetricGroup3.P312, false, false, true),
    ROT_60_REF_PNN("rot_60_ref_pnn", SymmetricGroup3.P231, false, true, false),
    ROT_60_REF_PNP("rot_60_ref_pnp", SymmetricGroup3.P312, true, false, false),
    ROT_60_REF_PPN("rot_60_ref_ppn", SymmetricGroup3.P312, false, true, false),
    ROT_60_REF_PPP("rot_60_ref_ppp", SymmetricGroup3.P231, true, true, true),
    SWAP_XY("swap_xy", SymmetricGroup3.P213, false, false, false),
    SWAP_YZ("swap_yz", SymmetricGroup3.P132, false, false, false),
    SWAP_XZ("swap_xz", SymmetricGroup3.P321, false, false, false),
    SWAP_NEG_XY("swap_neg_xy", SymmetricGroup3.P213, true, true, false),
    SWAP_NEG_YZ("swap_neg_yz", SymmetricGroup3.P132, false, true, true),
    SWAP_NEG_XZ("swap_neg_xz", SymmetricGroup3.P321, true, false, true),
    ROT_90_REF_X_NEG("rot_90_ref_x_neg", SymmetricGroup3.P132, true, false, true),
    ROT_90_REF_X_POS("rot_90_ref_x_pos", SymmetricGroup3.P132, true, true, false),
    ROT_90_REF_Y_NEG("rot_90_ref_y_neg", SymmetricGroup3.P321, true, true, false),
    ROT_90_REF_Y_POS("rot_90_ref_y_pos", SymmetricGroup3.P321, false, true, true),
    ROT_90_REF_Z_NEG("rot_90_ref_z_neg", SymmetricGroup3.P213, false, true, true),
    ROT_90_REF_Z_POS("rot_90_ref_z_pos", SymmetricGroup3.P213, true, false, true);

    private static final Direction.Axis[] AXES;
    private final Matrix3fc transformation;
    private final String name;
    @Nullable
    private Map<Direction, Direction> rotatedDirections;
    private final boolean invertX;
    private final boolean invertY;
    private final boolean invertZ;
    private final SymmetricGroup3 permutation;
    private static final OctahedralGroup[][] CAYLEY_TABLE;
    private static final OctahedralGroup[] INVERSE_TABLE;
    private static final OctahedralGroup[][] XY_TABLE;

    private OctahedralGroup(String string2, SymmetricGroup3 symmetricGroup3, boolean bl, boolean bl2, boolean bl3) {
        this.name = string2;
        this.invertX = bl;
        this.invertY = bl2;
        this.invertZ = bl3;
        this.permutation = symmetricGroup3;
        Matrix3f matrix3f = new Matrix3f().scaling(bl ? -1.0f : 1.0f, bl2 ? -1.0f : 1.0f, bl3 ? -1.0f : 1.0f);
        matrix3f.mul(symmetricGroup3.transformation());
        this.transformation = matrix3f;
    }

    private BooleanList packInversions() {
        return new BooleanArrayList(new boolean[]{this.invertX, this.invertY, this.invertZ});
    }

    public OctahedralGroup compose(OctahedralGroup octahedralGroup) {
        return CAYLEY_TABLE[this.ordinal()][octahedralGroup.ordinal()];
    }

    public OctahedralGroup inverse() {
        return INVERSE_TABLE[this.ordinal()];
    }

    public Matrix3fc transformation() {
        return this.transformation;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Direction rotate(Direction direction2) {
        if (this.rotatedDirections == null) {
            this.rotatedDirections = Util.makeEnumMap(Direction.class, direction -> {
                Direction.Axis axis = direction.getAxis();
                Direction.AxisDirection axisDirection = direction.getAxisDirection();
                Direction.Axis axis2 = this.permute(axis);
                Direction.AxisDirection axisDirection2 = this.inverts(axis2) ? axisDirection.opposite() : axisDirection;
                return Direction.fromAxisAndDirection(axis2, axisDirection2);
            });
        }
        return this.rotatedDirections.get(direction2);
    }

    public boolean inverts(Direction.Axis axis) {
        return switch (axis) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> this.invertX;
            case Direction.Axis.Y -> this.invertY;
            case Direction.Axis.Z -> this.invertZ;
        };
    }

    public Direction.Axis permute(Direction.Axis axis) {
        return AXES[this.permutation.permutation(axis.ordinal())];
    }

    public FrontAndTop rotate(FrontAndTop frontAndTop) {
        return FrontAndTop.fromFrontAndTop(this.rotate(frontAndTop.front()), this.rotate(frontAndTop.top()));
    }

    public static OctahedralGroup fromXYAngles(Quadrant quadrant, Quadrant quadrant2) {
        return XY_TABLE[quadrant.ordinal()][quadrant2.ordinal()];
    }

    static {
        AXES = Direction.Axis.values();
        CAYLEY_TABLE = Util.make(new OctahedralGroup[OctahedralGroup.values().length][OctahedralGroup.values().length], octahedralGroupArray -> {
            Map<Pair, OctahedralGroup> map = Arrays.stream(OctahedralGroup.values()).collect(Collectors.toMap(octahedralGroup -> Pair.of((Object)((Object)octahedralGroup.permutation), (Object)octahedralGroup.packInversions()), octahedralGroup -> octahedralGroup));
            for (OctahedralGroup octahedralGroup2 : OctahedralGroup.values()) {
                for (OctahedralGroup octahedralGroup3 : OctahedralGroup.values()) {
                    BooleanList booleanList = octahedralGroup2.packInversions();
                    BooleanList booleanList2 = octahedralGroup3.packInversions();
                    SymmetricGroup3 symmetricGroup3 = octahedralGroup3.permutation.compose(octahedralGroup2.permutation);
                    BooleanArrayList booleanArrayList = new BooleanArrayList(3);
                    for (int i = 0; i < 3; ++i) {
                        booleanArrayList.add(booleanList.getBoolean(i) ^ booleanList2.getBoolean(octahedralGroup2.permutation.permutation(i)));
                    }
                    octahedralGroupArray[octahedralGroup2.ordinal()][octahedralGroup3.ordinal()] = map.get(Pair.of((Object)((Object)symmetricGroup3), (Object)booleanArrayList));
                }
            }
        });
        INVERSE_TABLE = (OctahedralGroup[])Arrays.stream(OctahedralGroup.values()).map(octahedralGroup -> Arrays.stream(OctahedralGroup.values()).filter(octahedralGroup2 -> octahedralGroup.compose((OctahedralGroup)octahedralGroup2) == IDENTITY).findAny().get()).toArray(OctahedralGroup[]::new);
        XY_TABLE = Util.make(new OctahedralGroup[Quadrant.values().length][Quadrant.values().length], octahedralGroupArray -> {
            for (Quadrant quadrant : Quadrant.values()) {
                for (Quadrant quadrant2 : Quadrant.values()) {
                    int n;
                    OctahedralGroup octahedralGroup = IDENTITY;
                    for (n = 0; n < quadrant2.shift; ++n) {
                        octahedralGroup = octahedralGroup.compose(ROT_90_Y_NEG);
                    }
                    for (n = 0; n < quadrant.shift; ++n) {
                        octahedralGroup = octahedralGroup.compose(ROT_90_X_NEG);
                    }
                    octahedralGroupArray[quadrant.ordinal()][quadrant2.ordinal()] = octahedralGroup;
                }
            }
        });
    }
}

