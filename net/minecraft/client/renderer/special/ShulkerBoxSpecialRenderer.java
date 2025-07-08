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
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class ShulkerBoxSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final ShulkerBoxRenderer shulkerBoxRenderer;
    private final float openness;
    private final Direction orientation;
    private final Material material;

    public ShulkerBoxSpecialRenderer(ShulkerBoxRenderer shulkerBoxRenderer, float f, Direction direction, Material material) {
        this.shulkerBoxRenderer = shulkerBoxRenderer;
        this.openness = f;
        this.orientation = direction;
        this.material = material;
    }

    @Override
    public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2, boolean bl) {
        this.shulkerBoxRenderer.render(poseStack, multiBufferSource, n, n2, this.orientation, this.openness, this.material);
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        this.shulkerBoxRenderer.getExtents(this.orientation, this.openness, set);
    }

    public record Unbaked(ResourceLocation texture, float openness, Direction orientation) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("texture").forGetter(Unbaked::texture), (App)Codec.FLOAT.optionalFieldOf("openness", (Object)Float.valueOf(0.0f)).forGetter(Unbaked::openness), (App)Direction.CODEC.optionalFieldOf("orientation", Direction.UP).forGetter(Unbaked::orientation)).apply((Applicative)instance, Unbaked::new));

        public Unbaked() {
            this(ResourceLocation.withDefaultNamespace("shulker"), 0.0f, Direction.UP);
        }

        public Unbaked(DyeColor dyeColor) {
            this(Sheets.colorToShulkerMaterial(dyeColor), 0.0f, Direction.UP);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new ShulkerBoxSpecialRenderer(new ShulkerBoxRenderer(entityModelSet), this.openness, this.orientation, Sheets.SHULKER_MAPPER.apply(this.texture));
        }
    }
}

