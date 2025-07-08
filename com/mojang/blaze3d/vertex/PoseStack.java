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
 *  org.joml.Vector3fc
 */
package com.mojang.blaze3d.vertex;

import com.mojang.math.MatrixUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class PoseStack {
    private final List<Pose> poses = new ArrayList<Pose>(16);
    private int lastIndex;

    public PoseStack() {
        this.poses.add(new Pose());
    }

    public void translate(double d, double d2, double d3) {
        this.translate((float)d, (float)d2, (float)d3);
    }

    public void translate(float f, float f2, float f3) {
        this.last().translate(f, f2, f3);
    }

    public void translate(Vec3 vec3) {
        this.translate(vec3.x, vec3.y, vec3.z);
    }

    public void scale(float f, float f2, float f3) {
        this.last().scale(f, f2, f3);
    }

    public void mulPose(Quaternionfc quaternionfc) {
        this.last().rotate(quaternionfc);
    }

    public void rotateAround(Quaternionfc quaternionfc, float f, float f2, float f3) {
        this.last().rotateAround(quaternionfc, f, f2, f3);
    }

    public void pushPose() {
        Pose pose = this.last();
        ++this.lastIndex;
        if (this.lastIndex >= this.poses.size()) {
            this.poses.add(pose.copy());
        } else {
            this.poses.get(this.lastIndex).set(pose);
        }
    }

    public void popPose() {
        if (this.lastIndex == 0) {
            throw new NoSuchElementException();
        }
        --this.lastIndex;
    }

    public Pose last() {
        return this.poses.get(this.lastIndex);
    }

    public boolean isEmpty() {
        return this.lastIndex == 0;
    }

    public void setIdentity() {
        this.last().setIdentity();
    }

    public void mulPose(Matrix4fc matrix4fc) {
        this.last().mulPose(matrix4fc);
    }

    public static final class Pose {
        private final Matrix4f pose = new Matrix4f();
        private final Matrix3f normal = new Matrix3f();
        private boolean trustedNormals = true;

        private void computeNormalMatrix() {
            this.normal.set((Matrix4fc)this.pose).invert().transpose();
            this.trustedNormals = false;
        }

        void set(Pose pose) {
            this.pose.set((Matrix4fc)pose.pose);
            this.normal.set((Matrix3fc)pose.normal);
            this.trustedNormals = pose.trustedNormals;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }

        public Vector3f transformNormal(Vector3fc vector3fc, Vector3f vector3f) {
            return this.transformNormal(vector3fc.x(), vector3fc.y(), vector3fc.z(), vector3f);
        }

        public Vector3f transformNormal(float f, float f2, float f3, Vector3f vector3f) {
            Vector3f vector3f2 = this.normal.transform(f, f2, f3, vector3f);
            return this.trustedNormals ? vector3f2 : vector3f2.normalize();
        }

        public Matrix4f translate(float f, float f2, float f3) {
            return this.pose.translate(f, f2, f3);
        }

        public void scale(float f, float f2, float f3) {
            this.pose.scale(f, f2, f3);
            if (Math.abs(f) == Math.abs(f2) && Math.abs(f2) == Math.abs(f3)) {
                if (f < 0.0f || f2 < 0.0f || f3 < 0.0f) {
                    this.normal.scale(Math.signum(f), Math.signum(f2), Math.signum(f3));
                }
                return;
            }
            this.normal.scale(1.0f / f, 1.0f / f2, 1.0f / f3);
            this.trustedNormals = false;
        }

        public void rotate(Quaternionfc quaternionfc) {
            this.pose.rotate(quaternionfc);
            this.normal.rotate(quaternionfc);
        }

        public void rotateAround(Quaternionfc quaternionfc, float f, float f2, float f3) {
            this.pose.rotateAround(quaternionfc, f, f2, f3);
            this.normal.rotate(quaternionfc);
        }

        public void setIdentity() {
            this.pose.identity();
            this.normal.identity();
            this.trustedNormals = true;
        }

        public void mulPose(Matrix4fc matrix4fc) {
            this.pose.mul(matrix4fc);
            if (!MatrixUtil.isPureTranslation(matrix4fc)) {
                if (MatrixUtil.isOrthonormal(matrix4fc)) {
                    this.normal.mul((Matrix3fc)new Matrix3f(matrix4fc));
                } else {
                    this.computeNormalMatrix();
                }
            }
        }

        public Pose copy() {
            Pose pose = new Pose();
            pose.set(this);
            return pose;
        }
    }
}

