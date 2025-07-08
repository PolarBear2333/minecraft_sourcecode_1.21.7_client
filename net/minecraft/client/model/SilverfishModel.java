/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

public class SilverfishModel
extends EntityModel<EntityRenderState> {
    private static final int BODY_COUNT = 7;
    private final ModelPart[] bodyParts = new ModelPart[7];
    private final ModelPart[] bodyLayers = new ModelPart[3];
    private static final int[][] BODY_SIZES = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    public SilverfishModel(ModelPart modelPart) {
        super(modelPart);
        Arrays.setAll(this.bodyParts, n -> modelPart.getChild(SilverfishModel.getSegmentName(n)));
        Arrays.setAll(this.bodyLayers, n -> modelPart.getChild(SilverfishModel.getLayerName(n)));
    }

    private static String getLayerName(int n) {
        return "layer" + n;
    }

    private static String getSegmentName(int n) {
        return "segment" + n;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float[] fArray = new float[7];
        float f = -3.5f;
        for (int i = 0; i < 7; ++i) {
            partDefinition.addOrReplaceChild(SilverfishModel.getSegmentName(i), CubeListBuilder.create().texOffs(BODY_TEXS[i][0], BODY_TEXS[i][1]).addBox((float)BODY_SIZES[i][0] * -0.5f, 0.0f, (float)BODY_SIZES[i][2] * -0.5f, BODY_SIZES[i][0], BODY_SIZES[i][1], BODY_SIZES[i][2]), PartPose.offset(0.0f, 24 - BODY_SIZES[i][1], f));
            fArray[i] = f;
            if (i >= 6) continue;
            f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5f;
        }
        partDefinition.addOrReplaceChild(SilverfishModel.getLayerName(0), CubeListBuilder.create().texOffs(20, 0).addBox(-5.0f, 0.0f, (float)BODY_SIZES[2][2] * -0.5f, 10.0f, 8.0f, BODY_SIZES[2][2]), PartPose.offset(0.0f, 16.0f, fArray[2]));
        partDefinition.addOrReplaceChild(SilverfishModel.getLayerName(1), CubeListBuilder.create().texOffs(20, 11).addBox(-3.0f, 0.0f, (float)BODY_SIZES[4][2] * -0.5f, 6.0f, 4.0f, BODY_SIZES[4][2]), PartPose.offset(0.0f, 20.0f, fArray[4]));
        partDefinition.addOrReplaceChild(SilverfishModel.getLayerName(2), CubeListBuilder.create().texOffs(20, 18).addBox(-3.0f, 0.0f, (float)BODY_SIZES[4][2] * -0.5f, 6.0f, 5.0f, BODY_SIZES[1][2]), PartPose.offset(0.0f, 19.0f, fArray[1]));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(EntityRenderState entityRenderState) {
        super.setupAnim(entityRenderState);
        for (int i = 0; i < this.bodyParts.length; ++i) {
            this.bodyParts[i].yRot = Mth.cos(entityRenderState.ageInTicks * 0.9f + (float)i * 0.15f * (float)Math.PI) * (float)Math.PI * 0.05f * (float)(1 + Math.abs(i - 2));
            this.bodyParts[i].x = Mth.sin(entityRenderState.ageInTicks * 0.9f + (float)i * 0.15f * (float)Math.PI) * (float)Math.PI * 0.2f * (float)Math.abs(i - 2);
        }
        this.bodyLayers[0].yRot = this.bodyParts[2].yRot;
        this.bodyLayers[1].yRot = this.bodyParts[4].yRot;
        this.bodyLayers[1].x = this.bodyParts[4].x;
        this.bodyLayers[2].yRot = this.bodyParts[1].yRot;
        this.bodyLayers[2].x = this.bodyParts[1].x;
    }
}

