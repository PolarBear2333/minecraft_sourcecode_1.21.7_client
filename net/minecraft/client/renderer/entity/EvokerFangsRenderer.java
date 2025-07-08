/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.joml.Quaternionfc;

public class EvokerFangsRenderer
extends EntityRenderer<EvokerFangs, EvokerFangsRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel model;

    public EvokerFangsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new EvokerFangsModel(context.bakeLayer(ModelLayers.EVOKER_FANGS));
    }

    @Override
    public void render(EvokerFangsRenderState evokerFangsRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float f = evokerFangsRenderState.biteProgress;
        if (f == 0.0f) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f - evokerFangsRenderState.yRot));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        this.model.setupAnim(evokerFangsRenderState);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(evokerFangsRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public EvokerFangsRenderState createRenderState() {
        return new EvokerFangsRenderState();
    }

    @Override
    public void extractRenderState(EvokerFangs evokerFangs, EvokerFangsRenderState evokerFangsRenderState, float f) {
        super.extractRenderState(evokerFangs, evokerFangsRenderState, f);
        evokerFangsRenderState.yRot = evokerFangs.getYRot();
        evokerFangsRenderState.biteProgress = evokerFangs.getAnimationProgress(f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

