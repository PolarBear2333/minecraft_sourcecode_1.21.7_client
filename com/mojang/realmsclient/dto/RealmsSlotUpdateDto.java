/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import javax.annotation.Nullable;

public final class RealmsSlotUpdateDto
implements ReflectionBasedSerialization {
    @SerializedName(value="slotId")
    public final int slotId;
    @SerializedName(value="pvp")
    private final boolean pvp;
    @SerializedName(value="spawnMonsters")
    private final boolean spawnMonsters;
    @SerializedName(value="spawnProtection")
    private final int spawnProtection;
    @SerializedName(value="commandBlocks")
    private final boolean commandBlocks;
    @SerializedName(value="forceGameMode")
    private final boolean forceGameMode;
    @SerializedName(value="difficulty")
    private final int difficulty;
    @SerializedName(value="gameMode")
    private final int gameMode;
    @SerializedName(value="slotName")
    private final String slotName;
    @SerializedName(value="version")
    private final String version;
    @SerializedName(value="compatibility")
    private final RealmsServer.Compatibility compatibility;
    @SerializedName(value="worldTemplateId")
    private final long templateId;
    @Nullable
    @SerializedName(value="worldTemplateImage")
    private final String templateImage;
    @SerializedName(value="hardcore")
    private final boolean hardcore;

    public RealmsSlotUpdateDto(int n, RealmsWorldOptions realmsWorldOptions, boolean bl) {
        this.slotId = n;
        this.pvp = realmsWorldOptions.pvp;
        this.spawnMonsters = realmsWorldOptions.spawnMonsters;
        this.spawnProtection = realmsWorldOptions.spawnProtection;
        this.commandBlocks = realmsWorldOptions.commandBlocks;
        this.forceGameMode = realmsWorldOptions.forceGameMode;
        this.difficulty = realmsWorldOptions.difficulty;
        this.gameMode = realmsWorldOptions.gameMode;
        this.slotName = realmsWorldOptions.getSlotName(n);
        this.version = realmsWorldOptions.version;
        this.compatibility = realmsWorldOptions.compatibility;
        this.templateId = realmsWorldOptions.templateId;
        this.templateImage = realmsWorldOptions.templateImage;
        this.hardcore = bl;
    }
}

