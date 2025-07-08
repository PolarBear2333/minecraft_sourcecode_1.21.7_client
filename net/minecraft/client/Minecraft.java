/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.mojang.authlib.minecraft.BanDetails
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.minecraft.UserApiService$UserFlag
 *  com.mojang.authlib.minecraft.UserApiService$UserProperties
 *  com.mojang.authlib.yggdrasil.ProfileActionType
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.authlib.yggdrasil.ServicesKeyType
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.jtracy.DiscontinuousFrame
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2BooleanFunction
 *  javax.annotation.Nullable
 *  org.apache.commons.io.FileUtils
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.ClientShutdownWatchdog;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.DataFixer;
import com.mojang.jtracy.DiscontinuousFrame;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.management.ManagementFactory;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.Optionull;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.CommandHistory;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.InputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.Options;
import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.Screenshot;
import net.minecraft.client.User;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BanNoticeScreens;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.profiling.ClientMetricsSamplersProvider;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.DryFoliageColorReloadListener;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.WaypointStyleManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicInfo;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindResolver;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.Dialogs;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DialogTags;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.FileZipper;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.ModCheck;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.io.FileUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public class Minecraft
extends ReentrantBlockableEventLoop<Runnable>
implements WindowEventHandler {
    static Minecraft instance;
    private static final Logger LOGGER;
    public static final boolean ON_OSX;
    private static final int MAX_TICKS_PER_UPDATE = 10;
    public static final ResourceLocation DEFAULT_FONT;
    public static final ResourceLocation UNIFORM_FONT;
    public static final ResourceLocation ALT_FONT;
    private static final ResourceLocation REGIONAL_COMPLIANCIES;
    private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK;
    private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    private static final Component SAVING_LEVEL;
    public static final String UPDATE_DRIVERS_ADVICE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
    private final long canary = Double.doubleToLongBits(Math.PI);
    private final Path resourcePackDirectory;
    private final CompletableFuture<ProfileResult> profileFuture;
    private final TextureManager textureManager;
    private final ShaderManager shaderManager;
    private final DataFixer fixerUpper;
    private final VirtualScreen virtualScreen;
    private final Window window;
    private final DeltaTracker.Timer deltaTracker = new DeltaTracker.Timer(20.0f, 0L, this::getTickTargetMillis);
    private final RenderBuffers renderBuffers;
    public final LevelRenderer levelRenderer;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemModelResolver itemModelResolver;
    private final ItemRenderer itemRenderer;
    private final MapRenderer mapRenderer;
    public final ParticleEngine particleEngine;
    private final User user;
    public final Font font;
    public final Font fontFilterFishy;
    public final GameRenderer gameRenderer;
    public final DebugRenderer debugRenderer;
    private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference();
    public final Gui gui;
    public final Options options;
    private final HotbarManager hotbarManager;
    public final MouseHandler mouseHandler;
    public final KeyboardHandler keyboardHandler;
    private InputType lastInputType = InputType.NONE;
    public final File gameDirectory;
    private final String launchedVersion;
    private final String versionType;
    private final Proxy proxy;
    private final LevelStorageSource levelSource;
    private final boolean demo;
    private final boolean allowsMultiplayer;
    private final boolean allowsChat;
    private final ReloadableResourceManager resourceManager;
    private final VanillaPackResources vanillaPackResources;
    private final DownloadedPackSource downloadedPackSource;
    private final PackRepository resourcePackRepository;
    private final LanguageManager languageManager;
    private final BlockColors blockColors;
    private final RenderTarget mainRenderTarget;
    @Nullable
    private final TracyFrameCapture tracyFrameCapture;
    private final SoundManager soundManager;
    private final MusicManager musicManager;
    private final FontManager fontManager;
    private final SplashManager splashManager;
    private final GpuWarnlistManager gpuWarnlistManager;
    private final PeriodicNotificationManager regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, (Object2BooleanFunction<String>)((Object2BooleanFunction)Minecraft::countryEqualsISO3));
    private final YggdrasilAuthenticationService authenticationService;
    private final MinecraftSessionService minecraftSessionService;
    private final UserApiService userApiService;
    private final CompletableFuture<UserApiService.UserProperties> userPropertiesFuture;
    private final SkinManager skinManager;
    private final ModelManager modelManager;
    private final BlockRenderDispatcher blockRenderer;
    private final PaintingTextureManager paintingTextures;
    private final MapTextureManager mapTextureManager;
    private final MapDecorationTextureManager mapDecorationTextures;
    private final GuiSpriteManager guiSprites;
    private final WaypointStyleManager waypointStyles;
    private final ToastManager toastManager;
    private final Tutorial tutorial;
    private final PlayerSocialManager playerSocialManager;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final ClientTelemetryManager telemetryManager;
    private final ProfileKeyPairManager profileKeyPairManager;
    private final RealmsDataFetcher realmsDataFetcher;
    private final QuickPlayLog quickPlayLog;
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Nullable
    public ClientLevel level;
    @Nullable
    public LocalPlayer player;
    @Nullable
    private IntegratedServer singleplayerServer;
    @Nullable
    private Connection pendingConnection;
    private boolean isLocalServer;
    @Nullable
    public Entity cameraEntity;
    @Nullable
    public Entity crosshairPickEntity;
    @Nullable
    public HitResult hitResult;
    private int rightClickDelay;
    protected int missTime;
    private volatile boolean pause;
    private long lastNanoTime = Util.getNanos();
    private long lastTime;
    private int frames;
    public boolean noRender;
    @Nullable
    public Screen screen;
    @Nullable
    private Overlay overlay;
    private boolean clientLevelTeardownInProgress;
    Thread gameThread;
    private volatile boolean running;
    @Nullable
    private Supplier<CrashReport> delayedCrash;
    private static int fps;
    public String fpsString = "";
    private long frameTimeNs;
    private final FramerateLimitTracker framerateLimitTracker;
    public boolean wireframe;
    public boolean sectionPath;
    public boolean sectionVisibility;
    public boolean smartCull = true;
    private boolean windowActive;
    private final Queue<Runnable> progressTasks = Queues.newConcurrentLinkedQueue();
    @Nullable
    private CompletableFuture<Void> pendingReload;
    @Nullable
    private TutorialToast socialInteractionsToast;
    private int fpsPieRenderTicks;
    private final ContinuousProfiler fpsPieProfiler;
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private final ResourceLoadStateTracker reloadStateTracker = new ResourceLoadStateTracker();
    private long savedCpuDuration;
    private double gpuUtilization;
    @Nullable
    private TimerQuery.FrameProfile currentFrameProfile;
    private final GameNarrator narrator;
    private final ChatListener chatListener;
    private ReportingContext reportingContext;
    private final CommandHistory commandHistory;
    private final DirectoryValidator directoryValidator;
    private boolean gameLoadFinished;
    private final long clientStartTimeMs;
    private long clientTickCount;

    public Minecraft(final GameConfig gameConfig) {
        super("Client");
        Object object;
        Object object2;
        Object object3;
        instance = this;
        this.clientStartTimeMs = System.currentTimeMillis();
        this.gameDirectory = gameConfig.location.gameDirectory;
        File file = gameConfig.location.assetDirectory;
        this.resourcePackDirectory = gameConfig.location.resourcePackDirectory.toPath();
        this.launchedVersion = gameConfig.game.launchVersion;
        this.versionType = gameConfig.game.versionType;
        Path path = this.gameDirectory.toPath();
        this.directoryValidator = LevelStorageSource.parseValidator(path.resolve("allowed_symlinks.txt"));
        ClientPackSource clientPackSource = new ClientPackSource(gameConfig.location.getExternalAssetSource(), this.directoryValidator);
        this.downloadedPackSource = new DownloadedPackSource(this, path.resolve("downloads"), gameConfig.user);
        FolderRepositorySource folderRepositorySource = new FolderRepositorySource(this.resourcePackDirectory, PackType.CLIENT_RESOURCES, PackSource.DEFAULT, this.directoryValidator);
        this.resourcePackRepository = new PackRepository(clientPackSource, this.downloadedPackSource.createRepositorySource(), folderRepositorySource);
        this.vanillaPackResources = clientPackSource.getVanillaPack();
        this.proxy = gameConfig.user.proxy;
        this.authenticationService = new YggdrasilAuthenticationService(this.proxy);
        this.minecraftSessionService = this.authenticationService.createMinecraftSessionService();
        this.user = gameConfig.user.user;
        this.profileFuture = CompletableFuture.supplyAsync(() -> this.minecraftSessionService.fetchProfile(this.user.getProfileId(), true), Util.nonCriticalIoPool());
        this.userApiService = this.createUserApiService(this.authenticationService, gameConfig);
        this.userPropertiesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return this.userApiService.fetchProperties();
            }
            catch (AuthenticationException authenticationException) {
                LOGGER.error("Failed to fetch user properties", (Throwable)authenticationException);
                return UserApiService.OFFLINE_PROPERTIES;
            }
        }, Util.nonCriticalIoPool());
        LOGGER.info("Setting user: {}", (Object)this.user.getName());
        LOGGER.debug("(Session ID is {})", (Object)this.user.getSessionId());
        this.demo = gameConfig.game.demo;
        this.allowsMultiplayer = !gameConfig.game.disableMultiplayer;
        this.allowsChat = !gameConfig.game.disableChat;
        this.singleplayerServer = null;
        KeybindResolver.setKeyResolver(KeyMapping::createNameSupplier);
        this.fixerUpper = DataFixers.getDataFixer();
        this.gameThread = Thread.currentThread();
        this.options = new Options(this, this.gameDirectory);
        this.toastManager = new ToastManager(this, this.options);
        boolean bl = this.options.startedCleanly;
        this.options.startedCleanly = false;
        this.options.save();
        this.running = true;
        this.tutorial = new Tutorial(this, this.options);
        this.hotbarManager = new HotbarManager(path, this.fixerUpper);
        LOGGER.info("Backend library: {}", (Object)RenderSystem.getBackendDescription());
        DisplayData displayData = gameConfig.display;
        if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
            displayData = gameConfig.display.withSize(this.options.overrideWidth, this.options.overrideHeight);
        }
        if (!bl) {
            displayData = displayData.withFullscreen(false);
            this.options.fullscreenVideoModeString = null;
            LOGGER.warn("Detected unexpected shutdown during last game startup: resetting fullscreen mode");
        }
        Util.timeSource = RenderSystem.initBackendSystem();
        this.virtualScreen = new VirtualScreen(this);
        this.window = this.virtualScreen.newWindow(displayData, this.options.fullscreenVideoModeString, this.createTitle());
        this.setWindowActive(true);
        this.window.setWindowCloseCallback(new Runnable(){
            private boolean threadStarted;

            @Override
            public void run() {
                if (!this.threadStarted) {
                    this.threadStarted = true;
                    ClientShutdownWatchdog.startShutdownWatchdog(gameConfig.location.gameDirectory, Minecraft.this.gameThread.threadId());
                }
            }
        });
        GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS);
        try {
            this.window.setIcon(this.vanillaPackResources, SharedConstants.getCurrentVersion().stable() ? IconSet.RELEASE : IconSet.SNAPSHOT);
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't set icon", (Throwable)iOException);
        }
        this.mouseHandler = new MouseHandler(this);
        this.mouseHandler.setup(this.window.getWindow());
        this.keyboardHandler = new KeyboardHandler(this);
        this.keyboardHandler.setup(this.window.getWindow());
        RenderSystem.initRenderer(this.window.getWindow(), this.options.glDebugVerbosity, false, (resourceLocation, shaderType) -> this.getShaderManager().getShader((ResourceLocation)resourceLocation, (ShaderType)((Object)shaderType)), gameConfig.game.renderDebugLabels);
        LOGGER.info("Using optional rendering extensions: {}", (Object)String.join((CharSequence)", ", RenderSystem.getDevice().getEnabledExtensions()));
        this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
        this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        this.resourcePackRepository.reload();
        this.options.loadSelectedResourcePacks(this.resourcePackRepository);
        this.languageManager = new LanguageManager(this.options.languageCode, clientLanguage -> {
            if (this.player != null) {
                this.player.connection.updateSearchTrees();
            }
        });
        this.resourceManager.registerReloadListener(this.languageManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloadListener(this.textureManager);
        this.shaderManager = new ShaderManager(this.textureManager, this::triggerResourcePackRecovery);
        this.resourceManager.registerReloadListener(this.shaderManager);
        this.skinManager = new SkinManager(file.toPath().resolve("skins"), this.minecraftSessionService, this);
        this.levelSource = new LevelStorageSource(path.resolve("saves"), path.resolve("backups"), this.directoryValidator, this.fixerUpper);
        this.commandHistory = new CommandHistory(path);
        this.musicManager = new MusicManager(this);
        this.soundManager = new SoundManager(this.options, this.musicManager);
        this.resourceManager.registerReloadListener(this.soundManager);
        this.splashManager = new SplashManager(this.user);
        this.resourceManager.registerReloadListener(this.splashManager);
        this.fontManager = new FontManager(this.textureManager);
        this.font = this.fontManager.createFont();
        this.fontFilterFishy = this.fontManager.createFontFilterFishy();
        this.resourceManager.registerReloadListener(this.fontManager);
        this.updateFontOptions();
        this.resourceManager.registerReloadListener(new GrassColorReloadListener());
        this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
        this.resourceManager.registerReloadListener(new DryFoliageColorReloadListener());
        this.window.setErrorSection("Startup");
        RenderSystem.setupDefaultState();
        this.window.setErrorSection("Post startup");
        this.blockColors = BlockColors.createDefault();
        this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels().get());
        this.resourceManager.registerReloadListener(this.modelManager);
        EquipmentAssetManager equipmentAssetManager = new EquipmentAssetManager();
        this.resourceManager.registerReloadListener(equipmentAssetManager);
        this.itemModelResolver = new ItemModelResolver(this.modelManager);
        this.itemRenderer = new ItemRenderer(this.itemModelResolver);
        this.mapTextureManager = new MapTextureManager(this.textureManager);
        this.mapDecorationTextures = new MapDecorationTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.mapDecorationTextures);
        this.mapRenderer = new MapRenderer(this.mapDecorationTextures, this.mapTextureManager);
        try {
            int n = Runtime.getRuntime().availableProcessors();
            Tesselator.init();
            this.renderBuffers = new RenderBuffers(n);
        }
        catch (OutOfMemoryError outOfMemoryError) {
            TinyFileDialogs.tinyfd_messageBox((CharSequence)"Minecraft", (CharSequence)("Oh no! The game was unable to allocate memory off-heap while trying to start. You may try to free some memory by closing other applications on your computer, check that your system meets the minimum requirements, and try again. If the problem persists, please visit: " + String.valueOf(CommonLinks.GENERAL_HELP)), (CharSequence)"ok", (CharSequence)"error", (boolean)true);
            throw new SilentInitException("Unable to allocate render buffers", outOfMemoryError);
        }
        this.playerSocialManager = new PlayerSocialManager(this, this.userApiService);
        this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), this.modelManager.specialBlockModelRenderer(), this.blockColors);
        this.resourceManager.registerReloadListener(this.blockRenderer);
        this.entityRenderDispatcher = new EntityRenderDispatcher(this, this.textureManager, this.itemModelResolver, this.itemRenderer, this.mapRenderer, this.blockRenderer, this.font, this.options, this.modelManager.entityModels(), equipmentAssetManager);
        this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
        this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.font, this.modelManager.entityModels(), this.blockRenderer, this.itemModelResolver, this.itemRenderer, this.entityRenderDispatcher);
        this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
        this.particleEngine = new ParticleEngine(this.level, this.textureManager);
        this.resourceManager.registerReloadListener(this.particleEngine);
        this.paintingTextures = new PaintingTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.paintingTextures);
        this.guiSprites = new GuiSpriteManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.guiSprites);
        this.waypointStyles = new WaypointStyleManager();
        this.resourceManager.registerReloadListener(this.waypointStyles);
        this.gameRenderer = new GameRenderer(this, this.entityRenderDispatcher.getItemInHandRenderer(), this.renderBuffers);
        this.levelRenderer = new LevelRenderer(this, this.entityRenderDispatcher, this.blockEntityRenderDispatcher, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.levelRenderer);
        this.resourceManager.registerReloadListener(this.levelRenderer.getCloudRenderer());
        this.gpuWarnlistManager = new GpuWarnlistManager();
        this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
        this.resourceManager.registerReloadListener(this.regionalCompliancies);
        this.gui = new Gui(this);
        this.debugRenderer = new DebugRenderer(this);
        RealmsClient realmsClient = RealmsClient.getOrCreate(this);
        this.realmsDataFetcher = new RealmsDataFetcher(realmsClient);
        RenderSystem.setErrorCallback(this::onFullscreenError);
        if (this.mainRenderTarget.width != this.window.getWidth() || this.mainRenderTarget.height != this.window.getHeight()) {
            object3 = new StringBuilder("Recovering from unsupported resolution (" + this.window.getWidth() + "x" + this.window.getHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).");
            try {
                object2 = RenderSystem.getDevice();
                object = object2.getLastDebugMessages();
                if (!object.isEmpty()) {
                    ((StringBuilder)object3).append("\n\nReported GL debug messages:\n").append(String.join((CharSequence)"\n", object));
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.window.setWindowed(this.mainRenderTarget.width, this.mainRenderTarget.height);
            TinyFileDialogs.tinyfd_messageBox((CharSequence)"Minecraft", (CharSequence)((StringBuilder)object3).toString(), (CharSequence)"ok", (CharSequence)"error", (boolean)false);
        } else if (this.options.fullscreen().get().booleanValue() && !this.window.isFullscreen()) {
            if (bl) {
                this.window.toggleFullScreen();
                this.options.fullscreen().set(this.window.isFullscreen());
            } else {
                this.options.fullscreen().set(false);
            }
        }
        this.window.updateVsync(this.options.enableVsync().get());
        this.window.updateRawMouseInput(this.options.rawMouseInput().get());
        this.window.setDefaultErrorCallback();
        this.resizeDisplay();
        this.gameRenderer.preloadUiShader(this.vanillaPackResources.asProvider());
        this.telemetryManager = new ClientTelemetryManager(this, this.userApiService, this.user);
        this.profileKeyPairManager = ProfileKeyPairManager.create(this.userApiService, this.user, path);
        this.narrator = new GameNarrator(this);
        this.narrator.checkStatus(this.options.narrator().get() != NarratorStatus.OFF);
        this.chatListener = new ChatListener(this);
        this.chatListener.setMessageDelay(this.options.chatDelay().get());
        this.reportingContext = ReportingContext.create(ReportEnvironment.local(), this.userApiService);
        TitleScreen.registerTextures(this.textureManager);
        LoadingOverlay.registerTextures(this.textureManager);
        this.gameRenderer.getPanorama().registerTextures(this.textureManager);
        this.setScreen(new GenericMessageScreen(Component.translatable("gui.loadingMinecraft")));
        object3 = this.resourcePackRepository.openAllSelected();
        this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, (List<PackResources>)object3);
        object2 = this.resourceManager.createReload(Util.backgroundExecutor().forName("resourceLoad"), this, RESOURCE_RELOAD_INITIAL_TASK, (List<PackResources>)object3);
        GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
        object = new GameLoadCookie(realmsClient, gameConfig.quickPlay);
        this.setOverlay(new LoadingOverlay(this, (ReloadInstance)object2, arg_0 -> this.lambda$new$6((GameLoadCookie)object, arg_0), false));
        this.quickPlayLog = QuickPlayLog.of(gameConfig.quickPlay.logPath());
        this.framerateLimitTracker = new FramerateLimitTracker(this.options, this);
        this.fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks, this.framerateLimitTracker::isHeavilyThrottled);
        this.tracyFrameCapture = TracyClient.isAvailable() && gameConfig.game.captureTracyImages ? new TracyFrameCapture() : null;
    }

    private void onResourceLoadFinished(@Nullable GameLoadCookie gameLoadCookie) {
        if (!this.gameLoadFinished) {
            this.gameLoadFinished = true;
            this.onGameLoadFinished(gameLoadCookie);
        }
    }

    private void onGameLoadFinished(@Nullable GameLoadCookie gameLoadCookie) {
        Runnable runnable = this.buildInitialScreens(gameLoadCookie);
        GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
        GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS);
        GameLoadTimesEvent.INSTANCE.send(this.telemetryManager.getOutsideSessionSender());
        runnable.run();
        this.options.startedCleanly = true;
        this.options.save();
    }

    public boolean isGameLoadFinished() {
        return this.gameLoadFinished;
    }

    private Runnable buildInitialScreens(@Nullable GameLoadCookie gameLoadCookie) {
        ArrayList<Function<Runnable, Screen>> arrayList = new ArrayList<Function<Runnable, Screen>>();
        boolean bl = this.addInitialScreens(arrayList);
        Runnable runnable = () -> {
            if (gameLoadCookie != null && gameLoadCookie.quickPlayData.isEnabled()) {
                QuickPlay.connect(this, gameLoadCookie.quickPlayData.variant(), gameLoadCookie.realmsClient());
            } else {
                this.setScreen(new TitleScreen(true, new LogoRenderer(bl)));
            }
        };
        for (Function function : Lists.reverse(arrayList)) {
            Screen screen = (Screen)function.apply(runnable);
            runnable = () -> this.setScreen(screen);
        }
        return runnable;
    }

    private boolean addInitialScreens(List<Function<Runnable, Screen>> list) {
        ProfileResult profileResult;
        boolean bl = false;
        if (this.options.onboardAccessibility) {
            list.add(runnable -> new AccessibilityOnboardingScreen(this.options, (Runnable)runnable));
            bl = true;
        }
        BanDetails banDetails = this.multiplayerBan();
        if (banDetails != null) {
            list.add(runnable -> BanNoticeScreens.create(bl -> {
                if (bl) {
                    Util.getPlatform().openUri(CommonLinks.SUSPENSION_HELP);
                }
                runnable.run();
            }, banDetails));
        }
        if ((profileResult = this.profileFuture.join()) != null) {
            GameProfile gameProfile = profileResult.profile();
            Set set = profileResult.actions();
            if (set.contains(ProfileActionType.FORCED_NAME_CHANGE)) {
                list.add(runnable -> BanNoticeScreens.createNameBan(gameProfile.getName(), runnable));
            }
            if (set.contains(ProfileActionType.USING_BANNED_SKIN)) {
                list.add(BanNoticeScreens::createSkinBan);
            }
        }
        return bl;
    }

    private static boolean countryEqualsISO3(Object object) {
        try {
            return Locale.getDefault().getISO3Country().equals(object);
        }
        catch (MissingResourceException missingResourceException) {
            return false;
        }
    }

    public void updateTitle() {
        this.window.setTitle(this.createTitle());
    }

    private String createTitle() {
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getCurrentVersion().name());
        ClientPacketListener clientPacketListener = this.getConnection();
        if (clientPacketListener != null && clientPacketListener.getConnection().isConnected()) {
            stringBuilder.append(" - ");
            ServerData serverData = this.getCurrentServer();
            if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
                stringBuilder.append(I18n.get("title.singleplayer", new Object[0]));
            } else if (serverData != null && serverData.isRealm()) {
                stringBuilder.append(I18n.get("title.multiplayer.realms", new Object[0]));
            } else if (this.singleplayerServer != null || serverData != null && serverData.isLan()) {
                stringBuilder.append(I18n.get("title.multiplayer.lan", new Object[0]));
            } else {
                stringBuilder.append(I18n.get("title.multiplayer.other", new Object[0]));
            }
        }
        return stringBuilder.toString();
    }

    private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig) {
        if (gameConfig.user.user.getType() != User.Type.MSA) {
            return UserApiService.OFFLINE;
        }
        return yggdrasilAuthenticationService.createUserApiService(gameConfig.user.user.getAccessToken());
    }

    public static ModCheck checkModStatus() {
        return ModCheck.identify("vanilla", ClientBrandRetriever::getClientModName, "Client", Minecraft.class);
    }

    private void rollbackResourcePacks(Throwable throwable, @Nullable GameLoadCookie gameLoadCookie) {
        if (this.resourcePackRepository.getSelectedIds().size() > 1) {
            this.clearResourcePacksOnError(throwable, null, gameLoadCookie);
        } else {
            Util.throwAsRuntime(throwable);
        }
    }

    public void clearResourcePacksOnError(Throwable throwable, @Nullable Component component, @Nullable GameLoadCookie gameLoadCookie) {
        LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", throwable);
        this.reloadStateTracker.startRecovery(throwable);
        this.downloadedPackSource.onRecovery();
        this.resourcePackRepository.setSelected(Collections.emptyList());
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();
        this.options.save();
        this.reloadResourcePacks(true, gameLoadCookie).thenRunAsync(() -> this.addResourcePackLoadFailToast(component), this);
    }

    private void abortResourcePackRecovery() {
        this.setOverlay(null);
        if (this.level != null) {
            this.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
            this.disconnectWithProgressScreen();
        }
        this.setScreen(new TitleScreen());
        this.addResourcePackLoadFailToast(null);
    }

    private void addResourcePackLoadFailToast(@Nullable Component component) {
        ToastManager toastManager = this.getToastManager();
        SystemToast.addOrUpdate(toastManager, SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("resourcePack.load_fail"), component);
    }

    public void triggerResourcePackRecovery(Exception exception) {
        if (!this.resourcePackRepository.isAbleToClearAnyPack()) {
            if (this.resourcePackRepository.getSelectedIds().size() <= 1) {
                LOGGER.error(LogUtils.FATAL_MARKER, exception.getMessage(), (Throwable)exception);
                this.emergencySaveAndCrash(new CrashReport(exception.getMessage(), exception));
            } else {
                this.schedule(this::abortResourcePackRecovery);
            }
            return;
        }
        this.clearResourcePacksOnError(exception, Component.translatable("resourcePack.runtime_failure"), null);
    }

    public void run() {
        this.gameThread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) {
            this.gameThread.setPriority(10);
        }
        DiscontinuousFrame discontinuousFrame = TracyClient.createDiscontinuousFrame((String)"Client Tick");
        try {
            boolean bl = false;
            while (this.running) {
                this.handleDelayedCrash();
                try {
                    SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean bl2 = this.getDebugOverlay().showProfilerChart();
                    try (Profiler.Scope scope = Profiler.use(this.constructProfiler(bl2, singleTickProfiler));){
                        this.metricsRecorder.startTick();
                        discontinuousFrame.start();
                        this.runTick(!bl);
                        discontinuousFrame.end();
                        this.metricsRecorder.endTick();
                    }
                    this.finishProfilers(bl2, singleTickProfiler);
                }
                catch (OutOfMemoryError outOfMemoryError) {
                    if (bl) {
                        throw outOfMemoryError;
                    }
                    this.emergencySave();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", (Throwable)outOfMemoryError);
                    bl = true;
                }
            }
        }
        catch (ReportedException reportedException) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", (Throwable)reportedException);
            this.emergencySaveAndCrash(reportedException.getReport());
        }
        catch (Throwable throwable) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", throwable);
            this.emergencySaveAndCrash(new CrashReport("Unexpected error", throwable));
        }
    }

    void updateFontOptions() {
        this.fontManager.updateOptions(this.options);
    }

    private void onFullscreenError(int n, long l) {
        this.options.enableVsync().set(false);
        this.options.save();
    }

    public RenderTarget getMainRenderTarget() {
        return this.mainRenderTarget;
    }

    public String getLaunchedVersion() {
        return this.launchedVersion;
    }

    public String getVersionType() {
        return this.versionType;
    }

    public void delayCrash(CrashReport crashReport) {
        this.delayedCrash = () -> this.fillReport(crashReport);
    }

    public void delayCrashRaw(CrashReport crashReport) {
        this.delayedCrash = () -> crashReport;
    }

    private void handleDelayedCrash() {
        if (this.delayedCrash != null) {
            Minecraft.crash(this, this.gameDirectory, this.delayedCrash.get());
        }
    }

    public void emergencySaveAndCrash(CrashReport crashReport) {
        MemoryReserve.release();
        CrashReport crashReport2 = this.fillReport(crashReport);
        this.emergencySave();
        Minecraft.crash(this, this.gameDirectory, crashReport2);
    }

    public static int saveReport(File file, CrashReport crashReport) {
        Path path = file.toPath().resolve("crash-reports");
        Path path2 = path.resolve("crash-" + Util.getFilenameFormattedDateTime() + "-client.txt");
        Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport(ReportType.CRASH));
        if (crashReport.getSaveFile() != null) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + String.valueOf(crashReport.getSaveFile().toAbsolutePath()));
            return -1;
        }
        if (crashReport.saveToFile(path2, ReportType.CRASH)) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + String.valueOf(path2.toAbsolutePath()));
            return -1;
        }
        Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
        return -2;
    }

    public static void crash(@Nullable Minecraft minecraft, File file, CrashReport crashReport) {
        int n = Minecraft.saveReport(file, crashReport);
        if (minecraft != null) {
            minecraft.soundManager.emergencyShutdown();
        }
        System.exit(n);
    }

    public boolean isEnforceUnicode() {
        return this.options.forceUnicodeFont().get();
    }

    public CompletableFuture<Void> reloadResourcePacks() {
        return this.reloadResourcePacks(false, null);
    }

    private CompletableFuture<Void> reloadResourcePacks(boolean bl, @Nullable GameLoadCookie gameLoadCookie) {
        if (this.pendingReload != null) {
            return this.pendingReload;
        }
        CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        if (!bl && this.overlay instanceof LoadingOverlay) {
            this.pendingReload = completableFuture;
            return completableFuture;
        }
        this.resourcePackRepository.reload();
        List<PackResources> list = this.resourcePackRepository.openAllSelected();
        if (!bl) {
            this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, list);
        }
        this.setOverlay(new LoadingOverlay(this, this.resourceManager.createReload(Util.backgroundExecutor().forName("resourceLoad"), this, RESOURCE_RELOAD_INITIAL_TASK, list), optional -> Util.ifElse(optional, throwable -> {
            if (bl) {
                this.downloadedPackSource.onRecoveryFailure();
                this.abortResourcePackRecovery();
            } else {
                this.rollbackResourcePacks((Throwable)throwable, gameLoadCookie);
            }
        }, () -> {
            this.levelRenderer.allChanged();
            this.reloadStateTracker.finishReload();
            this.downloadedPackSource.onReloadSuccess();
            completableFuture.complete(null);
            this.onResourceLoadFinished(gameLoadCookie);
        }), !bl));
        return completableFuture;
    }

    private void selfTest() {
        boolean bl = false;
        BlockModelShaper blockModelShaper = this.getBlockRenderer().getBlockModelShaper();
        BlockStateModel blockStateModel = blockModelShaper.getModelManager().getMissingBlockStateModel();
        for (Block block : BuiltInRegistries.BLOCK) {
            for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
                Object object2;
                if (blockState.getRenderShape() != RenderShape.MODEL || (object2 = blockModelShaper.getBlockModel(blockState)) != blockStateModel) continue;
                LOGGER.debug("Missing model for: {}", (Object)blockState);
                bl = true;
            }
        }
        TextureAtlasSprite textureAtlasSprite = blockStateModel.particleIcon();
        for (Object object : BuiltInRegistries.BLOCK) {
            for (Object object2 : ((Block)object).getStateDefinition().getPossibleStates()) {
                TextureAtlasSprite textureAtlasSprite2 = blockModelShaper.getParticleIcon((BlockState)object2);
                if (((BlockBehaviour.BlockStateBase)object2).isAir() || textureAtlasSprite2 != textureAtlasSprite) continue;
                LOGGER.debug("Missing particle icon for: {}", object2);
            }
        }
        BuiltInRegistries.ITEM.listElements().forEach(reference -> {
            Item item = (Item)reference.value();
            String string = item.getDescriptionId();
            String string2 = Component.translatable(string).getString();
            if (string2.toLowerCase(Locale.ROOT).equals(item.getDescriptionId())) {
                LOGGER.debug("Missing translation for: {} {} {}", new Object[]{reference.key().location(), string, item});
            }
        });
        bl |= MenuScreens.selfTest();
        if (bl |= EntityRenderers.validateRegistrations()) {
            throw new IllegalStateException("Your game data is foobar, fix the errors above!");
        }
    }

    public LevelStorageSource getLevelSource() {
        return this.levelSource;
    }

    private void openChatScreen(String string) {
        ChatStatus chatStatus = this.getChatStatus();
        if (!chatStatus.isChatAllowed(this.isLocalServer())) {
            if (this.gui.isShowingChatDisabledByPlayer()) {
                this.gui.setChatDisabledByPlayerShown(false);
                this.setScreen(new ConfirmLinkScreen(bl -> {
                    if (bl) {
                        Util.getPlatform().openUri(CommonLinks.ACCOUNT_SETTINGS);
                    }
                    this.setScreen(null);
                }, ChatStatus.INFO_DISABLED_BY_PROFILE, CommonLinks.ACCOUNT_SETTINGS, true));
            } else {
                Component component = chatStatus.getMessage();
                this.gui.setOverlayMessage(component, false);
                this.narrator.saySystemNow(component);
                this.gui.setChatDisabledByPlayerShown(chatStatus == ChatStatus.DISABLED_BY_PROFILE);
            }
        } else {
            this.setScreen(new ChatScreen(string));
        }
    }

    public void setScreen(@Nullable Screen screen) {
        if (SharedConstants.IS_RUNNING_IN_IDE && Thread.currentThread() != this.gameThread) {
            LOGGER.error("setScreen called from non-game thread");
        }
        if (this.screen != null) {
            this.screen.removed();
        } else {
            this.setLastInputType(InputType.NONE);
        }
        if (screen == null && this.clientLevelTeardownInProgress) {
            throw new IllegalStateException("Trying to return to in-game GUI during disconnection");
        }
        if (screen == null && this.level == null) {
            screen = new TitleScreen();
        } else if (screen == null && this.player.isDeadOrDying()) {
            if (this.player.shouldShowDeathScreen()) {
                screen = new DeathScreen(null, this.level.getLevelData().isHardcore());
            } else {
                this.player.respawn();
            }
        }
        this.screen = screen;
        if (this.screen != null) {
            this.screen.added();
        }
        if (screen != null) {
            this.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            screen.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
            this.noRender = false;
        } else {
            this.soundManager.resume();
            this.mouseHandler.grabMouse();
        }
        this.updateTitle();
    }

    public void setOverlay(@Nullable Overlay overlay) {
        this.overlay = overlay;
    }

    public void destroy() {
        try {
            LOGGER.info("Stopping!");
            try {
                this.narrator.destroy();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                if (this.level != null) {
                    this.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
                }
                this.disconnectWithProgressScreen();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (this.screen != null) {
                this.screen.removed();
            }
            this.close();
        }
        finally {
            Util.timeSource = System::nanoTime;
            if (this.delayedCrash == null) {
                System.exit(0);
            }
        }
    }

    @Override
    public void close() {
        if (this.currentFrameProfile != null) {
            this.currentFrameProfile.cancel();
        }
        try {
            this.telemetryManager.close();
            this.regionalCompliancies.close();
            this.modelManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.shaderManager.close();
            this.levelRenderer.close();
            this.soundManager.destroy();
            this.particleEngine.close();
            this.paintingTextures.close();
            this.mapDecorationTextures.close();
            this.guiSprites.close();
            this.mapTextureManager.close();
            this.textureManager.close();
            this.resourceManager.close();
            if (this.tracyFrameCapture != null) {
                this.tracyFrameCapture.close();
            }
            FreeTypeUtil.destroy();
            Util.shutdownExecutors();
            RenderSystem.getDevice().close();
        }
        catch (Throwable throwable) {
            LOGGER.error("Shutdown failure!", throwable);
            throw throwable;
        }
        finally {
            this.virtualScreen.close();
            this.window.close();
        }
    }

    private void runTick(boolean bl) {
        boolean bl2;
        Object object;
        this.window.setErrorSection("Pre render");
        if (this.window.shouldClose()) {
            this.stop();
        }
        if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
            object = this.pendingReload;
            this.pendingReload = null;
            this.reloadResourcePacks().thenRun(() -> Minecraft.lambda$runTick$22((CompletableFuture)object));
        }
        while ((object = this.progressTasks.poll()) != null) {
            object.run();
        }
        int n = this.deltaTracker.advanceTime(Util.getMillis(), bl);
        ProfilerFiller profilerFiller = Profiler.get();
        if (bl) {
            profilerFiller.push("scheduledExecutables");
            this.runAllTasks();
            profilerFiller.pop();
            profilerFiller.push("tick");
            for (int i = 0; i < Math.min(10, n); ++i) {
                profilerFiller.incrementCounter("clientTick");
                this.tick();
            }
            profilerFiller.pop();
        }
        this.window.setErrorSection("Render");
        profilerFiller.push("gpuAsync");
        RenderSystem.executePendingTasks();
        profilerFiller.popPush("sound");
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        profilerFiller.popPush("toasts");
        this.toastManager.update();
        profilerFiller.popPush("render");
        long l = Util.getNanos();
        if (this.getDebugOverlay().showDebugScreen() || this.metricsRecorder.isRecording()) {
            boolean bl3 = bl2 = this.currentFrameProfile == null || this.currentFrameProfile.isDone();
            if (bl2) {
                TimerQuery.getInstance().ifPresent(TimerQuery::beginProfile);
            }
        } else {
            bl2 = false;
            this.gpuUtilization = 0.0;
        }
        RenderTarget renderTarget = this.getMainRenderTarget();
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1.0);
        profilerFiller.push("mouse");
        this.mouseHandler.handleAccumulatedMovement();
        profilerFiller.pop();
        if (!this.noRender) {
            profilerFiller.popPush("gameRenderer");
            this.gameRenderer.render(this.deltaTracker, bl);
            profilerFiller.pop();
        }
        profilerFiller.push("blit");
        if (!this.window.isMinimized()) {
            renderTarget.blitToScreen();
        }
        this.frameTimeNs = Util.getNanos() - l;
        if (bl2) {
            TimerQuery.getInstance().ifPresent(timerQuery -> {
                this.currentFrameProfile = timerQuery.endProfile();
            });
        }
        profilerFiller.popPush("updateDisplay");
        if (this.tracyFrameCapture != null) {
            this.tracyFrameCapture.upload();
            this.tracyFrameCapture.capture(renderTarget);
        }
        this.window.updateDisplay(this.tracyFrameCapture);
        int n2 = this.framerateLimitTracker.getFramerateLimit();
        if (n2 < 260) {
            RenderSystem.limitDisplayFPS(n2);
        }
        profilerFiller.popPush("yield");
        Thread.yield();
        profilerFiller.pop();
        this.window.setErrorSection("Post render");
        ++this.frames;
        boolean bl4 = this.pause;
        boolean bl5 = this.pause = this.hasSingleplayerServer() && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen()) && !this.singleplayerServer.isPublished();
        if (!bl4 && this.pause) {
            this.soundManager.pauseAllExcept(SoundSource.MUSIC, SoundSource.UI);
        }
        this.deltaTracker.updatePauseState(this.pause);
        this.deltaTracker.updateFrozenState(!this.isLevelRunningNormally());
        long l2 = Util.getNanos();
        long l3 = l2 - this.lastNanoTime;
        if (bl2) {
            this.savedCpuDuration = l3;
        }
        this.getDebugOverlay().logFrameDuration(l3);
        this.lastNanoTime = l2;
        profilerFiller.push("fpsUpdate");
        if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
            this.gpuUtilization = (double)this.currentFrameProfile.get() * 100.0 / (double)this.savedCpuDuration;
        }
        while (Util.getMillis() >= this.lastTime + 1000L) {
            Object object2 = this.gpuUtilization > 0.0 ? " GPU: " + (this.gpuUtilization > 100.0 ? String.valueOf(ChatFormatting.RED) + "100%" : Math.round(this.gpuUtilization) + "%") : "";
            fps = this.frames;
            this.fpsString = String.format(Locale.ROOT, "%d fps T: %s%s%s%s B: %d%s", fps, n2 == 260 ? "inf" : Integer.valueOf(n2), this.options.enableVsync().get() != false ? " vsync " : " ", this.options.graphicsMode().get(), this.options.cloudStatus().get() == CloudStatus.OFF ? "" : (this.options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"), this.options.biomeBlendRadius().get(), object2);
            this.lastTime += 1000L;
            this.frames = 0;
        }
        profilerFiller.pop();
    }

    private ProfilerFiller constructProfiler(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
        ProfilerFiller profilerFiller;
        if (!bl) {
            this.fpsPieProfiler.disable();
            if (!this.metricsRecorder.isRecording() && singleTickProfiler == null) {
                return InactiveProfiler.INSTANCE;
            }
        }
        if (bl) {
            if (!this.fpsPieProfiler.isEnabled()) {
                this.fpsPieRenderTicks = 0;
                this.fpsPieProfiler.enable();
            }
            ++this.fpsPieRenderTicks;
            profilerFiller = this.fpsPieProfiler.getFiller();
        } else {
            profilerFiller = InactiveProfiler.INSTANCE;
        }
        if (this.metricsRecorder.isRecording()) {
            profilerFiller = ProfilerFiller.combine(profilerFiller, this.metricsRecorder.getProfiler());
        }
        return SingleTickProfiler.decorateFiller(profilerFiller, singleTickProfiler);
    }

    private void finishProfilers(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
        if (singleTickProfiler != null) {
            singleTickProfiler.endTick();
        }
        ProfilerPieChart profilerPieChart = this.getDebugOverlay().getProfilerPieChart();
        if (bl) {
            profilerPieChart.setPieChartResults(this.fpsPieProfiler.getResults());
        } else {
            profilerPieChart.setPieChartResults(null);
        }
    }

    @Override
    public void resizeDisplay() {
        int n = this.window.calculateScale(this.options.guiScale().get(), this.isEnforceUnicode());
        this.window.setGuiScale(n);
        if (this.screen != null) {
            this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
        }
        RenderTarget renderTarget = this.getMainRenderTarget();
        renderTarget.resize(this.window.getWidth(), this.window.getHeight());
        this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
        this.mouseHandler.setIgnoreFirstMove();
    }

    @Override
    public void cursorEntered() {
        this.mouseHandler.cursorEntered();
    }

    public int getFps() {
        return fps;
    }

    public long getFrameTimeNs() {
        return this.frameTimeNs;
    }

    private void emergencySave() {
        MemoryReserve.release();
        try {
            if (this.isLocalServer && this.singleplayerServer != null) {
                this.singleplayerServer.halt(true);
            }
            this.disconnectWithSavingScreen();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        System.gc();
    }

    public boolean debugClientMetricsStart(Consumer<Component> consumer) {
        Consumer<Path> consumer2;
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsStop();
            return false;
        }
        Consumer<ProfileResults> consumer3 = profileResults -> {
            if (profileResults == EmptyProfileResults.EMPTY) {
                return;
            }
            int n = profileResults.getTickDuration();
            double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
            this.execute(() -> consumer.accept(Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), n, String.format(Locale.ROOT, "%.2f", (double)n / d))));
        };
        Consumer<Path> consumer4 = path -> {
            MutableComponent mutableComponent = Component.literal(path.toString()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(path.getParent())));
            this.execute(() -> consumer.accept(Component.translatable("debug.profiling.stop", mutableComponent)));
        };
        SystemReport systemReport = Minecraft.fillSystemReport(new SystemReport(), this, this.languageManager, this.launchedVersion, this.options);
        Consumer<List> consumer5 = list -> {
            Path path = this.archiveProfilingReport(systemReport, (List<Path>)list);
            consumer4.accept(path);
        };
        if (this.singleplayerServer == null) {
            consumer2 = path -> consumer5.accept((List)ImmutableList.of((Object)path));
        } else {
            this.singleplayerServer.fillSystemReport(systemReport);
            CompletableFuture completableFuture = new CompletableFuture();
            CompletableFuture completableFuture2 = new CompletableFuture();
            CompletableFuture.allOf(completableFuture, completableFuture2).thenRunAsync(() -> consumer5.accept((List)ImmutableList.of((Object)((Path)completableFuture.join()), (Object)((Path)completableFuture2.join()))), Util.ioPool());
            this.singleplayerServer.startRecordingMetrics(profileResults -> {}, completableFuture2::complete);
            consumer2 = completableFuture::complete;
        }
        this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ClientMetricsSamplersProvider(Util.timeSource, this.levelRenderer), Util.timeSource, Util.ioPool(), new MetricsPersister("client"), profileResults -> {
            this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
            consumer3.accept((ProfileResults)profileResults);
        }, consumer2);
        return true;
    }

    private void debugClientMetricsStop() {
        this.metricsRecorder.end();
        if (this.singleplayerServer != null) {
            this.singleplayerServer.finishRecordingMetrics();
        }
    }

    private void debugClientMetricsCancel() {
        this.metricsRecorder.cancel();
        if (this.singleplayerServer != null) {
            this.singleplayerServer.cancelRecordingMetrics();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path archiveProfilingReport(SystemReport systemReport, List<Path> list) {
        Path path;
        Iterator<Path> iterator;
        String string = this.isLocalServer() ? this.getSingleplayerServer().getWorldData().getLevelName() : ((iterator = this.getCurrentServer()) != null ? ((ServerData)((Object)iterator)).name : "unknown");
        try {
            iterator = String.format(Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), string, SharedConstants.getCurrentVersion().id());
            String object = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, (String)((Object)iterator), ".zip");
            path = MetricsPersister.PROFILING_RESULTS_DIR.resolve(object);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        try {
            iterator = new FileZipper(path);
            try {
                ((FileZipper)((Object)iterator)).add(Paths.get("system.txt", new String[0]), systemReport.toLineSeparatedString());
                ((FileZipper)((Object)iterator)).add(Paths.get("client", new String[0]).resolve(this.options.getFile().getName()), this.options.dumpOptionsForReport());
                list.forEach(((FileZipper)((Object)iterator))::add);
            }
            finally {
                ((FileZipper)((Object)iterator)).close();
            }
        }
        finally {
            for (Path path2 : list) {
                try {
                    FileUtils.forceDelete((File)path2.toFile());
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete temporary profiling result {}", (Object)path2, (Object)iOException);
                }
            }
        }
        return path;
    }

    public void stop() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void pauseGame(boolean bl) {
        boolean bl2;
        if (this.screen != null) {
            return;
        }
        boolean bl3 = bl2 = this.hasSingleplayerServer() && !this.singleplayerServer.isPublished();
        if (bl2) {
            this.setScreen(new PauseScreen(!bl));
        } else {
            this.setScreen(new PauseScreen(true));
        }
    }

    private void continueAttack(boolean bl) {
        if (!bl) {
            this.missTime = 0;
        }
        if (this.missTime > 0 || this.player.isUsingItem()) {
            return;
        }
        if (bl && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
            Direction direction;
            BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!this.level.getBlockState(blockPos).isAir() && this.gameMode.continueDestroyBlock(blockPos, direction = blockHitResult.getDirection())) {
                this.particleEngine.crack(blockPos, direction);
                this.player.swing(InteractionHand.MAIN_HAND);
            }
            return;
        }
        this.gameMode.stopDestroyBlock();
    }

    private boolean startAttack() {
        if (this.missTime > 0) {
            return false;
        }
        if (this.hitResult == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.gameMode.hasMissTime()) {
                this.missTime = 10;
            }
            return false;
        }
        if (this.player.isHandsBusy()) {
            return false;
        }
        ItemStack itemStack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemStack.isItemEnabled(this.level.enabledFeatures())) {
            return false;
        }
        boolean bl = false;
        switch (this.hitResult.getType()) {
            case ENTITY: {
                this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
                break;
            }
            case BLOCK: {
                BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (!this.level.getBlockState(blockPos).isAir()) {
                    this.gameMode.startDestroyBlock(blockPos, blockHitResult.getDirection());
                    if (!this.level.getBlockState(blockPos).isAir()) break;
                    bl = true;
                    break;
                }
            }
            case MISS: {
                if (this.gameMode.hasMissTime()) {
                    this.missTime = 10;
                }
                this.player.resetAttackStrengthTicker();
            }
        }
        this.player.swing(InteractionHand.MAIN_HAND);
        return bl;
    }

    private void startUseItem() {
        if (this.gameMode.isDestroying()) {
            return;
        }
        this.rightClickDelay = 4;
        if (this.player.isHandsBusy()) {
            return;
        }
        if (this.hitResult == null) {
            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
        }
        for (InteractionHand interactionHand : InteractionHand.values()) {
            Object object;
            Object object2;
            ItemStack itemStack = this.player.getItemInHand(interactionHand);
            if (!itemStack.isItemEnabled(this.level.enabledFeatures())) {
                return;
            }
            if (this.hitResult != null) {
                switch (this.hitResult.getType()) {
                    case ENTITY: {
                        object2 = (EntityHitResult)this.hitResult;
                        object = ((EntityHitResult)object2).getEntity();
                        if (!this.level.getWorldBorder().isWithinBounds(((Entity)object).blockPosition())) {
                            return;
                        }
                        InteractionResult interactionResult = this.gameMode.interactAt(this.player, (Entity)object, (EntityHitResult)object2, interactionHand);
                        if (!interactionResult.consumesAction()) {
                            interactionResult = this.gameMode.interact(this.player, (Entity)object, interactionHand);
                        }
                        if (!(interactionResult instanceof InteractionResult.Success)) break;
                        Object object3 = (InteractionResult.Success)interactionResult;
                        if (((InteractionResult.Success)object3).swingSource() == InteractionResult.SwingSource.CLIENT) {
                            this.player.swing(interactionHand);
                        }
                        return;
                    }
                    case BLOCK: {
                        Object object3 = (BlockHitResult)this.hitResult;
                        int n = itemStack.getCount();
                        InteractionResult interactionResult = this.gameMode.useItemOn(this.player, interactionHand, (BlockHitResult)object3);
                        if (interactionResult instanceof InteractionResult.Success) {
                            InteractionResult.Success success = (InteractionResult.Success)interactionResult;
                            if (success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                                this.player.swing(interactionHand);
                                if (!itemStack.isEmpty() && (itemStack.getCount() != n || this.player.hasInfiniteMaterials())) {
                                    this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
                                }
                            }
                            return;
                        }
                        if (!(interactionResult instanceof InteractionResult.Fail)) break;
                        return;
                    }
                }
            }
            if (itemStack.isEmpty() || !((object2 = this.gameMode.useItem(this.player, interactionHand)) instanceof InteractionResult.Success)) continue;
            object = (InteractionResult.Success)object2;
            if (((InteractionResult.Success)object).swingSource() == InteractionResult.SwingSource.CLIENT) {
                this.player.swing(interactionHand);
            }
            this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
            return;
        }
    }

    public MusicManager getMusicManager() {
        return this.musicManager;
    }

    public void tick() {
        Object object;
        Object object2;
        ++this.clientTickCount;
        if (this.level != null && !this.pause) {
            this.level.tickRateManager().tick();
        }
        if (this.rightClickDelay > 0) {
            --this.rightClickDelay;
        }
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("gui");
        this.chatListener.tick();
        this.gui.tick(this.pause);
        profilerFiller.pop();
        this.gameRenderer.pick(1.0f);
        this.tutorial.onLookAt(this.level, this.hitResult);
        profilerFiller.push("gameMode");
        if (!this.pause && this.level != null) {
            this.gameMode.tick();
        }
        profilerFiller.popPush("textures");
        if (this.isLevelRunningNormally()) {
            this.textureManager.tick();
        }
        if (this.screen == null && this.player != null) {
            if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                this.setScreen(null);
            } else if (this.player.isSleeping() && this.level != null) {
                this.setScreen(new InBedChatScreen());
            }
        } else {
            object2 = this.screen;
            if (object2 instanceof InBedChatScreen) {
                object = (InBedChatScreen)object2;
                if (!this.player.isSleeping()) {
                    ((InBedChatScreen)object).onPlayerWokeUp();
                }
            }
        }
        if (this.screen != null) {
            this.missTime = 10000;
        }
        if (this.screen != null) {
            try {
                this.screen.tick();
            }
            catch (Throwable throwable) {
                object2 = CrashReport.forThrowable(throwable, "Ticking screen");
                this.screen.fillCrashDetails((CrashReport)object2);
                throw new ReportedException((CrashReport)object2);
            }
        }
        if (!this.getDebugOverlay().showDebugScreen()) {
            this.gui.clearCache();
        }
        if (this.overlay == null && this.screen == null) {
            profilerFiller.popPush("Keybindings");
            this.handleKeybinds();
            if (this.missTime > 0) {
                --this.missTime;
            }
        }
        if (this.level != null) {
            profilerFiller.popPush("gameRenderer");
            if (!this.pause) {
                this.gameRenderer.tick();
            }
            profilerFiller.popPush("levelRenderer");
            if (!this.pause) {
                this.levelRenderer.tick();
            }
            profilerFiller.popPush("level");
            if (!this.pause) {
                this.level.tickEntities();
            }
        } else if (this.gameRenderer.currentPostEffect() != null) {
            this.gameRenderer.clearPostEffect();
        }
        this.musicManager.tick();
        this.soundManager.tick(this.pause);
        if (this.level != null) {
            if (!this.pause) {
                if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
                    object = Component.translatable("tutorial.socialInteractions.title");
                    object2 = Component.translatable("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(this.font, TutorialToast.Icons.SOCIAL_INTERACTIONS, (Component)object, (Component)object2, true, 8000);
                    this.toastManager.addToast(this.socialInteractionsToast);
                    this.options.joinedFirstServer = true;
                    this.options.save();
                }
                this.tutorial.tick();
                try {
                    this.level.tick(() -> true);
                }
                catch (Throwable throwable) {
                    object2 = CrashReport.forThrowable(throwable, "Exception in world tick");
                    if (this.level == null) {
                        CrashReportCategory crashReportCategory = ((CrashReport)object2).addCategory("Affected level");
                        crashReportCategory.setDetail("Problem", "Level is null!");
                    } else {
                        this.level.fillReportDetails((CrashReport)object2);
                    }
                    throw new ReportedException((CrashReport)object2);
                }
            }
            profilerFiller.popPush("animateTick");
            if (!this.pause && this.isLevelRunningNormally()) {
                this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
            }
            profilerFiller.popPush("particles");
            if (!this.pause && this.isLevelRunningNormally()) {
                this.particleEngine.tick();
            }
            if ((object = this.getConnection()) != null && !this.pause) {
                ((ClientCommonPacketListenerImpl)object).send(ServerboundClientTickEndPacket.INSTANCE);
            }
        } else if (this.pendingConnection != null) {
            profilerFiller.popPush("pendingConnection");
            this.pendingConnection.tick();
        }
        profilerFiller.popPush("keyboard");
        this.keyboardHandler.tick();
        profilerFiller.pop();
    }

    private boolean isLevelRunningNormally() {
        return this.level == null || this.level.tickRateManager().runsNormally();
    }

    private boolean isMultiplayerServer() {
        return !this.isLocalServer || this.singleplayerServer != null && this.singleplayerServer.isPublished();
    }

    private void handleKeybinds() {
        int n;
        while (this.options.keyTogglePerspective.consumeClick()) {
            CameraType cameraType = this.options.getCameraType();
            this.options.setCameraType(this.options.getCameraType().cycle());
            if (cameraType.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
            this.levelRenderer.needsUpdate();
        }
        while (this.options.keySmoothCamera.consumeClick()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }
        for (n = 0; n < 9; ++n) {
            boolean bl = this.options.keySaveHotbarActivator.isDown();
            boolean bl2 = this.options.keyLoadHotbarActivator.isDown();
            if (!this.options.keyHotbarSlots[n].consumeClick()) continue;
            if (this.player.isSpectator()) {
                this.gui.getSpectatorGui().onHotbarSelected(n);
                continue;
            }
            if (this.player.hasInfiniteMaterials() && this.screen == null && (bl2 || bl)) {
                CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, n, bl2, bl);
                continue;
            }
            this.player.getInventory().setSelectedSlot(n);
        }
        while (this.options.keySocialInteractions.consumeClick()) {
            if (!this.isMultiplayerServer()) {
                this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                this.narrator.saySystemNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
                continue;
            }
            if (this.socialInteractionsToast != null) {
                this.socialInteractionsToast.hide();
                this.socialInteractionsToast = null;
            }
            this.setScreen(new SocialInteractionsScreen());
        }
        while (this.options.keyInventory.consumeClick()) {
            if (this.gameMode.isServerControlledInventory()) {
                this.player.sendOpenInventory();
                continue;
            }
            this.tutorial.onOpenInventory();
            this.setScreen(new InventoryScreen(this.player));
        }
        while (this.options.keyAdvancements.consumeClick()) {
            this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
        }
        while (this.options.keyQuickActions.consumeClick()) {
            this.getQuickActionsDialog().ifPresent(holder -> this.player.connection.showDialog((Holder<Dialog>)holder, this.screen));
        }
        while (this.options.keySwapOffhand.consumeClick()) {
            if (this.player.isSpectator()) continue;
            this.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        }
        while (this.options.keyDrop.consumeClick()) {
            if (this.player.isSpectator() || !this.player.drop(Screen.hasControlDown())) continue;
            this.player.swing(InteractionHand.MAIN_HAND);
        }
        while (this.options.keyChat.consumeClick()) {
            this.openChatScreen("");
        }
        if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
            this.openChatScreen("/");
        }
        n = 0;
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                this.gameMode.releaseUsingItem(this.player);
            }
            while (this.options.keyAttack.consumeClick()) {
            }
            while (this.options.keyUse.consumeClick()) {
            }
            while (this.options.keyPickItem.consumeClick()) {
            }
        } else {
            while (this.options.keyAttack.consumeClick()) {
                n |= this.startAttack();
            }
            while (this.options.keyUse.consumeClick()) {
                this.startUseItem();
            }
            while (this.options.keyPickItem.consumeClick()) {
                this.pickBlock();
            }
        }
        if (this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
            this.startUseItem();
        }
        this.continueAttack(this.screen == null && n == 0 && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
    }

    private Optional<Holder<Dialog>> getQuickActionsDialog() {
        HolderLookup.RegistryLookup registryLookup = this.player.connection.registryAccess().lookupOrThrow(Registries.DIALOG);
        return registryLookup.get(DialogTags.QUICK_ACTIONS).flatMap(arg_0 -> Minecraft.lambda$getQuickActionsDialog$36((Registry)registryLookup, arg_0));
    }

    public ClientTelemetryManager getTelemetryManager() {
        return this.telemetryManager;
    }

    public double getGpuUtilization() {
        return this.gpuUtilization;
    }

    public ProfileKeyPairManager getProfileKeyPairManager() {
        return this.profileKeyPairManager;
    }

    public WorldOpenFlows createWorldOpenFlows() {
        return new WorldOpenFlows(this, this.levelSource);
    }

    public void doWorldLoad(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, boolean bl) {
        Object object;
        this.disconnectWithProgressScreen();
        this.progressListener.set(null);
        Instant instant = Instant.now();
        try {
            levelStorageAccess.saveDataTag(worldStem.registries().compositeAccess(), worldStem.worldData());
            object = Services.create(this.authenticationService, this.gameDirectory);
            ((Services)object).profileCache().setExecutor(this);
            SkullBlockEntity.setup((Services)object, this);
            GameProfileCache.setUsesAuthentication(false);
            this.singleplayerServer = MinecraftServer.spin(arg_0 -> this.lambda$doWorldLoad$38(levelStorageAccess, packRepository, worldStem, (Services)object, arg_0));
            this.isLocalServer = true;
            this.updateReportEnvironment(ReportEnvironment.local());
            this.quickPlayLog.setWorldData(QuickPlayLog.Type.SINGLEPLAYER, levelStorageAccess.getLevelId(), worldStem.worldData().getLevelName());
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Starting integrated server");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Starting integrated server");
            crashReportCategory.setDetail("Level ID", levelStorageAccess.getLevelId());
            crashReportCategory.setDetail("Level Name", () -> worldStem.worldData().getLevelName());
            throw new ReportedException(crashReport);
        }
        while (this.progressListener.get() == null) {
            Thread.yield();
        }
        object = new LevelLoadingScreen(this.progressListener.get());
        ProfilerFiller profilerFiller = Profiler.get();
        this.setScreen((Screen)object);
        profilerFiller.push("waitForServer");
        while (!this.singleplayerServer.isReady() || this.overlay != null) {
            ((Screen)object).tick();
            this.runTick(false);
            try {
                Thread.sleep(16L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            this.handleDelayedCrash();
        }
        GameTestTicker.SINGLETON.startTicking();
        profilerFiller.pop();
        Duration duration = Duration.between(instant, Instant.now());
        SocketAddress socketAddress = this.singleplayerServer.getConnection().startMemoryChannel();
        Connection connection = Connection.connectToLocalServer(socketAddress);
        connection.initiateServerboundPlayConnection(socketAddress.toString(), 0, new ClientHandshakePacketListenerImpl(connection, this, null, null, bl, duration, component -> {}, null));
        connection.send(new ServerboundHelloPacket(this.getUser().getName(), this.getUser().getProfileId()));
        this.pendingConnection = connection;
    }

    public void setLevel(ClientLevel clientLevel, ReceivingLevelScreen.Reason reason) {
        this.updateScreenAndTick(new ReceivingLevelScreen(() -> false, reason));
        this.level = clientLevel;
        this.updateLevelInEngines(clientLevel);
        if (!this.isLocalServer) {
            Services services = Services.create(this.authenticationService, this.gameDirectory);
            services.profileCache().setExecutor(this);
            SkullBlockEntity.setup(services, this);
            GameProfileCache.setUsesAuthentication(false);
        }
    }

    public void disconnectWithSavingScreen() {
        this.disconnect(new GenericMessageScreen(SAVING_LEVEL), false);
    }

    public void disconnectWithProgressScreen() {
        this.disconnect(new ProgressScreen(true), false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disconnect(Screen screen, boolean bl) {
        ClientPacketListener clientPacketListener = this.getConnection();
        if (clientPacketListener != null) {
            this.dropAllTasks();
            clientPacketListener.close();
            if (!bl) {
                this.clearDownloadedResourcePacks();
            }
        }
        this.playerSocialManager.stopOnlineMode();
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsCancel();
        }
        IntegratedServer integratedServer = this.singleplayerServer;
        this.singleplayerServer = null;
        this.gameRenderer.resetData();
        this.gameMode = null;
        this.narrator.clear();
        this.clientLevelTeardownInProgress = true;
        try {
            this.updateScreenAndTick(screen);
            if (this.level != null) {
                if (integratedServer != null) {
                    ProfilerFiller profilerFiller = Profiler.get();
                    profilerFiller.push("waitForServer");
                    while (!integratedServer.isShutdown()) {
                        this.runTick(false);
                    }
                    profilerFiller.pop();
                }
                this.gui.onDisconnected();
                this.isLocalServer = false;
            }
            this.level = null;
            this.updateLevelInEngines(null);
            this.player = null;
        }
        finally {
            this.clientLevelTeardownInProgress = false;
        }
        SkullBlockEntity.clear();
    }

    public void clearDownloadedResourcePacks() {
        this.downloadedPackSource.cleanupAfterDisconnect();
        this.runAllTasks();
    }

    public void clearClientLevel(Screen screen) {
        ClientPacketListener clientPacketListener = this.getConnection();
        if (clientPacketListener != null) {
            clientPacketListener.clearLevel();
        }
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsCancel();
        }
        this.gameRenderer.resetData();
        this.gameMode = null;
        this.narrator.clear();
        this.clientLevelTeardownInProgress = true;
        try {
            this.updateScreenAndTick(screen);
            this.gui.onDisconnected();
            this.level = null;
            this.updateLevelInEngines(null);
            this.player = null;
        }
        finally {
            this.clientLevelTeardownInProgress = false;
        }
        SkullBlockEntity.clear();
    }

    private void updateScreenAndTick(Screen screen) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("forcedTick");
        this.soundManager.stop();
        this.cameraEntity = null;
        this.pendingConnection = null;
        this.setScreen(screen);
        this.runTick(false);
        profilerFiller.pop();
    }

    public void forceSetScreen(Screen screen) {
        try (Zone zone = Profiler.get().zone("forcedTick");){
            this.setScreen(screen);
            this.runTick(false);
        }
    }

    private void updateLevelInEngines(@Nullable ClientLevel clientLevel) {
        this.levelRenderer.setLevel(clientLevel);
        this.particleEngine.setLevel(clientLevel);
        this.blockEntityRenderDispatcher.setLevel(clientLevel);
        this.gameRenderer.setLevel(clientLevel);
        this.updateTitle();
    }

    private UserApiService.UserProperties userProperties() {
        return this.userPropertiesFuture.join();
    }

    public boolean telemetryOptInExtra() {
        return this.extraTelemetryAvailable() && this.options.telemetryOptInExtra().get() != false;
    }

    public boolean extraTelemetryAvailable() {
        return this.allowsTelemetry() && this.userProperties().flag(UserApiService.UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
    }

    public boolean allowsTelemetry() {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return false;
        }
        return this.userProperties().flag(UserApiService.UserFlag.TELEMETRY_ENABLED);
    }

    public boolean allowsMultiplayer() {
        return this.allowsMultiplayer && this.userProperties().flag(UserApiService.UserFlag.SERVERS_ALLOWED) && this.multiplayerBan() == null && !this.isNameBanned();
    }

    public boolean allowsRealms() {
        return this.userProperties().flag(UserApiService.UserFlag.REALMS_ALLOWED) && this.multiplayerBan() == null;
    }

    @Nullable
    public BanDetails multiplayerBan() {
        return (BanDetails)this.userProperties().bannedScopes().get("MULTIPLAYER");
    }

    public boolean isNameBanned() {
        ProfileResult profileResult = this.profileFuture.getNow(null);
        return profileResult != null && profileResult.actions().contains(ProfileActionType.FORCED_NAME_CHANGE);
    }

    public boolean isBlocked(UUID uUID) {
        if (!this.getChatStatus().isChatAllowed(false)) {
            return (this.player == null || !uUID.equals(this.player.getUUID())) && !uUID.equals(Util.NIL_UUID);
        }
        return this.playerSocialManager.shouldHideMessageFrom(uUID);
    }

    public ChatStatus getChatStatus() {
        if (this.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return ChatStatus.DISABLED_BY_OPTIONS;
        }
        if (!this.allowsChat) {
            return ChatStatus.DISABLED_BY_LAUNCHER;
        }
        if (!this.userProperties().flag(UserApiService.UserFlag.CHAT_ALLOWED)) {
            return ChatStatus.DISABLED_BY_PROFILE;
        }
        return ChatStatus.ENABLED;
    }

    public final boolean isDemo() {
        return this.demo;
    }

    @Nullable
    public ClientPacketListener getConnection() {
        return this.player == null ? null : this.player.connection;
    }

    public static boolean renderNames() {
        return !Minecraft.instance.options.hideGui;
    }

    public static boolean useFancyGraphics() {
        return Minecraft.instance.options.graphicsMode().get().getId() >= GraphicsStatus.FANCY.getId();
    }

    public static boolean useShaderTransparency() {
        return !Minecraft.instance.gameRenderer.isPanoramicMode() && Minecraft.instance.options.graphicsMode().get().getId() >= GraphicsStatus.FABULOUS.getId();
    }

    public static boolean useAmbientOcclusion() {
        return Minecraft.instance.options.ambientOcclusion().get();
    }

    private void pickBlock() {
        if (this.hitResult == null || this.hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        boolean bl = Screen.hasControlDown();
        HitResult hitResult = this.hitResult;
        Objects.requireNonNull(hitResult);
        HitResult hitResult2 = hitResult;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{BlockHitResult.class, EntityHitResult.class}, (Object)hitResult2, n)) {
            case 0: {
                BlockHitResult blockHitResult = (BlockHitResult)hitResult2;
                this.gameMode.handlePickItemFromBlock(blockHitResult.getBlockPos(), bl);
                break;
            }
            case 1: {
                EntityHitResult entityHitResult = (EntityHitResult)hitResult2;
                this.gameMode.handlePickItemFromEntity(entityHitResult.getEntity(), bl);
                break;
            }
        }
    }

    public CrashReport fillReport(CrashReport crashReport) {
        SystemReport systemReport = crashReport.getSystemReport();
        try {
            Minecraft.fillSystemReport(systemReport, this, this.languageManager, this.launchedVersion, this.options);
            this.fillUptime(crashReport.addCategory("Uptime"));
            if (this.level != null) {
                this.level.fillReportDetails(crashReport);
            }
            if (this.singleplayerServer != null) {
                this.singleplayerServer.fillSystemReport(systemReport);
            }
            this.reloadStateTracker.fillCrashReport(crashReport);
        }
        catch (Throwable throwable) {
            LOGGER.error("Failed to collect details", throwable);
        }
        return crashReport;
    }

    public static void fillReport(@Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, @Nullable Options options, CrashReport crashReport) {
        SystemReport systemReport = crashReport.getSystemReport();
        Minecraft.fillSystemReport(systemReport, minecraft, languageManager, string, options);
    }

    private static String formatSeconds(double d) {
        return String.format(Locale.ROOT, "%.3fs", d);
    }

    private void fillUptime(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("JVM uptime", () -> Minecraft.formatSeconds((double)ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0));
        crashReportCategory.setDetail("Wall uptime", () -> Minecraft.formatSeconds((double)(System.currentTimeMillis() - this.clientStartTimeMs) / 1000.0));
        crashReportCategory.setDetail("High-res time", () -> Minecraft.formatSeconds((double)Util.getMillis() / 1000.0));
        crashReportCategory.setDetail("Client ticks", () -> String.format(Locale.ROOT, "%d ticks / %.3fs", this.clientTickCount, (double)this.clientTickCount / 20.0));
    }

    private static SystemReport fillSystemReport(SystemReport systemReport, @Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, @Nullable Options options) {
        systemReport.setDetail("Launched Version", () -> string);
        String string2 = Minecraft.getLauncherBrand();
        if (string2 != null) {
            systemReport.setDetail("Launcher name", string2);
        }
        systemReport.setDetail("Backend library", RenderSystem::getBackendDescription);
        systemReport.setDetail("Backend API", RenderSystem::getApiDescription);
        systemReport.setDetail("Window size", () -> minecraft != null ? minecraft.window.getWidth() + "x" + minecraft.window.getHeight() : "<not initialized>");
        systemReport.setDetail("GFLW Platform", Window::getPlatform);
        systemReport.setDetail("Render Extensions", () -> String.join((CharSequence)", ", RenderSystem.getDevice().getEnabledExtensions()));
        systemReport.setDetail("GL debug messages", () -> {
            GpuDevice gpuDevice = RenderSystem.tryGetDevice();
            if (gpuDevice == null) {
                return "<no renderer available>";
            }
            if (gpuDevice.isDebuggingEnabled()) {
                return String.join((CharSequence)"\n", gpuDevice.getLastDebugMessages());
            }
            return "<debugging unavailable>";
        });
        systemReport.setDetail("Is Modded", () -> Minecraft.checkModStatus().fullDescription());
        systemReport.setDetail("Universe", () -> minecraft != null ? Long.toHexString(minecraft.canary) : "404");
        systemReport.setDetail("Type", "Client (map_client.txt)");
        if (options != null) {
            String string3;
            if (minecraft != null && (string3 = minecraft.getGpuWarnlistManager().getAllWarnings()) != null) {
                systemReport.setDetail("GPU Warnings", string3);
            }
            systemReport.setDetail("Graphics mode", options.graphicsMode().get().toString());
            systemReport.setDetail("Render Distance", options.getEffectiveRenderDistance() + "/" + String.valueOf(options.renderDistance().get()) + " chunks");
        }
        if (minecraft != null) {
            systemReport.setDetail("Resource Packs", () -> PackRepository.displayPackList(minecraft.getResourcePackRepository().getSelectedPacks()));
        }
        if (languageManager != null) {
            systemReport.setDetail("Current Language", () -> languageManager.getSelected());
        }
        systemReport.setDetail("Locale", String.valueOf(Locale.getDefault()));
        systemReport.setDetail("System encoding", () -> System.getProperty("sun.jnu.encoding", "<not set>"));
        systemReport.setDetail("File encoding", () -> System.getProperty("file.encoding", "<not set>"));
        systemReport.setDetail("CPU", GLX::_getCpuInfo);
        return systemReport;
    }

    public static Minecraft getInstance() {
        return instance;
    }

    public CompletableFuture<Void> delayTextureReload() {
        return this.submit(this::reloadResourcePacks).thenCompose(completableFuture -> completableFuture);
    }

    public void updateReportEnvironment(ReportEnvironment reportEnvironment) {
        if (!this.reportingContext.matches(reportEnvironment)) {
            this.reportingContext = ReportingContext.create(reportEnvironment, this.userApiService);
        }
    }

    @Nullable
    public ServerData getCurrentServer() {
        return Optionull.map(this.getConnection(), ClientPacketListener::getServerData);
    }

    public boolean isLocalServer() {
        return this.isLocalServer;
    }

    public boolean hasSingleplayerServer() {
        return this.isLocalServer && this.singleplayerServer != null;
    }

    @Nullable
    public IntegratedServer getSingleplayerServer() {
        return this.singleplayerServer;
    }

    public boolean isSingleplayer() {
        IntegratedServer integratedServer = this.getSingleplayerServer();
        return integratedServer != null && !integratedServer.isPublished();
    }

    public boolean isLocalPlayer(UUID uUID) {
        return uUID.equals(this.getUser().getProfileId());
    }

    public User getUser() {
        return this.user;
    }

    public GameProfile getGameProfile() {
        ProfileResult profileResult = this.profileFuture.join();
        if (profileResult != null) {
            return profileResult.profile();
        }
        return new GameProfile(this.user.getProfileId(), this.user.getName());
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public ShaderManager getShaderManager() {
        return this.shaderManager;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public PackRepository getResourcePackRepository() {
        return this.resourcePackRepository;
    }

    public VanillaPackResources getVanillaPackResources() {
        return this.vanillaPackResources;
    }

    public DownloadedPackSource getDownloadedPackSource() {
        return this.downloadedPackSource;
    }

    public Path getResourcePackDirectory() {
        return this.resourcePackDirectory;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public Function<ResourceLocation, TextureAtlasSprite> getTextureAtlas(ResourceLocation resourceLocation) {
        return this.modelManager.getAtlas(resourceLocation)::getSprite;
    }

    public boolean isPaused() {
        return this.pause;
    }

    public GpuWarnlistManager getGpuWarnlistManager() {
        return this.gpuWarnlistManager;
    }

    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    public MusicInfo getSituationalMusic() {
        Music music = Optionull.map(this.screen, Screen::getBackgroundMusic);
        if (music != null) {
            return new MusicInfo(music);
        }
        if (this.player != null) {
            Level level = this.player.level();
            if (level.dimension() == Level.END) {
                if (this.gui.getBossOverlay().shouldPlayMusic()) {
                    return new MusicInfo(Musics.END_BOSS);
                }
                return new MusicInfo(Musics.END);
            }
            Holder<Biome> holder = level.getBiome(this.player.blockPosition());
            Biome biome = holder.value();
            float f = biome.getBackgroundMusicVolume();
            Optional<WeightedList<Music>> optional = biome.getBackgroundMusic();
            if (optional.isPresent()) {
                Optional<Music> optional2 = optional.get().getRandom(level.random);
                return new MusicInfo(optional2.orElse(null), f);
            }
            if (this.musicManager.isPlayingMusic(Musics.UNDER_WATER) || this.player.isUnderWater() && holder.is(BiomeTags.PLAYS_UNDERWATER_MUSIC)) {
                return new MusicInfo(Musics.UNDER_WATER, f);
            }
            if (level.dimension() != Level.NETHER && this.player.getAbilities().instabuild && this.player.getAbilities().mayfly) {
                return new MusicInfo(Musics.CREATIVE, f);
            }
            return new MusicInfo(Musics.GAME, f);
        }
        return new MusicInfo(Musics.MENU);
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return this.minecraftSessionService;
    }

    public SkinManager getSkinManager() {
        return this.skinManager;
    }

    @Nullable
    public Entity getCameraEntity() {
        return this.cameraEntity;
    }

    public void setCameraEntity(Entity entity) {
        this.cameraEntity = entity;
        this.gameRenderer.checkEntityPostEffect(entity);
    }

    public boolean shouldEntityAppearGlowing(Entity entity) {
        return entity.isCurrentlyGlowing() || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && entity.getType() == EntityType.PLAYER;
    }

    @Override
    protected Thread getRunningThread() {
        return this.gameThread;
    }

    @Override
    public Runnable wrapRunnable(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean shouldRun(Runnable runnable) {
        return true;
    }

    public BlockRenderDispatcher getBlockRenderer() {
        return this.blockRenderer;
    }

    public EntityRenderDispatcher getEntityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public DeltaTracker getDeltaTracker() {
        return this.deltaTracker;
    }

    public BlockColors getBlockColors() {
        return this.blockColors;
    }

    public boolean showOnlyReducedInfo() {
        return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo().get() != false;
    }

    public ToastManager getToastManager() {
        return this.toastManager;
    }

    public Tutorial getTutorial() {
        return this.tutorial;
    }

    public boolean isWindowActive() {
        return this.windowActive;
    }

    public HotbarManager getHotbarManager() {
        return this.hotbarManager;
    }

    public ModelManager getModelManager() {
        return this.modelManager;
    }

    public PaintingTextureManager getPaintingTextures() {
        return this.paintingTextures;
    }

    public MapTextureManager getMapTextureManager() {
        return this.mapTextureManager;
    }

    public MapDecorationTextureManager getMapDecorationTextures() {
        return this.mapDecorationTextures;
    }

    public GuiSpriteManager getGuiSprites() {
        return this.guiSprites;
    }

    public WaypointStyleManager getWaypointStyles() {
        return this.waypointStyles;
    }

    @Override
    public void setWindowActive(boolean bl) {
        this.windowActive = bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Component grabPanoramixScreenshot(File file) {
        int n = 4;
        int n2 = 4096;
        int n3 = 4096;
        int n4 = this.window.getWidth();
        int n5 = this.window.getHeight();
        RenderTarget renderTarget = this.getMainRenderTarget();
        float f = this.player.getXRot();
        float f2 = this.player.getYRot();
        float f3 = this.player.xRotO;
        float f4 = this.player.yRotO;
        this.gameRenderer.setRenderBlockOutline(false);
        try {
            this.gameRenderer.setPanoramicMode(true);
            this.window.setWidth(4096);
            this.window.setHeight(4096);
            renderTarget.resize(4096, 4096);
            for (int i = 0; i < 6; ++i) {
                switch (i) {
                    case 0: {
                        this.player.setYRot(f2);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 1: {
                        this.player.setYRot((f2 + 90.0f) % 360.0f);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 2: {
                        this.player.setYRot((f2 + 180.0f) % 360.0f);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 3: {
                        this.player.setYRot((f2 - 90.0f) % 360.0f);
                        this.player.setXRot(0.0f);
                        break;
                    }
                    case 4: {
                        this.player.setYRot(f2);
                        this.player.setXRot(-90.0f);
                        break;
                    }
                    default: {
                        this.player.setYRot(f2);
                        this.player.setXRot(90.0f);
                    }
                }
                this.player.yRotO = this.player.getYRot();
                this.player.xRotO = this.player.getXRot();
                this.gameRenderer.renderLevel(DeltaTracker.ONE);
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                Screenshot.grab(file, "panorama_" + i + ".png", renderTarget, 4, component -> {});
            }
            MutableComponent mutableComponent = Component.literal(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(file.getAbsoluteFile())));
            MutableComponent mutableComponent2 = Component.translatable("screenshot.success", mutableComponent);
            return mutableComponent2;
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't save image", (Throwable)exception);
            MutableComponent mutableComponent = Component.translatable("screenshot.failure", exception.getMessage());
            return mutableComponent;
        }
        finally {
            this.player.setXRot(f);
            this.player.setYRot(f2);
            this.player.xRotO = f3;
            this.player.yRotO = f4;
            this.gameRenderer.setRenderBlockOutline(true);
            this.window.setWidth(n4);
            this.window.setHeight(n5);
            renderTarget.resize(n4, n5);
            this.gameRenderer.setPanoramicMode(false);
        }
    }

    @Nullable
    public StoringChunkProgressListener getProgressListener() {
        return this.progressListener.get();
    }

    public SplashManager getSplashManager() {
        return this.splashManager;
    }

    @Nullable
    public Overlay getOverlay() {
        return this.overlay;
    }

    public PlayerSocialManager getPlayerSocialManager() {
        return this.playerSocialManager;
    }

    public Window getWindow() {
        return this.window;
    }

    public FramerateLimitTracker getFramerateLimitTracker() {
        return this.framerateLimitTracker;
    }

    public DebugScreenOverlay getDebugOverlay() {
        return this.gui.getDebugOverlay();
    }

    public RenderBuffers renderBuffers() {
        return this.renderBuffers;
    }

    public void updateMaxMipLevel(int n) {
        this.modelManager.updateMaxMipLevel(n);
    }

    public EntityModelSet getEntityModels() {
        return this.modelManager.entityModels().get();
    }

    public boolean isTextFilteringEnabled() {
        return this.userProperties().flag(UserApiService.UserFlag.PROFANITY_FILTER_ENABLED);
    }

    public void prepareForMultiplayer() {
        this.playerSocialManager.startOnlineMode();
        this.getProfileKeyPairManager().prepareKeyPair();
    }

    @Nullable
    public SignatureValidator getProfileKeySignatureValidator() {
        return SignatureValidator.from(this.authenticationService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
    }

    public boolean canValidateProfileKeys() {
        return !this.authenticationService.getServicesKeySet().keys(ServicesKeyType.PROFILE_KEY).isEmpty();
    }

    public InputType getLastInputType() {
        return this.lastInputType;
    }

    public void setLastInputType(InputType inputType) {
        this.lastInputType = inputType;
    }

    public GameNarrator getNarrator() {
        return this.narrator;
    }

    public ChatListener getChatListener() {
        return this.chatListener;
    }

    public ReportingContext getReportingContext() {
        return this.reportingContext;
    }

    public RealmsDataFetcher realmsDataFetcher() {
        return this.realmsDataFetcher;
    }

    public QuickPlayLog quickPlayLog() {
        return this.quickPlayLog;
    }

    public CommandHistory commandHistory() {
        return this.commandHistory;
    }

    public DirectoryValidator directoryValidator() {
        return this.directoryValidator;
    }

    private float getTickTargetMillis(float f) {
        TickRateManager tickRateManager;
        if (this.level != null && (tickRateManager = this.level.tickRateManager()).runsNormally()) {
            return Math.max(f, tickRateManager.millisecondsPerTick());
        }
        return f;
    }

    public ItemModelResolver getItemModelResolver() {
        return this.itemModelResolver;
    }

    @Nullable
    public static String getLauncherBrand() {
        return System.getProperty("minecraft.launcher.brand");
    }

    private /* synthetic */ IntegratedServer lambda$doWorldLoad$38(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Services services, Thread thread) {
        return new IntegratedServer(thread, this, levelStorageAccess, packRepository, worldStem, services, n -> {
            StoringChunkProgressListener storingChunkProgressListener = StoringChunkProgressListener.createFromGameruleRadius(n + 0);
            this.progressListener.set(storingChunkProgressListener);
            return ProcessorChunkProgressListener.createStarted(storingChunkProgressListener, this.progressTasks::add);
        });
    }

    private static /* synthetic */ Optional lambda$getQuickActionsDialog$36(Registry registry, HolderSet.Named named) {
        if (named.size() == 0) {
            return Optional.empty();
        }
        if (named.size() == 1) {
            return Optional.of(named.get(0));
        }
        return registry.get(Dialogs.QUICK_ACTIONS);
    }

    private static /* synthetic */ void lambda$runTick$22(CompletableFuture completableFuture) {
        completableFuture.complete(null);
    }

    private /* synthetic */ void lambda$new$6(GameLoadCookie gameLoadCookie, Optional optional) {
        Util.ifElse(optional, throwable -> this.rollbackResourcePacks((Throwable)throwable, gameLoadCookie), () -> {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                this.selfTest();
            }
            this.reloadStateTracker.finishReload();
            this.onResourceLoadFinished(gameLoadCookie);
        });
    }

    static {
        LOGGER = LogUtils.getLogger();
        ON_OSX = Util.getPlatform() == Util.OS.OSX;
        DEFAULT_FONT = ResourceLocation.withDefaultNamespace("default");
        UNIFORM_FONT = ResourceLocation.withDefaultNamespace("uniform");
        ALT_FONT = ResourceLocation.withDefaultNamespace("alt");
        REGIONAL_COMPLIANCIES = ResourceLocation.withDefaultNamespace("regional_compliancies.json");
        RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
        SOCIAL_INTERACTIONS_NOT_AVAILABLE = Component.translatable("multiplayer.socialInteractions.not_available");
        SAVING_LEVEL = Component.translatable("menu.savingLevel");
    }

    static final class GameLoadCookie
    extends Record {
        private final RealmsClient realmsClient;
        final GameConfig.QuickPlayData quickPlayData;

        GameLoadCookie(RealmsClient realmsClient, GameConfig.QuickPlayData quickPlayData) {
            this.realmsClient = realmsClient;
            this.quickPlayData = quickPlayData;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GameLoadCookie.class, "realmsClient;quickPlayData", "realmsClient", "quickPlayData"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GameLoadCookie.class, "realmsClient;quickPlayData", "realmsClient", "quickPlayData"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GameLoadCookie.class, "realmsClient;quickPlayData", "realmsClient", "quickPlayData"}, this, object);
        }

        public RealmsClient realmsClient() {
            return this.realmsClient;
        }

        public GameConfig.QuickPlayData quickPlayData() {
            return this.quickPlayData;
        }
    }

    public static enum ChatStatus {
        ENABLED(CommonComponents.EMPTY){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return true;
            }
        }
        ,
        DISABLED_BY_OPTIONS(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED)){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return false;
            }
        }
        ,
        DISABLED_BY_LAUNCHER(Component.translatable("chat.disabled.launcher").withStyle(ChatFormatting.RED)){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return bl;
            }
        }
        ,
        DISABLED_BY_PROFILE(Component.translatable("chat.disabled.profile", Component.keybind(Minecraft.instance.options.keyChat.getName())).withStyle(ChatFormatting.RED)){

            @Override
            public boolean isChatAllowed(boolean bl) {
                return bl;
            }
        };

        static final Component INFO_DISABLED_BY_PROFILE;
        private final Component message;

        ChatStatus(Component component) {
            this.message = component;
        }

        public Component getMessage() {
            return this.message;
        }

        public abstract boolean isChatAllowed(boolean var1);

        static {
            INFO_DISABLED_BY_PROFILE = Component.translatable("chat.disabled.profile.moreInfo");
        }
    }
}

