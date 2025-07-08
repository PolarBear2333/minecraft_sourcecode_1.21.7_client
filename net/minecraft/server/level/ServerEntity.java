/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TOLERANCE_LEVEL_ROTATION = 1;
    private static final double TOLERANCE_LEVEL_POSITION = 7.62939453125E-6;
    public static final int FORCED_POS_UPDATE_PERIOD = 60;
    private static final int FORCED_TELEPORT_PERIOD = 400;
    private final ServerLevel level;
    private final Entity entity;
    private final int updateInterval;
    private final boolean trackDelta;
    private final Consumer<Packet<?>> broadcast;
    private final BiConsumer<Packet<?>, List<UUID>> broadcastWithIgnore;
    private final VecDeltaCodec positionCodec = new VecDeltaCodec();
    private byte lastSentYRot;
    private byte lastSentXRot;
    private byte lastSentYHeadRot;
    private Vec3 lastSentMovement;
    private int tickCount;
    private int teleportDelay;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean wasRiding;
    private boolean wasOnGround;
    @Nullable
    private List<SynchedEntityData.DataValue<?>> trackedDataValues;

    public ServerEntity(ServerLevel serverLevel, Entity entity, int n, boolean bl, Consumer<Packet<?>> consumer, BiConsumer<Packet<?>, List<UUID>> biConsumer) {
        this.level = serverLevel;
        this.broadcast = consumer;
        this.entity = entity;
        this.updateInterval = n;
        this.trackDelta = bl;
        this.broadcastWithIgnore = biConsumer;
        this.positionCodec.setBase(entity.trackingPosition());
        this.lastSentMovement = entity.getDeltaMovement();
        this.lastSentYRot = Mth.packDegrees(entity.getYRot());
        this.lastSentXRot = Mth.packDegrees(entity.getXRot());
        this.lastSentYHeadRot = Mth.packDegrees(entity.getYHeadRot());
        this.wasOnGround = entity.onGround();
        this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
    }

    public void sendChanges() {
        Object object;
        DataComponentGetter dataComponentGetter;
        Object object2;
        List<Entity> list = this.entity.getPassengers();
        if (!list.equals(this.lastPassengers)) {
            object2 = this.mountedOrDismounted(list).map(Entity::getUUID).toList();
            this.broadcastWithIgnore.accept(new ClientboundSetPassengersPacket(this.entity), (List<UUID>)object2);
            this.lastPassengers = list;
        }
        if ((dataComponentGetter = this.entity) instanceof ItemFrame) {
            object2 = (ItemFrame)dataComponentGetter;
            if (this.tickCount % 10 == 0) {
                MapId mapId;
                dataComponentGetter = ((ItemFrame)object2).getItem();
                if (((ItemStack)dataComponentGetter).getItem() instanceof MapItem && (object = MapItem.getSavedData(mapId = dataComponentGetter.get(DataComponents.MAP_ID), (Level)this.level)) != null) {
                    for (ServerPlayer object3 : this.level.players()) {
                        ((MapItemSavedData)object).tickCarriedBy(object3, (ItemStack)dataComponentGetter);
                        Packet<?> packet = ((MapItemSavedData)object).getUpdatePacket(mapId, object3);
                        if (packet == null) continue;
                        object3.connection.send(packet);
                    }
                }
                this.sendDirtyEntityData();
            }
        }
        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            boolean bl;
            byte by = Mth.packDegrees(this.entity.getYRot());
            byte by2 = Mth.packDegrees(this.entity.getXRot());
            boolean bl2 = bl = Math.abs(by - this.lastSentYRot) >= 1 || Math.abs(by2 - this.lastSentXRot) >= 1;
            if (this.entity.isPassenger()) {
                if (bl) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), by, by2, this.entity.onGround()));
                    this.lastSentYRot = by;
                    this.lastSentXRot = by2;
                }
                this.positionCodec.setBase(this.entity.trackingPosition());
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                MinecartBehavior minecartBehavior;
                Entity entity = this.entity;
                if (entity instanceof AbstractMinecart && (minecartBehavior = ((AbstractMinecart)(object = (AbstractMinecart)entity)).getBehavior()) instanceof NewMinecartBehavior) {
                    NewMinecartBehavior newMinecartBehavior = (NewMinecartBehavior)minecartBehavior;
                    this.handleMinecartPosRot(newMinecartBehavior, by, by2, bl);
                } else {
                    Vec3 vec3;
                    double d;
                    boolean bl3;
                    ++this.teleportDelay;
                    Vec3 vec32 = this.entity.trackingPosition();
                    boolean bl4 = this.positionCodec.delta(vec32).lengthSqr() >= 7.62939453125E-6;
                    Packet<ClientGamePacketListener> packet = null;
                    boolean bl5 = bl4 || this.tickCount % 60 == 0;
                    boolean bl6 = false;
                    boolean bl7 = false;
                    long l = this.positionCodec.encodeX(vec32);
                    long l2 = this.positionCodec.encodeY(vec32);
                    long l3 = this.positionCodec.encodeZ(vec32);
                    boolean bl8 = bl3 = l < -32768L || l > 32767L || l2 < -32768L || l2 > 32767L || l3 < -32768L || l3 > 32767L;
                    if (this.entity.getRequiresPrecisePosition() || bl3 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
                        this.wasOnGround = this.entity.onGround();
                        this.teleportDelay = 0;
                        packet = ClientboundEntityPositionSyncPacket.of(this.entity);
                        bl6 = true;
                        bl7 = true;
                    } else if (bl5 && bl || this.entity instanceof AbstractArrow) {
                        packet = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)l, (short)l2, (short)l3, by, by2, this.entity.onGround());
                        bl6 = true;
                        bl7 = true;
                    } else if (bl5) {
                        packet = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)l, (short)l2, (short)l3, this.entity.onGround());
                        bl6 = true;
                    } else if (bl) {
                        packet = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), by, by2, this.entity.onGround());
                        bl7 = true;
                    }
                    if ((this.entity.hasImpulse || this.trackDelta || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && ((d = (vec3 = this.entity.getDeltaMovement()).distanceToSqr(this.lastSentMovement)) > 1.0E-7 || d > 0.0 && vec3.lengthSqr() == 0.0)) {
                        this.lastSentMovement = vec3;
                        Entity entity2 = this.entity;
                        if (entity2 instanceof AbstractHurtingProjectile) {
                            AbstractHurtingProjectile abstractHurtingProjectile = (AbstractHurtingProjectile)entity2;
                            this.broadcast.accept(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)List.of(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement), new ClientboundProjectilePowerPacket(abstractHurtingProjectile.getId(), abstractHurtingProjectile.accelerationPower))));
                        } else {
                            this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
                        }
                    }
                    if (packet != null) {
                        this.broadcast.accept(packet);
                    }
                    this.sendDirtyEntityData();
                    if (bl6) {
                        this.positionCodec.setBase(vec32);
                    }
                    if (bl7) {
                        this.lastSentYRot = by;
                        this.lastSentXRot = by2;
                    }
                    this.wasRiding = false;
                }
            }
            byte by3 = Mth.packDegrees(this.entity.getYHeadRot());
            if (Math.abs(by3 - this.lastSentYHeadRot) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, by3));
                this.lastSentYHeadRot = by3;
            }
            this.entity.hasImpulse = false;
        }
        ++this.tickCount;
        if (this.entity.hurtMarked) {
            this.entity.hurtMarked = false;
            this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
        }
    }

    private Stream<Entity> mountedOrDismounted(List<Entity> list) {
        return Streams.concat((Stream[])new Stream[]{this.lastPassengers.stream().filter(entity -> !list.contains(entity)), list.stream().filter(entity -> !this.lastPassengers.contains(entity))});
    }

    private void handleMinecartPosRot(NewMinecartBehavior newMinecartBehavior, byte by, byte by2, boolean bl) {
        this.sendDirtyEntityData();
        if (newMinecartBehavior.lerpSteps.isEmpty()) {
            boolean bl2;
            Vec3 vec3 = this.entity.getDeltaMovement();
            double d = vec3.distanceToSqr(this.lastSentMovement);
            Vec3 vec32 = this.entity.trackingPosition();
            boolean bl3 = this.positionCodec.delta(vec32).lengthSqr() >= 7.62939453125E-6;
            boolean bl4 = bl2 = bl3 || this.tickCount % 60 == 0;
            if (bl2 || bl || d > 1.0E-7) {
                this.broadcast.accept(new ClientboundMoveMinecartPacket(this.entity.getId(), List.of(new NewMinecartBehavior.MinecartStep(this.entity.position(), this.entity.getDeltaMovement(), this.entity.getYRot(), this.entity.getXRot(), 1.0f))));
            }
        } else {
            this.broadcast.accept(new ClientboundMoveMinecartPacket(this.entity.getId(), List.copyOf(newMinecartBehavior.lerpSteps)));
            newMinecartBehavior.lerpSteps.clear();
        }
        this.lastSentYRot = by;
        this.lastSentXRot = by2;
        this.positionCodec.setBase(this.entity.position());
    }

    public void removePairing(ServerPlayer serverPlayer) {
        this.entity.stopSeenByPlayer(serverPlayer);
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void addPairing(ServerPlayer serverPlayer) {
        ArrayList<Packet<? super ClientGamePacketListener>> arrayList = new ArrayList<Packet<? super ClientGamePacketListener>>();
        this.sendPairingData(serverPlayer, arrayList::add);
        serverPlayer.connection.send(new ClientboundBundlePacket((Iterable<Packet<? super ClientGamePacketListener>>)arrayList));
        this.entity.startSeenByPlayer(serverPlayer);
    }

    public void sendPairingData(ServerPlayer serverPlayer, Consumer<Packet<ClientGamePacketListener>> consumer) {
        Object object;
        Object object2;
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket(this);
        consumer.accept(packet);
        if (this.trackedDataValues != null) {
            consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
        }
        if ((object2 = this.entity) instanceof LivingEntity && !(object2 = ((LivingEntity)(object = (LivingEntity)object2)).getAttributes().getSyncableAttributes()).isEmpty()) {
            consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), (Collection<AttributeInstance>)object2));
        }
        if ((object2 = this.entity) instanceof LivingEntity) {
            object = (LivingEntity)object2;
            object2 = Lists.newArrayList();
            for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                ItemStack itemStack = ((LivingEntity)object).getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                object2.add(Pair.of((Object)equipmentSlot, (Object)itemStack.copy()));
            }
            if (!object2.isEmpty()) {
                consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), (List<Pair<EquipmentSlot, ItemStack>>)object2));
            }
        }
        if (!this.entity.getPassengers().isEmpty()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity));
        }
        if (this.entity.isPassenger()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }
        if ((object2 = this.entity) instanceof Leashable && (object = (Leashable)object2).isLeashed()) {
            consumer.accept(new ClientboundSetEntityLinkPacket(this.entity, object.getLeashHolder()));
        }
    }

    public Vec3 getPositionBase() {
        return this.positionCodec.getBase();
    }

    public Vec3 getLastSentMovement() {
        return this.lastSentMovement;
    }

    public float getLastSentXRot() {
        return Mth.unpackDegrees(this.lastSentXRot);
    }

    public float getLastSentYRot() {
        return Mth.unpackDegrees(this.lastSentYRot);
    }

    public float getLastSentYHeadRot() {
        return Mth.unpackDegrees(this.lastSentYHeadRot);
    }

    private void sendDirtyEntityData() {
        SynchedEntityData synchedEntityData = this.entity.getEntityData();
        List<SynchedEntityData.DataValue<?>> list = synchedEntityData.packDirty();
        if (list != null) {
            this.trackedDataValues = synchedEntityData.getNonDefaultValues();
            this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
        }
        if (this.entity instanceof LivingEntity) {
            Set<AttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getAttributesToSync();
            if (!set.isEmpty()) {
                this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), set));
            }
            set.clear();
        }
    }

    private void broadcastAndSend(Packet<?> packet) {
        this.broadcast.accept(packet);
        if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(packet);
        }
    }
}

