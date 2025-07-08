/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConduitBlockEntity
extends BlockEntity {
    private static final int BLOCK_REFRESH_RATE = 2;
    private static final int EFFECT_DURATION = 13;
    private static final float ROTATION_SPEED = -0.0375f;
    private static final int MIN_ACTIVE_SIZE = 16;
    private static final int MIN_KILL_SIZE = 42;
    private static final int KILL_RANGE = 8;
    private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
    public int tickCount;
    private float activeRotation;
    private boolean isActive;
    private boolean isHunting;
    private final List<BlockPos> effectBlocks = Lists.newArrayList();
    @Nullable
    private EntityReference<LivingEntity> destroyTarget;
    private long nextAmbientSoundActivation;

    public ConduitBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CONDUIT, blockPos, blockState);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.destroyTarget = EntityReference.read(valueInput, "Target");
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        EntityReference.store(this.destroyTarget, valueOutput, "Target");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity) {
        ++conduitBlockEntity.tickCount;
        long l = level.getGameTime();
        List<BlockPos> list = conduitBlockEntity.effectBlocks;
        if (l % 40L == 0L) {
            conduitBlockEntity.isActive = ConduitBlockEntity.updateShape(level, blockPos, list);
            ConduitBlockEntity.updateHunting(conduitBlockEntity, list);
        }
        LivingEntity livingEntity = EntityReference.get(conduitBlockEntity.destroyTarget, level, LivingEntity.class);
        ConduitBlockEntity.animationTick(level, blockPos, list, livingEntity, conduitBlockEntity.tickCount);
        if (conduitBlockEntity.isActive()) {
            conduitBlockEntity.activeRotation += 1.0f;
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity) {
        ++conduitBlockEntity.tickCount;
        long l = level.getGameTime();
        List<BlockPos> list = conduitBlockEntity.effectBlocks;
        if (l % 40L == 0L) {
            boolean bl = ConduitBlockEntity.updateShape(level, blockPos, list);
            if (bl != conduitBlockEntity.isActive) {
                SoundEvent soundEvent = bl ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE;
                level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            conduitBlockEntity.isActive = bl;
            ConduitBlockEntity.updateHunting(conduitBlockEntity, list);
            if (bl) {
                ConduitBlockEntity.applyEffects(level, blockPos, list);
                ConduitBlockEntity.updateAndAttackTarget((ServerLevel)level, blockPos, blockState, conduitBlockEntity, list.size() >= 42);
            }
        }
        if (conduitBlockEntity.isActive()) {
            if (l % 80L == 0L) {
                level.playSound(null, blockPos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            if (l > conduitBlockEntity.nextAmbientSoundActivation) {
                conduitBlockEntity.nextAmbientSoundActivation = l + 60L + (long)level.getRandom().nextInt(40);
                level.playSound(null, blockPos, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    private static void updateHunting(ConduitBlockEntity conduitBlockEntity, List<BlockPos> list) {
        conduitBlockEntity.setHunting(list.size() >= 42);
    }

    private static boolean updateShape(Level level, BlockPos blockPos, List<BlockPos> list) {
        int n;
        int n2;
        int n3;
        list.clear();
        for (n3 = -1; n3 <= 1; ++n3) {
            for (n2 = -1; n2 <= 1; ++n2) {
                for (n = -1; n <= 1; ++n) {
                    BlockPos blockPos2 = blockPos.offset(n3, n2, n);
                    if (level.isWaterAt(blockPos2)) continue;
                    return false;
                }
            }
        }
        for (n3 = -2; n3 <= 2; ++n3) {
            for (n2 = -2; n2 <= 2; ++n2) {
                for (n = -2; n <= 2; ++n) {
                    int n4 = Math.abs(n3);
                    int n5 = Math.abs(n2);
                    int n6 = Math.abs(n);
                    if (n4 <= 1 && n5 <= 1 && n6 <= 1 || (n3 != 0 || n5 != 2 && n6 != 2) && (n2 != 0 || n4 != 2 && n6 != 2) && (n != 0 || n4 != 2 && n5 != 2)) continue;
                    BlockPos blockPos3 = blockPos.offset(n3, n2, n);
                    BlockState blockState = level.getBlockState(blockPos3);
                    for (Block block : VALID_BLOCKS) {
                        if (!blockState.is(block)) continue;
                        list.add(blockPos3);
                    }
                }
            }
        }
        return list.size() >= 16;
    }

    private static void applyEffects(Level level, BlockPos blockPos, List<BlockPos> list) {
        int n;
        int n2;
        int n3 = list.size();
        int n4 = n3 / 7 * 16;
        int n5 = blockPos.getX();
        AABB aABB = new AABB(n5, n2 = blockPos.getY(), n = blockPos.getZ(), n5 + 1, n2 + 1, n + 1).inflate(n4).expandTowards(0.0, level.getHeight(), 0.0);
        List<Player> list2 = level.getEntitiesOfClass(Player.class, aABB);
        if (list2.isEmpty()) {
            return;
        }
        for (Player player : list2) {
            if (!blockPos.closerThan(player.blockPosition(), n4) || !player.isInWaterOrRain()) continue;
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
        }
    }

    private static void updateAndAttackTarget(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity, boolean bl) {
        EntityReference<LivingEntity> entityReference = ConduitBlockEntity.updateDestroyTarget(conduitBlockEntity.destroyTarget, serverLevel, blockPos, bl);
        LivingEntity livingEntity = EntityReference.get(entityReference, serverLevel, LivingEntity.class);
        if (livingEntity != null) {
            serverLevel.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0f, 1.0f);
            livingEntity.hurtServer(serverLevel, serverLevel.damageSources().magic(), 4.0f);
        }
        if (!Objects.equals(entityReference, conduitBlockEntity.destroyTarget)) {
            conduitBlockEntity.destroyTarget = entityReference;
            serverLevel.sendBlockUpdated(blockPos, blockState, blockState, 2);
        }
    }

    @Nullable
    private static EntityReference<LivingEntity> updateDestroyTarget(@Nullable EntityReference<LivingEntity> entityReference, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        if (!bl) {
            return null;
        }
        if (entityReference == null) {
            return ConduitBlockEntity.selectNewTarget(serverLevel, blockPos);
        }
        LivingEntity livingEntity = EntityReference.get(entityReference, serverLevel, LivingEntity.class);
        if (livingEntity == null || !livingEntity.isAlive() || !blockPos.closerThan(livingEntity.blockPosition(), 8.0)) {
            return null;
        }
        return entityReference;
    }

    @Nullable
    private static EntityReference<LivingEntity> selectNewTarget(ServerLevel serverLevel, BlockPos blockPos) {
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, ConduitBlockEntity.getDestroyRangeAABB(blockPos), livingEntity -> livingEntity instanceof Enemy && livingEntity.isInWaterOrRain());
        if (list.isEmpty()) {
            return null;
        }
        return new EntityReference<LivingEntity>(Util.getRandom(list, serverLevel.random));
    }

    private static AABB getDestroyRangeAABB(BlockPos blockPos) {
        return new AABB(blockPos).inflate(8.0);
    }

    private static void animationTick(Level level, BlockPos blockPos, List<BlockPos> list, @Nullable Entity entity, int n) {
        float f;
        RandomSource randomSource = level.random;
        double d = Mth.sin((float)(n + 35) * 0.1f) / 2.0f + 0.5f;
        d = (d * d + d) * (double)0.3f;
        Vec3 vec3 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.5 + d, (double)blockPos.getZ() + 0.5);
        for (BlockPos blockPos2 : list) {
            if (randomSource.nextInt(50) != 0) continue;
            BlockPos blockPos3 = blockPos2.subtract(blockPos);
            f = -0.5f + randomSource.nextFloat() + (float)blockPos3.getX();
            float f2 = -2.0f + randomSource.nextFloat() + (float)blockPos3.getY();
            float f3 = -0.5f + randomSource.nextFloat() + (float)blockPos3.getZ();
            level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, f, f2, f3);
        }
        if (entity != null) {
            Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            float f4 = (-0.5f + randomSource.nextFloat()) * (3.0f + entity.getBbWidth());
            float f5 = -1.0f + randomSource.nextFloat() * entity.getBbHeight();
            f = (-0.5f + randomSource.nextFloat()) * (3.0f + entity.getBbWidth());
            Vec3 vec33 = new Vec3(f4, f5, f);
            level.addParticle(ParticleTypes.NAUTILUS, vec32.x, vec32.y, vec32.z, vec33.x, vec33.y, vec33.z);
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isHunting() {
        return this.isHunting;
    }

    private void setHunting(boolean bl) {
        this.isHunting = bl;
    }

    public float getActiveRotation(float f) {
        return (this.activeRotation + f) * -0.0375f;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

