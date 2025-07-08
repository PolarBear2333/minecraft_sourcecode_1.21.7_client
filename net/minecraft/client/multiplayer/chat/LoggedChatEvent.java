/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.util.StringRepresentable;

public interface LoggedChatEvent {
    public static final Codec<LoggedChatEvent> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(LoggedChatEvent::type, Type::codec);

    public Type type();

    public static enum Type implements StringRepresentable
    {
        PLAYER("player", () -> LoggedChatMessage.Player.CODEC),
        SYSTEM("system", () -> LoggedChatMessage.System.CODEC);

        private final String serializedName;
        private final Supplier<MapCodec<? extends LoggedChatEvent>> codec;

        private Type(String string2, Supplier<MapCodec<? extends LoggedChatEvent>> supplier) {
            this.serializedName = string2;
            this.codec = supplier;
        }

        private MapCodec<? extends LoggedChatEvent> codec() {
            return this.codec.get();
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}

