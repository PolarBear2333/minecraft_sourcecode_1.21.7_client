/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.item;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;

public class BlockModelWrapper
implements ItemModel {
    private final List<ItemTintSource> tints;
    private final List<BakedQuad> quads;
    private final Supplier<Vector3f[]> extents;
    private final ModelRenderProperties properties;
    private final boolean animated;

    public BlockModelWrapper(List<ItemTintSource> list, List<BakedQuad> list2, ModelRenderProperties modelRenderProperties) {
        this.tints = list;
        this.quads = list2;
        this.properties = modelRenderProperties;
        this.extents = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(this.quads));
        boolean bl = false;
        for (BakedQuad bakedQuad : list2) {
            if (!bakedQuad.sprite().isAnimated()) continue;
            bl = true;
            break;
        }
        this.animated = bl;
    }

    public static Vector3f[] computeExtents(List<BakedQuad> list) {
        HashSet hashSet = new HashSet();
        for (BakedQuad bakedQuad : list) {
            FaceBakery.extractPositions(bakedQuad.vertices(), hashSet::add);
        }
        return (Vector3f[])hashSet.toArray(Vector3f[]::new);
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        itemStackRenderState.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layerRenderState = itemStackRenderState.newLayer();
        if (itemStack.hasFoil()) {
            ItemStackRenderState.FoilType foilType = BlockModelWrapper.hasSpecialAnimatedTexture(itemStack) ? ItemStackRenderState.FoilType.SPECIAL : ItemStackRenderState.FoilType.STANDARD;
            layerRenderState.setFoilType(foilType);
            itemStackRenderState.setAnimated();
            itemStackRenderState.appendModelIdentityElement((Object)foilType);
        }
        int n2 = this.tints.size();
        int[] nArray = layerRenderState.prepareTintLayers(n2);
        for (int i = 0; i < n2; ++i) {
            int n3;
            nArray[i] = n3 = this.tints.get(i).calculate(itemStack, clientLevel, livingEntity);
            itemStackRenderState.appendModelIdentityElement(n3);
        }
        layerRenderState.setExtents(this.extents);
        layerRenderState.setRenderType(ItemBlockRenderTypes.getRenderType(itemStack));
        this.properties.applyToLayer(layerRenderState, itemDisplayContext);
        layerRenderState.prepareQuadList().addAll(this.quads);
        if (this.animated) {
            itemStackRenderState.setAnimated();
        }
    }

    private static boolean hasSpecialAnimatedTexture(ItemStack itemStack) {
        return itemStack.is(ItemTags.COMPASSES) || itemStack.is(Items.CLOCK);
    }

    public record Unbaked(ResourceLocation model, List<ItemTintSource> tints) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::model), (App)ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)).apply((Applicative)instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            ModelBaker modelBaker = bakingContext.blockModelBaker();
            ResolvedModel resolvedModel = modelBaker.getModel(this.model);
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            List<BakedQuad> list = resolvedModel.bakeTopGeometry(textureSlots, modelBaker, BlockModelRotation.X0_Y0).getAll();
            ModelRenderProperties modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, resolvedModel, textureSlots);
            return new BlockModelWrapper(this.tints, list, modelRenderProperties);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}

