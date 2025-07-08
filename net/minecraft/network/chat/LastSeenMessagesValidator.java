/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;

public class LastSeenMessagesValidator {
    private final int lastSeenCount;
    private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList();
    @Nullable
    private MessageSignature lastPendingMessage;

    public LastSeenMessagesValidator(int n) {
        this.lastSeenCount = n;
        for (int i = 0; i < n; ++i) {
            this.trackedMessages.add(null);
        }
    }

    public void addPending(MessageSignature messageSignature) {
        if (!messageSignature.equals(this.lastPendingMessage)) {
            this.trackedMessages.add((Object)new LastSeenTrackedEntry(messageSignature, true));
            this.lastPendingMessage = messageSignature;
        }
    }

    public int trackedMessagesCount() {
        return this.trackedMessages.size();
    }

    public void applyOffset(int n) throws ValidationException {
        int n2 = this.trackedMessages.size() - this.lastSeenCount;
        if (n < 0 || n > n2) {
            throw new ValidationException("Advanced last seen window by " + n + " messages, but expected at most " + n2);
        }
        this.trackedMessages.removeElements(0, n);
    }

    public LastSeenMessages applyUpdate(LastSeenMessages.Update update) throws ValidationException {
        this.applyOffset(update.offset());
        ObjectArrayList objectArrayList = new ObjectArrayList(update.acknowledged().cardinality());
        if (update.acknowledged().length() > this.lastSeenCount) {
            throw new ValidationException("Last seen update contained " + update.acknowledged().length() + " messages, but maximum window size is " + this.lastSeenCount);
        }
        for (int i = 0; i < this.lastSeenCount; ++i) {
            boolean bl = update.acknowledged().get(i);
            LastSeenTrackedEntry lastSeenTrackedEntry = (LastSeenTrackedEntry)this.trackedMessages.get(i);
            if (bl) {
                if (lastSeenTrackedEntry == null) {
                    throw new ValidationException("Last seen update acknowledged unknown or previously ignored message at index " + i);
                }
                this.trackedMessages.set(i, (Object)lastSeenTrackedEntry.acknowledge());
                objectArrayList.add((Object)lastSeenTrackedEntry.signature());
                continue;
            }
            if (lastSeenTrackedEntry != null && !lastSeenTrackedEntry.pending()) {
                throw new ValidationException("Last seen update ignored previously acknowledged message at index " + i + " and signature " + String.valueOf(lastSeenTrackedEntry.signature()));
            }
            this.trackedMessages.set(i, null);
        }
        LastSeenMessages lastSeenMessages = new LastSeenMessages((List<MessageSignature>)objectArrayList);
        if (!update.verifyChecksum(lastSeenMessages)) {
            throw new ValidationException("Checksum mismatch on last seen update: the client and server must have desynced");
        }
        return lastSeenMessages;
    }

    public static class ValidationException
    extends Exception {
        public ValidationException(String string) {
            super(string);
        }
    }
}

