/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.npc;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractVillager
extends AgeableMob
implements InventoryCarrier,
Npc,
Merchant {
    private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
    public static final int VILLAGER_SLOT_OFFSET = 300;
    private static final int VILLAGER_INVENTORY_SIZE = 8;
    @Nullable
    private Player tradingPlayer;
    @Nullable
    protected MerchantOffers offers;
    private final SimpleContainer inventory = new SimpleContainer(8);

    public AbstractVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super((EntityType<? extends AgeableMob>)entityType, level);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(false);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    public int getUnhappyCounter() {
        return this.entityData.get(DATA_UNHAPPY_COUNTER);
    }

    public void setUnhappyCounter(int n) {
        this.entityData.set(DATA_UNHAPPY_COUNTER, n);
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_UNHAPPY_COUNTER, 0);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Override
    @Nullable
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.level().isClientSide) {
            throw new IllegalStateException("Cannot load Villager offers on the client");
        }
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.updateTrades();
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers merchantOffers) {
    }

    @Override
    public void overrideXp(int n) {
    }

    @Override
    public void notifyTrade(MerchantOffer merchantOffer) {
        merchantOffer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.rewardTradeXp(merchantOffer);
        if (this.tradingPlayer instanceof ServerPlayer) {
            CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, merchantOffer.getResult());
        }
    }

    protected abstract void rewardTradeXp(MerchantOffer var1);

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public void notifyTradeUpdated(ItemStack itemStack) {
        if (!this.level().isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            this.makeSound(this.getTradeUpdatedSound(!itemStack.isEmpty()));
        }
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    protected SoundEvent getTradeUpdatedSound(boolean bl) {
        return bl ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    public void playCelebrateSound() {
        this.makeSound(SoundEvents.VILLAGER_CELEBRATE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        MerchantOffers merchantOffers;
        super.addAdditionalSaveData(valueOutput);
        if (!this.level().isClientSide && !(merchantOffers = this.getOffers()).isEmpty()) {
            valueOutput.store("Offers", MerchantOffers.CODEC, merchantOffers);
        }
        this.writeInventoryToTag(valueOutput);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.offers = valueInput.read("Offers", MerchantOffers.CODEC).orElse(null);
        this.readInventoryFromTag(valueInput);
    }

    @Override
    @Nullable
    public Entity teleport(TeleportTransition teleportTransition) {
        this.stopTrading();
        return super.teleport(teleportTransition);
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.stopTrading();
    }

    protected void addParticlesAroundSelf(ParticleOptions particleOptions) {
        for (int i = 0; i < 5; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            double d3 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d, d2, d3);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public SlotAccess getSlot(int n) {
        int n2 = n - 300;
        if (n2 >= 0 && n2 < this.inventory.getContainerSize()) {
            return SlotAccess.forContainer(this.inventory, n2);
        }
        return super.getSlot(n);
    }

    protected abstract void updateTrades();

    protected void addOffersFromItemListings(MerchantOffers merchantOffers, VillagerTrades.ItemListing[] itemListingArray, int n) {
        ArrayList arrayList = Lists.newArrayList((Object[])itemListingArray);
        int n2 = 0;
        while (n2 < n && !arrayList.isEmpty()) {
            MerchantOffer merchantOffer = ((VillagerTrades.ItemListing)arrayList.remove(this.random.nextInt(arrayList.size()))).getOffer(this, this.random);
            if (merchantOffer == null) continue;
            merchantOffers.add(merchantOffer);
            ++n2;
        }
    }

    @Override
    public Vec3 getRopeHoldPosition(float f) {
        float f2 = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180);
        Vec3 vec3 = new Vec3(0.0, this.getBoundingBox().getYsize() - 1.0, 0.2);
        return this.getPosition(f).add(vec3.yRot(-f2));
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.getTradingPlayer() == player && this.isAlive() && player.canInteractWithEntity(this, 4.0);
    }
}

