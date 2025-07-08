/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BundleSelectedItemSpecialRenderer
implements ItemModel {
    static final ItemModel INSTANCE = new BundleSelectedItemSpecialRenderer();

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        itemStackRenderState.appendModelIdentityElement(this);
        ItemStack itemStack2 = BundleItem.getSelectedItemStack(itemStack);
        if (!itemStack2.isEmpty()) {
            itemModelResolver.appendItemLayers(itemStackRenderState, itemStack2, itemDisplayContext, clientLevel, livingEntity, n);
        }
    }

    public record Unbaked() implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit((Object)new Unbaked());

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            return INSTANCE;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }
    }
}

