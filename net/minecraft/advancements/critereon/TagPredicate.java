/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagPredicate<T>(TagKey<T> tag, boolean expected) {
    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
        return RecordCodecBuilder.create(instance -> instance.group((App)TagKey.codec(resourceKey).fieldOf("id").forGetter(TagPredicate::tag), (App)Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)).apply((Applicative)instance, TagPredicate::new));
    }

    public static <T> TagPredicate<T> is(TagKey<T> tagKey) {
        return new TagPredicate<T>(tagKey, true);
    }

    public static <T> TagPredicate<T> isNot(TagKey<T> tagKey) {
        return new TagPredicate<T>(tagKey, false);
    }

    public boolean matches(Holder<T> holder) {
        return holder.is(this.tag) == this.expected;
    }
}

