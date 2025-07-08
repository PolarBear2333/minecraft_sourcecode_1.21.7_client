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
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;

public class HangingSignSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final Model model;
    private final Material material;

    public HangingSignSpecialRenderer(Model model, Material material) {
        this.model = model;
        this.material = material;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        HangingSignRenderer.renderInHand(poseStack, multiBufferSource, n, n2, this.model, this.material);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        HangingSignRenderer.translateBase(poseStack, 0.0f);
        poseStack.scale(1.0f, -1.0f, -1.0f);
        this.model.root().getExtentsForGui(poseStack, set);
    }

    public record Unbaked(WoodType woodType, Optional<ResourceLocation> texture) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WoodType.CODEC.fieldOf("wood_type").forGetter(Unbaked::woodType), (App)ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(Unbaked::texture)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(WoodType woodType) {
            this(woodType, Optional.empty());
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            Model model = HangingSignRenderer.createSignModel(entityModelSet, this.woodType, HangingSignRenderer.AttachmentType.CEILING_MIDDLE);
            Material material = this.texture.map(Sheets.HANGING_SIGN_MAPPER::apply).orElseGet(() -> Sheets.getHangingSignMaterial(this.woodType));
            return new HangingSignSpecialRenderer(model, material);
        }
    }
}

