/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public abstract class SingleQuadParticle
extends Particle {
    protected float quadSize;

    protected SingleQuadParticle(ClientLevel clientLevel, double d, double d2, double d3) {
        super(clientLevel, d, d2, d3);
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    protected SingleQuadParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    public FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_XYZ;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Quaternionf quaternionf = new Quaternionf();
        this.getFacingCameraMode().setRotation(quaternionf, camera, f);
        if (this.roll != 0.0f) {
            quaternionf.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
        }
        this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
    }

    protected void renderRotatedQuad(VertexConsumer vertexConsumer, Camera camera, Quaternionf quaternionf, float f) {
        Vec3 vec3 = camera.getPosition();
        float f2 = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
        float f3 = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
        float f4 = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
        this.renderRotatedQuad(vertexConsumer, quaternionf, f2, f3, f4, f);
    }

    protected void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float f, float f2, float f3, float f4) {
        float f5 = this.getQuadSize(f4);
        float f6 = this.getU0();
        float f7 = this.getU1();
        float f8 = this.getV0();
        float f9 = this.getV1();
        int n = this.getLightColor(f4);
        this.renderVertex(vertexConsumer, quaternionf, f, f2, f3, 1.0f, -1.0f, f5, f7, f9, n);
        this.renderVertex(vertexConsumer, quaternionf, f, f2, f3, 1.0f, 1.0f, f5, f7, f8, n);
        this.renderVertex(vertexConsumer, quaternionf, f, f2, f3, -1.0f, 1.0f, f5, f6, f8, n);
        this.renderVertex(vertexConsumer, quaternionf, f, f2, f3, -1.0f, -1.0f, f5, f6, f9, n);
    }

    private void renderVertex(VertexConsumer vertexConsumer, Quaternionf quaternionf, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n) {
        Vector3f vector3f = new Vector3f(f4, f5, 0.0f).rotate((Quaternionfc)quaternionf).mul(f6).add(f, f2, f3);
        vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(f7, f8).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(n);
    }

    public float getQuadSize(float f) {
        return this.quadSize;
    }

    @Override
    public Particle scale(float f) {
        this.quadSize *= f;
        return super.scale(f);
    }

    protected abstract float getU0();

    protected abstract float getU1();

    protected abstract float getV0();

    protected abstract float getV1();

    public static interface FacingCameraMode {
        public static final FacingCameraMode LOOKAT_XYZ = (quaternionf, camera, f) -> quaternionf.set((Quaternionfc)camera.rotation());
        public static final FacingCameraMode LOOKAT_Y = (quaternionf, camera, f) -> quaternionf.set(0.0f, camera.rotation().y, 0.0f, camera.rotation().w);

        public void setRotation(Quaternionf var1, Camera var2, float var3);
    }
}

