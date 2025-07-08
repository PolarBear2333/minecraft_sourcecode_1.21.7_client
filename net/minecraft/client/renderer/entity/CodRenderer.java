/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;
import org.joml.Quaternionfc;

public class CodRenderer
extends MobRenderer<Cod, LivingEntityRenderState, CodModel> {
    private static final ResourceLocation COD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/cod.png");

    public CodRenderer(EntityRendererProvider.Context context) {
        super(context, new CodModel(context.bakeLayer(ModelLayers.COD)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return COD_LOCATION;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    protected void setupRotations(LivingEntityRenderState livingEntityRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(livingEntityRenderState, poseStack, f, f2);
        float f3 = 4.3f * Mth.sin(0.6f * livingEntityRenderState.ageInTicks);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f3));
        if (!livingEntityRenderState.isInWater) {
            poseStack.translate(0.1f, 0.1f, -0.1f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        }
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

