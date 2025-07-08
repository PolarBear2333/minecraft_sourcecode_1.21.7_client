/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

@DontObfuscate
public interface GpuDevice {
    public CommandEncoder createCommandEncoder();

    public GpuTexture createTexture(@Nullable Supplier<String> var1, int var2, TextureFormat var3, int var4, int var5, int var6, int var7);

    public GpuTexture createTexture(@Nullable String var1, int var2, TextureFormat var3, int var4, int var5, int var6, int var7);

    public GpuTextureView createTextureView(GpuTexture var1);

    public GpuTextureView createTextureView(GpuTexture var1, int var2, int var3);

    public GpuBuffer createBuffer(@Nullable Supplier<String> var1, int var2, int var3);

    public GpuBuffer createBuffer(@Nullable Supplier<String> var1, int var2, ByteBuffer var3);

    public String getImplementationInformation();

    public List<String> getLastDebugMessages();

    public boolean isDebuggingEnabled();

    public String getVendor();

    public String getBackendName();

    public String getVersion();

    public String getRenderer();

    public int getMaxTextureSize();

    public int getUniformOffsetAlignment();

    default public CompiledRenderPipeline precompilePipeline(RenderPipeline renderPipeline) {
        return this.precompilePipeline(renderPipeline, null);
    }

    public CompiledRenderPipeline precompilePipeline(RenderPipeline var1, @Nullable BiFunction<ResourceLocation, ShaderType, String> var2);

    public void clearPipelineCache();

    public List<String> getEnabledExtensions();

    public void close();
}

