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
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;

public class ShulkerBulletModel
extends EntityModel<ShulkerBulletRenderState> {
    private static final String MAIN = "main";
    private final ModelPart main;

    public ShulkerBulletModel(ModelPart modelPart) {
        super(modelPart);
        this.main = modelPart.getChild(MAIN);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(MAIN, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -1.0f, 8.0f, 8.0f, 2.0f).texOffs(0, 10).addBox(-1.0f, -4.0f, -4.0f, 2.0f, 8.0f, 8.0f).texOffs(20, 0).addBox(-4.0f, -1.0f, -4.0f, 8.0f, 2.0f, 8.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(ShulkerBulletRenderState shulkerBulletRenderState) {
        super.setupAnim(shulkerBulletRenderState);
        this.main.yRot = shulkerBulletRenderState.yRot * ((float)Math.PI / 180);
        this.main.xRot = shulkerBulletRenderState.xRot * ((float)Math.PI / 180);
    }
}

