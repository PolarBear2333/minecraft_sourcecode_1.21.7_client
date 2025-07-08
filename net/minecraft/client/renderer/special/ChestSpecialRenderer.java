/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class ChestSpecialRenderer
implements NoDataSpecialModelRenderer {
    public static final ResourceLocation GIFT_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("christmas");
    public static final ResourceLocation NORMAL_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("normal");
    public static final ResourceLocation TRAPPED_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("trapped");
    public static final ResourceLocation ENDER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("ender");
    private final ChestModel model;
    private final Material material;
    private final float openness;

    public ChestSpecialRenderer(ChestModel chestModel, Material material, float f) {
        this.model = chestModel;
        this.material = material;
        this.openness = f;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        VertexConsumer vertexConsumer = this.material.buffer(multiBufferSource, RenderType::entitySolid);
        this.model.setupAnim(this.openness);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, n2);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(this.openness);
        this.model.root().getExtentsForGui(poseStack, set);
    }

    public record Unbaked(ResourceLocation texture, float openness) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("texture").forGetter(Unbaked::texture), (App)Codec.FLOAT.optionalFieldOf("openness", (Object)Float.valueOf(0.0f)).forGetter(Unbaked::openness)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(ResourceLocation resourceLocation) {
            this(resourceLocation, 0.0f);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            ChestModel chestModel = new ChestModel(entityModelSet.bakeLayer(ModelLayers.CHEST));
            Material material = Sheets.CHEST_MAPPER.apply(this.texture);
            return new ChestSpecialRenderer(chestModel, material, this.openness);
        }
    }
}

