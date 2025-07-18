/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.network.config.ServerResourcePackConfigurationTask;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

public class ServerConfigurationPacketListenerImpl
extends ServerCommonPacketListenerImpl
implements ServerConfigurationPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component DISCONNECT_REASON_INVALID_DATA = Component.translatable("multiplayer.disconnect.invalid_player_data");
    private final GameProfile gameProfile;
    private final Queue<ConfigurationTask> configurationTasks = new ConcurrentLinkedQueue<ConfigurationTask>();
    @Nullable
    private ConfigurationTask currentTask;
    private ClientInformation clientInformation;
    @Nullable
    private SynchronizeRegistriesTask synchronizeRegistriesTask;

    public ServerConfigurationPacketListenerImpl(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
        this.gameProfile = commonListenerCookie.gameProfile();
        this.clientInformation = commonListenerCookie.clientInformation();
    }

    @Override
    protected GameProfile playerProfile() {
        return this.gameProfile;
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        LOGGER.info("{} lost connection: {}", (Object)this.gameProfile, (Object)disconnectionDetails.reason().getString());
        super.onDisconnect(disconnectionDetails);
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void startConfiguration() {
        this.send(new ClientboundCustomPayloadPacket(new BrandPayload(this.server.getServerModName())));
        ServerLinks serverLinks = this.server.serverLinks();
        if (!serverLinks.isEmpty()) {
            this.send(new ClientboundServerLinksPacket(serverLinks.untrust()));
        }
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = this.server.registries();
        List<KnownPack> list = this.server.getResourceManager().listPacks().flatMap(packResources -> packResources.location().knownPackInfo().stream()).toList();
        this.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(this.server.getWorldData().enabledFeatures())));
        this.synchronizeRegistriesTask = new SynchronizeRegistriesTask(list, layeredRegistryAccess);
        this.configurationTasks.add(this.synchronizeRegistriesTask);
        this.addOptionalTasks();
        this.configurationTasks.add(new JoinWorldTask());
        this.startNextTask();
    }

    public void returnToWorld() {
        this.configurationTasks.add(new JoinWorldTask());
        this.startNextTask();
    }

    private void addOptionalTasks() {
        this.server.getServerResourcePack().ifPresent(serverResourcePackInfo -> this.configurationTasks.add(new ServerResourcePackConfigurationTask((MinecraftServer.ServerResourcePackInfo)serverResourcePackInfo)));
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
        this.clientInformation = serverboundClientInformationPacket.information();
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
        super.handleResourcePackResponse(serverboundResourcePackPacket);
        if (serverboundResourcePackPacket.action().isTerminal()) {
            this.finishCurrentTask(ServerResourcePackConfigurationTask.TYPE);
        }
    }

    @Override
    public void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundSelectKnownPacks) {
        PacketUtils.ensureRunningOnSameThread(serverboundSelectKnownPacks, this, this.server);
        if (this.synchronizeRegistriesTask == null) {
            throw new IllegalStateException("Unexpected response from client: received pack selection, but no negotiation ongoing");
        }
        this.synchronizeRegistriesTask.handleResponse(serverboundSelectKnownPacks.knownPacks(), this::send);
        this.finishCurrentTask(SynchronizeRegistriesTask.TYPE);
    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundFinishConfigurationPacket, this, this.server);
        this.finishCurrentTask(JoinWorldTask.TYPE);
        this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess())));
        try {
            PlayerList playerList = this.server.getPlayerList();
            if (playerList.getPlayer(this.gameProfile.getId()) != null) {
                this.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
                return;
            }
            Component component = playerList.canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
            if (component != null) {
                this.disconnect(component);
                return;
            }
            ServerPlayer serverPlayer = new ServerPlayer(this.server, this.server.overworld(), this.gameProfile, this.clientInformation);
            playerList.placeNewPlayer(this.connection, serverPlayer, this.createCookie(this.clientInformation));
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't place player in world", (Throwable)exception);
            this.connection.send(new ClientboundDisconnectPacket(DISCONNECT_REASON_INVALID_DATA));
            this.connection.disconnect(DISCONNECT_REASON_INVALID_DATA);
        }
    }

    @Override
    public void tick() {
        this.keepConnectionAlive();
    }

    private void startNextTask() {
        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
        }
        if (!this.isAcceptingMessages()) {
            return;
        }
        ConfigurationTask configurationTask = this.configurationTasks.poll();
        if (configurationTask != null) {
            this.currentTask = configurationTask;
            configurationTask.start(this::send);
        }
    }

    private void finishCurrentTask(ConfigurationTask.Type type) {
        ConfigurationTask.Type type2;
        ConfigurationTask.Type type3 = type2 = this.currentTask != null ? this.currentTask.type() : null;
        if (!type.equals(type2)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + String.valueOf(type2) + ", requested: " + String.valueOf(type));
        }
        this.currentTask = null;
        this.startNextTask();
    }
}

