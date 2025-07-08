/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public abstract class RenderLayer<S extends EntityRenderState, M extends EntityModel<? super S>> {
    private final RenderLayerParent<S, M> renderer;

    public RenderLayer(RenderLayerParent<S, M> renderLayerParent) {
        this.renderer = renderLayerParent;
    }

    protected static <S extends LivingEntityRenderState> void coloredCutoutModelCopyLayerRender(EntityModel<S> entityModel, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, int n2) {
        if (!s.isInvisible) {
            entityModel.setupAnim(s);
            RenderLayer.renderColoredCutoutModel(entityModel, resourceLocation, poseStack, multiBufferSource, n, s, n2);
        }
    }

    protected static void renderColoredCutoutModel(EntityModel<?> entityModel, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, LivingEntityRenderState livingEntityRenderState, int n2) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(resourceLocation));
        entityModel.renderToBuffer(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0f), n2);
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    public abstract void render(PoseStack var1, MultiBufferSource var2, int var3, S var4, float var5, float var6);
}

