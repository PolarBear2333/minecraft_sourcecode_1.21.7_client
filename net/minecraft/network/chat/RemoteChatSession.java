/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
    public SignedMessageValidator createMessageValidator(Duration duration) {
        return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator(), () -> this.profilePublicKey.data().hasExpired(duration));
    }

    public SignedMessageChain.Decoder createMessageDecoder(UUID uUID) {
        return new SignedMessageChain(uUID, this.sessionId).decoder(this.profilePublicKey);
    }

    public Data asData() {
        return new Data(this.sessionId, this.profilePublicKey.data());
    }

    public boolean hasExpired() {
        return this.profilePublicKey.data().hasExpired();
    }

    public record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
        public static Data read(FriendlyByteBuf friendlyByteBuf) {
            return new Data(friendlyByteBuf.readUUID(), new ProfilePublicKey.Data(friendlyByteBuf));
        }

        public static void write(FriendlyByteBuf friendlyByteBuf, Data data) {
            friendlyByteBuf.writeUUID(data.sessionId);
            data.profilePublicKey.write(friendlyByteBuf);
        }

        public RemoteChatSession validate(GameProfile gameProfile, SignatureValidator signatureValidator) throws ProfilePublicKey.ValidationException {
            return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(signatureValidator, gameProfile.getId(), this.profilePublicKey));
        }
    }
}

