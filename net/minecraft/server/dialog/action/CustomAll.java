/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dialog.action;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.action.Action;

public record CustomAll(ResourceLocation id, Optional<CompoundTag> additions) implements Action
{
    public static final MapCodec<CustomAll> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("id").forGetter(CustomAll::id), (App)CompoundTag.CODEC.optionalFieldOf("additions").forGetter(CustomAll::additions)).apply((Applicative)instance, CustomAll::new));

    public MapCodec<CustomAll> codec() {
        return MAP_CODEC;
    }

    @Override
    public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> map) {
        CompoundTag compoundTag = this.additions.map(CompoundTag::copy).orElseGet(CompoundTag::new);
        map.forEach((string, valueGetter) -> compoundTag.put((String)string, valueGetter.asTag()));
        return Optional.of(new ClickEvent.Custom(this.id, Optional.of(compoundTag)));
    }
}

