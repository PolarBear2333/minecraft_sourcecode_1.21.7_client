/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.material;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;

public class Fluids {
    public static final Fluid EMPTY = Fluids.register("empty", new EmptyFluid());
    public static final FlowingFluid FLOWING_WATER = Fluids.register("flowing_water", new WaterFluid.Flowing());
    public static final FlowingFluid WATER = Fluids.register("water", new WaterFluid.Source());
    public static final FlowingFluid FLOWING_LAVA = Fluids.register("flowing_lava", new LavaFluid.Flowing());
    public static final FlowingFluid LAVA = Fluids.register("lava", new LavaFluid.Source());

    private static <T extends Fluid> T register(String string, T t) {
        return (T)Registry.register(BuiltInRegistries.FLUID, string, t);
    }

    static {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            for (FluidState fluidState : fluid.getStateDefinition().getPossibleStates()) {
                Fluid.FLUID_STATE_REGISTRY.add(fluidState);
            }
        }
    }
}

