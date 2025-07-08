/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;

public class TntMinecartRenderer
extends AbstractMinecartRenderer<MinecartTNT, MinecartTntRenderState> {
    private final BlockRenderDispatcher blockRenderer;

    public TntMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.TNT_MINECART);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    protected void renderMinecartContents(MinecartTntRenderState minecartTntRenderState, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        float f = minecartTntRenderState.fuseRemainingInTicks;
        if (f > -1.0f && f < 10.0f) {
            float f2 = 1.0f - f / 10.0f;
            f2 = Mth.clamp(f2, 0.0f, 1.0f);
            f2 *= f2;
            f2 *= f2;
            float f3 = 1.0f + f2 * 0.3f;
            poseStack.scale(f3, f3, f3);
        }
        TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, blockState, poseStack, multiBufferSource, n, f > -1.0f && (int)f / 5 % 2 == 0);
    }

    public static void renderWhiteSolidBlock(BlockRenderDispatcher blockRenderDispatcher, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, boolean bl) {
        int n2 = bl ? OverlayTexture.pack(OverlayTexture.u(1.0f), 10) : OverlayTexture.NO_OVERLAY;
        blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, n, n2);
    }

    @Override
    public MinecartTntRenderState createRenderState() {
        return new MinecartTntRenderState();
    }

    @Override
    public void extractRenderState(MinecartTNT minecartTNT, MinecartTntRenderState minecartTntRenderState, float f) {
        super.extractRenderState(minecartTNT, minecartTntRenderState, f);
        minecartTntRenderState.fuseRemainingInTicks = minecartTNT.getFuse() > -1 ? (float)minecartTNT.getFuse() - f + 1.0f : -1.0f;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

