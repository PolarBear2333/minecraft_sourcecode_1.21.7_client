/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class BedSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final BedRenderer bedRenderer;
    private final Material material;

    public BedSpecialRenderer(BedRenderer bedRenderer, Material material) {
        this.bedRenderer = bedRenderer;
        this.material = material;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        this.bedRenderer.renderInHand(poseStack, multiBufferSource, n, n2, this.material);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        this.bedRenderer.getExtents(set);
    }

    public record Unbaked(ResourceLocation texture) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("texture").forGetter(Unbaked::texture)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(DyeColor dyeColor) {
            this(Sheets.colorToResourceMaterial(dyeColor));
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new BedSpecialRenderer(new BedRenderer(entityModelSet), Sheets.BED_MAPPER.apply(this.texture));
        }
    }
}

