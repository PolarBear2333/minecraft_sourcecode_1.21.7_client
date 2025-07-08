/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public record Unstitcher(ResourceLocation resource, List<Region> regions, double xDivisor, double yDivisor) implements SpriteSource
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<Unstitcher> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("resource").forGetter(Unstitcher::resource), (App)ExtraCodecs.nonEmptyList(Region.CODEC.listOf()).fieldOf("regions").forGetter(Unstitcher::regions), (App)Codec.DOUBLE.optionalFieldOf("divisor_x", (Object)1.0).forGetter(Unstitcher::xDivisor), (App)Codec.DOUBLE.optionalFieldOf("divisor_y", (Object)1.0).forGetter(Unstitcher::yDivisor)).apply((Applicative)instance, Unstitcher::new));

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        ResourceLocation resourceLocation = TEXTURE_ID_CONVERTER.idToFile(this.resource);
        Optional<Resource> optional = resourceManager.getResource(resourceLocation);
        if (optional.isPresent()) {
            LazyLoadedImage lazyLoadedImage = new LazyLoadedImage(resourceLocation, optional.get(), this.regions.size());
            for (Region region : this.regions) {
                output.add(region.sprite, new RegionInstance(lazyLoadedImage, region, this.xDivisor, this.yDivisor));
            }
        } else {
            LOGGER.warn("Missing sprite: {}", (Object)resourceLocation);
        }
    }

    public MapCodec<Unstitcher> codec() {
        return MAP_CODEC;
    }

    public static final class Region
    extends Record {
        final ResourceLocation sprite;
        final double x;
        final double y;
        final double width;
        final double height;
        public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("sprite").forGetter(Region::sprite), (App)Codec.DOUBLE.fieldOf("x").forGetter(Region::x), (App)Codec.DOUBLE.fieldOf("y").forGetter(Region::y), (App)Codec.DOUBLE.fieldOf("width").forGetter(Region::width), (App)Codec.DOUBLE.fieldOf("height").forGetter(Region::height)).apply((Applicative)instance, Region::new));

        public Region(ResourceLocation resourceLocation, double d, double d2, double d3, double d4) {
            this.sprite = resourceLocation;
            this.x = d;
            this.y = d2;
            this.width = d3;
            this.height = d4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Region.class, "sprite;x;y;width;height", "sprite", "x", "y", "width", "height"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Region.class, "sprite;x;y;width;height", "sprite", "x", "y", "width", "height"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Region.class, "sprite;x;y;width;height", "sprite", "x", "y", "width", "height"}, this, object);
        }

        public ResourceLocation sprite() {
            return this.sprite;
        }

        public double x() {
            return this.x;
        }

        public double y() {
            return this.y;
        }

        public double width() {
            return this.width;
        }

        public double height() {
            return this.height;
        }
    }

    static class RegionInstance
    implements SpriteSource.SpriteSupplier {
        private final LazyLoadedImage image;
        private final Region region;
        private final double xDivisor;
        private final double yDivisor;

        RegionInstance(LazyLoadedImage lazyLoadedImage, Region region, double d, double d2) {
            this.image = lazyLoadedImage;
            this.region = region;
            this.xDivisor = d;
            this.yDivisor = d2;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public SpriteContents apply(SpriteResourceLoader spriteResourceLoader) {
            try {
                NativeImage nativeImage = this.image.get();
                double d = (double)nativeImage.getWidth() / this.xDivisor;
                double d2 = (double)nativeImage.getHeight() / this.yDivisor;
                int n = Mth.floor(this.region.x * d);
                int n2 = Mth.floor(this.region.y * d2);
                int n3 = Mth.floor(this.region.width * d);
                int n4 = Mth.floor(this.region.height * d2);
                NativeImage nativeImage2 = new NativeImage(NativeImage.Format.RGBA, n3, n4, false);
                nativeImage.copyRect(nativeImage2, n, n2, 0, 0, n3, n4, false, false);
                SpriteContents spriteContents = new SpriteContents(this.region.sprite, new FrameSize(n3, n4), nativeImage2, ResourceMetadata.EMPTY);
                return spriteContents;
            }
            catch (Exception exception) {
                LOGGER.error("Failed to unstitch region {}", (Object)this.region.sprite, (Object)exception);
            }
            finally {
                this.image.release();
            }
            return MissingTextureAtlasSprite.create();
        }

        @Override
        public void discard() {
            this.image.release();
        }

        @Override
        public /* synthetic */ Object apply(Object object) {
            return this.apply((SpriteResourceLoader)object);
        }
    }
}

