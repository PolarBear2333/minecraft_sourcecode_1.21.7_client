/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;

public class FontTexture
extends AbstractTexture
implements Dumpable {
    private static final int SIZE = 256;
    private final GlyphRenderTypes renderTypes;
    private final boolean colored;
    private final Node root;

    public FontTexture(Supplier<String> supplier, GlyphRenderTypes glyphRenderTypes, boolean bl) {
        this.colored = bl;
        this.root = new Node(0, 0, 256, 256);
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture(supplier, 7, bl ? TextureFormat.RGBA8 : TextureFormat.RED8, 256, 256, 1, 1);
        this.texture.setTextureFilter(FilterMode.NEAREST, false);
        this.textureView = gpuDevice.createTextureView(this.texture);
        this.renderTypes = glyphRenderTypes;
    }

    @Nullable
    public BakedGlyph add(SheetGlyphInfo sheetGlyphInfo) {
        if (sheetGlyphInfo.isColored() != this.colored) {
            return null;
        }
        Node node = this.root.insert(sheetGlyphInfo);
        if (node != null && this.texture != null && this.textureView != null) {
            sheetGlyphInfo.upload(node.x, node.y, this.texture);
            float f = 256.0f;
            float f2 = 256.0f;
            float f3 = 0.01f;
            return new BakedGlyph(this.renderTypes, this.textureView, ((float)node.x + 0.01f) / 256.0f, ((float)node.x - 0.01f + (float)sheetGlyphInfo.getPixelWidth()) / 256.0f, ((float)node.y + 0.01f) / 256.0f, ((float)node.y - 0.01f + (float)sheetGlyphInfo.getPixelHeight()) / 256.0f, sheetGlyphInfo.getLeft(), sheetGlyphInfo.getRight(), sheetGlyphInfo.getTop(), sheetGlyphInfo.getBottom());
        }
        return null;
    }

    @Override
    public void dumpContents(ResourceLocation resourceLocation, Path path) {
        if (this.texture == null) {
            return;
        }
        String string = resourceLocation.toDebugFileName();
        TextureUtil.writeAsPNG(path, string, this.texture, 0, n -> (n & 0xFF000000) == 0 ? -16777216 : n);
    }

    static class Node {
        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private Node left;
        @Nullable
        private Node right;
        private boolean occupied;

        Node(int n, int n2, int n3, int n4) {
            this.x = n;
            this.y = n2;
            this.width = n3;
            this.height = n4;
        }

        @Nullable
        Node insert(SheetGlyphInfo sheetGlyphInfo) {
            if (this.left != null && this.right != null) {
                Node node = this.left.insert(sheetGlyphInfo);
                if (node == null) {
                    node = this.right.insert(sheetGlyphInfo);
                }
                return node;
            }
            if (this.occupied) {
                return null;
            }
            int n = sheetGlyphInfo.getPixelWidth();
            int n2 = sheetGlyphInfo.getPixelHeight();
            if (n > this.width || n2 > this.height) {
                return null;
            }
            if (n == this.width && n2 == this.height) {
                this.occupied = true;
                return this;
            }
            int n3 = this.width - n;
            int n4 = this.height - n2;
            if (n3 > n4) {
                this.left = new Node(this.x, this.y, n, this.height);
                this.right = new Node(this.x + n + 1, this.y, this.width - n - 1, this.height);
            } else {
                this.left = new Node(this.x, this.y, this.width, n2);
                this.right = new Node(this.x, this.y + n2 + 1, this.width, this.height - n2 - 1);
            }
            return this.left.insert(sheetGlyphInfo);
        }
    }
}

