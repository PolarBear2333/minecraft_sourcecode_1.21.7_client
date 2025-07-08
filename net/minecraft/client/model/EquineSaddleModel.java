/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EquineRenderState;

public class EquineSaddleModel
extends AbstractEquineModel<EquineRenderState> {
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    private final ModelPart[] ridingParts;

    public EquineSaddleModel(ModelPart modelPart) {
        super(modelPart);
        ModelPart modelPart2 = this.headParts.getChild(LEFT_SADDLE_LINE);
        ModelPart modelPart3 = this.headParts.getChild(RIGHT_SADDLE_LINE);
        this.ridingParts = new ModelPart[]{modelPart2, modelPart3};
    }

    public static LayerDefinition createSaddleLayer(boolean bl) {
        return EquineSaddleModel.createFullScaleSaddleLayer(bl).apply(bl ? BABY_TRANSFORMER : MeshTransformer.IDENTITY);
    }

    public static LayerDefinition createFullScaleSaddleLayer(boolean bl) {
        MeshDefinition meshDefinition = bl ? EquineSaddleModel.createFullScaleBabyMesh(CubeDeformation.NONE) : EquineSaddleModel.createBodyMesh(CubeDeformation.NONE);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.getChild("body");
        PartDefinition partDefinition3 = partDefinition.getChild("head_parts");
        partDefinition2.addOrReplaceChild(SADDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-5.0f, -8.0f, -9.0f, 10.0f, 9.0f, 9.0f, new CubeDeformation(0.5f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(LEFT_SADDLE_MOUTH, CubeListBuilder.create().texOffs(29, 5).addBox(2.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(RIGHT_SADDLE_MOUTH, CubeListBuilder.create().texOffs(29, 5).addBox(-3.0f, -9.0f, -6.0f, 1.0f, 2.0f, 2.0f), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(LEFT_SADDLE_LINE, CubeListBuilder.create().texOffs(32, 2).addBox(3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f), PartPose.rotation(-0.5235988f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(RIGHT_SADDLE_LINE, CubeListBuilder.create().texOffs(32, 2).addBox(-3.1f, -6.0f, -8.0f, 0.0f, 3.0f, 16.0f), PartPose.rotation(-0.5235988f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(HEAD_SADDLE, CubeListBuilder.create().texOffs(1, 1).addBox(-3.0f, -11.0f, -1.9f, 6.0f, 5.0f, 6.0f, new CubeDeformation(0.22f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(MOUTH_SADDLE_WRAP, CubeListBuilder.create().texOffs(19, 0).addBox(-2.0f, -11.0f, -4.0f, 4.0f, 5.0f, 2.0f, new CubeDeformation(0.2f)), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(EquineRenderState equineRenderState) {
        super.setupAnim(equineRenderState);
        for (ModelPart modelPart : this.ridingParts) {
            modelPart.visible = equineRenderState.isRidden;
        }
    }
}

