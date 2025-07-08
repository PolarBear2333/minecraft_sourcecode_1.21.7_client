/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerTextFilter;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.GsonHelper;

public class LegacyTextFilter
extends ServerTextFilter {
    private static final String ENDPOINT = "v1/chat";
    final URL joinEndpoint;
    final JoinOrLeaveEncoder joinEncoder;
    final URL leaveEndpoint;
    final JoinOrLeaveEncoder leaveEncoder;
    private final String authKey;

    private LegacyTextFilter(URL uRL, ServerTextFilter.MessageEncoder messageEncoder, URL uRL2, JoinOrLeaveEncoder joinOrLeaveEncoder, URL uRL3, JoinOrLeaveEncoder joinOrLeaveEncoder2, String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, ExecutorService executorService) {
        super(uRL, messageEncoder, ignoreStrategy, executorService);
        this.joinEndpoint = uRL2;
        this.joinEncoder = joinOrLeaveEncoder;
        this.leaveEndpoint = uRL3;
        this.leaveEncoder = joinOrLeaveEncoder2;
        this.authKey = string;
    }

    @Nullable
    public static ServerTextFilter createTextFilterFromConfig(String string) {
        try {
            Object object;
            ServerTextFilter.MessageEncoder messageEncoder;
            JsonObject jsonObject = GsonHelper.parse(string);
            URI uRI = new URI(GsonHelper.getAsString(jsonObject, "apiServer"));
            String string2 = GsonHelper.getAsString(jsonObject, "apiKey");
            if (string2.isEmpty()) {
                throw new IllegalArgumentException("Missing API key");
            }
            int n = GsonHelper.getAsInt(jsonObject, "ruleId", 1);
            String string4 = GsonHelper.getAsString(jsonObject, "serverId", "");
            String string5 = GsonHelper.getAsString(jsonObject, "roomId", "Java:Chat");
            int n2 = GsonHelper.getAsInt(jsonObject, "hashesToDrop", -1);
            int n3 = GsonHelper.getAsInt(jsonObject, "maxConcurrentRequests", 7);
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "endpoints", null);
            String string6 = LegacyTextFilter.getEndpointFromConfig(jsonObject2, "chat", ENDPOINT);
            boolean bl = string6.equals(ENDPOINT);
            URL uRL = uRI.resolve("/" + string6).toURL();
            URL uRL2 = LegacyTextFilter.getEndpoint(uRI, jsonObject2, "join", "v1/join");
            URL uRL3 = LegacyTextFilter.getEndpoint(uRI, jsonObject2, "leave", "v1/leave");
            JoinOrLeaveEncoder joinOrLeaveEncoder = gameProfile -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("server", string4);
                jsonObject.addProperty("room", string5);
                jsonObject.addProperty("user_id", gameProfile.getId().toString());
                jsonObject.addProperty("user_display_name", gameProfile.getName());
                return jsonObject;
            };
            if (bl) {
                messageEncoder = (gameProfile, string3) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("rule", (Number)n);
                    jsonObject.addProperty("server", string4);
                    jsonObject.addProperty("room", string5);
                    jsonObject.addProperty("player", gameProfile.getId().toString());
                    jsonObject.addProperty("player_display_name", gameProfile.getName());
                    jsonObject.addProperty("text", string3);
                    jsonObject.addProperty("language", "*");
                    return jsonObject;
                };
            } else {
                object = String.valueOf(n);
                messageEncoder = (arg_0, arg_1) -> LegacyTextFilter.lambda$createTextFilterFromConfig$2((String)object, string4, string5, arg_0, arg_1);
            }
            object = ServerTextFilter.IgnoreStrategy.select(n2);
            ExecutorService executorService = LegacyTextFilter.createWorkerPool(n3);
            String string7 = Base64.getEncoder().encodeToString(string2.getBytes(StandardCharsets.US_ASCII));
            return new LegacyTextFilter(uRL, messageEncoder, uRL2, joinOrLeaveEncoder, uRL3, joinOrLeaveEncoder, string7, (ServerTextFilter.IgnoreStrategy)object, executorService);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse chat filter config {}", (Object)string, (Object)exception);
            return null;
        }
    }

    @Override
    public TextFilter createContext(GameProfile gameProfile) {
        return new ServerTextFilter.PlayerContext(gameProfile){

            @Override
            public void join() {
                LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.joinEndpoint, LegacyTextFilter.this.joinEncoder, this.streamExecutor);
            }

            @Override
            public void leave() {
                LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.leaveEndpoint, LegacyTextFilter.this.leaveEncoder, this.streamExecutor);
            }
        };
    }

    void processJoinOrLeave(GameProfile gameProfile, URL uRL, JoinOrLeaveEncoder joinOrLeaveEncoder, Executor executor) {
        executor.execute(() -> {
            JsonObject jsonObject = joinOrLeaveEncoder.encode(gameProfile);
            try {
                this.processRequest(jsonObject, uRL);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to send join/leave packet to {} for player {}", new Object[]{uRL, gameProfile, exception});
            }
        });
    }

    private void processRequest(JsonObject jsonObject, URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
        try (InputStream inputStream = httpURLConnection.getInputStream();){
            this.drainStream(inputStream);
        }
    }

    @Override
    protected void setAuthorizationProperty(HttpURLConnection httpURLConnection) {
        httpURLConnection.setRequestProperty("Authorization", "Basic " + this.authKey);
    }

    @Override
    protected FilteredText filterText(String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, JsonObject jsonObject) {
        boolean bl = GsonHelper.getAsBoolean(jsonObject, "response", false);
        if (bl) {
            return FilteredText.passThrough(string);
        }
        String string2 = GsonHelper.getAsString(jsonObject, "hashed", null);
        if (string2 == null) {
            return FilteredText.fullyFiltered(string);
        }
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "hashes");
        FilterMask filterMask = this.parseMask(string, jsonArray, ignoreStrategy);
        return new FilteredText(string, filterMask);
    }

    private static /* synthetic */ JsonObject lambda$createTextFilterFromConfig$2(String string, String string2, String string3, GameProfile gameProfile, String string4) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("rule_id", string);
        jsonObject.addProperty("category", string2);
        jsonObject.addProperty("subcategory", string3);
        jsonObject.addProperty("user_id", gameProfile.getId().toString());
        jsonObject.addProperty("user_display_name", gameProfile.getName());
        jsonObject.addProperty("text", string4);
        jsonObject.addProperty("language", "*");
        return jsonObject;
    }

    @FunctionalInterface
    static interface JoinOrLeaveEncoder {
        public JsonObject encode(GameProfile var1);
    }
}

