/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock
extends Block {
    public static final MapCodec<TntBlock> CODEC = TntBlock.simpleCodec(TntBlock::new);
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    public MapCodec<TntBlock> codec() {
        return CODEC;
    }

    public TntBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(UNSTABLE, false));
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (level.hasNeighborSignal(blockPos) && TntBlock.prime(level, blockPos)) {
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level.hasNeighborSignal(blockPos) && TntBlock.prime(level, blockPos)) {
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild && blockState.getValue(UNSTABLE).booleanValue()) {
            TntBlock.prime(level, blockPos);
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
            return;
        }
        PrimedTnt primedTnt = new PrimedTnt(serverLevel, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, explosion.getIndirectSourceEntity());
        int n = primedTnt.getFuse();
        primedTnt.setFuse((short)(serverLevel.random.nextInt(n / 4) + n / 8));
        serverLevel.addFreshEntity(primedTnt);
    }

    public static boolean prime(Level level, BlockPos blockPos) {
        return TntBlock.prime(level, blockPos, null);
    }

    private static boolean prime(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
        ServerLevel serverLevel;
        if (!(level instanceof ServerLevel) || !(serverLevel = (ServerLevel)level).getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
            return false;
        }
        PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, livingEntity);
        level.addFreshEntity(primedTnt);
        level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent((Entity)livingEntity, GameEvent.PRIME_FUSE, blockPos);
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ServerLevel serverLevel;
        if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE)) {
            return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
        }
        if (TntBlock.prime(level, blockPos, player)) {
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            Item item = itemStack.getItem();
            if (itemStack.is(Items.FLINT_AND_STEEL)) {
                itemStack.hurtAndBreak(1, (LivingEntity)player, LivingEntity.getSlotForHand(interactionHand));
            } else {
                itemStack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(item));
        } else if (level instanceof ServerLevel && !(serverLevel = (ServerLevel)level).getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
            player.displayClientMessage(Component.translatable("block.minecraft.tnt.disabled"), true);
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.mayInteract(serverLevel, blockPos) && TntBlock.prime(level, blockPos, entity instanceof LivingEntity ? (LivingEntity)entity : null)) {
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE);
    }
}

