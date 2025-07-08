/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class DetectedVersion {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldVersion BUILT_IN = DetectedVersion.createFromConstants();

    private static WorldVersion createFromConstants() {
        return new WorldVersion.Simple(UUID.randomUUID().toString().replaceAll("-", ""), "1.21.7", new DataVersion(4438, "main"), SharedConstants.getProtocolVersion(), 64, 81, new Date(), true);
    }

    private static WorldVersion createFromJson(JsonObject jsonObject) {
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "pack_version");
        return new WorldVersion.Simple(GsonHelper.getAsString(jsonObject, "id"), GsonHelper.getAsString(jsonObject, "name"), new DataVersion(GsonHelper.getAsInt(jsonObject, "world_version"), GsonHelper.getAsString(jsonObject, "series_id", "main")), GsonHelper.getAsInt(jsonObject, "protocol_version"), GsonHelper.getAsInt(jsonObject2, "resource"), GsonHelper.getAsInt(jsonObject2, "data"), Date.from(ZonedDateTime.parse(GsonHelper.getAsString(jsonObject, "build_time")).toInstant()), GsonHelper.getAsBoolean(jsonObject, "stable"));
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static WorldVersion tryDetectVersion() {
        try (InputStream inputStream = DetectedVersion.class.getResourceAsStream("/version.json");){
            WorldVersion worldVersion;
            if (inputStream == null) {
                LOGGER.warn("Missing version information!");
                WorldVersion worldVersion2 = BUILT_IN;
                return worldVersion2;
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);){
                worldVersion = DetectedVersion.createFromJson(GsonHelper.parse(inputStreamReader));
            }
            return worldVersion;
        }
        catch (JsonParseException | IOException throwable) {
            throw new IllegalStateException("Game version information is corrupt", throwable);
        }
    }
}

