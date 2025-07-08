/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.math.Fraction
 */
package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.apache.commons.lang3.math.Fraction;

public final class BundleContents
implements TooltipComponent {
    public static final BundleContents EMPTY = new BundleContents(List.of());
    public static final Codec<BundleContents> CODEC = ItemStack.CODEC.listOf().flatXmap(BundleContents::checkAndCreate, bundleContents -> DataResult.success(bundleContents.items));
    public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(BundleContents::new, bundleContents -> bundleContents.items);
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction((int)1, (int)16);
    private static final int NO_STACK_INDEX = -1;
    public static final int NO_SELECTED_ITEM_INDEX = -1;
    final List<ItemStack> items;
    final Fraction weight;
    final int selectedItem;

    BundleContents(List<ItemStack> list, Fraction fraction, int n) {
        this.items = list;
        this.weight = fraction;
        this.selectedItem = n;
    }

    private static DataResult<BundleContents> checkAndCreate(List<ItemStack> list) {
        try {
            Fraction fraction = BundleContents.computeContentWeight(list);
            return DataResult.success((Object)new BundleContents(list, fraction, -1));
        }
        catch (ArithmeticException arithmeticException) {
            return DataResult.error(() -> "Excessive total bundle weight");
        }
    }

    public BundleContents(List<ItemStack> list) {
        this(list, BundleContents.computeContentWeight(list), -1);
    }

    private static Fraction computeContentWeight(List<ItemStack> list) {
        Fraction fraction = Fraction.ZERO;
        for (ItemStack itemStack : list) {
            fraction = fraction.add(BundleContents.getWeight(itemStack).multiplyBy(Fraction.getFraction((int)itemStack.getCount(), (int)1)));
        }
        return fraction;
    }

    static Fraction getWeight(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            return BUNDLE_IN_BUNDLE_WEIGHT.add(bundleContents.weight());
        }
        List<BeehiveBlockEntity.Occupant> list = itemStack.getOrDefault(DataComponents.BEES, Bees.EMPTY).bees();
        if (!list.isEmpty()) {
            return Fraction.ONE;
        }
        return Fraction.getFraction((int)1, (int)itemStack.getMaxStackSize());
    }

    public static boolean canItemBeInBundle(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getItem().canFitInsideContainerItems();
    }

    public int getNumberOfItemsToShow() {
        int n = this.size();
        int n2 = n > 12 ? 11 : 12;
        int n3 = n % 4;
        int n4 = n3 == 0 ? 0 : 4 - n3;
        return Math.min(n, n2 - n4);
    }

    public ItemStack getItemUnsafe(int n) {
        return this.items.get(n);
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> items() {
        return this.items;
    }

    public Iterable<ItemStack> itemsCopy() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public int size() {
        return this.items.size();
    }

    public Fraction weight() {
        return this.weight;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getSelectedItem() {
        return this.selectedItem;
    }

    public boolean hasSelectedItem() {
        return this.selectedItem != -1;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BundleContents) {
            BundleContents bundleContents = (BundleContents)object;
            return this.weight.equals((Object)bundleContents.weight) && ItemStack.listMatches(this.items, bundleContents.items);
        }
        return false;
    }

    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    public String toString() {
        return "BundleContents" + String.valueOf(this.items);
    }

    public static class Mutable {
        private final List<ItemStack> items;
        private Fraction weight;
        private int selectedItem;

        public Mutable(BundleContents bundleContents) {
            this.items = new ArrayList<ItemStack>(bundleContents.items);
            this.weight = bundleContents.weight;
            this.selectedItem = bundleContents.selectedItem;
        }

        public Mutable clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            this.selectedItem = -1;
            return this;
        }

        private int findStackIndex(ItemStack itemStack) {
            if (!itemStack.isStackable()) {
                return -1;
            }
            for (int i = 0; i < this.items.size(); ++i) {
                if (!ItemStack.isSameItemSameComponents(this.items.get(i), itemStack)) continue;
                return i;
            }
            return -1;
        }

        private int getMaxAmountToAdd(ItemStack itemStack) {
            Fraction fraction = Fraction.ONE.subtract(this.weight);
            return Math.max(fraction.divideBy(BundleContents.getWeight(itemStack)).intValue(), 0);
        }

        public int tryInsert(ItemStack itemStack) {
            if (!BundleContents.canItemBeInBundle(itemStack)) {
                return 0;
            }
            int n = Math.min(itemStack.getCount(), this.getMaxAmountToAdd(itemStack));
            if (n == 0) {
                return 0;
            }
            this.weight = this.weight.add(BundleContents.getWeight(itemStack).multiplyBy(Fraction.getFraction((int)n, (int)1)));
            int n2 = this.findStackIndex(itemStack);
            if (n2 != -1) {
                ItemStack itemStack2 = this.items.remove(n2);
                ItemStack itemStack3 = itemStack2.copyWithCount(itemStack2.getCount() + n);
                itemStack.shrink(n);
                this.items.add(0, itemStack3);
            } else {
                this.items.add(0, itemStack.split(n));
            }
            return n;
        }

        public int tryTransfer(Slot slot, Player player) {
            ItemStack itemStack = slot.getItem();
            int n = this.getMaxAmountToAdd(itemStack);
            return BundleContents.canItemBeInBundle(itemStack) ? this.tryInsert(slot.safeTake(itemStack.getCount(), n, player)) : 0;
        }

        public void toggleSelectedItem(int n) {
            this.selectedItem = this.selectedItem == n || this.indexIsOutsideAllowedBounds(n) ? -1 : n;
        }

        private boolean indexIsOutsideAllowedBounds(int n) {
            return n < 0 || n >= this.items.size();
        }

        @Nullable
        public ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            }
            int n = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
            ItemStack itemStack = this.items.remove(n).copy();
            this.weight = this.weight.subtract(BundleContents.getWeight(itemStack).multiplyBy(Fraction.getFraction((int)itemStack.getCount(), (int)1)));
            this.toggleSelectedItem(-1);
            return itemStack;
        }

        public Fraction weight() {
            return this.weight;
        }

        public BundleContents toImmutable() {
            return new BundleContents(List.copyOf(this.items), this.weight, this.selectedItem);
        }
    }
}

