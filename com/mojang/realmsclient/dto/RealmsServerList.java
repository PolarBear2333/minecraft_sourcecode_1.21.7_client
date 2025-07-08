/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.ValueObject;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

public class RealmsServerList
extends ValueObject
implements ReflectionBasedSerialization {
    private static final Logger LOGGER = LogUtils.getLogger();
    @SerializedName(value="servers")
    public List<RealmsServer> servers = new ArrayList<RealmsServer>();

    public static RealmsServerList parse(GuardedSerializer guardedSerializer, String string) {
        try {
            RealmsServerList realmsServerList = guardedSerializer.fromJson(string, RealmsServerList.class);
            if (realmsServerList == null) {
                LOGGER.error("Could not parse McoServerList: {}", (Object)string);
                return new RealmsServerList();
            }
            realmsServerList.servers.forEach(RealmsServer::finalize);
            return realmsServerList;
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse McoServerList: {}", (Object)exception.getMessage());
            return new RealmsServerList();
        }
    }
}

