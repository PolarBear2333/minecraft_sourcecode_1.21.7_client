/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 *  org.lwjgl.opengl.GL31
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderManager;
import org.jetbrains.annotations.VisibleForTesting;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;

public class GlProgram
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Set<String> BUILT_IN_UNIFORMS = Sets.newHashSet((Object[])new String[]{"Projection", "Lighting", "Fog", "Globals"});
    public static GlProgram INVALID_PROGRAM = new GlProgram(-1, "invalid");
    private final Map<String, Uniform> uniformsByName = new HashMap<String, Uniform>();
    private final int programId;
    private final String debugLabel;

    private GlProgram(int n, String string) {
        this.programId = n;
        this.debugLabel = string;
    }

    public static GlProgram link(GlShaderModule glShaderModule, GlShaderModule glShaderModule2, VertexFormat vertexFormat, String string) throws ShaderManager.CompilationException {
        int n = GlStateManager.glCreateProgram();
        if (n <= 0) {
            throw new ShaderManager.CompilationException("Could not create shader program (returned program ID " + n + ")");
        }
        int n2 = 0;
        for (String string2 : vertexFormat.getElementAttributeNames()) {
            GlStateManager._glBindAttribLocation(n, n2, string2);
            ++n2;
        }
        GlStateManager.glAttachShader(n, glShaderModule.getShaderId());
        GlStateManager.glAttachShader(n, glShaderModule2.getShaderId());
        GlStateManager.glLinkProgram(n);
        int n3 = GlStateManager.glGetProgrami(n, 35714);
        if (n3 == 0) {
            String string2;
            string2 = GlStateManager.glGetProgramInfoLog(n, 32768);
            throw new ShaderManager.CompilationException("Error encountered when linking program containing VS " + String.valueOf(glShaderModule.getId()) + " and FS " + String.valueOf(glShaderModule2.getId()) + ". Log output: " + string2);
        }
        return new GlProgram(n, string);
    }

    /*
     * WARNING - void declaration
     */
    public void setupUniforms(List<RenderPipeline.UniformDescription> list, List<String> list2) {
        void var6_11;
        int n = 0;
        int n2 = 0;
        for (RenderPipeline.UniformDescription object : list) {
            String string = object.name();
            Uniform.Utb utb = switch (object.type()) {
                default -> throw new MatchException(null, null);
                case UniformType.UNIFORM_BUFFER -> {
                    int var9_18 = GL31.glGetUniformBlockIndex((int)this.programId, (CharSequence)string);
                    if (var9_18 == -1) {
                        yield null;
                    }
                    int var10_19 = n++;
                    GL31.glUniformBlockBinding((int)this.programId, (int)var9_18, (int)var10_19);
                    yield new Uniform.Ubo(var10_19);
                }
                case UniformType.TEXEL_BUFFER -> {
                    int var9_18 = GlStateManager._glGetUniformLocation(this.programId, string);
                    if (var9_18 == -1) {
                        LOGGER.warn("{} shader program does not use utb {} defined in the pipeline. This might be a bug.", (Object)this.debugLabel, (Object)string);
                        yield null;
                    }
                    int var10_19 = n2++;
                    yield new Uniform.Utb(var9_18, var10_19, Objects.requireNonNull(object.textureFormat()));
                }
            };
            if (utb == null) continue;
            this.uniformsByName.put(string, utb);
        }
        for (String string : list2) {
            int n3 = GlStateManager._glGetUniformLocation(this.programId, string);
            if (n3 == -1) {
                LOGGER.warn("{} shader program does not use sampler {} defined in the pipeline. This might be a bug.", (Object)this.debugLabel, (Object)string);
                continue;
            }
            int n4 = n2++;
            this.uniformsByName.put(string, new Uniform.Sampler(n3, n4));
        }
        int n5 = GlStateManager.glGetProgrami(this.programId, 35382);
        boolean bl = false;
        while (var6_11 < n5) {
            String string = GL31.glGetActiveUniformBlockName((int)this.programId, (int)var6_11);
            if (!this.uniformsByName.containsKey(string)) {
                if (!list2.contains(string) && BUILT_IN_UNIFORMS.contains(string)) {
                    int n6 = n++;
                    GL31.glUniformBlockBinding((int)this.programId, (int)var6_11, (int)n6);
                    this.uniformsByName.put(string, new Uniform.Ubo(n6));
                } else {
                    LOGGER.warn("Found unknown and unsupported uniform {} in {}", (Object)string, (Object)this.debugLabel);
                }
            }
            ++var6_11;
        }
    }

    @Override
    public void close() {
        this.uniformsByName.values().forEach(Uniform::close);
        GlStateManager.glDeleteProgram(this.programId);
    }

    @Nullable
    public Uniform getUniform(String string) {
        RenderSystem.assertOnRenderThread();
        return this.uniformsByName.get(string);
    }

    @VisibleForTesting
    public int getProgramId() {
        return this.programId;
    }

    public String toString() {
        return this.debugLabel;
    }

    public String getDebugLabel() {
        return this.debugLabel;
    }

    public Map<String, Uniform> getUniforms() {
        return this.uniformsByName;
    }
}

