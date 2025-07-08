/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetFireworkExplosionFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetFireworkExplosionFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetFireworkExplosionFunction.commonFields(instance).and(instance.group((App)FireworkExplosion.Shape.CODEC.optionalFieldOf("shape").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.shape), (App)FireworkExplosion.COLOR_LIST_CODEC.optionalFieldOf("colors").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.colors), (App)FireworkExplosion.COLOR_LIST_CODEC.optionalFieldOf("fade_colors").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.fadeColors), (App)Codec.BOOL.optionalFieldOf("trail").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.trail), (App)Codec.BOOL.optionalFieldOf("twinkle").forGetter(setFireworkExplosionFunction -> setFireworkExplosionFunction.twinkle))).apply((Applicative)instance, SetFireworkExplosionFunction::new));
    public static final FireworkExplosion DEFAULT_VALUE = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(), IntList.of(), false, false);
    final Optional<FireworkExplosion.Shape> shape;
    final Optional<IntList> colors;
    final Optional<IntList> fadeColors;
    final Optional<Boolean> trail;
    final Optional<Boolean> twinkle;

    public SetFireworkExplosionFunction(List<LootItemCondition> list, Optional<FireworkExplosion.Shape> optional, Optional<IntList> optional2, Optional<IntList> optional3, Optional<Boolean> optional4, Optional<Boolean> optional5) {
        super(list);
        this.shape = optional;
        this.colors = optional2;
        this.fadeColors = optional3;
        this.trail = optional4;
        this.twinkle = optional5;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.FIREWORK_EXPLOSION, DEFAULT_VALUE, this::apply);
        return itemStack;
    }

    private FireworkExplosion apply(FireworkExplosion fireworkExplosion) {
        return new FireworkExplosion(this.shape.orElseGet(fireworkExplosion::shape), this.colors.orElseGet(fireworkExplosion::colors), this.fadeColors.orElseGet(fireworkExplosion::fadeColors), this.trail.orElseGet(fireworkExplosion::hasTrail), this.twinkle.orElseGet(fireworkExplosion::hasTwinkle));
    }

    public LootItemFunctionType<SetFireworkExplosionFunction> getType() {
        return LootItemFunctions.SET_FIREWORK_EXPLOSION;
    }
}

