/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity.animal.sniffer;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
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
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.SnifferAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Sniffer
extends Animal {
    private static final int DIGGING_PARTICLES_DELAY_TICKS = 1700;
    private static final int DIGGING_PARTICLES_DURATION_TICKS = 6000;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final int DIGGING_DROP_SEED_OFFSET_TICKS = 120;
    private static final int SNIFFER_BABY_AGE_TICKS = 48000;
    private static final float DIGGING_BB_HEIGHT_OFFSET = 0.4f;
    private static final EntityDimensions DIGGING_DIMENSIONS = EntityDimensions.scalable(EntityType.SNIFFER.getWidth(), EntityType.SNIFFER.getHeight() - 0.4f).withEyeHeight(0.81f);
    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.SNIFFER_STATE);
    private static final EntityDataAccessor<Integer> DATA_DROP_SEED_AT_TICK = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.INT);
    public final AnimationState feelingHappyAnimationState = new AnimationState();
    public final AnimationState scentingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();
    public final AnimationState diggingAnimationState = new AnimationState();
    public final AnimationState risingAnimationState = new AnimationState();

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.1f).add(Attributes.MAX_HEALTH, 14.0);
    }

    public Sniffer(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, -1.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLING);
        builder.define(DATA_DROP_SEED_AT_TICK, 0);
    }

    @Override
    public void onPathfindingStart() {
        super.onPathfindingStart();
        if (this.isOnFire() || this.isInWater()) {
            this.setPathfindingMalus(PathType.WATER, 0.0f);
        }
    }

    @Override
    public void onPathfindingDone() {
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (this.getState() == State.DIGGING) {
            return DIGGING_DIMENSIONS.scale(this.getAgeScale());
        }
        return super.getDefaultDimensions(pose);
    }

    public boolean isSearching() {
        return this.getState() == State.SEARCHING;
    }

    public boolean isTempted() {
        return this.brain.getMemory(MemoryModuleType.IS_TEMPTED).orElse(false);
    }

    public boolean canSniff() {
        return !this.isTempted() && !this.isPanicking() && !this.isInWater() && !this.isInLove() && this.onGround() && !this.isPassenger() && !this.isLeashed();
    }

    public boolean canPlayDiggingSound() {
        return this.getState() == State.DIGGING || this.getState() == State.SEARCHING;
    }

    private BlockPos getHeadBlock() {
        Vec3 vec3 = this.getHeadPosition();
        return BlockPos.containing(vec3.x(), this.getY() + (double)0.2f, vec3.z());
    }

    private Vec3 getHeadPosition() {
        return this.position().add(this.getForward().scale(2.25));
    }

    @Override
    public boolean supportQuadLeash() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, -0.01, 0.63, 0.38, 1.15);
    }

    private State getState() {
        return this.entityData.get(DATA_STATE);
    }

    private Sniffer setState(State state) {
        this.entityData.set(DATA_STATE, state);
        return this;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_STATE.equals(entityDataAccessor)) {
            State state = this.getState();
            this.resetAnimations();
            switch (state.ordinal()) {
                case 2: {
                    this.scentingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case 3: {
                    this.sniffingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case 5: {
                    this.diggingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case 6: {
                    this.risingAnimationState.startIfStopped(this.tickCount);
                    break;
                }
                case 1: {
                    this.feelingHappyAnimationState.startIfStopped(this.tickCount);
                }
            }
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimations() {
        this.diggingAnimationState.stop();
        this.sniffingAnimationState.stop();
        this.risingAnimationState.stop();
        this.feelingHappyAnimationState.stop();
        this.scentingAnimationState.stop();
    }

    public Sniffer transitionTo(State state) {
        switch (state.ordinal()) {
            case 0: {
                this.setState(State.IDLING);
                break;
            }
            case 2: {
                this.setState(State.SCENTING).onScentingStart();
                break;
            }
            case 3: {
                this.playSound(SoundEvents.SNIFFER_SNIFFING, 1.0f, 1.0f);
                this.setState(State.SNIFFING);
                break;
            }
            case 4: {
                this.setState(State.SEARCHING);
                break;
            }
            case 5: {
                this.setState(State.DIGGING).onDiggingStart();
                break;
            }
            case 6: {
                this.playSound(SoundEvents.SNIFFER_DIGGING_STOP, 1.0f, 1.0f);
                this.setState(State.RISING);
                break;
            }
            case 1: {
                this.playSound(SoundEvents.SNIFFER_HAPPY, 1.0f, 1.0f);
                this.setState(State.FEELING_HAPPY);
            }
        }
        return this;
    }

    private Sniffer onScentingStart() {
        this.playSound(SoundEvents.SNIFFER_SCENTING, 1.0f, this.isBaby() ? 1.3f : 1.0f);
        return this;
    }

    private Sniffer onDiggingStart() {
        this.entityData.set(DATA_DROP_SEED_AT_TICK, this.tickCount + 120);
        this.level().broadcastEntityEvent(this, (byte)63);
        return this;
    }

    public Sniffer onDiggingComplete(boolean bl) {
        if (bl) {
            this.storeExploredPosition(this.getOnPos());
        }
        return this;
    }

    Optional<BlockPos> calculateDigPosition() {
        return IntStream.range(0, 5).mapToObj(n -> LandRandomPos.getPos(this, 10 + 2 * n, 3)).filter(Objects::nonNull).map(BlockPos::containing).filter(blockPos -> this.level().getWorldBorder().isWithinBounds((BlockPos)blockPos)).map(BlockPos::below).filter(this::canDig).findFirst();
    }

    boolean canDig() {
        return !this.isPanicking() && !this.isTempted() && !this.isBaby() && !this.isInWater() && this.onGround() && !this.isPassenger() && this.canDig(this.getHeadBlock().below());
    }

    private boolean canDig(BlockPos blockPos) {
        return this.level().getBlockState(blockPos).is(BlockTags.SNIFFER_DIGGABLE_BLOCK) && this.getExploredPositions().noneMatch(globalPos -> GlobalPos.of(this.level().dimension(), blockPos).equals(globalPos)) && Optional.ofNullable(this.getNavigation().createPath(blockPos, 1)).map(Path::canReach).orElse(false) != false;
    }

    private void dropSeed() {
        ServerLevel serverLevel;
        Object object;
        block3: {
            block2: {
                object = this.level();
                if (!(object instanceof ServerLevel)) break block2;
                serverLevel = (ServerLevel)object;
                if (this.entityData.get(DATA_DROP_SEED_AT_TICK) == this.tickCount) break block3;
            }
            return;
        }
        object = this.getHeadBlock();
        this.dropFromGiftLootTable(serverLevel, BuiltInLootTables.SNIFFER_DIGGING, (arg_0, arg_1) -> this.lambda$dropSeed$3((BlockPos)object, arg_0, arg_1));
        this.playSound(SoundEvents.SNIFFER_DROP_SEED, 1.0f, 1.0f);
    }

    private Sniffer emitDiggingParticles(AnimationState animationState) {
        boolean bl;
        boolean bl2 = bl = animationState.getTimeInMillis(this.tickCount) > 1700L && animationState.getTimeInMillis(this.tickCount) < 6000L;
        if (bl) {
            BlockPos blockPos = this.getHeadBlock();
            BlockState blockState = this.level().getBlockState(blockPos.below());
            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                for (int i = 0; i < 30; ++i) {
                    Vec3 vec3 = Vec3.atCenterOf(blockPos).add(0.0, -0.65f, 0.0);
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
                }
                if (this.tickCount % 10 == 0) {
                    this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), blockState.getSoundType().getHitSound(), this.getSoundSource(), 0.5f, 0.5f, false);
                }
            }
        }
        if (this.tickCount % 10 == 0) {
            this.level().gameEvent(GameEvent.ENTITY_ACTION, this.getHeadBlock(), GameEvent.Context.of(this));
        }
        return this;
    }

    private Sniffer storeExploredPosition(BlockPos blockPos) {
        List list = this.getExploredPositions().limit(20L).collect(Collectors.toList());
        list.add(0, GlobalPos.of(this.level().dimension(), blockPos));
        this.getBrain().setMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, list);
        return this;
    }

    private Stream<GlobalPos> getExploredPositions() {
        return this.getBrain().getMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS).stream().flatMap(Collection::stream);
    }

    @Override
    public void jumpFromGround() {
        double d;
        super.jumpFromGround();
        double d2 = this.moveControl.getSpeedModifier();
        if (d2 > 0.0 && (d = this.getDeltaMovement().horizontalDistanceSqr()) < 0.01) {
            this.moveRelative(0.1f, new Vec3(0.0, 0.0, 1.0));
        }
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel serverLevel, Animal animal) {
        ItemStack itemStack = new ItemStack(Items.SNIFFER_EGG);
        ItemEntity itemEntity = new ItemEntity(serverLevel, this.position().x(), this.position().y(), this.position().z(), itemStack);
        itemEntity.setDefaultPickUpDelay();
        this.finalizeSpawnChildFromBreeding(serverLevel, animal, null);
        this.playSound(SoundEvents.SNIFFER_EGG_PLOP, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 0.5f);
        serverLevel.addFreshEntity(itemEntity);
    }

    @Override
    public void die(DamageSource damageSource) {
        this.transitionTo(State.IDLING);
        super.die(damageSource);
    }

    @Override
    public void tick() {
        switch (this.getState().ordinal()) {
            case 5: {
                this.emitDiggingParticles(this.diggingAnimationState).dropSeed();
                break;
            }
            case 4: {
                this.playSearchingSound();
            }
        }
        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl = this.isFood(itemStack);
        InteractionResult interactionResult = super.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction() && bl) {
            this.playEatingSound();
        }
        return interactionResult;
    }

    @Override
    protected void playEatingSound() {
        this.level().playSound(null, this, SoundEvents.SNIFFER_EAT, SoundSource.NEUTRAL, 1.0f, Mth.randomBetween(this.level().random, 0.8f, 1.2f));
    }

    private void playSearchingSound() {
        if (this.level().isClientSide() && this.tickCount % 20 == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SNIFFER_SEARCHING, this.getSoundSource(), 1.0f, 1.0f, false);
        }
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.SNIFFER_STEP, 0.15f, 1.0f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Set.of(State.DIGGING, State.SEARCHING).contains((Object)this.getState()) ? null : SoundEvents.SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SNIFFER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNIFFER_DEATH;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public void setBaby(boolean bl) {
        this.setAge(bl ? -48000 : 0);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.SNIFFER.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal instanceof Sniffer) {
            Sniffer sniffer = (Sniffer)animal;
            Set<State> set = Set.of(State.IDLING, State.SCENTING, State.FEELING_HAPPY);
            return set.contains((Object)this.getState()) && set.contains((Object)sniffer.getState()) && super.canMate(animal);
        }
        return false;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.SNIFFER_FOOD);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return SnifferAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Sniffer> getBrain() {
        return super.getBrain();
    }

    protected Brain.Provider<Sniffer> brainProvider() {
        return Brain.provider(SnifferAi.MEMORY_TYPES, SnifferAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("snifferBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.popPush("snifferActivityUpdate");
        SnifferAi.updateActivity(this);
        profilerFiller.pop();
        super.customServerAiStep(serverLevel);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    private /* synthetic */ void lambda$dropSeed$3(BlockPos blockPos, ServerLevel serverLevel, ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(this.level(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
        itemEntity.setDefaultPickUpDelay();
        serverLevel.addFreshEntity(itemEntity);
    }

    public static enum State {
        IDLING(0),
        FEELING_HAPPY(1),
        SCENTING(2),
        SNIFFING(3),
        SEARCHING(4),
        DIGGING(5),
        RISING(6);

        public static final IntFunction<State> BY_ID;
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC;
        private final int id;

        private State(int n2) {
            this.id = n2;
        }

        public int id() {
            return this.id;
        }

        static {
            BY_ID = ByIdMap.continuous(State::id, State.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, State::id);
        }
    }
}

