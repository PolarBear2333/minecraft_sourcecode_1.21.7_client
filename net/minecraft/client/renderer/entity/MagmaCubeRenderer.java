/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;

public class MagmaCubeRenderer
extends MobRenderer<MagmaCube, SlimeRenderState, LavaSlimeModel> {
    private static final ResourceLocation MAGMACUBE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/magmacube.png");

    public MagmaCubeRenderer(EntityRendererProvider.Context context) {
        super(context, new LavaSlimeModel(context.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25f);
    }

    @Override
    protected int getBlockLightLevel(MagmaCube magmaCube, BlockPos blockPos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeRenderState slimeRenderState) {
        return MAGMACUBE_LOCATION;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(MagmaCube magmaCube, SlimeRenderState slimeRenderState, float f) {
        super.extractRenderState(magmaCube, slimeRenderState, f);
        slimeRenderState.squish = Mth.lerp(f, magmaCube.oSquish, magmaCube.squish);
        slimeRenderState.size = magmaCube.getSize();
    }

    @Override
    protected float getShadowRadius(SlimeRenderState slimeRenderState) {
        return (float)slimeRenderState.size * 0.25f;
    }

    @Override
    protected void scale(SlimeRenderState slimeRenderState, PoseStack poseStack) {
        int n = slimeRenderState.size;
        float f = slimeRenderState.squish / ((float)n * 0.5f + 1.0f);
        float f2 = 1.0f / (f + 1.0f);
        poseStack.scale(f2 * (float)n, 1.0f / f2 * (float)n, f2 * (float)n);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((SlimeRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SlimeRenderState)livingEntityRenderState);
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

