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
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ResourceKeyArgument<T>
implements ArgumentType<ResourceKey<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.place.feature.invalid", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.place.structure.invalid", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.place.jigsaw.invalid", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_RECIPE = new DynamicCommandExceptionType(object -> Component.translatableEscape("recipe.notFound", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_ADVANCEMENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("advancement.advancementNotFound", object));
    final ResourceKey<? extends Registry<T>> registryKey;

    public ResourceKeyArgument(ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
    }

    public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceKeyArgument<T>(resourceKey);
    }

    public static <T> ResourceKey<T> getRegistryKey(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey resourceKey2 = (ResourceKey)commandContext.getArgument(string, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourceKey2.cast(resourceKey);
        return optional.orElseThrow(() -> dynamicCommandExceptionType.create((Object)resourceKey2.location()));
    }

    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> commandContext, ResourceKey<? extends Registry<T>> resourceKey) {
        return ((CommandSourceStack)commandContext.getSource()).getServer().registryAccess().lookupOrThrow(resourceKey);
    }

    private static <T> Holder.Reference<T> resolveKey(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey resourceKey2 = ResourceKeyArgument.getRegistryKey(commandContext, string, resourceKey, dynamicCommandExceptionType);
        return (Holder.Reference)ResourceKeyArgument.getRegistry(commandContext, resourceKey).get(resourceKey2).orElseThrow(() -> dynamicCommandExceptionType.create((Object)resourceKey2.location()));
    }

    public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceKeyArgument.resolveKey(commandContext, string, Registries.CONFIGURED_FEATURE, ERROR_INVALID_FEATURE);
    }

    public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceKeyArgument.resolveKey(commandContext, string, Registries.STRUCTURE, ERROR_INVALID_STRUCTURE);
    }

    public static Holder.Reference<StructureTemplatePool> getStructureTemplatePool(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceKeyArgument.resolveKey(commandContext, string, Registries.TEMPLATE_POOL, ERROR_INVALID_TEMPLATE_POOL);
    }

    public static RecipeHolder<?> getRecipe(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        RecipeManager recipeManager = ((CommandSourceStack)commandContext.getSource()).getServer().getRecipeManager();
        ResourceKey<Recipe<?>> resourceKey = ResourceKeyArgument.getRegistryKey(commandContext, string, Registries.RECIPE, ERROR_INVALID_RECIPE);
        return recipeManager.byKey(resourceKey).orElseThrow(() -> ERROR_INVALID_RECIPE.create((Object)resourceKey.location()));
    }

    public static AdvancementHolder getAdvancement(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceKey resourceKey = ResourceKeyArgument.getRegistryKey(commandContext, string, Registries.ADVANCEMENT, ERROR_INVALID_ADVANCEMENT);
        AdvancementHolder advancementHolder = ((CommandSourceStack)commandContext.getSource()).getServer().getAdvancements().get(resourceKey.location());
        if (advancementHolder == null) {
            throw ERROR_INVALID_ADVANCEMENT.create((Object)resourceKey.location());
        }
        return advancementHolder;
    }

    public ResourceKey<T> parse(StringReader stringReader) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        return ResourceKey.create(this.registryKey, resourceLocation);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.listSuggestions(commandContext, suggestionsBuilder, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceKeyArgument<T>, Template> {
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
        public Template unpack(ResourceKeyArgument<T> resourceKeyArgument) {
            return new Template(resourceKeyArgument.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> resourceKey) {
                this.registryKey = resourceKey;
            }

            @Override
            public ResourceKeyArgument<T> instantiate(CommandBuildContext commandBuildContext) {
                return new ResourceKeyArgument(this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

