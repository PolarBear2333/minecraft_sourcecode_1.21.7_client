/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.resources.model;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public enum BlockModelRotation implements ModelState
{
    X0_Y0(Quadrant.R0, Quadrant.R0),
    X0_Y90(Quadrant.R0, Quadrant.R90),
    X0_Y180(Quadrant.R0, Quadrant.R180),
    X0_Y270(Quadrant.R0, Quadrant.R270),
    X90_Y0(Quadrant.R90, Quadrant.R0),
    X90_Y90(Quadrant.R90, Quadrant.R90),
    X90_Y180(Quadrant.R90, Quadrant.R180),
    X90_Y270(Quadrant.R90, Quadrant.R270),
    X180_Y0(Quadrant.R180, Quadrant.R0),
    X180_Y90(Quadrant.R180, Quadrant.R90),
    X180_Y180(Quadrant.R180, Quadrant.R180),
    X180_Y270(Quadrant.R180, Quadrant.R270),
    X270_Y0(Quadrant.R270, Quadrant.R0),
    X270_Y90(Quadrant.R270, Quadrant.R90),
    X270_Y180(Quadrant.R270, Quadrant.R180),
    X270_Y270(Quadrant.R270, Quadrant.R270);

    private static final BlockModelRotation[][] XY_TABLE;
    private final Quadrant xRotation;
    private final Quadrant yRotation;
    final Transformation transformation;
    private final OctahedralGroup actualRotation;
    final Map<Direction, Matrix4fc> faceMapping = new EnumMap<Direction, Matrix4fc>(Direction.class);
    final Map<Direction, Matrix4fc> inverseFaceMapping = new EnumMap<Direction, Matrix4fc>(Direction.class);
    private final WithUvLock withUvLock = new WithUvLock(this);

    private BlockModelRotation(Quadrant quadrant, Quadrant quadrant2) {
        this.xRotation = quadrant;
        this.yRotation = quadrant2;
        this.actualRotation = OctahedralGroup.fromXYAngles(quadrant, quadrant2);
        this.transformation = this.actualRotation != OctahedralGroup.IDENTITY ? new Transformation((Matrix4fc)new Matrix4f(this.actualRotation.transformation())) : Transformation.identity();
        for (Direction direction : Direction.values()) {
            Matrix4fc matrix4fc = BlockMath.getFaceTransformation(this.transformation, direction).getMatrix();
            this.faceMapping.put(direction, matrix4fc);
            this.inverseFaceMapping.put(direction, (Matrix4fc)matrix4fc.invertAffine(new Matrix4f()));
        }
    }

    @Override
    public Transformation transformation() {
        return this.transformation;
    }

    public static BlockModelRotation by(Quadrant quadrant, Quadrant quadrant2) {
        return XY_TABLE[quadrant.ordinal()][quadrant2.ordinal()];
    }

    public OctahedralGroup actualRotation() {
        return this.actualRotation;
    }

    public ModelState withUvLock() {
        return this.withUvLock;
    }

    static {
        XY_TABLE = Util.make(new BlockModelRotation[Quadrant.values().length][Quadrant.values().length], blockModelRotationArray -> {
            BlockModelRotation[] blockModelRotationArray2 = BlockModelRotation.values();
            int n = blockModelRotationArray2.length;
            for (int i = 0; i < n; ++i) {
                BlockModelRotation blockModelRotation;
                blockModelRotationArray[blockModelRotation.xRotation.ordinal()][blockModelRotation.yRotation.ordinal()] = blockModelRotation = blockModelRotationArray2[i];
            }
        });
    }

    record WithUvLock(BlockModelRotation parent) implements ModelState
    {
        @Override
        public Transformation transformation() {
            return this.parent.transformation;
        }

        @Override
        public Matrix4fc faceTransformation(Direction direction) {
            return this.parent.faceMapping.getOrDefault(direction, NO_TRANSFORM);
        }

        @Override
        public Matrix4fc inverseFaceTransformation(Direction direction) {
            return this.parent.inverseFaceMapping.getOrDefault(direction, NO_TRANSFORM);
        }
    }
}

