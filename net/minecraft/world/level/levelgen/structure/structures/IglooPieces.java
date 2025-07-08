/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class IglooPieces {
    public static final int GENERATION_HEIGHT = 90;
    static final ResourceLocation STRUCTURE_LOCATION_IGLOO = ResourceLocation.withDefaultNamespace("igloo/top");
    private static final ResourceLocation STRUCTURE_LOCATION_LADDER = ResourceLocation.withDefaultNamespace("igloo/middle");
    private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = ResourceLocation.withDefaultNamespace("igloo/bottom");
    static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of((Object)STRUCTURE_LOCATION_IGLOO, (Object)new BlockPos(3, 5, 5), (Object)STRUCTURE_LOCATION_LADDER, (Object)new BlockPos(1, 3, 1), (Object)STRUCTURE_LOCATION_LABORATORY, (Object)new BlockPos(3, 6, 7));
    static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of((Object)STRUCTURE_LOCATION_IGLOO, (Object)BlockPos.ZERO, (Object)STRUCTURE_LOCATION_LADDER, (Object)new BlockPos(2, -3, 4), (Object)STRUCTURE_LOCATION_LABORATORY, (Object)new BlockPos(0, -3, -2));

    public static void addPieces(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
        if (randomSource.nextDouble() < 0.5) {
            int n = randomSource.nextInt(8) + 4;
            structurePieceAccessor.addPiece(new IglooPiece(structureTemplateManager, STRUCTURE_LOCATION_LABORATORY, blockPos, rotation, n * 3));
            for (int i = 0; i < n - 1; ++i) {
                structurePieceAccessor.addPiece(new IglooPiece(structureTemplateManager, STRUCTURE_LOCATION_LADDER, blockPos, rotation, i * 3));
            }
        }
        structurePieceAccessor.addPiece(new IglooPiece(structureTemplateManager, STRUCTURE_LOCATION_IGLOO, blockPos, rotation, 0));
    }

    public static class IglooPiece
    extends TemplateStructurePiece {
        public IglooPiece(StructureTemplateManager structureTemplateManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, int n) {
            super(StructurePieceType.IGLOO, 0, structureTemplateManager, resourceLocation, resourceLocation.toString(), IglooPiece.makeSettings(rotation, resourceLocation), IglooPiece.makePosition(resourceLocation, blockPos, n));
        }

        public IglooPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
            super(StructurePieceType.IGLOO, compoundTag, structureTemplateManager, resourceLocation -> IglooPiece.makeSettings(compoundTag.read("Rot", Rotation.LEGACY_CODEC).orElseThrow(), resourceLocation));
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation, ResourceLocation resourceLocation) {
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).setRotationPivot(PIVOTS.get(resourceLocation)).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK).setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
        }

        private static BlockPos makePosition(ResourceLocation resourceLocation, BlockPos blockPos, int n) {
            return blockPos.offset(OFFSETS.get(resourceLocation)).below(n);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
        }

        @Override
        protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
            if (!"chest".equals(string)) {
                return;
            }
            serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos.below());
            if (blockEntity instanceof ChestBlockEntity) {
                ((ChestBlockEntity)blockEntity).setLootTable(BuiltInLootTables.IGLOO_CHEST, randomSource.nextLong());
            }
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockPos blockPos2;
            BlockState blockState;
            ResourceLocation resourceLocation = ResourceLocation.parse(this.templateName);
            StructurePlaceSettings structurePlaceSettings = IglooPiece.makeSettings(this.placeSettings.getRotation(), resourceLocation);
            BlockPos blockPos3 = OFFSETS.get(resourceLocation);
            BlockPos blockPos4 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structurePlaceSettings, new BlockPos(3 - blockPos3.getX(), 0, -blockPos3.getZ())));
            int n = worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockPos4.getX(), blockPos4.getZ());
            BlockPos blockPos5 = this.templatePosition;
            this.templatePosition = this.templatePosition.offset(0, n - 90 - 1, 0);
            super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
            if (resourceLocation.equals(STRUCTURE_LOCATION_IGLOO) && !(blockState = worldGenLevel.getBlockState((blockPos2 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structurePlaceSettings, new BlockPos(3, 0, 5)))).below())).isAir() && !blockState.is(Blocks.LADDER)) {
                worldGenLevel.setBlock(blockPos2, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
            }
            this.templatePosition = blockPos5;
        }
    }
}

