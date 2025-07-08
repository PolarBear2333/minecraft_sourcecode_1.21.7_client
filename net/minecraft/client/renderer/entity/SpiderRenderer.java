/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;

public class SpiderRenderer<T extends Spider>
extends MobRenderer<T, LivingEntityRenderState, SpiderModel> {
    private static final ResourceLocation SPIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/spider/spider.png");

    public SpiderRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.SPIDER);
    }

    public SpiderRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, new SpiderModel(context.bakeLayer(modelLayerLocation)), 0.8f);
        this.addLayer(new SpiderEyesLayer<SpiderModel>(this));
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return SPIDER_LOCATION;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public void extractRenderState(T t, LivingEntityRenderState livingEntityRenderState, float f) {
        super.extractRenderState(t, livingEntityRenderState, f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

