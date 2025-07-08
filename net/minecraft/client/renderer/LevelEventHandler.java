/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LevelEventHandler {
    private final Minecraft minecraft;
    private final Level level;
    private final LevelRenderer levelRenderer;
    private final Map<BlockPos, SoundInstance> playingJukeboxSongs = new HashMap<BlockPos, SoundInstance>();

    public LevelEventHandler(Minecraft minecraft, Level level, LevelRenderer levelRenderer) {
        this.minecraft = minecraft;
        this.level = level;
        this.levelRenderer = levelRenderer;
    }

    public void globalLevelEvent(int n, BlockPos blockPos, int n2) {
        switch (n) {
            case 1023: 
            case 1028: 
            case 1038: {
                Camera camera = this.minecraft.gameRenderer.getMainCamera();
                if (!camera.isInitialized()) break;
                Vec3 vec3 = Vec3.atCenterOf(blockPos).subtract(camera.getPosition()).normalize();
                Vec3 vec32 = camera.getPosition().add(vec3.scale(2.0));
                if (n == 1023) {
                    this.level.playLocalSound(vec32.x, vec32.y, vec32.z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                if (n == 1038) {
                    this.level.playLocalSound(vec32.x, vec32.y, vec32.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0f, 1.0f, false);
                    break;
                }
                this.level.playLocalSound(vec32.x, vec32.y, vec32.z, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0f, 1.0f, false);
            }
        }
    }

    public void levelEvent(int n, BlockPos blockPos, int n2) {
        RandomSource randomSource = this.level.random;
        switch (n) {
            case 1035: {
                this.level.playLocalSound(blockPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1033: {
                this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1034: {
                this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1032: {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, randomSource.nextFloat() * 0.4f + 0.8f, 0.25f));
                break;
            }
            case 1001: {
                this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 1000: {
                this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1049: {
                this.level.playLocalSound(blockPos, SoundEvents.CRAFTER_CRAFT, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1050: {
                this.level.playLocalSound(blockPos, SoundEvents.CRAFTER_FAIL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 1004: {
                this.level.playLocalSound(blockPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0f, 1.2f, false);
                break;
            }
            case 1002: {
                this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0f, 1.2f, false);
                break;
            }
            case 1051: {
                this.level.playLocalSound(blockPos, SoundEvents.WIND_CHARGE_THROW, SoundSource.BLOCKS, 0.5f, 0.4f / (this.level.getRandom().nextFloat() * 0.4f + 0.8f), false);
                break;
            }
            case 2010: {
                this.shootParticles(n2, blockPos, randomSource, ParticleTypes.WHITE_SMOKE);
                break;
            }
            case 2000: {
                this.shootParticles(n2, blockPos, randomSource, ParticleTypes.SMOKE);
                break;
            }
            case 2003: {
                double d = (double)blockPos.getX() + 0.5;
                double d2 = blockPos.getY();
                double d3 = (double)blockPos.getZ() + 0.5;
                for (int i = 0; i < 8; ++i) {
                    this.levelRenderer.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), d, d2, d3, randomSource.nextGaussian() * 0.15, randomSource.nextDouble() * 0.2, randomSource.nextGaussian() * 0.15);
                }
                for (double d4 = 0.0; d4 < Math.PI * 2; d4 += 0.15707963267948966) {
                    this.levelRenderer.addParticle(ParticleTypes.PORTAL, d + Math.cos(d4) * 5.0, d2 - 0.4, d3 + Math.sin(d4) * 5.0, Math.cos(d4) * -5.0, 0.0, Math.sin(d4) * -5.0);
                    this.levelRenderer.addParticle(ParticleTypes.PORTAL, d + Math.cos(d4) * 5.0, d2 - 0.4, d3 + Math.sin(d4) * 5.0, Math.cos(d4) * -7.0, 0.0, Math.sin(d4) * -7.0);
                }
                break;
            }
            case 2002: 
            case 2007: {
                Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                for (int i = 0; i < 8; ++i) {
                    this.levelRenderer.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), vec3.x, vec3.y, vec3.z, randomSource.nextGaussian() * 0.15, randomSource.nextDouble() * 0.2, randomSource.nextGaussian() * 0.15);
                }
                float f = (float)(n2 >> 16 & 0xFF) / 255.0f;
                float f2 = (float)(n2 >> 8 & 0xFF) / 255.0f;
                float f3 = (float)(n2 >> 0 & 0xFF) / 255.0f;
                SimpleParticleType simpleParticleType = n == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;
                for (int i = 0; i < 100; ++i) {
                    double d = randomSource.nextDouble() * 4.0;
                    double d5 = randomSource.nextDouble() * Math.PI * 2.0;
                    double d6 = Math.cos(d5) * d;
                    double d7 = 0.01 + randomSource.nextDouble() * 0.5;
                    double d8 = Math.sin(d5) * d;
                    Particle particle = this.levelRenderer.addParticleInternal(simpleParticleType, simpleParticleType.getType().getOverrideLimiter(), vec3.x + d6 * 0.1, vec3.y + 0.3, vec3.z + d8 * 0.1, d6, d7, d8);
                    if (particle == null) continue;
                    float f4 = 0.75f + randomSource.nextFloat() * 0.25f;
                    particle.setColor(f * f4, f2 * f4, f3 * f4);
                    particle.setPower((float)d);
                }
                this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2001: {
                BlockState blockState = Block.stateById(n2);
                if (!blockState.isAir()) {
                    SoundType soundType = blockState.getSoundType();
                    this.level.playLocalSound(blockPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f, false);
                }
                this.level.addDestroyBlockEffect(blockPos, blockState);
                break;
            }
            case 3008: {
                BlockState blockState = Block.stateById(n2);
                Block block = blockState.getBlock();
                if (block instanceof BrushableBlock) {
                    BrushableBlock brushableBlock = (BrushableBlock)block;
                    this.level.playLocalSound(blockPos, brushableBlock.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0f, 1.0f, false);
                }
                this.level.addDestroyBlockEffect(blockPos, blockState);
                break;
            }
            case 2004: {
                for (int i = 0; i < 20; ++i) {
                    double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
                    double d9 = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
                    double d10 = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
                    this.level.addParticle(ParticleTypes.SMOKE, d, d9, d10, 0.0, 0.0, 0.0);
                    this.level.addParticle(ParticleTypes.FLAME, d, d9, d10, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 3011: {
                TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource, TrialSpawner.FlameParticle.decode((int)n2).particleType);
                break;
            }
            case 3012: {
                this.level.playLocalSound(blockPos, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource, TrialSpawner.FlameParticle.decode((int)n2).particleType);
                break;
            }
            case 3021: {
                this.level.playLocalSound(blockPos, SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource, TrialSpawner.FlameParticle.decode((int)n2).particleType);
                break;
            }
            case 3013: {
                this.level.playLocalSound(blockPos, SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, n2, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER);
                break;
            }
            case 3019: {
                this.level.playLocalSound(blockPos, SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, n2, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS);
                break;
            }
            case 3020: {
                this.level.playLocalSound(blockPos, SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundSource.BLOCKS, n2 == 0 ? 0.3f : 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, 0, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS);
                TrialSpawner.addBecomeOminousParticles(this.level, blockPos, randomSource);
                break;
            }
            case 3014: {
                this.level.playLocalSound(blockPos, SoundEvents.TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                TrialSpawner.addEjectItemParticles(this.level, blockPos, randomSource);
                break;
            }
            case 3017: {
                TrialSpawner.addEjectItemParticles(this.level, blockPos, randomSource);
                break;
            }
            case 3015: {
                BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
                if (!(blockEntity instanceof VaultBlockEntity)) break;
                VaultBlockEntity vaultBlockEntity = (VaultBlockEntity)blockEntity;
                VaultBlockEntity.Client.emitActivationParticles(this.level, vaultBlockEntity.getBlockPos(), vaultBlockEntity.getBlockState(), vaultBlockEntity.getSharedData(), n2 == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME);
                this.level.playLocalSound(blockPos, SoundEvents.VAULT_ACTIVATE, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                break;
            }
            case 3016: {
                VaultBlockEntity.Client.emitDeactivationParticles(this.level, blockPos, n2 == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME);
                this.level.playLocalSound(blockPos, SoundEvents.VAULT_DEACTIVATE, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                break;
            }
            case 3018: {
                for (int i = 0; i < 10; ++i) {
                    double d = randomSource.nextGaussian() * 0.02;
                    double d11 = randomSource.nextGaussian() * 0.02;
                    double d12 = randomSource.nextGaussian() * 0.02;
                    this.level.addParticle(ParticleTypes.POOF, (double)blockPos.getX() + randomSource.nextDouble(), (double)blockPos.getY() + randomSource.nextDouble(), (double)blockPos.getZ() + randomSource.nextDouble(), d, d11, d12);
                }
                this.level.playLocalSound(blockPos, SoundEvents.COBWEB_PLACE, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, true);
                break;
            }
            case 1505: {
                BoneMealItem.addGrowthParticles(this.level, blockPos, n2);
                this.level.playLocalSound(blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 2011: {
                ParticleUtils.spawnParticleInBlock(this.level, blockPos, n2, ParticleTypes.HAPPY_VILLAGER);
                break;
            }
            case 2012: {
                ParticleUtils.spawnParticleInBlock(this.level, blockPos, n2, ParticleTypes.HAPPY_VILLAGER);
                break;
            }
            case 3009: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.EGG_CRACK, UniformInt.of(3, 6));
                break;
            }
            case 3002: {
                if (n2 >= 0 && n2 < Direction.Axis.VALUES.length) {
                    ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[n2], this.level, blockPos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
                    break;
                }
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
                break;
            }
            case 2013: {
                ParticleUtils.spawnSmashAttackParticles(this.level, blockPos, n2);
                break;
            }
            case 3006: {
                int n3 = n2 >> 6;
                if (n3 > 0) {
                    if (randomSource.nextFloat() < 0.3f + (float)n3 * 0.1f) {
                        float f = 0.15f + 0.02f * (float)n3 * (float)n3 * randomSource.nextFloat();
                        float f5 = 0.4f + 0.3f * (float)n3 * randomSource.nextFloat();
                        this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, f, f5, false);
                    }
                    byte by = (byte)(n2 & 0x3F);
                    UniformInt uniformInt = UniformInt.of(0, n3);
                    float f = 0.005f;
                    Supplier<Vec3> supplier = () -> new Vec3(Mth.nextDouble(randomSource, -0.005f, 0.005f), Mth.nextDouble(randomSource, -0.005f, 0.005f), Mth.nextDouble(randomSource, -0.005f, 0.005f));
                    if (by == 0) {
                        for (Direction direction : Direction.values()) {
                            float f6 = direction == Direction.DOWN ? (float)Math.PI : 0.0f;
                            double d = direction.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
                            ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(f6), uniformInt, direction, supplier, d);
                        }
                    } else {
                        for (Direction direction : MultifaceBlock.unpack(by)) {
                            float f7 = direction == Direction.UP ? (float)Math.PI : 0.0f;
                            double d = 0.35;
                            ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(f7), uniformInt, direction, supplier, 0.35);
                        }
                    }
                } else {
                    this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                    boolean bl = this.level.getBlockState(blockPos).isCollisionShapeFullBlock(this.level, blockPos);
                    int n4 = bl ? 40 : 20;
                    float f = bl ? 0.45f : 0.25f;
                    float f8 = 0.07f;
                    for (int i = 0; i < n4; ++i) {
                        float f9 = 2.0f * randomSource.nextFloat() - 1.0f;
                        float f10 = 2.0f * randomSource.nextFloat() - 1.0f;
                        float f11 = 2.0f * randomSource.nextFloat() - 1.0f;
                        this.level.addParticle(ParticleTypes.SCULK_CHARGE_POP, (double)blockPos.getX() + 0.5 + (double)(f9 * f), (double)blockPos.getY() + 0.5 + (double)(f10 * f), (double)blockPos.getZ() + 0.5 + (double)(f11 * f), f9 * 0.07f, f10 * 0.07f, f11 * 0.07f);
                    }
                }
                break;
            }
            case 3007: {
                boolean bl;
                for (int i = 0; i < 10; ++i) {
                    this.level.addParticle(new ShriekParticleOption(i * 5), (double)blockPos.getX() + 0.5, (double)blockPos.getY() + SculkShriekerBlock.TOP_Y, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
                }
                BlockState blockState = this.level.getBlockState(blockPos);
                boolean bl2 = bl = blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED) != false;
                if (bl) break;
                this.level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + SculkShriekerBlock.TOP_Y, (double)blockPos.getZ() + 0.5, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 2.0f, 0.6f + this.level.random.nextFloat() * 0.4f, false);
                break;
            }
            case 3003: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                this.level.playLocalSound(blockPos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                break;
            }
            case 3004: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
                break;
            }
            case 3005: {
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
                break;
            }
            case 2008: {
                this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
                break;
            }
            case 1500: {
                ComposterBlock.handleFill(this.level, blockPos, n2 > 0);
                break;
            }
            case 1504: {
                PointedDripstoneBlock.spawnDripParticle(this.level, blockPos, this.level.getBlockState(blockPos));
                break;
            }
            case 1501: {
                this.level.playLocalSound(blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8f, false);
                for (int i = 0; i < 8; ++i) {
                    this.level.addParticle(ParticleTypes.LARGE_SMOKE, (double)blockPos.getX() + randomSource.nextDouble(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + randomSource.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1502: {
                this.level.playLocalSound(blockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5f, 2.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8f, false);
                for (int i = 0; i < 5; ++i) {
                    double d = (double)blockPos.getX() + randomSource.nextDouble() * 0.6 + 0.2;
                    double d13 = (double)blockPos.getY() + randomSource.nextDouble() * 0.6 + 0.2;
                    double d14 = (double)blockPos.getZ() + randomSource.nextDouble() * 0.6 + 0.2;
                    this.level.addParticle(ParticleTypes.SMOKE, d, d13, d14, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1503: {
                this.level.playLocalSound(blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                for (int i = 0; i < 16; ++i) {
                    double d = (double)blockPos.getX() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
                    double d15 = (double)blockPos.getY() + 0.8125;
                    double d16 = (double)blockPos.getZ() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
                    this.level.addParticle(ParticleTypes.SMOKE, d, d15, d16, 0.0, 0.0, 0.0);
                }
                break;
            }
            case 2006: {
                for (int i = 0; i < 200; ++i) {
                    float f = randomSource.nextFloat() * 4.0f;
                    float f12 = randomSource.nextFloat() * ((float)Math.PI * 2);
                    double d = Mth.cos(f12) * f;
                    double d17 = 0.01 + randomSource.nextDouble() * 0.5;
                    double d18 = Mth.sin(f12) * f;
                    Particle particle = this.levelRenderer.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + d * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + d18 * 0.1, d, d17, d18);
                    if (particle == null) continue;
                    particle.setPower(f);
                }
                if (n2 != 1) break;
                this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 2009: {
                for (int i = 0; i < 8; ++i) {
                    this.level.addParticle(ParticleTypes.CLOUD, (double)blockPos.getX() + randomSource.nextDouble(), (double)blockPos.getY() + 1.2, (double)blockPos.getZ() + randomSource.nextDouble(), 0.0, 0.0, 0.0);
                }
                break;
            }
            case 1009: {
                if (n2 == 0) {
                    this.level.playLocalSound(blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8f, false);
                    break;
                }
                if (n2 != 1) break;
                this.level.playLocalSound(blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7f, 1.6f + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.4f, false);
                break;
            }
            case 1029: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1030: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, randomSource.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1044: {
                this.level.playLocalSound(blockPos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1031: {
                this.level.playLocalSound(blockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1039: {
                this.level.playLocalSound(blockPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1010: {
                this.level.registryAccess().lookupOrThrow(Registries.JUKEBOX_SONG).get(n2).ifPresent(reference -> this.playJukeboxSong((Holder<JukeboxSong>)reference, blockPos));
                break;
            }
            case 1011: {
                this.stopJukeboxSongAndNotifyNearby(blockPos);
                break;
            }
            case 1015: {
                this.level.playLocalSound(blockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1017: {
                this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1016: {
                this.level.playLocalSound(blockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1019: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1022: {
                this.level.playLocalSound(blockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1021: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1020: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1018: {
                this.level.playLocalSound(blockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1024: {
                this.level.playLocalSound(blockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1026: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1027: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1040: {
                this.level.playLocalSound(blockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1041: {
                this.level.playLocalSound(blockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1025: {
                this.level.playLocalSound(blockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
                break;
            }
            case 1042: {
                this.level.playLocalSound(blockPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1043: {
                this.level.playLocalSound(blockPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 3000: {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, true, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
                this.level.playLocalSound(blockPos, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0f, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
                break;
            }
            case 3001: {
                this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0f, 0.8f + this.level.random.nextFloat() * 0.3f, false);
                break;
            }
            case 1045: {
                this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1046: {
                this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1047: {
                this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0f, this.level.random.nextFloat() * 0.1f + 0.9f, false);
                break;
            }
            case 1048: {
                this.level.playLocalSound(blockPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f, false);
            }
        }
    }

    private void shootParticles(int n, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
        Direction direction = Direction.from3DDataValue(n);
        int n2 = direction.getStepX();
        int n3 = direction.getStepY();
        int n4 = direction.getStepZ();
        for (int i = 0; i < 10; ++i) {
            double d = randomSource.nextDouble() * 0.2 + 0.01;
            double d2 = (double)blockPos.getX() + (double)n2 * 0.6 + 0.5 + (double)n2 * 0.01 + (randomSource.nextDouble() - 0.5) * (double)n4 * 0.5;
            double d3 = (double)blockPos.getY() + (double)n3 * 0.6 + 0.5 + (double)n3 * 0.01 + (randomSource.nextDouble() - 0.5) * (double)n3 * 0.5;
            double d4 = (double)blockPos.getZ() + (double)n4 * 0.6 + 0.5 + (double)n4 * 0.01 + (randomSource.nextDouble() - 0.5) * (double)n2 * 0.5;
            double d5 = (double)n2 * d + randomSource.nextGaussian() * 0.01;
            double d6 = (double)n3 * d + randomSource.nextGaussian() * 0.01;
            double d7 = (double)n4 * d + randomSource.nextGaussian() * 0.01;
            this.levelRenderer.addParticle(simpleParticleType, d2, d3, d4, d5, d6, d7);
        }
    }

    private void playJukeboxSong(Holder<JukeboxSong> holder, BlockPos blockPos) {
        this.stopJukeboxSong(blockPos);
        JukeboxSong jukeboxSong = holder.value();
        SoundEvent soundEvent = jukeboxSong.soundEvent().value();
        SimpleSoundInstance simpleSoundInstance = SimpleSoundInstance.forJukeboxSong(soundEvent, Vec3.atCenterOf(blockPos));
        this.playingJukeboxSongs.put(blockPos, simpleSoundInstance);
        this.minecraft.getSoundManager().play(simpleSoundInstance);
        this.minecraft.gui.setNowPlaying(jukeboxSong.description());
        this.notifyNearbyEntities(this.level, blockPos, true);
    }

    private void stopJukeboxSong(BlockPos blockPos) {
        SoundInstance soundInstance = this.playingJukeboxSongs.remove(blockPos);
        if (soundInstance != null) {
            this.minecraft.getSoundManager().stop(soundInstance);
        }
    }

    private void stopJukeboxSongAndNotifyNearby(BlockPos blockPos) {
        this.stopJukeboxSong(blockPos);
        this.notifyNearbyEntities(this.level, blockPos, false);
    }

    private void notifyNearbyEntities(Level level, BlockPos blockPos, boolean bl) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(3.0));
        for (LivingEntity livingEntity : list) {
            livingEntity.setRecordPlayingNearby(blockPos, bl);
        }
    }
}

