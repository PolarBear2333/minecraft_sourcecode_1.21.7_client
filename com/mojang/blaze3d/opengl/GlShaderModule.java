/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringUtils
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public class GlShaderModule
implements AutoCloseable {
    private static final int NOT_ALLOCATED = -1;
    public static final GlShaderModule INVALID_SHADER = new GlShaderModule(-1, ResourceLocation.withDefaultNamespace("invalid"), ShaderType.VERTEX);
    private final ResourceLocation id;
    private int shaderId;
    private final ShaderType type;

    public GlShaderModule(int n, ResourceLocation resourceLocation, ShaderType shaderType) {
        this.id = resourceLocation;
        this.shaderId = n;
        this.type = shaderType;
    }

    public static GlShaderModule compile(ResourceLocation resourceLocation, ShaderType shaderType, String string) throws ShaderManager.CompilationException {
        RenderSystem.assertOnRenderThread();
        int n = GlStateManager.glCreateShader(GlConst.toGl(shaderType));
        GlStateManager.glShaderSource(n, string);
        GlStateManager.glCompileShader(n);
        if (GlStateManager.glGetShaderi(n, 35713) == 0) {
            String string2 = StringUtils.trim((String)GlStateManager.glGetShaderInfoLog(n, 32768));
            throw new ShaderManager.CompilationException("Couldn't compile " + shaderType.getName() + " shader (" + String.valueOf(resourceLocation) + ") : " + string2);
        }
        return new GlShaderModule(n, resourceLocation, shaderType);
    }

    @Override
    public void close() {
        if (this.shaderId == -1) {
            throw new IllegalStateException("Already closed");
        }
        RenderSystem.assertOnRenderThread();
        GlStateManager.glDeleteShader(this.shaderId);
        this.shaderId = -1;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public int getShaderId() {
        return this.shaderId;
    }

    public String getDebugLabel() {
        return this.type.idConverter().idToFile(this.id).toString();
    }
}

