/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class SheetedDecalTextureGenerator
implements VertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f cameraInversePose;
    private final Matrix3f normalInversePose;
    private final float textureScale;
    private final Vector3f worldPos = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private float x;
    private float y;
    private float z;

    public SheetedDecalTextureGenerator(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f) {
        this.delegate = vertexConsumer;
        this.cameraInversePose = new Matrix4f((Matrix4fc)pose.pose()).invert();
        this.normalInversePose = new Matrix3f((Matrix3fc)pose.normal()).invert();
        this.textureScale = f;
    }

    @Override
    public VertexConsumer addVertex(float f, float f2, float f3) {
        this.x = f;
        this.y = f2;
        this.z = f3;
        this.delegate.addVertex(f, f2, f3);
        return this;
    }

    @Override
    public VertexConsumer setColor(int n, int n2, int n3, int n4) {
        this.delegate.setColor(-1);
        return this;
    }

    @Override
    public VertexConsumer setUv(float f, float f2) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int n, int n2) {
        this.delegate.setUv1(n, n2);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int n, int n2) {
        this.delegate.setUv2(n, n2);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float f, float f2, float f3) {
        this.delegate.setNormal(f, f2, f3);
        Vector3f vector3f = this.normalInversePose.transform(f, f2, f3, this.normal);
        Direction direction = Direction.getApproximateNearest(vector3f.x(), vector3f.y(), vector3f.z());
        Vector3f vector3f2 = this.cameraInversePose.transformPosition(this.x, this.y, this.z, this.worldPos);
        vector3f2.rotateY((float)Math.PI);
        vector3f2.rotateX(-1.5707964f);
        vector3f2.rotate((Quaternionfc)direction.getRotation());
        this.delegate.setUv(-vector3f2.x() * this.textureScale, -vector3f2.y() * this.textureScale);
        return this;
    }
}

