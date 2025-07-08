/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;

public class TextureAtlasSprite {
    private final ResourceLocation atlasLocation;
    private final SpriteContents contents;
    private final boolean animated;
    final int x;
    final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;

    protected TextureAtlasSprite(ResourceLocation resourceLocation, SpriteContents spriteContents, int n, int n2, int n3, int n4) {
        this.atlasLocation = resourceLocation;
        this.contents = spriteContents;
        this.animated = spriteContents.metadata().getSection(AnimationMetadataSection.TYPE).isPresent();
        this.x = n3;
        this.y = n4;
        this.u0 = (float)n3 / (float)n;
        this.u1 = (float)(n3 + spriteContents.width()) / (float)n;
        this.v0 = (float)n4 / (float)n2;
        this.v1 = (float)(n4 + spriteContents.height()) / (float)n2;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public float getU0() {
        return this.u0;
    }

    public float getU1() {
        return this.u1;
    }

    public SpriteContents contents() {
        return this.contents;
    }

    public boolean isAnimated() {
        return this.animated;
    }

    @Nullable
    public Ticker createTicker() {
        final SpriteTicker spriteTicker = this.contents.createTicker();
        if (spriteTicker != null) {
            return new Ticker(){

                @Override
                public void tickAndUpload(GpuTexture gpuTexture) {
                    spriteTicker.tickAndUpload(TextureAtlasSprite.this.x, TextureAtlasSprite.this.y, gpuTexture);
                }

                @Override
                public void close() {
                    spriteTicker.close();
                }
            };
        }
        return null;
    }

    public float getU(float f) {
        float f2 = this.u1 - this.u0;
        return this.u0 + f2 * f;
    }

    public float getUOffset(float f) {
        float f2 = this.u1 - this.u0;
        return (f - this.u0) / f2;
    }

    public float getV0() {
        return this.v0;
    }

    public float getV1() {
        return this.v1;
    }

    public float getV(float f) {
        float f2 = this.v1 - this.v0;
        return this.v0 + f2 * f;
    }

    public float getVOffset(float f) {
        float f2 = this.v1 - this.v0;
        return (f - this.v0) / f2;
    }

    public ResourceLocation atlasLocation() {
        return this.atlasLocation;
    }

    public String toString() {
        return "TextureAtlasSprite{contents='" + String.valueOf(this.contents) + "', u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + "}";
    }

    public void uploadFirstFrame(GpuTexture gpuTexture) {
        this.contents.uploadFirstFrame(this.x, this.y, gpuTexture);
    }

    private float atlasSize() {
        float f = (float)this.contents.width() / (this.u1 - this.u0);
        float f2 = (float)this.contents.height() / (this.v1 - this.v0);
        return Math.max(f2, f);
    }

    public float uvShrinkRatio() {
        return 4.0f / this.atlasSize();
    }

    public VertexConsumer wrap(VertexConsumer vertexConsumer) {
        return new SpriteCoordinateExpander(vertexConsumer, this);
    }

    public static interface Ticker
    extends AutoCloseable {
        public void tickAndUpload(GpuTexture var1);

        @Override
        public void close();
    }
}

