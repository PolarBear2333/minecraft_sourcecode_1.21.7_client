/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public abstract class AbstractBoatRenderer
extends EntityRenderer<AbstractBoat, BoatRenderState> {
    public AbstractBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    public void render(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.375f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - boatRenderState.yRot));
        float f = boatRenderState.hurtTime;
        if (f > 0.0f) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.sin(f) * f * boatRenderState.damageTime / 10.0f * (float)boatRenderState.hurtDir));
        }
        if (!boatRenderState.isUnderWater && !Mth.equal(boatRenderState.bubbleAngle, 0.0f)) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().setAngleAxis(boatRenderState.bubbleAngle * ((float)Math.PI / 180), 1.0f, 0.0f, 1.0f));
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        EntityModel<BoatRenderState> entityModel = this.model();
        entityModel.setupAnim(boatRenderState);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType());
        entityModel.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        this.renderTypeAdditions(boatRenderState, poseStack, multiBufferSource, n);
        poseStack.popPose();
        super.render(boatRenderState, poseStack, multiBufferSource, n);
    }

    protected void renderTypeAdditions(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
    }

    protected abstract EntityModel<BoatRenderState> model();

    protected abstract RenderType renderType();

    @Override
    public BoatRenderState createRenderState() {
        return new BoatRenderState();
    }

    @Override
    public void extractRenderState(AbstractBoat abstractBoat, BoatRenderState boatRenderState, float f) {
        super.extractRenderState(abstractBoat, boatRenderState, f);
        boatRenderState.yRot = abstractBoat.getYRot(f);
        boatRenderState.hurtTime = (float)abstractBoat.getHurtTime() - f;
        boatRenderState.hurtDir = abstractBoat.getHurtDir();
        boatRenderState.damageTime = Math.max(abstractBoat.getDamage() - f, 0.0f);
        boatRenderState.bubbleAngle = abstractBoat.getBubbleAngle(f);
        boatRenderState.isUnderWater = abstractBoat.isUnderWater();
        boatRenderState.rowingTimeLeft = abstractBoat.getRowingTime(0, f);
        boatRenderState.rowingTimeRight = abstractBoat.getRowingTime(1, f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

