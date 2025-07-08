/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class WarmCowModel
extends CowModel {
    public WarmCowModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = WarmCowModel.createBaseCowModel();
        meshDefinition.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -6.0f, 8.0f, 8.0f, 6.0f).texOffs(1, 33).addBox(-3.0f, 1.0f, -7.0f, 6.0f, 3.0f, 1.0f).texOffs(27, 0).addBox(-8.0f, -3.0f, -5.0f, 4.0f, 2.0f, 2.0f).texOffs(39, 0).addBox(-8.0f, -5.0f, -5.0f, 2.0f, 2.0f, 2.0f).texOffs(27, 0).mirror().addBox(4.0f, -3.0f, -5.0f, 4.0f, 2.0f, 2.0f).mirror(false).texOffs(39, 0).mirror().addBox(6.0f, -5.0f, -5.0f, 2.0f, 2.0f, 2.0f).mirror(false), PartPose.offset(0.0f, 4.0f, -8.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}

