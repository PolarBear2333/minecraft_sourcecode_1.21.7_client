/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;

public interface ErrorCollector<S> {
    public void store(int var1, SuggestionSupplier<S> var2, Object var3);

    default public void store(int n, Object object) {
        this.store(n, SuggestionSupplier.empty(), object);
    }

    public void finish(int var1);

    public static class LongestOnly<S>
    implements ErrorCollector<S> {
        private MutableErrorEntry<S>[] entries = new MutableErrorEntry[16];
        private int nextErrorEntry;
        private int lastCursor = -1;

        private void discardErrorsFromShorterParse(int n) {
            if (n > this.lastCursor) {
                this.lastCursor = n;
                this.nextErrorEntry = 0;
            }
        }

        @Override
        public void finish(int n) {
            this.discardErrorsFromShorterParse(n);
        }

        @Override
        public void store(int n, SuggestionSupplier<S> suggestionSupplier, Object object) {
            this.discardErrorsFromShorterParse(n);
            if (n == this.lastCursor) {
                this.addErrorEntry(suggestionSupplier, object);
            }
        }

        private void addErrorEntry(SuggestionSupplier<S> suggestionSupplier, Object object) {
            Object object2;
            int n;
            int n2 = this.entries.length;
            if (this.nextErrorEntry >= n2) {
                n = Util.growByHalf(n2, this.nextErrorEntry + 1);
                object2 = new MutableErrorEntry[n];
                System.arraycopy(this.entries, 0, object2, 0, n2);
                this.entries = object2;
            }
            if ((object2 = this.entries[n = this.nextErrorEntry++]) == null) {
                this.entries[n] = object2 = new MutableErrorEntry();
            }
            object2.suggestions = suggestionSupplier;
            object2.reason = object;
        }

        public List<ErrorEntry<S>> entries() {
            int n = this.nextErrorEntry;
            if (n == 0) {
                return List.of();
            }
            ArrayList<ErrorEntry<S>> arrayList = new ArrayList<ErrorEntry<S>>(n);
            for (int i = 0; i < n; ++i) {
                MutableErrorEntry<S> mutableErrorEntry = this.entries[i];
                arrayList.add(new ErrorEntry(this.lastCursor, mutableErrorEntry.suggestions, mutableErrorEntry.reason));
            }
            return arrayList;
        }

        public int cursor() {
            return this.lastCursor;
        }

        static class MutableErrorEntry<S> {
            SuggestionSupplier<S> suggestions = SuggestionSupplier.empty();
            Object reason = "empty";

            MutableErrorEntry() {
            }
        }
    }

    public static class Nop<S>
    implements ErrorCollector<S> {
        @Override
        public void store(int n, SuggestionSupplier<S> suggestionSupplier, Object object) {
        }

        @Override
        public void finish(int n) {
        }
    }
}

