/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ContainerHelper {
    public static final String TAG_ITEMS = "Items";

    public static ItemStack removeItem(List<ItemStack> list, int n, int n2) {
        if (n < 0 || n >= list.size() || list.get(n).isEmpty() || n2 <= 0) {
            return ItemStack.EMPTY;
        }
        return list.get(n).split(n2);
    }

    public static ItemStack takeItem(List<ItemStack> list, int n) {
        if (n < 0 || n >= list.size()) {
            return ItemStack.EMPTY;
        }
        return list.set(n, ItemStack.EMPTY);
    }

    public static void saveAllItems(ValueOutput valueOutput, NonNullList<ItemStack> nonNullList) {
        ContainerHelper.saveAllItems(valueOutput, nonNullList, true);
    }

    public static void saveAllItems(ValueOutput valueOutput, NonNullList<ItemStack> nonNullList, boolean bl) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList = valueOutput.list(TAG_ITEMS, ItemStackWithSlot.CODEC);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = nonNullList.get(i);
            if (itemStack.isEmpty()) continue;
            typedOutputList.add(new ItemStackWithSlot(i, itemStack));
        }
        if (typedOutputList.isEmpty() && !bl) {
            valueOutput.discard(TAG_ITEMS);
        }
    }

    public static void loadAllItems(ValueInput valueInput, NonNullList<ItemStack> nonNullList) {
        for (ItemStackWithSlot itemStackWithSlot : valueInput.listOrEmpty(TAG_ITEMS, ItemStackWithSlot.CODEC)) {
            if (!itemStackWithSlot.isValidInContainer(nonNullList.size())) continue;
            nonNullList.set(itemStackWithSlot.slot(), itemStackWithSlot.stack());
        }
    }

    public static int clearOrCountMatchingItems(Container container, Predicate<ItemStack> predicate, int n, boolean bl) {
        int n2 = 0;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            int n3 = ContainerHelper.clearOrCountMatchingItems(itemStack, predicate, n - n2, bl);
            if (n3 > 0 && !bl && itemStack.isEmpty()) {
                container.setItem(i, ItemStack.EMPTY);
            }
            n2 += n3;
        }
        return n2;
    }

    public static int clearOrCountMatchingItems(ItemStack itemStack, Predicate<ItemStack> predicate, int n, boolean bl) {
        if (itemStack.isEmpty() || !predicate.test(itemStack)) {
            return 0;
        }
        if (bl) {
            return itemStack.getCount();
        }
        int n2 = n < 0 ? itemStack.getCount() : Math.min(n, itemStack.getCount());
        itemStack.shrink(n2);
        return n2;
    }
}

