/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;
import org.joml.Quaternionfc;

public class SalmonRenderer
extends MobRenderer<Salmon, SalmonRenderState, SalmonModel> {
    private static final ResourceLocation SALMON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");
    private final SalmonModel smallSalmonModel;
    private final SalmonModel mediumSalmonModel;
    private final SalmonModel largeSalmonModel;

    public SalmonRenderer(EntityRendererProvider.Context context) {
        super(context, new SalmonModel(context.bakeLayer(ModelLayers.SALMON)), 0.4f);
        this.smallSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON_SMALL));
        this.mediumSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON));
        this.largeSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON_LARGE));
    }

    @Override
    public void extractRenderState(Salmon salmon, SalmonRenderState salmonRenderState, float f) {
        super.extractRenderState(salmon, salmonRenderState, f);
        salmonRenderState.variant = salmon.getVariant();
    }

    @Override
    public ResourceLocation getTextureLocation(SalmonRenderState salmonRenderState) {
        return SALMON_LOCATION;
    }

    @Override
    public SalmonRenderState createRenderState() {
        return new SalmonRenderState();
    }

    @Override
    protected void setupRotations(SalmonRenderState salmonRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(salmonRenderState, poseStack, f, f2);
        float f3 = 1.0f;
        float f4 = 1.0f;
        if (!salmonRenderState.isInWater) {
            f3 = 1.3f;
            f4 = 1.7f;
        }
        float f5 = f3 * 4.3f * Mth.sin(f4 * 0.6f * salmonRenderState.ageInTicks);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f5));
        if (!salmonRenderState.isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        }
    }

    @Override
    public void render(SalmonRenderState salmonRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        this.model = salmonRenderState.variant == Salmon.Variant.SMALL ? this.smallSalmonModel : (salmonRenderState.variant == Salmon.Variant.LARGE ? this.largeSalmonModel : this.mediumSalmonModel);
        super.render(salmonRenderState, poseStack, multiBufferSource, n);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SalmonRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

