/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.ResourceLocation;

public class SheepWoolLayer
extends RenderLayer<SheepRenderState, SheepModel> {
    private static final ResourceLocation SHEEP_WOOL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep_wool.png");
    private final EntityModel<SheepRenderState> adultModel;
    private final EntityModel<SheepRenderState> babyModel;

    public SheepWoolLayer(RenderLayerParent<SheepRenderState, SheepModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.adultModel = new SheepFurModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_WOOL));
        this.babyModel = new SheepFurModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_BABY_WOOL));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, SheepRenderState sheepRenderState, float f, float f2) {
        EntityModel<SheepRenderState> entityModel;
        if (sheepRenderState.isSheared) {
            return;
        }
        EntityModel<SheepRenderState> entityModel2 = entityModel = sheepRenderState.isBaby ? this.babyModel : this.adultModel;
        if (sheepRenderState.isInvisible) {
            if (sheepRenderState.appearsGlowing) {
                entityModel.setupAnim(sheepRenderState);
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.outline(SHEEP_WOOL_LOCATION));
                entityModel.renderToBuffer(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(sheepRenderState, 0.0f), -16777216);
            }
            return;
        }
        SheepWoolLayer.coloredCutoutModelCopyLayerRender(entityModel, SHEEP_WOOL_LOCATION, poseStack, multiBufferSource, n, sheepRenderState, sheepRenderState.getWoolColor());
    }
}

