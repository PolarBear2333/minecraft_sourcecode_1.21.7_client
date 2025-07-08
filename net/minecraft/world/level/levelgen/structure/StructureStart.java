/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.slf4j.Logger;

public final class StructureStart {
    public static final String INVALID_START_ID = "INVALID";
    public static final StructureStart INVALID_START = new StructureStart(null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Structure structure;
    private final PiecesContainer pieceContainer;
    private final ChunkPos chunkPos;
    private int references;
    @Nullable
    private volatile BoundingBox cachedBoundingBox;

    public StructureStart(Structure structure, ChunkPos chunkPos, int n, PiecesContainer piecesContainer) {
        this.structure = structure;
        this.chunkPos = chunkPos;
        this.references = n;
        this.pieceContainer = piecesContainer;
    }

    @Nullable
    public static StructureStart loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        String string = compoundTag.getStringOr("id", "");
        if (INVALID_START_ID.equals(string)) {
            return INVALID_START;
        }
        HolderLookup.RegistryLookup registryLookup = structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Structure structure = (Structure)registryLookup.getValue(ResourceLocation.parse(string));
        if (structure == null) {
            LOGGER.error("Unknown stucture id: {}", (Object)string);
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(compoundTag.getIntOr("ChunkX", 0), compoundTag.getIntOr("ChunkZ", 0));
        int n = compoundTag.getIntOr("references", 0);
        ListTag listTag = compoundTag.getListOrEmpty("Children");
        try {
            PiecesContainer piecesContainer = PiecesContainer.load(listTag, structurePieceSerializationContext);
            if (structure instanceof OceanMonumentStructure) {
                piecesContainer = OceanMonumentStructure.regeneratePiecesAfterLoad(chunkPos, l, piecesContainer);
            }
            return new StructureStart(structure, chunkPos, n, piecesContainer);
        }
        catch (Exception exception) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)exception);
            return null;
        }
    }

    public BoundingBox getBoundingBox() {
        BoundingBox boundingBox = this.cachedBoundingBox;
        if (boundingBox == null) {
            this.cachedBoundingBox = boundingBox = this.structure.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
        }
        return boundingBox;
    }

    public void placeInChunk(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos) {
        List<StructurePiece> list = this.pieceContainer.pieces();
        if (list.isEmpty()) {
            return;
        }
        BoundingBox boundingBox2 = list.get((int)0).boundingBox;
        BlockPos blockPos = boundingBox2.getCenter();
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), boundingBox2.minY(), blockPos.getZ());
        for (StructurePiece structurePiece : list) {
            if (!structurePiece.getBoundingBox().intersects(boundingBox)) continue;
            structurePiece.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos2);
        }
        this.structure.afterPlace(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, this.pieceContainer);
    }

    public CompoundTag createTag(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos) {
        CompoundTag compoundTag = new CompoundTag();
        if (!this.isValid()) {
            compoundTag.putString("id", INVALID_START_ID);
            return compoundTag;
        }
        compoundTag.putString("id", structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(this.structure).toString());
        compoundTag.putInt("ChunkX", chunkPos.x);
        compoundTag.putInt("ChunkZ", chunkPos.z);
        compoundTag.putInt("references", this.references);
        compoundTag.put("Children", this.pieceContainer.save(structurePieceSerializationContext));
        return compoundTag;
    }

    public boolean isValid() {
        return !this.pieceContainer.isEmpty();
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public boolean canBeReferenced() {
        return this.references < this.getMaxReferences();
    }

    public void addReference() {
        ++this.references;
    }

    public int getReferences() {
        return this.references;
    }

    protected int getMaxReferences() {
        return 1;
    }

    public Structure getStructure() {
        return this.structure;
    }

    public List<StructurePiece> getPieces() {
        return this.pieceContainer.pieces();
    }
}

