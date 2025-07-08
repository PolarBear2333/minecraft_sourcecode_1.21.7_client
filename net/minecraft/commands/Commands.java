/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.CommandContextBuilder
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.PermissionSource;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.commands.DamageCommand;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.DebugCommand;
import net.minecraft.server.commands.DebugConfigCommand;
import net.minecraft.server.commands.DebugMobSpawningCommand;
import net.minecraft.server.commands.DebugPathCommand;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.DialogCommand;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.commands.ForceLoadCommand;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.commands.HelpCommand;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.JfrCommand;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.commands.PerfCommand;
import net.minecraft.server.commands.PermissionCheck;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.server.commands.RaidCommand;
import net.minecraft.server.commands.RandomCommand;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.commands.ReturnCommand;
import net.minecraft.server.commands.RideCommand;
import net.minecraft.server.commands.RotateCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.ServerPackCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.commands.SpawnArmorTrimsCommand;
import net.minecraft.server.commands.SpectateCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.commands.StopCommand;
import net.minecraft.server.commands.StopSoundCommand;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.TagCommand;
import net.minecraft.server.commands.TeamCommand;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.commands.TickCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.commands.TransferCommand;
import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.commands.VersionCommand;
import net.minecraft.server.commands.WardenSpawnTrackerCommand;
import net.minecraft.server.commands.WaypointCommand;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class Commands {
    public static final String COMMAND_PREFIX = "/";
    private static final ThreadLocal<ExecutionContext<CommandSourceStack>> CURRENT_EXECUTION_CONTEXT = new ThreadLocal();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int LEVEL_ALL = 0;
    public static final int LEVEL_MODERATORS = 1;
    public static final int LEVEL_GAMEMASTERS = 2;
    public static final int LEVEL_ADMINS = 3;
    public static final int LEVEL_OWNERS = 4;
    private static final ClientboundCommandsPacket.NodeInspector<CommandSourceStack> COMMAND_NODE_INSPECTOR = new ClientboundCommandsPacket.NodeInspector<CommandSourceStack>(){

        @Override
        @Nullable
        public ResourceLocation suggestionId(ArgumentCommandNode<CommandSourceStack, ?> argumentCommandNode) {
            SuggestionProvider suggestionProvider = argumentCommandNode.getCustomSuggestions();
            return suggestionProvider != null ? SuggestionProviders.getName(suggestionProvider) : null;
        }

        @Override
        public boolean isExecutable(CommandNode<CommandSourceStack> commandNode) {
            return commandNode.getCommand() != null;
        }

        @Override
        public boolean isRestricted(CommandNode<CommandSourceStack> commandNode) {
            PermissionCheck permissionCheck;
            Predicate predicate = commandNode.getRequirement();
            return predicate instanceof PermissionCheck && (permissionCheck = (PermissionCheck)predicate).requiredLevel() > 0;
        }
    };
    private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher();

    public Commands(CommandSelection commandSelection, CommandBuildContext commandBuildContext) {
        AdvancementCommands.register(this.dispatcher);
        AttributeCommand.register(this.dispatcher, commandBuildContext);
        ExecuteCommand.register(this.dispatcher, commandBuildContext);
        BossBarCommands.register(this.dispatcher, commandBuildContext);
        ClearInventoryCommands.register(this.dispatcher, commandBuildContext);
        CloneCommands.register(this.dispatcher, commandBuildContext);
        DamageCommand.register(this.dispatcher, commandBuildContext);
        DataCommands.register(this.dispatcher);
        DataPackCommand.register(this.dispatcher, commandBuildContext);
        DebugCommand.register(this.dispatcher);
        DefaultGameModeCommands.register(this.dispatcher);
        DialogCommand.register(this.dispatcher, commandBuildContext);
        DifficultyCommand.register(this.dispatcher);
        EffectCommands.register(this.dispatcher, commandBuildContext);
        EmoteCommands.register(this.dispatcher);
        EnchantCommand.register(this.dispatcher, commandBuildContext);
        ExperienceCommand.register(this.dispatcher);
        FillCommand.register(this.dispatcher, commandBuildContext);
        FillBiomeCommand.register(this.dispatcher, commandBuildContext);
        ForceLoadCommand.register(this.dispatcher);
        FunctionCommand.register(this.dispatcher);
        GameModeCommand.register(this.dispatcher);
        GameRuleCommand.register(this.dispatcher, commandBuildContext);
        GiveCommand.register(this.dispatcher, commandBuildContext);
        HelpCommand.register(this.dispatcher);
        ItemCommands.register(this.dispatcher, commandBuildContext);
        KickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        ListPlayersCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher, commandBuildContext);
        LootCommand.register(this.dispatcher, commandBuildContext);
        MsgCommand.register(this.dispatcher);
        ParticleCommand.register(this.dispatcher, commandBuildContext);
        PlaceCommand.register(this.dispatcher);
        PlaySoundCommand.register(this.dispatcher);
        RandomCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        RecipeCommand.register(this.dispatcher);
        ReturnCommand.register(this.dispatcher);
        RideCommand.register(this.dispatcher);
        RotateCommand.register(this.dispatcher);
        SayCommand.register(this.dispatcher);
        ScheduleCommand.register(this.dispatcher);
        ScoreboardCommand.register(this.dispatcher, commandBuildContext);
        SeedCommand.register(this.dispatcher, commandSelection != CommandSelection.INTEGRATED);
        VersionCommand.register(this.dispatcher, commandSelection != CommandSelection.INTEGRATED);
        SetBlockCommand.register(this.dispatcher, commandBuildContext);
        SetSpawnCommand.register(this.dispatcher);
        SetWorldSpawnCommand.register(this.dispatcher);
        SpectateCommand.register(this.dispatcher);
        SpreadPlayersCommand.register(this.dispatcher);
        StopSoundCommand.register(this.dispatcher);
        SummonCommand.register(this.dispatcher, commandBuildContext);
        TagCommand.register(this.dispatcher);
        TeamCommand.register(this.dispatcher, commandBuildContext);
        TeamMsgCommand.register(this.dispatcher);
        TeleportCommand.register(this.dispatcher);
        TellRawCommand.register(this.dispatcher, commandBuildContext);
        TestCommand.register(this.dispatcher, commandBuildContext);
        TickCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        TitleCommand.register(this.dispatcher, commandBuildContext);
        TriggerCommand.register(this.dispatcher);
        WaypointCommand.register(this.dispatcher, commandBuildContext);
        WeatherCommand.register(this.dispatcher);
        WorldBorderCommand.register(this.dispatcher);
        if (JvmProfiler.INSTANCE.isAvailable()) {
            JfrCommand.register(this.dispatcher);
        }
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            RaidCommand.register(this.dispatcher, commandBuildContext);
            DebugPathCommand.register(this.dispatcher);
            DebugMobSpawningCommand.register(this.dispatcher);
            WardenSpawnTrackerCommand.register(this.dispatcher);
            SpawnArmorTrimsCommand.register(this.dispatcher);
            ServerPackCommand.register(this.dispatcher);
            if (commandSelection.includeDedicated) {
                DebugConfigCommand.register(this.dispatcher, commandBuildContext);
            }
        }
        if (commandSelection.includeDedicated) {
            BanIpCommands.register(this.dispatcher);
            BanListCommands.register(this.dispatcher);
            BanPlayerCommands.register(this.dispatcher);
            DeOpCommands.register(this.dispatcher);
            OpCommand.register(this.dispatcher);
            PardonCommand.register(this.dispatcher);
            PardonIpCommand.register(this.dispatcher);
            PerfCommand.register(this.dispatcher);
            SaveAllCommand.register(this.dispatcher);
            SaveOffCommand.register(this.dispatcher);
            SaveOnCommand.register(this.dispatcher);
            SetPlayerIdleTimeoutCommand.register(this.dispatcher);
            StopCommand.register(this.dispatcher);
            TransferCommand.register(this.dispatcher);
            WhitelistCommand.register(this.dispatcher);
        }
        if (commandSelection.includeIntegrated) {
            PublishCommand.register(this.dispatcher);
        }
        this.dispatcher.setConsumer(ExecutionCommandSource.resultConsumer());
    }

    public static <S> ParseResults<S> mapSource(ParseResults<S> parseResults, UnaryOperator<S> unaryOperator) {
        CommandContextBuilder commandContextBuilder = parseResults.getContext();
        CommandContextBuilder commandContextBuilder2 = commandContextBuilder.withSource(unaryOperator.apply(commandContextBuilder.getSource()));
        return new ParseResults(commandContextBuilder2, parseResults.getReader(), parseResults.getExceptions());
    }

    public void performPrefixedCommand(CommandSourceStack commandSourceStack, String string) {
        string = Commands.trimOptionalPrefix(string);
        this.performCommand((ParseResults<CommandSourceStack>)this.dispatcher.parse(string, (Object)commandSourceStack), string);
    }

    public static String trimOptionalPrefix(String string) {
        return string.startsWith(COMMAND_PREFIX) ? string.substring(1) : string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void performCommand(ParseResults<CommandSourceStack> parseResults, String string) {
        CommandSourceStack commandSourceStack = (CommandSourceStack)parseResults.getContext().getSource();
        Profiler.get().push(() -> COMMAND_PREFIX + string);
        ContextChain<CommandSourceStack> contextChain = Commands.finishParsing(parseResults, string, commandSourceStack);
        try {
            if (contextChain != null) {
                Commands.executeCommandInContext(commandSourceStack, executionContext -> ExecutionContext.queueInitialCommandExecution(executionContext, string, contextChain, commandSourceStack, CommandResultCallback.EMPTY));
            }
        }
        catch (Exception exception) {
            MutableComponent mutableComponent = Component.literal(exception.getMessage() == null ? exception.getClass().getName() : exception.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Command exception: /{}", (Object)string, (Object)exception);
                StackTraceElement[] stackTraceElementArray = exception.getStackTrace();
                for (int i = 0; i < Math.min(stackTraceElementArray.length, 3); ++i) {
                    mutableComponent.append("\n\n").append(stackTraceElementArray[i].getMethodName()).append("\n ").append(stackTraceElementArray[i].getFileName()).append(":").append(String.valueOf(stackTraceElementArray[i].getLineNumber()));
                }
            }
            commandSourceStack.sendFailure(Component.translatable("command.failed").withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(mutableComponent))));
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                commandSourceStack.sendFailure(Component.literal(Util.describeError(exception)));
                LOGGER.error("'/{}' threw an exception", (Object)string, (Object)exception);
            }
        }
        finally {
            Profiler.get().pop();
        }
    }

    @Nullable
    private static ContextChain<CommandSourceStack> finishParsing(ParseResults<CommandSourceStack> parseResults, String string, CommandSourceStack commandSourceStack) {
        try {
            Commands.validateParseResults(parseResults);
            return (ContextChain)ContextChain.tryFlatten((CommandContext)parseResults.getContext().build(string)).orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader()));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            commandSourceStack.sendFailure(ComponentUtils.fromMessage(commandSyntaxException.getRawMessage()));
            if (commandSyntaxException.getInput() != null && commandSyntaxException.getCursor() >= 0) {
                int n = Math.min(commandSyntaxException.getInput().length(), commandSyntaxException.getCursor());
                MutableComponent mutableComponent = Component.empty().withStyle(ChatFormatting.GRAY).withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand(COMMAND_PREFIX + string)));
                if (n > 10) {
                    mutableComponent.append(CommonComponents.ELLIPSIS);
                }
                mutableComponent.append(commandSyntaxException.getInput().substring(Math.max(0, n - 10), n));
                if (n < commandSyntaxException.getInput().length()) {
                    MutableComponent mutableComponent2 = Component.literal(commandSyntaxException.getInput().substring(n)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                    mutableComponent.append(mutableComponent2);
                }
                mutableComponent.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                commandSourceStack.sendFailure(mutableComponent);
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void executeCommandInContext(CommandSourceStack commandSourceStack, Consumer<ExecutionContext<CommandSourceStack>> consumer) {
        block9: {
            boolean bl;
            MinecraftServer minecraftServer = commandSourceStack.getServer();
            ExecutionContext<CommandSourceStack> executionContext = CURRENT_EXECUTION_CONTEXT.get();
            boolean bl2 = bl = executionContext == null;
            if (bl) {
                int n = Math.max(1, minecraftServer.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH));
                int n2 = minecraftServer.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_FORK_COUNT);
                try (ExecutionContext executionContext2 = new ExecutionContext(n, n2, Profiler.get());){
                    CURRENT_EXECUTION_CONTEXT.set(executionContext2);
                    consumer.accept(executionContext2);
                    executionContext2.runCommandQueue();
                    break block9;
                }
                finally {
                    CURRENT_EXECUTION_CONTEXT.set(null);
                }
            }
            consumer.accept(executionContext);
        }
    }

    public void sendCommands(ServerPlayer serverPlayer) {
        HashMap hashMap = new HashMap();
        RootCommandNode rootCommandNode = new RootCommandNode();
        hashMap.put((CommandNode)this.dispatcher.getRoot(), (CommandNode)rootCommandNode);
        Commands.fillUsableCommands(this.dispatcher.getRoot(), rootCommandNode, serverPlayer.createCommandSourceStack(), hashMap);
        serverPlayer.connection.send(new ClientboundCommandsPacket(rootCommandNode, COMMAND_NODE_INSPECTOR));
    }

    private static <S> void fillUsableCommands(CommandNode<S> commandNode, CommandNode<S> commandNode2, S s, Map<CommandNode<S>, CommandNode<S>> map) {
        for (CommandNode commandNode3 : commandNode.getChildren()) {
            if (!commandNode3.canUse(s)) continue;
            ArgumentBuilder argumentBuilder = commandNode3.createBuilder();
            if (argumentBuilder.getRedirect() != null) {
                argumentBuilder.redirect(map.get(argumentBuilder.getRedirect()));
            }
            CommandNode commandNode4 = argumentBuilder.build();
            map.put(commandNode3, commandNode4);
            commandNode2.addChild(commandNode4);
            if (commandNode3.getChildren().isEmpty()) continue;
            Commands.fillUsableCommands(commandNode3, commandNode4, s, map);
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> literal(String string) {
        return LiteralArgumentBuilder.literal((String)string);
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String string, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument((String)string, argumentType);
    }

    public static Predicate<String> createValidator(ParseFunction parseFunction) {
        return string -> {
            try {
                parseFunction.parse(new StringReader(string));
                return true;
            }
            catch (CommandSyntaxException commandSyntaxException) {
                return false;
            }
        };
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.dispatcher;
    }

    public static <S> void validateParseResults(ParseResults<S> parseResults) throws CommandSyntaxException {
        CommandSyntaxException commandSyntaxException = Commands.getParseException(parseResults);
        if (commandSyntaxException != null) {
            throw commandSyntaxException;
        }
    }

    @Nullable
    public static <S> CommandSyntaxException getParseException(ParseResults<S> parseResults) {
        if (!parseResults.getReader().canRead()) {
            return null;
        }
        if (parseResults.getExceptions().size() == 1) {
            return (CommandSyntaxException)((Object)parseResults.getExceptions().values().iterator().next());
        }
        if (parseResults.getContext().getRange().isEmpty()) {
            return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
        }
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parseResults.getReader());
    }

    public static CommandBuildContext createValidationContext(final HolderLookup.Provider provider) {
        return new CommandBuildContext(){

            @Override
            public FeatureFlagSet enabledFeatures() {
                return FeatureFlags.REGISTRY.allFlags();
            }

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return provider.listRegistryKeys();
            }

            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return provider.lookup(resourceKey).map(this::createLookup);
            }

            private <T> HolderLookup.RegistryLookup.Delegate<T> createLookup(final HolderLookup.RegistryLookup<T> registryLookup) {
                return new HolderLookup.RegistryLookup.Delegate<T>(this){

                    @Override
                    public HolderLookup.RegistryLookup<T> parent() {
                        return registryLookup;
                    }

                    @Override
                    public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                        return Optional.of(this.getOrThrow(tagKey));
                    }

                    @Override
                    public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
                        Optional<HolderSet.Named<HolderSet.Named>> optional = this.parent().get(tagKey);
                        return optional.orElseGet(() -> HolderSet.emptyNamed(this.parent(), tagKey));
                    }
                };
            }
        };
    }

    public static void validate() {
        CommandBuildContext commandBuildContext = Commands.createValidationContext(VanillaRegistries.createLookup());
        CommandDispatcher<CommandSourceStack> commandDispatcher = new Commands(CommandSelection.ALL, commandBuildContext).getDispatcher();
        RootCommandNode rootCommandNode = commandDispatcher.getRoot();
        commandDispatcher.findAmbiguities((commandNode, commandNode2, commandNode3, collection) -> LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", new Object[]{commandDispatcher.getPath(commandNode2), commandDispatcher.getPath(commandNode3), collection}));
        Set<ArgumentType<?>> set = ArgumentUtils.findUsedArgumentTypes(rootCommandNode);
        Set set2 = set.stream().filter(argumentType -> !ArgumentTypeInfos.isClassRecognized(argumentType.getClass())).collect(Collectors.toSet());
        if (!set2.isEmpty()) {
            LOGGER.warn("Missing type registration for following arguments:\n {}", (Object)set2.stream().map(argumentType -> "\t" + String.valueOf(argumentType)).collect(Collectors.joining(",\n")));
            throw new IllegalStateException("Unregistered argument types");
        }
    }

    public static <T extends PermissionSource> PermissionCheck<T> hasPermission(int n) {
        return new PermissionSource.Check(n);
    }

    public static enum CommandSelection {
        ALL(true, true),
        DEDICATED(false, true),
        INTEGRATED(true, false);

        final boolean includeIntegrated;
        final boolean includeDedicated;

        private CommandSelection(boolean bl, boolean bl2) {
            this.includeIntegrated = bl;
            this.includeDedicated = bl2;
        }
    }

    @FunctionalInterface
    public static interface ParseFunction {
        public void parse(StringReader var1) throws CommandSyntaxException;
    }
}

