/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyBlockState
extends LootItemConditionalFunction {
    public static final MapCodec<CopyBlockState> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyBlockState.commonFields(instance).and(instance.group((App)BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(copyBlockState -> copyBlockState.block), (App)Codec.STRING.listOf().fieldOf("properties").forGetter(copyBlockState -> copyBlockState.properties.stream().map(Property::getName).toList()))).apply((Applicative)instance, CopyBlockState::new));
    private final Holder<Block> block;
    private final Set<Property<?>> properties;

    CopyBlockState(List<LootItemCondition> list, Holder<Block> holder, Set<Property<?>> set) {
        super(list);
        this.block = holder;
        this.properties = set;
    }

    private CopyBlockState(List<LootItemCondition> list, Holder<Block> holder, List<String> list2) {
        this(list, holder, list2.stream().map(holder.value().getStateDefinition()::getProperty).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    public LootItemFunctionType<CopyBlockState> getType() {
        return LootItemFunctions.COPY_STATE;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockState blockState = lootContext.getOptionalParameter(LootContextParams.BLOCK_STATE);
        if (blockState != null) {
            itemStack.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, blockItemStateProperties -> {
                for (Property<?> property : this.properties) {
                    if (!blockState.hasProperty(property)) continue;
                    blockItemStateProperties = blockItemStateProperties.with(property, blockState);
                }
                return blockItemStateProperties;
            });
        }
        return itemStack;
    }

    public static Builder copyState(Block block) {
        return new Builder(block);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final Holder<Block> block;
        private final ImmutableSet.Builder<Property<?>> properties = ImmutableSet.builder();

        Builder(Block block) {
            this.block = block.builtInRegistryHolder();
        }

        public Builder copy(Property<?> property) {
            if (!this.block.value().getStateDefinition().getProperties().contains(property)) {
                throw new IllegalStateException("Property " + String.valueOf(property) + " is not present on block " + String.valueOf(this.block));
            }
            this.properties.add(property);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyBlockState(this.getConditions(), this.block, (Set<Property<?>>)this.properties.build());
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

