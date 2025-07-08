/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ConditionalEffect<T>(T effect, Optional<LootItemCondition> requirements) {
    public static Codec<LootItemCondition> conditionCodec(ContextKeySet contextKeySet) {
        return LootItemCondition.DIRECT_CODEC.validate(lootItemCondition -> {
            ProblemReporter.Collector collector = new ProblemReporter.Collector();
            ValidationContext validationContext = new ValidationContext(collector, contextKeySet);
            lootItemCondition.validate(validationContext);
            if (!collector.isEmpty()) {
                return DataResult.error(() -> "Validation error in enchantment effect condition: " + collector.getReport());
            }
            return DataResult.success((Object)lootItemCondition);
        });
    }

    public static <T> Codec<ConditionalEffect<T>> codec(Codec<T> codec, ContextKeySet contextKeySet) {
        return RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("effect").forGetter(ConditionalEffect::effect), (App)ConditionalEffect.conditionCodec(contextKeySet).optionalFieldOf("requirements").forGetter(ConditionalEffect::requirements)).apply((Applicative)instance, ConditionalEffect::new));
    }

    public boolean matches(LootContext lootContext) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(lootContext);
    }
}

