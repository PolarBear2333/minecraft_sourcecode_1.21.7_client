/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CompositeModel
implements ItemModel {
    private final List<ItemModel> models;

    public CompositeModel(List<ItemModel> list) {
        this.models = list;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        itemStackRenderState.appendModelIdentityElement(this);
        itemStackRenderState.ensureCapacity(this.models.size());
        for (ItemModel itemModel : this.models) {
            itemModel.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, livingEntity, n);
        }
    }

    public record Unbaked(List<ItemModel.Unbaked> models) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ItemModels.CODEC.listOf().fieldOf("models").forGetter(Unbaked::models)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            for (ItemModel.Unbaked unbaked : this.models) {
                unbaked.resolveDependencies(resolver);
            }
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            return new CompositeModel(this.models.stream().map(unbaked -> unbaked.bake(bakingContext)).toList());
        }
    }
}

