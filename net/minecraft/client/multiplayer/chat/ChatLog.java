/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;

public class ChatLog {
    private final LoggedChatEvent[] buffer;
    private int nextId;

    public static Codec<ChatLog> codec(int n) {
        return Codec.list(LoggedChatEvent.CODEC).comapFlatMap(list -> {
            int n2 = list.size();
            if (n2 > n) {
                return DataResult.error(() -> "Expected: a buffer of size less than or equal to " + n + " but: " + n2 + " is greater than " + n);
            }
            return DataResult.success((Object)new ChatLog(n, (List<LoggedChatEvent>)list));
        }, ChatLog::loggedChatEvents);
    }

    public ChatLog(int n) {
        this.buffer = new LoggedChatEvent[n];
    }

    private ChatLog(int n, List<LoggedChatEvent> list) {
        this.buffer = (LoggedChatEvent[])list.toArray(n2 -> new LoggedChatEvent[n]);
        this.nextId = list.size();
    }

    private List<LoggedChatEvent> loggedChatEvents() {
        ArrayList<LoggedChatEvent> arrayList = new ArrayList<LoggedChatEvent>(this.size());
        for (int i = this.start(); i <= this.end(); ++i) {
            arrayList.add(this.lookup(i));
        }
        return arrayList;
    }

    public void push(LoggedChatEvent loggedChatEvent) {
        this.buffer[this.index((int)this.nextId++)] = loggedChatEvent;
    }

    @Nullable
    public LoggedChatEvent lookup(int n) {
        return n >= this.start() && n <= this.end() ? this.buffer[this.index(n)] : null;
    }

    private int index(int n) {
        return n % this.buffer.length;
    }

    public int start() {
        return Math.max(this.nextId - this.buffer.length, 0);
    }

    public int end() {
        return this.nextId - 1;
    }

    private int size() {
        return this.end() - this.start() + 1;
    }
}

