/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState> {
    protected static final float NAMETAG_SCALE = 0.025f;
    public static final int LEASH_RENDER_STEPS = 24;
    public static final float LEASH_WIDTH = 0.05f;
    protected final EntityRenderDispatcher entityRenderDispatcher;
    private final Font font;
    protected float shadowRadius;
    protected float shadowStrength = 1.0f;
    private final S reusedState = this.createRenderState();

    protected EntityRenderer(EntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.font = context.getFont();
    }

    public final int getPackedLightCoords(T t, float f) {
        BlockPos blockPos = BlockPos.containing(((Entity)t).getLightProbePosition(f));
        return LightTexture.pack(this.getBlockLightLevel(t, blockPos), this.getSkyLightLevel(t, blockPos));
    }

    protected int getSkyLightLevel(T t, BlockPos blockPos) {
        return ((Entity)t).level().getBrightness(LightLayer.SKY, blockPos);
    }

    protected int getBlockLightLevel(T t, BlockPos blockPos) {
        if (((Entity)t).isOnFire()) {
            return 15;
        }
        return ((Entity)t).level().getBrightness(LightLayer.BLOCK, blockPos);
    }

    public boolean shouldRender(T t, Frustum frustum, double d, double d2, double d3) {
        Leashable leashable;
        Entity entity;
        if (!((Entity)t).shouldRender(d, d2, d3)) {
            return false;
        }
        if (!this.affectedByCulling(t)) {
            return true;
        }
        AABB aABB = this.getBoundingBoxForCulling(t).inflate(0.5);
        if (aABB.hasNaN() || aABB.getSize() == 0.0) {
            aABB = new AABB(((Entity)t).getX() - 2.0, ((Entity)t).getY() - 2.0, ((Entity)t).getZ() - 2.0, ((Entity)t).getX() + 2.0, ((Entity)t).getY() + 2.0, ((Entity)t).getZ() + 2.0);
        }
        if (frustum.isVisible(aABB)) {
            return true;
        }
        if (t instanceof Leashable && (entity = (leashable = (Leashable)t).getLeashHolder()) != null) {
            AABB aABB2 = this.entityRenderDispatcher.getRenderer(entity).getBoundingBoxForCulling(entity);
            return frustum.isVisible(aABB2) || frustum.isVisible(aABB.minmax(aABB2));
        }
        return false;
    }

    protected AABB getBoundingBoxForCulling(T t) {
        return ((Entity)t).getBoundingBox();
    }

    protected boolean affectedByCulling(T t) {
        return true;
    }

    public Vec3 getRenderOffset(S s) {
        if (((EntityRenderState)s).passengerOffset != null) {
            return ((EntityRenderState)s).passengerOffset;
        }
        return Vec3.ZERO;
    }

    public void render(S s, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (((EntityRenderState)s).leashStates != null) {
            for (EntityRenderState.LeashState leashState : ((EntityRenderState)s).leashStates) {
                EntityRenderer.renderLeash(poseStack, multiBufferSource, leashState);
            }
        }
        if (((EntityRenderState)s).nameTag != null) {
            this.renderNameTag(s, ((EntityRenderState)s).nameTag, poseStack, multiBufferSource, n);
        }
    }

    private static void renderLeash(PoseStack poseStack, MultiBufferSource multiBufferSource, EntityRenderState.LeashState leashState) {
        int n;
        float f = (float)(leashState.end.x - leashState.start.x);
        float f2 = (float)(leashState.end.y - leashState.start.y);
        float f3 = (float)(leashState.end.z - leashState.start.z);
        float f4 = Mth.invSqrt(f * f + f3 * f3) * 0.05f / 2.0f;
        float f5 = f3 * f4;
        float f6 = f * f4;
        poseStack.pushPose();
        poseStack.translate(leashState.offset);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        for (n = 0; n <= 24; ++n) {
            EntityRenderer.addVertexPair(vertexConsumer, matrix4f, f, f2, f3, 0.05f, 0.05f, f5, f6, n, false, leashState);
        }
        for (n = 24; n >= 0; --n) {
            EntityRenderer.addVertexPair(vertexConsumer, matrix4f, f, f2, f3, 0.05f, 0.0f, f5, f6, n, true, leashState);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, int n, boolean bl, EntityRenderState.LeashState leashState) {
        float f8 = (float)n / 24.0f;
        int n2 = (int)Mth.lerp(f8, leashState.startBlockLight, leashState.endBlockLight);
        int n3 = (int)Mth.lerp(f8, leashState.startSkyLight, leashState.endSkyLight);
        int n4 = LightTexture.pack(n2, n3);
        float f9 = n % 2 == (bl ? 1 : 0) ? 0.7f : 1.0f;
        float f10 = 0.5f * f9;
        float f11 = 0.4f * f9;
        float f12 = 0.3f * f9;
        float f13 = f * f8;
        float f14 = leashState.slack ? (f2 > 0.0f ? f2 * f8 * f8 : f2 - f2 * (1.0f - f8) * (1.0f - f8)) : f2 * f8;
        float f15 = f3 * f8;
        vertexConsumer.addVertex(matrix4f, f13 - f6, f14 + f5, f15 + f7).setColor(f10, f11, f12, 1.0f).setLight(n4);
        vertexConsumer.addVertex(matrix4f, f13 + f6, f14 + f4 - f5, f15 - f7).setColor(f10, f11, f12, 1.0f).setLight(n4);
    }

    protected boolean shouldShowName(T t, double d) {
        return ((Entity)t).shouldShowName() || ((Entity)t).hasCustomName() && t == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public Font getFont() {
        return this.font;
    }

    protected void renderNameTag(S s, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        Vec3 vec3 = ((EntityRenderState)s).nameTagAttachment;
        if (vec3 == null) {
            return;
        }
        boolean bl = !((EntityRenderState)s).isDiscrete;
        int n2 = "deadmau5".equals(component.getString()) ? -10 : 0;
        poseStack.pushPose();
        poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
        poseStack.mulPose((Quaternionfc)this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = this.getFont();
        float f = (float)(-font.width(component)) / 2.0f;
        int n3 = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 255.0f) << 24;
        font.drawInBatch(component, f, (float)n2, -2130706433, false, matrix4f, multiBufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, n3, n);
        if (bl) {
            font.drawInBatch(component, f, (float)n2, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.lightCoordsWithEmission(n, 2));
        }
        poseStack.popPose();
    }

    @Nullable
    protected Component getNameTag(T t) {
        return ((Entity)t).getDisplayName();
    }

    protected float getShadowRadius(S s) {
        return this.shadowRadius;
    }

    protected float getShadowStrength(S s) {
        return this.shadowStrength;
    }

    public abstract S createRenderState();

    public final S createRenderState(T t, float f) {
        S s = this.reusedState;
        this.extractRenderState(t, s, f);
        return s;
    }

    public void extractRenderState(T t, S s, float f) {
        Entity entity;
        boolean bl;
        Object object;
        AbstractMinecart abstractMinecart;
        Object object2;
        ((EntityRenderState)s).entityType = ((Entity)t).getType();
        ((EntityRenderState)s).x = Mth.lerp((double)f, ((Entity)t).xOld, ((Entity)t).getX());
        ((EntityRenderState)s).y = Mth.lerp((double)f, ((Entity)t).yOld, ((Entity)t).getY());
        ((EntityRenderState)s).z = Mth.lerp((double)f, ((Entity)t).zOld, ((Entity)t).getZ());
        ((EntityRenderState)s).isInvisible = ((Entity)t).isInvisible();
        ((EntityRenderState)s).ageInTicks = (float)((Entity)t).tickCount + f;
        ((EntityRenderState)s).boundingBoxWidth = ((Entity)t).getBbWidth();
        ((EntityRenderState)s).boundingBoxHeight = ((Entity)t).getBbHeight();
        ((EntityRenderState)s).eyeHeight = ((Entity)t).getEyeHeight();
        if (((Entity)t).isPassenger() && (object2 = ((Entity)t).getVehicle()) instanceof AbstractMinecart && (object2 = (abstractMinecart = (AbstractMinecart)object2).getBehavior()) instanceof NewMinecartBehavior && ((NewMinecartBehavior)(object = (NewMinecartBehavior)object2)).cartHasPosRotLerp()) {
            double d = Mth.lerp((double)f, abstractMinecart.xOld, abstractMinecart.getX());
            double d2 = Mth.lerp((double)f, abstractMinecart.yOld, abstractMinecart.getY());
            double d3 = Mth.lerp((double)f, abstractMinecart.zOld, abstractMinecart.getZ());
            ((EntityRenderState)s).passengerOffset = ((NewMinecartBehavior)object).getCartLerpPosition(f).subtract(new Vec3(d, d2, d3));
        } else {
            ((EntityRenderState)s).passengerOffset = null;
        }
        ((EntityRenderState)s).distanceToCameraSq = this.entityRenderDispatcher.distanceToSqr((Entity)t);
        boolean bl2 = bl = ((EntityRenderState)s).distanceToCameraSq < 4096.0 && this.shouldShowName(t, ((EntityRenderState)s).distanceToCameraSq);
        if (bl) {
            ((EntityRenderState)s).nameTag = this.getNameTag(t);
            ((EntityRenderState)s).nameTagAttachment = ((Entity)t).getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, ((Entity)t).getYRot(f));
        } else {
            ((EntityRenderState)s).nameTag = null;
        }
        ((EntityRenderState)s).isDiscrete = ((Entity)t).isDiscrete();
        if (t instanceof Leashable && (entity = (object = (Leashable)t).getLeashHolder()) instanceof Entity) {
            int n;
            Entity entity2 = entity;
            float f2 = ((Entity)t).getPreciseBodyRotation(f) * ((float)Math.PI / 180);
            Vec3 vec3 = object.getLeashOffset(f);
            BlockPos blockPos = BlockPos.containing(((Entity)t).getEyePosition(f));
            BlockPos blockPos2 = BlockPos.containing(entity2.getEyePosition(f));
            int n2 = this.getBlockLightLevel(t, blockPos);
            int n3 = this.entityRenderDispatcher.getRenderer(entity2).getBlockLightLevel(entity2, blockPos2);
            int n4 = ((Entity)t).level().getBrightness(LightLayer.SKY, blockPos);
            int n5 = ((Entity)t).level().getBrightness(LightLayer.SKY, blockPos2);
            boolean bl3 = entity2.supportQuadLeashAsHolder() && object.supportQuadLeash();
            int n6 = n = bl3 ? 4 : 1;
            if (((EntityRenderState)s).leashStates == null || ((EntityRenderState)s).leashStates.size() != n) {
                ((EntityRenderState)s).leashStates = new ArrayList<EntityRenderState.LeashState>(n);
                for (int i = 0; i < n; ++i) {
                    ((EntityRenderState)s).leashStates.add(new EntityRenderState.LeashState());
                }
            }
            if (bl3) {
                float f3 = entity2.getPreciseBodyRotation(f) * ((float)Math.PI / 180);
                Vec3 vec32 = entity2.getPosition(f);
                Vec3[] vec3Array = object.getQuadLeashOffsets();
                Vec3[] vec3Array2 = entity2.getQuadLeashHolderOffsets();
                for (int i = 0; i < n; ++i) {
                    EntityRenderState.LeashState leashState = ((EntityRenderState)s).leashStates.get(i);
                    leashState.offset = vec3Array[i].yRot(-f2);
                    leashState.start = ((Entity)t).getPosition(f).add(leashState.offset);
                    leashState.end = vec32.add(vec3Array2[i].yRot(-f3));
                    leashState.startBlockLight = n2;
                    leashState.endBlockLight = n3;
                    leashState.startSkyLight = n4;
                    leashState.endSkyLight = n5;
                    leashState.slack = false;
                }
            } else {
                Vec3 vec33 = vec3.yRot(-f2);
                EntityRenderState.LeashState leashState = ((EntityRenderState)s).leashStates.getFirst();
                leashState.offset = vec33;
                leashState.start = ((Entity)t).getPosition(f).add(vec33);
                leashState.end = entity2.getRopeHoldPosition(f);
                leashState.startBlockLight = n2;
                leashState.endBlockLight = n3;
                leashState.startSkyLight = n4;
                leashState.endSkyLight = n5;
            }
        } else {
            ((EntityRenderState)s).leashStates = null;
        }
        ((EntityRenderState)s).displayFireAnimation = ((Entity)t).displayFireAnimation();
        object = Minecraft.getInstance();
        if (((Minecraft)object).getEntityRenderDispatcher().shouldRenderHitBoxes() && !((EntityRenderState)s).isInvisible && !((Minecraft)object).showOnlyReducedInfo()) {
            this.extractHitboxes(t, s, f);
        } else {
            ((EntityRenderState)s).hitboxesRenderState = null;
            ((EntityRenderState)s).serverHitboxesRenderState = null;
        }
    }

    private void extractHitboxes(T t, S s, float f) {
        ((EntityRenderState)s).hitboxesRenderState = this.extractHitboxes(t, f, false);
        ((EntityRenderState)s).serverHitboxesRenderState = null;
    }

    private HitboxesRenderState extractHitboxes(T t, float f, boolean bl) {
        ImmutableList.Builder builder = new ImmutableList.Builder();
        AABB aABB = ((Entity)t).getBoundingBox();
        HitboxRenderState hitboxRenderState = bl ? new HitboxRenderState(aABB.minX - ((Entity)t).getX(), aABB.minY - ((Entity)t).getY(), aABB.minZ - ((Entity)t).getZ(), aABB.maxX - ((Entity)t).getX(), aABB.maxY - ((Entity)t).getY(), aABB.maxZ - ((Entity)t).getZ(), 0.0f, 1.0f, 0.0f) : new HitboxRenderState(aABB.minX - ((Entity)t).getX(), aABB.minY - ((Entity)t).getY(), aABB.minZ - ((Entity)t).getZ(), aABB.maxX - ((Entity)t).getX(), aABB.maxY - ((Entity)t).getY(), aABB.maxZ - ((Entity)t).getZ(), 1.0f, 1.0f, 1.0f);
        builder.add((Object)hitboxRenderState);
        Entity entity = ((Entity)t).getVehicle();
        if (entity != null) {
            float f2 = Math.min(entity.getBbWidth(), ((Entity)t).getBbWidth()) / 2.0f;
            float f3 = 0.0625f;
            Vec3 vec3 = entity.getPassengerRidingPosition((Entity)t).subtract(((Entity)t).position());
            HitboxRenderState hitboxRenderState2 = new HitboxRenderState(vec3.x - (double)f2, vec3.y, vec3.z - (double)f2, vec3.x + (double)f2, vec3.y + 0.0625, vec3.z + (double)f2, 1.0f, 1.0f, 0.0f);
            builder.add((Object)hitboxRenderState2);
        }
        this.extractAdditionalHitboxes(t, (ImmutableList.Builder<HitboxRenderState>)builder, f);
        Vec3 vec3 = ((Entity)t).getViewVector(f);
        return new HitboxesRenderState(vec3.x, vec3.y, vec3.z, (ImmutableList<HitboxRenderState>)builder.build());
    }

    protected void extractAdditionalHitboxes(T t, ImmutableList.Builder<HitboxRenderState> builder, float f) {
    }

    @Nullable
    private static Entity getServerSideEntity(Entity entity) {
        ServerLevel serverLevel;
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer != null && (serverLevel = integratedServer.getLevel(entity.level().dimension())) != null) {
            return serverLevel.getEntity(entity.getId());
        }
        return null;
    }
}

