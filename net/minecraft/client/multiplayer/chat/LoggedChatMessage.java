/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.ExtraCodecs;

public interface LoggedChatMessage
extends LoggedChatEvent {
    public static Player player(GameProfile gameProfile, PlayerChatMessage playerChatMessage, ChatTrustLevel chatTrustLevel) {
        return new Player(gameProfile, playerChatMessage, chatTrustLevel);
    }

    public static System system(Component component, Instant instant) {
        return new System(component, instant);
    }

    public Component toContentComponent();

    default public Component toNarrationComponent() {
        return this.toContentComponent();
    }

    public boolean canReport(UUID var1);

    public record Player(GameProfile profile, PlayerChatMessage message, ChatTrustLevel trustLevel) implements LoggedChatMessage
    {
        public static final MapCodec<Player> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.GAME_PROFILE.fieldOf("profile").forGetter(Player::profile), (App)PlayerChatMessage.MAP_CODEC.forGetter(Player::message), (App)ChatTrustLevel.CODEC.optionalFieldOf("trust_level", (Object)ChatTrustLevel.SECURE).forGetter(Player::trustLevel)).apply((Applicative)instance, Player::new));
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        @Override
        public Component toContentComponent() {
            if (!this.message.filterMask().isEmpty()) {
                Component component = this.message.filterMask().applyWithFormatting(this.message.signedContent());
                return component != null ? component : Component.empty();
            }
            return this.message.decoratedContent();
        }

        @Override
        public Component toNarrationComponent() {
            Component component = this.toContentComponent();
            Component component2 = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.message.narrate", this.profile.getName(), component, component2);
        }

        public Component toHeadingComponent() {
            Component component = this.getTimeComponent();
            return Component.translatable("gui.chatSelection.heading", this.profile.getName(), component);
        }

        private Component getTimeComponent() {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(this.message.timeStamp(), ZoneOffset.systemDefault());
            return Component.literal(localDateTime.format(TIME_FORMATTER)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
        }

        @Override
        public boolean canReport(UUID uUID) {
            return this.message.hasSignatureFrom(uUID);
        }

        public UUID profileId() {
            return this.profile.getId();
        }

        @Override
        public LoggedChatEvent.Type type() {
            return LoggedChatEvent.Type.PLAYER;
        }
    }

    public record System(Component message, Instant timeStamp) implements LoggedChatMessage
    {
        public static final MapCodec<System> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("message").forGetter(System::message), (App)ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(System::timeStamp)).apply((Applicative)instance, System::new));

        @Override
        public Component toContentComponent() {
            return this.message;
        }

        @Override
        public boolean canReport(UUID uUID) {
            return false;
        }

        @Override
        public LoggedChatEvent.Type type() {
            return LoggedChatEvent.Type.SYSTEM;
        }
    }
}

