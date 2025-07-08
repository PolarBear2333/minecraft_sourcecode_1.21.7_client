/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Parrot
extends ShoulderRidingEntity
implements FlyingAnimal {
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
    private static final Predicate<Mob> NOT_PARROT_PREDICATE = new Predicate<Mob>(){

        @Override
        public boolean test(@Nullable Mob mob) {
            return mob != null && MOB_SOUND_MAP.containsKey(mob.getType());
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Mob)object);
        }
    };
    static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
        hashMap.put(EntityType.BOGGED, SoundEvents.PARROT_IMITATE_BOGGED);
        hashMap.put(EntityType.BREEZE, SoundEvents.PARROT_IMITATE_BREEZE);
        hashMap.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        hashMap.put(EntityType.CREAKING, SoundEvents.PARROT_IMITATE_CREAKING);
        hashMap.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
        hashMap.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
        hashMap.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
        hashMap.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
        hashMap.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
        hashMap.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
        hashMap.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
        hashMap.put(EntityType.HAPPY_GHAST, SoundEvents.EMPTY);
        hashMap.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
        hashMap.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
        hashMap.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
        hashMap.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
        hashMap.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
        hashMap.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
        hashMap.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
        hashMap.put(EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
        hashMap.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
        hashMap.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
        hashMap.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
        hashMap.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
        hashMap.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
        hashMap.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
        hashMap.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        hashMap.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
        hashMap.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
        hashMap.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
        hashMap.put(EntityType.WARDEN, SoundEvents.PARROT_IMITATE_WARDEN);
        hashMap.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
        hashMap.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
        hashMap.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
        hashMap.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
        hashMap.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
        hashMap.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0f;
    private float nextFlap = 1.0f;
    private boolean partyParrot;
    @Nullable
    private BlockPos jukebox;

    public Parrot(EntityType<? extends Parrot> entityType, Level level) {
        super((EntityType<? extends ShoulderRidingEntity>)entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
        this.setPathfindingMalus(PathType.COCOA, -1.0f);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(Util.getRandom(Variant.values(), serverLevelAccessor.getRandom()));
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(false);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TamableAnimal.TamableAnimalPanicGoal(1.25));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 5.0f, 1.0f));
        this.goalSelector.addGoal(2, new ParrotWanderGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
        this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0, 3.0f, 7.0f));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.FLYING_SPEED, 0.4f).add(Attributes.MOVEMENT_SPEED, 0.2f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        return flyingPathNavigation;
    }

    @Override
    public void aiStep() {
        if (this.jukebox == null || !this.jukebox.closerToCenterThan(this.position(), 3.46) || !this.level().getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
            this.partyParrot = false;
            this.jukebox = null;
        }
        if (this.level().random.nextInt(400) == 0) {
            Parrot.imitateNearbyMobs(this.level(), this);
        }
        super.aiStep();
        this.calculateFlapping();
    }

    @Override
    public void setRecordPlayingNearby(BlockPos blockPos, boolean bl) {
        this.jukebox = blockPos;
        this.partyParrot = bl;
    }

    public boolean isPartyParrot() {
        return this.partyParrot;
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (float)(this.onGround() || this.isPassenger() ? -1 : 4) * 0.3f;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0f, 1.0f);
        if (!this.onGround() && this.flapping < 1.0f) {
            this.flapping = 1.0f;
        }
        this.flapping *= 0.9f;
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0) {
            this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
        }
        this.flap += this.flapping * 2.0f;
    }

    public static boolean imitateNearbyMobs(Level level, Entity entity) {
        Mob mob;
        if (!entity.isAlive() || entity.isSilent() || level.random.nextInt(2) != 0) {
            return false;
        }
        List<Mob> list = level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(20.0), NOT_PARROT_PREDICATE);
        if (!list.isEmpty() && !(mob = list.get(level.random.nextInt(list.size()))).isSilent()) {
            SoundEvent soundEvent = Parrot.getImitatedSound(mob.getType());
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, entity.getSoundSource(), 0.7f, Parrot.getPitch(level.random));
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!this.isTame() && itemStack.is(ItemTags.PARROT_FOOD)) {
            this.usePlayerItem(player, interactionHand, itemStack);
            if (!this.isSilent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_EAT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            if (!this.level().isClientSide) {
                if (this.random.nextInt(10) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (itemStack.is(ItemTags.PARROT_POISONOUS_FOOD)) {
            this.usePlayerItem(player, interactionHand, itemStack);
            this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
            if (player.isCreative() || !this.isInvulnerable()) {
                this.hurt(this.damageSources().playerAttack(player), Float.MAX_VALUE);
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.isFlying() && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    public static boolean checkParrotSpawnRules(EntityType<Parrot> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.PARROTS_SPAWNABLE_ON) && Parrot.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    public boolean canMate(Animal animal) {
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound() {
        return Parrot.getAmbient(this.level(), this.level().random);
    }

    public static SoundEvent getAmbient(Level level, RandomSource randomSource) {
        if (level.getDifficulty() != Difficulty.PEACEFUL && randomSource.nextInt(1000) == 0) {
            ArrayList arrayList = Lists.newArrayList(MOB_SOUND_MAP.keySet());
            return Parrot.getImitatedSound((EntityType)arrayList.get(randomSource.nextInt(arrayList.size())));
        }
        return SoundEvents.PARROT_AMBIENT;
    }

    private static SoundEvent getImitatedSound(EntityType<?> entityType) {
        return MOB_SOUND_MAP.getOrDefault(entityType, SoundEvents.PARROT_AMBIENT);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.PARROT_STEP, 0.15f, 1.0f);
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.playSound(SoundEvents.PARROT_FLY, 0.15f, 1.0f);
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0f;
    }

    @Override
    public float getVoicePitch() {
        return Parrot.getPitch(this.random);
    }

    public static float getPitch(RandomSource randomSource) {
        return (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        if (entity instanceof Player) {
            return;
        }
        super.doPush(entity);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        this.setOrderedToSit(false);
        return super.hurtServer(serverLevel, damageSource, f);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    private void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.PARROT_VARIANT) {
            return Parrot.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.PARROT_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T t) {
        if (dataComponentType == DataComponents.PARROT_VARIANT) {
            this.setVariant(Parrot.castComponentValue(DataComponents.PARROT_VARIANT, t));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, t);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, Variant.DEFAULT.id);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("Variant", Variant.LEGACY_CODEC, this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setVariant(valueInput.read("Variant", Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    protected boolean canFlyToOwner() {
        return true;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.5f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    public static enum Variant implements StringRepresentable
    {
        RED_BLUE(0, "red_blue"),
        BLUE(1, "blue"),
        GREEN(2, "green"),
        YELLOW_BLUE(3, "yellow_blue"),
        GRAY(4, "gray");

        public static final Variant DEFAULT;
        private static final IntFunction<Variant> BY_ID;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> LEGACY_CODEC;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        final int id;
        private final String name;

        private Variant(int n2, String string2) {
            this.id = n2;
            this.name = string2;
        }

        public int getId() {
            return this.id;
        }

        public static Variant byId(int n) {
            return BY_ID.apply(n);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            DEFAULT = RED_BLUE;
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::getId);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::getId);
        }
    }

    static class ParrotWanderGoal
    extends WaterAvoidingRandomFlyingGoal {
        public ParrotWanderGoal(PathfinderMob pathfinderMob, double d) {
            super(pathfinderMob, d);
        }

        @Override
        @Nullable
        protected Vec3 getPosition() {
            Vec3 vec3 = null;
            if (this.mob.isInWater()) {
                vec3 = LandRandomPos.getPos(this.mob, 15, 15);
            }
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3 = this.getTreePos();
            }
            return vec3 == null ? super.getPosition() : vec3;
        }

        @Nullable
        private Vec3 getTreePos() {
            BlockPos blockPos = this.mob.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
            Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0), Mth.floor(this.mob.getY() - 6.0), Mth.floor(this.mob.getZ() - 3.0), Mth.floor(this.mob.getX() + 3.0), Mth.floor(this.mob.getY() + 6.0), Mth.floor(this.mob.getZ() + 3.0));
            for (BlockPos blockPos2 : iterable) {
                BlockState blockState;
                boolean bl;
                if (blockPos.equals(blockPos2) || !(bl = (blockState = this.mob.level().getBlockState(mutableBlockPos2.setWithOffset((Vec3i)blockPos2, Direction.DOWN))).getBlock() instanceof LeavesBlock || blockState.is(BlockTags.LOGS)) || !this.mob.level().isEmptyBlock(blockPos2) || !this.mob.level().isEmptyBlock(mutableBlockPos.setWithOffset((Vec3i)blockPos2, Direction.UP))) continue;
                return Vec3.atBottomCenterOf(blockPos2);
            }
            return null;
        }
    }
}

