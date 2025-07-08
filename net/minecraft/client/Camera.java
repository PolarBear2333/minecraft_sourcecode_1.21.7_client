/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Camera
implements TrackedWaypoint.Camera {
    private static final float DEFAULT_CAMERA_DISTANCE = 4.0f;
    private static final Vector3f FORWARDS = new Vector3f(0.0f, 0.0f, -1.0f);
    private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final Vector3f LEFT = new Vector3f(-1.0f, 0.0f, 0.0f);
    private boolean initialized;
    private BlockGetter level;
    private Entity entity;
    private Vec3 position = Vec3.ZERO;
    private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
    private final Vector3f forwards = new Vector3f((Vector3fc)FORWARDS);
    private final Vector3f up = new Vector3f((Vector3fc)UP);
    private final Vector3f left = new Vector3f((Vector3fc)LEFT);
    private float xRot;
    private float yRot;
    private final Quaternionf rotation = new Quaternionf();
    private boolean detached;
    private float eyeHeight;
    private float eyeHeightOld;
    private float partialTickTime;
    public static final float FOG_DISTANCE_SCALE = 0.083333336f;

    public void setup(BlockGetter blockGetter, Entity entity, boolean bl, boolean bl2, float f) {
        NewMinecartBehavior newMinecartBehavior;
        Object object;
        Object object2;
        this.initialized = true;
        this.level = blockGetter;
        this.entity = entity;
        this.detached = bl;
        this.partialTickTime = f;
        if (entity.isPassenger() && (object2 = entity.getVehicle()) instanceof Minecart && (object2 = ((AbstractMinecart)(object = (Minecart)object2)).getBehavior()) instanceof NewMinecartBehavior && (newMinecartBehavior = (NewMinecartBehavior)object2).cartHasPosRotLerp()) {
            object2 = ((Entity)object).getPassengerRidingPosition(entity).subtract(((Entity)object).position()).subtract(entity.getVehicleAttachmentPoint((Entity)object)).add(new Vec3(0.0, Mth.lerp(f, this.eyeHeightOld, this.eyeHeight), 0.0));
            this.setRotation(entity.getViewYRot(f), entity.getViewXRot(f));
            this.setPosition(newMinecartBehavior.getCartLerpPosition(f).add((Vec3)object2));
        } else {
            this.setRotation(entity.getViewYRot(f), entity.getViewXRot(f));
            this.setPosition(Mth.lerp((double)f, entity.xo, entity.getX()), Mth.lerp((double)f, entity.yo, entity.getY()) + (double)Mth.lerp(f, this.eyeHeightOld, this.eyeHeight), Mth.lerp((double)f, entity.zo, entity.getZ()));
        }
        if (bl) {
            Entity entity2;
            if (bl2) {
                this.setRotation(this.yRot + 180.0f, -this.xRot);
            }
            float f2 = 4.0f;
            float f3 = 1.0f;
            if (entity instanceof LivingEntity) {
                object2 = (LivingEntity)entity;
                f3 = ((LivingEntity)object2).getScale();
                f2 = (float)((LivingEntity)object2).getAttributeValue(Attributes.CAMERA_DISTANCE);
            }
            float f4 = f3;
            float f5 = f2;
            if (entity.isPassenger() && (entity2 = entity.getVehicle()) instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity2;
                f4 = livingEntity.getScale();
                f5 = (float)livingEntity.getAttributeValue(Attributes.CAMERA_DISTANCE);
            }
            this.move(-this.getMaxZoom(Math.max(f3 * f2, f4 * f5)), 0.0f, 0.0f);
        } else if (entity instanceof LivingEntity && ((LivingEntity)entity).isSleeping()) {
            object = ((LivingEntity)entity).getBedOrientation();
            this.setRotation(object != null ? ((Direction)object).toYRot() - 180.0f : 0.0f, 0.0f);
            this.move(0.0f, 0.3f, 0.0f);
        }
    }

    public void tick() {
        if (this.entity != null) {
            this.eyeHeightOld = this.eyeHeight;
            this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5f;
        }
    }

    private float getMaxZoom(float f) {
        float f2 = 0.1f;
        for (int i = 0; i < 8; ++i) {
            float f3;
            Vec3 vec3;
            float f4 = (i & 1) * 2 - 1;
            float f5 = (i >> 1 & 1) * 2 - 1;
            float f6 = (i >> 2 & 1) * 2 - 1;
            Vec3 vec32 = this.position.add(f4 * 0.1f, f5 * 0.1f, f6 * 0.1f);
            BlockHitResult blockHitResult = this.level.clip(new ClipContext(vec32, vec3 = vec32.add(new Vec3(this.forwards).scale(-f)), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
            if (((HitResult)blockHitResult).getType() == HitResult.Type.MISS || !((f3 = (float)blockHitResult.getLocation().distanceToSqr(this.position)) < Mth.square(f))) continue;
            f = Mth.sqrt(f3);
        }
        return f;
    }

    protected void move(float f, float f2, float f3) {
        Vector3f vector3f = new Vector3f(f3, f2, -f).rotate((Quaternionfc)this.rotation);
        this.setPosition(new Vec3(this.position.x + (double)vector3f.x, this.position.y + (double)vector3f.y, this.position.z + (double)vector3f.z));
    }

    protected void setRotation(float f, float f2) {
        this.xRot = f2;
        this.yRot = f;
        this.rotation.rotationYXZ((float)Math.PI - f * ((float)Math.PI / 180), -f2 * ((float)Math.PI / 180), 0.0f);
        FORWARDS.rotate((Quaternionfc)this.rotation, this.forwards);
        UP.rotate((Quaternionfc)this.rotation, this.up);
        LEFT.rotate((Quaternionfc)this.rotation, this.left);
    }

    protected void setPosition(double d, double d2, double d3) {
        this.setPosition(new Vec3(d, d2, d3));
    }

    protected void setPosition(Vec3 vec3) {
        this.position = vec3;
        this.blockPosition.set(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }

    public float getXRot() {
        return this.xRot;
    }

    public float getYRot() {
        return this.yRot;
    }

    public Quaternionf rotation() {
        return this.rotation;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isDetached() {
        return this.detached;
    }

    public NearPlane getNearPlane() {
        Minecraft minecraft = Minecraft.getInstance();
        double d = (double)minecraft.getWindow().getWidth() / (double)minecraft.getWindow().getHeight();
        double d2 = Math.tan((double)((float)minecraft.options.fov().get().intValue() * ((float)Math.PI / 180)) / 2.0) * (double)0.05f;
        double d3 = d2 * d;
        Vec3 vec3 = new Vec3(this.forwards).scale(0.05f);
        Vec3 vec32 = new Vec3(this.left).scale(d3);
        Vec3 vec33 = new Vec3(this.up).scale(d2);
        return new NearPlane(vec3, vec32, vec33);
    }

    public FogType getFluidInCamera() {
        if (!this.initialized) {
            return FogType.NONE;
        }
        FluidState fluidState = this.level.getFluidState(this.blockPosition);
        if (fluidState.is(FluidTags.WATER) && this.position.y < (double)((float)this.blockPosition.getY() + fluidState.getHeight(this.level, this.blockPosition))) {
            return FogType.WATER;
        }
        NearPlane nearPlane = this.getNearPlane();
        List<Vec3> list = Arrays.asList(nearPlane.forward, nearPlane.getTopLeft(), nearPlane.getTopRight(), nearPlane.getBottomLeft(), nearPlane.getBottomRight());
        for (Vec3 vec3 : list) {
            Vec3 vec32 = this.position.add(vec3);
            BlockPos blockPos = BlockPos.containing(vec32);
            FluidState fluidState2 = this.level.getFluidState(blockPos);
            if (fluidState2.is(FluidTags.LAVA)) {
                if (!(vec32.y <= (double)(fluidState2.getHeight(this.level, blockPos) + (float)blockPos.getY()))) continue;
                return FogType.LAVA;
            }
            BlockState blockState = this.level.getBlockState(blockPos);
            if (!blockState.is(Blocks.POWDER_SNOW)) continue;
            return FogType.POWDER_SNOW;
        }
        return FogType.NONE;
    }

    public final Vector3f getLookVector() {
        return this.forwards;
    }

    public final Vector3f getUpVector() {
        return this.up;
    }

    public final Vector3f getLeftVector() {
        return this.left;
    }

    public void reset() {
        this.level = null;
        this.entity = null;
        this.initialized = false;
    }

    public float getPartialTickTime() {
        return this.partialTickTime;
    }

    @Override
    public float yaw() {
        return Mth.wrapDegrees(this.getYRot());
    }

    @Override
    public Vec3 position() {
        return this.getPosition();
    }

    public static class NearPlane {
        final Vec3 forward;
        private final Vec3 left;
        private final Vec3 up;

        NearPlane(Vec3 vec3, Vec3 vec32, Vec3 vec33) {
            this.forward = vec3;
            this.left = vec32;
            this.up = vec33;
        }

        public Vec3 getTopLeft() {
            return this.forward.add(this.up).add(this.left);
        }

        public Vec3 getTopRight() {
            return this.forward.add(this.up).subtract(this.left);
        }

        public Vec3 getBottomLeft() {
            return this.forward.subtract(this.up).add(this.left);
        }

        public Vec3 getBottomRight() {
            return this.forward.subtract(this.up).subtract(this.left);
        }

        public Vec3 getPointOnPlane(float f, float f2) {
            return this.forward.add(this.up.scale(f2)).subtract(this.left.scale(f));
        }
    }
}

