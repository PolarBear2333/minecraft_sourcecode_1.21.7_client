/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultiset
 *  com.google.common.collect.Multiset
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.select;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface SelectItemModelProperty<T> {
    @Nullable
    public T get(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4, ItemDisplayContext var5);

    public Codec<T> valueCodec();

    public Type<? extends SelectItemModelProperty<T>, T> type();

    public record Type<P extends SelectItemModelProperty<T>, T>(MapCodec<SelectItemModel.UnbakedSwitch<P, T>> switchCodec) {
        public static <P extends SelectItemModelProperty<T>, T> Type<P, T> create(MapCodec<P> mapCodec, Codec<T> codec) {
            MapCodec mapCodec2 = RecordCodecBuilder.mapCodec(instance -> instance.group((App)mapCodec.forGetter(SelectItemModel.UnbakedSwitch::property), (App)Type.createCasesFieldCodec(codec).forGetter(SelectItemModel.UnbakedSwitch::cases)).apply((Applicative)instance, SelectItemModel.UnbakedSwitch::new));
            return new Type<P, T>(mapCodec2);
        }

        public static <T> MapCodec<List<SelectItemModel.SwitchCase<T>>> createCasesFieldCodec(Codec<T> codec) {
            return SelectItemModel.SwitchCase.codec(codec).listOf().validate(Type::validateCases).fieldOf("cases");
        }

        private static <T> DataResult<List<SelectItemModel.SwitchCase<T>>> validateCases(List<SelectItemModel.SwitchCase<T>> list) {
            if (list.isEmpty()) {
                return DataResult.error(() -> "Empty case list");
            }
            HashMultiset hashMultiset = HashMultiset.create();
            for (SelectItemModel.SwitchCase<T> switchCase : list) {
                hashMultiset.addAll(switchCase.values());
            }
            if (hashMultiset.size() != hashMultiset.entrySet().size()) {
                return DataResult.error(() -> Type.lambda$validateCases$4((Multiset)hashMultiset));
            }
            return DataResult.success(list);
        }

        private static /* synthetic */ String lambda$validateCases$4(Multiset multiset) {
            return "Duplicate case conditions: " + multiset.entrySet().stream().filter(entry -> entry.getCount() > 1).map(entry -> entry.getElement().toString()).collect(Collectors.joining(", "));
        }
    }
}

