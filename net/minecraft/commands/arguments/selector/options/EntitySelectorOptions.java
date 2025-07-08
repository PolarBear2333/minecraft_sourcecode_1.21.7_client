/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public class EntitySelectorOptions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Option> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.entity.options.unknown", object));
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.entity.options.inapplicable", object));
    public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.options.distance.negative"));
    public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.options.level.negative"));
    public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType((Message)Component.translatable("argument.entity.options.limit.toosmall"));
    public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.entity.options.sort.irreversible", object));
    public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.entity.options.mode.invalid", object));
    public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.entity.options.type.invalid", object));

    private static void register(String string, Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
        OPTIONS.put(string, new Option(modifier, predicate, component));
    }

    public static void bootStrap() {
        if (!OPTIONS.isEmpty()) {
            return;
        }
        EntitySelectorOptions.register("name", entitySelectorParser -> {
            int n = entitySelectorParser.getReader().getCursor();
            boolean bl = entitySelectorParser.shouldInvertValue();
            String string = entitySelectorParser.getReader().readString();
            if (entitySelectorParser.hasNameNotEquals() && !bl) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)"name");
            }
            if (bl) {
                entitySelectorParser.setHasNameNotEquals(true);
            } else {
                entitySelectorParser.setHasNameEquals(true);
            }
            entitySelectorParser.addPredicate(entity -> entity.getName().getString().equals(string) != bl);
        }, entitySelectorParser -> !entitySelectorParser.hasNameEquals(), Component.translatable("argument.entity.options.name.description"));
        EntitySelectorOptions.register("distance", entitySelectorParser -> {
            int n = entitySelectorParser.getReader().getCursor();
            MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromReader(entitySelectorParser.getReader());
            if (doubles.min().isPresent() && doubles.min().get() < 0.0 || doubles.max().isPresent() && doubles.max().get() < 0.0) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_RANGE_NEGATIVE.createWithContext((ImmutableStringReader)entitySelectorParser.getReader());
            }
            entitySelectorParser.setDistance(doubles);
            entitySelectorParser.setWorldLimited();
        }, entitySelectorParser -> entitySelectorParser.getDistance().isAny(), Component.translatable("argument.entity.options.distance.description"));
        EntitySelectorOptions.register("level", entitySelectorParser -> {
            int n = entitySelectorParser.getReader().getCursor();
            MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(entitySelectorParser.getReader());
            if (ints.min().isPresent() && ints.min().get() < 0 || ints.max().isPresent() && ints.max().get() < 0) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_LEVEL_NEGATIVE.createWithContext((ImmutableStringReader)entitySelectorParser.getReader());
            }
            entitySelectorParser.setLevel(ints);
            entitySelectorParser.setIncludesEntities(false);
        }, entitySelectorParser -> entitySelectorParser.getLevel().isAny(), Component.translatable("argument.entity.options.level.description"));
        EntitySelectorOptions.register("x", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setX(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getX() == null, Component.translatable("argument.entity.options.x.description"));
        EntitySelectorOptions.register("y", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setY(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getY() == null, Component.translatable("argument.entity.options.y.description"));
        EntitySelectorOptions.register("z", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setZ(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getZ() == null, Component.translatable("argument.entity.options.z.description"));
        EntitySelectorOptions.register("dx", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setDeltaX(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getDeltaX() == null, Component.translatable("argument.entity.options.dx.description"));
        EntitySelectorOptions.register("dy", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setDeltaY(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getDeltaY() == null, Component.translatable("argument.entity.options.dy.description"));
        EntitySelectorOptions.register("dz", entitySelectorParser -> {
            entitySelectorParser.setWorldLimited();
            entitySelectorParser.setDeltaZ(entitySelectorParser.getReader().readDouble());
        }, entitySelectorParser -> entitySelectorParser.getDeltaZ() == null, Component.translatable("argument.entity.options.dz.description"));
        EntitySelectorOptions.register("x_rotation", entitySelectorParser -> entitySelectorParser.setRotX(WrappedMinMaxBounds.fromReader(entitySelectorParser.getReader(), true, Mth::wrapDegrees)), entitySelectorParser -> entitySelectorParser.getRotX() == WrappedMinMaxBounds.ANY, Component.translatable("argument.entity.options.x_rotation.description"));
        EntitySelectorOptions.register("y_rotation", entitySelectorParser -> entitySelectorParser.setRotY(WrappedMinMaxBounds.fromReader(entitySelectorParser.getReader(), true, Mth::wrapDegrees)), entitySelectorParser -> entitySelectorParser.getRotY() == WrappedMinMaxBounds.ANY, Component.translatable("argument.entity.options.y_rotation.description"));
        EntitySelectorOptions.register("limit", entitySelectorParser -> {
            int n = entitySelectorParser.getReader().getCursor();
            int n2 = entitySelectorParser.getReader().readInt();
            if (n2 < 1) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_LIMIT_TOO_SMALL.createWithContext((ImmutableStringReader)entitySelectorParser.getReader());
            }
            entitySelectorParser.setMaxResults(n2);
            entitySelectorParser.setLimited(true);
        }, entitySelectorParser -> !entitySelectorParser.isCurrentEntity() && !entitySelectorParser.isLimited(), Component.translatable("argument.entity.options.limit.description"));
        EntitySelectorOptions.register("sort", entitySelectorParser -> {
            int n = entitySelectorParser.getReader().getCursor();
            String string = entitySelectorParser.getReader().readUnquotedString();
            entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), suggestionsBuilder));
            entitySelectorParser.setOrder(switch (string) {
                case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
                case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
                case "random" -> EntitySelectorParser.ORDER_RANDOM;
                case "arbitrary" -> EntitySelector.ORDER_ARBITRARY;
                default -> {
                    entitySelectorParser.getReader().setCursor(n);
                    throw ERROR_SORT_UNKNOWN.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)string);
                }
            });
            entitySelectorParser.setSorted(true);
        }, entitySelectorParser -> !entitySelectorParser.isCurrentEntity() && !entitySelectorParser.isSorted(), Component.translatable("argument.entity.options.sort.description"));
        EntitySelectorOptions.register("gamemode", entitySelectorParser -> {
            entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> {
                String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
                boolean bl = !entitySelectorParser.hasGamemodeNotEquals();
                boolean bl2 = true;
                if (!string.isEmpty()) {
                    if (string.charAt(0) == '!') {
                        bl = false;
                        string = string.substring(1);
                    } else {
                        bl2 = false;
                    }
                }
                for (GameType gameType : GameType.values()) {
                    if (!gameType.getName().toLowerCase(Locale.ROOT).startsWith(string)) continue;
                    if (bl2) {
                        suggestionsBuilder.suggest("!" + gameType.getName());
                    }
                    if (!bl) continue;
                    suggestionsBuilder.suggest(gameType.getName());
                }
                return suggestionsBuilder.buildFuture();
            });
            int n = entitySelectorParser.getReader().getCursor();
            boolean bl = entitySelectorParser.shouldInvertValue();
            if (entitySelectorParser.hasGamemodeNotEquals() && !bl) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)"gamemode");
            }
            String string = entitySelectorParser.getReader().readUnquotedString();
            GameType gameType = GameType.byName(string, null);
            if (gameType == null) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_GAME_MODE_INVALID.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)string);
            }
            entitySelectorParser.setIncludesEntities(false);
            entitySelectorParser.addPredicate(entity -> {
                if (entity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    GameType gameType2 = serverPlayer.gameMode();
                    return gameType2 == gameType ^ bl;
                }
                return false;
            });
            if (bl) {
                entitySelectorParser.setHasGamemodeNotEquals(true);
            } else {
                entitySelectorParser.setHasGamemodeEquals(true);
            }
        }, entitySelectorParser -> !entitySelectorParser.hasGamemodeEquals(), Component.translatable("argument.entity.options.gamemode.description"));
        EntitySelectorOptions.register("team", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            String string = entitySelectorParser.getReader().readUnquotedString();
            entitySelectorParser.addPredicate(entity -> {
                PlayerTeam playerTeam = entity.getTeam();
                String string2 = playerTeam == null ? "" : ((Team)playerTeam).getName();
                return string2.equals(string) != bl;
            });
            if (bl) {
                entitySelectorParser.setHasTeamNotEquals(true);
            } else {
                entitySelectorParser.setHasTeamEquals(true);
            }
        }, entitySelectorParser -> !entitySelectorParser.hasTeamEquals(), Component.translatable("argument.entity.options.team.description"));
        EntitySelectorOptions.register("type", entitySelectorParser -> {
            entitySelectorParser.setSuggestions((suggestionsBuilder, consumer) -> {
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), suggestionsBuilder, String.valueOf('!'));
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map(named -> named.key().location()), suggestionsBuilder, "!#");
                if (!entitySelectorParser.isTypeLimitedInversely()) {
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), suggestionsBuilder);
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map(named -> named.key().location()), suggestionsBuilder, String.valueOf('#'));
                }
                return suggestionsBuilder.buildFuture();
            });
            int n = entitySelectorParser.getReader().getCursor();
            boolean bl = entitySelectorParser.shouldInvertValue();
            if (entitySelectorParser.isTypeLimitedInversely() && !bl) {
                entitySelectorParser.getReader().setCursor(n);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)"type");
            }
            if (bl) {
                entitySelectorParser.setTypeLimitedInversely();
            }
            if (entitySelectorParser.isTag()) {
                TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.read(entitySelectorParser.getReader()));
                entitySelectorParser.addPredicate(entity -> entity.getType().is(tagKey) != bl);
            } else {
                ResourceLocation resourceLocation = ResourceLocation.read(entitySelectorParser.getReader());
                EntityType entityType = (EntityType)BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation).orElseThrow(() -> {
                    entitySelectorParser.getReader().setCursor(n);
                    return ERROR_ENTITY_TYPE_INVALID.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)resourceLocation.toString());
                });
                if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
                    entitySelectorParser.setIncludesEntities(false);
                }
                entitySelectorParser.addPredicate(entity -> Objects.equals(entityType, entity.getType()) != bl);
                if (!bl) {
                    entitySelectorParser.limitToType(entityType);
                }
            }
        }, entitySelectorParser -> !entitySelectorParser.isTypeLimited(), Component.translatable("argument.entity.options.type.description"));
        EntitySelectorOptions.register("tag", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            String string = entitySelectorParser.getReader().readUnquotedString();
            entitySelectorParser.addPredicate(entity -> {
                if ("".equals(string)) {
                    return entity.getTags().isEmpty() != bl;
                }
                return entity.getTags().contains(string) != bl;
            });
        }, entitySelectorParser -> true, Component.translatable("argument.entity.options.tag.description"));
        EntitySelectorOptions.register("nbt", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            CompoundTag compoundTag = TagParser.parseCompoundAsArgument(entitySelectorParser.getReader());
            entitySelectorParser.addPredicate(entity -> {
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);){
                    ServerPlayer serverPlayer;
                    ItemStack itemStack;
                    TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
                    entity.saveWithoutId(tagValueOutput);
                    if (entity instanceof ServerPlayer && !(itemStack = (serverPlayer = (ServerPlayer)entity).getInventory().getSelectedItem()).isEmpty()) {
                        tagValueOutput.store("SelectedItem", ItemStack.CODEC, itemStack);
                    }
                    boolean bl2 = NbtUtils.compareNbt(compoundTag, tagValueOutput.buildResult(), true) != bl;
                    return bl2;
                }
            });
        }, entitySelectorParser -> true, Component.translatable("argument.entity.options.nbt.description"));
        EntitySelectorOptions.register("scores", entitySelectorParser -> {
            StringReader stringReader = entitySelectorParser.getReader();
            HashMap hashMap = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                String string = stringReader.readUnquotedString();
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromReader(stringReader);
                hashMap.put(string, ints);
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!hashMap.isEmpty()) {
                entitySelectorParser.addPredicate(entity -> {
                    ServerScoreboard serverScoreboard = entity.getServer().getScoreboard();
                    for (Map.Entry entry : hashMap.entrySet()) {
                        Objective objective = serverScoreboard.getObjective((String)entry.getKey());
                        if (objective == null) {
                            return false;
                        }
                        ReadOnlyScoreInfo readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo((ScoreHolder)entity, objective);
                        if (readOnlyScoreInfo == null) {
                            return false;
                        }
                        if (((MinMaxBounds.Ints)entry.getValue()).matches(readOnlyScoreInfo.value())) continue;
                        return false;
                    }
                    return true;
                });
            }
            entitySelectorParser.setHasScores(true);
        }, entitySelectorParser -> !entitySelectorParser.hasScores(), Component.translatable("argument.entity.options.scores.description"));
        EntitySelectorOptions.register("advancements", entitySelectorParser -> {
            StringReader stringReader = entitySelectorParser.getReader();
            HashMap hashMap = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                if (stringReader.canRead() && stringReader.peek() == '{') {
                    HashMap hashMap2 = Maps.newHashMap();
                    stringReader.skipWhitespace();
                    stringReader.expect('{');
                    stringReader.skipWhitespace();
                    while (stringReader.canRead() && stringReader.peek() != '}') {
                        stringReader.skipWhitespace();
                        String string = stringReader.readUnquotedString();
                        stringReader.skipWhitespace();
                        stringReader.expect('=');
                        stringReader.skipWhitespace();
                        boolean bl = stringReader.readBoolean();
                        hashMap2.put(string, criterionProgress -> criterionProgress.isDone() == bl);
                        stringReader.skipWhitespace();
                        if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                        stringReader.skip();
                    }
                    stringReader.skipWhitespace();
                    stringReader.expect('}');
                    stringReader.skipWhitespace();
                    hashMap.put(resourceLocation, advancementProgress -> {
                        for (Map.Entry entry : hashMap2.entrySet()) {
                            CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
                            if (criterionProgress != null && ((Predicate)entry.getValue()).test(criterionProgress)) continue;
                            return false;
                        }
                        return true;
                    });
                } else {
                    boolean bl = stringReader.readBoolean();
                    hashMap.put(resourceLocation, advancementProgress -> advancementProgress.isDone() == bl);
                }
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!hashMap.isEmpty()) {
                entitySelectorParser.addPredicate(entity -> {
                    if (!(entity instanceof ServerPlayer)) {
                        return false;
                    }
                    ServerPlayer serverPlayer = (ServerPlayer)entity;
                    PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
                    ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();
                    for (Map.Entry entry : hashMap.entrySet()) {
                        AdvancementHolder advancementHolder = serverAdvancementManager.get((ResourceLocation)entry.getKey());
                        if (advancementHolder != null && ((Predicate)entry.getValue()).test(playerAdvancements.getOrStartProgress(advancementHolder))) continue;
                        return false;
                    }
                    return true;
                });
                entitySelectorParser.setIncludesEntities(false);
            }
            entitySelectorParser.setHasAdvancements(true);
        }, entitySelectorParser -> !entitySelectorParser.hasAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
        EntitySelectorOptions.register("predicate", entitySelectorParser -> {
            boolean bl = entitySelectorParser.shouldInvertValue();
            ResourceKey<LootItemCondition> resourceKey = ResourceKey.create(Registries.PREDICATE, ResourceLocation.read(entitySelectorParser.getReader()));
            entitySelectorParser.addPredicate(entity -> {
                if (!(entity.level() instanceof ServerLevel)) {
                    return false;
                }
                ServerLevel serverLevel = (ServerLevel)entity.level();
                Optional<LootItemCondition> optional = serverLevel.getServer().reloadableRegistries().lookup().get(resourceKey).map(Holder::value);
                if (optional.isEmpty()) {
                    return false;
                }
                LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position()).create(LootContextParamSets.SELECTOR);
                LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
                lootContext.pushVisitedElement(LootContext.createVisitedEntry(optional.get()));
                return bl ^ optional.get().test(lootContext);
            });
        }, entitySelectorParser -> true, Component.translatable("argument.entity.options.predicate.description"));
    }

    public static Modifier get(EntitySelectorParser entitySelectorParser, String string, int n) throws CommandSyntaxException {
        Option option = OPTIONS.get(string);
        if (option != null) {
            if (option.canUse.test(entitySelectorParser)) {
                return option.modifier;
            }
            throw ERROR_INAPPLICABLE_OPTION.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)string);
        }
        entitySelectorParser.getReader().setCursor(n);
        throw ERROR_UNKNOWN_OPTION.createWithContext((ImmutableStringReader)entitySelectorParser.getReader(), (Object)string);
    }

    public static void suggestNames(EntitySelectorParser entitySelectorParser, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Option> entry : OPTIONS.entrySet()) {
            if (!entry.getValue().canUse.test(entitySelectorParser) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) continue;
            suggestionsBuilder.suggest(entry.getKey() + "=", (Message)entry.getValue().description);
        }
    }

    static final class Option
    extends Record {
        final Modifier modifier;
        final Predicate<EntitySelectorParser> canUse;
        final Component description;

        Option(Modifier modifier, Predicate<EntitySelectorParser> predicate, Component component) {
            this.modifier = modifier;
            this.canUse = predicate;
            this.description = component;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Option.class, "modifier;canUse;description", "modifier", "canUse", "description"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Option.class, "modifier;canUse;description", "modifier", "canUse", "description"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Option.class, "modifier;canUse;description", "modifier", "canUse", "description"}, this, object);
        }

        public Modifier modifier() {
            return this.modifier;
        }

        public Predicate<EntitySelectorParser> canUse() {
            return this.canUse;
        }

        public Component description() {
            return this.description;
        }
    }

    public static interface Modifier {
        public void handle(EntitySelectorParser var1) throws CommandSyntaxException;
    }
}

