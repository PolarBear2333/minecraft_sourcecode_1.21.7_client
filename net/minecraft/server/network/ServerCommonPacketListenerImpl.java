/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  io.netty.channel.ChannelFutureListener
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import org.slf4j.Logger;

public abstract class ServerCommonPacketListenerImpl
implements ServerCommonPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int LATENCY_CHECK_INTERVAL = 15000;
    private static final int CLOSED_LISTENER_TIMEOUT = 15000;
    private static final Component TIMEOUT_DISCONNECTION_MESSAGE = Component.translatable("disconnect.timeout");
    static final Component DISCONNECT_UNEXPECTED_QUERY = Component.translatable("multiplayer.disconnect.unexpected_query_response");
    protected final MinecraftServer server;
    protected final Connection connection;
    private final boolean transferred;
    private long keepAliveTime;
    private boolean keepAlivePending;
    private long keepAliveChallenge;
    private long closedListenerTime;
    private boolean closed = false;
    private int latency;
    private volatile boolean suspendFlushingOnServerThread = false;

    public ServerCommonPacketListenerImpl(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        this.server = minecraftServer;
        this.connection = connection;
        this.keepAliveTime = Util.getMillis();
        this.latency = commonListenerCookie.latency();
        this.transferred = commonListenerCookie.transferred();
    }

    private void close() {
        if (!this.closed) {
            this.closedListenerTime = Util.getMillis();
            this.closed = true;
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        if (this.isSingleplayerOwner()) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.halt(false);
        }
    }

    @Override
    public void onPacketError(Packet packet, Exception exception) throws ReportedException {
        ServerCommonPacketListener.super.onPacketError(packet, exception);
        this.server.reportPacketHandlingException(exception, packet.type());
    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
        if (this.keepAlivePending && serverboundKeepAlivePacket.getId() == this.keepAliveChallenge) {
            int n = (int)(Util.getMillis() - this.keepAliveTime);
            this.latency = (this.latency * 3 + n) / 4;
            this.keepAlivePending = false;
        } else if (!this.isSingleplayerOwner()) {
            this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
        }
    }

    @Override
    public void handlePong(ServerboundPongPacket serverboundPongPacket) {
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
    }

    @Override
    public void handleCustomClickAction(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundCustomClickActionPacket, this, this.server);
        this.server.handleCustomClickAction(serverboundCustomClickActionPacket.id(), serverboundCustomClickActionPacket.payload());
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundResourcePackPacket, this, this.server);
        if (serverboundResourcePackPacket.action() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
            LOGGER.info("Disconnecting {} due to resource pack {} rejection", (Object)this.playerProfile().getName(), (Object)serverboundResourcePackPacket.id());
            this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
        }
    }

    @Override
    public void handleCookieResponse(ServerboundCookieResponsePacket serverboundCookieResponsePacket) {
        this.disconnect(DISCONNECT_UNEXPECTED_QUERY);
    }

    protected void keepConnectionAlive() {
        Profiler.get().push("keepAlive");
        long l = Util.getMillis();
        if (!this.isSingleplayerOwner() && l - this.keepAliveTime >= 15000L) {
            if (this.keepAlivePending) {
                this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
            } else if (this.checkIfClosed(l)) {
                this.keepAlivePending = true;
                this.keepAliveTime = l;
                this.keepAliveChallenge = l;
                this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
            }
        }
        Profiler.get().pop();
    }

    private boolean checkIfClosed(long l) {
        if (this.closed) {
            if (l - this.closedListenerTime >= 15000L) {
                this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
            }
            return false;
        }
        return true;
    }

    public void suspendFlushing() {
        this.suspendFlushingOnServerThread = true;
    }

    public void resumeFlushing() {
        this.suspendFlushingOnServerThread = false;
        this.connection.flushChannel();
    }

    public void send(Packet<?> packet) {
        this.send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener) {
        if (packet.isTerminal()) {
            this.close();
        }
        boolean bl = !this.suspendFlushingOnServerThread || !this.server.isSameThread();
        try {
            this.connection.send(packet, channelFutureListener, bl);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Sending packet");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Packet being sent");
            crashReportCategory.setDetail("Packet class", () -> packet.getClass().getCanonicalName());
            throw new ReportedException(crashReport);
        }
    }

    public void disconnect(Component component) {
        this.disconnect(new DisconnectionDetails(component));
    }

    public void disconnect(DisconnectionDetails disconnectionDetails) {
        this.connection.send(new ClientboundDisconnectPacket(disconnectionDetails.reason()), PacketSendListener.thenRun(() -> this.connection.disconnect(disconnectionDetails)));
        this.connection.setReadOnly();
        this.server.executeBlocking(this.connection::handleDisconnection);
    }

    protected boolean isSingleplayerOwner() {
        return this.server.isSingleplayerOwner(this.playerProfile());
    }

    protected abstract GameProfile playerProfile();

    @VisibleForDebug
    public GameProfile getOwner() {
        return this.playerProfile();
    }

    public int latency() {
        return this.latency;
    }

    protected CommonListenerCookie createCookie(ClientInformation clientInformation) {
        return new CommonListenerCookie(this.playerProfile(), this.latency, clientInformation, this.transferred);
    }
}

