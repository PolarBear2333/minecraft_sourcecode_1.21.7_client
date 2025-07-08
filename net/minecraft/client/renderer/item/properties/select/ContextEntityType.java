/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record ContextEntityType() implements SelectItemModelProperty<ResourceKey<EntityType<?>>>
{
    public static final Codec<ResourceKey<EntityType<?>>> VALUE_CODEC = ResourceKey.codec(Registries.ENTITY_TYPE);
    public static final SelectItemModelProperty.Type<ContextEntityType, ResourceKey<EntityType<?>>> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit((Object)new ContextEntityType()), VALUE_CODEC);

    @Override
    @Nullable
    public ResourceKey<EntityType<?>> get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return livingEntity == null ? null : livingEntity.getType().builtInRegistryHolder().key();
    }

    @Override
    public SelectItemModelProperty.Type<ContextEntityType, ResourceKey<EntityType<?>>> type() {
        return TYPE;
    }

    @Override
    public Codec<ResourceKey<EntityType<?>>> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext);
    }
}

