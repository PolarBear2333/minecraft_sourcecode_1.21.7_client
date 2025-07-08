/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public record DamagePredicate(MinMaxBounds.Ints durability, MinMaxBounds.Ints damage) implements DataComponentPredicate
{
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", (Object)MinMaxBounds.Ints.ANY).forGetter(DamagePredicate::durability), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("damage", (Object)MinMaxBounds.Ints.ANY).forGetter(DamagePredicate::damage)).apply((Applicative)instance, DamagePredicate::new));

    @Override
    public boolean matches(DataComponentGetter dataComponentGetter) {
        Integer n = dataComponentGetter.get(DataComponents.DAMAGE);
        if (n == null) {
            return false;
        }
        int n2 = dataComponentGetter.getOrDefault(DataComponents.MAX_DAMAGE, 0);
        if (!this.durability.matches(n2 - n)) {
            return false;
        }
        return this.damage.matches(n);
    }

    public static DamagePredicate durability(MinMaxBounds.Ints ints) {
        return new DamagePredicate(ints, MinMaxBounds.Ints.ANY);
    }
}

