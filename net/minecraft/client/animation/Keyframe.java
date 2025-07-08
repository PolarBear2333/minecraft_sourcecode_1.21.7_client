/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package net.minecraft.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import org.joml.Vector3f;

public record Keyframe(float timestamp, Vector3f target, AnimationChannel.Interpolation interpolation) {
}

