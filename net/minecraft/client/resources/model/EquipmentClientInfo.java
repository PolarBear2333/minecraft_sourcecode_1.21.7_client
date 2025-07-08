/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public record EquipmentClientInfo(Map<LayerType, List<Layer>> layers) {
    private static final Codec<List<Layer>> LAYER_LIST_CODEC = ExtraCodecs.nonEmptyList(Layer.CODEC.listOf());
    public static final Codec<EquipmentClientInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.nonEmptyMap(Codec.unboundedMap(LayerType.CODEC, LAYER_LIST_CODEC)).fieldOf("layers").forGetter(EquipmentClientInfo::layers)).apply((Applicative)instance, EquipmentClientInfo::new));

    public static Builder builder() {
        return new Builder();
    }

    public List<Layer> getLayers(LayerType layerType) {
        return this.layers.getOrDefault(layerType, List.of());
    }

    public static class Builder {
        private final Map<LayerType, List<Layer>> layersByType = new EnumMap<LayerType, List<Layer>>(LayerType.class);

        Builder() {
        }

        public Builder addHumanoidLayers(ResourceLocation resourceLocation) {
            return this.addHumanoidLayers(resourceLocation, false);
        }

        public Builder addHumanoidLayers(ResourceLocation resourceLocation, boolean bl) {
            this.addLayers(LayerType.HUMANOID_LEGGINGS, Layer.leatherDyeable(resourceLocation, bl));
            this.addMainHumanoidLayer(resourceLocation, bl);
            return this;
        }

        public Builder addMainHumanoidLayer(ResourceLocation resourceLocation, boolean bl) {
            return this.addLayers(LayerType.HUMANOID, Layer.leatherDyeable(resourceLocation, bl));
        }

        public Builder addLayers(LayerType layerType2, Layer ... layerArray) {
            Collections.addAll(this.layersByType.computeIfAbsent(layerType2, layerType -> new ArrayList()), layerArray);
            return this;
        }

        public EquipmentClientInfo build() {
            return new EquipmentClientInfo((Map)this.layersByType.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> List.copyOf((Collection)entry.getValue()))));
        }
    }

    public static enum LayerType implements StringRepresentable
    {
        HUMANOID("humanoid"),
        HUMANOID_LEGGINGS("humanoid_leggings"),
        WINGS("wings"),
        WOLF_BODY("wolf_body"),
        HORSE_BODY("horse_body"),
        LLAMA_BODY("llama_body"),
        PIG_SADDLE("pig_saddle"),
        STRIDER_SADDLE("strider_saddle"),
        CAMEL_SADDLE("camel_saddle"),
        HORSE_SADDLE("horse_saddle"),
        DONKEY_SADDLE("donkey_saddle"),
        MULE_SADDLE("mule_saddle"),
        ZOMBIE_HORSE_SADDLE("zombie_horse_saddle"),
        SKELETON_HORSE_SADDLE("skeleton_horse_saddle"),
        HAPPY_GHAST_BODY("happy_ghast_body");

        public static final Codec<LayerType> CODEC;
        private final String id;

        private LayerType(String string2) {
            this.id = string2;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public String trimAssetPrefix() {
            return "trims/entity/" + this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(LayerType::values);
        }
    }

    public record Layer(ResourceLocation textureId, Optional<Dyeable> dyeable, boolean usePlayerTexture) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("texture").forGetter(Layer::textureId), (App)Dyeable.CODEC.optionalFieldOf("dyeable").forGetter(Layer::dyeable), (App)Codec.BOOL.optionalFieldOf("use_player_texture", (Object)false).forGetter(Layer::usePlayerTexture)).apply((Applicative)instance, Layer::new));

        public Layer(ResourceLocation resourceLocation) {
            this(resourceLocation, Optional.empty(), false);
        }

        public static Layer leatherDyeable(ResourceLocation resourceLocation, boolean bl) {
            return new Layer(resourceLocation, bl ? Optional.of(new Dyeable(Optional.of(-6265536))) : Optional.empty(), false);
        }

        public static Layer onlyIfDyed(ResourceLocation resourceLocation, boolean bl) {
            return new Layer(resourceLocation, bl ? Optional.of(new Dyeable(Optional.empty())) : Optional.empty(), false);
        }

        public ResourceLocation getTextureLocation(LayerType layerType) {
            return this.textureId.withPath(string -> "textures/entity/equipment/" + layerType.getSerializedName() + "/" + string + ".png");
        }
    }

    public record Dyeable(Optional<Integer> colorWhenUndyed) {
        public static final Codec<Dyeable> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color_when_undyed").forGetter(Dyeable::colorWhenUndyed)).apply((Applicative)instance, Dyeable::new));
    }
}

