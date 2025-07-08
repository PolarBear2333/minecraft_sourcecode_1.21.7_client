/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;

public class EndCrystalRenderer
extends EntityRenderer<EndCrystal, EndCrystalRenderState> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private final EndCrystalModel model;

    public EndCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new EndCrystalModel(context.bakeLayer(ModelLayers.END_CRYSTAL));
    }

    @Override
    public void render(EndCrystalRenderState endCrystalRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.translate(0.0f, -0.5f, 0.0f);
        this.model.setupAnim(endCrystalRenderState);
        this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(RENDER_TYPE), n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        Vec3 vec3 = endCrystalRenderState.beamOffset;
        if (vec3 != null) {
            float f = EndCrystalRenderer.getY(endCrystalRenderState.ageInTicks);
            float f2 = (float)vec3.x;
            float f3 = (float)vec3.y;
            float f4 = (float)vec3.z;
            poseStack.translate(vec3);
            EnderDragonRenderer.renderCrystalBeams(-f2, -f3 + f, -f4, endCrystalRenderState.ageInTicks, poseStack, multiBufferSource, n);
        }
        super.render(endCrystalRenderState, poseStack, multiBufferSource, n);
    }

    public static float getY(float f) {
        float f2 = Mth.sin(f * 0.2f) / 2.0f + 0.5f;
        f2 = (f2 * f2 + f2) * 0.4f;
        return f2 - 1.4f;
    }

    @Override
    public EndCrystalRenderState createRenderState() {
        return new EndCrystalRenderState();
    }

    @Override
    public void extractRenderState(EndCrystal endCrystal, EndCrystalRenderState endCrystalRenderState, float f) {
        super.extractRenderState(endCrystal, endCrystalRenderState, f);
        endCrystalRenderState.ageInTicks = (float)endCrystal.time + f;
        endCrystalRenderState.showsBottom = endCrystal.showsBottom();
        BlockPos blockPos = endCrystal.getBeamTarget();
        endCrystalRenderState.beamOffset = blockPos != null ? Vec3.atCenterOf(blockPos).subtract(endCrystal.getPosition(f)) : null;
    }

    @Override
    public boolean shouldRender(EndCrystal endCrystal, Frustum frustum, double d, double d2, double d3) {
        return super.shouldRender(endCrystal, frustum, d, d2, d3) || endCrystal.getBeamTarget() != null;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

