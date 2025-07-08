/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

public class JfrCommand {
    private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.jfr.start.failed"));
    private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.jfr.dump.failed", object));

    private JfrCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("jfr").requires(Commands.hasPermission(4))).then(Commands.literal("start").executes(commandContext -> JfrCommand.startJfr((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("stop").executes(commandContext -> JfrCommand.stopJfr((CommandSourceStack)commandContext.getSource()))));
    }

    private static int startJfr(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        Environment environment = Environment.from(commandSourceStack.getServer());
        if (!JvmProfiler.INSTANCE.start(environment)) {
            throw START_FAILED.create();
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.jfr.started"), false);
        return 1;
    }

    private static int stopJfr(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        try {
            Path path = Paths.get(".", new String[0]).relativize(JvmProfiler.INSTANCE.stop().normalize());
            Path path2 = !commandSourceStack.getServer().isPublished() || SharedConstants.IS_RUNNING_IN_IDE ? path.toAbsolutePath() : path;
            MutableComponent mutableComponent = Component.literal(path.toString()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(path2.toString())).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click"))));
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.jfr.stopped", mutableComponent), false);
            return 1;
        }
        catch (Throwable throwable) {
            throw DUMP_FAILED.create((Object)throwable.getMessage());
        }
    }
}

