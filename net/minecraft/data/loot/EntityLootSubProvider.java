/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SheepPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public abstract class EntityLootSubProvider
implements LootTableSubProvider {
    protected final HolderLookup.Provider registries;
    private final FeatureFlagSet allowed;
    private final FeatureFlagSet required;
    private final Map<EntityType<?>, Map<ResourceKey<LootTable>, LootTable.Builder>> map = Maps.newHashMap();

    protected final AnyOfCondition.Builder shouldSmeltLoot() {
        HolderGetter holderGetter = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return AnyOfCondition.anyOf(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(holderGetter.getOrThrow(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY)))).build())))));
    }

    protected EntityLootSubProvider(FeatureFlagSet featureFlagSet, HolderLookup.Provider provider) {
        this(featureFlagSet, featureFlagSet, provider);
    }

    protected EntityLootSubProvider(FeatureFlagSet featureFlagSet, FeatureFlagSet featureFlagSet2, HolderLookup.Provider provider) {
        this.allowed = featureFlagSet;
        this.required = featureFlagSet2;
        this.registries = provider;
    }

    public static LootPool.Builder createSheepDispatchPool(Map<DyeColor, ResourceKey<LootTable>> map) {
        AlternativesEntry.Builder builder = AlternativesEntry.alternatives(new LootPoolEntryContainer.Builder[0]);
        for (Map.Entry<DyeColor, ResourceKey<LootTable>> entry : map.entrySet()) {
            builder = builder.otherwise((LootPoolEntryContainer.Builder<?>)NestedLootTable.lootTableReference(entry.getValue()).when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.SHEEP_COLOR, entry.getKey())).build()).subPredicate(SheepPredicate.hasWool()))));
        }
        return LootPool.lootPool().add(builder);
    }

    public abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        this.generate();
        HashSet hashSet = new HashSet();
        BuiltInRegistries.ENTITY_TYPE.listElements().forEach(reference -> {
            EntityType entityType = (EntityType)reference.value();
            if (!entityType.isEnabled(this.allowed)) {
                return;
            }
            Optional<ResourceKey<LootTable>> optional = entityType.getDefaultLootTable();
            if (optional.isPresent()) {
                Map<ResourceKey<LootTable>, LootTable.Builder> map = this.map.remove(entityType);
                if (entityType.isEnabled(this.required) && (map == null || !map.containsKey(optional.get()))) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", optional.get(), reference.key().location()));
                }
                if (map != null) {
                    map.forEach((resourceKey, builder) -> {
                        if (!hashSet.add(resourceKey)) {
                            throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", resourceKey, reference.key().location()));
                        }
                        biConsumer.accept((ResourceKey<LootTable>)resourceKey, (LootTable.Builder)builder);
                    });
                }
            } else {
                Map<ResourceKey<LootTable>, LootTable.Builder> map = this.map.remove(entityType);
                if (map != null) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot", map.keySet().stream().map(resourceKey -> resourceKey.location().toString()).collect(Collectors.joining(",")), reference.key().location()));
                }
            }
        });
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + String.valueOf(this.map.keySet()));
        }
    }

    protected LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> holderGetter) {
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(holderGetter, EntityType.FROG)));
    }

    protected LootItemCondition.Builder killedByFrogVariant(HolderGetter<EntityType<?>> holderGetter, HolderGetter<FrogVariant> holderGetter2, ResourceKey<FrogVariant> resourceKey) {
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(holderGetter, EntityType.FROG).components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT, holderGetter2.getOrThrow(resourceKey))).build())));
    }

    protected void add(EntityType<?> entityType, LootTable.Builder builder) {
        this.add(entityType, entityType.getDefaultLootTable().orElseThrow(() -> new IllegalStateException("Entity " + String.valueOf(entityType) + " has no loot table")), builder);
    }

    protected void add(EntityType<?> entityType2, ResourceKey<LootTable> resourceKey, LootTable.Builder builder) {
        this.map.computeIfAbsent(entityType2, entityType -> new HashMap()).put(resourceKey, builder);
    }
}

