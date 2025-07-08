/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface Leashable {
    public static final String LEASH_TAG = "leash";
    public static final double LEASH_TOO_FAR_DIST = 12.0;
    public static final double LEASH_ELASTIC_DIST = 6.0;
    public static final double MAXIMUM_ALLOWED_LEASHED_DIST = 16.0;
    public static final Vec3 AXIS_SPECIFIC_ELASTICITY = new Vec3(0.8, 0.2, 0.8);
    public static final float SPRING_DAMPENING = 0.7f;
    public static final double TORSIONAL_ELASTICITY = 10.0;
    public static final double STIFFNESS = 0.11;
    public static final List<Vec3> ENTITY_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3(0.0, 0.5, 0.5));
    public static final List<Vec3> LEASHER_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3(0.0, 0.5, 0.0));
    public static final List<Vec3> SHARED_QUAD_ATTACHMENT_POINTS = ImmutableList.of((Object)new Vec3(-0.5, 0.5, 0.5), (Object)new Vec3(-0.5, 0.5, -0.5), (Object)new Vec3(0.5, 0.5, -0.5), (Object)new Vec3(0.5, 0.5, 0.5));

    @Nullable
    public LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canHaveALeashAttachedTo(Entity entity) {
        if (this == entity) {
            return false;
        }
        if (this.leashDistanceTo(entity) > this.leashSnapDistance()) {
            return false;
        }
        return this.canBeLeashed();
    }

    default public double leashDistanceTo(Entity entity) {
        return entity.getBoundingBox().getCenter().distanceTo(((Entity)((Object)this)).getBoundingBox().getCenter());
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setDelayedLeashHolderId(int n) {
        this.setLeashData(new LeashData(n));
        Leashable.dropLeash((Entity)((Object)this), false, false);
    }

    default public void readLeashData(ValueInput valueInput) {
        LeashData leashData = valueInput.read(LEASH_TAG, LeashData.CODEC).orElse(null);
        if (this.getLeashData() != null && leashData == null) {
            this.removeLeash();
        }
        this.setLeashData(leashData);
    }

    default public void writeLeashData(ValueOutput valueOutput, @Nullable LeashData leashData) {
        valueOutput.storeNullable(LEASH_TAG, LeashData.CODEC, leashData);
    }

    private static <E extends Entity> void restoreLeashFromSave(E e, LeashData leashData) {
        Object object;
        if (leashData.delayedLeashInfo != null && (object = e.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            object = leashData.delayedLeashInfo.left();
            Optional optional = leashData.delayedLeashInfo.right();
            if (((Optional)object).isPresent()) {
                Entity entity = serverLevel.getEntity((UUID)((Optional)object).get());
                if (entity != null) {
                    Leashable.setLeashedTo(e, entity, true);
                    return;
                }
            } else if (optional.isPresent()) {
                Leashable.setLeashedTo(e, LeashFenceKnotEntity.getOrCreateKnot(serverLevel, (BlockPos)optional.get()), true);
                return;
            }
            if (e.tickCount > 100) {
                e.spawnAtLocation(serverLevel, Items.LEAD);
                ((Leashable)((Object)e)).setLeashData(null);
            }
        }
    }

    default public void dropLeash() {
        Leashable.dropLeash((Entity)((Object)this), true, true);
    }

    default public void removeLeash() {
        Leashable.dropLeash((Entity)((Object)this), true, false);
    }

    default public void onLeashRemoved() {
    }

    private static <E extends Entity> void dropLeash(E e, boolean bl, boolean bl2) {
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData != null && leashData.leashHolder != null) {
            ((Leashable)((Object)e)).setLeashData(null);
            ((Leashable)((Object)e)).onLeashRemoved();
            Level level = e.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                if (bl2) {
                    e.spawnAtLocation(serverLevel, Items.LEAD);
                }
                if (bl) {
                    serverLevel.getChunkSource().broadcast(e, new ClientboundSetEntityLinkPacket(e, null));
                }
                leashData.leashHolder.notifyLeasheeRemoved((Leashable)((Object)e));
            }
        }
    }

    public static <E extends Entity> void tickLeash(ServerLevel serverLevel, E e) {
        Entity entity;
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData != null && leashData.delayedLeashInfo != null) {
            Leashable.restoreLeashFromSave(e, leashData);
        }
        if (leashData == null || leashData.leashHolder == null) {
            return;
        }
        if (!e.isAlive() || !leashData.leashHolder.isAlive()) {
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                ((Leashable)((Object)e)).dropLeash();
            } else {
                ((Leashable)((Object)e)).removeLeash();
            }
        }
        if ((entity = ((Leashable)((Object)e)).getLeashHolder()) != null && entity.level() == e.level()) {
            double d = ((Leashable)((Object)e)).leashDistanceTo(entity);
            ((Leashable)((Object)e)).whenLeashedTo(entity);
            if (d > ((Leashable)((Object)e)).leashSnapDistance()) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
                ((Leashable)((Object)e)).leashTooFarBehaviour();
            } else if (d > ((Leashable)((Object)e)).leashElasticDistance() - (double)entity.getBbWidth() - (double)e.getBbWidth() && ((Leashable)((Object)e)).checkElasticInteractions(entity, leashData)) {
                ((Leashable)((Object)e)).onElasticLeashPull();
            } else {
                ((Leashable)((Object)e)).closeRangeLeashBehaviour(entity);
            }
            e.setYRot((float)((double)e.getYRot() - leashData.angularMomentum));
            leashData.angularMomentum *= (double)Leashable.angularFriction(e);
        }
    }

    default public void onElasticLeashPull() {
        Entity entity = (Entity)((Object)this);
        entity.checkFallDistanceAccumulation();
    }

    default public double leashSnapDistance() {
        return 12.0;
    }

    default public double leashElasticDistance() {
        return 6.0;
    }

    public static <E extends Entity> float angularFriction(E e) {
        if (e.onGround()) {
            return e.level().getBlockState(e.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91f;
        }
        if (e.isInLiquid()) {
            return 0.8f;
        }
        return 0.91f;
    }

    default public void whenLeashedTo(Entity entity) {
        entity.notifyLeashHolder(this);
    }

    default public void leashTooFarBehaviour() {
        this.dropLeash();
    }

    default public void closeRangeLeashBehaviour(Entity entity) {
    }

    default public boolean checkElasticInteractions(Entity entity, LeashData leashData) {
        boolean bl = entity.supportQuadLeashAsHolder() && this.supportQuadLeash();
        List<Wrench> list = Leashable.computeElasticInteraction((Entity)((Object)this), entity, bl ? SHARED_QUAD_ATTACHMENT_POINTS : ENTITY_ATTACHMENT_POINT, bl ? SHARED_QUAD_ATTACHMENT_POINTS : LEASHER_ATTACHMENT_POINT);
        if (list.isEmpty()) {
            return false;
        }
        Wrench wrench = Wrench.accumulate(list).scale(bl ? 0.25 : 1.0);
        leashData.angularMomentum += 10.0 * wrench.torque();
        Vec3 vec3 = Leashable.getHolderMovement(entity).subtract(((Entity)((Object)this)).getKnownMovement());
        ((Entity)((Object)this)).addDeltaMovement(wrench.force().multiply(AXIS_SPECIFIC_ELASTICITY).add(vec3.scale(0.11)));
        return true;
    }

    private static Vec3 getHolderMovement(Entity entity) {
        Mob mob;
        if (entity instanceof Mob && (mob = (Mob)entity).isNoAi()) {
            return Vec3.ZERO;
        }
        return entity.getKnownMovement();
    }

    private static <E extends Entity> List<Wrench> computeElasticInteraction(E e, Entity entity, List<Vec3> list, List<Vec3> list2) {
        double d = ((Leashable)((Object)e)).leashElasticDistance();
        Vec3 vec3 = Leashable.getHolderMovement(e);
        float f = e.getYRot() * ((float)Math.PI / 180);
        Vec3 vec32 = new Vec3(e.getBbWidth(), e.getBbHeight(), e.getBbWidth());
        float f2 = entity.getYRot() * ((float)Math.PI / 180);
        Vec3 vec33 = new Vec3(entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth());
        ArrayList<Wrench> arrayList = new ArrayList<Wrench>();
        for (int i = 0; i < list.size(); ++i) {
            Vec3 vec34 = list.get(i).multiply(vec32).yRot(-f);
            Vec3 vec35 = e.position().add(vec34);
            Vec3 vec36 = list2.get(i).multiply(vec33).yRot(-f2);
            Vec3 vec37 = entity.position().add(vec36);
            Leashable.computeDampenedSpringInteraction(vec37, vec35, d, vec3, vec34).ifPresent(arrayList::add);
        }
        return arrayList;
    }

    private static Optional<Wrench> computeDampenedSpringInteraction(Vec3 vec3, Vec3 vec32, double d, Vec3 vec33, Vec3 vec34) {
        boolean bl;
        double d2 = vec32.distanceTo(vec3);
        if (d2 < d) {
            return Optional.empty();
        }
        Vec3 vec35 = vec3.subtract(vec32).normalize().scale(d2 - d);
        double d3 = Wrench.torqueFromForce(vec34, vec35);
        boolean bl2 = bl = vec33.dot(vec35) >= 0.0;
        if (bl) {
            vec35 = vec35.scale(0.3f);
        }
        return Optional.of(new Wrench(vec35, d3));
    }

    default public boolean supportQuadLeash() {
        return false;
    }

    default public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets((Entity)((Object)this), 0.0, 0.5, 0.5, 0.5);
    }

    public static Vec3[] createQuadLeashOffsets(Entity entity, double d, double d2, double d3, double d4) {
        float f = entity.getBbWidth();
        double d5 = d * (double)f;
        double d6 = d2 * (double)f;
        double d7 = d3 * (double)f;
        double d8 = d4 * (double)entity.getBbHeight();
        return new Vec3[]{new Vec3(-d7, d8, d6 + d5), new Vec3(-d7, d8, -d6 + d5), new Vec3(d7, d8, -d6 + d5), new Vec3(d7, d8, d6 + d5)};
    }

    default public Vec3 getLeashOffset(float f) {
        return this.getLeashOffset();
    }

    default public Vec3 getLeashOffset() {
        Entity entity = (Entity)((Object)this);
        return new Vec3(0.0, entity.getEyeHeight(), entity.getBbWidth() * 0.4f);
    }

    default public void setLeashedTo(Entity entity, boolean bl) {
        if (this == entity) {
            return;
        }
        Leashable.setLeashedTo((Entity)((Object)this), entity, bl);
    }

    private static <E extends Entity> void setLeashedTo(E e, Entity entity, boolean bl) {
        Level level;
        Object object;
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData == null) {
            leashData = new LeashData(entity);
            ((Leashable)((Object)e)).setLeashData(leashData);
        } else {
            object = leashData.leashHolder;
            leashData.setLeashHolder(entity);
            if (object != null && object != entity) {
                ((Entity)object).notifyLeasheeRemoved((Leashable)((Object)e));
            }
        }
        if (bl && (level = e.level()) instanceof ServerLevel) {
            object = (ServerLevel)level;
            ((ServerLevel)object).getChunkSource().broadcast(e, new ClientboundSetEntityLinkPacket(e, entity));
        }
        if (e.isPassenger()) {
            e.stopRiding();
        }
    }

    @Nullable
    default public Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    @Nullable
    private static <E extends Entity> Entity getLeashHolder(E e) {
        Entity entity;
        LeashData leashData = ((Leashable)((Object)e)).getLeashData();
        if (leashData == null) {
            return null;
        }
        if (leashData.delayedLeashHolderId != 0 && e.level().isClientSide && (entity = e.level().getEntity(leashData.delayedLeashHolderId)) instanceof Entity) {
            Entity entity2 = entity;
            leashData.setLeashHolder(entity2);
        }
        return leashData.leashHolder;
    }

    public static List<Leashable> leashableLeashedTo(Entity entity) {
        return Leashable.leashableInArea(entity, leashable -> leashable.getLeashHolder() == entity);
    }

    public static List<Leashable> leashableInArea(Entity entity, Predicate<Leashable> predicate) {
        return Leashable.leashableInArea(entity.level(), entity.getBoundingBox().getCenter(), predicate);
    }

    public static List<Leashable> leashableInArea(Level level, Vec3 vec3, Predicate<Leashable> predicate) {
        double d = 32.0;
        AABB aABB = AABB.ofSize(vec3, 32.0, 32.0, 32.0);
        return level.getEntitiesOfClass(Entity.class, aABB, entity -> {
            Leashable leashable;
            return entity instanceof Leashable && predicate.test(leashable = (Leashable)((Object)entity));
        }).stream().map(Leashable.class::cast).toList();
    }

    public static final class LeashData {
        public static final Codec<LeashData> CODEC = Codec.xor((Codec)UUIDUtil.CODEC.fieldOf("UUID").codec(), BlockPos.CODEC).xmap(LeashData::new, leashData -> {
            Entity entity = leashData.leashHolder;
            if (entity instanceof LeashFenceKnotEntity) {
                LeashFenceKnotEntity leashFenceKnotEntity = (LeashFenceKnotEntity)entity;
                return Either.right((Object)leashFenceKnotEntity.getPos());
            }
            if (leashData.leashHolder != null) {
                return Either.left((Object)leashData.leashHolder.getUUID());
            }
            return Objects.requireNonNull(leashData.delayedLeashInfo, "Invalid LeashData had no attachment");
        });
        int delayedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPos> delayedLeashInfo;
        public double angularMomentum;

        private LeashData(Either<UUID, BlockPos> either) {
            this.delayedLeashInfo = either;
        }

        LeashData(Entity entity) {
            this.leashHolder = entity;
        }

        LeashData(int n) {
            this.delayedLeashHolderId = n;
        }

        public void setLeashHolder(Entity entity) {
            this.leashHolder = entity;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }

    public record Wrench(Vec3 force, double torque) {
        static Wrench ZERO = new Wrench(Vec3.ZERO, 0.0);

        static double torqueFromForce(Vec3 vec3, Vec3 vec32) {
            return vec3.z * vec32.x - vec3.x * vec32.z;
        }

        static Wrench accumulate(List<Wrench> list) {
            if (list.isEmpty()) {
                return ZERO;
            }
            double d = 0.0;
            double d2 = 0.0;
            double d3 = 0.0;
            double d4 = 0.0;
            for (Wrench wrench : list) {
                Vec3 vec3 = wrench.force;
                d += vec3.x;
                d2 += vec3.y;
                d3 += vec3.z;
                d4 += wrench.torque;
            }
            return new Wrench(new Vec3(d, d2, d3), d4);
        }

        public Wrench scale(double d) {
            return new Wrench(this.force.scale(d), this.torque * d);
        }
    }
}

