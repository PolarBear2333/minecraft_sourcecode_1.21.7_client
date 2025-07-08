/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.VillagerMetadataSection;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerProfessionLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, (Object)ResourceLocation.withDefaultNamespace("stone"));
        int2ObjectOpenHashMap.put(2, (Object)ResourceLocation.withDefaultNamespace("iron"));
        int2ObjectOpenHashMap.put(3, (Object)ResourceLocation.withDefaultNamespace("gold"));
        int2ObjectOpenHashMap.put(4, (Object)ResourceLocation.withDefaultNamespace("emerald"));
        int2ObjectOpenHashMap.put(5, (Object)ResourceLocation.withDefaultNamespace("diamond"));
    });
    private final Object2ObjectMap<ResourceKey<VillagerType>, VillagerMetadataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap();
    private final Object2ObjectMap<ResourceKey<VillagerProfession>, VillagerMetadataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap();
    private final ResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<S, M> renderLayerParent, ResourceManager resourceManager, String string) {
        super(renderLayerParent);
        this.resourceManager = resourceManager;
        this.path = string;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, S s, float f, float f2) {
        if (((LivingEntityRenderState)s).isInvisible) {
            return;
        }
        VillagerData villagerData = ((VillagerDataHolderRenderState)s).getVillagerData();
        if (villagerData == null) {
            return;
        }
        Holder<VillagerType> holder = villagerData.type();
        Holder<VillagerProfession> holder2 = villagerData.profession();
        VillagerMetadataSection.Hat hat = this.getHatData(this.typeHatCache, "type", holder);
        VillagerMetadataSection.Hat hat2 = this.getHatData(this.professionHatCache, "profession", holder2);
        Object m = this.getParentModel();
        ((VillagerLikeModel)m).hatVisible(hat2 == VillagerMetadataSection.Hat.NONE || hat2 == VillagerMetadataSection.Hat.PARTIAL && hat != VillagerMetadataSection.Hat.FULL);
        ResourceLocation resourceLocation = this.getResourceLocation("type", holder);
        VillagerProfessionLayer.renderColoredCutoutModel(m, resourceLocation, poseStack, multiBufferSource, n, s, -1);
        ((VillagerLikeModel)m).hatVisible(true);
        if (!holder2.is(VillagerProfession.NONE) && !((LivingEntityRenderState)s).isBaby) {
            ResourceLocation resourceLocation2 = this.getResourceLocation("profession", holder2);
            VillagerProfessionLayer.renderColoredCutoutModel(m, resourceLocation2, poseStack, multiBufferSource, n, s, -1);
            if (!holder2.is(VillagerProfession.NITWIT)) {
                ResourceLocation resourceLocation3 = this.getResourceLocation("profession_level", (ResourceLocation)LEVEL_LOCATIONS.get(Mth.clamp(villagerData.level(), 1, LEVEL_LOCATIONS.size())));
                VillagerProfessionLayer.renderColoredCutoutModel(m, resourceLocation3, poseStack, multiBufferSource, n, s, -1);
            }
        }
    }

    private ResourceLocation getResourceLocation(String string, ResourceLocation resourceLocation) {
        return resourceLocation.withPath(string2 -> "textures/entity/" + this.path + "/" + string + "/" + string2 + ".png");
    }

    private ResourceLocation getResourceLocation(String string, Holder<?> holder) {
        return holder.unwrapKey().map(resourceKey -> this.getResourceLocation(string, resourceKey.location())).orElse(MissingTextureAtlasSprite.getLocation());
    }

    public <K> VillagerMetadataSection.Hat getHatData(Object2ObjectMap<ResourceKey<K>, VillagerMetadataSection.Hat> object2ObjectMap, String string, Holder<K> holder) {
        ResourceKey resourceKey = holder.unwrapKey().orElse(null);
        if (resourceKey == null) {
            return VillagerMetadataSection.Hat.NONE;
        }
        return (VillagerMetadataSection.Hat)object2ObjectMap.computeIfAbsent((Object)resourceKey, object -> this.resourceManager.getResource(this.getResourceLocation(string, resourceKey.location())).flatMap(resource -> {
            try {
                return resource.metadata().getSection(VillagerMetadataSection.TYPE).map(VillagerMetadataSection::hat);
            }
            catch (IOException iOException) {
                return Optional.empty();
            }
        }).orElse(VillagerMetadataSection.Hat.NONE));
    }
}

