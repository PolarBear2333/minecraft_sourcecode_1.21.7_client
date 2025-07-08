/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;

public class BuriedTreasureStructure
extends Structure {
    public static final MapCodec<BuriedTreasureStructure> CODEC = BuriedTreasureStructure.simpleCodec(BuriedTreasureStructure::new);

    public BuriedTreasureStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        return BuriedTreasureStructure.onTopOfChunkCenter(generationContext, Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> BuriedTreasureStructure.generatePieces(structurePiecesBuilder, generationContext));
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        BlockPos blockPos = new BlockPos(generationContext.chunkPos().getBlockX(9), 90, generationContext.chunkPos().getBlockZ(9));
        structurePiecesBuilder.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.BURIED_TREASURE;
    }
}

