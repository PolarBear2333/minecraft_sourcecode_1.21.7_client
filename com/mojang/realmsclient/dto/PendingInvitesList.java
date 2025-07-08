/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.dto.ValueObject;
import java.util.List;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

public class PendingInvitesList
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<PendingInvite> pendingInvites = Lists.newArrayList();

    public static PendingInvitesList parse(String string) {
        PendingInvitesList pendingInvitesList = new PendingInvitesList();
        try {
            JsonObject jsonObject = LenientJsonParser.parse(string).getAsJsonObject();
            if (jsonObject.get("invites").isJsonArray()) {
                for (JsonElement jsonElement : jsonObject.get("invites").getAsJsonArray()) {
                    pendingInvitesList.pendingInvites.add(PendingInvite.parse(jsonElement.getAsJsonObject()));
                }
            }
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse PendingInvitesList: {}", (Object)exception.getMessage());
        }
        return pendingInvitesList;
    }
}

