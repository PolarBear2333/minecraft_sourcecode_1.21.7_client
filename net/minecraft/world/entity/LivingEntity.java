/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Objects
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.Contract
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.ElytraAnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable,
WaypointTransmitter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_ACTIVE_EFFECTS = "active_effects";
    public static final String TAG_ATTRIBUTES = "attributes";
    public static final String TAG_SLEEPING_POS = "sleeping_pos";
    public static final String TAG_EQUIPMENT = "equipment";
    public static final String TAG_BRAIN = "Brain";
    public static final String TAG_FALL_FLYING = "FallFlying";
    public static final String TAG_HURT_TIME = "HurtTime";
    public static final String TAG_DEATH_TIME = "DeathTime";
    public static final String TAG_HURT_BY_TIMESTAMP = "HurtByTimestamp";
    public static final String TAG_HEALTH = "Health";
    private static final ResourceLocation SPEED_MODIFIER_POWDER_SNOW_ID = ResourceLocation.withDefaultNamespace("powder_snow");
    private static final ResourceLocation SPRINTING_MODIFIER_ID = ResourceLocation.withDefaultNamespace("sprinting");
    private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(SPRINTING_MODIFIER_ID, 0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int EQUIPMENT_SLOT_OFFSET = 98;
    public static final int ARMOR_SLOT_OFFSET = 100;
    public static final int BODY_ARMOR_OFFSET = 105;
    public static final int SADDLE_OFFSET = 106;
    public static final int SWING_DURATION = 6;
    public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
    private static final int DAMAGE_SOURCE_TIMEOUT = 40;
    public static final double MIN_MOVEMENT_DISTANCE = 0.003;
    public static final double DEFAULT_BASE_GRAVITY = 0.08;
    public static final int DEATH_DURATION = 20;
    protected static final float INPUT_FRICTION = 0.98f;
    private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
    private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
    public static final float BASE_JUMP_POWER = 0.42f;
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 128.0;
    protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
    protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
    protected static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
    protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.PARTICLES);
    private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final int PARTICLE_FREQUENCY_WHEN_INVISIBLE = 15;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(0.2f);
    public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5f;
    public static final float DEFAULT_BABY_SCALE = 0.5f;
    public static final Predicate<LivingEntity> PLAYER_NOT_WEARING_DISGUISE_ITEM = livingEntity -> {
        if (!(livingEntity instanceof Player)) {
            return true;
        }
        Player player = (Player)livingEntity;
        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
        return !itemStack.is(ItemTags.GAZE_DISGUISE_EQUIPMENT);
    };
    private static final Dynamic<?> EMPTY_BRAIN = new Dynamic((DynamicOps)JavaOps.INSTANCE, Map.of("memories", Map.of()));
    private final AttributeMap attributes;
    private final CombatTracker combatTracker = new CombatTracker(this);
    private final Map<Holder<MobEffect>, MobEffectInstance> activeEffects = Maps.newHashMap();
    private final Map<EquipmentSlot, ItemStack> lastEquipmentItems = Util.makeEnumMap(EquipmentSlot.class, equipmentSlot -> ItemStack.EMPTY);
    public boolean swinging;
    private boolean discardFriction = false;
    public InteractionHand swingingArm;
    public int swingTime;
    public int removeArrowTime;
    public int removeStingerTime;
    public int hurtTime;
    public int hurtDuration;
    public int deathTime;
    public float oAttackAnim;
    public float attackAnim;
    protected int attackStrengthTicker;
    public final WalkAnimationState walkAnimation = new WalkAnimationState();
    public final int invulnerableDuration = 20;
    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    public final ElytraAnimationState elytraAnimationState = new ElytraAnimationState(this);
    @Nullable
    protected EntityReference<Player> lastHurtByPlayer;
    protected int lastHurtByPlayerMemoryTime;
    protected boolean dead;
    protected int noActionTime;
    protected float lastHurt;
    protected boolean jumping;
    public float xxa;
    public float yya;
    public float zza;
    protected InterpolationHandler interpolation = new InterpolationHandler(this);
    protected double lerpYHeadRot;
    protected int lerpHeadSteps;
    private boolean effectsDirty = true;
    @Nullable
    private EntityReference<LivingEntity> lastHurtByMob;
    private int lastHurtByMobTimestamp;
    @Nullable
    private LivingEntity lastHurtMob;
    private int lastHurtMobTimestamp;
    private float speed;
    private int noJumpDelay;
    private float absorptionAmount;
    protected ItemStack useItem = ItemStack.EMPTY;
    protected int useItemRemaining;
    protected int fallFlyTicks;
    private BlockPos lastPos;
    private Optional<BlockPos> lastClimbablePos = Optional.empty();
    @Nullable
    private DamageSource lastDamageSource;
    private long lastDamageStamp;
    protected int autoSpinAttackTicks;
    protected float autoSpinAttackDmg;
    @Nullable
    protected ItemStack autoSpinAttackItemStack;
    private float swimAmount;
    private float swimAmountO;
    protected Brain<?> brain;
    private boolean skipDropExperience;
    private final EnumMap<EquipmentSlot, Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>>> activeLocationDependentEnchantments = new EnumMap(EquipmentSlot.class);
    protected final EntityEquipment equipment;
    private Waypoint.Icon locatorBarIcon = new Waypoint.Icon();

    protected LivingEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.attributes = new AttributeMap(DefaultAttributes.getSupplier(entityType));
        this.setHealth(this.getMaxHealth());
        this.equipment = this.createEquipment();
        this.blocksBuilding = true;
        this.reapplyPosition();
        this.setYRot((float)(Math.random() * 6.2831854820251465));
        this.yHeadRot = this.getYRot();
        this.brain = this.makeBrain(EMPTY_BRAIN);
    }

    @Contract(pure=true)
    protected EntityEquipment createEquipment() {
        return new EntityEquipment();
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return this.brainProvider().makeBrain(dynamic);
    }

    @Override
    public void kill(ServerLevel serverLevel) {
        this.hurtServer(serverLevel, this.damageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canAttackType(EntityType<?> entityType) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
        builder.define(DATA_EFFECT_PARTICLES, List.of());
        builder.define(DATA_EFFECT_AMBIENCE_ID, false);
        builder.define(DATA_ARROW_COUNT_ID, 0);
        builder.define(DATA_STINGER_COUNT_ID, 0);
        builder.define(DATA_HEALTH_ID, Float.valueOf(1.0f));
        builder.define(SLEEPING_POS_ID, Optional.empty());
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return AttributeSupplier.builder().add(Attributes.MAX_HEALTH).add(Attributes.KNOCKBACK_RESISTANCE).add(Attributes.MOVEMENT_SPEED).add(Attributes.ARMOR).add(Attributes.ARMOR_TOUGHNESS).add(Attributes.MAX_ABSORPTION).add(Attributes.STEP_HEIGHT).add(Attributes.SCALE).add(Attributes.GRAVITY).add(Attributes.SAFE_FALL_DISTANCE).add(Attributes.FALL_DAMAGE_MULTIPLIER).add(Attributes.JUMP_STRENGTH).add(Attributes.OXYGEN_BONUS).add(Attributes.BURNING_TIME).add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE).add(Attributes.WATER_MOVEMENT_EFFICIENCY).add(Attributes.MOVEMENT_EFFICIENCY).add(Attributes.ATTACK_KNOCKBACK).add(Attributes.CAMERA_DISTANCE).add(Attributes.WAYPOINT_TRANSMIT_RANGE);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        Level level;
        if (!this.isInWater()) {
            this.updateInWaterStateAndDoWaterCurrentPushing();
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (bl && this.fallDistance > 0.0) {
                this.onChangedBlock(serverLevel, blockPos);
                double d2 = Math.max(0, Mth.floor(this.calculateFallPower(this.fallDistance)));
                if (d2 > 0.0 && !blockState.isAir()) {
                    double d3;
                    double d4 = this.getX();
                    double d5 = this.getY();
                    double d6 = this.getZ();
                    BlockPos blockPos2 = this.blockPosition();
                    if (blockPos.getX() != blockPos2.getX() || blockPos.getZ() != blockPos2.getZ()) {
                        d3 = d4 - (double)blockPos.getX() - 0.5;
                        double d7 = d6 - (double)blockPos.getZ() - 0.5;
                        double d8 = Math.max(Math.abs(d3), Math.abs(d7));
                        d4 = (double)blockPos.getX() + 0.5 + d3 / d8 * 0.5;
                        d6 = (double)blockPos.getZ() + 0.5 + d7 / d8 * 0.5;
                    }
                    d3 = Math.min((double)0.2f + d2 / 15.0, 2.5);
                    int n = (int)(150.0 * d3);
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), d4, d5, d6, n, 0.0, 0.0, 0.0, 0.15f);
                }
            }
        }
        super.checkFallDamage(d, bl, blockState, blockPos);
        if (bl) {
            this.lastClimbablePos = Optional.empty();
        }
    }

    public boolean canBreatheUnderwater() {
        return this.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
    }

    public float getSwimAmount(float f) {
        return Mth.lerp(f, this.swimAmountO, this.swimAmount);
    }

    public boolean hasLandedInLiquid() {
        return this.getDeltaMovement().y() < (double)1.0E-5f && this.isInLiquid();
    }

    @Override
    public void baseTick() {
        Level level;
        Object object;
        Object object2;
        this.oAttackAnim = this.attackAnim;
        if (this.firstTick) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
        }
        if ((object2 = this.level()) instanceof ServerLevel) {
            object = (ServerLevel)object2;
            EnchantmentHelper.tickEffects((ServerLevel)object, this);
        }
        super.baseTick();
        object = Profiler.get();
        object.push("livingEntityBaseTick");
        if (this.fireImmune() || this.level().isClientSide) {
            this.clearFire();
        }
        if (this.isAlive() && (level = this.level()) instanceof ServerLevel) {
            double d;
            double d2;
            object2 = (ServerLevel)level;
            boolean bl = this instanceof Player;
            if (this.isInWall()) {
                this.hurtServer((ServerLevel)object2, this.damageSources().inWall(), 1.0f);
            } else if (bl && !((Level)object2).getWorldBorder().isWithinBounds(this.getBoundingBox()) && (d2 = ((Level)object2).getWorldBorder().getDistanceToBorder(this) + ((Level)object2).getWorldBorder().getDamageSafeZone()) < 0.0 && (d = ((Level)object2).getWorldBorder().getDamagePerBlock()) > 0.0) {
                this.hurtServer((ServerLevel)object2, this.damageSources().outOfBorder(), Math.max(1, Mth.floor(-d2 * d)));
            }
            if (this.isEyeInFluid(FluidTags.WATER) && !((Level)object2).getBlockState(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
                boolean bl2;
                boolean bl3 = bl2 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && (!bl || !((Player)this).getAbilities().invulnerable);
                if (bl2) {
                    this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                    if (this.getAirSupply() == -20) {
                        this.setAirSupply(0);
                        ((ServerLevel)object2).broadcastEntityEvent(this, (byte)67);
                        this.hurtServer((ServerLevel)object2, this.damageSources().drown(), 2.0f);
                    }
                } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                    this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
                }
                if (this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }
            BlockPos blockPos = this.blockPosition();
            if (!Objects.equal((Object)this.lastPos, (Object)blockPos)) {
                this.lastPos = blockPos;
                this.onChangedBlock((ServerLevel)object2, blockPos);
            }
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
            --this.invulnerableTime;
        }
        if (this.isDeadOrDying() && this.level().shouldTickDeath(this)) {
            this.tickDeath();
        }
        if (this.lastHurtByPlayerMemoryTime > 0) {
            --this.lastHurtByPlayerMemoryTime;
        } else {
            this.lastHurtByPlayer = null;
        }
        if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
            this.lastHurtMob = null;
        }
        if ((object2 = this.getLastHurtByMob()) != null) {
            if (!((LivingEntity)object2).isAlive()) {
                this.setLastHurtByMob(null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob(null);
            }
        }
        this.tickEffects();
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        object.pop();
    }

    @Override
    protected float getBlockSpeedFactor() {
        return Mth.lerp((float)this.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), super.getBlockSpeedFactor(), 1.0f);
    }

    public float getLuck() {
        return 0.0f;
    }

    protected void removeFrost() {
        AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstance == null) {
            return;
        }
        if (attributeInstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
            attributeInstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_ID);
        }
    }

    protected void tryAddFrost() {
        int n;
        if (!this.getBlockStateOnLegacy().isAir() && (n = this.getTicksFrozen()) > 0) {
            AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance == null) {
                return;
            }
            float f = -0.05f * this.getPercentFrozen();
            attributeInstance.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_POWDER_SNOW_ID, f, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void onChangedBlock(ServerLevel serverLevel, BlockPos blockPos) {
        EnchantmentHelper.runLocationChangedEffects(serverLevel, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getAgeScale() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    public final float getScale() {
        AttributeMap attributeMap = this.getAttributes();
        if (attributeMap == null) {
            return 1.0f;
        }
        return this.sanitizeScale((float)attributeMap.getValue(Attributes.SCALE));
    }

    protected float sanitizeScale(float f) {
        return f;
    }

    public boolean isAffectedByFluids() {
        return true;
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot() {
        return !this.isBaby();
    }

    protected int decreaseAirSupply(int n) {
        AttributeInstance attributeInstance = this.getAttribute(Attributes.OXYGEN_BONUS);
        double d = attributeInstance != null ? attributeInstance.getValue() : 0.0;
        if (d > 0.0 && this.random.nextDouble() >= 1.0 / (d + 1.0)) {
            return n;
        }
        return n - 1;
    }

    protected int increaseAirSupply(int n) {
        return Math.min(n + 4, this.getMaxAirSupply());
    }

    public final int getExperienceReward(ServerLevel serverLevel, @Nullable Entity entity) {
        return EnchantmentHelper.processMobExperience(serverLevel, entity, this, this.getBaseExperienceReward(serverLevel));
    }

    protected int getBaseExperienceReward(ServerLevel serverLevel) {
        return 0;
    }

    protected boolean isAlwaysExperienceDropper() {
        return false;
    }

    @Nullable
    public LivingEntity getLastHurtByMob() {
        return EntityReference.get(this.lastHurtByMob, this.level(), LivingEntity.class);
    }

    @Nullable
    public Player getLastHurtByPlayer() {
        return EntityReference.get(this.lastHurtByPlayer, this.level(), Player.class);
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getLastHurtByMob();
    }

    public int getLastHurtByMobTimestamp() {
        return this.lastHurtByMobTimestamp;
    }

    public void setLastHurtByPlayer(Player player, int n) {
        this.setLastHurtByPlayer(new EntityReference<Player>(player), n);
    }

    public void setLastHurtByPlayer(UUID uUID, int n) {
        this.setLastHurtByPlayer(new EntityReference<Player>(uUID), n);
    }

    private void setLastHurtByPlayer(EntityReference<Player> entityReference, int n) {
        this.lastHurtByPlayer = entityReference;
        this.lastHurtByPlayerMemoryTime = n;
    }

    public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
        this.lastHurtByMob = livingEntity != null ? new EntityReference<LivingEntity>(livingEntity) : null;
        this.lastHurtByMobTimestamp = this.tickCount;
    }

    @Nullable
    public LivingEntity getLastHurtMob() {
        return this.lastHurtMob;
    }

    public int getLastHurtMobTimestamp() {
        return this.lastHurtMobTimestamp;
    }

    public void setLastHurtMob(Entity entity) {
        this.lastHurtMob = entity instanceof LivingEntity ? (LivingEntity)entity : null;
        this.lastHurtMobTimestamp = this.tickCount;
    }

    public int getNoActionTime() {
        return this.noActionTime;
    }

    public void setNoActionTime(int n) {
        this.noActionTime = n;
    }

    public boolean shouldDiscardFriction() {
        return this.discardFriction;
    }

    public void setDiscardFriction(boolean bl) {
        this.discardFriction = bl;
    }

    protected boolean doesEmitEquipEvent(EquipmentSlot equipmentSlot) {
        return true;
    }

    public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        if (this.level().isClientSide() || this.isSpectator()) {
            return;
        }
        if (ItemStack.isSameItemSameComponents(itemStack, itemStack2) || this.firstTick) {
            return;
        }
        Equippable equippable = itemStack2.get(DataComponents.EQUIPPABLE);
        if (!this.isSilent() && equippable != null && equipmentSlot == equippable.slot()) {
            this.level().playSeededSound(null, this.getX(), this.getY(), this.getZ(), this.getEquipSound(equipmentSlot, itemStack2, equippable), this.getSoundSource(), 1.0f, 1.0f, this.random.nextLong());
        }
        if (this.doesEmitEquipEvent(equipmentSlot)) {
            this.gameEvent(equippable != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
        }
    }

    protected Holder<SoundEvent> getEquipSound(EquipmentSlot equipmentSlot, ItemStack itemStack, Equippable equippable) {
        return equippable.equipSound();
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        Level level;
        if ((removalReason == Entity.RemovalReason.KILLED || removalReason == Entity.RemovalReason.DISCARDED) && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.triggerOnDeathMobEffects(serverLevel, removalReason);
        }
        super.remove(removalReason);
        this.brain.clearMemories();
    }

    @Override
    public void onRemoval(Entity.RemovalReason removalReason) {
        super.onRemoval(removalReason);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.getWaypointManager().untrackWaypoint(this);
        }
    }

    protected void triggerOnDeathMobEffects(ServerLevel serverLevel, Entity.RemovalReason removalReason) {
        for (MobEffectInstance mobEffectInstance : this.getActiveEffects()) {
            mobEffectInstance.onMobRemoved(serverLevel, this, removalReason);
        }
        this.activeEffects.clear();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putFloat(TAG_HEALTH, this.getHealth());
        valueOutput.putShort(TAG_HURT_TIME, (short)this.hurtTime);
        valueOutput.putInt(TAG_HURT_BY_TIMESTAMP, this.lastHurtByMobTimestamp);
        valueOutput.putShort(TAG_DEATH_TIME, (short)this.deathTime);
        valueOutput.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        valueOutput.store(TAG_ATTRIBUTES, AttributeInstance.Packed.LIST_CODEC, this.getAttributes().pack());
        if (!this.activeEffects.isEmpty()) {
            valueOutput.store(TAG_ACTIVE_EFFECTS, MobEffectInstance.CODEC.listOf(), List.copyOf(this.activeEffects.values()));
        }
        valueOutput.putBoolean(TAG_FALL_FLYING, this.isFallFlying());
        this.getSleepingPos().ifPresent(blockPos -> valueOutput.store(TAG_SLEEPING_POS, BlockPos.CODEC, blockPos));
        DataResult dataResult = this.brain.serializeStart(NbtOps.INSTANCE).map(tag -> new Dynamic((DynamicOps)NbtOps.INSTANCE, tag));
        dataResult.resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(dynamic -> valueOutput.store(TAG_BRAIN, Codec.PASSTHROUGH, dynamic));
        if (this.lastHurtByPlayer != null) {
            this.lastHurtByPlayer.store(valueOutput, "last_hurt_by_player");
            valueOutput.putInt("last_hurt_by_player_memory_time", this.lastHurtByPlayerMemoryTime);
        }
        if (this.lastHurtByMob != null) {
            this.lastHurtByMob.store(valueOutput, "last_hurt_by_mob");
            valueOutput.putInt("ticks_since_last_hurt_by_mob", this.tickCount - this.lastHurtByMobTimestamp);
        }
        if (!this.equipment.isEmpty()) {
            valueOutput.store(TAG_EQUIPMENT, EntityEquipment.CODEC, this.equipment);
        }
        if (this.locatorBarIcon.hasData()) {
            valueOutput.store("locator_bar_icon", Waypoint.Icon.CODEC, this.locatorBarIcon);
        }
    }

    @Nullable
    public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
        if (itemStack.isEmpty()) {
            return null;
        }
        if (this.level().isClientSide) {
            this.swing(InteractionHand.MAIN_HAND);
            return null;
        }
        ItemEntity itemEntity = this.createItemStackToDrop(itemStack, bl, bl2);
        if (itemEntity != null) {
            this.level().addFreshEntity(itemEntity);
        }
        return itemEntity;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.internalSetAbsorptionAmount(valueInput.getFloatOr("AbsorptionAmount", 0.0f));
        if (this.level() != null && !this.level().isClientSide) {
            valueInput.read(TAG_ATTRIBUTES, AttributeInstance.Packed.LIST_CODEC).ifPresent(this.getAttributes()::apply);
        }
        List list = valueInput.read(TAG_ACTIVE_EFFECTS, MobEffectInstance.CODEC.listOf()).orElse(List.of());
        this.activeEffects.clear();
        for (MobEffectInstance mobEffectInstance : list) {
            this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
        }
        this.setHealth(valueInput.getFloatOr(TAG_HEALTH, this.getMaxHealth()));
        this.hurtTime = valueInput.getShortOr(TAG_HURT_TIME, (short)0);
        this.deathTime = valueInput.getShortOr(TAG_DEATH_TIME, (short)0);
        this.lastHurtByMobTimestamp = valueInput.getIntOr(TAG_HURT_BY_TIMESTAMP, 0);
        valueInput.getString("Team").ifPresent(string -> {
            boolean bl;
            Scoreboard scoreboard = this.level().getScoreboard();
            PlayerTeam playerTeam = scoreboard.getPlayerTeam((String)string);
            boolean bl2 = bl = playerTeam != null && scoreboard.addPlayerToTeam(this.getStringUUID(), playerTeam);
            if (!bl) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", string);
            }
        });
        this.setSharedFlag(7, valueInput.getBooleanOr(TAG_FALL_FLYING, false));
        valueInput.read(TAG_SLEEPING_POS, BlockPos.CODEC).ifPresentOrElse(blockPos -> {
            this.setSleepingPos((BlockPos)blockPos);
            this.entityData.set(DATA_POSE, Pose.SLEEPING);
            if (!this.firstTick) {
                this.setPosToBed((BlockPos)blockPos);
            }
        }, this::clearSleepingPos);
        valueInput.read(TAG_BRAIN, Codec.PASSTHROUGH).ifPresent(dynamic -> {
            this.brain = this.makeBrain((Dynamic<?>)dynamic);
        });
        this.lastHurtByPlayer = EntityReference.read(valueInput, "last_hurt_by_player");
        this.lastHurtByPlayerMemoryTime = valueInput.getIntOr("last_hurt_by_player_memory_time", 0);
        this.lastHurtByMob = EntityReference.read(valueInput, "last_hurt_by_mob");
        this.lastHurtByMobTimestamp = valueInput.getIntOr("ticks_since_last_hurt_by_mob", 0) + this.tickCount;
        this.equipment.setAll(valueInput.read(TAG_EQUIPMENT, EntityEquipment.CODEC).orElseGet(EntityEquipment::new));
        this.locatorBarIcon = valueInput.read("locator_bar_icon", Waypoint.Icon.CODEC).orElseGet(Waypoint.Icon::new);
    }

    protected void tickEffects() {
        List<ParticleOptions> list = this.level();
        if (list instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)((Object)list);
            list = this.activeEffects.keySet().iterator();
            try {
                while (list.hasNext()) {
                    Holder<MobEffect> holder = list.next();
                    MobEffectInstance mobEffectInstance = this.activeEffects.get(holder);
                    if (!mobEffectInstance.tickServer(serverLevel, this, () -> this.onEffectUpdated(mobEffectInstance, true, null))) {
                        list.remove();
                        this.onEffectsRemoved(List.of(mobEffectInstance));
                        continue;
                    }
                    if (mobEffectInstance.getDuration() % 600 != 0) continue;
                    this.onEffectUpdated(mobEffectInstance, false, null);
                }
            }
            catch (ConcurrentModificationException concurrentModificationException) {
                // empty catch block
            }
            if (this.effectsDirty) {
                this.updateInvisibilityStatus();
                this.updateGlowingStatus();
                this.effectsDirty = false;
            }
        } else {
            for (MobEffectInstance mobEffectInstance : this.activeEffects.values()) {
                mobEffectInstance.tickClient();
            }
            list = this.entityData.get(DATA_EFFECT_PARTICLES);
            if (!list.isEmpty()) {
                int n;
                boolean bl = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
                int n2 = this.isInvisible() ? 15 : 4;
                int n3 = n = bl ? 5 : 1;
                if (this.random.nextInt(n2 * n) == 0) {
                    this.level().addParticle((ParticleOptions)Util.getRandom(list, this.random), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 1.0, 1.0, 1.0);
                }
            }
        }
    }

    protected void updateInvisibilityStatus() {
        if (this.activeEffects.isEmpty()) {
            this.removeEffectParticles();
            this.setInvisible(false);
            return;
        }
        this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
        this.updateSynchronizedMobEffectParticles();
    }

    private void updateSynchronizedMobEffectParticles() {
        List<ParticleOptions> list = this.activeEffects.values().stream().filter(MobEffectInstance::isVisible).map(MobEffectInstance::getParticleOptions).toList();
        this.entityData.set(DATA_EFFECT_PARTICLES, list);
        this.entityData.set(DATA_EFFECT_AMBIENCE_ID, LivingEntity.areAllEffectsAmbient(this.activeEffects.values()));
    }

    private void updateGlowingStatus() {
        boolean bl = this.isCurrentlyGlowing();
        if (this.getSharedFlag(6) != bl) {
            this.setSharedFlag(6, bl);
        }
    }

    public double getVisibilityPercent(@Nullable Entity entity) {
        double d = 1.0;
        if (this.isDiscrete()) {
            d *= 0.8;
        }
        if (this.isInvisible()) {
            float f = this.getArmorCoverPercentage();
            if (f < 0.1f) {
                f = 0.1f;
            }
            d *= 0.7 * (double)f;
        }
        if (entity != null) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
            EntityType<?> entityType = entity.getType();
            if (entityType == EntityType.SKELETON && itemStack.is(Items.SKELETON_SKULL) || entityType == EntityType.ZOMBIE && itemStack.is(Items.ZOMBIE_HEAD) || entityType == EntityType.PIGLIN && itemStack.is(Items.PIGLIN_HEAD) || entityType == EntityType.PIGLIN_BRUTE && itemStack.is(Items.PIGLIN_HEAD) || entityType == EntityType.CREEPER && itemStack.is(Items.CREEPER_HEAD)) {
                d *= 0.5;
            }
        }
        return d;
    }

    public boolean canAttack(LivingEntity livingEntity) {
        if (livingEntity instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return livingEntity.canBeSeenAsEnemy();
    }

    public boolean canBeSeenAsEnemy() {
        return !this.isInvulnerable() && this.canBeSeenByAnyone();
    }

    public boolean canBeSeenByAnyone() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> collection) {
        for (MobEffectInstance mobEffectInstance : collection) {
            if (!mobEffectInstance.isVisible() || mobEffectInstance.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void removeEffectParticles() {
        this.entityData.set(DATA_EFFECT_PARTICLES, List.of());
    }

    public boolean removeAllEffects() {
        if (this.level().isClientSide) {
            return false;
        }
        if (this.activeEffects.isEmpty()) {
            return false;
        }
        HashMap hashMap = Maps.newHashMap(this.activeEffects);
        this.activeEffects.clear();
        this.onEffectsRemoved(hashMap.values());
        return true;
    }

    public Collection<MobEffectInstance> getActiveEffects() {
        return this.activeEffects.values();
    }

    public Map<Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
        return this.activeEffects;
    }

    public boolean hasEffect(Holder<MobEffect> holder) {
        return this.activeEffects.containsKey(holder);
    }

    @Nullable
    public MobEffectInstance getEffect(Holder<MobEffect> holder) {
        return this.activeEffects.get(holder);
    }

    public float getEffectBlendFactor(Holder<MobEffect> holder, float f) {
        MobEffectInstance mobEffectInstance = this.getEffect(holder);
        if (mobEffectInstance != null) {
            return mobEffectInstance.getBlendFactor(this, f);
        }
        return 0.0f;
    }

    public final boolean addEffect(MobEffectInstance mobEffectInstance) {
        return this.addEffect(mobEffectInstance, null);
    }

    public boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (!this.canBeAffected(mobEffectInstance)) {
            return false;
        }
        MobEffectInstance mobEffectInstance2 = this.activeEffects.get(mobEffectInstance.getEffect());
        boolean bl = false;
        if (mobEffectInstance2 == null) {
            this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
            this.onEffectAdded(mobEffectInstance, entity);
            bl = true;
            mobEffectInstance.onEffectAdded(this);
        } else if (mobEffectInstance2.update(mobEffectInstance)) {
            this.onEffectUpdated(mobEffectInstance2, true, entity);
            bl = true;
        }
        mobEffectInstance.onEffectStarted(this);
        return bl;
    }

    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        if (this.getType().is(EntityTypeTags.IMMUNE_TO_INFESTED)) {
            return !mobEffectInstance.is(MobEffects.INFESTED);
        }
        if (this.getType().is(EntityTypeTags.IMMUNE_TO_OOZING)) {
            return !mobEffectInstance.is(MobEffects.OOZING);
        }
        if (this.getType().is(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
            return !mobEffectInstance.is(MobEffects.REGENERATION) && !mobEffectInstance.is(MobEffects.POISON);
        }
        return true;
    }

    public void forceAddEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (!this.canBeAffected(mobEffectInstance)) {
            return;
        }
        MobEffectInstance mobEffectInstance2 = this.activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
        if (mobEffectInstance2 == null) {
            this.onEffectAdded(mobEffectInstance, entity);
        } else {
            mobEffectInstance.copyBlendState(mobEffectInstance2);
            this.onEffectUpdated(mobEffectInstance, true, entity);
        }
    }

    public boolean isInvertedHealAndHarm() {
        return this.getType().is(EntityTypeTags.INVERTED_HEALING_AND_HARM);
    }

    @Nullable
    public final MobEffectInstance removeEffectNoUpdate(Holder<MobEffect> holder) {
        return this.activeEffects.remove(holder);
    }

    public boolean removeEffect(Holder<MobEffect> holder) {
        MobEffectInstance mobEffectInstance = this.removeEffectNoUpdate(holder);
        if (mobEffectInstance != null) {
            this.onEffectsRemoved(List.of(mobEffectInstance));
            return true;
        }
        return false;
    }

    protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        if (!this.level().isClientSide) {
            this.effectsDirty = true;
            mobEffectInstance.getEffect().value().addAttributeModifiers(this.getAttributes(), mobEffectInstance.getAmplifier());
            this.sendEffectToPassengers(mobEffectInstance);
        }
    }

    public void sendEffectToPassengers(MobEffectInstance mobEffectInstance) {
        for (Entity entity : this.getPassengers()) {
            if (!(entity instanceof ServerPlayer)) continue;
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, false));
        }
    }

    protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl, @Nullable Entity entity) {
        if (this.level().isClientSide) {
            return;
        }
        this.effectsDirty = true;
        if (bl) {
            MobEffect mobEffect = mobEffectInstance.getEffect().value();
            mobEffect.removeAttributeModifiers(this.getAttributes());
            mobEffect.addAttributeModifiers(this.getAttributes(), mobEffectInstance.getAmplifier());
            this.refreshDirtyAttributes();
        }
        this.sendEffectToPassengers(mobEffectInstance);
    }

    protected void onEffectsRemoved(Collection<MobEffectInstance> collection) {
        if (this.level().isClientSide) {
            return;
        }
        this.effectsDirty = true;
        for (MobEffectInstance mobEffectInstance : collection) {
            mobEffectInstance.getEffect().value().removeAttributeModifiers(this.getAttributes());
            for (Entity entity : this.getPassengers()) {
                if (!(entity instanceof ServerPlayer)) continue;
                ServerPlayer serverPlayer = (ServerPlayer)entity;
                serverPlayer.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
            }
        }
        this.refreshDirtyAttributes();
    }

    private void refreshDirtyAttributes() {
        Set<AttributeInstance> set = this.getAttributes().getAttributesToUpdate();
        for (AttributeInstance attributeInstance : set) {
            this.onAttributeUpdated(attributeInstance.getAttribute());
        }
        set.clear();
    }

    protected void onAttributeUpdated(Holder<Attribute> holder) {
        Object object;
        if (holder.is(Attributes.MAX_HEALTH)) {
            float f = this.getMaxHealth();
            if (this.getHealth() > f) {
                this.setHealth(f);
            }
        } else if (holder.is(Attributes.MAX_ABSORPTION)) {
            float f = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > f) {
                this.setAbsorptionAmount(f);
            }
        } else if (holder.is(Attributes.SCALE)) {
            this.refreshDimensions();
        } else if (holder.is(Attributes.WAYPOINT_TRANSMIT_RANGE) && (object = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            object = serverLevel.getWaypointManager();
            if (this.attributes.getValue(holder) > 0.0) {
                ((ServerWaypointManager)object).trackWaypoint(this);
            } else {
                ((ServerWaypointManager)object).untrackWaypoint(this);
            }
        }
    }

    public void heal(float f) {
        float f2 = this.getHealth();
        if (f2 > 0.0f) {
            this.setHealth(f2 + f);
        }
    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH_ID).floatValue();
    }

    public void setHealth(float f) {
        this.entityData.set(DATA_HEALTH_ID, Float.valueOf(Mth.clamp(f, 0.0f, this.getMaxHealth())));
    }

    public boolean isDeadOrDying() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        Entity entity;
        LivingEntity livingEntity;
        boolean bl;
        boolean bl2;
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        if (this.isDeadOrDying()) {
            return false;
        }
        if (damageSource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        this.noActionTime = 0;
        if (f < 0.0f) {
            f = 0.0f;
        }
        float f2 = f;
        float f3 = this.applyItemBlocking(serverLevel, damageSource, f);
        f -= f3;
        boolean bl3 = bl2 = f3 > 0.0f;
        if (damageSource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            f *= 5.0f;
        }
        if (damageSource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.hurtHelmet(damageSource, f);
            f *= 0.75f;
        }
        if (Float.isNaN(f) || Float.isInfinite(f)) {
            f = Float.MAX_VALUE;
        }
        boolean bl4 = true;
        if ((float)this.invulnerableTime > 10.0f && !damageSource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (f <= this.lastHurt) {
                return false;
            }
            this.actuallyHurt(serverLevel, damageSource, f - this.lastHurt);
            this.lastHurt = f;
            bl4 = false;
        } else {
            this.lastHurt = f;
            this.invulnerableTime = 20;
            this.actuallyHurt(serverLevel, damageSource, f);
            this.hurtTime = this.hurtDuration = 10;
        }
        this.resolveMobResponsibleForDamage(damageSource);
        this.resolvePlayerResponsibleForDamage(damageSource);
        if (bl4) {
            BlocksAttacks blocksAttacks = this.getUseItem().get(DataComponents.BLOCKS_ATTACKS);
            if (bl2 && blocksAttacks != null) {
                blocksAttacks.onBlocked(serverLevel, this);
            } else {
                serverLevel.broadcastDamageEvent(this, damageSource);
            }
            if (!(damageSource.is(DamageTypeTags.NO_IMPACT) || bl2 && !(f > 0.0f))) {
                this.markHurt();
            }
            if (!damageSource.is(DamageTypeTags.NO_KNOCKBACK)) {
                double d = 0.0;
                double d2 = 0.0;
                Entity entity2 = damageSource.getDirectEntity();
                if (entity2 instanceof Projectile) {
                    Projectile projectile = (Projectile)entity2;
                    entity2 = projectile.calculateHorizontalHurtKnockbackDirection(this, damageSource);
                    d = -entity2.leftDouble();
                    d2 = -entity2.rightDouble();
                } else if (damageSource.getSourcePosition() != null) {
                    d = damageSource.getSourcePosition().x() - this.getX();
                    d2 = damageSource.getSourcePosition().z() - this.getZ();
                }
                this.knockback(0.4f, d, d2);
                if (!bl2) {
                    this.indicateDamage(d, d2);
                }
            }
        }
        if (this.isDeadOrDying()) {
            if (!this.checkTotemDeathProtection(damageSource)) {
                if (bl4) {
                    this.makeSound(this.getDeathSound());
                    this.playSecondaryHurtSound(damageSource);
                }
                this.die(damageSource);
            }
        } else if (bl4) {
            this.playHurtSound(damageSource);
            this.playSecondaryHurtSound(damageSource);
        }
        boolean bl5 = bl = !bl2 || f > 0.0f;
        if (bl) {
            this.lastDamageSource = damageSource;
            this.lastDamageStamp = this.level().getGameTime();
            for (MobEffectInstance object2 : this.getActiveEffects()) {
                object2.onMobHurt(serverLevel, this, damageSource, f);
            }
        }
        if ((livingEntity = this) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(serverPlayer, damageSource, f2, f, bl2);
            if (f3 > 0.0f && f3 < 3.4028235E37f) {
                serverPlayer.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f3 * 10.0f));
            }
        }
        if ((entity = damageSource.getEntity()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverPlayer, this, damageSource, f2, f, bl2);
        }
        return bl;
    }

    public float applyItemBlocking(ServerLevel serverLevel, DamageSource damageSource, float f) {
        double d;
        Object object;
        AbstractArrow abstractArrow;
        BlocksAttacks blocksAttacks;
        ItemStack itemStack;
        block10: {
            block9: {
                if (f <= 0.0f) {
                    return 0.0f;
                }
                itemStack = this.getItemBlockingWith();
                if (itemStack == null) {
                    return 0.0f;
                }
                blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
                if (blocksAttacks == null) break block9;
                if (!blocksAttacks.bypassedBy().map(damageSource::is).orElse(false).booleanValue()) break block10;
            }
            return 0.0f;
        }
        Entity entity = damageSource.getDirectEntity();
        if (entity instanceof AbstractArrow && (abstractArrow = (AbstractArrow)entity).getPierceLevel() > 0) {
            return 0.0f;
        }
        Vec3 vec3 = damageSource.getSourcePosition();
        if (vec3 != null) {
            Vec3 vec32 = this.calculateViewVector(0.0f, this.getYHeadRot());
            object = vec3.subtract(this.position());
            object = new Vec3(((Vec3)object).x, 0.0, ((Vec3)object).z).normalize();
            d = Math.acos(((Vec3)object).dot(vec32));
        } else {
            d = 3.1415927410125732;
        }
        float f2 = blocksAttacks.resolveBlockedDamage(damageSource, f, d);
        blocksAttacks.hurtBlockingItem(this.level(), itemStack, this, this.getUsedItemHand(), f2);
        if (!damageSource.is(DamageTypeTags.IS_PROJECTILE) && (object = damageSource.getDirectEntity()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)object;
            this.blockUsingItem(serverLevel, livingEntity);
        }
        return f2;
    }

    private void playSecondaryHurtSound(DamageSource damageSource) {
        if (damageSource.is(DamageTypes.THORNS)) {
            SoundSource soundSource = this instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            this.level().playSound(null, this.position().x, this.position().y, this.position().z, SoundEvents.THORNS_HIT, soundSource);
        }
    }

    protected void resolveMobResponsibleForDamage(DamageSource damageSource) {
        Entity entity = damageSource.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (!(damageSource.is(DamageTypeTags.NO_ANGER) || damageSource.is(DamageTypes.WIND_CHARGE) && this.getType().is(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
                this.setLastHurtByMob(livingEntity);
            }
        }
    }

    @Nullable
    protected Player resolvePlayerResponsibleForDamage(DamageSource damageSource) {
        Wolf wolf;
        Entity entity = damageSource.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            this.setLastHurtByPlayer(player, 100);
        } else if (entity instanceof Wolf && (wolf = (Wolf)entity).isTame()) {
            if (wolf.getOwnerReference() != null) {
                this.setLastHurtByPlayer(wolf.getOwnerReference().getUUID(), 100);
            } else {
                this.lastHurtByPlayer = null;
                this.lastHurtByPlayerMemoryTime = 0;
            }
        }
        return EntityReference.get(this.lastHurtByPlayer, this.level(), Player.class);
    }

    protected void blockUsingItem(ServerLevel serverLevel, LivingEntity livingEntity) {
        livingEntity.blockedByItem(this);
    }

    protected void blockedByItem(LivingEntity livingEntity) {
        livingEntity.knockback(0.5, livingEntity.getX() - this.getX(), livingEntity.getZ() - this.getZ());
    }

    private boolean checkTotemDeathProtection(DamageSource damageSource) {
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack itemStack = null;
        DeathProtection deathProtection = null;
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack2 = this.getItemInHand(interactionHand);
            deathProtection = itemStack2.get(DataComponents.DEATH_PROTECTION);
            if (deathProtection == null) continue;
            itemStack = itemStack2.copy();
            itemStack2.shrink(1);
            break;
        }
        if (itemStack != null) {
            LivingEntity livingEntity = this;
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, itemStack);
                this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
            this.setHealth(1.0f);
            deathProtection.applyEffects(itemStack, this);
            this.level().broadcastEntityEvent(this, (byte)35);
        }
        return deathProtection != null;
    }

    @Nullable
    public DamageSource getLastDamageSource() {
        if (this.level().getGameTime() - this.lastDamageStamp > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource damageSource) {
        this.makeSound(this.getHurtSound(damageSource));
    }

    public void makeSound(@Nullable SoundEvent soundEvent) {
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    private void breakItem(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            Holder<SoundEvent> holder = itemStack.get(DataComponents.BREAK_SOUND);
            if (holder != null && !this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), holder.value(), this.getSoundSource(), 0.8f, 0.8f + this.level().random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles(itemStack, 5);
        }
    }

    public void die(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity entity = damageSource.getEntity();
        LivingEntity livingEntity = this.getKillCredit();
        if (livingEntity != null) {
            livingEntity.awardKillScore(this, damageSource);
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        if (!this.level().isClientSide && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getCombatTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getCombatTracker().recheckStatus();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (entity == null || entity.killedEntity(serverLevel, this)) {
                this.gameEvent(GameEvent.ENTITY_DIE);
                this.dropAllDeathLoot(serverLevel, damageSource);
                this.createWitherRose(livingEntity);
            }
            this.level().broadcastEntityEvent(this, (byte)3);
        }
        this.setPose(Pose.DYING);
    }

    protected void createWitherRose(@Nullable LivingEntity livingEntity) {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        boolean bl = false;
        if (livingEntity instanceof WitherBoss) {
            Object object;
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                object = this.blockPosition();
                BlockState blockState = Blocks.WITHER_ROSE.defaultBlockState();
                if (this.level().getBlockState((BlockPos)object).isAir() && blockState.canSurvive(this.level(), (BlockPos)object)) {
                    this.level().setBlock((BlockPos)object, blockState, 3);
                    bl = true;
                }
            }
            if (!bl) {
                object = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.level().addFreshEntity((Entity)object);
            }
        }
    }

    protected void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource) {
        boolean bl;
        boolean bl2 = bl = this.lastHurtByPlayerMemoryTime > 0;
        if (this.shouldDropLoot() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.dropFromLootTable(serverLevel, damageSource, bl);
            this.dropCustomDeathLoot(serverLevel, damageSource, bl);
        }
        this.dropEquipment(serverLevel);
        this.dropExperience(serverLevel, damageSource.getEntity());
    }

    protected void dropEquipment(ServerLevel serverLevel) {
    }

    protected void dropExperience(ServerLevel serverLevel, @Nullable Entity entity) {
        if (!this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerMemoryTime > 0 && this.shouldDropExperience() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
            ExperienceOrb.award(serverLevel, this.position(), this.getExperienceReward(serverLevel, entity));
        }
    }

    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
    }

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getKnockback(Entity entity, DamageSource damageSource) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return EnchantmentHelper.modifyKnockback(serverLevel, this.getWeaponItem(), entity, damageSource, f);
        }
        return f;
    }

    protected void dropFromLootTable(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        Optional<ResourceKey<LootTable>> optional = this.getLootTable();
        if (optional.isEmpty()) {
            return;
        }
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(optional.get());
        LootParams.Builder builder = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource).withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());
        Player player = this.getLastHurtByPlayer();
        if (bl && player != null) {
            builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
        }
        LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootParams, this.getLootTableSeed(), itemStack -> this.spawnAtLocation(serverLevel, (ItemStack)itemStack));
    }

    public boolean dropFromGiftLootTable(ServerLevel serverLevel, ResourceKey<LootTable> resourceKey, BiConsumer<ServerLevel, ItemStack> biConsumer) {
        return this.dropFromLootTable(serverLevel, resourceKey, builder -> builder.withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.GIFT), biConsumer);
    }

    protected void dropFromShearingLootTable(ServerLevel serverLevel, ResourceKey<LootTable> resourceKey, ItemStack itemStack, BiConsumer<ServerLevel, ItemStack> biConsumer) {
        this.dropFromLootTable(serverLevel, resourceKey, builder -> builder.withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.TOOL, itemStack).create(LootContextParamSets.SHEARING), biConsumer);
    }

    protected boolean dropFromLootTable(ServerLevel serverLevel, ResourceKey<LootTable> resourceKey, Function<LootParams.Builder, LootParams> function, BiConsumer<ServerLevel, ItemStack> biConsumer) {
        LootParams lootParams;
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams = function.apply(new LootParams.Builder(serverLevel)));
        if (!objectArrayList.isEmpty()) {
            objectArrayList.forEach(itemStack -> biConsumer.accept(serverLevel, (ItemStack)itemStack));
            return true;
        }
        return false;
    }

    public void knockback(double d, double d2, double d3) {
        if ((d *= 1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.hasImpulse = true;
        Vec3 vec3 = this.getDeltaMovement();
        while (d2 * d2 + d3 * d3 < (double)1.0E-5f) {
            d2 = (Math.random() - Math.random()) * 0.01;
            d3 = (Math.random() - Math.random()) * 0.01;
        }
        Vec3 vec32 = new Vec3(d2, 0.0, d3).normalize().scale(d);
        this.setDeltaMovement(vec3.x / 2.0 - vec32.x, this.onGround() ? Math.min(0.4, vec3.y / 2.0 + d) : vec3.y, vec3.z / 2.0 - vec32.z);
    }

    public void indicateDamage(double d, double d2) {
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GENERIC_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    private SoundEvent getFallDamageSound(int n) {
        return n > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void skipDropExperience() {
        this.skipDropExperience = true;
    }

    public boolean wasExperienceConsumed() {
        return this.skipDropExperience;
    }

    public float getHurtDir() {
        return 0.0f;
    }

    protected AABB getHitbox() {
        AABB aABB = this.getBoundingBox();
        Entity entity = this.getVehicle();
        if (entity != null) {
            Vec3 vec3 = entity.getPassengerRidingPosition(this);
            return aABB.setMinY(Math.max(vec3.y, aABB.minY));
        }
        return aABB;
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantments(EquipmentSlot equipmentSlot2) {
        return (Map)this.activeLocationDependentEnchantments.computeIfAbsent(equipmentSlot2, equipmentSlot -> new Reference2ObjectArrayMap());
    }

    public Fallsounds getFallSounds() {
        return new Fallsounds(SoundEvents.GENERIC_SMALL_FALL, SoundEvents.GENERIC_BIG_FALL);
    }

    public Optional<BlockPos> getLastClimbablePos() {
        return this.lastClimbablePos;
    }

    public boolean onClimbable() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos blockPos = this.blockPosition();
        BlockState blockState = this.getInBlockState();
        if (blockState.is(BlockTags.CLIMBABLE)) {
            this.lastClimbablePos = Optional.of(blockPos);
            return true;
        }
        if (blockState.getBlock() instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(blockPos, blockState)) {
            this.lastClimbablePos = Optional.of(blockPos);
            return true;
        }
        return false;
    }

    private boolean trapdoorUsableAsLadder(BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(TrapDoorBlock.OPEN).booleanValue()) {
            BlockState blockState2 = this.level().getBlockState(blockPos.below());
            return blockState2.is(Blocks.LADDER) && blockState2.getValue(LadderBlock.FACING) == blockState.getValue(TrapDoorBlock.FACING);
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    public boolean isLookingAtMe(LivingEntity livingEntity, double d, boolean bl, boolean bl2, double ... dArray) {
        Vec3 vec3 = livingEntity.getViewVector(1.0f).normalize();
        for (double d2 : dArray) {
            Vec3 vec32 = new Vec3(this.getX() - livingEntity.getX(), d2 - livingEntity.getEyeY(), this.getZ() - livingEntity.getZ());
            double d3 = vec32.length();
            vec32 = vec32.normalize();
            double d4 = vec3.dot(vec32);
            double d5 = bl ? d3 : 1.0;
            if (!(d4 > 1.0 - d / d5) || !livingEntity.hasLineOfSight(this, bl2 ? ClipContext.Block.VISUAL : ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, d2)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        return this.getComfortableFallDistance(0.0f);
    }

    protected final int getComfortableFallDistance(float f) {
        return Mth.floor(f + 3.0f);
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        boolean bl = super.causeFallDamage(d, f, damageSource);
        int n = this.calculateFallDamage(d, f);
        if (n > 0) {
            this.playSound(this.getFallDamageSound(n), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.hurt(damageSource, n);
            return true;
        }
        return bl;
    }

    protected int calculateFallDamage(double d, float f) {
        if (this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        double d2 = this.calculateFallPower(d);
        return Mth.floor(d2 * (double)f * this.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
    }

    private double calculateFallPower(double d) {
        return d + 1.0E-6 - this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
    }

    protected void playBlockFallSound() {
        if (this.isSilent()) {
            return;
        }
        int n = Mth.floor(this.getX());
        int n2 = Mth.floor(this.getY() - (double)0.2f);
        int n3 = Mth.floor(this.getZ());
        BlockState blockState = this.level().getBlockState(new BlockPos(n, n2, n3));
        if (!blockState.isAir()) {
            SoundType soundType = blockState.getSoundType();
            this.playSound(soundType.getFallSound(), soundType.getVolume() * 0.5f, soundType.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateHurt(float f) {
        this.hurtTime = this.hurtDuration = 10;
    }

    public int getArmorValue() {
        return Mth.floor(this.getAttributeValue(Attributes.ARMOR));
    }

    protected void hurtArmor(DamageSource damageSource, float f) {
    }

    protected void hurtHelmet(DamageSource damageSource, float f) {
    }

    protected void doHurtEquipment(DamageSource damageSource, float f, EquipmentSlot ... equipmentSlotArray) {
        if (f <= 0.0f) {
            return;
        }
        int n = (int)Math.max(1.0f, f / 4.0f);
        for (EquipmentSlot equipmentSlot : equipmentSlotArray) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || !equippable.damageOnHurt() || !itemStack.isDamageableItem() || !itemStack.canBeHurtBy(damageSource)) continue;
            itemStack.hurtAndBreak(n, this, equipmentSlot);
        }
    }

    protected float getDamageAfterArmorAbsorb(DamageSource damageSource, float f) {
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor(damageSource, f);
            f = CombatRules.getDamageAfterAbsorb(this, f, damageSource, this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        return f;
    }

    protected float getDamageAfterMagicAbsorb(DamageSource damageSource, float f) {
        float f2;
        int n;
        int n2;
        float f3;
        float f4;
        float f5;
        if (damageSource.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return f;
        }
        if (this.hasEffect(MobEffects.RESISTANCE) && !damageSource.is(DamageTypeTags.BYPASSES_RESISTANCE) && (f5 = (f4 = f) - (f = Math.max((f3 = f * (float)(n2 = 25 - (n = (this.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && f5 < 3.4028235E37f) {
            if (this instanceof ServerPlayer) {
                ((ServerPlayer)this).awardStat(Stats.DAMAGE_RESISTED, Math.round(f5 * 10.0f));
            } else if (damageSource.getEntity() instanceof ServerPlayer) {
                ((ServerPlayer)damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f5 * 10.0f));
            }
        }
        if (f <= 0.0f) {
            return 0.0f;
        }
        if (damageSource.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return f;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            f2 = EnchantmentHelper.getDamageProtection(serverLevel, this, damageSource);
        } else {
            f2 = 0.0f;
        }
        if (f2 > 0.0f) {
            f = CombatRules.getDamageAfterMagicAbsorb(f, f2);
        }
        return f;
    }

    protected void actuallyHurt(ServerLevel serverLevel, DamageSource damageSource, float f) {
        Entity entity;
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return;
        }
        f = this.getDamageAfterArmorAbsorb(damageSource, f);
        float f2 = f = this.getDamageAfterMagicAbsorb(damageSource, f);
        f = Math.max(f - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (f2 - f));
        float f3 = f2 - f;
        if (f3 > 0.0f && f3 < 3.4028235E37f && (entity = damageSource.getEntity()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f3 * 10.0f));
        }
        if (f == 0.0f) {
            return;
        }
        this.getCombatTracker().recordDamage(damageSource, f);
        this.setHealth(this.getHealth() - f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Nullable
    public LivingEntity getKillCredit() {
        if (this.lastHurtByPlayer != null) {
            return this.lastHurtByPlayer.getEntity(this.level(), Player.class);
        }
        if (this.lastHurtByMob != null) {
            return this.lastHurtByMob.getEntity(this.level(), LivingEntity.class);
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float)this.getAttributeValue(Attributes.MAX_ABSORPTION);
    }

    public final int getArrowCount() {
        return this.entityData.get(DATA_ARROW_COUNT_ID);
    }

    public final void setArrowCount(int n) {
        this.entityData.set(DATA_ARROW_COUNT_ID, n);
    }

    public final int getStingerCount() {
        return this.entityData.get(DATA_STINGER_COUNT_ID);
    }

    public final void setStingerCount(int n) {
        this.entityData.set(DATA_STINGER_COUNT_ID, n);
    }

    private int getCurrentSwingDuration() {
        if (MobEffectUtil.hasDigSpeed(this)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
        }
        if (this.hasEffect(MobEffects.MINING_FATIGUE)) {
            return 6 + (1 + this.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }
        return 6;
    }

    public void swing(InteractionHand interactionHand) {
        this.swing(interactionHand, false);
    }

    public void swing(InteractionHand interactionHand, boolean bl) {
        if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = interactionHand;
            if (this.level() instanceof ServerLevel) {
                ClientboundAnimatePacket clientboundAnimatePacket = new ClientboundAnimatePacket(this, interactionHand == InteractionHand.MAIN_HAND ? 0 : 3);
                ServerChunkCache serverChunkCache = ((ServerLevel)this.level()).getChunkSource();
                if (bl) {
                    serverChunkCache.broadcastAndSend(this, clientboundAnimatePacket);
                } else {
                    serverChunkCache.broadcast(this, clientboundAnimatePacket);
                }
            }
        }
    }

    @Override
    public void handleDamageEvent(DamageSource damageSource) {
        this.walkAnimation.setSpeed(1.5f);
        this.invulnerableTime = 20;
        this.hurtTime = this.hurtDuration = 10;
        SoundEvent soundEvent = this.getHurtSound(damageSource);
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.lastDamageSource = damageSource;
        this.lastDamageStamp = this.level().getGameTime();
    }

    @Override
    public void handleEntityEvent(byte by) {
        switch (by) {
            case 3: {
                SoundEvent soundEvent = this.getDeathSound();
                if (soundEvent != null) {
                    this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof Player) break;
                this.setHealth(0.0f);
                this.die(this.damageSources().generic());
                break;
            }
            case 46: {
                int n = 128;
                for (int i = 0; i < 128; ++i) {
                    double d = (double)i / 127.0;
                    float f = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float f2 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float f3 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double d2 = Mth.lerp(d, this.xo, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    double d3 = Mth.lerp(d, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
                    double d4 = Mth.lerp(d, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    this.level().addParticle(ParticleTypes.PORTAL, d2, d3, d4, f, f2, f3);
                }
                break;
            }
            case 47: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.FEET));
                break;
            }
            case 65: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.BODY));
                break;
            }
            case 68: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.SADDLE));
                break;
            }
            case 54: {
                HoneyBlock.showJumpParticles(this);
                break;
            }
            case 55: {
                this.swapHandItems();
                break;
            }
            case 60: {
                this.makePoofParticles();
                break;
            }
            case 67: {
                this.makeDrownParticles();
                break;
            }
            default: {
                super.handleEntityEvent(by);
            }
        }
    }

    public void makePoofParticles() {
        for (int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            double d3 = this.random.nextGaussian() * 0.02;
            double d4 = 10.0;
            this.level().addParticle(ParticleTypes.POOF, this.getRandomX(1.0) - d * 10.0, this.getRandomY() - d2 * 10.0, this.getRandomZ(1.0) - d3 * 10.0, d, d2, d3);
        }
    }

    private void makeDrownParticles() {
        Vec3 vec3 = this.getDeltaMovement();
        for (int i = 0; i < 8; ++i) {
            double d = this.random.triangle(0.0, 1.0);
            double d2 = this.random.triangle(0.0, 1.0);
            double d3 = this.random.triangle(0.0, 1.0);
            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d, this.getY() + d2, this.getZ() + d3, vec3.x, vec3.y, vec3.z);
        }
    }

    private void swapHandItems() {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
        this.setItemSlot(EquipmentSlot.OFFHAND, this.getItemBySlot(EquipmentSlot.MAINHAND));
        this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
    }

    @Override
    protected void onBelowWorld() {
        this.hurt(this.damageSources().fellOutOfWorld(), 4.0f);
    }

    protected void updateSwingTime() {
        int n = this.getCurrentSwingDuration();
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= n) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }
        this.attackAnim = (float)this.swingTime / (float)n;
    }

    @Nullable
    public AttributeInstance getAttribute(Holder<Attribute> holder) {
        return this.getAttributes().getInstance(holder);
    }

    public double getAttributeValue(Holder<Attribute> holder) {
        return this.getAttributes().getValue(holder);
    }

    public double getAttributeBaseValue(Holder<Attribute> holder) {
        return this.getAttributes().getBaseValue(holder);
    }

    public AttributeMap getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandItem() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffhandItem() {
        return this.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    public ItemStack getItemHeldByArm(HumanoidArm humanoidArm) {
        return this.getMainArm() == humanoidArm ? this.getMainHandItem() : this.getOffhandItem();
    }

    @Override
    @Nonnull
    public ItemStack getWeaponItem() {
        return this.getMainHandItem();
    }

    public boolean isHolding(Item item) {
        return this.isHolding((ItemStack itemStack) -> itemStack.is(item));
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandItem()) || predicate.test(this.getOffhandItem());
    }

    public ItemStack getItemInHand(InteractionHand interactionHand) {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            return this.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        if (interactionHand == InteractionHand.OFF_HAND) {
            return this.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)interactionHand));
    }

    public void setItemInHand(InteractionHand interactionHand, ItemStack itemStack) {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        } else if (interactionHand == InteractionHand.OFF_HAND) {
            this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)interactionHand));
        }
    }

    public boolean hasItemInSlot(EquipmentSlot equipmentSlot) {
        return !this.getItemBySlot(equipmentSlot).isEmpty();
    }

    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return true;
    }

    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return this.equipment.get(equipmentSlot);
    }

    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.onEquipItem(equipmentSlot, this.equipment.set(equipmentSlot, itemStack), itemStack);
    }

    public float getArmorCoverPercentage() {
        int n = 0;
        int n2 = 0;
        for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                ++n2;
            }
            ++n;
        }
        return n > 0 ? (float)n2 / (float)n : 0.0f;
    }

    @Override
    public void setSprinting(boolean bl) {
        super.setSprinting(bl);
        AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        attributeInstance.removeModifier(SPEED_MODIFIER_SPRINTING.id());
        if (bl) {
            attributeInstance.addTransientModifier(SPEED_MODIFIER_SPRINTING);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getVoicePitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isSleeping()) {
            super.push(entity);
        }
    }

    private void dismountVehicle(Entity entity) {
        Vec3 vec32;
        if (this.isRemoved()) {
            vec32 = this.position();
        } else if (entity.isRemoved() || this.level().getBlockState(entity.blockPosition()).is(BlockTags.PORTALS)) {
            boolean bl;
            double d = Math.max(this.getY(), entity.getY());
            vec32 = new Vec3(this.getX(), d, this.getZ());
            boolean bl2 = bl = this.getBbWidth() <= 4.0f && this.getBbHeight() <= 4.0f;
            if (bl) {
                double d2 = (double)this.getBbHeight() / 2.0;
                Vec3 vec33 = vec32.add(0.0, d2, 0.0);
                VoxelShape voxelShape = Shapes.create(AABB.ofSize(vec33, this.getBbWidth(), this.getBbHeight(), this.getBbWidth()));
                vec32 = this.level().findFreePosition(this, voxelShape, vec33, this.getBbWidth(), this.getBbHeight(), this.getBbWidth()).map(vec3 -> vec3.add(0.0, -d2, 0.0)).orElse(vec32);
            }
        } else {
            vec32 = entity.getDismountLocationForPassenger(this);
        }
        this.dismountTo(vec32.x, vec32.y, vec32.z);
    }

    @Override
    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpPower() {
        return this.getJumpPower(1.0f);
    }

    protected float getJumpPower(float f) {
        return (float)this.getAttributeValue(Attributes.JUMP_STRENGTH) * f * this.getBlockJumpFactor() + this.getJumpBoostPower();
    }

    public float getJumpBoostPower() {
        return this.hasEffect(MobEffects.JUMP_BOOST) ? 0.1f * ((float)this.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1.0f) : 0.0f;
    }

    @VisibleForTesting
    public void jumpFromGround() {
        float f = this.getJumpPower();
        if (f <= 1.0E-5f) {
            return;
        }
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, Math.max((double)f, vec3.y), vec3.z);
        if (this.isSprinting()) {
            float f2 = this.getYRot() * ((float)Math.PI / 180);
            this.addDeltaMovement(new Vec3((double)(-Mth.sin(f2)) * 0.2, 0.0, (double)Mth.cos(f2) * 0.2));
        }
        this.hasImpulse = true;
    }

    protected void goDownInWater() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04f, 0.0));
    }

    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04f, 0.0));
    }

    protected float getWaterSlowDown() {
        return 0.8f;
    }

    public boolean canStandOnFluid(FluidState fluidState) {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return this.getAttributeValue(Attributes.GRAVITY);
    }

    protected double getEffectiveGravity() {
        boolean bl;
        boolean bl2 = bl = this.getDeltaMovement().y <= 0.0;
        if (bl && this.hasEffect(MobEffects.SLOW_FALLING)) {
            return Math.min(this.getGravity(), 0.01);
        }
        return this.getGravity();
    }

    public void travel(Vec3 vec3) {
        FluidState fluidState = this.level().getFluidState(this.blockPosition());
        if ((this.isInWater() || this.isInLava()) && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
            this.travelInFluid(vec3);
        } else if (this.isFallFlying()) {
            this.travelFallFlying(vec3);
        } else {
            this.travelInAir(vec3);
        }
    }

    protected void travelFlying(Vec3 vec3, float f) {
        this.travelFlying(vec3, 0.02f, 0.02f, f);
    }

    protected void travelFlying(Vec3 vec3, float f, float f2, float f3) {
        if (this.isInWater()) {
            this.moveRelative(f, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8f));
        } else if (this.isInLava()) {
            this.moveRelative(f2, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            this.moveRelative(f3, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91f));
        }
    }

    private void travelInAir(Vec3 vec3) {
        BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
        float f = this.onGround() ? this.level().getBlockState(blockPos).getBlock().getFriction() : 1.0f;
        float f2 = f * 0.91f;
        Vec3 vec32 = this.handleRelativeFrictionAndCalculateMovement(vec3, f);
        double d = vec32.y;
        MobEffectInstance mobEffectInstance = this.getEffect(MobEffects.LEVITATION);
        d = mobEffectInstance != null ? (d += (0.05 * (double)(mobEffectInstance.getAmplifier() + 1) - vec32.y) * 0.2) : (!this.level().isClientSide || this.level().hasChunkAt(blockPos) ? (d -= this.getEffectiveGravity()) : (this.getY() > (double)this.level().getMinY() ? -0.1 : 0.0));
        if (this.shouldDiscardFriction()) {
            this.setDeltaMovement(vec32.x, d, vec32.z);
        } else {
            float f3 = this instanceof FlyingAnimal ? f2 : 0.98f;
            this.setDeltaMovement(vec32.x * (double)f2, d * (double)f3, vec32.z * (double)f2);
        }
    }

    private void travelInFluid(Vec3 vec3) {
        boolean bl = this.getDeltaMovement().y <= 0.0;
        double d = this.getY();
        double d2 = this.getEffectiveGravity();
        if (this.isInWater()) {
            float f = this.isSprinting() ? 0.9f : this.getWaterSlowDown();
            float f2 = 0.02f;
            float f3 = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
            if (!this.onGround()) {
                f3 *= 0.5f;
            }
            if (f3 > 0.0f) {
                f += (0.54600006f - f) * f3;
                f2 += (this.getSpeed() - f2) * f3;
            }
            if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                f = 0.96f;
            }
            this.moveRelative(f2, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 vec32 = this.getDeltaMovement();
            if (this.horizontalCollision && this.onClimbable()) {
                vec32 = new Vec3(vec32.x, 0.2, vec32.z);
            }
            vec32 = vec32.multiply(f, 0.8f, f);
            this.setDeltaMovement(this.getFluidFallingAdjustedMovement(d2, bl, vec32));
        } else {
            this.moveRelative(0.02f, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8f, 0.5));
                Vec3 vec33 = this.getFluidFallingAdjustedMovement(d2, bl, this.getDeltaMovement());
                this.setDeltaMovement(vec33);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            }
            if (d2 != 0.0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d2 / 4.0, 0.0));
            }
        }
        Vec3 vec34 = this.getDeltaMovement();
        if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + (double)0.6f - this.getY() + d, vec34.z)) {
            this.setDeltaMovement(vec34.x, 0.3f, vec34.z);
        }
    }

    private void travelFallFlying(Vec3 vec3) {
        if (this.onClimbable()) {
            this.travelInAir(vec3);
            this.stopFallFlying();
            return;
        }
        Vec3 vec32 = this.getDeltaMovement();
        double d = vec32.horizontalDistance();
        this.setDeltaMovement(this.updateFallFlyingMovement(vec32));
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level().isClientSide) {
            double d2 = this.getDeltaMovement().horizontalDistance();
            this.handleFallFlyingCollisions(d, d2);
        }
    }

    public void stopFallFlying() {
        this.setSharedFlag(7, true);
        this.setSharedFlag(7, false);
    }

    private Vec3 updateFallFlyingMovement(Vec3 vec3) {
        double d;
        Vec3 vec32 = this.getLookAngle();
        float f = this.getXRot() * ((float)Math.PI / 180);
        double d2 = Math.sqrt(vec32.x * vec32.x + vec32.z * vec32.z);
        double d3 = vec3.horizontalDistance();
        double d4 = this.getEffectiveGravity();
        double d5 = Mth.square(Math.cos(f));
        vec3 = vec3.add(0.0, d4 * (-1.0 + d5 * 0.75), 0.0);
        if (vec3.y < 0.0 && d2 > 0.0) {
            d = vec3.y * -0.1 * d5;
            vec3 = vec3.add(vec32.x * d / d2, d, vec32.z * d / d2);
        }
        if (f < 0.0f && d2 > 0.0) {
            d = d3 * (double)(-Mth.sin(f)) * 0.04;
            vec3 = vec3.add(-vec32.x * d / d2, d * 3.2, -vec32.z * d / d2);
        }
        if (d2 > 0.0) {
            vec3 = vec3.add((vec32.x / d2 * d3 - vec3.x) * 0.1, 0.0, (vec32.z / d2 * d3 - vec3.z) * 0.1);
        }
        return vec3.multiply(0.99f, 0.98f, 0.99f);
    }

    private void handleFallFlyingCollisions(double d, double d2) {
        double d3;
        float f;
        if (this.horizontalCollision && (f = (float)((d3 = d - d2) * 10.0 - 3.0)) > 0.0f) {
            this.playSound(this.getFallDamageSound((int)f), 1.0f, 1.0f);
            this.hurt(this.damageSources().flyIntoWall(), f);
        }
    }

    private void travelRidden(Player player, Vec3 vec3) {
        Vec3 vec32 = this.getRiddenInput(player, vec3);
        this.tickRidden(player, vec32);
        if (this.canSimulateMovement()) {
            this.setSpeed(this.getRiddenSpeed(player));
            this.travel(vec32);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    protected void tickRidden(Player player, Vec3 vec3) {
    }

    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        return vec3;
    }

    protected float getRiddenSpeed(Player player) {
        return this.getSpeed();
    }

    public void calculateEntityAnimation(boolean bl) {
        float f = (float)Mth.length(this.getX() - this.xo, bl ? this.getY() - this.yo : 0.0, this.getZ() - this.zo);
        if (this.isPassenger() || !this.isAlive()) {
            this.walkAnimation.stop();
        } else {
            this.updateWalkAnimation(f);
        }
    }

    protected void updateWalkAnimation(float f) {
        float f2 = Math.min(f * 4.0f, 1.0f);
        this.walkAnimation.update(f2, 0.4f, this.isBaby() ? 3.0f : 1.0f);
    }

    private Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 vec3, float f) {
        this.moveRelative(this.getFrictionInfluencedSpeed(f), vec3);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 vec32 = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.wasInPowderSnow && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
            vec32 = new Vec3(vec32.x, 0.2, vec32.z);
        }
        return vec32;
    }

    public Vec3 getFluidFallingAdjustedMovement(double d, boolean bl, Vec3 vec3) {
        if (d != 0.0 && !this.isSprinting()) {
            double d2 = bl && Math.abs(vec3.y - 0.005) >= 0.003 && Math.abs(vec3.y - d / 16.0) < 0.003 ? -0.003 : vec3.y - d / 16.0;
            return new Vec3(vec3.x, d2, vec3.z);
        }
        return vec3;
    }

    private Vec3 handleOnClimbable(Vec3 vec3) {
        if (this.onClimbable()) {
            this.resetFallDistance();
            float f = 0.15f;
            double d = Mth.clamp(vec3.x, (double)-0.15f, (double)0.15f);
            double d2 = Mth.clamp(vec3.z, (double)-0.15f, (double)0.15f);
            double d3 = Math.max(vec3.y, (double)-0.15f);
            if (d3 < 0.0 && !this.getInBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
                d3 = 0.0;
            }
            vec3 = new Vec3(d, d3, d2);
        }
        return vec3;
    }

    private float getFrictionInfluencedSpeed(float f) {
        if (this.onGround()) {
            return this.getSpeed() * (0.21600002f / (f * f * f));
        }
        return this.getFlyingSpeed();
    }

    protected float getFlyingSpeed() {
        return this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1f : 0.02f;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float f) {
        this.speed = f;
    }

    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        this.setLastHurtMob(entity);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.updatingUsingItem();
        this.updateSwimAmount();
        if (!this.level().isClientSide) {
            int n;
            int n2 = this.getArrowCount();
            if (n2 > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - n2);
                }
                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(n2 - 1);
                }
            }
            if ((n = this.getStingerCount()) > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - n);
                }
                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(n - 1);
                }
            }
            this.detectEquipmentUpdates();
            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }
            if (this.isSleeping() && !this.checkBedExists()) {
                this.stopSleeping();
            }
        }
        if (!this.isRemoved()) {
            this.aiStep();
        }
        double d = this.getX() - this.xo;
        double d2 = this.getZ() - this.zo;
        float f = (float)(d * d + d2 * d2);
        float f2 = this.yBodyRot;
        if (f > 0.0025000002f) {
            float f3 = (float)Mth.atan2(d2, d) * 57.295776f - 90.0f;
            float f4 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - f3);
            f2 = 95.0f < f4 && f4 < 265.0f ? f3 - 180.0f : f3;
        }
        if (this.attackAnim > 0.0f) {
            f2 = this.getYRot();
        }
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("headTurn");
        this.tickHeadTurn(f2);
        profilerFiller.pop();
        profilerFiller.push("rangeChecks");
        while (this.getYRot() - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (this.getYRot() - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        while (this.yBodyRot - this.yBodyRotO < -180.0f) {
            this.yBodyRotO -= 360.0f;
        }
        while (this.yBodyRot - this.yBodyRotO >= 180.0f) {
            this.yBodyRotO += 360.0f;
        }
        while (this.getXRot() - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (this.getXRot() - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (this.yHeadRot - this.yHeadRotO < -180.0f) {
            this.yHeadRotO -= 360.0f;
        }
        while (this.yHeadRot - this.yHeadRotO >= 180.0f) {
            this.yHeadRotO += 360.0f;
        }
        profilerFiller.pop();
        this.fallFlyTicks = this.isFallFlying() ? ++this.fallFlyTicks : 0;
        if (this.isSleeping()) {
            this.setXRot(0.0f);
        }
        this.refreshDirtyAttributes();
        this.elytraAnimationState.tick();
    }

    private void detectEquipmentUpdates() {
        Map<EquipmentSlot, ItemStack> map = this.collectEquipmentChanges();
        if (map != null) {
            this.handleHandSwap(map);
            if (!map.isEmpty()) {
                this.handleEquipmentChanges(map);
            }
        }
    }

    @Nullable
    private Map<EquipmentSlot, ItemStack> collectEquipmentChanges() {
        Object object;
        ItemStack itemStack;
        Object object2;
        Map map = null;
        for (EquipmentSlot object3 : EquipmentSlot.VALUES) {
            object2 = this.lastEquipmentItems.get(object3);
            if (!this.equipmentHasChanged((ItemStack)object2, itemStack = this.getItemBySlot(object3))) continue;
            if (map == null) {
                map = Maps.newEnumMap(EquipmentSlot.class);
            }
            map.put(object3, itemStack);
            object = this.getAttributes();
            if (((ItemStack)object2).isEmpty()) continue;
            this.stopLocationBasedEffects((ItemStack)object2, object3, (AttributeMap)object);
        }
        if (map != null) {
            for (Map.Entry entry : map.entrySet()) {
                object2 = (EquipmentSlot)entry.getKey();
                itemStack = (ItemStack)entry.getValue();
                if (itemStack.isEmpty() || itemStack.isBroken()) continue;
                itemStack.forEachModifier((EquipmentSlot)object2, (holder, attributeModifier) -> {
                    AttributeInstance attributeInstance = this.attributes.getInstance((Holder<Attribute>)holder);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(attributeModifier.id());
                        attributeInstance.addTransientModifier((AttributeModifier)attributeModifier);
                    }
                });
                Level level = this.level();
                if (!(level instanceof ServerLevel)) continue;
                object = (ServerLevel)level;
                EnchantmentHelper.runLocationChangedEffects((ServerLevel)object, itemStack, this, (EquipmentSlot)object2);
            }
        }
        return map;
    }

    public boolean equipmentHasChanged(ItemStack itemStack, ItemStack itemStack2) {
        return !ItemStack.matches(itemStack2, itemStack);
    }

    private void handleHandSwap(Map<EquipmentSlot, ItemStack> map) {
        ItemStack itemStack = map.get(EquipmentSlot.MAINHAND);
        ItemStack itemStack2 = map.get(EquipmentSlot.OFFHAND);
        if (itemStack != null && itemStack2 != null && ItemStack.matches(itemStack, this.lastEquipmentItems.get(EquipmentSlot.OFFHAND)) && ItemStack.matches(itemStack2, this.lastEquipmentItems.get(EquipmentSlot.MAINHAND))) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundEntityEventPacket(this, 55));
            map.remove(EquipmentSlot.MAINHAND);
            map.remove(EquipmentSlot.OFFHAND);
            this.lastEquipmentItems.put(EquipmentSlot.MAINHAND, itemStack.copy());
            this.lastEquipmentItems.put(EquipmentSlot.OFFHAND, itemStack2.copy());
        }
    }

    private void handleEquipmentChanges(Map<EquipmentSlot, ItemStack> map) {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)map.size());
        map.forEach((equipmentSlot, itemStack) -> {
            ItemStack itemStack2 = itemStack.copy();
            arrayList.add(Pair.of((Object)equipmentSlot, (Object)itemStack2));
            this.lastEquipmentItems.put((EquipmentSlot)equipmentSlot, itemStack2);
        });
        ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEquipmentPacket(this.getId(), arrayList));
    }

    protected void tickHeadTurn(float f) {
        float f2 = Mth.wrapDegrees(f - this.yBodyRot);
        this.yBodyRot += f2 * 0.3f;
        float f3 = Mth.wrapDegrees(this.getYRot() - this.yBodyRot);
        float f4 = this.getMaxHeadRotationRelativeToBody();
        if (Math.abs(f3) > f4) {
            this.yBodyRot += f3 - (float)Mth.sign(f3) * f4;
        }
    }

    protected float getMaxHeadRotationRelativeToBody() {
        return 50.0f;
    }

    /*
     * Unable to fully structure code
     */
    public void aiStep() {
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }
        if (this.isInterpolating()) {
            this.getInterpolation().interpolate();
        } else if (!this.canSimulateMovement()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }
        this.equipment.tick(this);
        var1_1 = this.getDeltaMovement();
        var2_2 = var1_1.x;
        var4_3 = var1_1.y;
        var6_4 = var1_1.z;
        if (this.getType().equals(EntityType.PLAYER)) {
            if (var1_1.horizontalDistanceSqr() < 9.0E-6) {
                var2_2 = 0.0;
                var6_4 = 0.0;
            }
        } else {
            if (Math.abs(var1_1.x) < 0.003) {
                var2_2 = 0.0;
            }
            if (Math.abs(var1_1.z) < 0.003) {
                var6_4 = 0.0;
            }
        }
        if (Math.abs(var1_1.y) < 0.003) {
            var4_3 = 0.0;
        }
        this.setDeltaMovement(var2_2, var4_3, var6_4);
        var8_5 = Profiler.get();
        var8_5.push("ai");
        this.applyInput();
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0f;
            this.zza = 0.0f;
        } else if (this.isEffectiveAi() && !this.level().isClientSide) {
            var8_5.push("newAi");
            this.serverAiStep();
            var8_5.pop();
        }
        var8_5.pop();
        var8_5.push("jump");
        if (this.jumping && this.isAffectedByFluids()) {
            var9_6 = this.isInLava() != false ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            var11_8 = this.isInWater() != false && var9_6 > 0.0;
            var12_12 = this.getFluidJumpThreshold();
            if (var11_8 && (!this.onGround() || var9_6 > var12_12)) {
                this.jumpInLiquid(FluidTags.WATER);
            } else if (this.isInLava() && (!this.onGround() || var9_6 > var12_12)) {
                this.jumpInLiquid(FluidTags.LAVA);
            } else if ((this.onGround() || var11_8 && var9_6 <= var12_12) && this.noJumpDelay == 0) {
                this.jumpFromGround();
                this.noJumpDelay = 10;
            }
        } else {
            this.noJumpDelay = 0;
        }
        var8_5.pop();
        var8_5.push("travel");
        if (this.isFallFlying()) {
            this.updateFallFlying();
        }
        var9_7 = this.getBoundingBox();
        var10_14 = new Vec3(this.xxa, this.yya, this.zza);
        if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
            this.resetFallDistance();
        }
        if (!((var12_13 = this.getControllingPassenger()) instanceof Player)) ** GOTO lbl-1000
        var11_9 = (Player)var12_13;
        if (this.isAlive()) {
            this.travelRidden(var11_9, var10_14);
        } else if (this.canSimulateMovement() && this.isEffectiveAi()) {
            this.travel(var10_14);
        }
        if (!this.level().isClientSide() || this.isLocalInstanceAuthoritative()) {
            this.applyEffectsFromBlocks();
        }
        if (this.level().isClientSide()) {
            this.calculateEntityAnimation(this instanceof FlyingAnimal);
        }
        var8_5.pop();
        var12_13 = this.level();
        if (var12_13 instanceof ServerLevel) {
            var11_10 = (ServerLevel)var12_13;
            var8_5.push("freezing");
            if (!this.isInPowderSnow || !this.canFreeze()) {
                this.setTicksFrozen(Math.max(0, this.getTicksFrozen() - 2));
            }
            this.removeFrost();
            this.tryAddFrost();
            if (this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
                this.hurtServer(var11_10, this.damageSources().freeze(), 1.0f);
            }
            var8_5.pop();
        }
        var8_5.push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack(var9_7, this.getBoundingBox());
        }
        this.pushEntities();
        var8_5.pop();
        var12_13 = this.level();
        if (var12_13 instanceof ServerLevel) {
            var11_11 = (ServerLevel)var12_13;
            if (this.isSensitiveToWater() && this.isInWaterOrRain()) {
                this.hurtServer(var11_11, this.damageSources().drown(), 1.0f);
            }
        }
    }

    protected void applyInput() {
        this.xxa *= 0.98f;
        this.zza *= 0.98f;
    }

    public boolean isSensitiveToWater() {
        return false;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    protected void updateFallFlying() {
        this.checkFallDistanceAccumulation();
        if (!this.level().isClientSide) {
            if (!this.canGlide()) {
                this.setSharedFlag(7, false);
                return;
            }
            int n = this.fallFlyTicks + 1;
            if (n % 10 == 0) {
                int n2 = n / 10;
                if (n2 % 2 == 0) {
                    List<EquipmentSlot> list = EquipmentSlot.VALUES.stream().filter(equipmentSlot -> LivingEntity.canGlideUsing(this.getItemBySlot((EquipmentSlot)equipmentSlot), equipmentSlot)).toList();
                    EquipmentSlot equipmentSlot2 = Util.getRandom(list, this.random);
                    this.getItemBySlot(equipmentSlot2).hurtAndBreak(1, this, equipmentSlot2);
                }
                this.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
    }

    protected boolean canGlide() {
        if (this.onGround() || this.isPassenger() || this.hasEffect(MobEffects.LEVITATION)) {
            return false;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            if (!LivingEntity.canGlideUsing(this.getItemBySlot(equipmentSlot), equipmentSlot)) continue;
            return true;
        }
        return false;
    }

    protected void serverAiStep() {
    }

    protected void pushEntities() {
        Object object;
        int n;
        List<Entity> list = this.level().getPushableEntities(this, this.getBoundingBox());
        if (list.isEmpty()) {
            return;
        }
        Level object22 = this.level();
        if (object22 instanceof ServerLevel && (n = ((ServerLevel)(object = (ServerLevel)object22)).getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING)) > 0 && list.size() > n - 1 && this.random.nextInt(4) == 0) {
            int n2 = 0;
            for (Entity entity : list) {
                if (entity.isPassenger()) continue;
                ++n2;
            }
            if (n2 > n - 1) {
                this.hurtServer((ServerLevel)object, this.damageSources().cramming(), 6.0f);
            }
        }
        for (Entity entity : list) {
            this.doPush(entity);
        }
    }

    protected void checkAutoSpinAttack(AABB aABB, AABB aABB2) {
        AABB aABB3 = aABB.minmax(aABB2);
        List<Entity> list = this.level().getEntities(this, aABB3);
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!(entity instanceof LivingEntity)) continue;
                this.doAutoAttackOnTouch((LivingEntity)entity);
                this.autoSpinAttackTicks = 0;
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.autoSpinAttackTicks = 0;
        }
        if (!this.level().isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
            this.autoSpinAttackDmg = 0.0f;
            this.autoSpinAttackItemStack = null;
        }
    }

    protected void doPush(Entity entity) {
        entity.push(this);
    }

    protected void doAutoAttackOnTouch(LivingEntity livingEntity) {
    }

    public boolean isAutoSpinAttack() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity entity = this.getVehicle();
        super.stopRiding();
        if (entity != null && entity != this.getVehicle() && !this.level().isClientSide) {
            this.dismountVehicle(entity);
        }
    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.resetFallDistance();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    public void lerpHeadTo(float f, int n) {
        this.lerpYHeadRot = f;
        this.lerpHeadSteps = n;
    }

    public void setJumping(boolean bl) {
        this.jumping = bl;
    }

    public void onItemPickup(ItemEntity itemEntity) {
        Entity entity = itemEntity.getOwner();
        if (entity instanceof ServerPlayer) {
            CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayer)entity, itemEntity.getItem(), this);
        }
    }

    public void take(Entity entity, int n) {
        if (!entity.isRemoved() && !this.level().isClientSide && (entity instanceof ItemEntity || entity instanceof AbstractArrow || entity instanceof ExperienceOrb)) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(entity, new ClientboundTakeItemEntityPacket(entity.getId(), this.getId(), n));
        }
    }

    public boolean hasLineOfSight(Entity entity) {
        return this.hasLineOfSight(entity, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity.getEyeY());
    }

    public boolean hasLineOfSight(Entity entity, ClipContext.Block block, ClipContext.Fluid fluid, double d) {
        if (entity.level() != this.level()) {
            return false;
        }
        Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 vec32 = new Vec3(entity.getX(), d, entity.getZ());
        if (vec32.distanceTo(vec3) > 128.0) {
            return false;
        }
        return this.level().clip(new ClipContext(vec3, vec32, block, fluid, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getViewYRot(float f) {
        if (f == 1.0f) {
            return this.yHeadRot;
        }
        return Mth.rotLerp(f, this.yHeadRotO, this.yHeadRot);
    }

    public float getAttackAnim(float f) {
        float f2 = this.attackAnim - this.oAttackAnim;
        if (f2 < 0.0f) {
            f2 += 1.0f;
        }
        return this.oAttackAnim + f2 * f;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.onClimbable();
    }

    @Override
    public float getYHeadRot() {
        return this.yHeadRot;
    }

    @Override
    public void setYHeadRot(float f) {
        this.yHeadRot = f;
    }

    @Override
    public void setYBodyRot(float f) {
        this.yBodyRot = f;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    public static Vec3 resetForwardDirectionOfRelativePortalPosition(Vec3 vec3) {
        return new Vec3(vec3.x, vec3.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float f) {
        this.internalSetAbsorptionAmount(Mth.clamp(f, 0.0f, this.getMaxAbsorption()));
    }

    protected void internalSetAbsorptionAmount(float f) {
        this.absorptionAmount = f;
    }

    public void onEnterCombat() {
    }

    public void onLeaveCombat() {
    }

    protected void updateEffectVisibility() {
        this.effectsDirty = true;
    }

    public abstract HumanoidArm getMainArm();

    public boolean isUsingItem() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
    }

    public InteractionHand getUsedItemHand() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private void updatingUsingItem() {
        if (this.isUsingItem()) {
            if (ItemStack.isSameItem(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                this.updateUsingItem(this.useItem);
            } else {
                this.stopUsingItem();
            }
        }
    }

    @Nullable
    private ItemEntity createItemStackToDrop(ItemStack itemStack, boolean bl, boolean bl2) {
        if (itemStack.isEmpty()) {
            return null;
        }
        double d = this.getEyeY() - (double)0.3f;
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), d, this.getZ(), itemStack);
        itemEntity.setPickUpDelay(40);
        if (bl2) {
            itemEntity.setThrower(this);
        }
        if (bl) {
            float f = this.random.nextFloat() * 0.5f;
            float f2 = this.random.nextFloat() * ((float)Math.PI * 2);
            itemEntity.setDeltaMovement(-Mth.sin(f2) * f, 0.2f, Mth.cos(f2) * f);
        } else {
            float f = 0.3f;
            float f3 = Mth.sin(this.getXRot() * ((float)Math.PI / 180));
            float f4 = Mth.cos(this.getXRot() * ((float)Math.PI / 180));
            float f5 = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float f6 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            float f7 = this.random.nextFloat() * ((float)Math.PI * 2);
            float f8 = 0.02f * this.random.nextFloat();
            itemEntity.setDeltaMovement((double)(-f5 * f4 * 0.3f) + Math.cos(f7) * (double)f8, -f3 * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)(f6 * f4 * 0.3f) + Math.sin(f7) * (double)f8);
        }
        return itemEntity;
    }

    protected void updateUsingItem(ItemStack itemStack) {
        itemStack.onUseTick(this.level(), this, this.getUseItemRemainingTicks());
        if (--this.useItemRemaining == 0 && !this.level().isClientSide && !itemStack.useOnRelease()) {
            this.completeUsingItem();
        }
    }

    private void updateSwimAmount() {
        this.swimAmountO = this.swimAmount;
        this.swimAmount = this.isVisuallySwimming() ? Math.min(1.0f, this.swimAmount + 0.09f) : Math.max(0.0f, this.swimAmount - 0.09f);
    }

    protected void setLivingEntityFlag(int n, boolean bl) {
        int n2 = this.entityData.get(DATA_LIVING_ENTITY_FLAGS).byteValue();
        n2 = bl ? (n2 |= n) : (n2 &= ~n);
        this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)n2);
    }

    public void startUsingItem(InteractionHand interactionHand) {
        ItemStack itemStack = this.getItemInHand(interactionHand);
        if (itemStack.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.useItem = itemStack;
        this.useItemRemaining = itemStack.getUseDuration(this);
        if (!this.level().isClientSide) {
            this.setLivingEntityFlag(1, true);
            this.setLivingEntityFlag(2, interactionHand == InteractionHand.OFF_HAND);
            this.gameEvent(GameEvent.ITEM_INTERACT_START);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (SLEEPING_POS_ID.equals(entityDataAccessor)) {
            if (this.level().isClientSide) {
                this.getSleepingPos().ifPresent(this::setPosToBed);
            }
        } else if (DATA_LIVING_ENTITY_FLAGS.equals(entityDataAccessor) && this.level().isClientSide) {
            if (this.isUsingItem() && this.useItem.isEmpty()) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                if (!this.useItem.isEmpty()) {
                    this.useItemRemaining = this.useItem.getUseDuration(this);
                }
            } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
                this.useItem = ItemStack.EMPTY;
                this.useItemRemaining = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
        super.lookAt(anchor, vec3);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot = this.yHeadRot;
    }

    @Override
    public float getPreciseBodyRotation(float f) {
        return Mth.lerp(f, this.yBodyRotO, this.yBodyRot);
    }

    public void spawnItemParticles(ItemStack itemStack, int n) {
        for (int i = 0; i < n; ++i) {
            Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-this.getXRot() * ((float)Math.PI / 180));
            vec3 = vec3.yRot(-this.getYRot() * ((float)Math.PI / 180));
            double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3 vec32 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
            vec32 = vec32.xRot(-this.getXRot() * ((float)Math.PI / 180));
            vec32 = vec32.yRot(-this.getYRot() * ((float)Math.PI / 180));
            vec32 = vec32.add(this.getX(), this.getEyeY(), this.getZ());
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack), vec32.x, vec32.y, vec32.z, vec3.x, vec3.y + 0.05, vec3.z);
        }
    }

    protected void completeUsingItem() {
        if (this.level().isClientSide && !this.isUsingItem()) {
            return;
        }
        InteractionHand interactionHand = this.getUsedItemHand();
        if (!this.useItem.equals(this.getItemInHand(interactionHand))) {
            this.releaseUsingItem();
            return;
        }
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            ItemStack itemStack = this.useItem.finishUsingItem(this.level(), this);
            if (itemStack != this.useItem) {
                this.setItemInHand(interactionHand, itemStack);
            }
            this.stopUsingItem();
        }
    }

    public void handleExtraItemsCreatedOnUse(ItemStack itemStack) {
    }

    public ItemStack getUseItem() {
        return this.useItem;
    }

    public int getUseItemRemainingTicks() {
        return this.useItemRemaining;
    }

    public int getTicksUsingItem() {
        if (this.isUsingItem()) {
            return this.useItem.getUseDuration(this) - this.getUseItemRemainingTicks();
        }
        return 0;
    }

    public void releaseUsingItem() {
        ItemStack itemStack = this.getItemInHand(this.getUsedItemHand());
        if (!this.useItem.isEmpty() && ItemStack.isSameItem(itemStack, this.useItem)) {
            this.useItem = itemStack;
            this.useItem.releaseUsing(this.level(), this, this.getUseItemRemainingTicks());
            if (this.useItem.useOnRelease()) {
                this.updatingUsingItem();
            }
        }
        this.stopUsingItem();
    }

    public void stopUsingItem() {
        if (!this.level().isClientSide) {
            boolean bl = this.isUsingItem();
            this.setLivingEntityFlag(1, false);
            if (bl) {
                this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.useItem = ItemStack.EMPTY;
        this.useItemRemaining = 0;
    }

    public boolean isBlocking() {
        return this.getItemBlockingWith() != null;
    }

    @Nullable
    public ItemStack getItemBlockingWith() {
        int n;
        if (!this.isUsingItem()) {
            return null;
        }
        BlocksAttacks blocksAttacks = this.useItem.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks != null && (n = this.useItem.getItem().getUseDuration(this.useItem, this) - this.useItemRemaining) >= blocksAttacks.blockDelayTicks()) {
            return this.useItem;
        }
        return null;
    }

    public boolean isSuppressingSlidingDownLadder() {
        return this.isShiftKeyDown();
    }

    public boolean isFallFlying() {
        return this.getSharedFlag(7);
    }

    @Override
    public boolean isVisuallySwimming() {
        return super.isVisuallySwimming() || !this.isFallFlying() && this.hasPose(Pose.FALL_FLYING);
    }

    public int getFallFlyingTicks() {
        return this.fallFlyTicks;
    }

    public boolean randomTeleport(double d, double d2, double d3, boolean bl) {
        Object object;
        double d4 = this.getX();
        double d5 = this.getY();
        double d6 = this.getZ();
        double d7 = d2;
        boolean bl2 = false;
        Object object2 = BlockPos.containing(d, d7, d3);
        Level level = this.level();
        if (level.hasChunkAt((BlockPos)object2)) {
            boolean bl3 = false;
            while (!bl3 && ((Vec3i)object2).getY() > level.getMinY()) {
                object = ((BlockPos)object2).below();
                BlockState blockState = level.getBlockState((BlockPos)object);
                if (blockState.blocksMotion()) {
                    bl3 = true;
                    continue;
                }
                d7 -= 1.0;
                object2 = object;
            }
            if (bl3) {
                this.teleportTo(d, d7, d3);
                if (level.noCollision(this) && !level.containsAnyLiquid(this.getBoundingBox())) {
                    bl2 = true;
                }
            }
        }
        if (!bl2) {
            this.teleportTo(d4, d5, d6);
            return false;
        }
        if (bl) {
            level.broadcastEntityEvent(this, (byte)46);
        }
        if ((object = this) instanceof PathfinderMob) {
            PathfinderMob pathfinderMob = (PathfinderMob)object;
            pathfinderMob.getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedByPotions() {
        return !this.isDeadOrDying();
    }

    public boolean attackable() {
        return true;
    }

    public void setRecordPlayingNearby(BlockPos blockPos, boolean bl) {
    }

    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public final EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.SLEEPING ? SLEEPING_DIMENSIONS : this.getDefaultDimensions(pose).scale(this.getScale());
    }

    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getType().getDimensions().scale(this.getAgeScale());
    }

    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of((Object)((Object)Pose.STANDING));
    }

    public AABB getLocalBoundsForPose(Pose pose) {
        EntityDimensions entityDimensions = this.getDimensions(pose);
        return new AABB(-entityDimensions.width() / 2.0f, 0.0, -entityDimensions.width() / 2.0f, entityDimensions.width() / 2.0f, entityDimensions.height(), entityDimensions.width() / 2.0f);
    }

    protected boolean wouldNotSuffocateAtTargetPose(Pose pose) {
        AABB aABB = this.getDimensions(pose).makeBoundingBox(this.position());
        return this.level().noBlockCollision(this, aABB);
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return super.canUsePortal(bl) && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPos() {
        return this.entityData.get(SLEEPING_POS_ID);
    }

    public void setSleepingPos(BlockPos blockPos) {
        this.entityData.set(SLEEPING_POS_ID, Optional.of(blockPos));
    }

    public void clearSleepingPos() {
        this.entityData.set(SLEEPING_POS_ID, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPos().isPresent();
    }

    public void startSleeping(BlockPos blockPos) {
        BlockState blockState;
        if (this.isPassenger()) {
            this.stopRiding();
        }
        if ((blockState = this.level().getBlockState(blockPos)).getBlock() instanceof BedBlock) {
            this.level().setBlock(blockPos, (BlockState)blockState.setValue(BedBlock.OCCUPIED, true), 3);
        }
        this.setPose(Pose.SLEEPING);
        this.setPosToBed(blockPos);
        this.setSleepingPos(blockPos);
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
    }

    private void setPosToBed(BlockPos blockPos) {
        this.setPos((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.6875, (double)blockPos.getZ() + 0.5);
    }

    private boolean checkBedExists() {
        return this.getSleepingPos().map(blockPos -> this.level().getBlockState((BlockPos)blockPos).getBlock() instanceof BedBlock).orElse(false);
    }

    public void stopSleeping() {
        this.getSleepingPos().filter(this.level()::hasChunkAt).ifPresent(blockPos -> {
            BlockState blockState = this.level().getBlockState((BlockPos)blockPos);
            if (blockState.getBlock() instanceof BedBlock) {
                Direction direction = (Direction)blockState.getValue(BedBlock.FACING);
                this.level().setBlock((BlockPos)blockPos, (BlockState)blockState.setValue(BedBlock.OCCUPIED, false), 3);
                Vec3 vec3 = BedBlock.findStandUpPosition(this.getType(), this.level(), blockPos, direction, this.getYRot()).orElseGet(() -> {
                    BlockPos blockPos2 = blockPos.above();
                    return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.1, (double)blockPos2.getZ() + 0.5);
                });
                Vec3 vec32 = Vec3.atBottomCenterOf(blockPos).subtract(vec3).normalize();
                float f = (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 57.2957763671875 - 90.0);
                this.setPos(vec3.x, vec3.y, vec3.z);
                this.setYRot(f);
                this.setXRot(0.0f);
            }
        });
        Vec3 vec3 = this.position();
        this.setPose(Pose.STANDING);
        this.setPos(vec3.x, vec3.y, vec3.z);
        this.clearSleepingPos();
    }

    @Nullable
    public Direction getBedOrientation() {
        BlockPos blockPos = this.getSleepingPos().orElse(null);
        return blockPos != null ? BedBlock.getBedOrientation(this.level(), blockPos) : null;
    }

    @Override
    public boolean isInWall() {
        return !this.isSleeping() && super.isInWall();
    }

    public ItemStack getProjectile(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    private static byte entityEventForEquipmentBreak(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> 47;
            case EquipmentSlot.OFFHAND -> 48;
            case EquipmentSlot.HEAD -> 49;
            case EquipmentSlot.CHEST -> 50;
            case EquipmentSlot.FEET -> 52;
            case EquipmentSlot.LEGS -> 51;
            case EquipmentSlot.BODY -> 65;
            case EquipmentSlot.SADDLE -> 68;
        };
    }

    public void onEquippedItemBroken(Item item, EquipmentSlot equipmentSlot) {
        this.level().broadcastEntityEvent(this, LivingEntity.entityEventForEquipmentBreak(equipmentSlot));
        this.stopLocationBasedEffects(this.getItemBySlot(equipmentSlot), equipmentSlot, this.attributes);
    }

    private void stopLocationBasedEffects(ItemStack itemStack, EquipmentSlot equipmentSlot, AttributeMap attributeMap) {
        itemStack.forEachModifier(equipmentSlot, (holder, attributeModifier) -> {
            AttributeInstance attributeInstance = attributeMap.getInstance((Holder<Attribute>)holder);
            if (attributeInstance != null) {
                attributeInstance.removeModifier((AttributeModifier)attributeModifier);
            }
        });
        EnchantmentHelper.stopLocationBasedEffects(itemStack, this, equipmentSlot);
    }

    public static EquipmentSlot getSlotForHand(InteractionHand interactionHand) {
        return interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    public final boolean canEquipWithDispenser(ItemStack itemStack) {
        if (!this.isAlive() || this.isSpectator()) {
            return false;
        }
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !equippable.dispensable()) {
            return false;
        }
        EquipmentSlot equipmentSlot = equippable.slot();
        if (!this.canUseSlot(equipmentSlot) || !equippable.canBeEquippedBy(this.getType())) {
            return false;
        }
        return this.getItemBySlot(equipmentSlot).isEmpty() && this.canDispenserEquipIntoSlot(equipmentSlot);
    }

    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return true;
    }

    public final EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && this.canUseSlot(equippable.slot())) {
            return equippable.slot();
        }
        return EquipmentSlot.MAINHAND;
    }

    public final boolean isEquippableInSlot(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return equipmentSlot == EquipmentSlot.MAINHAND && this.canUseSlot(EquipmentSlot.MAINHAND);
        }
        return equipmentSlot == equippable.slot() && this.canUseSlot(equippable.slot()) && equippable.canBeEquippedBy(this.getType());
    }

    private static SlotAccess createEquipmentSlotAccess(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HEAD || equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
            return SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot);
        }
        return SlotAccess.forEquipmentSlot(livingEntity, equipmentSlot, itemStack -> itemStack.isEmpty() || livingEntity.getEquipmentSlotForItem((ItemStack)itemStack) == equipmentSlot);
    }

    @Nullable
    private static EquipmentSlot getEquipmentSlot(int n) {
        if (n == 100 + EquipmentSlot.HEAD.getIndex()) {
            return EquipmentSlot.HEAD;
        }
        if (n == 100 + EquipmentSlot.CHEST.getIndex()) {
            return EquipmentSlot.CHEST;
        }
        if (n == 100 + EquipmentSlot.LEGS.getIndex()) {
            return EquipmentSlot.LEGS;
        }
        if (n == 100 + EquipmentSlot.FEET.getIndex()) {
            return EquipmentSlot.FEET;
        }
        if (n == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if (n == 99) {
            return EquipmentSlot.OFFHAND;
        }
        if (n == 105) {
            return EquipmentSlot.BODY;
        }
        if (n == 106) {
            return EquipmentSlot.SADDLE;
        }
        return null;
    }

    @Override
    public SlotAccess getSlot(int n) {
        EquipmentSlot equipmentSlot = LivingEntity.getEquipmentSlot(n);
        if (equipmentSlot != null) {
            return LivingEntity.createEquipmentSlotAccess(this, equipmentSlot);
        }
        return super.getSlot(n);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
            if (!this.getItemBySlot(equipmentSlot).is(ItemTags.FREEZE_IMMUNE_WEARABLES)) continue;
            return false;
        }
        return super.canFreeze();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return !this.level().isClientSide() && this.hasEffect(MobEffects.GLOWING) || super.isCurrentlyGlowing();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.yBodyRot;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        double d = clientboundAddEntityPacket.getX();
        double d2 = clientboundAddEntityPacket.getY();
        double d3 = clientboundAddEntityPacket.getZ();
        float f = clientboundAddEntityPacket.getYRot();
        float f2 = clientboundAddEntityPacket.getXRot();
        this.syncPacketPositionCodec(d, d2, d3);
        this.yBodyRot = clientboundAddEntityPacket.getYHeadRot();
        this.yHeadRot = clientboundAddEntityPacket.getYHeadRot();
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.setId(clientboundAddEntityPacket.getId());
        this.setUUID(clientboundAddEntityPacket.getUUID());
        this.absSnapTo(d, d2, d3, f, f2);
        this.setDeltaMovement(clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
    }

    public float getSecondsToDisableBlocking() {
        Weapon weapon = this.getWeaponItem().get(DataComponents.WEAPON);
        return weapon != null ? weapon.disableBlockingForSeconds() : 0.0f;
    }

    @Override
    public float maxUpStep() {
        float f = (float)this.getAttributeValue(Attributes.STEP_HEIGHT);
        return this.getControllingPassenger() instanceof Player ? Math.max(f, 1.0f) : f;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity entity) {
        return this.position().add(this.getPassengerAttachmentPoint(entity, this.getDimensions(this.getPose()), this.getScale() * this.getAgeScale()));
    }

    protected void lerpHeadRotationStep(int n, double d) {
        this.yHeadRot = (float)Mth.rotLerp(1.0 / (double)n, (double)this.yHeadRot, d);
    }

    @Override
    public void igniteForTicks(int n) {
        super.igniteForTicks(Mth.ceil((double)n * this.getAttributeValue(Attributes.BURNING_TIME)));
    }

    public boolean hasInfiniteMaterials() {
        return false;
    }

    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
        return this.isInvulnerableToBase(damageSource) || EnchantmentHelper.isImmuneToDamage(serverLevel, this, damageSource);
    }

    public static boolean canGlideUsing(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        if (!itemStack.has(DataComponents.GLIDER)) {
            return false;
        }
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equipmentSlot == equippable.slot() && !itemStack.nextDamageWillBreak();
    }

    @VisibleForTesting
    public int getLastHurtByPlayerMemoryTime() {
        return this.lastHurtByPlayerMemoryTime;
    }

    @Override
    public boolean isTransmittingWaypoint() {
        return this.getAttributeValue(Attributes.WAYPOINT_TRANSMIT_RANGE) > 0.0;
    }

    @Override
    public Optional<WaypointTransmitter.Connection> makeWaypointConnectionWith(ServerPlayer serverPlayer) {
        if (this.firstTick || serverPlayer == this) {
            return Optional.empty();
        }
        if (WaypointTransmitter.doesSourceIgnoreReceiver(this, serverPlayer)) {
            return Optional.empty();
        }
        Waypoint.Icon icon = this.locatorBarIcon.cloneAndAssignStyle(this);
        if (WaypointTransmitter.isReallyFar(this, serverPlayer)) {
            return Optional.of(new WaypointTransmitter.EntityAzimuthConnection(this, icon, serverPlayer));
        }
        if (!WaypointTransmitter.isChunkVisible(this.chunkPosition(), serverPlayer)) {
            return Optional.of(new WaypointTransmitter.EntityChunkConnection(this, icon, serverPlayer));
        }
        return Optional.of(new WaypointTransmitter.EntityBlockConnection(this, icon, serverPlayer));
    }

    @Override
    public Waypoint.Icon waypointIcon() {
        return this.locatorBarIcon;
    }

    public record Fallsounds(SoundEvent small, SoundEvent big) {
    }
}

