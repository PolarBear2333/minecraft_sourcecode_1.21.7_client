/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.timers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;

public record FunctionCallback(ResourceLocation functionId) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionCallback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("Name").forGetter(FunctionCallback::functionId)).apply((Applicative)instance, FunctionCallback::new));

    @Override
    public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long l) {
        ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
        serverFunctionManager.get(this.functionId).ifPresent(commandFunction -> serverFunctionManager.execute((CommandFunction<CommandSourceStack>)commandFunction, serverFunctionManager.getGameLoopSender()));
    }

    @Override
    public MapCodec<FunctionCallback> codec() {
        return CODEC;
    }
}

