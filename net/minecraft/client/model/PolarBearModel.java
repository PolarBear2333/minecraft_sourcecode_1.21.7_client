/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;

public class PolarBearModel
extends QuadrupedModel<PolarBearRenderState> {
    private static final float BABY_HEAD_SCALE = 2.25f;
    private static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0f, 4.0f, 2.25f, 2.0f, 24.0f, Set.of("head"));

    public PolarBearModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -3.0f, -3.0f, 7.0f, 7.0f, 7.0f).texOffs(0, 44).addBox("mouth", -2.5f, 1.0f, -6.0f, 5.0f, 3.0f, 3.0f).texOffs(26, 0).addBox("right_ear", -4.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f).texOffs(26, 0).mirror().addBox("left_ear", 2.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 10.0f, -16.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 19).addBox(-5.0f, -13.0f, -7.0f, 14.0f, 14.0f, 11.0f).texOffs(39, 0).addBox(-4.0f, -25.0f, -7.0f, 12.0f, 12.0f, 10.0f), PartPose.offsetAndRotation(-2.0f, 9.0f, 12.0f, 1.5707964f, 0.0f, 0.0f));
        int n = 10;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(50, 22).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 8.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.5f, 14.0f, 6.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(4.5f, 14.0f, 6.0f));
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(50, 40).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 6.0f);
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-3.5f, 14.0f, -8.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(3.5f, 14.0f, -8.0f));
        return LayerDefinition.create(meshDefinition, 128, 64).apply(bl ? BABY_TRANSFORMER : MeshTransformer.IDENTITY).apply(MeshTransformer.scaling(1.2f));
    }

    @Override
    public void setupAnim(PolarBearRenderState polarBearRenderState) {
        super.setupAnim(polarBearRenderState);
        float f = polarBearRenderState.standScale * polarBearRenderState.standScale;
        float f2 = polarBearRenderState.ageScale;
        float f3 = polarBearRenderState.isBaby ? 0.44444445f : 1.0f;
        this.body.xRot -= f * (float)Math.PI * 0.35f;
        this.body.y += f * f2 * 2.0f;
        this.rightFrontLeg.y -= f * f2 * 20.0f;
        this.rightFrontLeg.z += f * f2 * 4.0f;
        this.rightFrontLeg.xRot -= f * (float)Math.PI * 0.45f;
        this.leftFrontLeg.y = this.rightFrontLeg.y;
        this.leftFrontLeg.z = this.rightFrontLeg.z;
        this.leftFrontLeg.xRot -= f * (float)Math.PI * 0.45f;
        this.head.y -= f * f3 * 24.0f;
        this.head.z += f * f3 * 13.0f;
        this.head.xRot += f * (float)Math.PI * 0.15f;
    }
}

