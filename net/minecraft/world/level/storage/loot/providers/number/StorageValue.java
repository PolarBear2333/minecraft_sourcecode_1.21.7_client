/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record StorageValue(ResourceLocation storage, NbtPathArgument.NbtPath path) implements NumberProvider
{
    public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("storage").forGetter(StorageValue::storage), (App)NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(StorageValue::path)).apply((Applicative)instance, StorageValue::new));

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.STORAGE;
    }

    private Number getNumericTag(LootContext lootContext, Number number) {
        CompoundTag compoundTag = lootContext.getLevel().getServer().getCommandStorage().get(this.storage);
        try {
            Tag tag;
            List<Tag> list = this.path.get(compoundTag);
            if (list.size() == 1 && (tag = list.getFirst()) instanceof NumericTag) {
                NumericTag numericTag = (NumericTag)tag;
                return numericTag.box();
            }
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return number;
    }

    @Override
    public float getFloat(LootContext lootContext) {
        return this.getNumericTag(lootContext, Float.valueOf(0.0f)).floatValue();
    }

    @Override
    public int getInt(LootContext lootContext) {
        return this.getNumericTag(lootContext, 0).intValue();
    }
}

