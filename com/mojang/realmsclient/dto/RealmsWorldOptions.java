/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.ValueObject;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;

public class RealmsWorldOptions
extends ValueObject
implements ReflectionBasedSerialization {
    @SerializedName(value="pvp")
    public boolean pvp = true;
    @SerializedName(value="spawnMonsters")
    public boolean spawnMonsters = true;
    @SerializedName(value="spawnProtection")
    public int spawnProtection = 0;
    @SerializedName(value="commandBlocks")
    public boolean commandBlocks = false;
    @SerializedName(value="forceGameMode")
    public boolean forceGameMode = false;
    @SerializedName(value="difficulty")
    public int difficulty = 2;
    @SerializedName(value="gameMode")
    public int gameMode = 0;
    @SerializedName(value="slotName")
    private String slotName = "";
    @SerializedName(value="version")
    public String version = "";
    @SerializedName(value="compatibility")
    public RealmsServer.Compatibility compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
    @SerializedName(value="worldTemplateId")
    public long templateId = -1L;
    @Nullable
    @SerializedName(value="worldTemplateImage")
    public String templateImage = null;
    public boolean empty;

    private RealmsWorldOptions() {
    }

    public RealmsWorldOptions(boolean bl, boolean bl2, int n, boolean bl3, int n2, int n3, boolean bl4, String string, String string2, RealmsServer.Compatibility compatibility) {
        this.pvp = bl;
        this.spawnMonsters = bl2;
        this.spawnProtection = n;
        this.commandBlocks = bl3;
        this.difficulty = n2;
        this.gameMode = n3;
        this.forceGameMode = bl4;
        this.slotName = string;
        this.version = string2;
        this.compatibility = compatibility;
    }

    public static RealmsWorldOptions createDefaults() {
        return new RealmsWorldOptions();
    }

    public static RealmsWorldOptions createDefaultsWith(GameType gameType, boolean bl, Difficulty difficulty, boolean bl2, String string, String string2) {
        RealmsWorldOptions realmsWorldOptions = RealmsWorldOptions.createDefaults();
        realmsWorldOptions.commandBlocks = bl;
        realmsWorldOptions.difficulty = difficulty.getId();
        realmsWorldOptions.gameMode = gameType.getId();
        realmsWorldOptions.slotName = string2;
        realmsWorldOptions.version = string;
        return realmsWorldOptions;
    }

    public static RealmsWorldOptions createFromSettings(LevelSettings levelSettings, boolean bl, String string) {
        return RealmsWorldOptions.createDefaultsWith(levelSettings.gameType(), bl, levelSettings.difficulty(), levelSettings.hardcore(), string, levelSettings.levelName());
    }

    public static RealmsWorldOptions createEmptyDefaults() {
        RealmsWorldOptions realmsWorldOptions = RealmsWorldOptions.createDefaults();
        realmsWorldOptions.setEmpty(true);
        return realmsWorldOptions;
    }

    public void setEmpty(boolean bl) {
        this.empty = bl;
    }

    public static RealmsWorldOptions parse(GuardedSerializer guardedSerializer, String string) {
        RealmsWorldOptions realmsWorldOptions = guardedSerializer.fromJson(string, RealmsWorldOptions.class);
        if (realmsWorldOptions == null) {
            return RealmsWorldOptions.createDefaults();
        }
        RealmsWorldOptions.finalize(realmsWorldOptions);
        return realmsWorldOptions;
    }

    private static void finalize(RealmsWorldOptions realmsWorldOptions) {
        if (realmsWorldOptions.slotName == null) {
            realmsWorldOptions.slotName = "";
        }
        if (realmsWorldOptions.version == null) {
            realmsWorldOptions.version = "";
        }
        if (realmsWorldOptions.compatibility == null) {
            realmsWorldOptions.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
        }
    }

    public String getSlotName(int n) {
        if (StringUtil.isBlank(this.slotName)) {
            if (this.empty) {
                return I18n.get("mco.configure.world.slot.empty", new Object[0]);
            }
            return this.getDefaultSlotName(n);
        }
        return this.slotName;
    }

    public String getDefaultSlotName(int n) {
        return I18n.get("mco.configure.world.slot", n);
    }

    public RealmsWorldOptions clone() {
        return new RealmsWorldOptions(this.pvp, this.spawnMonsters, this.spawnProtection, this.commandBlocks, this.difficulty, this.gameMode, this.forceGameMode, this.slotName, this.version, this.compatibility);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }
}

