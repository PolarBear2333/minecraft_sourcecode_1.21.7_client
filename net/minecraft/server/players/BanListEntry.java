/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  javax.annotation.Nullable
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.StoredUserEntry;

public abstract class BanListEntry<T>
extends StoredUserEntry<T> {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    public static final String EXPIRES_NEVER = "forever";
    protected final Date created;
    protected final String source;
    @Nullable
    protected final Date expires;
    protected final String reason;

    public BanListEntry(@Nullable T t, @Nullable Date date, @Nullable String string, @Nullable Date date2, @Nullable String string2) {
        super(t);
        this.created = date == null ? new Date() : date;
        this.source = string == null ? "(Unknown)" : string;
        this.expires = date2;
        this.reason = string2 == null ? "Banned by an operator." : string2;
    }

    protected BanListEntry(@Nullable T t, JsonObject jsonObject) {
        super(t);
        Date date;
        Date date2;
        try {
            date2 = jsonObject.has("created") ? DATE_FORMAT.parse(jsonObject.get("created").getAsString()) : new Date();
        }
        catch (ParseException parseException) {
            date2 = new Date();
        }
        this.created = date2;
        this.source = jsonObject.has("source") ? jsonObject.get("source").getAsString() : "(Unknown)";
        try {
            date = jsonObject.has("expires") ? DATE_FORMAT.parse(jsonObject.get("expires").getAsString()) : null;
        }
        catch (ParseException parseException) {
            date = null;
        }
        this.expires = date;
        this.reason = jsonObject.has("reason") ? jsonObject.get("reason").getAsString() : "Banned by an operator.";
    }

    public Date getCreated() {
        return this.created;
    }

    public String getSource() {
        return this.source;
    }

    @Nullable
    public Date getExpires() {
        return this.expires;
    }

    public String getReason() {
        return this.reason;
    }

    public abstract Component getDisplayName();

    @Override
    boolean hasExpired() {
        if (this.expires == null) {
            return false;
        }
        return this.expires.before(new Date());
    }

    @Override
    protected void serialize(JsonObject jsonObject) {
        jsonObject.addProperty("created", DATE_FORMAT.format(this.created));
        jsonObject.addProperty("source", this.source);
        jsonObject.addProperty("expires", this.expires == null ? EXPIRES_NEVER : DATE_FORMAT.format(this.expires));
        jsonObject.addProperty("reason", this.reason);
    }
}

