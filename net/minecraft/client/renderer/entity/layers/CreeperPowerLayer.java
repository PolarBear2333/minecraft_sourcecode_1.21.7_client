/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.ResourceLocation;

public class CreeperPowerLayer
extends EnergySwirlLayer<CreeperRenderState, CreeperModel> {
    private static final ResourceLocation POWER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    private final CreeperModel model;

    public CreeperPowerLayer(RenderLayerParent<CreeperRenderState, CreeperModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new CreeperModel(entityModelSet.bakeLayer(ModelLayers.CREEPER_ARMOR));
    }

    @Override
    protected boolean isPowered(CreeperRenderState creeperRenderState) {
        return creeperRenderState.isPowered;
    }

    @Override
    protected float xOffset(float f) {
        return f * 0.01f;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return POWER_LOCATION;
    }

    @Override
    protected CreeperModel model() {
        return this.model;
    }

    @Override
    protected /* synthetic */ EntityModel model() {
        return this.model();
    }
}

