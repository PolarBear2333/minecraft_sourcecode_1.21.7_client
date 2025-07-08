/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Quaternionfc;

public class CustomHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private static final float ITEM_SCALE = 0.625f;
    private static final float SKULL_SCALE = 1.1875f;
    private final Transforms transforms;
    private final Function<SkullBlock.Type, SkullModelBase> skullModels;

    public CustomHeadLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet) {
        this(renderLayerParent, entityModelSet, Transforms.DEFAULT);
    }

    public CustomHeadLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, Transforms transforms) {
        super(renderLayerParent);
        this.transforms = transforms;
        this.skullModels = Util.memoize(type -> SkullBlockRenderer.createModel(entityModelSet, type));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        if (((LivingEntityRenderState)s).headItem.isEmpty() && ((LivingEntityRenderState)s).wornHeadType == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(this.transforms.horizontalScale(), 1.0f, this.transforms.horizontalScale());
        Object m = this.getParentModel();
        ((Model)m).root().translateAndRotate(poseStack);
        ((HeadedModel)m).getHead().translateAndRotate(poseStack);
        if (((LivingEntityRenderState)s).wornHeadType != null) {
            poseStack.translate(0.0f, this.transforms.skullYOffset(), 0.0f);
            poseStack.scale(1.1875f, -1.1875f, -1.1875f);
            poseStack.translate(-0.5, 0.0, -0.5);
            SkullBlock.Type type = ((LivingEntityRenderState)s).wornHeadType;
            SkullModelBase skullModelBase = this.skullModels.apply(type);
            RenderType renderType = SkullBlockRenderer.getRenderType(type, ((LivingEntityRenderState)s).wornHeadProfile);
            SkullBlockRenderer.renderSkull(null, 180.0f, ((LivingEntityRenderState)s).wornHeadAnimationPos, poseStack, multiBufferSource, n, skullModelBase, renderType);
        } else {
            CustomHeadLayer.translateToHead(poseStack, this.transforms);
            ((LivingEntityRenderState)s).headItem.render(poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    public static void translateToHead(PoseStack poseStack, Transforms transforms) {
        poseStack.translate(0.0f, -0.25f + transforms.yOffset(), 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
    }

    public record Transforms(float yOffset, float skullYOffset, float horizontalScale) {
        public static final Transforms DEFAULT = new Transforms(0.0f, 0.0f, 1.0f);
    }
}

