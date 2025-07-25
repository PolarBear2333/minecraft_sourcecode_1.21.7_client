/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.BanListEntry;

public class UserBanListEntry
extends BanListEntry<GameProfile> {
    public UserBanListEntry(@Nullable GameProfile gameProfile) {
        this(gameProfile, (Date)null, (String)null, (Date)null, (String)null);
    }

    public UserBanListEntry(@Nullable GameProfile gameProfile, @Nullable Date date, @Nullable String string, @Nullable Date date2, @Nullable String string2) {
        super(gameProfile, date, string, date2, string2);
    }

    public UserBanListEntry(JsonObject jsonObject) {
        super(UserBanListEntry.createGameProfile(jsonObject), jsonObject);
    }

    @Override
    protected void serialize(JsonObject jsonObject) {
        if (this.getUser() == null) {
            return;
        }
        jsonObject.addProperty("uuid", ((GameProfile)this.getUser()).getId().toString());
        jsonObject.addProperty("name", ((GameProfile)this.getUser()).getName());
        super.serialize(jsonObject);
    }

    @Override
    public Component getDisplayName() {
        GameProfile gameProfile = (GameProfile)this.getUser();
        return gameProfile != null ? Component.literal(gameProfile.getName()) : Component.translatable("commands.banlist.entry.unknown");
    }

    @Nullable
    private static GameProfile createGameProfile(JsonObject jsonObject) {
        UUID uUID;
        if (!jsonObject.has("uuid") || !jsonObject.has("name")) {
            return null;
        }
        String string = jsonObject.get("uuid").getAsString();
        try {
            uUID = UUID.fromString(string);
        }
        catch (Throwable throwable) {
            return null;
        }
        return new GameProfile(uUID, jsonObject.get("name").getAsString());
    }
}

