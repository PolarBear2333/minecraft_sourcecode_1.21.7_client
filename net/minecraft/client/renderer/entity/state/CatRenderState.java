/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class CatRenderState
extends FelineRenderState {
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/cat/tabby.png");
    public ResourceLocation texture = DEFAULT_TEXTURE;
    public boolean isLyingOnTopOfSleepingPlayer;
    @Nullable
    public DyeColor collarColor;
}

