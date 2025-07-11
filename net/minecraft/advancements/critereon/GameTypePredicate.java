/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.level.GameType;

public record GameTypePredicate(List<GameType> types) {
    public static final GameTypePredicate ANY = GameTypePredicate.of(GameType.values());
    public static final GameTypePredicate SURVIVAL_LIKE = GameTypePredicate.of(GameType.SURVIVAL, GameType.ADVENTURE);
    public static final Codec<GameTypePredicate> CODEC = GameType.CODEC.listOf().xmap(GameTypePredicate::new, GameTypePredicate::types);

    public static GameTypePredicate of(GameType ... gameTypeArray) {
        return new GameTypePredicate(Arrays.stream(gameTypeArray).toList());
    }

    public boolean matches(GameType gameType) {
        return this.types.contains(gameType);
    }
}

