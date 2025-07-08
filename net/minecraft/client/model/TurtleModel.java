/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.util.Mth;

public class TurtleModel
extends QuadrupedModel<TurtleRenderState> {
    private static final String EGG_BELLY = "egg_belly";
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 120.0f, 0.0f, 9.0f, 6.0f, 120.0f, Set.of("head"));
    private final ModelPart eggBelly;

    public TurtleModel(ModelPart modelPart) {
        super(modelPart);
        this.eggBelly = modelPart.getChild(EGG_BELLY);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(3, 0).addBox(-3.0f, -1.0f, -3.0f, 6.0f, 5.0f, 6.0f), PartPose.offset(0.0f, 19.0f, -10.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(7, 37).addBox("shell", -9.5f, 3.0f, -10.0f, 19.0f, 20.0f, 6.0f).texOffs(31, 1).addBox("belly", -5.5f, 3.0f, -13.0f, 11.0f, 18.0f, 3.0f), PartPose.offsetAndRotation(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(EGG_BELLY, CubeListBuilder.create().texOffs(70, 33).addBox(-4.5f, 3.0f, -14.0f, 9.0f, 18.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        boolean bl = true;
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(1, 23).addBox(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), PartPose.offset(-3.5f, 22.0f, 11.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(1, 12).addBox(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), PartPose.offset(3.5f, 22.0f, 11.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(27, 30).addBox(-13.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), PartPose.offset(-5.0f, 21.0f, -4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(27, 24).addBox(0.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), PartPose.offset(5.0f, 21.0f, -4.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    public void setupAnim(TurtleRenderState turtleRenderState) {
        super.setupAnim(turtleRenderState);
        float f = turtleRenderState.walkAnimationPos;
        float f2 = turtleRenderState.walkAnimationSpeed;
        if (turtleRenderState.isOnLand) {
            float f3 = turtleRenderState.isLayingEgg ? 4.0f : 1.0f;
            float f4 = turtleRenderState.isLayingEgg ? 2.0f : 1.0f;
            float f5 = f * 5.0f;
            float f6 = Mth.cos(f3 * f5);
            float f7 = Mth.cos(f5);
            this.rightFrontLeg.yRot = -f6 * 8.0f * f2 * f4;
            this.leftFrontLeg.yRot = f6 * 8.0f * f2 * f4;
            this.rightHindLeg.yRot = -f7 * 3.0f * f2;
            this.leftHindLeg.yRot = f7 * 3.0f * f2;
        } else {
            float f8;
            float f9 = 0.5f * f2;
            this.rightHindLeg.xRot = f8 = Mth.cos(f * 0.6662f * 0.6f) * f9;
            this.leftHindLeg.xRot = -f8;
            this.rightFrontLeg.zRot = -f8;
            this.leftFrontLeg.zRot = f8;
        }
        this.eggBelly.visible = turtleRenderState.hasEgg;
        if (this.eggBelly.visible) {
            this.root.y -= 1.0f;
        }
    }
}

