/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 */
package net.minecraft.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketDecoderException
extends DecoderException
implements SkipPacketException,
IdDispatchCodec.DontDecorateException {
    public SkipPacketDecoderException(String string) {
        super(string);
    }

    public SkipPacketDecoderException(Throwable throwable) {
        super(throwable);
    }
}

