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
import net.minecraft.client.renderer.entity.state.SlimeRenderState;

public class LavaSlimeModel
extends EntityModel<SlimeRenderState> {
    private static final int SEGMENT_COUNT = 8;
    private final ModelPart[] bodyCubes = new ModelPart[8];

    public LavaSlimeModel(ModelPart modelPart) {
        super(modelPart);
        Arrays.setAll(this.bodyCubes, n -> modelPart.getChild(LavaSlimeModel.getSegmentName(n)));
    }

    private static String getSegmentName(int n) {
        return "cube" + n;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        for (int i = 0; i < 8; ++i) {
            int n = 0;
            int n2 = 0;
            if (i > 0 && i < 4) {
                n2 += 9 * i;
            } else if (i > 3) {
                n = 32;
                n2 += 9 * i - 36;
            }
            partDefinition.addOrReplaceChild(LavaSlimeModel.getSegmentName(i), CubeListBuilder.create().texOffs(n, n2).addBox(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f), PartPose.ZERO);
        }
        partDefinition.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(24, 40).addBox(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(SlimeRenderState slimeRenderState) {
        super.setupAnim(slimeRenderState);
        float f = Math.max(0.0f, slimeRenderState.squish);
        for (int i = 0; i < this.bodyCubes.length; ++i) {
            this.bodyCubes[i].y = (float)(-(4 - i)) * f * 1.7f;
        }
    }
}

