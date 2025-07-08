/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment.providers;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.EnchantmentsByCostWithDifficulty;
import net.minecraft.world.item.enchantment.providers.SingleEnchantment;

public interface VanillaEnchantmentProviders {
    public static final ResourceKey<EnchantmentProvider> MOB_SPAWN_EQUIPMENT = VanillaEnchantmentProviders.create("mob_spawn_equipment");
    public static final ResourceKey<EnchantmentProvider> PILLAGER_SPAWN_CROSSBOW = VanillaEnchantmentProviders.create("pillager_spawn_crossbow");
    public static final ResourceKey<EnchantmentProvider> RAID_PILLAGER_POST_WAVE_3 = VanillaEnchantmentProviders.create("raid/pillager_post_wave_3");
    public static final ResourceKey<EnchantmentProvider> RAID_PILLAGER_POST_WAVE_5 = VanillaEnchantmentProviders.create("raid/pillager_post_wave_5");
    public static final ResourceKey<EnchantmentProvider> RAID_VINDICATOR = VanillaEnchantmentProviders.create("raid/vindicator");
    public static final ResourceKey<EnchantmentProvider> RAID_VINDICATOR_POST_WAVE_5 = VanillaEnchantmentProviders.create("raid/vindicator_post_wave_5");
    public static final ResourceKey<EnchantmentProvider> ENDERMAN_LOOT_DROP = VanillaEnchantmentProviders.create("enderman_loot_drop");

    public static void bootstrap(BootstrapContext<EnchantmentProvider> bootstrapContext) {
        HolderGetter<Enchantment> holderGetter = bootstrapContext.lookup(Registries.ENCHANTMENT);
        bootstrapContext.register(MOB_SPAWN_EQUIPMENT, new EnchantmentsByCostWithDifficulty(holderGetter.getOrThrow(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT), 5, 17));
        bootstrapContext.register(PILLAGER_SPAWN_CROSSBOW, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.PIERCING), ConstantInt.of(1)));
        bootstrapContext.register(RAID_PILLAGER_POST_WAVE_3, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.QUICK_CHARGE), ConstantInt.of(1)));
        bootstrapContext.register(RAID_PILLAGER_POST_WAVE_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.QUICK_CHARGE), ConstantInt.of(2)));
        bootstrapContext.register(RAID_VINDICATOR, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.SHARPNESS), ConstantInt.of(1)));
        bootstrapContext.register(RAID_VINDICATOR_POST_WAVE_5, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.SHARPNESS), ConstantInt.of(2)));
        bootstrapContext.register(ENDERMAN_LOOT_DROP, new SingleEnchantment(holderGetter.getOrThrow(Enchantments.SILK_TOUCH), ConstantInt.of(1)));
    }

    public static ResourceKey<EnchantmentProvider> create(String string) {
        return ResourceKey.create(Registries.ENCHANTMENT_PROVIDER, ResourceLocation.withDefaultNamespace(string));
    }
}

