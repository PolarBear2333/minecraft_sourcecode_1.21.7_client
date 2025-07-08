/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.layers.SheepWoolUndercoatLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sheep.Sheep;

public class SheepRenderer
extends AgeableMobRenderer<Sheep, SheepRenderState, SheepModel> {
    private static final ResourceLocation SHEEP_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep.png");

    public SheepRenderer(EntityRendererProvider.Context context) {
        super(context, new SheepModel(context.bakeLayer(ModelLayers.SHEEP)), new SheepModel(context.bakeLayer(ModelLayers.SHEEP_BABY)), 0.7f);
        this.addLayer(new SheepWoolUndercoatLayer(this, context.getModelSet()));
        this.addLayer(new SheepWoolLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(SheepRenderState sheepRenderState) {
        return SHEEP_LOCATION;
    }

    @Override
    public SheepRenderState createRenderState() {
        return new SheepRenderState();
    }

    @Override
    public void extractRenderState(Sheep sheep, SheepRenderState sheepRenderState, float f) {
        super.extractRenderState(sheep, sheepRenderState, f);
        sheepRenderState.headEatAngleScale = sheep.getHeadEatAngleScale(f);
        sheepRenderState.headEatPositionScale = sheep.getHeadEatPositionScale(f);
        sheepRenderState.isSheared = sheep.isSheared();
        sheepRenderState.woolColor = sheep.getColor();
        sheepRenderState.id = sheep.getId();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SheepRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

