/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AbstractPiglinModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

public class PiglinModel
extends AbstractPiglinModel<PiglinRenderState> {
    public PiglinModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(PiglinRenderState piglinRenderState) {
        super.setupAnim(piglinRenderState);
        float f = 0.5235988f;
        float f2 = piglinRenderState.attackTime;
        PiglinArmPose piglinArmPose = piglinRenderState.armPose;
        if (piglinArmPose == PiglinArmPose.DANCING) {
            float f3 = piglinRenderState.ageInTicks / 60.0f;
            this.rightEar.zRot = 0.5235988f + (float)Math.PI / 180 * Mth.sin(f3 * 30.0f) * 10.0f;
            this.leftEar.zRot = -0.5235988f - (float)Math.PI / 180 * Mth.cos(f3 * 30.0f) * 10.0f;
            this.head.x += Mth.sin(f3 * 10.0f);
            this.head.y += Mth.sin(f3 * 40.0f) + 0.4f;
            this.rightArm.zRot = (float)Math.PI / 180 * (70.0f + Mth.cos(f3 * 40.0f) * 10.0f);
            this.leftArm.zRot = this.rightArm.zRot * -1.0f;
            this.rightArm.y += Mth.sin(f3 * 40.0f) * 0.5f - 0.5f;
            this.leftArm.y += Mth.sin(f3 * 40.0f) * 0.5f + 0.5f;
            this.body.y += Mth.sin(f3 * 40.0f) * 0.35f;
        } else if (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && f2 == 0.0f) {
            this.holdWeaponHigh(piglinRenderState);
        } else if (piglinArmPose == PiglinArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, piglinRenderState.mainArm == HumanoidArm.RIGHT);
        } else if (piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, piglinRenderState.maxCrossbowChageDuration, piglinRenderState.ticksUsingItem, piglinRenderState.mainArm == HumanoidArm.RIGHT);
        } else if (piglinArmPose == PiglinArmPose.ADMIRING_ITEM) {
            this.head.xRot = 0.5f;
            this.head.yRot = 0.0f;
            if (piglinRenderState.mainArm == HumanoidArm.LEFT) {
                this.rightArm.yRot = -0.5f;
                this.rightArm.xRot = -0.9f;
            } else {
                this.leftArm.yRot = 0.5f;
                this.leftArm.xRot = -0.9f;
            }
        }
    }

    @Override
    protected void setupAttackAnimation(PiglinRenderState piglinRenderState, float f) {
        float f2 = piglinRenderState.attackTime;
        if (f2 > 0.0f && piglinRenderState.armPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, piglinRenderState.mainArm, f2, piglinRenderState.ageInTicks);
            return;
        }
        super.setupAttackAnimation(piglinRenderState, f);
    }

    private void holdWeaponHigh(PiglinRenderState piglinRenderState) {
        if (piglinRenderState.mainArm == HumanoidArm.LEFT) {
            this.leftArm.xRot = -1.8f;
        } else {
            this.rightArm.xRot = -1.8f;
        }
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
    }
}

