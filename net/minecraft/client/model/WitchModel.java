/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.util.Mth;

public class WitchModel
extends EntityModel<WitchRenderState>
implements HeadedModel,
VillagerLikeModel {
    protected final ModelPart nose;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart hatRim;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart arms;

    public WitchModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hatRim = this.hat.getChild("hat_rim");
        this.nose = this.head.getChild("nose");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
        this.arms = modelPart.getChild("arms");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = VillagerModel.createBodyModel();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 64).addBox(0.0f, 0.0f, 0.0f, 10.0f, 2.0f, 10.0f), PartPose.offset(-5.0f, -10.03125f, -5.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(0, 76).addBox(0.0f, 0.0f, 0.0f, 7.0f, 4.0f, 7.0f), PartPose.offsetAndRotation(1.75f, -4.0f, 2.0f, -0.05235988f, 0.0f, 0.02617994f));
        PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(0, 87).addBox(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f), PartPose.offsetAndRotation(1.75f, -4.0f, 2.0f, -0.10471976f, 0.0f, 0.05235988f));
        partDefinition5.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(0, 95).addBox(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation(1.75f, -2.0f, 2.0f, -0.20943952f, 0.0f, 0.10471976f));
        PartDefinition partDefinition6 = partDefinition2.getChild("nose");
        partDefinition6.addOrReplaceChild("mole", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 3.0f, -6.75f, 1.0f, 1.0f, 1.0f, new CubeDeformation(-0.25f)), PartPose.offset(0.0f, -2.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 128);
    }

    @Override
    public void setupAnim(WitchRenderState witchRenderState) {
        super.setupAnim(witchRenderState);
        this.head.yRot = witchRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = witchRenderState.xRot * ((float)Math.PI / 180);
        this.rightLeg.xRot = Mth.cos(witchRenderState.walkAnimationPos * 0.6662f) * 1.4f * witchRenderState.walkAnimationSpeed * 0.5f;
        this.leftLeg.xRot = Mth.cos(witchRenderState.walkAnimationPos * 0.6662f + (float)Math.PI) * 1.4f * witchRenderState.walkAnimationSpeed * 0.5f;
        float f = 0.01f * (float)(witchRenderState.entityId % 10);
        this.nose.xRot = Mth.sin(witchRenderState.ageInTicks * f) * 4.5f * ((float)Math.PI / 180);
        this.nose.zRot = Mth.cos(witchRenderState.ageInTicks * f) * 2.5f * ((float)Math.PI / 180);
        if (witchRenderState.isHoldingItem) {
            this.nose.setPos(0.0f, 1.0f, -1.5f);
            this.nose.xRot = -0.9f;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void hatVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.hatRim.visible = bl;
    }

    @Override
    public void translateToArms(PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.arms.translateAndRotate(poseStack);
    }
}

