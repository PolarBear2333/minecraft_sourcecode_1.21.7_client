/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  org.apache.commons.lang3.Validate
 *  org.slf4j.Logger
 */
package net.minecraft.core.registries;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.numbers.NumberFormatType;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogTypes;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.ActionTypes;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.DialogBodyTypes;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.server.dialog.input.InputControlTypes;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnConditions;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplays;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplays;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.EnchantmentProviderTypes;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSources;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGenerators;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBindings;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class BuiltInRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
    private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry(ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable());
    public static final DefaultedRegistry<GameEvent> GAME_EVENT = BuiltInRegistries.registerDefaulted(Registries.GAME_EVENT, "step", GameEvent::bootstrap);
    public static final Registry<SoundEvent> SOUND_EVENT = BuiltInRegistries.registerSimple(Registries.SOUND_EVENT, registry -> SoundEvents.ITEM_PICKUP);
    public static final DefaultedRegistry<Fluid> FLUID = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.FLUID, "empty", registry -> Fluids.EMPTY);
    public static final Registry<MobEffect> MOB_EFFECT = BuiltInRegistries.registerSimple(Registries.MOB_EFFECT, MobEffects::bootstrap);
    public static final DefaultedRegistry<Block> BLOCK = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.BLOCK, "air", registry -> Blocks.AIR);
    public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.ENTITY_TYPE, "pig", registry -> EntityType.PIG);
    public static final DefaultedRegistry<Item> ITEM = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.ITEM, "air", registry -> Items.AIR);
    public static final Registry<Potion> POTION = BuiltInRegistries.registerSimple(Registries.POTION, Potions::bootstrap);
    public static final Registry<ParticleType<?>> PARTICLE_TYPE = BuiltInRegistries.registerSimple(Registries.PARTICLE_TYPE, registry -> ParticleTypes.BLOCK);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = BuiltInRegistries.registerSimpleWithIntrusiveHolders(Registries.BLOCK_ENTITY_TYPE, registry -> BlockEntityType.FURNACE);
    public static final Registry<ResourceLocation> CUSTOM_STAT = BuiltInRegistries.registerSimple(Registries.CUSTOM_STAT, registry -> Stats.JUMP);
    public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = BuiltInRegistries.registerDefaulted(Registries.CHUNK_STATUS, "empty", registry -> ChunkStatus.EMPTY);
    public static final Registry<RuleTestType<?>> RULE_TEST = BuiltInRegistries.registerSimple(Registries.RULE_TEST, registry -> RuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<RuleBlockEntityModifierType<?>> RULE_BLOCK_ENTITY_MODIFIER = BuiltInRegistries.registerSimple(Registries.RULE_BLOCK_ENTITY_MODIFIER, registry -> RuleBlockEntityModifierType.PASSTHROUGH);
    public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = BuiltInRegistries.registerSimple(Registries.POS_RULE_TEST, registry -> PosRuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<MenuType<?>> MENU = BuiltInRegistries.registerSimple(Registries.MENU, registry -> MenuType.ANVIL);
    public static final Registry<RecipeType<?>> RECIPE_TYPE = BuiltInRegistries.registerSimple(Registries.RECIPE_TYPE, registry -> RecipeType.CRAFTING);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = BuiltInRegistries.registerSimple(Registries.RECIPE_SERIALIZER, registry -> RecipeSerializer.SHAPELESS_RECIPE);
    public static final Registry<Attribute> ATTRIBUTE = BuiltInRegistries.registerSimple(Registries.ATTRIBUTE, Attributes::bootstrap);
    public static final Registry<PositionSourceType<?>> POSITION_SOURCE_TYPE = BuiltInRegistries.registerSimple(Registries.POSITION_SOURCE_TYPE, registry -> PositionSourceType.BLOCK);
    public static final Registry<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPE = BuiltInRegistries.registerSimple(Registries.COMMAND_ARGUMENT_TYPE, ArgumentTypeInfos::bootstrap);
    public static final Registry<StatType<?>> STAT_TYPE = BuiltInRegistries.registerSimple(Registries.STAT_TYPE, registry -> Stats.ITEM_USED);
    public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = BuiltInRegistries.registerDefaulted(Registries.VILLAGER_TYPE, "plains", VillagerType::bootstrap);
    public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = BuiltInRegistries.registerDefaulted(Registries.VILLAGER_PROFESSION, "none", VillagerProfession::bootstrap);
    public static final Registry<PoiType> POINT_OF_INTEREST_TYPE = BuiltInRegistries.registerSimple(Registries.POINT_OF_INTEREST_TYPE, PoiTypes::bootstrap);
    public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = BuiltInRegistries.registerDefaulted(Registries.MEMORY_MODULE_TYPE, "dummy", registry -> MemoryModuleType.DUMMY);
    public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = BuiltInRegistries.registerDefaulted(Registries.SENSOR_TYPE, "dummy", registry -> SensorType.DUMMY);
    public static final Registry<Schedule> SCHEDULE = BuiltInRegistries.registerSimple(Registries.SCHEDULE, registry -> Schedule.EMPTY);
    public static final Registry<Activity> ACTIVITY = BuiltInRegistries.registerSimple(Registries.ACTIVITY, registry -> Activity.IDLE);
    public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_POOL_ENTRY_TYPE, registry -> LootPoolEntries.EMPTY);
    public static final Registry<LootItemFunctionType<?>> LOOT_FUNCTION_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_FUNCTION_TYPE, registry -> LootItemFunctions.SET_COUNT);
    public static final Registry<LootItemConditionType> LOOT_CONDITION_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_CONDITION_TYPE, registry -> LootItemConditions.INVERTED);
    public static final Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_NUMBER_PROVIDER_TYPE, registry -> NumberProviders.CONSTANT);
    public static final Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_NBT_PROVIDER_TYPE, registry -> NbtProviders.CONTEXT);
    public static final Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_SCORE_PROVIDER_TYPE, registry -> ScoreboardNameProviders.CONTEXT);
    public static final Registry<FloatProviderType<?>> FLOAT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.FLOAT_PROVIDER_TYPE, registry -> FloatProviderType.CONSTANT);
    public static final Registry<IntProviderType<?>> INT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.INT_PROVIDER_TYPE, registry -> IntProviderType.CONSTANT);
    public static final Registry<HeightProviderType<?>> HEIGHT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.HEIGHT_PROVIDER_TYPE, registry -> HeightProviderType.CONSTANT);
    public static final Registry<BlockPredicateType<?>> BLOCK_PREDICATE_TYPE = BuiltInRegistries.registerSimple(Registries.BLOCK_PREDICATE_TYPE, registry -> BlockPredicateType.NOT);
    public static final Registry<WorldCarver<?>> CARVER = BuiltInRegistries.registerSimple(Registries.CARVER, registry -> WorldCarver.CAVE);
    public static final Registry<Feature<?>> FEATURE = BuiltInRegistries.registerSimple(Registries.FEATURE, registry -> Feature.ORE);
    public static final Registry<StructurePlacementType<?>> STRUCTURE_PLACEMENT = BuiltInRegistries.registerSimple(Registries.STRUCTURE_PLACEMENT, registry -> StructurePlacementType.RANDOM_SPREAD);
    public static final Registry<StructurePieceType> STRUCTURE_PIECE = BuiltInRegistries.registerSimple(Registries.STRUCTURE_PIECE, registry -> StructurePieceType.MINE_SHAFT_ROOM);
    public static final Registry<StructureType<?>> STRUCTURE_TYPE = BuiltInRegistries.registerSimple(Registries.STRUCTURE_TYPE, registry -> StructureType.JIGSAW);
    public static final Registry<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPE = BuiltInRegistries.registerSimple(Registries.PLACEMENT_MODIFIER_TYPE, registry -> PlacementModifierType.COUNT);
    public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.BLOCK_STATE_PROVIDER_TYPE, registry -> BlockStateProviderType.SIMPLE_STATE_PROVIDER);
    public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPE = BuiltInRegistries.registerSimple(Registries.FOLIAGE_PLACER_TYPE, registry -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = BuiltInRegistries.registerSimple(Registries.TRUNK_PLACER_TYPE, registry -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
    public static final Registry<RootPlacerType<?>> ROOT_PLACER_TYPE = BuiltInRegistries.registerSimple(Registries.ROOT_PLACER_TYPE, registry -> RootPlacerType.MANGROVE_ROOT_PLACER);
    public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPE = BuiltInRegistries.registerSimple(Registries.TREE_DECORATOR_TYPE, registry -> TreeDecoratorType.LEAVE_VINE);
    public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPE = BuiltInRegistries.registerSimple(Registries.FEATURE_SIZE_TYPE, registry -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
    public static final Registry<MapCodec<? extends BiomeSource>> BIOME_SOURCE = BuiltInRegistries.registerSimple(Registries.BIOME_SOURCE, BiomeSources::bootstrap);
    public static final Registry<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR = BuiltInRegistries.registerSimple(Registries.CHUNK_GENERATOR, ChunkGenerators::bootstrap);
    public static final Registry<MapCodec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITION = BuiltInRegistries.registerSimple(Registries.MATERIAL_CONDITION, SurfaceRules.ConditionSource::bootstrap);
    public static final Registry<MapCodec<? extends SurfaceRules.RuleSource>> MATERIAL_RULE = BuiltInRegistries.registerSimple(Registries.MATERIAL_RULE, SurfaceRules.RuleSource::bootstrap);
    public static final Registry<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = BuiltInRegistries.registerSimple(Registries.DENSITY_FUNCTION_TYPE, DensityFunctions::bootstrap);
    public static final Registry<MapCodec<? extends Block>> BLOCK_TYPE = BuiltInRegistries.registerSimple(Registries.BLOCK_TYPE, BlockTypes::bootstrap);
    public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = BuiltInRegistries.registerSimple(Registries.STRUCTURE_PROCESSOR, registry -> StructureProcessorType.BLOCK_IGNORE);
    public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = BuiltInRegistries.registerSimple(Registries.STRUCTURE_POOL_ELEMENT, registry -> StructurePoolElementType.EMPTY);
    public static final Registry<MapCodec<? extends PoolAliasBinding>> POOL_ALIAS_BINDING_TYPE = BuiltInRegistries.registerSimple(Registries.POOL_ALIAS_BINDING, PoolAliasBindings::bootstrap);
    public static final Registry<DecoratedPotPattern> DECORATED_POT_PATTERN = BuiltInRegistries.registerSimple(Registries.DECORATED_POT_PATTERN, DecoratedPotPatterns::bootstrap);
    public static final Registry<CreativeModeTab> CREATIVE_MODE_TAB = BuiltInRegistries.registerSimple(Registries.CREATIVE_MODE_TAB, CreativeModeTabs::bootstrap);
    public static final Registry<CriterionTrigger<?>> TRIGGER_TYPES = BuiltInRegistries.registerSimple(Registries.TRIGGER_TYPE, CriteriaTriggers::bootstrap);
    public static final Registry<NumberFormatType<?>> NUMBER_FORMAT_TYPE = BuiltInRegistries.registerSimple(Registries.NUMBER_FORMAT_TYPE, NumberFormatTypes::bootstrap);
    public static final Registry<DataComponentType<?>> DATA_COMPONENT_TYPE = BuiltInRegistries.registerSimple(Registries.DATA_COMPONENT_TYPE, DataComponents::bootstrap);
    public static final Registry<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATE_TYPE = BuiltInRegistries.registerSimple(Registries.ENTITY_SUB_PREDICATE_TYPE, EntitySubPredicates::bootstrap);
    public static final Registry<DataComponentPredicate.Type<?>> DATA_COMPONENT_PREDICATE_TYPE = BuiltInRegistries.registerSimple(Registries.DATA_COMPONENT_PREDICATE_TYPE, DataComponentPredicates::bootstrap);
    public static final Registry<MapDecorationType> MAP_DECORATION_TYPE = BuiltInRegistries.registerSimple(Registries.MAP_DECORATION_TYPE, MapDecorationTypes::bootstrap);
    public static final Registry<DataComponentType<?>> ENCHANTMENT_EFFECT_COMPONENT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, EnchantmentEffectComponents::bootstrap);
    public static final Registry<MapCodec<? extends LevelBasedValue>> ENCHANTMENT_LEVEL_BASED_VALUE_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, LevelBasedValue::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, EnchantmentEntityEffect::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentLocationBasedEffect>> ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, EnchantmentLocationBasedEffect::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentValueEffect>> ENCHANTMENT_VALUE_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, EnchantmentValueEffect::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentProvider>> ENCHANTMENT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_PROVIDER_TYPE, EnchantmentProviderTypes::bootstrap);
    public static final Registry<ConsumeEffect.Type<?>> CONSUME_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.CONSUME_EFFECT_TYPE, registry -> ConsumeEffect.Type.APPLY_EFFECTS);
    public static final Registry<RecipeDisplay.Type<?>> RECIPE_DISPLAY = BuiltInRegistries.registerSimple(Registries.RECIPE_DISPLAY, RecipeDisplays::bootstrap);
    public static final Registry<SlotDisplay.Type<?>> SLOT_DISPLAY = BuiltInRegistries.registerSimple(Registries.SLOT_DISPLAY, SlotDisplays::bootstrap);
    public static final Registry<RecipeBookCategory> RECIPE_BOOK_CATEGORY = BuiltInRegistries.registerSimple(Registries.RECIPE_BOOK_CATEGORY, RecipeBookCategories::bootstrap);
    public static final Registry<TicketType> TICKET_TYPE = BuiltInRegistries.registerSimple(Registries.TICKET_TYPE, registry -> TicketType.UNKNOWN);
    public static final Registry<MapCodec<? extends TestEnvironmentDefinition>> TEST_ENVIRONMENT_DEFINITION_TYPE = BuiltInRegistries.registerSimple(Registries.TEST_ENVIRONMENT_DEFINITION_TYPE, TestEnvironmentDefinition::bootstrap);
    public static final Registry<MapCodec<? extends GameTestInstance>> TEST_INSTANCE_TYPE = BuiltInRegistries.registerSimple(Registries.TEST_INSTANCE_TYPE, GameTestInstance::bootstrap);
    public static final Registry<MapCodec<? extends SpawnCondition>> SPAWN_CONDITION_TYPE = BuiltInRegistries.registerSimple(Registries.SPAWN_CONDITION_TYPE, SpawnConditions::bootstrap);
    public static final Registry<MapCodec<? extends Dialog>> DIALOG_TYPE = BuiltInRegistries.registerSimple(Registries.DIALOG_TYPE, DialogTypes::bootstrap);
    public static final Registry<MapCodec<? extends Action>> DIALOG_ACTION_TYPE = BuiltInRegistries.registerSimple(Registries.DIALOG_ACTION_TYPE, ActionTypes::bootstrap);
    public static final Registry<MapCodec<? extends InputControl>> INPUT_CONTROL_TYPE = BuiltInRegistries.registerSimple(Registries.INPUT_CONTROL_TYPE, InputControlTypes::bootstrap);
    public static final Registry<MapCodec<? extends DialogBody>> DIALOG_BODY_TYPE = BuiltInRegistries.registerSimple(Registries.DIALOG_BODY_TYPE, DialogBodyTypes::bootstrap);
    public static final Registry<Consumer<GameTestHelper>> TEST_FUNCTION = BuiltInRegistries.registerSimple(Registries.TEST_FUNCTION, BuiltinTestFunctions::bootstrap);
    public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, RegistryBootstrap<T> registryBootstrap) {
        return BuiltInRegistries.internalRegister(resourceKey, new MappedRegistry(resourceKey, Lifecycle.stable(), false), registryBootstrap);
    }

    private static <T> Registry<T> registerSimpleWithIntrusiveHolders(ResourceKey<? extends Registry<T>> resourceKey, RegistryBootstrap<T> registryBootstrap) {
        return BuiltInRegistries.internalRegister(resourceKey, new MappedRegistry(resourceKey, Lifecycle.stable(), true), registryBootstrap);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> resourceKey, String string, RegistryBootstrap<T> registryBootstrap) {
        return BuiltInRegistries.internalRegister(resourceKey, new DefaultedMappedRegistry(string, resourceKey, Lifecycle.stable(), false), registryBootstrap);
    }

    private static <T> DefaultedRegistry<T> registerDefaultedWithIntrusiveHolders(ResourceKey<? extends Registry<T>> resourceKey, String string, RegistryBootstrap<T> registryBootstrap) {
        return BuiltInRegistries.internalRegister(resourceKey, new DefaultedMappedRegistry(string, resourceKey, Lifecycle.stable(), true), registryBootstrap);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> resourceKey, R r, RegistryBootstrap<T> registryBootstrap) {
        Bootstrap.checkBootstrapCalled(() -> "registry " + String.valueOf(resourceKey.location()));
        ResourceLocation resourceLocation = resourceKey.location();
        LOADERS.put(resourceLocation, () -> registryBootstrap.run(r));
        WRITABLE_REGISTRY.register(resourceKey, r, RegistrationInfo.BUILT_IN);
        return r;
    }

    public static void bootStrap() {
        BuiltInRegistries.createContents();
        BuiltInRegistries.freeze();
        BuiltInRegistries.validate(REGISTRY);
    }

    private static void createContents() {
        LOADERS.forEach((resourceLocation, supplier) -> {
            if (supplier.get() == null) {
                LOGGER.error("Unable to bootstrap registry '{}'", resourceLocation);
            }
        });
    }

    private static void freeze() {
        REGISTRY.freeze();
        for (Registry registry : REGISTRY) {
            BuiltInRegistries.bindBootstrappedTagsToEmpty(registry);
            registry.freeze();
        }
    }

    private static <T extends Registry<?>> void validate(Registry<T> registry) {
        registry.forEach(registry2 -> {
            if (registry2.keySet().isEmpty()) {
                Util.logAndPauseIfInIde("Registry '" + String.valueOf(registry.getKey(registry2)) + "' was empty after loading");
            }
            if (registry2 instanceof DefaultedRegistry) {
                ResourceLocation resourceLocation = ((DefaultedRegistry)registry2).getDefaultKey();
                Validate.notNull(registry2.getValue(resourceLocation), (String)("Missing default of DefaultedMappedRegistry: " + String.valueOf(resourceLocation)), (Object[])new Object[0]);
            }
        });
    }

    public static <T> HolderGetter<T> acquireBootstrapRegistrationLookup(Registry<T> registry) {
        return ((WritableRegistry)registry).createRegistrationLookup();
    }

    private static void bindBootstrappedTagsToEmpty(Registry<?> registry) {
        ((MappedRegistry)registry).bindAllTagsToEmpty();
    }

    @FunctionalInterface
    static interface RegistryBootstrap<T> {
        public Object run(Registry<T> var1);
    }
}

