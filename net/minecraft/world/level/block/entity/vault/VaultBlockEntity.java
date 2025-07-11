/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity.vault;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class VaultBlockEntity
extends BlockEntity {
    private final VaultServerData serverData = new VaultServerData();
    private final VaultSharedData sharedData = new VaultSharedData();
    private final VaultClientData clientData = new VaultClientData();
    private VaultConfig config = VaultConfig.DEFAULT;

    public VaultBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.VAULT, blockPos, blockState);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return Util.make(new CompoundTag(), compoundTag -> compoundTag.store("shared_data", VaultSharedData.CODEC, provider.createSerializationContext(NbtOps.INSTANCE), this.sharedData));
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.store("config", VaultConfig.CODEC, this.config);
        valueOutput.store("shared_data", VaultSharedData.CODEC, this.sharedData);
        valueOutput.store("server_data", VaultServerData.CODEC, this.serverData);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        valueInput.read("server_data", VaultServerData.CODEC).ifPresent(this.serverData::set);
        this.config = valueInput.read("config", VaultConfig.CODEC).orElse(VaultConfig.DEFAULT);
        valueInput.read("shared_data", VaultSharedData.CODEC).ifPresent(this.sharedData::set);
    }

    @Nullable
    public VaultServerData getServerData() {
        return this.level == null || this.level.isClientSide ? null : this.serverData;
    }

    public VaultSharedData getSharedData() {
        return this.sharedData;
    }

    public VaultClientData getClientData() {
        return this.clientData;
    }

    public VaultConfig getConfig() {
        return this.config;
    }

    @VisibleForTesting
    public void setConfig(VaultConfig vaultConfig) {
        this.config = vaultConfig;
    }

    public static final class Client {
        private static final int PARTICLE_TICK_RATE = 20;
        private static final float IDLE_PARTICLE_CHANCE = 0.5f;
        private static final float AMBIENT_SOUND_CHANCE = 0.02f;
        private static final int ACTIVATION_PARTICLE_COUNT = 20;
        private static final int DEACTIVATION_PARTICLE_COUNT = 20;

        public static void tick(Level level, BlockPos blockPos, BlockState blockState, VaultClientData vaultClientData, VaultSharedData vaultSharedData) {
            vaultClientData.updateDisplayItemSpin();
            if (level.getGameTime() % 20L == 0L) {
                Client.emitConnectionParticlesForNearbyPlayers(level, blockPos, blockState, vaultSharedData);
            }
            Client.emitIdleParticles(level, blockPos, vaultSharedData, blockState.getValue(VaultBlock.OMINOUS) != false ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
            Client.playIdleSounds(level, blockPos, vaultSharedData);
        }

        public static void emitActivationParticles(Level level, BlockPos blockPos, BlockState blockState, VaultSharedData vaultSharedData, ParticleOptions particleOptions) {
            Client.emitConnectionParticlesForNearbyPlayers(level, blockPos, blockState, vaultSharedData);
            RandomSource randomSource = level.random;
            for (int i = 0; i < 20; ++i) {
                Vec3 vec3 = Client.randomPosInsideCage(blockPos, randomSource);
                level.addParticle(ParticleTypes.SMOKE, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
                level.addParticle(particleOptions, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
            }
        }

        public static void emitDeactivationParticles(Level level, BlockPos blockPos, ParticleOptions particleOptions) {
            RandomSource randomSource = level.random;
            for (int i = 0; i < 20; ++i) {
                Vec3 vec3 = Client.randomPosCenterOfCage(blockPos, randomSource);
                Vec3 vec32 = new Vec3(randomSource.nextGaussian() * 0.02, randomSource.nextGaussian() * 0.02, randomSource.nextGaussian() * 0.02);
                level.addParticle(particleOptions, vec3.x(), vec3.y(), vec3.z(), vec32.x(), vec32.y(), vec32.z());
            }
        }

        private static void emitIdleParticles(Level level, BlockPos blockPos, VaultSharedData vaultSharedData, ParticleOptions particleOptions) {
            RandomSource randomSource = level.getRandom();
            if (randomSource.nextFloat() <= 0.5f) {
                Vec3 vec3 = Client.randomPosInsideCage(blockPos, randomSource);
                level.addParticle(ParticleTypes.SMOKE, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
                if (Client.shouldDisplayActiveEffects(vaultSharedData)) {
                    level.addParticle(particleOptions, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
                }
            }
        }

        private static void emitConnectionParticlesForPlayer(Level level, Vec3 vec3, Player player) {
            RandomSource randomSource = level.random;
            Vec3 vec32 = vec3.vectorTo(player.position().add(0.0, player.getBbHeight() / 2.0f, 0.0));
            int n = Mth.nextInt(randomSource, 2, 5);
            for (int i = 0; i < n; ++i) {
                Vec3 vec33 = vec32.offsetRandom(randomSource, 1.0f);
                level.addParticle(ParticleTypes.VAULT_CONNECTION, vec3.x(), vec3.y(), vec3.z(), vec33.x(), vec33.y(), vec33.z());
            }
        }

        private static void emitConnectionParticlesForNearbyPlayers(Level level, BlockPos blockPos, BlockState blockState, VaultSharedData vaultSharedData) {
            Set<UUID> set = vaultSharedData.getConnectedPlayers();
            if (set.isEmpty()) {
                return;
            }
            Vec3 vec3 = Client.keyholePos(blockPos, blockState.getValue(VaultBlock.FACING));
            for (UUID uUID : set) {
                Player player = level.getPlayerByUUID(uUID);
                if (player == null || !Client.isWithinConnectionRange(blockPos, vaultSharedData, player)) continue;
                Client.emitConnectionParticlesForPlayer(level, vec3, player);
            }
        }

        private static boolean isWithinConnectionRange(BlockPos blockPos, VaultSharedData vaultSharedData, Player player) {
            return player.blockPosition().distSqr(blockPos) <= Mth.square(vaultSharedData.connectedParticlesRange());
        }

        private static void playIdleSounds(Level level, BlockPos blockPos, VaultSharedData vaultSharedData) {
            if (!Client.shouldDisplayActiveEffects(vaultSharedData)) {
                return;
            }
            RandomSource randomSource = level.getRandom();
            if (randomSource.nextFloat() <= 0.02f) {
                level.playLocalSound(blockPos, SoundEvents.VAULT_AMBIENT, SoundSource.BLOCKS, randomSource.nextFloat() * 0.25f + 0.75f, randomSource.nextFloat() + 0.5f, false);
            }
        }

        public static boolean shouldDisplayActiveEffects(VaultSharedData vaultSharedData) {
            return vaultSharedData.hasDisplayItem();
        }

        private static Vec3 randomPosCenterOfCage(BlockPos blockPos, RandomSource randomSource) {
            return Vec3.atLowerCornerOf(blockPos).add(Mth.nextDouble(randomSource, 0.4, 0.6), Mth.nextDouble(randomSource, 0.4, 0.6), Mth.nextDouble(randomSource, 0.4, 0.6));
        }

        private static Vec3 randomPosInsideCage(BlockPos blockPos, RandomSource randomSource) {
            return Vec3.atLowerCornerOf(blockPos).add(Mth.nextDouble(randomSource, 0.1, 0.9), Mth.nextDouble(randomSource, 0.25, 0.75), Mth.nextDouble(randomSource, 0.1, 0.9));
        }

        private static Vec3 keyholePos(BlockPos blockPos, Direction direction) {
            return Vec3.atBottomCenterOf(blockPos).add((double)direction.getStepX() * 0.5, 1.75, (double)direction.getStepZ() * 0.5);
        }
    }

    public static final class Server {
        private static final int UNLOCKING_DELAY_TICKS = 14;
        private static final int DISPLAY_CYCLE_TICK_RATE = 20;
        private static final int INSERT_FAIL_SOUND_BUFFER_TICKS = 15;

        public static void tick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData) {
            VaultState vaultState = blockState.getValue(VaultBlock.STATE);
            if (Server.shouldCycleDisplayItem(serverLevel.getGameTime(), vaultState)) {
                Server.cycleDisplayItemFromLootTable(serverLevel, vaultState, vaultConfig, vaultSharedData, blockPos);
            }
            BlockState blockState2 = blockState;
            if (serverLevel.getGameTime() >= vaultServerData.stateUpdatingResumesAt() && blockState != (blockState2 = (BlockState)blockState2.setValue(VaultBlock.STATE, vaultState.tickAndGetNext(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData)))) {
                Server.setVaultState(serverLevel, blockPos, blockState, blockState2, vaultConfig, vaultSharedData);
            }
            if (vaultServerData.isDirty || vaultSharedData.isDirty) {
                VaultBlockEntity.setChanged(serverLevel, blockPos, blockState);
                if (vaultSharedData.isDirty) {
                    serverLevel.sendBlockUpdated(blockPos, blockState, blockState2, 2);
                }
                vaultServerData.isDirty = false;
                vaultSharedData.isDirty = false;
            }
        }

        public static void tryInsertKey(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, Player player, ItemStack itemStack) {
            VaultState vaultState = blockState.getValue(VaultBlock.STATE);
            if (!Server.canEjectReward(vaultConfig, vaultState)) {
                return;
            }
            if (!Server.isValidToInsert(vaultConfig, itemStack)) {
                Server.playInsertFailSound(serverLevel, vaultServerData, blockPos, SoundEvents.VAULT_INSERT_ITEM_FAIL);
                return;
            }
            if (vaultServerData.hasRewardedPlayer(player)) {
                Server.playInsertFailSound(serverLevel, vaultServerData, blockPos, SoundEvents.VAULT_REJECT_REWARDED_PLAYER);
                return;
            }
            List<ItemStack> list = Server.resolveItemsToEject(serverLevel, vaultConfig, blockPos, player, itemStack);
            if (list.isEmpty()) {
                return;
            }
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            itemStack.consume(vaultConfig.keyItem().getCount(), player);
            Server.unlock(serverLevel, blockState, blockPos, vaultConfig, vaultServerData, vaultSharedData, list);
            vaultServerData.addToRewardedPlayers(player);
            vaultSharedData.updateConnectedPlayersWithinRange(serverLevel, blockPos, vaultServerData, vaultConfig, vaultConfig.deactivationRange());
        }

        static void setVaultState(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, BlockState blockState2, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
            VaultState vaultState = blockState.getValue(VaultBlock.STATE);
            VaultState vaultState2 = blockState2.getValue(VaultBlock.STATE);
            serverLevel.setBlock(blockPos, blockState2, 3);
            vaultState.onTransition(serverLevel, blockPos, vaultState2, vaultConfig, vaultSharedData, blockState2.getValue(VaultBlock.OMINOUS));
        }

        static void cycleDisplayItemFromLootTable(ServerLevel serverLevel, VaultState vaultState, VaultConfig vaultConfig, VaultSharedData vaultSharedData, BlockPos blockPos) {
            if (!Server.canEjectReward(vaultConfig, vaultState)) {
                vaultSharedData.setDisplayItem(ItemStack.EMPTY);
                return;
            }
            ItemStack itemStack = Server.getRandomDisplayItemFromLootTable(serverLevel, blockPos, vaultConfig.overrideLootTableToDisplay().orElse(vaultConfig.lootTable()));
            vaultSharedData.setDisplayItem(itemStack);
        }

        private static ItemStack getRandomDisplayItemFromLootTable(ServerLevel serverLevel, BlockPos blockPos, ResourceKey<LootTable> resourceKey) {
            LootParams lootParams;
            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
            ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).create(LootContextParamSets.VAULT), serverLevel.getRandom());
            if (objectArrayList.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return Util.getRandom(objectArrayList, serverLevel.getRandom());
        }

        private static void unlock(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, List<ItemStack> list) {
            vaultServerData.setItemsToEject(list);
            vaultSharedData.setDisplayItem(vaultServerData.getNextItemToEject());
            vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + 14L);
            Server.setVaultState(serverLevel, blockPos, blockState, (BlockState)blockState.setValue(VaultBlock.STATE, VaultState.UNLOCKING), vaultConfig, vaultSharedData);
        }

        private static List<ItemStack> resolveItemsToEject(ServerLevel serverLevel, VaultConfig vaultConfig, BlockPos blockPos, Player player, ItemStack itemStack) {
            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(vaultConfig.lootTable());
            LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.TOOL, itemStack).create(LootContextParamSets.VAULT);
            return lootTable.getRandomItems(lootParams);
        }

        private static boolean canEjectReward(VaultConfig vaultConfig, VaultState vaultState) {
            return !vaultConfig.keyItem().isEmpty() && vaultState != VaultState.INACTIVE;
        }

        private static boolean isValidToInsert(VaultConfig vaultConfig, ItemStack itemStack) {
            return ItemStack.isSameItemSameComponents(itemStack, vaultConfig.keyItem()) && itemStack.getCount() >= vaultConfig.keyItem().getCount();
        }

        private static boolean shouldCycleDisplayItem(long l, VaultState vaultState) {
            return l % 20L == 0L && vaultState == VaultState.ACTIVE;
        }

        private static void playInsertFailSound(ServerLevel serverLevel, VaultServerData vaultServerData, BlockPos blockPos, SoundEvent soundEvent) {
            if (serverLevel.getGameTime() >= vaultServerData.getLastInsertFailTimestamp() + 15L) {
                serverLevel.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS);
                vaultServerData.setLastInsertFailTimestamp(serverLevel.getGameTime());
            }
        }
    }
}

