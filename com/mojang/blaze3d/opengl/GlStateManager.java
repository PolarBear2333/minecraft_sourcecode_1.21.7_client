/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.mojang.jtracy.Plot
 *  com.mojang.jtracy.TracyClient
 *  javax.annotation.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL20C
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL32
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.opengl;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@DontObfuscate
public class GlStateManager {
    private static final boolean ON_LINUX = Util.getPlatform() == Util.OS.LINUX;
    private static final Plot PLOT_TEXTURES = TracyClient.createPlot((String)"GPU Textures");
    private static int numTextures = 0;
    private static final Plot PLOT_BUFFERS = TracyClient.createPlot((String)"GPU Buffers");
    private static int numBuffers = 0;
    private static final BlendState BLEND = new BlendState();
    private static final DepthState DEPTH = new DepthState();
    private static final CullState CULL = new CullState();
    private static final PolygonOffsetState POLY_OFFSET = new PolygonOffsetState();
    private static final ColorLogicState COLOR_LOGIC = new ColorLogicState();
    private static final ScissorState SCISSOR = new ScissorState();
    private static int activeTexture;
    private static final TextureState[] TEXTURES;
    private static final ColorMask COLOR_MASK;
    private static int readFbo;
    private static int writeFbo;

    public static void _disableScissorTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.SCISSOR.mode.disable();
    }

    public static void _enableScissorTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.SCISSOR.mode.enable();
    }

    public static void _scissorBox(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        GL20.glScissor((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static void _disableDepthTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.DEPTH.mode.disable();
    }

    public static void _enableDepthTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.DEPTH.mode.enable();
    }

    public static void _depthFunc(int n) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.DEPTH.func) {
            GlStateManager.DEPTH.func = n;
            GL11.glDepthFunc((int)n);
        }
    }

    public static void _depthMask(boolean bl) {
        RenderSystem.assertOnRenderThread();
        if (bl != GlStateManager.DEPTH.mask) {
            GlStateManager.DEPTH.mask = bl;
            GL11.glDepthMask((boolean)bl);
        }
    }

    public static void _disableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.mode.disable();
    }

    public static void _enableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.mode.enable();
    }

    public static void _blendFuncSeparate(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.BLEND.srcRgb || n2 != GlStateManager.BLEND.dstRgb || n3 != GlStateManager.BLEND.srcAlpha || n4 != GlStateManager.BLEND.dstAlpha) {
            GlStateManager.BLEND.srcRgb = n;
            GlStateManager.BLEND.dstRgb = n2;
            GlStateManager.BLEND.srcAlpha = n3;
            GlStateManager.BLEND.dstAlpha = n4;
            GlStateManager.glBlendFuncSeparate(n, n2, n3, n4);
        }
    }

    public static int glGetProgrami(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgrami((int)n, (int)n2);
    }

    public static void glAttachShader(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL20.glAttachShader((int)n, (int)n2);
    }

    public static void glDeleteShader(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteShader((int)n);
    }

    public static int glCreateShader(int n) {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateShader((int)n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void glShaderSource(int n, String string) {
        RenderSystem.assertOnRenderThread();
        byte[] byArray = string.getBytes(Charsets.UTF_8);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)(byArray.length + 1));
        byteBuffer.put(byArray);
        byteBuffer.put((byte)0);
        byteBuffer.flip();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            pointerBuffer.put(byteBuffer);
            GL20C.nglShaderSource((int)n, (int)1, (long)pointerBuffer.address0(), (long)0L);
        }
        finally {
            MemoryUtil.memFree((Buffer)byteBuffer);
        }
    }

    public static void glCompileShader(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glCompileShader((int)n);
    }

    public static int glGetShaderi(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderi((int)n, (int)n2);
    }

    public static void _glUseProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glUseProgram((int)n);
    }

    public static int glCreateProgram() {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteProgram((int)n);
    }

    public static void glLinkProgram(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glLinkProgram((int)n);
    }

    public static int _glGetUniformLocation(int n, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetUniformLocation((int)n, (CharSequence)charSequence);
    }

    public static void _glUniform1(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1iv((int)n, (IntBuffer)intBuffer);
    }

    public static void _glUniform1i(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1i((int)n, (int)n2);
    }

    public static void _glUniform1(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniform2(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform2fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniform3(int n, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform3iv((int)n, (IntBuffer)intBuffer);
    }

    public static void _glUniform3(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform3fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniform4(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform4fv((int)n, (FloatBuffer)floatBuffer);
    }

    public static void _glUniformMatrix4(int n, FloatBuffer floatBuffer) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniformMatrix4fv((int)n, (boolean)false, (FloatBuffer)floatBuffer);
    }

    public static void _glBindAttribLocation(int n, int n2, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        GL20.glBindAttribLocation((int)n, (int)n2, (CharSequence)charSequence);
    }

    public static int _glGenBuffers() {
        RenderSystem.assertOnRenderThread();
        PLOT_BUFFERS.setValue((double)(++numBuffers));
        return GL15.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertOnRenderThread();
        return GL30.glGenVertexArrays();
    }

    public static void _glBindBuffer(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL15.glBindBuffer((int)n, (int)n2);
    }

    public static void _glBindVertexArray(int n) {
        RenderSystem.assertOnRenderThread();
        GL30.glBindVertexArray((int)n);
    }

    public static void _glBufferData(int n, ByteBuffer byteBuffer, int n2) {
        RenderSystem.assertOnRenderThread();
        GL15.glBufferData((int)n, (ByteBuffer)byteBuffer, (int)n2);
    }

    public static void _glBufferSubData(int n, int n2, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        GL15.glBufferSubData((int)n, (long)n2, (ByteBuffer)byteBuffer);
    }

    public static void _glBufferData(int n, long l, int n2) {
        RenderSystem.assertOnRenderThread();
        GL15.glBufferData((int)n, (long)l, (int)n2);
    }

    @Nullable
    public static ByteBuffer _glMapBufferRange(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        return GL30.glMapBufferRange((int)n, (long)n2, (long)n3, (int)n4);
    }

    public static void _glUnmapBuffer(int n) {
        RenderSystem.assertOnRenderThread();
        GL15.glUnmapBuffer((int)n);
    }

    public static void _glDeleteBuffers(int n) {
        RenderSystem.assertOnRenderThread();
        PLOT_BUFFERS.setValue((double)(--numBuffers));
        GL15.glDeleteBuffers((int)n);
    }

    public static void _glBindFramebuffer(int n, int n2) {
        if ((n == 36008 || n == 36160) && readFbo != n2) {
            GL30.glBindFramebuffer((int)36008, (int)n2);
            readFbo = n2;
        }
        if ((n == 36009 || n == 36160) && writeFbo != n2) {
            GL30.glBindFramebuffer((int)36009, (int)n2);
            writeFbo = n2;
        }
    }

    public static int getFrameBuffer(int n) {
        if (n == 36008) {
            return readFbo;
        }
        if (n == 36009) {
            return writeFbo;
        }
        return 0;
    }

    public static void _glBlitFrameBuffer(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10) {
        RenderSystem.assertOnRenderThread();
        GL30.glBlitFramebuffer((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (int)n9, (int)n10);
    }

    public static void _glDeleteFramebuffers(int n) {
        RenderSystem.assertOnRenderThread();
        GL30.glDeleteFramebuffers((int)n);
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertOnRenderThread();
        return GL30.glGenFramebuffers();
    }

    public static void _glFramebufferTexture2D(int n, int n2, int n3, int n4, int n5) {
        RenderSystem.assertOnRenderThread();
        GL30.glFramebufferTexture2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5);
    }

    public static void glActiveTexture(int n) {
        RenderSystem.assertOnRenderThread();
        GL13.glActiveTexture((int)n);
    }

    public static void glBlendFuncSeparate(int n, int n2, int n3, int n4) {
        RenderSystem.assertOnRenderThread();
        GL14.glBlendFuncSeparate((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static String glGetShaderInfoLog(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderInfoLog((int)n, (int)n2);
    }

    public static String glGetProgramInfoLog(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgramInfoLog((int)n, (int)n2);
    }

    public static void _enableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.enable.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.enable.disable();
    }

    public static void _polygonMode(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL11.glPolygonMode((int)n, (int)n2);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.fill.disable();
    }

    public static void _polygonOffset(float f, float f2) {
        RenderSystem.assertOnRenderThread();
        if (f != GlStateManager.POLY_OFFSET.factor || f2 != GlStateManager.POLY_OFFSET.units) {
            GlStateManager.POLY_OFFSET.factor = f;
            GlStateManager.POLY_OFFSET.units = f2;
            GL11.glPolygonOffset((float)f, (float)f2);
        }
    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int n) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.COLOR_LOGIC.op) {
            GlStateManager.COLOR_LOGIC.op = n;
            GL11.glLogicOp((int)n);
        }
    }

    public static void _activeTexture(int n) {
        RenderSystem.assertOnRenderThread();
        if (activeTexture != n - 33984) {
            activeTexture = n - 33984;
            GlStateManager.glActiveTexture(n);
        }
    }

    public static void _texParameter(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexParameteri((int)n, (int)n2, (int)n3);
    }

    public static int _getTexLevelParameter(int n, int n2, int n3) {
        return GL11.glGetTexLevelParameteri((int)n, (int)n2, (int)n3);
    }

    public static int _genTexture() {
        RenderSystem.assertOnRenderThread();
        PLOT_TEXTURES.setValue((double)(++numTextures));
        return GL11.glGenTextures();
    }

    public static void _deleteTexture(int n) {
        RenderSystem.assertOnRenderThread();
        GL11.glDeleteTextures((int)n);
        for (TextureState textureState : TEXTURES) {
            if (textureState.binding != n) continue;
            textureState.binding = -1;
        }
        PLOT_TEXTURES.setValue((double)(--numTextures));
    }

    public static void _bindTexture(int n) {
        RenderSystem.assertOnRenderThread();
        if (n != GlStateManager.TEXTURES[GlStateManager.activeTexture].binding) {
            GlStateManager.TEXTURES[GlStateManager.activeTexture].binding = n;
            GL11.glBindTexture((int)3553, (int)n);
        }
    }

    public static int _getActiveTexture() {
        return activeTexture + 33984;
    }

    public static void _texImage2D(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, @Nullable IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexImage2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (IntBuffer)intBuffer);
    }

    public static void _texSubImage2D(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexSubImage2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (long)l);
    }

    public static void _texSubImage2D(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, IntBuffer intBuffer) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexSubImage2D((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (IntBuffer)intBuffer);
    }

    public static void _viewport(int n, int n2, int n3, int n4) {
        GL11.glViewport((int)n, (int)n2, (int)n3, (int)n4);
    }

    public static void _colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertOnRenderThread();
        if (bl != GlStateManager.COLOR_MASK.red || bl2 != GlStateManager.COLOR_MASK.green || bl3 != GlStateManager.COLOR_MASK.blue || bl4 != GlStateManager.COLOR_MASK.alpha) {
            GlStateManager.COLOR_MASK.red = bl;
            GlStateManager.COLOR_MASK.green = bl2;
            GlStateManager.COLOR_MASK.blue = bl3;
            GlStateManager.COLOR_MASK.alpha = bl4;
            GL11.glColorMask((boolean)bl, (boolean)bl2, (boolean)bl3, (boolean)bl4);
        }
    }

    public static void _clear(int n) {
        RenderSystem.assertOnRenderThread();
        GL11.glClear((int)n);
        if (MacosUtil.IS_MACOS) {
            GlStateManager._getError();
        }
    }

    public static void _vertexAttribPointer(int n, int n2, int n3, boolean bl, int n4, long l) {
        RenderSystem.assertOnRenderThread();
        GL20.glVertexAttribPointer((int)n, (int)n2, (int)n3, (boolean)bl, (int)n4, (long)l);
    }

    public static void _vertexAttribIPointer(int n, int n2, int n3, int n4, long l) {
        RenderSystem.assertOnRenderThread();
        GL30.glVertexAttribIPointer((int)n, (int)n2, (int)n3, (int)n4, (long)l);
    }

    public static void _enableVertexAttribArray(int n) {
        RenderSystem.assertOnRenderThread();
        GL20.glEnableVertexAttribArray((int)n);
    }

    public static void _drawElements(int n, int n2, int n3, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawElements((int)n, (int)n2, (int)n3, (long)l);
    }

    public static void _drawArrays(int n, int n2, int n3) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawArrays((int)n, (int)n2, (int)n3);
    }

    public static void _pixelStore(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        GL11.glPixelStorei((int)n, (int)n2);
    }

    public static void _readPixels(int n, int n2, int n3, int n4, int n5, int n6, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (long)l);
    }

    public static int _getError() {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetError();
    }

    public static void clearGlErrors() {
        RenderSystem.assertOnRenderThread();
        while (GL11.glGetError() != 0) {
        }
    }

    public static String _getString(int n) {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetString((int)n);
    }

    public static int _getInteger(int n) {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetInteger((int)n);
    }

    public static long _glFenceSync(int n, int n2) {
        RenderSystem.assertOnRenderThread();
        return GL32.glFenceSync((int)n, (int)n2);
    }

    public static int _glClientWaitSync(long l, int n, long l2) {
        RenderSystem.assertOnRenderThread();
        return GL32.glClientWaitSync((long)l, (int)n, (long)l2);
    }

    public static void _glDeleteSync(long l) {
        RenderSystem.assertOnRenderThread();
        GL32.glDeleteSync((long)l);
    }

    static {
        TEXTURES = (TextureState[])IntStream.range(0, 12).mapToObj(n -> new TextureState()).toArray(TextureState[]::new);
        COLOR_MASK = new ColorMask();
    }

    static class ScissorState {
        public final BooleanState mode = new BooleanState(3089);

        ScissorState() {
        }
    }

    static class BooleanState {
        private final int state;
        private boolean enabled;

        public BooleanState(int n) {
            this.state = n;
        }

        public void disable() {
            this.setEnabled(false);
        }

        public void enable() {
            this.setEnabled(true);
        }

        public void setEnabled(boolean bl) {
            RenderSystem.assertOnRenderThread();
            if (bl != this.enabled) {
                this.enabled = bl;
                if (bl) {
                    GL11.glEnable((int)this.state);
                } else {
                    GL11.glDisable((int)this.state);
                }
            }
        }
    }

    static class DepthState {
        public final BooleanState mode = new BooleanState(2929);
        public boolean mask = true;
        public int func = 513;

        DepthState() {
        }
    }

    static class BlendState {
        public final BooleanState mode = new BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int srcAlpha = 1;
        public int dstAlpha = 0;

        BlendState() {
        }
    }

    static class CullState {
        public final BooleanState enable = new BooleanState(2884);

        CullState() {
        }
    }

    static class PolygonOffsetState {
        public final BooleanState fill = new BooleanState(32823);
        public float factor;
        public float units;

        PolygonOffsetState() {
        }
    }

    static class ColorLogicState {
        public final BooleanState enable = new BooleanState(3058);
        public int op = 5379;

        ColorLogicState() {
        }
    }

    static class TextureState {
        public int binding;

        TextureState() {
        }
    }

    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;

        ColorMask() {
        }
    }
}

