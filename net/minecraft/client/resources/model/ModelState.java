/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.resources.model;

import com.mojang.math.Transformation;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public interface ModelState {
    public static final Matrix4fc NO_TRANSFORM = new Matrix4f();

    default public Transformation transformation() {
        return Transformation.identity();
    }

    default public Matrix4fc faceTransformation(Direction direction) {
        return NO_TRANSFORM;
    }

    default public Matrix4fc inverseFaceTransformation(Direction direction) {
        return NO_TRANSFORM;
    }
}

