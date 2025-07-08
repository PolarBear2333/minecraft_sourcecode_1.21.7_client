/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BreezeWindLayer
extends RenderLayer<BreezeRenderState, BreezeModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_wind.png");
    private final BreezeModel model;

    public BreezeWindLayer(EntityRendererProvider.Context context, RenderLayerParent<BreezeRenderState, BreezeModel> renderLayerParent) {
        super(renderLayerParent);
        this.model = new BreezeModel(context.bakeLayer(ModelLayers.BREEZE_WIND));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, BreezeRenderState breezeRenderState, float f, float f2) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(breezeRenderState.ageInTicks) % 1.0f, 0.0f));
        this.model.setupAnim(breezeRenderState);
        BreezeRenderer.enable(this.model, this.model.wind()).renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
    }

    private float xOffset(float f) {
        return f * 0.02f;
    }
}

