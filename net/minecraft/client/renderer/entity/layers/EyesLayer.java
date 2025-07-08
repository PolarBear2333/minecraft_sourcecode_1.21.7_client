/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class EyesLayer<S extends EntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public EyesLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType());
        ((Model)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
    }

    public abstract RenderType renderType();
}

