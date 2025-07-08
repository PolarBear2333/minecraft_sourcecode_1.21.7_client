/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Fox;
import org.joml.Quaternionfc;

public class FoxRenderer
extends AgeableMobRenderer<Fox, FoxRenderState, FoxModel> {
    private static final ResourceLocation RED_FOX_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png");
    private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/fox_sleep.png");
    private static final ResourceLocation SNOW_FOX_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png");
    private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox_sleep.png");

    public FoxRenderer(EntityRendererProvider.Context context) {
        super(context, new FoxModel(context.bakeLayer(ModelLayers.FOX)), new FoxModel(context.bakeLayer(ModelLayers.FOX_BABY)), 0.4f);
        this.addLayer(new FoxHeldItemLayer(this));
    }

    @Override
    protected void setupRotations(FoxRenderState foxRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(foxRenderState, poseStack, f, f2);
        if (foxRenderState.isPouncing || foxRenderState.isFaceplanted) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-foxRenderState.xRot));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FoxRenderState foxRenderState) {
        if (foxRenderState.variant == Fox.Variant.RED) {
            return foxRenderState.isSleeping ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
        }
        return foxRenderState.isSleeping ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
    }

    @Override
    public FoxRenderState createRenderState() {
        return new FoxRenderState();
    }

    @Override
    public void extractRenderState(Fox fox, FoxRenderState foxRenderState, float f) {
        super.extractRenderState(fox, foxRenderState, f);
        HoldingEntityRenderState.extractHoldingEntityRenderState(fox, foxRenderState, this.itemModelResolver);
        foxRenderState.headRollAngle = fox.getHeadRollAngle(f);
        foxRenderState.isCrouching = fox.isCrouching();
        foxRenderState.crouchAmount = fox.getCrouchAmount(f);
        foxRenderState.isSleeping = fox.isSleeping();
        foxRenderState.isSitting = fox.isSitting();
        foxRenderState.isFaceplanted = fox.isFaceplanted();
        foxRenderState.isPouncing = fox.isPouncing();
        foxRenderState.variant = fox.getVariant();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((FoxRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

