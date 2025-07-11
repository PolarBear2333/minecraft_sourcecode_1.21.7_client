/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.data.advancements.packs;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ChanneledLightningTrigger;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FallAfterExplosionTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledByArrowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LightningBoltPredicate;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.SlideDownBlockTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.advancements.critereon.TargetBlockTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.JukeboxPlayablePredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.advancements.packs.VanillaHusbandryAdvancements;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class VanillaAdventureAdvancements
implements AdvancementSubProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DISTANCE_FROM_BOTTOM_TO_TOP = 384;
    private static final int Y_COORDINATE_AT_TOP = 320;
    private static final int Y_COORDINATE_AT_BOTTOM = -64;
    private static final int BEDROCK_THICKNESS = 5;
    private static final Map<MobCategory, Set<EntityType<?>>> EXCEPTIONS_BY_EXPECTED_CATEGORIES = Map.of(MobCategory.MONSTER, Set.of(EntityType.GIANT, EntityType.ILLUSIONER, EntityType.WARDEN));
    private static final List<EntityType<?>> MOBS_TO_KILL = Arrays.asList(EntityType.BLAZE, EntityType.BOGGED, EntityType.BREEZE, EntityType.CAVE_SPIDER, EntityType.CREAKING, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN);

    private static Criterion<LightningStrikeTrigger.TriggerInstance> fireCountAndBystander(MinMaxBounds.Ints ints, Optional<EntityPredicate> optional) {
        return LightningStrikeTrigger.TriggerInstance.lightningStrike(Optional.of(EntityPredicate.Builder.entity().distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(30.0))).subPredicate(LightningBoltPredicate.blockSetOnFire(ints)).build()), optional);
    }

    private static Criterion<UsingItemTrigger.TriggerInstance> lookAtThroughItem(EntityPredicate.Builder builder, ItemPredicate.Builder builder2) {
        return UsingItemTrigger.TriggerInstance.lookingAt(EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().setLookingAt(builder).build()), builder2);
    }

    @Override
    public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer) {
        HolderGetter holderGetter = provider.lookupOrThrow(Registries.ENTITY_TYPE);
        HolderGetter holderGetter2 = provider.lookupOrThrow(Registries.ITEM);
        HolderGetter holderGetter3 = provider.lookupOrThrow(Registries.BLOCK);
        AdvancementHolder advancementHolder = Advancement.Builder.advancement().display(Items.MAP, (Component)Component.translatable("advancements.adventure.root.title"), (Component)Component.translatable("advancements.adventure.root.description"), ResourceLocation.withDefaultNamespace("gui/advancements/backgrounds/adventure"), AdvancementType.TASK, false, false, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity()).addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer()).save(consumer, "adventure/root");
        AdvancementHolder advancementHolder2 = Advancement.Builder.advancement().parent(advancementHolder).display(Blocks.RED_BED, (Component)Component.translatable("advancements.adventure.sleep_in_bed.title"), (Component)Component.translatable("advancements.adventure.sleep_in_bed.description"), null, AdvancementType.TASK, true, true, false).addCriterion("slept_in_bed", PlayerTrigger.TriggerInstance.sleptInBed()).save(consumer, "adventure/sleep_in_bed");
        VanillaAdventureAdvancements.createAdventuringTime(provider, consumer, advancementHolder2, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);
        AdvancementHolder advancementHolder3 = Advancement.Builder.advancement().parent(advancementHolder).display(Items.EMERALD, (Component)Component.translatable("advancements.adventure.trade.title"), (Component)Component.translatable("advancements.adventure.trade.description"), null, AdvancementType.TASK, true, true, false).addCriterion("traded", TradeTrigger.TriggerInstance.tradedWithVillager()).save(consumer, "adventure/trade");
        Advancement.Builder.advancement().parent(advancementHolder3).display(Items.EMERALD, (Component)Component.translatable("advancements.adventure.trade_at_world_height.title"), (Component)Component.translatable("advancements.adventure.trade_at_world_height.description"), null, AdvancementType.TASK, true, true, false).addCriterion("trade_at_world_height", TradeTrigger.TriggerInstance.tradedWithVillager(EntityPredicate.Builder.entity().located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0))))).save(consumer, "adventure/trade_at_world_height");
        AdvancementHolder advancementHolder4 = VanillaAdventureAdvancements.createMonsterHunterAdvancement(advancementHolder, consumer, holderGetter, VanillaAdventureAdvancements.validateMobsToKill(MOBS_TO_KILL, holderGetter));
        AdvancementHolder advancementHolder5 = Advancement.Builder.advancement().parent(advancementHolder4).display(Items.BOW, (Component)Component.translatable("advancements.adventure.shoot_arrow.title"), (Component)Component.translatable("advancements.adventure.shoot_arrow.description"), null, AdvancementType.TASK, true, true, false).addCriterion("shot_arrow", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of(holderGetter, EntityTypeTags.ARROWS))))).save(consumer, "adventure/shoot_arrow");
        AdvancementHolder advancementHolder6 = Advancement.Builder.advancement().parent(advancementHolder4).display(Items.TRIDENT, (Component)Component.translatable("advancements.adventure.throw_trident.title"), (Component)Component.translatable("advancements.adventure.throw_trident.description"), null, AdvancementType.TASK, true, true, false).addCriterion("shot_trident", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of(holderGetter, EntityType.TRIDENT))))).save(consumer, "adventure/throw_trident");
        Advancement.Builder.advancement().parent(advancementHolder6).display(Items.TRIDENT, (Component)Component.translatable("advancements.adventure.very_very_frightening.title"), (Component)Component.translatable("advancements.adventure.very_very_frightening.description"), null, AdvancementType.TASK, true, true, false).addCriterion("struck_villager", ChanneledLightningTrigger.TriggerInstance.channeledLightning(EntityPredicate.Builder.entity().of(holderGetter, EntityType.VILLAGER))).save(consumer, "adventure/very_very_frightening");
        Advancement.Builder.advancement().parent(advancementHolder3).display(Blocks.CARVED_PUMPKIN, (Component)Component.translatable("advancements.adventure.summon_iron_golem.title"), (Component)Component.translatable("advancements.adventure.summon_iron_golem.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("summoned_golem", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(holderGetter, EntityType.IRON_GOLEM))).save(consumer, "adventure/summon_iron_golem");
        Advancement.Builder.advancement().parent(advancementHolder5).display(Items.ARROW, (Component)Component.translatable("advancements.adventure.sniper_duel.title"), (Component)Component.translatable("advancements.adventure.sniper_duel.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("killed_skeleton", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(holderGetter, EntityType.SKELETON).distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(50.0))), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)))).save(consumer, "adventure/sniper_duel");
        Advancement.Builder.advancement().parent(advancementHolder4).display(Items.TOTEM_OF_UNDYING, (Component)Component.translatable("advancements.adventure.totem_of_undying.title"), (Component)Component.translatable("advancements.adventure.totem_of_undying.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("used_totem", UsedTotemTrigger.TriggerInstance.usedTotem(holderGetter2, Items.TOTEM_OF_UNDYING)).save(consumer, "adventure/totem_of_undying");
        AdvancementHolder advancementHolder7 = Advancement.Builder.advancement().parent(advancementHolder).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.ol_betsy.title"), (Component)Component.translatable("advancements.adventure.ol_betsy.description"), null, AdvancementType.TASK, true, true, false).addCriterion("shot_crossbow", ShotCrossbowTrigger.TriggerInstance.shotCrossbow(holderGetter2, Items.CROSSBOW)).save(consumer, "adventure/ol_betsy");
        Advancement.Builder.advancement().parent(advancementHolder7).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.whos_the_pillager_now.title"), (Component)Component.translatable("advancements.adventure.whos_the_pillager_now.description"), null, AdvancementType.TASK, true, true, false).addCriterion("kill_pillager", KilledByArrowTrigger.TriggerInstance.crossbowKilled((HolderGetter<Item>)holderGetter2, EntityPredicate.Builder.entity().of(holderGetter, EntityType.PILLAGER))).save(consumer, "adventure/whos_the_pillager_now");
        Advancement.Builder.advancement().parent(advancementHolder7).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.two_birds_one_arrow.title"), (Component)Component.translatable("advancements.adventure.two_birds_one_arrow.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(65)).addCriterion("two_birds", KilledByArrowTrigger.TriggerInstance.crossbowKilled((HolderGetter<Item>)holderGetter2, EntityPredicate.Builder.entity().of(holderGetter, EntityType.PHANTOM), EntityPredicate.Builder.entity().of(holderGetter, EntityType.PHANTOM))).save(consumer, "adventure/two_birds_one_arrow");
        Advancement.Builder.advancement().parent(advancementHolder7).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.arbalistic.title"), (Component)Component.translatable("advancements.adventure.arbalistic.description"), null, AdvancementType.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(85)).addCriterion("arbalistic", KilledByArrowTrigger.TriggerInstance.crossbowKilled((HolderGetter<Item>)holderGetter2, MinMaxBounds.Ints.exactly(5))).save(consumer, "adventure/arbalistic");
        HolderGetter holderGetter4 = provider.lookupOrThrow(Registries.BANNER_PATTERN);
        AdvancementHolder advancementHolder8 = Advancement.Builder.advancement().parent(advancementHolder).display(Raid.getOminousBannerInstance(holderGetter4), (Component)Component.translatable("advancements.adventure.voluntary_exile.title"), (Component)Component.translatable("advancements.adventure.voluntary_exile.description"), null, AdvancementType.TASK, true, true, true).addCriterion("voluntary_exile", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(holderGetter, EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.captainPredicate(holderGetter2, holderGetter4)))).save(consumer, "adventure/voluntary_exile");
        Advancement.Builder.advancement().parent(advancementHolder8).display(Raid.getOminousBannerInstance(holderGetter4), (Component)Component.translatable("advancements.adventure.hero_of_the_village.title"), (Component)Component.translatable("advancements.adventure.hero_of_the_village.description"), null, AdvancementType.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(100)).addCriterion("hero_of_the_village", PlayerTrigger.TriggerInstance.raidWon()).save(consumer, "adventure/hero_of_the_village");
        Advancement.Builder.advancement().parent(advancementHolder).display(Blocks.HONEY_BLOCK.asItem(), (Component)Component.translatable("advancements.adventure.honey_block_slide.title"), (Component)Component.translatable("advancements.adventure.honey_block_slide.description"), null, AdvancementType.TASK, true, true, false).addCriterion("honey_block_slide", SlideDownBlockTrigger.TriggerInstance.slidesDownBlock(Blocks.HONEY_BLOCK)).save(consumer, "adventure/honey_block_slide");
        Advancement.Builder.advancement().parent(advancementHolder5).display(Blocks.TARGET.asItem(), (Component)Component.translatable("advancements.adventure.bullseye.title"), (Component)Component.translatable("advancements.adventure.bullseye.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("bullseye", TargetBlockTrigger.TriggerInstance.targetHit(MinMaxBounds.Ints.exactly(15), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(30.0))))))).save(consumer, "adventure/bullseye");
        Advancement.Builder.advancement().parent(advancementHolder2).display(Items.LEATHER_BOOTS, (Component)Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"), (Component)Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"), null, AdvancementType.TASK, true, true, false).addCriterion("walk_on_powder_snow_with_leather_boots", PlayerTrigger.TriggerInstance.walkOnBlockWithEquipment(holderGetter3, holderGetter2, Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)).save(consumer, "adventure/walk_on_powder_snow_with_leather_boots");
        Advancement.Builder.advancement().parent(advancementHolder).display(Items.LIGHTNING_ROD, (Component)Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.title"), (Component)Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.description"), null, AdvancementType.TASK, true, true, false).addCriterion("lightning_rod_with_villager_no_fire", VanillaAdventureAdvancements.fireCountAndBystander(MinMaxBounds.Ints.exactly(0), Optional.of(EntityPredicate.Builder.entity().of(holderGetter, EntityType.VILLAGER).build()))).save(consumer, "adventure/lightning_rod_with_villager_no_fire");
        AdvancementHolder advancementHolder9 = Advancement.Builder.advancement().parent(advancementHolder).display(Items.SPYGLASS, (Component)Component.translatable("advancements.adventure.spyglass_at_parrot.title"), (Component)Component.translatable("advancements.adventure.spyglass_at_parrot.description"), null, AdvancementType.TASK, true, true, false).addCriterion("spyglass_at_parrot", VanillaAdventureAdvancements.lookAtThroughItem(EntityPredicate.Builder.entity().of(holderGetter, EntityType.PARROT), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.SPYGLASS))).save(consumer, "adventure/spyglass_at_parrot");
        AdvancementHolder advancementHolder10 = Advancement.Builder.advancement().parent(advancementHolder9).display(Items.SPYGLASS, (Component)Component.translatable("advancements.adventure.spyglass_at_ghast.title"), (Component)Component.translatable("advancements.adventure.spyglass_at_ghast.description"), null, AdvancementType.TASK, true, true, false).addCriterion("spyglass_at_ghast", VanillaAdventureAdvancements.lookAtThroughItem(EntityPredicate.Builder.entity().of(holderGetter, EntityType.GHAST), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.SPYGLASS))).save(consumer, "adventure/spyglass_at_ghast");
        Advancement.Builder.advancement().parent(advancementHolder2).display(Items.JUKEBOX, (Component)Component.translatable("advancements.adventure.play_jukebox_in_meadows.title"), (Component)Component.translatable("advancements.adventure.play_jukebox_in_meadows.description"), null, AdvancementType.TASK, true, true, false).addCriterion("play_jukebox_in_meadows", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBiomes(HolderSet.direct(provider.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.MEADOW))).setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)holderGetter3, Blocks.JUKEBOX)), ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.JUKEBOX_PLAYABLE, JukeboxPlayablePredicate.any()).build()))).save(consumer, "adventure/play_jukebox_in_meadows");
        Advancement.Builder.advancement().parent(advancementHolder10).display(Items.SPYGLASS, (Component)Component.translatable("advancements.adventure.spyglass_at_dragon.title"), (Component)Component.translatable("advancements.adventure.spyglass_at_dragon.description"), null, AdvancementType.TASK, true, true, false).addCriterion("spyglass_at_dragon", VanillaAdventureAdvancements.lookAtThroughItem(EntityPredicate.Builder.entity().of(holderGetter, EntityType.ENDER_DRAGON), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.SPYGLASS))).save(consumer, "adventure/spyglass_at_dragon");
        Advancement.Builder.advancement().parent(advancementHolder).display(Items.WATER_BUCKET, (Component)Component.translatable("advancements.adventure.fall_from_world_height.title"), (Component)Component.translatable("advancements.adventure.fall_from_world_height.description"), null, AdvancementType.TASK, true, true, false).addCriterion("fall_from_world_height", DistanceTrigger.TriggerInstance.fallFromHeight(EntityPredicate.Builder.entity().located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atMost(-59.0))), DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(379.0)), LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0)))).save(consumer, "adventure/fall_from_world_height");
        Advancement.Builder.advancement().parent(advancementHolder4).display(Blocks.SCULK_CATALYST, (Component)Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.title"), (Component)Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.description"), null, AdvancementType.CHALLENGE, true, true, false).addCriterion("kill_mob_near_sculk_catalyst", KilledTrigger.TriggerInstance.playerKilledEntityNearSculkCatalyst()).save(consumer, "adventure/kill_mob_near_sculk_catalyst");
        Advancement.Builder.advancement().parent(advancementHolder).display(Blocks.SCULK_SENSOR, (Component)Component.translatable("advancements.adventure.avoid_vibration.title"), (Component)Component.translatable("advancements.adventure.avoid_vibration.description"), null, AdvancementType.TASK, true, true, false).addCriterion("avoid_vibration", PlayerTrigger.TriggerInstance.avoidVibration()).save(consumer, "adventure/avoid_vibration");
        AdvancementHolder advancementHolder11 = VanillaAdventureAdvancements.respectingTheRemnantsCriterions(holderGetter2, Advancement.Builder.advancement()).parent(advancementHolder).display(Items.BRUSH, (Component)Component.translatable("advancements.adventure.salvage_sherd.title"), (Component)Component.translatable("advancements.adventure.salvage_sherd.description"), null, AdvancementType.TASK, true, true, false).save(consumer, "adventure/salvage_sherd");
        Advancement.Builder.advancement().parent(advancementHolder11).display(DecoratedPotBlockEntity.createDecoratedPotItem(new PotDecorations(Optional.empty(), Optional.of(Items.HEART_POTTERY_SHERD), Optional.empty(), Optional.of(Items.EXPLORER_POTTERY_SHERD))), (Component)Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.title"), (Component)Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.description"), null, AdvancementType.TASK, true, true, false).addCriterion("pot_crafted_using_only_sherds", RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("decorated_pot")), List.of(ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, ItemTags.DECORATED_POT_SHERDS)))).save(consumer, "adventure/craft_decorated_pot_using_only_sherds");
        AdvancementHolder advancementHolder12 = VanillaAdventureAdvancements.craftingANewLook(Advancement.Builder.advancement()).parent(advancementHolder).display(new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE), (Component)Component.translatable("advancements.adventure.trim_with_any_armor_pattern.title"), (Component)Component.translatable("advancements.adventure.trim_with_any_armor_pattern.description"), null, AdvancementType.TASK, true, true, false).save(consumer, "adventure/trim_with_any_armor_pattern");
        VanillaAdventureAdvancements.smithingWithStyle(Advancement.Builder.advancement()).parent(advancementHolder12).display(new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), (Component)Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.title"), (Component)Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(150)).save(consumer, "adventure/trim_with_all_exclusive_armor_patterns");
        Advancement.Builder.advancement().parent(advancementHolder).display(Items.CHISELED_BOOKSHELF, (Component)Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.title"), (Component)Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("chiseled_bookshelf", VanillaAdventureAdvancements.placedBlockReadByComparator(holderGetter3, Blocks.CHISELED_BOOKSHELF)).addCriterion("comparator", VanillaAdventureAdvancements.placedComparatorReadingBlock(holderGetter3, Blocks.CHISELED_BOOKSHELF)).save(consumer, "adventure/read_power_of_chiseled_bookshelf");
        Advancement.Builder.advancement().parent(advancementHolder).display(Items.ARMADILLO_SCUTE, (Component)Component.translatable("advancements.adventure.brush_armadillo.title"), (Component)Component.translatable("advancements.adventure.brush_armadillo.description"), null, AdvancementType.TASK, true, true, false).addCriterion("brush_armadillo", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.BRUSH), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(holderGetter, EntityType.ARMADILLO))))).save(consumer, "adventure/brush_armadillo");
        AdvancementHolder advancementHolder13 = Advancement.Builder.advancement().parent(advancementHolder).display(Blocks.CHISELED_TUFF, (Component)Component.translatable("advancements.adventure.minecraft_trials_edition.title"), (Component)Component.translatable("advancements.adventure.minecraft_trials_edition.description"), null, AdvancementType.TASK, true, true, false).addCriterion("minecraft_trials_edition", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(provider.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.TRIAL_CHAMBERS)))).save(consumer, "adventure/minecraft_trials_edition");
        Advancement.Builder.advancement().parent(advancementHolder13).display(Items.COPPER_BULB, (Component)Component.translatable("advancements.adventure.lighten_up.title"), (Component)Component.translatable("advancements.adventure.lighten_up.description"), null, AdvancementType.TASK, true, true, false).addCriterion("lighten_up", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)holderGetter3, Blocks.OXIDIZED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB, Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CopperBulbBlock.LIT, true))), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, VanillaHusbandryAdvancements.WAX_SCRAPING_TOOLS))).save(consumer, "adventure/lighten_up");
        AdvancementHolder advancementHolder14 = Advancement.Builder.advancement().parent(advancementHolder13).display(Items.TRIAL_KEY, (Component)Component.translatable("advancements.adventure.under_lock_and_key.title"), (Component)Component.translatable("advancements.adventure.under_lock_and_key.description"), null, AdvancementType.TASK, true, true, false).addCriterion("under_lock_and_key", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)holderGetter3, Blocks.VAULT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VaultBlock.OMINOUS, false))), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.TRIAL_KEY))).save(consumer, "adventure/under_lock_and_key");
        Advancement.Builder.advancement().parent(advancementHolder14).display(Items.OMINOUS_TRIAL_KEY, (Component)Component.translatable("advancements.adventure.revaulting.title"), (Component)Component.translatable("advancements.adventure.revaulting.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("revaulting", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)holderGetter3, Blocks.VAULT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VaultBlock.OMINOUS, true))), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.OMINOUS_TRIAL_KEY))).save(consumer, "adventure/revaulting");
        Advancement.Builder.advancement().parent(advancementHolder13).display(Items.WIND_CHARGE, (Component)Component.translatable("advancements.adventure.blowback.title"), (Component)Component.translatable("advancements.adventure.blowback.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(40)).addCriterion("blowback", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(holderGetter, EntityType.BREEZE), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of(holderGetter, EntityType.BREEZE_WIND_CHARGE)))).save(consumer, "adventure/blowback");
        Advancement.Builder.advancement().parent(advancementHolder).display(Items.CRAFTER, (Component)Component.translatable("advancements.adventure.crafters_crafting_crafters.title"), (Component)Component.translatable("advancements.adventure.crafters_crafting_crafters.description"), null, AdvancementType.TASK, true, true, false).addCriterion("crafter_crafted_crafter", RecipeCraftedTrigger.TriggerInstance.crafterCraftedItem(ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("crafter")))).save(consumer, "adventure/crafters_crafting_crafters");
        Advancement.Builder.advancement().parent(advancementHolder).display(Items.LODESTONE, (Component)Component.translatable("advancements.adventure.use_lodestone.title"), (Component)Component.translatable("advancements.adventure.use_lodestone.description"), null, AdvancementType.TASK, true, true, false).addCriterion("use_lodestone", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)holderGetter3, Blocks.LODESTONE)), ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.COMPASS))).save(consumer, "adventure/use_lodestone");
        Advancement.Builder.advancement().parent(advancementHolder13).display(Items.WIND_CHARGE, (Component)Component.translatable("advancements.adventure.who_needs_rockets.title"), (Component)Component.translatable("advancements.adventure.who_needs_rockets.description"), null, AdvancementType.TASK, true, true, false).addCriterion("who_needs_rockets", FallAfterExplosionTrigger.TriggerInstance.fallAfterExplosion(DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(7.0)), EntityPredicate.Builder.entity().of(holderGetter, EntityType.WIND_CHARGE))).save(consumer, "adventure/who_needs_rockets");
        Advancement.Builder.advancement().parent(advancementHolder13).display(Items.MACE, (Component)Component.translatable("advancements.adventure.overoverkill.title"), (Component)Component.translatable("advancements.adventure.overoverkill.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("overoverkill", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(DamagePredicate.Builder.damageInstance().dealtDamage(MinMaxBounds.Doubles.atLeast(100.0)).type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_MACE_SMASH)).direct(EntityPredicate.Builder.entity().of(holderGetter, EntityType.PLAYER).equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().of((HolderGetter<Item>)holderGetter2, Items.MACE))))))).save(consumer, "adventure/overoverkill");
        Advancement.Builder.advancement().parent(advancementHolder).display(Blocks.CREAKING_HEART, (Component)Component.translatable("advancements.adventure.heart_transplanter.title"), (Component)Component.translatable("advancements.adventure.heart_transplanter.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("place_creaking_heart_dormant", ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(Blocks.CREAKING_HEART, BlockStateProperties.CREAKING_HEART_STATE, CreakingHeartState.DORMANT)).addCriterion("place_creaking_heart_awake", ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(Blocks.CREAKING_HEART, BlockStateProperties.CREAKING_HEART_STATE, CreakingHeartState.AWAKE)).addCriterion("place_pale_oak_log", VanillaAdventureAdvancements.placedBlockActivatesCreakingHeart(holderGetter3, BlockTags.PALE_OAK_LOGS)).save(consumer, "adventure/heart_transplanter");
    }

    public static AdvancementHolder createMonsterHunterAdvancement(AdvancementHolder advancementHolder, Consumer<AdvancementHolder> consumer, HolderGetter<EntityType<?>> holderGetter, List<EntityType<?>> list) {
        AdvancementHolder advancementHolder2 = VanillaAdventureAdvancements.addMobsToKill(Advancement.Builder.advancement(), holderGetter, list).parent(advancementHolder).display(Items.IRON_SWORD, (Component)Component.translatable("advancements.adventure.kill_a_mob.title"), (Component)Component.translatable("advancements.adventure.kill_a_mob.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).save(consumer, "adventure/kill_a_mob");
        VanillaAdventureAdvancements.addMobsToKill(Advancement.Builder.advancement(), holderGetter, list).parent(advancementHolder2).display(Items.DIAMOND_SWORD, (Component)Component.translatable("advancements.adventure.kill_all_mobs.title"), (Component)Component.translatable("advancements.adventure.kill_all_mobs.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save(consumer, "adventure/kill_all_mobs");
        return advancementHolder2;
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockReadByComparator(HolderGetter<Block> holderGetter, Block block) {
        LootItemCondition.Builder[] builderArray = (LootItemCondition.Builder[])ComparatorBlock.FACING.getPossibleValues().stream().map(direction -> {
            StatePropertiesPredicate.Builder builder = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, direction);
            BlockPredicate.Builder builder2 = BlockPredicate.Builder.block().of(holderGetter, Blocks.COMPARATOR).setProperties(builder);
            return LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(builder2), new BlockPos(direction.getOpposite().getUnitVec3i()));
        }).toArray(LootItemCondition.Builder[]::new);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block), AnyOfCondition.anyOf(builderArray));
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedComparatorReadingBlock(HolderGetter<Block> holderGetter, Block block) {
        LootItemCondition.Builder[] builderArray = (LootItemCondition.Builder[])ComparatorBlock.FACING.getPossibleValues().stream().map(direction -> {
            StatePropertiesPredicate.Builder builder = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, direction);
            LootItemBlockStatePropertyCondition.Builder builder2 = new LootItemBlockStatePropertyCondition.Builder(Blocks.COMPARATOR).setProperties(builder);
            LootItemCondition.Builder builder3 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(holderGetter, block)), new BlockPos(direction.getUnitVec3i()));
            return AllOfCondition.allOf(builder2, builder3);
        }).toArray(LootItemCondition.Builder[]::new);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AnyOfCondition.anyOf(builderArray));
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockActivatesCreakingHeart(HolderGetter<Block> holderGetter, TagKey<Block> tagKey) {
        LootItemCondition.Builder[] builderArray = (LootItemCondition.Builder[])Stream.of(Direction.values()).map(direction -> {
            StatePropertiesPredicate.Builder builder = StatePropertiesPredicate.Builder.properties().hasProperty(CreakingHeartBlock.AXIS, direction.getAxis());
            BlockPredicate.Builder builder2 = BlockPredicate.Builder.block().of(holderGetter, tagKey).setProperties(builder);
            Vec3i vec3i = direction.getUnitVec3i();
            LootItemCondition.Builder builder3 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(builder2));
            LootItemCondition.Builder builder4 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(holderGetter, Blocks.CREAKING_HEART).setProperties(builder)), new BlockPos(vec3i));
            LootItemCondition.Builder builder5 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(builder2), new BlockPos(vec3i.multiply(2)));
            return AllOfCondition.allOf(builder3, builder4, builder5);
        }).toArray(LootItemCondition.Builder[]::new);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AnyOfCondition.anyOf(builderArray));
    }

    private static Advancement.Builder smithingWithStyle(Advancement.Builder builder) {
        builder.requirements(AdvancementRequirements.Strategy.AND);
        Set<Item> set = Set.of(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
        VanillaRecipeProvider.smithingTrims().filter(trimTemplate -> set.contains(trimTemplate.template())).forEach(trimTemplate -> builder.addCriterion("armor_trimmed_" + String.valueOf(trimTemplate.recipeId().location()), RecipeCraftedTrigger.TriggerInstance.craftedItem(trimTemplate.recipeId())));
        return builder;
    }

    private static Advancement.Builder craftingANewLook(Advancement.Builder builder) {
        builder.requirements(AdvancementRequirements.Strategy.OR);
        VanillaRecipeProvider.smithingTrims().map(VanillaRecipeProvider.TrimTemplate::recipeId).forEach(resourceKey -> builder.addCriterion("armor_trimmed_" + String.valueOf(resourceKey.location()), RecipeCraftedTrigger.TriggerInstance.craftedItem(resourceKey)));
        return builder;
    }

    private static Advancement.Builder respectingTheRemnantsCriterions(HolderGetter<Item> holderGetter, Advancement.Builder builder) {
        List<Pair> list = List.of(Pair.of((Object)"desert_pyramid", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY)), Pair.of((Object)"desert_well", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)), Pair.of((Object)"ocean_ruin_cold", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY)), Pair.of((Object)"ocean_ruin_warm", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY)), Pair.of((Object)"trail_ruins_rare", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE)), Pair.of((Object)"trail_ruins_common", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON)));
        list.forEach(pair -> builder.addCriterion((String)pair.getFirst(), (Criterion)pair.getSecond()));
        String string = "has_sherd";
        builder.addCriterion("has_sherd", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(holderGetter, ItemTags.DECORATED_POT_SHERDS)));
        builder.requirements(new AdvancementRequirements(List.of(list.stream().map(Pair::getFirst).toList(), List.of("has_sherd"))));
        return builder;
    }

    protected static void createAdventuringTime(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer, AdvancementHolder advancementHolder, MultiNoiseBiomeSourceParameterList.Preset preset) {
        VanillaAdventureAdvancements.addBiomes(Advancement.Builder.advancement(), provider, preset.usedBiomes().toList()).parent(advancementHolder).display(Items.DIAMOND_BOOTS, (Component)Component.translatable("advancements.adventure.adventuring_time.title"), (Component)Component.translatable("advancements.adventure.adventuring_time.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(500)).save(consumer, "adventure/adventuring_time");
    }

    private static Advancement.Builder addMobsToKill(Advancement.Builder builder, HolderGetter<EntityType<?>> holderGetter, List<EntityType<?>> list) {
        list.forEach(entityType -> builder.addCriterion(BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)entityType).toString(), KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(holderGetter, (EntityType<?>)entityType))));
        return builder;
    }

    protected static Advancement.Builder addBiomes(Advancement.Builder builder, HolderLookup.Provider provider, List<ResourceKey<Biome>> list) {
        HolderGetter holderGetter = provider.lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> resourceKey : list) {
            builder.addCriterion(resourceKey.location().toString(), PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome(holderGetter.getOrThrow(resourceKey))));
        }
        return builder;
    }

    private static List<EntityType<?>> validateMobsToKill(List<EntityType<?>> list, HolderLookup<EntityType<?>> holderLookup) {
        Sets.SetView setView;
        ArrayList<String> arrayList = new ArrayList<String>();
        Set<EntityType<?>> set2 = Set.copyOf(list);
        Set set3 = set2.stream().map(EntityType::getCategory).collect(Collectors.toSet());
        Sets.SetView setView2 = Sets.symmetricDifference(EXCEPTIONS_BY_EXPECTED_CATEGORIES.keySet(), set3);
        if (!setView2.isEmpty()) {
            arrayList.add("Found EntityType with MobCategory only in either expected exceptions or kill_all_mobs advancement: %s".formatted(setView2.stream().map(Object::toString).sorted().collect(Collectors.joining(", "))));
        }
        if (!(setView = Sets.intersection(EXCEPTIONS_BY_EXPECTED_CATEGORIES.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()), set2)).isEmpty()) {
            arrayList.add("Found EntityType in both expected exceptions and kill_all_mobs advancement: %s".formatted(setView.stream().map(Object::toString).sorted().collect(Collectors.joining(", "))));
        }
        Map map = holderLookup.listElements().map(Holder.Reference::value).filter(Predicate.not(set2::contains)).collect(Collectors.groupingBy(EntityType::getCategory, Collectors.toSet()));
        EXCEPTIONS_BY_EXPECTED_CATEGORIES.forEach((mobCategory, set) -> {
            Sets.SetView setView = Sets.difference(map.getOrDefault(mobCategory, Set.of()), (Set)set);
            if (!setView.isEmpty()) {
                arrayList.add("Found (new?) EntityType with MobCategory %s which are in neither expected exceptions nor kill_all_mobs advancement: %s".formatted(mobCategory, setView.stream().map(Object::toString).sorted().collect(Collectors.joining(", "))));
            }
        });
        if (!arrayList.isEmpty()) {
            arrayList.forEach(arg_0 -> ((Logger)LOGGER).error(arg_0));
            throw new IllegalStateException("Found inconsistencies with kill_all_mobs advancement");
        }
        return list;
    }
}

