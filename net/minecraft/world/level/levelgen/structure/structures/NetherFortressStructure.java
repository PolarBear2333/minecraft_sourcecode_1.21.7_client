/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;

public class NetherFortressStructure
extends Structure {
    public static final WeightedList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedList.builder().add(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 2, 3), 10).add(new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 4, 4), 5).add(new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 5, 5), 8).add(new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 5, 5), 2).add(new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 4, 4), 3).build();
    public static final MapCodec<NetherFortressStructure> CODEC = NetherFortressStructure.simpleCodec(NetherFortressStructure::new);

    public NetherFortressStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        ChunkPos chunkPos = generationContext.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 64, chunkPos.getMinBlockZ());
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> NetherFortressStructure.generatePieces(structurePiecesBuilder, generationContext)));
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        NetherFortressPieces.StartPiece startPiece = new NetherFortressPieces.StartPiece(generationContext.random(), generationContext.chunkPos().getBlockX(2), generationContext.chunkPos().getBlockZ(2));
        structurePiecesBuilder.addPiece(startPiece);
        startPiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
        List<StructurePiece> list = startPiece.pendingChildren;
        while (!list.isEmpty()) {
            int n = generationContext.random().nextInt(list.size());
            StructurePiece structurePiece = list.remove(n);
            structurePiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
        }
        structurePiecesBuilder.moveInsideHeights(generationContext.random(), 48, 70);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.FORTRESS;
    }
}

