/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.PeekingIterator
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;

public class IntersectionIterator<T>
extends AbstractIterator<T> {
    private final PeekingIterator<T> firstIterator;
    private final PeekingIterator<T> secondIterator;
    private final Comparator<T> comparator;

    public IntersectionIterator(Iterator<T> iterator, Iterator<T> iterator2, Comparator<T> comparator) {
        this.firstIterator = Iterators.peekingIterator(iterator);
        this.secondIterator = Iterators.peekingIterator(iterator2);
        this.comparator = comparator;
    }

    protected T computeNext() {
        while (this.firstIterator.hasNext() && this.secondIterator.hasNext()) {
            int n = this.comparator.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (n == 0) {
                this.secondIterator.next();
                return (T)this.firstIterator.next();
            }
            if (n < 0) {
                this.firstIterator.next();
                continue;
            }
            this.secondIterator.next();
        }
        return (T)this.endOfData();
    }
}

