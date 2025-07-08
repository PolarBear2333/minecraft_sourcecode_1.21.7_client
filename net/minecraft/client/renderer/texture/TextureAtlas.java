/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class TextureAtlas
extends AbstractTexture
implements Dumpable,
Tickable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");
    private List<SpriteContents> sprites = List.of();
    private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
    @Nullable
    private TextureAtlasSprite missingSprite;
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;
    private int width;
    private int height;
    private int mipLevel;

    public TextureAtlas(ResourceLocation resourceLocation) {
        this.location = resourceLocation;
        this.maxSupportedTextureSize = RenderSystem.getDevice().getMaxTextureSize();
    }

    private void createTexture(int n, int n2, int n3) {
        LOGGER.info("Created: {}x{}x{} {}-atlas", new Object[]{n, n2, n3, this.location});
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.close();
        this.texture = gpuDevice.createTexture(this.location::toString, 7, TextureFormat.RGBA8, n, n2, 1, n3 + 1);
        this.textureView = gpuDevice.createTextureView(this.texture);
        this.width = n;
        this.height = n2;
        this.mipLevel = n3;
    }

    public void upload(SpriteLoader.Preparations preparations) {
        this.createTexture(preparations.width(), preparations.height(), preparations.mipLevel());
        this.clearTextureData();
        this.setFilter(false, this.mipLevel > 1);
        this.texturesByName = Map.copyOf(preparations.regions());
        this.missingSprite = this.texturesByName.get(MissingTextureAtlasSprite.getLocation());
        if (this.missingSprite == null) {
            throw new IllegalStateException("Atlas '" + String.valueOf(this.location) + "' (" + this.texturesByName.size() + " sprites) has no missing texture sprite");
        }
        ArrayList<SpriteContents> arrayList = new ArrayList<SpriteContents>();
        ArrayList<TextureAtlasSprite.Ticker> arrayList2 = new ArrayList<TextureAtlasSprite.Ticker>();
        for (TextureAtlasSprite textureAtlasSprite : preparations.regions().values()) {
            arrayList.add(textureAtlasSprite.contents());
            try {
                textureAtlasSprite.uploadFirstFrame(this.texture);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
                crashReportCategory.setDetail("Atlas path", this.location);
                crashReportCategory.setDetail("Sprite", textureAtlasSprite);
                throw new ReportedException(crashReport);
            }
            TextureAtlasSprite.Ticker ticker = textureAtlasSprite.createTicker();
            if (ticker == null) continue;
            arrayList2.add(ticker);
        }
        this.sprites = List.copyOf(arrayList);
        this.animatedTextures = List.copyOf(arrayList2);
    }

    @Override
    public void dumpContents(ResourceLocation resourceLocation, Path path) throws IOException {
        String string = resourceLocation.toDebugFileName();
        TextureUtil.writeAsPNG(path, string, this.getTexture(), this.mipLevel, n -> n);
        TextureAtlas.dumpSpriteNames(path, string, this.texturesByName);
    }

    private static void dumpSpriteNames(Path path, String string, Map<ResourceLocation, TextureAtlasSprite> map) {
        Path path2 = path.resolve(string + ".txt");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path2, new OpenOption[0]);){
            for (Map.Entry entry : map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)entry.getValue();
                bufferedWriter.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), textureAtlasSprite.getX(), textureAtlasSprite.getY(), textureAtlasSprite.contents().width(), textureAtlasSprite.contents().height()));
            }
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to write file {}", (Object)path2, (Object)iOException);
        }
    }

    public void cycleAnimationFrames() {
        if (this.texture == null) {
            return;
        }
        for (TextureAtlasSprite.Ticker ticker : this.animatedTextures) {
            ticker.tickAndUpload(this.texture);
        }
    }

    @Override
    public void tick() {
        this.cycleAnimationFrames();
    }

    public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        TextureAtlasSprite textureAtlasSprite = this.texturesByName.getOrDefault(resourceLocation, this.missingSprite);
        if (textureAtlasSprite == null) {
            throw new IllegalStateException("Tried to lookup sprite, but atlas is not initialized");
        }
        return textureAtlasSprite;
    }

    public void clearTextureData() {
        this.sprites.forEach(SpriteContents::close);
        this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
        this.sprites = List.of();
        this.animatedTextures = List.of();
        this.texturesByName = Map.of();
        this.missingSprite = null;
    }

    public ResourceLocation location() {
        return this.location;
    }

    public int maxSupportedTextureSize() {
        return this.maxSupportedTextureSize;
    }

    int getWidth() {
        return this.width;
    }

    int getHeight() {
        return this.height;
    }
}

