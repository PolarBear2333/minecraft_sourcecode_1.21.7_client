/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.math.MatrixUtil;
import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class FaceBakery {
    public static final int VERTEX_INT_SIZE = 8;
    public static final int VERTEX_COUNT = 4;
    private static final int COLOR_INDEX = 3;
    public static final int UV_INDEX = 4;
    private static final Vector3fc NO_RESCALE = new Vector3f(1.0f, 1.0f, 1.0f);
    private static final Vector3fc BLOCK_MIDDLE = new Vector3f(0.5f, 0.5f, 0.5f);

    @VisibleForTesting
    static BlockElementFace.UVs defaultFaceUV(Vector3fc vector3fc, Vector3fc vector3fc2, Direction direction) {
        return switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> new BlockElementFace.UVs(vector3fc.x(), 16.0f - vector3fc2.z(), vector3fc2.x(), 16.0f - vector3fc.z());
            case Direction.UP -> new BlockElementFace.UVs(vector3fc.x(), vector3fc.z(), vector3fc2.x(), vector3fc2.z());
            case Direction.NORTH -> new BlockElementFace.UVs(16.0f - vector3fc2.x(), 16.0f - vector3fc2.y(), 16.0f - vector3fc.x(), 16.0f - vector3fc.y());
            case Direction.SOUTH -> new BlockElementFace.UVs(vector3fc.x(), 16.0f - vector3fc2.y(), vector3fc2.x(), 16.0f - vector3fc.y());
            case Direction.WEST -> new BlockElementFace.UVs(vector3fc.z(), 16.0f - vector3fc2.y(), vector3fc2.z(), 16.0f - vector3fc.y());
            case Direction.EAST -> new BlockElementFace.UVs(16.0f - vector3fc2.z(), 16.0f - vector3fc2.y(), 16.0f - vector3fc.z(), 16.0f - vector3fc.y());
        };
    }

    public static BakedQuad bakeQuad(Vector3fc vector3fc, Vector3fc vector3fc2, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState, @Nullable BlockElementRotation blockElementRotation, boolean bl, int n) {
        BlockElementFace.UVs uVs = blockElementFace.uvs();
        if (uVs == null) {
            uVs = FaceBakery.defaultFaceUV(vector3fc, vector3fc2, direction);
        }
        uVs = FaceBakery.shrinkUVs(textureAtlasSprite, uVs);
        Matrix4fc matrix4fc = modelState.inverseFaceTransformation(direction);
        int[] nArray = FaceBakery.makeVertices(uVs, blockElementFace.rotation(), matrix4fc, textureAtlasSprite, direction, FaceBakery.setupShape(vector3fc, vector3fc2), modelState.transformation(), blockElementRotation);
        Direction direction2 = FaceBakery.calculateFacing(nArray);
        if (blockElementRotation == null) {
            FaceBakery.recalculateWinding(nArray, direction2);
        }
        return new BakedQuad(nArray, blockElementFace.tintIndex(), direction2, textureAtlasSprite, bl, n);
    }

    private static BlockElementFace.UVs shrinkUVs(TextureAtlasSprite textureAtlasSprite, BlockElementFace.UVs uVs) {
        float f = uVs.minU();
        float f2 = uVs.minV();
        float f3 = uVs.maxU();
        float f4 = uVs.maxV();
        float f5 = textureAtlasSprite.uvShrinkRatio();
        float f6 = (f + f + f3 + f3) / 4.0f;
        float f7 = (f2 + f2 + f4 + f4) / 4.0f;
        return new BlockElementFace.UVs(Mth.lerp(f5, f, f6), Mth.lerp(f5, f2, f7), Mth.lerp(f5, f3, f6), Mth.lerp(f5, f4, f7));
    }

    private static int[] makeVertices(BlockElementFace.UVs uVs, Quadrant quadrant, Matrix4fc matrix4fc, TextureAtlasSprite textureAtlasSprite, Direction direction, float[] fArray, Transformation transformation, @Nullable BlockElementRotation blockElementRotation) {
        FaceInfo faceInfo = FaceInfo.fromFacing(direction);
        int[] nArray = new int[32];
        for (int i = 0; i < 4; ++i) {
            FaceBakery.bakeVertex(nArray, i, faceInfo, uVs, quadrant, matrix4fc, fArray, textureAtlasSprite, transformation, blockElementRotation);
        }
        return nArray;
    }

    private static float[] setupShape(Vector3fc vector3fc, Vector3fc vector3fc2) {
        float[] fArray = new float[Direction.values().length];
        fArray[FaceInfo.Constants.MIN_X] = vector3fc.x() / 16.0f;
        fArray[FaceInfo.Constants.MIN_Y] = vector3fc.y() / 16.0f;
        fArray[FaceInfo.Constants.MIN_Z] = vector3fc.z() / 16.0f;
        fArray[FaceInfo.Constants.MAX_X] = vector3fc2.x() / 16.0f;
        fArray[FaceInfo.Constants.MAX_Y] = vector3fc2.y() / 16.0f;
        fArray[FaceInfo.Constants.MAX_Z] = vector3fc2.z() / 16.0f;
        return fArray;
    }

    private static void bakeVertex(int[] nArray, int n, FaceInfo faceInfo, BlockElementFace.UVs uVs, Quadrant quadrant, Matrix4fc matrix4fc, float[] fArray, TextureAtlasSprite textureAtlasSprite, Transformation transformation, @Nullable BlockElementRotation blockElementRotation) {
        float f;
        float f2;
        FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(n);
        Vector3f vector3f = new Vector3f(fArray[vertexInfo.xFace], fArray[vertexInfo.yFace], fArray[vertexInfo.zFace]);
        FaceBakery.applyElementRotation(vector3f, blockElementRotation);
        FaceBakery.applyModelRotation(vector3f, transformation);
        float f3 = BlockElementFace.getU(uVs, quadrant, n);
        float f4 = BlockElementFace.getV(uVs, quadrant, n);
        if (MatrixUtil.isIdentity(matrix4fc)) {
            f2 = f3;
            f = f4;
        } else {
            Vector3f vector3f2 = matrix4fc.transformPosition(new Vector3f(FaceBakery.cornerToCenter(f3), FaceBakery.cornerToCenter(f4), 0.0f));
            f2 = FaceBakery.centerToCorner(vector3f2.x);
            f = FaceBakery.centerToCorner(vector3f2.y);
        }
        FaceBakery.fillVertex(nArray, n, vector3f, textureAtlasSprite, f2, f);
    }

    private static float cornerToCenter(float f) {
        return f - 0.5f;
    }

    private static float centerToCorner(float f) {
        return f + 0.5f;
    }

    private static void fillVertex(int[] nArray, int n, Vector3f vector3f, TextureAtlasSprite textureAtlasSprite, float f, float f2) {
        int n2 = n * 8;
        nArray[n2] = Float.floatToRawIntBits(vector3f.x());
        nArray[n2 + 1] = Float.floatToRawIntBits(vector3f.y());
        nArray[n2 + 2] = Float.floatToRawIntBits(vector3f.z());
        nArray[n2 + 3] = -1;
        nArray[n2 + 4] = Float.floatToRawIntBits(textureAtlasSprite.getU(f));
        nArray[n2 + 4 + 1] = Float.floatToRawIntBits(textureAtlasSprite.getV(f2));
    }

    private static void applyElementRotation(Vector3f vector3f, @Nullable BlockElementRotation blockElementRotation) {
        if (blockElementRotation == null) {
            return;
        }
        Vector3fc vector3fc = blockElementRotation.axis().getPositive().getUnitVec3f();
        Matrix4f matrix4f = new Matrix4f().rotation(blockElementRotation.angle() * ((float)Math.PI / 180), vector3fc);
        Vector3fc vector3fc2 = blockElementRotation.rescale() ? FaceBakery.computeRescale(blockElementRotation) : NO_RESCALE;
        FaceBakery.rotateVertexBy(vector3f, (Vector3fc)blockElementRotation.origin(), (Matrix4fc)matrix4f, vector3fc2);
    }

    private static Vector3fc computeRescale(BlockElementRotation blockElementRotation) {
        if (blockElementRotation.angle() == 0.0f) {
            return NO_RESCALE;
        }
        float f = Math.abs(blockElementRotation.angle());
        float f2 = 1.0f / Mth.cos(f * ((float)Math.PI / 180));
        return switch (blockElementRotation.axis()) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> new Vector3f(1.0f, f2, f2);
            case Direction.Axis.Y -> new Vector3f(f2, 1.0f, f2);
            case Direction.Axis.Z -> new Vector3f(f2, f2, 1.0f);
        };
    }

    private static void applyModelRotation(Vector3f vector3f, Transformation transformation) {
        if (transformation == Transformation.identity()) {
            return;
        }
        FaceBakery.rotateVertexBy(vector3f, BLOCK_MIDDLE, transformation.getMatrix(), NO_RESCALE);
    }

    private static void rotateVertexBy(Vector3f vector3f, Vector3fc vector3fc, Matrix4fc matrix4fc, Vector3fc vector3fc2) {
        vector3f.sub(vector3fc);
        matrix4fc.transformPosition(vector3f);
        vector3f.mul(vector3fc2);
        vector3f.add(vector3fc);
    }

    private static Direction calculateFacing(int[] nArray) {
        Vector3f vector3f = FaceBakery.vectorFromData(nArray, 0);
        Vector3f vector3f2 = FaceBakery.vectorFromData(nArray, 8);
        Vector3f vector3f3 = FaceBakery.vectorFromData(nArray, 16);
        Vector3f vector3f4 = new Vector3f((Vector3fc)vector3f).sub((Vector3fc)vector3f2);
        Vector3f vector3f5 = new Vector3f((Vector3fc)vector3f3).sub((Vector3fc)vector3f2);
        Vector3f vector3f6 = new Vector3f((Vector3fc)vector3f5).cross((Vector3fc)vector3f4).normalize();
        if (!vector3f6.isFinite()) {
            return Direction.UP;
        }
        Direction direction = null;
        float f = 0.0f;
        for (Direction direction2 : Direction.values()) {
            float f2 = vector3f6.dot(direction2.getUnitVec3f());
            if (!(f2 >= 0.0f) || !(f2 > f)) continue;
            f = f2;
            direction = direction2;
        }
        if (direction == null) {
            return Direction.UP;
        }
        return direction;
    }

    private static float xFromData(int[] nArray, int n) {
        return Float.intBitsToFloat(nArray[n]);
    }

    private static float yFromData(int[] nArray, int n) {
        return Float.intBitsToFloat(nArray[n + 1]);
    }

    private static float zFromData(int[] nArray, int n) {
        return Float.intBitsToFloat(nArray[n + 2]);
    }

    private static Vector3f vectorFromData(int[] nArray, int n) {
        return new Vector3f(FaceBakery.xFromData(nArray, n), FaceBakery.yFromData(nArray, n), FaceBakery.zFromData(nArray, n));
    }

    private static void recalculateWinding(int[] nArray, Direction direction) {
        float f;
        int n;
        int[] nArray2 = new int[nArray.length];
        System.arraycopy(nArray, 0, nArray2, 0, nArray.length);
        float[] fArray = new float[Direction.values().length];
        fArray[FaceInfo.Constants.MIN_X] = 999.0f;
        fArray[FaceInfo.Constants.MIN_Y] = 999.0f;
        fArray[FaceInfo.Constants.MIN_Z] = 999.0f;
        fArray[FaceInfo.Constants.MAX_X] = -999.0f;
        fArray[FaceInfo.Constants.MAX_Y] = -999.0f;
        fArray[FaceInfo.Constants.MAX_Z] = -999.0f;
        for (int i = 0; i < 4; ++i) {
            n = 8 * i;
            float f2 = FaceBakery.xFromData(nArray2, n);
            float f3 = FaceBakery.yFromData(nArray2, n);
            f = FaceBakery.zFromData(nArray2, n);
            if (f2 < fArray[FaceInfo.Constants.MIN_X]) {
                fArray[FaceInfo.Constants.MIN_X] = f2;
            }
            if (f3 < fArray[FaceInfo.Constants.MIN_Y]) {
                fArray[FaceInfo.Constants.MIN_Y] = f3;
            }
            if (f < fArray[FaceInfo.Constants.MIN_Z]) {
                fArray[FaceInfo.Constants.MIN_Z] = f;
            }
            if (f2 > fArray[FaceInfo.Constants.MAX_X]) {
                fArray[FaceInfo.Constants.MAX_X] = f2;
            }
            if (f3 > fArray[FaceInfo.Constants.MAX_Y]) {
                fArray[FaceInfo.Constants.MAX_Y] = f3;
            }
            if (!(f > fArray[FaceInfo.Constants.MAX_Z])) continue;
            fArray[FaceInfo.Constants.MAX_Z] = f;
        }
        FaceInfo faceInfo = FaceInfo.fromFacing(direction);
        for (n = 0; n < 4; ++n) {
            int n2 = 8 * n;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(n);
            f = fArray[vertexInfo.xFace];
            float f4 = fArray[vertexInfo.yFace];
            float f5 = fArray[vertexInfo.zFace];
            nArray[n2] = Float.floatToRawIntBits(f);
            nArray[n2 + 1] = Float.floatToRawIntBits(f4);
            nArray[n2 + 2] = Float.floatToRawIntBits(f5);
            for (int i = 0; i < 4; ++i) {
                int n3 = 8 * i;
                float f6 = FaceBakery.xFromData(nArray2, n3);
                float f7 = FaceBakery.yFromData(nArray2, n3);
                float f8 = FaceBakery.zFromData(nArray2, n3);
                if (!Mth.equal(f, f6) || !Mth.equal(f4, f7) || !Mth.equal(f5, f8)) continue;
                nArray[n2 + 4] = nArray2[n3 + 4];
                nArray[n2 + 4 + 1] = nArray2[n3 + 4 + 1];
            }
        }
    }

    public static void extractPositions(int[] nArray, Consumer<Vector3f> consumer) {
        for (int i = 0; i < 4; ++i) {
            consumer.accept(FaceBakery.vectorFromData(nArray, 8 * i));
        }
    }
}

