/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.resources.server.ServerPackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.slf4j.Logger;

public class RealmsConnect {
    static final Logger LOGGER = LogUtils.getLogger();
    final Screen onlineScreen;
    volatile boolean aborted;
    @Nullable
    Connection connection;

    public RealmsConnect(Screen screen) {
        this.onlineScreen = screen;
    }

    public void connect(final RealmsServer realmsServer, ServerAddress serverAddress) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.prepareForMultiplayer();
        minecraft.getNarrator().saySystemNow(Component.translatable("mco.connect.success"));
        final String string = serverAddress.getHost();
        final int n = serverAddress.getPort();
        new Thread("Realms-connect-task"){

            @Override
            public void run() {
                InetSocketAddress inetSocketAddress = null;
                try {
                    inetSocketAddress = new InetSocketAddress(string, n);
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport(), minecraft.getDebugOverlay().getBandwidthLogger());
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    ClientHandshakePacketListenerImpl clientHandshakePacketListenerImpl = new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, minecraft, realmsServer.toServerData(string), RealmsConnect.this.onlineScreen, false, null, component -> {}, null);
                    if (realmsServer.isMinigameActive()) {
                        clientHandshakePacketListenerImpl.setMinigameName(realmsServer.minigameName);
                    }
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.initiateServerboundPlayConnection(string, n, clientHandshakePacketListenerImpl);
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getUser().getProfileId()));
                    minecraft.updateReportEnvironment(ReportEnvironment.realm(realmsServer));
                    minecraft.quickPlayLog().setWorldData(QuickPlayLog.Type.REALMS, String.valueOf(realmsServer.id), Objects.requireNonNullElse(realmsServer.name, "unknown"));
                    minecraft.getDownloadedPackSource().configureForServerControl(RealmsConnect.this.connection, ServerPackManager.PackPromptStatus.ALLOWED);
                }
                catch (Exception exception) {
                    Object object;
                    minecraft.getDownloadedPackSource().cleanupAfterDisconnect();
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to world", (Throwable)exception);
                    String string2 = exception.toString();
                    if (inetSocketAddress != null) {
                        object = String.valueOf(inetSocketAddress) + ":" + n;
                        string2 = string2.replaceAll((String)object, "");
                    }
                    object = new DisconnectedScreen(RealmsConnect.this.onlineScreen, (Component)Component.translatable("mco.connect.failed"), Component.translatable("disconnect.genericReason", string2), CommonComponents.GUI_BACK);
                    minecraft.execute(() -> 1.lambda$run$1(minecraft, (DisconnectedScreen)object));
                }
            }

            private static /* synthetic */ void lambda$run$1(Minecraft minecraft2, DisconnectedScreen disconnectedScreen) {
                minecraft2.setScreen(disconnectedScreen);
            }
        }.start();
    }

    public void abort() {
        this.aborted = true;
        if (this.connection != null && this.connection.isConnected()) {
            this.connection.disconnect(Component.translatable("disconnect.genericReason"));
            this.connection.handleDisconnection();
        }
    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }
    }
}

