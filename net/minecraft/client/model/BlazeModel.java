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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class BlazeModel
extends EntityModel<LivingEntityRenderState> {
    private final ModelPart[] upperBodyParts;
    private final ModelPart head;

    public BlazeModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.upperBodyParts = new ModelPart[12];
        Arrays.setAll(this.upperBodyParts, n -> modelPart.getChild(BlazeModel.getPartName(n)));
    }

    private static String getPartName(int n) {
        return "part" + n;
    }

    public static LayerDefinition createBodyLayer() {
        float f;
        float f2;
        float f3;
        int n;
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        float f4 = 0.0f;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, 0.0f, 0.0f, 2.0f, 8.0f, 2.0f);
        for (n = 0; n < 4; ++n) {
            f3 = Mth.cos(f4) * 9.0f;
            f2 = -2.0f + Mth.cos((float)(n * 2) * 0.25f);
            f = Mth.sin(f4) * 9.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(n), cubeListBuilder, PartPose.offset(f3, f2, f));
            f4 += 1.5707964f;
        }
        f4 = 0.7853982f;
        for (n = 4; n < 8; ++n) {
            f3 = Mth.cos(f4) * 7.0f;
            f2 = 2.0f + Mth.cos((float)(n * 2) * 0.25f);
            f = Mth.sin(f4) * 7.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(n), cubeListBuilder, PartPose.offset(f3, f2, f));
            f4 += 1.5707964f;
        }
        f4 = 0.47123894f;
        for (n = 8; n < 12; ++n) {
            f3 = Mth.cos(f4) * 5.0f;
            f2 = 11.0f + Mth.cos((float)n * 1.5f * 0.5f);
            f = Mth.sin(f4) * 5.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(n), cubeListBuilder, PartPose.offset(f3, f2, f));
            f4 += 1.5707964f;
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
        int n;
        super.setupAnim(livingEntityRenderState);
        float f = livingEntityRenderState.ageInTicks * (float)Math.PI * -0.1f;
        for (n = 0; n < 4; ++n) {
            this.upperBodyParts[n].y = -2.0f + Mth.cos(((float)(n * 2) + livingEntityRenderState.ageInTicks) * 0.25f);
            this.upperBodyParts[n].x = Mth.cos(f) * 9.0f;
            this.upperBodyParts[n].z = Mth.sin(f) * 9.0f;
            f += 1.5707964f;
        }
        f = 0.7853982f + livingEntityRenderState.ageInTicks * (float)Math.PI * 0.03f;
        for (n = 4; n < 8; ++n) {
            this.upperBodyParts[n].y = 2.0f + Mth.cos(((float)(n * 2) + livingEntityRenderState.ageInTicks) * 0.25f);
            this.upperBodyParts[n].x = Mth.cos(f) * 7.0f;
            this.upperBodyParts[n].z = Mth.sin(f) * 7.0f;
            f += 1.5707964f;
        }
        f = 0.47123894f + livingEntityRenderState.ageInTicks * (float)Math.PI * -0.05f;
        for (n = 8; n < 12; ++n) {
            this.upperBodyParts[n].y = 11.0f + Mth.cos(((float)n * 1.5f + livingEntityRenderState.ageInTicks) * 0.5f);
            this.upperBodyParts[n].x = Mth.cos(f) * 5.0f;
            this.upperBodyParts[n].z = Mth.sin(f) * 5.0f;
            f += 1.5707964f;
        }
        this.head.yRot = livingEntityRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = livingEntityRenderState.xRot * ((float)Math.PI / 180);
    }
}

