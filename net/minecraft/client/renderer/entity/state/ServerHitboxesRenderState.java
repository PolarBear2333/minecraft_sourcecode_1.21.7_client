/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;

public record ServerHitboxesRenderState(boolean missing, double serverEntityX, double serverEntityY, double serverEntityZ, double deltaMovementX, double deltaMovementY, double deltaMovementZ, float eyeHeight, @Nullable HitboxesRenderState hitboxes) {
    public ServerHitboxesRenderState(boolean bl) {
        this(bl, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0f, null);
    }
}

