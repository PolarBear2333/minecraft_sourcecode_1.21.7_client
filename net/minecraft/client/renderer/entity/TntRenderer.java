/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import org.joml.Quaternionfc;

public class TntRenderer
extends EntityRenderer<PrimedTnt, TntRenderState> {
    private final BlockRenderDispatcher blockRenderer;

    public TntRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TntRenderState tntRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5f, 0.0f);
        float f = tntRenderState.fuseRemainingInTicks;
        if (tntRenderState.fuseRemainingInTicks < 10.0f) {
            float f2 = 1.0f - tntRenderState.fuseRemainingInTicks / 10.0f;
            f2 = Mth.clamp(f2, 0.0f, 1.0f);
            f2 *= f2;
            f2 *= f2;
            float f3 = 1.0f + f2 * 0.3f;
            poseStack.scale(f3, f3, f3);
        }
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5f, -0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        if (tntRenderState.blockState != null) {
            TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, tntRenderState.blockState, poseStack, multiBufferSource, n, (int)f / 5 % 2 == 0);
        }
        poseStack.popPose();
        super.render(tntRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public TntRenderState createRenderState() {
        return new TntRenderState();
    }

    @Override
    public void extractRenderState(PrimedTnt primedTnt, TntRenderState tntRenderState, float f) {
        super.extractRenderState(primedTnt, tntRenderState, f);
        tntRenderState.fuseRemainingInTicks = (float)primedTnt.getFuse() - f + 1.0f;
        tntRenderState.blockState = primedTnt.getBlockState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

