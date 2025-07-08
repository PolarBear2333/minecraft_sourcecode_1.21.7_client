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
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.ItemModelPropertyTest;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ConditionalItemModel
implements ItemModel {
    private final ItemModelPropertyTest property;
    private final ItemModel onTrue;
    private final ItemModel onFalse;

    public ConditionalItemModel(ItemModelPropertyTest itemModelPropertyTest, ItemModel itemModel, ItemModel itemModel2) {
        this.property = itemModelPropertyTest;
        this.onTrue = itemModel;
        this.onFalse = itemModel2;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        itemStackRenderState.appendModelIdentityElement(this);
        (this.property.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext) ? this.onTrue : this.onFalse).update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, livingEntity, n);
    }

    public record Unbaked(ConditionalItemModelProperty property, ItemModel.Unbaked onTrue, ItemModel.Unbaked onFalse) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ConditionalItemModelProperties.MAP_CODEC.forGetter(Unbaked::property), (App)ItemModels.CODEC.fieldOf("on_true").forGetter(Unbaked::onTrue), (App)ItemModels.CODEC.fieldOf("on_false").forGetter(Unbaked::onFalse)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            return new ConditionalItemModel(this.adaptProperty(this.property, bakingContext.contextSwapper()), this.onTrue.bake(bakingContext), this.onFalse.bake(bakingContext));
        }

        private ItemModelPropertyTest adaptProperty(ConditionalItemModelProperty conditionalItemModelProperty, @Nullable RegistryContextSwapper registryContextSwapper) {
            if (registryContextSwapper == null) {
                return conditionalItemModelProperty;
            }
            CacheSlot<ClientLevel, ItemModelPropertyTest> cacheSlot = new CacheSlot<ClientLevel, ItemModelPropertyTest>(clientLevel -> Unbaked.swapContext(conditionalItemModelProperty, registryContextSwapper, clientLevel));
            return (itemStack, clientLevel, livingEntity, n, itemDisplayContext) -> {
                ConditionalItemModelProperty conditionalItemModelProperty2 = clientLevel == null ? conditionalItemModelProperty : (ItemModelPropertyTest)cacheSlot.compute(clientLevel);
                return conditionalItemModelProperty2.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext);
            };
        }

        private static <T extends ConditionalItemModelProperty> T swapContext(T t, RegistryContextSwapper registryContextSwapper, ClientLevel clientLevel) {
            return registryContextSwapper.swapTo(t.type().codec(), t, clientLevel.registryAccess()).result().orElse(t);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.onTrue.resolveDependencies(resolver);
            this.onFalse.resolveDependencies(resolver);
        }
    }
}

