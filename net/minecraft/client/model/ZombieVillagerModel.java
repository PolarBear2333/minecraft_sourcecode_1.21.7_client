/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import net.minecraft.world.entity.HumanoidArm;

public class ZombieVillagerModel<S extends ZombieVillagerRenderState>
extends HumanoidModel<S>
implements VillagerLikeModel {
    private final ModelPart hatRim;

    public ZombieVillagerModel(ModelPart modelPart) {
        super(modelPart);
        this.hatRim = this.hat.getChild("hat_rim");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", new CubeListBuilder().texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f).texOffs(24, 0).addBox(-1.0f, -3.0f, -6.0f, 2.0f, 4.0f, 2.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, new CubeDeformation(0.5f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(30, 47).addBox(-8.0f, -8.0f, -6.0f, 16.0f, 16.0f, 1.0f), PartPose.rotation(-1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f).texOffs(0, 38).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 20.0f, 6.0f, new CubeDeformation(0.05f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 22).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createArmorLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation.extend(0.1f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.1f)), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.1f)), PartPose.offset(2.0f, 12.0f, 0.0f));
        partDefinition2.getChild("hat").addOrReplaceChild("hat_rim", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(S s) {
        super.setupAnim(s);
        float f = ((ZombieVillagerRenderState)s).attackTime;
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, ((ZombieVillagerRenderState)s).isAggressive, f, ((ZombieVillagerRenderState)s).ageInTicks);
    }

    @Override
    public void hatVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.hatRim.visible = bl;
    }

    @Override
    public void translateToArms(PoseStack poseStack) {
        this.translateToHand(HumanoidArm.RIGHT, poseStack);
    }
}

