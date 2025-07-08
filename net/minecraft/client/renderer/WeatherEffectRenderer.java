/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WeatherEffectRenderer {
    private static final int RAIN_RADIUS = 10;
    private static final int RAIN_DIAMETER = 21;
    private static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");
    private static final int RAIN_TABLE_SIZE = 32;
    private static final int HALF_RAIN_TABLE_SIZE = 16;
    private int rainSoundTime;
    private final float[] columnSizeX = new float[1024];
    private final float[] columnSizeZ = new float[1024];

    public WeatherEffectRenderer() {
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float f2 = i - 16;
                float f3 = Mth.length(f, f2);
                this.columnSizeX[i * 32 + j] = -f2 / f3;
                this.columnSizeZ[i * 32 + j] = f / f3;
            }
        }
    }

    public void render(Level level, MultiBufferSource multiBufferSource, int n, float f, Vec3 vec3) {
        float f2 = level.getRainLevel(f);
        if (f2 <= 0.0f) {
            return;
        }
        int n2 = Minecraft.useFancyGraphics() ? 10 : 5;
        ArrayList<ColumnInstance> arrayList = new ArrayList<ColumnInstance>();
        ArrayList<ColumnInstance> arrayList2 = new ArrayList<ColumnInstance>();
        this.collectColumnInstances(level, n, f, vec3, n2, arrayList, arrayList2);
        if (!arrayList.isEmpty() || !arrayList2.isEmpty()) {
            this.render(multiBufferSource, vec3, n2, f2, arrayList, arrayList2);
        }
    }

    private void collectColumnInstances(Level level, int n, float f, Vec3 vec3, int n2, List<ColumnInstance> list, List<ColumnInstance> list2) {
        int n3 = Mth.floor(vec3.x);
        int n4 = Mth.floor(vec3.y);
        int n5 = Mth.floor(vec3.z);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        RandomSource randomSource = RandomSource.create();
        for (int i = n5 - n2; i <= n5 + n2; ++i) {
            for (int j = n3 - n2; j <= n3 + n2; ++j) {
                Biome.Precipitation precipitation;
                int n6 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, j, i);
                int n7 = Math.max(n4 - n2, n6);
                int n8 = Math.max(n4 + n2, n6);
                if (n8 - n7 == 0 || (precipitation = this.getPrecipitationAt(level, mutableBlockPos.set(j, n4, i))) == Biome.Precipitation.NONE) continue;
                int n9 = j * j * 3121 + j * 45238971 ^ i * i * 418711 + i * 13761;
                randomSource.setSeed(n9);
                int n10 = Math.max(n4, n6);
                int n11 = LevelRenderer.getLightColor(level, mutableBlockPos.set(j, n10, i));
                if (precipitation == Biome.Precipitation.RAIN) {
                    list.add(this.createRainColumnInstance(randomSource, n, j, n7, n8, i, n11, f));
                    continue;
                }
                if (precipitation != Biome.Precipitation.SNOW) continue;
                list2.add(this.createSnowColumnInstance(randomSource, n, j, n7, n8, i, n11, f));
            }
        }
    }

    private void render(MultiBufferSource multiBufferSource, Vec3 vec3, int n, float f, List<ColumnInstance> list, List<ColumnInstance> list2) {
        RenderType renderType;
        if (!list.isEmpty()) {
            renderType = RenderType.weather(RAIN_LOCATION, Minecraft.useShaderTransparency());
            this.renderInstances(multiBufferSource.getBuffer(renderType), list, vec3, 1.0f, n, f);
        }
        if (!list2.isEmpty()) {
            renderType = RenderType.weather(SNOW_LOCATION, Minecraft.useShaderTransparency());
            this.renderInstances(multiBufferSource.getBuffer(renderType), list2, vec3, 0.8f, n, f);
        }
    }

    private ColumnInstance createRainColumnInstance(RandomSource randomSource, int n, int n2, int n3, int n4, int n5, int n6, float f) {
        int n7 = n & 0x1FFFF;
        int n8 = n2 * n2 * 3121 + n2 * 45238971 + n5 * n5 * 418711 + n5 * 13761 & 0xFF;
        float f2 = 3.0f + randomSource.nextFloat();
        float f3 = -((float)(n7 + n8) + f) / 32.0f * f2;
        float f4 = f3 % 32.0f;
        return new ColumnInstance(n2, n5, n3, n4, 0.0f, f4, n6);
    }

    private ColumnInstance createSnowColumnInstance(RandomSource randomSource, int n, int n2, int n3, int n4, int n5, int n6, float f) {
        float f2 = (float)n + f;
        float f3 = (float)(randomSource.nextDouble() + (double)(f2 * 0.01f * (float)randomSource.nextGaussian()));
        float f4 = (float)(randomSource.nextDouble() + (double)(f2 * (float)randomSource.nextGaussian() * 0.001f));
        float f5 = -((float)(n & 0x1FF) + f) / 512.0f;
        int n7 = LightTexture.pack((LightTexture.block(n6) * 3 + 15) / 4, (LightTexture.sky(n6) * 3 + 15) / 4);
        return new ColumnInstance(n2, n5, n3, n4, f3, f5 + f4, n7);
    }

    private void renderInstances(VertexConsumer vertexConsumer, List<ColumnInstance> list, Vec3 vec3, float f, int n, float f2) {
        for (ColumnInstance columnInstance : list) {
            float f3 = (float)((double)columnInstance.x + 0.5 - vec3.x);
            float f4 = (float)((double)columnInstance.z + 0.5 - vec3.z);
            float f5 = (float)Mth.lengthSquared(f3, f4);
            float f6 = Mth.lerp(f5 / (float)(n * n), f, 0.5f) * f2;
            int n2 = ARGB.white(f6);
            int n3 = (columnInstance.z - Mth.floor(vec3.z) + 16) * 32 + columnInstance.x - Mth.floor(vec3.x) + 16;
            float f7 = this.columnSizeX[n3] / 2.0f;
            float f8 = this.columnSizeZ[n3] / 2.0f;
            float f9 = f3 - f7;
            float f10 = f3 + f7;
            float f11 = (float)((double)columnInstance.topY - vec3.y);
            float f12 = (float)((double)columnInstance.bottomY - vec3.y);
            float f13 = f4 - f8;
            float f14 = f4 + f8;
            float f15 = columnInstance.uOffset + 0.0f;
            float f16 = columnInstance.uOffset + 1.0f;
            float f17 = (float)columnInstance.bottomY * 0.25f + columnInstance.vOffset;
            float f18 = (float)columnInstance.topY * 0.25f + columnInstance.vOffset;
            vertexConsumer.addVertex(f9, f11, f13).setUv(f15, f17).setColor(n2).setLight(columnInstance.lightCoords);
            vertexConsumer.addVertex(f10, f11, f14).setUv(f16, f17).setColor(n2).setLight(columnInstance.lightCoords);
            vertexConsumer.addVertex(f10, f12, f14).setUv(f16, f18).setColor(n2).setLight(columnInstance.lightCoords);
            vertexConsumer.addVertex(f9, f12, f13).setUv(f15, f18).setColor(n2).setLight(columnInstance.lightCoords);
        }
    }

    public void tickRainParticles(ClientLevel clientLevel, Camera camera, int n, ParticleStatus particleStatus) {
        float f = clientLevel.getRainLevel(1.0f) / (Minecraft.useFancyGraphics() ? 1.0f : 2.0f);
        if (f <= 0.0f) {
            return;
        }
        RandomSource randomSource = RandomSource.create((long)n * 312987231L);
        BlockPos blockPos = BlockPos.containing(camera.getPosition());
        Vec3i vec3i = null;
        int n2 = (int)(100.0f * f * f) / (particleStatus == ParticleStatus.DECREASED ? 2 : 1);
        for (int i = 0; i < n2; ++i) {
            int n3;
            int n4 = randomSource.nextInt(21) - 10;
            BlockPos blockPos2 = clientLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(n4, 0, n3 = randomSource.nextInt(21) - 10));
            if (blockPos2.getY() <= clientLevel.getMinY() || blockPos2.getY() > blockPos.getY() + 10 || blockPos2.getY() < blockPos.getY() - 10 || this.getPrecipitationAt(clientLevel, blockPos2) != Biome.Precipitation.RAIN) continue;
            vec3i = blockPos2.below();
            if (particleStatus == ParticleStatus.MINIMAL) break;
            double d = randomSource.nextDouble();
            double d2 = randomSource.nextDouble();
            BlockState blockState = clientLevel.getBlockState((BlockPos)vec3i);
            FluidState fluidState = clientLevel.getFluidState((BlockPos)vec3i);
            VoxelShape voxelShape = blockState.getCollisionShape(clientLevel, (BlockPos)vec3i);
            double d3 = voxelShape.max(Direction.Axis.Y, d, d2);
            double d4 = fluidState.getHeight(clientLevel, (BlockPos)vec3i);
            double d5 = Math.max(d3, d4);
            SimpleParticleType simpleParticleType = fluidState.is(FluidTags.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState) ? ParticleTypes.SMOKE : ParticleTypes.RAIN;
            clientLevel.addParticle(simpleParticleType, (double)vec3i.getX() + d, (double)vec3i.getY() + d5, (double)vec3i.getZ() + d2, 0.0, 0.0, 0.0);
        }
        if (vec3i != null && randomSource.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (vec3i.getY() > blockPos.getY() + 1 && clientLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor(blockPos.getY())) {
                clientLevel.playLocalSound((BlockPos)vec3i, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1f, 0.5f, false);
            } else {
                clientLevel.playLocalSound((BlockPos)vec3i, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2f, 1.0f, false);
            }
        }
    }

    private Biome.Precipitation getPrecipitationAt(Level level, BlockPos blockPos) {
        if (!level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
            return Biome.Precipitation.NONE;
        }
        Biome biome = level.getBiome(blockPos).value();
        return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
    }

    static final class ColumnInstance
    extends Record {
        final int x;
        final int z;
        final int bottomY;
        final int topY;
        final float uOffset;
        final float vOffset;
        final int lightCoords;

        ColumnInstance(int n, int n2, int n3, int n4, float f, float f2, int n5) {
            this.x = n;
            this.z = n2;
            this.bottomY = n3;
            this.topY = n4;
            this.uOffset = f;
            this.vOffset = f2;
            this.lightCoords = n5;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ColumnInstance.class, "x;z;bottomY;topY;uOffset;vOffset;lightCoords", "x", "z", "bottomY", "topY", "uOffset", "vOffset", "lightCoords"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ColumnInstance.class, "x;z;bottomY;topY;uOffset;vOffset;lightCoords", "x", "z", "bottomY", "topY", "uOffset", "vOffset", "lightCoords"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ColumnInstance.class, "x;z;bottomY;topY;uOffset;vOffset;lightCoords", "x", "z", "bottomY", "topY", "uOffset", "vOffset", "lightCoords"}, this, object);
        }

        public int x() {
            return this.x;
        }

        public int z() {
            return this.z;
        }

        public int bottomY() {
            return this.bottomY;
        }

        public int topY() {
            return this.topY;
        }

        public float uOffset() {
            return this.uOffset;
        }

        public float vOffset() {
            return this.vOffset;
        }

        public int lightCoords() {
            return this.lightCoords;
        }
    }
}

