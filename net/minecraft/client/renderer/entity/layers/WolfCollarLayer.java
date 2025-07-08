/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class WolfCollarLayer
extends RenderLayer<WolfRenderState, WolfModel> {
    private static final ResourceLocation WOLF_COLLAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");

    public WolfCollarLayer(RenderLayerParent<WolfRenderState, WolfModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, WolfRenderState wolfRenderState, float f, float f2) {
        DyeColor dyeColor = wolfRenderState.collarColor;
        if (dyeColor == null || wolfRenderState.isInvisible) {
            return;
        }
        int n2 = dyeColor.getTextureDiffuseColor();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(WOLF_COLLAR_LOCATION));
        ((WolfModel)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY, n2);
    }
}

