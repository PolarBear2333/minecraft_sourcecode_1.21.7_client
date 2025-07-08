/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

public class RealmsNews
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    public String newsLink;

    public static RealmsNews parse(String string) {
        RealmsNews realmsNews = new RealmsNews();
        try {
            JsonObject jsonObject = LenientJsonParser.parse(string).getAsJsonObject();
            realmsNews.newsLink = JsonUtils.getStringOr("newsLink", jsonObject, null);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RealmsNews: {}", (Object)exception.getMessage());
        }
        return realmsNews;
    }
}

