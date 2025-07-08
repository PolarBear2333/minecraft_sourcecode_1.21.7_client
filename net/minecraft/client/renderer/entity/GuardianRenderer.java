/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class GuardianRenderer
extends MobRenderer<Guardian, GuardianRenderState, GuardianModel> {
    private static final ResourceLocation GUARDIAN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

    public GuardianRenderer(EntityRendererProvider.Context context) {
        this(context, 0.5f, ModelLayers.GUARDIAN);
    }

    protected GuardianRenderer(EntityRendererProvider.Context context, float f, ModelLayerLocation modelLayerLocation) {
        super(context, new GuardianModel(context.bakeLayer(modelLayerLocation)), f);
    }

    @Override
    public boolean shouldRender(Guardian guardian, Frustum frustum, double d, double d2, double d3) {
        LivingEntity livingEntity;
        if (super.shouldRender(guardian, frustum, d, d2, d3)) {
            return true;
        }
        if (guardian.hasActiveAttackTarget() && (livingEntity = guardian.getActiveAttackTarget()) != null) {
            Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, 1.0f);
            Vec3 vec32 = this.getPosition(guardian, guardian.getEyeHeight(), 1.0f);
            return frustum.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z));
        }
        return false;
    }

    private Vec3 getPosition(LivingEntity livingEntity, double d, float f) {
        double d2 = Mth.lerp((double)f, livingEntity.xOld, livingEntity.getX());
        double d3 = Mth.lerp((double)f, livingEntity.yOld, livingEntity.getY()) + d;
        double d4 = Mth.lerp((double)f, livingEntity.zOld, livingEntity.getZ());
        return new Vec3(d2, d3, d4);
    }

    @Override
    public void render(GuardianRenderState guardianRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        super.render(guardianRenderState, poseStack, multiBufferSource, n);
        Vec3 vec3 = guardianRenderState.attackTargetPosition;
        if (vec3 != null) {
            float f = guardianRenderState.attackTime * 0.5f % 1.0f;
            poseStack.pushPose();
            poseStack.translate(0.0f, guardianRenderState.eyeHeight, 0.0f);
            GuardianRenderer.renderBeam(poseStack, multiBufferSource.getBuffer(BEAM_RENDER_TYPE), vec3.subtract(guardianRenderState.eyePosition), guardianRenderState.attackTime, guardianRenderState.attackScale, f);
            poseStack.popPose();
        }
    }

    private static void renderBeam(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 vec3, float f, float f2, float f3) {
        float f4 = (float)(vec3.length() + 1.0);
        vec3 = vec3.normalize();
        float f5 = (float)Math.acos(vec3.y);
        float f6 = 1.5707964f - (float)Math.atan2(vec3.z, vec3.x);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f6 * 57.295776f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f5 * 57.295776f));
        float f7 = f * 0.05f * -1.5f;
        float f8 = f2 * f2;
        int n = 64 + (int)(f8 * 191.0f);
        int n2 = 32 + (int)(f8 * 191.0f);
        int n3 = 128 - (int)(f8 * 64.0f);
        float f9 = 0.2f;
        float f10 = 0.282f;
        float f11 = Mth.cos(f7 + 2.3561945f) * 0.282f;
        float f12 = Mth.sin(f7 + 2.3561945f) * 0.282f;
        float f13 = Mth.cos(f7 + 0.7853982f) * 0.282f;
        float f14 = Mth.sin(f7 + 0.7853982f) * 0.282f;
        float f15 = Mth.cos(f7 + 3.926991f) * 0.282f;
        float f16 = Mth.sin(f7 + 3.926991f) * 0.282f;
        float f17 = Mth.cos(f7 + 5.4977875f) * 0.282f;
        float f18 = Mth.sin(f7 + 5.4977875f) * 0.282f;
        float f19 = Mth.cos(f7 + (float)Math.PI) * 0.2f;
        float f20 = Mth.sin(f7 + (float)Math.PI) * 0.2f;
        float f21 = Mth.cos(f7 + 0.0f) * 0.2f;
        float f22 = Mth.sin(f7 + 0.0f) * 0.2f;
        float f23 = Mth.cos(f7 + 1.5707964f) * 0.2f;
        float f24 = Mth.sin(f7 + 1.5707964f) * 0.2f;
        float f25 = Mth.cos(f7 + 4.712389f) * 0.2f;
        float f26 = Mth.sin(f7 + 4.712389f) * 0.2f;
        float f27 = f4;
        float f28 = 0.0f;
        float f29 = 0.4999f;
        float f30 = -1.0f + f3;
        float f31 = f30 + f4 * 2.5f;
        PoseStack.Pose pose = poseStack.last();
        GuardianRenderer.vertex(vertexConsumer, pose, f19, f27, f20, n, n2, n3, 0.4999f, f31);
        GuardianRenderer.vertex(vertexConsumer, pose, f19, 0.0f, f20, n, n2, n3, 0.4999f, f30);
        GuardianRenderer.vertex(vertexConsumer, pose, f21, 0.0f, f22, n, n2, n3, 0.0f, f30);
        GuardianRenderer.vertex(vertexConsumer, pose, f21, f27, f22, n, n2, n3, 0.0f, f31);
        GuardianRenderer.vertex(vertexConsumer, pose, f23, f27, f24, n, n2, n3, 0.4999f, f31);
        GuardianRenderer.vertex(vertexConsumer, pose, f23, 0.0f, f24, n, n2, n3, 0.4999f, f30);
        GuardianRenderer.vertex(vertexConsumer, pose, f25, 0.0f, f26, n, n2, n3, 0.0f, f30);
        GuardianRenderer.vertex(vertexConsumer, pose, f25, f27, f26, n, n2, n3, 0.0f, f31);
        float f32 = Mth.floor(f) % 2 == 0 ? 0.5f : 0.0f;
        GuardianRenderer.vertex(vertexConsumer, pose, f11, f27, f12, n, n2, n3, 0.5f, f32 + 0.5f);
        GuardianRenderer.vertex(vertexConsumer, pose, f13, f27, f14, n, n2, n3, 1.0f, f32 + 0.5f);
        GuardianRenderer.vertex(vertexConsumer, pose, f17, f27, f18, n, n2, n3, 1.0f, f32);
        GuardianRenderer.vertex(vertexConsumer, pose, f15, f27, f16, n, n2, n3, 0.5f, f32);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float f2, float f3, int n, int n2, int n3, float f4, float f5) {
        vertexConsumer.addVertex(pose, f, f2, f3).setColor(n, n2, n3, 255).setUv(f4, f5).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(GuardianRenderState guardianRenderState) {
        return GUARDIAN_LOCATION;
    }

    @Override
    public GuardianRenderState createRenderState() {
        return new GuardianRenderState();
    }

    @Override
    public void extractRenderState(Guardian guardian, GuardianRenderState guardianRenderState, float f) {
        super.extractRenderState(guardian, guardianRenderState, f);
        guardianRenderState.spikesAnimation = guardian.getSpikesAnimation(f);
        guardianRenderState.tailAnimation = guardian.getTailAnimation(f);
        guardianRenderState.eyePosition = guardian.getEyePosition(f);
        Entity entity = GuardianRenderer.getEntityToLookAt(guardian);
        if (entity != null) {
            guardianRenderState.lookDirection = guardian.getViewVector(f);
            guardianRenderState.lookAtPosition = entity.getEyePosition(f);
        } else {
            guardianRenderState.lookDirection = null;
            guardianRenderState.lookAtPosition = null;
        }
        LivingEntity livingEntity = guardian.getActiveAttackTarget();
        if (livingEntity != null) {
            guardianRenderState.attackScale = guardian.getAttackAnimationScale(f);
            guardianRenderState.attackTime = guardian.getClientSideAttackTime() + f;
            guardianRenderState.attackTargetPosition = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, f);
        } else {
            guardianRenderState.attackTargetPosition = null;
        }
    }

    @Nullable
    private static Entity getEntityToLookAt(Guardian guardian) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (guardian.hasActiveAttackTarget()) {
            return guardian.getActiveAttackTarget();
        }
        return entity;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((GuardianRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

