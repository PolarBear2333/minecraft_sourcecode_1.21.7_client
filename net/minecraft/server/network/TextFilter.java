/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.network.FilteredText;

public interface TextFilter {
    public static final TextFilter DUMMY = new TextFilter(){

        @Override
        public CompletableFuture<FilteredText> processStreamMessage(String string) {
            return CompletableFuture.completedFuture(FilteredText.passThrough(string));
        }

        @Override
        public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
            return CompletableFuture.completedFuture((List)list.stream().map(FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
        }
    };

    default public void join() {
    }

    default public void leave() {
    }

    public CompletableFuture<FilteredText> processStreamMessage(String var1);

    public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> var1);
}

