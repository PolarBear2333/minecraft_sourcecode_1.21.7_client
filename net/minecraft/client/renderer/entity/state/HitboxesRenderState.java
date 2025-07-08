/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.renderer.entity.state;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;

public record HitboxesRenderState(double viewX, double viewY, double viewZ, ImmutableList<HitboxRenderState> hitboxes) {
}

