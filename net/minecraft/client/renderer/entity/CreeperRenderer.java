/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperRenderer
extends MobRenderer<Creeper, CreeperRenderState, CreeperModel> {
    private static final ResourceLocation CREEPER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png");

    public CreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5f);
        this.addLayer(new CreeperPowerLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(CreeperRenderState creeperRenderState, PoseStack poseStack) {
        float f = creeperRenderState.swelling;
        float f2 = 1.0f + Mth.sin(f * 100.0f) * f * 0.01f;
        f = Mth.clamp(f, 0.0f, 1.0f);
        f *= f;
        f *= f;
        float f3 = (1.0f + f * 0.4f) * f2;
        float f4 = (1.0f + f * 0.1f) / f2;
        poseStack.scale(f3, f4, f3);
    }

    @Override
    protected float getWhiteOverlayProgress(CreeperRenderState creeperRenderState) {
        float f = creeperRenderState.swelling;
        if ((int)(f * 10.0f) % 2 == 0) {
            return 0.0f;
        }
        return Mth.clamp(f, 0.5f, 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(CreeperRenderState creeperRenderState) {
        return CREEPER_LOCATION;
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public void extractRenderState(Creeper creeper, CreeperRenderState creeperRenderState, float f) {
        super.extractRenderState(creeper, creeperRenderState, f);
        creeperRenderState.swelling = creeper.getSwelling(f);
        creeperRenderState.isPowered = creeper.isPowered();
    }

    @Override
    protected /* synthetic */ float getWhiteOverlayProgress(LivingEntityRenderState livingEntityRenderState) {
        return this.getWhiteOverlayProgress((CreeperRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

