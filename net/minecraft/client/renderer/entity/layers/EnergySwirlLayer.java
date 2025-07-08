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
import net.minecraft.resources.ResourceLocation;

public abstract class EnergySwirlLayer<S extends EntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public EnergySwirlLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        if (!this.isPowered(s)) {
            return;
        }
        float f3 = ((EntityRenderState)s).ageInTicks;
        M m = this.model();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f3) % 1.0f, f3 * 0.01f % 1.0f));
        ((EntityModel)m).setupAnim(s);
        ((Model)m).renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY, -8355712);
    }

    protected abstract boolean isPowered(S var1);

    protected abstract float xOffset(float var1);

    protected abstract ResourceLocation getTextureLocation();

    protected abstract M model();
}

