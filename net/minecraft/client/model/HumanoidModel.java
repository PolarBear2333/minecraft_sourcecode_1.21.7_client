/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class HumanoidModel<T extends HumanoidRenderState>
extends EntityModel<T>
implements ArmedModel,
HeadedModel {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0f, 0.0f, 2.0f, 2.0f, 24.0f, Set.of("head"));
    public static final float OVERLAY_SCALE = 0.25f;
    public static final float HAT_OVERLAY_SCALE = 0.5f;
    public static final float LEGGINGS_OVERLAY_SCALE = -0.1f;
    private static final float DUCK_WALK_ROTATION = 0.005f;
    private static final float SPYGLASS_ARM_ROT_Y = 0.2617994f;
    private static final float SPYGLASS_ARM_ROT_X = 1.9198622f;
    private static final float SPYGLASS_ARM_CROUCH_ROT_X = 0.2617994f;
    private static final float HIGHEST_SHIELD_BLOCKING_ANGLE = -1.3962634f;
    private static final float LOWEST_SHIELD_BLOCKING_ANGLE = 0.43633232f;
    private static final float HORIZONTAL_SHIELD_MOVEMENT_LIMIT = 0.5235988f;
    public static final float TOOT_HORN_XROT_BASE = 1.4835298f;
    public static final float TOOT_HORN_YROT_BASE = 0.5235988f;
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public HumanoidModel(ModelPart modelPart) {
        this(modelPart, RenderType::entityCutoutNoCull);
    }

    public HumanoidModel(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        super(modelPart, function);
        this.head = modelPart.getChild("head");
        this.hat = this.head.getChild("hat");
        this.body = modelPart.getChild("body");
        this.rightArm = modelPart.getChild("right_arm");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, float f) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation.extend(0.5f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-1.9f, 12.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f + f, 0.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(T t) {
        boolean bl;
        super.setupAnim(t);
        ArmPose armPose = ((HumanoidRenderState)t).leftArmPose;
        ArmPose armPose2 = ((HumanoidRenderState)t).rightArmPose;
        float f = ((HumanoidRenderState)t).swimAmount;
        boolean bl2 = ((HumanoidRenderState)t).isFallFlying;
        this.head.xRot = ((HumanoidRenderState)t).xRot * ((float)Math.PI / 180);
        this.head.yRot = ((HumanoidRenderState)t).yRot * ((float)Math.PI / 180);
        if (bl2) {
            this.head.xRot = -0.7853982f;
        } else if (f > 0.0f) {
            this.head.xRot = Mth.rotLerpRad(f, this.head.xRot, -0.7853982f);
        }
        float f2 = ((HumanoidRenderState)t).walkAnimationPos;
        float f3 = ((HumanoidRenderState)t).walkAnimationSpeed;
        this.rightArm.xRot = Mth.cos(f2 * 0.6662f + (float)Math.PI) * 2.0f * f3 * 0.5f / ((HumanoidRenderState)t).speedValue;
        this.leftArm.xRot = Mth.cos(f2 * 0.6662f) * 2.0f * f3 * 0.5f / ((HumanoidRenderState)t).speedValue;
        this.rightLeg.xRot = Mth.cos(f2 * 0.6662f) * 1.4f * f3 / ((HumanoidRenderState)t).speedValue;
        this.leftLeg.xRot = Mth.cos(f2 * 0.6662f + (float)Math.PI) * 1.4f * f3 / ((HumanoidRenderState)t).speedValue;
        this.rightLeg.yRot = 0.005f;
        this.leftLeg.yRot = -0.005f;
        this.rightLeg.zRot = 0.005f;
        this.leftLeg.zRot = -0.005f;
        if (((HumanoidRenderState)t).isPassenger) {
            this.rightArm.xRot += -0.62831855f;
            this.leftArm.xRot += -0.62831855f;
            this.rightLeg.xRot = -1.4137167f;
            this.rightLeg.yRot = 0.31415927f;
            this.rightLeg.zRot = 0.07853982f;
            this.leftLeg.xRot = -1.4137167f;
            this.leftLeg.yRot = -0.31415927f;
            this.leftLeg.zRot = -0.07853982f;
        }
        boolean bl3 = bl = ((HumanoidRenderState)t).mainArm == HumanoidArm.RIGHT;
        if (((HumanoidRenderState)t).isUsingItem) {
            boolean bl4 = var9_9 = ((HumanoidRenderState)t).useItemHand == InteractionHand.MAIN_HAND;
            if (var9_9 == bl) {
                this.poseRightArm(t, armPose2);
            } else {
                this.poseLeftArm(t, armPose);
            }
        } else {
            boolean bl5 = var9_9 = bl ? armPose.isTwoHanded() : armPose2.isTwoHanded();
            if (bl != var9_9) {
                this.poseLeftArm(t, armPose);
                this.poseRightArm(t, armPose2);
            } else {
                this.poseRightArm(t, armPose2);
                this.poseLeftArm(t, armPose);
            }
        }
        this.setupAttackAnimation(t, ((HumanoidRenderState)t).ageInTicks);
        if (((HumanoidRenderState)t).isCrouching) {
            this.body.xRot = 0.5f;
            this.rightArm.xRot += 0.4f;
            this.leftArm.xRot += 0.4f;
            this.rightLeg.z += 4.0f;
            this.leftLeg.z += 4.0f;
            this.head.y += 4.2f;
            this.body.y += 3.2f;
            this.leftArm.y += 3.2f;
            this.rightArm.y += 3.2f;
        }
        if (armPose2 != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.rightArm, ((HumanoidRenderState)t).ageInTicks, 1.0f);
        }
        if (armPose != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.leftArm, ((HumanoidRenderState)t).ageInTicks, -1.0f);
        }
        if (f > 0.0f) {
            float f4;
            float f5;
            float f6 = f2 % 26.0f;
            HumanoidArm humanoidArm = ((HumanoidRenderState)t).attackArm;
            float f7 = humanoidArm == HumanoidArm.RIGHT && ((HumanoidRenderState)t).attackTime > 0.0f ? 0.0f : f;
            float f8 = f5 = humanoidArm == HumanoidArm.LEFT && ((HumanoidRenderState)t).attackTime > 0.0f ? 0.0f : f;
            if (!((HumanoidRenderState)t).isUsingItem) {
                if (f6 < 14.0f) {
                    this.leftArm.xRot = Mth.rotLerpRad(f5, this.leftArm.xRot, 0.0f);
                    this.rightArm.xRot = Mth.lerp(f7, this.rightArm.xRot, 0.0f);
                    this.leftArm.yRot = Mth.rotLerpRad(f5, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(f7, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = Mth.rotLerpRad(f5, this.leftArm.zRot, (float)Math.PI + 1.8707964f * this.quadraticArmUpdate(f6) / this.quadraticArmUpdate(14.0f));
                    this.rightArm.zRot = Mth.lerp(f7, this.rightArm.zRot, (float)Math.PI - 1.8707964f * this.quadraticArmUpdate(f6) / this.quadraticArmUpdate(14.0f));
                } else if (f6 >= 14.0f && f6 < 22.0f) {
                    f4 = (f6 - 14.0f) / 8.0f;
                    this.leftArm.xRot = Mth.rotLerpRad(f5, this.leftArm.xRot, 1.5707964f * f4);
                    this.rightArm.xRot = Mth.lerp(f7, this.rightArm.xRot, 1.5707964f * f4);
                    this.leftArm.yRot = Mth.rotLerpRad(f5, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(f7, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = Mth.rotLerpRad(f5, this.leftArm.zRot, 5.012389f - 1.8707964f * f4);
                    this.rightArm.zRot = Mth.lerp(f7, this.rightArm.zRot, 1.2707963f + 1.8707964f * f4);
                } else if (f6 >= 22.0f && f6 < 26.0f) {
                    f4 = (f6 - 22.0f) / 4.0f;
                    this.leftArm.xRot = Mth.rotLerpRad(f5, this.leftArm.xRot, 1.5707964f - 1.5707964f * f4);
                    this.rightArm.xRot = Mth.lerp(f7, this.rightArm.xRot, 1.5707964f - 1.5707964f * f4);
                    this.leftArm.yRot = Mth.rotLerpRad(f5, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(f7, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = Mth.rotLerpRad(f5, this.leftArm.zRot, (float)Math.PI);
                    this.rightArm.zRot = Mth.lerp(f7, this.rightArm.zRot, (float)Math.PI);
                }
            }
            f4 = 0.3f;
            float f9 = 0.33333334f;
            this.leftLeg.xRot = Mth.lerp(f, this.leftLeg.xRot, 0.3f * Mth.cos(f2 * 0.33333334f + (float)Math.PI));
            this.rightLeg.xRot = Mth.lerp(f, this.rightLeg.xRot, 0.3f * Mth.cos(f2 * 0.33333334f));
        }
    }

    private void poseRightArm(T t, ArmPose armPose) {
        switch (armPose.ordinal()) {
            case 0: {
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 2: {
                this.poseBlockingArm(this.rightArm, true);
                break;
            }
            case 1: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.31415927f;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 4: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - (float)Math.PI;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 3: {
                this.rightArm.yRot = -0.1f + this.head.yRot;
                this.leftArm.yRot = 0.1f + this.head.yRot + 0.4f;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case 5: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, ((HumanoidRenderState)t).maxCrossbowChargeDuration, ((HumanoidRenderState)t).ticksUsingItem, true);
                break;
            }
            case 6: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
                break;
            }
            case 9: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.62831855f;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 7: {
                this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622f - (((HumanoidRenderState)t).isCrouching ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.rightArm.yRot = this.head.yRot - 0.2617994f;
                break;
            }
            case 8: {
                this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2f, 1.2f) - 1.4835298f;
                this.rightArm.yRot = this.head.yRot - 0.5235988f;
            }
        }
    }

    private void poseLeftArm(T t, ArmPose armPose) {
        switch (armPose.ordinal()) {
            case 0: {
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 2: {
                this.poseBlockingArm(this.leftArm, false);
                break;
            }
            case 1: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.31415927f;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 4: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - (float)Math.PI;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 3: {
                this.rightArm.yRot = -0.1f + this.head.yRot - 0.4f;
                this.leftArm.yRot = 0.1f + this.head.yRot;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case 5: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, ((HumanoidRenderState)t).maxCrossbowChargeDuration, ((HumanoidRenderState)t).ticksUsingItem, false);
                break;
            }
            case 6: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
                break;
            }
            case 9: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.62831855f;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 7: {
                this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622f - (((HumanoidRenderState)t).isCrouching ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.leftArm.yRot = this.head.yRot + 0.2617994f;
                break;
            }
            case 8: {
                this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2f, 1.2f) - 1.4835298f;
                this.leftArm.yRot = this.head.yRot + 0.5235988f;
            }
        }
    }

    private void poseBlockingArm(ModelPart modelPart, boolean bl) {
        modelPart.xRot = modelPart.xRot * 0.5f - 0.9424779f + Mth.clamp(this.head.xRot, -1.3962634f, 0.43633232f);
        modelPart.yRot = (bl ? -30.0f : 30.0f) * ((float)Math.PI / 180) + Mth.clamp(this.head.yRot, -0.5235988f, 0.5235988f);
    }

    protected void setupAttackAnimation(T t, float f) {
        float f2 = ((HumanoidRenderState)t).attackTime;
        if (f2 <= 0.0f) {
            return;
        }
        HumanoidArm humanoidArm = ((HumanoidRenderState)t).attackArm;
        ModelPart modelPart = this.getArm(humanoidArm);
        float f3 = f2;
        this.body.yRot = Mth.sin(Mth.sqrt(f3) * ((float)Math.PI * 2)) * 0.2f;
        if (humanoidArm == HumanoidArm.LEFT) {
            this.body.yRot *= -1.0f;
        }
        float f4 = ((HumanoidRenderState)t).ageScale;
        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0f * f4;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0f * f4;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0f * f4;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0f * f4;
        this.rightArm.yRot += this.body.yRot;
        this.leftArm.yRot += this.body.yRot;
        this.leftArm.xRot += this.body.yRot;
        f3 = 1.0f - f2;
        f3 *= f3;
        f3 *= f3;
        f3 = 1.0f - f3;
        float f5 = Mth.sin(f3 * (float)Math.PI);
        float f6 = Mth.sin(f2 * (float)Math.PI) * -(this.head.xRot - 0.7f) * 0.75f;
        modelPart.xRot -= f5 * 1.2f + f6;
        modelPart.yRot += this.body.yRot * 2.0f;
        modelPart.zRot += Mth.sin(f2 * (float)Math.PI) * -0.4f;
    }

    private float quadraticArmUpdate(float f) {
        return -65.0f * f + f * f;
    }

    public void copyPropertiesTo(HumanoidModel<T> humanoidModel) {
        humanoidModel.head.copyFrom(this.head);
        humanoidModel.body.copyFrom(this.body);
        humanoidModel.rightArm.copyFrom(this.rightArm);
        humanoidModel.leftArm.copyFrom(this.leftArm);
        humanoidModel.rightLeg.copyFrom(this.rightLeg);
        humanoidModel.leftLeg.copyFrom(this.leftLeg);
    }

    public void setAllVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.body.visible = bl;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.getArm(humanoidArm).translateAndRotate(poseStack);
    }

    protected ModelPart getArm(HumanoidArm humanoidArm) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return this.leftArm;
        }
        return this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    public static enum ArmPose {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true),
        SPYGLASS(false),
        TOOT_HORN(false),
        BRUSH(false);

        private final boolean twoHanded;

        private ArmPose(boolean bl) {
            this.twoHanded = bl;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }
    }
}

