/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PoolElementStructurePiece
extends StructurePiece {
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureTemplateManager structureTemplateManager;
    private final LiquidSettings liquidSettings;

    public PoolElementStructurePiece(StructureTemplateManager structureTemplateManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int n, Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings) {
        super(StructurePieceType.JIGSAW, 0, boundingBox);
        this.structureTemplateManager = structureTemplateManager;
        this.element = structurePoolElement;
        this.position = blockPos;
        this.groundLevelDelta = n;
        this.rotation = rotation;
        this.liquidSettings = liquidSettings;
    }

    public PoolElementStructurePiece(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        super(StructurePieceType.JIGSAW, compoundTag);
        this.structureTemplateManager = structurePieceSerializationContext.structureTemplateManager();
        this.position = new BlockPos(compoundTag.getIntOr("PosX", 0), compoundTag.getIntOr("PosY", 0), compoundTag.getIntOr("PosZ", 0));
        this.groundLevelDelta = compoundTag.getIntOr("ground_level_delta", 0);
        RegistryOps<Tag> registryOps = structurePieceSerializationContext.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        this.element = compoundTag.read("pool_element", StructurePoolElement.CODEC, registryOps).orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
        this.rotation = compoundTag.read("rotation", Rotation.LEGACY_CODEC).orElseThrow();
        this.boundingBox = this.element.getBoundingBox(this.structureTemplateManager, this.position, this.rotation);
        ListTag listTag = compoundTag.getListOrEmpty("junctions");
        this.junctions.clear();
        listTag.forEach(tag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic(registryOps, tag))));
        this.liquidSettings = compoundTag.read("liquid_settings", LiquidSettings.CODEC).orElse(JigsawStructure.DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        compoundTag.putInt("PosX", this.position.getX());
        compoundTag.putInt("PosY", this.position.getY());
        compoundTag.putInt("PosZ", this.position.getZ());
        compoundTag.putInt("ground_level_delta", this.groundLevelDelta);
        RegistryOps<Tag> registryOps = structurePieceSerializationContext.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        compoundTag.store("pool_element", StructurePoolElement.CODEC, registryOps, this.element);
        compoundTag.store("rotation", Rotation.LEGACY_CODEC, this.rotation);
        ListTag listTag = new ListTag();
        for (JigsawJunction jigsawJunction : this.junctions) {
            listTag.add((Tag)jigsawJunction.serialize(registryOps).getValue());
        }
        compoundTag.put("junctions", listTag);
        if (this.liquidSettings != JigsawStructure.DEFAULT_LIQUID_SETTINGS) {
            compoundTag.store("liquid_settings", LiquidSettings.CODEC, registryOps, this.liquidSettings);
        }
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        this.place(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, blockPos, false);
    }

    public void place(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, BlockPos blockPos, boolean bl) {
        this.element.place(this.structureTemplateManager, worldGenLevel, structureManager, chunkGenerator, this.position, blockPos, this.rotation, boundingBox, randomSource, this.liquidSettings, bl);
    }

    @Override
    public void move(int n, int n2, int n3) {
        super.move(n, n2, n3);
        this.position = this.position.offset(n, n2, n3);
    }

    @Override
    public Rotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
    }

    public StructurePoolElement getElement() {
        return this.element;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction jigsawJunction) {
        this.junctions.add(jigsawJunction);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }
}

