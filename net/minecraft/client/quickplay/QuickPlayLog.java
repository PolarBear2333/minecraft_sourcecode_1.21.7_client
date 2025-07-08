/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.quickplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;

public class QuickPlayLog {
    private static final QuickPlayLog INACTIVE = new QuickPlayLog(""){

        @Override
        public void log(Minecraft minecraft) {
        }

        @Override
        public void setWorldData(Type type, String string, String string2) {
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private final Path path;
    @Nullable
    private QuickPlayWorld worldData;

    QuickPlayLog(String string) {
        this.path = Minecraft.getInstance().gameDirectory.toPath().resolve(string);
    }

    public static QuickPlayLog of(@Nullable String string) {
        if (string == null) {
            return INACTIVE;
        }
        return new QuickPlayLog(string);
    }

    public void setWorldData(Type type, String string, String string2) {
        this.worldData = new QuickPlayWorld(type, string, string2);
    }

    public void log(Minecraft minecraft) {
        if (minecraft.gameMode == null || this.worldData == null) {
            LOGGER.error("Failed to log session for quickplay. Missing world data or gamemode");
            return;
        }
        Util.ioPool().execute(() -> {
            try {
                Files.deleteIfExists(this.path);
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to delete quickplay log file {}", (Object)this.path, (Object)iOException);
            }
            QuickPlayEntry quickPlayEntry = new QuickPlayEntry(this.worldData, Instant.now(), minecraft.gameMode.getPlayerMode());
            Codec.list(QuickPlayEntry.CODEC).encodeStart((DynamicOps)JsonOps.INSTANCE, List.of(quickPlayEntry)).resultOrPartial(Util.prefix("Quick Play: ", arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(jsonElement -> {
                try {
                    Files.createDirectories(this.path.getParent(), new FileAttribute[0]);
                    Files.writeString(this.path, (CharSequence)GSON.toJson(jsonElement), new OpenOption[0]);
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to write to quickplay log file {}", (Object)this.path, (Object)iOException);
                }
            });
        });
    }

    record QuickPlayWorld(Type type, String id, String name) {
        public static final MapCodec<QuickPlayWorld> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Type.CODEC.fieldOf("type").forGetter(QuickPlayWorld::type), (App)ExtraCodecs.ESCAPED_STRING.fieldOf("id").forGetter(QuickPlayWorld::id), (App)Codec.STRING.fieldOf("name").forGetter(QuickPlayWorld::name)).apply((Applicative)instance, QuickPlayWorld::new));
    }

    public static enum Type implements StringRepresentable
    {
        SINGLEPLAYER("singleplayer"),
        MULTIPLAYER("multiplayer"),
        REALMS("realms");

        static final Codec<Type> CODEC;
        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values);
        }
    }

    record QuickPlayEntry(QuickPlayWorld quickPlayWorld, Instant lastPlayedTime, GameType gamemode) {
        public static final Codec<QuickPlayEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)QuickPlayWorld.MAP_CODEC.forGetter(QuickPlayEntry::quickPlayWorld), (App)ExtraCodecs.INSTANT_ISO8601.fieldOf("lastPlayedTime").forGetter(QuickPlayEntry::lastPlayedTime), (App)GameType.CODEC.fieldOf("gamemode").forGetter(QuickPlayEntry::gamemode)).apply((Applicative)instance, QuickPlayEntry::new));
    }
}

