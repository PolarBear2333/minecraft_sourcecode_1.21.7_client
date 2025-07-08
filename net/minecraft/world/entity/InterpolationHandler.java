/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class InterpolationHandler {
    public static final int DEFAULT_INTERPOLATION_STEPS = 3;
    private final Entity entity;
    private int interpolationSteps;
    private final InterpolationData interpolationData = new InterpolationData(0, Vec3.ZERO, 0.0f, 0.0f);
    @Nullable
    private Vec3 previousTickPosition;
    @Nullable
    private Vec2 previousTickRot;
    @Nullable
    private final Consumer<InterpolationHandler> onInterpolationStart;

    public InterpolationHandler(Entity entity) {
        this(entity, 3, null);
    }

    public InterpolationHandler(Entity entity, int n) {
        this(entity, n, null);
    }

    public InterpolationHandler(Entity entity, @Nullable Consumer<InterpolationHandler> consumer) {
        this(entity, 3, consumer);
    }

    public InterpolationHandler(Entity entity, int n, @Nullable Consumer<InterpolationHandler> consumer) {
        this.interpolationSteps = n;
        this.entity = entity;
        this.onInterpolationStart = consumer;
    }

    public Vec3 position() {
        return this.interpolationData.steps > 0 ? this.interpolationData.position : this.entity.position();
    }

    public float yRot() {
        return this.interpolationData.steps > 0 ? this.interpolationData.yRot : this.entity.getYRot();
    }

    public float xRot() {
        return this.interpolationData.steps > 0 ? this.interpolationData.xRot : this.entity.getXRot();
    }

    public void interpolateTo(Vec3 vec3, float f, float f2) {
        if (this.interpolationSteps == 0) {
            this.entity.snapTo(vec3, f, f2);
            this.cancel();
            return;
        }
        this.interpolationData.steps = this.interpolationSteps;
        this.interpolationData.position = vec3;
        this.interpolationData.yRot = f;
        this.interpolationData.xRot = f2;
        this.previousTickPosition = this.entity.position();
        this.previousTickRot = new Vec2(this.entity.getXRot(), this.entity.getYRot());
        if (this.onInterpolationStart != null) {
            this.onInterpolationStart.accept(this);
        }
    }

    public boolean hasActiveInterpolation() {
        return this.interpolationData.steps > 0;
    }

    public void setInterpolationLength(int n) {
        this.interpolationSteps = n;
    }

    public void interpolate() {
        if (!this.hasActiveInterpolation()) {
            this.cancel();
            return;
        }
        double d = 1.0 / (double)this.interpolationData.steps;
        if (this.previousTickPosition != null) {
            Vec3 vec3 = this.entity.position().subtract(this.previousTickPosition);
            if (this.entity.level().noCollision(this.entity, this.entity.makeBoundingBox(this.interpolationData.position.add(vec3)))) {
                this.interpolationData.addDelta(vec3);
            }
        }
        if (this.previousTickRot != null) {
            float f = this.entity.getYRot() - this.previousTickRot.y;
            float f2 = this.entity.getXRot() - this.previousTickRot.x;
            this.interpolationData.addRotation(f, f2);
        }
        double d2 = Mth.lerp(d, this.entity.getX(), this.interpolationData.position.x);
        double d3 = Mth.lerp(d, this.entity.getY(), this.interpolationData.position.y);
        double d4 = Mth.lerp(d, this.entity.getZ(), this.interpolationData.position.z);
        Vec3 vec3 = new Vec3(d2, d3, d4);
        float f = (float)Mth.rotLerp(d, (double)this.entity.getYRot(), (double)this.interpolationData.yRot);
        float f3 = (float)Mth.lerp(d, (double)this.entity.getXRot(), (double)this.interpolationData.xRot);
        this.entity.setPos(vec3);
        this.entity.setRot(f, f3);
        this.interpolationData.decrease();
        this.previousTickPosition = vec3;
        this.previousTickRot = new Vec2(this.entity.getXRot(), this.entity.getYRot());
    }

    public void cancel() {
        this.interpolationData.steps = 0;
        this.previousTickPosition = null;
        this.previousTickRot = null;
    }

    static class InterpolationData {
        protected int steps;
        Vec3 position;
        float yRot;
        float xRot;

        InterpolationData(int n, Vec3 vec3, float f, float f2) {
            this.steps = n;
            this.position = vec3;
            this.yRot = f;
            this.xRot = f2;
        }

        public void decrease() {
            --this.steps;
        }

        public void addDelta(Vec3 vec3) {
            this.position = this.position.add(vec3);
        }

        public void addRotation(float f, float f2) {
            this.yRot += f;
            this.xRot += f2;
        }
    }
}

