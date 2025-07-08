/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemRenderer {
    public static final ResourceLocation ENCHANTED_GLINT_ARMOR = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_armor.png");
    public static final ResourceLocation ENCHANTED_GLINT_ITEM = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
    public static final float SPECIAL_FOIL_UI_SCALE = 0.5f;
    public static final float SPECIAL_FOIL_FIRST_PERSON_SCALE = 0.75f;
    public static final float SPECIAL_FOIL_TEXTURE_SCALE = 0.0078125f;
    public static final int NO_TINT = -1;
    private final ItemModelResolver resolver;
    private final ItemStackRenderState scratchItemStackRenderState = new ItemStackRenderState();

    public ItemRenderer(ItemModelResolver itemModelResolver) {
        this.resolver = itemModelResolver;
    }

    public static void renderItem(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, int[] nArray, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType) {
        VertexConsumer vertexConsumer;
        if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
            PoseStack.Pose pose = poseStack.last().copy();
            if (itemDisplayContext == ItemDisplayContext.GUI) {
                MatrixUtil.mulComponentWise(pose.pose(), 0.5f);
            } else if (itemDisplayContext.firstPerson()) {
                MatrixUtil.mulComponentWise(pose.pose(), 0.75f);
            }
            vertexConsumer = ItemRenderer.getSpecialFoilBuffer(multiBufferSource, renderType, pose);
        } else {
            vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, renderType, true, foilType != ItemStackRenderState.FoilType.NONE);
        }
        ItemRenderer.renderQuadList(poseStack, vertexConsumer, list, nArray, n, n2);
    }

    public static VertexConsumer getArmorFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl) {
        if (bl) {
            return VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.armorEntityGlint()), multiBufferSource.getBuffer(renderType));
        }
        return multiBufferSource.getBuffer(renderType);
    }

    private static VertexConsumer getSpecialFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
        return VertexMultiConsumer.create((VertexConsumer)new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(ItemRenderer.useTransparentGlint(renderType) ? RenderType.glintTranslucent() : RenderType.glint()), pose, 0.0078125f), multiBufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            if (ItemRenderer.useTransparentGlint(renderType)) {
                return VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.glintTranslucent()), multiBufferSource.getBuffer(renderType));
            }
            return VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.glint() : RenderType.entityGlint()), multiBufferSource.getBuffer(renderType));
        }
        return multiBufferSource.getBuffer(renderType);
    }

    private static boolean useTransparentGlint(RenderType renderType) {
        return Minecraft.useShaderTransparency() && renderType == Sheets.translucentItemSheet();
    }

    private static int getLayerColorSafe(int[] nArray, int n) {
        if (n < 0 || n >= nArray.length) {
            return -1;
        }
        return nArray[n];
    }

    private static void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, int[] nArray, int n, int n2) {
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad bakedQuad : list) {
            float f;
            float f2;
            float f3;
            float f4;
            if (bakedQuad.isTinted()) {
                int n3 = ItemRenderer.getLayerColorSafe(nArray, bakedQuad.tintIndex());
                f4 = (float)ARGB.alpha(n3) / 255.0f;
                f3 = (float)ARGB.red(n3) / 255.0f;
                f2 = (float)ARGB.green(n3) / 255.0f;
                f = (float)ARGB.blue(n3) / 255.0f;
            } else {
                f4 = 1.0f;
                f3 = 1.0f;
                f2 = 1.0f;
                f = 1.0f;
            }
            vertexConsumer.putBulkData(pose, bakedQuad, f3, f2, f, f4, n, n2);
        }
    }

    public void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int n, int n2, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int n3) {
        this.renderStatic(null, itemStack, itemDisplayContext, poseStack, multiBufferSource, level, n, n2, n3);
    }

    public void renderStatic(@Nullable LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int n, int n2, int n3) {
        this.resolver.updateForTopItem(this.scratchItemStackRenderState, itemStack, itemDisplayContext, level, livingEntity, n3);
        this.scratchItemStackRenderState.render(poseStack, multiBufferSource, n, n2);
    }
}

