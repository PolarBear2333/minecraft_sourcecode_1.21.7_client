/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record UseRemainder(ItemStack convertInto) {
    public static final Codec<UseRemainder> CODEC = ItemStack.CODEC.xmap(UseRemainder::new, UseRemainder::convertInto);
    public static final StreamCodec<RegistryFriendlyByteBuf, UseRemainder> STREAM_CODEC = StreamCodec.composite(ItemStack.STREAM_CODEC, UseRemainder::convertInto, UseRemainder::new);

    public ItemStack convertIntoRemainder(ItemStack itemStack, int n, boolean bl, OnExtraCreatedRemainder onExtraCreatedRemainder) {
        if (bl) {
            return itemStack;
        }
        if (itemStack.getCount() >= n) {
            return itemStack;
        }
        ItemStack itemStack2 = this.convertInto.copy();
        if (itemStack.isEmpty()) {
            return itemStack2;
        }
        onExtraCreatedRemainder.apply(itemStack2);
        return itemStack;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        UseRemainder useRemainder = (UseRemainder)object;
        return ItemStack.matches(this.convertInto, useRemainder.convertInto);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(this.convertInto);
    }

    @FunctionalInterface
    public static interface OnExtraCreatedRemainder {
        public void apply(ItemStack var1);
    }
}

