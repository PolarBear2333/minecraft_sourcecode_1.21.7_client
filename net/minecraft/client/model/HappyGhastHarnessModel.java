/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HappyGhastModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;

public class HappyGhastHarnessModel
extends EntityModel<HappyGhastRenderState> {
    private static final float GOGGLES_Y_OFFSET = 14.0f;
    private final ModelPart goggles;

    public HappyGhastHarnessModel(ModelPart modelPart) {
        super(modelPart);
        this.goggles = modelPart.getChild("goggles");
    }

    public static LayerDefinition createHarnessLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("harness", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 16.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0f, -2.5f, -2.5f, 16.0f, 5.0f, 5.0f, new CubeDeformation(0.15f)), PartPose.offset(0.0f, 14.0f, -5.5f));
        return LayerDefinition.create(meshDefinition, 64, 64).apply(MeshTransformer.scaling(4.0f)).apply(bl ? HappyGhastModel.BABY_TRANSFORMER : MeshTransformer.IDENTITY);
    }

    @Override
    public void setupAnim(HappyGhastRenderState happyGhastRenderState) {
        super.setupAnim(happyGhastRenderState);
        if (happyGhastRenderState.isRidden) {
            this.goggles.xRot = 0.0f;
            this.goggles.y = 14.0f;
        } else {
            this.goggles.xRot = -0.7854f;
            this.goggles.y = 9.0f;
        }
    }
}

