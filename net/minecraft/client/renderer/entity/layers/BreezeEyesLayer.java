/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BreezeEyesLayer
extends RenderLayer<BreezeRenderState, BreezeModel> {
    private static final RenderType BREEZE_EYES = RenderType.breezeEyes(ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_eyes.png"));

    public BreezeEyesLayer(RenderLayerParent<BreezeRenderState, BreezeModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, BreezeRenderState breezeRenderState, float f, float f2) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BREEZE_EYES);
        BreezeModel breezeModel = (BreezeModel)this.getParentModel();
        BreezeRenderer.enable(breezeModel, breezeModel.head(), breezeModel.eyes()).renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
    }
}

