/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class RenderSectionRegion
implements BlockAndTintGetter {
    public static final int RADIUS = 1;
    public static final int SIZE = 3;
    private final int minSectionX;
    private final int minSectionY;
    private final int minSectionZ;
    private final SectionCopy[] sections;
    private final Level level;

    RenderSectionRegion(Level level, int n, int n2, int n3, SectionCopy[] sectionCopyArray) {
        this.level = level;
        this.minSectionX = n;
        this.minSectionY = n2;
        this.minSectionZ = n3;
        this.sections = sectionCopyArray;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return this.getSection(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.getSection(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockState(blockPos).getFluidState();
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return this.level.getShade(direction, bl);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        return this.getSection(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockEntity(blockPos);
    }

    private SectionCopy getSection(int n, int n2, int n3) {
        return this.sections[RenderSectionRegion.index(this.minSectionX, this.minSectionY, this.minSectionZ, n, n2, n3)];
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return this.level.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    public static int index(int n, int n2, int n3, int n4, int n5, int n6) {
        return n4 - n + (n5 - n2) * 3 + (n6 - n3) * 3 * 3;
    }
}

