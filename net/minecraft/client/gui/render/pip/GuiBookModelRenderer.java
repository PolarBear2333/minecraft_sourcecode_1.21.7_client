/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiBookModelRenderState;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionfc;

public class GuiBookModelRenderer
extends PictureInPictureRenderer<GuiBookModelRenderState> {
    public GuiBookModelRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiBookModelRenderState> getRenderStateClass() {
        return GuiBookModelRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiBookModelRenderState guiBookModelRenderState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(25.0f));
        float f = guiBookModelRenderState.open();
        poseStack.translate((1.0f - f) * 0.2f, (1.0f - f) * 0.1f, (1.0f - f) * 0.25f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-(1.0f - f) * 90.0f - 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(180.0f));
        float f2 = guiBookModelRenderState.flip();
        float f3 = Mth.clamp(Mth.frac(f2 + 0.25f) * 1.6f - 0.3f, 0.0f, 1.0f);
        float f4 = Mth.clamp(Mth.frac(f2 + 0.75f) * 1.6f - 0.3f, 0.0f, 1.0f);
        BookModel bookModel = guiBookModelRenderState.bookModel();
        bookModel.setupAnim(0.0f, f3, f4, f);
        ResourceLocation resourceLocation = guiBookModelRenderState.texture();
        VertexConsumer vertexConsumer = this.bufferSource.getBuffer(bookModel.renderType(resourceLocation));
        bookModel.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
    }

    @Override
    protected float getTranslateY(int n, int n2) {
        return 17 * n2;
    }

    @Override
    protected String getTextureLabel() {
        return "book model";
    }
}

