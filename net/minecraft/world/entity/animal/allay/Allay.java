/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Allay
extends PathfinderMob
implements InventoryCarrier,
VibrationSystem {
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 1, 1);
    private static final int LIFTING_ITEM_ANIMATION_DURATION = 5;
    private static final float DANCING_LOOP_DURATION = 55.0f;
    private static final float SPINNING_ANIMATION_DURATION = 15.0f;
    private static final int DEFAULT_DUPLICATION_COOLDOWN = 0;
    private static final int DUPLICATION_COOLDOWN_TICKS = 6000;
    private static final int NUM_OF_DUPLICATION_HEARTS = 3;
    public static final int MAX_NOTEBLOCK_DISTANCE = 1024;
    private static final EntityDataAccessor<Boolean> DATA_DANCING = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_DUPLICATE = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
    protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.LIKED_NOTEBLOCK_POSITION, MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.IS_PANICKING, (Object[])new MemoryModuleType[0]);
    public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of((Object)Float.valueOf(0.5625f), (Object)Float.valueOf(0.625f), (Object)Float.valueOf(0.75f), (Object)Float.valueOf(0.9375f), (Object)Float.valueOf(1.0f), (Object)Float.valueOf(1.0f), (Object)Float.valueOf(1.125f), (Object)Float.valueOf(1.25f), (Object)Float.valueOf(1.5f), (Object)Float.valueOf(1.875f), (Object)Float.valueOf(2.0f), (Object)Float.valueOf(2.25f), (Object[])new Float[]{Float.valueOf(2.5f), Float.valueOf(3.0f), Float.valueOf(3.75f), Float.valueOf(4.0f)});
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicVibrationListener;
    private VibrationSystem.Data vibrationData;
    private final VibrationSystem.User vibrationUser;
    private final DynamicGameEventListener<JukeboxListener> dynamicJukeboxListener;
    private final SimpleContainer inventory = new SimpleContainer(1);
    @Nullable
    private BlockPos jukeboxPos;
    private long duplicationCooldown = 0L;
    private float holdingItemAnimationTicks;
    private float holdingItemAnimationTicks0;
    private float dancingAnimationTicks;
    private float spinningAnimationTicks;
    private float spinningAnimationTicks0;

    public Allay(EntityType<? extends Allay> entityType, Level level) {
        super((EntityType<? extends PathfinderMob>)entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setCanPickUpLoot(this.canPickUpLoot());
        this.vibrationUser = new VibrationUser();
        this.vibrationData = new VibrationSystem.Data();
        this.dynamicVibrationListener = new DynamicGameEventListener<VibrationSystem.Listener>(new VibrationSystem.Listener(this));
        this.dynamicJukeboxListener = new DynamicGameEventListener<JukeboxListener>(new JukeboxListener(this.vibrationUser.getPositionSource(), GameEvent.JUKEBOX_PLAY.value().notificationRadius()));
    }

    protected Brain.Provider<Allay> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return AllayAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Allay> getBrain() {
        return super.getBrain();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.FLYING_SPEED, 0.1f).add(Attributes.MOVEMENT_SPEED, 0.1f).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setRequiredPathLength(48.0f);
        return flyingPathNavigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DANCING, false);
        builder.define(DATA_CAN_DUPLICATE, true);
    }

    @Override
    public void travel(Vec3 vec3) {
        this.travelFlying(vec3, this.getSpeed());
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isLikedPlayer(damageSource.getEntity())) {
            return false;
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return this.isLikedPlayer(entity) || super.considersEntityAsAlly(entity);
    }

    private boolean isLikedPlayer(@Nullable Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            Optional<UUID> optional = this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            return optional.isPresent() && player.getUUID().equals(optional.get());
        }
        return false;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasItemInSlot(EquipmentSlot.MAINHAND) ? SoundEvents.ALLAY_AMBIENT_WITH_ITEM : SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("allayBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.pop();
        profilerFiller.push("allayActivityUpdate");
        AllayAi.updateActivity(this);
        profilerFiller.pop();
        super.customServerAiStep(serverLevel);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive() && this.tickCount % 10 == 0) {
            this.heal(1.0f);
        }
        if (this.isDancing() && this.shouldStopDancing() && this.tickCount % 20 == 0) {
            this.setDancing(false);
            this.jukeboxPos = null;
        }
        this.updateDuplicationCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.holdingItemAnimationTicks0 = this.holdingItemAnimationTicks;
            this.holdingItemAnimationTicks = this.hasItemInHand() ? Mth.clamp(this.holdingItemAnimationTicks + 1.0f, 0.0f, 5.0f) : Mth.clamp(this.holdingItemAnimationTicks - 1.0f, 0.0f, 5.0f);
            if (this.isDancing()) {
                this.dancingAnimationTicks += 1.0f;
                this.spinningAnimationTicks0 = this.spinningAnimationTicks;
                this.spinningAnimationTicks = this.isSpinning() ? (this.spinningAnimationTicks += 1.0f) : (this.spinningAnimationTicks -= 1.0f);
                this.spinningAnimationTicks = Mth.clamp(this.spinningAnimationTicks, 0.0f, 15.0f);
            } else {
                this.dancingAnimationTicks = 0.0f;
                this.spinningAnimationTicks = 0.0f;
                this.spinningAnimationTicks0 = 0.0f;
            }
        } else {
            VibrationSystem.Ticker.tick(this.level(), this.vibrationData, this.vibrationUser);
            if (this.isPanicking()) {
                this.setDancing(false);
            }
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isOnPickupCooldown() && this.hasItemInHand();
    }

    public boolean hasItemInHand() {
        return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return false;
    }

    private boolean isOnPickupCooldown() {
        return this.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        ItemStack itemStack2 = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (this.isDancing() && itemStack.is(ItemTags.DUPLICATES_ALLAYS) && this.canDuplicate()) {
            this.duplicateAllay();
            this.level().broadcastEntityEvent(this, (byte)18);
            this.level().playSound((Entity)player, this, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.removeInteractionItem(player, itemStack);
            return InteractionResult.SUCCESS;
        }
        if (itemStack2.isEmpty() && !itemStack.isEmpty()) {
            ItemStack itemStack3 = itemStack.copyWithCount(1);
            this.setItemInHand(InteractionHand.MAIN_HAND, itemStack3);
            this.removeInteractionItem(player, itemStack);
            this.level().playSound((Entity)player, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, player.getUUID());
            return InteractionResult.SUCCESS;
        }
        if (!itemStack2.isEmpty() && interactionHand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.level().playSound((Entity)player, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.swing(InteractionHand.MAIN_HAND);
            for (ItemStack itemStack4 : this.getInventory().removeAllItems()) {
                BehaviorUtils.throwItem(this, itemStack4, this.position());
            }
            this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
            player.addItem(itemStack2);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    public void setJukeboxPlaying(BlockPos blockPos, boolean bl) {
        if (bl) {
            if (!this.isDancing()) {
                this.jukeboxPos = blockPos;
                this.setDancing(true);
            }
        } else if (blockPos.equals(this.jukeboxPos) || this.jukeboxPos == null) {
            this.jukeboxPos = null;
            this.setDancing(false);
        }
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    @Override
    public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack itemStack) {
        ItemStack itemStack2 = this.getItemInHand(InteractionHand.MAIN_HAND);
        return !itemStack2.isEmpty() && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.inventory.canAddItem(itemStack) && this.allayConsidersItemEqual(itemStack2, itemStack);
    }

    private boolean allayConsidersItemEqual(ItemStack itemStack, ItemStack itemStack2) {
        return ItemStack.isSameItem(itemStack, itemStack2) && !this.hasNonMatchingPotion(itemStack, itemStack2);
    }

    private boolean hasNonMatchingPotion(ItemStack itemStack, ItemStack itemStack2) {
        PotionContents potionContents;
        PotionContents potionContents2 = itemStack.get(DataComponents.POTION_CONTENTS);
        return !Objects.equals(potionContents2, potionContents = itemStack2.get(DataComponents.POTION_CONTENTS));
    }

    @Override
    protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
        InventoryCarrier.pickUpItem(serverLevel, this, this, itemEntity);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public boolean isFlapping() {
        return !this.onGround();
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            biConsumer.accept(this.dynamicVibrationListener, serverLevel);
            biConsumer.accept(this.dynamicJukeboxListener, serverLevel);
        }
    }

    public boolean isDancing() {
        return this.entityData.get(DATA_DANCING);
    }

    public void setDancing(boolean bl) {
        if (this.level().isClientSide || !this.isEffectiveAi() || bl && this.isPanicking()) {
            return;
        }
        this.entityData.set(DATA_DANCING, bl);
    }

    private boolean shouldStopDancing() {
        return this.jukeboxPos == null || !this.jukeboxPos.closerToCenterThan(this.position(), GameEvent.JUKEBOX_PLAY.value().notificationRadius()) || !this.level().getBlockState(this.jukeboxPos).is(Blocks.JUKEBOX);
    }

    public float getHoldingItemAnimationProgress(float f) {
        return Mth.lerp(f, this.holdingItemAnimationTicks0, this.holdingItemAnimationTicks) / 5.0f;
    }

    public boolean isSpinning() {
        float f = this.dancingAnimationTicks % 55.0f;
        return f < 15.0f;
    }

    public float getSpinningProgress(float f) {
        return Mth.lerp(f, this.spinningAnimationTicks0, this.spinningAnimationTicks) / 15.0f;
    }

    @Override
    public boolean equipmentHasChanged(ItemStack itemStack, ItemStack itemStack2) {
        return !this.allayConsidersItemEqual(itemStack, itemStack2);
    }

    @Override
    protected void dropEquipment(ServerLevel serverLevel) {
        super.dropEquipment(serverLevel);
        this.inventory.removeAllItems().forEach(itemStack -> this.spawnAtLocation(serverLevel, (ItemStack)itemStack));
        ItemStack itemStack2 = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemStack2.isEmpty() && !EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            this.spawnAtLocation(serverLevel, itemStack2);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.writeInventoryToTag(valueOutput);
        valueOutput.store("listener", VibrationSystem.Data.CODEC, this.vibrationData);
        valueOutput.putLong("DuplicationCooldown", this.duplicationCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readInventoryFromTag(valueInput);
        this.vibrationData = valueInput.read("listener", VibrationSystem.Data.CODEC).orElseGet(VibrationSystem.Data::new);
        this.setDuplicationCooldown(valueInput.getIntOr("DuplicationCooldown", 0));
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    private void updateDuplicationCooldown() {
        if (!this.level().isClientSide() && this.duplicationCooldown > 0L) {
            this.setDuplicationCooldown(this.duplicationCooldown - 1L);
        }
    }

    private void setDuplicationCooldown(long l) {
        this.duplicationCooldown = l;
        this.entityData.set(DATA_CAN_DUPLICATE, l == 0L);
    }

    private void duplicateAllay() {
        Allay allay = EntityType.ALLAY.create(this.level(), EntitySpawnReason.BREEDING);
        if (allay != null) {
            allay.snapTo(this.position());
            allay.setPersistenceRequired();
            allay.resetDuplicationCooldown();
            this.resetDuplicationCooldown();
            this.level().addFreshEntity(allay);
        }
    }

    private void resetDuplicationCooldown() {
        this.setDuplicationCooldown(6000L);
    }

    private boolean canDuplicate() {
        return this.entityData.get(DATA_CAN_DUPLICATE);
    }

    private void removeInteractionItem(Player player, ItemStack itemStack) {
        itemStack.consume(1, player);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)this.getEyeHeight() * 0.6, (double)this.getBbWidth() * 0.1);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 18) {
            for (int i = 0; i < 3; ++i) {
                this.spawnHeartParticle();
            }
        } else {
            super.handleEntityEvent(by);
        }
    }

    private void spawnHeartParticle() {
        double d = this.random.nextGaussian() * 0.02;
        double d2 = this.random.nextGaussian() * 0.02;
        double d3 = this.random.nextGaussian() * 0.02;
        this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, d2, d3);
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    class VibrationUser
    implements VibrationSystem.User {
        private static final int VIBRATION_EVENT_LISTENER_RANGE = 16;
        private final PositionSource positionSource;

        VibrationUser() {
            this.positionSource = new EntityPositionSource(Allay.this, Allay.this.getEyeHeight());
        }

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, GameEvent.Context context) {
            if (Allay.this.isNoAi()) {
                return false;
            }
            Optional<GlobalPos> optional = Allay.this.getBrain().getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
            if (optional.isEmpty()) {
                return true;
            }
            GlobalPos globalPos = optional.get();
            return globalPos.isCloseEnough(serverLevel.dimension(), Allay.this.blockPosition(), 1024) && globalPos.pos().equals(blockPos);
        }

        @Override
        public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity2, float f) {
            if (holder.is(GameEvent.NOTE_BLOCK_PLAY)) {
                AllayAi.hearNoteblock(Allay.this, new BlockPos(blockPos));
            }
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.ALLAY_CAN_LISTEN;
        }
    }

    class JukeboxListener
    implements GameEventListener {
        private final PositionSource listenerSource;
        private final int listenerRadius;

        public JukeboxListener(PositionSource positionSource, int n) {
            this.listenerSource = positionSource;
            this.listenerRadius = n;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRadius;
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3) {
            if (holder.is(GameEvent.JUKEBOX_PLAY)) {
                Allay.this.setJukeboxPlaying(BlockPos.containing(vec3), true);
                return true;
            }
            if (holder.is(GameEvent.JUKEBOX_STOP_PLAY)) {
                Allay.this.setJukeboxPlaying(BlockPos.containing(vec3), false);
                return true;
            }
            return false;
        }
    }
}

