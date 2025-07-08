/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.util.Mth;

public class EndermanModel<T extends EndermanRenderState>
extends HumanoidModel<T> {
    public EndermanModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        float f = -14.0f;
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, -13.0f, 0.0f));
        partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(-0.5f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, -14.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(-5.0f, -12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0f, -2.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(5.0f, -12.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(-2.0f, -5.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(2.0f, -5.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t) {
        super.setupAnim(t);
        this.head.visible = true;
        this.rightArm.xRot *= 0.5f;
        this.leftArm.xRot *= 0.5f;
        this.rightLeg.xRot *= 0.5f;
        this.leftLeg.xRot *= 0.5f;
        float f = 0.4f;
        this.rightArm.xRot = Mth.clamp(this.rightArm.xRot, -0.4f, 0.4f);
        this.leftArm.xRot = Mth.clamp(this.leftArm.xRot, -0.4f, 0.4f);
        this.rightLeg.xRot = Mth.clamp(this.rightLeg.xRot, -0.4f, 0.4f);
        this.leftLeg.xRot = Mth.clamp(this.leftLeg.xRot, -0.4f, 0.4f);
        if (((EndermanRenderState)t).carriedBlock != null) {
            this.rightArm.xRot = -0.5f;
            this.leftArm.xRot = -0.5f;
            this.rightArm.zRot = 0.05f;
            this.leftArm.zRot = -0.05f;
        }
        if (((EndermanRenderState)t).isCreepy) {
            float f2 = 5.0f;
            this.head.y -= 5.0f;
            this.hat.y += 5.0f;
        }
    }
}

