/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.mojang.authlib.exceptions.AuthenticationUnavailableException
 *  com.mojang.authlib.exceptions.ForcedUsernameChangeException
 *  com.mojang.authlib.exceptions.InsufficientPrivilegesException
 *  com.mojang.authlib.exceptions.InvalidCredentialsException
 *  com.mojang.authlib.exceptions.UserBannedException
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.ForcedUsernameChangeException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserBannedException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.Crypt;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

public class ClientHandshakePacketListenerImpl
implements ClientLoginPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    @Nullable
    private final ServerData serverData;
    @Nullable
    private final Screen parent;
    private final Consumer<Component> updateStatus;
    private final Connection connection;
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadDuration;
    @Nullable
    private String minigameName;
    private final Map<ResourceLocation, byte[]> cookies;
    private final boolean wasTransferredTo;
    private final AtomicReference<State> state = new AtomicReference<State>(State.CONNECTING);

    public ClientHandshakePacketListenerImpl(Connection connection, Minecraft minecraft, @Nullable ServerData serverData, @Nullable Screen screen, boolean bl, @Nullable Duration duration, Consumer<Component> consumer, @Nullable TransferState transferState) {
        this.connection = connection;
        this.minecraft = minecraft;
        this.serverData = serverData;
        this.parent = screen;
        this.updateStatus = consumer;
        this.newWorld = bl;
        this.worldLoadDuration = duration;
        this.cookies = transferState != null ? new HashMap<ResourceLocation, byte[]>(transferState.cookies()) : new HashMap();
        this.wasTransferredTo = transferState != null;
    }

    private void switchState(State state) {
        State state3 = this.state.updateAndGet(state2 -> {
            if (!state.fromStates.contains(state2)) {
                throw new IllegalStateException("Tried to switch to " + String.valueOf((Object)state) + " from " + String.valueOf(state2) + ", but expected one of " + String.valueOf(state.fromStates));
            }
            return state;
        });
        this.updateStatus.accept(state3.message);
    }

    @Override
    public void handleHello(ClientboundHelloPacket clientboundHelloPacket) {
        ServerboundKeyPacket serverboundKeyPacket;
        Cipher cipher;
        Cipher cipher2;
        String string;
        this.switchState(State.AUTHORIZING);
        try {
            SecretKey secretKey = Crypt.generateSecretKey();
            PublicKey publicKey = clientboundHelloPacket.getPublicKey();
            string = new BigInteger(Crypt.digestData(clientboundHelloPacket.getServerId(), publicKey, secretKey)).toString(16);
            cipher2 = Crypt.getCipher(2, secretKey);
            cipher = Crypt.getCipher(1, secretKey);
            byte[] byArray = clientboundHelloPacket.getChallenge();
            serverboundKeyPacket = new ServerboundKeyPacket(secretKey, publicKey, byArray);
        }
        catch (Exception exception) {
            throw new IllegalStateException("Protocol error", exception);
        }
        if (clientboundHelloPacket.shouldAuthenticate()) {
            Util.ioPool().execute(() -> {
                Component component = this.authenticateServer(string);
                if (component != null) {
                    if (this.serverData != null && this.serverData.isLan()) {
                        LOGGER.warn(component.getString());
                    } else {
                        this.connection.disconnect(component);
                        return;
                    }
                }
                this.setEncryption(serverboundKeyPacket, cipher2, cipher);
            });
        } else {
            this.setEncryption(serverboundKeyPacket, cipher2, cipher);
        }
    }

    private void setEncryption(ServerboundKeyPacket serverboundKeyPacket, Cipher cipher, Cipher cipher2) {
        this.switchState(State.ENCRYPTING);
        this.connection.send(serverboundKeyPacket, PacketSendListener.thenRun(() -> this.connection.setEncryptionKey(cipher, cipher2)));
    }

    @Nullable
    private Component authenticateServer(String string) {
        try {
            this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getProfileId(), this.minecraft.getUser().getAccessToken(), string);
        }
        catch (AuthenticationUnavailableException authenticationUnavailableException) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.serversUnavailable"));
        }
        catch (InvalidCredentialsException invalidCredentialsException) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.invalidSession"));
        }
        catch (InsufficientPrivilegesException insufficientPrivilegesException) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.insufficientPrivileges"));
        }
        catch (ForcedUsernameChangeException | UserBannedException throwable) {
            return Component.translatable("disconnect.loginFailedInfo", Component.translatable("disconnect.loginFailedInfo.userBanned"));
        }
        catch (AuthenticationException authenticationException) {
            return Component.translatable("disconnect.loginFailedInfo", authenticationException.getMessage());
        }
        return null;
    }

    private MinecraftSessionService getMinecraftSessionService() {
        return this.minecraft.getMinecraftSessionService();
    }

    @Override
    public void handleLoginFinished(ClientboundLoginFinishedPacket clientboundLoginFinishedPacket) {
        this.switchState(State.JOINING);
        GameProfile gameProfile = clientboundLoginFinishedPacket.gameProfile();
        this.connection.setupInboundProtocol(ConfigurationProtocols.CLIENTBOUND, new ClientConfigurationPacketListenerImpl(this.minecraft, this.connection, new CommonListenerCookie(gameProfile, this.minecraft.getTelemetryManager().createWorldSessionManager(this.newWorld, this.worldLoadDuration, this.minigameName), ClientRegistryLayer.createRegistryAccess().compositeAccess(), FeatureFlags.DEFAULT_FLAGS, null, this.serverData, this.parent, this.cookies, null, Map.of(), ServerLinks.EMPTY)));
        this.connection.send(ServerboundLoginAcknowledgedPacket.INSTANCE);
        this.connection.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND);
        this.connection.send(new ServerboundCustomPayloadPacket(new BrandPayload(ClientBrandRetriever.getClientModName())));
        this.connection.send(new ServerboundClientInformationPacket(this.minecraft.options.buildPlayerInformation()));
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        Component component;
        Component component2 = component = this.wasTransferredTo ? CommonComponents.TRANSFER_CONNECT_FAILED : CommonComponents.CONNECT_FAILED;
        if (this.serverData != null && this.serverData.isRealm()) {
            this.minecraft.setScreen(new DisconnectedScreen(this.parent, component, disconnectionDetails.reason(), CommonComponents.GUI_BACK));
        } else {
            this.minecraft.setScreen(new DisconnectedScreen(this.parent, component, disconnectionDetails));
        }
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    public void handleDisconnect(ClientboundLoginDisconnectPacket clientboundLoginDisconnectPacket) {
        this.connection.disconnect(clientboundLoginDisconnectPacket.reason());
    }

    @Override
    public void handleCompression(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket) {
        if (!this.connection.isMemoryConnection()) {
            this.connection.setupCompression(clientboundLoginCompressionPacket.getCompressionThreshold(), false);
        }
    }

    @Override
    public void handleCustomQuery(ClientboundCustomQueryPacket clientboundCustomQueryPacket) {
        this.updateStatus.accept(Component.translatable("connect.negotiating"));
        this.connection.send(new ServerboundCustomQueryAnswerPacket(clientboundCustomQueryPacket.transactionId(), null));
    }

    public void setMinigameName(@Nullable String string) {
        this.minigameName = string;
    }

    @Override
    public void handleRequestCookie(ClientboundCookieRequestPacket clientboundCookieRequestPacket) {
        this.connection.send(new ServerboundCookieResponsePacket(clientboundCookieRequestPacket.key(), this.cookies.get(clientboundCookieRequestPacket.key())));
    }

    @Override
    public void fillListenerSpecificCrashDetails(CrashReport crashReport, CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Server type", () -> this.serverData != null ? this.serverData.type().toString() : "<unknown>");
        crashReportCategory.setDetail("Login phase", () -> this.state.get().toString());
        crashReportCategory.setDetail("Is Local", () -> String.valueOf(this.connection.isMemoryConnection()));
    }

    static enum State {
        CONNECTING(Component.translatable("connect.connecting"), Set.of()),
        AUTHORIZING(Component.translatable("connect.authorizing"), Set.of(CONNECTING)),
        ENCRYPTING(Component.translatable("connect.encrypting"), Set.of(AUTHORIZING)),
        JOINING(Component.translatable("connect.joining"), Set.of(ENCRYPTING, CONNECTING));

        final Component message;
        final Set<State> fromStates;

        private State(Component component, Set<State> set) {
            this.message = component;
            this.fromStates = set;
        }
    }
}

