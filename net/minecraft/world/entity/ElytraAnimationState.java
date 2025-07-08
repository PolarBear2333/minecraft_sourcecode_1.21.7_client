/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraAnimationState {
    private static final float DEFAULT_X_ROT = 0.2617994f;
    private static final float DEFAULT_Z_ROT = -0.2617994f;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float rotXOld;
    private float rotYOld;
    private float rotZOld;
    private final LivingEntity entity;

    public ElytraAnimationState(LivingEntity livingEntity) {
        this.entity = livingEntity;
    }

    public void tick() {
        float f;
        float f2;
        float f3;
        this.rotXOld = this.rotX;
        this.rotYOld = this.rotY;
        this.rotZOld = this.rotZ;
        if (this.entity.isFallFlying()) {
            float f4 = 1.0f;
            Vec3 vec3 = this.entity.getDeltaMovement();
            if (vec3.y < 0.0) {
                Vec3 vec32 = vec3.normalize();
                f4 = 1.0f - (float)Math.pow(-vec32.y, 1.5);
            }
            f3 = Mth.lerp(f4, 0.2617994f, 0.34906584f);
            f2 = Mth.lerp(f4, -0.2617994f, -1.5707964f);
            f = 0.0f;
        } else if (this.entity.isCrouching()) {
            f3 = 0.6981317f;
            f2 = -0.7853982f;
            f = 0.08726646f;
        } else {
            f3 = 0.2617994f;
            f2 = -0.2617994f;
            f = 0.0f;
        }
        this.rotX += (f3 - this.rotX) * 0.3f;
        this.rotY += (f - this.rotY) * 0.3f;
        this.rotZ += (f2 - this.rotZ) * 0.3f;
    }

    public float getRotX(float f) {
        return Mth.lerp(f, this.rotXOld, this.rotX);
    }

    public float getRotY(float f) {
        return Mth.lerp(f, this.rotYOld, this.rotY);
    }

    public float getRotZ(float f) {
        return Mth.lerp(f, this.rotZOld, this.rotZ);
    }
}

