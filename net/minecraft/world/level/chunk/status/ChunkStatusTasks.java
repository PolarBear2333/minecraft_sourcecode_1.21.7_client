/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.chunk.status;

import com.mojang.logging.LogUtils;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

public class ChunkStatusTasks {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean isLighted(ChunkAccess chunkAccess) {
        return chunkAccess.getPersistedStatus().isOrAfter(ChunkStatus.LIGHT) && chunkAccess.isLightCorrect();
    }

    static CompletableFuture<ChunkAccess> passThrough(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> generateStructureStarts(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ServerLevel serverLevel = worldGenContext.level();
        if (serverLevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
            worldGenContext.generator().createStructures(serverLevel.registryAccess(), serverLevel.getChunkSource().getGeneratorState(), serverLevel.structureManager(), chunkAccess, worldGenContext.structureManager(), serverLevel.dimension());
        }
        serverLevel.onStructureStartsAvailable(chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> loadStructureStarts(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        worldGenContext.level().onStructureStartsAvailable(chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> generateStructureReferences(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ServerLevel serverLevel = worldGenContext.level();
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
        worldGenContext.generator().createReferences(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> generateBiomes(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ServerLevel serverLevel = worldGenContext.level();
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
        return worldGenContext.generator().createBiomes(serverLevel.getChunkSource().randomState(), Blender.of(worldGenRegion), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
    }

    static CompletableFuture<ChunkAccess> generateNoise(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess2) {
        ServerLevel serverLevel = worldGenContext.level();
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess2);
        return worldGenContext.generator().fillFromNoise(Blender.of(worldGenRegion), serverLevel.getChunkSource().randomState(), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess2).thenApply(chunkAccess -> {
            ProtoChunk protoChunk;
            BelowZeroRetrogen belowZeroRetrogen;
            if (chunkAccess instanceof ProtoChunk && (belowZeroRetrogen = (protoChunk = (ProtoChunk)chunkAccess).getBelowZeroRetrogen()) != null) {
                BelowZeroRetrogen.replaceOldBedrock(protoChunk);
                if (belowZeroRetrogen.hasBedrockHoles()) {
                    belowZeroRetrogen.applyBedrockMask(protoChunk);
                }
            }
            return chunkAccess;
        });
    }

    static CompletableFuture<ChunkAccess> generateSurface(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ServerLevel serverLevel = worldGenContext.level();
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
        worldGenContext.generator().buildSurface(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), serverLevel.getChunkSource().randomState(), chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> generateCarvers(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ServerLevel serverLevel = worldGenContext.level();
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
        if (chunkAccess instanceof ProtoChunk) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            Blender.addAroundOldChunksCarvingMaskFilter(worldGenRegion, protoChunk);
        }
        worldGenContext.generator().applyCarvers(worldGenRegion, serverLevel.getSeed(), serverLevel.getChunkSource().randomState(), serverLevel.getBiomeManager(), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> generateFeatures(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ServerLevel serverLevel = worldGenContext.level();
        Heightmap.primeHeightmaps(chunkAccess, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
        worldGenContext.generator().applyBiomeDecoration(worldGenRegion, chunkAccess, serverLevel.structureManager().forWorldGenRegion(worldGenRegion));
        Blender.generateBorderTicks(worldGenRegion, chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> initializeLight(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ThreadedLevelLightEngine threadedLevelLightEngine = worldGenContext.lightEngine();
        chunkAccess.initializeLightSources();
        ((ProtoChunk)chunkAccess).setLightEngine(threadedLevelLightEngine);
        boolean bl = ChunkStatusTasks.isLighted(chunkAccess);
        return threadedLevelLightEngine.initializeLight(chunkAccess, bl);
    }

    static CompletableFuture<ChunkAccess> light(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        boolean bl = ChunkStatusTasks.isLighted(chunkAccess);
        return worldGenContext.lightEngine().lightChunk(chunkAccess, bl);
    }

    static CompletableFuture<ChunkAccess> generateSpawn(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        if (!chunkAccess.isUpgrading()) {
            worldGenContext.generator().spawnOriginalMobs(new WorldGenRegion(worldGenContext.level(), staticCache2D, chunkStep, chunkAccess));
        }
        return CompletableFuture.completedFuture(chunkAccess);
    }

    static CompletableFuture<ChunkAccess> full(WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        GenerationChunkHolder generationChunkHolder = staticCache2D.get(chunkPos.x, chunkPos.z);
        return CompletableFuture.supplyAsync(() -> {
            LevelChunk levelChunk2;
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            ServerLevel serverLevel = worldGenContext.level();
            if (protoChunk instanceof ImposterProtoChunk) {
                ImposterProtoChunk imposterProtoChunk = (ImposterProtoChunk)protoChunk;
                levelChunk2 = imposterProtoChunk.getWrapped();
            } else {
                levelChunk2 = new LevelChunk(serverLevel, protoChunk, levelChunk -> {
                    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(chunkAccess.problemPath(), LOGGER);){
                        ChunkStatusTasks.postLoadProtoChunk(serverLevel, TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)serverLevel.registryAccess(), protoChunk.getEntities()));
                    }
                });
                generationChunkHolder.replaceProtoChunk(new ImposterProtoChunk(levelChunk2, false));
            }
            levelChunk2.setFullStatus(generationChunkHolder::getFullStatus);
            levelChunk2.runPostLoad();
            levelChunk2.setLoaded(true);
            levelChunk2.registerAllBlockEntitiesAfterLevelLoad();
            levelChunk2.registerTickContainerInLevel(serverLevel);
            levelChunk2.setUnsavedListener(worldGenContext.unsavedListener());
            return levelChunk2;
        }, worldGenContext.mainThreadExecutor());
    }

    private static void postLoadProtoChunk(ServerLevel serverLevel, ValueInput.ValueInputList valueInputList) {
        if (!valueInputList.isEmpty()) {
            serverLevel.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(valueInputList, serverLevel, EntitySpawnReason.LOAD));
        }
    }
}

