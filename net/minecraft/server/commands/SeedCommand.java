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
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

public class SeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("seed").requires(Commands.hasPermission(bl ? 2 : 0))).executes(commandContext -> {
            long l = ((CommandSourceStack)commandContext.getSource()).getLevel().getSeed();
            MutableComponent mutableComponent = ComponentUtils.copyOnClickText(String.valueOf(l));
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(() -> Component.translatable("commands.seed.success", mutableComponent), false);
            return (int)l;
        }));
    }
}

