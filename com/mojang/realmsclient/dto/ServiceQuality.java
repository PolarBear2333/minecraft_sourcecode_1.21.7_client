/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public enum ServiceQuality {
    GREAT(1, "icon/ping_5"),
    GOOD(2, "icon/ping_4"),
    OKAY(3, "icon/ping_3"),
    POOR(4, "icon/ping_2"),
    UNKNOWN(5, "icon/ping_unknown");

    final int value;
    private final ResourceLocation icon;

    private ServiceQuality(int n2, String string2) {
        this.value = n2;
        this.icon = ResourceLocation.withDefaultNamespace(string2);
    }

    @Nullable
    public static ServiceQuality byValue(int n) {
        for (ServiceQuality serviceQuality : ServiceQuality.values()) {
            if (serviceQuality.getValue() != n) continue;
            return serviceQuality;
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public ResourceLocation getIcon() {
        return this.icon;
    }

    public static class RealmsServiceQualityJsonAdapter
    extends TypeAdapter<ServiceQuality> {
        private static final Logger LOGGER = LogUtils.getLogger();

        public void write(JsonWriter jsonWriter, ServiceQuality serviceQuality) throws IOException {
            jsonWriter.value((long)serviceQuality.value);
        }

        public ServiceQuality read(JsonReader jsonReader) throws IOException {
            int n = jsonReader.nextInt();
            ServiceQuality serviceQuality = ServiceQuality.byValue(n);
            if (serviceQuality == null) {
                LOGGER.warn("Unsupported ServiceQuality {}", (Object)n);
                return UNKNOWN;
            }
            return serviceQuality;
        }

        public /* synthetic */ Object read(JsonReader jsonReader) throws IOException {
            return this.read(jsonReader);
        }

        public /* synthetic */ void write(JsonWriter jsonWriter, Object object) throws IOException {
            this.write(jsonWriter, (ServiceQuality)((Object)object));
        }
    }
}

