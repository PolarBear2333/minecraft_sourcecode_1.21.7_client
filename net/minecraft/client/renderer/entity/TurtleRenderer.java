/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;

public class TurtleRenderer
extends AgeableMobRenderer<Turtle, TurtleRenderState, TurtleModel> {
    private static final ResourceLocation TURTLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/turtle/big_sea_turtle.png");

    public TurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new TurtleModel(context.bakeLayer(ModelLayers.TURTLE)), new TurtleModel(context.bakeLayer(ModelLayers.TURTLE_BABY)), 0.7f);
    }

    @Override
    protected float getShadowRadius(TurtleRenderState turtleRenderState) {
        float f = super.getShadowRadius(turtleRenderState);
        if (turtleRenderState.isBaby) {
            return f * 0.83f;
        }
        return f;
    }

    @Override
    public TurtleRenderState createRenderState() {
        return new TurtleRenderState();
    }

    @Override
    public void extractRenderState(Turtle turtle, TurtleRenderState turtleRenderState, float f) {
        super.extractRenderState(turtle, turtleRenderState, f);
        turtleRenderState.isOnLand = !turtle.isInWater() && turtle.onGround();
        turtleRenderState.isLayingEgg = turtle.isLayingEgg();
        turtleRenderState.hasEgg = !turtle.isBaby() && turtle.hasEgg();
    }

    @Override
    public ResourceLocation getTextureLocation(TurtleRenderState turtleRenderState) {
        return TURTLE_LOCATION;
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((TurtleRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((TurtleRenderState)entityRenderState);
    }
}

