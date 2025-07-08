/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;

public class BreezeRenderer
extends MobRenderer<Breeze, BreezeRenderState, BreezeModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze.png");

    public BreezeRenderer(EntityRendererProvider.Context context) {
        super(context, new BreezeModel(context.bakeLayer(ModelLayers.BREEZE)), 0.5f);
        this.addLayer(new BreezeWindLayer(context, this));
        this.addLayer(new BreezeEyesLayer(this));
    }

    @Override
    public void render(BreezeRenderState breezeRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        BreezeModel breezeModel = (BreezeModel)this.getModel();
        BreezeRenderer.enable(breezeModel, breezeModel.head(), breezeModel.rods());
        super.render(breezeRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public ResourceLocation getTextureLocation(BreezeRenderState breezeRenderState) {
        return TEXTURE_LOCATION;
    }

    @Override
    public BreezeRenderState createRenderState() {
        return new BreezeRenderState();
    }

    @Override
    public void extractRenderState(Breeze breeze, BreezeRenderState breezeRenderState, float f) {
        super.extractRenderState(breeze, breezeRenderState, f);
        breezeRenderState.idle.copyFrom(breeze.idle);
        breezeRenderState.shoot.copyFrom(breeze.shoot);
        breezeRenderState.slide.copyFrom(breeze.slide);
        breezeRenderState.slideBack.copyFrom(breeze.slideBack);
        breezeRenderState.inhale.copyFrom(breeze.inhale);
        breezeRenderState.longJump.copyFrom(breeze.longJump);
    }

    public static BreezeModel enable(BreezeModel breezeModel, ModelPart ... modelPartArray) {
        breezeModel.head().visible = false;
        breezeModel.eyes().visible = false;
        breezeModel.rods().visible = false;
        breezeModel.wind().visible = false;
        for (ModelPart modelPart : modelPartArray) {
            modelPart.visible = true;
        }
        return breezeModel;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((BreezeRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

