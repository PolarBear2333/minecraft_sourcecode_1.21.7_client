/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

public class SpriteContents
implements Stitcher.Entry,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation name;
    final int width;
    final int height;
    private final NativeImage originalImage;
    NativeImage[] byMipLevel;
    @Nullable
    private final AnimatedTexture animatedTexture;
    private final ResourceMetadata metadata;

    public SpriteContents(ResourceLocation resourceLocation, FrameSize frameSize, NativeImage nativeImage, ResourceMetadata resourceMetadata) {
        this.name = resourceLocation;
        this.width = frameSize.width();
        this.height = frameSize.height();
        this.metadata = resourceMetadata;
        this.animatedTexture = resourceMetadata.getSection(AnimationMetadataSection.TYPE).map(animationMetadataSection -> this.createAnimatedTexture(frameSize, nativeImage.getWidth(), nativeImage.getHeight(), (AnimationMetadataSection)animationMetadataSection)).orElse(null);
        this.originalImage = nativeImage;
        this.byMipLevel = new NativeImage[]{this.originalImage};
    }

    public void increaseMipLevel(int n) {
        try {
            this.byMipLevel = MipmapGenerator.generateMipLevels(this.byMipLevel, n);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
            crashReportCategory.setDetail("First frame", () -> {
                StringBuilder stringBuilder = new StringBuilder();
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(this.originalImage.getWidth()).append("x").append(this.originalImage.getHeight());
                return stringBuilder.toString();
            });
            CrashReportCategory crashReportCategory2 = crashReport.addCategory("Frame being iterated");
            crashReportCategory2.setDetail("Sprite name", this.name);
            crashReportCategory2.setDetail("Sprite size", () -> this.width + " x " + this.height);
            crashReportCategory2.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            crashReportCategory2.setDetail("Mipmap levels", n);
            throw new ReportedException(crashReport);
        }
    }

    private int getFrameCount() {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private AnimatedTexture createAnimatedTexture(FrameSize frameSize, int n, int n2, AnimationMetadataSection animationMetadataSection) {
        ArrayList<FrameInfo> arrayList;
        int n3 = n / frameSize.width();
        int n4 = n2 / frameSize.height();
        int n5 = n3 * n4;
        int n6 = animationMetadataSection.defaultFrameTime();
        if (animationMetadataSection.frames().isEmpty()) {
            arrayList = new ArrayList<FrameInfo>(n5);
            for (int i = 0; i < n5; ++i) {
                arrayList.add(new FrameInfo(i, n6));
            }
        } else {
            Object object;
            AnimationFrame animationFrame2;
            List<AnimationFrame> list = animationMetadataSection.frames().get();
            arrayList = new ArrayList(list.size());
            for (AnimationFrame animationFrame2 : list) {
                arrayList.add(new FrameInfo(animationFrame2.index(), animationFrame2.timeOr(n6)));
            }
            int n7 = 0;
            animationFrame2 = new IntOpenHashSet();
            Iterator iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                object = (FrameInfo)iterator.next();
                boolean bl = true;
                if (((FrameInfo)object).time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", new Object[]{this.name, n7, ((FrameInfo)object).time});
                    bl = false;
                }
                if (((FrameInfo)object).index < 0 || ((FrameInfo)object).index >= n5) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", new Object[]{this.name, n7, ((FrameInfo)object).index});
                    bl = false;
                }
                if (bl) {
                    animationFrame2.add(((FrameInfo)object).index);
                } else {
                    iterator.remove();
                }
                ++n7;
            }
            object = IntStream.range(0, n5).filter(arg_0 -> SpriteContents.lambda$createAnimatedTexture$4((IntSet)animationFrame2, arg_0)).toArray();
            if (((Object)object).length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", (Object)this.name, (Object)Arrays.toString((int[])object));
            }
        }
        if (arrayList.size() <= 1) {
            return null;
        }
        return new AnimatedTexture(List.copyOf(arrayList), n3, animationMetadataSection.interpolatedFrames());
    }

    void upload(int n, int n2, int n3, int n4, NativeImage[] nativeImageArray, GpuTexture gpuTexture) {
        for (int i = 0; i < this.byMipLevel.length; ++i) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, nativeImageArray[i], i, 0, n >> i, n2 >> i, this.width >> i, this.height >> i, n3 >> i, n4 >> i);
        }
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public ResourceLocation name() {
        return this.name;
    }

    public IntStream getUniqueFrames() {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    @Nullable
    public SpriteTicker createTicker() {
        return this.animatedTexture != null ? this.animatedTexture.createTicker() : null;
    }

    public ResourceMetadata metadata() {
        return this.metadata;
    }

    @Override
    public void close() {
        for (NativeImage nativeImage : this.byMipLevel) {
            nativeImage.close();
        }
    }

    public String toString() {
        return "SpriteContents{name=" + String.valueOf(this.name) + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
    }

    public boolean isTransparent(int n, int n2, int n3) {
        int n4 = n2;
        int n5 = n3;
        if (this.animatedTexture != null) {
            n4 += this.animatedTexture.getFrameX(n) * this.width;
            n5 += this.animatedTexture.getFrameY(n) * this.height;
        }
        return ARGB.alpha(this.originalImage.getPixel(n4, n5)) == 0;
    }

    public void uploadFirstFrame(int n, int n2, GpuTexture gpuTexture) {
        if (this.animatedTexture != null) {
            this.animatedTexture.uploadFirstFrame(n, n2, gpuTexture);
        } else {
            this.upload(n, n2, 0, 0, this.byMipLevel, gpuTexture);
        }
    }

    private static /* synthetic */ boolean lambda$createAnimatedTexture$4(IntSet intSet, int n) {
        return !intSet.contains(n);
    }

    class AnimatedTexture {
        final List<FrameInfo> frames;
        private final int frameRowSize;
        private final boolean interpolateFrames;

        AnimatedTexture(List<FrameInfo> list, int n, boolean bl) {
            this.frames = list;
            this.frameRowSize = n;
            this.interpolateFrames = bl;
        }

        int getFrameX(int n) {
            return n % this.frameRowSize;
        }

        int getFrameY(int n) {
            return n / this.frameRowSize;
        }

        void uploadFrame(int n, int n2, int n3, GpuTexture gpuTexture) {
            int n4 = this.getFrameX(n3) * SpriteContents.this.width;
            int n5 = this.getFrameY(n3) * SpriteContents.this.height;
            SpriteContents.this.upload(n, n2, n4, n5, SpriteContents.this.byMipLevel, gpuTexture);
        }

        public SpriteTicker createTicker() {
            return new Ticker(SpriteContents.this, this, this.interpolateFrames ? new InterpolationData() : null);
        }

        public void uploadFirstFrame(int n, int n2, GpuTexture gpuTexture) {
            this.uploadFrame(n, n2, this.frames.get((int)0).index, gpuTexture);
        }

        public IntStream getUniqueFrames() {
            return this.frames.stream().mapToInt(frameInfo -> frameInfo.index).distinct();
        }
    }

    static final class FrameInfo
    extends Record {
        final int index;
        final int time;

        FrameInfo(int n, int n2) {
            this.index = n;
            this.time = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FrameInfo.class, "index;time", "index", "time"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FrameInfo.class, "index;time", "index", "time"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FrameInfo.class, "index;time", "index", "time"}, this, object);
        }

        public int index() {
            return this.index;
        }

        public int time() {
            return this.time;
        }
    }

    class Ticker
    implements SpriteTicker {
        int frame;
        int subFrame;
        final AnimatedTexture animationInfo;
        @Nullable
        private final InterpolationData interpolationData;

        Ticker(SpriteContents spriteContents, @Nullable AnimatedTexture animatedTexture, InterpolationData interpolationData) {
            this.animationInfo = animatedTexture;
            this.interpolationData = interpolationData;
        }

        @Override
        public void tickAndUpload(int n, int n2, GpuTexture gpuTexture) {
            ++this.subFrame;
            FrameInfo frameInfo = this.animationInfo.frames.get(this.frame);
            if (this.subFrame >= frameInfo.time) {
                int n3 = frameInfo.index;
                this.frame = (this.frame + 1) % this.animationInfo.frames.size();
                this.subFrame = 0;
                int n4 = this.animationInfo.frames.get((int)this.frame).index;
                if (n3 != n4) {
                    this.animationInfo.uploadFrame(n, n2, n4, gpuTexture);
                }
            } else if (this.interpolationData != null) {
                this.interpolationData.uploadInterpolatedFrame(n, n2, this, gpuTexture);
            }
        }

        @Override
        public void close() {
            if (this.interpolationData != null) {
                this.interpolationData.close();
            }
        }
    }

    final class InterpolationData
    implements AutoCloseable {
        private final NativeImage[] activeFrame;

        InterpolationData() {
            this.activeFrame = new NativeImage[SpriteContents.this.byMipLevel.length];
            for (int i = 0; i < this.activeFrame.length; ++i) {
                int n = SpriteContents.this.width >> i;
                int n2 = SpriteContents.this.height >> i;
                this.activeFrame[i] = new NativeImage(n, n2, false);
            }
        }

        void uploadInterpolatedFrame(int n, int n2, Ticker ticker, GpuTexture gpuTexture) {
            AnimatedTexture animatedTexture = ticker.animationInfo;
            List<FrameInfo> list = animatedTexture.frames;
            FrameInfo frameInfo = list.get(ticker.frame);
            float f = (float)ticker.subFrame / (float)frameInfo.time;
            int n3 = frameInfo.index;
            int n4 = list.get((int)((ticker.frame + 1) % list.size())).index;
            if (n3 != n4) {
                for (int i = 0; i < this.activeFrame.length; ++i) {
                    int n5 = SpriteContents.this.width >> i;
                    int n6 = SpriteContents.this.height >> i;
                    for (int j = 0; j < n6; ++j) {
                        for (int k = 0; k < n5; ++k) {
                            int n7 = this.getPixel(animatedTexture, n3, i, k, j);
                            int n8 = this.getPixel(animatedTexture, n4, i, k, j);
                            this.activeFrame[i].setPixel(k, j, ARGB.lerp(f, n7, n8));
                        }
                    }
                }
                SpriteContents.this.upload(n, n2, 0, 0, this.activeFrame, gpuTexture);
            }
        }

        private int getPixel(AnimatedTexture animatedTexture, int n, int n2, int n3, int n4) {
            return SpriteContents.this.byMipLevel[n2].getPixel(n3 + (animatedTexture.getFrameX(n) * SpriteContents.this.width >> n2), n4 + (animatedTexture.getFrameY(n) * SpriteContents.this.height >> n2));
        }

        @Override
        public void close() {
            for (NativeImage nativeImage : this.activeFrame) {
                nativeImage.close();
            }
        }
    }
}

