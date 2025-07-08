/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFixUtils
 *  it.unimi.dsi.fastutil.longs.LongSets
 *  it.unimi.dsi.fastutil.longs.LongSets$EmptySet
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.BandwidthDebugChart;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.gui.components.debugchart.PingDebugChart;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class DebugScreenOverlay {
    private static final float CROSSHAIR_SCALE = 0.01f;
    private static final int CROSHAIR_INDEX_COUNT = 18;
    private static final int COLOR_GREY = -2039584;
    private static final int MARGIN_RIGHT = 2;
    private static final int MARGIN_LEFT = 2;
    private static final int MARGIN_TOP = 2;
    private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Maps.newEnumMap(Map.of(Heightmap.Types.WORLD_SURFACE_WG, "SW", Heightmap.Types.WORLD_SURFACE, "S", Heightmap.Types.OCEAN_FLOOR_WG, "OW", Heightmap.Types.OCEAN_FLOOR, "O", Heightmap.Types.MOTION_BLOCKING, "M", Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML"));
    private final Minecraft minecraft;
    private final AllocationRateCalculator allocationRateCalculator;
    private final Font font;
    private final GpuBuffer crosshairBuffer;
    private final RenderSystem.AutoStorageIndexBuffer crosshairIndicies = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
    private HitResult block;
    private HitResult liquid;
    @Nullable
    private ChunkPos lastPos;
    @Nullable
    private LevelChunk clientChunk;
    @Nullable
    private CompletableFuture<LevelChunk> serverChunk;
    private boolean renderDebug;
    private boolean renderProfilerChart;
    private boolean renderFpsCharts;
    private boolean renderNetworkCharts;
    private final LocalSampleLogger frameTimeLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger tickTimeLogger = new LocalSampleLogger(TpsDebugDimensions.values().length);
    private final LocalSampleLogger pingLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger bandwidthLogger = new LocalSampleLogger(1);
    private final Map<RemoteDebugSampleType, LocalSampleLogger> remoteSupportingLoggers = Map.of(RemoteDebugSampleType.TICK_TIME, this.tickTimeLogger);
    private final FpsDebugChart fpsChart;
    private final TpsDebugChart tpsChart;
    private final PingDebugChart pingChart;
    private final BandwidthDebugChart bandwidthChart;
    private final ProfilerPieChart profilerPieChart;

    public DebugScreenOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.allocationRateCalculator = new AllocationRateCalculator();
        this.font = minecraft.font;
        this.fpsChart = new FpsDebugChart(this.font, this.frameTimeLogger);
        this.tpsChart = new TpsDebugChart(this.font, this.tickTimeLogger, () -> Float.valueOf(minecraft.level.tickRateManager().millisecondsPerTick()));
        this.pingChart = new PingDebugChart(this.font, this.pingLogger);
        this.bandwidthChart = new BandwidthDebugChart(this.font, this.bandwidthLogger);
        this.profilerPieChart = new ProfilerPieChart(this.font);
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_COLOR_NORMAL.getVertexSize() * 12);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-65536).setNormal(1.0f, 0.0f, 0.0f);
            bufferBuilder.addVertex(1.0f, 0.0f, 0.0f).setColor(-65536).setNormal(1.0f, 0.0f, 0.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-16711936).setNormal(0.0f, 1.0f, 0.0f);
            bufferBuilder.addVertex(0.0f, 1.0f, 0.0f).setColor(-16711936).setNormal(0.0f, 1.0f, 0.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-8421377).setNormal(0.0f, 0.0f, 1.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 1.0f).setColor(-8421377).setNormal(0.0f, 0.0f, 1.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow();){
                this.crosshairBuffer = RenderSystem.getDevice().createBuffer(() -> "Crosshair vertex buffer", 32, meshData.vertexBuffer());
            }
        }
    }

    public void clearChunkCache() {
        this.serverChunk = null;
        this.clientChunk = null;
    }

    public void render(GuiGraphics guiGraphics) {
        int n;
        int n2;
        int n3;
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("debug");
        Entity entity = this.minecraft.getCameraEntity();
        this.block = entity.pick(20.0, 0.0f, false);
        this.liquid = entity.pick(20.0, 0.0f, true);
        this.drawGameInformation(guiGraphics);
        this.drawSystemInformation(guiGraphics);
        guiGraphics.nextStratum();
        this.profilerPieChart.setBottomOffset(10);
        if (this.renderFpsCharts) {
            n3 = guiGraphics.guiWidth();
            n2 = n3 / 2;
            this.fpsChart.drawChart(guiGraphics, 0, this.fpsChart.getWidth(n2));
            if (this.tickTimeLogger.size() > 0) {
                n = this.tpsChart.getWidth(n2);
                this.tpsChart.drawChart(guiGraphics, n3 - n, n);
            }
            this.profilerPieChart.setBottomOffset(this.tpsChart.getFullHeight());
        }
        if (this.renderNetworkCharts) {
            n3 = guiGraphics.guiWidth();
            n2 = n3 / 2;
            if (!this.minecraft.isLocalServer()) {
                this.bandwidthChart.drawChart(guiGraphics, 0, this.bandwidthChart.getWidth(n2));
            }
            n = this.pingChart.getWidth(n2);
            this.pingChart.drawChart(guiGraphics, n3 - n, n);
            this.profilerPieChart.setBottomOffset(this.pingChart.getFullHeight());
        }
        try (Zone zone = profilerFiller.zone("profilerPie");){
            this.profilerPieChart.render(guiGraphics);
        }
        profilerFiller.pop();
    }

    protected void drawGameInformation(GuiGraphics guiGraphics) {
        List<String> list = this.getGameInformation();
        list.add("");
        boolean bl = this.minecraft.getSingleplayerServer() != null;
        list.add("Debug charts: [F3+1] Profiler " + (this.renderProfilerChart ? "visible" : "hidden") + "; [F3+2] " + (bl ? "FPS + TPS " : "FPS ") + (this.renderFpsCharts ? "visible" : "hidden") + "; [F3+3] " + (!this.minecraft.isLocalServer() ? "Bandwidth + Ping" : "Ping") + (this.renderNetworkCharts ? " visible" : " hidden"));
        list.add("For help: press F3 + Q");
        this.renderLines(guiGraphics, list, true);
    }

    protected void drawSystemInformation(GuiGraphics guiGraphics) {
        List<String> list = this.getSystemInformation();
        this.renderLines(guiGraphics, list, false);
    }

    private void renderLines(GuiGraphics guiGraphics, List<String> list, boolean bl) {
        int n;
        int n2;
        int n3;
        String string;
        int n4;
        int n5 = this.font.lineHeight;
        for (n4 = 0; n4 < list.size(); ++n4) {
            string = list.get(n4);
            if (Strings.isNullOrEmpty((String)string)) continue;
            n3 = this.font.width(string);
            n2 = bl ? 2 : guiGraphics.guiWidth() - 2 - n3;
            n = 2 + n5 * n4;
            guiGraphics.fill(n2 - 1, n - 1, n2 + n3 + 1, n + n5 - 1, -1873784752);
        }
        for (n4 = 0; n4 < list.size(); ++n4) {
            string = list.get(n4);
            if (Strings.isNullOrEmpty((String)string)) continue;
            n3 = this.font.width(string);
            n2 = bl ? 2 : guiGraphics.guiWidth() - 2 - n3;
            n = 2 + n5 * n4;
            guiGraphics.drawString(this.font, string, n2, n, -2039584, false);
        }
    }

    protected List<String> getGameInformation() {
        ResourceLocation resourceLocation;
        Object object;
        Object object2;
        Level level;
        String string;
        Object object3;
        Object object4;
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        Connection connection = clientPacketListener.getConnection();
        float f = connection.getAverageSentPackets();
        float f2 = connection.getAverageReceivedPackets();
        TickRateManager tickRateManager = this.getLevel().tickRateManager();
        String string2 = tickRateManager.isSteppingForward() ? " (frozen - stepping)" : (tickRateManager.isFrozen() ? " (frozen)" : "");
        if (integratedServer != null) {
            object4 = integratedServer.tickRateManager();
            boolean bl = ((ServerTickRateManager)object4).isSprinting();
            if (bl) {
                string2 = " (sprinting)";
            }
            object3 = bl ? "-" : String.format(Locale.ROOT, "%.1f", Float.valueOf(tickRateManager.millisecondsPerTick()));
            string = String.format(Locale.ROOT, "Integrated server @ %.1f/%s ms%s, %.0f tx, %.0f rx", Float.valueOf(integratedServer.getCurrentSmoothedTickTime()), object3, string2, Float.valueOf(f), Float.valueOf(f2));
        } else {
            string = String.format(Locale.ROOT, "\"%s\" server%s, %.0f tx, %.0f rx", clientPacketListener.serverBrand(), string2, Float.valueOf(f), Float.valueOf(f2));
        }
        object4 = this.minecraft.getCameraEntity().blockPosition();
        if (this.minecraft.showOnlyReducedInfo()) {
            return Lists.newArrayList((Object[])new String[]{"Minecraft " + SharedConstants.getCurrentVersion().name() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.minecraft.fpsString, string, this.minecraft.levelRenderer.getSectionStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats(), "", String.format(Locale.ROOT, "Chunk-relative: %d %d %d", ((Vec3i)object4).getX() & 0xF, ((Vec3i)object4).getY() & 0xF, ((Vec3i)object4).getZ() & 0xF)});
        }
        Entity entity = this.minecraft.getCameraEntity();
        object3 = entity.getDirection();
        String string3 = switch (1.$SwitchMap$net$minecraft$core$Direction[((Enum)object3).ordinal()]) {
            case 1 -> "Towards negative Z";
            case 2 -> "Towards positive Z";
            case 3 -> "Towards negative X";
            case 4 -> "Towards positive X";
            default -> "Invalid";
        };
        ChunkPos chunkPos = new ChunkPos((BlockPos)object4);
        if (!Objects.equals(this.lastPos, chunkPos)) {
            this.lastPos = chunkPos;
            this.clearChunkCache();
        }
        LongSets.EmptySet emptySet = (level = this.getLevel()) instanceof ServerLevel ? ((ServerLevel)level).getForceLoadedChunks() : LongSets.EMPTY_SET;
        ArrayList arrayList = Lists.newArrayList((Object[])new String[]{"Minecraft " + SharedConstants.getCurrentVersion().name() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + (String)("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")", this.minecraft.fpsString, string, this.minecraft.levelRenderer.getSectionStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats()});
        String string4 = this.getServerChunkStats();
        if (string4 != null) {
            arrayList.add(string4);
        }
        arrayList.add(String.valueOf(this.minecraft.level.dimension().location()) + " FC: " + emptySet.size());
        arrayList.add("");
        arrayList.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY(), this.minecraft.getCameraEntity().getZ()));
        arrayList.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", ((Vec3i)object4).getX(), ((Vec3i)object4).getY(), ((Vec3i)object4).getZ(), ((Vec3i)object4).getX() & 0xF, ((Vec3i)object4).getY() & 0xF, ((Vec3i)object4).getZ() & 0xF));
        arrayList.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkPos.x, SectionPos.blockToSectionCoord(((Vec3i)object4).getY()), chunkPos.z, chunkPos.getRegionLocalX(), chunkPos.getRegionLocalZ(), chunkPos.getRegionX(), chunkPos.getRegionZ()));
        arrayList.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", object3, string3, Float.valueOf(Mth.wrapDegrees(entity.getYRot())), Float.valueOf(Mth.wrapDegrees(entity.getXRot()))));
        LevelChunk levelChunk = this.getClientChunk();
        if (levelChunk.isEmpty()) {
            arrayList.add("Waiting for chunk...");
        } else {
            int n = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness((BlockPos)object4, 0);
            int n2 = this.minecraft.level.getBrightness(LightLayer.SKY, (BlockPos)object4);
            int n3 = this.minecraft.level.getBrightness(LightLayer.BLOCK, (BlockPos)object4);
            arrayList.add("Client Light: " + n + " (" + n2 + " sky, " + n3 + " block)");
            object2 = this.getServerChunk();
            object = new StringBuilder("CH");
            for (Heightmap.Types object5 : Heightmap.Types.values()) {
                if (!object5.sendToClient()) continue;
                ((StringBuilder)object).append(" ").append(HEIGHTMAP_NAMES.get(object5)).append(": ").append(levelChunk.getHeight(object5, ((Vec3i)object4).getX(), ((Vec3i)object4).getZ()));
            }
            arrayList.add(((StringBuilder)object).toString());
            ((StringBuilder)object).setLength(0);
            ((StringBuilder)object).append("SH");
            for (Heightmap.Types types : Heightmap.Types.values()) {
                if (!types.keepAfterWorldgen()) continue;
                ((StringBuilder)object).append(" ").append(HEIGHTMAP_NAMES.get(types)).append(": ");
                if (object2 != null) {
                    ((StringBuilder)object).append(((ChunkAccess)object2).getHeight(types, ((Vec3i)object4).getX(), ((Vec3i)object4).getZ()));
                    continue;
                }
                ((StringBuilder)object).append("??");
            }
            arrayList.add(((StringBuilder)object).toString());
            if (this.minecraft.level.isInsideBuildHeight(((Vec3i)object4).getY())) {
                arrayList.add("Biome: " + DebugScreenOverlay.printBiome(this.minecraft.level.getBiome((BlockPos)object4)));
                if (object2 != null) {
                    float f3 = level.getMoonBrightness();
                    long l = ((ChunkAccess)object2).getInhabitedTime();
                    DifficultyInstance difficultyInstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, f3);
                    arrayList.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", Float.valueOf(difficultyInstance.getEffectiveDifficulty()), Float.valueOf(difficultyInstance.getSpecialMultiplier()), this.minecraft.level.getDayTime() / 24000L));
                } else {
                    arrayList.add("Local Difficulty: ??");
                }
            }
            if (object2 != null && ((ChunkAccess)object2).isOldNoiseGeneration()) {
                arrayList.add("Blending: Old");
            }
        }
        ServerLevel serverLevel = this.getServerLevel();
        if (serverLevel != null) {
            ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
            ChunkGenerator chunkGenerator = serverChunkCache.getGenerator();
            object2 = serverChunkCache.randomState();
            chunkGenerator.addDebugScreenInfo(arrayList, (RandomState)object2, (BlockPos)object4);
            object = ((RandomState)object2).sampler();
            BiomeSource biomeSource = chunkGenerator.getBiomeSource();
            biomeSource.addDebugInfo(arrayList, (BlockPos)object4, (Climate.Sampler)object);
            NaturalSpawner.SpawnState spawnState = serverChunkCache.getLastSpawnState();
            if (spawnState != null) {
                Object2IntMap<MobCategory> object2IntMap = spawnState.getMobCategoryCounts();
                int n = spawnState.getSpawnableChunkCount();
                arrayList.add("SC: " + n + ", " + Stream.of(MobCategory.values()).map(mobCategory -> Character.toUpperCase(mobCategory.getName().charAt(0)) + ": " + object2IntMap.getInt(mobCategory)).collect(Collectors.joining(", ")));
            } else {
                arrayList.add("SC: N/A");
            }
        }
        if ((resourceLocation = this.minecraft.gameRenderer.currentPostEffect()) != null) {
            arrayList.add("Post: " + String.valueOf(resourceLocation));
        }
        arrayList.add(this.minecraft.getSoundManager().getDebugString() + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0f)));
        return arrayList;
    }

    private static String printBiome(Holder<Biome> holder) {
        return (String)holder.unwrap().map(resourceKey -> resourceKey.location().toString(), biome -> "[unregistered " + String.valueOf(biome) + "]");
    }

    @Nullable
    private ServerLevel getServerLevel() {
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        if (integratedServer != null) {
            return integratedServer.getLevel(this.minecraft.level.dimension());
        }
        return null;
    }

    @Nullable
    private String getServerChunkStats() {
        ServerLevel serverLevel = this.getServerLevel();
        if (serverLevel != null) {
            return serverLevel.gatherChunkSourceStats();
        }
        return null;
    }

    private Level getLevel() {
        return (Level)DataFixUtils.orElse(Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap(integratedServer -> Optional.ofNullable(integratedServer.getLevel(this.minecraft.level.dimension()))), (Object)this.minecraft.level);
    }

    @Nullable
    private LevelChunk getServerChunk() {
        if (this.serverChunk == null) {
            ServerLevel serverLevel = this.getServerLevel();
            if (serverLevel == null) {
                return null;
            }
            this.serverChunk = serverLevel.getChunkSource().getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false).thenApply(chunkResult -> chunkResult.orElse(null));
        }
        return this.serverChunk.getNow(null);
    }

    private LevelChunk getClientChunk() {
        if (this.clientChunk == null) {
            this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
        }
        return this.clientChunk;
    }

    protected List<String> getSystemInformation() {
        StateHolder stateHolder;
        Object object;
        long l = Runtime.getRuntime().maxMemory();
        long l2 = Runtime.getRuntime().totalMemory();
        long l3 = Runtime.getRuntime().freeMemory();
        long l4 = l2 - l3;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        ArrayList arrayList = Lists.newArrayList((Object[])new String[]{String.format(Locale.ROOT, "Java: %s", System.getProperty("java.version")), String.format(Locale.ROOT, "Mem: %2d%% %03d/%03dMB", l4 * 100L / l, DebugScreenOverlay.bytesToMegabytes(l4), DebugScreenOverlay.bytesToMegabytes(l)), String.format(Locale.ROOT, "Allocation rate: %03dMB/s", DebugScreenOverlay.bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(l4))), String.format(Locale.ROOT, "Allocated: %2d%% %03dMB", l2 * 100L / l, DebugScreenOverlay.bytesToMegabytes(l2)), "", String.format(Locale.ROOT, "CPU: %s", GLX._getCpuInfo()), "", String.format(Locale.ROOT, "Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), gpuDevice.getVendor()), gpuDevice.getRenderer(), String.format(Locale.ROOT, "%s %s", gpuDevice.getBackendName(), gpuDevice.getVersion())});
        if (this.minecraft.showOnlyReducedInfo()) {
            return arrayList;
        }
        if (this.block.getType() == HitResult.Type.BLOCK) {
            object = ((BlockHitResult)this.block).getBlockPos();
            stateHolder = this.minecraft.level.getBlockState((BlockPos)object);
            arrayList.add("");
            arrayList.add(String.valueOf(ChatFormatting.UNDERLINE) + "Targeted Block: " + ((Vec3i)object).getX() + ", " + ((Vec3i)object).getY() + ", " + ((Vec3i)object).getZ());
            arrayList.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(((BlockBehaviour.BlockStateBase)stateHolder).getBlock())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : stateHolder.getValues().entrySet()) {
                arrayList.add(this.getPropertyValueString(entry));
            }
            ((BlockBehaviour.BlockStateBase)stateHolder).getTags().map(tagKey -> "#" + String.valueOf(tagKey.location())).forEach(arrayList::add);
        }
        if (this.liquid.getType() == HitResult.Type.BLOCK) {
            object = ((BlockHitResult)this.liquid).getBlockPos();
            stateHolder = this.minecraft.level.getFluidState((BlockPos)object);
            arrayList.add("");
            arrayList.add(String.valueOf(ChatFormatting.UNDERLINE) + "Targeted Fluid: " + ((Vec3i)object).getX() + ", " + ((Vec3i)object).getY() + ", " + ((Vec3i)object).getZ());
            arrayList.add(String.valueOf(BuiltInRegistries.FLUID.getKey(((FluidState)stateHolder).getType())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : stateHolder.getValues().entrySet()) {
                arrayList.add(this.getPropertyValueString(entry));
            }
            ((FluidState)stateHolder).getTags().map(tagKey -> "#" + String.valueOf(tagKey.location())).forEach(arrayList::add);
        }
        if ((object = this.minecraft.crosshairPickEntity) != null) {
            arrayList.add("");
            arrayList.add(String.valueOf(ChatFormatting.UNDERLINE) + "Targeted Entity");
            arrayList.add(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(((Entity)object).getType())));
        }
        return arrayList;
    }

    private String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
        Property<?> property = entry.getKey();
        Comparable<?> comparable = entry.getValue();
        Object object = Util.getPropertyName(property, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            object = String.valueOf(ChatFormatting.GREEN) + (String)object;
        } else if (Boolean.FALSE.equals(comparable)) {
            object = String.valueOf(ChatFormatting.RED) + (String)object;
        }
        return property.getName() + ": " + (String)object;
    }

    private static long bytesToMegabytes(long l) {
        return l / 1024L / 1024L;
    }

    public boolean showDebugScreen() {
        return this.renderDebug && !this.minecraft.options.hideGui;
    }

    public boolean showProfilerChart() {
        return this.showDebugScreen() && this.renderProfilerChart;
    }

    public boolean showNetworkCharts() {
        return this.showDebugScreen() && this.renderNetworkCharts;
    }

    public boolean showFpsCharts() {
        return this.showDebugScreen() && this.renderFpsCharts;
    }

    public void toggleOverlay() {
        this.renderDebug = !this.renderDebug;
    }

    public void toggleNetworkCharts() {
        boolean bl = this.renderNetworkCharts = !this.renderDebug || !this.renderNetworkCharts;
        if (this.renderNetworkCharts) {
            this.renderDebug = true;
            this.renderFpsCharts = false;
        }
    }

    public void toggleFpsCharts() {
        boolean bl = this.renderFpsCharts = !this.renderDebug || !this.renderFpsCharts;
        if (this.renderFpsCharts) {
            this.renderDebug = true;
            this.renderNetworkCharts = false;
        }
    }

    public void toggleProfilerChart() {
        boolean bl = this.renderProfilerChart = !this.renderDebug || !this.renderProfilerChart;
        if (this.renderProfilerChart) {
            this.renderDebug = true;
        }
    }

    public void logFrameDuration(long l) {
        this.frameTimeLogger.logSample(l);
    }

    public LocalSampleLogger getTickTimeLogger() {
        return this.tickTimeLogger;
    }

    public LocalSampleLogger getPingLogger() {
        return this.pingLogger;
    }

    public LocalSampleLogger getBandwidthLogger() {
        return this.bandwidthLogger;
    }

    public ProfilerPieChart getProfilerPieChart() {
        return this.profilerPieChart;
    }

    public void logRemoteSample(long[] lArray, RemoteDebugSampleType remoteDebugSampleType) {
        LocalSampleLogger localSampleLogger = this.remoteSupportingLoggers.get((Object)remoteDebugSampleType);
        if (localSampleLogger != null) {
            localSampleLogger.logFullSample(lArray);
        }
    }

    public void reset() {
        this.renderDebug = false;
        this.tickTimeLogger.reset();
        this.pingLogger.reset();
        this.bandwidthLogger.reset();
    }

    public void render3dCrosshair(Camera camera) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translate(0.0f, 0.0f, -1.0f);
        matrix4fStack.rotateX(camera.getXRot() * ((float)Math.PI / 180));
        matrix4fStack.rotateY(camera.getYRot() * ((float)Math.PI / 180));
        float f = 0.01f * (float)this.minecraft.getWindow().getGuiScale();
        matrix4fStack.scale(-f, f, -f);
        RenderPipeline renderPipeline = RenderPipelines.LINES;
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView gpuTextureView = renderTarget.getColorTextureView();
        GpuTextureView gpuTextureView2 = renderTarget.getDepthTextureView();
        GpuBuffer gpuBuffer = this.crosshairIndicies.getBuffer(18);
        GpuBufferSlice[] gpuBufferSliceArray = RenderSystem.getDynamicUniforms().writeTransforms(new DynamicUniforms.Transform((Matrix4fc)new Matrix4f((Matrix4fc)matrix4fStack), (Vector4fc)new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 4.0f), new DynamicUniforms.Transform((Matrix4fc)new Matrix4f((Matrix4fc)matrix4fStack), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 2.0f));
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "3d crosshair", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setVertexBuffer(0, this.crosshairBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.crosshairIndicies.type());
            renderPass.setUniform("DynamicTransforms", gpuBufferSliceArray[0]);
            renderPass.drawIndexed(0, 0, 18, 1);
            renderPass.setUniform("DynamicTransforms", gpuBufferSliceArray[1]);
            renderPass.drawIndexed(0, 0, 18, 1);
        }
        matrix4fStack.popMatrix();
    }

    static class AllocationRateCalculator {
        private static final int UPDATE_INTERVAL_MS = 500;
        private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
        private long lastTime = 0L;
        private long lastHeapUsage = -1L;
        private long lastGcCounts = -1L;
        private long lastRate = 0L;

        AllocationRateCalculator() {
        }

        long bytesAllocatedPerSecond(long l) {
            long l2 = System.currentTimeMillis();
            if (l2 - this.lastTime < 500L) {
                return this.lastRate;
            }
            long l3 = AllocationRateCalculator.gcCounts();
            if (this.lastTime != 0L && l3 == this.lastGcCounts) {
                double d = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(l2 - this.lastTime);
                long l4 = l - this.lastHeapUsage;
                this.lastRate = Math.round((double)l4 * d);
            }
            this.lastTime = l2;
            this.lastHeapUsage = l;
            this.lastGcCounts = l3;
            return this.lastRate;
        }

        private static long gcCounts() {
            long l = 0L;
            for (GarbageCollectorMXBean garbageCollectorMXBean : GC_MBEANS) {
                l += garbageCollectorMXBean.getCollectionCount();
            }
            return l;
        }
    }
}

