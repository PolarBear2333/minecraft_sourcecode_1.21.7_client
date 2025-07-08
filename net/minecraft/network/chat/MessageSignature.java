/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
    public static final Codec<MessageSignature> CODEC = ExtraCodecs.BASE64_STRING.xmap(MessageSignature::new, MessageSignature::bytes);
    public static final int BYTES = 256;

    public MessageSignature {
        Preconditions.checkState((byArray.length == 256 ? 1 : 0) != 0, (Object)"Invalid message signature size");
    }

    public static MessageSignature read(FriendlyByteBuf friendlyByteBuf) {
        byte[] byArray = new byte[256];
        friendlyByteBuf.readBytes(byArray);
        return new MessageSignature(byArray);
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, MessageSignature messageSignature) {
        friendlyByteBuf.writeBytes(messageSignature.bytes);
    }

    public boolean verify(SignatureValidator signatureValidator, SignatureUpdater signatureUpdater) {
        return signatureValidator.validate(signatureUpdater, this.bytes);
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(this.bytes);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MessageSignature)) return false;
        MessageSignature messageSignature = (MessageSignature)object;
        if (!Arrays.equals(this.bytes, messageSignature.bytes)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.bytes);
    }

    public static String describe(@Nullable MessageSignature messageSignature) {
        if (messageSignature == null) {
            return "<no signature>";
        }
        return messageSignature.toString();
    }

    public Packed pack(MessageSignatureCache messageSignatureCache) {
        int n = messageSignatureCache.pack(this);
        return n != -1 ? new Packed(n) : new Packed(this);
    }

    public int checksum() {
        return Arrays.hashCode(this.bytes);
    }

    public record Packed(int id, @Nullable MessageSignature fullSignature) {
        public static final int FULL_SIGNATURE = -1;

        public Packed(MessageSignature messageSignature) {
            this(-1, messageSignature);
        }

        public Packed(int n) {
            this(n, null);
        }

        public static Packed read(FriendlyByteBuf friendlyByteBuf) {
            int n = friendlyByteBuf.readVarInt() - 1;
            if (n == -1) {
                return new Packed(MessageSignature.read(friendlyByteBuf));
            }
            return new Packed(n);
        }

        public static void write(FriendlyByteBuf friendlyByteBuf, Packed packed) {
            friendlyByteBuf.writeVarInt(packed.id() + 1);
            if (packed.fullSignature() != null) {
                MessageSignature.write(friendlyByteBuf, packed.fullSignature());
            }
        }

        public Optional<MessageSignature> unpack(MessageSignatureCache messageSignatureCache) {
            if (this.fullSignature != null) {
                return Optional.of(this.fullSignature);
            }
            return Optional.ofNullable(messageSignatureCache.unpack(this.id));
        }
    }
}

