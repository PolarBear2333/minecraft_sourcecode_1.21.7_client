/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class Attributes {
    public static final Holder<Attribute> ARMOR = Attributes.register("armor", new RangedAttribute("attribute.name.armor", 0.0, 0.0, 30.0).setSyncable(true));
    public static final Holder<Attribute> ARMOR_TOUGHNESS = Attributes.register("armor_toughness", new RangedAttribute("attribute.name.armor_toughness", 0.0, 0.0, 20.0).setSyncable(true));
    public static final Holder<Attribute> ATTACK_DAMAGE = Attributes.register("attack_damage", new RangedAttribute("attribute.name.attack_damage", 2.0, 0.0, 2048.0));
    public static final Holder<Attribute> ATTACK_KNOCKBACK = Attributes.register("attack_knockback", new RangedAttribute("attribute.name.attack_knockback", 0.0, 0.0, 5.0));
    public static final Holder<Attribute> ATTACK_SPEED = Attributes.register("attack_speed", new RangedAttribute("attribute.name.attack_speed", 4.0, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> BLOCK_BREAK_SPEED = Attributes.register("block_break_speed", new RangedAttribute("attribute.name.block_break_speed", 1.0, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> BLOCK_INTERACTION_RANGE = Attributes.register("block_interaction_range", new RangedAttribute("attribute.name.block_interaction_range", 4.5, 0.0, 64.0).setSyncable(true));
    public static final Holder<Attribute> BURNING_TIME = Attributes.register("burning_time", new RangedAttribute("attribute.name.burning_time", 1.0, 0.0, 1024.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEGATIVE));
    public static final Holder<Attribute> CAMERA_DISTANCE = Attributes.register("camera_distance", new RangedAttribute("attribute.name.camera_distance", 4.0, 0.0, 32.0).setSyncable(true));
    public static final Holder<Attribute> EXPLOSION_KNOCKBACK_RESISTANCE = Attributes.register("explosion_knockback_resistance", new RangedAttribute("attribute.name.explosion_knockback_resistance", 0.0, 0.0, 1.0).setSyncable(true));
    public static final Holder<Attribute> ENTITY_INTERACTION_RANGE = Attributes.register("entity_interaction_range", new RangedAttribute("attribute.name.entity_interaction_range", 3.0, 0.0, 64.0).setSyncable(true));
    public static final Holder<Attribute> FALL_DAMAGE_MULTIPLIER = Attributes.register("fall_damage_multiplier", new RangedAttribute("attribute.name.fall_damage_multiplier", 1.0, 0.0, 100.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEGATIVE));
    public static final Holder<Attribute> FLYING_SPEED = Attributes.register("flying_speed", new RangedAttribute("attribute.name.flying_speed", 0.4, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> FOLLOW_RANGE = Attributes.register("follow_range", new RangedAttribute("attribute.name.follow_range", 32.0, 0.0, 2048.0));
    public static final Holder<Attribute> GRAVITY = Attributes.register("gravity", new RangedAttribute("attribute.name.gravity", 0.08, -1.0, 1.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));
    public static final Holder<Attribute> JUMP_STRENGTH = Attributes.register("jump_strength", new RangedAttribute("attribute.name.jump_strength", 0.42f, 0.0, 32.0).setSyncable(true));
    public static final Holder<Attribute> KNOCKBACK_RESISTANCE = Attributes.register("knockback_resistance", new RangedAttribute("attribute.name.knockback_resistance", 0.0, 0.0, 1.0));
    public static final Holder<Attribute> LUCK = Attributes.register("luck", new RangedAttribute("attribute.name.luck", 0.0, -1024.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> MAX_ABSORPTION = Attributes.register("max_absorption", new RangedAttribute("attribute.name.max_absorption", 0.0, 0.0, 2048.0).setSyncable(true));
    public static final Holder<Attribute> MAX_HEALTH = Attributes.register("max_health", new RangedAttribute("attribute.name.max_health", 20.0, 1.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> MINING_EFFICIENCY = Attributes.register("mining_efficiency", new RangedAttribute("attribute.name.mining_efficiency", 0.0, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> MOVEMENT_EFFICIENCY = Attributes.register("movement_efficiency", new RangedAttribute("attribute.name.movement_efficiency", 0.0, 0.0, 1.0).setSyncable(true));
    public static final Holder<Attribute> MOVEMENT_SPEED = Attributes.register("movement_speed", new RangedAttribute("attribute.name.movement_speed", 0.7, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> OXYGEN_BONUS = Attributes.register("oxygen_bonus", new RangedAttribute("attribute.name.oxygen_bonus", 0.0, 0.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> SAFE_FALL_DISTANCE = Attributes.register("safe_fall_distance", new RangedAttribute("attribute.name.safe_fall_distance", 3.0, -1024.0, 1024.0).setSyncable(true));
    public static final Holder<Attribute> SCALE = Attributes.register("scale", new RangedAttribute("attribute.name.scale", 1.0, 0.0625, 16.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL));
    public static final Holder<Attribute> SNEAKING_SPEED = Attributes.register("sneaking_speed", new RangedAttribute("attribute.name.sneaking_speed", 0.3, 0.0, 1.0).setSyncable(true));
    public static final Holder<Attribute> SPAWN_REINFORCEMENTS_CHANCE = Attributes.register("spawn_reinforcements", new RangedAttribute("attribute.name.spawn_reinforcements", 0.0, 0.0, 1.0));
    public static final Holder<Attribute> STEP_HEIGHT = Attributes.register("step_height", new RangedAttribute("attribute.name.step_height", 0.6, 0.0, 10.0).setSyncable(true));
    public static final Holder<Attribute> SUBMERGED_MINING_SPEED = Attributes.register("submerged_mining_speed", new RangedAttribute("attribute.name.submerged_mining_speed", 0.2, 0.0, 20.0).setSyncable(true));
    public static final Holder<Attribute> SWEEPING_DAMAGE_RATIO = Attributes.register("sweeping_damage_ratio", new RangedAttribute("attribute.name.sweeping_damage_ratio", 0.0, 0.0, 1.0).setSyncable(true));
    public static final Holder<Attribute> TEMPT_RANGE = Attributes.register("tempt_range", new RangedAttribute("attribute.name.tempt_range", 10.0, 0.0, 2048.0));
    public static final Holder<Attribute> WATER_MOVEMENT_EFFICIENCY = Attributes.register("water_movement_efficiency", new RangedAttribute("attribute.name.water_movement_efficiency", 0.0, 0.0, 1.0).setSyncable(true));
    public static final Holder<Attribute> WAYPOINT_TRANSMIT_RANGE = Attributes.register("waypoint_transmit_range", new RangedAttribute("attribute.name.waypoint_transmit_range", 0.0, 0.0, 6.0E7).setSentiment(Attribute.Sentiment.NEUTRAL));
    public static final Holder<Attribute> WAYPOINT_RECEIVE_RANGE = Attributes.register("waypoint_receive_range", new RangedAttribute("attribute.name.waypoint_receive_range", 0.0, 0.0, 6.0E7).setSentiment(Attribute.Sentiment.NEUTRAL));

    private static Holder<Attribute> register(String string, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ResourceLocation.withDefaultNamespace(string), attribute);
    }

    public static Holder<Attribute> bootstrap(Registry<Attribute> registry) {
        return MAX_HEALTH;
    }
}

