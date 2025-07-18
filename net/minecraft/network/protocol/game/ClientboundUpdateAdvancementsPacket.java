/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAdvancementsPacket> STREAM_CODEC = Packet.codec(ClientboundUpdateAdvancementsPacket::write, ClientboundUpdateAdvancementsPacket::new);
    private final boolean reset;
    private final List<AdvancementHolder> added;
    private final Set<ResourceLocation> removed;
    private final Map<ResourceLocation, AdvancementProgress> progress;
    private final boolean showAdvancements;

    public ClientboundUpdateAdvancementsPacket(boolean bl, Collection<AdvancementHolder> collection, Set<ResourceLocation> set, Map<ResourceLocation, AdvancementProgress> map, boolean bl2) {
        this.reset = bl;
        this.added = List.copyOf(collection);
        this.removed = Set.copyOf(set);
        this.progress = Map.copyOf(map);
        this.showAdvancements = bl2;
    }

    private ClientboundUpdateAdvancementsPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.reset = registryFriendlyByteBuf.readBoolean();
        this.added = (List)AdvancementHolder.LIST_STREAM_CODEC.decode(registryFriendlyByteBuf);
        this.removed = registryFriendlyByteBuf.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
        this.progress = registryFriendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
        this.showAdvancements = registryFriendlyByteBuf.readBoolean();
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeBoolean(this.reset);
        AdvancementHolder.LIST_STREAM_CODEC.encode(registryFriendlyByteBuf, this.added);
        registryFriendlyByteBuf.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
        registryFriendlyByteBuf.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (friendlyByteBuf, advancementProgress) -> advancementProgress.serializeToNetwork((FriendlyByteBuf)((Object)friendlyByteBuf)));
        registryFriendlyByteBuf.writeBoolean(this.showAdvancements);
    }

    @Override
    public PacketType<ClientboundUpdateAdvancementsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateAdvancementsPacket(this);
    }

    public List<AdvancementHolder> getAdded() {
        return this.added;
    }

    public Set<ResourceLocation> getRemoved() {
        return this.removed;
    }

    public Map<ResourceLocation, AdvancementProgress> getProgress() {
        return this.progress;
    }

    public boolean shouldReset() {
        return this.reset;
    }

    public boolean shouldShowAdvancements() {
        return this.showAdvancements;
    }
}

