/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EntityReference<StoredEntityType extends UniquelyIdentifyable> {
    private static final Codec<? extends EntityReference<?>> CODEC = UUIDUtil.CODEC.xmap(EntityReference::new, EntityReference::getUUID);
    private static final StreamCodec<ByteBuf, ? extends EntityReference<?>> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(EntityReference::new, EntityReference::getUUID);
    private Either<UUID, StoredEntityType> entity;

    public static <Type extends UniquelyIdentifyable> Codec<EntityReference<Type>> codec() {
        return CODEC;
    }

    public static <Type extends UniquelyIdentifyable> StreamCodec<ByteBuf, EntityReference<Type>> streamCodec() {
        return STREAM_CODEC;
    }

    public EntityReference(StoredEntityType StoredEntityType) {
        this.entity = Either.right(StoredEntityType);
    }

    public EntityReference(UUID uUID) {
        this.entity = Either.left((Object)uUID);
    }

    public UUID getUUID() {
        return (UUID)this.entity.map(uUID -> uUID, UniquelyIdentifyable::getUUID);
    }

    @Nullable
    public StoredEntityType getEntity(UUIDLookup<? super StoredEntityType> uUIDLookup, Class<StoredEntityType> clazz) {
        StoredEntityType StoredEntityType;
        Object object;
        Optional optional = this.entity.right();
        if (optional.isPresent()) {
            object = (UniquelyIdentifyable)optional.get();
            if (object.isRemoved()) {
                this.entity = Either.left((Object)object.getUUID());
            } else {
                return (StoredEntityType)object;
            }
        }
        if (((Optional)(object = this.entity.left())).isPresent() && (StoredEntityType = this.resolve((UniquelyIdentifyable)uUIDLookup.getEntity((UUID)((Optional)object).get()), clazz)) != null && !StoredEntityType.isRemoved()) {
            this.entity = Either.right(StoredEntityType);
            return StoredEntityType;
        }
        return null;
    }

    @Nullable
    private StoredEntityType resolve(@Nullable UniquelyIdentifyable uniquelyIdentifyable, Class<StoredEntityType> clazz) {
        if (uniquelyIdentifyable != null && clazz.isAssignableFrom(uniquelyIdentifyable.getClass())) {
            return (StoredEntityType)((UniquelyIdentifyable)clazz.cast(uniquelyIdentifyable));
        }
        return null;
    }

    public boolean matches(StoredEntityType StoredEntityType) {
        return this.getUUID().equals(StoredEntityType.getUUID());
    }

    public void store(ValueOutput valueOutput, String string) {
        valueOutput.store(string, UUIDUtil.CODEC, this.getUUID());
    }

    public static void store(@Nullable EntityReference<?> entityReference, ValueOutput valueOutput, String string) {
        if (entityReference != null) {
            entityReference.store(valueOutput, string);
        }
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> StoredEntityType get(@Nullable EntityReference<StoredEntityType> entityReference, UUIDLookup<? super StoredEntityType> uUIDLookup, Class<StoredEntityType> clazz) {
        return entityReference != null ? (StoredEntityType)entityReference.getEntity(uUIDLookup, clazz) : null;
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> EntityReference<StoredEntityType> read(ValueInput valueInput, String string) {
        return valueInput.read(string, EntityReference.codec()).orElse(null);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> EntityReference<StoredEntityType> readWithOldOwnerConversion(ValueInput valueInput, String string2, Level level) {
        Optional<UUID> optional = valueInput.read(string2, UUIDUtil.CODEC);
        if (optional.isPresent()) {
            return new EntityReference<StoredEntityType>(optional.get());
        }
        return valueInput.getString(string2).map(string -> OldUsersConverter.convertMobOwnerIfNecessary(level.getServer(), string)).map(EntityReference::new).orElse(null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof EntityReference)) return false;
        EntityReference entityReference = (EntityReference)object;
        if (!this.getUUID().equals(entityReference.getUUID())) return false;
        return true;
    }

    public int hashCode() {
        return this.getUUID().hashCode();
    }
}

