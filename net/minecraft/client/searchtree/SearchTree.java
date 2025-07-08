/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.searchtree.SuffixArray;

@FunctionalInterface
public interface SearchTree<T> {
    public static <T> SearchTree<T> empty() {
        return string -> List.of();
    }

    public static <T> SearchTree<T> plainText(List<T> list, Function<T, Stream<String>> function) {
        if (list.isEmpty()) {
            return SearchTree.empty();
        }
        SuffixArray suffixArray = new SuffixArray();
        for (Object t : list) {
            function.apply(t).forEach(string -> suffixArray.add(t, string.toLowerCase(Locale.ROOT)));
        }
        suffixArray.generate();
        return suffixArray::search;
    }

    public List<T> search(String var1);
}

