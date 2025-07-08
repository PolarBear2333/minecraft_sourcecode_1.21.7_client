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
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.joml.Quaternionfc;

public class ShulkerBulletRenderer
extends EntityRenderer<ShulkerBullet, ShulkerBulletRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    private final ShulkerBulletModel model;

    public ShulkerBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel(context.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    protected int getBlockLightLevel(ShulkerBullet shulkerBullet, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(ShulkerBulletRenderState shulkerBulletRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        float f = shulkerBulletRenderState.ageInTicks;
        poseStack.translate(0.0f, 0.15f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(Mth.sin(f * 0.1f) * 180.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.cos(f * 0.1f) * 180.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(Mth.sin(f * 0.15f) * 360.0f));
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        this.model.setupAnim(shulkerBulletRenderState);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, vertexConsumer2, n, OverlayTexture.NO_OVERLAY, 0x26FFFFFF);
        poseStack.popPose();
        super.render(shulkerBulletRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public ShulkerBulletRenderState createRenderState() {
        return new ShulkerBulletRenderState();
    }

    @Override
    public void extractRenderState(ShulkerBullet shulkerBullet, ShulkerBulletRenderState shulkerBulletRenderState, float f) {
        super.extractRenderState(shulkerBullet, shulkerBulletRenderState, f);
        shulkerBulletRenderState.yRot = shulkerBullet.getYRot(f);
        shulkerBulletRenderState.xRot = shulkerBullet.getXRot(f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

