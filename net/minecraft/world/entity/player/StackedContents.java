/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.objects.ObjectIterable
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntMaps
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.player;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;

public class StackedContents<T> {
    public final Reference2IntOpenHashMap<T> amounts = new Reference2IntOpenHashMap();

    boolean hasAtLeast(T t, int n) {
        return this.amounts.getInt(t) >= n;
    }

    void take(T t, int n) {
        int n2 = this.amounts.addTo(t, -n);
        if (n2 < n) {
            throw new IllegalStateException("Took " + n + " items, but only had " + n2);
        }
    }

    void put(T t, int n) {
        this.amounts.addTo(t, n);
    }

    public boolean tryPick(List<? extends IngredientInfo<T>> list, int n, @Nullable Output<T> output) {
        return new RecipePicker(list).tryPick(n, output);
    }

    public int tryPickAll(List<? extends IngredientInfo<T>> list, int n, @Nullable Output<T> output) {
        return new RecipePicker(list).tryPickAll(n, output);
    }

    public void clear() {
        this.amounts.clear();
    }

    public void account(T t, int n) {
        this.put(t, n);
    }

    List<T> getUniqueAvailableIngredientItems(Iterable<? extends IngredientInfo<T>> iterable) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Reference2IntMap.Entry entry : Reference2IntMaps.fastIterable(this.amounts)) {
            if (entry.getIntValue() <= 0 || !StackedContents.anyIngredientMatches(iterable, entry.getKey())) continue;
            arrayList.add(entry.getKey());
        }
        return arrayList;
    }

    private static <T> boolean anyIngredientMatches(Iterable<? extends IngredientInfo<T>> iterable, T t) {
        for (IngredientInfo<T> ingredientInfo : iterable) {
            if (!ingredientInfo.acceptsItem(t)) continue;
            return true;
        }
        return false;
    }

    @VisibleForTesting
    public int getResultUpperBound(List<? extends IngredientInfo<T>> list) {
        int n = Integer.MAX_VALUE;
        ObjectIterable objectIterable = Reference2IntMaps.fastIterable(this.amounts);
        block0: for (IngredientInfo<Object> ingredientInfo : list) {
            int n2 = 0;
            for (Reference2IntMap.Entry entry : objectIterable) {
                int n3 = entry.getIntValue();
                if (n3 <= n2) continue;
                if (ingredientInfo.acceptsItem(entry.getKey())) {
                    n2 = n3;
                }
                if (n2 < n) continue;
                continue block0;
            }
            n = n2;
            if (n != 0) continue;
            break;
        }
        return n;
    }

    class RecipePicker {
        private final List<? extends IngredientInfo<T>> ingredients;
        private final int ingredientCount;
        private final List<T> items;
        private final int itemCount;
        private final BitSet data;
        private final IntList path = new IntArrayList();

        public RecipePicker(List<? extends IngredientInfo<T>> list) {
            this.ingredients = list;
            this.ingredientCount = list.size();
            this.items = StackedContents.this.getUniqueAvailableIngredientItems(list);
            this.itemCount = this.items.size();
            this.data = new BitSet(this.visitedIngredientCount() + this.visitedItemCount() + this.satisfiedCount() + this.connectionCount() + this.residualCount());
            this.setInitialConnections();
        }

        private void setInitialConnections() {
            for (int i = 0; i < this.ingredientCount; ++i) {
                IngredientInfo ingredientInfo = this.ingredients.get(i);
                for (int j = 0; j < this.itemCount; ++j) {
                    if (!ingredientInfo.acceptsItem(this.items.get(j))) continue;
                    this.setConnection(j, i);
                }
            }
        }

        public boolean tryPick(int n, @Nullable Output<T> output) {
            int n2;
            int n3;
            int n4;
            IntList intList;
            if (n <= 0) {
                return true;
            }
            int n5 = 0;
            while ((intList = this.tryAssigningNewItem(n)) != null) {
                n4 = intList.getInt(0);
                StackedContents.this.take(this.items.get(n4), n);
                n3 = intList.size() - 1;
                this.setSatisfied(intList.getInt(n3));
                ++n5;
                for (n2 = 0; n2 < intList.size() - 1; ++n2) {
                    int n6;
                    int n7;
                    if (RecipePicker.isPathIndexItem(n2)) {
                        n7 = intList.getInt(n2);
                        n6 = intList.getInt(n2 + 1);
                        this.assign(n7, n6);
                        continue;
                    }
                    n7 = intList.getInt(n2 + 1);
                    n6 = intList.getInt(n2);
                    this.unassign(n7, n6);
                }
            }
            boolean bl = n5 == this.ingredientCount;
            n4 = bl && output != null ? 1 : 0;
            this.clearAllVisited();
            this.clearSatisfied();
            block2: for (n3 = 0; n3 < this.ingredientCount; ++n3) {
                for (n2 = 0; n2 < this.itemCount; ++n2) {
                    if (!this.isAssigned(n2, n3)) continue;
                    this.unassign(n2, n3);
                    StackedContents.this.put(this.items.get(n2), n);
                    if (n4 == 0) continue block2;
                    output.accept(this.items.get(n2));
                    continue block2;
                }
            }
            assert (this.data.get(this.residualOffset(), this.residualOffset() + this.residualCount()).isEmpty());
            return bl;
        }

        private static boolean isPathIndexItem(int n) {
            return (n & 1) == 0;
        }

        @Nullable
        private IntList tryAssigningNewItem(int n) {
            this.clearAllVisited();
            for (int i = 0; i < this.itemCount; ++i) {
                IntList intList;
                if (!StackedContents.this.hasAtLeast(this.items.get(i), n) || (intList = this.findNewItemAssignmentPath(i)) == null) continue;
                return intList;
            }
            return null;
        }

        @Nullable
        private IntList findNewItemAssignmentPath(int n) {
            this.path.clear();
            this.visitItem(n);
            this.path.add(n);
            while (!this.path.isEmpty()) {
                int n2;
                int n3 = this.path.size();
                if (RecipePicker.isPathIndexItem(n3 - 1)) {
                    n2 = this.path.getInt(n3 - 1);
                    for (var4_4 = 0; var4_4 < this.ingredientCount; ++var4_4) {
                        if (this.hasVisitedIngredient(var4_4) || !this.hasConnection(n2, var4_4) || this.isAssigned(n2, var4_4)) continue;
                        this.visitIngredient(var4_4);
                        this.path.add(var4_4);
                        break;
                    }
                } else {
                    n2 = this.path.getInt(n3 - 1);
                    if (!this.isSatisfied(n2)) {
                        return this.path;
                    }
                    for (var4_4 = 0; var4_4 < this.itemCount; ++var4_4) {
                        if (this.hasVisitedItem(var4_4) || !this.isAssigned(var4_4, n2)) continue;
                        assert (this.hasConnection(var4_4, n2));
                        this.visitItem(var4_4);
                        this.path.add(var4_4);
                        break;
                    }
                }
                if ((n2 = this.path.size()) != n3) continue;
                this.path.removeInt(n2 - 1);
            }
            return null;
        }

        private int visitedIngredientOffset() {
            return 0;
        }

        private int visitedIngredientCount() {
            return this.ingredientCount;
        }

        private int visitedItemOffset() {
            return this.visitedIngredientOffset() + this.visitedIngredientCount();
        }

        private int visitedItemCount() {
            return this.itemCount;
        }

        private int satisfiedOffset() {
            return this.visitedItemOffset() + this.visitedItemCount();
        }

        private int satisfiedCount() {
            return this.ingredientCount;
        }

        private int connectionOffset() {
            return this.satisfiedOffset() + this.satisfiedCount();
        }

        private int connectionCount() {
            return this.ingredientCount * this.itemCount;
        }

        private int residualOffset() {
            return this.connectionOffset() + this.connectionCount();
        }

        private int residualCount() {
            return this.ingredientCount * this.itemCount;
        }

        private boolean isSatisfied(int n) {
            return this.data.get(this.getSatisfiedIndex(n));
        }

        private void setSatisfied(int n) {
            this.data.set(this.getSatisfiedIndex(n));
        }

        private int getSatisfiedIndex(int n) {
            assert (n >= 0 && n < this.ingredientCount);
            return this.satisfiedOffset() + n;
        }

        private void clearSatisfied() {
            this.clearRange(this.satisfiedOffset(), this.satisfiedCount());
        }

        private void setConnection(int n, int n2) {
            this.data.set(this.getConnectionIndex(n, n2));
        }

        private boolean hasConnection(int n, int n2) {
            return this.data.get(this.getConnectionIndex(n, n2));
        }

        private int getConnectionIndex(int n, int n2) {
            assert (n >= 0 && n < this.itemCount);
            assert (n2 >= 0 && n2 < this.ingredientCount);
            return this.connectionOffset() + n * this.ingredientCount + n2;
        }

        private boolean isAssigned(int n, int n2) {
            return this.data.get(this.getResidualIndex(n, n2));
        }

        private void assign(int n, int n2) {
            int n3 = this.getResidualIndex(n, n2);
            assert (!this.data.get(n3));
            this.data.set(n3);
        }

        private void unassign(int n, int n2) {
            int n3 = this.getResidualIndex(n, n2);
            assert (this.data.get(n3));
            this.data.clear(n3);
        }

        private int getResidualIndex(int n, int n2) {
            assert (n >= 0 && n < this.itemCount);
            assert (n2 >= 0 && n2 < this.ingredientCount);
            return this.residualOffset() + n * this.ingredientCount + n2;
        }

        private void visitIngredient(int n) {
            this.data.set(this.getVisitedIngredientIndex(n));
        }

        private boolean hasVisitedIngredient(int n) {
            return this.data.get(this.getVisitedIngredientIndex(n));
        }

        private int getVisitedIngredientIndex(int n) {
            assert (n >= 0 && n < this.ingredientCount);
            return this.visitedIngredientOffset() + n;
        }

        private void visitItem(int n) {
            this.data.set(this.getVisitiedItemIndex(n));
        }

        private boolean hasVisitedItem(int n) {
            return this.data.get(this.getVisitiedItemIndex(n));
        }

        private int getVisitiedItemIndex(int n) {
            assert (n >= 0 && n < this.itemCount);
            return this.visitedItemOffset() + n;
        }

        private void clearAllVisited() {
            this.clearRange(this.visitedIngredientOffset(), this.visitedIngredientCount());
            this.clearRange(this.visitedItemOffset(), this.visitedItemCount());
        }

        private void clearRange(int n, int n2) {
            this.data.clear(n, n + n2);
        }

        public int tryPickAll(int n, @Nullable Output<T> output) {
            int n2;
            int n3 = 0;
            int n4 = Math.min(n, StackedContents.this.getResultUpperBound(this.ingredients)) + 1;
            while (true) {
                if (this.tryPick(n2 = (n3 + n4) / 2, null)) {
                    if (n4 - n3 <= 1) break;
                    n3 = n2;
                    continue;
                }
                n4 = n2;
            }
            if (n2 > 0) {
                this.tryPick(n2, output);
            }
            return n2;
        }
    }

    @FunctionalInterface
    public static interface Output<T> {
        public void accept(T var1);
    }

    @FunctionalInterface
    public static interface IngredientInfo<T> {
        public boolean acceptsItem(T var1);
    }
}

