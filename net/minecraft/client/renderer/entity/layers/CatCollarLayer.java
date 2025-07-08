/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class CatCollarLayer
extends RenderLayer<CatRenderState, CatModel> {
    private static final ResourceLocation CAT_COLLAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/cat/cat_collar.png");
    private final CatModel adultModel;
    private final CatModel babyModel;

    public CatCollarLayer(RenderLayerParent<CatRenderState, CatModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.adultModel = new CatModel(entityModelSet.bakeLayer(ModelLayers.CAT_COLLAR));
        this.babyModel = new CatModel(entityModelSet.bakeLayer(ModelLayers.CAT_BABY_COLLAR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, CatRenderState catRenderState, float f, float f2) {
        DyeColor dyeColor = catRenderState.collarColor;
        if (dyeColor == null) {
            return;
        }
        int n2 = dyeColor.getTextureDiffuseColor();
        CatModel catModel = catRenderState.isBaby ? this.babyModel : this.adultModel;
        CatCollarLayer.coloredCutoutModelCopyLayerRender(catModel, CAT_COLLAR_LOCATION, poseStack, multiBufferSource, n, catRenderState, n2);
    }
}

