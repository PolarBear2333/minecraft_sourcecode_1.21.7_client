/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stop").requires(Commands.hasPermission(4))).executes(commandContext -> {
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.stop.stopping"), true);
            ((CommandSourceStack)commandContext.getSource()).getServer().halt(false);
            return 1;
        }));
    }
}

