/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class DynamicTexture
extends AbstractTexture
implements Dumpable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private NativeImage pixels;

    public DynamicTexture(Supplier<String> supplier, NativeImage nativeImage) {
        this.pixels = nativeImage;
        this.createTexture(supplier);
        this.upload();
    }

    public DynamicTexture(String string, int n, int n2, boolean bl) {
        this.pixels = new NativeImage(n, n2, bl);
        this.createTexture(string);
    }

    public DynamicTexture(Supplier<String> supplier, int n, int n2, boolean bl) {
        this.pixels = new NativeImage(n, n2, bl);
        this.createTexture(supplier);
    }

    private void createTexture(Supplier<String> supplier) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture(supplier, 5, TextureFormat.RGBA8, this.pixels.getWidth(), this.pixels.getHeight(), 1, 1);
        this.texture.setTextureFilter(FilterMode.NEAREST, false);
        this.textureView = gpuDevice.createTextureView(this.texture);
    }

    private void createTexture(String string) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture(string, 5, TextureFormat.RGBA8, this.pixels.getWidth(), this.pixels.getHeight(), 1, 1);
        this.texture.setTextureFilter(FilterMode.NEAREST, false);
        this.textureView = gpuDevice.createTextureView(this.texture);
    }

    public void upload() {
        if (this.pixels != null && this.texture != null) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.pixels);
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", (Object)this.getTexture().getLabel());
        }
    }

    @Nullable
    public NativeImage getPixels() {
        return this.pixels;
    }

    public void setPixels(NativeImage nativeImage) {
        if (this.pixels != null) {
            this.pixels.close();
        }
        this.pixels = nativeImage;
    }

    @Override
    public void close() {
        if (this.pixels != null) {
            this.pixels.close();
            this.pixels = null;
        }
        super.close();
    }

    @Override
    public void dumpContents(ResourceLocation resourceLocation, Path path) throws IOException {
        if (this.pixels != null) {
            String string = resourceLocation.toDebugFileName() + ".png";
            Path path2 = path.resolve(string);
            this.pixels.writeToFile(path2);
        }
    }
}

