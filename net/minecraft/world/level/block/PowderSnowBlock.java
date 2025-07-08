/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PowderSnowBlock
extends Block
implements BucketPickup {
    public static final MapCodec<PowderSnowBlock> CODEC = PowderSnowBlock.simpleCodec(PowderSnowBlock::new);
    private static final float HORIZONTAL_PARTICLE_MOMENTUM_FACTOR = 0.083333336f;
    private static final float IN_BLOCK_HORIZONTAL_SPEED_MULTIPLIER = 0.9f;
    private static final float IN_BLOCK_VERTICAL_SPEED_MULTIPLIER = 1.5f;
    private static final float NUM_BLOCKS_TO_FALL_INTO_BLOCK = 2.5f;
    private static final VoxelShape FALLING_COLLISION_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.9f, 1.0);
    private static final double MINIMUM_FALL_DISTANCE_FOR_SOUND = 4.0;
    private static final double MINIMUM_FALL_DISTANCE_FOR_BIG_SOUND = 7.0;

    public MapCodec<PowderSnowBlock> codec() {
        return CODEC;
    }

    public PowderSnowBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        if (blockState2.is(this)) {
            return true;
        }
        return super.skipRendering(blockState, blockState2, direction);
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        Object object;
        if (!(entity instanceof LivingEntity) || entity.getInBlockState().is(this)) {
            entity.makeStuckInBlock(blockState, new Vec3(0.9f, 1.5, 0.9f));
            if (level.isClientSide) {
                boolean bl;
                object = level.getRandom();
                boolean bl2 = bl = entity.xOld != entity.getX() || entity.zOld != entity.getZ();
                if (bl && object.nextBoolean()) {
                    level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX(), blockPos.getY() + 1, entity.getZ(), Mth.randomBetween((RandomSource)object, -1.0f, 1.0f) * 0.083333336f, 0.05f, Mth.randomBetween((RandomSource)object, -1.0f, 1.0f) * 0.083333336f);
                }
            }
        }
        object = blockPos.immutable();
        insideBlockEffectApplier.runBefore(InsideBlockEffectType.EXTINGUISH, arg_0 -> PowderSnowBlock.lambda$entityInside$0(level, (BlockPos)object, arg_0));
        insideBlockEffectApplier.apply(InsideBlockEffectType.FREEZE);
        insideBlockEffectApplier.apply(InsideBlockEffectType.EXTINGUISH);
    }

    @Override
    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, double d) {
        if (d < 4.0 || !(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        LivingEntity.Fallsounds fallsounds = livingEntity.getFallSounds();
        SoundEvent soundEvent = d < 7.0 ? fallsounds.small() : fallsounds.big();
        entity.playSound(soundEvent, 1.0f, 1.0f);
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Entity entity) {
        VoxelShape voxelShape = this.getCollisionShape(blockState, blockGetter, blockPos, CollisionContext.of(entity));
        return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        EntityCollisionContext entityCollisionContext;
        Entity entity;
        if (!collisionContext.isPlacement() && collisionContext instanceof EntityCollisionContext && (entity = (entityCollisionContext = (EntityCollisionContext)collisionContext).getEntity()) != null) {
            if (entity.fallDistance > 2.5) {
                return FALLING_COLLISION_SHAPE;
            }
            boolean bl = entity instanceof FallingBlockEntity;
            if (bl || PowderSnowBlock.canEntityWalkOnPowderSnow(entity) && collisionContext.isAbove(Shapes.block(), blockPos, false) && !collisionContext.isDescending()) {
                return super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
            }
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    public static boolean canEntityWalkOnPowderSnow(Entity entity) {
        if (entity.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
            return true;
        }
        if (entity instanceof LivingEntity) {
            return ((LivingEntity)entity).getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS);
        }
        return false;
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity livingEntity, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
        if (!levelAccessor.isClientSide()) {
            levelAccessor.levelEvent(2001, blockPos, Block.getId(blockState));
        }
        return new ItemStack(Items.POWDER_SNOW_BUCKET);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return true;
    }

    private static /* synthetic */ void lambda$entityInside$0(Level level, BlockPos blockPos, Entity entity) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (entity.isOnFire() && (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || entity instanceof Player) && entity.mayInteract(serverLevel, blockPos)) {
                level.destroyBlock(blockPos, false);
            }
        }
    }
}

