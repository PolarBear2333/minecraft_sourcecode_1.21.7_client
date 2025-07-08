/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.joml.Vector3f;

public class DecoratedPotSpecialRenderer
implements SpecialModelRenderer<PotDecorations> {
    private final DecoratedPotRenderer decoratedPotRenderer;

    public DecoratedPotSpecialRenderer(DecoratedPotRenderer decoratedPotRenderer) {
        this.decoratedPotRenderer = decoratedPotRenderer;
    }

    @Override
    @Nullable
    public PotDecorations extractArgument(ItemStack itemStack) {
        return itemStack.get(DataComponents.POT_DECORATIONS);
    }

    @Override
    public void render(@Nullable PotDecorations potDecorations, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        this.decoratedPotRenderer.renderInHand(poseStack, multiBufferSource, n, n2, Objects.requireNonNullElse(potDecorations, PotDecorations.EMPTY));
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        this.decoratedPotRenderer.getExtents(set);
    }

    @Override
    @Nullable
    public /* synthetic */ Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit((Object)new Unbaked());

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new DecoratedPotSpecialRenderer(new DecoratedPotRenderer(entityModelSet));
        }
    }
}

