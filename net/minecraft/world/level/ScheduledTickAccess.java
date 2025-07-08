/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface ScheduledTickAccess {
    public <T> ScheduledTick<T> createTick(BlockPos var1, T var2, int var3, TickPriority var4);

    public <T> ScheduledTick<T> createTick(BlockPos var1, T var2, int var3);

    public LevelTickAccess<Block> getBlockTicks();

    default public void scheduleTick(BlockPos blockPos, Block block, int n, TickPriority tickPriority) {
        this.getBlockTicks().schedule(this.createTick(blockPos, block, n, tickPriority));
    }

    default public void scheduleTick(BlockPos blockPos, Block block, int n) {
        this.getBlockTicks().schedule(this.createTick(blockPos, block, n));
    }

    public LevelTickAccess<Fluid> getFluidTicks();

    default public void scheduleTick(BlockPos blockPos, Fluid fluid, int n, TickPriority tickPriority) {
        this.getFluidTicks().schedule(this.createTick(blockPos, fluid, n, tickPriority));
    }

    default public void scheduleTick(BlockPos blockPos, Fluid fluid, int n) {
        this.getFluidTicks().schedule(this.createTick(blockPos, fluid, n));
    }
}

