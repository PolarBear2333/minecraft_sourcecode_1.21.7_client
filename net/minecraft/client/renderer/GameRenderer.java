/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiBannerResultRenderer;
import net.minecraft.client.gui.render.pip.GuiBookModelRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.GuiProfilerChartRenderer;
import net.minecraft.client.gui.render.pip.GuiSignRenderer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.slf4j.Logger;

public class GameRenderer
implements TrackedWaypoint.Projector,
AutoCloseable {
    private static final ResourceLocation BLUR_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("blur");
    public static final int MAX_BLUR_RADIUS = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float PROJECTION_Z_NEAR = 0.05f;
    public static final float PROJECTION_3D_HUD_Z_FAR = 100.0f;
    private static final float PORTAL_SPINNING_SPEED = 20.0f;
    private static final float NAUSEA_SPINNING_SPEED = 7.0f;
    private final Minecraft minecraft;
    private final RandomSource random = RandomSource.create();
    private float renderDistance;
    public final ItemInHandRenderer itemInHandRenderer;
    private final ScreenEffectRenderer screenEffectRenderer;
    private final RenderBuffers renderBuffers;
    private float spinningEffectTime;
    private float spinningEffectSpeed;
    private float fovModifier;
    private float oldFovModifier;
    private float darkenWorldAmount;
    private float darkenWorldAmountO;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private boolean hasWorldScreenshot;
    private long lastActiveTime = Util.getMillis();
    private final LightTexture lightTexture;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    private boolean panoramicMode;
    protected final CubeMap cubeMap = new CubeMap(ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama"));
    protected final PanoramaRenderer panorama = new PanoramaRenderer(this.cubeMap);
    private final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
    private final FogRenderer fogRenderer = new FogRenderer();
    private final GuiRenderer guiRenderer;
    private final GuiRenderState guiRenderState;
    @Nullable
    private ResourceLocation postEffectId;
    private boolean effectActive;
    private final Camera mainCamera = new Camera();
    private final Lighting lighting = new Lighting();
    private final GlobalSettingsUniform globalSettingsUniform = new GlobalSettingsUniform();
    private final PerspectiveProjectionMatrixBuffer levelProjectionMatrixBuffer = new PerspectiveProjectionMatrixBuffer("level");
    private final CachedPerspectiveProjectionMatrixBuffer hud3dProjectionMatrixBuffer = new CachedPerspectiveProjectionMatrixBuffer("3d hud", 0.05f, 100.0f);

    public GameRenderer(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, RenderBuffers renderBuffers) {
        this.minecraft = minecraft;
        this.itemInHandRenderer = itemInHandRenderer;
        this.lightTexture = new LightTexture(this, minecraft);
        this.renderBuffers = renderBuffers;
        this.guiRenderState = new GuiRenderState();
        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
        this.guiRenderer = new GuiRenderer(this.guiRenderState, bufferSource, List.of(new GuiEntityRenderer(bufferSource, minecraft.getEntityRenderDispatcher()), new GuiSkinRenderer(bufferSource), new GuiBookModelRenderer(bufferSource), new GuiBannerResultRenderer(bufferSource), new GuiSignRenderer(bufferSource), new GuiProfilerChartRenderer(bufferSource)));
        this.screenEffectRenderer = new ScreenEffectRenderer(minecraft, bufferSource);
    }

    @Override
    public void close() {
        this.globalSettingsUniform.close();
        this.lightTexture.close();
        this.overlayTexture.close();
        this.resourcePool.close();
        this.guiRenderer.close();
        this.levelProjectionMatrixBuffer.close();
        this.hud3dProjectionMatrixBuffer.close();
        this.lighting.close();
        this.cubeMap.close();
        this.fogRenderer.close();
    }

    public void setRenderBlockOutline(boolean bl) {
        this.renderBlockOutline = bl;
    }

    public void setPanoramicMode(boolean bl) {
        this.panoramicMode = bl;
    }

    public boolean isPanoramicMode() {
        return this.panoramicMode;
    }

    public void clearPostEffect() {
        this.postEffectId = null;
    }

    public void togglePostEffect() {
        this.effectActive = !this.effectActive;
    }

    public void checkEntityPostEffect(@Nullable Entity entity) {
        this.postEffectId = null;
        if (entity instanceof Creeper) {
            this.setPostEffect(ResourceLocation.withDefaultNamespace("creeper"));
        } else if (entity instanceof Spider) {
            this.setPostEffect(ResourceLocation.withDefaultNamespace("spider"));
        } else if (entity instanceof EnderMan) {
            this.setPostEffect(ResourceLocation.withDefaultNamespace("invert"));
        }
    }

    private void setPostEffect(ResourceLocation resourceLocation) {
        this.postEffectId = resourceLocation;
        this.effectActive = true;
    }

    public void processBlurEffect() {
        PostChain postChain = this.minecraft.getShaderManager().getPostChain(BLUR_POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (postChain != null) {
            postChain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
        }
    }

    public void preloadUiShader(ResourceProvider resourceProvider) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        BiFunction<ResourceLocation, ShaderType, String> biFunction = (resourceLocation, shaderType) -> {
            String string;
            block8: {
                ResourceLocation resourceLocation2 = shaderType.idConverter().idToFile((ResourceLocation)resourceLocation);
                BufferedReader bufferedReader = resourceProvider.getResourceOrThrow(resourceLocation2).openAsReader();
                try {
                    string = IOUtils.toString((Reader)bufferedReader);
                    if (bufferedReader == null) break block8;
                }
                catch (Throwable throwable) {
                    try {
                        if (bufferedReader != null) {
                            try {
                                ((Reader)bufferedReader).close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException iOException) {
                        LOGGER.error("Coudln't preload {} shader {}: {}", new Object[]{shaderType, resourceLocation, iOException});
                        return null;
                    }
                }
                ((Reader)bufferedReader).close();
            }
            return string;
        };
        gpuDevice.precompilePipeline(RenderPipelines.GUI, biFunction);
        gpuDevice.precompilePipeline(RenderPipelines.GUI_TEXTURED, biFunction);
        if (TracyClient.isAvailable()) {
            gpuDevice.precompilePipeline(RenderPipelines.TRACY_BLIT, biFunction);
        }
    }

    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        LocalPlayer localPlayer = this.minecraft.player;
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(localPlayer);
        }
        this.mainCamera.tick();
        this.itemInHandRenderer.tick();
        float f = localPlayer.portalEffectIntensity;
        float f2 = localPlayer.getEffectBlendFactor(MobEffects.NAUSEA, 1.0f);
        if (f > 0.0f || f2 > 0.0f) {
            this.spinningEffectSpeed = (f * 20.0f + f2 * 7.0f) / (f + f2);
            this.spinningEffectTime += this.spinningEffectSpeed;
        } else {
            this.spinningEffectSpeed = 0.0f;
        }
        if (!this.minecraft.level.tickRateManager().runsNormally()) {
            return;
        }
        this.minecraft.levelRenderer.tickParticles(this.mainCamera);
        this.darkenWorldAmountO = this.darkenWorldAmount;
        if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
            this.darkenWorldAmount += 0.05f;
            if (this.darkenWorldAmount > 1.0f) {
                this.darkenWorldAmount = 1.0f;
            }
        } else if (this.darkenWorldAmount > 0.0f) {
            this.darkenWorldAmount -= 0.0125f;
        }
        this.screenEffectRenderer.tick();
    }

    @Nullable
    public ResourceLocation currentPostEffect() {
        return this.postEffectId;
    }

    public void resize(int n, int n2) {
        this.resourcePool.clear();
        this.minecraft.levelRenderer.resize(n, n2);
    }

    public void pick(float f) {
        Entity entity;
        HitResult hitResult;
        Entity entity2 = this.minecraft.getCameraEntity();
        if (entity2 == null) {
            return;
        }
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }
        Profiler.get().push("pick");
        double d = this.minecraft.player.blockInteractionRange();
        double d2 = this.minecraft.player.entityInteractionRange();
        this.minecraft.hitResult = hitResult = this.pick(entity2, d, d2, f);
        if (hitResult instanceof EntityHitResult) {
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            entity = entityHitResult.getEntity();
        } else {
            entity = null;
        }
        this.minecraft.crosshairPickEntity = entity;
        Profiler.get().pop();
    }

    private HitResult pick(Entity entity, double d, double d2, float f) {
        double d3 = Math.max(d, d2);
        double d4 = Mth.square(d3);
        Vec3 vec3 = entity.getEyePosition(f);
        HitResult hitResult = entity.pick(d3, f, false);
        double d5 = hitResult.getLocation().distanceToSqr(vec3);
        if (hitResult.getType() != HitResult.Type.MISS) {
            d4 = d5;
            d3 = Math.sqrt(d4);
        }
        Vec3 vec32 = entity.getViewVector(f);
        Vec3 vec33 = vec3.add(vec32.x * d3, vec32.y * d3, vec32.z * d3);
        float f2 = 1.0f;
        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(d3)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, EntitySelector.CAN_BE_PICKED, d4);
        if (entityHitResult != null && entityHitResult.getLocation().distanceToSqr(vec3) < d5) {
            return GameRenderer.filterHitResult(entityHitResult, vec3, d2);
        }
        return GameRenderer.filterHitResult(hitResult, vec3, d);
    }

    private static HitResult filterHitResult(HitResult hitResult, Vec3 vec3, double d) {
        Vec3 vec32 = hitResult.getLocation();
        if (!vec32.closerThan(vec3, d)) {
            Vec3 vec33 = hitResult.getLocation();
            Direction direction = Direction.getApproximateNearest(vec33.x - vec3.x, vec33.y - vec3.y, vec33.z - vec3.z);
            return BlockHitResult.miss(vec33, direction, BlockPos.containing(vec33));
        }
        return hitResult;
    }

    private void tickFov() {
        float f;
        Object object = this.minecraft.getCameraEntity();
        if (object instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)object;
            object = this.minecraft.options;
            boolean bl = ((Options)object).getCameraType().isFirstPerson();
            float f2 = ((Options)object).fovEffectScale().get().floatValue();
            f = abstractClientPlayer.getFieldOfViewModifier(bl, f2);
        } else {
            f = 1.0f;
        }
        this.oldFovModifier = this.fovModifier;
        this.fovModifier += (f - this.fovModifier) * 0.5f;
        this.fovModifier = Mth.clamp(this.fovModifier, 0.1f, 1.5f);
    }

    private float getFov(Camera camera, float f, boolean bl) {
        Object object;
        Entity entity;
        if (this.panoramicMode) {
            return 90.0f;
        }
        float f2 = 70.0f;
        if (bl) {
            f2 = this.minecraft.options.fov().get().intValue();
            f2 *= Mth.lerp(f, this.oldFovModifier, this.fovModifier);
        }
        if ((entity = camera.getEntity()) instanceof LivingEntity && ((LivingEntity)(object = (LivingEntity)entity)).isDeadOrDying()) {
            float f3 = Math.min((float)((LivingEntity)object).deathTime + f, 20.0f);
            f2 /= (1.0f - 500.0f / (f3 + 500.0f)) * 2.0f + 1.0f;
        }
        if ((object = camera.getFluidInCamera()) == FogType.LAVA || object == FogType.WATER) {
            float f4 = this.minecraft.options.fovEffectScale().get().floatValue();
            f2 *= Mth.lerp(f4, 1.0f, 0.85714287f);
        }
        return f2;
    }

    private void bobHurt(PoseStack poseStack, float f) {
        Entity entity = this.minecraft.getCameraEntity();
        if (entity instanceof LivingEntity) {
            float f2;
            LivingEntity livingEntity = (LivingEntity)entity;
            float f3 = (float)livingEntity.hurtTime - f;
            if (livingEntity.isDeadOrDying()) {
                f2 = Math.min((float)livingEntity.deathTime + f, 20.0f);
                poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(40.0f - 8000.0f / (f2 + 200.0f)));
            }
            if (f3 < 0.0f) {
                return;
            }
            f3 /= (float)livingEntity.hurtDuration;
            f3 = Mth.sin(f3 * f3 * f3 * f3 * (float)Math.PI);
            f2 = livingEntity.getHurtDir();
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-f2));
            float f4 = (float)((double)(-f3) * 14.0 * this.minecraft.options.damageTiltStrength().get());
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f4));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f2));
        }
    }

    private void bobView(PoseStack poseStack, float f) {
        Entity entity = this.minecraft.getCameraEntity();
        if (!(entity instanceof AbstractClientPlayer)) {
            return;
        }
        AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)entity;
        float f2 = abstractClientPlayer.walkDist - abstractClientPlayer.walkDistO;
        float f3 = -(abstractClientPlayer.walkDist + f2 * f);
        float f4 = Mth.lerp(f, abstractClientPlayer.oBob, abstractClientPlayer.bob);
        poseStack.translate(Mth.sin(f3 * (float)Math.PI) * f4 * 0.5f, -Math.abs(Mth.cos(f3 * (float)Math.PI) * f4), 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(Mth.sin(f3 * (float)Math.PI) * f4 * 3.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Math.abs(Mth.cos(f3 * (float)Math.PI - 0.2f) * f4) * 5.0f));
    }

    private void renderItemInHand(float f, boolean bl, Matrix4f matrix4f) {
        if (this.panoramicMode) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.mulPose((Matrix4fc)matrix4f.invert(new Matrix4f()));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix().mul((Matrix4fc)matrix4f);
        this.bobHurt(poseStack, f);
        if (this.minecraft.options.bobView().get().booleanValue()) {
            this.bobView(poseStack, f);
        }
        if (this.minecraft.options.getCameraType().isFirstPerson() && !bl && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.lightTexture.turnOnLightLayer();
            this.itemInHandRenderer.renderHandsWithItems(f, poseStack, this.renderBuffers.bufferSource(), this.minecraft.player, this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, f));
            this.lightTexture.turnOffLightLayer();
        }
        matrix4fStack.popMatrix();
        poseStack.popPose();
    }

    public Matrix4f getProjectionMatrix(float f) {
        Matrix4f matrix4f = new Matrix4f();
        return matrix4f.perspective(f * ((float)Math.PI / 180), (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(), 0.05f, this.getDepthFar());
    }

    public float getDepthFar() {
        return Math.max(this.renderDistance * 4.0f, (float)(this.minecraft.options.cloudRange().get() * 16));
    }

    public static float getNightVisionScale(LivingEntity livingEntity, float f) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(MobEffects.NIGHT_VISION);
        if (!mobEffectInstance.endsWithin(200)) {
            return 1.0f;
        }
        return 0.7f + Mth.sin(((float)mobEffectInstance.getDuration() - f) * (float)Math.PI * 0.2f) * 0.3f;
    }

    public void render(DeltaTracker deltaTracker, boolean bl) {
        Object object;
        if (this.minecraft.isWindowActive() || !this.minecraft.options.pauseOnLostFocus || this.minecraft.options.touchscreen().get().booleanValue() && this.minecraft.mouseHandler.isRightPressed()) {
            this.lastActiveTime = Util.getMillis();
        } else if (Util.getMillis() - this.lastActiveTime > 500L) {
            this.minecraft.pauseGame(false);
        }
        if (this.minecraft.noRender) {
            return;
        }
        this.globalSettingsUniform.update(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.options.glintStrength().get(), this.minecraft.level == null ? 0L : this.minecraft.level.getGameTime(), deltaTracker, this.minecraft.options.getMenuBackgroundBlurriness());
        ProfilerFiller profilerFiller = Profiler.get();
        boolean bl2 = this.minecraft.isGameLoadFinished();
        int n = (int)this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
        int n2 = (int)this.minecraft.mouseHandler.getScaledYPos(this.minecraft.getWindow());
        if (bl2 && bl && this.minecraft.level != null) {
            profilerFiller.push("world");
            this.renderLevel(deltaTracker);
            this.tryTakeScreenshotIfNeeded();
            this.minecraft.levelRenderer.doEntityOutline();
            if (this.postEffectId != null && this.effectActive) {
                RenderSystem.resetTextureMatrix();
                object = this.minecraft.getShaderManager().getPostChain(this.postEffectId, LevelTargetBundle.MAIN_TARGETS);
                if (object != null) {
                    ((PostChain)object).process(this.minecraft.getMainRenderTarget(), this.resourcePool);
                }
            }
        }
        this.fogRenderer.endFrame();
        object = this.minecraft.getMainRenderTarget();
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(((RenderTarget)object).getDepthTexture(), 1.0);
        this.minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        this.guiRenderState.reset();
        GuiGraphics guiGraphics = new GuiGraphics(this.minecraft, this.guiRenderState);
        if (bl2 && bl && this.minecraft.level != null) {
            profilerFiller.popPush("gui");
            this.minecraft.gui.render(guiGraphics, deltaTracker);
            profilerFiller.pop();
        }
        if (this.minecraft.getOverlay() != null) {
            try {
                this.minecraft.getOverlay().render(guiGraphics, n, n2, deltaTracker.getGameTimeDeltaTicks());
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering overlay");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Overlay render details");
                crashReportCategory.setDetail("Overlay name", () -> this.minecraft.getOverlay().getClass().getCanonicalName());
                throw new ReportedException(crashReport);
            }
        }
        if (bl2 && this.minecraft.screen != null) {
            try {
                this.minecraft.screen.renderWithTooltip(guiGraphics, n, n2, deltaTracker.getGameTimeDeltaTicks());
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering screen");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Screen render details");
                crashReportCategory.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                this.minecraft.mouseHandler.fillMousePositionDetails(crashReportCategory, this.minecraft.getWindow());
                throw new ReportedException(crashReport);
            }
            try {
                if (this.minecraft.screen != null) {
                    this.minecraft.screen.handleDelayedNarration();
                }
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Narrating screen");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Screen details");
                crashReportCategory.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
                throw new ReportedException(crashReport);
            }
        }
        if (bl2 && bl && this.minecraft.level != null) {
            this.minecraft.gui.renderSavingIndicator(guiGraphics, deltaTracker);
        }
        if (bl2) {
            try (Zone zone = profilerFiller.zone("toasts");){
                this.minecraft.getToastManager().render(guiGraphics);
            }
        }
        this.guiRenderer.render(this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        this.guiRenderer.incrementFrameNumber();
        this.resourcePool.endFrame();
    }

    private void tryTakeScreenshotIfNeeded() {
        if (this.hasWorldScreenshot || !this.minecraft.isLocalServer()) {
            return;
        }
        long l = Util.getMillis();
        if (l - this.lastScreenshotAttempt < 1000L) {
            return;
        }
        this.lastScreenshotAttempt = l;
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        if (integratedServer == null || integratedServer.isStopped()) {
            return;
        }
        integratedServer.getWorldScreenshotFile().ifPresent(path -> {
            if (Files.isRegularFile(path, new LinkOption[0])) {
                this.hasWorldScreenshot = true;
            } else {
                this.takeAutoScreenshot((Path)path);
            }
        });
    }

    private void takeAutoScreenshot(Path path) {
        if (this.minecraft.levelRenderer.countRenderedSections() > 10 && this.minecraft.levelRenderer.hasRenderedAllSections()) {
            Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget(), nativeImage -> Util.ioPool().execute(() -> {
                int n = nativeImage.getWidth();
                int n2 = nativeImage.getHeight();
                int n3 = 0;
                int n4 = 0;
                if (n > n2) {
                    n3 = (n - n2) / 2;
                    n = n2;
                } else {
                    n4 = (n2 - n) / 2;
                    n2 = n;
                }
                try (NativeImage nativeImage2 = new NativeImage(64, 64, false);){
                    nativeImage.resizeSubRectTo(n3, n4, n, n2, nativeImage2);
                    nativeImage2.writeToFile(path);
                }
                catch (IOException iOException) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable)iOException);
                }
                finally {
                    nativeImage.close();
                }
            }));
        }
    }

    private boolean shouldRenderBlockOutline() {
        boolean bl;
        if (!this.renderBlockOutline) {
            return false;
        }
        Entity entity = this.minecraft.getCameraEntity();
        boolean bl2 = bl = entity instanceof Player && !this.minecraft.options.hideGui;
        if (bl && !((Player)entity).getAbilities().mayBuild) {
            ItemStack itemStack = ((LivingEntity)entity).getMainHandItem();
            HitResult hitResult = this.minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockState = this.minecraft.level.getBlockState(blockPos);
                if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                    bl = blockState.getMenuProvider(this.minecraft.level, blockPos) != null;
                } else {
                    BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, blockPos, false);
                    HolderLookup.RegistryLookup registryLookup = this.minecraft.level.registryAccess().lookupOrThrow(Registries.BLOCK);
                    bl = !itemStack.isEmpty() && (itemStack.canBreakBlockInAdventureMode(blockInWorld) || itemStack.canPlaceOnBlockInAdventureMode(blockInWorld));
                }
            }
        }
        return bl;
    }

    public void renderLevel(DeltaTracker deltaTracker) {
        Matrix4f matrix4f;
        float f;
        float f2 = deltaTracker.getGameTimeDeltaPartialTick(true);
        LocalPlayer localPlayer = this.minecraft.player;
        this.lightTexture.updateLightTexture(f2);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(localPlayer);
        }
        this.pick(f2);
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("center");
        boolean bl = this.shouldRenderBlockOutline();
        profilerFiller.popPush("camera");
        Camera camera = this.mainCamera;
        LocalPlayer localPlayer2 = this.minecraft.getCameraEntity() == null ? localPlayer : this.minecraft.getCameraEntity();
        float f3 = this.minecraft.level.tickRateManager().isEntityFrozen(localPlayer2) ? 1.0f : f2;
        camera.setup(this.minecraft.level, localPlayer2, !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), f3);
        this.renderDistance = this.minecraft.options.getEffectiveRenderDistance() * 16;
        float f4 = this.getFov(camera, f2, true);
        Matrix4f matrix4f2 = this.getProjectionMatrix(f4);
        PoseStack poseStack = new PoseStack();
        this.bobHurt(poseStack, camera.getPartialTickTime());
        if (this.minecraft.options.bobView().get().booleanValue()) {
            this.bobView(poseStack, camera.getPartialTickTime());
        }
        matrix4f2.mul((Matrix4fc)poseStack.last().pose());
        float f5 = this.minecraft.options.screenEffectScale().get().floatValue();
        float f6 = Mth.lerp(f2, localPlayer.oPortalEffectIntensity, localPlayer.portalEffectIntensity);
        float f7 = localPlayer.getEffectBlendFactor(MobEffects.NAUSEA, f2);
        float f8 = Math.max(f6, f7) * (f5 * f5);
        if (f8 > 0.0f) {
            f = 5.0f / (f8 * f8 + 5.0f) - f8 * 0.04f;
            f *= f;
            matrix4f = new Vector3f(0.0f, Mth.SQRT_OF_TWO / 2.0f, Mth.SQRT_OF_TWO / 2.0f);
            float f9 = (this.spinningEffectTime + f2 * this.spinningEffectSpeed) * ((float)Math.PI / 180);
            matrix4f2.rotate(f9, (Vector3fc)matrix4f);
            matrix4f2.scale(1.0f / f, 1.0f, 1.0f);
            matrix4f2.rotate(-f9, (Vector3fc)matrix4f);
        }
        f = Math.max(f4, (float)this.minecraft.options.fov().get().intValue());
        matrix4f = this.getProjectionMatrix(f);
        RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(matrix4f2), ProjectionType.PERSPECTIVE);
        Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        Matrix4f matrix4f3 = new Matrix4f().rotation((Quaternionfc)quaternionf);
        this.minecraft.levelRenderer.prepareCullFrustum(camera.getPosition(), matrix4f3, matrix4f);
        profilerFiller.popPush("fog");
        boolean bl2 = this.minecraft.level.effects().isFoggyAt(camera.getBlockPosition().getX(), camera.getBlockPosition().getZ()) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
        Vector4f vector4f = this.fogRenderer.setupFog(camera, this.minecraft.options.getEffectiveRenderDistance(), bl2, deltaTracker, this.getDarkenWorldAmount(f2), this.minecraft.level);
        GpuBufferSlice gpuBufferSlice = this.fogRenderer.getBuffer(FogRenderer.FogMode.WORLD);
        profilerFiller.popPush("level");
        this.minecraft.levelRenderer.renderLevel(this.resourcePool, deltaTracker, bl, camera, matrix4f3, matrix4f2, gpuBufferSlice, vector4f, !bl2);
        profilerFiller.popPush("hand");
        boolean bl3 = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
        RenderSystem.setProjectionMatrix(this.hud3dProjectionMatrixBuffer.getBuffer(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.getFov(camera, f2, false)), ProjectionType.PERSPECTIVE);
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.minecraft.getMainRenderTarget().getDepthTexture(), 1.0);
        this.renderItemInHand(f2, bl3, matrix4f3);
        profilerFiller.popPush("screen effects");
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        this.screenEffectRenderer.renderScreenEffect(bl3, f2);
        bufferSource.endBatch();
        profilerFiller.pop();
        RenderSystem.setShaderFog(this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        if (this.minecraft.gui.shouldRenderDebugCrosshair()) {
            this.minecraft.getDebugOverlay().render3dCrosshair(camera);
        }
    }

    public void resetData() {
        this.screenEffectRenderer.resetItemActivation();
        this.minecraft.getMapTextureManager().resetData();
        this.mainCamera.reset();
        this.hasWorldScreenshot = false;
    }

    public void displayItemActivation(ItemStack itemStack) {
        this.screenEffectRenderer.displayItemActivation(itemStack, this.random);
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public float getDarkenWorldAmount(float f) {
        return Mth.lerp(f, this.darkenWorldAmountO, this.darkenWorldAmount);
    }

    public float getRenderDistance() {
        return this.renderDistance;
    }

    public Camera getMainCamera() {
        return this.mainCamera;
    }

    public LightTexture lightTexture() {
        return this.lightTexture;
    }

    public OverlayTexture overlayTexture() {
        return this.overlayTexture;
    }

    @Override
    public Vec3 projectPointToScreen(Vec3 vec3) {
        Matrix4f matrix4f = this.getProjectionMatrix(this.getFov(this.mainCamera, 0.0f, true));
        Quaternionf quaternionf = this.mainCamera.rotation().conjugate(new Quaternionf());
        Matrix4f matrix4f2 = new Matrix4f().rotation((Quaternionfc)quaternionf);
        Matrix4f matrix4f3 = matrix4f.mul((Matrix4fc)matrix4f2);
        Vec3 vec32 = this.mainCamera.getPosition();
        Vec3 vec33 = vec3.subtract(vec32);
        Vector3f vector3f = matrix4f3.transformProject(vec33.toVector3f());
        return new Vec3(vector3f);
    }

    @Override
    public double projectHorizonToScreen() {
        float f = this.mainCamera.getXRot();
        if (f <= -90.0f) {
            return Double.NEGATIVE_INFINITY;
        }
        if (f >= 90.0f) {
            return Double.POSITIVE_INFINITY;
        }
        float f2 = this.getFov(this.mainCamera, 0.0f, true);
        return Math.tan(f * ((float)Math.PI / 180)) / Math.tan(f2 / 2.0f * ((float)Math.PI / 180));
    }

    public GlobalSettingsUniform getGlobalSettingsUniform() {
        return this.globalSettingsUniform;
    }

    public Lighting getLighting() {
        return this.lighting;
    }

    public void setLevel(@Nullable ClientLevel clientLevel) {
        if (clientLevel != null) {
            this.lighting.updateLevel(clientLevel.effects().constantAmbientLight());
        }
    }

    public PanoramaRenderer getPanorama() {
        return this.panorama;
    }
}

