/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.PngInfo;
import org.slf4j.Logger;

public class ServerData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ICON_SIZE = 1024;
    public String name;
    public String ip;
    public Component status;
    public Component motd;
    @Nullable
    public ServerStatus.Players players;
    public long ping;
    public int protocol = SharedConstants.getCurrentVersion().protocolVersion();
    public Component version = Component.literal(SharedConstants.getCurrentVersion().name());
    public List<Component> playerList = Collections.emptyList();
    private ServerPackStatus packStatus = ServerPackStatus.PROMPT;
    @Nullable
    private byte[] iconBytes;
    private Type type;
    private State state = State.INITIAL;

    public ServerData(String string, String string2, Type type) {
        this.name = string;
        this.ip = string2;
        this.type = type;
    }

    public CompoundTag write() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", this.name);
        compoundTag.putString("ip", this.ip);
        compoundTag.storeNullable("icon", ExtraCodecs.BASE64_STRING, this.iconBytes);
        compoundTag.store(ServerPackStatus.FIELD_CODEC, this.packStatus);
        return compoundTag;
    }

    public ServerPackStatus getResourcePackStatus() {
        return this.packStatus;
    }

    public void setResourcePackStatus(ServerPackStatus serverPackStatus) {
        this.packStatus = serverPackStatus;
    }

    public static ServerData read(CompoundTag compoundTag) {
        ServerData serverData = new ServerData(compoundTag.getStringOr("name", ""), compoundTag.getStringOr("ip", ""), Type.OTHER);
        serverData.setIconBytes(compoundTag.read("icon", ExtraCodecs.BASE64_STRING).orElse(null));
        serverData.setResourcePackStatus(compoundTag.read(ServerPackStatus.FIELD_CODEC).orElse(ServerPackStatus.PROMPT));
        return serverData;
    }

    @Nullable
    public byte[] getIconBytes() {
        return this.iconBytes;
    }

    public void setIconBytes(@Nullable byte[] byArray) {
        this.iconBytes = byArray;
    }

    public boolean isLan() {
        return this.type == Type.LAN;
    }

    public boolean isRealm() {
        return this.type == Type.REALM;
    }

    public Type type() {
        return this.type;
    }

    public void copyNameIconFrom(ServerData serverData) {
        this.ip = serverData.ip;
        this.name = serverData.name;
        this.iconBytes = serverData.iconBytes;
    }

    public void copyFrom(ServerData serverData) {
        this.copyNameIconFrom(serverData);
        this.setResourcePackStatus(serverData.getResourcePackStatus());
        this.type = serverData.type;
    }

    public State state() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Nullable
    public static byte[] validateIcon(@Nullable byte[] byArray) {
        if (byArray != null) {
            try {
                PngInfo pngInfo = PngInfo.fromBytes(byArray);
                if (pngInfo.width() <= 1024 && pngInfo.height() <= 1024) {
                    return byArray;
                }
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to decode server icon", (Throwable)iOException);
            }
        }
        return null;
    }

    public static enum ServerPackStatus {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        public static final MapCodec<ServerPackStatus> FIELD_CODEC;
        private final Component name;

        private ServerPackStatus(String string2) {
            this.name = Component.translatable("addServer.resourcePack." + string2);
        }

        public Component getName() {
            return this.name;
        }

        static {
            FIELD_CODEC = Codec.BOOL.optionalFieldOf("acceptTextures").xmap(optional -> optional.map(bl -> bl != false ? ENABLED : DISABLED).orElse(PROMPT), serverPackStatus -> switch (serverPackStatus.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Optional.of(true);
                case 1 -> Optional.of(false);
                case 2 -> Optional.empty();
            });
        }
    }

    public static enum State {
        INITIAL,
        PINGING,
        UNREACHABLE,
        INCOMPATIBLE,
        SUCCESSFUL;

    }

    public static enum Type {
        LAN,
        REALM,
        OTHER;

    }
}

