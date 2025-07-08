/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.GlowSquid;

public class GlowSquidRenderer
extends SquidRenderer<GlowSquid> {
    private static final ResourceLocation GLOW_SQUID_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/squid/glow_squid.png");

    public GlowSquidRenderer(EntityRendererProvider.Context context, SquidModel squidModel, SquidModel squidModel2) {
        super(context, squidModel, squidModel2);
    }

    @Override
    public ResourceLocation getTextureLocation(SquidRenderState squidRenderState) {
        return GLOW_SQUID_LOCATION;
    }

    @Override
    protected int getBlockLightLevel(GlowSquid glowSquid, BlockPos blockPos) {
        int n = (int)Mth.clampedLerp(0.0f, 15.0f, 1.0f - (float)glowSquid.getDarkTicksRemaining() / 10.0f);
        if (n == 15) {
            return 15;
        }
        return Math.max(n, super.getBlockLightLevel(glowSquid, blockPos));
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SquidRenderState)livingEntityRenderState);
    }
}

