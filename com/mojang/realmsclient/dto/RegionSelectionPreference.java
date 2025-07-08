/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import org.slf4j.Logger;

public enum RegionSelectionPreference {
    AUTOMATIC_PLAYER(0, "realms.configuration.region_preference.automatic_player"),
    AUTOMATIC_OWNER(1, "realms.configuration.region_preference.automatic_owner"),
    MANUAL(2, "");

    public static final RegionSelectionPreference DEFAULT_SELECTION;
    public final int id;
    public final String translationKey;

    private RegionSelectionPreference(int n2, String string2) {
        this.id = n2;
        this.translationKey = string2;
    }

    static {
        DEFAULT_SELECTION = AUTOMATIC_PLAYER;
    }

    public static class RegionSelectionPreferenceJsonAdapter
    extends TypeAdapter<RegionSelectionPreference> {
        private static final Logger LOGGER = LogUtils.getLogger();

        public void write(JsonWriter jsonWriter, RegionSelectionPreference regionSelectionPreference) throws IOException {
            jsonWriter.value((long)regionSelectionPreference.id);
        }

        public RegionSelectionPreference read(JsonReader jsonReader) throws IOException {
            int n = jsonReader.nextInt();
            for (RegionSelectionPreference regionSelectionPreference : RegionSelectionPreference.values()) {
                if (regionSelectionPreference.id != n) continue;
                return regionSelectionPreference;
            }
            LOGGER.warn("Unsupported RegionSelectionPreference {}", (Object)n);
            return DEFAULT_SELECTION;
        }

        public /* synthetic */ Object read(JsonReader jsonReader) throws IOException {
            return this.read(jsonReader);
        }

        public /* synthetic */ void write(JsonWriter jsonWriter, Object object) throws IOException {
            this.write(jsonWriter, (RegionSelectionPreference)((Object)object));
        }
    }
}

