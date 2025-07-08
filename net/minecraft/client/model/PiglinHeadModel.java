/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class PiglinHeadModel
extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public PiglinHeadModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PiglinModel.addHead(CubeDeformation.NONE, meshDefinition);
        return meshDefinition;
    }

    @Override
    public void setupAnim(float f, float f2, float f3) {
        this.head.yRot = f2 * ((float)Math.PI / 180);
        this.head.xRot = f3 * ((float)Math.PI / 180);
        float f4 = 1.2f;
        this.leftEar.zRot = (float)(-(Math.cos(f * (float)Math.PI * 0.2f * 1.2f) + 2.5)) * 0.2f;
        this.rightEar.zRot = (float)(Math.cos(f * (float)Math.PI * 0.2f) + 2.5) * 0.2f;
    }
}

