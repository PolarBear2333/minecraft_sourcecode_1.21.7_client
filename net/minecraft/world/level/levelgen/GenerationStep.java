/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public class GenerationStep {

    public static enum Decoration implements StringRepresentable
    {
        RAW_GENERATION("raw_generation"),
        LAKES("lakes"),
        LOCAL_MODIFICATIONS("local_modifications"),
        UNDERGROUND_STRUCTURES("underground_structures"),
        SURFACE_STRUCTURES("surface_structures"),
        STRONGHOLDS("strongholds"),
        UNDERGROUND_ORES("underground_ores"),
        UNDERGROUND_DECORATION("underground_decoration"),
        FLUID_SPRINGS("fluid_springs"),
        VEGETAL_DECORATION("vegetal_decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification");

        public static final Codec<Decoration> CODEC;
        private final String name;

        private Decoration(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Decoration::values);
        }
    }
}

