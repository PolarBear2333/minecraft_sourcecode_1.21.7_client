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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class TadpoleModel
extends EntityModel<LivingEntityRenderState> {
    private final ModelPart tail;

    public TadpoleModel(ModelPart modelPart) {
        super(modelPart, RenderType::entityCutoutNoCull);
        this.tail = modelPart.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 0.0f;
        float f2 = 22.0f;
        float f3 = -3.0f;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.0f, 0.0f, 3.0f, 2.0f, 3.0f), PartPose.offset(0.0f, 22.0f, -3.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -1.0f, 0.0f, 0.0f, 2.0f, 7.0f), PartPose.offset(0.0f, 22.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Override
    public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
        super.setupAnim(livingEntityRenderState);
        float f = livingEntityRenderState.isInWater ? 1.0f : 1.5f;
        this.tail.yRot = -f * 0.25f * Mth.sin(0.3f * livingEntityRenderState.ageInTicks);
    }
}

