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
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface ItemModel {
    public void update(ItemStackRenderState var1, ItemStack var2, ItemModelResolver var3, ItemDisplayContext var4, @Nullable ClientLevel var5, @Nullable LivingEntity var6, int var7);

    public record BakingContext(ModelBaker blockModelBaker, EntityModelSet entityModelSet, ItemModel missingItemModel, @Nullable RegistryContextSwapper contextSwapper) {
    }

    public static interface Unbaked
    extends ResolvableModel {
        public MapCodec<? extends Unbaked> type();

        public ItemModel bake(BakingContext var1);
    }
}

