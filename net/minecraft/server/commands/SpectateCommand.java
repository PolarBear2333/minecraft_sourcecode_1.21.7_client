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
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SpectateCommand {
    private static final SimpleCommandExceptionType ERROR_SELF = new SimpleCommandExceptionType((Message)Component.translatable("commands.spectate.self"));
    private static final DynamicCommandExceptionType ERROR_NOT_SPECTATOR = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.spectate.not_spectator", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spectate").requires(Commands.hasPermission(2))).executes(commandContext -> SpectateCommand.spectate((CommandSourceStack)commandContext.getSource(), null, ((CommandSourceStack)commandContext.getSource()).getPlayerOrException()))).then(((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity()).executes(commandContext -> SpectateCommand.spectate((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException()))).then(Commands.argument("player", EntityArgument.player()).executes(commandContext -> SpectateCommand.spectate((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), EntityArgument.getPlayer((CommandContext<CommandSourceStack>)commandContext, "player"))))));
    }

    private static int spectate(CommandSourceStack commandSourceStack, @Nullable Entity entity, ServerPlayer serverPlayer) throws CommandSyntaxException {
        if (serverPlayer == entity) {
            throw ERROR_SELF.create();
        }
        if (!serverPlayer.isSpectator()) {
            throw ERROR_NOT_SPECTATOR.create((Object)serverPlayer.getDisplayName());
        }
        serverPlayer.setCamera(entity);
        if (entity != null) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.spectate.success.started", entity.getDisplayName()), false);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.spectate.success.stopped"), false);
        }
        return 1;
    }
}

