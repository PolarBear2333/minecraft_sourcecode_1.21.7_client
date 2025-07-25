/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeehiveBlock
extends BaseEntityBlock {
    public static final MapCodec<BeehiveBlock> CODEC = BeehiveBlock.simpleCodec(BeehiveBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;
    public static final int MAX_HONEY_LEVELS = 5;
    private static final int SHEARED_HONEYCOMB_COUNT = 3;

    public MapCodec<BeehiveBlock> codec() {
        return CODEC;
    }

    public BeehiveBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(HONEY_LEVEL, 0)).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return blockState.getValue(HONEY_LEVEL);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        if (!level.isClientSide && blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            if (!EnchantmentHelper.hasTag(itemStack, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)) {
                beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                Containers.updateNeighboursAfterDestroy(blockState, level, blockPos);
                this.angerNearbyBees(level, blockPos);
            }
            CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)player, blockState, itemStack, beehiveBlockEntity.getOccupantCount());
        }
    }

    @Override
    protected void onExplosionHit(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        super.onExplosionHit(blockState, serverLevel, blockPos, explosion, biConsumer);
        this.angerNearbyBees(serverLevel, blockPos);
    }

    private void angerNearbyBees(Level level, BlockPos blockPos) {
        AABB aABB = new AABB(blockPos).inflate(8.0, 6.0, 8.0);
        List<Bee> list = level.getEntitiesOfClass(Bee.class, aABB);
        if (!list.isEmpty()) {
            List<Player> list2 = level.getEntitiesOfClass(Player.class, aABB);
            if (list2.isEmpty()) {
                return;
            }
            for (Bee bee : list) {
                if (bee.getTarget() != null) continue;
                Player player = Util.getRandom(list2, level.random);
                bee.setTarget(player);
            }
        }
    }

    public static void dropHoneycomb(Level level, BlockPos blockPos) {
        BeehiveBlock.popResource(level, blockPos, new ItemStack(Items.HONEYCOMB, 3));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int n = blockState.getValue(HONEY_LEVEL);
        boolean bl = false;
        if (n >= 5) {
            Item item = itemStack.getItem();
            if (itemStack.is(Items.SHEARS)) {
                level.playSound((Entity)player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
                BeehiveBlock.dropHoneycomb(level, blockPos);
                itemStack.hurtAndBreak(1, (LivingEntity)player, LivingEntity.getSlotForHand(interactionHand));
                bl = true;
                level.gameEvent((Entity)player, GameEvent.SHEAR, blockPos);
            } else if (itemStack.is(Items.GLASS_BOTTLE)) {
                itemStack.shrink(1);
                level.playSound((Entity)player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (itemStack.isEmpty()) {
                    player.setItemInHand(interactionHand, new ItemStack(Items.HONEY_BOTTLE));
                } else if (!player.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
                    player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
                }
                bl = true;
                level.gameEvent((Entity)player, GameEvent.FLUID_PICKUP, blockPos);
            }
            if (!level.isClientSide() && bl) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }
        }
        if (bl) {
            if (!CampfireBlock.isSmokeyPos(level, blockPos)) {
                if (this.hiveContainsBees(level, blockPos)) {
                    this.angerNearbyBees(level, blockPos);
                }
                this.releaseBeesAndResetHoneyLevel(level, blockState, blockPos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            } else {
                this.resetHoneyLevel(level, blockState, blockPos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    private boolean hiveContainsBees(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            return !beehiveBlockEntity.isEmpty();
        }
        return false;
    }

    public void releaseBeesAndResetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos, @Nullable Player player, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus) {
        this.resetHoneyLevel(level, blockState, blockPos);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, beeReleaseStatus);
        }
    }

    public void resetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos) {
        level.setBlock(blockPos, (BlockState)blockState.setValue(HONEY_LEVEL, 0), 3);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(HONEY_LEVEL) >= 5) {
            for (int i = 0; i < randomSource.nextInt(1) + 1; ++i) {
                this.trySpawnDripParticles(level, blockPos, blockState);
            }
        }
    }

    private void trySpawnDripParticles(Level level, BlockPos blockPos, BlockState blockState) {
        if (!blockState.getFluidState().isEmpty() || level.random.nextFloat() < 0.3f) {
            return;
        }
        VoxelShape voxelShape = blockState.getCollisionShape(level, blockPos);
        double d = voxelShape.max(Direction.Axis.Y);
        if (d >= 1.0 && !blockState.is(BlockTags.IMPERMEABLE)) {
            double d2 = voxelShape.min(Direction.Axis.Y);
            if (d2 > 0.0) {
                this.spawnParticle(level, blockPos, voxelShape, (double)blockPos.getY() + d2 - 0.05);
            } else {
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState2 = level.getBlockState(blockPos2);
                VoxelShape voxelShape2 = blockState2.getCollisionShape(level, blockPos2);
                double d3 = voxelShape2.max(Direction.Axis.Y);
                if ((d3 < 1.0 || !blockState2.isCollisionShapeFullBlock(level, blockPos2)) && blockState2.getFluidState().isEmpty()) {
                    this.spawnParticle(level, blockPos, voxelShape, (double)blockPos.getY() - 0.05);
                }
            }
        }
    }

    private void spawnParticle(Level level, BlockPos blockPos, VoxelShape voxelShape, double d) {
        this.spawnFluidParticle(level, (double)blockPos.getX() + voxelShape.min(Direction.Axis.X), (double)blockPos.getX() + voxelShape.max(Direction.Axis.X), (double)blockPos.getZ() + voxelShape.min(Direction.Axis.Z), (double)blockPos.getZ() + voxelShape.max(Direction.Axis.Z), d);
    }

    private void spawnFluidParticle(Level level, double d, double d2, double d3, double d4, double d5) {
        level.addParticle(ParticleTypes.DRIPPING_HONEY, Mth.lerp(level.random.nextDouble(), d, d2), d5, Mth.lerp(level.random.nextDouble(), d3, d4), 0.0, 0.0, 0.0);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HONEY_LEVEL, FACING);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BeehiveBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : BeehiveBlock.createTickerHelper(blockEntityType, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (level instanceof ServerLevel) {
            BlockEntity blockEntity;
            ServerLevel serverLevel = (ServerLevel)level;
            if (player.preventsBlockDrops() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && (blockEntity = level.getBlockEntity(blockPos)) instanceof BeehiveBlockEntity) {
                boolean bl;
                BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
                int n = blockState.getValue(HONEY_LEVEL);
                boolean bl2 = bl = !beehiveBlockEntity.isEmpty();
                if (bl || n > 0) {
                    ItemStack itemStack = new ItemStack(this);
                    itemStack.applyComponents(beehiveBlockEntity.collectComponents());
                    itemStack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HONEY_LEVEL, n));
                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        BlockEntity blockEntity;
        Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if ((entity instanceof PrimedTnt || entity instanceof Creeper || entity instanceof WitherSkull || entity instanceof WitherBoss || entity instanceof MinecartTNT) && (blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)) instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            beehiveBlockEntity.emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        }
        return super.getDrops(blockState, builder);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        ItemStack itemStack = super.getCloneItemStack(levelReader, blockPos, blockState, bl);
        if (bl) {
            itemStack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HONEY_LEVEL, blockState.getValue(HONEY_LEVEL)));
        }
        return itemStack;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        BlockEntity blockEntity;
        if (levelReader.getBlockState(blockPos2).getBlock() instanceof FireBlock && (blockEntity = levelReader.getBlockEntity(blockPos)) instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            beehiveBlockEntity.emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }
}

