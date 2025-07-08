/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

public class ArmadilloRenderer
extends AgeableMobRenderer<Armadillo, ArmadilloRenderState, ArmadilloModel> {
    private static final ResourceLocation ARMADILLO_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armadillo.png");

    public ArmadilloRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmadilloModel(context.bakeLayer(ModelLayers.ARMADILLO)), new ArmadilloModel(context.bakeLayer(ModelLayers.ARMADILLO_BABY)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(ArmadilloRenderState armadilloRenderState) {
        return ARMADILLO_LOCATION;
    }

    @Override
    public ArmadilloRenderState createRenderState() {
        return new ArmadilloRenderState();
    }

    @Override
    public void extractRenderState(Armadillo armadillo, ArmadilloRenderState armadilloRenderState, float f) {
        super.extractRenderState(armadillo, armadilloRenderState, f);
        armadilloRenderState.isHidingInShell = armadillo.shouldHideInShell();
        armadilloRenderState.peekAnimationState.copyFrom(armadillo.peekAnimationState);
        armadilloRenderState.rollOutAnimationState.copyFrom(armadillo.rollOutAnimationState);
        armadilloRenderState.rollUpAnimationState.copyFrom(armadillo.rollUpAnimationState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ArmadilloRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

