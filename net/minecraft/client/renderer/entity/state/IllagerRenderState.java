/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;

public class IllagerRenderState
extends ArmedEntityRenderState {
    public boolean isRiding;
    public boolean isAggressive;
    public HumanoidArm mainArm = HumanoidArm.RIGHT;
    public AbstractIllager.IllagerArmPose armPose = AbstractIllager.IllagerArmPose.NEUTRAL;
    public int maxCrossbowChargeDuration;
    public int ticksUsingItem;
    public float attackAnim;
}

