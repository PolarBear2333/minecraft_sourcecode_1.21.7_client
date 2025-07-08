/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class BeeStingerModel
extends Model {
    public BeeStingerModel(ModelPart modelPart) {
        super(modelPart, RenderType::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 0.0f);
        partDefinition.addOrReplaceChild("cross_1", cubeListBuilder, PartPose.rotation(0.7853982f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("cross_2", cubeListBuilder, PartPose.rotation(2.3561945f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}

