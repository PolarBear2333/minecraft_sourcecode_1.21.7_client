/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Items;

public abstract class AbstractSkeletonRenderer<T extends AbstractSkeleton, S extends SkeletonRenderState>
extends HumanoidMobRenderer<T, S, SkeletonModel<S>> {
    public AbstractSkeletonRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3) {
        this(context, modelLayerLocation2, modelLayerLocation3, new SkeletonModel(context.bakeLayer(modelLayerLocation)));
    }

    public AbstractSkeletonRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, SkeletonModel<S> skeletonModel) {
        super(context, skeletonModel, 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new SkeletonModel(context.bakeLayer(modelLayerLocation)), new SkeletonModel(context.bakeLayer(modelLayerLocation2)), context.getEquipmentRenderer()));
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        ((SkeletonRenderState)s).isAggressive = ((Mob)t).isAggressive();
        ((SkeletonRenderState)s).isShaking = ((AbstractSkeleton)t).isShaking();
        ((SkeletonRenderState)s).isHoldingBow = ((LivingEntity)t).getMainHandItem().is(Items.BOW);
    }

    @Override
    protected boolean isShaking(S s) {
        return ((SkeletonRenderState)s).isShaking;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(AbstractSkeleton abstractSkeleton, HumanoidArm humanoidArm) {
        if (abstractSkeleton.getMainArm() == humanoidArm && abstractSkeleton.isAggressive() && abstractSkeleton.getMainHandItem().is(Items.BOW)) {
            return HumanoidModel.ArmPose.BOW_AND_ARROW;
        }
        return HumanoidModel.ArmPose.EMPTY;
    }
}

