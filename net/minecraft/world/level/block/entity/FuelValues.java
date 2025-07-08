/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntSortedMap
 */
package net.minecraft.world.level.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import java.util.Collections;
import java.util.SequencedSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class FuelValues {
    private final Object2IntSortedMap<Item> values;

    FuelValues(Object2IntSortedMap<Item> object2IntSortedMap) {
        this.values = object2IntSortedMap;
    }

    public boolean isFuel(ItemStack itemStack) {
        return this.values.containsKey((Object)itemStack.getItem());
    }

    public SequencedSet<Item> fuelItems() {
        return Collections.unmodifiableSequencedSet(this.values.keySet());
    }

    public int burnDuration(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        return this.values.getInt((Object)itemStack.getItem());
    }

    public static FuelValues vanillaBurnTimes(HolderLookup.Provider provider, FeatureFlagSet featureFlagSet) {
        return FuelValues.vanillaBurnTimes(provider, featureFlagSet, 200);
    }

    public static FuelValues vanillaBurnTimes(HolderLookup.Provider provider, FeatureFlagSet featureFlagSet, int n) {
        return new Builder(provider, featureFlagSet).add(Items.LAVA_BUCKET, n * 100).add(Blocks.COAL_BLOCK, n * 8 * 10).add(Items.BLAZE_ROD, n * 12).add(Items.COAL, n * 8).add(Items.CHARCOAL, n * 8).add(ItemTags.LOGS, n * 3 / 2).add(ItemTags.BAMBOO_BLOCKS, n * 3 / 2).add(ItemTags.PLANKS, n * 3 / 2).add(Blocks.BAMBOO_MOSAIC, n * 3 / 2).add(ItemTags.WOODEN_STAIRS, n * 3 / 2).add(Blocks.BAMBOO_MOSAIC_STAIRS, n * 3 / 2).add(ItemTags.WOODEN_SLABS, n * 3 / 4).add(Blocks.BAMBOO_MOSAIC_SLAB, n * 3 / 4).add(ItemTags.WOODEN_TRAPDOORS, n * 3 / 2).add(ItemTags.WOODEN_PRESSURE_PLATES, n * 3 / 2).add(ItemTags.WOODEN_FENCES, n * 3 / 2).add(ItemTags.FENCE_GATES, n * 3 / 2).add(Blocks.NOTE_BLOCK, n * 3 / 2).add(Blocks.BOOKSHELF, n * 3 / 2).add(Blocks.CHISELED_BOOKSHELF, n * 3 / 2).add(Blocks.LECTERN, n * 3 / 2).add(Blocks.JUKEBOX, n * 3 / 2).add(Blocks.CHEST, n * 3 / 2).add(Blocks.TRAPPED_CHEST, n * 3 / 2).add(Blocks.CRAFTING_TABLE, n * 3 / 2).add(Blocks.DAYLIGHT_DETECTOR, n * 3 / 2).add(ItemTags.BANNERS, n * 3 / 2).add(Items.BOW, n * 3 / 2).add(Items.FISHING_ROD, n * 3 / 2).add(Blocks.LADDER, n * 3 / 2).add(ItemTags.SIGNS, n).add(ItemTags.HANGING_SIGNS, n * 4).add(Items.WOODEN_SHOVEL, n).add(Items.WOODEN_SWORD, n).add(Items.WOODEN_HOE, n).add(Items.WOODEN_AXE, n).add(Items.WOODEN_PICKAXE, n).add(ItemTags.WOODEN_DOORS, n).add(ItemTags.BOATS, n * 6).add(ItemTags.WOOL, n / 2).add(ItemTags.WOODEN_BUTTONS, n / 2).add(Items.STICK, n / 2).add(ItemTags.SAPLINGS, n / 2).add(Items.BOWL, n / 2).add(ItemTags.WOOL_CARPETS, 1 + n / 3).add(Blocks.DRIED_KELP_BLOCK, 1 + n * 20).add(Items.CROSSBOW, n * 3 / 2).add(Blocks.BAMBOO, n / 4).add(Blocks.DEAD_BUSH, n / 2).add(Blocks.SHORT_DRY_GRASS, n / 2).add(Blocks.TALL_DRY_GRASS, n / 2).add(Blocks.SCAFFOLDING, n / 4).add(Blocks.LOOM, n * 3 / 2).add(Blocks.BARREL, n * 3 / 2).add(Blocks.CARTOGRAPHY_TABLE, n * 3 / 2).add(Blocks.FLETCHING_TABLE, n * 3 / 2).add(Blocks.SMITHING_TABLE, n * 3 / 2).add(Blocks.COMPOSTER, n * 3 / 2).add(Blocks.AZALEA, n / 2).add(Blocks.FLOWERING_AZALEA, n / 2).add(Blocks.MANGROVE_ROOTS, n * 3 / 2).add(Blocks.LEAF_LITTER, n / 2).remove(ItemTags.NON_FLAMMABLE_WOOD).build();
    }

    public static class Builder {
        private final HolderLookup<Item> items;
        private final FeatureFlagSet enabledFeatures;
        private final Object2IntSortedMap<Item> values = new Object2IntLinkedOpenHashMap();

        public Builder(HolderLookup.Provider provider, FeatureFlagSet featureFlagSet) {
            this.items = provider.lookupOrThrow(Registries.ITEM);
            this.enabledFeatures = featureFlagSet;
        }

        public FuelValues build() {
            return new FuelValues(this.values);
        }

        public Builder remove(TagKey<Item> tagKey) {
            this.values.keySet().removeIf(item -> item.builtInRegistryHolder().is(tagKey));
            return this;
        }

        public Builder add(TagKey<Item> tagKey, int n) {
            this.items.get(tagKey).ifPresent(named -> {
                for (Holder holder : named) {
                    this.putInternal(n, (Item)holder.value());
                }
            });
            return this;
        }

        public Builder add(ItemLike itemLike, int n) {
            Item item = itemLike.asItem();
            this.putInternal(n, item);
            return this;
        }

        private void putInternal(int n, Item item) {
            if (item.isEnabled(this.enabledFeatures)) {
                this.values.put((Object)item, n);
            }
        }
    }
}

