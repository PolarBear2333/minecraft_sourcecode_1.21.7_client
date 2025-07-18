/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityArgument
implements ArgumentType<EntitySelector> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.toomany"));
    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = new SimpleCommandExceptionType((Message)Component.translatable("argument.player.toomany"));
    public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = new SimpleCommandExceptionType((Message)Component.translatable("argument.player.entities"));
    public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.notfound.entity"));
    public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.notfound.player"));
    public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.selector.not_allowed"));
    final boolean single;
    final boolean playersOnly;

    protected EntityArgument(boolean bl, boolean bl2) {
        this.single = bl;
        this.playersOnly = bl2;
    }

    public static EntityArgument entity() {
        return new EntityArgument(true, false);
    }

    public static Entity getEntity(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((EntitySelector)commandContext.getArgument(string, EntitySelector.class)).findSingleEntity((CommandSourceStack)commandContext.getSource());
    }

    public static EntityArgument entities() {
        return new EntityArgument(false, false);
    }

    public static Collection<? extends Entity> getEntities(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Collection<? extends Entity> collection = EntityArgument.getOptionalEntities(commandContext, string);
        if (collection.isEmpty()) {
            throw NO_ENTITIES_FOUND.create();
        }
        return collection;
    }

    public static Collection<? extends Entity> getOptionalEntities(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((EntitySelector)commandContext.getArgument(string, EntitySelector.class)).findEntities((CommandSourceStack)commandContext.getSource());
    }

    public static Collection<ServerPlayer> getOptionalPlayers(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((EntitySelector)commandContext.getArgument(string, EntitySelector.class)).findPlayers((CommandSourceStack)commandContext.getSource());
    }

    public static EntityArgument player() {
        return new EntityArgument(true, true);
    }

    public static ServerPlayer getPlayer(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((EntitySelector)commandContext.getArgument(string, EntitySelector.class)).findSinglePlayer((CommandSourceStack)commandContext.getSource());
    }

    public static EntityArgument players() {
        return new EntityArgument(false, true);
    }

    public static Collection<ServerPlayer> getPlayers(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        List<ServerPlayer> list = ((EntitySelector)commandContext.getArgument(string, EntitySelector.class)).findPlayers((CommandSourceStack)commandContext.getSource());
        if (list.isEmpty()) {
            throw NO_PLAYERS_FOUND.create();
        }
        return list;
    }

    public EntitySelector parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader, true);
    }

    public <S> EntitySelector parse(StringReader stringReader, S s) throws CommandSyntaxException {
        return this.parse(stringReader, EntitySelectorParser.allowSelectors(s));
    }

    private EntitySelector parse(StringReader stringReader, boolean bl) throws CommandSyntaxException {
        boolean bl2 = false;
        EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader, bl);
        EntitySelector entitySelector = entitySelectorParser.parse();
        if (entitySelector.getMaxResults() > 1 && this.single) {
            if (this.playersOnly) {
                stringReader.setCursor(0);
                throw ERROR_NOT_SINGLE_PLAYER.createWithContext((ImmutableStringReader)stringReader);
            }
            stringReader.setCursor(0);
            throw ERROR_NOT_SINGLE_ENTITY.createWithContext((ImmutableStringReader)stringReader);
        }
        if (entitySelector.includesEntities() && this.playersOnly && !entitySelector.isSelfSelector()) {
            stringReader.setCursor(0);
            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext((ImmutableStringReader)stringReader);
        }
        return entitySelector;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder2) {
        Object object = commandContext.getSource();
        if (object instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)object;
            object = new StringReader(suggestionsBuilder2.getInput());
            object.setCursor(suggestionsBuilder2.getStart());
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser((StringReader)object, EntitySelectorParser.allowSelectors(sharedSuggestionProvider));
            try {
                entitySelectorParser.parse();
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
            return entitySelectorParser.fillSuggestions(suggestionsBuilder2, suggestionsBuilder -> {
                Collection<String> collection = sharedSuggestionProvider.getOnlinePlayerNames();
                Collection<String> collection2 = this.playersOnly ? collection : Iterables.concat(collection, sharedSuggestionProvider.getSelectedEntities());
                SharedSuggestionProvider.suggest(collection2, suggestionsBuilder);
            });
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader, Object object) throws CommandSyntaxException {
        return this.parse(stringReader, object);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class Info
    implements ArgumentTypeInfo<EntityArgument, Template> {
        private static final byte FLAG_SINGLE = 1;
        private static final byte FLAG_PLAYERS_ONLY = 2;

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            int n = 0;
            if (template.single) {
                n |= 1;
            }
            if (template.playersOnly) {
                n |= 2;
            }
            friendlyByteBuf.writeByte(n);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            byte by = friendlyByteBuf.readByte();
            return new Template((by & 1) != 0, (by & 2) != 0);
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("amount", template.single ? "single" : "multiple");
            jsonObject.addProperty("type", template.playersOnly ? "players" : "entities");
        }

        @Override
        public Template unpack(EntityArgument entityArgument) {
            return new Template(entityArgument.single, entityArgument.playersOnly);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<EntityArgument> {
            final boolean single;
            final boolean playersOnly;

            Template(boolean bl, boolean bl2) {
                this.single = bl;
                this.playersOnly = bl2;
            }

            @Override
            public EntityArgument instantiate(CommandBuildContext commandBuildContext) {
                return new EntityArgument(this.single, this.playersOnly);
            }

            @Override
            public ArgumentTypeInfo<EntityArgument, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

