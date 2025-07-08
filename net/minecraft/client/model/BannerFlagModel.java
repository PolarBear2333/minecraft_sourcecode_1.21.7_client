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
import net.minecraft.util.Mth;

public class BannerFlagModel
extends Model {
    private final ModelPart flag;

    public BannerFlagModel(ModelPart modelPart) {
        super(modelPart, RenderType::entitySolid);
        this.flag = modelPart.getChild("flag");
    }

    public static LayerDefinition createFlagLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0f, 0.0f, -2.0f, 20.0f, 40.0f, 1.0f), PartPose.offset(0.0f, bl ? -44.0f : -20.5f, bl ? 0.0f : 10.5f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public void setupAnim(float f) {
        this.flag.xRot = (-0.0125f + 0.01f * Mth.cos((float)Math.PI * 2 * f)) * (float)Math.PI;
    }
}

