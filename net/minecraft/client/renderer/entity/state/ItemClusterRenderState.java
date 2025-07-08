/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemClusterRenderState
extends EntityRenderState {
    public final ItemStackRenderState item = new ItemStackRenderState();
    public int count;
    public int seed;

    public void extractItemGroupRenderState(Entity entity, ItemStack itemStack, ItemModelResolver itemModelResolver) {
        itemModelResolver.updateForNonLiving(this.item, itemStack, ItemDisplayContext.GROUND, entity);
        this.count = ItemClusterRenderState.getRenderedAmount(itemStack.getCount());
        this.seed = ItemClusterRenderState.getSeedForItemStack(itemStack);
    }

    public static int getSeedForItemStack(ItemStack itemStack) {
        return itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
    }

    public static int getRenderedAmount(int n) {
        if (n <= 1) {
            return 1;
        }
        if (n <= 16) {
            return 2;
        }
        if (n <= 32) {
            return 3;
        }
        if (n <= 48) {
            return 4;
        }
        return 5;
    }
}

