/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.damagesource;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DeathMessageType;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
    public static final Codec<DamageType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("message_id").forGetter(DamageType::msgId), (App)DamageScaling.CODEC.fieldOf("scaling").forGetter(DamageType::scaling), (App)Codec.FLOAT.fieldOf("exhaustion").forGetter(DamageType::exhaustion), (App)DamageEffects.CODEC.optionalFieldOf("effects", (Object)DamageEffects.HURT).forGetter(DamageType::effects), (App)DeathMessageType.CODEC.optionalFieldOf("death_message_type", (Object)DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)).apply((Applicative)instance, DamageType::new));
    public static final Codec<Holder<DamageType>> CODEC = RegistryFixedCodec.create(Registries.DAMAGE_TYPE);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DamageType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.DAMAGE_TYPE);

    public DamageType(String string, DamageScaling damageScaling, float f) {
        this(string, damageScaling, f, DamageEffects.HURT, DeathMessageType.DEFAULT);
    }

    public DamageType(String string, DamageScaling damageScaling, float f, DamageEffects damageEffects) {
        this(string, damageScaling, f, damageEffects, DeathMessageType.DEFAULT);
    }

    public DamageType(String string, float f, DamageEffects damageEffects) {
        this(string, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, f, damageEffects);
    }

    public DamageType(String string, float f) {
        this(string, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, f);
    }
}

