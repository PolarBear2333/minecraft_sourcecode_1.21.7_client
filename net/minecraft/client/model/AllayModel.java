/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionfc;

public class AllayModel
extends EntityModel<AllayRenderState>
implements ArmedModel {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = 0.7853982f;
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464f;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = -1.0471976f;

    public AllayModel(ModelPart modelPart) {
        super(modelPart.getChild("root"), RenderType::entityTranslucent);
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 23.5f, 0.0f));
        partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -3.99f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 16).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(-0.2f)), PartPose.offset(0.0f, -4.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new CubeDeformation(-0.01f)), PartPose.offset(-1.75f, 0.5f, 0.0f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new CubeDeformation(-0.01f)), PartPose.offset(1.75f, 0.5f, 0.0f));
        partDefinition3.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-0.5f, 0.0f, 0.6f));
        partDefinition3.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.5f, 0.0f, 0.6f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(AllayRenderState allayRenderState) {
        float f;
        float f2;
        float f3;
        super.setupAnim(allayRenderState);
        float f4 = allayRenderState.walkAnimationSpeed;
        float f5 = allayRenderState.walkAnimationPos;
        float f6 = allayRenderState.ageInTicks * 20.0f * ((float)Math.PI / 180) + f5;
        float f7 = Mth.cos(f6) * (float)Math.PI * 0.15f + f4;
        float f8 = allayRenderState.ageInTicks * 9.0f * ((float)Math.PI / 180);
        float f9 = Math.min(f4 / 0.3f, 1.0f);
        float f10 = 1.0f - f9;
        float f11 = allayRenderState.holdingAnimationProgress;
        if (allayRenderState.isDancing) {
            f3 = allayRenderState.ageInTicks * 8.0f * ((float)Math.PI / 180) + f4;
            f2 = Mth.cos(f3) * 16.0f * ((float)Math.PI / 180);
            f = allayRenderState.spinningProgress;
            float f12 = Mth.cos(f3) * 14.0f * ((float)Math.PI / 180);
            float f13 = Mth.cos(f3) * 30.0f * ((float)Math.PI / 180);
            this.root.yRot = allayRenderState.isSpinning ? (float)Math.PI * 4 * f : this.root.yRot;
            this.root.zRot = f2 * (1.0f - f);
            this.head.yRot = f13 * (1.0f - f);
            this.head.zRot = f12 * (1.0f - f);
        } else {
            this.head.xRot = allayRenderState.xRot * ((float)Math.PI / 180);
            this.head.yRot = allayRenderState.yRot * ((float)Math.PI / 180);
        }
        this.right_wing.xRot = 0.43633232f * (1.0f - f9);
        this.right_wing.yRot = -0.7853982f + f7;
        this.left_wing.xRot = 0.43633232f * (1.0f - f9);
        this.left_wing.yRot = 0.7853982f - f7;
        this.body.xRot = f9 * 0.7853982f;
        f3 = f11 * Mth.lerp(f9, -1.0471976f, -1.134464f);
        this.root.y += (float)Math.cos(f8) * 0.25f * f10;
        this.right_arm.xRot = f3;
        this.left_arm.xRot = f3;
        f2 = f10 * (1.0f - f11);
        f = 0.43633232f - Mth.cos(f8 + 4.712389f) * (float)Math.PI * 0.075f * f2;
        this.left_arm.zRot = -f;
        this.right_arm.zRot = f;
        this.right_arm.yRot = 0.27925268f * f11;
        this.left_arm.yRot = -0.27925268f * f11;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        float f = 1.0f;
        float f2 = 3.0f;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0f, 0.0625f, 0.1875f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation(this.right_arm.xRot));
        poseStack.scale(0.7f, 0.7f, 0.7f);
        poseStack.translate(0.0625f, 0.0f, 0.0f);
    }
}

