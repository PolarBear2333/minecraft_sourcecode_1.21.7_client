/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ContextAwarePredicate {
    public static final Codec<ContextAwarePredicate> CODEC = LootItemCondition.DIRECT_CODEC.listOf().xmap(ContextAwarePredicate::new, contextAwarePredicate -> contextAwarePredicate.conditions);
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> list) {
        this.conditions = list;
        this.compositePredicates = Util.allOf(list);
    }

    public static ContextAwarePredicate create(LootItemCondition ... lootItemConditionArray) {
        return new ContextAwarePredicate(List.of(lootItemConditionArray));
    }

    public boolean matches(LootContext lootContext) {
        return this.compositePredicates.test(lootContext);
    }

    public void validate(ValidationContext validationContext) {
        for (int i = 0; i < this.conditions.size(); ++i) {
            LootItemCondition lootItemCondition = this.conditions.get(i);
            lootItemCondition.validate(validationContext.forChild(new ProblemReporter.IndexedPathElement(i)));
        }
    }
}

