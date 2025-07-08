/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.SummaryReporter;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.ChunkRegionReadEvent;
import net.minecraft.util.profiling.jfr.event.ChunkRegionWriteEvent;
import net.minecraft.util.profiling.jfr.event.NetworkSummaryEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.StructureGenerationEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class JfrProfiler
implements JvmProfiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ROOT_CATEGORY = "Minecraft";
    public static final String WORLD_GEN_CATEGORY = "World Generation";
    public static final String TICK_CATEGORY = "Ticking";
    public static final String NETWORK_CATEGORY = "Network";
    public static final String STORAGE_CATEGORY = "Storage";
    private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(ChunkGenerationEvent.class, ChunkRegionReadEvent.class, ChunkRegionWriteEvent.class, PacketReceivedEvent.class, PacketSentEvent.class, NetworkSummaryEvent.class, ServerTickTimeEvent.class, StructureGenerationEvent.class, WorldLoadFinishedEvent.class);
    private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd-HHmmss").toFormatter().withZone(ZoneId.systemDefault());
    private static final JfrProfiler INSTANCE = new JfrProfiler();
    @Nullable
    Recording recording;
    private float currentAverageTickTime;
    private final Map<String, NetworkSummaryEvent.SumAggregation> networkTrafficByAddress = new ConcurrentHashMap<String, NetworkSummaryEvent.SumAggregation>();

    private JfrProfiler() {
        CUSTOM_EVENTS.forEach(FlightRecorder::register);
        FlightRecorder.addPeriodicEvent(ServerTickTimeEvent.class, () -> new ServerTickTimeEvent(this.currentAverageTickTime).commit());
        FlightRecorder.addPeriodicEvent(NetworkSummaryEvent.class, () -> {
            Iterator<NetworkSummaryEvent.SumAggregation> iterator = this.networkTrafficByAddress.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().commitEvent();
                iterator.remove();
            }
        });
    }

    public static JfrProfiler getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean start(Environment environment) {
        boolean bl;
        URL uRL = JfrProfiler.class.getResource(FLIGHT_RECORDER_CONFIG);
        if (uRL == null) {
            LOGGER.warn("Could not find default flight recorder config at {}", (Object)FLIGHT_RECORDER_CONFIG);
            return false;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));
        try {
            bl = this.start(bufferedReader, environment);
        }
        catch (Throwable throwable) {
            try {
                try {
                    bufferedReader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to start flight recorder using configuration at {}", (Object)uRL, (Object)iOException);
                return false;
            }
        }
        bufferedReader.close();
        return bl;
    }

    @Override
    public Path stop() {
        if (this.recording == null) {
            throw new IllegalStateException("Not currently profiling");
        }
        this.networkTrafficByAddress.clear();
        Path path = this.recording.getDestination();
        this.recording.stop();
        return path;
    }

    @Override
    public boolean isRunning() {
        return this.recording != null;
    }

    @Override
    public boolean isAvailable() {
        return FlightRecorder.isAvailable();
    }

    private boolean start(Reader reader, Environment environment) {
        if (this.isRunning()) {
            LOGGER.warn("Profiling already in progress");
            return false;
        }
        try {
            Configuration configuration = Configuration.create(reader);
            String string = DATE_TIME_FORMATTER.format(Instant.now());
            this.recording = Util.make(new Recording(configuration), recording -> {
                CUSTOM_EVENTS.forEach(recording::enable);
                recording.setDumpOnExit(true);
                recording.setToDisk(true);
                recording.setName(String.format(Locale.ROOT, "%s-%s-%s", environment.getDescription(), SharedConstants.getCurrentVersion().name(), string));
            });
            Path path = Paths.get(String.format(Locale.ROOT, "debug/%s-%s.jfr", environment.getDescription(), string), new String[0]);
            FileUtil.createDirectoriesSafe(path.getParent());
            this.recording.setDestination(path);
            this.recording.start();
            this.setupSummaryListener();
        }
        catch (IOException | ParseException exception) {
            LOGGER.warn("Failed to start jfr profiling", (Throwable)exception);
            return false;
        }
        LOGGER.info("Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command", new Object[]{this.recording.getId(), this.recording.getName(), this.recording.getDestination()});
        return true;
    }

    private void setupSummaryListener() {
        FlightRecorder.addListener(new FlightRecorderListener(){
            final SummaryReporter summaryReporter = new SummaryReporter(() -> {
                JfrProfiler.this.recording = null;
            });

            @Override
            public void recordingStateChanged(Recording recording) {
                if (recording != JfrProfiler.this.recording || recording.getState() != RecordingState.STOPPED) {
                    return;
                }
                this.summaryReporter.recordingStopped(recording.getDestination());
                FlightRecorder.removeListener(this);
            }
        });
    }

    @Override
    public void onServerTick(float f) {
        if (ServerTickTimeEvent.TYPE.isEnabled()) {
            this.currentAverageTickTime = f;
        }
    }

    @Override
    public void onPacketReceived(ConnectionProtocol connectionProtocol, PacketType<?> packetType, SocketAddress socketAddress, int n) {
        if (PacketReceivedEvent.TYPE.isEnabled()) {
            new PacketReceivedEvent(connectionProtocol.id(), packetType.flow().id(), packetType.id().toString(), socketAddress, n).commit();
        }
        if (NetworkSummaryEvent.TYPE.isEnabled()) {
            this.networkStatFor(socketAddress).trackReceivedPacket(n);
        }
    }

    @Override
    public void onPacketSent(ConnectionProtocol connectionProtocol, PacketType<?> packetType, SocketAddress socketAddress, int n) {
        if (PacketSentEvent.TYPE.isEnabled()) {
            new PacketSentEvent(connectionProtocol.id(), packetType.flow().id(), packetType.id().toString(), socketAddress, n).commit();
        }
        if (NetworkSummaryEvent.TYPE.isEnabled()) {
            this.networkStatFor(socketAddress).trackSentPacket(n);
        }
    }

    private NetworkSummaryEvent.SumAggregation networkStatFor(SocketAddress socketAddress) {
        return this.networkTrafficByAddress.computeIfAbsent(socketAddress.toString(), NetworkSummaryEvent.SumAggregation::new);
    }

    @Override
    public void onRegionFileRead(RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, RegionFileVersion regionFileVersion, int n) {
        if (ChunkRegionReadEvent.TYPE.isEnabled()) {
            new ChunkRegionReadEvent(regionStorageInfo, chunkPos, regionFileVersion, n).commit();
        }
    }

    @Override
    public void onRegionFileWrite(RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, RegionFileVersion regionFileVersion, int n) {
        if (ChunkRegionWriteEvent.TYPE.isEnabled()) {
            new ChunkRegionWriteEvent(regionStorageInfo, chunkPos, regionFileVersion, n).commit();
        }
    }

    @Override
    @Nullable
    public ProfiledDuration onWorldLoadedStarted() {
        if (!WorldLoadFinishedEvent.TYPE.isEnabled()) {
            return null;
        }
        WorldLoadFinishedEvent worldLoadFinishedEvent = new WorldLoadFinishedEvent();
        worldLoadFinishedEvent.begin();
        return bl -> worldLoadFinishedEvent.commit();
    }

    @Override
    @Nullable
    public ProfiledDuration onChunkGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string) {
        if (!ChunkGenerationEvent.TYPE.isEnabled()) {
            return null;
        }
        ChunkGenerationEvent chunkGenerationEvent = new ChunkGenerationEvent(chunkPos, resourceKey, string);
        chunkGenerationEvent.begin();
        return bl -> chunkGenerationEvent.commit();
    }

    @Override
    @Nullable
    public ProfiledDuration onStructureGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, Holder<Structure> holder) {
        if (!StructureGenerationEvent.TYPE.isEnabled()) {
            return null;
        }
        StructureGenerationEvent structureGenerationEvent = new StructureGenerationEvent(chunkPos, holder, resourceKey);
        structureGenerationEvent.begin();
        return bl -> {
            structureGenerationEvent.success = bl;
            structureGenerationEvent.commit();
        };
    }
}

