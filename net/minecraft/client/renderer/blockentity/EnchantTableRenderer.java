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
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class EnchantTableRenderer
implements BlockEntityRenderer<EnchantingTableBlockEntity> {
    public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));
    private final BookModel bookModel;

    public EnchantTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(EnchantingTableBlockEntity enchantingTableBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, Vec3 vec3) {
        float f2;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75f, 0.5f);
        float f3 = (float)enchantingTableBlockEntity.time + f;
        poseStack.translate(0.0f, 0.1f + Mth.sin(f3 * 0.1f) * 0.01f, 0.0f);
        for (f2 = enchantingTableBlockEntity.rot - enchantingTableBlockEntity.oRot; f2 >= (float)Math.PI; f2 -= (float)Math.PI * 2) {
        }
        while (f2 < (float)(-Math.PI)) {
            f2 += (float)Math.PI * 2;
        }
        float f4 = enchantingTableBlockEntity.oRot + f2 * f;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotation(-f4));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(80.0f));
        float f5 = Mth.lerp(f, enchantingTableBlockEntity.oFlip, enchantingTableBlockEntity.flip);
        float f6 = Mth.frac(f5 + 0.25f) * 1.6f - 0.3f;
        float f7 = Mth.frac(f5 + 0.75f) * 1.6f - 0.3f;
        float f8 = Mth.lerp(f, enchantingTableBlockEntity.oOpen, enchantingTableBlockEntity.open);
        this.bookModel.setupAnim(f3, Mth.clamp(f6, 0.0f, 1.0f), Mth.clamp(f7, 0.0f, 1.0f), f8);
        VertexConsumer vertexConsumer = BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }
}

