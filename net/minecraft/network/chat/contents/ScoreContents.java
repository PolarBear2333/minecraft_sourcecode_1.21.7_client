/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;

public record ScoreContents(Either<SelectorPattern, String> name, String objective) implements ComponentContents
{
    public static final MapCodec<ScoreContents> INNER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.either(SelectorPattern.CODEC, (Codec)Codec.STRING).fieldOf("name").forGetter(ScoreContents::name), (App)Codec.STRING.fieldOf("objective").forGetter(ScoreContents::objective)).apply((Applicative)instance, ScoreContents::new));
    public static final MapCodec<ScoreContents> CODEC = INNER_CODEC.fieldOf("score");
    public static final ComponentContents.Type<ScoreContents> TYPE = new ComponentContents.Type<ScoreContents>(CODEC, "score");

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }

    private ScoreHolder findTargetName(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        Optional optional = this.name.left();
        if (optional.isPresent()) {
            List<? extends Entity> list = ((SelectorPattern)optional.get()).resolved().findEntities(commandSourceStack);
            if (!list.isEmpty()) {
                if (list.size() != 1) {
                    throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
                }
                return list.getFirst();
            }
            return ScoreHolder.forNameOnly(((SelectorPattern)optional.get()).pattern());
        }
        return ScoreHolder.forNameOnly((String)this.name.right().orElseThrow());
    }

    private MutableComponent getScore(ScoreHolder scoreHolder, CommandSourceStack commandSourceStack) {
        ReadOnlyScoreInfo readOnlyScoreInfo;
        ServerScoreboard serverScoreboard;
        Objective objective;
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer != null && (objective = (serverScoreboard = minecraftServer.getScoreboard()).getObjective(this.objective)) != null && (readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective)) != null) {
            return readOnlyScoreInfo.formatValue(objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
        }
        return Component.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        if (commandSourceStack == null) {
            return Component.empty();
        }
        ScoreHolder scoreHolder = this.findTargetName(commandSourceStack);
        ScoreHolder scoreHolder2 = entity != null && scoreHolder.equals(ScoreHolder.WILDCARD) ? entity : scoreHolder;
        return this.getScore(scoreHolder2, commandSourceStack);
    }

    @Override
    public String toString() {
        return "score{name='" + String.valueOf(this.name) + "', objective='" + this.objective + "'}";
    }
}

