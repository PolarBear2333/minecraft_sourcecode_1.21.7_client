/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class SkeletonModel<S extends SkeletonRenderState>
extends HumanoidModel<S> {
    public SkeletonModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        SkeletonModel.createDefaultSkeletonMesh(partDefinition);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    protected static void createDefaultSkeletonMesh(PartDefinition partDefinition) {
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
    }

    @Override
    public void setupAnim(S s) {
        super.setupAnim(s);
        if (((SkeletonRenderState)s).isAggressive && !((SkeletonRenderState)s).isHoldingBow) {
            float f = ((SkeletonRenderState)s).attackTime;
            float f2 = Mth.sin(f * (float)Math.PI);
            float f3 = Mth.sin((1.0f - (1.0f - f) * (1.0f - f)) * (float)Math.PI);
            this.rightArm.zRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightArm.yRot = -(0.1f - f2 * 0.6f);
            this.leftArm.yRot = 0.1f - f2 * 0.6f;
            this.rightArm.xRot = -1.5707964f;
            this.leftArm.xRot = -1.5707964f;
            this.rightArm.xRot -= f2 * 1.2f - f3 * 0.4f;
            this.leftArm.xRot -= f2 * 1.2f - f3 * 0.4f;
            AnimationUtils.bobArms(this.rightArm, this.leftArm, ((SkeletonRenderState)s).ageInTicks);
        }
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        float f = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        ModelPart modelPart = this.getArm(humanoidArm);
        modelPart.x += f;
        modelPart.translateAndRotate(poseStack);
        modelPart.x -= f;
    }
}

