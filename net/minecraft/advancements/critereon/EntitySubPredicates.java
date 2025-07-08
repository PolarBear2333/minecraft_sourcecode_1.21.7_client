/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.advancements.critereon.LightningBoltPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.RaiderPredicate;
import net.minecraft.advancements.critereon.SheepPredicate;
import net.minecraft.advancements.critereon.SlimePredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntitySubPredicates {
    public static final MapCodec<LightningBoltPredicate> LIGHTNING = EntitySubPredicates.register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<FishingHookPredicate> FISHING_HOOK = EntitySubPredicates.register("fishing_hook", FishingHookPredicate.CODEC);
    public static final MapCodec<PlayerPredicate> PLAYER = EntitySubPredicates.register("player", PlayerPredicate.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = EntitySubPredicates.register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = EntitySubPredicates.register("raider", RaiderPredicate.CODEC);
    public static final MapCodec<SheepPredicate> SHEEP = EntitySubPredicates.register("sheep", SheepPredicate.CODEC);

    private static <T extends EntitySubPredicate> MapCodec<T> register(String string, MapCodec<T> mapCodec) {
        return Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, string, mapCodec);
    }

    public static MapCodec<? extends EntitySubPredicate> bootstrap(Registry<MapCodec<? extends EntitySubPredicate>> registry) {
        return LIGHTNING;
    }
}

