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
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class SpecialModelWrapper<T>
implements ItemModel {
    private final SpecialModelRenderer<T> specialRenderer;
    private final ModelRenderProperties properties;

    public SpecialModelWrapper(SpecialModelRenderer<T> specialModelRenderer, ModelRenderProperties modelRenderProperties) {
        this.specialRenderer = specialModelRenderer;
        this.properties = modelRenderProperties;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        ItemStackRenderState.FoilType foilType;
        itemStackRenderState.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layerRenderState = itemStackRenderState.newLayer();
        if (itemStack.hasFoil()) {
            foilType = ItemStackRenderState.FoilType.STANDARD;
            layerRenderState.setFoilType(foilType);
            itemStackRenderState.setAnimated();
            itemStackRenderState.appendModelIdentityElement((Object)foilType);
        }
        foilType = this.specialRenderer.extractArgument(itemStack);
        layerRenderState.setExtents(() -> {
            HashSet<Vector3f> hashSet = new HashSet<Vector3f>();
            this.specialRenderer.getExtents(hashSet);
            return hashSet.toArray(new Vector3f[0]);
        });
        layerRenderState.setupSpecialModel(this.specialRenderer, foilType);
        if (foilType != null) {
            itemStackRenderState.appendModelIdentityElement((Object)foilType);
        }
        this.properties.applyToLayer(layerRenderState, itemDisplayContext);
    }

    public record Unbaked(ResourceLocation base, SpecialModelRenderer.Unbaked specialModel) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("base").forGetter(Unbaked::base), (App)SpecialModelRenderers.CODEC.fieldOf("model").forGetter(Unbaked::specialModel)).apply((Applicative)instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            SpecialModelRenderer<?> specialModelRenderer = this.specialModel.bake(bakingContext.entityModelSet());
            if (specialModelRenderer == null) {
                return bakingContext.missingItemModel();
            }
            ModelRenderProperties modelRenderProperties = this.getProperties(bakingContext);
            return new SpecialModelWrapper(specialModelRenderer, modelRenderProperties);
        }

        private ModelRenderProperties getProperties(ItemModel.BakingContext bakingContext) {
            ModelBaker modelBaker = bakingContext.blockModelBaker();
            ResolvedModel resolvedModel = modelBaker.getModel(this.base);
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            return ModelRenderProperties.fromResolvedModel(modelBaker, resolvedModel, textureSlots);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

