/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.equipment.trim;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern) implements TooltipProvider
{
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), (App)TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)).apply((Applicative)instance, ArmorTrim::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(TrimMaterial.STREAM_CODEC, ArmorTrim::material, TrimPattern.STREAM_CODEC, ArmorTrim::pattern, ArmorTrim::new);
    private static final Component UPGRADE_TITLE = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.upgrade"))).withStyle(ChatFormatting.GRAY);

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        consumer.accept(UPGRADE_TITLE);
        consumer.accept(CommonComponents.space().append(this.pattern.value().copyWithStyle(this.material)));
        consumer.accept(CommonComponents.space().append(this.material.value().description()));
    }

    public ResourceLocation layerAssetId(String string, ResourceKey<EquipmentAsset> resourceKey) {
        MaterialAssetGroup.AssetInfo assetInfo = this.material().value().assets().assetId(resourceKey);
        return this.pattern().value().assetId().withPath(string2 -> string + "/" + string2 + "_" + assetInfo.suffix());
    }
}

