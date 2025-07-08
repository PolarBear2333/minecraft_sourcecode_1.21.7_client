/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import org.joml.Quaternionfc;

public class SquidRenderer<T extends Squid>
extends AgeableMobRenderer<T, SquidRenderState, SquidModel> {
    private static final ResourceLocation SQUID_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/squid/squid.png");

    public SquidRenderer(EntityRendererProvider.Context context, SquidModel squidModel, SquidModel squidModel2) {
        super(context, squidModel, squidModel2, 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(SquidRenderState squidRenderState) {
        return SQUID_LOCATION;
    }

    @Override
    public SquidRenderState createRenderState() {
        return new SquidRenderState();
    }

    @Override
    public void extractRenderState(T t, SquidRenderState squidRenderState, float f) {
        super.extractRenderState(t, squidRenderState, f);
        squidRenderState.tentacleAngle = Mth.lerp(f, ((Squid)t).oldTentacleAngle, ((Squid)t).tentacleAngle);
        squidRenderState.xBodyRot = Mth.lerp(f, ((Squid)t).xBodyRotO, ((Squid)t).xBodyRot);
        squidRenderState.zBodyRot = Mth.lerp(f, ((Squid)t).zBodyRotO, ((Squid)t).zBodyRot);
    }

    @Override
    protected void setupRotations(SquidRenderState squidRenderState, PoseStack poseStack, float f, float f2) {
        poseStack.translate(0.0f, squidRenderState.isBaby ? 0.25f : 0.5f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(squidRenderState.xBodyRot));
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(squidRenderState.zBodyRot));
        poseStack.translate(0.0f, squidRenderState.isBaby ? -0.6f : -1.2f, 0.0f);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SquidRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

