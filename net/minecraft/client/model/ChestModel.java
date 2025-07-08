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

public class ChestModel
extends Model {
    private static final String BOTTOM = "bottom";
    private static final String LID = "lid";
    private static final String LOCK = "lock";
    private final ModelPart lid;
    private final ModelPart lock;

    public ChestModel(ModelPart modelPart) {
        super(modelPart, RenderType::entitySolid);
        this.lid = modelPart.getChild(LID);
        this.lock = modelPart.getChild(LOCK);
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BOTTOM, CubeListBuilder.create().texOffs(0, 19).addBox(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f), PartPose.offset(0.0f, 9.0f, 1.0f));
        partDefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0).addBox(7.0f, -2.0f, 14.0f, 2.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 9.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyRightLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BOTTOM, CubeListBuilder.create().texOffs(0, 19).addBox(1.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f), PartPose.offset(0.0f, 9.0f, 1.0f));
        partDefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0).addBox(15.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 9.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyLeftLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BOTTOM, CubeListBuilder.create().texOffs(0, 19).addBox(0.0f, 0.0f, 1.0f, 15.0f, 10.0f, 14.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f), PartPose.offset(0.0f, 9.0f, 1.0f));
        partDefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -2.0f, 14.0f, 1.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 9.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public void setupAnim(float f) {
        this.lock.xRot = this.lid.xRot = -(f * 1.5707964f);
    }
}

