/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.joml.Quaternionfc;

public class ThrownTridentRenderer
extends EntityRenderer<ThrownTrident, ThrownTridentRenderState> {
    public static final ResourceLocation TRIDENT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/trident.png");
    private final TridentModel model;

    public ThrownTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TridentModel(context.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void render(ThrownTridentRenderState thrownTridentRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(thrownTridentRenderState.yRot - 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(thrownTridentRenderState.xRot + 90.0f));
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, this.model.renderType(TRIDENT_LOCATION), false, thrownTridentRenderState.isFoil);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(thrownTridentRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public ThrownTridentRenderState createRenderState() {
        return new ThrownTridentRenderState();
    }

    @Override
    public void extractRenderState(ThrownTrident thrownTrident, ThrownTridentRenderState thrownTridentRenderState, float f) {
        super.extractRenderState(thrownTrident, thrownTridentRenderState, f);
        thrownTridentRenderState.yRot = thrownTrident.getYRot(f);
        thrownTridentRenderState.xRot = thrownTrident.getXRot(f);
        thrownTridentRenderState.isFoil = thrownTrident.isFoil();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

