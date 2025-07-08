/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;

public interface NbtProvider {
    @Nullable
    public Tag get(LootContext var1);

    public Set<ContextKey<?>> getReferencedContextParams();

    public LootNbtProviderType getType();
}

