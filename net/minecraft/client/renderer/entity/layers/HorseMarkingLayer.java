/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Markings;

public class HorseMarkingLayer
extends RenderLayer<HorseRenderState, HorseModel> {
    private static final ResourceLocation INVISIBLE_TEXTURE = ResourceLocation.withDefaultNamespace("invisible");
    private static final Map<Markings, ResourceLocation> LOCATION_BY_MARKINGS = Maps.newEnumMap(Map.of(Markings.NONE, INVISIBLE_TEXTURE, Markings.WHITE, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_white.png"), Markings.WHITE_FIELD, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_whitefield.png"), Markings.WHITE_DOTS, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_whitedots.png"), Markings.BLACK_DOTS, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_blackdots.png")));

    public HorseMarkingLayer(RenderLayerParent<HorseRenderState, HorseModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, HorseRenderState horseRenderState, float f, float f2) {
        ResourceLocation resourceLocation = LOCATION_BY_MARKINGS.get((Object)horseRenderState.markings);
        if (resourceLocation == INVISIBLE_TEXTURE || horseRenderState.isInvisible) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation));
        ((HorseModel)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(horseRenderState, 0.0f));
    }
}

