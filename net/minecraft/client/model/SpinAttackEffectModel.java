/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;

public class SpinAttackEffectModel
extends EntityModel<PlayerRenderState> {
    private static final int BOX_COUNT = 2;
    private final ModelPart[] boxes = new ModelPart[2];

    public SpinAttackEffectModel(ModelPart modelPart) {
        super(modelPart);
        for (int i = 0; i < 2; ++i) {
            this.boxes[i] = modelPart.getChild(SpinAttackEffectModel.boxName(i));
        }
    }

    private static String boxName(int n) {
        return "box" + n;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        for (int i = 0; i < 2; ++i) {
            float f = -3.2f + 9.6f * (float)(i + 1);
            float f2 = 0.75f * (float)(i + 1);
            partDefinition.addOrReplaceChild(SpinAttackEffectModel.boxName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f + f, -8.0f, 16.0f, 32.0f, 16.0f), PartPose.ZERO.withScale(f2));
        }
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(PlayerRenderState playerRenderState) {
        super.setupAnim(playerRenderState);
        for (int i = 0; i < this.boxes.length; ++i) {
            float f = playerRenderState.ageInTicks * (float)(-(45 + (i + 1) * 5));
            this.boxes[i].yRot = Mth.wrapDegrees(f) * ((float)Math.PI / 180);
        }
    }
}

