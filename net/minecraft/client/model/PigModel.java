/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class PigModel
extends QuadrupedModel<LivingEntityRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(false, 4.0f, 4.0f, Set.of("head"));

    public PigModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        return LayerDefinition.create(PigModel.createBasePigModel(cubeDeformation), 64, 64);
    }

    protected static MeshDefinition createBasePigModel(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = QuadrupedModel.createBodyMesh(6, true, false, cubeDeformation);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, cubeDeformation).texOffs(16, 16).addBox(-2.0f, 0.0f, -9.0f, 4.0f, 3.0f, 1.0f, cubeDeformation), PartPose.offset(0.0f, 12.0f, -6.0f));
        return meshDefinition;
    }
}

