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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EmptyModel
implements ItemModel {
    public static final ItemModel INSTANCE = new EmptyModel();

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        itemStackRenderState.appendModelIdentityElement(this);
    }

    public record Unbaked() implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            return INSTANCE;
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

