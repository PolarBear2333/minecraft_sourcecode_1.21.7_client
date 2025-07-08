/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.npc;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public record VillagerData(Holder<VillagerType> type, Holder<VillagerProfession> profession, int level) {
    public static final int MIN_VILLAGER_LEVEL = 1;
    public static final int MAX_VILLAGER_LEVEL = 5;
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BuiltInRegistries.VILLAGER_TYPE.holderByNameCodec().fieldOf("type").orElseGet(() -> BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS)).forGetter(villagerData -> villagerData.type), (App)BuiltInRegistries.VILLAGER_PROFESSION.holderByNameCodec().fieldOf("profession").orElseGet(() -> BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE)).forGetter(villagerData -> villagerData.profession), (App)Codec.INT.fieldOf("level").orElse((Object)1).forGetter(villagerData -> villagerData.level)).apply((Applicative)instance, VillagerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.VILLAGER_TYPE), VillagerData::type, ByteBufCodecs.holderRegistry(Registries.VILLAGER_PROFESSION), VillagerData::profession, ByteBufCodecs.VAR_INT, VillagerData::level, VillagerData::new);

    public VillagerData {
        n = Math.max(1, n);
    }

    public VillagerData withType(Holder<VillagerType> holder) {
        return new VillagerData(holder, this.profession, this.level);
    }

    public VillagerData withType(HolderGetter.Provider provider, ResourceKey<VillagerType> resourceKey) {
        return this.withType(provider.getOrThrow(resourceKey));
    }

    public VillagerData withProfession(Holder<VillagerProfession> holder) {
        return new VillagerData(this.type, holder, this.level);
    }

    public VillagerData withProfession(HolderGetter.Provider provider, ResourceKey<VillagerProfession> resourceKey) {
        return this.withProfession(provider.getOrThrow(resourceKey));
    }

    public VillagerData withLevel(int n) {
        return new VillagerData(this.type, this.profession, n);
    }

    public static int getMinXpPerLevel(int n) {
        return VillagerData.canLevelUp(n) ? NEXT_LEVEL_XP_THRESHOLDS[n - 1] : 0;
    }

    public static int getMaxXpPerLevel(int n) {
        return VillagerData.canLevelUp(n) ? NEXT_LEVEL_XP_THRESHOLDS[n] : 0;
    }

    public static boolean canLevelUp(int n) {
        return n >= 1 && n < 5;
    }
}

