/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.ChickenVariant;

public class ChickenRenderState
extends LivingEntityRenderState {
    public float flap;
    public float flapSpeed;
    @Nullable
    public ChickenVariant variant;
}

