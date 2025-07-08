/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.util.UndashedUuid
 *  javax.annotation.Nullable
 */
package net.minecraft.client;

import com.mojang.util.UndashedUuid;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class User {
    private final String name;
    private final UUID uuid;
    private final String accessToken;
    private final Optional<String> xuid;
    private final Optional<String> clientId;
    private final Type type;

    public User(String string, UUID uUID, String string2, Optional<String> optional, Optional<String> optional2, Type type) {
        this.name = string;
        this.uuid = uUID;
        this.accessToken = string2;
        this.xuid = optional;
        this.clientId = optional2;
        this.type = type;
    }

    public String getSessionId() {
        return "token:" + this.accessToken + ":" + UndashedUuid.toString((UUID)this.uuid);
    }

    public UUID getProfileId() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Optional<String> getClientId() {
        return this.clientId;
    }

    public Optional<String> getXuid() {
        return this.xuid;
    }

    public Type getType() {
        return this.type;
    }

    public static enum Type {
        LEGACY("legacy"),
        MOJANG("mojang"),
        MSA("msa");

        private static final Map<String, Type> BY_NAME;
        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        @Nullable
        public static Type byName(String string) {
            return BY_NAME.get(string.toLowerCase(Locale.ROOT));
        }

        public String getName() {
            return this.name;
        }

        static {
            BY_NAME = Arrays.stream(Type.values()).collect(Collectors.toMap(type -> type.name, Function.identity()));
        }
    }
}

