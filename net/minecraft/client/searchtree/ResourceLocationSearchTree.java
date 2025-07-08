/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.resources.ResourceLocation;

public interface ResourceLocationSearchTree<T> {
    public static <T> ResourceLocationSearchTree<T> empty() {
        return new ResourceLocationSearchTree<T>(){

            @Override
            public List<T> searchNamespace(String string) {
                return List.of();
            }

            @Override
            public List<T> searchPath(String string) {
                return List.of();
            }
        };
    }

    public static <T> ResourceLocationSearchTree<T> create(List<T> list, Function<T, Stream<ResourceLocation>> function) {
        if (list.isEmpty()) {
            return ResourceLocationSearchTree.empty();
        }
        final SuffixArray suffixArray = new SuffixArray();
        final SuffixArray suffixArray2 = new SuffixArray();
        for (Object t : list) {
            function.apply(t).forEach(resourceLocation -> {
                suffixArray.add(t, resourceLocation.getNamespace().toLowerCase(Locale.ROOT));
                suffixArray2.add(t, resourceLocation.getPath().toLowerCase(Locale.ROOT));
            });
        }
        suffixArray.generate();
        suffixArray2.generate();
        return new ResourceLocationSearchTree<T>(){

            @Override
            public List<T> searchNamespace(String string) {
                return suffixArray.search(string);
            }

            @Override
            public List<T> searchPath(String string) {
                return suffixArray2.search(string);
            }
        };
    }

    public List<T> searchNamespace(String var1);

    public List<T> searchPath(String var1);
}

