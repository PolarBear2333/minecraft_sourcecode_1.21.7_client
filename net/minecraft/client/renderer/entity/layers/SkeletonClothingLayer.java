/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;

public class SkeletonClothingLayer<S extends SkeletonRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private final SkeletonModel<S> layerModel;
    private final ResourceLocation clothesLocation;

    public SkeletonClothingLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, ModelLayerLocation modelLayerLocation, ResourceLocation resourceLocation) {
        super(renderLayerParent);
        this.clothesLocation = resourceLocation;
        this.layerModel = new SkeletonModel(entityModelSet.bakeLayer(modelLayerLocation));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        SkeletonClothingLayer.coloredCutoutModelCopyLayerRender(this.layerModel, this.clothesLocation, poseStack, multiBufferSource, n, s, -1);
    }
}

