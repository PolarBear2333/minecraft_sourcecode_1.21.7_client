/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public class EquipmentLayerRenderer {
    private static final int NO_LAYER_COLOR = 0;
    private final EquipmentAssetManager equipmentAssets;
    private final Function<LayerTextureKey, ResourceLocation> layerTextureLookup;
    private final Function<TrimSpriteKey, TextureAtlasSprite> trimSpriteLookup;

    public EquipmentLayerRenderer(EquipmentAssetManager equipmentAssetManager, TextureAtlas textureAtlas) {
        this.equipmentAssets = equipmentAssetManager;
        this.layerTextureLookup = Util.memoize(layerTextureKey -> layerTextureKey.layer.getTextureLocation(layerTextureKey.layerType));
        this.trimSpriteLookup = Util.memoize(trimSpriteKey -> textureAtlas.getSprite(trimSpriteKey.spriteId()));
    }

    public void renderLayers(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> resourceKey, Model model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        this.renderLayers(layerType, resourceKey, model, itemStack, poseStack, multiBufferSource, n, null);
    }

    public void renderLayers(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> resourceKey, Model model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, @Nullable ResourceLocation resourceLocation) {
        List<EquipmentClientInfo.Layer> list = this.equipmentAssets.get(resourceKey).getLayers(layerType);
        if (list.isEmpty()) {
            return;
        }
        int n2 = DyedItemColor.getOrDefault(itemStack, 0);
        boolean bl = itemStack.hasFoil();
        for (EquipmentClientInfo.Layer object : list) {
            int vertexConsumer = EquipmentLayerRenderer.getColorForLayer(object, n2);
            if (vertexConsumer == 0) continue;
            ResourceLocation resourceLocation2 = object.usePlayerTexture() && resourceLocation != null ? resourceLocation : this.layerTextureLookup.apply(new LayerTextureKey(layerType, object));
            VertexConsumer vertexConsumer2 = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(resourceLocation2), bl);
            model.renderToBuffer(poseStack, vertexConsumer2, n, OverlayTexture.NO_OVERLAY, vertexConsumer);
            bl = false;
        }
        ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
        if (armorTrim != null) {
            TextureAtlasSprite textureAtlasSprite = this.trimSpriteLookup.apply(new TrimSpriteKey(armorTrim, layerType, resourceKey));
            VertexConsumer vertexConsumer = textureAtlasSprite.wrap(multiBufferSource.getBuffer(Sheets.armorTrimsSheet(armorTrim.pattern().value().decal())));
            model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
        }
    }

    private static int getColorForLayer(EquipmentClientInfo.Layer layer, int n) {
        Optional<EquipmentClientInfo.Dyeable> optional = layer.dyeable();
        if (optional.isPresent()) {
            int n2 = optional.get().colorWhenUndyed().map(ARGB::opaque).orElse(0);
            return n != 0 ? n : n2;
        }
        return -1;
    }

    static final class LayerTextureKey
    extends Record {
        final EquipmentClientInfo.LayerType layerType;
        final EquipmentClientInfo.Layer layer;

        LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
            this.layerType = layerType;
            this.layer = layer;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LayerTextureKey.class, "layerType;layer", "layerType", "layer"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LayerTextureKey.class, "layerType;layer", "layerType", "layer"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LayerTextureKey.class, "layerType;layer", "layerType", "layer"}, this, object);
        }

        public EquipmentClientInfo.LayerType layerType() {
            return this.layerType;
        }

        public EquipmentClientInfo.Layer layer() {
            return this.layer;
        }
    }

    record TrimSpriteKey(ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId) {
        public ResourceLocation spriteId() {
            return this.trim.layerAssetId(this.layerType.trimAssetPrefix(), this.equipmentAssetId);
        }
    }
}

