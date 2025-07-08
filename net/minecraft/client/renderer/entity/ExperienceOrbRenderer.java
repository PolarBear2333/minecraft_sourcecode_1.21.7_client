/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import org.joml.Quaternionfc;

public class ExperienceOrbRenderer
extends EntityRenderer<ExperienceOrb, ExperienceOrbRenderState> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public ExperienceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    protected int getBlockLightLevel(ExperienceOrb experienceOrb, BlockPos blockPos) {
        return Mth.clamp(super.getBlockLightLevel(experienceOrb, blockPos) + 7, 0, 15);
    }

    @Override
    public void render(ExperienceOrbRenderState experienceOrbRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        int n2 = experienceOrbRenderState.icon;
        float f = (float)(n2 % 4 * 16 + 0) / 64.0f;
        float f2 = (float)(n2 % 4 * 16 + 16) / 64.0f;
        float f3 = (float)(n2 / 4 * 16 + 0) / 64.0f;
        float f4 = (float)(n2 / 4 * 16 + 16) / 64.0f;
        float f5 = 1.0f;
        float f6 = 0.5f;
        float f7 = 0.25f;
        float f8 = 255.0f;
        float f9 = experienceOrbRenderState.ageInTicks / 2.0f;
        int n3 = (int)((Mth.sin(f9 + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int n4 = 255;
        int n5 = (int)((Mth.sin(f9 + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        poseStack.translate(0.0f, 0.1f, 0.0f);
        poseStack.mulPose((Quaternionfc)this.entityRenderDispatcher.cameraOrientation());
        float f10 = 0.3f;
        poseStack.scale(0.3f, 0.3f, 0.3f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        ExperienceOrbRenderer.vertex(vertexConsumer, pose, -0.5f, -0.25f, n3, 255, n5, f, f4, n);
        ExperienceOrbRenderer.vertex(vertexConsumer, pose, 0.5f, -0.25f, n3, 255, n5, f2, f4, n);
        ExperienceOrbRenderer.vertex(vertexConsumer, pose, 0.5f, 0.75f, n3, 255, n5, f2, f3, n);
        ExperienceOrbRenderer.vertex(vertexConsumer, pose, -0.5f, 0.75f, n3, 255, n5, f, f3, n);
        poseStack.popPose();
        super.render(experienceOrbRenderState, poseStack, multiBufferSource, n);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float f2, int n, int n2, int n3, float f3, float f4, int n4) {
        vertexConsumer.addVertex(pose, f, f2, 0.0f).setColor(n, n2, n3, 128).setUv(f3, f4).setOverlay(OverlayTexture.NO_OVERLAY).setLight(n4).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public ExperienceOrbRenderState createRenderState() {
        return new ExperienceOrbRenderState();
    }

    @Override
    public void extractRenderState(ExperienceOrb experienceOrb, ExperienceOrbRenderState experienceOrbRenderState, float f) {
        super.extractRenderState(experienceOrb, experienceOrbRenderState, f);
        experienceOrbRenderState.icon = experienceOrb.getIcon();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

