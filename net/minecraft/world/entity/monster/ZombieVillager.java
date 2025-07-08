/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ZombieVillager
extends Zombie
implements VillagerDataHolder {
    private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.VILLAGER_DATA);
    private static final int VILLAGER_CONVERSION_WAIT_MIN = 3600;
    private static final int VILLAGER_CONVERSION_WAIT_MAX = 6000;
    private static final int MAX_SPECIAL_BLOCKS_COUNT = 14;
    private static final int SPECIAL_BLOCK_RADIUS = 4;
    private static final int NOT_CONVERTING = -1;
    private static final int DEFAULT_XP = 0;
    private int villagerConversionTime;
    @Nullable
    private UUID conversionStarter;
    @Nullable
    private GossipContainer gossips;
    @Nullable
    private MerchantOffers tradeOffers;
    private int villagerXp = 0;

    public ZombieVillager(EntityType<? extends ZombieVillager> entityType, Level level) {
        super((EntityType<? extends Zombie>)entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CONVERTING_ID, false);
        builder.define(DATA_VILLAGER_DATA, Villager.createDefaultVillagerData());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("VillagerData", VillagerData.CODEC, this.getVillagerData());
        valueOutput.storeNullable("Offers", MerchantOffers.CODEC, this.tradeOffers);
        valueOutput.storeNullable("Gossips", GossipContainer.CODEC, this.gossips);
        valueOutput.putInt("ConversionTime", this.isConverting() ? this.villagerConversionTime : -1);
        valueOutput.storeNullable("ConversionPlayer", UUIDUtil.CODEC, this.conversionStarter);
        valueOutput.putInt("Xp", this.villagerXp);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.entityData.set(DATA_VILLAGER_DATA, valueInput.read("VillagerData", VillagerData.CODEC).orElseGet(Villager::createDefaultVillagerData));
        this.tradeOffers = valueInput.read("Offers", MerchantOffers.CODEC).orElse(null);
        this.gossips = valueInput.read("Gossips", GossipContainer.CODEC).orElse(null);
        int n = valueInput.getIntOr("ConversionTime", -1);
        if (n != -1) {
            UUID uUID = valueInput.read("ConversionPlayer", UUIDUtil.CODEC).orElse(null);
            this.startConverting(uUID, n);
        } else {
            this.getEntityData().set(DATA_CONVERTING_ID, false);
            this.villagerConversionTime = -1;
        }
        this.villagerXp = valueInput.getIntOr("Xp", 0);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isConverting()) {
            int n = this.getConversionProgress();
            this.villagerConversionTime -= n;
            if (this.villagerConversionTime <= 0) {
                this.finishConversion((ServerLevel)this.level());
            }
        }
        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.GOLDEN_APPLE)) {
            if (this.hasEffect(MobEffects.WEAKNESS)) {
                itemStack.consume(1, player);
                if (!this.level().isClientSide) {
                    this.startConverting(player.getUUID(), this.random.nextInt(2401) + 3600);
                }
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.isConverting() && this.villagerXp == 0;
    }

    public boolean isConverting() {
        return this.getEntityData().get(DATA_CONVERTING_ID);
    }

    private void startConverting(@Nullable UUID uUID, int n) {
        this.conversionStarter = uUID;
        this.villagerConversionTime = n;
        this.getEntityData().set(DATA_CONVERTING_ID, true);
        this.removeEffect(MobEffects.WEAKNESS);
        this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, n, Math.min(this.level().getDifficulty().getId() - 1, 0)));
        this.level().broadcastEntityEvent(this, (byte)16);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 16) {
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0f + this.random.nextFloat(), this.random.nextFloat() * 0.7f + 0.3f, false);
            }
            return;
        }
        super.handleEntityEvent(by);
    }

    private void finishConversion(ServerLevel serverLevel) {
        this.convertTo(EntityType.VILLAGER, ConversionParams.single(this, false, false), villager -> {
            Object object = this.dropPreservedEquipment(serverLevel, itemStack -> !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)).iterator();
            while (object.hasNext()) {
                EquipmentSlot equipmentSlot = object.next();
                SlotAccess slotAccess = villager.getSlot(equipmentSlot.getIndex() + 300);
                slotAccess.set(this.getItemBySlot(equipmentSlot));
            }
            villager.setVillagerData(this.getVillagerData());
            if (this.gossips != null) {
                villager.setGossips(this.gossips);
            }
            if (this.tradeOffers != null) {
                villager.setOffers(this.tradeOffers.copy());
            }
            villager.setVillagerXp(this.villagerXp);
            villager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(villager.blockPosition()), EntitySpawnReason.CONVERSION, null);
            villager.refreshBrain(serverLevel);
            if (this.conversionStarter != null && (object = serverLevel.getPlayerByUUID(this.conversionStarter)) instanceof ServerPlayer) {
                CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayer)object, this, (Villager)villager);
                serverLevel.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, (Entity)object, (ReputationEventHandler)((Object)villager));
            }
            villager.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
            if (!this.isSilent()) {
                serverLevel.levelEvent(null, 1027, this.blockPosition(), 0);
            }
        });
    }

    @VisibleForTesting
    public void setVillagerConversionTime(int n) {
        this.villagerConversionTime = n;
    }

    private int getConversionProgress() {
        int n = 1;
        if (this.random.nextFloat() < 0.01f) {
            int n2 = 0;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int i = (int)this.getX() - 4; i < (int)this.getX() + 4 && n2 < 14; ++i) {
                for (int j = (int)this.getY() - 4; j < (int)this.getY() + 4 && n2 < 14; ++j) {
                    for (int k = (int)this.getZ() - 4; k < (int)this.getZ() + 4 && n2 < 14; ++k) {
                        BlockState blockState = this.level().getBlockState(mutableBlockPos.set(i, j, k));
                        if (!blockState.is(Blocks.IRON_BARS) && !(blockState.getBlock() instanceof BedBlock)) continue;
                        if (this.random.nextFloat() < 0.3f) {
                            ++n;
                        }
                        ++n2;
                    }
                }
            }
        }
        return n;
    }

    @Override
    public float getVoicePitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 2.0f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_VILLAGER_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_VILLAGER_STEP;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    public void setTradeOffers(MerchantOffers merchantOffers) {
        this.tradeOffers = merchantOffers;
    }

    public void setGossips(GossipContainer gossipContainer) {
        this.gossips = gossipContainer;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        VillagerData villagerData = this.getVillagerData().withType(serverLevelAccessor.registryAccess(), VillagerType.byBiome(serverLevelAccessor.getBiome(this.blockPosition())));
        Optional optional = BuiltInRegistries.VILLAGER_PROFESSION.getRandom(this.random);
        if (optional.isPresent()) {
            villagerData = villagerData.withProfession(optional.get());
        }
        this.setVillagerData(villagerData);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        VillagerData villagerData2 = this.getVillagerData();
        if (!villagerData2.profession().equals(villagerData.profession())) {
            this.tradeOffers = null;
        }
        this.entityData.set(DATA_VILLAGER_DATA, villagerData);
    }

    @Override
    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER_DATA);
    }

    public int getVillagerXp() {
        return this.villagerXp;
    }

    public void setVillagerXp(int n) {
        this.villagerXp = n;
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.VILLAGER_VARIANT) {
            return ZombieVillager.castComponentValue(dataComponentType, this.getVillagerData().type());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.VILLAGER_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T t) {
        if (dataComponentType == DataComponents.VILLAGER_VARIANT) {
            Holder<VillagerType> holder = ZombieVillager.castComponentValue(DataComponents.VILLAGER_VARIANT, t);
            this.setVillagerData(this.getVillagerData().withType(holder));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, t);
    }
}

