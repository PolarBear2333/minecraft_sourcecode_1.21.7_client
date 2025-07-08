/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

public class Screenshot {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SCREENSHOT_DIR = "screenshots";

    public static void grab(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
        Screenshot.grab(file, null, renderTarget, 1, consumer);
    }

    public static void grab(File file, @Nullable String string, RenderTarget renderTarget, int n, Consumer<Component> consumer) {
        Screenshot.takeScreenshot(renderTarget, n, nativeImage -> {
            File file2 = new File(file, SCREENSHOT_DIR);
            file2.mkdir();
            File file3 = string == null ? Screenshot.getFile(file2) : new File(file2, string);
            Util.ioPool().execute(() -> {
                try (NativeImage nativeImage2 = nativeImage;){
                    nativeImage.writeToFile(file3);
                    MutableComponent mutableComponent = Component.literal(file3.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(file3.getAbsoluteFile())));
                    consumer.accept(Component.translatable("screenshot.success", mutableComponent));
                }
                catch (Exception exception) {
                    LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
                    consumer.accept(Component.translatable("screenshot.failure", exception.getMessage()));
                }
            });
        });
    }

    public static void takeScreenshot(RenderTarget renderTarget, Consumer<NativeImage> consumer) {
        Screenshot.takeScreenshot(renderTarget, 1, consumer);
    }

    public static void takeScreenshot(RenderTarget renderTarget, int n, Consumer<NativeImage> consumer) {
        int n2 = renderTarget.width;
        int n3 = renderTarget.height;
        GpuTexture gpuTexture = renderTarget.getColorTexture();
        if (gpuTexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        }
        if (n2 % n != 0 || n3 % n != 0) {
            throw new IllegalArgumentException("Image size is not divisible by downscale factor");
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Screenshot buffer", 9, n2 * n3 * gpuTexture.getFormat().pixelSize());
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gpuTexture, gpuBuffer, 0, () -> {
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false);){
                int n4 = n3 / n;
                int n5 = n2 / n;
                NativeImage nativeImage = new NativeImage(n5, n4, false);
                for (int i = 0; i < n4; ++i) {
                    for (int j = 0; j < n5; ++j) {
                        int n6;
                        int n7;
                        if (n == 1) {
                            n7 = mappedView.data().getInt((j + i * n2) * gpuTexture.getFormat().pixelSize());
                            nativeImage.setPixelABGR(j, n3 - i - 1, n7 | 0xFF000000);
                            continue;
                        }
                        n7 = 0;
                        int n8 = 0;
                        int n9 = 0;
                        for (n6 = 0; n6 < n; ++n6) {
                            for (int k = 0; k < n; ++k) {
                                int n10 = mappedView.data().getInt((j * n + n6 + (i * n + k) * n2) * gpuTexture.getFormat().pixelSize());
                                n7 += ARGB.red(n10);
                                n8 += ARGB.green(n10);
                                n9 += ARGB.blue(n10);
                            }
                        }
                        n6 = n * n;
                        nativeImage.setPixelABGR(j, n4 - i - 1, ARGB.color(255, n7 / n6, n8 / n6, n9 / n6));
                    }
                }
                consumer.accept(nativeImage);
            }
            gpuBuffer.close();
        }, 0);
    }

    private static File getFile(File file) {
        String string = Util.getFilenameFormattedDateTime();
        int n = 1;
        File file2;
        while ((file2 = new File(file, string + (String)(n == 1 ? "" : "_" + n) + ".png")).exists()) {
            ++n;
        }
        return file2;
    }
}

