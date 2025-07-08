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
import net.minecraft.resources.ResourceLocation;

public class TridentModel
extends Model {
    public static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/trident.png");

    public TridentModel(ModelPart modelPart) {
        super(modelPart, RenderType::entitySolid);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5f, 2.0f, -0.5f, 1.0f, 25.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("base", CubeListBuilder.create().texOffs(4, 0).addBox(-1.5f, 0.0f, -0.5f, 3.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_spike", CubeListBuilder.create().texOffs(4, 3).addBox(-2.5f, -3.0f, -0.5f, 1.0f, 4.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("middle_spike", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5f, -4.0f, -0.5f, 1.0f, 4.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("right_spike", CubeListBuilder.create().texOffs(4, 3).mirror().addBox(1.5f, -3.0f, -0.5f, 1.0f, 4.0f, 1.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 32, 32);
    }
}

