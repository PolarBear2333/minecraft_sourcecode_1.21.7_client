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
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.util.Mth;

public class PhantomModel
extends EntityModel<PhantomRenderState> {
    private static final String TAIL_BASE = "tail_base";
    private static final String TAIL_TIP = "tail_tip";
    private final ModelPart leftWingBase;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingBase;
    private final ModelPart rightWingTip;
    private final ModelPart tailBase;
    private final ModelPart tailTip;

    public PhantomModel(ModelPart modelPart) {
        super(modelPart);
        ModelPart modelPart2 = modelPart.getChild("body");
        this.tailBase = modelPart2.getChild(TAIL_BASE);
        this.tailTip = this.tailBase.getChild(TAIL_TIP);
        this.leftWingBase = modelPart2.getChild("left_wing_base");
        this.leftWingTip = this.leftWingBase.getChild("left_wing_tip");
        this.rightWingBase = modelPart2.getChild("right_wing_base");
        this.rightWingTip = this.rightWingBase.getChild("right_wing_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0f, -2.0f, -8.0f, 5.0f, 3.0f, 9.0f), PartPose.rotation(-0.1f, 0.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(TAIL_BASE, CubeListBuilder.create().texOffs(3, 20).addBox(-2.0f, 0.0f, 0.0f, 3.0f, 2.0f, 6.0f), PartPose.offset(0.0f, -2.0f, 1.0f));
        partDefinition3.addOrReplaceChild(TAIL_TIP, CubeListBuilder.create().texOffs(4, 29).addBox(-1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 6.0f), PartPose.offset(0.0f, 0.5f, 6.0f));
        PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild("left_wing_base", CubeListBuilder.create().texOffs(23, 12).addBox(0.0f, 0.0f, 0.0f, 6.0f, 2.0f, 9.0f), PartPose.offsetAndRotation(2.0f, -2.0f, -8.0f, 0.0f, 0.0f, 0.1f));
        partDefinition4.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().texOffs(16, 24).addBox(0.0f, 0.0f, 0.0f, 13.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f));
        PartDefinition partDefinition5 = partDefinition2.addOrReplaceChild("right_wing_base", CubeListBuilder.create().texOffs(23, 12).mirror().addBox(-6.0f, 0.0f, 0.0f, 6.0f, 2.0f, 9.0f), PartPose.offsetAndRotation(-3.0f, -2.0f, -8.0f, 0.0f, 0.0f, -0.1f));
        partDefinition5.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().texOffs(16, 24).mirror().addBox(-13.0f, 0.0f, 0.0f, 13.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(-6.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f));
        partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -2.0f, -5.0f, 7.0f, 3.0f, 5.0f), PartPose.offsetAndRotation(0.0f, 1.0f, -7.0f, 0.2f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(PhantomRenderState phantomRenderState) {
        super.setupAnim(phantomRenderState);
        float f = phantomRenderState.flapTime * 7.448451f * ((float)Math.PI / 180);
        float f2 = 16.0f;
        this.leftWingBase.zRot = Mth.cos(f) * 16.0f * ((float)Math.PI / 180);
        this.leftWingTip.zRot = Mth.cos(f) * 16.0f * ((float)Math.PI / 180);
        this.rightWingBase.zRot = -this.leftWingBase.zRot;
        this.rightWingTip.zRot = -this.leftWingTip.zRot;
        this.tailBase.xRot = -(5.0f + Mth.cos(f * 2.0f) * 5.0f) * ((float)Math.PI / 180);
        this.tailTip.xRot = -(5.0f + Mth.cos(f * 2.0f) * 5.0f) * ((float)Math.PI / 180);
    }
}

