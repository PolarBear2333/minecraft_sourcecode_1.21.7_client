/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.block.model;

import net.minecraft.core.Direction;
import org.joml.Vector3f;

public record BlockElementRotation(Vector3f origin, Direction.Axis axis, float angle, boolean rescale) {
}

