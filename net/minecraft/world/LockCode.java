/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public record LockCode(ItemPredicate predicate) {
    public static final LockCode NO_LOCK = new LockCode(ItemPredicate.Builder.item().build());
    public static final Codec<LockCode> CODEC = ItemPredicate.CODEC.xmap(LockCode::new, LockCode::predicate);
    public static final String TAG_LOCK = "lock";

    public boolean unlocksWith(ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }

    public void addToTag(ValueOutput valueOutput) {
        if (this != NO_LOCK) {
            valueOutput.store(TAG_LOCK, CODEC, this);
        }
    }

    public static LockCode fromTag(ValueInput valueInput) {
        return valueInput.read(TAG_LOCK, CODEC).orElse(NO_LOCK);
    }
}

