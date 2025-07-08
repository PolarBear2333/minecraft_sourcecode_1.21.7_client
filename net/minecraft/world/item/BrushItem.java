/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem
extends Item {
    public static final int ANIMATION_DURATION = 10;
    private static final int USE_DURATION = 200;

    public BrushItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        if (player != null && this.calculateHitResult(player).getType() == HitResult.Type.BLOCK) {
            player.startUsingItem(useOnContext.getHand());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 200;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int n) {
        boolean bl;
        BlockHitResult blockHitResult;
        Player player;
        block11: {
            block10: {
                if (n < 0 || !(livingEntity instanceof Player)) {
                    livingEntity.releaseUsingItem();
                    return;
                }
                player = (Player)livingEntity;
                HitResult hitResult = this.calculateHitResult(player);
                if (!(hitResult instanceof BlockHitResult)) break block10;
                blockHitResult = (BlockHitResult)hitResult;
                if (hitResult.getType() == HitResult.Type.BLOCK) break block11;
            }
            livingEntity.releaseUsingItem();
            return;
        }
        int n2 = this.getUseDuration(itemStack, livingEntity) - n + 1;
        boolean bl2 = bl = n2 % 10 == 5;
        if (bl) {
            SoundEvent soundEvent;
            Object object;
            Object object2;
            HumanoidArm humanoidArm;
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            HumanoidArm humanoidArm2 = humanoidArm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            if (blockState.shouldSpawnTerrainParticles() && blockState.getRenderShape() != RenderShape.INVISIBLE) {
                this.spawnDustParticles(level, blockHitResult, blockState, livingEntity.getViewVector(0.0f), humanoidArm);
            }
            if ((object2 = blockState.getBlock()) instanceof BrushableBlock) {
                object = (BrushableBlock)object2;
                soundEvent = ((BrushableBlock)object).getBrushSound();
            } else {
                soundEvent = SoundEvents.BRUSH_GENERIC;
            }
            level.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS);
            if (level instanceof ServerLevel) {
                boolean bl3;
                object = (ServerLevel)level;
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof BrushableBlockEntity && (bl3 = ((BrushableBlockEntity)(object2 = (BrushableBlockEntity)blockEntity)).brush(level.getGameTime(), (ServerLevel)object, player, blockHitResult.getDirection(), itemStack))) {
                    EquipmentSlot equipmentSlot = itemStack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                    itemStack.hurtAndBreak(1, (LivingEntity)player, equipmentSlot);
                }
            }
        }
    }

    private HitResult calculateHitResult(Player player) {
        return ProjectileUtil.getHitResultOnViewVector(player, EntitySelector.CAN_BE_PICKED, player.blockInteractionRange());
    }

    private void spawnDustParticles(Level level, BlockHitResult blockHitResult, BlockState blockState, Vec3 vec3, HumanoidArm humanoidArm) {
        double d = 3.0;
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        int n2 = level.getRandom().nextInt(7, 12);
        BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
        Direction direction = blockHitResult.getDirection();
        DustParticlesDelta dustParticlesDelta = DustParticlesDelta.fromDirection(vec3, direction);
        Vec3 vec32 = blockHitResult.getLocation();
        for (int i = 0; i < n2; ++i) {
            level.addParticle(blockParticleOption, vec32.x - (double)(direction == Direction.WEST ? 1.0E-6f : 0.0f), vec32.y, vec32.z - (double)(direction == Direction.NORTH ? 1.0E-6f : 0.0f), dustParticlesDelta.xd() * (double)n * 3.0 * level.getRandom().nextDouble(), 0.0, dustParticlesDelta.zd() * (double)n * 3.0 * level.getRandom().nextDouble());
        }
    }

    record DustParticlesDelta(double xd, double yd, double zd) {
        private static final double ALONG_SIDE_DELTA = 1.0;
        private static final double OUT_FROM_SIDE_DELTA = 0.1;

        public static DustParticlesDelta fromDirection(Vec3 vec3, Direction direction) {
            double d = 0.0;
            return switch (direction) {
                default -> throw new MatchException(null, null);
                case Direction.DOWN, Direction.UP -> new DustParticlesDelta(vec3.z(), 0.0, -vec3.x());
                case Direction.NORTH -> new DustParticlesDelta(1.0, 0.0, -0.1);
                case Direction.SOUTH -> new DustParticlesDelta(-1.0, 0.0, 0.1);
                case Direction.WEST -> new DustParticlesDelta(-0.1, 0.0, -1.0);
                case Direction.EAST -> new DustParticlesDelta(0.1, 0.0, 1.0);
            };
        }
    }
}

