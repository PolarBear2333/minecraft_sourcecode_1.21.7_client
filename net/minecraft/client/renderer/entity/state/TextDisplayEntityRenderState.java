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

public class TextDisplayEntityRenderState
extends DisplayEntityRenderState {
    @Nullable
    public Display.TextDisplay.TextRenderState textRenderState;
    @Nullable
    public Display.TextDisplay.CachedInfo cachedInfo;

    @Override
    public boolean hasSubState() {
        return this.textRenderState != null;
    }
}

