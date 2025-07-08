/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;

public class SpriteSourceList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
    private final List<SpriteSource> sources;

    private SpriteSourceList(List<SpriteSource> list) {
        this.sources = list;
    }

    public List<Function<SpriteResourceLoader, SpriteContents>> list(ResourceManager resourceManager) {
        final HashMap hashMap = new HashMap();
        SpriteSource.Output output = new SpriteSource.Output(){

            @Override
            public void add(ResourceLocation resourceLocation, SpriteSource.SpriteSupplier spriteSupplier) {
                SpriteSource.SpriteSupplier spriteSupplier2 = hashMap.put(resourceLocation, spriteSupplier);
                if (spriteSupplier2 != null) {
                    spriteSupplier2.discard();
                }
            }

            @Override
            public void removeAll(Predicate<ResourceLocation> predicate) {
                Iterator iterator = hashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    if (!predicate.test((ResourceLocation)entry.getKey())) continue;
                    ((SpriteSource.SpriteSupplier)entry.getValue()).discard();
                    iterator.remove();
                }
            }
        };
        this.sources.forEach(spriteSource -> spriteSource.run(resourceManager, output));
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(spriteResourceLoader -> MissingTextureAtlasSprite.create());
        builder.addAll(hashMap.values());
        return builder.build();
    }

    public static SpriteSourceList load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = ATLAS_INFO_CONVERTER.idToFile(resourceLocation);
        ArrayList<SpriteSource> arrayList = new ArrayList<SpriteSource>();
        for (Resource resource : resourceManager.getResourceStack(resourceLocation2)) {
            try {
                BufferedReader bufferedReader = resource.openAsReader();
                try {
                    Dynamic dynamic = new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)StrictJsonParser.parse(bufferedReader));
                    arrayList.addAll((Collection)SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow());
                }
                finally {
                    if (bufferedReader == null) continue;
                    bufferedReader.close();
                }
            }
            catch (Exception exception) {
                LOGGER.error("Failed to parse atlas definition {} in pack {}", new Object[]{resourceLocation2, resource.sourcePackId(), exception});
            }
        }
        return new SpriteSourceList(arrayList);
    }
}

