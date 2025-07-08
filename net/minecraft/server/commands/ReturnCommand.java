/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.tree.CommandNode
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.FallthroughTask;

public class ReturnCommand {
    public static <T extends ExecutionCommandSource<T>> void register(CommandDispatcher<T> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal((String)"return").requires(Commands.hasPermission(2))).then(RequiredArgumentBuilder.argument((String)"value", (ArgumentType)IntegerArgumentType.integer()).executes(new ReturnValueCustomExecutor()))).then(LiteralArgumentBuilder.literal((String)"fail").executes(new ReturnFailCustomExecutor()))).then(LiteralArgumentBuilder.literal((String)"run").forward((CommandNode)commandDispatcher.getRoot(), new ReturnFromCommandCustomModifier(), false)));
    }

    static class ReturnValueCustomExecutor<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor.CommandAdapter<T> {
        ReturnValueCustomExecutor() {
        }

        @Override
        public void run(T t, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
            int n = IntegerArgumentType.getInteger((CommandContext)contextChain.getTopContext(), (String)"value");
            t.callback().onSuccess(n);
            Frame frame = executionControl.currentFrame();
            frame.returnSuccess(n);
            frame.discard();
        }
    }

    static class ReturnFailCustomExecutor<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor.CommandAdapter<T> {
        ReturnFailCustomExecutor() {
        }

        @Override
        public void run(T t, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
            t.callback().onFailure();
            Frame frame = executionControl.currentFrame();
            frame.returnFailure();
            frame.discard();
        }
    }

    static class ReturnFromCommandCustomModifier<T extends ExecutionCommandSource<T>>
    implements CustomModifierExecutor.ModifierAdapter<T> {
        ReturnFromCommandCustomModifier() {
        }

        @Override
        public void apply(T t, List<T> list, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
            if (list.isEmpty()) {
                if (chainModifiers.isReturn()) {
                    executionControl.queueNext(FallthroughTask.instance());
                }
                return;
            }
            executionControl.currentFrame().discard();
            ContextChain contextChain2 = contextChain.nextStage();
            String string = contextChain2.getTopContext().getInput();
            executionControl.queueNext(new BuildContexts.Continuation<T>(string, contextChain2, chainModifiers.setReturn(), t, list));
        }
    }
}

