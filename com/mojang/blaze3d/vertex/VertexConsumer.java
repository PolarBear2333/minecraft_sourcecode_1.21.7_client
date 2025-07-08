/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix4f
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.lwjgl.system.MemoryStack
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

public interface VertexConsumer {
    public VertexConsumer addVertex(float var1, float var2, float var3);

    public VertexConsumer setColor(int var1, int var2, int var3, int var4);

    public VertexConsumer setUv(float var1, float var2);

    public VertexConsumer setUv1(int var1, int var2);

    public VertexConsumer setUv2(int var1, int var2);

    public VertexConsumer setNormal(float var1, float var2, float var3);

    default public void addVertex(float f, float f2, float f3, int n, float f4, float f5, int n2, int n3, float f6, float f7, float f8) {
        this.addVertex(f, f2, f3);
        this.setColor(n);
        this.setUv(f4, f5);
        this.setOverlay(n2);
        this.setLight(n3);
        this.setNormal(f6, f7, f8);
    }

    default public VertexConsumer setColor(float f, float f2, float f3, float f4) {
        return this.setColor((int)(f * 255.0f), (int)(f2 * 255.0f), (int)(f3 * 255.0f), (int)(f4 * 255.0f));
    }

    default public VertexConsumer setColor(int n) {
        return this.setColor(ARGB.red(n), ARGB.green(n), ARGB.blue(n), ARGB.alpha(n));
    }

    default public VertexConsumer setWhiteAlpha(int n) {
        return this.setColor(ARGB.color(n, -1));
    }

    default public VertexConsumer setLight(int n) {
        return this.setUv2(n & 0xFFFF, n >> 16 & 0xFFFF);
    }

    default public VertexConsumer setOverlay(int n) {
        return this.setUv1(n & 0xFFFF, n >> 16 & 0xFFFF);
    }

    default public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float f, float f2, float f3, float f4, int n, int n2) {
        this.putBulkData(pose, bakedQuad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, f, f2, f3, f4, new int[]{n, n, n, n}, n2, false);
    }

    default public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fArray, float f, float f2, float f3, float f4, int[] nArray, int n, boolean bl) {
        int[] nArray2 = bakedQuad.vertices();
        Vector3fc vector3fc = bakedQuad.direction().getUnitVec3f();
        Matrix4f matrix4f = pose.pose();
        Vector3f vector3f = pose.transformNormal(vector3fc, new Vector3f());
        int n2 = 8;
        int n3 = nArray2.length / 8;
        int n4 = (int)(f4 * 255.0f);
        int n5 = bakedQuad.lightEmission();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int i = 0; i < n3; ++i) {
                float f5;
                float f6;
                float f7;
                float f8;
                intBuffer.clear();
                intBuffer.put(nArray2, i * 8, 8);
                float f9 = byteBuffer.getFloat(0);
                float f10 = byteBuffer.getFloat(4);
                float f11 = byteBuffer.getFloat(8);
                if (bl) {
                    float f12 = byteBuffer.get(12) & 0xFF;
                    float f13 = byteBuffer.get(13) & 0xFF;
                    f8 = byteBuffer.get(14) & 0xFF;
                    f7 = f12 * fArray[i] * f;
                    f6 = f13 * fArray[i] * f2;
                    f5 = f8 * fArray[i] * f3;
                } else {
                    f7 = fArray[i] * f * 255.0f;
                    f6 = fArray[i] * f2 * 255.0f;
                    f5 = fArray[i] * f3 * 255.0f;
                }
                int n6 = ARGB.color(n4, (int)f7, (int)f6, (int)f5);
                int n7 = LightTexture.lightCoordsWithEmission(nArray[i], n5);
                f8 = byteBuffer.getFloat(16);
                float f14 = byteBuffer.getFloat(20);
                Vector3f vector3f2 = matrix4f.transformPosition(f9, f10, f11, new Vector3f());
                this.addVertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), n6, f8, f14, n, n7, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    default public VertexConsumer addVertex(Vector3f vector3f) {
        return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer addVertex(PoseStack.Pose pose, Vector3f vector3f) {
        return this.addVertex(pose, vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer addVertex(PoseStack.Pose pose, float f, float f2, float f3) {
        return this.addVertex(pose.pose(), f, f2, f3);
    }

    default public VertexConsumer addVertex(Matrix4f matrix4f, float f, float f2, float f3) {
        Vector3f vector3f = matrix4f.transformPosition(f, f2, f3, new Vector3f());
        return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer addVertexWith2DPose(Matrix3x2f matrix3x2f, float f, float f2, float f3) {
        Vector2f vector2f = matrix3x2f.transformPosition(f, f2, new Vector2f());
        return this.addVertex(vector2f.x(), vector2f.y(), f3);
    }

    default public VertexConsumer setNormal(PoseStack.Pose pose, float f, float f2, float f3) {
        Vector3f vector3f = pose.transformNormal(f, f2, f3, new Vector3f());
        return this.setNormal(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default public VertexConsumer setNormal(PoseStack.Pose pose, Vector3f vector3f) {
        return this.setNormal(pose, vector3f.x(), vector3f.y(), vector3f.z());
    }
}

