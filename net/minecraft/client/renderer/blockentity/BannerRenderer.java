/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Set;
import net.minecraft.client.model.BannerFlagModel;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class BannerRenderer
implements BlockEntityRenderer<BannerBlockEntity> {
    private static final int MAX_PATTERNS = 16;
    private static final float SIZE = 0.6666667f;
    private final BannerModel standingModel;
    private final BannerModel wallModel;
    private final BannerFlagModel standingFlagModel;
    private final BannerFlagModel wallFlagModel;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getModelSet());
    }

    public BannerRenderer(EntityModelSet entityModelSet) {
        this.standingModel = new BannerModel(entityModelSet.bakeLayer(ModelLayers.STANDING_BANNER));
        this.wallModel = new BannerModel(entityModelSet.bakeLayer(ModelLayers.WALL_BANNER));
        this.standingFlagModel = new BannerFlagModel(entityModelSet.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.wallFlagModel = new BannerFlagModel(entityModelSet.bakeLayer(ModelLayers.WALL_BANNER_FLAG));
    }

    @Override
    public void render(BannerBlockEntity bannerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        BannerFlagModel bannerFlagModel;
        BannerModel bannerModel;
        float f2;
        BlockState blockState = bannerBlockEntity.getBlockState();
        if (blockState.getBlock() instanceof BannerBlock) {
            f2 = -RotationSegment.convertToDegrees(blockState.getValue(BannerBlock.ROTATION));
            bannerModel = this.standingModel;
            bannerFlagModel = this.standingFlagModel;
        } else {
            f2 = -blockState.getValue(WallBannerBlock.FACING).toYRot();
            bannerModel = this.wallModel;
            bannerFlagModel = this.wallFlagModel;
        }
        long l = bannerBlockEntity.getLevel().getGameTime();
        BlockPos blockPos = bannerBlockEntity.getBlockPos();
        float f3 = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l, 100L) + f) / 100.0f;
        BannerRenderer.renderBanner(poseStack, multiBufferSource, n, n2, f2, bannerModel, bannerFlagModel, f3, bannerBlockEntity.getBaseColor(), bannerBlockEntity.getPatterns());
    }

    public void renderInHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers) {
        BannerRenderer.renderBanner(poseStack, multiBufferSource, n, n2, 0.0f, this.standingModel, this.standingFlagModel, 0.0f, dyeColor, bannerPatternLayers);
    }

    private static void renderBanner(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, float f, BannerModel bannerModel, BannerFlagModel bannerFlagModel, float f2, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        bannerModel.renderToBuffer(poseStack, ModelBakery.BANNER_BASE.buffer(multiBufferSource, RenderType::entitySolid), n, n2);
        bannerFlagModel.setupAnim(f2);
        BannerRenderer.renderPatterns(poseStack, multiBufferSource, n, n2, bannerFlagModel.root(), ModelBakery.BANNER_BASE, true, dyeColor, bannerPatternLayers);
        poseStack.popPose();
    }

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, ModelPart modelPart, Material material, boolean bl, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers) {
        BannerRenderer.renderPatterns(poseStack, multiBufferSource, n, n2, modelPart, material, bl, dyeColor, bannerPatternLayers, false, true);
    }

    public static void renderPatterns(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, ModelPart modelPart, Material material, boolean bl, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, boolean bl2, boolean bl3) {
        modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid, bl3, bl2), n, n2);
        BannerRenderer.renderPatternLayer(poseStack, multiBufferSource, n, n2, modelPart, bl ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE, dyeColor);
        for (int i = 0; i < 16 && i < bannerPatternLayers.layers().size(); ++i) {
            BannerPatternLayers.Layer layer = bannerPatternLayers.layers().get(i);
            Material material2 = bl ? Sheets.getBannerMaterial(layer.pattern()) : Sheets.getShieldMaterial(layer.pattern());
            BannerRenderer.renderPatternLayer(poseStack, multiBufferSource, n, n2, modelPart, material2, layer.color());
        }
    }

    private static void renderPatternLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, ModelPart modelPart, Material material, DyeColor dyeColor) {
        int n3 = dyeColor.getTextureDiffuseColor();
        modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entityNoOutline), n, n2, n3);
    }

    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        this.standingModel.root().getExtentsForGui(poseStack, set);
        this.standingFlagModel.setupAnim(0.0f);
        this.standingFlagModel.root().getExtentsForGui(poseStack, set);
    }
}

