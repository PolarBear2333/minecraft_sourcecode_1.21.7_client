/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandRenderState
extends HumanoidRenderState {
    public float yRot;
    public float wiggle;
    public boolean isMarker;
    public boolean isSmall;
    public boolean showArms;
    public boolean showBasePlate = true;
    public Rotations headPose = ArmorStand.DEFAULT_HEAD_POSE;
    public Rotations bodyPose = ArmorStand.DEFAULT_BODY_POSE;
    public Rotations leftArmPose = ArmorStand.DEFAULT_LEFT_ARM_POSE;
    public Rotations rightArmPose = ArmorStand.DEFAULT_RIGHT_ARM_POSE;
    public Rotations leftLegPose = ArmorStand.DEFAULT_LEFT_LEG_POSE;
    public Rotations rightLegPose = ArmorStand.DEFAULT_RIGHT_LEG_POSE;
}

