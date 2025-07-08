/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T>
implements ArgumentType<Holder.Reference<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType(object -> Component.translatableEscape("entity.not_summonable", object));
    public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("argument.resource.not_found", object, object2));
    public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType((object, object2, object3) -> Component.translatableEscape("argument.resource.invalid_type", object, object2, object3));
    final ResourceKey<? extends Registry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    public ResourceArgument(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
        this.registryLookup = commandBuildContext.lookupOrThrow(resourceKey);
    }

    public static <T> ResourceArgument<T> resource(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceArgument<T>(commandBuildContext, resourceKey);
    }

    public static <T> Holder.Reference<T> getResource(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey) throws CommandSyntaxException {
        Holder.Reference reference = (Holder.Reference)commandContext.getArgument(string, Holder.Reference.class);
        ResourceKey resourceKey2 = reference.key();
        if (resourceKey2.isFor(resourceKey)) {
            return reference;
        }
        throw ERROR_INVALID_RESOURCE_TYPE.create((Object)resourceKey2.location(), (Object)resourceKey2.registry(), (Object)resourceKey.location());
    }

    public static Holder.Reference<Attribute> getAttribute(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceArgument.getResource(commandContext, string, Registries.ATTRIBUTE);
    }

    public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceArgument.getResource(commandContext, string, Registries.CONFIGURED_FEATURE);
    }

    public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceArgument.getResource(commandContext, string, Registries.STRUCTURE);
    }

    public static Holder.Reference<EntityType<?>> getEntityType(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceArgument.getResource(commandContext, string, Registries.ENTITY_TYPE);
    }

    public static Holder.Reference<EntityType<?>> getSummonableEntityType(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Holder.Reference<EntityType<?>> reference = ResourceArgument.getResource(commandContext, string, Registries.ENTITY_TYPE);
        if (!((EntityType)reference.value()).canSummon()) {
            throw ERROR_NOT_SUMMONABLE_ENTITY.create((Object)reference.key().location().toString());
        }
        return reference;
    }

    public static Holder.Reference<MobEffect> getMobEffect(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceArgument.getResource(commandContext, string, Registries.MOB_EFFECT);
    }

    public static Holder.Reference<Enchantment> getEnchantment(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceArgument.getResource(commandContext, string, Registries.ENCHANTMENT);
    }

    public Holder.Reference<T> parse(StringReader stringReader) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        ResourceKey resourceKey = ResourceKey.create(this.registryKey, resourceLocation);
        return this.registryLookup.get(resourceKey).orElseThrow(() -> ERROR_UNKNOWN_RESOURCE.createWithContext((ImmutableStringReader)stringReader, (Object)resourceLocation, (Object)this.registryKey.location()));
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
    implements ArgumentTypeInfo<ResourceArgument<T>, Template> {
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
        public Template unpack(ResourceArgument<T> resourceArgument) {
            return new Template(resourceArgument.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> resourceKey) {
                this.registryKey = resourceKey;
            }

            @Override
            public ResourceArgument<T> instantiate(CommandBuildContext commandBuildContext) {
                return new ResourceArgument(commandBuildContext, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

