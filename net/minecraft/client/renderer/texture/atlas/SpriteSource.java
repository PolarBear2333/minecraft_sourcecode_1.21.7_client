/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.client.renderer.texture.atlas;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public interface SpriteSource {
    public static final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

    public void run(ResourceManager var1, Output var2);

    public MapCodec<? extends SpriteSource> codec();

    public static interface SpriteSupplier
    extends Function<SpriteResourceLoader, SpriteContents> {
        default public void discard() {
        }
    }

    public static interface Output {
        default public void add(ResourceLocation resourceLocation, Resource resource) {
            this.add(resourceLocation, spriteResourceLoader -> spriteResourceLoader.loadSprite(resourceLocation, resource));
        }

        public void add(ResourceLocation var1, SpriteSupplier var2);

        public void removeAll(Predicate<ResourceLocation> var1);
    }
}

