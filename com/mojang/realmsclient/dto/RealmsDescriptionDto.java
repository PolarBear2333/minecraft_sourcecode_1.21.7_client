/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.ValueObject;
import javax.annotation.Nullable;

public class RealmsDescriptionDto
extends ValueObject
implements ReflectionBasedSerialization {
    @SerializedName(value="name")
    @Nullable
    public String name;
    @SerializedName(value="description")
    public String description;

    public RealmsDescriptionDto(@Nullable String string, String string2) {
        this.name = string;
        this.description = string2;
    }
}

