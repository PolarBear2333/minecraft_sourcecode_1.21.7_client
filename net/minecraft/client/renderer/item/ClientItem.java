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
 */
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.util.RegistryContextSwapper;

public record ClientItem(ItemModel.Unbaked model, Properties properties, @Nullable RegistryContextSwapper registrySwapper) {
    public static final Codec<ClientItem> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemModels.CODEC.fieldOf("model").forGetter(ClientItem::model), (App)Properties.MAP_CODEC.forGetter(ClientItem::properties)).apply((Applicative)instance, ClientItem::new));

    public ClientItem(ItemModel.Unbaked unbaked, Properties properties) {
        this(unbaked, properties, null);
    }

    public ClientItem withRegistrySwapper(RegistryContextSwapper registryContextSwapper) {
        return new ClientItem(this.model, this.properties, registryContextSwapper);
    }

    public record Properties(boolean handAnimationOnSwap, boolean oversizedInGui) {
        public static final Properties DEFAULT = new Properties(true, false);
        public static final MapCodec<Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("hand_animation_on_swap", (Object)true).forGetter(Properties::handAnimationOnSwap), (App)Codec.BOOL.optionalFieldOf("oversized_in_gui", (Object)false).forGetter(Properties::oversizedInGui)).apply((Applicative)instance, Properties::new));
    }
}

