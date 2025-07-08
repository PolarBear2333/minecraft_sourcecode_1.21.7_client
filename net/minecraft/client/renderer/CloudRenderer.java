/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.slf4j.Logger;

public class CloudRenderer
extends SimplePreparableReloadListener<Optional<TextureData>>
implements AutoCloseable {
    private static final int FLAG_INSIDE_FACE = 16;
    private static final int FLAG_USE_TOP_COLOR = 32;
    private static final int MAX_RADIUS_CHUNKS = 128;
    private static final float CELL_SIZE_IN_BLOCKS = 12.0f;
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec3().putVec3().get();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
    private static final float BLOCKS_PER_SECOND = 0.6f;
    private static final long EMPTY_CELL = 0L;
    private static final int COLOR_OFFSET = 4;
    private static final int NORTH_OFFSET = 3;
    private static final int EAST_OFFSET = 2;
    private static final int SOUTH_OFFSET = 1;
    private static final int WEST_OFFSET = 0;
    private boolean needsRebuild = true;
    private int prevCellX = Integer.MIN_VALUE;
    private int prevCellZ = Integer.MIN_VALUE;
    private RelativeCameraPos prevRelativeCameraPos = RelativeCameraPos.INSIDE_CLOUDS;
    @Nullable
    private CloudStatus prevType;
    @Nullable
    private TextureData texture;
    private int quadCount = 0;
    private final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    private final MappableRingBuffer ubo = new MappableRingBuffer(() -> "Cloud UBO", 130, UBO_SIZE);
    @Nullable
    private MappableRingBuffer utb;

    /*
     * Enabled aggressive exception aggregation
     */
    @Override
    protected Optional<TextureData> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try (InputStream inputStream = resourceManager.open(TEXTURE_LOCATION);){
            NativeImage nativeImage = NativeImage.read(inputStream);
            try {
                int n = nativeImage.getWidth();
                int n2 = nativeImage.getHeight();
                long[] lArray = new long[n * n2];
                for (int i = 0; i < n2; ++i) {
                    for (int j = 0; j < n; ++j) {
                        int n3 = nativeImage.getPixel(j, i);
                        if (CloudRenderer.isCellEmpty(n3)) {
                            lArray[j + i * n] = 0L;
                            continue;
                        }
                        boolean bl = CloudRenderer.isCellEmpty(nativeImage.getPixel(j, Math.floorMod(i - 1, n2)));
                        boolean bl2 = CloudRenderer.isCellEmpty(nativeImage.getPixel(Math.floorMod(j + 1, n2), i));
                        boolean bl3 = CloudRenderer.isCellEmpty(nativeImage.getPixel(j, Math.floorMod(i + 1, n2)));
                        boolean bl4 = CloudRenderer.isCellEmpty(nativeImage.getPixel(Math.floorMod(j - 1, n2), i));
                        lArray[j + i * n] = CloudRenderer.packCellData(n3, bl, bl2, bl3, bl4);
                    }
                }
                Optional<TextureData> optional = Optional.of(new TextureData(lArray, n, n2));
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return optional;
            }
            catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to load cloud texture", (Throwable)iOException);
            return Optional.empty();
        }
    }

    private static int getSizeForCloudDistance(int n) {
        int n2 = 4;
        int n3 = (n + 1) * 2 * ((n + 1) * 2) / 2;
        int n4 = n3 * 4 + 54;
        return n4 * 3;
    }

    @Override
    protected void apply(Optional<TextureData> optional, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.texture = optional.orElse(null);
        this.needsRebuild = true;
    }

    private static boolean isCellEmpty(int n) {
        return ARGB.alpha(n) < 10;
    }

    private static long packCellData(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        return (long)n << 4 | (long)((bl ? 1 : 0) << 3) | (long)((bl2 ? 1 : 0) << 2) | (long)((bl3 ? 1 : 0) << 1) | (long)((bl4 ? 1 : 0) << 0);
    }

    private static boolean isNorthEmpty(long l) {
        return (l >> 3 & 1L) != 0L;
    }

    private static boolean isEastEmpty(long l) {
        return (l >> 2 & 1L) != 0L;
    }

    private static boolean isSouthEmpty(long l) {
        return (l >> 1 & 1L) != 0L;
    }

    private static boolean isWestEmpty(long l) {
        return (l >> 0 & 1L) != 0L;
    }

    public void render(int n, CloudStatus cloudStatus, float f, Vec3 vec3, float f2) {
        GpuTextureView gpuTextureView;
        GpuTextureView gpuTextureView2;
        Object object;
        RenderPipeline renderPipeline;
        float f3;
        float f4;
        if (this.texture == null) {
            return;
        }
        int n2 = Math.min(Minecraft.getInstance().options.cloudRange().get(), 128) * 16;
        int n3 = Mth.ceil((float)n2 / 12.0f);
        int n4 = CloudRenderer.getSizeForCloudDistance(n3);
        if (this.utb == null || this.utb.currentBuffer().size() != n4) {
            if (this.utb != null) {
                this.utb.close();
            }
            this.utb = new MappableRingBuffer(() -> "Cloud UTB", 258, n4);
        }
        RelativeCameraPos relativeCameraPos = (f4 = (f3 = (float)((double)f - vec3.y)) + 4.0f) < 0.0f ? RelativeCameraPos.ABOVE_CLOUDS : (f3 > 0.0f ? RelativeCameraPos.BELOW_CLOUDS : RelativeCameraPos.INSIDE_CLOUDS);
        double d = vec3.x + (double)(f2 * 0.030000001f);
        double d2 = vec3.z + (double)3.96f;
        double d3 = (double)this.texture.width * 12.0;
        double d4 = (double)this.texture.height * 12.0;
        d -= (double)Mth.floor(d / d3) * d3;
        d2 -= (double)Mth.floor(d2 / d4) * d4;
        int n5 = Mth.floor(d / 12.0);
        int n6 = Mth.floor(d2 / 12.0);
        float f5 = (float)(d - (double)((float)n5 * 12.0f));
        float f6 = (float)(d2 - (double)((float)n6 * 12.0f));
        boolean bl = cloudStatus == CloudStatus.FANCY;
        RenderPipeline renderPipeline2 = renderPipeline = bl ? RenderPipelines.CLOUDS : RenderPipelines.FLAT_CLOUDS;
        if (this.needsRebuild || n5 != this.prevCellX || n6 != this.prevCellZ || relativeCameraPos != this.prevRelativeCameraPos || cloudStatus != this.prevType) {
            this.needsRebuild = false;
            this.prevCellX = n5;
            this.prevCellZ = n6;
            this.prevRelativeCameraPos = relativeCameraPos;
            this.prevType = cloudStatus;
            this.utb.rotate();
            object = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.utb.currentBuffer(), false, true);
            try {
                this.buildMesh(relativeCameraPos, object.data(), n5, n6, bl, n3);
                this.quadCount = object.data().position() / 3;
            }
            finally {
                if (object != null) {
                    object.close();
                }
            }
        }
        if (this.quadCount == 0) {
            return;
        }
        object = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.ubo.currentBuffer(), false, true);
        try {
            Std140Builder.intoBuffer(object.data()).putVec4(ARGB.redFloat(n), ARGB.greenFloat(n), ARGB.blueFloat(n), 1.0f).putVec3(-f5, f3, -f6).putVec3(12.0f, 4.0f, 12.0f);
        }
        finally {
            if (object != null) {
                object.close();
            }
        }
        object = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 0.0f);
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget renderTarget2 = Minecraft.getInstance().levelRenderer.getCloudsTarget();
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(6 * this.quadCount);
        if (renderTarget2 != null) {
            gpuTextureView2 = renderTarget2.getColorTextureView();
            gpuTextureView = renderTarget2.getDepthTextureView();
        } else {
            gpuTextureView2 = renderTarget.getColorTextureView();
            gpuTextureView = renderTarget.getDepthTextureView();
        }
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Clouds", gpuTextureView2, OptionalInt.empty(), gpuTextureView, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", (GpuBufferSlice)object);
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.setVertexBuffer(0, RenderSystem.getQuadVertexBuffer());
            renderPass.setUniform("CloudInfo", this.ubo.currentBuffer());
            renderPass.setUniform("CloudFaces", this.utb.currentBuffer());
            renderPass.setPipeline(renderPipeline);
            renderPass.drawIndexed(0, 0, 6 * this.quadCount, 1);
        }
    }

    private void buildMesh(RelativeCameraPos relativeCameraPos, ByteBuffer byteBuffer, int n, int n2, boolean bl, int n3) {
        if (this.texture == null) {
            return;
        }
        long[] lArray = this.texture.cells;
        int n4 = this.texture.width;
        int n5 = this.texture.height;
        for (int i = 0; i <= 2 * n3; ++i) {
            for (int j = -i; j <= i; ++j) {
                int n6 = i - Math.abs(j);
                if (n6 < 0 || n6 > n3 || j * j + n6 * n6 > n3 * n3) continue;
                if (n6 != 0) {
                    this.tryBuildCell(relativeCameraPos, byteBuffer, n, n2, bl, j, n4, -n6, n5, lArray);
                }
                this.tryBuildCell(relativeCameraPos, byteBuffer, n, n2, bl, j, n4, n6, n5, lArray);
            }
        }
    }

    private void tryBuildCell(RelativeCameraPos relativeCameraPos, ByteBuffer byteBuffer, int n, int n2, boolean bl, int n3, int n4, int n5, int n6, long[] lArray) {
        int n7;
        int n8 = Math.floorMod(n + n3, n4);
        long l = lArray[n8 + (n7 = Math.floorMod(n2 + n5, n6)) * n4];
        if (l == 0L) {
            return;
        }
        if (bl) {
            this.buildExtrudedCell(relativeCameraPos, byteBuffer, n3, n5, l);
        } else {
            this.buildFlatCell(byteBuffer, n3, n5);
        }
    }

    private void buildFlatCell(ByteBuffer byteBuffer, int n, int n2) {
        this.encodeFace(byteBuffer, n, n2, Direction.DOWN, 32);
    }

    private void encodeFace(ByteBuffer byteBuffer, int n, int n2, Direction direction, int n3) {
        int n4 = direction.get3DDataValue() | n3;
        n4 |= (n & 1) << 7;
        byteBuffer.put((byte)(n >> 1)).put((byte)(n2 >> 1)).put((byte)(n4 |= (n2 & 1) << 6));
    }

    private void buildExtrudedCell(RelativeCameraPos relativeCameraPos, ByteBuffer byteBuffer, int n, int n2, long l) {
        boolean bl;
        if (relativeCameraPos != RelativeCameraPos.BELOW_CLOUDS) {
            this.encodeFace(byteBuffer, n, n2, Direction.UP, 0);
        }
        if (relativeCameraPos != RelativeCameraPos.ABOVE_CLOUDS) {
            this.encodeFace(byteBuffer, n, n2, Direction.DOWN, 0);
        }
        if (CloudRenderer.isNorthEmpty(l) && n2 > 0) {
            this.encodeFace(byteBuffer, n, n2, Direction.NORTH, 0);
        }
        if (CloudRenderer.isSouthEmpty(l) && n2 < 0) {
            this.encodeFace(byteBuffer, n, n2, Direction.SOUTH, 0);
        }
        if (CloudRenderer.isWestEmpty(l) && n > 0) {
            this.encodeFace(byteBuffer, n, n2, Direction.WEST, 0);
        }
        if (CloudRenderer.isEastEmpty(l) && n < 0) {
            this.encodeFace(byteBuffer, n, n2, Direction.EAST, 0);
        }
        boolean bl2 = bl = Math.abs(n) <= 1 && Math.abs(n2) <= 1;
        if (bl) {
            for (Direction direction : Direction.values()) {
                this.encodeFace(byteBuffer, n, n2, direction, 16);
            }
        }
    }

    public void markForRebuild() {
        this.needsRebuild = true;
    }

    public void endFrame() {
        this.ubo.rotate();
    }

    @Override
    public void close() {
        this.ubo.close();
        if (this.utb != null) {
            this.utb.close();
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    static enum RelativeCameraPos {
        ABOVE_CLOUDS,
        INSIDE_CLOUDS,
        BELOW_CLOUDS;

    }

    public static final class TextureData
    extends Record {
        final long[] cells;
        final int width;
        final int height;

        public TextureData(long[] lArray, int n, int n2) {
            this.cells = lArray;
            this.width = n;
            this.height = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextureData.class, "cells;width;height", "cells", "width", "height"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextureData.class, "cells;width;height", "cells", "width", "height"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextureData.class, "cells;width;height", "cells", "width", "height"}, this, object);
        }

        public long[] cells() {
            return this.cells;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }
    }
}

