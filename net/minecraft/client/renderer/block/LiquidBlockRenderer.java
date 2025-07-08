/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlockRenderer {
    private static final float MAX_FLUID_HEIGHT = 0.8888889f;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites() {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).particleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).particleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(FluidState fluidState, FluidState fluidState2) {
        return fluidState2.getType().isSame(fluidState.getType());
    }

    private static boolean isFaceOccludedByState(Direction direction, float f, BlockState blockState) {
        VoxelShape voxelShape = blockState.getFaceOcclusionShape(direction.getOpposite());
        if (voxelShape == Shapes.empty()) {
            return false;
        }
        if (voxelShape == Shapes.block()) {
            boolean bl = f == 1.0f;
            return direction != Direction.UP || bl;
        }
        VoxelShape voxelShape2 = Shapes.box(0.0, 0.0, 0.0, 1.0, f, 1.0);
        return Shapes.blockOccludes(voxelShape2, voxelShape, direction);
    }

    private static boolean isFaceOccludedByNeighbor(Direction direction, float f, BlockState blockState) {
        return LiquidBlockRenderer.isFaceOccludedByState(direction, f, blockState);
    }

    private static boolean isFaceOccludedBySelf(BlockState blockState, Direction direction) {
        return LiquidBlockRenderer.isFaceOccludedByState(direction.getOpposite(), 1.0f, blockState);
    }

    public static boolean shouldRenderFace(FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
        return !LiquidBlockRenderer.isFaceOccludedBySelf(blockState, direction) && !LiquidBlockRenderer.isNeighborSameFluid(fluidState, fluidState2);
    }

    public void tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float f8;
        float f9;
        float f10;
        float f11;
        float f12;
        float f13;
        float f14;
        float f15;
        float f16;
        float f17;
        float f18;
        float f19;
        boolean bl = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] textureAtlasSpriteArray = bl ? this.lavaIcons : this.waterIcons;
        int n = bl ? 0xFFFFFF : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        float f20 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f21 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f22 = (float)(n & 0xFF) / 255.0f;
        BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.DOWN));
        FluidState fluidState2 = blockState2.getFluidState();
        BlockState blockState3 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.UP));
        FluidState fluidState3 = blockState3.getFluidState();
        BlockState blockState4 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.NORTH));
        FluidState fluidState4 = blockState4.getFluidState();
        BlockState blockState5 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.SOUTH));
        FluidState fluidState5 = blockState5.getFluidState();
        BlockState blockState6 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.WEST));
        FluidState fluidState6 = blockState6.getFluidState();
        BlockState blockState7 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.EAST));
        FluidState fluidState7 = blockState7.getFluidState();
        boolean bl2 = !LiquidBlockRenderer.isNeighborSameFluid(fluidState, fluidState3);
        boolean bl3 = LiquidBlockRenderer.shouldRenderFace(fluidState, blockState, Direction.DOWN, fluidState2) && !LiquidBlockRenderer.isFaceOccludedByNeighbor(Direction.DOWN, 0.8888889f, blockState2);
        boolean bl4 = LiquidBlockRenderer.shouldRenderFace(fluidState, blockState, Direction.NORTH, fluidState4);
        boolean bl5 = LiquidBlockRenderer.shouldRenderFace(fluidState, blockState, Direction.SOUTH, fluidState5);
        boolean bl6 = LiquidBlockRenderer.shouldRenderFace(fluidState, blockState, Direction.WEST, fluidState6);
        boolean bl7 = LiquidBlockRenderer.shouldRenderFace(fluidState, blockState, Direction.EAST, fluidState7);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return;
        }
        float f23 = blockAndTintGetter.getShade(Direction.DOWN, true);
        float f24 = blockAndTintGetter.getShade(Direction.UP, true);
        float f25 = blockAndTintGetter.getShade(Direction.NORTH, true);
        float f26 = blockAndTintGetter.getShade(Direction.WEST, true);
        Fluid fluid = fluidState.getType();
        float f27 = this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, fluidState);
        if (f27 >= 1.0f) {
            f19 = 1.0f;
            f18 = 1.0f;
            f17 = 1.0f;
            f16 = 1.0f;
        } else {
            f15 = this.getHeight(blockAndTintGetter, fluid, blockPos.north(), blockState4, fluidState4);
            f14 = this.getHeight(blockAndTintGetter, fluid, blockPos.south(), blockState5, fluidState5);
            f13 = this.getHeight(blockAndTintGetter, fluid, blockPos.east(), blockState7, fluidState7);
            f12 = this.getHeight(blockAndTintGetter, fluid, blockPos.west(), blockState6, fluidState6);
            f19 = this.calculateAverageHeight(blockAndTintGetter, fluid, f27, f15, f13, blockPos.relative(Direction.NORTH).relative(Direction.EAST));
            f18 = this.calculateAverageHeight(blockAndTintGetter, fluid, f27, f15, f12, blockPos.relative(Direction.NORTH).relative(Direction.WEST));
            f17 = this.calculateAverageHeight(blockAndTintGetter, fluid, f27, f14, f13, blockPos.relative(Direction.SOUTH).relative(Direction.EAST));
            f16 = this.calculateAverageHeight(blockAndTintGetter, fluid, f27, f14, f12, blockPos.relative(Direction.SOUTH).relative(Direction.WEST));
        }
        f15 = blockPos.getX() & 0xF;
        f14 = blockPos.getY() & 0xF;
        f13 = blockPos.getZ() & 0xF;
        f12 = 0.001f;
        float f28 = f11 = bl3 ? 0.001f : 0.0f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccludedByNeighbor(Direction.UP, Math.min(Math.min(f18, f16), Math.min(f17, f19)), blockState3)) {
            float f29;
            float f30;
            float f31;
            f18 -= 0.001f;
            f16 -= 0.001f;
            f17 -= 0.001f;
            f19 -= 0.001f;
            Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
            if (vec3.x == 0.0 && vec3.z == 0.0) {
                var54_47 = textureAtlasSpriteArray[0];
                f10 = var54_47.getU(0.0f);
                f31 = var54_47.getV(0.0f);
                f9 = f10;
                f8 = var54_47.getV(1.0f);
                f7 = var54_47.getU(1.0f);
                f6 = f8;
                f5 = f7;
                f4 = f31;
            } else {
                var54_47 = textureAtlasSpriteArray[1];
                f30 = (float)Mth.atan2(vec3.z, vec3.x) - 1.5707964f;
                f29 = Mth.sin(f30) * 0.25f;
                float f32 = Mth.cos(f30) * 0.25f;
                f3 = 0.5f;
                f10 = var54_47.getU(0.5f + (-f32 - f29));
                f31 = var54_47.getV(0.5f + (-f32 + f29));
                f9 = var54_47.getU(0.5f + (-f32 + f29));
                f8 = var54_47.getV(0.5f + (f32 + f29));
                f7 = var54_47.getU(0.5f + (f32 + f29));
                f6 = var54_47.getV(0.5f + (f32 - f29));
                f5 = var54_47.getU(0.5f + (f32 - f29));
                f4 = var54_47.getV(0.5f + (-f32 - f29));
            }
            float f33 = (f10 + f9 + f7 + f5) / 4.0f;
            f30 = (f31 + f8 + f6 + f4) / 4.0f;
            f29 = textureAtlasSpriteArray[0].uvShrinkRatio();
            f10 = Mth.lerp(f29, f10, f33);
            f9 = Mth.lerp(f29, f9, f33);
            f7 = Mth.lerp(f29, f7, f33);
            f5 = Mth.lerp(f29, f5, f33);
            f31 = Mth.lerp(f29, f31, f30);
            f8 = Mth.lerp(f29, f8, f30);
            f6 = Mth.lerp(f29, f6, f30);
            f4 = Mth.lerp(f29, f4, f30);
            int n2 = this.getLightColor(blockAndTintGetter, blockPos);
            f3 = f24 * f20;
            f2 = f24 * f21;
            f = f24 * f22;
            this.vertex(vertexConsumer, f15 + 0.0f, f14 + f18, f13 + 0.0f, f3, f2, f, f10, f31, n2);
            this.vertex(vertexConsumer, f15 + 0.0f, f14 + f16, f13 + 1.0f, f3, f2, f, f9, f8, n2);
            this.vertex(vertexConsumer, f15 + 1.0f, f14 + f17, f13 + 1.0f, f3, f2, f, f7, f6, n2);
            this.vertex(vertexConsumer, f15 + 1.0f, f14 + f19, f13 + 0.0f, f3, f2, f, f5, f4, n2);
            if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
                this.vertex(vertexConsumer, f15 + 0.0f, f14 + f18, f13 + 0.0f, f3, f2, f, f10, f31, n2);
                this.vertex(vertexConsumer, f15 + 1.0f, f14 + f19, f13 + 0.0f, f3, f2, f, f5, f4, n2);
                this.vertex(vertexConsumer, f15 + 1.0f, f14 + f17, f13 + 1.0f, f3, f2, f, f7, f6, n2);
                this.vertex(vertexConsumer, f15 + 0.0f, f14 + f16, f13 + 1.0f, f3, f2, f, f9, f8, n2);
            }
        }
        if (bl3) {
            f10 = textureAtlasSpriteArray[0].getU0();
            f9 = textureAtlasSpriteArray[0].getU1();
            f7 = textureAtlasSpriteArray[0].getV0();
            f5 = textureAtlasSpriteArray[0].getV1();
            int n3 = this.getLightColor(blockAndTintGetter, blockPos.below());
            f8 = f23 * f20;
            f6 = f23 * f21;
            f4 = f23 * f22;
            this.vertex(vertexConsumer, f15, f14 + f11, f13 + 1.0f, f8, f6, f4, f10, f5, n3);
            this.vertex(vertexConsumer, f15, f14 + f11, f13, f8, f6, f4, f10, f7, n3);
            this.vertex(vertexConsumer, f15 + 1.0f, f14 + f11, f13, f8, f6, f4, f9, f7, n3);
            this.vertex(vertexConsumer, f15 + 1.0f, f14 + f11, f13 + 1.0f, f8, f6, f4, f9, f5, n3);
        }
        int n4 = this.getLightColor(blockAndTintGetter, blockPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Block block;
            float f34;
            float f35;
            if (!(switch (direction) {
                case Direction.NORTH -> {
                    f5 = f18;
                    f35 = f19;
                    f8 = f15;
                    f4 = f15 + 1.0f;
                    f6 = f13 + 0.001f;
                    f34 = f13 + 0.001f;
                    yield bl4;
                }
                case Direction.SOUTH -> {
                    f5 = f17;
                    f35 = f16;
                    f8 = f15 + 1.0f;
                    f4 = f15;
                    f6 = f13 + 1.0f - 0.001f;
                    f34 = f13 + 1.0f - 0.001f;
                    yield bl5;
                }
                case Direction.WEST -> {
                    f5 = f16;
                    f35 = f18;
                    f8 = f15 + 0.001f;
                    f4 = f15 + 0.001f;
                    f6 = f13 + 1.0f;
                    f34 = f13;
                    yield bl6;
                }
                default -> {
                    f5 = f19;
                    f35 = f17;
                    f8 = f15 + 1.0f - 0.001f;
                    f4 = f15 + 1.0f - 0.001f;
                    f6 = f13;
                    f34 = f13 + 1.0f;
                    yield bl7;
                }
            }) || LiquidBlockRenderer.isFaceOccludedByNeighbor(direction, Math.max(f5, f35), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) continue;
            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite textureAtlasSprite = textureAtlasSpriteArray[1];
            if (!bl && ((block = blockAndTintGetter.getBlockState(blockPos2).getBlock()) instanceof HalfTransparentBlock || block instanceof LeavesBlock)) {
                textureAtlasSprite = this.waterOverlay;
            }
            float f36 = textureAtlasSprite.getU(0.0f);
            f3 = textureAtlasSprite.getU(0.5f);
            f2 = textureAtlasSprite.getV((1.0f - f5) * 0.5f);
            f = textureAtlasSprite.getV((1.0f - f35) * 0.5f);
            float f37 = textureAtlasSprite.getV(0.5f);
            float f38 = direction.getAxis() == Direction.Axis.Z ? f25 : f26;
            float f39 = f24 * f38 * f20;
            float f40 = f24 * f38 * f21;
            float f41 = f24 * f38 * f22;
            this.vertex(vertexConsumer, f8, f14 + f5, f6, f39, f40, f41, f36, f2, n4);
            this.vertex(vertexConsumer, f4, f14 + f35, f34, f39, f40, f41, f3, f, n4);
            this.vertex(vertexConsumer, f4, f14 + f11, f34, f39, f40, f41, f3, f37, n4);
            this.vertex(vertexConsumer, f8, f14 + f11, f6, f39, f40, f41, f36, f37, n4);
            if (textureAtlasSprite == this.waterOverlay) continue;
            this.vertex(vertexConsumer, f8, f14 + f11, f6, f39, f40, f41, f36, f37, n4);
            this.vertex(vertexConsumer, f4, f14 + f11, f34, f39, f40, f41, f3, f37, n4);
            this.vertex(vertexConsumer, f4, f14 + f35, f34, f39, f40, f41, f3, f, n4);
            this.vertex(vertexConsumer, f8, f14 + f5, f6, f39, f40, f41, f36, f2, n4);
        }
    }

    private float calculateAverageHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, float f, float f2, float f3, BlockPos blockPos) {
        if (f3 >= 1.0f || f2 >= 1.0f) {
            return 1.0f;
        }
        float[] fArray = new float[2];
        if (f3 > 0.0f || f2 > 0.0f) {
            float f4 = this.getHeight(blockAndTintGetter, fluid, blockPos);
            if (f4 >= 1.0f) {
                return 1.0f;
            }
            this.addWeightedHeight(fArray, f4);
        }
        this.addWeightedHeight(fArray, f);
        this.addWeightedHeight(fArray, f3);
        this.addWeightedHeight(fArray, f2);
        return fArray[0] / fArray[1];
    }

    private void addWeightedHeight(float[] fArray, float f) {
        if (f >= 0.8f) {
            fArray[0] = fArray[0] + f * 10.0f;
            fArray[1] = fArray[1] + 10.0f;
        } else if (f >= 0.0f) {
            fArray[0] = fArray[0] + f;
            fArray[1] = fArray[1] + 1.0f;
        }
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos) {
        BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
        return this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, blockState.getFluidState());
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (fluid.isSame(fluidState.getType())) {
            BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.above());
            if (fluid.isSame(blockState2.getFluidState().getType())) {
                return 1.0f;
            }
            return fluidState.getOwnHeight();
        }
        if (!blockState.isSolid()) {
            return 0.0f;
        }
        return -1.0f;
    }

    private void vertex(VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n) {
        vertexConsumer.addVertex(f, f2, f3).setColor(f4, f5, f6, 1.0f).setUv(f7, f8).setLight(n).setNormal(0.0f, 1.0f, 0.0f);
    }

    private int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int n = LevelRenderer.getLightColor(blockAndTintGetter, blockPos);
        int n2 = LevelRenderer.getLightColor(blockAndTintGetter, blockPos.above());
        int n3 = n & 0xFF;
        int n4 = n2 & 0xFF;
        int n5 = n >> 16 & 0xFF;
        int n6 = n2 >> 16 & 0xFF;
        return (n3 > n4 ? n3 : n4) | (n5 > n6 ? n5 : n6) << 16;
    }
}

