/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

public class AbstractPiglinModel<S extends HumanoidRenderState>
extends HumanoidModel<S> {
    private static final String LEFT_SLEEVE = "left_sleeve";
    private static final String RIGHT_SLEEVE = "right_sleeve";
    private static final String LEFT_PANTS = "left_pants";
    private static final String RIGHT_PANTS = "right_pants";
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    public final ModelPart rightEar;
    public final ModelPart leftEar;

    public AbstractPiglinModel(ModelPart modelPart) {
        super(modelPart, RenderType::entityTranslucent);
        this.leftSleeve = this.leftArm.getChild(LEFT_SLEEVE);
        this.rightSleeve = this.rightArm.getChild(RIGHT_SLEEVE);
        this.leftPants = this.leftLeg.getChild(LEFT_PANTS);
        this.rightPants = this.rightLeg.getChild(RIGHT_PANTS);
        this.jacket = this.body.getChild("jacket");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, false);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.ZERO);
        PartDefinition partDefinition2 = AbstractPiglinModel.addHead(cubeDeformation, meshDefinition);
        partDefinition2.clearChild("hat");
        return meshDefinition;
    }

    public static PartDefinition addHead(CubeDeformation cubeDeformation, MeshDefinition meshDefinition) {
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -8.0f, -4.0f, 10.0f, 8.0f, 8.0f, cubeDeformation).texOffs(31, 1).addBox(-2.0f, -4.0f, -5.0f, 4.0f, 4.0f, 1.0f, cubeDeformation).texOffs(2, 4).addBox(2.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, cubeDeformation).texOffs(2, 0).addBox(-3.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, cubeDeformation), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(4.5f, -6.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(-4.5f, -6.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
        return partDefinition2;
    }

    @Override
    public void setupAnim(S s) {
        super.setupAnim(s);
        float f = ((HumanoidRenderState)s).walkAnimationPos;
        float f2 = ((HumanoidRenderState)s).walkAnimationSpeed;
        float f3 = 0.5235988f;
        float f4 = ((HumanoidRenderState)s).ageInTicks * 0.1f + f * 0.5f;
        float f5 = 0.08f + f2 * 0.4f;
        this.leftEar.zRot = -0.5235988f - Mth.cos(f4 * 1.2f) * f5;
        this.rightEar.zRot = 0.5235988f + Mth.cos(f4) * f5;
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
    }
}

