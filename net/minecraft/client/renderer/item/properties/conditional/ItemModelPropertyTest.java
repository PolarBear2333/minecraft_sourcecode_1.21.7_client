/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.conditional;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemModelPropertyTest {
    public boolean get(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4, ItemDisplayContext var5);
}

