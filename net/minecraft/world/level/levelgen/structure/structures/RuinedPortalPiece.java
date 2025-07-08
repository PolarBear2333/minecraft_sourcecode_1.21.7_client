/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class RuinedPortalPiece
extends TemplateStructurePiece {
    private static final float PROBABILITY_OF_GOLD_GONE = 0.3f;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07f;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2f;
    private final VerticalPlacement verticalPlacement;
    private final Properties properties;

    public RuinedPortalPiece(StructureTemplateManager structureTemplateManager, BlockPos blockPos, VerticalPlacement verticalPlacement, Properties properties, ResourceLocation resourceLocation, StructureTemplate structureTemplate, Rotation rotation, Mirror mirror, BlockPos blockPos2) {
        super(StructurePieceType.RUINED_PORTAL, 0, structureTemplateManager, resourceLocation, resourceLocation.toString(), RuinedPortalPiece.makeSettings(mirror, rotation, verticalPlacement, blockPos2, properties), blockPos);
        this.verticalPlacement = verticalPlacement;
        this.properties = properties;
    }

    public RuinedPortalPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
        super(StructurePieceType.RUINED_PORTAL, compoundTag, structureTemplateManager, resourceLocation -> RuinedPortalPiece.makeSettings(structureTemplateManager, compoundTag, resourceLocation));
        this.verticalPlacement = compoundTag.read("VerticalPlacement", VerticalPlacement.CODEC).orElseThrow();
        this.properties = compoundTag.read("Properties", Properties.CODEC).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
        compoundTag.store("Rotation", Rotation.LEGACY_CODEC, this.placeSettings.getRotation());
        compoundTag.store("Mirror", Mirror.LEGACY_CODEC, this.placeSettings.getMirror());
        compoundTag.store("VerticalPlacement", VerticalPlacement.CODEC, this.verticalPlacement);
        compoundTag.store("Properties", Properties.CODEC, this.properties);
    }

    private static StructurePlaceSettings makeSettings(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag, ResourceLocation resourceLocation) {
        StructureTemplate structureTemplate = structureTemplateManager.getOrCreate(resourceLocation);
        BlockPos blockPos = new BlockPos(structureTemplate.getSize().getX() / 2, 0, structureTemplate.getSize().getZ() / 2);
        return RuinedPortalPiece.makeSettings(compoundTag.read("Mirror", Mirror.LEGACY_CODEC).orElseThrow(), compoundTag.read("Rotation", Rotation.LEGACY_CODEC).orElseThrow(), compoundTag.read("VerticalPlacement", VerticalPlacement.CODEC).orElseThrow(), blockPos, (Properties)Properties.CODEC.parse(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("Properties"))).getPartialOrThrow());
    }

    private static StructurePlaceSettings makeSettings(Mirror mirror, Rotation rotation, VerticalPlacement verticalPlacement, BlockPos blockPos, Properties properties) {
        BlockIgnoreProcessor blockIgnoreProcessor = properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
        ArrayList arrayList = Lists.newArrayList();
        arrayList.add(RuinedPortalPiece.getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3f, Blocks.AIR));
        arrayList.add(RuinedPortalPiece.getLavaProcessorRule(verticalPlacement, properties));
        if (!properties.cold) {
            arrayList.add(RuinedPortalPiece.getBlockReplaceRule(Blocks.NETHERRACK, 0.07f, Blocks.MAGMA_BLOCK));
        }
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror).setRotationPivot(blockPos).addProcessor(blockIgnoreProcessor).addProcessor(new RuleProcessor(arrayList)).addProcessor(new BlockAgeProcessor(properties.mossiness)).addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)).addProcessor(new LavaSubmergedBlockProcessor());
        if (properties.replaceWithBlackstone) {
            structurePlaceSettings.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
        }
        return structurePlaceSettings;
    }

    private static ProcessorRule getLavaProcessorRule(VerticalPlacement verticalPlacement, Properties properties) {
        if (verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR) {
            return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        }
        if (properties.cold) {
            return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK);
        }
        return RuinedPortalPiece.getBlockReplaceRule(Blocks.LAVA, 0.2f, Blocks.MAGMA_BLOCK);
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos2) {
        BoundingBox boundingBox2 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (!boundingBox.isInside(boundingBox2.getCenter())) {
            return;
        }
        boundingBox.encapsulate(boundingBox2);
        super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos2);
        this.spreadNetherrack(randomSource, worldGenLevel);
        this.addNetherrackDripColumnsBelowPortal(randomSource, worldGenLevel);
        if (this.properties.vines || this.properties.overgrown) {
            BlockPos.betweenClosedStream(this.getBoundingBox()).forEach(blockPos -> {
                if (this.properties.vines) {
                    this.maybeAddVines(randomSource, worldGenLevel, (BlockPos)blockPos);
                }
                if (this.properties.overgrown) {
                    this.maybeAddLeavesAbove(randomSource, worldGenLevel, (BlockPos)blockPos);
                }
            });
        }
    }

    @Override
    protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
    }

    private void maybeAddVines(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.isAir() || blockState.is(Blocks.VINE)) {
            return;
        }
        Direction direction = RuinedPortalPiece.getRandomHorizontalDirection(randomSource);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
        if (!blockState2.isAir()) {
            return;
        }
        if (!Block.isFaceFull(blockState.getCollisionShape(levelAccessor, blockPos), direction)) {
            return;
        }
        BooleanProperty booleanProperty = VineBlock.getPropertyForFace(direction.getOpposite());
        levelAccessor.setBlock(blockPos2, (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true), 3);
    }

    private void maybeAddLeavesAbove(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
        if (randomSource.nextFloat() < 0.5f && levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK) && levelAccessor.getBlockState(blockPos.above()).isAir()) {
            levelAccessor.setBlock(blockPos.above(), (BlockState)Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
        }
    }

    private void addNetherrackDripColumnsBelowPortal(RandomSource randomSource, LevelAccessor levelAccessor) {
        for (int i = this.boundingBox.minX() + 1; i < this.boundingBox.maxX(); ++i) {
            for (int j = this.boundingBox.minZ() + 1; j < this.boundingBox.maxZ(); ++j) {
                BlockPos blockPos = new BlockPos(i, this.boundingBox.minY(), j);
                if (!levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK)) continue;
                this.addNetherrackDripColumn(randomSource, levelAccessor, blockPos.below());
            }
        }
    }

    private void addNetherrackDripColumn(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        this.placeNetherrackOrMagma(randomSource, levelAccessor, mutableBlockPos);
        for (int i = 8; i > 0 && randomSource.nextFloat() < 0.5f; --i) {
            mutableBlockPos.move(Direction.DOWN);
            this.placeNetherrackOrMagma(randomSource, levelAccessor, mutableBlockPos);
        }
    }

    private void spreadNetherrack(RandomSource randomSource, LevelAccessor levelAccessor) {
        boolean bl = this.verticalPlacement == VerticalPlacement.ON_LAND_SURFACE || this.verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR;
        BlockPos blockPos = this.boundingBox.getCenter();
        int n = blockPos.getX();
        int n2 = blockPos.getZ();
        float[] fArray = new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f, 0.9f, 0.8f, 0.7f, 0.6f, 0.4f, 0.2f};
        int n3 = fArray.length;
        int n4 = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
        int n5 = randomSource.nextInt(Math.max(1, 8 - n4 / 2));
        int n6 = 3;
        BlockPos.MutableBlockPos mutableBlockPos = BlockPos.ZERO.mutable();
        for (int i = n - n3; i <= n + n3; ++i) {
            for (int j = n2 - n3; j <= n2 + n3; ++j) {
                int n7 = Math.abs(i - n) + Math.abs(j - n2);
                int n8 = Math.max(0, n7 + n5);
                if (n8 >= n3) continue;
                float f = fArray[n8];
                if (!(randomSource.nextDouble() < (double)f)) continue;
                int n9 = RuinedPortalPiece.getSurfaceY(levelAccessor, i, j, this.verticalPlacement);
                int n10 = bl ? n9 : Math.min(this.boundingBox.minY(), n9);
                mutableBlockPos.set(i, n10, j);
                if (Math.abs(n10 - this.boundingBox.minY()) > 3 || !this.canBlockBeReplacedByNetherrackOrMagma(levelAccessor, mutableBlockPos)) continue;
                this.placeNetherrackOrMagma(randomSource, levelAccessor, mutableBlockPos);
                if (this.properties.overgrown) {
                    this.maybeAddLeavesAbove(randomSource, levelAccessor, mutableBlockPos);
                }
                this.addNetherrackDripColumn(randomSource, levelAccessor, (BlockPos)mutableBlockPos.below());
            }
        }
    }

    private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return !blockState.is(Blocks.AIR) && !blockState.is(Blocks.OBSIDIAN) && !blockState.is(BlockTags.FEATURES_CANNOT_REPLACE) && (this.verticalPlacement == VerticalPlacement.IN_NETHER || !blockState.is(Blocks.LAVA));
    }

    private void placeNetherrackOrMagma(RandomSource randomSource, LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!this.properties.cold && randomSource.nextFloat() < 0.07f) {
            levelAccessor.setBlock(blockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            levelAccessor.setBlock(blockPos, Blocks.NETHERRACK.defaultBlockState(), 3);
        }
    }

    private static int getSurfaceY(LevelAccessor levelAccessor, int n, int n2, VerticalPlacement verticalPlacement) {
        return levelAccessor.getHeight(RuinedPortalPiece.getHeightMapType(verticalPlacement), n, n2) - 1;
    }

    public static Heightmap.Types getHeightMapType(VerticalPlacement verticalPlacement) {
        return verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
    }

    private static ProcessorRule getBlockReplaceRule(Block block, float f, Block block2) {
        return new ProcessorRule(new RandomBlockMatchTest(block, f), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
    }

    private static ProcessorRule getBlockReplaceRule(Block block, Block block2) {
        return new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
    }

    public static enum VerticalPlacement implements StringRepresentable
    {
        ON_LAND_SURFACE("on_land_surface"),
        PARTLY_BURIED("partly_buried"),
        ON_OCEAN_FLOOR("on_ocean_floor"),
        IN_MOUNTAIN("in_mountain"),
        UNDERGROUND("underground"),
        IN_NETHER("in_nether");

        public static final Codec<VerticalPlacement> CODEC;
        private final String name;

        private VerticalPlacement(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(VerticalPlacement::values);
        }
    }

    public static class Properties {
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.fieldOf("cold").forGetter(properties -> properties.cold), (App)Codec.FLOAT.fieldOf("mossiness").forGetter(properties -> Float.valueOf(properties.mossiness)), (App)Codec.BOOL.fieldOf("air_pocket").forGetter(properties -> properties.airPocket), (App)Codec.BOOL.fieldOf("overgrown").forGetter(properties -> properties.overgrown), (App)Codec.BOOL.fieldOf("vines").forGetter(properties -> properties.vines), (App)Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(properties -> properties.replaceWithBlackstone)).apply((Applicative)instance, Properties::new));
        public boolean cold;
        public float mossiness;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public Properties() {
        }

        public Properties(boolean bl, float f, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
            this.cold = bl;
            this.mossiness = f;
            this.airPocket = bl2;
            this.overgrown = bl3;
            this.vines = bl4;
            this.replaceWithBlackstone = bl5;
        }
    }
}

