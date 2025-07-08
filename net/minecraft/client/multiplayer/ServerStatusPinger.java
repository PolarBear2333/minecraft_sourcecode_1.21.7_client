/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.Bootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.socket.nio.NioSocketChannel
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.LegacyServerPinger;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.slf4j.Logger;

public class ServerStatusPinger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component CANT_CONNECT_MESSAGE = Component.translatable("multiplayer.status.cannot_connect").withColor(-65536);
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public void pingServer(final ServerData serverData, final Runnable runnable, final Runnable runnable2) throws UnknownHostException {
        final ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
        Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
        if (optional.isEmpty()) {
            this.onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, serverData);
            return;
        }
        final InetSocketAddress inetSocketAddress = optional.get();
        final Connection connection = Connection.connectToServer(inetSocketAddress, false, null);
        this.connections.add(connection);
        serverData.motd = Component.translatable("multiplayer.status.pinging");
        serverData.playerList = Collections.emptyList();
        ClientStatusPacketListener clientStatusPacketListener = new ClientStatusPacketListener(){
            private boolean success;
            private boolean receivedPing;
            private long pingStart;

            @Override
            public void handleStatusResponse(ClientboundStatusResponsePacket clientboundStatusResponsePacket) {
                if (this.receivedPing) {
                    connection.disconnect(Component.translatable("multiplayer.status.unrequested"));
                    return;
                }
                this.receivedPing = true;
                ServerStatus serverStatus = clientboundStatusResponsePacket.status();
                serverData.motd = serverStatus.description();
                serverStatus.version().ifPresentOrElse(version -> {
                    serverData2.version = Component.literal(version.name());
                    serverData2.protocol = version.protocol();
                }, () -> {
                    serverData2.version = Component.translatable("multiplayer.status.old");
                    serverData2.protocol = 0;
                });
                serverStatus.players().ifPresentOrElse(players -> {
                    serverData2.status = ServerStatusPinger.formatPlayerCount(players.online(), players.max());
                    serverData2.players = players;
                    if (!players.sample().isEmpty()) {
                        ArrayList<Component> arrayList = new ArrayList<Component>(players.sample().size());
                        for (GameProfile gameProfile : players.sample()) {
                            arrayList.add(Component.literal(gameProfile.getName()));
                        }
                        if (players.sample().size() < players.online()) {
                            arrayList.add(Component.translatable("multiplayer.status.and_more", players.online() - players.sample().size()));
                        }
                        serverData2.playerList = arrayList;
                    } else {
                        serverData2.playerList = List.of();
                    }
                }, () -> {
                    serverData2.status = Component.translatable("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY);
                });
                serverStatus.favicon().ifPresent(favicon -> {
                    if (!Arrays.equals(favicon.iconBytes(), serverData.getIconBytes())) {
                        serverData.setIconBytes(ServerData.validateIcon(favicon.iconBytes()));
                        runnable.run();
                    }
                });
                this.pingStart = Util.getMillis();
                connection.send(new ServerboundPingRequestPacket(this.pingStart));
                this.success = true;
            }

            @Override
            public void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket) {
                long l = this.pingStart;
                long l2 = Util.getMillis();
                serverData.ping = l2 - l;
                connection.disconnect(Component.translatable("multiplayer.status.finished"));
                runnable2.run();
            }

            @Override
            public void onDisconnect(DisconnectionDetails disconnectionDetails) {
                if (!this.success) {
                    ServerStatusPinger.this.onPingFailed(disconnectionDetails.reason(), serverData);
                    ServerStatusPinger.this.pingLegacyServer(inetSocketAddress, serverAddress, serverData);
                }
            }

            @Override
            public boolean isAcceptingMessages() {
                return connection.isConnected();
            }
        };
        try {
            connection.initiateServerboundStatusConnection(serverAddress.getHost(), serverAddress.getPort(), clientStatusPacketListener);
            connection.send(ServerboundStatusRequestPacket.INSTANCE);
        }
        catch (Throwable throwable) {
            LOGGER.error("Failed to ping server {}", (Object)serverAddress, (Object)throwable);
        }
    }

    void onPingFailed(Component component, ServerData serverData) {
        LOGGER.error("Can't ping {}: {}", (Object)serverData.ip, (Object)component.getString());
        serverData.motd = CANT_CONNECT_MESSAGE;
        serverData.status = CommonComponents.EMPTY;
    }

    void pingLegacyServer(InetSocketAddress inetSocketAddress, final ServerAddress serverAddress, final ServerData serverData) {
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group((EventLoopGroup)Connection.NETWORK_WORKER_GROUP.get())).handler((ChannelHandler)new ChannelInitializer<Channel>(this){

            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                channel.pipeline().addLast(new ChannelHandler[]{new LegacyServerPinger(serverAddress, (n, string, string2, n2, n3) -> {
                    serverData.setState(ServerData.State.INCOMPATIBLE);
                    serverData2.version = Component.literal(string);
                    serverData2.motd = Component.literal(string2);
                    serverData2.status = ServerStatusPinger.formatPlayerCount(n2, n3);
                    serverData2.players = new ServerStatus.Players(n3, n2, List.of());
                })});
            }
        })).channel(NioSocketChannel.class)).connect(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
    }

    public static Component formatPlayerCount(int n, int n2) {
        MutableComponent mutableComponent = Component.literal(Integer.toString(n)).withStyle(ChatFormatting.GRAY);
        MutableComponent mutableComponent2 = Component.literal(Integer.toString(n2)).withStyle(ChatFormatting.GRAY);
        return Component.translatable("multiplayer.status.player_count", mutableComponent, mutableComponent2).withStyle(ChatFormatting.DARK_GRAY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (connection.isConnected()) {
                    connection.tick();
                    continue;
                }
                iterator.remove();
                connection.handleDisconnection();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeAll() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (!connection.isConnected()) continue;
                iterator.remove();
                connection.disconnect(Component.translatable("multiplayer.status.cancelled"));
            }
        }
    }
}

