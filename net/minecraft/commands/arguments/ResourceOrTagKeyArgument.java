/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.datafixers.util.Either
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagKeyArgument<T>
implements ArgumentType<Result<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceOrTagKeyArgument(ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
    }

    public static <T> ResourceOrTagKeyArgument<T> resourceOrTagKey(ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceOrTagKeyArgument<T>(resourceKey);
    }

    public static <T> Result<T> getResourceOrTagKey(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        Result result = (Result)commandContext.getArgument(string, Result.class);
        Optional<Result<T>> optional = result.cast(resourceKey);
        return optional.orElseThrow(() -> dynamicCommandExceptionType.create((Object)result));
    }

    public Result<T> parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int n = stringReader.getCursor();
            try {
                stringReader.skip();
                ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
                return new TagResult(TagKey.create(this.registryKey, resourceLocation));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                stringReader.setCursor(n);
                throw commandSyntaxException;
            }
        }
        ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        return new ResourceResult(ResourceKey.create(this.registryKey, resourceLocation));
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.listSuggestions(commandContext, suggestionsBuilder, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result<T>
    extends Predicate<Holder<T>> {
        public Either<ResourceKey<T>, TagKey<T>> unwrap();

        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

        public String asPrintable();
    }

    record TagResult<T>(TagKey<T> key) implements Result<T>
    {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.right(this.key);
        }

        @Override
        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.key.cast(resourceKey).map(TagResult::new);
        }

        @Override
        public boolean test(Holder<T> holder) {
            return holder.is(this.key);
        }

        @Override
        public String asPrintable() {
            return "#" + String.valueOf(this.key.location());
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Holder)object);
        }
    }

    record ResourceResult<T>(ResourceKey<T> key) implements Result<T>
    {
        @Override
        public Either<ResourceKey<T>, TagKey<T>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public <E> Optional<Result<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
            return this.key.cast(resourceKey).map(ResourceResult::new);
        }

        @Override
        public boolean test(Holder<T> holder) {
            return holder.is(this.key);
        }

        @Override
        public String asPrintable() {
            return this.key.location().toString();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Holder)object);
        }
    }

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeResourceKey(template.registryKey);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return new Template(friendlyByteBuf.readRegistryKey());
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("registry", template.registryKey.location().toString());
        }

        @Override
        public Template unpack(ResourceOrTagKeyArgument<T> resourceOrTagKeyArgument) {
            return new Template(resourceOrTagKeyArgument.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceOrTagKeyArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> resourceKey) {
                this.registryKey = resourceKey;
            }

            @Override
            public ResourceOrTagKeyArgument<T> instantiate(CommandBuildContext commandBuildContext) {
                return new ResourceOrTagKeyArgument(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

