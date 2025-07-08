/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.ValueObject;
import java.util.List;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

public class BackupList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<Backup> backups;

    public static BackupList parse(String string) {
        BackupList backupList = new BackupList();
        backupList.backups = Lists.newArrayList();
        try {
            JsonElement jsonElement = LenientJsonParser.parse(string).getAsJsonObject().get("backups");
            if (jsonElement.isJsonArray()) {
                for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
                    backupList.backups.add(Backup.parse(jsonElement2));
                }
            }
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse BackupList: {}", (Object)exception.getMessage());
        }
        return backupList;
    }
}

