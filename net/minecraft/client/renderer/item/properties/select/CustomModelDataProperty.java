/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.PrimitiveCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

public record CustomModelDataProperty(int index) implements SelectItemModelProperty<String>
{
    public static final PrimitiveCodec<String> VALUE_CODEC = Codec.STRING;
    public static final SelectItemModelProperty.Type<CustomModelDataProperty, String> TYPE = SelectItemModelProperty.Type.create(RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", (Object)0).forGetter(CustomModelDataProperty::index)).apply((Applicative)instance, CustomModelDataProperty::new)), VALUE_CODEC);

    @Override
    @Nullable
    public String get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData != null) {
            return customModelData.getString(this.index);
        }
        return null;
    }

    @Override
    public SelectItemModelProperty.Type<CustomModelDataProperty, String> type() {
        return TYPE;
    }

    @Override
    public Codec<String> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext);
    }
}

