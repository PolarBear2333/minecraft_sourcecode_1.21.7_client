/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.Util;
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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherFossilPieces {
    private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("nether_fossils/fossil_1"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_2"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_3"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_4"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_5"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_6"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_7"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_8"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_9"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_10"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_11"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_12"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_13"), ResourceLocation.withDefaultNamespace("nether_fossils/fossil_14")};

    public static void addPieces(StructureTemplateManager structureTemplateManager, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, BlockPos blockPos) {
        Rotation rotation = Rotation.getRandom(randomSource);
        structurePieceAccessor.addPiece(new NetherFossilPiece(structureTemplateManager, Util.getRandom(FOSSILS, randomSource), blockPos, rotation));
    }

    public static class NetherFossilPiece
    extends TemplateStructurePiece {
        public NetherFossilPiece(StructureTemplateManager structureTemplateManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation) {
            super(StructurePieceType.NETHER_FOSSIL, 0, structureTemplateManager, resourceLocation, resourceLocation.toString(), NetherFossilPiece.makeSettings(rotation), blockPos);
        }

        public NetherFossilPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FOSSIL, compoundTag, structureTemplateManager, (ResourceLocation resourceLocation) -> NetherFossilPiece.makeSettings(compoundTag.read("Rot", Rotation.LEGACY_CODEC).orElseThrow()));
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation) {
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.store("Rot", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
        }

        @Override
        protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BoundingBox boundingBox2 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
            boundingBox.encapsulate(boundingBox2);
            super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
            this.placeDriedGhast(worldGenLevel, randomSource, boundingBox2, boundingBox);
        }

        private void placeDriedGhast(WorldGenLevel worldGenLevel, RandomSource randomSource, BoundingBox boundingBox, BoundingBox boundingBox2) {
            int n;
            int n2;
            int n3;
            BlockPos blockPos;
            RandomSource randomSource2 = RandomSource.create(worldGenLevel.getSeed()).forkPositional().at(boundingBox.getCenter());
            if (randomSource2.nextFloat() < 0.5f && worldGenLevel.getBlockState(blockPos = new BlockPos(n3 = boundingBox.minX() + randomSource2.nextInt(boundingBox.getXSpan()), n2 = boundingBox.minY(), n = boundingBox.minZ() + randomSource2.nextInt(boundingBox.getZSpan()))).isAir() && boundingBox2.isInside(blockPos)) {
                worldGenLevel.setBlock(blockPos, Blocks.DRIED_GHAST.defaultBlockState().rotate(Rotation.getRandom(randomSource2)), 2);
            }
        }
    }
}

