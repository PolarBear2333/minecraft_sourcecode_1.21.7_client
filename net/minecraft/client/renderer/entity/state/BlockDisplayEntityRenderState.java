/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.world.entity.Display;

public class BlockDisplayEntityRenderState
extends DisplayEntityRenderState {
    @Nullable
    public Display.BlockDisplay.BlockRenderState blockRenderState;

    @Override
    public boolean hasSubState() {
        return this.blockRenderState != null;
    }
}

