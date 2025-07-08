/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;

public class TextureTarget
extends RenderTarget {
    public TextureTarget(@Nullable String string, int n, int n2, boolean bl) {
        super(string, bl);
        RenderSystem.assertOnRenderThread();
        this.resize(n, n2);
    }
}

