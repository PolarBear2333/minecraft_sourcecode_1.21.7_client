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
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record ComponentMatches(DataComponentPredicate.Single<?> predicate) implements ConditionalItemModelProperty
{
    public static final MapCodec<ComponentMatches> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DataComponentPredicate.singleCodec("predicate").forGetter(ComponentMatches::predicate)).apply((Applicative)instance, ComponentMatches::new));

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.predicate.predicate().matches(itemStack);
    }

    public MapCodec<ComponentMatches> type() {
        return MAP_CODEC;
    }
}

