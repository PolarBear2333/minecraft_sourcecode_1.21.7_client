/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public record VanillaEquipmentLoot(HolderLookup.Provider registries) implements LootTableSubProvider
{
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        HolderGetter holderGetter = this.registries.lookupOrThrow(Registries.TRIM_PATTERN);
        HolderGetter holderGetter2 = this.registries.lookupOrThrow(Registries.TRIM_MATERIAL);
        HolderGetter holderGetter3 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        ArmorTrim armorTrim = new ArmorTrim(holderGetter2.getOrThrow(TrimMaterials.COPPER), holderGetter.getOrThrow(TrimPatterns.FLOW));
        ArmorTrim armorTrim2 = new ArmorTrim(holderGetter2.getOrThrow(TrimMaterials.COPPER), holderGetter.getOrThrow(TrimPatterns.BOLT));
        biConsumer.accept(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)NestedLootTable.inlineLootTable(VanillaEquipmentLoot.trialChamberEquipment(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, armorTrim2, (HolderLookup.RegistryLookup<Enchantment>)holderGetter3).build()).setWeight(4)).add((LootPoolEntryContainer.Builder<?>)NestedLootTable.inlineLootTable(VanillaEquipmentLoot.trialChamberEquipment(Items.IRON_HELMET, Items.IRON_CHESTPLATE, armorTrim, (HolderLookup.RegistryLookup<Enchantment>)holderGetter3).build()).setWeight(2)).add((LootPoolEntryContainer.Builder<?>)NestedLootTable.inlineLootTable(VanillaEquipmentLoot.trialChamberEquipment(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, armorTrim, (HolderLookup.RegistryLookup<Enchantment>)holderGetter3).build()).setWeight(1))));
        biConsumer.accept(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(NestedLootTable.lootTableReference(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.IRON_SWORD).setWeight(4)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.IRON_SWORD).apply(new SetEnchantmentsFunction.Builder().withEnchantment(holderGetter3.getOrThrow(Enchantments.SHARPNESS), ConstantValue.exactly(1.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.IRON_SWORD).apply(new SetEnchantmentsFunction.Builder().withEnchantment(holderGetter3.getOrThrow(Enchantments.KNOCKBACK), ConstantValue.exactly(1.0f))))).add(LootItem.lootTableItem(Items.DIAMOND_SWORD))));
        biConsumer.accept(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(NestedLootTable.lootTableReference(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.BOW).setWeight(2)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.BOW).apply(new SetEnchantmentsFunction.Builder().withEnchantment(holderGetter3.getOrThrow(Enchantments.POWER), ConstantValue.exactly(1.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.BOW).apply(new SetEnchantmentsFunction.Builder().withEnchantment(holderGetter3.getOrThrow(Enchantments.PUNCH), ConstantValue.exactly(1.0f)))))));
    }

    public static LootTable.Builder trialChamberEquipment(Item item, Item item2, ArmorTrim armorTrim, HolderLookup.RegistryLookup<Enchantment> registryLookup) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(LootItemRandomChanceCondition.randomChance(0.5f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(item).apply(SetComponentsFunction.setComponent(DataComponents.TRIM, armorTrim))).apply(new SetEnchantmentsFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.PROTECTION), ConstantValue.exactly(4.0f)).withEnchantment(registryLookup.getOrThrow(Enchantments.PROJECTILE_PROTECTION), ConstantValue.exactly(4.0f)).withEnchantment(registryLookup.getOrThrow(Enchantments.FIRE_PROTECTION), ConstantValue.exactly(4.0f)))))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(LootItemRandomChanceCondition.randomChance(0.5f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(item2).apply(SetComponentsFunction.setComponent(DataComponents.TRIM, armorTrim))).apply(new SetEnchantmentsFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.PROTECTION), ConstantValue.exactly(4.0f)).withEnchantment(registryLookup.getOrThrow(Enchantments.PROJECTILE_PROTECTION), ConstantValue.exactly(4.0f)).withEnchantment(registryLookup.getOrThrow(Enchantments.FIRE_PROTECTION), ConstantValue.exactly(4.0f))))));
    }
}

