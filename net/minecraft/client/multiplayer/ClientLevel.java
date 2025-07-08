/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.slf4j.Logger;

public class ClientLevel
extends Level
implements CacheSlot.Cleaner<ClientLevel> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Component DEFAULT_QUIT_MESSAGE = Component.translatable("multiplayer.status.quitting");
    private static final double FLUID_PARTICLE_SPAWN_OFFSET = 0.05;
    private static final int NORMAL_LIGHT_UPDATES_PER_FRAME = 10;
    private static final int LIGHT_UPDATE_QUEUE_SIZE_THRESHOLD = 1000;
    final EntityTickList tickingEntities = new EntityTickList();
    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<Entity>(Entity.class, new EntityCallbacks());
    private final ClientPacketListener connection;
    private final LevelRenderer levelRenderer;
    private final LevelEventHandler levelEventHandler;
    private final ClientLevelData clientLevelData;
    private final DimensionSpecialEffects effects;
    private final TickRateManager tickRateManager;
    private final Minecraft minecraft = Minecraft.getInstance();
    final List<AbstractClientPlayer> players = Lists.newArrayList();
    final List<EnderDragonPart> dragonParts = Lists.newArrayList();
    private final Map<MapId, MapItemSavedData> mapData = Maps.newHashMap();
    private static final int CLOUD_COLOR = -1;
    private int skyFlashTime;
    private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap(3), object2ObjectArrayMap -> {
        object2ObjectArrayMap.put((Object)BiomeColors.GRASS_COLOR_RESOLVER, (Object)new BlockTintCache(blockPos -> this.calculateBlockTint((BlockPos)blockPos, BiomeColors.GRASS_COLOR_RESOLVER)));
        object2ObjectArrayMap.put((Object)BiomeColors.FOLIAGE_COLOR_RESOLVER, (Object)new BlockTintCache(blockPos -> this.calculateBlockTint((BlockPos)blockPos, BiomeColors.FOLIAGE_COLOR_RESOLVER)));
        object2ObjectArrayMap.put((Object)BiomeColors.DRY_FOLIAGE_COLOR_RESOLVER, (Object)new BlockTintCache(blockPos -> this.calculateBlockTint((BlockPos)blockPos, BiomeColors.DRY_FOLIAGE_COLOR_RESOLVER)));
        object2ObjectArrayMap.put((Object)BiomeColors.WATER_COLOR_RESOLVER, (Object)new BlockTintCache(blockPos -> this.calculateBlockTint((BlockPos)blockPos, BiomeColors.WATER_COLOR_RESOLVER)));
    });
    private final ClientChunkCache chunkSource;
    private final Deque<Runnable> lightUpdateQueue = Queues.newArrayDeque();
    private int serverSimulationDistance;
    private final BlockStatePredictionHandler blockStatePredictionHandler = new BlockStatePredictionHandler();
    private final Set<BlockEntity> globallyRenderedBlockEntities = new ReferenceOpenHashSet();
    private final int seaLevel;
    private boolean tickDayTime;
    private static final Set<Item> MARKER_PARTICLE_ITEMS = Set.of(Items.BARRIER, Items.LIGHT);

    public void handleBlockChangedAck(int n) {
        this.blockStatePredictionHandler.endPredictionsUpTo(n, this);
    }

    @Override
    public void onBlockEntityAdded(BlockEntity blockEntity) {
        BlockEntityRenderer<BlockEntity> blockEntityRenderer = this.minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        if (blockEntityRenderer != null && blockEntityRenderer.shouldRenderOffScreen()) {
            this.globallyRenderedBlockEntities.add(blockEntity);
        }
    }

    public Set<BlockEntity> getGloballyRenderedBlockEntities() {
        return this.globallyRenderedBlockEntities;
    }

    public void setServerVerifiedBlockState(BlockPos blockPos, BlockState blockState, int n) {
        if (!this.blockStatePredictionHandler.updateKnownServerState(blockPos, blockState)) {
            super.setBlock(blockPos, blockState, n, 512);
        }
    }

    public void syncBlockState(BlockPos blockPos, BlockState blockState, Vec3 vec3) {
        BlockState blockState2 = this.getBlockState(blockPos);
        if (blockState2 != blockState) {
            this.setBlock(blockPos, blockState, 19);
            LocalPlayer localPlayer = this.minecraft.player;
            if (this == localPlayer.level() && localPlayer.isColliding(blockPos, blockState)) {
                localPlayer.absSnapTo(vec3.x, vec3.y, vec3.z);
            }
        }
    }

    BlockStatePredictionHandler getBlockStatePredictionHandler() {
        return this.blockStatePredictionHandler;
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int n, int n2) {
        if (this.blockStatePredictionHandler.isPredicting()) {
            BlockState blockState2 = this.getBlockState(blockPos);
            boolean bl = super.setBlock(blockPos, blockState, n, n2);
            if (bl) {
                this.blockStatePredictionHandler.retainKnownServerState(blockPos, blockState2, this.minecraft.player);
            }
            return bl;
        }
        return super.setBlock(blockPos, blockState, n, n2);
    }

    public ClientLevel(ClientPacketListener clientPacketListener, ClientLevelData clientLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, int n, int n2, LevelRenderer levelRenderer, boolean bl, long l, int n3) {
        super(clientLevelData, resourceKey, clientPacketListener.registryAccess(), holder, true, bl, l, 1000000);
        this.connection = clientPacketListener;
        this.chunkSource = new ClientChunkCache(this, n);
        this.tickRateManager = new TickRateManager();
        this.clientLevelData = clientLevelData;
        this.levelRenderer = levelRenderer;
        this.seaLevel = n3;
        this.levelEventHandler = new LevelEventHandler(this.minecraft, this, levelRenderer);
        this.effects = DimensionSpecialEffects.forType(holder.value());
        this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0f);
        this.serverSimulationDistance = n2;
        this.updateSkyBrightness();
        this.prepareWeather();
    }

    public void queueLightUpdate(Runnable runnable) {
        this.lightUpdateQueue.add(runnable);
    }

    public void pollLightUpdates() {
        Runnable runnable;
        int n = this.lightUpdateQueue.size();
        int n2 = n < 1000 ? Math.max(10, n / 10) : n;
        for (int i = 0; i < n2 && (runnable = this.lightUpdateQueue.poll()) != null; ++i) {
            runnable.run();
        }
    }

    public DimensionSpecialEffects effects() {
        return this.effects;
    }

    public void tick(BooleanSupplier booleanSupplier) {
        this.getWorldBorder().tick();
        this.updateSkyBrightness();
        if (this.tickRateManager().runsNormally()) {
            this.tickTime();
        }
        if (this.skyFlashTime > 0) {
            this.setSkyFlashTime(this.skyFlashTime - 1);
        }
        try (Zone zone = Profiler.get().zone("blocks");){
            this.chunkSource.tick(booleanSupplier, true);
        }
    }

    private void tickTime() {
        this.clientLevelData.setGameTime(this.clientLevelData.getGameTime() + 1L);
        if (this.tickDayTime) {
            this.clientLevelData.setDayTime(this.clientLevelData.getDayTime() + 1L);
        }
    }

    public void setTimeFromServer(long l, long l2, boolean bl) {
        this.clientLevelData.setGameTime(l);
        this.clientLevelData.setDayTime(l2);
        this.tickDayTime = bl;
    }

    public Iterable<Entity> entitiesForRendering() {
        return this.getEntities().getAll();
    }

    public void tickEntities() {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("entities");
        this.tickingEntities.forEach(entity -> {
            if (entity.isRemoved() || entity.isPassenger() || this.tickRateManager.isEntityFrozen((Entity)entity)) {
                return;
            }
            this.guardEntityTick(this::tickNonPassenger, entity);
        });
        profilerFiller.pop();
        this.tickBlockEntities();
    }

    public boolean isTickingEntity(Entity entity) {
        return this.tickingEntities.contains(entity);
    }

    @Override
    public boolean shouldTickDeath(Entity entity) {
        return entity.chunkPosition().getChessboardDistance(this.minecraft.player.chunkPosition()) <= this.serverSimulationDistance;
    }

    public void tickNonPassenger(Entity entity) {
        entity.setOldPosAndRot();
        ++entity.tickCount;
        Profiler.get().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        entity.tick();
        Profiler.get().pop();
        for (Entity entity2 : entity.getPassengers()) {
            this.tickPassenger(entity, entity2);
        }
    }

    private void tickPassenger(Entity entity, Entity entity2) {
        if (entity2.isRemoved() || entity2.getVehicle() != entity) {
            entity2.stopRiding();
            return;
        }
        if (!(entity2 instanceof Player) && !this.tickingEntities.contains(entity2)) {
            return;
        }
        entity2.setOldPosAndRot();
        ++entity2.tickCount;
        entity2.rideTick();
        for (Entity entity3 : entity2.getPassengers()) {
            this.tickPassenger(entity2, entity3);
        }
    }

    public void unload(LevelChunk levelChunk) {
        levelChunk.clearAllBlockEntities();
        this.chunkSource.getLightEngine().setLightEnabled(levelChunk.getPos(), false);
        this.entityStorage.stopTicking(levelChunk.getPos());
    }

    public void onChunkLoaded(ChunkPos chunkPos) {
        this.tintCaches.forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(chunkPos.x, chunkPos.z));
        this.entityStorage.startTicking(chunkPos);
    }

    public void onSectionBecomingNonEmpty(long l) {
        this.levelRenderer.onSectionBecomingNonEmpty(l);
    }

    public void clearTintCaches() {
        this.tintCaches.forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateAll());
    }

    @Override
    public boolean hasChunk(int n, int n2) {
        return true;
    }

    public int getEntityCount() {
        return this.entityStorage.count();
    }

    public void addEntity(Entity entity) {
        this.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
        this.entityStorage.addEntity(entity);
    }

    public void removeEntity(int n, Entity.RemovalReason removalReason) {
        Entity entity = this.getEntities().get(n);
        if (entity != null) {
            entity.setRemoved(removalReason);
            entity.onClientRemoval();
        }
    }

    @Override
    public List<Entity> getPushableEntities(Entity entity, AABB aABB) {
        LocalPlayer localPlayer = this.minecraft.player;
        if (localPlayer != null && localPlayer != entity && localPlayer.getBoundingBox().intersects(aABB) && EntitySelector.pushableBy(entity).test(localPlayer)) {
            return List.of(localPlayer);
        }
        return List.of();
    }

    @Override
    @Nullable
    public Entity getEntity(int n) {
        return this.getEntities().get(n);
    }

    public void disconnect(Component component) {
        this.connection.getConnection().disconnect(component);
    }

    public void animateTick(int n, int n2, int n3) {
        int n4 = 32;
        RandomSource randomSource = RandomSource.create();
        Block block = this.getMarkerParticleTarget();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 667; ++i) {
            this.doAnimateTick(n, n2, n3, 16, randomSource, block, mutableBlockPos);
            this.doAnimateTick(n, n2, n3, 32, randomSource, block, mutableBlockPos);
        }
    }

    @Nullable
    private Block getMarkerParticleTarget() {
        ItemStack itemStack;
        Item item;
        if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE && MARKER_PARTICLE_ITEMS.contains(item = (itemStack = this.minecraft.player.getMainHandItem()).getItem()) && item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            return blockItem.getBlock();
        }
        return null;
    }

    public void doAnimateTick(int n, int n2, int n3, int n4, RandomSource randomSource, @Nullable Block block, BlockPos.MutableBlockPos mutableBlockPos) {
        int n5 = n + this.random.nextInt(n4) - this.random.nextInt(n4);
        int n6 = n2 + this.random.nextInt(n4) - this.random.nextInt(n4);
        int n7 = n3 + this.random.nextInt(n4) - this.random.nextInt(n4);
        mutableBlockPos.set(n5, n6, n7);
        BlockState blockState = this.getBlockState(mutableBlockPos);
        blockState.getBlock().animateTick(blockState, this, mutableBlockPos, randomSource);
        FluidState fluidState = this.getFluidState(mutableBlockPos);
        if (!fluidState.isEmpty()) {
            fluidState.animateTick(this, mutableBlockPos, randomSource);
            ParticleOptions particleOptions = fluidState.getDripParticle();
            if (particleOptions != null && this.random.nextInt(10) == 0) {
                boolean bl = blockState.isFaceSturdy(this, mutableBlockPos, Direction.DOWN);
                Vec3i vec3i = mutableBlockPos.below();
                this.trySpawnDripParticles((BlockPos)vec3i, this.getBlockState((BlockPos)vec3i), particleOptions, bl);
            }
        }
        if (block == blockState.getBlock()) {
            this.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, blockState), (double)n5 + 0.5, (double)n6 + 0.5, (double)n7 + 0.5, 0.0, 0.0, 0.0);
        }
        if (!blockState.isCollisionShapeFullBlock(this, mutableBlockPos)) {
            this.getBiome(mutableBlockPos).value().getAmbientParticle().ifPresent(ambientParticleSettings -> {
                if (ambientParticleSettings.canSpawn(this.random)) {
                    this.addParticle(ambientParticleSettings.getOptions(), (double)mutableBlockPos.getX() + this.random.nextDouble(), (double)mutableBlockPos.getY() + this.random.nextDouble(), (double)mutableBlockPos.getZ() + this.random.nextDouble(), 0.0, 0.0, 0.0);
                }
            });
        }
    }

    private void trySpawnDripParticles(BlockPos blockPos, BlockState blockState, ParticleOptions particleOptions, boolean bl) {
        if (!blockState.getFluidState().isEmpty()) {
            return;
        }
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos);
        double d = voxelShape.max(Direction.Axis.Y);
        if (d < 1.0) {
            if (bl) {
                this.spawnFluidParticle(blockPos.getX(), blockPos.getX() + 1, blockPos.getZ(), blockPos.getZ() + 1, (double)(blockPos.getY() + 1) - 0.05, particleOptions);
            }
        } else if (!blockState.is(BlockTags.IMPERMEABLE)) {
            double d2 = voxelShape.min(Direction.Axis.Y);
            if (d2 > 0.0) {
                this.spawnParticle(blockPos, particleOptions, voxelShape, (double)blockPos.getY() + d2 - 0.05);
            } else {
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState2 = this.getBlockState(blockPos2);
                VoxelShape voxelShape2 = blockState2.getCollisionShape(this, blockPos2);
                double d3 = voxelShape2.max(Direction.Axis.Y);
                if (d3 < 1.0 && blockState2.getFluidState().isEmpty()) {
                    this.spawnParticle(blockPos, particleOptions, voxelShape, (double)blockPos.getY() - 0.05);
                }
            }
        }
    }

    private void spawnParticle(BlockPos blockPos, ParticleOptions particleOptions, VoxelShape voxelShape, double d) {
        this.spawnFluidParticle((double)blockPos.getX() + voxelShape.min(Direction.Axis.X), (double)blockPos.getX() + voxelShape.max(Direction.Axis.X), (double)blockPos.getZ() + voxelShape.min(Direction.Axis.Z), (double)blockPos.getZ() + voxelShape.max(Direction.Axis.Z), d, particleOptions);
    }

    private void spawnFluidParticle(double d, double d2, double d3, double d4, double d5, ParticleOptions particleOptions) {
        this.addParticle(particleOptions, Mth.lerp(this.random.nextDouble(), d, d2), d5, Mth.lerp(this.random.nextDouble(), d3, d4), 0.0, 0.0, 0.0);
    }

    @Override
    public CrashReportCategory fillReportDetails(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = super.fillReportDetails(crashReport);
        crashReportCategory.setDetail("Server brand", () -> this.minecraft.player.connection.serverBrand());
        crashReportCategory.setDetail("Server type", () -> this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
        crashReportCategory.setDetail("Tracked entity count", () -> String.valueOf(this.getEntityCount()));
        return crashReportCategory;
    }

    @Override
    public void playSeededSound(@Nullable Entity entity, double d, double d2, double d3, Holder<SoundEvent> holder, SoundSource soundSource, float f, float f2, long l) {
        if (entity == this.minecraft.player) {
            this.playSound(d, d2, d3, holder.value(), soundSource, f, f2, false, l);
        }
    }

    @Override
    public void playSeededSound(@Nullable Entity entity, Entity entity2, Holder<SoundEvent> holder, SoundSource soundSource, float f, float f2, long l) {
        if (entity == this.minecraft.player) {
            this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(holder.value(), soundSource, f, f2, entity2, l));
        }
    }

    @Override
    public void playLocalSound(Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(soundEvent, soundSource, f, f2, entity, this.random.nextLong()));
    }

    @Override
    public void playPlayerSound(SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        if (this.minecraft.player != null) {
            this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(soundEvent, soundSource, f, f2, this.minecraft.player, this.random.nextLong()));
        }
    }

    @Override
    public void playLocalSound(double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2, boolean bl) {
        this.playSound(d, d2, d3, soundEvent, soundSource, f, f2, bl, this.random.nextLong());
    }

    private void playSound(double d, double d2, double d3, SoundEvent soundEvent, SoundSource soundSource, float f, float f2, boolean bl, long l) {
        double d4 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(d, d2, d3);
        SimpleSoundInstance simpleSoundInstance = new SimpleSoundInstance(soundEvent, soundSource, f, f2, RandomSource.create(l), d, d2, d3);
        if (bl && d4 > 100.0) {
            double d5 = Math.sqrt(d4) / 40.0;
            this.minecraft.getSoundManager().playDelayed(simpleSoundInstance, (int)(d5 * 20.0));
        } else {
            this.minecraft.getSoundManager().play(simpleSoundInstance);
        }
    }

    @Override
    public void createFireworks(double d, double d2, double d3, double d4, double d5, double d6, List<FireworkExplosion> list) {
        if (list.isEmpty()) {
            for (int i = 0; i < this.random.nextInt(3) + 2; ++i) {
                this.addParticle(ParticleTypes.POOF, d, d2, d3, this.random.nextGaussian() * 0.05, 0.005, this.random.nextGaussian() * 0.05);
            }
        } else {
            this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, d, d2, d3, d4, d5, d6, this.minecraft.particleEngine, list));
        }
    }

    @Override
    public void sendPacketToServer(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public RecipeAccess recipeAccess() {
        return this.connection.recipes();
    }

    @Override
    public TickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public ClientChunkCache getChunkSource() {
        return this.chunkSource;
    }

    @Override
    @Nullable
    public MapItemSavedData getMapData(MapId mapId) {
        return this.mapData.get(mapId);
    }

    public void overrideMapData(MapId mapId, MapItemSavedData mapItemSavedData) {
        this.mapData.put(mapId, mapItemSavedData);
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.connection.scoreboard();
    }

    @Override
    public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int n) {
        this.levelRenderer.blockChanged(this, blockPos, blockState, blockState2, n);
    }

    @Override
    public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        this.levelRenderer.setBlockDirty(blockPos, blockState, blockState2);
    }

    public void setSectionDirtyWithNeighbors(int n, int n2, int n3) {
        this.levelRenderer.setSectionDirtyWithNeighbors(n, n2, n3);
    }

    public void setSectionRangeDirty(int n, int n2, int n3, int n4, int n5, int n6) {
        this.levelRenderer.setSectionRangeDirty(n, n2, n3, n4, n5, n6);
    }

    @Override
    public void destroyBlockProgress(int n, BlockPos blockPos, int n2) {
        this.levelRenderer.destroyBlockProgress(n, blockPos, n2);
    }

    @Override
    public void globalLevelEvent(int n, BlockPos blockPos, int n2) {
        this.levelEventHandler.globalLevelEvent(n, blockPos, n2);
    }

    @Override
    public void levelEvent(@Nullable Entity entity, int n, BlockPos blockPos, int n2) {
        try {
            this.levelEventHandler.levelEvent(n, blockPos, n2);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Playing level event");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Level event being played");
            crashReportCategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, blockPos));
            crashReportCategory.setDetail("Event source", entity);
            crashReportCategory.setDetail("Event type", n);
            crashReportCategory.setDetail("Event data", n2);
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
        this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter(), d, d2, d3, d4, d5, d6);
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double d2, double d3, double d4, double d5, double d6) {
        this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter() || bl, bl2, d, d2, d3, d4, d5, d6);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
        this.levelRenderer.addParticle(particleOptions, false, true, d, d2, d3, d4, d5, d6);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean bl, double d, double d2, double d3, double d4, double d5, double d6) {
        this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter() || bl, true, d, d2, d3, d4, d5, d6);
    }

    public List<AbstractClientPlayer> players() {
        return this.players;
    }

    public List<EnderDragonPart> dragonParts() {
        return this.dragonParts;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int n, int n2, int n3) {
        return this.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
    }

    public float getSkyDarken(float f) {
        float f2 = this.getTimeOfDay(f);
        float f3 = 1.0f - (Mth.cos(f2 * ((float)Math.PI * 2)) * 2.0f + 0.2f);
        f3 = Mth.clamp(f3, 0.0f, 1.0f);
        f3 = 1.0f - f3;
        f3 *= 1.0f - this.getRainLevel(f) * 5.0f / 16.0f;
        return (f3 *= 1.0f - this.getThunderLevel(f) * 5.0f / 16.0f) * 0.8f + 0.2f;
    }

    public int getSkyColor(Vec3 vec3, float f) {
        int n4;
        float f2;
        float f3;
        float f4 = this.getTimeOfDay(f);
        Vec3 vec32 = vec3.subtract(2.0, 2.0, 2.0).scale(0.25);
        Vec3 vec33 = CubicSampler.gaussianSampleVec3(vec32, (n, n2, n3) -> Vec3.fromRGB24(this.getBiomeManager().getNoiseBiomeAtQuart(n, n2, n3).value().getSkyColor()));
        float f5 = Mth.cos(f4 * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        f5 = Mth.clamp(f5, 0.0f, 1.0f);
        vec33 = vec33.scale(f5);
        int n5 = ARGB.color(vec33);
        float f6 = this.getRainLevel(f);
        if (f6 > 0.0f) {
            f3 = 0.6f;
            f2 = f6 * 0.75f;
            int n6 = ARGB.scaleRGB(ARGB.greyscale(n5), 0.6f);
            n5 = ARGB.lerp(f2, n5, n6);
        }
        if ((f3 = this.getThunderLevel(f)) > 0.0f) {
            f2 = 0.2f;
            float f7 = f3 * 0.75f;
            int n7 = ARGB.scaleRGB(ARGB.greyscale(n5), 0.2f);
            n5 = ARGB.lerp(f7, n5, n7);
        }
        if ((n4 = this.getSkyFlashTime()) > 0) {
            float f8 = Math.min((float)n4 - f, 1.0f);
            n5 = ARGB.lerp(f8 *= 0.45f, n5, ARGB.color(204, 204, 255));
        }
        return n5;
    }

    public int getCloudColor(float f) {
        int n = -1;
        float f2 = this.getRainLevel(f);
        if (f2 > 0.0f) {
            int n2 = ARGB.scaleRGB(ARGB.greyscale(n), 0.6f);
            n = ARGB.lerp(f2 * 0.95f, n, n2);
        }
        float f3 = this.getTimeOfDay(f);
        float f4 = Mth.cos(f3 * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        f4 = Mth.clamp(f4, 0.0f, 1.0f);
        n = ARGB.multiply(n, ARGB.colorFromFloat(1.0f, f4 * 0.9f + 0.1f, f4 * 0.9f + 0.1f, f4 * 0.85f + 0.15f));
        float f5 = this.getThunderLevel(f);
        if (f5 > 0.0f) {
            int n3 = ARGB.scaleRGB(ARGB.greyscale(n), 0.2f);
            n = ARGB.lerp(f5 * 0.95f, n, n3);
        }
        return n;
    }

    public float getStarBrightness(float f) {
        float f2 = this.getTimeOfDay(f);
        float f3 = 1.0f - (Mth.cos(f2 * ((float)Math.PI * 2)) * 2.0f + 0.25f);
        f3 = Mth.clamp(f3, 0.0f, 1.0f);
        return f3 * f3 * 0.5f;
    }

    public int getSkyFlashTime() {
        return this.minecraft.options.hideLightningFlash().get() != false ? 0 : this.skyFlashTime;
    }

    @Override
    public void setSkyFlashTime(int n) {
        this.skyFlashTime = n;
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        boolean bl2 = this.effects().constantAmbientLight();
        if (!bl) {
            return bl2 ? 0.9f : 1.0f;
        }
        switch (direction) {
            case DOWN: {
                return bl2 ? 0.9f : 0.5f;
            }
            case UP: {
                return bl2 ? 0.9f : 1.0f;
            }
            case NORTH: 
            case SOUTH: {
                return 0.8f;
            }
            case WEST: 
            case EAST: {
                return 0.6f;
            }
        }
        return 1.0f;
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        BlockTintCache blockTintCache = (BlockTintCache)this.tintCaches.get((Object)colorResolver);
        return blockTintCache.getColor(blockPos);
    }

    public int calculateBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        int n = Minecraft.getInstance().options.biomeBlendRadius().get();
        if (n == 0) {
            return colorResolver.getColor(this.getBiome(blockPos).value(), blockPos.getX(), blockPos.getZ());
        }
        int n2 = (n * 2 + 1) * (n * 2 + 1);
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        Cursor3D cursor3D = new Cursor3D(blockPos.getX() - n, blockPos.getY(), blockPos.getZ() - n, blockPos.getX() + n, blockPos.getY(), blockPos.getZ() + n);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        while (cursor3D.advance()) {
            mutableBlockPos.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
            int n6 = colorResolver.getColor(this.getBiome(mutableBlockPos).value(), mutableBlockPos.getX(), mutableBlockPos.getZ());
            n3 += (n6 & 0xFF0000) >> 16;
            n4 += (n6 & 0xFF00) >> 8;
            n5 += n6 & 0xFF;
        }
        return (n3 / n2 & 0xFF) << 16 | (n4 / n2 & 0xFF) << 8 | n5 / n2 & 0xFF;
    }

    public void setDefaultSpawnPos(BlockPos blockPos, float f) {
        this.levelData.setSpawn(blockPos, f);
    }

    public String toString() {
        return "ClientLevel";
    }

    @Override
    public ClientLevelData getLevelData() {
        return this.clientLevelData;
    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
    }

    protected Map<MapId, MapItemSavedData> getAllMapData() {
        return ImmutableMap.copyOf(this.mapData);
    }

    protected void addMapData(Map<MapId, MapItemSavedData> map) {
        this.mapData.putAll(map);
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return this.entityStorage.getEntityGetter();
    }

    @Override
    public String gatherChunkSourceStats() {
        return "Chunks[C] W: " + this.chunkSource.gatherStats() + " E: " + this.entityStorage.gatherStats();
    }

    @Override
    public void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState) {
        this.minecraft.particleEngine.destroy(blockPos, blockState);
    }

    public void setServerSimulationDistance(int n) {
        this.serverSimulationDistance = n;
    }

    public int getServerSimulationDistance() {
        return this.serverSimulationDistance;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.connection.enabledFeatures();
    }

    @Override
    public PotionBrewing potionBrewing() {
        return this.connection.potionBrewing();
    }

    @Override
    public FuelValues fuelValues() {
        return this.connection.fuelValues();
    }

    @Override
    public void explode(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double d2, double d3, float f, boolean bl, Level.ExplosionInteraction explosionInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, Holder<SoundEvent> holder) {
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    @Override
    public int getClientLeafTintColor(BlockPos blockPos) {
        return Minecraft.getInstance().getBlockColors().getColor(this.getBlockState(blockPos), this, blockPos, 0);
    }

    @Override
    public void registerForCleaning(CacheSlot<ClientLevel, ?> cacheSlot) {
        this.connection.registerForCleaning(cacheSlot);
    }

    @Override
    public /* synthetic */ LevelData getLevelData() {
        return this.getLevelData();
    }

    public /* synthetic */ Collection dragonParts() {
        return this.dragonParts();
    }

    @Override
    public /* synthetic */ ChunkSource getChunkSource() {
        return this.getChunkSource();
    }

    final class EntityCallbacks
    implements LevelCallback<Entity> {
        EntityCallbacks() {
        }

        @Override
        public void onCreated(Entity entity) {
        }

        @Override
        public void onDestroyed(Entity entity) {
        }

        @Override
        public void onTickingStart(Entity entity) {
            ClientLevel.this.tickingEntities.add(entity);
        }

        @Override
        public void onTickingEnd(Entity entity) {
            ClientLevel.this.tickingEntities.remove(entity);
        }

        @Override
        public void onTrackingStart(Entity entity) {
            Entity entity2 = entity;
            Objects.requireNonNull(entity2);
            Entity entity3 = entity2;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractClientPlayer.class, EnderDragon.class}, (Object)entity3, n)) {
                case 0: {
                    AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)entity3;
                    ClientLevel.this.players.add(abstractClientPlayer);
                    break;
                }
                case 1: {
                    EnderDragon enderDragon = (EnderDragon)entity3;
                    ClientLevel.this.dragonParts.addAll(Arrays.asList(enderDragon.getSubEntities()));
                    break;
                }
            }
        }

        @Override
        public void onTrackingEnd(Entity entity) {
            entity.unRide();
            Entity entity2 = entity;
            Objects.requireNonNull(entity2);
            Entity entity3 = entity2;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractClientPlayer.class, EnderDragon.class}, (Object)entity3, n)) {
                case 0: {
                    AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)entity3;
                    ClientLevel.this.players.remove(abstractClientPlayer);
                    break;
                }
                case 1: {
                    EnderDragon enderDragon = (EnderDragon)entity3;
                    ClientLevel.this.dragonParts.removeAll(Arrays.asList(enderDragon.getSubEntities()));
                    break;
                }
            }
        }

        @Override
        public void onSectionChange(Entity entity) {
        }

        @Override
        public /* synthetic */ void onSectionChange(Object object) {
            this.onSectionChange((Entity)object);
        }

        @Override
        public /* synthetic */ void onTrackingEnd(Object object) {
            this.onTrackingEnd((Entity)object);
        }

        @Override
        public /* synthetic */ void onTrackingStart(Object object) {
            this.onTrackingStart((Entity)object);
        }

        @Override
        public /* synthetic */ void onTickingStart(Object object) {
            this.onTickingStart((Entity)object);
        }

        @Override
        public /* synthetic */ void onDestroyed(Object object) {
            this.onDestroyed((Entity)object);
        }

        @Override
        public /* synthetic */ void onCreated(Object object) {
            this.onCreated((Entity)object);
        }
    }

    public static class ClientLevelData
    implements WritableLevelData {
        private final boolean hardcore;
        private final boolean isFlat;
        private BlockPos spawnPos;
        private float spawnAngle;
        private long gameTime;
        private long dayTime;
        private boolean raining;
        private Difficulty difficulty;
        private boolean difficultyLocked;

        public ClientLevelData(Difficulty difficulty, boolean bl, boolean bl2) {
            this.difficulty = difficulty;
            this.hardcore = bl;
            this.isFlat = bl2;
        }

        @Override
        public BlockPos getSpawnPos() {
            return this.spawnPos;
        }

        @Override
        public float getSpawnAngle() {
            return this.spawnAngle;
        }

        @Override
        public long getGameTime() {
            return this.gameTime;
        }

        @Override
        public long getDayTime() {
            return this.dayTime;
        }

        public void setGameTime(long l) {
            this.gameTime = l;
        }

        public void setDayTime(long l) {
            this.dayTime = l;
        }

        @Override
        public void setSpawn(BlockPos blockPos, float f) {
            this.spawnPos = blockPos.immutable();
            this.spawnAngle = f;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return this.raining;
        }

        @Override
        public void setRaining(boolean bl) {
            this.raining = bl;
        }

        @Override
        public boolean isHardcore() {
            return this.hardcore;
        }

        @Override
        public Difficulty getDifficulty() {
            return this.difficulty;
        }

        @Override
        public boolean isDifficultyLocked() {
            return this.difficultyLocked;
        }

        @Override
        public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
            WritableLevelData.super.fillCrashReportCategory(crashReportCategory, levelHeightAccessor);
        }

        public void setDifficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
        }

        public void setDifficultyLocked(boolean bl) {
            this.difficultyLocked = bl;
        }

        public double getHorizonHeight(LevelHeightAccessor levelHeightAccessor) {
            if (this.isFlat) {
                return levelHeightAccessor.getMinY();
            }
            return 63.0;
        }

        public float voidDarknessOnsetRange() {
            if (this.isFlat) {
                return 1.0f;
            }
            return 32.0f;
        }
    }
}

