/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.Item;

public interface ItemModelOutput {
    public void accept(Item var1, ItemModel.Unbaked var2);

    public void copy(Item var1, Item var2);
}

