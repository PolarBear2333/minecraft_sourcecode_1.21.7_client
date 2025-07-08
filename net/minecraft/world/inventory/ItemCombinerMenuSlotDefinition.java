/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
    private final List<SlotDefinition> slots;
    private final SlotDefinition resultSlot;

    ItemCombinerMenuSlotDefinition(List<SlotDefinition> list, SlotDefinition slotDefinition) {
        if (list.isEmpty() || slotDefinition.equals(SlotDefinition.EMPTY)) {
            throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
        }
        this.slots = list;
        this.resultSlot = slotDefinition;
    }

    public static Builder create() {
        return new Builder();
    }

    public SlotDefinition getSlot(int n) {
        return this.slots.get(n);
    }

    public SlotDefinition getResultSlot() {
        return this.resultSlot;
    }

    public List<SlotDefinition> getSlots() {
        return this.slots;
    }

    public int getNumOfInputSlots() {
        return this.slots.size();
    }

    public int getResultSlotIndex() {
        return this.getNumOfInputSlots();
    }

    public static final class SlotDefinition
    extends Record {
        final int slotIndex;
        private final int x;
        private final int y;
        private final Predicate<ItemStack> mayPlace;
        static final SlotDefinition EMPTY = new SlotDefinition(0, 0, 0, itemStack -> true);

        public SlotDefinition(int n, int n2, int n3, Predicate<ItemStack> predicate) {
            this.slotIndex = n;
            this.x = n2;
            this.y = n3;
            this.mayPlace = predicate;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SlotDefinition.class, "slotIndex;x;y;mayPlace", "slotIndex", "x", "y", "mayPlace"}, this, object);
        }

        public int slotIndex() {
            return this.slotIndex;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public Predicate<ItemStack> mayPlace() {
            return this.mayPlace;
        }
    }

    public static class Builder {
        private final List<SlotDefinition> inputSlots = new ArrayList<SlotDefinition>();
        private SlotDefinition resultSlot = SlotDefinition.EMPTY;

        public Builder withSlot(int n, int n2, int n3, Predicate<ItemStack> predicate) {
            this.inputSlots.add(new SlotDefinition(n, n2, n3, predicate));
            return this;
        }

        public Builder withResultSlot(int n, int n2, int n3) {
            this.resultSlot = new SlotDefinition(n, n2, n3, itemStack -> false);
            return this;
        }

        public ItemCombinerMenuSlotDefinition build() {
            int n = this.inputSlots.size();
            for (int i = 0; i < n; ++i) {
                SlotDefinition slotDefinition = this.inputSlots.get(i);
                if (slotDefinition.slotIndex == i) continue;
                throw new IllegalArgumentException("Expected input slots to have continous indexes");
            }
            if (this.resultSlot.slotIndex != n) {
                throw new IllegalArgumentException("Expected result slot index to follow last input slot");
            }
            return new ItemCombinerMenuSlotDefinition(this.inputSlots, this.resultSlot);
        }
    }
}

