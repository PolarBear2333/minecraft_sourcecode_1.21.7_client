/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerPlayerGameMode {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected ServerLevel level;
    protected final ServerPlayer player;
    private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
    @Nullable
    private GameType previousGameModeForPlayer;
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos = BlockPos.ZERO;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPos delayedDestroyPos = BlockPos.ZERO;
    private int delayedTickStart;
    private int lastSentState = -1;

    public ServerPlayerGameMode(ServerPlayer serverPlayer) {
        this.player = serverPlayer;
        this.level = serverPlayer.level();
    }

    public boolean changeGameModeForPlayer(GameType gameType) {
        if (gameType == this.gameModeForPlayer) {
            return false;
        }
        this.setGameModeForPlayer(gameType, this.previousGameModeForPlayer);
        this.player.onUpdateAbilities();
        this.level.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player));
        this.level.updateSleepingPlayerList();
        if (gameType == GameType.CREATIVE) {
            this.player.resetCurrentImpulseContext();
        }
        return true;
    }

    protected void setGameModeForPlayer(GameType gameType, @Nullable GameType gameType2) {
        this.previousGameModeForPlayer = gameType2;
        this.gameModeForPlayer = gameType;
        gameType.updatePlayerAbilities(this.player.getAbilities());
    }

    public GameType getGameModeForPlayer() {
        return this.gameModeForPlayer;
    }

    @Nullable
    public GameType getPreviousGameModeForPlayer() {
        return this.previousGameModeForPlayer;
    }

    public boolean isSurvival() {
        return this.gameModeForPlayer.isSurvival();
    }

    public boolean isCreative() {
        return this.gameModeForPlayer.isCreative();
    }

    public void tick() {
        ++this.gameTicks;
        if (this.hasDelayedDestroy) {
            BlockState blockState = this.level.getBlockState(this.delayedDestroyPos);
            if (blockState.isAir()) {
                this.hasDelayedDestroy = false;
            } else {
                float f = this.incrementDestroyProgress(blockState, this.delayedDestroyPos, this.delayedTickStart);
                if (f >= 1.0f) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(this.delayedDestroyPos);
                }
            }
        } else if (this.isDestroyingBlock) {
            BlockState blockState = this.level.getBlockState(this.destroyPos);
            if (blockState.isAir()) {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            } else {
                this.incrementDestroyProgress(blockState, this.destroyPos, this.destroyProgressStart);
            }
        }
    }

    private float incrementDestroyProgress(BlockState blockState, BlockPos blockPos, int n) {
        int n2 = this.gameTicks - n;
        float f = blockState.getDestroyProgress(this.player, this.player.level(), blockPos) * (float)(n2 + 1);
        int n3 = (int)(f * 10.0f);
        if (n3 != this.lastSentState) {
            this.level.destroyBlockProgress(this.player.getId(), blockPos, n3);
            this.lastSentState = n3;
        }
        return f;
    }

    private void debugLogging(BlockPos blockPos, boolean bl, int n, String string) {
    }

    public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int n, int n2) {
        if (!this.player.canInteractWithBlock(blockPos, 1.0)) {
            this.debugLogging(blockPos, false, n2, "too far");
            return;
        }
        if (blockPos.getY() > n) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, n2, "too high");
            return;
        }
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            if (!this.level.mayInteract(this.player, blockPos)) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
                this.debugLogging(blockPos, false, n2, "may not interact");
                return;
            }
            if (this.player.getAbilities().instabuild) {
                this.destroyAndAck(blockPos, n2, "creative destroy");
                return;
            }
            if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
                this.debugLogging(blockPos, false, n2, "block action restricted");
                return;
            }
            this.destroyProgressStart = this.gameTicks;
            float f = 1.0f;
            BlockState blockState = this.level.getBlockState(blockPos);
            if (!blockState.isAir()) {
                EnchantmentHelper.onHitBlock(this.level, this.player.getMainHandItem(), this.player, this.player, EquipmentSlot.MAINHAND, Vec3.atCenterOf(blockPos), blockState, item -> this.player.onEquippedItemBroken((Item)item, EquipmentSlot.MAINHAND));
                blockState.attack(this.level, blockPos, this.player);
                f = blockState.getDestroyProgress(this.player, this.player.level(), blockPos);
            }
            if (!blockState.isAir() && f >= 1.0f) {
                this.destroyAndAck(blockPos, n2, "insta mine");
            } else {
                if (this.isDestroyingBlock) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                    this.debugLogging(blockPos, false, n2, "abort destroying since another started (client insta mine, server disagreed)");
                }
                this.isDestroyingBlock = true;
                this.destroyPos = blockPos.immutable();
                int n3 = (int)(f * 10.0f);
                this.level.destroyBlockProgress(this.player.getId(), blockPos, n3);
                this.debugLogging(blockPos, true, n2, "actual start of destroying");
                this.lastSentState = n3;
            }
        } else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            if (blockPos.equals(this.destroyPos)) {
                int n4 = this.gameTicks - this.destroyProgressStart;
                BlockState blockState = this.level.getBlockState(blockPos);
                if (!blockState.isAir()) {
                    float f = blockState.getDestroyProgress(this.player, this.player.level(), blockPos) * (float)(n4 + 1);
                    if (f >= 0.7f) {
                        this.isDestroyingBlock = false;
                        this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
                        this.destroyAndAck(blockPos, n2, "destroyed");
                        return;
                    }
                    if (!this.hasDelayedDestroy) {
                        this.isDestroyingBlock = false;
                        this.hasDelayedDestroy = true;
                        this.delayedDestroyPos = blockPos;
                        this.delayedTickStart = this.destroyProgressStart;
                    }
                }
            }
            this.debugLogging(blockPos, true, n2, "stopped destroying");
        } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            this.isDestroyingBlock = false;
            if (!Objects.equals(this.destroyPos, blockPos)) {
                LOGGER.warn("Mismatch in destroy block pos: {} {}", (Object)this.destroyPos, (Object)blockPos);
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.debugLogging(blockPos, true, n2, "aborted mismatched destroying");
            }
            this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
            this.debugLogging(blockPos, true, n2, "aborted destroying");
        }
    }

    public void destroyAndAck(BlockPos blockPos, int n, String string) {
        if (this.destroyBlock(blockPos)) {
            this.debugLogging(blockPos, true, n, string);
        } else {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, n, string);
        }
    }

    public boolean destroyBlock(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (!this.player.getMainHandItem().canDestroyBlock(blockState, this.level, blockPos, this.player)) {
            return false;
        }
        BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
            this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            return false;
        }
        if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
            return false;
        }
        BlockState blockState2 = block.playerWillDestroy(this.level, blockPos, blockState, this.player);
        boolean bl = this.level.removeBlock(blockPos, false);
        if (bl) {
            block.destroy(this.level, blockPos, blockState2);
        }
        if (this.player.preventsBlockDrops()) {
            return true;
        }
        ItemStack itemStack = this.player.getMainHandItem();
        ItemStack itemStack2 = itemStack.copy();
        boolean bl2 = this.player.hasCorrectToolForDrops(blockState2);
        itemStack.mineBlock(this.level, blockState2, blockPos, this.player);
        if (bl && bl2) {
            block.playerDestroy(this.level, this.player, blockPos, blockState2, blockEntity, itemStack2);
        }
        return true;
    }

    public InteractionResult useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand) {
        ItemStack itemStack2;
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        }
        if (serverPlayer.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.PASS;
        }
        int n = itemStack.getCount();
        int n2 = itemStack.getDamageValue();
        InteractionResult interactionResult = itemStack.use(level, serverPlayer, interactionHand);
        if (interactionResult instanceof InteractionResult.Success) {
            InteractionResult.Success success = (InteractionResult.Success)interactionResult;
            itemStack2 = Objects.requireNonNullElse(success.heldItemTransformedTo(), serverPlayer.getItemInHand(interactionHand));
        } else {
            itemStack2 = serverPlayer.getItemInHand(interactionHand);
        }
        if (itemStack2 == itemStack && itemStack2.getCount() == n && itemStack2.getUseDuration(serverPlayer) <= 0 && itemStack2.getDamageValue() == n2) {
            return interactionResult;
        }
        if (interactionResult instanceof InteractionResult.Fail && itemStack2.getUseDuration(serverPlayer) > 0 && !serverPlayer.isUsingItem()) {
            return interactionResult;
        }
        if (itemStack != itemStack2) {
            serverPlayer.setItemInHand(interactionHand, itemStack2);
        }
        if (itemStack2.isEmpty()) {
            serverPlayer.setItemInHand(interactionHand, ItemStack.EMPTY);
        }
        if (!serverPlayer.isUsingItem()) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }
        return interactionResult;
    }

    public InteractionResult useItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        InteractionResult interactionResult;
        Object object;
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.getBlock().isEnabled(level.enabledFeatures())) {
            return InteractionResult.FAIL;
        }
        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider menuProvider = blockState.getMenuProvider(level, blockPos);
            if (menuProvider != null) {
                serverPlayer.openMenu(menuProvider);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        boolean bl = !serverPlayer.getMainHandItem().isEmpty() || !serverPlayer.getOffhandItem().isEmpty();
        boolean bl2 = serverPlayer.isSecondaryUseActive() && bl;
        ItemStack itemStack2 = itemStack.copy();
        if (!bl2) {
            object = blockState.useItemOn(serverPlayer.getItemInHand(interactionHand), level, serverPlayer, interactionHand, blockHitResult);
            if (object.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
                return object;
            }
            if (object instanceof InteractionResult.TryEmptyHandInteraction && interactionHand == InteractionHand.MAIN_HAND && (interactionResult = blockState.useWithoutItem(level, serverPlayer, blockHitResult)).consumesAction()) {
                CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(serverPlayer, blockPos);
                return interactionResult;
            }
        }
        if (itemStack.isEmpty() || serverPlayer.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.PASS;
        }
        object = new UseOnContext(serverPlayer, interactionHand, blockHitResult);
        if (serverPlayer.hasInfiniteMaterials()) {
            int n = itemStack.getCount();
            interactionResult = itemStack.useOn((UseOnContext)object);
            itemStack.setCount(n);
        } else {
            interactionResult = itemStack.useOn((UseOnContext)object);
        }
        if (interactionResult.consumesAction()) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
        }
        return interactionResult;
    }

    public void setLevel(ServerLevel serverLevel) {
        this.level = serverLevel;
    }
}

