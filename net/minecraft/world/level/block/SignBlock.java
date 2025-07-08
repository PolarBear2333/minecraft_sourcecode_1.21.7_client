/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock
extends BaseEntityBlock
implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 16.0);
    private final WoodType type;

    protected SignBlock(WoodType woodType, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = woodType;
    }

    protected abstract MapCodec<? extends SignBlock> codec();

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SignBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        boolean bl;
        SignApplicator signApplicator;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SignBlockEntity)) {
            return InteractionResult.PASS;
        }
        SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
        Object object = itemStack.getItem();
        blockEntity = object instanceof SignApplicator ? (signApplicator = (SignApplicator)object) : null;
        boolean bl2 = bl = blockEntity != null && player.mayBuild();
        if (!(level instanceof ServerLevel)) {
            return bl || signBlockEntity.isWaxed() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        object = (ServerLevel)level;
        if (!bl || signBlockEntity.isWaxed() || this.otherPlayerIsEditingSign(player, signBlockEntity)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        boolean bl3 = signBlockEntity.isFacingFrontText(player);
        if (blockEntity.canApplyToSign(signBlockEntity.getText(bl3), player) && blockEntity.tryApplyToSign((Level)object, signBlockEntity, bl3, player)) {
            signBlockEntity.executeClickCommandsIfPresent((ServerLevel)object, player, blockPos, bl3);
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            object.gameEvent(GameEvent.BLOCK_CHANGE, signBlockEntity.getBlockPos(), GameEvent.Context.of(player, signBlockEntity.getBlockState()));
            itemStack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        Object object = level.getBlockEntity(blockPos);
        if (!(object instanceof SignBlockEntity)) {
            return InteractionResult.PASS;
        }
        SignBlockEntity signBlockEntity = (SignBlockEntity)object;
        if (!(level instanceof ServerLevel)) {
            Util.pauseInIde(new IllegalStateException("Expected to only call this on server"));
            return InteractionResult.CONSUME;
        }
        object = (ServerLevel)level;
        boolean bl = signBlockEntity.isFacingFrontText(player);
        boolean bl2 = signBlockEntity.executeClickCommandsIfPresent((ServerLevel)object, player, blockPos, bl);
        if (signBlockEntity.isWaxed()) {
            object.playSound(null, signBlockEntity.getBlockPos(), signBlockEntity.getSignInteractionFailedSoundEvent(), SoundSource.BLOCKS);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (bl2) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!this.otherPlayerIsEditingSign(player, signBlockEntity) && player.mayBuild() && this.hasEditableText(player, signBlockEntity, bl)) {
            this.openTextEdit(player, signBlockEntity, bl);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    private boolean hasEditableText(Player player, SignBlockEntity signBlockEntity, boolean bl) {
        SignText signText = signBlockEntity.getText(bl);
        return Arrays.stream(signText.getMessages(player.isTextFilteringEnabled())).allMatch(component -> component.equals(CommonComponents.EMPTY) || component.getContents() instanceof PlainTextContents);
    }

    public abstract float getYRotationDegrees(BlockState var1);

    public Vec3 getSignHitboxCenterPosition(BlockState blockState) {
        return new Vec3(0.5, 0.5, 0.5);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    public WoodType type() {
        return this.type;
    }

    public static WoodType getWoodType(Block block) {
        WoodType woodType = block instanceof SignBlock ? ((SignBlock)block).type() : WoodType.OAK;
        return woodType;
    }

    public void openTextEdit(Player player, SignBlockEntity signBlockEntity, boolean bl) {
        signBlockEntity.setAllowedPlayerEditor(player.getUUID());
        player.openTextEdit(signBlockEntity, bl);
    }

    private boolean otherPlayerIsEditingSign(Player player, SignBlockEntity signBlockEntity) {
        UUID uUID = signBlockEntity.getPlayerWhoMayEdit();
        return uUID != null && !uUID.equals(player.getUUID());
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return SignBlock.createTickerHelper(blockEntityType, BlockEntityType.SIGN, SignBlockEntity::tick);
    }
}

