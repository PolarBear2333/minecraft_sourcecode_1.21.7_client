/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@DontObfuscate
public class TextureUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_MIPMAP_LEVEL = 0;
    private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

    public static ByteBuffer readResource(InputStream inputStream) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        if (readableByteChannel instanceof SeekableByteChannel) {
            SeekableByteChannel seekableByteChannel = (SeekableByteChannel)readableByteChannel;
            return TextureUtil.readResource(readableByteChannel, (int)seekableByteChannel.size() + 1);
        }
        return TextureUtil.readResource(readableByteChannel, 8192);
    }

    private static ByteBuffer readResource(ReadableByteChannel readableByteChannel, int n) throws IOException {
        ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)n);
        try {
            while (readableByteChannel.read(byteBuffer) != -1) {
                if (byteBuffer.hasRemaining()) continue;
                byteBuffer = MemoryUtil.memRealloc((ByteBuffer)byteBuffer, (int)(byteBuffer.capacity() * 2));
            }
            return byteBuffer;
        }
        catch (IOException iOException) {
            MemoryUtil.memFree((Buffer)byteBuffer);
            throw iOException;
        }
    }

    public static void writeAsPNG(Path path, String string, GpuTexture gpuTexture, int n, IntUnaryOperator intUnaryOperator) {
        RenderSystem.assertOnRenderThread();
        int n2 = 0;
        for (int i = 0; i <= n; ++i) {
            n2 += gpuTexture.getFormat().pixelSize() * gpuTexture.getWidth(i) * gpuTexture.getHeight(i);
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Texture output buffer", 9, n2);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        Runnable runnable = () -> {
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false);){
                int n2 = 0;
                for (int i = 0; i <= n; ++i) {
                    int n3 = gpuTexture.getWidth(i);
                    int n4 = gpuTexture.getHeight(i);
                    try (NativeImage nativeImage = new NativeImage(n3, n4, false);){
                        for (int j = 0; j < n4; ++j) {
                            for (int k = 0; k < n3; ++k) {
                                int n5 = mappedView.data().getInt(n2 + (k + j * n3) * gpuTexture.getFormat().pixelSize());
                                nativeImage.setPixelABGR(k, j, intUnaryOperator.applyAsInt(n5));
                            }
                        }
                        Path path2 = path.resolve(string + "_" + i + ".png");
                        nativeImage.writeToFile(path2);
                        LOGGER.debug("Exported png to: {}", (Object)path2.toAbsolutePath());
                    }
                    catch (IOException iOException) {
                        LOGGER.debug("Unable to write: ", (Throwable)iOException);
                    }
                    n2 += gpuTexture.getFormat().pixelSize() * n3 * n4;
                }
            }
            gpuBuffer.close();
        };
        AtomicInteger atomicInteger = new AtomicInteger();
        int n3 = 0;
        for (int i = 0; i <= n; ++i) {
            commandEncoder.copyTextureToBuffer(gpuTexture, gpuBuffer, n3, () -> {
                if (atomicInteger.getAndIncrement() == n) {
                    runnable.run();
                }
            }, i);
            n3 += gpuTexture.getFormat().pixelSize() * gpuTexture.getWidth(i) * gpuTexture.getHeight(i);
        }
    }

    public static Path getDebugTexturePath(Path path) {
        return path.resolve("screenshots").resolve("debug");
    }

    public static Path getDebugTexturePath() {
        return TextureUtil.getDebugTexturePath(Path.of(".", new String[0]));
    }
}

