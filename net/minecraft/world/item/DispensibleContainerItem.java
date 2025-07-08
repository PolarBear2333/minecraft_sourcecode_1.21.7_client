/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface DispensibleContainerItem {
    default public void checkExtraContent(@Nullable LivingEntity livingEntity, Level level, ItemStack itemStack, BlockPos blockPos) {
    }

    public boolean emptyContents(@Nullable LivingEntity var1, Level var2, BlockPos var3, @Nullable BlockHitResult var4);
}

