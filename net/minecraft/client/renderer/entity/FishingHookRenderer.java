/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class FishingHookRenderer
extends EntityRenderer<FishingHook, FishingHookRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0;

    public FishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(FishingHook fishingHook, Frustum frustum, double d, double d2, double d3) {
        return super.shouldRender(fishingHook, frustum, d, d2, d3) && fishingHook.getPlayerOwner() != null;
    }

    @Override
    public void render(FishingHookRenderState fishingHookRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        FishingHookRenderer.vertex(vertexConsumer, pose, n, 0.0f, 0, 0, 1);
        FishingHookRenderer.vertex(vertexConsumer, pose, n, 1.0f, 0, 1, 1);
        FishingHookRenderer.vertex(vertexConsumer, pose, n, 1.0f, 1, 1, 0);
        FishingHookRenderer.vertex(vertexConsumer, pose, n, 0.0f, 1, 0, 0);
        poseStack.popPose();
        float f = (float)fishingHookRenderState.lineOriginOffset.x;
        float f2 = (float)fishingHookRenderState.lineOriginOffset.y;
        float f3 = (float)fishingHookRenderState.lineOriginOffset.z;
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose2 = poseStack.last();
        int n2 = 16;
        for (int i = 0; i <= 16; ++i) {
            FishingHookRenderer.stringVertex(f, f2, f3, vertexConsumer2, pose2, FishingHookRenderer.fraction(i, 16), FishingHookRenderer.fraction(i + 1, 16));
        }
        poseStack.popPose();
        super.render(fishingHookRenderState, poseStack, multiBufferSource, n);
    }

    public static HumanoidArm getHoldingArm(Player player) {
        return player.getMainHandItem().getItem() instanceof FishingRodItem ? player.getMainArm() : player.getMainArm().getOpposite();
    }

    private Vec3 getPlayerHandPos(Player player, float f, float f2) {
        int n;
        int n2 = n = FishingHookRenderer.getHoldingArm(player) == HumanoidArm.RIGHT ? 1 : -1;
        if (!this.entityRenderDispatcher.options.getCameraType().isFirstPerson() || player != Minecraft.getInstance().player) {
            float f3 = Mth.lerp(f2, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180);
            double d = Mth.sin(f3);
            double d2 = Mth.cos(f3);
            float f4 = player.getScale();
            double d3 = (double)n * 0.35 * (double)f4;
            double d4 = 0.8 * (double)f4;
            float f5 = player.isCrouching() ? -0.1875f : 0.0f;
            return player.getEyePosition(f2).add(-d2 * d3 - d * d4, (double)f5 - 0.45 * (double)f4, -d * d3 + d2 * d4);
        }
        double d = 960.0 / (double)this.entityRenderDispatcher.options.fov().get().intValue();
        Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)n * 0.525f, -0.1f).scale(d).yRot(f * 0.5f).xRot(-f * 0.7f);
        return player.getEyePosition(f2).add(vec3);
    }

    private static float fraction(int n, int n2) {
        return (float)n / (float)n2;
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int n, float f, int n2, int n3, int n4) {
        vertexConsumer.addVertex(pose, f - 0.5f, (float)n2 - 0.5f, 0.0f).setColor(-1).setUv(n3, n4).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void stringVertex(float f, float f2, float f3, VertexConsumer vertexConsumer, PoseStack.Pose pose, float f4, float f5) {
        float f6 = f * f4;
        float f7 = f2 * (f4 * f4 + f4) * 0.5f + 0.25f;
        float f8 = f3 * f4;
        float f9 = f * f5 - f6;
        float f10 = f2 * (f5 * f5 + f5) * 0.5f + 0.25f - f7;
        float f11 = f3 * f5 - f8;
        float f12 = Mth.sqrt(f9 * f9 + f10 * f10 + f11 * f11);
        vertexConsumer.addVertex(pose, f6, f7, f8).setColor(-16777216).setNormal(pose, f9 /= f12, f10 /= f12, f11 /= f12);
    }

    @Override
    public FishingHookRenderState createRenderState() {
        return new FishingHookRenderState();
    }

    @Override
    public void extractRenderState(FishingHook fishingHook, FishingHookRenderState fishingHookRenderState, float f) {
        super.extractRenderState(fishingHook, fishingHookRenderState, f);
        Player player = fishingHook.getPlayerOwner();
        if (player == null) {
            fishingHookRenderState.lineOriginOffset = Vec3.ZERO;
            return;
        }
        float f2 = player.getAttackAnim(f);
        float f3 = Mth.sin(Mth.sqrt(f2) * (float)Math.PI);
        Vec3 vec3 = this.getPlayerHandPos(player, f3, f);
        Vec3 vec32 = fishingHook.getPosition(f).add(0.0, 0.25, 0.0);
        fishingHookRenderState.lineOriginOffset = vec3.subtract(vec32);
    }

    @Override
    protected boolean affectedByCulling(FishingHook fishingHook) {
        return false;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ boolean affectedByCulling(Entity entity) {
        return this.affectedByCulling((FishingHook)entity);
    }
}

