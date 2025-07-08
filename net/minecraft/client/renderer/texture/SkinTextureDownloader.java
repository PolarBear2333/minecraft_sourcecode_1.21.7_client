/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

public class SkinTextureDownloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;
    private static final int LEGACY_SKIN_HEIGHT = 32;

    public static CompletableFuture<ResourceLocation> downloadAndRegisterSkin(ResourceLocation resourceLocation, Path path, String string, boolean bl) {
        return CompletableFuture.supplyAsync(() -> {
            NativeImage nativeImage;
            try {
                nativeImage = SkinTextureDownloader.downloadSkin(path, string);
            }
            catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
            return bl ? SkinTextureDownloader.processLegacySkin(nativeImage, string) : nativeImage;
        }, Util.nonCriticalIoPool().forName("downloadTexture")).thenCompose(nativeImage -> SkinTextureDownloader.registerTextureInManager(resourceLocation, nativeImage));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static NativeImage downloadSkin(Path path, String string) throws IOException {
        if (Files.isRegularFile(path, new LinkOption[0])) {
            LOGGER.debug("Loading HTTP texture from local cache ({})", (Object)path);
            try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
                NativeImage nativeImage = NativeImage.read(inputStream);
                return nativeImage;
            }
        }
        HttpURLConnection httpURLConnection = null;
        LOGGER.debug("Downloading HTTP texture from {} to {}", (Object)string, (Object)path);
        URI uRI = URI.create(string);
        try {
            httpURLConnection = (HttpURLConnection)uRI.toURL().openConnection(Minecraft.getInstance().getProxy());
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();
            int n = httpURLConnection.getResponseCode();
            if (n / 100 != 2) {
                throw new IOException("Failed to open " + String.valueOf(uRI) + ", HTTP error code: " + n);
            }
            byte[] byArray = httpURLConnection.getInputStream().readAllBytes();
            try {
                FileUtil.createDirectoriesSafe(path.getParent());
                Files.write(path, byArray, new OpenOption[0]);
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to cache texture {} in {}", (Object)string, (Object)path);
            }
            NativeImage nativeImage = NativeImage.read(byArray);
            return nativeImage;
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private static CompletableFuture<ResourceLocation> registerTextureInManager(ResourceLocation resourceLocation, NativeImage nativeImage) {
        Minecraft minecraft = Minecraft.getInstance();
        return CompletableFuture.supplyAsync(() -> {
            DynamicTexture dynamicTexture = new DynamicTexture(resourceLocation::toString, nativeImage);
            minecraft.getTextureManager().register(resourceLocation, dynamicTexture);
            return resourceLocation;
        }, minecraft);
    }

    private static NativeImage processLegacySkin(NativeImage nativeImage, String string) {
        boolean bl;
        int n = nativeImage.getHeight();
        int n2 = nativeImage.getWidth();
        if (n2 != 64 || n != 32 && n != 64) {
            nativeImage.close();
            throw new IllegalStateException("Discarding incorrectly sized (" + n2 + "x" + n + ") skin texture from " + string);
        }
        boolean bl2 = bl = n == 32;
        if (bl) {
            NativeImage nativeImage2 = new NativeImage(64, 64, true);
            nativeImage2.copyFrom(nativeImage);
            nativeImage.close();
            nativeImage = nativeImage2;
            nativeImage.fillRect(0, 32, 64, 32, 0);
            nativeImage.copyRect(4, 16, 16, 32, 4, 4, true, false);
            nativeImage.copyRect(8, 16, 16, 32, 4, 4, true, false);
            nativeImage.copyRect(0, 20, 24, 32, 4, 12, true, false);
            nativeImage.copyRect(4, 20, 16, 32, 4, 12, true, false);
            nativeImage.copyRect(8, 20, 8, 32, 4, 12, true, false);
            nativeImage.copyRect(12, 20, 16, 32, 4, 12, true, false);
            nativeImage.copyRect(44, 16, -8, 32, 4, 4, true, false);
            nativeImage.copyRect(48, 16, -8, 32, 4, 4, true, false);
            nativeImage.copyRect(40, 20, 0, 32, 4, 12, true, false);
            nativeImage.copyRect(44, 20, -8, 32, 4, 12, true, false);
            nativeImage.copyRect(48, 20, -16, 32, 4, 12, true, false);
            nativeImage.copyRect(52, 20, -8, 32, 4, 12, true, false);
        }
        SkinTextureDownloader.setNoAlpha(nativeImage, 0, 0, 32, 16);
        if (bl) {
            SkinTextureDownloader.doNotchTransparencyHack(nativeImage, 32, 0, 64, 32);
        }
        SkinTextureDownloader.setNoAlpha(nativeImage, 0, 16, 64, 32);
        SkinTextureDownloader.setNoAlpha(nativeImage, 16, 48, 48, 64);
        return nativeImage;
    }

    private static void doNotchTransparencyHack(NativeImage nativeImage, int n, int n2, int n3, int n4) {
        int n5;
        int n6;
        for (n6 = n; n6 < n3; ++n6) {
            for (n5 = n2; n5 < n4; ++n5) {
                int n7 = nativeImage.getPixel(n6, n5);
                if (ARGB.alpha(n7) >= 128) continue;
                return;
            }
        }
        for (n6 = n; n6 < n3; ++n6) {
            for (n5 = n2; n5 < n4; ++n5) {
                nativeImage.setPixel(n6, n5, nativeImage.getPixel(n6, n5) & 0xFFFFFF);
            }
        }
    }

    private static void setNoAlpha(NativeImage nativeImage, int n, int n2, int n3, int n4) {
        for (int i = n; i < n3; ++i) {
            for (int j = n2; j < n4; ++j) {
                nativeImage.setPixel(i, j, ARGB.opaque(nativeImage.getPixel(i, j)));
            }
        }
    }
}

