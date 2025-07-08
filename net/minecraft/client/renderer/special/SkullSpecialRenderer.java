/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Vector3f;

public class SkullSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final SkullModelBase model;
    private final float animation;
    private final RenderType renderType;

    public SkullSpecialRenderer(SkullModelBase skullModelBase, float f, RenderType renderType) {
        this.model = skullModelBase;
        this.animation = f;
        this.renderType = renderType;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        SkullBlockRenderer.renderSkull(null, 180.0f, this.animation, poseStack, multiBufferSource, n, this.model, this.renderType);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.model.setupAnim(this.animation, 180.0f, 0.0f);
        this.model.root().getExtentsForGui(poseStack, set);
    }

    public record Unbaked(SkullBlock.Type kind, Optional<ResourceLocation> textureOverride, float animation) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SkullBlock.Type.CODEC.fieldOf("kind").forGetter(Unbaked::kind), (App)ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(Unbaked::textureOverride), (App)Codec.FLOAT.optionalFieldOf("animation", (Object)Float.valueOf(0.0f)).forGetter(Unbaked::animation)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(SkullBlock.Type type) {
            this(type, Optional.empty(), 0.0f);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        @Nullable
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            SkullModelBase skullModelBase = SkullBlockRenderer.createModel(entityModelSet, this.kind);
            ResourceLocation resourceLocation2 = this.textureOverride.map(resourceLocation -> resourceLocation.withPath(string -> "textures/entity/" + string + ".png")).orElse(null);
            if (skullModelBase == null) {
                return null;
            }
            RenderType renderType = SkullBlockRenderer.getSkullRenderType(this.kind, resourceLocation2);
            return new SkullSpecialRenderer(skullModelBase, this.animation, renderType);
        }
    }
}

