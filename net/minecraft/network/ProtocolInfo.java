/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.VisibleForDebug;

public interface ProtocolInfo<T extends PacketListener> {
    public ConnectionProtocol id();

    public PacketFlow flow();

    public StreamCodec<ByteBuf, Packet<? super T>> codec();

    @Nullable
    public BundlerInfo bundlerInfo();

    public static interface DetailsProvider {
        public Details details();
    }

    public static interface Details {
        public ConnectionProtocol id();

        public PacketFlow flow();

        @VisibleForDebug
        public void listPackets(PacketVisitor var1);

        @FunctionalInterface
        public static interface PacketVisitor {
            public void accept(PacketType<?> var1, int var2);
        }
    }
}

