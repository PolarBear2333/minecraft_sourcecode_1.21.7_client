/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteCoordinateExpander
implements VertexConsumer {
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public SpriteCoordinateExpander(VertexConsumer vertexConsumer, TextureAtlasSprite textureAtlasSprite) {
        this.delegate = vertexConsumer;
        this.sprite = textureAtlasSprite;
    }

    @Override
    public VertexConsumer addVertex(float f, float f2, float f3) {
        return this.delegate.addVertex(f, f2, f3);
    }

    @Override
    public VertexConsumer setColor(int n, int n2, int n3, int n4) {
        return this.delegate.setColor(n, n2, n3, n4);
    }

    @Override
    public VertexConsumer setUv(float f, float f2) {
        return this.delegate.setUv(this.sprite.getU(f), this.sprite.getV(f2));
    }

    @Override
    public VertexConsumer setUv1(int n, int n2) {
        return this.delegate.setUv1(n, n2);
    }

    @Override
    public VertexConsumer setUv2(int n, int n2) {
        return this.delegate.setUv2(n, n2);
    }

    @Override
    public VertexConsumer setNormal(float f, float f2, float f3) {
        return this.delegate.setNormal(f, f2, f3);
    }

    @Override
    public void addVertex(float f, float f2, float f3, int n, float f4, float f5, int n2, int n3, float f6, float f7, float f8) {
        this.delegate.addVertex(f, f2, f3, n, this.sprite.getU(f4), this.sprite.getV(f5), n2, n3, f6, f7, f8);
    }
}

