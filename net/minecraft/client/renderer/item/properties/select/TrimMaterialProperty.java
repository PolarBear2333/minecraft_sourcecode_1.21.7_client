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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public record TrimMaterialProperty() implements SelectItemModelProperty<ResourceKey<TrimMaterial>>
{
    public static final Codec<ResourceKey<TrimMaterial>> VALUE_CODEC = ResourceKey.codec(Registries.TRIM_MATERIAL);
    public static final SelectItemModelProperty.Type<TrimMaterialProperty, ResourceKey<TrimMaterial>> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit((Object)new TrimMaterialProperty()), VALUE_CODEC);

    @Override
    @Nullable
    public ResourceKey<TrimMaterial> get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
        if (armorTrim == null) {
            return null;
        }
        return armorTrim.material().unwrapKey().orElse(null);
    }

    @Override
    public SelectItemModelProperty.Type<TrimMaterialProperty, ResourceKey<TrimMaterial>> type() {
        return TYPE;
    }

    @Override
    public Codec<ResourceKey<TrimMaterial>> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext);
    }
}

