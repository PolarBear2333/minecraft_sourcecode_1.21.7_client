/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetPlayerIdleTimeoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setidletimeout").requires(Commands.hasPermission(3))).then(Commands.argument("minutes", IntegerArgumentType.integer((int)0)).executes(commandContext -> SetPlayerIdleTimeoutCommand.setIdleTimeout((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"minutes")))));
    }

    private static int setIdleTimeout(CommandSourceStack commandSourceStack, int n) {
        commandSourceStack.getServer().setPlayerIdleTimeout(n);
        if (n > 0) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.setidletimeout.success", n), true);
        } else {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.setidletimeout.success.disabled"), true);
        }
        return n;
    }
}

