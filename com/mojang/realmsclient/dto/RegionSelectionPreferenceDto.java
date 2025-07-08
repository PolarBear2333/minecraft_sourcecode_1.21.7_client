/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.JsonAdapter
 *  com.google.gson.annotations.SerializedName
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.RealmsRegion;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.RegionSelectionPreference;
import com.mojang.realmsclient.dto.ValueObject;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class RegionSelectionPreferenceDto
extends ValueObject
implements ReflectionBasedSerialization {
    public static final RegionSelectionPreferenceDto DEFAULT = new RegionSelectionPreferenceDto(RegionSelectionPreference.AUTOMATIC_OWNER, null);
    private static final Logger LOGGER = LogUtils.getLogger();
    @SerializedName(value="regionSelectionPreference")
    @JsonAdapter(value=RegionSelectionPreference.RegionSelectionPreferenceJsonAdapter.class)
    public RegionSelectionPreference regionSelectionPreference;
    @SerializedName(value="preferredRegion")
    @JsonAdapter(value=RealmsRegion.RealmsRegionJsonAdapter.class)
    @Nullable
    public RealmsRegion preferredRegion;

    public RegionSelectionPreferenceDto(RegionSelectionPreference regionSelectionPreference, @Nullable RealmsRegion realmsRegion) {
        this.regionSelectionPreference = regionSelectionPreference;
        this.preferredRegion = realmsRegion;
    }

    private RegionSelectionPreferenceDto() {
    }

    public static RegionSelectionPreferenceDto parse(GuardedSerializer guardedSerializer, String string) {
        try {
            RegionSelectionPreferenceDto regionSelectionPreferenceDto = guardedSerializer.fromJson(string, RegionSelectionPreferenceDto.class);
            if (regionSelectionPreferenceDto == null) {
                LOGGER.error("Could not parse RegionSelectionPreference: {}", (Object)string);
                return new RegionSelectionPreferenceDto();
            }
            return regionSelectionPreferenceDto;
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RegionSelectionPreference: {}", (Object)exception.getMessage());
            return new RegionSelectionPreferenceDto();
        }
    }

    public RegionSelectionPreferenceDto clone() {
        return new RegionSelectionPreferenceDto(this.regionSelectionPreference, this.preferredRegion);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }
}

