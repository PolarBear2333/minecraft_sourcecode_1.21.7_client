/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.protocol.status;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record ServerStatus(Component description, Optional<Players> players, Optional<Version> version, Optional<Favicon> favicon, boolean enforcesSecureChat) {
    public static final Codec<ServerStatus> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ComponentSerialization.CODEC.lenientOptionalFieldOf("description", (Object)CommonComponents.EMPTY).forGetter(ServerStatus::description), (App)Players.CODEC.lenientOptionalFieldOf("players").forGetter(ServerStatus::players), (App)Version.CODEC.lenientOptionalFieldOf("version").forGetter(ServerStatus::version), (App)Favicon.CODEC.lenientOptionalFieldOf("favicon").forGetter(ServerStatus::favicon), (App)Codec.BOOL.lenientOptionalFieldOf("enforcesSecureChat", (Object)false).forGetter(ServerStatus::enforcesSecureChat)).apply((Applicative)instance, ServerStatus::new));

    public record Players(int max, int online, List<GameProfile> sample) {
        private static final Codec<GameProfile> PROFILE_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(GameProfile::getId), (App)Codec.STRING.fieldOf("name").forGetter(GameProfile::getName)).apply((Applicative)instance, GameProfile::new));
        public static final Codec<Players> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("max").forGetter(Players::max), (App)Codec.INT.fieldOf("online").forGetter(Players::online), (App)PROFILE_CODEC.listOf().lenientOptionalFieldOf("sample", List.of()).forGetter(Players::sample)).apply((Applicative)instance, Players::new));
    }

    public record Version(String name, int protocol) {
        public static final Codec<Version> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(Version::name), (App)Codec.INT.fieldOf("protocol").forGetter(Version::protocol)).apply((Applicative)instance, Version::new));

        public static Version current() {
            WorldVersion worldVersion = SharedConstants.getCurrentVersion();
            return new Version(worldVersion.name(), worldVersion.protocolVersion());
        }
    }

    public record Favicon(byte[] iconBytes) {
        private static final String PREFIX = "data:image/png;base64,";
        public static final Codec<Favicon> CODEC = Codec.STRING.comapFlatMap(string -> {
            if (!string.startsWith(PREFIX)) {
                return DataResult.error(() -> "Unknown format");
            }
            try {
                String string2 = string.substring(PREFIX.length()).replaceAll("\n", "");
                byte[] byArray = Base64.getDecoder().decode(string2.getBytes(StandardCharsets.UTF_8));
                return DataResult.success((Object)new Favicon(byArray));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return DataResult.error(() -> "Malformed base64 server icon");
            }
        }, favicon -> PREFIX + new String(Base64.getEncoder().encode(favicon.iconBytes), StandardCharsets.UTF_8));
    }
}

