/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemIdFix
extends DataFix {
    private static final Int2ObjectMap<String> ITEM_NAMES = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, (Object)"minecraft:stone");
        int2ObjectOpenHashMap.put(2, (Object)"minecraft:grass");
        int2ObjectOpenHashMap.put(3, (Object)"minecraft:dirt");
        int2ObjectOpenHashMap.put(4, (Object)"minecraft:cobblestone");
        int2ObjectOpenHashMap.put(5, (Object)"minecraft:planks");
        int2ObjectOpenHashMap.put(6, (Object)"minecraft:sapling");
        int2ObjectOpenHashMap.put(7, (Object)"minecraft:bedrock");
        int2ObjectOpenHashMap.put(8, (Object)"minecraft:flowing_water");
        int2ObjectOpenHashMap.put(9, (Object)"minecraft:water");
        int2ObjectOpenHashMap.put(10, (Object)"minecraft:flowing_lava");
        int2ObjectOpenHashMap.put(11, (Object)"minecraft:lava");
        int2ObjectOpenHashMap.put(12, (Object)"minecraft:sand");
        int2ObjectOpenHashMap.put(13, (Object)"minecraft:gravel");
        int2ObjectOpenHashMap.put(14, (Object)"minecraft:gold_ore");
        int2ObjectOpenHashMap.put(15, (Object)"minecraft:iron_ore");
        int2ObjectOpenHashMap.put(16, (Object)"minecraft:coal_ore");
        int2ObjectOpenHashMap.put(17, (Object)"minecraft:log");
        int2ObjectOpenHashMap.put(18, (Object)"minecraft:leaves");
        int2ObjectOpenHashMap.put(19, (Object)"minecraft:sponge");
        int2ObjectOpenHashMap.put(20, (Object)"minecraft:glass");
        int2ObjectOpenHashMap.put(21, (Object)"minecraft:lapis_ore");
        int2ObjectOpenHashMap.put(22, (Object)"minecraft:lapis_block");
        int2ObjectOpenHashMap.put(23, (Object)"minecraft:dispenser");
        int2ObjectOpenHashMap.put(24, (Object)"minecraft:sandstone");
        int2ObjectOpenHashMap.put(25, (Object)"minecraft:noteblock");
        int2ObjectOpenHashMap.put(27, (Object)"minecraft:golden_rail");
        int2ObjectOpenHashMap.put(28, (Object)"minecraft:detector_rail");
        int2ObjectOpenHashMap.put(29, (Object)"minecraft:sticky_piston");
        int2ObjectOpenHashMap.put(30, (Object)"minecraft:web");
        int2ObjectOpenHashMap.put(31, (Object)"minecraft:tallgrass");
        int2ObjectOpenHashMap.put(32, (Object)"minecraft:deadbush");
        int2ObjectOpenHashMap.put(33, (Object)"minecraft:piston");
        int2ObjectOpenHashMap.put(35, (Object)"minecraft:wool");
        int2ObjectOpenHashMap.put(37, (Object)"minecraft:yellow_flower");
        int2ObjectOpenHashMap.put(38, (Object)"minecraft:red_flower");
        int2ObjectOpenHashMap.put(39, (Object)"minecraft:brown_mushroom");
        int2ObjectOpenHashMap.put(40, (Object)"minecraft:red_mushroom");
        int2ObjectOpenHashMap.put(41, (Object)"minecraft:gold_block");
        int2ObjectOpenHashMap.put(42, (Object)"minecraft:iron_block");
        int2ObjectOpenHashMap.put(43, (Object)"minecraft:double_stone_slab");
        int2ObjectOpenHashMap.put(44, (Object)"minecraft:stone_slab");
        int2ObjectOpenHashMap.put(45, (Object)"minecraft:brick_block");
        int2ObjectOpenHashMap.put(46, (Object)"minecraft:tnt");
        int2ObjectOpenHashMap.put(47, (Object)"minecraft:bookshelf");
        int2ObjectOpenHashMap.put(48, (Object)"minecraft:mossy_cobblestone");
        int2ObjectOpenHashMap.put(49, (Object)"minecraft:obsidian");
        int2ObjectOpenHashMap.put(50, (Object)"minecraft:torch");
        int2ObjectOpenHashMap.put(51, (Object)"minecraft:fire");
        int2ObjectOpenHashMap.put(52, (Object)"minecraft:mob_spawner");
        int2ObjectOpenHashMap.put(53, (Object)"minecraft:oak_stairs");
        int2ObjectOpenHashMap.put(54, (Object)"minecraft:chest");
        int2ObjectOpenHashMap.put(56, (Object)"minecraft:diamond_ore");
        int2ObjectOpenHashMap.put(57, (Object)"minecraft:diamond_block");
        int2ObjectOpenHashMap.put(58, (Object)"minecraft:crafting_table");
        int2ObjectOpenHashMap.put(60, (Object)"minecraft:farmland");
        int2ObjectOpenHashMap.put(61, (Object)"minecraft:furnace");
        int2ObjectOpenHashMap.put(62, (Object)"minecraft:lit_furnace");
        int2ObjectOpenHashMap.put(65, (Object)"minecraft:ladder");
        int2ObjectOpenHashMap.put(66, (Object)"minecraft:rail");
        int2ObjectOpenHashMap.put(67, (Object)"minecraft:stone_stairs");
        int2ObjectOpenHashMap.put(69, (Object)"minecraft:lever");
        int2ObjectOpenHashMap.put(70, (Object)"minecraft:stone_pressure_plate");
        int2ObjectOpenHashMap.put(72, (Object)"minecraft:wooden_pressure_plate");
        int2ObjectOpenHashMap.put(73, (Object)"minecraft:redstone_ore");
        int2ObjectOpenHashMap.put(76, (Object)"minecraft:redstone_torch");
        int2ObjectOpenHashMap.put(77, (Object)"minecraft:stone_button");
        int2ObjectOpenHashMap.put(78, (Object)"minecraft:snow_layer");
        int2ObjectOpenHashMap.put(79, (Object)"minecraft:ice");
        int2ObjectOpenHashMap.put(80, (Object)"minecraft:snow");
        int2ObjectOpenHashMap.put(81, (Object)"minecraft:cactus");
        int2ObjectOpenHashMap.put(82, (Object)"minecraft:clay");
        int2ObjectOpenHashMap.put(84, (Object)"minecraft:jukebox");
        int2ObjectOpenHashMap.put(85, (Object)"minecraft:fence");
        int2ObjectOpenHashMap.put(86, (Object)"minecraft:pumpkin");
        int2ObjectOpenHashMap.put(87, (Object)"minecraft:netherrack");
        int2ObjectOpenHashMap.put(88, (Object)"minecraft:soul_sand");
        int2ObjectOpenHashMap.put(89, (Object)"minecraft:glowstone");
        int2ObjectOpenHashMap.put(90, (Object)"minecraft:portal");
        int2ObjectOpenHashMap.put(91, (Object)"minecraft:lit_pumpkin");
        int2ObjectOpenHashMap.put(95, (Object)"minecraft:stained_glass");
        int2ObjectOpenHashMap.put(96, (Object)"minecraft:trapdoor");
        int2ObjectOpenHashMap.put(97, (Object)"minecraft:monster_egg");
        int2ObjectOpenHashMap.put(98, (Object)"minecraft:stonebrick");
        int2ObjectOpenHashMap.put(99, (Object)"minecraft:brown_mushroom_block");
        int2ObjectOpenHashMap.put(100, (Object)"minecraft:red_mushroom_block");
        int2ObjectOpenHashMap.put(101, (Object)"minecraft:iron_bars");
        int2ObjectOpenHashMap.put(102, (Object)"minecraft:glass_pane");
        int2ObjectOpenHashMap.put(103, (Object)"minecraft:melon_block");
        int2ObjectOpenHashMap.put(106, (Object)"minecraft:vine");
        int2ObjectOpenHashMap.put(107, (Object)"minecraft:fence_gate");
        int2ObjectOpenHashMap.put(108, (Object)"minecraft:brick_stairs");
        int2ObjectOpenHashMap.put(109, (Object)"minecraft:stone_brick_stairs");
        int2ObjectOpenHashMap.put(110, (Object)"minecraft:mycelium");
        int2ObjectOpenHashMap.put(111, (Object)"minecraft:waterlily");
        int2ObjectOpenHashMap.put(112, (Object)"minecraft:nether_brick");
        int2ObjectOpenHashMap.put(113, (Object)"minecraft:nether_brick_fence");
        int2ObjectOpenHashMap.put(114, (Object)"minecraft:nether_brick_stairs");
        int2ObjectOpenHashMap.put(116, (Object)"minecraft:enchanting_table");
        int2ObjectOpenHashMap.put(119, (Object)"minecraft:end_portal");
        int2ObjectOpenHashMap.put(120, (Object)"minecraft:end_portal_frame");
        int2ObjectOpenHashMap.put(121, (Object)"minecraft:end_stone");
        int2ObjectOpenHashMap.put(122, (Object)"minecraft:dragon_egg");
        int2ObjectOpenHashMap.put(123, (Object)"minecraft:redstone_lamp");
        int2ObjectOpenHashMap.put(125, (Object)"minecraft:double_wooden_slab");
        int2ObjectOpenHashMap.put(126, (Object)"minecraft:wooden_slab");
        int2ObjectOpenHashMap.put(127, (Object)"minecraft:cocoa");
        int2ObjectOpenHashMap.put(128, (Object)"minecraft:sandstone_stairs");
        int2ObjectOpenHashMap.put(129, (Object)"minecraft:emerald_ore");
        int2ObjectOpenHashMap.put(130, (Object)"minecraft:ender_chest");
        int2ObjectOpenHashMap.put(131, (Object)"minecraft:tripwire_hook");
        int2ObjectOpenHashMap.put(133, (Object)"minecraft:emerald_block");
        int2ObjectOpenHashMap.put(134, (Object)"minecraft:spruce_stairs");
        int2ObjectOpenHashMap.put(135, (Object)"minecraft:birch_stairs");
        int2ObjectOpenHashMap.put(136, (Object)"minecraft:jungle_stairs");
        int2ObjectOpenHashMap.put(137, (Object)"minecraft:command_block");
        int2ObjectOpenHashMap.put(138, (Object)"minecraft:beacon");
        int2ObjectOpenHashMap.put(139, (Object)"minecraft:cobblestone_wall");
        int2ObjectOpenHashMap.put(141, (Object)"minecraft:carrots");
        int2ObjectOpenHashMap.put(142, (Object)"minecraft:potatoes");
        int2ObjectOpenHashMap.put(143, (Object)"minecraft:wooden_button");
        int2ObjectOpenHashMap.put(145, (Object)"minecraft:anvil");
        int2ObjectOpenHashMap.put(146, (Object)"minecraft:trapped_chest");
        int2ObjectOpenHashMap.put(147, (Object)"minecraft:light_weighted_pressure_plate");
        int2ObjectOpenHashMap.put(148, (Object)"minecraft:heavy_weighted_pressure_plate");
        int2ObjectOpenHashMap.put(151, (Object)"minecraft:daylight_detector");
        int2ObjectOpenHashMap.put(152, (Object)"minecraft:redstone_block");
        int2ObjectOpenHashMap.put(153, (Object)"minecraft:quartz_ore");
        int2ObjectOpenHashMap.put(154, (Object)"minecraft:hopper");
        int2ObjectOpenHashMap.put(155, (Object)"minecraft:quartz_block");
        int2ObjectOpenHashMap.put(156, (Object)"minecraft:quartz_stairs");
        int2ObjectOpenHashMap.put(157, (Object)"minecraft:activator_rail");
        int2ObjectOpenHashMap.put(158, (Object)"minecraft:dropper");
        int2ObjectOpenHashMap.put(159, (Object)"minecraft:stained_hardened_clay");
        int2ObjectOpenHashMap.put(160, (Object)"minecraft:stained_glass_pane");
        int2ObjectOpenHashMap.put(161, (Object)"minecraft:leaves2");
        int2ObjectOpenHashMap.put(162, (Object)"minecraft:log2");
        int2ObjectOpenHashMap.put(163, (Object)"minecraft:acacia_stairs");
        int2ObjectOpenHashMap.put(164, (Object)"minecraft:dark_oak_stairs");
        int2ObjectOpenHashMap.put(170, (Object)"minecraft:hay_block");
        int2ObjectOpenHashMap.put(171, (Object)"minecraft:carpet");
        int2ObjectOpenHashMap.put(172, (Object)"minecraft:hardened_clay");
        int2ObjectOpenHashMap.put(173, (Object)"minecraft:coal_block");
        int2ObjectOpenHashMap.put(174, (Object)"minecraft:packed_ice");
        int2ObjectOpenHashMap.put(175, (Object)"minecraft:double_plant");
        int2ObjectOpenHashMap.put(256, (Object)"minecraft:iron_shovel");
        int2ObjectOpenHashMap.put(257, (Object)"minecraft:iron_pickaxe");
        int2ObjectOpenHashMap.put(258, (Object)"minecraft:iron_axe");
        int2ObjectOpenHashMap.put(259, (Object)"minecraft:flint_and_steel");
        int2ObjectOpenHashMap.put(260, (Object)"minecraft:apple");
        int2ObjectOpenHashMap.put(261, (Object)"minecraft:bow");
        int2ObjectOpenHashMap.put(262, (Object)"minecraft:arrow");
        int2ObjectOpenHashMap.put(263, (Object)"minecraft:coal");
        int2ObjectOpenHashMap.put(264, (Object)"minecraft:diamond");
        int2ObjectOpenHashMap.put(265, (Object)"minecraft:iron_ingot");
        int2ObjectOpenHashMap.put(266, (Object)"minecraft:gold_ingot");
        int2ObjectOpenHashMap.put(267, (Object)"minecraft:iron_sword");
        int2ObjectOpenHashMap.put(268, (Object)"minecraft:wooden_sword");
        int2ObjectOpenHashMap.put(269, (Object)"minecraft:wooden_shovel");
        int2ObjectOpenHashMap.put(270, (Object)"minecraft:wooden_pickaxe");
        int2ObjectOpenHashMap.put(271, (Object)"minecraft:wooden_axe");
        int2ObjectOpenHashMap.put(272, (Object)"minecraft:stone_sword");
        int2ObjectOpenHashMap.put(273, (Object)"minecraft:stone_shovel");
        int2ObjectOpenHashMap.put(274, (Object)"minecraft:stone_pickaxe");
        int2ObjectOpenHashMap.put(275, (Object)"minecraft:stone_axe");
        int2ObjectOpenHashMap.put(276, (Object)"minecraft:diamond_sword");
        int2ObjectOpenHashMap.put(277, (Object)"minecraft:diamond_shovel");
        int2ObjectOpenHashMap.put(278, (Object)"minecraft:diamond_pickaxe");
        int2ObjectOpenHashMap.put(279, (Object)"minecraft:diamond_axe");
        int2ObjectOpenHashMap.put(280, (Object)"minecraft:stick");
        int2ObjectOpenHashMap.put(281, (Object)"minecraft:bowl");
        int2ObjectOpenHashMap.put(282, (Object)"minecraft:mushroom_stew");
        int2ObjectOpenHashMap.put(283, (Object)"minecraft:golden_sword");
        int2ObjectOpenHashMap.put(284, (Object)"minecraft:golden_shovel");
        int2ObjectOpenHashMap.put(285, (Object)"minecraft:golden_pickaxe");
        int2ObjectOpenHashMap.put(286, (Object)"minecraft:golden_axe");
        int2ObjectOpenHashMap.put(287, (Object)"minecraft:string");
        int2ObjectOpenHashMap.put(288, (Object)"minecraft:feather");
        int2ObjectOpenHashMap.put(289, (Object)"minecraft:gunpowder");
        int2ObjectOpenHashMap.put(290, (Object)"minecraft:wooden_hoe");
        int2ObjectOpenHashMap.put(291, (Object)"minecraft:stone_hoe");
        int2ObjectOpenHashMap.put(292, (Object)"minecraft:iron_hoe");
        int2ObjectOpenHashMap.put(293, (Object)"minecraft:diamond_hoe");
        int2ObjectOpenHashMap.put(294, (Object)"minecraft:golden_hoe");
        int2ObjectOpenHashMap.put(295, (Object)"minecraft:wheat_seeds");
        int2ObjectOpenHashMap.put(296, (Object)"minecraft:wheat");
        int2ObjectOpenHashMap.put(297, (Object)"minecraft:bread");
        int2ObjectOpenHashMap.put(298, (Object)"minecraft:leather_helmet");
        int2ObjectOpenHashMap.put(299, (Object)"minecraft:leather_chestplate");
        int2ObjectOpenHashMap.put(300, (Object)"minecraft:leather_leggings");
        int2ObjectOpenHashMap.put(301, (Object)"minecraft:leather_boots");
        int2ObjectOpenHashMap.put(302, (Object)"minecraft:chainmail_helmet");
        int2ObjectOpenHashMap.put(303, (Object)"minecraft:chainmail_chestplate");
        int2ObjectOpenHashMap.put(304, (Object)"minecraft:chainmail_leggings");
        int2ObjectOpenHashMap.put(305, (Object)"minecraft:chainmail_boots");
        int2ObjectOpenHashMap.put(306, (Object)"minecraft:iron_helmet");
        int2ObjectOpenHashMap.put(307, (Object)"minecraft:iron_chestplate");
        int2ObjectOpenHashMap.put(308, (Object)"minecraft:iron_leggings");
        int2ObjectOpenHashMap.put(309, (Object)"minecraft:iron_boots");
        int2ObjectOpenHashMap.put(310, (Object)"minecraft:diamond_helmet");
        int2ObjectOpenHashMap.put(311, (Object)"minecraft:diamond_chestplate");
        int2ObjectOpenHashMap.put(312, (Object)"minecraft:diamond_leggings");
        int2ObjectOpenHashMap.put(313, (Object)"minecraft:diamond_boots");
        int2ObjectOpenHashMap.put(314, (Object)"minecraft:golden_helmet");
        int2ObjectOpenHashMap.put(315, (Object)"minecraft:golden_chestplate");
        int2ObjectOpenHashMap.put(316, (Object)"minecraft:golden_leggings");
        int2ObjectOpenHashMap.put(317, (Object)"minecraft:golden_boots");
        int2ObjectOpenHashMap.put(318, (Object)"minecraft:flint");
        int2ObjectOpenHashMap.put(319, (Object)"minecraft:porkchop");
        int2ObjectOpenHashMap.put(320, (Object)"minecraft:cooked_porkchop");
        int2ObjectOpenHashMap.put(321, (Object)"minecraft:painting");
        int2ObjectOpenHashMap.put(322, (Object)"minecraft:golden_apple");
        int2ObjectOpenHashMap.put(323, (Object)"minecraft:sign");
        int2ObjectOpenHashMap.put(324, (Object)"minecraft:wooden_door");
        int2ObjectOpenHashMap.put(325, (Object)"minecraft:bucket");
        int2ObjectOpenHashMap.put(326, (Object)"minecraft:water_bucket");
        int2ObjectOpenHashMap.put(327, (Object)"minecraft:lava_bucket");
        int2ObjectOpenHashMap.put(328, (Object)"minecraft:minecart");
        int2ObjectOpenHashMap.put(329, (Object)"minecraft:saddle");
        int2ObjectOpenHashMap.put(330, (Object)"minecraft:iron_door");
        int2ObjectOpenHashMap.put(331, (Object)"minecraft:redstone");
        int2ObjectOpenHashMap.put(332, (Object)"minecraft:snowball");
        int2ObjectOpenHashMap.put(333, (Object)"minecraft:boat");
        int2ObjectOpenHashMap.put(334, (Object)"minecraft:leather");
        int2ObjectOpenHashMap.put(335, (Object)"minecraft:milk_bucket");
        int2ObjectOpenHashMap.put(336, (Object)"minecraft:brick");
        int2ObjectOpenHashMap.put(337, (Object)"minecraft:clay_ball");
        int2ObjectOpenHashMap.put(338, (Object)"minecraft:reeds");
        int2ObjectOpenHashMap.put(339, (Object)"minecraft:paper");
        int2ObjectOpenHashMap.put(340, (Object)"minecraft:book");
        int2ObjectOpenHashMap.put(341, (Object)"minecraft:slime_ball");
        int2ObjectOpenHashMap.put(342, (Object)"minecraft:chest_minecart");
        int2ObjectOpenHashMap.put(343, (Object)"minecraft:furnace_minecart");
        int2ObjectOpenHashMap.put(344, (Object)"minecraft:egg");
        int2ObjectOpenHashMap.put(345, (Object)"minecraft:compass");
        int2ObjectOpenHashMap.put(346, (Object)"minecraft:fishing_rod");
        int2ObjectOpenHashMap.put(347, (Object)"minecraft:clock");
        int2ObjectOpenHashMap.put(348, (Object)"minecraft:glowstone_dust");
        int2ObjectOpenHashMap.put(349, (Object)"minecraft:fish");
        int2ObjectOpenHashMap.put(350, (Object)"minecraft:cooked_fished");
        int2ObjectOpenHashMap.put(351, (Object)"minecraft:dye");
        int2ObjectOpenHashMap.put(352, (Object)"minecraft:bone");
        int2ObjectOpenHashMap.put(353, (Object)"minecraft:sugar");
        int2ObjectOpenHashMap.put(354, (Object)"minecraft:cake");
        int2ObjectOpenHashMap.put(355, (Object)"minecraft:bed");
        int2ObjectOpenHashMap.put(356, (Object)"minecraft:repeater");
        int2ObjectOpenHashMap.put(357, (Object)"minecraft:cookie");
        int2ObjectOpenHashMap.put(358, (Object)"minecraft:filled_map");
        int2ObjectOpenHashMap.put(359, (Object)"minecraft:shears");
        int2ObjectOpenHashMap.put(360, (Object)"minecraft:melon");
        int2ObjectOpenHashMap.put(361, (Object)"minecraft:pumpkin_seeds");
        int2ObjectOpenHashMap.put(362, (Object)"minecraft:melon_seeds");
        int2ObjectOpenHashMap.put(363, (Object)"minecraft:beef");
        int2ObjectOpenHashMap.put(364, (Object)"minecraft:cooked_beef");
        int2ObjectOpenHashMap.put(365, (Object)"minecraft:chicken");
        int2ObjectOpenHashMap.put(366, (Object)"minecraft:cooked_chicken");
        int2ObjectOpenHashMap.put(367, (Object)"minecraft:rotten_flesh");
        int2ObjectOpenHashMap.put(368, (Object)"minecraft:ender_pearl");
        int2ObjectOpenHashMap.put(369, (Object)"minecraft:blaze_rod");
        int2ObjectOpenHashMap.put(370, (Object)"minecraft:ghast_tear");
        int2ObjectOpenHashMap.put(371, (Object)"minecraft:gold_nugget");
        int2ObjectOpenHashMap.put(372, (Object)"minecraft:nether_wart");
        int2ObjectOpenHashMap.put(373, (Object)"minecraft:potion");
        int2ObjectOpenHashMap.put(374, (Object)"minecraft:glass_bottle");
        int2ObjectOpenHashMap.put(375, (Object)"minecraft:spider_eye");
        int2ObjectOpenHashMap.put(376, (Object)"minecraft:fermented_spider_eye");
        int2ObjectOpenHashMap.put(377, (Object)"minecraft:blaze_powder");
        int2ObjectOpenHashMap.put(378, (Object)"minecraft:magma_cream");
        int2ObjectOpenHashMap.put(379, (Object)"minecraft:brewing_stand");
        int2ObjectOpenHashMap.put(380, (Object)"minecraft:cauldron");
        int2ObjectOpenHashMap.put(381, (Object)"minecraft:ender_eye");
        int2ObjectOpenHashMap.put(382, (Object)"minecraft:speckled_melon");
        int2ObjectOpenHashMap.put(383, (Object)"minecraft:spawn_egg");
        int2ObjectOpenHashMap.put(384, (Object)"minecraft:experience_bottle");
        int2ObjectOpenHashMap.put(385, (Object)"minecraft:fire_charge");
        int2ObjectOpenHashMap.put(386, (Object)"minecraft:writable_book");
        int2ObjectOpenHashMap.put(387, (Object)"minecraft:written_book");
        int2ObjectOpenHashMap.put(388, (Object)"minecraft:emerald");
        int2ObjectOpenHashMap.put(389, (Object)"minecraft:item_frame");
        int2ObjectOpenHashMap.put(390, (Object)"minecraft:flower_pot");
        int2ObjectOpenHashMap.put(391, (Object)"minecraft:carrot");
        int2ObjectOpenHashMap.put(392, (Object)"minecraft:potato");
        int2ObjectOpenHashMap.put(393, (Object)"minecraft:baked_potato");
        int2ObjectOpenHashMap.put(394, (Object)"minecraft:poisonous_potato");
        int2ObjectOpenHashMap.put(395, (Object)"minecraft:map");
        int2ObjectOpenHashMap.put(396, (Object)"minecraft:golden_carrot");
        int2ObjectOpenHashMap.put(397, (Object)"minecraft:skull");
        int2ObjectOpenHashMap.put(398, (Object)"minecraft:carrot_on_a_stick");
        int2ObjectOpenHashMap.put(399, (Object)"minecraft:nether_star");
        int2ObjectOpenHashMap.put(400, (Object)"minecraft:pumpkin_pie");
        int2ObjectOpenHashMap.put(401, (Object)"minecraft:fireworks");
        int2ObjectOpenHashMap.put(402, (Object)"minecraft:firework_charge");
        int2ObjectOpenHashMap.put(403, (Object)"minecraft:enchanted_book");
        int2ObjectOpenHashMap.put(404, (Object)"minecraft:comparator");
        int2ObjectOpenHashMap.put(405, (Object)"minecraft:netherbrick");
        int2ObjectOpenHashMap.put(406, (Object)"minecraft:quartz");
        int2ObjectOpenHashMap.put(407, (Object)"minecraft:tnt_minecart");
        int2ObjectOpenHashMap.put(408, (Object)"minecraft:hopper_minecart");
        int2ObjectOpenHashMap.put(417, (Object)"minecraft:iron_horse_armor");
        int2ObjectOpenHashMap.put(418, (Object)"minecraft:golden_horse_armor");
        int2ObjectOpenHashMap.put(419, (Object)"minecraft:diamond_horse_armor");
        int2ObjectOpenHashMap.put(420, (Object)"minecraft:lead");
        int2ObjectOpenHashMap.put(421, (Object)"minecraft:name_tag");
        int2ObjectOpenHashMap.put(422, (Object)"minecraft:command_block_minecart");
        int2ObjectOpenHashMap.put(2256, (Object)"minecraft:record_13");
        int2ObjectOpenHashMap.put(2257, (Object)"minecraft:record_cat");
        int2ObjectOpenHashMap.put(2258, (Object)"minecraft:record_blocks");
        int2ObjectOpenHashMap.put(2259, (Object)"minecraft:record_chirp");
        int2ObjectOpenHashMap.put(2260, (Object)"minecraft:record_far");
        int2ObjectOpenHashMap.put(2261, (Object)"minecraft:record_mall");
        int2ObjectOpenHashMap.put(2262, (Object)"minecraft:record_mellohi");
        int2ObjectOpenHashMap.put(2263, (Object)"minecraft:record_stal");
        int2ObjectOpenHashMap.put(2264, (Object)"minecraft:record_strad");
        int2ObjectOpenHashMap.put(2265, (Object)"minecraft:record_ward");
        int2ObjectOpenHashMap.put(2266, (Object)"minecraft:record_11");
        int2ObjectOpenHashMap.put(2267, (Object)"minecraft:record_wait");
        int2ObjectOpenHashMap.defaultReturnValue((Object)"minecraft:air");
    });

    public ItemIdFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public static String getItem(int n) {
        return (String)ITEM_NAMES.get(n);
    }

    public TypeRewriteRule makeRule() {
        Type type = DSL.or((Type)DSL.intType(), (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Type type2 = DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)type);
        return this.fixTypeEverywhereTyped("ItemIdFix", this.getInputSchema().getType(References.ITEM_STACK), this.getOutputSchema().getType(References.ITEM_STACK), typed -> typed.update(opticFinder, type2, either -> (Pair)either.map(n -> Pair.of((Object)References.ITEM_NAME.typeName(), (Object)ItemIdFix.getItem(n)), pair -> pair)));
    }
}

