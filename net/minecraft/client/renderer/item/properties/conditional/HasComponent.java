/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record HasComponent(DataComponentType<?> componentType, boolean ignoreDefault) implements ConditionalItemModelProperty
{
    public static final MapCodec<HasComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("component").forGetter(HasComponent::componentType), (App)Codec.BOOL.optionalFieldOf("ignore_default", (Object)false).forGetter(HasComponent::ignoreDefault)).apply((Applicative)instance, HasComponent::new));

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.ignoreDefault ? itemStack.hasNonDefault(this.componentType) : itemStack.has(this.componentType);
    }

    public MapCodec<HasComponent> type() {
        return MAP_CODEC;
    }
}

