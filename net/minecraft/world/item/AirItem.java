/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class AirItem
extends Item {
    public AirItem(Block block, Item.Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return this.getName();
    }
}

