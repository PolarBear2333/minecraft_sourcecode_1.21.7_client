/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BoatRenderer
extends AbstractBoatRenderer {
    private final Model waterPatchModel;
    private final ResourceLocation texture;
    private final EntityModel<BoatRenderState> model;

    public BoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context);
        this.texture = modelLayerLocation.model().withPath(string -> "textures/entity/" + string + ".png");
        this.waterPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), resourceLocation -> RenderType.waterMask());
        this.model = new BoatModel(context.bakeLayer(modelLayerLocation));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return this.model.renderType(this.texture);
    }

    @Override
    protected void renderTypeAdditions(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (!boatRenderState.isUnderWater) {
            this.waterPatchModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.waterPatchModel.renderType(this.texture)), n, OverlayTexture.NO_OVERLAY);
        }
    }
}

