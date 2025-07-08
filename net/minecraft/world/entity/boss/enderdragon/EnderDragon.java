/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class EnderDragon
extends Mob
implements Enemy {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
    private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = TargetingConditions.forCombat().range(64.0);
    private static final int GROWL_INTERVAL_MIN = 200;
    private static final int GROWL_INTERVAL_MAX = 400;
    private static final float SITTING_ALLOWED_DAMAGE_PERCENTAGE = 0.25f;
    private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
    private static final String DRAGON_PHASE_KEY = "DragonPhase";
    private static final int DEFAULT_DEATH_TIME = 0;
    public final DragonFlightHistory flightHistory = new DragonFlightHistory();
    private final EnderDragonPart[] subEntities;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart wing1;
    private final EnderDragonPart wing2;
    public float oFlapTime;
    public float flapTime;
    public boolean inWall;
    public int dragonDeathTime = 0;
    public float yRotA;
    @Nullable
    public EndCrystal nearestCrystal;
    @Nullable
    private EndDragonFight dragonFight;
    private BlockPos fightOrigin = BlockPos.ZERO;
    private final EnderDragonPhaseManager phaseManager;
    private int growlTime = 100;
    private float sittingDamageReceived;
    private final Node[] nodes = new Node[24];
    private final int[] nodeAdjacency = new int[24];
    private final BinaryHeap openSet = new BinaryHeap();

    public EnderDragon(EntityType<? extends EnderDragon> entityType, Level level) {
        super((EntityType<? extends Mob>)EntityType.ENDER_DRAGON, level);
        this.head = new EnderDragonPart(this, "head", 1.0f, 1.0f);
        this.neck = new EnderDragonPart(this, "neck", 3.0f, 3.0f);
        this.body = new EnderDragonPart(this, "body", 5.0f, 3.0f);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.wing1 = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.wing2 = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
        this.setHealth(this.getMaxHealth());
        this.noPhysics = true;
        this.phaseManager = new EnderDragonPhaseManager(this);
    }

    public void setDragonFight(EndDragonFight endDragonFight) {
        this.dragonFight = endDragonFight;
    }

    public void setFightOrigin(BlockPos blockPos) {
        this.fightOrigin = blockPos;
    }

    public BlockPos getFightOrigin() {
        return this.fightOrigin;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.CAMERA_DISTANCE, 16.0);
    }

    @Override
    public boolean isFlapping() {
        float f = Mth.cos(this.flapTime * ((float)Math.PI * 2));
        float f2 = Mth.cos(this.oFlapTime * ((float)Math.PI * 2));
        return f2 <= -0.3f && f >= -0.3f;
    }

    @Override
    public void onFlap() {
        if (this.level().isClientSide && !this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0f, 0.8f + this.random.nextFloat() * 0.3f, false);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
    }

    @Override
    public void aiStep() {
        float f;
        float f2;
        float f3;
        Vec3[] vec3Array;
        Object object;
        Object object2;
        this.processFlappingMovement();
        if (this.level().isClientSide) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5f, 0.8f + this.random.nextFloat() * 0.3f, false);
                this.growlTime = 200 + this.random.nextInt(200);
            }
        }
        if (this.dragonFight == null && (object2 = this.level()) instanceof ServerLevel && (object2 = ((ServerLevel)(object = (ServerLevel)object2)).getDragonFight()) != null && this.getUUID().equals(((EndDragonFight)object2).getDragonUUID())) {
            this.dragonFight = object2;
        }
        this.oFlapTime = this.flapTime;
        if (this.isDeadOrDying()) {
            float f4 = (this.random.nextFloat() - 0.5f) * 8.0f;
            float f5 = (this.random.nextFloat() - 0.5f) * 4.0f;
            float f6 = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f4, this.getY() + 2.0 + (double)f5, this.getZ() + (double)f6, 0.0, 0.0, 0.0);
            return;
        }
        this.checkCrystals();
        object = this.getDeltaMovement();
        float f7 = 0.2f / ((float)((Vec3)object).horizontalDistance() * 10.0f + 1.0f);
        this.flapTime = this.phaseManager.getCurrentPhase().isSitting() ? (this.flapTime += 0.1f) : (this.inWall ? (this.flapTime += f7 * 0.5f) : (this.flapTime += (f7 *= (float)Math.pow(2.0, ((Vec3)object).y))));
        this.setYRot(Mth.wrapDegrees(this.getYRot()));
        if (this.isNoAi()) {
            this.flapTime = 0.5f;
            return;
        }
        this.flightHistory.record(this.getY(), this.getYRot());
        Object object3 = this.level();
        if (!(object3 instanceof ServerLevel)) {
            this.interpolation.interpolate();
            this.phaseManager.getCurrentPhase().doClientTick();
        } else {
            Vec3 vec3;
            vec3Array = (Vec3[])object3;
            object3 = this.phaseManager.getCurrentPhase();
            object3.doServerTick((ServerLevel)vec3Array);
            if (this.phaseManager.getCurrentPhase() != object3) {
                object3 = this.phaseManager.getCurrentPhase();
                object3.doServerTick((ServerLevel)vec3Array);
            }
            if ((vec3 = object3.getFlyTargetLocation()) != null) {
                double d = vec3.x - this.getX();
                double d2 = vec3.y - this.getY();
                double d3 = vec3.z - this.getZ();
                double d4 = d * d + d2 * d2 + d3 * d3;
                float f8 = object3.getFlySpeed();
                double d5 = Math.sqrt(d * d + d3 * d3);
                if (d5 > 0.0) {
                    d2 = Mth.clamp(d2 / d5, (double)(-f8), (double)f8);
                }
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, d2 * 0.01, 0.0));
                this.setYRot(Mth.wrapDegrees(this.getYRot()));
                Vec3 vec32 = vec3.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                Vec3 vec33 = new Vec3(Mth.sin(this.getYRot() * ((float)Math.PI / 180)), this.getDeltaMovement().y, -Mth.cos(this.getYRot() * ((float)Math.PI / 180))).normalize();
                f3 = Math.max(((float)vec33.dot(vec32) + 0.5f) / 1.5f, 0.0f);
                if (Math.abs(d) > (double)1.0E-5f || Math.abs(d3) > (double)1.0E-5f) {
                    f2 = Mth.clamp(Mth.wrapDegrees(180.0f - (float)Mth.atan2(d, d3) * 57.295776f - this.getYRot()), -50.0f, 50.0f);
                    this.yRotA *= 0.8f;
                    this.yRotA += f2 * object3.getTurnSpeed();
                    this.setYRot(this.getYRot() + this.yRotA * 0.1f);
                }
                f2 = (float)(2.0 / (d4 + 1.0));
                f = 0.06f;
                this.moveRelative(0.06f * (f3 * f2 + (1.0f - f2)), new Vec3(0.0, 0.0, -1.0));
                if (this.inWall) {
                    this.move(MoverType.SELF, this.getDeltaMovement().scale(0.8f));
                } else {
                    this.move(MoverType.SELF, this.getDeltaMovement());
                }
                Vec3 vec34 = this.getDeltaMovement().normalize();
                double d6 = 0.8 + 0.15 * (vec34.dot(vec33) + 1.0) / 2.0;
                this.setDeltaMovement(this.getDeltaMovement().multiply(d6, 0.91f, d6));
            }
        }
        if (!this.level().isClientSide()) {
            this.applyEffectsFromBlocks();
        }
        this.yBodyRot = this.getYRot();
        vec3Array = new Vec3[this.subEntities.length];
        for (int i = 0; i < this.subEntities.length; ++i) {
            vec3Array[i] = new Vec3(this.subEntities[i].getX(), this.subEntities[i].getY(), this.subEntities[i].getZ());
        }
        float f9 = (float)(this.flightHistory.get(5).y() - this.flightHistory.get(10).y()) * 10.0f * ((float)Math.PI / 180);
        float f10 = Mth.cos(f9);
        float f11 = Mth.sin(f9);
        float f12 = this.getYRot() * ((float)Math.PI / 180);
        float f13 = Mth.sin(f12);
        float f14 = Mth.cos(f12);
        this.tickPart(this.body, f13 * 0.5f, 0.0, -f14 * 0.5f);
        this.tickPart(this.wing1, f14 * 4.5f, 2.0, f13 * 4.5f);
        this.tickPart(this.wing2, f14 * -4.5f, 2.0, f13 * -4.5f);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.hurtTime == 0) {
                this.knockBack(serverLevel, serverLevel.getEntities(this, this.wing1.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                this.knockBack(serverLevel, serverLevel.getEntities(this, this.wing2.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                this.hurt(serverLevel, serverLevel.getEntities(this, this.head.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                this.hurt(serverLevel, serverLevel.getEntities(this, this.neck.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            }
        }
        float f15 = Mth.sin(this.getYRot() * ((float)Math.PI / 180) - this.yRotA * 0.01f);
        float f16 = Mth.cos(this.getYRot() * ((float)Math.PI / 180) - this.yRotA * 0.01f);
        float f17 = this.getHeadYOffset();
        this.tickPart(this.head, f15 * 6.5f * f10, f17 + f11 * 6.5f, -f16 * 6.5f * f10);
        this.tickPart(this.neck, f15 * 5.5f * f10, f17 + f11 * 5.5f, -f16 * 5.5f * f10);
        DragonFlightHistory.Sample sample = this.flightHistory.get(5);
        for (int i = 0; i < 3; ++i) {
            EnderDragonPart enderDragonPart = null;
            if (i == 0) {
                enderDragonPart = this.tail1;
            }
            if (i == 1) {
                enderDragonPart = this.tail2;
            }
            if (i == 2) {
                enderDragonPart = this.tail3;
            }
            DragonFlightHistory.Sample sample2 = this.flightHistory.get(12 + i * 2);
            float f18 = this.getYRot() * ((float)Math.PI / 180) + this.rotWrap(sample2.yRot() - sample.yRot()) * ((float)Math.PI / 180);
            float f19 = Mth.sin(f18);
            f3 = Mth.cos(f18);
            f2 = 1.5f;
            f = (float)(i + 1) * 2.0f;
            this.tickPart(enderDragonPart, -(f13 * 1.5f + f19 * f) * f10, sample2.y() - sample.y() - (double)((f + 1.5f) * f11) + 1.5, (f14 * 1.5f + f3 * f) * f10);
        }
        Level level2 = this.level();
        if (level2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level2;
            this.inWall = this.checkWalls(serverLevel, this.head.getBoundingBox()) | this.checkWalls(serverLevel, this.neck.getBoundingBox()) | this.checkWalls(serverLevel, this.body.getBoundingBox());
            if (this.dragonFight != null) {
                this.dragonFight.updateDragon(this);
            }
        }
        for (int i = 0; i < this.subEntities.length; ++i) {
            this.subEntities[i].xo = vec3Array[i].x;
            this.subEntities[i].yo = vec3Array[i].y;
            this.subEntities[i].zo = vec3Array[i].z;
            this.subEntities[i].xOld = vec3Array[i].x;
            this.subEntities[i].yOld = vec3Array[i].y;
            this.subEntities[i].zOld = vec3Array[i].z;
        }
    }

    private void tickPart(EnderDragonPart enderDragonPart, double d, double d2, double d3) {
        enderDragonPart.setPos(this.getX() + d, this.getY() + d2, this.getZ() + d3);
    }

    private float getHeadYOffset() {
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            return -1.0f;
        }
        DragonFlightHistory.Sample sample = this.flightHistory.get(5);
        DragonFlightHistory.Sample sample2 = this.flightHistory.get(0);
        return (float)(sample.y() - sample2.y());
    }

    private void checkCrystals() {
        if (this.nearestCrystal != null) {
            if (this.nearestCrystal.isRemoved()) {
                this.nearestCrystal = null;
            } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0f);
            }
        }
        if (this.random.nextInt(10) == 0) {
            List<EndCrystal> list = this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0));
            EndCrystal endCrystal = null;
            double d = Double.MAX_VALUE;
            for (EndCrystal endCrystal2 : list) {
                double d2 = endCrystal2.distanceToSqr(this);
                if (!(d2 < d)) continue;
                d = d2;
                endCrystal = endCrystal2;
            }
            this.nearestCrystal = endCrystal;
        }
    }

    private void knockBack(ServerLevel serverLevel, List<Entity> list) {
        double d = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
        double d2 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;
        for (Entity entity : list) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity livingEntity = (LivingEntity)entity;
            double d3 = entity.getX() - d;
            double d4 = entity.getZ() - d2;
            double d5 = Math.max(d3 * d3 + d4 * d4, 0.1);
            entity.push(d3 / d5 * 4.0, 0.2f, d4 / d5 * 4.0);
            if (this.phaseManager.getCurrentPhase().isSitting() || livingEntity.getLastHurtByMobTimestamp() >= entity.tickCount - 2) continue;
            DamageSource damageSource = this.damageSources().mobAttack(this);
            entity.hurtServer(serverLevel, damageSource, 5.0f);
            EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
        }
    }

    private void hurt(ServerLevel serverLevel, List<Entity> list) {
        for (Entity entity : list) {
            if (!(entity instanceof LivingEntity)) continue;
            DamageSource damageSource = this.damageSources().mobAttack(this);
            entity.hurtServer(serverLevel, damageSource, 10.0f);
            EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
        }
    }

    private float rotWrap(double d) {
        return (float)Mth.wrapDegrees(d);
    }

    private boolean checkWalls(ServerLevel serverLevel, AABB aABB) {
        int n = Mth.floor(aABB.minX);
        int n2 = Mth.floor(aABB.minY);
        int n3 = Mth.floor(aABB.minZ);
        int n4 = Mth.floor(aABB.maxX);
        int n5 = Mth.floor(aABB.maxY);
        int n6 = Mth.floor(aABB.maxZ);
        boolean bl = false;
        boolean bl2 = false;
        for (int i = n; i <= n4; ++i) {
            for (int j = n2; j <= n5; ++j) {
                for (int k = n3; k <= n6; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    BlockState blockState = serverLevel.getBlockState(blockPos);
                    if (blockState.isAir() || blockState.is(BlockTags.DRAGON_TRANSPARENT)) continue;
                    if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || blockState.is(BlockTags.DRAGON_IMMUNE)) {
                        bl = true;
                        continue;
                    }
                    bl2 = serverLevel.removeBlock(blockPos, false) || bl2;
                }
            }
        }
        if (bl2) {
            BlockPos blockPos = new BlockPos(n + this.random.nextInt(n4 - n + 1), n2 + this.random.nextInt(n5 - n2 + 1), n3 + this.random.nextInt(n6 - n3 + 1));
            serverLevel.levelEvent(2008, blockPos, 0);
        }
        return bl;
    }

    public boolean hurt(ServerLevel serverLevel, EnderDragonPart enderDragonPart, DamageSource damageSource, float f) {
        if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        }
        f = this.phaseManager.getCurrentPhase().onHurt(damageSource, f);
        if (enderDragonPart != this.head) {
            f = f / 4.0f + Math.min(f, 1.0f);
        }
        if (f < 0.01f) {
            return false;
        }
        if (damageSource.getEntity() instanceof Player || damageSource.is(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
            float f2 = this.getHealth();
            this.reallyHurt(serverLevel, damageSource, f);
            if (this.isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                this.setHealth(1.0f);
                this.phaseManager.setPhase(EnderDragonPhase.DYING);
            }
            if (this.phaseManager.getCurrentPhase().isSitting()) {
                this.sittingDamageReceived = this.sittingDamageReceived + f2 - this.getHealth();
                if (this.sittingDamageReceived > 0.25f * this.getMaxHealth()) {
                    this.sittingDamageReceived = 0.0f;
                    this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                }
            }
        }
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return this.hurt(serverLevel, this.body, damageSource, f);
    }

    protected void reallyHurt(ServerLevel serverLevel, DamageSource damageSource, float f) {
        super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    public void kill(ServerLevel serverLevel) {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
            this.dragonFight.setDragonKilled(this);
        }
    }

    @Override
    protected void tickDeath() {
        Level level;
        Object object;
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
        }
        ++this.dragonDeathTime;
        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float f2 = (this.random.nextFloat() - 0.5f) * 4.0f;
            float f3 = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0 + (double)f2, this.getZ() + (double)f3, 0.0, 0.0, 0.0);
        }
        int n = 500;
        if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
            n = 12000;
        }
        if ((object = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                ExperienceOrb.award(serverLevel, this.position(), Mth.floor((float)n * 0.08f));
            }
            if (this.dragonDeathTime == 1 && !this.isSilent()) {
                serverLevel.globalLevelEvent(1028, this.blockPosition(), 0);
            }
        }
        Vec3 vec3 = new Vec3(0.0, 0.1f, 0.0);
        this.move(MoverType.SELF, vec3);
        for (EnderDragonPart enderDragonPart : this.subEntities) {
            enderDragonPart.setOldPosAndRot();
            enderDragonPart.setPos(enderDragonPart.position().add(vec3));
        }
        if (this.dragonDeathTime == 200 && (level = this.level()) instanceof ServerLevel) {
            object = (ServerLevel)level;
            if (((ServerLevel)object).getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                ExperienceOrb.award((ServerLevel)object, this.position(), Mth.floor((float)n * 0.2f));
            }
            if (this.dragonFight != null) {
                this.dragonFight.setDragonKilled(this);
            }
            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    public int findClosestNode() {
        if (this.nodes[0] == null) {
            for (int i = 0; i < 24; ++i) {
                int n;
                int n2;
                int n3 = 5;
                int n4 = i;
                if (i < 12) {
                    n2 = Mth.floor(60.0f * Mth.cos(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)n4)));
                    n = Mth.floor(60.0f * Mth.sin(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)n4)));
                } else if (i < 20) {
                    n2 = Mth.floor(40.0f * Mth.cos(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)(n4 -= 12))));
                    n = Mth.floor(40.0f * Mth.sin(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)n4)));
                    n3 += 10;
                } else {
                    n2 = Mth.floor(20.0f * Mth.cos(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)(n4 -= 20))));
                    n = Mth.floor(20.0f * Mth.sin(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)n4)));
                }
                int n5 = Math.max(73, this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(n2, 0, n)).getY() + n3);
                this.nodes[i] = new Node(n2, n5, n);
            }
            this.nodeAdjacency[0] = 6146;
            this.nodeAdjacency[1] = 8197;
            this.nodeAdjacency[2] = 8202;
            this.nodeAdjacency[3] = 16404;
            this.nodeAdjacency[4] = 32808;
            this.nodeAdjacency[5] = 32848;
            this.nodeAdjacency[6] = 65696;
            this.nodeAdjacency[7] = 131392;
            this.nodeAdjacency[8] = 131712;
            this.nodeAdjacency[9] = 263424;
            this.nodeAdjacency[10] = 526848;
            this.nodeAdjacency[11] = 525313;
            this.nodeAdjacency[12] = 1581057;
            this.nodeAdjacency[13] = 3166214;
            this.nodeAdjacency[14] = 2138120;
            this.nodeAdjacency[15] = 6373424;
            this.nodeAdjacency[16] = 4358208;
            this.nodeAdjacency[17] = 12910976;
            this.nodeAdjacency[18] = 9044480;
            this.nodeAdjacency[19] = 9706496;
            this.nodeAdjacency[20] = 15216640;
            this.nodeAdjacency[21] = 0xD0E000;
            this.nodeAdjacency[22] = 11763712;
            this.nodeAdjacency[23] = 0x7E0000;
        }
        return this.findClosestNode(this.getX(), this.getY(), this.getZ());
    }

    public int findClosestNode(double d, double d2, double d3) {
        float f = 10000.0f;
        int n = 0;
        Node node = new Node(Mth.floor(d), Mth.floor(d2), Mth.floor(d3));
        int n2 = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            n2 = 12;
        }
        for (int i = n2; i < 24; ++i) {
            float f2;
            if (this.nodes[i] == null || !((f2 = this.nodes[i].distanceToSqr(node)) < f)) continue;
            f = f2;
            n = i;
        }
        return n;
    }

    @Nullable
    public Path findPath(int n, int n2, @Nullable Node node) {
        Node node2;
        for (int i = 0; i < 24; ++i) {
            node2 = this.nodes[i];
            node2.closed = false;
            node2.f = 0.0f;
            node2.g = 0.0f;
            node2.h = 0.0f;
            node2.cameFrom = null;
            node2.heapIdx = -1;
        }
        Node node3 = this.nodes[n];
        node2 = this.nodes[n2];
        node3.g = 0.0f;
        node3.f = node3.h = node3.distanceTo(node2);
        this.openSet.clear();
        this.openSet.insert(node3);
        Node node4 = node3;
        int n3 = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            n3 = 12;
        }
        while (!this.openSet.isEmpty()) {
            int n4;
            Node node5 = this.openSet.pop();
            if (node5.equals(node2)) {
                if (node != null) {
                    node.cameFrom = node2;
                    node2 = node;
                }
                return this.reconstructPath(node3, node2);
            }
            if (node5.distanceTo(node2) < node4.distanceTo(node2)) {
                node4 = node5;
            }
            node5.closed = true;
            int n5 = 0;
            for (n4 = 0; n4 < 24; ++n4) {
                if (this.nodes[n4] != node5) continue;
                n5 = n4;
                break;
            }
            for (n4 = n3; n4 < 24; ++n4) {
                if ((this.nodeAdjacency[n5] & 1 << n4) <= 0) continue;
                Node node6 = this.nodes[n4];
                if (node6.closed) continue;
                float f = node5.g + node5.distanceTo(node6);
                if (node6.inOpenSet() && !(f < node6.g)) continue;
                node6.cameFrom = node5;
                node6.g = f;
                node6.h = node6.distanceTo(node2);
                if (node6.inOpenSet()) {
                    this.openSet.changeCost(node6, node6.g + node6.h);
                    continue;
                }
                node6.f = node6.g + node6.h;
                this.openSet.insert(node6);
            }
        }
        if (node4 == node3) {
            return null;
        }
        LOGGER.debug("Failed to find path from {} to {}", (Object)n, (Object)n2);
        if (node != null) {
            node.cameFrom = node4;
            node4 = node;
        }
        return this.reconstructPath(node3, node4);
    }

    private Path reconstructPath(Node node, Node node2) {
        ArrayList arrayList = Lists.newArrayList();
        Node node3 = node2;
        arrayList.add(0, node3);
        while (node3.cameFrom != null) {
            node3 = node3.cameFrom;
            arrayList.add(0, node3);
        }
        return new Path(arrayList, new BlockPos(node2.x, node2.y, node2.z), true);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt(DRAGON_PHASE_KEY, this.phaseManager.getCurrentPhase().getPhase().getId());
        valueOutput.putInt(DRAGON_DEATH_TIME_KEY, this.dragonDeathTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        valueInput.getInt(DRAGON_PHASE_KEY).ifPresent(n -> this.phaseManager.setPhase(EnderDragonPhase.getById(n)));
        this.dragonDeathTime = valueInput.getIntOr(DRAGON_DEATH_TIME_KEY, 0);
    }

    @Override
    public void checkDespawn() {
    }

    public EnderDragonPart[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public Vec3 getHeadLookVector(float f) {
        Vec3 vec3;
        DragonPhaseInstance dragonPhaseInstance = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> enderDragonPhase = dragonPhaseInstance.getPhase();
        if (enderDragonPhase == EnderDragonPhase.LANDING || enderDragonPhase == EnderDragonPhase.TAKEOFF) {
            BlockPos blockPos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
            float f2 = Math.max((float)Math.sqrt(blockPos.distToCenterSqr(this.position())) / 4.0f, 1.0f);
            float f3 = 6.0f / f2;
            float f4 = this.getXRot();
            float f5 = 1.5f;
            this.setXRot(-f3 * 1.5f * 5.0f);
            vec3 = this.getViewVector(f);
            this.setXRot(f4);
        } else if (dragonPhaseInstance.isSitting()) {
            float f6 = this.getXRot();
            float f7 = 1.5f;
            this.setXRot(-45.0f);
            vec3 = this.getViewVector(f);
            this.setXRot(f6);
        } else {
            vec3 = this.getViewVector(f);
        }
        return vec3;
    }

    public void onCrystalDestroyed(ServerLevel serverLevel, EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource) {
        Player player;
        Entity entity = damageSource.getEntity();
        Player player2 = entity instanceof Player ? (player = (Player)entity) : serverLevel.getNearestPlayer(CRYSTAL_DESTROY_TARGETING, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (endCrystal == this.nearestCrystal) {
            this.hurt(serverLevel, this.head, this.damageSources().explosion(endCrystal, player2), 10.0f);
        }
        this.phaseManager.getCurrentPhase().onCrystalDestroyed(endCrystal, blockPos, damageSource, player2);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_PHASE.equals(entityDataAccessor) && this.level().isClientSide) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    public EnderDragonPhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }

    @Override
    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return false;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        EnderDragonPart[] enderDragonPartArray = this.getSubEntities();
        for (int i = 0; i < enderDragonPartArray.length; ++i) {
            enderDragonPartArray[i].setId(i + clientboundAddEntityPacket.getId() + 1);
        }
    }

    @Override
    public boolean canAttack(LivingEntity livingEntity) {
        return livingEntity.canBeSeenAsEnemy();
    }

    @Override
    protected float sanitizeScale(float f) {
        return 1.0f;
    }
}

