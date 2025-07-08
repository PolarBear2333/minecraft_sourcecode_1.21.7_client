/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.util.concurrent.GenericFutureListener
 */
package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.List;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.LegacyProtocolUtils;
import net.minecraft.util.Mth;

public class LegacyServerPinger
extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Splitter SPLITTER = Splitter.on((char)'\u0000').limit(6);
    private final ServerAddress address;
    private final Output output;

    public LegacyServerPinger(ServerAddress serverAddress, Output output) {
        this.address = serverAddress;
        this.output = output;
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelActive(channelHandlerContext);
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        try {
            byteBuf.writeByte(254);
            byteBuf.writeByte(1);
            byteBuf.writeByte(250);
            LegacyProtocolUtils.writeLegacyString(byteBuf, "MC|PingHost");
            int n = byteBuf.writerIndex();
            byteBuf.writeShort(0);
            int n2 = byteBuf.writerIndex();
            byteBuf.writeByte(127);
            LegacyProtocolUtils.writeLegacyString(byteBuf, this.address.getHost());
            byteBuf.writeInt(this.address.getPort());
            int n3 = byteBuf.writerIndex() - n2;
            byteBuf.setShort(n, n3);
            channelHandlerContext.channel().writeAndFlush((Object)byteBuf).addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
        }
        catch (Exception exception) {
            byteBuf.release();
            throw exception;
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        String string;
        List list;
        short s = byteBuf.readUnsignedByte();
        if (s == 255 && "\u00a71".equals((list = SPLITTER.splitToList((CharSequence)(string = LegacyProtocolUtils.readLegacyString(byteBuf)))).get(0))) {
            int n = Mth.getInt((String)list.get(1), 0);
            String string2 = (String)list.get(2);
            String string3 = (String)list.get(3);
            int n2 = Mth.getInt((String)list.get(4), -1);
            int n3 = Mth.getInt((String)list.get(5), -1);
            this.output.handleResponse(n, string2, string3, n2, n3);
        }
        channelHandlerContext.close();
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        channelHandlerContext.close();
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        this.channelRead0(channelHandlerContext, (ByteBuf)object);
    }

    @FunctionalInterface
    public static interface Output {
        public void handleResponse(int var1, String var2, String var3, int var4, int var5);
    }
}

