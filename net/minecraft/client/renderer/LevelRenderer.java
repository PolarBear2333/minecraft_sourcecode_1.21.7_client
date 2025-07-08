/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.slf4j.Logger;

public class LevelRenderer
implements ResourceManagerReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TRANSPARENCY_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("transparency");
    private static final ResourceLocation ENTITY_OUTLINE_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("entity_outline");
    public static final int SECTION_SIZE = 16;
    public static final int HALF_SECTION_SIZE = 8;
    public static final int NEARBY_SECTION_DISTANCE_IN_BLOCKS = 32;
    private static final int MINIMUM_TRANSPARENT_SORT_COUNT = 15;
    private static final Comparator<Entity> ENTITY_COMPARATOR = Comparator.comparing(entity -> entity.getType().hashCode());
    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private final SkyRenderer skyRenderer = new SkyRenderer();
    private final CloudRenderer cloudRenderer = new CloudRenderer();
    private final WorldBorderRenderer worldBorderRenderer = new WorldBorderRenderer();
    private final WeatherEffectRenderer weatherEffectRenderer = new WeatherEffectRenderer();
    @Nullable
    private ClientLevel level;
    private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList(10000);
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections = new ObjectArrayList(50);
    @Nullable
    private ViewArea viewArea;
    private int ticks;
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap();
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap();
    @Nullable
    private RenderTarget entityOutlineTarget;
    private final LevelTargetBundle targets = new LevelTargetBundle();
    private int lastCameraSectionX = Integer.MIN_VALUE;
    private int lastCameraSectionY = Integer.MIN_VALUE;
    private int lastCameraSectionZ = Integer.MIN_VALUE;
    private double prevCamX = Double.MIN_VALUE;
    private double prevCamY = Double.MIN_VALUE;
    private double prevCamZ = Double.MIN_VALUE;
    private double prevCamRotX = Double.MIN_VALUE;
    private double prevCamRotY = Double.MIN_VALUE;
    @Nullable
    private SectionRenderDispatcher sectionRenderDispatcher;
    private int lastViewDistance = -1;
    private final List<Entity> visibleEntities = new ArrayList<Entity>();
    private int visibleEntityCount;
    private Frustum cullingFrustum;
    private boolean captureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    @Nullable
    private BlockPos lastTranslucentSortBlockPos;
    private int translucencyResortIterationIndex;

    public LevelRenderer(Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers) {
        this.minecraft = minecraft;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.renderBuffers = renderBuffers;
    }

    public void tickParticles(Camera camera) {
        this.weatherEffectRenderer.tickRainParticles(this.minecraft.level, camera, this.ticks, this.minecraft.options.particles().get());
    }

    @Override
    public void close() {
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.destroyBuffers();
        }
        this.skyRenderer.close();
        this.cloudRenderer.close();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.initOutline();
    }

    public void initOutline() {
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.destroyBuffers();
        }
        this.entityOutlineTarget = new TextureTarget("Entity Outline", this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), true);
    }

    @Nullable
    private PostChain getTransparencyChain() {
        if (!Minecraft.useShaderTransparency()) {
            return null;
        }
        PostChain postChain = this.minecraft.getShaderManager().getPostChain(TRANSPARENCY_POST_CHAIN_ID, LevelTargetBundle.SORTING_TARGETS);
        if (postChain == null) {
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
            this.minecraft.options.save();
        }
        return postChain;
    }

    public void doEntityOutline() {
        if (this.shouldShowEntityOutlines()) {
            this.entityOutlineTarget.blitAndBlendToTexture(this.minecraft.getMainRenderTarget().getColorTextureView());
        }
    }

    protected boolean shouldShowEntityOutlines() {
        return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityOutlineTarget != null && this.minecraft.player != null;
    }

    public void setLevel(@Nullable ClientLevel clientLevel) {
        this.lastCameraSectionX = Integer.MIN_VALUE;
        this.lastCameraSectionY = Integer.MIN_VALUE;
        this.lastCameraSectionZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setLevel(clientLevel);
        this.level = clientLevel;
        if (clientLevel != null) {
            this.allChanged();
        } else {
            if (this.viewArea != null) {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }
            if (this.sectionRenderDispatcher != null) {
                this.sectionRenderDispatcher.dispose();
            }
            this.sectionRenderDispatcher = null;
            this.sectionOcclusionGraph.waitAndReset(null);
            this.clearVisibleSections();
        }
    }

    private void clearVisibleSections() {
        this.visibleSections.clear();
        this.nearbyVisibleSections.clear();
    }

    public void allChanged() {
        if (this.level == null) {
            return;
        }
        this.level.clearTintCaches();
        if (this.sectionRenderDispatcher == null) {
            this.sectionRenderDispatcher = new SectionRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.renderBuffers, this.minecraft.getBlockRenderer(), this.minecraft.getBlockEntityRenderDispatcher());
        } else {
            this.sectionRenderDispatcher.setLevel(this.level);
        }
        this.cloudRenderer.markForRebuild();
        ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
        this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
        if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
        }
        this.sectionRenderDispatcher.clearCompileQueue();
        this.viewArea = new ViewArea(this.sectionRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
        this.sectionOcclusionGraph.waitAndReset(this.viewArea);
        this.clearVisibleSections();
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        this.viewArea.repositionCamera(SectionPos.of(camera.getPosition()));
    }

    public void resize(int n, int n2) {
        this.needsUpdate();
        if (this.entityOutlineTarget != null) {
            this.entityOutlineTarget.resize(n, n2);
        }
    }

    public String getSectionStatistics() {
        int n = this.viewArea.sections.length;
        int n2 = this.countRenderedSections();
        return String.format(Locale.ROOT, "C: %d/%d %sD: %d, %s", n2, n, this.minecraft.smartCull ? "(s) " : "", this.lastViewDistance, this.sectionRenderDispatcher == null ? "null" : this.sectionRenderDispatcher.getStats());
    }

    public SectionRenderDispatcher getSectionRenderDispatcher() {
        return this.sectionRenderDispatcher;
    }

    public double getTotalSections() {
        return this.viewArea.sections.length;
    }

    public double getLastViewDistance() {
        return this.lastViewDistance;
    }

    public int countRenderedSections() {
        int n = 0;
        for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
            if (!renderSection.getSectionMesh().hasRenderableLayers()) continue;
            ++n;
        }
        return n;
    }

    public String getEntityStatistics() {
        return "E: " + this.visibleEntityCount + "/" + this.level.getEntityCount() + ", SD: " + this.level.getServerSimulationDistance();
    }

    private void setupRender(Camera camera, Frustum frustum, boolean bl, boolean bl2) {
        Vec3 vec3 = camera.getPosition();
        if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
            this.allChanged();
        }
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("camera");
        int n = SectionPos.posToSectionCoord(vec3.x());
        int n2 = SectionPos.posToSectionCoord(vec3.y());
        int n3 = SectionPos.posToSectionCoord(vec3.z());
        if (this.lastCameraSectionX != n || this.lastCameraSectionY != n2 || this.lastCameraSectionZ != n3) {
            this.lastCameraSectionX = n;
            this.lastCameraSectionY = n2;
            this.lastCameraSectionZ = n3;
            this.viewArea.repositionCamera(SectionPos.of(vec3));
            this.worldBorderRenderer.invalidate();
        }
        this.sectionRenderDispatcher.setCameraPosition(vec3);
        profilerFiller.popPush("cull");
        double d = Math.floor(vec3.x / 8.0);
        double d2 = Math.floor(vec3.y / 8.0);
        double d3 = Math.floor(vec3.z / 8.0);
        if (d != this.prevCamX || d2 != this.prevCamY || d3 != this.prevCamZ) {
            this.sectionOcclusionGraph.invalidate();
        }
        this.prevCamX = d;
        this.prevCamY = d2;
        this.prevCamZ = d3;
        profilerFiller.popPush("update");
        if (!bl) {
            boolean bl3 = this.minecraft.smartCull;
            if (bl2 && this.level.getBlockState(camera.getBlockPosition()).isSolidRender()) {
                bl3 = false;
            }
            profilerFiller.push("section_occlusion_graph");
            this.sectionOcclusionGraph.update(bl3, camera, frustum, (List<SectionRenderDispatcher.RenderSection>)this.visibleSections, this.level.getChunkSource().getLoadedEmptySections());
            profilerFiller.pop();
            double d4 = Math.floor(camera.getXRot() / 2.0f);
            double d5 = Math.floor(camera.getYRot() / 2.0f);
            if (this.sectionOcclusionGraph.consumeFrustumUpdate() || d4 != this.prevCamRotX || d5 != this.prevCamRotY) {
                this.applyFrustum(LevelRenderer.offsetFrustum(frustum));
                this.prevCamRotX = d4;
                this.prevCamRotY = d5;
            }
        }
        profilerFiller.pop();
    }

    public static Frustum offsetFrustum(Frustum frustum) {
        return new Frustum(frustum).offsetToFullyIncludeCameraCube(8);
    }

    private void applyFrustum(Frustum frustum) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        }
        Profiler.get().push("apply_frustum");
        this.clearVisibleSections();
        this.sectionOcclusionGraph.addSectionsInFrustum(frustum, (List<SectionRenderDispatcher.RenderSection>)this.visibleSections, (List<SectionRenderDispatcher.RenderSection>)this.nearbyVisibleSections);
        Profiler.get().pop();
    }

    public void addRecentlyCompiledSection(SectionRenderDispatcher.RenderSection renderSection) {
        this.sectionOcclusionGraph.schedulePropagationFrom(renderSection);
    }

    public void prepareCullFrustum(Vec3 vec3, Matrix4f matrix4f, Matrix4f matrix4f2) {
        this.cullingFrustum = new Frustum(matrix4f, matrix4f2);
        this.cullingFrustum.prepare(vec3.x(), vec3.y(), vec3.z());
    }

    public void renderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2) {
        Optional<Integer> optional;
        float f = deltaTracker.getGameTimeDeltaPartialTick(false);
        this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
        final ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("light_update_queue");
        this.level.pollLightUpdates();
        profilerFiller.popPush("light_updates");
        this.level.getChunkSource().getLightEngine().runLightUpdates();
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        profilerFiller.popPush("culling");
        boolean bl3 = this.capturedFrustum != null;
        Frustum frustum = bl3 ? this.capturedFrustum : this.cullingFrustum;
        profilerFiller.popPush("captureFrustum");
        if (this.captureFrustum) {
            this.capturedFrustum = bl3 ? new Frustum(matrix4f, matrix4f2) : frustum;
            this.capturedFrustum.prepare(d, d2, d3);
            this.captureFrustum = false;
        }
        profilerFiller.popPush("cullEntities");
        boolean bl4 = this.collectVisibleEntities(camera, frustum, this.visibleEntities);
        this.visibleEntityCount = this.visibleEntities.size();
        profilerFiller.popPush("terrain_setup");
        this.setupRender(camera, frustum, bl3, this.minecraft.player.isSpectator());
        profilerFiller.popPush("compile_sections");
        this.compileSections(camera);
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)matrix4f);
        FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
        this.targets.main = frameGraphBuilder.importExternal("main", this.minecraft.getMainRenderTarget());
        int n = this.minecraft.getMainRenderTarget().width;
        int n2 = this.minecraft.getMainRenderTarget().height;
        RenderTargetDescriptor renderTargetDescriptor = new RenderTargetDescriptor(n, n2, true, 0);
        PostChain postChain = this.getTransparencyChain();
        if (postChain != null) {
            this.targets.translucent = frameGraphBuilder.createInternal("translucent", renderTargetDescriptor);
            this.targets.itemEntity = frameGraphBuilder.createInternal("item_entity", renderTargetDescriptor);
            this.targets.particles = frameGraphBuilder.createInternal("particles", renderTargetDescriptor);
            this.targets.weather = frameGraphBuilder.createInternal("weather", renderTargetDescriptor);
            this.targets.clouds = frameGraphBuilder.createInternal("clouds", renderTargetDescriptor);
        }
        if (this.entityOutlineTarget != null) {
            this.targets.entityOutline = frameGraphBuilder.importExternal("entity_outline", this.entityOutlineTarget);
        }
        FramePass framePass = frameGraphBuilder.addPass("clear");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        framePass.executes(() -> {
            RenderTarget renderTarget = this.minecraft.getMainRenderTarget();
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), ARGB.colorFromFloat(0.0f, vector4f.x, vector4f.y, vector4f.z), renderTarget.getDepthTexture(), 1.0);
        });
        if (bl2) {
            this.addSkyPass(frameGraphBuilder, camera, f, gpuBufferSlice);
        }
        this.addMainPass(frameGraphBuilder, frustum, camera, matrix4f, gpuBufferSlice, bl, bl4, deltaTracker, profilerFiller);
        PostChain postChain2 = this.minecraft.getShaderManager().getPostChain(ENTITY_OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS);
        if (bl4 && postChain2 != null) {
            postChain2.addToFrame(frameGraphBuilder, n, n2, this.targets);
        }
        this.addParticlesPass(frameGraphBuilder, camera, f, gpuBufferSlice);
        CloudStatus cloudStatus = this.minecraft.options.getCloudsType();
        if (cloudStatus != CloudStatus.OFF && (optional = this.level.dimensionType().cloudHeight()).isPresent()) {
            float f2 = (float)this.ticks + f;
            int n3 = this.level.getCloudColor(f);
            this.addCloudsPass(frameGraphBuilder, cloudStatus, camera.getPosition(), f2, n3, (float)optional.get().intValue() + 0.33f);
        }
        this.addWeatherPass(frameGraphBuilder, camera.getPosition(), f, gpuBufferSlice);
        if (postChain != null) {
            postChain.addToFrame(frameGraphBuilder, n, n2, this.targets);
        }
        this.addLateDebugPass(frameGraphBuilder, vec3, gpuBufferSlice);
        profilerFiller.popPush("framegraph");
        frameGraphBuilder.execute(graphicsResourceAllocator, new FrameGraphBuilder.Inspector(){

            @Override
            public void beforeExecutePass(String string) {
                profilerFiller.push(string);
            }

            @Override
            public void afterExecutePass(String string) {
                profilerFiller.pop();
            }
        });
        this.visibleEntities.clear();
        this.targets.clear();
        matrix4fStack.popMatrix();
        profilerFiller.pop();
    }

    private void addMainPass(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera, Matrix4f matrix4f, GpuBufferSlice gpuBufferSlice, boolean bl, boolean bl2, DeltaTracker deltaTracker, ProfilerFiller profilerFiller) {
        FramePass framePass = frameGraphBuilder.addPass("main");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        if (this.targets.translucent != null) {
            this.targets.translucent = framePass.readsAndWrites(this.targets.translucent);
        }
        if (this.targets.itemEntity != null) {
            this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
        }
        if (this.targets.weather != null) {
            this.targets.weather = framePass.readsAndWrites(this.targets.weather);
        }
        if (bl2 && this.targets.entityOutline != null) {
            this.targets.entityOutline = framePass.readsAndWrites(this.targets.entityOutline);
        }
        ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
        ResourceHandle<RenderTarget> resourceHandle2 = this.targets.translucent;
        ResourceHandle<RenderTarget> resourceHandle3 = this.targets.itemEntity;
        ResourceHandle<RenderTarget> resourceHandle4 = this.targets.entityOutline;
        framePass.executes(() -> {
            Object object;
            RenderSystem.setShaderFog(gpuBufferSlice);
            float f = deltaTracker.getGameTimeDeltaPartialTick(false);
            Vec3 vec3 = camera.getPosition();
            double d = vec3.x();
            double d2 = vec3.y();
            double d3 = vec3.z();
            profilerFiller.push("terrain");
            ChunkSectionsToRender chunkSectionsToRender = this.prepareChunkRenders((Matrix4fc)matrix4f, d, d2, d3);
            chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.OPAQUE);
            this.minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
            if (resourceHandle3 != null) {
                ((RenderTarget)resourceHandle3.get()).copyDepthFrom(this.minecraft.getMainRenderTarget());
            }
            if (this.shouldShowEntityOutlines() && resourceHandle4 != null) {
                object = (RenderTarget)resourceHandle4.get();
                RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(((RenderTarget)object).getColorTexture(), 0, ((RenderTarget)object).getDepthTexture(), 1.0);
            }
            object = new PoseStack();
            MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
            MultiBufferSource.BufferSource bufferSource2 = this.renderBuffers.crumblingBufferSource();
            profilerFiller.popPush("entities");
            this.visibleEntities.sort(ENTITY_COMPARATOR);
            this.renderEntities((PoseStack)object, bufferSource, camera, deltaTracker, this.visibleEntities);
            bufferSource.endLastBatch();
            this.checkPoseStack((PoseStack)object);
            profilerFiller.popPush("blockentities");
            this.renderBlockEntities((PoseStack)object, bufferSource, bufferSource2, camera, f);
            bufferSource.endLastBatch();
            this.checkPoseStack((PoseStack)object);
            bufferSource.endBatch(RenderType.solid());
            bufferSource.endBatch(RenderType.endPortal());
            bufferSource.endBatch(RenderType.endGateway());
            bufferSource.endBatch(Sheets.solidBlockSheet());
            bufferSource.endBatch(Sheets.cutoutBlockSheet());
            bufferSource.endBatch(Sheets.bedSheet());
            bufferSource.endBatch(Sheets.shulkerBoxSheet());
            bufferSource.endBatch(Sheets.signSheet());
            bufferSource.endBatch(Sheets.hangingSignSheet());
            bufferSource.endBatch(Sheets.chestSheet());
            this.renderBuffers.outlineBufferSource().endOutlineBatch();
            if (bl) {
                this.renderBlockOutline(camera, bufferSource, (PoseStack)object, false);
            }
            profilerFiller.popPush("debug");
            this.minecraft.debugRenderer.render((PoseStack)object, frustum, bufferSource, d, d2, d3);
            bufferSource.endLastBatch();
            this.checkPoseStack((PoseStack)object);
            bufferSource.endBatch(Sheets.translucentItemSheet());
            bufferSource.endBatch(Sheets.bannerSheet());
            bufferSource.endBatch(Sheets.shieldSheet());
            bufferSource.endBatch(RenderType.armorEntityGlint());
            bufferSource.endBatch(RenderType.glint());
            bufferSource.endBatch(RenderType.glintTranslucent());
            bufferSource.endBatch(RenderType.entityGlint());
            profilerFiller.popPush("destroyProgress");
            this.renderBlockDestroyAnimation((PoseStack)object, camera, bufferSource2);
            bufferSource2.endBatch();
            this.checkPoseStack((PoseStack)object);
            bufferSource.endBatch(RenderType.waterMask());
            bufferSource.endBatch();
            if (resourceHandle2 != null) {
                ((RenderTarget)resourceHandle2.get()).copyDepthFrom((RenderTarget)resourceHandle.get());
            }
            profilerFiller.popPush("translucent");
            chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.TRANSLUCENT);
            profilerFiller.popPush("string");
            chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.TRIPWIRE);
            if (bl) {
                this.renderBlockOutline(camera, bufferSource, (PoseStack)object, true);
            }
            bufferSource.endBatch();
            profilerFiller.pop();
        });
    }

    private void addParticlesPass(FrameGraphBuilder frameGraphBuilder, Camera camera, float f, GpuBufferSlice gpuBufferSlice) {
        FramePass framePass = frameGraphBuilder.addPass("particles");
        if (this.targets.particles != null) {
            this.targets.particles = framePass.readsAndWrites(this.targets.particles);
            framePass.reads(this.targets.main);
        } else {
            this.targets.main = framePass.readsAndWrites(this.targets.main);
        }
        ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
        ResourceHandle<RenderTarget> resourceHandle2 = this.targets.particles;
        framePass.executes(() -> {
            RenderSystem.setShaderFog(gpuBufferSlice);
            if (resourceHandle2 != null) {
                ((RenderTarget)resourceHandle2.get()).copyDepthFrom((RenderTarget)resourceHandle.get());
            }
            this.minecraft.particleEngine.render(camera, f, this.renderBuffers.bufferSource());
        });
    }

    private void addCloudsPass(FrameGraphBuilder frameGraphBuilder, CloudStatus cloudStatus, Vec3 vec3, float f, int n, float f2) {
        FramePass framePass = frameGraphBuilder.addPass("clouds");
        if (this.targets.clouds != null) {
            this.targets.clouds = framePass.readsAndWrites(this.targets.clouds);
        } else {
            this.targets.main = framePass.readsAndWrites(this.targets.main);
        }
        framePass.executes(() -> this.cloudRenderer.render(n, cloudStatus, f2, vec3, f));
    }

    private void addWeatherPass(FrameGraphBuilder frameGraphBuilder, Vec3 vec3, float f, GpuBufferSlice gpuBufferSlice) {
        int n = this.minecraft.options.getEffectiveRenderDistance() * 16;
        float f2 = this.minecraft.gameRenderer.getDepthFar();
        FramePass framePass = frameGraphBuilder.addPass("weather");
        if (this.targets.weather != null) {
            this.targets.weather = framePass.readsAndWrites(this.targets.weather);
        } else {
            this.targets.main = framePass.readsAndWrites(this.targets.main);
        }
        framePass.executes(() -> {
            RenderSystem.setShaderFog(gpuBufferSlice);
            MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
            this.weatherEffectRenderer.render(this.minecraft.level, bufferSource, this.ticks, f, vec3);
            this.worldBorderRenderer.render(this.level.getWorldBorder(), vec3, n, f2);
            bufferSource.endBatch();
        });
    }

    private void addLateDebugPass(FrameGraphBuilder frameGraphBuilder, Vec3 vec3, GpuBufferSlice gpuBufferSlice) {
        FramePass framePass = frameGraphBuilder.addPass("late_debug");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        if (this.targets.itemEntity != null) {
            this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
        }
        ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
        framePass.executes(() -> {
            RenderSystem.setShaderFog(gpuBufferSlice);
            PoseStack poseStack = new PoseStack();
            MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
            this.minecraft.debugRenderer.renderAfterTranslucents(poseStack, bufferSource, vec3.x, vec3.y, vec3.z);
            bufferSource.endLastBatch();
            this.checkPoseStack(poseStack);
        });
    }

    private boolean collectVisibleEntities(Camera camera, Frustum frustum, List<Entity> list) {
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        boolean bl = false;
        boolean bl2 = this.shouldShowEntityOutlines();
        Entity.setViewScale(Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get());
        for (Entity entity : this.level.entitiesForRendering()) {
            BlockPos blockPos;
            if (!this.entityRenderDispatcher.shouldRender(entity, frustum, d, d2, d3) && !entity.hasIndirectPassenger(this.minecraft.player) || !this.level.isOutsideBuildHeight((blockPos = entity.blockPosition()).getY()) && !this.isSectionCompiled(blockPos) || entity == camera.getEntity() && !camera.isDetached() && (!(camera.getEntity() instanceof LivingEntity) || !((LivingEntity)camera.getEntity()).isSleeping()) || entity instanceof LocalPlayer && camera.getEntity() != entity) continue;
            list.add(entity);
            if (!bl2 || !this.minecraft.shouldEntityAppearGlowing(entity)) continue;
            bl = true;
        }
        return bl;
    }

    private void renderEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> list) {
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
        boolean bl = this.shouldShowEntityOutlines();
        for (Entity entity : list) {
            MultiBufferSource multiBufferSource;
            if (entity.tickCount == 0) {
                entity.xOld = entity.getX();
                entity.yOld = entity.getY();
                entity.zOld = entity.getZ();
            }
            if (bl && this.minecraft.shouldEntityAppearGlowing(entity)) {
                OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
                multiBufferSource = outlineBufferSource;
                int n = entity.getTeamColor();
                outlineBufferSource.setColor(ARGB.red(n), ARGB.green(n), ARGB.blue(n), 255);
            } else {
                multiBufferSource = bufferSource;
            }
            float f = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
            this.renderEntity(entity, d, d2, d3, f, poseStack, multiBufferSource);
        }
    }

    private void renderBlockEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource bufferSource2, Camera camera, float f) {
        List<BlockEntity> list;
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        for (Object object : this.visibleSections) {
            list = ((SectionRenderDispatcher.RenderSection)object).getSectionMesh().getRenderableBlockEntities();
            if (list.isEmpty()) continue;
            for (BlockEntity blockEntity : list) {
                int n;
                BlockPos blockPos = blockEntity.getBlockPos();
                MultiBufferSource multiBufferSource = bufferSource;
                poseStack.pushPose();
                poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - d2, (double)blockPos.getZ() - d3);
                SortedSet sortedSet = (SortedSet)this.destructionProgress.get(blockPos.asLong());
                if (sortedSet != null && !sortedSet.isEmpty() && (n = ((BlockDestructionProgress)sortedSet.last()).getProgress()) >= 0) {
                    PoseStack.Pose pose = poseStack.last();
                    SheetedDecalTextureGenerator sheetedDecalTextureGenerator = new SheetedDecalTextureGenerator(bufferSource2.getBuffer(ModelBakery.DESTROY_TYPES.get(n)), pose, 1.0f);
                    multiBufferSource = renderType -> {
                        VertexConsumer vertexConsumer2 = bufferSource.getBuffer(renderType);
                        if (renderType.affectsCrumbling()) {
                            return VertexMultiConsumer.create(sheetedDecalTextureGenerator, vertexConsumer2);
                        }
                        return vertexConsumer2;
                    };
                }
                this.blockEntityRenderDispatcher.render(blockEntity, f, poseStack, multiBufferSource);
                poseStack.popPose();
            }
        }
        Iterator<BlockEntity> iterator = this.level.getGloballyRenderedBlockEntities().iterator();
        while (iterator.hasNext()) {
            Object object;
            object = (BlockEntity)iterator.next();
            if (((BlockEntity)object).isRemoved()) {
                iterator.remove();
                continue;
            }
            list = ((BlockEntity)object).getBlockPos();
            poseStack.pushPose();
            poseStack.translate((double)((Vec3i)((Object)list)).getX() - d, (double)((Vec3i)((Object)list)).getY() - d2, (double)((Vec3i)((Object)list)).getZ() - d3);
            this.blockEntityRenderDispatcher.render(object, f, poseStack, bufferSource);
            poseStack.popPose();
        }
    }

    private void renderBlockDestroyAnimation(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource bufferSource) {
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        for (Long2ObjectMap.Entry entry : this.destructionProgress.long2ObjectEntrySet()) {
            SortedSet sortedSet;
            BlockPos blockPos = BlockPos.of(entry.getLongKey());
            if (blockPos.distToCenterSqr(d, d2, d3) > 1024.0 || (sortedSet = (SortedSet)entry.getValue()) == null || sortedSet.isEmpty()) continue;
            int n = ((BlockDestructionProgress)sortedSet.last()).getProgress();
            poseStack.pushPose();
            poseStack.translate((double)blockPos.getX() - d, (double)blockPos.getY() - d2, (double)blockPos.getZ() - d3);
            PoseStack.Pose pose = poseStack.last();
            SheetedDecalTextureGenerator sheetedDecalTextureGenerator = new SheetedDecalTextureGenerator(bufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(n)), pose, 1.0f);
            this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos), blockPos, this.level, poseStack, sheetedDecalTextureGenerator);
            poseStack.popPose();
        }
    }

    private void renderBlockOutline(Camera camera, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl) {
        Object object = this.minecraft.hitResult;
        if (!(object instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult)object;
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        object = blockHitResult.getBlockPos();
        BlockState blockState = this.level.getBlockState((BlockPos)object);
        if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds((BlockPos)object)) {
            VertexConsumer vertexConsumer;
            boolean bl2 = ItemBlockRenderTypes.getChunkRenderType(blockState).sortOnUpload();
            if (bl2 != bl) {
                return;
            }
            Vec3 vec3 = camera.getPosition();
            Boolean bl3 = this.minecraft.options.highContrastBlockOutline().get();
            if (bl3.booleanValue()) {
                vertexConsumer = bufferSource.getBuffer(RenderType.secondaryBlockOutline());
                this.renderHitOutline(poseStack, vertexConsumer, camera.getEntity(), vec3.x, vec3.y, vec3.z, (BlockPos)object, blockState, -16777216);
            }
            vertexConsumer = bufferSource.getBuffer(RenderType.lines());
            int n = bl3 != false ? -11010079 : ARGB.color(102, -16777216);
            this.renderHitOutline(poseStack, vertexConsumer, camera.getEntity(), vec3.x, vec3.y, vec3.z, (BlockPos)object, blockState, n);
            bufferSource.endLastBatch();
        }
    }

    private void checkPoseStack(PoseStack poseStack) {
        if (!poseStack.isEmpty()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    private void renderEntity(Entity entity, double d, double d2, double d3, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        double d4 = Mth.lerp((double)f, entity.xOld, entity.getX());
        double d5 = Mth.lerp((double)f, entity.yOld, entity.getY());
        double d6 = Mth.lerp((double)f, entity.zOld, entity.getZ());
        this.entityRenderDispatcher.render(entity, d4 - d, d5 - d2, d6 - d3, f, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, f));
    }

    private void scheduleTranslucentSectionResort(Vec3 vec3) {
        if (this.visibleSections.isEmpty()) {
            return;
        }
        BlockPos blockPos = BlockPos.containing(vec3);
        boolean bl = !blockPos.equals(this.lastTranslucentSortBlockPos);
        Profiler.get().push("translucent_sort");
        TranslucencyPointOfView translucencyPointOfView = new TranslucencyPointOfView();
        for (SectionRenderDispatcher.RenderSection renderSection : this.nearbyVisibleSections) {
            this.scheduleResort(renderSection, translucencyPointOfView, vec3, bl, true);
        }
        this.translucencyResortIterationIndex %= this.visibleSections.size();
        int n = Math.max(this.visibleSections.size() / 8, 15);
        while (n-- > 0) {
            int n2 = this.translucencyResortIterationIndex++ % this.visibleSections.size();
            this.scheduleResort((SectionRenderDispatcher.RenderSection)this.visibleSections.get(n2), translucencyPointOfView, vec3, bl, false);
        }
        this.lastTranslucentSortBlockPos = blockPos;
        Profiler.get().pop();
    }

    private void scheduleResort(SectionRenderDispatcher.RenderSection renderSection, TranslucencyPointOfView translucencyPointOfView, Vec3 vec3, boolean bl, boolean bl2) {
        boolean bl3;
        translucencyPointOfView.set(vec3, renderSection.getSectionNode());
        boolean bl4 = renderSection.getSectionMesh().isDifferentPointOfView(translucencyPointOfView);
        boolean bl5 = bl3 = bl && (translucencyPointOfView.isAxisAligned() || bl2);
        if ((bl3 || bl4) && !renderSection.transparencyResortingScheduled() && renderSection.hasTranslucentGeometry()) {
            renderSection.resortTransparency(this.sectionRenderDispatcher);
        }
    }

    private ChunkSectionsToRender prepareChunkRenders(Matrix4fc matrix4fc, double d, double d2, double d3) {
        ObjectListIterator objectListIterator = this.visibleSections.listIterator(0);
        EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> enumMap = new EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>>(ChunkSectionLayer.class);
        int n = 0;
        for (ChunkSectionLayer object2 : ChunkSectionLayer.values()) {
            enumMap.put(object2, new ArrayList());
        }
        ArrayList arrayList = new ArrayList();
        Vector4f vector4f = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        Matrix4f matrix4f = new Matrix4f();
        while (objectListIterator.hasNext()) {
            SectionRenderDispatcher.RenderSection renderSection = (SectionRenderDispatcher.RenderSection)objectListIterator.next();
            SectionMesh sectionMesh = renderSection.getSectionMesh();
            for (ChunkSectionLayer chunkSectionLayer : ChunkSectionLayer.values()) {
                VertexFormat.IndexType indexType;
                GpuBuffer gpuBuffer;
                SectionBuffers sectionBuffers = sectionMesh.getBuffers(chunkSectionLayer);
                if (sectionBuffers == null) continue;
                if (sectionBuffers.getIndexBuffer() == null) {
                    if (sectionBuffers.getIndexCount() > n) {
                        n = sectionBuffers.getIndexCount();
                    }
                    gpuBuffer = null;
                    indexType = null;
                } else {
                    gpuBuffer = sectionBuffers.getIndexBuffer();
                    indexType = sectionBuffers.getIndexType();
                }
                BlockPos blockPos = renderSection.getRenderOrigin();
                int n2 = arrayList.size();
                arrayList.add(new DynamicUniforms.Transform(matrix4fc, (Vector4fc)vector4f, (Vector3fc)new Vector3f((float)((double)blockPos.getX() - d), (float)((double)blockPos.getY() - d2), (float)((double)blockPos.getZ() - d3)), (Matrix4fc)matrix4f, 1.0f));
                enumMap.get((Object)chunkSectionLayer).add(new RenderPass.Draw<GpuBufferSlice[]>(0, sectionBuffers.getVertexBuffer(), gpuBuffer, indexType, 0, sectionBuffers.getIndexCount(), (gpuBufferSliceArray, uniformUploader) -> uniformUploader.upload("DynamicTransforms", gpuBufferSliceArray[n2])));
            }
        }
        GpuBufferSlice[] gpuBufferSliceArray2 = RenderSystem.getDynamicUniforms().writeTransforms(arrayList.toArray(new DynamicUniforms.Transform[0]));
        return new ChunkSectionsToRender(enumMap, n, gpuBufferSliceArray2);
    }

    public void endFrame() {
        this.cloudRenderer.endFrame();
    }

    public void captureFrustum() {
        this.captureFrustum = true;
    }

    public void killFrustum() {
        this.capturedFrustum = null;
    }

    public void tick() {
        if (this.level.tickRateManager().runsNormally()) {
            ++this.ticks;
        }
        if (this.ticks % 20 != 0) {
            return;
        }
        ObjectIterator objectIterator = this.destroyingBlocks.values().iterator();
        while (objectIterator.hasNext()) {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)objectIterator.next();
            int n = blockDestructionProgress.getUpdatedRenderTick();
            if (this.ticks - n <= 400) continue;
            objectIterator.remove();
            this.removeProgress(blockDestructionProgress);
        }
    }

    private void removeProgress(BlockDestructionProgress blockDestructionProgress) {
        long l = blockDestructionProgress.getPos().asLong();
        Set set = (Set)this.destructionProgress.get(l);
        set.remove(blockDestructionProgress);
        if (set.isEmpty()) {
            this.destructionProgress.remove(l);
        }
    }

    private void addSkyPass(FrameGraphBuilder frameGraphBuilder, Camera camera, float f, GpuBufferSlice gpuBufferSlice) {
        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA || this.doesMobEffectBlockSky(camera)) {
            return;
        }
        DimensionSpecialEffects dimensionSpecialEffects = this.level.effects();
        DimensionSpecialEffects.SkyType skyType = dimensionSpecialEffects.skyType();
        if (skyType == DimensionSpecialEffects.SkyType.NONE) {
            return;
        }
        FramePass framePass = frameGraphBuilder.addPass("sky");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        framePass.executes(() -> {
            RenderSystem.setShaderFog(gpuBufferSlice);
            if (skyType == DimensionSpecialEffects.SkyType.END) {
                this.skyRenderer.renderEndSky();
                return;
            }
            PoseStack poseStack = new PoseStack();
            float f2 = this.level.getSunAngle(f);
            float f3 = this.level.getTimeOfDay(f);
            float f4 = 1.0f - this.level.getRainLevel(f);
            float f5 = this.level.getStarBrightness(f) * f4;
            int n = dimensionSpecialEffects.getSunriseOrSunsetColor(f3);
            int n2 = this.level.getMoonPhase();
            int n3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), f);
            float f6 = ARGB.redFloat(n3);
            float f7 = ARGB.greenFloat(n3);
            float f8 = ARGB.blueFloat(n3);
            this.skyRenderer.renderSkyDisc(f6, f7, f8);
            MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
            if (dimensionSpecialEffects.isSunriseOrSunset(f3)) {
                this.skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, f2, n);
            }
            this.skyRenderer.renderSunMoonAndStars(poseStack, bufferSource, f3, n2, f4, f5);
            bufferSource.endBatch();
            if (this.shouldRenderDarkDisc(f)) {
                this.skyRenderer.renderDarkDisc();
            }
        });
    }

    private boolean shouldRenderDarkDisc(float f) {
        return this.minecraft.player.getEyePosition((float)f).y - this.level.getLevelData().getHorizonHeight(this.level) < 0.0;
    }

    private boolean doesMobEffectBlockSky(Camera camera) {
        Entity entity = camera.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity.hasEffect(MobEffects.BLINDNESS) || livingEntity.hasEffect(MobEffects.DARKNESS);
        }
        return false;
    }

    private void compileSections(Camera camera) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("populate_sections_to_compile");
        RenderRegionCache renderRegionCache = new RenderRegionCache();
        BlockPos blockPos = camera.getBlockPosition();
        ArrayList arrayList = Lists.newArrayList();
        for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
            if (!renderSection.isDirty() || renderSection.getSectionMesh() == CompiledSectionMesh.UNCOMPILED && !renderSection.hasAllNeighbors()) continue;
            boolean bl = false;
            if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                BlockPos blockPos2 = SectionPos.of(renderSection.getSectionNode()).center();
                bl = blockPos2.distSqr(blockPos) < 768.0 || renderSection.isDirtyFromPlayer();
            } else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                bl = renderSection.isDirtyFromPlayer();
            }
            if (bl) {
                profilerFiller.push("build_near_sync");
                this.sectionRenderDispatcher.rebuildSectionSync(renderSection, renderRegionCache);
                renderSection.setNotDirty();
                profilerFiller.pop();
                continue;
            }
            arrayList.add(renderSection);
        }
        profilerFiller.popPush("upload");
        this.sectionRenderDispatcher.uploadAllPendingUploads();
        profilerFiller.popPush("schedule_async_compile");
        for (SectionRenderDispatcher.RenderSection renderSection : arrayList) {
            renderSection.rebuildSectionAsync(renderRegionCache);
            renderSection.setNotDirty();
        }
        profilerFiller.pop();
        this.scheduleTranslucentSectionResort(camera.getPosition());
    }

    private void renderHitOutline(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double d, double d2, double d3, BlockPos blockPos, BlockState blockState, int n) {
        ShapeRenderer.renderShape(poseStack, vertexConsumer, blockState.getShape(this.level, blockPos, CollisionContext.of(entity)), (double)blockPos.getX() - d, (double)blockPos.getY() - d2, (double)blockPos.getZ() - d3, n);
    }

    public void blockChanged(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockState blockState2, int n) {
        this.setBlockDirty(blockPos, (n & 8) != 0);
    }

    private void setBlockDirty(BlockPos blockPos, boolean bl) {
        for (int i = blockPos.getZ() - 1; i <= blockPos.getZ() + 1; ++i) {
            for (int j = blockPos.getX() - 1; j <= blockPos.getX() + 1; ++j) {
                for (int k = blockPos.getY() - 1; k <= blockPos.getY() + 1; ++k) {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), bl);
                }
            }
        }
    }

    public void setBlocksDirty(int n, int n2, int n3, int n4, int n5, int n6) {
        for (int i = n3 - 1; i <= n6 + 1; ++i) {
            for (int j = n - 1; j <= n4 + 1; ++j) {
                for (int k = n2 - 1; k <= n5 + 1; ++k) {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i));
                }
            }
        }
    }

    public void setBlockDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        if (this.minecraft.getModelManager().requiresRender(blockState, blockState2)) {
            this.setBlocksDirty(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
    }

    public void setSectionDirtyWithNeighbors(int n, int n2, int n3) {
        this.setSectionRangeDirty(n - 1, n2 - 1, n3 - 1, n + 1, n2 + 1, n3 + 1);
    }

    public void setSectionRangeDirty(int n, int n2, int n3, int n4, int n5, int n6) {
        for (int i = n3; i <= n6; ++i) {
            for (int j = n; j <= n4; ++j) {
                for (int k = n2; k <= n5; ++k) {
                    this.setSectionDirty(j, k, i);
                }
            }
        }
    }

    public void setSectionDirty(int n, int n2, int n3) {
        this.setSectionDirty(n, n2, n3, false);
    }

    private void setSectionDirty(int n, int n2, int n3, boolean bl) {
        this.viewArea.setDirty(n, n2, n3, bl);
    }

    public void onSectionBecomingNonEmpty(long l) {
        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSection(l);
        if (renderSection != null) {
            this.sectionOcclusionGraph.schedulePropagationFrom(renderSection);
        }
    }

    public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double d2, double d3, double d4, double d5, double d6) {
        this.addParticle(particleOptions, bl, false, d, d2, d3, d4, d5, d6);
    }

    public void addParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double d2, double d3, double d4, double d5, double d6) {
        try {
            this.addParticleInternal(particleOptions, bl, bl2, d, d2, d3, d4, d5, d6);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception while adding particle");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being added");
            crashReportCategory.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType()));
            crashReportCategory.setDetail("Parameters", () -> ParticleTypes.CODEC.encodeStart(this.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), (Object)particleOptions).toString());
            crashReportCategory.setDetail("Position", () -> CrashReportCategory.formatLocation((LevelHeightAccessor)this.level, d, d2, d3));
            throw new ReportedException(crashReport);
        }
    }

    public <T extends ParticleOptions> void addParticle(T t, double d, double d2, double d3, double d4, double d5, double d6) {
        this.addParticle(t, t.getType().getOverrideLimiter(), d, d2, d3, d4, d5, d6);
    }

    @Nullable
    Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, double d, double d2, double d3, double d4, double d5, double d6) {
        return this.addParticleInternal(particleOptions, bl, false, d, d2, d3, d4, d5, d6);
    }

    @Nullable
    private Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double d2, double d3, double d4, double d5, double d6) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        ParticleStatus particleStatus = this.calculateParticleLevel(bl2);
        if (bl) {
            return this.minecraft.particleEngine.createParticle(particleOptions, d, d2, d3, d4, d5, d6);
        }
        if (camera.getPosition().distanceToSqr(d, d2, d3) > 1024.0) {
            return null;
        }
        if (particleStatus == ParticleStatus.MINIMAL) {
            return null;
        }
        return this.minecraft.particleEngine.createParticle(particleOptions, d, d2, d3, d4, d5, d6);
    }

    private ParticleStatus calculateParticleLevel(boolean bl) {
        ParticleStatus particleStatus = this.minecraft.options.particles().get();
        if (bl && particleStatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
            particleStatus = ParticleStatus.DECREASED;
        }
        if (particleStatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
            particleStatus = ParticleStatus.MINIMAL;
        }
        return particleStatus;
    }

    public void destroyBlockProgress(int n, BlockPos blockPos, int n2) {
        if (n2 < 0 || n2 >= 10) {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)this.destroyingBlocks.remove(n);
            if (blockDestructionProgress != null) {
                this.removeProgress(blockDestructionProgress);
            }
        } else {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)this.destroyingBlocks.get(n);
            if (blockDestructionProgress != null) {
                this.removeProgress(blockDestructionProgress);
            }
            if (blockDestructionProgress == null || blockDestructionProgress.getPos().getX() != blockPos.getX() || blockDestructionProgress.getPos().getY() != blockPos.getY() || blockDestructionProgress.getPos().getZ() != blockPos.getZ()) {
                blockDestructionProgress = new BlockDestructionProgress(n, blockPos);
                this.destroyingBlocks.put(n, (Object)blockDestructionProgress);
            }
            blockDestructionProgress.setProgress(n2);
            blockDestructionProgress.updateTick(this.ticks);
            ((SortedSet)this.destructionProgress.computeIfAbsent(blockDestructionProgress.getPos().asLong(), l -> Sets.newTreeSet())).add(blockDestructionProgress);
        }
    }

    public boolean hasRenderedAllSections() {
        return this.sectionRenderDispatcher.isQueueEmpty();
    }

    public void onChunkReadyToRender(ChunkPos chunkPos) {
        this.sectionOcclusionGraph.onChunkReadyToRender(chunkPos);
    }

    public void needsUpdate() {
        this.sectionOcclusionGraph.invalidate();
        this.cloudRenderer.markForRebuild();
    }

    public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        return LevelRenderer.getLightColor(BrightnessGetter.DEFAULT, blockAndTintGetter, blockAndTintGetter.getBlockState(blockPos), blockPos);
    }

    public static int getLightColor(BrightnessGetter brightnessGetter, BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos) {
        int n;
        if (blockState.emissiveRendering(blockAndTintGetter, blockPos)) {
            return 0xF000F0;
        }
        int n2 = brightnessGetter.packedBrightness(blockAndTintGetter, blockPos);
        int n3 = LightTexture.block(n2);
        if (n3 < (n = blockState.getLightEmission())) {
            int n4 = LightTexture.sky(n2);
            return LightTexture.pack(n, n4);
        }
        return n2;
    }

    public boolean isSectionCompiled(BlockPos blockPos) {
        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSectionAt(blockPos);
        return renderSection != null && renderSection.sectionMesh.get() != CompiledSectionMesh.UNCOMPILED;
    }

    @Nullable
    public RenderTarget entityOutlineTarget() {
        return this.targets.entityOutline != null ? this.targets.entityOutline.get() : null;
    }

    @Nullable
    public RenderTarget getTranslucentTarget() {
        return this.targets.translucent != null ? this.targets.translucent.get() : null;
    }

    @Nullable
    public RenderTarget getItemEntityTarget() {
        return this.targets.itemEntity != null ? this.targets.itemEntity.get() : null;
    }

    @Nullable
    public RenderTarget getParticlesTarget() {
        return this.targets.particles != null ? this.targets.particles.get() : null;
    }

    @Nullable
    public RenderTarget getWeatherTarget() {
        return this.targets.weather != null ? this.targets.weather.get() : null;
    }

    @Nullable
    public RenderTarget getCloudsTarget() {
        return this.targets.clouds != null ? this.targets.clouds.get() : null;
    }

    @VisibleForDebug
    public ObjectArrayList<SectionRenderDispatcher.RenderSection> getVisibleSections() {
        return this.visibleSections;
    }

    @VisibleForDebug
    public SectionOcclusionGraph getSectionOcclusionGraph() {
        return this.sectionOcclusionGraph;
    }

    @Nullable
    public Frustum getCapturedFrustum() {
        return this.capturedFrustum;
    }

    public CloudRenderer getCloudRenderer() {
        return this.cloudRenderer;
    }

    @FunctionalInterface
    public static interface BrightnessGetter {
        public static final BrightnessGetter DEFAULT = (blockAndTintGetter, blockPos) -> {
            int n = blockAndTintGetter.getBrightness(LightLayer.SKY, blockPos);
            int n2 = blockAndTintGetter.getBrightness(LightLayer.BLOCK, blockPos);
            return Brightness.pack(n2, n);
        };

        public int packedBrightness(BlockAndTintGetter var1, BlockPos var2);
    }
}

