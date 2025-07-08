/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.player;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Input(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
    private static final byte FLAG_FORWARD = 1;
    private static final byte FLAG_BACKWARD = 2;
    private static final byte FLAG_LEFT = 4;
    private static final byte FLAG_RIGHT = 8;
    private static final byte FLAG_JUMP = 16;
    private static final byte FLAG_SHIFT = 32;
    private static final byte FLAG_SPRINT = 64;
    public static final StreamCodec<FriendlyByteBuf, Input> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, Input>(){

        @Override
        public void encode(FriendlyByteBuf friendlyByteBuf, Input input) {
            byte by = 0;
            by = (byte)(by | (input.forward() ? 1 : 0));
            by = (byte)(by | (input.backward() ? 2 : 0));
            by = (byte)(by | (input.left() ? 4 : 0));
            by = (byte)(by | (input.right() ? 8 : 0));
            by = (byte)(by | (input.jump() ? 16 : 0));
            by = (byte)(by | (input.shift() ? 32 : 0));
            by = (byte)(by | (input.sprint() ? 64 : 0));
            friendlyByteBuf.writeByte(by);
        }

        @Override
        public Input decode(FriendlyByteBuf friendlyByteBuf) {
            byte by = friendlyByteBuf.readByte();
            boolean bl = (by & 1) != 0;
            boolean bl2 = (by & 2) != 0;
            boolean bl3 = (by & 4) != 0;
            boolean bl4 = (by & 8) != 0;
            boolean bl5 = (by & 0x10) != 0;
            boolean bl6 = (by & 0x20) != 0;
            boolean bl7 = (by & 0x40) != 0;
            return new Input(bl, bl2, bl3, bl4, bl5, bl6, bl7);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((FriendlyByteBuf)((Object)object), (Input)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((FriendlyByteBuf)((Object)object));
        }
    };
    public static Input EMPTY = new Input(false, false, false, false, false, false, false);
}

