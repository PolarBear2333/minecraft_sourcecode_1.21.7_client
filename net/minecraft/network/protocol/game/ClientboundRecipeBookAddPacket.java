/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

public record ClientboundRecipeBookAddPacket(List<Entry> entries, boolean replace) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipeBookAddPacket> STREAM_CODEC = StreamCodec.composite(Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRecipeBookAddPacket::entries, ByteBufCodecs.BOOL, ClientboundRecipeBookAddPacket::replace, ClientboundRecipeBookAddPacket::new);

    @Override
    public PacketType<ClientboundRecipeBookAddPacket> type() {
        return GamePacketTypes.CLIENTBOUND_RECIPE_BOOK_ADD;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRecipeBookAdd(this);
    }

    public record Entry(RecipeDisplayEntry contents, byte flags) {
        public static final byte FLAG_NOTIFICATION = 1;
        public static final byte FLAG_HIGHLIGHT = 2;
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(RecipeDisplayEntry.STREAM_CODEC, Entry::contents, ByteBufCodecs.BYTE, Entry::flags, Entry::new);

        public Entry(RecipeDisplayEntry recipeDisplayEntry, boolean bl, boolean bl2) {
            this(recipeDisplayEntry, (byte)((bl ? 1 : 0) | (bl2 ? 2 : 0)));
        }

        public boolean notification() {
            return (this.flags & 1) != 0;
        }

        public boolean highlight() {
            return (this.flags & 2) != 0;
        }
    }
}

