/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 */
package com.mojang.blaze3d;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.client.renderer.RenderPipelines;

public class TracyFrameCapture
implements AutoCloseable {
    private static final int MAX_WIDTH = 320;
    private static final int MAX_HEIGHT = 180;
    private static final int BYTES_PER_PIXEL = 4;
    private int targetWidth;
    private int targetHeight;
    private int width = 320;
    private int height = 180;
    private GpuTexture frameBuffer;
    private GpuTextureView frameBufferView;
    private GpuBuffer pixelbuffer;
    private int lastCaptureDelay;
    private boolean capturedThisFrame;
    private Status status = Status.WAITING_FOR_CAPTURE;

    public TracyFrameCapture() {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.frameBuffer = gpuDevice.createTexture("Tracy Frame Capture", 10, TextureFormat.RGBA8, this.width, this.height, 1, 1);
        this.frameBufferView = gpuDevice.createTextureView(this.frameBuffer);
        this.pixelbuffer = gpuDevice.createBuffer(() -> "Tracy Frame Capture buffer", 9, this.width * this.height * 4);
    }

    private void resize(int n, int n2) {
        float f = (float)n / (float)n2;
        if (n > 320) {
            n = 320;
            n2 = (int)(320.0f / f);
        }
        if (n2 > 180) {
            n = (int)(180.0f * f);
            n2 = 180;
        }
        n = n / 4 * 4;
        n2 = n2 / 4 * 4;
        if (this.width != n || this.height != n2) {
            this.width = n;
            this.height = n2;
            GpuDevice gpuDevice = RenderSystem.getDevice();
            this.frameBuffer.close();
            this.frameBuffer = gpuDevice.createTexture("Tracy Frame Capture", 10, TextureFormat.RGBA8, n, n2, 1, 1);
            this.frameBufferView.close();
            this.frameBufferView = gpuDevice.createTextureView(this.frameBuffer);
            this.pixelbuffer.close();
            this.pixelbuffer = gpuDevice.createBuffer(() -> "Tracy Frame Capture buffer", 9, n * n2 * 4);
        }
    }

    public void capture(RenderTarget renderTarget) {
        if (this.status != Status.WAITING_FOR_CAPTURE || this.capturedThisFrame || renderTarget.getColorTexture() == null) {
            return;
        }
        this.capturedThisFrame = true;
        if (renderTarget.width != this.targetWidth || renderTarget.height != this.targetHeight) {
            this.targetWidth = renderTarget.width;
            this.targetHeight = renderTarget.height;
            this.resize(this.targetWidth, this.targetHeight);
        }
        this.status = Status.WAITING_FOR_COPY;
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Tracy blit", this.frameBufferView, OptionalInt.empty());){
            renderPass.setPipeline(RenderPipelines.TRACY_BLIT);
            renderPass.setVertexBuffer(0, RenderSystem.getQuadVertexBuffer());
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.bindSampler("InSampler", renderTarget.getColorTextureView());
            renderPass.drawIndexed(0, 0, 6, 1);
        }
        commandEncoder.copyTextureToBuffer(this.frameBuffer, this.pixelbuffer, 0, () -> {
            this.status = Status.WAITING_FOR_UPLOAD;
        }, 0);
        this.lastCaptureDelay = 0;
    }

    public void upload() {
        if (this.status != Status.WAITING_FOR_UPLOAD) {
            return;
        }
        this.status = Status.WAITING_FOR_CAPTURE;
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.pixelbuffer, true, false);){
            TracyClient.frameImage((ByteBuffer)mappedView.data(), (int)this.width, (int)this.height, (int)this.lastCaptureDelay, (boolean)true);
        }
    }

    public void endFrame() {
        ++this.lastCaptureDelay;
        this.capturedThisFrame = false;
        TracyClient.markFrame();
    }

    @Override
    public void close() {
        this.frameBuffer.close();
        this.frameBufferView.close();
        this.pixelbuffer.close();
    }

    static enum Status {
        WAITING_FOR_CAPTURE,
        WAITING_FOR_COPY,
        WAITING_FOR_UPLOAD;

    }
}

