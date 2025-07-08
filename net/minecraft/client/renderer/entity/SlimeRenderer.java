/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

public class SlimeRenderer
extends MobRenderer<Slime, SlimeRenderState, SlimeModel> {
    public static final ResourceLocation SLIME_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");

    public SlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25f);
        this.addLayer(new SlimeOuterLayer(this, context.getModelSet()));
    }

    @Override
    protected float getShadowRadius(SlimeRenderState slimeRenderState) {
        return (float)slimeRenderState.size * 0.25f;
    }

    @Override
    protected void scale(SlimeRenderState slimeRenderState, PoseStack poseStack) {
        float f = 0.999f;
        poseStack.scale(0.999f, 0.999f, 0.999f);
        poseStack.translate(0.0f, 0.001f, 0.0f);
        float f2 = slimeRenderState.size;
        float f3 = slimeRenderState.squish / (f2 * 0.5f + 1.0f);
        float f4 = 1.0f / (f3 + 1.0f);
        poseStack.scale(f4 * f2, 1.0f / f4 * f2, f4 * f2);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeRenderState slimeRenderState) {
        return SLIME_LOCATION;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(Slime slime, SlimeRenderState slimeRenderState, float f) {
        super.extractRenderState(slime, slimeRenderState, f);
        slimeRenderState.squish = Mth.lerp(f, slime.oSquish, slime.squish);
        slimeRenderState.size = slime.getSize();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((SlimeRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((SlimeRenderState)entityRenderState);
    }
}

