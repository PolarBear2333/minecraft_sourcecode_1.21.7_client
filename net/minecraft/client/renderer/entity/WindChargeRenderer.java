/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;

public class WindChargeRenderer
extends EntityRenderer<AbstractWindCharge, EntityRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/wind_charge.png");
    private final WindChargeModel model;

    public WindChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WindChargeModel(context.bakeLayer(ModelLayers.WIND_CHARGE));
    }

    @Override
    public void render(EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(entityRenderState.ageInTicks) % 1.0f, 0.0f));
        this.model.setupAnim(entityRenderState);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        super.render(entityRenderState, poseStack, multiBufferSource, n);
    }

    protected float xOffset(float f) {
        return f * 0.03f;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}

