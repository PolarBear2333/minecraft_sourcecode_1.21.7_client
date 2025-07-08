/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Keyable
 */
package net.minecraft.client.renderer.block.model.multipart;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Keyable;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.client.renderer.block.model.multipart.CombinedCondition;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;

@FunctionalInterface
public interface Condition {
    public static final Codec<Condition> CODEC = Codec.recursive((String)"condition", codec -> {
        Codec codec2 = Codec.simpleMap(CombinedCondition.Operation.CODEC, (Codec)codec.listOf(), (Keyable)StringRepresentable.keys(CombinedCondition.Operation.values())).codec().comapFlatMap(map -> {
            if (map.size() != 1) {
                return DataResult.error(() -> "Invalid map size for combiner condition, expected exactly one element");
            }
            Map.Entry entry = map.entrySet().iterator().next();
            return DataResult.success((Object)new CombinedCondition((CombinedCondition.Operation)entry.getKey(), (List)entry.getValue()));
        }, combinedCondition -> Map.of(combinedCondition.operation(), combinedCondition.terms()));
        return Codec.either((Codec)codec2, KeyValueCondition.CODEC).flatComapMap(either -> (Condition)either.map(combinedCondition -> combinedCondition, keyValueCondition -> keyValueCondition), condition -> {
            Condition condition2 = condition;
            Objects.requireNonNull(condition2);
            Condition condition3 = condition2;
            int n = 0;
            DataResult dataResult = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{CombinedCondition.class, KeyValueCondition.class}, (Object)condition3, n)) {
                case 0 -> {
                    CombinedCondition var4_3 = (CombinedCondition)condition3;
                    yield DataResult.success((Object)Either.left((Object)var4_3));
                }
                case 1 -> {
                    KeyValueCondition var5_4 = (KeyValueCondition)condition3;
                    yield DataResult.success((Object)Either.right((Object)var5_4));
                }
                default -> DataResult.error(() -> "Unrecognized condition");
            };
            return dataResult;
        });
    });

    public <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> var1);
}

