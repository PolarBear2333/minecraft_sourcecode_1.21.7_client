/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.client.server.LanServerPinger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class IntegratedServer
extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SIM_DISTANCE = 2;
    private final Minecraft minecraft;
    private boolean paused = true;
    private int publishedPort = -1;
    @Nullable
    private GameType publishedGameType;
    @Nullable
    private LanServerPinger lanPinger;
    @Nullable
    private UUID uuid;
    private int previousSimulationDistance = 0;

    public IntegratedServer(Thread thread, Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, minecraft.getProxy(), minecraft.getFixerUpper(), services, chunkProgressListenerFactory);
        this.setSingleplayerProfile(minecraft.getGameProfile());
        this.setDemo(minecraft.isDemo());
        this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
        this.minecraft = minecraft;
    }

    @Override
    public boolean initServer() {
        LOGGER.info("Starting integrated minecraft server version {}", (Object)SharedConstants.getCurrentVersion().name());
        this.setUsesAuthentication(true);
        this.setPvpAllowed(true);
        this.setFlightAllowed(true);
        this.initializeKeyPair();
        this.loadLevel();
        GameProfile gameProfile = this.getSingleplayerProfile();
        String string = this.getWorldData().getLevelName();
        this.setMotd((String)(gameProfile != null ? gameProfile.getName() + " - " + string : string));
        return true;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void tickServer(BooleanSupplier booleanSupplier) {
        int n;
        boolean bl;
        boolean bl2 = this.paused;
        this.paused = Minecraft.getInstance().isPaused();
        ProfilerFiller profilerFiller = Profiler.get();
        if (!bl2 && this.paused) {
            profilerFiller.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            profilerFiller.pop();
        }
        boolean bl3 = bl = Minecraft.getInstance().getConnection() != null;
        if (bl && this.paused) {
            this.tickPaused();
            return;
        }
        if (bl2 && !this.paused) {
            this.forceTimeSynchronization();
        }
        super.tickServer(booleanSupplier);
        int n2 = Math.max(2, this.minecraft.options.renderDistance().get());
        if (n2 != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", (Object)n2, (Object)this.getPlayerList().getViewDistance());
            this.getPlayerList().setViewDistance(n2);
        }
        if ((n = Math.max(2, this.minecraft.options.simulationDistance().get())) != this.previousSimulationDistance) {
            LOGGER.info("Changing simulation distance to {}, from {}", (Object)n, (Object)this.previousSimulationDistance);
            this.getPlayerList().setSimulationDistance(n);
            this.previousSimulationDistance = n;
        }
    }

    @Override
    protected LocalSampleLogger getTickTimeLogger() {
        return this.minecraft.getDebugOverlay().getTickTimeLogger();
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return true;
    }

    private void tickPaused() {
        for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
            serverPlayer.awardStat(Stats.TOTAL_WORLD_TIME);
        }
    }

    @Override
    public boolean shouldRconBroadcast() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @Override
    public Path getServerDirectory() {
        return this.minecraft.gameDirectory.toPath();
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public void onServerCrash(CrashReport crashReport) {
        this.minecraft.delayCrashRaw(crashReport);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport systemReport) {
        systemReport.setDetail("Type", "Integrated Server (map_client.txt)");
        systemReport.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        systemReport.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
        return systemReport;
    }

    @Override
    public ModCheck getModdedStatus() {
        return Minecraft.checkModStatus().merge(super.getModdedStatus());
    }

    @Override
    public boolean publishServer(@Nullable GameType gameType, boolean bl, int n) {
        try {
            this.minecraft.prepareForMultiplayer();
            this.minecraft.getConnection().prepareKeyPair();
            this.getConnection().startTcpServerListener(null, n);
            LOGGER.info("Started serving on {}", (Object)n);
            this.publishedPort = n;
            this.lanPinger = new LanServerPinger(this.getMotd(), "" + n);
            this.lanPinger.start();
            this.publishedGameType = gameType;
            this.getPlayerList().setAllowCommandsForAllPlayers(bl);
            int n2 = this.getProfilePermissions(this.minecraft.player.getGameProfile());
            this.minecraft.player.setPermissionLevel(n2);
            for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
                this.getCommands().sendCommands(serverPlayer);
            }
            return true;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    @Override
    public void stopServer() {
        super.stopServer();
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public void halt(boolean bl) {
        this.executeBlocking(() -> {
            ArrayList arrayList = Lists.newArrayList(this.getPlayerList().getPlayers());
            for (ServerPlayer serverPlayer : arrayList) {
                if (serverPlayer.getUUID().equals(this.uuid)) continue;
                this.getPlayerList().remove(serverPlayer);
            }
        });
        super.halt(bl);
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public boolean isPublished() {
        return this.publishedPort > -1;
    }

    @Override
    public int getPort() {
        return this.publishedPort;
    }

    @Override
    public void setDefaultGameType(GameType gameType) {
        super.setDefaultGameType(gameType);
        this.publishedGameType = null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 2;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 2;
    }

    public void setUUID(UUID uUID) {
        this.uuid = uUID;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile gameProfile) {
        return this.getSingleplayerProfile() != null && gameProfile.getName().equalsIgnoreCase(this.getSingleplayerProfile().getName());
    }

    @Override
    public int getScaledTrackingDistance(int n) {
        return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)n);
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.minecraft.options.syncWrites;
    }

    @Override
    @Nullable
    public GameType getForcedGameType() {
        if (this.isPublished() && !this.isHardcore()) {
            return (GameType)MoreObjects.firstNonNull((Object)this.publishedGameType, (Object)this.worldData.getGameType());
        }
        return null;
    }

    @Override
    public boolean saveEverything(boolean bl, boolean bl2, boolean bl3) {
        boolean bl4 = super.saveEverything(bl, bl2, bl3);
        this.warnOnLowDiskSpace();
        return bl4;
    }

    private void warnOnLowDiskSpace() {
        if (this.storageSource.checkForLowDiskSpace()) {
            this.minecraft.execute(() -> SystemToast.onLowDiskSpace(this.minecraft));
        }
    }

    @Override
    public void reportChunkLoadFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
        super.reportChunkLoadFailure(throwable, regionStorageInfo, chunkPos);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkLoadFailure(this.minecraft, chunkPos));
    }

    @Override
    public void reportChunkSaveFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
        super.reportChunkSaveFailure(throwable, regionStorageInfo, chunkPos);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkSaveFailure(this.minecraft, chunkPos));
    }

    @Override
    public /* synthetic */ SampleLogger getTickTimeLogger() {
        return this.getTickTimeLogger();
    }
}

