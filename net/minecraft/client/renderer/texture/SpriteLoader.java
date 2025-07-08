/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import org.slf4j.Logger;

public class SpriteLoader {
    public static final Set<MetadataSectionType<?>> DEFAULT_METADATA_SECTIONS = Set.of(AnimationMetadataSection.TYPE);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;
    private final int minWidth;
    private final int minHeight;

    public SpriteLoader(ResourceLocation resourceLocation, int n, int n2, int n3) {
        this.location = resourceLocation;
        this.maxSupportedTextureSize = n;
        this.minWidth = n2;
        this.minHeight = n3;
    }

    public static SpriteLoader create(TextureAtlas textureAtlas) {
        return new SpriteLoader(textureAtlas.location(), textureAtlas.maxSupportedTextureSize(), textureAtlas.getWidth(), textureAtlas.getHeight());
    }

    public Preparations stitch(List<SpriteContents> list, int n, Executor executor) {
        try (Zone zone = Profiler.get().zone(() -> "stitch " + String.valueOf(this.location));){
            int n2;
            int n3 = this.maxSupportedTextureSize;
            Stitcher<SpriteContents> stitcher = new Stitcher<SpriteContents>(n3, n3, n);
            int n4 = Integer.MAX_VALUE;
            int n5 = 1 << n;
            for (SpriteContents spriteContents : list) {
                n4 = Math.min(n4, Math.min(spriteContents.width(), spriteContents.height()));
                n2 = Math.min(Integer.lowestOneBit(spriteContents.width()), Integer.lowestOneBit(spriteContents.height()));
                if (n2 < n5) {
                    LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", new Object[]{spriteContents.name(), spriteContents.width(), spriteContents.height(), Mth.log2(n5), Mth.log2(n2)});
                    n5 = n2;
                }
                stitcher.registerSprite(spriteContents);
            }
            int n6 = Math.min(n4, n5);
            int n7 = Mth.log2(n6);
            if (n7 < n) {
                LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[]{this.location, n, n7, n6});
                n2 = n7;
            } else {
                n2 = n;
            }
            try {
                stitcher.stitch();
            }
            catch (StitcherException stitcherException) {
                CrashReport crashReport = CrashReport.forThrowable(stitcherException, "Stitching");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
                crashReportCategory.setDetail("Sprites", stitcherException.getAllSprites().stream().map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height())).collect(Collectors.joining(",")));
                crashReportCategory.setDetail("Max Texture Size", n3);
                throw new ReportedException(crashReport);
            }
            int n8 = Math.max(stitcher.getWidth(), this.minWidth);
            int n9 = Math.max(stitcher.getHeight(), this.minHeight);
            Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, n8, n9);
            TextureAtlasSprite textureAtlasSprite = map.get(MissingTextureAtlasSprite.getLocation());
            CompletableFuture<Object> completableFuture = n2 > 0 ? CompletableFuture.runAsync(() -> map.values().forEach(textureAtlasSprite -> textureAtlasSprite.contents().increaseMipLevel(n2)), executor) : CompletableFuture.completedFuture(null);
            Preparations preparations = new Preparations(n8, n9, n2, textureAtlasSprite, map, completableFuture);
            return preparations;
        }
    }

    public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(SpriteResourceLoader spriteResourceLoader, List<Function<SpriteResourceLoader, SpriteContents>> list2, Executor executor) {
        List<CompletableFuture> list3 = list2.stream().map(function -> CompletableFuture.supplyAsync(() -> (SpriteContents)function.apply(spriteResourceLoader), executor)).toList();
        return Util.sequence(list3).thenApply(list -> list.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<Preparations> loadAndStitch(ResourceManager resourceManager, ResourceLocation resourceLocation, int n, Executor executor) {
        return this.loadAndStitch(resourceManager, resourceLocation, n, executor, DEFAULT_METADATA_SECTIONS);
    }

    public CompletableFuture<Preparations> loadAndStitch(ResourceManager resourceManager, ResourceLocation resourceLocation, int n, Executor executor, Collection<MetadataSectionType<?>> collection) {
        SpriteResourceLoader spriteResourceLoader = SpriteResourceLoader.create(collection);
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> SpriteSourceList.load(resourceManager, resourceLocation).list(resourceManager), executor).thenCompose(list -> SpriteLoader.runSpriteSuppliers(spriteResourceLoader, list, executor))).thenApply(list -> this.stitch((List<SpriteContents>)list, n, executor));
    }

    private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int n, int n2) {
        HashMap<ResourceLocation, TextureAtlasSprite> hashMap = new HashMap<ResourceLocation, TextureAtlasSprite>();
        stitcher.gatherSprites((spriteContents, n3, n4) -> hashMap.put(spriteContents.name(), new TextureAtlasSprite(this.location, (SpriteContents)spriteContents, n, n2, n3, n4)));
        return hashMap;
    }

    public record Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
        public CompletableFuture<Preparations> waitForUpload() {
            return this.readyForUpload.thenApply(void_ -> this);
        }
    }
}

