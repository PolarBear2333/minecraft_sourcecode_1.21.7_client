/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class SpiderModel
extends EntityModel<LivingEntityRenderState> {
    private static final String BODY_0 = "body0";
    private static final String BODY_1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public SpiderModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightMiddleHindLeg = modelPart.getChild(RIGHT_MIDDLE_HIND_LEG);
        this.leftMiddleHindLeg = modelPart.getChild(LEFT_MIDDLE_HIND_LEG);
        this.rightMiddleFrontLeg = modelPart.getChild(RIGHT_MIDDLE_FRONT_LEG);
        this.leftMiddleFrontLeg = modelPart.getChild(LEFT_MIDDLE_FRONT_LEG);
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = 15;
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 15.0f, -3.0f));
        partDefinition.addOrReplaceChild(BODY_0, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild(BODY_1, CubeListBuilder.create().texOffs(0, 12).addBox(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f), PartPose.offset(0.0f, 15.0f, 9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        float f = 0.7853982f;
        float f2 = 0.3926991f;
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, 2.0f, 0.0f, 0.7853982f, -0.7853982f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, 2.0f, 0.0f, -0.7853982f, 0.7853982f));
        partDefinition.addOrReplaceChild(RIGHT_MIDDLE_HIND_LEG, cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, 1.0f, 0.0f, 0.3926991f, -0.58119464f));
        partDefinition.addOrReplaceChild(LEFT_MIDDLE_HIND_LEG, cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, 1.0f, 0.0f, -0.3926991f, 0.58119464f));
        partDefinition.addOrReplaceChild(RIGHT_MIDDLE_FRONT_LEG, cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, 0.0f, 0.0f, -0.3926991f, -0.58119464f));
        partDefinition.addOrReplaceChild(LEFT_MIDDLE_FRONT_LEG, cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, 0.0f, 0.0f, 0.3926991f, 0.58119464f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, -1.0f, 0.0f, -0.7853982f, -0.7853982f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, -1.0f, 0.0f, 0.7853982f, 0.7853982f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
        super.setupAnim(livingEntityRenderState);
        this.head.yRot = livingEntityRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = livingEntityRenderState.xRot * ((float)Math.PI / 180);
        float f = livingEntityRenderState.walkAnimationPos * 0.6662f;
        float f2 = livingEntityRenderState.walkAnimationSpeed;
        float f3 = -(Mth.cos(f * 2.0f + 0.0f) * 0.4f) * f2;
        float f4 = -(Mth.cos(f * 2.0f + (float)Math.PI) * 0.4f) * f2;
        float f5 = -(Mth.cos(f * 2.0f + 1.5707964f) * 0.4f) * f2;
        float f6 = -(Mth.cos(f * 2.0f + 4.712389f) * 0.4f) * f2;
        float f7 = Math.abs(Mth.sin(f + 0.0f) * 0.4f) * f2;
        float f8 = Math.abs(Mth.sin(f + (float)Math.PI) * 0.4f) * f2;
        float f9 = Math.abs(Mth.sin(f + 1.5707964f) * 0.4f) * f2;
        float f10 = Math.abs(Mth.sin(f + 4.712389f) * 0.4f) * f2;
        this.rightHindLeg.yRot += f3;
        this.leftHindLeg.yRot -= f3;
        this.rightMiddleHindLeg.yRot += f4;
        this.leftMiddleHindLeg.yRot -= f4;
        this.rightMiddleFrontLeg.yRot += f5;
        this.leftMiddleFrontLeg.yRot -= f5;
        this.rightFrontLeg.yRot += f6;
        this.leftFrontLeg.yRot -= f6;
        this.rightHindLeg.zRot += f7;
        this.leftHindLeg.zRot -= f7;
        this.rightMiddleHindLeg.zRot += f8;
        this.leftMiddleHindLeg.zRot -= f8;
        this.rightMiddleFrontLeg.zRot += f9;
        this.leftMiddleFrontLeg.zRot -= f9;
        this.rightFrontLeg.zRot += f10;
        this.leftFrontLeg.zRot -= f10;
    }
}

