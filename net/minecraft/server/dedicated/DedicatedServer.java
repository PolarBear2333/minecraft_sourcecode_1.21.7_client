/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.dedicated.ServerWatchdog;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerTextFilter;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.DebugSampleSubscriptionTracker;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.RemoteSampleLogger;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class DedicatedServer
extends MinecraftServer
implements ServerInterface {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int CONVERSION_RETRY_DELAY_MS = 5000;
    private static final int CONVERSION_RETRIES = 2;
    private final List<ConsoleInput> consoleInput = Collections.synchronizedList(Lists.newArrayList());
    @Nullable
    private QueryThreadGs4 queryThreadGs4;
    private final RconConsoleSource rconConsoleSource;
    @Nullable
    private RconThread rconThread;
    private final DedicatedServerSettings settings;
    @Nullable
    private MinecraftServerGui gui;
    @Nullable
    private final ServerTextFilter serverTextFilter;
    @Nullable
    private RemoteSampleLogger tickTimeLogger;
    @Nullable
    private DebugSampleSubscriptionTracker debugSampleSubscriptionTracker;
    private final ServerLinks serverLinks;

    public DedicatedServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, DedicatedServerSettings dedicatedServerSettings, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, Proxy.NO_PROXY, dataFixer, services, chunkProgressListenerFactory);
        this.settings = dedicatedServerSettings;
        this.rconConsoleSource = new RconConsoleSource(this);
        this.serverTextFilter = ServerTextFilter.createFromConfig(dedicatedServerSettings.getProperties());
        this.serverLinks = DedicatedServer.createServerLinks(dedicatedServerSettings);
    }

    @Override
    public boolean initServer() throws IOException {
        Thread thread = new Thread("Server console handler"){

            @Override
            public void run() {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                try {
                    String string;
                    while (!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning() && (string = bufferedReader.readLine()) != null) {
                        DedicatedServer.this.handleConsoleInput(string, DedicatedServer.this.createCommandSourceStack());
                    }
                }
                catch (IOException iOException) {
                    LOGGER.error("Exception handling console input", (Throwable)iOException);
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
        LOGGER.info("Starting minecraft server version {}", (Object)SharedConstants.getCurrentVersion().name());
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }
        LOGGER.info("Loading properties");
        DedicatedServerProperties dedicatedServerProperties = this.settings.getProperties();
        if (this.isSingleplayer()) {
            this.setLocalIp("127.0.0.1");
        } else {
            this.setUsesAuthentication(dedicatedServerProperties.onlineMode);
            this.setPreventProxyConnections(dedicatedServerProperties.preventProxyConnections);
            this.setLocalIp(dedicatedServerProperties.serverIp);
        }
        this.setPvpAllowed(dedicatedServerProperties.pvp);
        this.setFlightAllowed(dedicatedServerProperties.allowFlight);
        this.setMotd(dedicatedServerProperties.motd);
        super.setPlayerIdleTimeout(dedicatedServerProperties.playerIdleTimeout.get());
        this.setEnforceWhitelist(dedicatedServerProperties.enforceWhitelist);
        this.worldData.setGameType(dedicatedServerProperties.gamemode);
        LOGGER.info("Default game type: {}", (Object)dedicatedServerProperties.gamemode);
        InetAddress inetAddress = null;
        if (!this.getLocalIp().isEmpty()) {
            inetAddress = InetAddress.getByName(this.getLocalIp());
        }
        if (this.getPort() < 0) {
            this.setPort(dedicatedServerProperties.serverPort);
        }
        this.initializeKeyPair();
        LOGGER.info("Starting Minecraft server on {}:{}", (Object)(this.getLocalIp().isEmpty() ? "*" : this.getLocalIp()), (Object)this.getPort());
        try {
            this.getConnection().startTcpServerListener(inetAddress, this.getPort());
        }
        catch (IOException iOException) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", (Object)iOException.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
        }
        if (!this.usesAuthentication()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }
        if (this.convertOldUsers()) {
            this.getProfileCache().save();
        }
        if (!OldUsersConverter.serverReadyAfterUserconversion(this)) {
            return false;
        }
        this.setPlayerList(new DedicatedPlayerList(this, this.registries(), this.playerDataStorage));
        this.debugSampleSubscriptionTracker = new DebugSampleSubscriptionTracker(this.getPlayerList());
        this.tickTimeLogger = new RemoteSampleLogger(TpsDebugDimensions.values().length, this.debugSampleSubscriptionTracker, RemoteDebugSampleType.TICK_TIME);
        long l = Util.getNanos();
        SkullBlockEntity.setup(this.services, this);
        GameProfileCache.setUsesAuthentication(this.usesAuthentication());
        LOGGER.info("Preparing level \"{}\"", (Object)this.getLevelIdName());
        this.loadLevel();
        long l2 = Util.getNanos() - l;
        String string = String.format(Locale.ROOT, "%.3fs", (double)l2 / 1.0E9);
        LOGGER.info("Done ({})! For help, type \"help\"", (Object)string);
        if (dedicatedServerProperties.announcePlayerAchievements != null) {
            this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(dedicatedServerProperties.announcePlayerAchievements, this);
        }
        if (dedicatedServerProperties.enableQuery) {
            LOGGER.info("Starting GS4 status listener");
            this.queryThreadGs4 = QueryThreadGs4.create(this);
        }
        if (dedicatedServerProperties.enableRcon) {
            LOGGER.info("Starting remote control listener");
            this.rconThread = RconThread.create(this);
        }
        if (this.getMaxTickLength() > 0L) {
            Thread thread2 = new Thread(new ServerWatchdog(this));
            thread2.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
            thread2.setName("Server Watchdog");
            thread2.setDaemon(true);
            thread2.start();
        }
        if (dedicatedServerProperties.enableJmxMonitoring) {
            MinecraftServerStatistics.registerJmxMonitoring(this);
            LOGGER.info("JMX monitoring enabled");
        }
        return true;
    }

    @Override
    public boolean isSpawningMonsters() {
        return this.settings.getProperties().spawnMonsters && super.isSpawningMonsters();
    }

    @Override
    public DedicatedServerProperties getProperties() {
        return this.settings.getProperties();
    }

    @Override
    public void forceDifficulty() {
        this.setDifficulty(this.getProperties().difficulty, true);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport systemReport) {
        systemReport.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        systemReport.setDetail("Type", () -> "Dedicated Server (map_server.txt)");
        return systemReport;
    }

    @Override
    public void dumpServerProperties(Path path) throws IOException {
        DedicatedServerProperties dedicatedServerProperties = this.getProperties();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
            bufferedWriter.write(String.format(Locale.ROOT, "sync-chunk-writes=%s%n", dedicatedServerProperties.syncChunkWrites));
            bufferedWriter.write(String.format(Locale.ROOT, "gamemode=%s%n", dedicatedServerProperties.gamemode));
            bufferedWriter.write(String.format(Locale.ROOT, "spawn-monsters=%s%n", dedicatedServerProperties.spawnMonsters));
            bufferedWriter.write(String.format(Locale.ROOT, "entity-broadcast-range-percentage=%d%n", dedicatedServerProperties.entityBroadcastRangePercentage));
            bufferedWriter.write(String.format(Locale.ROOT, "max-world-size=%d%n", dedicatedServerProperties.maxWorldSize));
            bufferedWriter.write(String.format(Locale.ROOT, "view-distance=%d%n", dedicatedServerProperties.viewDistance));
            bufferedWriter.write(String.format(Locale.ROOT, "simulation-distance=%d%n", dedicatedServerProperties.simulationDistance));
            bufferedWriter.write(String.format(Locale.ROOT, "generate-structures=%s%n", dedicatedServerProperties.worldOptions.generateStructures()));
            bufferedWriter.write(String.format(Locale.ROOT, "use-native=%s%n", dedicatedServerProperties.useNativeTransport));
            bufferedWriter.write(String.format(Locale.ROOT, "rate-limit=%d%n", dedicatedServerProperties.rateLimitPacketsPerSecond));
        }
    }

    @Override
    public void onServerExit() {
        if (this.serverTextFilter != null) {
            this.serverTextFilter.close();
        }
        if (this.gui != null) {
            this.gui.close();
        }
        if (this.rconThread != null) {
            this.rconThread.stop();
        }
        if (this.queryThreadGs4 != null) {
            this.queryThreadGs4.stop();
        }
    }

    @Override
    public void tickConnection() {
        super.tickConnection();
        this.handleConsoleInputs();
    }

    @Override
    public boolean isLevelEnabled(Level level) {
        if (level.dimension() == Level.NETHER) {
            return this.getProperties().allowNether;
        }
        return true;
    }

    public void handleConsoleInput(String string, CommandSourceStack commandSourceStack) {
        this.consoleInput.add(new ConsoleInput(string, commandSourceStack));
    }

    public void handleConsoleInputs() {
        while (!this.consoleInput.isEmpty()) {
            ConsoleInput consoleInput = this.consoleInput.remove(0);
            this.getCommands().performPrefixedCommand(consoleInput.source, consoleInput.msg);
        }
    }

    @Override
    public boolean isDedicatedServer() {
        return true;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return this.getProperties().rateLimitPacketsPerSecond;
    }

    @Override
    public boolean isEpollEnabled() {
        return this.getProperties().useNativeTransport;
    }

    @Override
    public DedicatedPlayerList getPlayerList() {
        return (DedicatedPlayerList)super.getPlayerList();
    }

    @Override
    public boolean isPublished() {
        return true;
    }

    @Override
    public String getServerIp() {
        return this.getLocalIp();
    }

    @Override
    public int getServerPort() {
        return this.getPort();
    }

    @Override
    public String getServerName() {
        return this.getMotd();
    }

    public void showGui() {
        if (this.gui == null) {
            this.gui = MinecraftServerGui.showFrameFor(this);
        }
    }

    @Override
    public boolean hasGui() {
        return this.gui != null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return this.getProperties().enableCommandBlock;
    }

    @Override
    public int getSpawnProtectionRadius() {
        return this.getProperties().spawnProtection;
    }

    @Override
    public boolean isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player) {
        int n;
        if (serverLevel.dimension() != Level.OVERWORLD) {
            return false;
        }
        if (this.getPlayerList().getOps().isEmpty()) {
            return false;
        }
        if (this.getPlayerList().isOp(player.getGameProfile())) {
            return false;
        }
        if (this.getSpawnProtectionRadius() <= 0) {
            return false;
        }
        BlockPos blockPos2 = serverLevel.getSharedSpawnPos();
        int n2 = Mth.abs(blockPos.getX() - blockPos2.getX());
        int n3 = Math.max(n2, n = Mth.abs(blockPos.getZ() - blockPos2.getZ()));
        return n3 <= this.getSpawnProtectionRadius();
    }

    @Override
    public boolean repliesToStatus() {
        return this.getProperties().enableStatus;
    }

    @Override
    public boolean hidesOnlinePlayers() {
        return this.getProperties().hideOnlinePlayers;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return this.getProperties().opPermissionLevel;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return this.getProperties().functionPermissionLevel;
    }

    @Override
    public void setPlayerIdleTimeout(int n) {
        super.setPlayerIdleTimeout(n);
        this.settings.update(dedicatedServerProperties -> (DedicatedServerProperties)dedicatedServerProperties.playerIdleTimeout.update(this.registryAccess(), n));
    }

    @Override
    public boolean shouldRconBroadcast() {
        return this.getProperties().broadcastRconToOps;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getProperties().broadcastConsoleToOps;
    }

    @Override
    public int getAbsoluteMaxWorldSize() {
        return this.getProperties().maxWorldSize;
    }

    @Override
    public int getCompressionThreshold() {
        return this.getProperties().networkCompressionThreshold;
    }

    @Override
    public boolean enforceSecureProfile() {
        DedicatedServerProperties dedicatedServerProperties = this.getProperties();
        return dedicatedServerProperties.enforceSecureProfile && dedicatedServerProperties.onlineMode && this.services.canValidateProfileKeys();
    }

    @Override
    public boolean logIPs() {
        return this.getProperties().logIPs;
    }

    protected boolean convertOldUsers() {
        int n;
        boolean bl = false;
        for (n = 0; !bl && n <= 2; ++n) {
            if (n > 0) {
                LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
                this.waitForRetry();
            }
            bl = OldUsersConverter.convertUserBanlist(this);
        }
        boolean bl2 = false;
        for (n = 0; !bl2 && n <= 2; ++n) {
            if (n > 0) {
                LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
                this.waitForRetry();
            }
            bl2 = OldUsersConverter.convertIpBanlist(this);
        }
        boolean bl3 = false;
        for (n = 0; !bl3 && n <= 2; ++n) {
            if (n > 0) {
                LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
                this.waitForRetry();
            }
            bl3 = OldUsersConverter.convertOpsList(this);
        }
        boolean bl4 = false;
        for (n = 0; !bl4 && n <= 2; ++n) {
            if (n > 0) {
                LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
                this.waitForRetry();
            }
            bl4 = OldUsersConverter.convertWhiteList(this);
        }
        boolean bl5 = false;
        for (n = 0; !bl5 && n <= 2; ++n) {
            if (n > 0) {
                LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
                this.waitForRetry();
            }
            bl5 = OldUsersConverter.convertPlayers(this);
        }
        return bl || bl2 || bl3 || bl4 || bl5;
    }

    private void waitForRetry() {
        try {
            Thread.sleep(5000L);
        }
        catch (InterruptedException interruptedException) {
            return;
        }
    }

    public long getMaxTickLength() {
        return this.getProperties().maxTickTime;
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return this.getProperties().maxChainedNeighborUpdates;
    }

    @Override
    public String getPluginNames() {
        return "";
    }

    @Override
    public String runCommand(String string) {
        this.rconConsoleSource.prepareForCommand();
        this.executeBlocking(() -> this.getCommands().performPrefixedCommand(this.rconConsoleSource.createCommandSourceStack(), string));
        return this.rconConsoleSource.getCommandResponse();
    }

    public void storeUsingWhiteList(boolean bl) {
        this.settings.update(dedicatedServerProperties -> (DedicatedServerProperties)dedicatedServerProperties.whiteList.update(this.registryAccess(), bl));
    }

    @Override
    public void stopServer() {
        super.stopServer();
        Util.shutdownExecutors();
        SkullBlockEntity.clear();
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile gameProfile) {
        return false;
    }

    @Override
    public int getScaledTrackingDistance(int n) {
        return this.getProperties().entityBroadcastRangePercentage * n / 100;
    }

    @Override
    public String getLevelIdName() {
        return this.storageSource.getLevelId();
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.settings.getProperties().syncChunkWrites;
    }

    @Override
    public TextFilter createTextFilterForPlayer(ServerPlayer serverPlayer) {
        if (this.serverTextFilter != null) {
            return this.serverTextFilter.createContext(serverPlayer.getGameProfile());
        }
        return TextFilter.DUMMY;
    }

    @Override
    @Nullable
    public GameType getForcedGameType() {
        return this.settings.getProperties().forceGameMode ? this.worldData.getGameType() : null;
    }

    @Override
    public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
        return this.settings.getProperties().serverResourcePackInfo;
    }

    @Override
    public void endMetricsRecordingTick() {
        super.endMetricsRecordingTick();
        this.debugSampleSubscriptionTracker.tick(this.getTickCount());
    }

    @Override
    public SampleLogger getTickTimeLogger() {
        return this.tickTimeLogger;
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return this.debugSampleSubscriptionTracker.shouldLogSamples(RemoteDebugSampleType.TICK_TIME);
    }

    @Override
    public void subscribeToDebugSample(ServerPlayer serverPlayer, RemoteDebugSampleType remoteDebugSampleType) {
        this.debugSampleSubscriptionTracker.subscribe(serverPlayer, remoteDebugSampleType);
    }

    @Override
    public boolean acceptsTransfers() {
        return this.settings.getProperties().acceptsTransfers;
    }

    @Override
    public ServerLinks serverLinks() {
        return this.serverLinks;
    }

    @Override
    public int pauseWhileEmptySeconds() {
        return this.settings.getProperties().pauseWhenEmptySeconds;
    }

    private static ServerLinks createServerLinks(DedicatedServerSettings dedicatedServerSettings) {
        Optional<URI> optional = DedicatedServer.parseBugReportLink(dedicatedServerSettings.getProperties());
        return optional.map(uRI -> new ServerLinks(List.of(ServerLinks.KnownLinkType.BUG_REPORT.create((URI)uRI)))).orElse(ServerLinks.EMPTY);
    }

    private static Optional<URI> parseBugReportLink(DedicatedServerProperties dedicatedServerProperties) {
        String string = dedicatedServerProperties.bugReportLink;
        if (string.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Util.parseAndValidateUntrustedUri(string));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse bug link {}", (Object)string, (Object)exception);
            return Optional.empty();
        }
    }

    @Override
    public /* synthetic */ PlayerList getPlayerList() {
        return this.getPlayerList();
    }
}

