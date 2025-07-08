/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

public class LeashKnotRenderer
extends EntityRenderer<LeashFenceKnotEntity, EntityRenderState> {
    private static final ResourceLocation KNOT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/lead_knot.png");
    private final LeashKnotModel model;

    public LeashKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel(context.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    @Override
    public void render(EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.model.setupAnim(entityRenderState);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entityRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}

