/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.decoration;

import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Painting
extends HangingEntity {
    private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(Painting.class, EntityDataSerializers.PAINTING_VARIANT);
    public static final float DEPTH = 0.0625f;

    public Painting(EntityType<? extends Painting> entityType, Level level) {
        super((EntityType<? extends HangingEntity>)entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PAINTING_VARIANT_ID, VariantUtils.getAny(this.registryAccess(), Registries.PAINTING_VARIANT));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_PAINTING_VARIANT_ID.equals(entityDataAccessor)) {
            this.recalculateBoundingBox();
        }
    }

    private void setVariant(Holder<PaintingVariant> holder) {
        this.entityData.set(DATA_PAINTING_VARIANT_ID, holder);
    }

    public Holder<PaintingVariant> getVariant() {
        return this.entityData.get(DATA_PAINTING_VARIANT_ID);
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.PAINTING_VARIANT) {
            return Painting.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.PAINTING_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T t) {
        if (dataComponentType == DataComponents.PAINTING_VARIANT) {
            this.setVariant(Painting.castComponentValue(DataComponents.PAINTING_VARIANT, t));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, t);
    }

    public static Optional<Painting> create(Level level, BlockPos blockPos, Direction direction) {
        Painting painting = new Painting(level, blockPos);
        ArrayList<Holder> arrayList = new ArrayList<Holder>();
        level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(arrayList::add);
        if (arrayList.isEmpty()) {
            return Optional.empty();
        }
        painting.setDirection(direction);
        arrayList.removeIf(holder -> {
            painting.setVariant((Holder<PaintingVariant>)holder);
            return !painting.survives();
        });
        if (arrayList.isEmpty()) {
            return Optional.empty();
        }
        int n = arrayList.stream().mapToInt(Painting::variantArea).max().orElse(0);
        arrayList.removeIf(holder -> Painting.variantArea(holder) < n);
        Optional optional = Util.getRandomSafe(arrayList, painting.random);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        painting.setVariant((Holder)optional.get());
        painting.setDirection(direction);
        return Optional.of(painting);
    }

    private static int variantArea(Holder<PaintingVariant> holder) {
        return holder.value().area();
    }

    private Painting(Level level, BlockPos blockPos) {
        super((EntityType<? extends HangingEntity>)EntityType.PAINTING, level, blockPos);
    }

    public Painting(Level level, BlockPos blockPos, Direction direction, Holder<PaintingVariant> holder) {
        this(level, blockPos);
        this.setVariant(holder);
        this.setDirection(direction);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.store("facing", Direction.LEGACY_ID_CODEC_2D, this.getDirection());
        super.addAdditionalSaveData(valueOutput);
        VariantUtils.writeVariant(valueOutput, this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        Direction direction = valueInput.read("facing", Direction.LEGACY_ID_CODEC_2D).orElse(Direction.SOUTH);
        super.readAdditionalSaveData(valueInput);
        this.setDirection(direction);
        VariantUtils.readVariant(valueInput, Registries.PAINTING_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos blockPos, Direction direction) {
        float f = 0.46875f;
        Vec3 vec3 = Vec3.atCenterOf(blockPos).relative(direction, -0.46875);
        PaintingVariant paintingVariant = this.getVariant().value();
        double d = this.offsetForPaintingSize(paintingVariant.width());
        double d2 = this.offsetForPaintingSize(paintingVariant.height());
        Direction direction2 = direction.getCounterClockWise();
        Vec3 vec32 = vec3.relative(direction2, d).relative(Direction.UP, d2);
        Direction.Axis axis = direction.getAxis();
        double d3 = axis == Direction.Axis.X ? 0.0625 : (double)paintingVariant.width();
        double d4 = paintingVariant.height();
        double d5 = axis == Direction.Axis.Z ? 0.0625 : (double)paintingVariant.width();
        return AABB.ofSize(vec32, d3, d4, d5);
    }

    private double offsetForPaintingSize(int n) {
        return n % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void dropItem(ServerLevel serverLevel, @Nullable Entity entity) {
        Player player;
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        this.playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);
        if (entity instanceof Player && (player = (Player)entity).hasInfiniteMaterials()) {
            return;
        }
        this.spawnAtLocation(serverLevel, Items.PAINTING);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void snapTo(double d, double d2, double d3, float f, float f2) {
        this.setPos(d, d2, d3);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, this.getDirection().get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}

