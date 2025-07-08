/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import org.joml.Quaternionfc;

public class IronGolemRenderer
extends MobRenderer<IronGolem, IronGolemRenderState, IronGolemModel> {
    private static final ResourceLocation GOLEM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");

    public IronGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7f);
        this.addLayer(new IronGolemCrackinessLayer(this));
        this.addLayer(new IronGolemFlowerLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public ResourceLocation getTextureLocation(IronGolemRenderState ironGolemRenderState) {
        return GOLEM_LOCATION;
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new IronGolemRenderState();
    }

    @Override
    public void extractRenderState(IronGolem ironGolem, IronGolemRenderState ironGolemRenderState, float f) {
        super.extractRenderState(ironGolem, ironGolemRenderState, f);
        ironGolemRenderState.attackTicksRemaining = (float)ironGolem.getAttackAnimationTick() > 0.0f ? (float)ironGolem.getAttackAnimationTick() - f : 0.0f;
        ironGolemRenderState.offerFlowerTick = ironGolem.getOfferFlowerTick();
        ironGolemRenderState.crackiness = ironGolem.getCrackiness();
    }

    @Override
    protected void setupRotations(IronGolemRenderState ironGolemRenderState, PoseStack poseStack, float f, float f2) {
        super.setupRotations(ironGolemRenderState, poseStack, f, f2);
        if ((double)ironGolemRenderState.walkAnimationSpeed < 0.01) {
            return;
        }
        float f3 = 13.0f;
        float f4 = ironGolemRenderState.walkAnimationPos + 6.0f;
        float f5 = (Math.abs(f4 % 13.0f - 6.5f) - 3.25f) / 3.25f;
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(6.5f * f5));
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((IronGolemRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

