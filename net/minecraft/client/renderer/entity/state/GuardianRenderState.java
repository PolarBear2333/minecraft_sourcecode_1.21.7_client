/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

public class GuardianRenderState
extends LivingEntityRenderState {
    public float spikesAnimation;
    public float tailAnimation;
    public Vec3 eyePosition = Vec3.ZERO;
    @Nullable
    public Vec3 lookDirection;
    @Nullable
    public Vec3 lookAtPosition;
    @Nullable
    public Vec3 attackTargetPosition;
    public float attackTime;
    public float attackScale;
}

