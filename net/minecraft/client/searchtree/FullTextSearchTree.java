/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.IntersectionIterator;
import net.minecraft.client.searchtree.MergingUniqueIterator;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.resources.ResourceLocation;

public class FullTextSearchTree<T>
extends IdSearchTree<T> {
    private final SearchTree<T> plainTextSearchTree;

    public FullTextSearchTree(Function<T, Stream<String>> function, Function<T, Stream<ResourceLocation>> function2, List<T> list) {
        super(function2, list);
        this.plainTextSearchTree = SearchTree.plainText(list, function);
    }

    @Override
    protected List<T> searchPlainText(String string) {
        return this.plainTextSearchTree.search(string);
    }

    @Override
    protected List<T> searchResourceLocation(String string, String string2) {
        List list = this.resourceLocationSearchTree.searchNamespace(string);
        List list2 = this.resourceLocationSearchTree.searchPath(string2);
        List<T> list3 = this.plainTextSearchTree.search(string2);
        MergingUniqueIterator mergingUniqueIterator = new MergingUniqueIterator(list2.iterator(), list3.iterator(), this.additionOrder);
        return ImmutableList.copyOf(new IntersectionIterator(list.iterator(), mergingUniqueIterator, this.additionOrder));
    }
}

