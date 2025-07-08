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
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.util.Mth;

public class HoglinModel
extends EntityModel<HoglinRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 8.0f, 6.0f, 1.9f, 2.0f, 24.0f, Set.of("head"));
    private static final float DEFAULT_HEAD_X_ROT = 0.87266463f;
    private static final float ATTACK_HEAD_X_ROT_END = -0.34906584f;
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart body;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart mane;

    public HoglinModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.mane = this.body.getChild("mane");
        this.head = modelPart.getChild("head");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
    }

    private static MeshDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-8.0f, -7.0f, -13.0f, 16.0f, 14.0f, 26.0f), PartPose.offset(0.0f, 7.0f, 0.0f));
        partDefinition2.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(90, 33).addBox(0.0f, 0.0f, -9.0f, 0.0f, 10.0f, 19.0f, new CubeDeformation(0.001f)), PartPose.offset(0.0f, -14.0f, -7.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(61, 1).addBox(-7.0f, -3.0f, -19.0f, 14.0f, 6.0f, 19.0f), PartPose.offsetAndRotation(0.0f, 2.0f, -12.0f, 0.87266463f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(1, 1).addBox(-6.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f), PartPose.offsetAndRotation(-6.0f, -2.0f, -3.0f, 0.0f, 0.0f, -0.6981317f));
        partDefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(1, 6).addBox(0.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f), PartPose.offsetAndRotation(6.0f, -2.0f, -3.0f, 0.0f, 0.0f, 0.6981317f));
        partDefinition3.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(10, 13).addBox(-1.0f, -11.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset(-7.0f, 2.0f, -12.0f));
        partDefinition3.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(1, 13).addBox(-1.0f, -11.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset(7.0f, 2.0f, -12.0f));
        int n = 14;
        int n2 = 11;
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(66, 42).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f), PartPose.offset(-4.0f, 10.0f, -8.5f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(41, 42).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f), PartPose.offset(4.0f, 10.0f, -8.5f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(21, 45).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f), PartPose.offset(-5.0f, 13.0f, 10.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 45).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f), PartPose.offset(5.0f, 13.0f, 10.0f));
        return meshDefinition;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HoglinModel.createMesh();
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    public static LayerDefinition createBabyLayer() {
        MeshDefinition meshDefinition = HoglinModel.createMesh();
        PartDefinition partDefinition = meshDefinition.getRoot().getChild("body");
        partDefinition.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(90, 33).addBox(0.0f, 0.0f, -9.0f, 0.0f, 10.0f, 19.0f, new CubeDeformation(0.001f)), PartPose.offset(0.0f, -14.0f, -3.0f));
        return LayerDefinition.create(meshDefinition, 128, 64).apply(BABY_TRANSFORMER);
    }

    @Override
    public void setupAnim(HoglinRenderState hoglinRenderState) {
        super.setupAnim(hoglinRenderState);
        float f = hoglinRenderState.walkAnimationSpeed;
        float f2 = hoglinRenderState.walkAnimationPos;
        this.rightEar.zRot = -0.6981317f - f * Mth.sin(f2);
        this.leftEar.zRot = 0.6981317f + f * Mth.sin(f2);
        this.head.yRot = hoglinRenderState.yRot * ((float)Math.PI / 180);
        float f3 = 1.0f - (float)Mth.abs(10 - 2 * hoglinRenderState.attackAnimationRemainingTicks) / 10.0f;
        this.head.xRot = Mth.lerp(f3, 0.87266463f, -0.34906584f);
        if (hoglinRenderState.isBaby) {
            this.head.y += f3 * 2.5f;
        }
        float f4 = 1.2f;
        this.rightFrontLeg.xRot = Mth.cos(f2) * 1.2f * f;
        this.rightHindLeg.xRot = this.leftFrontLeg.xRot = Mth.cos(f2 + (float)Math.PI) * 1.2f * f;
        this.leftHindLeg.xRot = this.rightFrontLeg.xRot;
    }
}

