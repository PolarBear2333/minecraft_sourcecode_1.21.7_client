/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.CowVariant;

public class CowRenderer
extends MobRenderer<Cow, CowRenderState, CowModel> {
    private final Map<CowVariant.ModelType, AdultAndBabyModelPair<CowModel>> models;

    public CowRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel(context.bakeLayer(ModelLayers.COW)), 0.7f);
        this.models = CowRenderer.bakeModels(context);
    }

    private static Map<CowVariant.ModelType, AdultAndBabyModelPair<CowModel>> bakeModels(EntityRendererProvider.Context context) {
        return Maps.newEnumMap(Map.of(CowVariant.ModelType.NORMAL, new AdultAndBabyModelPair<CowModel>(new CowModel(context.bakeLayer(ModelLayers.COW)), new CowModel(context.bakeLayer(ModelLayers.COW_BABY))), CowVariant.ModelType.WARM, new AdultAndBabyModelPair<CowModel>(new CowModel(context.bakeLayer(ModelLayers.WARM_COW)), new CowModel(context.bakeLayer(ModelLayers.WARM_COW_BABY))), CowVariant.ModelType.COLD, new AdultAndBabyModelPair<CowModel>(new CowModel(context.bakeLayer(ModelLayers.COLD_COW)), new CowModel(context.bakeLayer(ModelLayers.COLD_COW_BABY)))));
    }

    @Override
    public ResourceLocation getTextureLocation(CowRenderState cowRenderState) {
        return cowRenderState.variant == null ? MissingTextureAtlasSprite.getLocation() : cowRenderState.variant.modelAndTexture().asset().texturePath();
    }

    @Override
    public CowRenderState createRenderState() {
        return new CowRenderState();
    }

    @Override
    public void extractRenderState(Cow cow, CowRenderState cowRenderState, float f) {
        super.extractRenderState(cow, cowRenderState, f);
        cowRenderState.variant = cow.getVariant().value();
    }

    @Override
    public void render(CowRenderState cowRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        if (cowRenderState.variant == null) {
            return;
        }
        this.model = this.models.get(cowRenderState.variant.modelAndTexture().model()).getModel(cowRenderState.isBaby);
        super.render(cowRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((CowRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

