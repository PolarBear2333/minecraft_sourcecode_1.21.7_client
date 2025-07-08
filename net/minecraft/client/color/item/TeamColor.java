/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public record TeamColor(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<TeamColor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(TeamColor::defaultColor)).apply((Applicative)instance, TeamColor::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        ChatFormatting chatFormatting;
        PlayerTeam playerTeam;
        if (livingEntity != null && (playerTeam = livingEntity.getTeam()) != null && (chatFormatting = ((Team)playerTeam).getColor()).getColor() != null) {
            return ARGB.opaque(chatFormatting.getColor());
        }
        return ARGB.opaque(this.defaultColor);
    }

    public MapCodec<TeamColor> type() {
        return MAP_CODEC;
    }
}

