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
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.util.Mth;

public class RavagerModel
extends EntityModel<RavagerRenderState> {
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart neck;

    public RavagerModel(ModelPart modelPart) {
        super(modelPart);
        this.neck = modelPart.getChild("neck");
        this.head = this.neck.getChild("head");
        this.mouth = this.head.getChild("mouth");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int n = 16;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(68, 73).addBox(-5.0f, -1.0f, -18.0f, 10.0f, 10.0f, 18.0f), PartPose.offset(0.0f, -7.0f, 5.5f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -20.0f, -14.0f, 16.0f, 20.0f, 16.0f).texOffs(0, 0).addBox(-2.0f, -6.0f, -18.0f, 4.0f, 8.0f, 4.0f), PartPose.offset(0.0f, 16.0f, -17.0f));
        partDefinition3.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(74, 55).addBox(0.0f, -14.0f, -2.0f, 2.0f, 14.0f, 4.0f), PartPose.offsetAndRotation(-10.0f, -14.0f, -8.0f, 1.0995574f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(74, 55).mirror().addBox(0.0f, -14.0f, -2.0f, 2.0f, 14.0f, 4.0f), PartPose.offsetAndRotation(8.0f, -14.0f, -8.0f, 1.0995574f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0f, 0.0f, -16.0f, 16.0f, 3.0f, 16.0f), PartPose.offset(0.0f, -2.0f, 2.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 55).addBox(-7.0f, -10.0f, -7.0f, 14.0f, 16.0f, 20.0f).texOffs(0, 91).addBox(-6.0f, 6.0f, -7.0f, 12.0f, 13.0f, 18.0f), PartPose.offsetAndRotation(0.0f, 1.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(-8.0f, -13.0f, 18.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(8.0f, -13.0f, 18.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(-8.0f, -13.0f, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(8.0f, -13.0f, -5.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(RavagerRenderState ravagerRenderState) {
        float f;
        float f2;
        super.setupAnim(ravagerRenderState);
        float f3 = ravagerRenderState.stunnedTicksRemaining;
        float f4 = ravagerRenderState.attackTicksRemaining;
        int n = 10;
        if (f4 > 0.0f) {
            f2 = Mth.triangleWave(f4, 10.0f);
            f = (1.0f + f2) * 0.5f;
            float f5 = f * f * f * 12.0f;
            float f6 = f5 * Mth.sin(this.neck.xRot);
            this.neck.z = -6.5f + f5;
            this.neck.y = -7.0f - f6;
            this.mouth.xRot = f4 > 5.0f ? Mth.sin((-4.0f + f4) / 4.0f) * (float)Math.PI * 0.4f : 0.15707964f * Mth.sin((float)Math.PI * f4 / 10.0f);
        } else {
            f2 = -1.0f;
            f = -1.0f * Mth.sin(this.neck.xRot);
            this.neck.x = 0.0f;
            this.neck.y = -7.0f - f;
            this.neck.z = 5.5f;
            boolean bl = f3 > 0.0f;
            this.neck.xRot = bl ? 0.21991149f : 0.0f;
            this.mouth.xRot = (float)Math.PI * (bl ? 0.05f : 0.01f);
            if (bl) {
                double d = (double)f3 / 40.0;
                this.neck.x = (float)Math.sin(d * 10.0) * 3.0f;
            } else if ((double)ravagerRenderState.roarAnimation > 0.0) {
                float f7 = Mth.sin(ravagerRenderState.roarAnimation * (float)Math.PI * 0.25f);
                this.mouth.xRot = 1.5707964f * f7;
            }
        }
        this.head.xRot = ravagerRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = ravagerRenderState.yRot * ((float)Math.PI / 180);
        f2 = ravagerRenderState.walkAnimationPos;
        f = 0.4f * ravagerRenderState.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(f2 * 0.6662f) * f;
        this.leftHindLeg.xRot = Mth.cos(f2 * 0.6662f + (float)Math.PI) * f;
        this.rightFrontLeg.xRot = Mth.cos(f2 * 0.6662f + (float)Math.PI) * f;
        this.leftFrontLeg.xRot = Mth.cos(f2 * 0.6662f) * f;
    }
}

