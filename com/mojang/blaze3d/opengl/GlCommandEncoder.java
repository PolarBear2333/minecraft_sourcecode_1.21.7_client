/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL11C
 *  org.lwjgl.opengl.GL31
 *  org.lwjgl.opengl.GL32
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlFence;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.ARGB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.slf4j.Logger;

public class GlCommandEncoder
implements CommandEncoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GlDevice device;
    private final int readFbo;
    private final int drawFbo;
    @Nullable
    private RenderPipeline lastPipeline;
    private boolean inRenderPass;
    @Nullable
    private GlProgram lastProgram;

    protected GlCommandEncoder(GlDevice glDevice) {
        this.device = glDevice;
        this.readFbo = glDevice.directStateAccess().createFrameBufferObject();
        this.drawFbo = glDevice.directStateAccess().createFrameBufferObject();
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> supplier, GpuTextureView gpuTextureView, OptionalInt optionalInt) {
        return this.createRenderPass(supplier, gpuTextureView, optionalInt, null, OptionalDouble.empty());
    }

    @Override
    public RenderPass createRenderPass(Supplier<String> supplier, GpuTextureView gpuTextureView, OptionalInt optionalInt, @Nullable GpuTextureView gpuTextureView2, OptionalDouble optionalDouble) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        if (optionalDouble.isPresent() && gpuTextureView2 == null) {
            LOGGER.warn("Depth clear value was provided but no depth texture is being used");
        }
        if (gpuTextureView.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        }
        if ((gpuTextureView.texture().usage() & 8) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_RENDER_ATTACHMENT");
        }
        if (gpuTextureView.texture().getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported as an attachment");
        }
        if (gpuTextureView2 != null) {
            if (gpuTextureView2.isClosed()) {
                throw new IllegalStateException("Depth texture is closed");
            }
            if ((gpuTextureView2.texture().usage() & 8) == 0) {
                throw new IllegalStateException("Depth texture must have USAGE_RENDER_ATTACHMENT");
            }
            if (gpuTextureView2.texture().getDepthOrLayers() > 1) {
                throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported as an attachment");
            }
        }
        this.inRenderPass = true;
        this.device.debugLabels().pushDebugGroup(supplier);
        int n = ((GlTexture)gpuTextureView.texture()).getFbo(this.device.directStateAccess(), gpuTextureView2 == null ? null : gpuTextureView2.texture());
        GlStateManager._glBindFramebuffer(36160, n);
        int n2 = 0;
        if (optionalInt.isPresent()) {
            int n3 = optionalInt.getAsInt();
            GL11.glClearColor((float)ARGB.redFloat(n3), (float)ARGB.greenFloat(n3), (float)ARGB.blueFloat(n3), (float)ARGB.alphaFloat(n3));
            n2 |= 0x4000;
        }
        if (gpuTextureView2 != null && optionalDouble.isPresent()) {
            GL11.glClearDepth((double)optionalDouble.getAsDouble());
            n2 |= 0x100;
        }
        if (n2 != 0) {
            GlStateManager._disableScissorTest();
            GlStateManager._depthMask(true);
            GlStateManager._colorMask(true, true, true, true);
            GlStateManager._clear(n2);
        }
        GlStateManager._viewport(0, 0, gpuTextureView.getWidth(0), gpuTextureView.getHeight(0));
        this.lastPipeline = null;
        return new GlRenderPass(this, gpuTextureView2 != null);
    }

    @Override
    public void clearColorTexture(GpuTexture gpuTexture, int n) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyColorTexture(gpuTexture);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, ((GlTexture)gpuTexture).id, 0, 0, 36160);
        GL11.glClearColor((float)ARGB.redFloat(n), (float)ARGB.greenFloat(n), (float)ARGB.blueFloat(n), (float)ARGB.alphaFloat(n));
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clear(16384);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture gpuTexture, int n, GpuTexture gpuTexture2, double d) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyColorTexture(gpuTexture);
        this.verifyDepthTexture(gpuTexture2);
        int n2 = ((GlTexture)gpuTexture).getFbo(this.device.directStateAccess(), gpuTexture2);
        GlStateManager._glBindFramebuffer(36160, n2);
        GlStateManager._disableScissorTest();
        GL11.glClearDepth((double)d);
        GL11.glClearColor((float)ARGB.redFloat(n), (float)ARGB.greenFloat(n), (float)ARGB.blueFloat(n), (float)ARGB.alphaFloat(n));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clear(16640);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture gpuTexture, int n, GpuTexture gpuTexture2, double d, int n2, int n3, int n4, int n5) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyColorTexture(gpuTexture);
        this.verifyDepthTexture(gpuTexture2);
        this.verifyRegion(gpuTexture, n2, n3, n4, n5);
        int n6 = ((GlTexture)gpuTexture).getFbo(this.device.directStateAccess(), gpuTexture2);
        GlStateManager._glBindFramebuffer(36160, n6);
        GlStateManager._scissorBox(n2, n3, n4, n5);
        GlStateManager._enableScissorTest();
        GL11.glClearDepth((double)d);
        GL11.glClearColor((float)ARGB.redFloat(n), (float)ARGB.greenFloat(n), (float)ARGB.blueFloat(n), (float)ARGB.alphaFloat(n));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clear(16640);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private void verifyRegion(GpuTexture gpuTexture, int n, int n2, int n3, int n4) {
        if (n < 0 || n >= gpuTexture.getWidth(0)) {
            throw new IllegalArgumentException("regionX should not be outside of the texture");
        }
        if (n2 < 0 || n2 >= gpuTexture.getHeight(0)) {
            throw new IllegalArgumentException("regionY should not be outside of the texture");
        }
        if (n3 <= 0) {
            throw new IllegalArgumentException("regionWidth should be greater than 0");
        }
        if (n + n3 > gpuTexture.getWidth(0)) {
            throw new IllegalArgumentException("regionWidth + regionX should be less than the texture width");
        }
        if (n4 <= 0) {
            throw new IllegalArgumentException("regionHeight should be greater than 0");
        }
        if (n2 + n4 > gpuTexture.getHeight(0)) {
            throw new IllegalArgumentException("regionWidth + regionX should be less than the texture height");
        }
    }

    @Override
    public void clearDepthTexture(GpuTexture gpuTexture, double d) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyDepthTexture(gpuTexture);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, 0, ((GlTexture)gpuTexture).id, 0, 36160);
        GL11.glDrawBuffer((int)0);
        GL11.glClearDepth((double)d);
        GlStateManager._depthMask(true);
        GlStateManager._disableScissorTest();
        GlStateManager._clear(256);
        GL11.glDrawBuffer((int)36064);
        GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private void verifyColorTexture(GpuTexture gpuTexture) {
        if (!gpuTexture.getFormat().hasColorAspect()) {
            throw new IllegalStateException("Trying to clear a non-color texture as color");
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        }
        if ((gpuTexture.usage() & 8) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_RENDER_ATTACHMENT");
        }
        if (gpuTexture.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Clearing a texture with multiple layers or depths is not yet supported");
        }
    }

    private void verifyDepthTexture(GpuTexture gpuTexture) {
        if (!gpuTexture.getFormat().hasDepthAspect()) {
            throw new IllegalStateException("Trying to clear a non-depth texture as depth");
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Depth texture is closed");
        }
        if ((gpuTexture.usage() & 8) == 0) {
            throw new IllegalStateException("Depth texture must have USAGE_RENDER_ATTACHMENT");
        }
        if (gpuTexture.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Clearing a texture with multiple layers or depths is not yet supported");
        }
    }

    @Override
    public void writeToBuffer(GpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        GlBuffer glBuffer = (GlBuffer)gpuBufferSlice.buffer();
        if (glBuffer.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        if ((glBuffer.usage() & 8) == 0) {
            throw new IllegalStateException("Buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        int n = byteBuffer.remaining();
        if (n > gpuBufferSlice.length()) {
            throw new IllegalArgumentException("Cannot write more data than the slice allows (attempting to write " + n + " bytes into a slice of length " + gpuBufferSlice.length() + ")");
        }
        if (gpuBufferSlice.length() + gpuBufferSlice.offset() > glBuffer.size) {
            throw new IllegalArgumentException("Cannot write more data than this buffer can hold (attempting to write " + n + " bytes at offset " + gpuBufferSlice.offset() + " to " + glBuffer.size + " size buffer)");
        }
        this.device.directStateAccess().bufferSubData(glBuffer.handle, gpuBufferSlice.offset(), byteBuffer);
    }

    @Override
    public GpuBuffer.MappedView mapBuffer(GpuBuffer gpuBuffer, boolean bl, boolean bl2) {
        return this.mapBuffer(gpuBuffer.slice(), bl, bl2);
    }

    @Override
    public GpuBuffer.MappedView mapBuffer(GpuBufferSlice gpuBufferSlice, boolean bl, boolean bl2) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        GlBuffer glBuffer = (GlBuffer)gpuBufferSlice.buffer();
        if (glBuffer.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        if (!bl && !bl2) {
            throw new IllegalArgumentException("At least read or write must be true");
        }
        if (bl && (glBuffer.usage() & 1) == 0) {
            throw new IllegalStateException("Buffer is not readable");
        }
        if (bl2 && (glBuffer.usage() & 2) == 0) {
            throw new IllegalStateException("Buffer is not writable");
        }
        if (gpuBufferSlice.offset() + gpuBufferSlice.length() > glBuffer.size) {
            throw new IllegalArgumentException("Cannot map more data than this buffer can hold (attempting to map " + gpuBufferSlice.length() + " bytes at offset " + gpuBufferSlice.offset() + " from " + glBuffer.size + " size buffer)");
        }
        int n = 0;
        if (bl) {
            n |= 1;
        }
        if (bl2) {
            n |= 0x22;
        }
        return this.device.getBufferStorage().mapBuffer(this.device.directStateAccess(), glBuffer, gpuBufferSlice.offset(), gpuBufferSlice.length(), n);
    }

    @Override
    public void copyToBuffer(GpuBufferSlice gpuBufferSlice, GpuBufferSlice gpuBufferSlice2) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        GlBuffer glBuffer = (GlBuffer)gpuBufferSlice.buffer();
        if (glBuffer.closed) {
            throw new IllegalStateException("Source buffer already closed");
        }
        if ((glBuffer.usage() & 8) == 0) {
            throw new IllegalStateException("Source buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        GlBuffer glBuffer2 = (GlBuffer)gpuBufferSlice2.buffer();
        if (glBuffer2.closed) {
            throw new IllegalStateException("Target buffer already closed");
        }
        if ((glBuffer2.usage() & 8) == 0) {
            throw new IllegalStateException("Target buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (gpuBufferSlice.length() != gpuBufferSlice2.length()) {
            throw new IllegalArgumentException("Cannot copy from slice of size " + gpuBufferSlice.length() + " to slice of size " + gpuBufferSlice2.length() + ", they must be equal");
        }
        if (gpuBufferSlice.offset() + gpuBufferSlice.length() > glBuffer.size) {
            throw new IllegalArgumentException("Cannot copy more data than the source buffer holds (attempting to copy " + gpuBufferSlice.length() + " bytes at offset " + gpuBufferSlice.offset() + " from " + glBuffer.size + " size buffer)");
        }
        if (gpuBufferSlice2.offset() + gpuBufferSlice2.length() > glBuffer2.size) {
            throw new IllegalArgumentException("Cannot copy more data than the target buffer can hold (attempting to copy " + gpuBufferSlice2.length() + " bytes at offset " + gpuBufferSlice2.offset() + " to " + glBuffer2.size + " size buffer)");
        }
        this.device.directStateAccess().copyBufferSubData(glBuffer.handle, glBuffer2.handle, gpuBufferSlice.offset(), gpuBufferSlice2.offset(), gpuBufferSlice.length());
    }

    @Override
    public void writeToTexture(GpuTexture gpuTexture, NativeImage nativeImage) {
        int n = gpuTexture.getWidth(0);
        int n2 = gpuTexture.getHeight(0);
        if (nativeImage.getWidth() != n || nativeImage.getHeight() != n2) {
            throw new IllegalArgumentException("Cannot replace texture of size " + n + "x" + n2 + " with image of size " + nativeImage.getWidth() + "x" + nativeImage.getHeight());
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((gpuTexture.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
        }
        this.writeToTexture(gpuTexture, nativeImage, 0, 0, 0, 0, n, n2, 0, 0);
    }

    @Override
    public void writeToTexture(GpuTexture gpuTexture, NativeImage nativeImage, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        int n9;
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (n < 0 || n >= gpuTexture.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel " + n + ", must be >= 0 and < " + gpuTexture.getMipLevels());
        }
        if (n7 + n5 > nativeImage.getWidth() || n8 + n6 > nativeImage.getHeight()) {
            throw new IllegalArgumentException("Copy source (" + nativeImage.getWidth() + "x" + nativeImage.getHeight() + ") is not large enough to read a rectangle of " + n5 + "x" + n6 + " from " + n7 + "x" + n8);
        }
        if (n3 + n5 > gpuTexture.getWidth(n) || n4 + n6 > gpuTexture.getHeight(n)) {
            throw new IllegalArgumentException("Dest texture (" + n5 + "x" + n6 + ") is not large enough to write a rectangle of " + n5 + "x" + n6 + " at " + n3 + "x" + n4 + " (at mip level " + n + ")");
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((gpuTexture.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
        }
        if (n2 >= gpuTexture.getDepthOrLayers()) {
            throw new UnsupportedOperationException("Depth or layer is out of range, must be >= 0 and < " + gpuTexture.getDepthOrLayers());
        }
        if ((gpuTexture.usage() & 0x10) != 0) {
            n9 = GlConst.CUBEMAP_TARGETS[n2 % 6];
            GL11.glBindTexture((int)34067, (int)((GlTexture)gpuTexture).id);
        } else {
            n9 = 3553;
            GlStateManager._bindTexture(((GlTexture)gpuTexture).id);
        }
        GlStateManager._pixelStore(3314, nativeImage.getWidth());
        GlStateManager._pixelStore(3316, n7);
        GlStateManager._pixelStore(3315, n8);
        GlStateManager._pixelStore(3317, nativeImage.format().components());
        GlStateManager._texSubImage2D(n9, n, n3, n4, n5, n6, GlConst.toGl(nativeImage.format()), 5121, nativeImage.getPointer());
    }

    @Override
    public void writeToTexture(GpuTexture gpuTexture, IntBuffer intBuffer, NativeImage.Format format, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7;
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (n < 0 || n >= gpuTexture.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel, must be >= 0 and < " + gpuTexture.getMipLevels());
        }
        if (n5 * n6 > intBuffer.remaining()) {
            throw new IllegalArgumentException("Copy would overrun the source buffer (remaining length of " + intBuffer.remaining() + ", but copy is " + n5 + "x" + n6 + ")");
        }
        if (n3 + n5 > gpuTexture.getWidth(n) || n4 + n6 > gpuTexture.getHeight(n)) {
            throw new IllegalArgumentException("Dest texture (" + gpuTexture.getWidth(n) + "x" + gpuTexture.getHeight(n) + ") is not large enough to write a rectangle of " + n5 + "x" + n6 + " at " + n3 + "x" + n4);
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((gpuTexture.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
        }
        if (n2 >= gpuTexture.getDepthOrLayers()) {
            throw new UnsupportedOperationException("Depth or layer is out of range, must be >= 0 and < " + gpuTexture.getDepthOrLayers());
        }
        if ((gpuTexture.usage() & 0x10) != 0) {
            n7 = GlConst.CUBEMAP_TARGETS[n2 % 6];
            GL11.glBindTexture((int)34067, (int)((GlTexture)gpuTexture).id);
        } else {
            n7 = 3553;
            GlStateManager._bindTexture(((GlTexture)gpuTexture).id);
        }
        GlStateManager._pixelStore(3314, n5);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        GlStateManager._pixelStore(3317, format.components());
        GlStateManager._texSubImage2D(n7, n, n3, n4, n5, n6, GlConst.toGl(format), 5121, intBuffer);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture gpuTexture, GpuBuffer gpuBuffer, int n, Runnable runnable, int n2) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        this.copyTextureToBuffer(gpuTexture, gpuBuffer, n, runnable, n2, 0, 0, gpuTexture.getWidth(n2), gpuTexture.getHeight(n2));
    }

    @Override
    public void copyTextureToBuffer(GpuTexture gpuTexture, GpuBuffer gpuBuffer, int n, Runnable runnable, int n2, int n3, int n4, int n5, int n6) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (n2 < 0 || n2 >= gpuTexture.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel " + n2 + ", must be >= 0 and < " + gpuTexture.getMipLevels());
        }
        if (gpuTexture.getWidth(n2) * gpuTexture.getHeight(n2) * gpuTexture.getFormat().pixelSize() + n > gpuBuffer.size()) {
            throw new IllegalArgumentException("Buffer of size " + gpuBuffer.size() + " is not large enough to hold " + n5 + "x" + n6 + " pixels (" + gpuTexture.getFormat().pixelSize() + " bytes each) starting from offset " + n);
        }
        if ((gpuTexture.usage() & 2) == 0) {
            throw new IllegalArgumentException("Texture needs USAGE_COPY_SRC to be a source for a copy");
        }
        if ((gpuBuffer.usage() & 8) == 0) {
            throw new IllegalArgumentException("Buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (n3 + n5 > gpuTexture.getWidth(n2) || n4 + n6 > gpuTexture.getHeight(n2)) {
            throw new IllegalArgumentException("Copy source texture (" + gpuTexture.getWidth(n2) + "x" + gpuTexture.getHeight(n2) + ") is not large enough to read a rectangle of " + n5 + "x" + n6 + " from " + n3 + "," + n4);
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Source texture is closed");
        }
        if (gpuBuffer.isClosed()) {
            throw new IllegalStateException("Destination buffer is closed");
        }
        if (gpuTexture.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for copying");
        }
        GlStateManager.clearGlErrors();
        this.device.directStateAccess().bindFrameBufferTextures(this.readFbo, ((GlTexture)gpuTexture).glId(), 0, n2, 36008);
        GlStateManager._glBindBuffer(35051, ((GlBuffer)gpuBuffer).handle);
        GlStateManager._pixelStore(3330, n5);
        GlStateManager._readPixels(n3, n4, n5, n6, GlConst.toGlExternalId(gpuTexture.getFormat()), GlConst.toGlType(gpuTexture.getFormat()), n);
        RenderSystem.queueFencedTask(runnable);
        GlStateManager._glFramebufferTexture2D(36008, 36064, 3553, 0, n2);
        GlStateManager._glBindFramebuffer(36008, 0);
        GlStateManager._glBindBuffer(35051, 0);
        int n7 = GlStateManager._getError();
        if (n7 != 0) {
            throw new IllegalStateException("Couldn't perform copyTobuffer for texture " + gpuTexture.getLabel() + ": GL error " + n7);
        }
    }

    @Override
    public void copyTextureToTexture(GpuTexture gpuTexture, GpuTexture gpuTexture2, int n, int n2, int n3, int n4, int n5, int n6, int n7) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (n < 0 || n >= gpuTexture.getMipLevels() || n >= gpuTexture2.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel " + n + ", must be >= 0 and < " + gpuTexture.getMipLevels() + " and < " + gpuTexture2.getMipLevels());
        }
        if (n2 + n6 > gpuTexture2.getWidth(n) || n3 + n7 > gpuTexture2.getHeight(n)) {
            throw new IllegalArgumentException("Dest texture (" + gpuTexture2.getWidth(n) + "x" + gpuTexture2.getHeight(n) + ") is not large enough to write a rectangle of " + n6 + "x" + n7 + " at " + n2 + "x" + n3);
        }
        if (n4 + n6 > gpuTexture.getWidth(n) || n5 + n7 > gpuTexture.getHeight(n)) {
            throw new IllegalArgumentException("Source texture (" + gpuTexture.getWidth(n) + "x" + gpuTexture.getHeight(n) + ") is not large enough to read a rectangle of " + n6 + "x" + n7 + " at " + n4 + "x" + n5);
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Source texture is closed");
        }
        if (gpuTexture2.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((gpuTexture.usage() & 2) == 0) {
            throw new IllegalArgumentException("Texture needs USAGE_COPY_SRC to be a source for a copy");
        }
        if ((gpuTexture2.usage() & 1) == 0) {
            throw new IllegalArgumentException("Texture needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (gpuTexture.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for copying");
        }
        if (gpuTexture2.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for copying");
        }
        GlStateManager.clearGlErrors();
        GlStateManager._disableScissorTest();
        boolean bl = gpuTexture.getFormat().hasDepthAspect();
        int n8 = ((GlTexture)gpuTexture).glId();
        int n9 = ((GlTexture)gpuTexture2).glId();
        this.device.directStateAccess().bindFrameBufferTextures(this.readFbo, bl ? 0 : n8, bl ? n8 : 0, 0, 0);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, bl ? 0 : n9, bl ? n9 : 0, 0, 0);
        this.device.directStateAccess().blitFrameBuffers(this.readFbo, this.drawFbo, n4, n5, n6, n7, n2, n3, n6, n7, bl ? 256 : 16384, 9728);
        int n10 = GlStateManager._getError();
        if (n10 != 0) {
            throw new IllegalStateException("Couldn't perform copyToTexture for texture " + gpuTexture.getLabel() + " to " + gpuTexture2.getLabel() + ": GL error " + n10);
        }
    }

    @Override
    public void presentTexture(GpuTextureView gpuTextureView) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (!gpuTextureView.texture().getFormat().hasColorAspect()) {
            throw new IllegalStateException("Cannot present a non-color texture!");
        }
        if ((gpuTextureView.texture().usage() & 8) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_RENDER_ATTACHMENT to presented to the screen");
        }
        if (gpuTextureView.texture().getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for presentation");
        }
        GlStateManager._disableScissorTest();
        GlStateManager._viewport(0, 0, gpuTextureView.getWidth(0), gpuTextureView.getHeight(0));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, ((GlTexture)gpuTextureView.texture()).glId(), 0, 0, 0);
        this.device.directStateAccess().blitFrameBuffers(this.drawFbo, 0, 0, 0, gpuTextureView.getWidth(0), gpuTextureView.getHeight(0), 0, 0, gpuTextureView.getWidth(0), gpuTextureView.getHeight(0), 16384, 9728);
    }

    @Override
    public GpuFence createFence() {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        return new GlFence();
    }

    protected <T> void executeDrawMultiple(GlRenderPass glRenderPass, Collection<RenderPass.Draw<T>> collection, @Nullable GpuBuffer gpuBuffer, @Nullable VertexFormat.IndexType indexType, Collection<String> collection2, T t) {
        if (!this.trySetup(glRenderPass, collection2)) {
            return;
        }
        if (indexType == null) {
            indexType = VertexFormat.IndexType.SHORT;
        }
        for (RenderPass.Draw<T> draw : collection) {
            BiConsumer<T, RenderPass.UniformUploader> biConsumer;
            VertexFormat.IndexType indexType2 = draw.indexType() == null ? indexType : draw.indexType();
            glRenderPass.setIndexBuffer(draw.indexBuffer() == null ? gpuBuffer : draw.indexBuffer(), indexType2);
            glRenderPass.setVertexBuffer(draw.slot(), draw.vertexBuffer());
            if (GlRenderPass.VALIDATION) {
                if (glRenderPass.indexBuffer == null) {
                    throw new IllegalStateException("Missing index buffer");
                }
                if (glRenderPass.indexBuffer.isClosed()) {
                    throw new IllegalStateException("Index buffer has been closed!");
                }
                if (glRenderPass.vertexBuffers[0] == null) {
                    throw new IllegalStateException("Missing vertex buffer at slot 0");
                }
                if (glRenderPass.vertexBuffers[0].isClosed()) {
                    throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
                }
            }
            if ((biConsumer = draw.uniformUploaderConsumer()) != null) {
                biConsumer.accept(t, (string, gpuBufferSlice) -> {
                    Uniform uniform = glRenderPass.pipeline.program().getUniform(string);
                    if (uniform instanceof Uniform.Ubo) {
                        int n;
                        Uniform.Ubo ubo = (Uniform.Ubo)uniform;
                        try {
                            int n2;
                            n = n2 = ubo.blockBinding();
                        }
                        catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }
                        GL32.glBindBufferRange((int)35345, (int)n, (int)((GlBuffer)gpuBufferSlice.buffer()).handle, (long)gpuBufferSlice.offset(), (long)gpuBufferSlice.length());
                    }
                });
            }
            this.drawFromBuffers(glRenderPass, 0, draw.firstIndex(), draw.indexCount(), indexType2, glRenderPass.pipeline, 1);
        }
    }

    protected void executeDraw(GlRenderPass glRenderPass, int n, int n2, int n3, @Nullable VertexFormat.IndexType indexType, int n4) {
        if (!this.trySetup(glRenderPass, Collections.emptyList())) {
            return;
        }
        if (GlRenderPass.VALIDATION) {
            if (indexType != null) {
                if (glRenderPass.indexBuffer == null) {
                    throw new IllegalStateException("Missing index buffer");
                }
                if (glRenderPass.indexBuffer.isClosed()) {
                    throw new IllegalStateException("Index buffer has been closed!");
                }
                if ((glRenderPass.indexBuffer.usage() & 0x40) == 0) {
                    throw new IllegalStateException("Index buffer must have GpuBuffer.USAGE_INDEX!");
                }
            }
            if (glRenderPass.vertexBuffers[0] == null) {
                throw new IllegalStateException("Missing vertex buffer at slot 0");
            }
            if (glRenderPass.vertexBuffers[0].isClosed()) {
                throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
            }
            if ((glRenderPass.vertexBuffers[0].usage() & 0x20) == 0) {
                throw new IllegalStateException("Vertex buffer must have GpuBuffer.USAGE_VERTEX!");
            }
        }
        this.drawFromBuffers(glRenderPass, n, n2, n3, indexType, glRenderPass.pipeline, n4);
    }

    private void drawFromBuffers(GlRenderPass glRenderPass, int n, int n2, int n3, @Nullable VertexFormat.IndexType indexType, GlRenderPipeline glRenderPipeline, int n4) {
        this.device.vertexArrayCache().bindVertexArray(glRenderPipeline.info().getVertexFormat(), (GlBuffer)glRenderPass.vertexBuffers[0]);
        if (indexType != null) {
            GlStateManager._glBindBuffer(34963, ((GlBuffer)glRenderPass.indexBuffer).handle);
            if (n4 > 1) {
                if (n > 0) {
                    GL32.glDrawElementsInstancedBaseVertex((int)GlConst.toGl(glRenderPipeline.info().getVertexFormatMode()), (int)n3, (int)GlConst.toGl(indexType), (long)((long)n2 * (long)indexType.bytes), (int)n4, (int)n);
                } else {
                    GL31.glDrawElementsInstanced((int)GlConst.toGl(glRenderPipeline.info().getVertexFormatMode()), (int)n3, (int)GlConst.toGl(indexType), (long)((long)n2 * (long)indexType.bytes), (int)n4);
                }
            } else if (n > 0) {
                GL32.glDrawElementsBaseVertex((int)GlConst.toGl(glRenderPipeline.info().getVertexFormatMode()), (int)n3, (int)GlConst.toGl(indexType), (long)((long)n2 * (long)indexType.bytes), (int)n);
            } else {
                GlStateManager._drawElements(GlConst.toGl(glRenderPipeline.info().getVertexFormatMode()), n3, GlConst.toGl(indexType), (long)n2 * (long)indexType.bytes);
            }
        } else if (n4 > 1) {
            GL31.glDrawArraysInstanced((int)GlConst.toGl(glRenderPipeline.info().getVertexFormatMode()), (int)n, (int)n3, (int)n4);
        } else {
            GlStateManager._drawArrays(GlConst.toGl(glRenderPipeline.info().getVertexFormatMode()), n, n3);
        }
    }

    /*
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    private boolean trySetup(GlRenderPass var1_1, Collection<String> var2_2) {
        if (GlRenderPass.VALIDATION) {
            if (var1_1.pipeline == null) {
                throw new IllegalStateException("Can't draw without a render pipeline");
            }
            if (var1_1.pipeline.program() == GlProgram.INVALID_PROGRAM) {
                throw new IllegalStateException("Pipeline contains invalid shader program");
            }
            for (Map.Entry<String, Uniform> var4_4 : var1_1.pipeline.info().getUniforms()) {
                var5_5 = var1_1.uniforms.get(var4_4.name());
                if (var2_2.contains(var4_4.name())) continue;
                if (var5_5 == null) {
                    throw new IllegalStateException("Missing uniform " + var4_4.name() + " (should be " + String.valueOf((Object)var4_4.type()) + ")");
                }
                if (var4_4.type() == UniformType.UNIFORM_BUFFER) {
                    if (var5_5.buffer().isClosed()) {
                        throw new IllegalStateException("Uniform buffer " + var4_4.name() + " is already closed");
                    }
                    if ((var5_5.buffer().usage() & 128) == 0) {
                        throw new IllegalStateException("Uniform buffer " + var4_4.name() + " must have GpuBuffer.USAGE_UNIFORM");
                    }
                }
                if (var4_4.type() != UniformType.TEXEL_BUFFER) continue;
                if (var5_5.offset() != 0 || var5_5.length() != var5_5.buffer().size()) {
                    throw new IllegalStateException("Uniform texel buffers do not support a slice of a buffer, must be entire buffer");
                }
                if (var4_4.textureFormat() != null) continue;
                throw new IllegalStateException("Invalid uniform texel buffer " + var4_4.name() + " (missing a texture format)");
            }
            for (Map.Entry<String, Uniform> var4_4 : var1_1.pipeline.program().getUniforms().entrySet()) {
                if (!(var4_4.getValue() instanceof Uniform.Sampler)) continue;
                var5_5 = var4_4.getKey();
                var6_7 = (GlTextureView)var1_1.samplers.get(var5_5);
                if (var6_7 == null) {
                    throw new IllegalStateException("Missing sampler " + (String)var5_5);
                }
                if (var6_7.isClosed()) {
                    throw new IllegalStateException("Sampler " + (String)var5_5 + " (" + var6_7.texture().getLabel() + ") has been closed!");
                }
                if ((var6_7.texture().usage() & 4) != 0) continue;
                throw new IllegalStateException("Sampler " + (String)var5_5 + " (" + var6_7.texture().getLabel() + ") must have USAGE_TEXTURE_BINDING!");
            }
            if (var1_1.pipeline.info().wantsDepthTexture() && !var1_1.hasDepthTexture()) {
                GlCommandEncoder.LOGGER.warn("Render pipeline {} wants a depth texture but none was provided - this is probably a bug", (Object)var1_1.pipeline.info().getLocation());
            }
        } else if (var1_1.pipeline == null || var1_1.pipeline.program() == GlProgram.INVALID_PROGRAM) {
            return false;
        }
        var3_3 = var1_1.pipeline.info();
        var4_4 = var1_1.pipeline.program();
        this.applyPipelineState((RenderPipeline)var3_3);
        v0 = var5_6 = this.lastProgram != var4_4;
        if (var5_6) {
            GlStateManager._glUseProgram(var4_4.getProgramId());
            this.lastProgram = var4_4;
        }
        block15: for (Map.Entry<String, Uniform> var7_9 : var4_4.getUniforms().entrySet()) {
            var8_10 = var7_9.getKey();
            var9_11 = var1_1.dirtyUniforms.contains(var8_10);
            Objects.requireNonNull(var7_9.getValue());
            var11_13 = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Uniform.Ubo.class, Uniform.Utb.class, Uniform.Sampler.class}, (Object)var10_12, var11_13)) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    var12_14 = (Uniform.Ubo)var10_12;
                    var13_15 = var14_16 = var12_14.blockBinding();
                    if (!var9_11) continue block15;
                    var14_17 = var1_1.uniforms.get(var8_10);
                    GL32.glBindBufferRange((int)35345, (int)var13_15, (int)((GlBuffer)var14_17.buffer()).handle, (long)var14_17.offset(), (long)var14_17.length());
                    continue block15;
                }
                case 1: {
                    var14_18 = (Uniform.Utb)var10_12;
                    var15_19 = var19_23 = var14_18.location();
                    var16_20 = var19_23 = var14_18.samplerIndex();
                    var17_21 = var19_24 = var14_18.format();
                    var18_22 = var19_25 = var14_18.texture();
                    if (!var5_6 && !var9_11) ** GOTO lbl72
                    GlStateManager._glUniform1i(var15_19, var16_20);
lbl72:
                    // 2 sources

                    GlStateManager._activeTexture(33984 + var16_20);
                    GL11C.glBindTexture((int)35882, (int)var18_22);
                    if (!var9_11) continue block15;
                    var19_26 = var1_1.uniforms.get(var8_10);
                    GL31.glTexBuffer((int)35882, (int)GlConst.toGlInternalId(var17_21), (int)((GlBuffer)var19_26.buffer()).handle);
                    continue block15;
                }
                case 2: 
            }
            var19_27 = (Uniform.Sampler)var10_12;
            var20_28 = var22_31 = var19_27.location();
            var21_29 = var22_31 = var19_27.samplerIndex();
            var22_30 = (GlTextureView)var1_1.samplers.get(var8_10);
            if (var22_30 == null) continue;
            if (var5_6 || var9_11) {
                GlStateManager._glUniform1i(var20_28, var21_29);
            }
            GlStateManager._activeTexture(33984 + var21_29);
            var23_32 = var22_30.texture();
            if ((var23_32.usage() & 16) != 0) {
                var24_33 = 34067;
                GL11.glBindTexture((int)34067, (int)var23_32.id);
            } else {
                var24_33 = 3553;
                GlStateManager._bindTexture(var23_32.id);
            }
            GlStateManager._texParameter(var24_33, 33084, var22_30.baseMipLevel());
            GlStateManager._texParameter(var24_33, 33085, var22_30.baseMipLevel() + var22_30.mipLevels() - 1);
            var23_32.flushModeChanges(var24_33);
        }
        var1_1.dirtyUniforms.clear();
        if (var1_1.isScissorEnabled()) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(var1_1.getScissorX(), var1_1.getScissorY(), var1_1.getScissorWidth(), var1_1.getScissorHeight());
        } else {
            GlStateManager._disableScissorTest();
        }
        return true;
        catch (Throwable var6_8) {
            throw new MatchException(var6_8.toString(), var6_8);
        }
    }

    private void applyPipelineState(RenderPipeline renderPipeline) {
        if (this.lastPipeline == renderPipeline) {
            return;
        }
        this.lastPipeline = renderPipeline;
        if (renderPipeline.getDepthTestFunction() != DepthTestFunction.NO_DEPTH_TEST) {
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(GlConst.toGl(renderPipeline.getDepthTestFunction()));
        } else {
            GlStateManager._disableDepthTest();
        }
        if (renderPipeline.isCull()) {
            GlStateManager._enableCull();
        } else {
            GlStateManager._disableCull();
        }
        if (renderPipeline.getBlendFunction().isPresent()) {
            GlStateManager._enableBlend();
            BlendFunction blendFunction = renderPipeline.getBlendFunction().get();
            GlStateManager._blendFuncSeparate(GlConst.toGl(blendFunction.sourceColor()), GlConst.toGl(blendFunction.destColor()), GlConst.toGl(blendFunction.sourceAlpha()), GlConst.toGl(blendFunction.destAlpha()));
        } else {
            GlStateManager._disableBlend();
        }
        GlStateManager._polygonMode(1032, GlConst.toGl(renderPipeline.getPolygonMode()));
        GlStateManager._depthMask(renderPipeline.isWriteDepth());
        GlStateManager._colorMask(renderPipeline.isWriteColor(), renderPipeline.isWriteColor(), renderPipeline.isWriteColor(), renderPipeline.isWriteAlpha());
        if (renderPipeline.getDepthBiasConstant() != 0.0f || renderPipeline.getDepthBiasScaleFactor() != 0.0f) {
            GlStateManager._polygonOffset(renderPipeline.getDepthBiasScaleFactor(), renderPipeline.getDepthBiasConstant());
            GlStateManager._enablePolygonOffset();
        } else {
            GlStateManager._disablePolygonOffset();
        }
        switch (renderPipeline.getColorLogic()) {
            case NONE: {
                GlStateManager._disableColorLogicOp();
                break;
            }
            case OR_REVERSE: {
                GlStateManager._enableColorLogicOp();
                GlStateManager._logicOp(5387);
            }
        }
    }

    public void finishRenderPass() {
        this.inRenderPass = false;
        GlStateManager._glBindFramebuffer(36160, 0);
        this.device.debugLabels().popDebugGroup();
    }

    protected GlDevice getDevice() {
        return this.device;
    }
}

