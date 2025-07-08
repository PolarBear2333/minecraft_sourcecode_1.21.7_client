/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.slf4j.Logger;

public class V99
extends Schema {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Map<String, String> ITEM_TO_BLOCKENTITY = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
        hashMap.put("minecraft:furnace", "Furnace");
        hashMap.put("minecraft:lit_furnace", "Furnace");
        hashMap.put("minecraft:chest", "Chest");
        hashMap.put("minecraft:trapped_chest", "Chest");
        hashMap.put("minecraft:ender_chest", "EnderChest");
        hashMap.put("minecraft:jukebox", "RecordPlayer");
        hashMap.put("minecraft:dispenser", "Trap");
        hashMap.put("minecraft:dropper", "Dropper");
        hashMap.put("minecraft:sign", "Sign");
        hashMap.put("minecraft:mob_spawner", "MobSpawner");
        hashMap.put("minecraft:noteblock", "Music");
        hashMap.put("minecraft:brewing_stand", "Cauldron");
        hashMap.put("minecraft:enhanting_table", "EnchantTable");
        hashMap.put("minecraft:command_block", "CommandBlock");
        hashMap.put("minecraft:beacon", "Beacon");
        hashMap.put("minecraft:skull", "Skull");
        hashMap.put("minecraft:daylight_detector", "DLDetector");
        hashMap.put("minecraft:hopper", "Hopper");
        hashMap.put("minecraft:banner", "Banner");
        hashMap.put("minecraft:flower_pot", "FlowerPot");
        hashMap.put("minecraft:repeating_command_block", "CommandBlock");
        hashMap.put("minecraft:chain_command_block", "CommandBlock");
        hashMap.put("minecraft:standing_sign", "Sign");
        hashMap.put("minecraft:wall_sign", "Sign");
        hashMap.put("minecraft:piston_head", "Piston");
        hashMap.put("minecraft:daylight_detector_inverted", "DLDetector");
        hashMap.put("minecraft:unpowered_comparator", "Comparator");
        hashMap.put("minecraft:powered_comparator", "Comparator");
        hashMap.put("minecraft:wall_banner", "Banner");
        hashMap.put("minecraft:standing_banner", "Banner");
        hashMap.put("minecraft:structure_block", "Structure");
        hashMap.put("minecraft:end_portal", "Airportal");
        hashMap.put("minecraft:end_gateway", "EndGateway");
        hashMap.put("minecraft:shield", "Banner");
    });
    public static final Map<String, String> ITEM_TO_ENTITY = Map.of("minecraft:armor_stand", "ArmorStand", "minecraft:painting", "Painting");
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> dynamicOps, T t) {
            return V99.addNames(new Dynamic(dynamicOps, t), ITEM_TO_BLOCKENTITY, ITEM_TO_ENTITY);
        }
    };

    public V99(int n, Schema schema) {
        super(n, schema);
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
    }

    protected static void registerMinecart(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap hashMap = Maps.newHashMap();
        schema.register((Map)hashMap, "Item", string -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)hashMap, "XPOrb");
        V99.registerThrowableProjectile(schema, hashMap, "ThrownEgg");
        schema.registerSimple((Map)hashMap, "LeashKnot");
        schema.registerSimple((Map)hashMap, "Painting");
        schema.register((Map)hashMap, "Arrow", string -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        schema.register((Map)hashMap, "TippedArrow", string -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        schema.register((Map)hashMap, "SpectralArrow", string -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        V99.registerThrowableProjectile(schema, hashMap, "Snowball");
        V99.registerThrowableProjectile(schema, hashMap, "Fireball");
        V99.registerThrowableProjectile(schema, hashMap, "SmallFireball");
        V99.registerThrowableProjectile(schema, hashMap, "ThrownEnderpearl");
        schema.registerSimple((Map)hashMap, "EyeOfEnderSignal");
        schema.register((Map)hashMap, "ThrownPotion", string -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"Potion", (TypeTemplate)References.ITEM_STACK.in(schema)));
        V99.registerThrowableProjectile(schema, hashMap, "ThrownExpBottle");
        schema.register((Map)hashMap, "ItemFrame", string -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in(schema)));
        V99.registerThrowableProjectile(schema, hashMap, "WitherSkull");
        schema.registerSimple((Map)hashMap, "PrimedTnt");
        schema.register((Map)hashMap, "FallingSand", string -> DSL.optionalFields((String)"Block", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"TileEntityData", (TypeTemplate)References.BLOCK_ENTITY.in(schema)));
        schema.register((Map)hashMap, "FireworksRocketEntity", string -> DSL.optionalFields((String)"FireworksItem", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)hashMap, "Boat");
        schema.register((Map)hashMap, "Minecart", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, hashMap, "MinecartRideable");
        schema.register((Map)hashMap, "MinecartChest", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, hashMap, "MinecartFurnace");
        V99.registerMinecart(schema, hashMap, "MinecartTNT");
        schema.register((Map)hashMap, "MinecartSpawner", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)References.UNTAGGED_SPAWNER.in(schema)));
        schema.register((Map)hashMap, "MinecartHopper", string -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
        schema.register((Map)hashMap, "MinecartCommandBlock", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in(schema), (String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)hashMap, "ArmorStand");
        schema.registerSimple((Map)hashMap, "Creeper");
        schema.registerSimple((Map)hashMap, "Skeleton");
        schema.registerSimple((Map)hashMap, "Spider");
        schema.registerSimple((Map)hashMap, "Giant");
        schema.registerSimple((Map)hashMap, "Zombie");
        schema.registerSimple((Map)hashMap, "Slime");
        schema.registerSimple((Map)hashMap, "Ghast");
        schema.registerSimple((Map)hashMap, "PigZombie");
        schema.register((Map)hashMap, "Enderman", string -> DSL.optionalFields((String)"carried", (TypeTemplate)References.BLOCK_NAME.in(schema)));
        schema.registerSimple((Map)hashMap, "CaveSpider");
        schema.registerSimple((Map)hashMap, "Silverfish");
        schema.registerSimple((Map)hashMap, "Blaze");
        schema.registerSimple((Map)hashMap, "LavaSlime");
        schema.registerSimple((Map)hashMap, "EnderDragon");
        schema.registerSimple((Map)hashMap, "WitherBoss");
        schema.registerSimple((Map)hashMap, "Bat");
        schema.registerSimple((Map)hashMap, "Witch");
        schema.registerSimple((Map)hashMap, "Endermite");
        schema.registerSimple((Map)hashMap, "Guardian");
        schema.registerSimple((Map)hashMap, "Pig");
        schema.registerSimple((Map)hashMap, "Sheep");
        schema.registerSimple((Map)hashMap, "Cow");
        schema.registerSimple((Map)hashMap, "Chicken");
        schema.registerSimple((Map)hashMap, "Squid");
        schema.registerSimple((Map)hashMap, "Wolf");
        schema.registerSimple((Map)hashMap, "MushroomCow");
        schema.registerSimple((Map)hashMap, "SnowMan");
        schema.registerSimple((Map)hashMap, "Ozelot");
        schema.registerSimple((Map)hashMap, "VillagerGolem");
        schema.register((Map)hashMap, "EntityHorse", string -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"ArmorItem", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)hashMap, "Rabbit");
        schema.register((Map)hashMap, "Villager", string -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)References.VILLAGER_TRADE.in(schema)))));
        schema.registerSimple((Map)hashMap, "EnderCrystal");
        schema.register((Map)hashMap, "AreaEffectCloud", string -> DSL.optionalFields((String)"Particle", (TypeTemplate)References.PARTICLE.in(schema)));
        schema.registerSimple((Map)hashMap, "ShulkerBullet");
        schema.registerSimple((Map)hashMap, "DragonFireball");
        schema.registerSimple((Map)hashMap, "Shulker");
        return hashMap;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap hashMap = Maps.newHashMap();
        V99.registerInventory(schema, hashMap, "Furnace");
        V99.registerInventory(schema, hashMap, "Chest");
        schema.registerSimple((Map)hashMap, "EnderChest");
        schema.register((Map)hashMap, "RecordPlayer", string -> DSL.optionalFields((String)"RecordItem", (TypeTemplate)References.ITEM_STACK.in(schema)));
        V99.registerInventory(schema, hashMap, "Trap");
        V99.registerInventory(schema, hashMap, "Dropper");
        schema.register((Map)hashMap, "Sign", () -> V99.sign(schema));
        schema.register((Map)hashMap, "MobSpawner", string -> References.UNTAGGED_SPAWNER.in(schema));
        schema.registerSimple((Map)hashMap, "Music");
        schema.registerSimple((Map)hashMap, "Piston");
        V99.registerInventory(schema, hashMap, "Cauldron");
        schema.registerSimple((Map)hashMap, "EnchantTable");
        schema.registerSimple((Map)hashMap, "Airportal");
        schema.register((Map)hashMap, "Control", () -> DSL.optionalFields((String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)hashMap, "Beacon");
        schema.register((Map)hashMap, "Skull", () -> DSL.optionalFields((String)"custom_name", (TypeTemplate)References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)hashMap, "DLDetector");
        V99.registerInventory(schema, hashMap, "Hopper");
        schema.registerSimple((Map)hashMap, "Comparator");
        schema.register((Map)hashMap, "FlowerPot", string -> DSL.optionalFields((String)"Item", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)References.ITEM_NAME.in(schema))));
        schema.register((Map)hashMap, "Banner", () -> DSL.optionalFields((String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)hashMap, "Structure");
        schema.registerSimple((Map)hashMap, "EndGateway");
        return hashMap;
    }

    public static TypeTemplate sign(Schema schema) {
        return DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"Text1", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"Text2", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"Text3", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"Text4", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText1", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText2", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText3", (Object)References.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText4", (Object)References.TEXT_COMPONENT.in(schema))});
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        schema.registerType(false, References.LEVEL, () -> DSL.optionalFields((String)"CustomBossEvents", (TypeTemplate)DSL.compoundList((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in(schema))), (TypeTemplate)References.LIGHTWEIGHT_LEVEL.in(schema)));
        schema.registerType(false, References.LIGHTWEIGHT_LEVEL, DSL::remainder);
        schema.registerType(false, References.PLAYER, () -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)), (String)"EnderItems", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))));
        schema.registerType(false, References.CHUNK, () -> DSL.fields((String)"Level", (TypeTemplate)DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in(schema)), (String)"TileEntities", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_ENTITY.in(schema), (TypeTemplate)DSL.remainder())), (String)"TileTicks", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"i", (TypeTemplate)References.BLOCK_NAME.in(schema))))));
        schema.registerType(true, References.BLOCK_ENTITY, () -> DSL.optionalFields((String)"components", (TypeTemplate)References.DATA_COMPONENTS.in(schema), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", (Type)DSL.string(), (Map)map2)));
        schema.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields((String)"Riding", (TypeTemplate)References.ENTITY_TREE.in(schema), (TypeTemplate)References.ENTITY.in(schema)));
        schema.registerType(false, References.ENTITY_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        schema.registerType(true, References.ENTITY, () -> DSL.and((TypeTemplate)References.ENTITY_EQUIPMENT.in(schema), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)DSL.constType((Type)DSL.string()), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", (Type)DSL.string(), (Map)map))));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)References.ITEM_NAME.in(schema)), (String)"tag", (TypeTemplate)V99.itemStackTag(schema)), (Hook.HookFunction)ADD_NAMES, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
        schema.registerType(false, References.OPTIONS, DSL::remainder);
        schema.registerType(false, References.BLOCK_NAME, () -> DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)DSL.constType(NamespacedSchema.namespacedString())));
        schema.registerType(false, References.ITEM_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        schema.registerType(false, References.STATS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_TICKETS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_MAP_DATA, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"banners", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, References.SAVED_DATA_MAP_INDEX, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_RAIDS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_SCOREBOARD, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"Objectives", (TypeTemplate)DSL.list((TypeTemplate)References.OBJECTIVE.in(schema)), (String)"Teams", (TypeTemplate)DSL.list((TypeTemplate)References.TEAM.in(schema)), (String)"PlayerScores", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"display", (TypeTemplate)References.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, References.SAVED_DATA_STRUCTURE_FEATURE_INDICES, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"Features", (TypeTemplate)DSL.compoundList((TypeTemplate)References.STRUCTURE_FEATURE.in(schema)))));
        schema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        schema.registerType(false, References.OBJECTIVE, DSL::remainder);
        schema.registerType(false, References.TEAM, () -> DSL.optionalFields((String)"MemberNamePrefix", (TypeTemplate)References.TEXT_COMPONENT.in(schema), (String)"MemberNameSuffix", (TypeTemplate)References.TEXT_COMPONENT.in(schema), (String)"DisplayName", (TypeTemplate)References.TEXT_COMPONENT.in(schema)));
        schema.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
        schema.registerType(false, References.POI_CHUNK, DSL::remainder);
        schema.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in(schema))));
        schema.registerType(true, References.DATA_COMPONENTS, DSL::remainder);
        schema.registerType(true, References.VILLAGER_TRADE, () -> DSL.optionalFields((String)"buy", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"buyB", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"sell", (TypeTemplate)References.ITEM_STACK.in(schema)));
        schema.registerType(true, References.PARTICLE, () -> DSL.constType((Type)DSL.string()));
        schema.registerType(true, References.TEXT_COMPONENT, () -> DSL.constType((Type)DSL.string()));
        schema.registerType(false, References.STRUCTURE, () -> DSL.optionalFields((String)"entities", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)References.ENTITY_TREE.in(schema))), (String)"blocks", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)References.BLOCK_ENTITY.in(schema))), (String)"palette", (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_STATE.in(schema))));
        schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
        schema.registerType(true, References.ENTITY_EQUIPMENT, () -> DSL.optional((TypeTemplate)DSL.field((String)"Equipment", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)))));
    }

    public static TypeTemplate itemStackTag(Schema schema) {
        return DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"EntityTag", (Object)References.ENTITY_TREE.in(schema)), Pair.of((Object)"BlockEntityTag", (Object)References.BLOCK_ENTITY.in(schema)), Pair.of((Object)"CanDestroy", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))), Pair.of((Object)"CanPlaceOn", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))), Pair.of((Object)"Items", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"ChargedProjectiles", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"pages", (Object)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in(schema))), Pair.of((Object)"filtered_pages", (Object)DSL.compoundList((TypeTemplate)References.TEXT_COMPONENT.in(schema))), Pair.of((Object)"display", (Object)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in(schema), (String)"Lore", (TypeTemplate)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in(schema))))});
    }

    protected static <T> T addNames(Dynamic<T> dynamic, Map<String, String> map, Map<String, String> map2) {
        return (T)dynamic.update("tag", dynamic3 -> dynamic3.update("BlockEntityTag", dynamic2 -> {
            String string = dynamic.get("id").asString().result().map(NamespacedSchema::ensureNamespaced).orElse("minecraft:air");
            if (!"minecraft:air".equals(string)) {
                String string2 = (String)map.get(string);
                if (string2 == null) {
                    LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", (Object)string);
                } else {
                    return dynamic2.set("id", dynamic.createString(string2));
                }
            }
            return dynamic2;
        }).update("EntityTag", dynamic2 -> {
            if (dynamic2.get("id").result().isPresent()) {
                return dynamic2;
            }
            String string = NamespacedSchema.ensureNamespaced(dynamic.get("id").asString(""));
            String string2 = (String)map2.get(string);
            if (string2 != null) {
                return dynamic2.set("id", dynamic.createString(string2));
            }
            return dynamic2;
        })).getValue();
    }
}

