/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.List;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ModelBlockRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors blockColors;
    private static final int CACHE_SIZE = 100;
    static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public ModelBlockRenderer(BlockColors blockColors) {
        this.blockColors = blockColors;
    }

    public void tesselateBlock(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int n) {
        if (list.isEmpty()) {
            return;
        }
        boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && list.getFirst().useAmbientOcclusion();
        poseStack.translate(blockState.getOffset(blockPos));
        try {
            if (bl2) {
                this.tesselateWithAO(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, n);
            } else {
                this.tesselateWithoutAO(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, n);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block model");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            crashReportCategory.setDetail("Using AO", bl2);
            throw new ReportedException(crashReport);
        }
    }

    private static boolean shouldRenderFace(BlockAndTintGetter blockAndTintGetter, BlockState blockState, boolean bl, Direction direction, BlockPos blockPos) {
        if (!bl) {
            return true;
        }
        BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos);
        return Block.shouldRenderFace(blockState, blockState2, direction);
    }

    public void tesselateWithAO(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int n) {
        AmbientOcclusionRenderStorage ambientOcclusionRenderStorage = new AmbientOcclusionRenderStorage();
        int n2 = 0;
        int n3 = 0;
        for (BlockModelPart blockModelPart : list) {
            for (Direction direction : DIRECTIONS) {
                List<BakedQuad> list2;
                boolean bl2;
                int n4 = 1 << direction.ordinal();
                boolean bl3 = (n2 & n4) == 1;
                boolean bl4 = bl2 = (n3 & n4) == 1;
                if (bl3 && !bl2 || (list2 = blockModelPart.getQuads(direction)).isEmpty()) continue;
                if (!bl3) {
                    bl2 = ModelBlockRenderer.shouldRenderFace(blockAndTintGetter, blockState, bl, direction, ambientOcclusionRenderStorage.scratchPos.setWithOffset((Vec3i)blockPos, direction));
                    n2 |= n4;
                    if (bl2) {
                        n3 |= n4;
                    }
                }
                if (!bl2) continue;
                this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list2, ambientOcclusionRenderStorage, n);
            }
            List<BakedQuad> list3 = blockModelPart.getQuads(null);
            if (list3.isEmpty()) continue;
            this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list3, ambientOcclusionRenderStorage, n);
        }
    }

    public void tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int n) {
        CommonRenderStorage commonRenderStorage = new CommonRenderStorage();
        int n2 = 0;
        int n3 = 0;
        for (BlockModelPart blockModelPart : list) {
            for (Direction direction : DIRECTIONS) {
                List<BakedQuad> list2;
                boolean bl2;
                int n4 = 1 << direction.ordinal();
                boolean bl3 = (n2 & n4) == 1;
                boolean bl4 = bl2 = (n3 & n4) == 1;
                if (bl3 && !bl2 || (list2 = blockModelPart.getQuads(direction)).isEmpty()) continue;
                BlockPos.MutableBlockPos mutableBlockPos = commonRenderStorage.scratchPos.setWithOffset((Vec3i)blockPos, direction);
                if (!bl3) {
                    bl2 = ModelBlockRenderer.shouldRenderFace(blockAndTintGetter, blockState, bl, direction, mutableBlockPos);
                    n2 |= n4;
                    if (bl2) {
                        n3 |= n4;
                    }
                }
                if (!bl2) continue;
                int n5 = commonRenderStorage.cache.getLightColor(blockState, blockAndTintGetter, mutableBlockPos);
                this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, n5, n, false, poseStack, vertexConsumer, list2, commonRenderStorage);
            }
            List<BakedQuad> list3 = blockModelPart.getQuads(null);
            if (list3.isEmpty()) continue;
            this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, -1, n, true, poseStack, vertexConsumer, list3, commonRenderStorage);
        }
    }

    private void renderModelFaceAO(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, AmbientOcclusionRenderStorage ambientOcclusionRenderStorage, int n) {
        for (BakedQuad bakedQuad : list) {
            ModelBlockRenderer.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.vertices(), bakedQuad.direction(), ambientOcclusionRenderStorage);
            ambientOcclusionRenderStorage.calculate(blockAndTintGetter, blockState, blockPos, bakedQuad.direction(), bakedQuad.shade());
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, ambientOcclusionRenderStorage, n);
        }
    }

    private void putQuadData(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, VertexConsumer vertexConsumer, PoseStack.Pose pose, BakedQuad bakedQuad, CommonRenderStorage commonRenderStorage, int n) {
        float f;
        float f2;
        float f3;
        int n2 = bakedQuad.tintIndex();
        if (n2 != -1) {
            int n3;
            if (commonRenderStorage.tintCacheIndex == n2) {
                n3 = commonRenderStorage.tintCacheValue;
            } else {
                n3 = this.blockColors.getColor(blockState, blockAndTintGetter, blockPos, n2);
                commonRenderStorage.tintCacheIndex = n2;
                commonRenderStorage.tintCacheValue = n3;
            }
            f3 = ARGB.redFloat(n3);
            f2 = ARGB.greenFloat(n3);
            f = ARGB.blueFloat(n3);
        } else {
            f3 = 1.0f;
            f2 = 1.0f;
            f = 1.0f;
        }
        vertexConsumer.putBulkData(pose, bakedQuad, commonRenderStorage.brightness, f3, f2, f, 1.0f, commonRenderStorage.lightmap, n, true);
    }

    private static void calculateShape(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int[] nArray, Direction direction, CommonRenderStorage commonRenderStorage) {
        float f;
        float f2 = 32.0f;
        float f3 = 32.0f;
        float f4 = 32.0f;
        float f5 = -32.0f;
        float f6 = -32.0f;
        float f7 = -32.0f;
        for (int i = 0; i < 4; ++i) {
            f = Float.intBitsToFloat(nArray[i * 8]);
            float f8 = Float.intBitsToFloat(nArray[i * 8 + 1]);
            float f9 = Float.intBitsToFloat(nArray[i * 8 + 2]);
            f2 = Math.min(f2, f);
            f3 = Math.min(f3, f8);
            f4 = Math.min(f4, f9);
            f5 = Math.max(f5, f);
            f6 = Math.max(f6, f8);
            f7 = Math.max(f7, f9);
        }
        if (commonRenderStorage instanceof AmbientOcclusionRenderStorage) {
            AmbientOcclusionRenderStorage ambientOcclusionRenderStorage = (AmbientOcclusionRenderStorage)commonRenderStorage;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.WEST.index] = f2;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.EAST.index] = f5;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.DOWN.index] = f3;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.UP.index] = f6;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.NORTH.index] = f4;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.SOUTH.index] = f7;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_WEST.index] = 1.0f - f2;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_EAST.index] = 1.0f - f5;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_DOWN.index] = 1.0f - f3;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_UP.index] = 1.0f - f6;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_NORTH.index] = 1.0f - f4;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_SOUTH.index] = 1.0f - f7;
        }
        float f10 = 1.0E-4f;
        f = 0.9999f;
        commonRenderStorage.facePartial = switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN, Direction.UP -> {
                if (f2 >= 1.0E-4f || f4 >= 1.0E-4f || f5 <= 0.9999f || f7 <= 0.9999f) {
                    yield true;
                }
                yield false;
            }
            case Direction.NORTH, Direction.SOUTH -> {
                if (f2 >= 1.0E-4f || f3 >= 1.0E-4f || f5 <= 0.9999f || f6 <= 0.9999f) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST, Direction.EAST -> f3 >= 1.0E-4f || f4 >= 1.0E-4f || f6 <= 0.9999f || f7 <= 0.9999f;
        };
        commonRenderStorage.faceCubic = switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> {
                if (f3 == f6 && (f3 < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (f3 == f6 && (f6 > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.NORTH -> {
                if (f4 == f7 && (f4 < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (f4 == f7 && (f7 > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (f2 == f5 && (f2 < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> f2 == f5 && (f5 > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos));
        };
    }

    private void renderModelFaceFlat(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int n, int n2, boolean bl, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, CommonRenderStorage commonRenderStorage) {
        for (BakedQuad bakedQuad : list) {
            float f;
            if (bl) {
                ModelBlockRenderer.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.vertices(), bakedQuad.direction(), commonRenderStorage);
                BlockPos blockPos2 = commonRenderStorage.faceCubic ? commonRenderStorage.scratchPos.setWithOffset((Vec3i)blockPos, bakedQuad.direction()) : blockPos;
                n = commonRenderStorage.cache.getLightColor(blockState, blockAndTintGetter, blockPos2);
            }
            commonRenderStorage.brightness[0] = f = blockAndTintGetter.getShade(bakedQuad.direction(), bakedQuad.shade());
            commonRenderStorage.brightness[1] = f;
            commonRenderStorage.brightness[2] = f;
            commonRenderStorage.brightness[3] = f;
            commonRenderStorage.lightmap[0] = n;
            commonRenderStorage.lightmap[1] = n;
            commonRenderStorage.lightmap[2] = n;
            commonRenderStorage.lightmap[3] = n;
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, commonRenderStorage, n2);
        }
    }

    public static void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, BlockStateModel blockStateModel, float f, float f2, float f3, int n, int n2) {
        for (BlockModelPart blockModelPart : blockStateModel.collectParts(RandomSource.create(42L))) {
            for (Direction direction : DIRECTIONS) {
                ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, f2, f3, blockModelPart.getQuads(direction), n, n2);
            }
            ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, f2, f3, blockModelPart.getQuads(null), n, n2);
        }
    }

    private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float f2, float f3, List<BakedQuad> list, int n, int n2) {
        for (BakedQuad bakedQuad : list) {
            float f4;
            float f5;
            float f6;
            if (bakedQuad.isTinted()) {
                f6 = Mth.clamp(f, 0.0f, 1.0f);
                f5 = Mth.clamp(f2, 0.0f, 1.0f);
                f4 = Mth.clamp(f3, 0.0f, 1.0f);
            } else {
                f6 = 1.0f;
                f5 = 1.0f;
                f4 = 1.0f;
            }
            vertexConsumer.putBulkData(pose, bakedQuad, f6, f5, f4, 1.0f, n, n2);
        }
    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    static class AmbientOcclusionRenderStorage
    extends CommonRenderStorage {
        final float[] faceShape = new float[SizeInfo.COUNT];

        public void calculate(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, Direction direction, boolean bl) {
            float f;
            int n;
            float f2;
            int n2;
            float f3;
            int n3;
            float f4;
            int n4;
            float f5;
            BlockState blockState2;
            boolean bl2;
            BlockPos blockPos2 = this.faceCubic ? blockPos.relative(direction) : blockPos;
            AdjacencyInfo adjacencyInfo = AdjacencyInfo.fromFacing(direction);
            BlockPos.MutableBlockPos mutableBlockPos = this.scratchPos;
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]);
            BlockState blockState3 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n5 = this.cache.getLightColor(blockState3, blockAndTintGetter, mutableBlockPos);
            float f6 = this.cache.getShadeBrightness(blockState3, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]);
            BlockState blockState4 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n6 = this.cache.getLightColor(blockState4, blockAndTintGetter, mutableBlockPos);
            float f7 = this.cache.getShadeBrightness(blockState4, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]);
            BlockState blockState5 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n7 = this.cache.getLightColor(blockState5, blockAndTintGetter, mutableBlockPos);
            float f8 = this.cache.getShadeBrightness(blockState5, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]);
            BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int n8 = this.cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
            float f9 = this.cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
            BlockState blockState7 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(direction));
            boolean bl3 = !blockState7.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState7.getLightBlock() == 0;
            BlockState blockState8 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(direction));
            boolean bl4 = !blockState8.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState8.getLightBlock() == 0;
            BlockState blockState9 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]).move(direction));
            boolean bl5 = !blockState9.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState9.getLightBlock() == 0;
            BlockState blockState10 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]).move(direction));
            boolean bl6 = bl2 = !blockState10.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState10.getLightBlock() == 0;
            if (bl5 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f5 = this.cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n4 = this.cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f5 = f6;
                n4 = n5;
            }
            if (bl2 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f4 = this.cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n3 = this.cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f4 = f6;
                n3 = n5;
            }
            if (bl5 || bl4) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f3 = this.cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n2 = this.cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f3 = f6;
                n2 = n5;
            }
            if (bl2 || bl4) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
                blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
                f2 = this.cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
                n = this.cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            } else {
                f2 = f6;
                n = n5;
            }
            int n9 = this.cache.getLightColor(blockState, blockAndTintGetter, blockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            BlockState blockState11 = blockAndTintGetter.getBlockState(mutableBlockPos);
            if (this.faceCubic || !blockState11.isSolidRender()) {
                n9 = this.cache.getLightColor(blockState11, blockAndTintGetter, mutableBlockPos);
            }
            float f10 = this.faceCubic ? this.cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos2), blockAndTintGetter, blockPos2) : this.cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos), blockAndTintGetter, blockPos);
            AmbientVertexRemap ambientVertexRemap = AmbientVertexRemap.fromFacing(direction);
            if (!this.facePartial || !adjacencyInfo.doNonCubicWeight) {
                f = (f9 + f6 + f4 + f10) * 0.25f;
                var42_43 = (f8 + f6 + f5 + f10) * 0.25f;
                var43_45 = (f8 + f7 + f3 + f10) * 0.25f;
                var44_46 = (f9 + f7 + f2 + f10) * 0.25f;
                this.lightmap[ambientVertexRemap.vert0] = AmbientOcclusionRenderStorage.blend(n8, n5, n3, n9);
                this.lightmap[ambientVertexRemap.vert1] = AmbientOcclusionRenderStorage.blend(n7, n5, n4, n9);
                this.lightmap[ambientVertexRemap.vert2] = AmbientOcclusionRenderStorage.blend(n7, n6, n2, n9);
                this.lightmap[ambientVertexRemap.vert3] = AmbientOcclusionRenderStorage.blend(n8, n6, n, n9);
                this.brightness[ambientVertexRemap.vert0] = f;
                this.brightness[ambientVertexRemap.vert1] = var42_43;
                this.brightness[ambientVertexRemap.vert2] = var43_45;
                this.brightness[ambientVertexRemap.vert3] = var44_46;
            } else {
                f = (f9 + f6 + f4 + f10) * 0.25f;
                var42_43 = (f8 + f6 + f5 + f10) * 0.25f;
                var43_45 = (f8 + f7 + f3 + f10) * 0.25f;
                var44_46 = (f9 + f7 + f2 + f10) * 0.25f;
                float f11 = this.faceShape[adjacencyInfo.vert0Weights[0].index] * this.faceShape[adjacencyInfo.vert0Weights[1].index];
                float f12 = this.faceShape[adjacencyInfo.vert0Weights[2].index] * this.faceShape[adjacencyInfo.vert0Weights[3].index];
                float f13 = this.faceShape[adjacencyInfo.vert0Weights[4].index] * this.faceShape[adjacencyInfo.vert0Weights[5].index];
                float f14 = this.faceShape[adjacencyInfo.vert0Weights[6].index] * this.faceShape[adjacencyInfo.vert0Weights[7].index];
                float f15 = this.faceShape[adjacencyInfo.vert1Weights[0].index] * this.faceShape[adjacencyInfo.vert1Weights[1].index];
                float f16 = this.faceShape[adjacencyInfo.vert1Weights[2].index] * this.faceShape[adjacencyInfo.vert1Weights[3].index];
                float f17 = this.faceShape[adjacencyInfo.vert1Weights[4].index] * this.faceShape[adjacencyInfo.vert1Weights[5].index];
                float f18 = this.faceShape[adjacencyInfo.vert1Weights[6].index] * this.faceShape[adjacencyInfo.vert1Weights[7].index];
                float f19 = this.faceShape[adjacencyInfo.vert2Weights[0].index] * this.faceShape[adjacencyInfo.vert2Weights[1].index];
                float f20 = this.faceShape[adjacencyInfo.vert2Weights[2].index] * this.faceShape[adjacencyInfo.vert2Weights[3].index];
                float f21 = this.faceShape[adjacencyInfo.vert2Weights[4].index] * this.faceShape[adjacencyInfo.vert2Weights[5].index];
                float f22 = this.faceShape[adjacencyInfo.vert2Weights[6].index] * this.faceShape[adjacencyInfo.vert2Weights[7].index];
                float f23 = this.faceShape[adjacencyInfo.vert3Weights[0].index] * this.faceShape[adjacencyInfo.vert3Weights[1].index];
                float f24 = this.faceShape[adjacencyInfo.vert3Weights[2].index] * this.faceShape[adjacencyInfo.vert3Weights[3].index];
                float f25 = this.faceShape[adjacencyInfo.vert3Weights[4].index] * this.faceShape[adjacencyInfo.vert3Weights[5].index];
                float f26 = this.faceShape[adjacencyInfo.vert3Weights[6].index] * this.faceShape[adjacencyInfo.vert3Weights[7].index];
                this.brightness[ambientVertexRemap.vert0] = Math.clamp(f * f11 + var42_43 * f12 + var43_45 * f13 + var44_46 * f14, 0.0f, 1.0f);
                this.brightness[ambientVertexRemap.vert1] = Math.clamp(f * f15 + var42_43 * f16 + var43_45 * f17 + var44_46 * f18, 0.0f, 1.0f);
                this.brightness[ambientVertexRemap.vert2] = Math.clamp(f * f19 + var42_43 * f20 + var43_45 * f21 + var44_46 * f22, 0.0f, 1.0f);
                this.brightness[ambientVertexRemap.vert3] = Math.clamp(f * f23 + var42_43 * f24 + var43_45 * f25 + var44_46 * f26, 0.0f, 1.0f);
                int n10 = AmbientOcclusionRenderStorage.blend(n8, n5, n3, n9);
                int n11 = AmbientOcclusionRenderStorage.blend(n7, n5, n4, n9);
                int n12 = AmbientOcclusionRenderStorage.blend(n7, n6, n2, n9);
                int n13 = AmbientOcclusionRenderStorage.blend(n8, n6, n, n9);
                this.lightmap[ambientVertexRemap.vert0] = AmbientOcclusionRenderStorage.blend(n10, n11, n12, n13, f11, f12, f13, f14);
                this.lightmap[ambientVertexRemap.vert1] = AmbientOcclusionRenderStorage.blend(n10, n11, n12, n13, f15, f16, f17, f18);
                this.lightmap[ambientVertexRemap.vert2] = AmbientOcclusionRenderStorage.blend(n10, n11, n12, n13, f19, f20, f21, f22);
                this.lightmap[ambientVertexRemap.vert3] = AmbientOcclusionRenderStorage.blend(n10, n11, n12, n13, f23, f24, f25, f26);
            }
            f = blockAndTintGetter.getShade(direction, bl);
            int n14 = 0;
            while (n14 < this.brightness.length) {
                int n15 = n14++;
                this.brightness[n15] = this.brightness[n15] * f;
            }
        }

        private static int blend(int n, int n2, int n3, int n4) {
            if (n == 0) {
                n = n4;
            }
            if (n2 == 0) {
                n2 = n4;
            }
            if (n3 == 0) {
                n3 = n4;
            }
            return n + n2 + n3 + n4 >> 2 & 0xFF00FF;
        }

        private static int blend(int n, int n2, int n3, int n4, float f, float f2, float f3, float f4) {
            int n5 = (int)((float)(n >> 16 & 0xFF) * f + (float)(n2 >> 16 & 0xFF) * f2 + (float)(n3 >> 16 & 0xFF) * f3 + (float)(n4 >> 16 & 0xFF) * f4) & 0xFF;
            int n6 = (int)((float)(n & 0xFF) * f + (float)(n2 & 0xFF) * f2 + (float)(n3 & 0xFF) * f3 + (float)(n4 & 0xFF) * f4) & 0xFF;
            return n5 << 16 | n6;
        }
    }

    static class CommonRenderStorage {
        public final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
        public boolean faceCubic;
        public boolean facePartial;
        public final float[] brightness = new float[4];
        public final int[] lightmap = new int[4];
        public int tintCacheIndex = -1;
        public int tintCacheValue;
        public final Cache cache = CACHE.get();

        CommonRenderStorage() {
        }
    }

    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                protected void rehash(int n) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                protected void rehash(int n) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });
        private final LevelRenderer.BrightnessGetter cachedBrightnessGetter = (blockAndTintGetter, blockPos) -> {
            long l = blockPos.asLong();
            int n = this.colorCache.get(l);
            if (n != Integer.MAX_VALUE) {
                return n;
            }
            int n2 = LevelRenderer.BrightnessGetter.DEFAULT.packedBrightness(blockAndTintGetter, blockPos);
            if (this.colorCache.size() == 100) {
                this.colorCache.removeFirstInt();
            }
            this.colorCache.put(l, n2);
            return n2;
        };

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            return LevelRenderer.getLightColor(this.enabled ? this.cachedBrightnessGetter : LevelRenderer.BrightnessGetter.DEFAULT, blockAndTintGetter, blockState, blockPos);
        }

        public float getShadeBrightness(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            float f;
            long l = blockPos.asLong();
            if (this.enabled && !Float.isNaN(f = this.brightnessCache.get(l))) {
                return f;
            }
            f = blockState.getShadeBrightness(blockAndTintGetter, blockPos);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }
                this.brightnessCache.put(l, f);
            }
            return f;
        }
    }

    protected static enum SizeInfo {
        DOWN(0),
        UP(1),
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5),
        FLIP_DOWN(6),
        FLIP_UP(7),
        FLIP_NORTH(8),
        FLIP_SOUTH(9),
        FLIP_WEST(10),
        FLIP_EAST(11);

        public static final int COUNT;
        final int index;

        private SizeInfo(int n2) {
            this.index = n2;
        }

        static {
            COUNT = SizeInfo.values().length;
        }
    }

    protected static enum AdjacencyInfo {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new SizeInfo[]{SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH});

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final SizeInfo[] vert0Weights;
        final SizeInfo[] vert1Weights;
        final SizeInfo[] vert2Weights;
        final SizeInfo[] vert3Weights;
        private static final AdjacencyInfo[] BY_FACING;

        private AdjacencyInfo(Direction[] directionArray, float f, boolean bl, SizeInfo[] sizeInfoArray, SizeInfo[] sizeInfoArray2, SizeInfo[] sizeInfoArray3, SizeInfo[] sizeInfoArray4) {
            this.corners = directionArray;
            this.doNonCubicWeight = bl;
            this.vert0Weights = sizeInfoArray;
            this.vert1Weights = sizeInfoArray2;
            this.vert2Weights = sizeInfoArray3;
            this.vert3Weights = sizeInfoArray4;
        }

        public static AdjacencyInfo fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AdjacencyInfo[6], adjacencyInfoArray -> {
                adjacencyInfoArray[Direction.DOWN.get3DDataValue()] = DOWN;
                adjacencyInfoArray[Direction.UP.get3DDataValue()] = UP;
                adjacencyInfoArray[Direction.NORTH.get3DDataValue()] = NORTH;
                adjacencyInfoArray[Direction.SOUTH.get3DDataValue()] = SOUTH;
                adjacencyInfoArray[Direction.WEST.get3DDataValue()] = WEST;
                adjacencyInfoArray[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }

    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
        private static final AmbientVertexRemap[] BY_FACING;

        private AmbientVertexRemap(int n2, int n3, int n4, int n5) {
            this.vert0 = n2;
            this.vert1 = n3;
            this.vert2 = n4;
            this.vert3 = n5;
        }

        public static AmbientVertexRemap fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AmbientVertexRemap[6], ambientVertexRemapArray -> {
                ambientVertexRemapArray[Direction.DOWN.get3DDataValue()] = DOWN;
                ambientVertexRemapArray[Direction.UP.get3DDataValue()] = UP;
                ambientVertexRemapArray[Direction.NORTH.get3DDataValue()] = NORTH;
                ambientVertexRemapArray[Direction.SOUTH.get3DDataValue()] = SOUTH;
                ambientVertexRemapArray[Direction.WEST.get3DDataValue()] = WEST;
                ambientVertexRemapArray[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }
}

