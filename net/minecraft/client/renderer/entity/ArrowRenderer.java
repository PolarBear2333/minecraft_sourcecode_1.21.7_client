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
import net.minecraft.client.model.ArrowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.joml.Quaternionfc;

public abstract class ArrowRenderer<T extends AbstractArrow, S extends ArrowRenderState>
extends EntityRenderer<T, S> {
    private final ArrowModel model;

    public ArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
    }

    @Override
    public void render(S s, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(((ArrowRenderState)s).yRot - 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(((ArrowRenderState)s).xRot));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(s)));
        this.model.setupAnim((ArrowRenderState)s);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(s, poseStack, multiBufferSource, n);
    }

    protected abstract ResourceLocation getTextureLocation(S var1);

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        ((ArrowRenderState)s).xRot = ((Entity)t).getXRot(f);
        ((ArrowRenderState)s).yRot = ((Entity)t).getYRot(f);
        ((ArrowRenderState)s).shake = (float)((AbstractArrow)t).shakeTime - f;
    }
}

