/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public abstract class AbstractSignRenderer
implements BlockEntityRenderer<SignBlockEntity> {
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;

    public AbstractSignRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    protected abstract Model getSignModel(BlockState var1, WoodType var2);

    protected abstract Material getSignMaterial(WoodType var1);

    protected abstract float getSignModelRenderScale();

    protected abstract float getSignTextRenderScale();

    protected abstract Vec3 getTextOffset();

    protected abstract void translateSign(PoseStack var1, float var2, BlockState var3);

    @Override
    public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        BlockState blockState = signBlockEntity.getBlockState();
        SignBlock signBlock = (SignBlock)blockState.getBlock();
        Model model = this.getSignModel(blockState, signBlock.type());
        this.renderSignWithText(signBlockEntity, poseStack, multiBufferSource, n, n2, blockState, signBlock, signBlock.type(), model);
    }

    private void renderSignWithText(SignBlockEntity signBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, BlockState blockState, SignBlock signBlock, WoodType woodType, Model model) {
        poseStack.pushPose();
        this.translateSign(poseStack, -signBlock.getYRotationDegrees(blockState), blockState);
        this.renderSign(poseStack, multiBufferSource, n, n2, woodType, model);
        this.renderSignText(signBlockEntity.getBlockPos(), signBlockEntity.getFrontText(), poseStack, multiBufferSource, n, signBlockEntity.getTextLineHeight(), signBlockEntity.getMaxTextLineWidth(), true);
        this.renderSignText(signBlockEntity.getBlockPos(), signBlockEntity.getBackText(), poseStack, multiBufferSource, n, signBlockEntity.getTextLineHeight(), signBlockEntity.getMaxTextLineWidth(), false);
        poseStack.popPose();
    }

    protected void renderSign(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, WoodType woodType, Model model) {
        poseStack.pushPose();
        float f = this.getSignModelRenderScale();
        poseStack.scale(f, -f, -f);
        Material material = this.getSignMaterial(woodType);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, model::renderType);
        model.renderToBuffer(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }

    private void renderSignText(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, int n3, boolean bl) {
        int n4;
        boolean bl2;
        int n5;
        poseStack.pushPose();
        this.translateSignText(poseStack, bl, this.getTextOffset());
        int n6 = AbstractSignRenderer.getDarkColor(signText);
        int n7 = 4 * n2 / 2;
        FormattedCharSequence[] formattedCharSequenceArray = signText.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
            List<FormattedCharSequence> list = this.font.split((FormattedText)component, n3);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        if (signText.hasGlowingText()) {
            n5 = signText.getColor().getTextColor();
            bl2 = AbstractSignRenderer.isOutlineVisible(blockPos, n5);
            n4 = 0xF000F0;
        } else {
            n5 = n6;
            bl2 = false;
            n4 = n;
        }
        for (int i = 0; i < 4; ++i) {
            FormattedCharSequence formattedCharSequence = formattedCharSequenceArray[i];
            float f = -this.font.width(formattedCharSequence) / 2;
            if (bl2) {
                this.font.drawInBatch8xOutline(formattedCharSequence, f, i * n2 - n7, n5, n6, poseStack.last().pose(), multiBufferSource, n4);
                continue;
            }
            this.font.drawInBatch(formattedCharSequence, f, (float)(i * n2 - n7), n5, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, n4);
        }
        poseStack.popPose();
    }

    private void translateSignText(PoseStack poseStack, boolean bl, Vec3 vec3) {
        if (!bl) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        }
        float f = 0.015625f * this.getSignTextRenderScale();
        poseStack.translate(vec3);
        poseStack.scale(f, -f, f);
    }

    private static boolean isOutlineVisible(BlockPos blockPos, int n) {
        if (n == DyeColor.BLACK.getTextColor()) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
            return true;
        }
        Entity entity = minecraft.getCameraEntity();
        return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockPos)) < (double)OUTLINE_RENDER_DISTANCE;
    }

    public static int getDarkColor(SignText signText) {
        int n = signText.getColor().getTextColor();
        if (n == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
            return -988212;
        }
        return ARGB.scaleRGB(n, 0.4f);
    }
}

