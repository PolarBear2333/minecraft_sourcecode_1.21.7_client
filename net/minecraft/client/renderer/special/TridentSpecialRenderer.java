/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class TridentSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final TridentModel model;

    public TridentSpecialRenderer(TridentModel tridentModel) {
        this.model = tridentModel;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        poseStack.pushPose();
        poseStack.scale(1.0f, -1.0f, -1.0f);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(multiBufferSource, this.model.renderType(TridentModel.TEXTURE), false, bl);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, n2);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        poseStack.scale(1.0f, -1.0f, -1.0f);
        this.model.root().getExtentsForGui(poseStack, set);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit((Object)new Unbaked());

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new TridentSpecialRenderer(new TridentModel(entityModelSet.bakeLayer(ModelLayers.TRIDENT)));
        }
    }
}

