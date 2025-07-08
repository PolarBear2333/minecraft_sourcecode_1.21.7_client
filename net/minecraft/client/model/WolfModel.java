/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.util.Mth;

public class WolfModel
extends EntityModel<WolfRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(Set.of("head"));
    private static final String REAL_HEAD = "real_head";
    private static final String UPPER_BODY = "upper_body";
    private static final String REAL_TAIL = "real_tail";
    private final ModelPart head;
    private final ModelPart realHead;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart realTail;
    private final ModelPart upperBody;
    private static final int LEG_SIZE = 8;

    public WolfModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.realHead = this.head.getChild(REAL_HEAD);
        this.body = modelPart.getChild("body");
        this.upperBody = modelPart.getChild(UPPER_BODY);
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = modelPart.getChild("tail");
        this.realTail = this.tail.getChild(REAL_TAIL);
    }

    public static MeshDefinition createMeshDefinition(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 13.5f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0f, 13.5f, -7.0f));
        partDefinition2.addOrReplaceChild(REAL_HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -3.0f, -2.0f, 6.0f, 6.0f, 4.0f, cubeDeformation).texOffs(16, 14).addBox(-2.0f, -5.0f, 0.0f, 2.0f, 2.0f, 1.0f, cubeDeformation).texOffs(16, 14).addBox(2.0f, -5.0f, 0.0f, 2.0f, 2.0f, 1.0f, cubeDeformation).texOffs(0, 10).addBox(-0.5f, -0.001f, -5.0f, 3.0f, 3.0f, 4.0f, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0f, -2.0f, -3.0f, 6.0f, 9.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 14.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create().texOffs(21, 0).addBox(-3.0f, -3.0f, -3.0f, 8.0f, 6.0f, 7.0f, cubeDeformation), PartPose.offsetAndRotation(-1.0f, 14.0f, -3.0f, 1.5707964f, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().mirror().texOffs(0, 18).addBox(0.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder2, PartPose.offset(-2.5f, 16.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(0.5f, 16.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-2.5f, 16.0f, -4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(0.5f, 16.0f, -4.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0f, 12.0f, 8.0f, 0.62831855f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(REAL_TAIL, CubeListBuilder.create().texOffs(9, 18).addBox(0.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f, cubeDeformation), PartPose.ZERO);
        return meshDefinition;
    }

    @Override
    public void setupAnim(WolfRenderState wolfRenderState) {
        super.setupAnim(wolfRenderState);
        float f = wolfRenderState.walkAnimationPos;
        float f2 = wolfRenderState.walkAnimationSpeed;
        this.tail.yRot = wolfRenderState.isAngry ? 0.0f : Mth.cos(f * 0.6662f) * 1.4f * f2;
        if (wolfRenderState.isSitting) {
            float f3 = wolfRenderState.ageScale;
            this.upperBody.y += 2.0f * f3;
            this.upperBody.xRot = 1.2566371f;
            this.upperBody.yRot = 0.0f;
            this.body.y += 4.0f * f3;
            this.body.z -= 2.0f * f3;
            this.body.xRot = 0.7853982f;
            this.tail.y += 9.0f * f3;
            this.tail.z -= 2.0f * f3;
            this.rightHindLeg.y += 6.7f * f3;
            this.rightHindLeg.z -= 5.0f * f3;
            this.rightHindLeg.xRot = 4.712389f;
            this.leftHindLeg.y += 6.7f * f3;
            this.leftHindLeg.z -= 5.0f * f3;
            this.leftHindLeg.xRot = 4.712389f;
            this.rightFrontLeg.xRot = 5.811947f;
            this.rightFrontLeg.x += 0.01f * f3;
            this.rightFrontLeg.y += 1.0f * f3;
            this.leftFrontLeg.xRot = 5.811947f;
            this.leftFrontLeg.x -= 0.01f * f3;
            this.leftFrontLeg.y += 1.0f * f3;
        } else {
            this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
            this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
            this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * f2;
            this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * f2;
        }
        this.realHead.zRot = wolfRenderState.headRollAngle + wolfRenderState.getBodyRollAngle(0.0f);
        this.upperBody.zRot = wolfRenderState.getBodyRollAngle(-0.08f);
        this.body.zRot = wolfRenderState.getBodyRollAngle(-0.16f);
        this.realTail.zRot = wolfRenderState.getBodyRollAngle(-0.2f);
        this.head.xRot = wolfRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = wolfRenderState.yRot * ((float)Math.PI / 180);
        this.tail.xRot = wolfRenderState.tailAngle;
    }
}

