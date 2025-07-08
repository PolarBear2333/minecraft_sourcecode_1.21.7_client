/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

public abstract class AbstractHoglinRenderer<T extends Mob>
extends AgeableMobRenderer<T, HoglinRenderState, HoglinModel> {
    public AbstractHoglinRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, float f) {
        super(context, new HoglinModel(context.bakeLayer(modelLayerLocation)), new HoglinModel(context.bakeLayer(modelLayerLocation2)), f);
    }

    @Override
    public HoglinRenderState createRenderState() {
        return new HoglinRenderState();
    }

    @Override
    public void extractRenderState(T t, HoglinRenderState hoglinRenderState, float f) {
        super.extractRenderState(t, hoglinRenderState, f);
        hoglinRenderState.attackAnimationRemainingTicks = ((HoglinBase)t).getAttackAnimationRemainingTicks();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

