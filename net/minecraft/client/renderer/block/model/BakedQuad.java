/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public record BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, int lightEmission) {
    public boolean isTinted() {
        return this.tintIndex != -1;
    }
}

