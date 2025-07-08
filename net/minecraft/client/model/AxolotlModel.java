/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.util.Mth;

public class AxolotlModel
extends EntityModel<AxolotlRenderState> {
    public static final float SWIMMING_LEG_XROT = 1.8849558f;
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5f);
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;

    public AxolotlModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.topGills = this.head.getChild("top_gills");
        this.leftGills = this.head.getChild("left_gills");
        this.rightGills = this.head.getChild("right_gills");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0f, -2.0f, -9.0f, 8.0f, 4.0f, 10.0f).texOffs(2, 17).addBox(0.0f, -3.0f, -8.0f, 0.0f, 5.0f, 9.0f), PartPose.offset(0.0f, 20.0f, 5.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0f, -3.0f, -5.0f, 8.0f, 5.0f, 5.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f, -9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0f, -3.0f, 0.0f, 8.0f, 3.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder3 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        partDefinition3.addOrReplaceChild("top_gills", cubeListBuilder, PartPose.offset(0.0f, -3.0f, -1.0f));
        partDefinition3.addOrReplaceChild("left_gills", cubeListBuilder2, PartPose.offset(-4.0f, 0.0f, -1.0f));
        partDefinition3.addOrReplaceChild("right_gills", cubeListBuilder3, PartPose.offset(4.0f, 0.0f, -1.0f));
        CubeListBuilder cubeListBuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder5 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        partDefinition2.addOrReplaceChild("right_hind_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("left_hind_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("right_front_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("left_front_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0f, -3.0f, 0.0f, 0.0f, 5.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(AxolotlRenderState axolotlRenderState) {
        super.setupAnim(axolotlRenderState);
        float f = axolotlRenderState.playingDeadFactor;
        float f2 = axolotlRenderState.inWaterFactor;
        float f3 = axolotlRenderState.onGroundFactor;
        float f4 = axolotlRenderState.movingFactor;
        float f5 = 1.0f - f4;
        float f6 = 1.0f - Math.min(f3, f4);
        this.body.yRot += axolotlRenderState.yRot * ((float)Math.PI / 180);
        this.setupSwimmingAnimation(axolotlRenderState.ageInTicks, axolotlRenderState.xRot, Math.min(f4, f2));
        this.setupWaterHoveringAnimation(axolotlRenderState.ageInTicks, Math.min(f5, f2));
        this.setupGroundCrawlingAnimation(axolotlRenderState.ageInTicks, Math.min(f4, f3));
        this.setupLayStillOnGroundAnimation(axolotlRenderState.ageInTicks, Math.min(f5, f3));
        this.setupPlayDeadAnimation(f);
        this.applyMirrorLegRotations(f6);
    }

    private void setupLayStillOnGroundAnimation(float f, float f2) {
        if (f2 <= 1.0E-5f) {
            return;
        }
        float f3 = f * 0.09f;
        float f4 = Mth.sin(f3);
        float f5 = Mth.cos(f3);
        float f6 = f4 * f4 - 2.0f * f4;
        float f7 = f5 * f5 - 3.0f * f4;
        this.head.xRot += -0.09f * f6 * f2;
        this.head.zRot += -0.2f * f2;
        this.tail.yRot += (-0.1f + 0.1f * f6) * f2;
        float f8 = (0.6f + 0.05f * f7) * f2;
        this.topGills.xRot += f8;
        this.leftGills.yRot -= f8;
        this.rightGills.yRot += f8;
        this.leftHindLeg.xRot += 1.1f * f2;
        this.leftHindLeg.yRot += 1.0f * f2;
        this.leftFrontLeg.xRot += 0.8f * f2;
        this.leftFrontLeg.yRot += 2.3f * f2;
        this.leftFrontLeg.zRot -= 0.5f * f2;
    }

    private void setupGroundCrawlingAnimation(float f, float f2) {
        if (f2 <= 1.0E-5f) {
            return;
        }
        float f3 = f * 0.11f;
        float f4 = Mth.cos(f3);
        float f5 = (f4 * f4 - 2.0f * f4) / 5.0f;
        float f6 = 0.7f * f4;
        float f7 = 0.09f * f4 * f2;
        this.head.yRot += f7;
        this.tail.yRot += f7;
        float f8 = (0.6f - 0.08f * (f4 * f4 + 2.0f * Mth.sin(f3))) * f2;
        this.topGills.xRot += f8;
        this.leftGills.yRot -= f8;
        this.rightGills.yRot += f8;
        float f9 = 0.9424779f * f2;
        float f10 = 1.0995574f * f2;
        this.leftHindLeg.xRot += f9;
        this.leftHindLeg.yRot += (1.5f - f5) * f2;
        this.leftHindLeg.zRot += -0.1f * f2;
        this.leftFrontLeg.xRot += f10;
        this.leftFrontLeg.yRot += (1.5707964f - f6) * f2;
        this.rightHindLeg.xRot += f9;
        this.rightHindLeg.yRot += (-1.0f - f5) * f2;
        this.rightFrontLeg.xRot += f10;
        this.rightFrontLeg.yRot += (-1.5707964f - f6) * f2;
    }

    private void setupWaterHoveringAnimation(float f, float f2) {
        if (f2 <= 1.0E-5f) {
            return;
        }
        float f3 = f * 0.075f;
        float f4 = Mth.cos(f3);
        float f5 = Mth.sin(f3) * 0.15f;
        float f6 = (-0.15f + 0.075f * f4) * f2;
        this.body.xRot += f6;
        this.body.y -= f5 * f2;
        this.head.xRot -= f6;
        this.topGills.xRot += 0.2f * f4 * f2;
        float f7 = (-0.3f * f4 - 0.19f) * f2;
        this.leftGills.yRot += f7;
        this.rightGills.yRot -= f7;
        this.leftHindLeg.xRot += (2.3561945f - f4 * 0.11f) * f2;
        this.leftHindLeg.yRot += 0.47123894f * f2;
        this.leftHindLeg.zRot += 1.7278761f * f2;
        this.leftFrontLeg.xRot += (0.7853982f - f4 * 0.2f) * f2;
        this.leftFrontLeg.yRot += 2.042035f * f2;
        this.tail.yRot += 0.5f * f4 * f2;
    }

    private void setupSwimmingAnimation(float f, float f2, float f3) {
        if (f3 <= 1.0E-5f) {
            return;
        }
        float f4 = f * 0.33f;
        float f5 = Mth.sin(f4);
        float f6 = Mth.cos(f4);
        float f7 = 0.13f * f5;
        this.body.xRot += (f2 * ((float)Math.PI / 180) + f7) * f3;
        this.head.xRot -= f7 * 1.8f * f3;
        this.body.y -= 0.45f * f6 * f3;
        this.topGills.xRot += (-0.5f * f5 - 0.8f) * f3;
        float f8 = (0.3f * f5 + 0.9f) * f3;
        this.leftGills.yRot += f8;
        this.rightGills.yRot -= f8;
        this.tail.yRot += 0.3f * Mth.cos(f4 * 0.9f) * f3;
        this.leftHindLeg.xRot += 1.8849558f * f3;
        this.leftHindLeg.yRot += -0.4f * f5 * f3;
        this.leftHindLeg.zRot += 1.5707964f * f3;
        this.leftFrontLeg.xRot += 1.8849558f * f3;
        this.leftFrontLeg.yRot += (-0.2f * f6 - 0.1f) * f3;
        this.leftFrontLeg.zRot += 1.5707964f * f3;
    }

    private void setupPlayDeadAnimation(float f) {
        if (f <= 1.0E-5f) {
            return;
        }
        this.leftHindLeg.xRot += 1.4137167f * f;
        this.leftHindLeg.yRot += 1.0995574f * f;
        this.leftHindLeg.zRot += 0.7853982f * f;
        this.leftFrontLeg.xRot += 0.7853982f * f;
        this.leftFrontLeg.yRot += 2.042035f * f;
        this.body.xRot += -0.15f * f;
        this.body.zRot += 0.35f * f;
    }

    private void applyMirrorLegRotations(float f) {
        if (f <= 1.0E-5f) {
            return;
        }
        this.rightHindLeg.xRot += this.leftHindLeg.xRot * f;
        ModelPart modelPart = this.rightHindLeg;
        modelPart.yRot = modelPart.yRot + -this.leftHindLeg.yRot * f;
        modelPart = this.rightHindLeg;
        modelPart.zRot = modelPart.zRot + -this.leftHindLeg.zRot * f;
        this.rightFrontLeg.xRot += this.leftFrontLeg.xRot * f;
        modelPart = this.rightFrontLeg;
        modelPart.yRot = modelPart.yRot + -this.leftFrontLeg.yRot * f;
        modelPart = this.rightFrontLeg;
        modelPart.zRot = modelPart.zRot + -this.leftFrontLeg.zRot * f;
    }
}

