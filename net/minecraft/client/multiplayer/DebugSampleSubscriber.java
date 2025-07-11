/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer;

import java.util.EnumMap;
import net.minecraft.Util;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundDebugSampleSubscriptionPacket;
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public class DebugSampleSubscriber {
    public static final int REQUEST_INTERVAL_MS = 5000;
    private final ClientPacketListener connection;
    private final DebugScreenOverlay debugScreenOverlay;
    private final EnumMap<RemoteDebugSampleType, Long> lastRequested;

    public DebugSampleSubscriber(ClientPacketListener clientPacketListener, DebugScreenOverlay debugScreenOverlay) {
        this.debugScreenOverlay = debugScreenOverlay;
        this.connection = clientPacketListener;
        this.lastRequested = new EnumMap(RemoteDebugSampleType.class);
    }

    public void tick() {
        if (this.debugScreenOverlay.showFpsCharts()) {
            this.sendSubscriptionRequestIfNeeded(RemoteDebugSampleType.TICK_TIME);
        }
    }

    private void sendSubscriptionRequestIfNeeded(RemoteDebugSampleType remoteDebugSampleType) {
        long l = Util.getMillis();
        if (l > this.lastRequested.getOrDefault((Object)remoteDebugSampleType, 0L) + 5000L) {
            this.connection.send(new ServerboundDebugSampleSubscriptionPacket(remoteDebugSampleType));
            this.lastRequested.put(remoteDebugSampleType, l);
        }
    }
}

