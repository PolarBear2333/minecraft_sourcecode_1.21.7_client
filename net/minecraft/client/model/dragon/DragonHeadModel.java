/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.dragon;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class DragonHeadModel
extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart jaw;

    public DragonHeadModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.jaw = this.head.getChild("jaw");
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = -16.0f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("upper_lip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 176, 44).addBox("upper_head", -8.0f, -8.0f, -10.0f, 16, 16, 16, 112, 30).mirror(true).addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0).mirror(false).addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0), PartPose.offset(0.0f, -7.986666f, 0.0f).scaled(0.75f));
        partDefinition2.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(176, 65).addBox("jaw", -6.0f, 0.0f, -16.0f, 12.0f, 4.0f, 16.0f), PartPose.offset(0.0f, 4.0f, -8.0f));
        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    @Override
    public void setupAnim(float f, float f2, float f3) {
        this.jaw.xRot = (float)(Math.sin(f * (float)Math.PI * 0.2f) + 1.0) * 0.2f;
        this.head.yRot = f2 * ((float)Math.PI / 180);
        this.head.xRot = f3 * ((float)Math.PI / 180);
    }
}

