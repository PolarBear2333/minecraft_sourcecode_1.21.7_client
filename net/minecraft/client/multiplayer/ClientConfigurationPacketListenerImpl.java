/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.KnownPacksManager;
import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundResetChatPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

public class ClientConfigurationPacketListenerImpl
extends ClientCommonPacketListenerImpl
implements ClientConfigurationPacketListener,
TickablePacketListener {
    static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile localGameProfile;
    private FeatureFlagSet enabledFeatures;
    private final RegistryAccess.Frozen receivedRegistries;
    private final RegistryDataCollector registryDataCollector = new RegistryDataCollector();
    @Nullable
    private KnownPacksManager knownPacks;
    @Nullable
    protected ChatComponent.State chatState;

    public ClientConfigurationPacketListenerImpl(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
        this.localGameProfile = commonListenerCookie.localGameProfile();
        this.receivedRegistries = commonListenerCookie.receivedRegistries();
        this.enabledFeatures = commonListenerCookie.enabledFeatures();
        this.chatState = commonListenerCookie.chatState();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    protected void handleCustomPayload(CustomPacketPayload customPacketPayload) {
        this.handleUnknownCustomPayload(customPacketPayload);
    }

    private void handleUnknownCustomPayload(CustomPacketPayload customPacketPayload) {
        LOGGER.warn("Unknown custom packet payload: {}", (Object)customPacketPayload.type().id());
    }

    @Override
    public void handleRegistryData(ClientboundRegistryDataPacket clientboundRegistryDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRegistryDataPacket, this, this.minecraft);
        this.registryDataCollector.appendContents(clientboundRegistryDataPacket.registry(), clientboundRegistryDataPacket.entries());
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateTagsPacket, this, this.minecraft);
        this.registryDataCollector.appendTags(clientboundUpdateTagsPacket.getTags());
    }

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundUpdateEnabledFeaturesPacket) {
        this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(clientboundUpdateEnabledFeaturesPacket.features());
    }

    @Override
    public void handleSelectKnownPacks(ClientboundSelectKnownPacks clientboundSelectKnownPacks) {
        PacketUtils.ensureRunningOnSameThread(clientboundSelectKnownPacks, this, this.minecraft);
        if (this.knownPacks == null) {
            this.knownPacks = new KnownPacksManager();
        }
        List<KnownPack> list = this.knownPacks.trySelectingPacks(clientboundSelectKnownPacks.knownPacks());
        this.send(new ServerboundSelectKnownPacks(list));
    }

    @Override
    public void handleResetChat(ClientboundResetChatPacket clientboundResetChatPacket) {
        this.chatState = null;
    }

    private <T> T runWithResources(Function<ResourceProvider, T> function) {
        if (this.knownPacks == null) {
            return function.apply(ResourceProvider.EMPTY);
        }
        try (CloseableResourceManager closeableResourceManager = this.knownPacks.createResourceManager();){
            T t = function.apply(closeableResourceManager);
            return t;
        }
    }

    @Override
    public void handleConfigurationFinished(ClientboundFinishConfigurationPacket clientboundFinishConfigurationPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundFinishConfigurationPacket, this, this.minecraft);
        RegistryAccess.Frozen frozen = this.runWithResources(resourceProvider -> this.registryDataCollector.collectGameRegistries((ResourceProvider)resourceProvider, this.receivedRegistries, this.connection.isMemoryConnection()));
        this.connection.setupInboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(frozen)), new ClientPacketListener(this.minecraft, this.connection, new CommonListenerCookie(this.localGameProfile, this.telemetryManager, frozen, this.enabledFeatures, this.serverBrand, this.serverData, this.postDisconnectScreen, this.serverCookies, this.chatState, this.customReportDetails, this.serverLinks())));
        this.connection.send(ServerboundFinishConfigurationPacket.INSTANCE);
        this.connection.setupOutboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(frozen), new GameProtocols.Context(this){

            @Override
            public boolean hasInfiniteMaterials() {
                return true;
            }
        }));
    }

    @Override
    public void tick() {
        this.sendDeferredPackets();
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        super.onDisconnect(disconnectionDetails);
        this.minecraft.clearDownloadedResourcePacks();
    }

    @Override
    protected DialogConnectionAccess createDialogAccess() {
        return new DialogConnectionAccess(){

            @Override
            public void disconnect(Component component) {
                ClientConfigurationPacketListenerImpl.this.connection.disconnect(component);
            }

            @Override
            public void runCommand(String string, @Nullable Screen screen) {
                LOGGER.warn("Commands are not supported in configuration phase, trying to run '{}'", (Object)string);
            }

            @Override
            public void openDialog(Holder<Dialog> holder, @Nullable Screen screen) {
                ClientConfigurationPacketListenerImpl.this.showDialog(holder, this, screen);
            }

            @Override
            public void sendCustomAction(ResourceLocation resourceLocation, Optional<Tag> optional) {
                ClientConfigurationPacketListenerImpl.this.send(new ServerboundCustomClickActionPacket(resourceLocation, optional));
            }

            @Override
            public ServerLinks serverLinks() {
                return ClientConfigurationPacketListenerImpl.this.serverLinks();
            }
        };
    }
}

