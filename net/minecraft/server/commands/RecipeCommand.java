/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeCommand {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.recipe.give.failed"));
    private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.recipe.take.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("recipe").requires(Commands.hasPermission(2))).then(Commands.literal("give").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceKeyArgument.key(Registries.RECIPE)).executes(commandContext -> RecipeCommand.giveRecipes((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), Collections.singleton(ResourceKeyArgument.getRecipe((CommandContext<CommandSourceStack>)commandContext, "recipe")))))).then(Commands.literal("*").executes(commandContext -> RecipeCommand.giveRecipes((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getServer().getRecipeManager().getRecipes())))))).then(Commands.literal("take").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceKeyArgument.key(Registries.RECIPE)).executes(commandContext -> RecipeCommand.takeRecipes((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), Collections.singleton(ResourceKeyArgument.getRecipe((CommandContext<CommandSourceStack>)commandContext, "recipe")))))).then(Commands.literal("*").executes(commandContext -> RecipeCommand.takeRecipes((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "targets"), ((CommandSourceStack)commandContext.getSource()).getServer().getRecipeManager().getRecipes()))))));
    }

    private static int giveRecipes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Collection<RecipeHolder<?>> collection2) throws CommandSyntaxException {
        int n = 0;
        for (ServerPlayer serverPlayer : collection) {
            n += serverPlayer.awardRecipes(collection2);
        }
        if (n == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.recipe.give.success.single", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.recipe.give.success.multiple", collection2.size(), collection.size()), true);
        }
        return n;
    }

    private static int takeRecipes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Collection<RecipeHolder<?>> collection2) throws CommandSyntaxException {
        int n = 0;
        for (ServerPlayer serverPlayer : collection) {
            n += serverPlayer.resetRecipes(collection2);
        }
        if (n == 0) {
            throw ERROR_TAKE_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.recipe.take.success.single", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.recipe.take.success.multiple", collection2.size(), collection.size()), true);
        }
        return n;
    }
}

