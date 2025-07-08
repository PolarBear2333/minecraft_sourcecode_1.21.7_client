/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    private final Minecraft minecraft;
    private final MultiBufferSource bufferSource;
    public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
    @Nullable
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;

    public ScreenEffectRenderer(Minecraft minecraft, MultiBufferSource multiBufferSource) {
        this.minecraft = minecraft;
        this.bufferSource = multiBufferSource;
    }

    public void tick() {
        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    public void renderScreenEffect(boolean bl, float f) {
        PoseStack poseStack = new PoseStack();
        LocalPlayer localPlayer = this.minecraft.player;
        if (this.minecraft.options.getCameraType().isFirstPerson() && !bl) {
            BlockState blockState;
            if (!localPlayer.noPhysics && (blockState = ScreenEffectRenderer.getViewBlockingState(localPlayer)) != null) {
                ScreenEffectRenderer.renderTex(this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState), poseStack, this.bufferSource);
            }
            if (!this.minecraft.player.isSpectator()) {
                if (this.minecraft.player.isEyeInFluid(FluidTags.WATER)) {
                    ScreenEffectRenderer.renderWater(this.minecraft, poseStack, this.bufferSource);
                }
                if (this.minecraft.player.isOnFire()) {
                    ScreenEffectRenderer.renderFire(poseStack, this.bufferSource);
                }
            }
        }
        if (!this.minecraft.options.hideGui) {
            this.renderItemActivationAnimation(poseStack, f);
        }
    }

    private void renderItemActivationAnimation(PoseStack poseStack, float f) {
        if (this.itemActivationItem == null || this.itemActivationTicks <= 0) {
            return;
        }
        int n = 40 - this.itemActivationTicks;
        float f2 = ((float)n + f) / 40.0f;
        float f3 = f2 * f2;
        float f4 = f2 * f3;
        float f5 = 10.25f * f4 * f3 - 24.95f * f3 * f3 + 25.5f * f4 - 13.8f * f3 + 4.0f * f2;
        float f6 = f5 * (float)Math.PI;
        float f7 = (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight();
        float f8 = this.itemActivationOffX * 0.3f * f7;
        float f9 = this.itemActivationOffY * 0.3f;
        poseStack.pushPose();
        poseStack.translate(f8 * Mth.abs(Mth.sin(f6 * 2.0f)), f9 * Mth.abs(Mth.sin(f6 * 2.0f)), -10.0f + 9.0f * Mth.sin(f6));
        float f10 = 0.8f;
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(900.0f * Mth.abs(Mth.sin(f6))));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(6.0f * Mth.cos(f2 * 8.0f)));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(6.0f * Mth.cos(f2 * 8.0f)));
        this.minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, this.bufferSource, this.minecraft.level, 0);
        poseStack.popPose();
    }

    public void resetItemActivation() {
        this.itemActivationItem = null;
    }

    public void displayItemActivation(ItemStack itemStack, RandomSource randomSource) {
        this.itemActivationItem = itemStack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = randomSource.nextFloat() * 2.0f - 1.0f;
        this.itemActivationOffY = randomSource.nextFloat() * 2.0f - 1.0f;
    }

    @Nullable
    private static BlockState getViewBlockingState(Player player) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; ++i) {
            double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            double d2 = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f * player.getScale());
            double d3 = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            mutableBlockPos.set(d, d2, d3);
            BlockState blockState = player.level().getBlockState(mutableBlockPos);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE || !blockState.isViewBlocking(player.level(), mutableBlockPos)) continue;
            return blockState;
        }
        return null;
    }

    private static void renderTex(TextureAtlasSprite textureAtlasSprite, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        float f = 0.1f;
        int n = ARGB.colorFromFloat(1.0f, 0.1f, 0.1f, 0.1f);
        float f2 = -1.0f;
        float f3 = 1.0f;
        float f4 = -1.0f;
        float f5 = 1.0f;
        float f6 = -0.5f;
        float f7 = textureAtlasSprite.getU0();
        float f8 = textureAtlasSprite.getU1();
        float f9 = textureAtlasSprite.getV0();
        float f10 = textureAtlasSprite.getV1();
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockScreenEffect(textureAtlasSprite.atlasLocation()));
        vertexConsumer.addVertex(matrix4f, -1.0f, -1.0f, -0.5f).setUv(f8, f10).setColor(n);
        vertexConsumer.addVertex(matrix4f, 1.0f, -1.0f, -0.5f).setUv(f7, f10).setColor(n);
        vertexConsumer.addVertex(matrix4f, 1.0f, 1.0f, -0.5f).setUv(f7, f9).setColor(n);
        vertexConsumer.addVertex(matrix4f, -1.0f, 1.0f, -0.5f).setUv(f8, f9).setColor(n);
    }

    private static void renderWater(Minecraft minecraft, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        BlockPos blockPos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
        float f = LightTexture.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(blockPos));
        int n = ARGB.colorFromFloat(0.1f, f, f, f);
        float f2 = 4.0f;
        float f3 = -1.0f;
        float f4 = 1.0f;
        float f5 = -1.0f;
        float f6 = 1.0f;
        float f7 = -0.5f;
        float f8 = -minecraft.player.getYRot() / 64.0f;
        float f9 = minecraft.player.getXRot() / 64.0f;
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockScreenEffect(UNDERWATER_LOCATION));
        vertexConsumer.addVertex(matrix4f, -1.0f, -1.0f, -0.5f).setUv(4.0f + f8, 4.0f + f9).setColor(n);
        vertexConsumer.addVertex(matrix4f, 1.0f, -1.0f, -0.5f).setUv(0.0f + f8, 4.0f + f9).setColor(n);
        vertexConsumer.addVertex(matrix4f, 1.0f, 1.0f, -0.5f).setUv(0.0f + f8, 0.0f + f9).setColor(n);
        vertexConsumer.addVertex(matrix4f, -1.0f, 1.0f, -0.5f).setUv(4.0f + f8, 0.0f + f9).setColor(n);
    }

    private static void renderFire(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_1.sprite();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.fireScreenEffect(textureAtlasSprite.atlasLocation()));
        float f = textureAtlasSprite.getU0();
        float f2 = textureAtlasSprite.getU1();
        float f3 = (f + f2) / 2.0f;
        float f4 = textureAtlasSprite.getV0();
        float f5 = textureAtlasSprite.getV1();
        float f6 = (f4 + f5) / 2.0f;
        float f7 = textureAtlasSprite.uvShrinkRatio();
        float f8 = Mth.lerp(f7, f, f3);
        float f9 = Mth.lerp(f7, f2, f3);
        float f10 = Mth.lerp(f7, f4, f6);
        float f11 = Mth.lerp(f7, f5, f6);
        float f12 = 1.0f;
        for (int i = 0; i < 2; ++i) {
            poseStack.pushPose();
            float f13 = -0.5f;
            float f14 = 0.5f;
            float f15 = -0.5f;
            float f16 = 0.5f;
            float f17 = -0.5f;
            poseStack.translate((float)(-(i * 2 - 1)) * 0.24f, -0.3f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0f));
            Matrix4f matrix4f = poseStack.last().pose();
            vertexConsumer.addVertex(matrix4f, -0.5f, -0.5f, -0.5f).setUv(f9, f11).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            vertexConsumer.addVertex(matrix4f, 0.5f, -0.5f, -0.5f).setUv(f8, f11).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            vertexConsumer.addVertex(matrix4f, 0.5f, 0.5f, -0.5f).setUv(f8, f10).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            vertexConsumer.addVertex(matrix4f, -0.5f, 0.5f, -0.5f).setUv(f9, f10).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            poseStack.popPose();
        }
    }
}

