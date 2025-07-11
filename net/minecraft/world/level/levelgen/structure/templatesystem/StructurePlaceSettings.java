/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructurePlaceSettings {
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private BlockPos rotationPivot = BlockPos.ZERO;
    private boolean ignoreEntities;
    @Nullable
    private BoundingBox boundingBox;
    private LiquidSettings liquidSettings = LiquidSettings.APPLY_WATERLOGGING;
    @Nullable
    private RandomSource random;
    private int palette;
    private final List<StructureProcessor> processors = Lists.newArrayList();
    private boolean knownShape;
    private boolean finalizeEntities;

    public StructurePlaceSettings copy() {
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
        structurePlaceSettings.mirror = this.mirror;
        structurePlaceSettings.rotation = this.rotation;
        structurePlaceSettings.rotationPivot = this.rotationPivot;
        structurePlaceSettings.ignoreEntities = this.ignoreEntities;
        structurePlaceSettings.boundingBox = this.boundingBox;
        structurePlaceSettings.liquidSettings = this.liquidSettings;
        structurePlaceSettings.random = this.random;
        structurePlaceSettings.palette = this.palette;
        structurePlaceSettings.processors.addAll(this.processors);
        structurePlaceSettings.knownShape = this.knownShape;
        structurePlaceSettings.finalizeEntities = this.finalizeEntities;
        return structurePlaceSettings;
    }

    public StructurePlaceSettings setMirror(Mirror mirror) {
        this.mirror = mirror;
        return this;
    }

    public StructurePlaceSettings setRotation(Rotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public StructurePlaceSettings setRotationPivot(BlockPos blockPos) {
        this.rotationPivot = blockPos;
        return this;
    }

    public StructurePlaceSettings setIgnoreEntities(boolean bl) {
        this.ignoreEntities = bl;
        return this;
    }

    public StructurePlaceSettings setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    public StructurePlaceSettings setRandom(@Nullable RandomSource randomSource) {
        this.random = randomSource;
        return this;
    }

    public StructurePlaceSettings setLiquidSettings(LiquidSettings liquidSettings) {
        this.liquidSettings = liquidSettings;
        return this;
    }

    public StructurePlaceSettings setKnownShape(boolean bl) {
        this.knownShape = bl;
        return this;
    }

    public StructurePlaceSettings clearProcessors() {
        this.processors.clear();
        return this;
    }

    public StructurePlaceSettings addProcessor(StructureProcessor structureProcessor) {
        this.processors.add(structureProcessor);
        return this;
    }

    public StructurePlaceSettings popProcessor(StructureProcessor structureProcessor) {
        this.processors.remove(structureProcessor);
        return this;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public BlockPos getRotationPivot() {
        return this.rotationPivot;
    }

    public RandomSource getRandom(@Nullable BlockPos blockPos) {
        if (this.random != null) {
            return this.random;
        }
        if (blockPos == null) {
            return RandomSource.create(Util.getMillis());
        }
        return RandomSource.create(Mth.getSeed(blockPos));
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    @Nullable
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public boolean getKnownShape() {
        return this.knownShape;
    }

    public List<StructureProcessor> getProcessors() {
        return this.processors;
    }

    public boolean shouldApplyWaterlogging() {
        return this.liquidSettings == LiquidSettings.APPLY_WATERLOGGING;
    }

    public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> list, @Nullable BlockPos blockPos) {
        int n = list.size();
        if (n == 0) {
            throw new IllegalStateException("No palettes");
        }
        return list.get(this.getRandom(blockPos).nextInt(n));
    }

    public StructurePlaceSettings setFinalizeEntities(boolean bl) {
        this.finalizeEntities = bl;
        return this;
    }

    public boolean shouldFinalizeEntities() {
        return this.finalizeEntities;
    }
}

