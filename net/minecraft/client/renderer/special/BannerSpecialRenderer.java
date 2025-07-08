/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Vector3f;

public class BannerSpecialRenderer
implements SpecialModelRenderer<BannerPatternLayers> {
    private final BannerRenderer bannerRenderer;
    private final DyeColor baseColor;

    public BannerSpecialRenderer(DyeColor dyeColor, BannerRenderer bannerRenderer) {
        this.bannerRenderer = bannerRenderer;
        this.baseColor = dyeColor;
    }

    @Override
    @Nullable
    public BannerPatternLayers extractArgument(ItemStack itemStack) {
        return itemStack.get(DataComponents.BANNER_PATTERNS);
    }

    @Override
    public void render(@Nullable BannerPatternLayers bannerPatternLayers, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        this.bannerRenderer.renderInHand(poseStack, multiBufferSource, n, n2, this.baseColor, Objects.requireNonNullElse(bannerPatternLayers, BannerPatternLayers.EMPTY));
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        this.bannerRenderer.getExtents(set);
    }

    @Override
    @Nullable
    public /* synthetic */ Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }

    public record Unbaked(DyeColor baseColor) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DyeColor.CODEC.fieldOf("color").forGetter(Unbaked::baseColor)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new BannerSpecialRenderer(this.baseColor, new BannerRenderer(entityModelSet));
        }
    }
}

