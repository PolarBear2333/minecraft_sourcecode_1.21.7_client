/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.io.Files
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();

    public StoredUserList(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public void add(V v) {
        this.map.put(this.getKeyForUser(((StoredUserEntry)v).getUser()), v);
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after adding a user.", (Throwable)iOException);
        }
    }

    @Nullable
    public V get(K k) {
        this.removeExpired();
        return (V)((StoredUserEntry)this.map.get(this.getKeyForUser(k)));
    }

    public void remove(K k) {
        this.map.remove(this.getKeyForUser(k));
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)iOException);
        }
    }

    public void remove(StoredUserEntry<K> storedUserEntry) {
        this.remove(storedUserEntry.getUser());
    }

    public String[] getUserList() {
        return this.map.keySet().toArray(new String[0]);
    }

    public boolean isEmpty() {
        return this.map.size() < 1;
    }

    protected String getKeyForUser(K k) {
        return k.toString();
    }

    protected boolean contains(K k) {
        return this.map.containsKey(this.getKeyForUser(k));
    }

    private void removeExpired() {
        ArrayList arrayList = Lists.newArrayList();
        for (Object object : this.map.values()) {
            if (!((StoredUserEntry)object).hasExpired()) continue;
            arrayList.add(((StoredUserEntry)object).getUser());
        }
        for (Object object : arrayList) {
            this.map.remove(this.getKeyForUser(object));
        }
    }

    protected abstract StoredUserEntry<K> createEntry(JsonObject var1);

    public Collection<V> getEntries() {
        return this.map.values();
    }

    public void save() throws IOException {
        JsonArray jsonArray = new JsonArray();
        this.map.values().stream().map(storedUserEntry -> Util.make(new JsonObject(), storedUserEntry::serialize)).forEach(arg_0 -> ((JsonArray)jsonArray).add(arg_0));
        try (BufferedWriter bufferedWriter = Files.newWriter((File)this.file, (Charset)StandardCharsets.UTF_8);){
            GSON.toJson((JsonElement)jsonArray, GSON.newJsonWriter((Writer)bufferedWriter));
        }
    }

    public void load() throws IOException {
        if (!this.file.exists()) {
            return;
        }
        try (BufferedReader bufferedReader = Files.newReader((File)this.file, (Charset)StandardCharsets.UTF_8);){
            this.map.clear();
            JsonArray jsonArray = (JsonArray)GSON.fromJson((Reader)bufferedReader, JsonArray.class);
            if (jsonArray == null) {
                return;
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
                StoredUserEntry<K> storedUserEntry = this.createEntry(jsonObject);
                if (storedUserEntry.getUser() == null) continue;
                this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);
            }
        }
    }
}

