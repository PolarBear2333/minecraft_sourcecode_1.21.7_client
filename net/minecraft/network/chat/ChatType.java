/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public static final Codec<ChatType> DIRECT_CODEC = RecordCodecBuilder.create((T instance) -> instance.group((App)ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat), (App)ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)).apply((Applicative)instance, ChatType::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatType> DIRECT_STREAM_CODEC = StreamCodec.composite(ChatTypeDecoration.STREAM_CODEC, ChatType::chat, ChatTypeDecoration.STREAM_CODEC, ChatType::narration, ChatType::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ChatType>> STREAM_CODEC = ByteBufCodecs.holder(Registries.CHAT_TYPE, DIRECT_STREAM_CODEC);
    public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatType> CHAT = ChatType.create("chat");
    public static final ResourceKey<ChatType> SAY_COMMAND = ChatType.create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = ChatType.create("msg_command_incoming");
    public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = ChatType.create("msg_command_outgoing");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = ChatType.create("team_msg_command_incoming");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = ChatType.create("team_msg_command_outgoing");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = ChatType.create("emote_command");

    private static ResourceKey<ChatType> create(String string) {
        return ResourceKey.create(Registries.CHAT_TYPE, ResourceLocation.withDefaultNamespace(string));
    }

    public static void bootstrap(BootstrapContext<ChatType> bootstrapContext) {
        bootstrapContext.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstrapContext.register(SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstrapContext.register(MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstrapContext.register(MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstrapContext.register(TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstrapContext.register(TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
        bootstrapContext.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
    }

    public static Bound bind(ResourceKey<ChatType> resourceKey, Entity entity) {
        return ChatType.bind(resourceKey, entity.level().registryAccess(), entity.getDisplayName());
    }

    public static Bound bind(ResourceKey<ChatType> resourceKey, CommandSourceStack commandSourceStack) {
        return ChatType.bind(resourceKey, commandSourceStack.registryAccess(), commandSourceStack.getDisplayName());
    }

    public static Bound bind(ResourceKey<ChatType> resourceKey, RegistryAccess registryAccess, Component component) {
        HolderLookup.RegistryLookup registryLookup = registryAccess.lookupOrThrow(Registries.CHAT_TYPE);
        return new Bound(registryLookup.getOrThrow(resourceKey), component);
    }

    public record Bound(Holder<ChatType> chatType, Component name, Optional<Component> targetName) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Bound> STREAM_CODEC = StreamCodec.composite(STREAM_CODEC, Bound::chatType, ComponentSerialization.TRUSTED_STREAM_CODEC, Bound::name, ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC, Bound::targetName, Bound::new);

        Bound(Holder<ChatType> holder, Component component) {
            this(holder, component, Optional.empty());
        }

        public Component decorate(Component component) {
            return this.chatType.value().chat().decorate(component, this);
        }

        public Component decorateNarration(Component component) {
            return this.chatType.value().narration().decorate(component, this);
        }

        public Bound withTargetName(Component component) {
            return new Bound(this.chatType, this.name, Optional.of(component));
        }
    }
}

