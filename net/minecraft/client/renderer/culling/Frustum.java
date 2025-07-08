/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.FrustumIntersection
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package net.minecraft.client.renderer.culling;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final FrustumIntersection intersection = new FrustumIntersection();
    private final Matrix4f matrix = new Matrix4f();
    private Vector4f viewVector;
    private double camX;
    private double camY;
    private double camZ;

    public Frustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
        this.calculateFrustum(matrix4f, matrix4f2);
    }

    public Frustum(Frustum frustum) {
        this.intersection.set((Matrix4fc)frustum.matrix);
        this.matrix.set((Matrix4fc)frustum.matrix);
        this.camX = frustum.camX;
        this.camY = frustum.camY;
        this.camZ = frustum.camZ;
        this.viewVector = frustum.viewVector;
    }

    public Frustum offsetToFullyIncludeCameraCube(int n) {
        double d = Math.floor(this.camX / (double)n) * (double)n;
        double d2 = Math.floor(this.camY / (double)n) * (double)n;
        double d3 = Math.floor(this.camZ / (double)n) * (double)n;
        double d4 = Math.ceil(this.camX / (double)n) * (double)n;
        double d5 = Math.ceil(this.camY / (double)n) * (double)n;
        double d6 = Math.ceil(this.camZ / (double)n) * (double)n;
        while (this.intersection.intersectAab((float)(d - this.camX), (float)(d2 - this.camY), (float)(d3 - this.camZ), (float)(d4 - this.camX), (float)(d5 - this.camY), (float)(d6 - this.camZ)) != -2) {
            this.camX -= (double)(this.viewVector.x() * 4.0f);
            this.camY -= (double)(this.viewVector.y() * 4.0f);
            this.camZ -= (double)(this.viewVector.z() * 4.0f);
        }
        return this;
    }

    public void prepare(double d, double d2, double d3) {
        this.camX = d;
        this.camY = d2;
        this.camZ = d3;
    }

    private void calculateFrustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
        matrix4f2.mul((Matrix4fc)matrix4f, this.matrix);
        this.intersection.set((Matrix4fc)this.matrix);
        this.viewVector = this.matrix.transformTranspose(new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
    }

    public boolean isVisible(AABB aABB) {
        int n = this.cubeInFrustum(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
        return n == -2 || n == -1;
    }

    public int cubeInFrustum(BoundingBox boundingBox) {
        return this.cubeInFrustum(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1);
    }

    private int cubeInFrustum(double d, double d2, double d3, double d4, double d5, double d6) {
        float f = (float)(d - this.camX);
        float f2 = (float)(d2 - this.camY);
        float f3 = (float)(d3 - this.camZ);
        float f4 = (float)(d4 - this.camX);
        float f5 = (float)(d5 - this.camY);
        float f6 = (float)(d6 - this.camZ);
        return this.intersection.intersectAab(f, f2, f3, f4, f5, f6);
    }

    public Vector4f[] getFrustumPoints() {
        Vector4f[] vector4fArray = new Vector4f[]{new Vector4f(-1.0f, -1.0f, -1.0f, 1.0f), new Vector4f(1.0f, -1.0f, -1.0f, 1.0f), new Vector4f(1.0f, 1.0f, -1.0f, 1.0f), new Vector4f(-1.0f, 1.0f, -1.0f, 1.0f), new Vector4f(-1.0f, -1.0f, 1.0f, 1.0f), new Vector4f(1.0f, -1.0f, 1.0f, 1.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector4f(-1.0f, 1.0f, 1.0f, 1.0f)};
        Matrix4f matrix4f = this.matrix.invert(new Matrix4f());
        for (int i = 0; i < 8; ++i) {
            matrix4f.transform(vector4fArray[i]);
            vector4fArray[i].div(vector4fArray[i].w());
        }
        return vector4fArray;
    }

    public double getCamX() {
        return this.camX;
    }

    public double getCamY() {
        return this.camY;
    }

    public double getCamZ() {
        return this.camZ;
    }
}

