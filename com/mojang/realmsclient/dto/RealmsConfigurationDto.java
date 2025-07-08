/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsSetting;
import com.mojang.realmsclient.dto.RealmsSlotUpdateDto;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.RegionSelectionPreferenceDto;
import java.util.List;
import javax.annotation.Nullable;

public record RealmsConfigurationDto(@SerializedName(value="options") RealmsSlotUpdateDto options, @SerializedName(value="settings") List<RealmsSetting> settings, @Nullable @SerializedName(value="regionSelectionPreference") RegionSelectionPreferenceDto regionSelectionPreference, @Nullable @SerializedName(value="description") RealmsDescriptionDto description) implements ReflectionBasedSerialization
{
}

