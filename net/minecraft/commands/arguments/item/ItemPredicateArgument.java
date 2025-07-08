/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ComponentPredicateParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument
extends ParserBasedArgument<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo:'bar'}");
    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.item.id.invalid", object));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.item.tag.unknown", object));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.item.component.unknown", object));
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("arguments.item.component.malformed", object, object2));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.item.predicate.unknown", object));
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_PREDICATE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("arguments.item.predicate.malformed", object, object2));
    private static final ResourceLocation COUNT_ID = ResourceLocation.withDefaultNamespace("count");
    static final Map<ResourceLocation, ComponentWrapper> PSEUDO_COMPONENTS = Stream.of(new ComponentWrapper(COUNT_ID, itemStack -> true, (Decoder<? extends Predicate<ItemStack>>)MinMaxBounds.Ints.CODEC.map(ints -> itemStack -> ints.matches(itemStack.getCount())))).collect(Collectors.toUnmodifiableMap(ComponentWrapper::id, componentWrapper -> componentWrapper));
    static final Map<ResourceLocation, PredicateWrapper> PSEUDO_PREDICATES = Stream.of(new PredicateWrapper(COUNT_ID, (Decoder<? extends Predicate<ItemStack>>)MinMaxBounds.Ints.CODEC.map(ints -> itemStack -> ints.matches(itemStack.getCount())))).collect(Collectors.toUnmodifiableMap(PredicateWrapper::id, predicateWrapper -> predicateWrapper));

    public ItemPredicateArgument(CommandBuildContext commandBuildContext) {
        super(ComponentPredicateParser.createGrammar(new Context(commandBuildContext)).mapResult(list -> Util.allOf(list)::test));
    }

    public static ItemPredicateArgument itemPredicate(CommandBuildContext commandBuildContext) {
        return new ItemPredicateArgument(commandBuildContext);
    }

    public static Result getItemPredicate(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Result)commandContext.getArgument(string, Result.class);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static class Context
    implements ComponentPredicateParser.Context<Predicate<ItemStack>, ComponentWrapper, PredicateWrapper> {
        private final HolderLookup.Provider registries;
        private final HolderLookup.RegistryLookup<Item> items;
        private final HolderLookup.RegistryLookup<DataComponentType<?>> components;
        private final HolderLookup.RegistryLookup<DataComponentPredicate.Type<?>> predicates;

        Context(HolderLookup.Provider provider) {
            this.registries = provider;
            this.items = provider.lookupOrThrow(Registries.ITEM);
            this.components = provider.lookupOrThrow(Registries.DATA_COMPONENT_TYPE);
            this.predicates = provider.lookupOrThrow(Registries.DATA_COMPONENT_PREDICATE_TYPE);
        }

        @Override
        public Predicate<ItemStack> forElementType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            Holder.Reference<Item> reference = this.items.get(ResourceKey.create(Registries.ITEM, resourceLocation)).orElseThrow(() -> ERROR_UNKNOWN_ITEM.createWithContext(immutableStringReader, (Object)resourceLocation));
            return itemStack -> itemStack.is(reference);
        }

        @Override
        public Predicate<ItemStack> forTagType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            HolderSet holderSet = this.items.get(TagKey.create(Registries.ITEM, resourceLocation)).orElseThrow(() -> ERROR_UNKNOWN_TAG.createWithContext(immutableStringReader, (Object)resourceLocation));
            return itemStack -> itemStack.is(holderSet);
        }

        @Override
        public ComponentWrapper lookupComponentType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            ComponentWrapper componentWrapper = PSEUDO_COMPONENTS.get(resourceLocation);
            if (componentWrapper != null) {
                return componentWrapper;
            }
            DataComponentType dataComponentType = this.components.get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, resourceLocation)).map(Holder::value).orElseThrow(() -> ERROR_UNKNOWN_COMPONENT.createWithContext(immutableStringReader, (Object)resourceLocation));
            return ComponentWrapper.create(immutableStringReader, resourceLocation, dataComponentType);
        }

        @Override
        public Predicate<ItemStack> createComponentTest(ImmutableStringReader immutableStringReader, ComponentWrapper componentWrapper, Dynamic<?> dynamic) throws CommandSyntaxException {
            return componentWrapper.decode(immutableStringReader, RegistryOps.injectRegistryContext(dynamic, this.registries));
        }

        @Override
        public Predicate<ItemStack> createComponentTest(ImmutableStringReader immutableStringReader, ComponentWrapper componentWrapper) {
            return componentWrapper.presenceChecker;
        }

        @Override
        public PredicateWrapper lookupPredicateType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            PredicateWrapper predicateWrapper = PSEUDO_PREDICATES.get(resourceLocation);
            if (predicateWrapper != null) {
                return predicateWrapper;
            }
            return this.predicates.get(ResourceKey.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, resourceLocation)).map(PredicateWrapper::new).orElseThrow(() -> ERROR_UNKNOWN_PREDICATE.createWithContext(immutableStringReader, (Object)resourceLocation));
        }

        @Override
        public Predicate<ItemStack> createPredicateTest(ImmutableStringReader immutableStringReader, PredicateWrapper predicateWrapper, Dynamic<?> dynamic) throws CommandSyntaxException {
            return predicateWrapper.decode(immutableStringReader, RegistryOps.injectRegistryContext(dynamic, this.registries));
        }

        @Override
        public Stream<ResourceLocation> listElementTypes() {
            return this.items.listElementIds().map(ResourceKey::location);
        }

        @Override
        public Stream<ResourceLocation> listTagTypes() {
            return this.items.listTagIds().map(TagKey::location);
        }

        @Override
        public Stream<ResourceLocation> listComponentTypes() {
            return Stream.concat(PSEUDO_COMPONENTS.keySet().stream(), this.components.listElements().filter(reference -> !((DataComponentType)reference.value()).isTransient()).map(reference -> reference.key().location()));
        }

        @Override
        public Stream<ResourceLocation> listPredicateTypes() {
            return Stream.concat(PSEUDO_PREDICATES.keySet().stream(), this.predicates.listElementIds().map(ResourceKey::location));
        }

        @Override
        public Predicate<ItemStack> negate(Predicate<ItemStack> predicate) {
            return predicate.negate();
        }

        @Override
        public Predicate<ItemStack> anyOf(List<Predicate<ItemStack>> list) {
            return Util.anyOf(list);
        }

        @Override
        public /* synthetic */ Object anyOf(List list) {
            return this.anyOf(list);
        }

        @Override
        public /* synthetic */ Object createPredicateTest(ImmutableStringReader immutableStringReader, Object object, Dynamic dynamic) throws CommandSyntaxException {
            return this.createPredicateTest(immutableStringReader, (PredicateWrapper)object, dynamic);
        }

        @Override
        public /* synthetic */ Object lookupPredicateType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            return this.lookupPredicateType(immutableStringReader, resourceLocation);
        }

        @Override
        public /* synthetic */ Object lookupComponentType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            return this.lookupComponentType(immutableStringReader, resourceLocation);
        }

        @Override
        public /* synthetic */ Object forTagType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            return this.forTagType(immutableStringReader, resourceLocation);
        }

        @Override
        public /* synthetic */ Object forElementType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException {
            return this.forElementType(immutableStringReader, resourceLocation);
        }
    }

    public static interface Result
    extends Predicate<ItemStack> {
    }

    static final class ComponentWrapper
    extends Record {
        private final ResourceLocation id;
        final Predicate<ItemStack> presenceChecker;
        private final Decoder<? extends Predicate<ItemStack>> valueChecker;

        ComponentWrapper(ResourceLocation resourceLocation, Predicate<ItemStack> predicate, Decoder<? extends Predicate<ItemStack>> decoder) {
            this.id = resourceLocation;
            this.presenceChecker = predicate;
            this.valueChecker = decoder;
        }

        public static <T> ComponentWrapper create(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation, DataComponentType<T> dataComponentType) throws CommandSyntaxException {
            Codec<T> codec = dataComponentType.codec();
            if (codec == null) {
                throw ERROR_UNKNOWN_COMPONENT.createWithContext(immutableStringReader, (Object)resourceLocation);
            }
            return new ComponentWrapper(resourceLocation, itemStack -> itemStack.has(dataComponentType), (Decoder<? extends Predicate<ItemStack>>)codec.map(object -> itemStack -> {
                Object t = itemStack.get(dataComponentType);
                return Objects.equals(object, t);
            }));
        }

        public Predicate<ItemStack> decode(ImmutableStringReader immutableStringReader, Dynamic<?> dynamic) throws CommandSyntaxException {
            DataResult dataResult = this.valueChecker.parse(dynamic);
            return (Predicate)dataResult.getOrThrow(string -> ERROR_MALFORMED_COMPONENT.createWithContext(immutableStringReader, (Object)this.id.toString(), string));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ComponentWrapper.class, "id;presenceChecker;valueChecker", "id", "presenceChecker", "valueChecker"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ComponentWrapper.class, "id;presenceChecker;valueChecker", "id", "presenceChecker", "valueChecker"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ComponentWrapper.class, "id;presenceChecker;valueChecker", "id", "presenceChecker", "valueChecker"}, this, object);
        }

        public ResourceLocation id() {
            return this.id;
        }

        public Predicate<ItemStack> presenceChecker() {
            return this.presenceChecker;
        }

        public Decoder<? extends Predicate<ItemStack>> valueChecker() {
            return this.valueChecker;
        }
    }

    record PredicateWrapper(ResourceLocation id, Decoder<? extends Predicate<ItemStack>> type) {
        public PredicateWrapper(Holder.Reference<DataComponentPredicate.Type<?>> reference) {
            this(reference.key().location(), (Decoder<? extends Predicate<ItemStack>>)reference.value().codec().map(dataComponentPredicate -> dataComponentPredicate::matches));
        }

        public Predicate<ItemStack> decode(ImmutableStringReader immutableStringReader, Dynamic<?> dynamic) throws CommandSyntaxException {
            DataResult dataResult = this.type.parse(dynamic);
            return (Predicate)dataResult.getOrThrow(string -> ERROR_MALFORMED_PREDICATE.createWithContext(immutableStringReader, (Object)this.id.toString(), string));
        }
    }
}

