/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.telemetry;

import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.client.telemetry.events.PerformanceMetricsEvent;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.client.telemetry.events.WorldLoadTimesEvent;
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class WorldSessionTelemetryManager {
    private final UUID worldSessionId = UUID.randomUUID();
    private final TelemetryEventSender eventSender;
    private final WorldLoadEvent worldLoadEvent;
    private final WorldUnloadEvent worldUnloadEvent = new WorldUnloadEvent();
    private final PerformanceMetricsEvent performanceMetricsEvent;
    private final WorldLoadTimesEvent worldLoadTimesEvent;

    public WorldSessionTelemetryManager(TelemetryEventSender telemetryEventSender, boolean bl, @Nullable Duration duration, @Nullable String string) {
        this.worldLoadEvent = new WorldLoadEvent(string);
        this.performanceMetricsEvent = new PerformanceMetricsEvent();
        this.worldLoadTimesEvent = new WorldLoadTimesEvent(bl, duration);
        this.eventSender = telemetryEventSender.decorate(builder -> {
            this.worldLoadEvent.addProperties((TelemetryPropertyMap.Builder)builder);
            builder.put(TelemetryProperty.WORLD_SESSION_ID, this.worldSessionId);
        });
    }

    public void tick() {
        this.performanceMetricsEvent.tick(this.eventSender);
    }

    public void onPlayerInfoReceived(GameType gameType, boolean bl) {
        this.worldLoadEvent.setGameMode(gameType, bl);
        this.worldUnloadEvent.onPlayerInfoReceived();
        this.worldSessionStart();
    }

    public void onServerBrandReceived(String string) {
        this.worldLoadEvent.setServerBrand(string);
        this.worldSessionStart();
    }

    public void setTime(long l) {
        this.worldUnloadEvent.setTime(l);
    }

    public void worldSessionStart() {
        if (this.worldLoadEvent.send(this.eventSender)) {
            this.worldLoadTimesEvent.send(this.eventSender);
            this.performanceMetricsEvent.start();
        }
    }

    public void onDisconnect() {
        this.worldLoadEvent.send(this.eventSender);
        this.performanceMetricsEvent.stop();
        this.worldUnloadEvent.send(this.eventSender);
    }

    public void onAdvancementDone(Level level, AdvancementHolder advancementHolder) {
        ResourceLocation resourceLocation = advancementHolder.id();
        if (advancementHolder.value().sendsTelemetryEvent() && "minecraft".equals(resourceLocation.getNamespace())) {
            long l = level.getGameTime();
            this.eventSender.send(TelemetryEventType.ADVANCEMENT_MADE, builder -> {
                builder.put(TelemetryProperty.ADVANCEMENT_ID, resourceLocation.toString());
                builder.put(TelemetryProperty.ADVANCEMENT_GAME_TIME, l);
            });
        }
    }
}

