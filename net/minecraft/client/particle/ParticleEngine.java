/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.EvictingQueue
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.AshParticle;
import net.minecraft.client.particle.AttackSweepParticle;
import net.minecraft.client.particle.BlockMarker;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.BubbleColumnUpParticle;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.BubblePopParticle;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.DustColorTransitionParticle;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.particle.DustPlumeParticle;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.ExplodeParticle;
import net.minecraft.client.particle.FallingDustParticle;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.FlyStraightTowardsParticle;
import net.minecraft.client.particle.FlyTowardsPositionParticle;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.GustParticle;
import net.minecraft.client.particle.GustSeedParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.HugeExplosionSeedParticle;
import net.minecraft.client.particle.LargeSmokeParticle;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.MobAppearanceParticle;
import net.minecraft.client.particle.NoteParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDescription;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.ReversePortalParticle;
import net.minecraft.client.particle.SculkChargeParticle;
import net.minecraft.client.particle.SculkChargePopParticle;
import net.minecraft.client.particle.ShriekParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.particle.SpitParticle;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SquidInkParticle;
import net.minecraft.client.particle.SuspendedParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.particle.TrailParticle;
import net.minecraft.client.particle.TrialSpawnerDetectionParticle;
import net.minecraft.client.particle.VibrationSignalParticle;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.client.particle.WaterCurrentDownParticle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.particle.WhiteAshParticle;
import net.minecraft.client.particle.WhiteSmokeParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class ParticleEngine
implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
    private static final int MAX_PARTICLES_PER_LAYER = 16384;
    private static final List<ParticleRenderType> RENDER_ORDER = List.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
    protected ClientLevel level;
    private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
    private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
    private final RandomSource random = RandomSource.create();
    private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Map<ResourceLocation, MutableSpriteSet> spriteSets = Maps.newHashMap();
    private final TextureAtlas textureAtlas;
    private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap();

    public ParticleEngine(ClientLevel clientLevel, TextureManager textureManager) {
        this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
        textureManager.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = clientLevel;
        this.registerProviders();
    }

    private void registerProviders() {
        this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
        this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
        this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
        this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
        this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
        this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
        this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
        this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
        this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
        this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
        this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
        this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
        this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
        this.register(ParticleTypes.DRIPPING_LAVA, DripParticle::createLavaHangParticle);
        this.register(ParticleTypes.FALLING_LAVA, DripParticle::createLavaFallParticle);
        this.register(ParticleTypes.LANDING_LAVA, DripParticle::createLavaLandParticle);
        this.register(ParticleTypes.DRIPPING_WATER, DripParticle::createWaterHangParticle);
        this.register(ParticleTypes.FALLING_WATER, DripParticle::createWaterFallParticle);
        this.register(ParticleTypes.DUST, DustParticle.Provider::new);
        this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
        this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
        this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
        this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
        this.register(ParticleTypes.ENCHANT, FlyTowardsPositionParticle.EnchantProvider::new);
        this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
        this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobEffectProvider::new);
        this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
        this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
        this.register(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Provider::new);
        this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
        this.register(ParticleTypes.GUST, GustParticle.Provider::new);
        this.register(ParticleTypes.SMALL_GUST, GustParticle.SmallProvider::new);
        this.register(ParticleTypes.GUST_EMITTER_LARGE, new GustSeedParticle.Provider(3.0, 7, 0));
        this.register(ParticleTypes.GUST_EMITTER_SMALL, new GustSeedParticle.Provider(1.0, 3, 2));
        this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
        this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
        this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.INFESTED, SpellParticle.Provider::new);
        this.register(ParticleTypes.SCULK_SOUL, SoulParticle.EmissiveProvider::new);
        this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
        this.register(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Provider::new);
        this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
        this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
        this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
        this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
        this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
        this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
        this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
        this.register(ParticleTypes.ITEM_COBWEB, new BreakingItemParticle.CobwebProvider());
        this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
        this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
        this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
        this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
        this.register(ParticleTypes.NAUTILUS, FlyTowardsPositionParticle.NautilusProvider::new);
        this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
        this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
        this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
        this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
        this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
        this.register(ParticleTypes.WHITE_SMOKE, WhiteSmokeParticle.Provider::new);
        this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
        this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
        this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
        this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
        this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
        this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
        this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
        this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
        this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
        this.register(ParticleTypes.DRIPPING_HONEY, DripParticle::createHoneyHangParticle);
        this.register(ParticleTypes.FALLING_HONEY, DripParticle::createHoneyFallParticle);
        this.register(ParticleTypes.LANDING_HONEY, DripParticle::createHoneyLandParticle);
        this.register(ParticleTypes.FALLING_NECTAR, DripParticle::createNectarFallParticle);
        this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle::createSporeBlossomFallParticle);
        this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
        this.register(ParticleTypes.ASH, AshParticle.Provider::new);
        this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
        this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
        this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle::createObsidianTearHangParticle);
        this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle::createObsidianTearFallParticle);
        this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle::createObsidianTearLandParticle);
        this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
        this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
        this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterHangParticle);
        this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterFallParticle);
        this.register(ParticleTypes.CHERRY_LEAVES, FallingLeavesParticle.CherryProvider::new);
        this.register(ParticleTypes.PALE_OAK_LEAVES, FallingLeavesParticle.PaleOakProvider::new);
        this.register(ParticleTypes.TINTED_LEAVES, FallingLeavesParticle.TintedLeavesProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaHangParticle);
        this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaFallParticle);
        this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
        this.register(ParticleTypes.TRAIL, TrailParticle.Provider::new);
        this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
        this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
        this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
        this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
        this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
        this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
        this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
        this.register(ParticleTypes.EGG_CRACK, SuspendedTownParticle.EggCrackProvider::new);
        this.register(ParticleTypes.DUST_PLUME, DustPlumeParticle.Provider::new);
        this.register(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER, TrialSpawnerDetectionParticle.Provider::new);
        this.register(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, TrialSpawnerDetectionParticle.Provider::new);
        this.register(ParticleTypes.VAULT_CONNECTION, FlyTowardsPositionParticle.VaultConnectionProvider::new);
        this.register(ParticleTypes.DUST_PILLAR, new TerrainParticle.DustPillarProvider());
        this.register(ParticleTypes.RAID_OMEN, SpellParticle.Provider::new);
        this.register(ParticleTypes.TRIAL_OMEN, SpellParticle.Provider::new);
        this.register(ParticleTypes.OMINOUS_SPAWNING, FlyStraightTowardsParticle.OminousSpawnProvider::new);
        this.register(ParticleTypes.BLOCK_CRUMBLE, new TerrainParticle.CrumblingProvider());
        this.register(ParticleTypes.FIREFLY, FireflyParticle.FireflyProvider::new);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider<T> particleProvider) {
        this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(particleType), particleProvider);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider.Sprite<T> sprite) {
        this.register(particleType, (SpriteSet spriteSet) -> (particleOptions, clientLevel, d, d2, d3, d4, d5, d6) -> {
            TextureSheetParticle textureSheetParticle = sprite.createParticle(particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
            if (textureSheetParticle != null) {
                textureSheetParticle.pickSprite(spriteSet);
            }
            return textureSheetParticle;
        });
    }

    private <T extends ParticleOptions> void register(ParticleType<T> particleType, SpriteParticleRegistration<T> spriteParticleRegistration) {
        MutableSpriteSet mutableSpriteSet = new MutableSpriteSet();
        this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType), mutableSpriteSet);
        this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(particleType), spriteParticleRegistration.create(mutableSpriteSet));
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        CompletionStage completionStage = CompletableFuture.supplyAsync(() -> PARTICLE_LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            ArrayList arrayList = new ArrayList(map.size());
            map.forEach((resourceLocation, resource) -> {
                ResourceLocation resourceLocation2 = PARTICLE_LISTER.fileToId((ResourceLocation)resourceLocation);
                arrayList.add(CompletableFuture.supplyAsync(() -> {
                    record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
                    }
                    return new ParticleDefinition(resourceLocation2, this.loadParticleDescription(resourceLocation2, (Resource)resource));
                }, executor));
            });
            return Util.sequence(arrayList);
        });
        CompletionStage completionStage2 = SpriteLoader.create(this.textureAtlas).loadAndStitch(resourceManager, AtlasIds.PARTICLES, 0, executor).thenCompose(SpriteLoader.Preparations::waitForUpload);
        return ((CompletableFuture)CompletableFuture.allOf(new CompletableFuture[]{completionStage2, completionStage}).thenCompose(preparationBarrier::wait)).thenAcceptAsync(arg_0 -> this.lambda$reload$7((CompletableFuture)completionStage2, (CompletableFuture)completionStage, arg_0), executor2);
    }

    public void close() {
        this.textureAtlas.clearTextureData();
    }

    private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation resourceLocation, Resource resource) {
        Optional<List<ResourceLocation>> optional;
        block9: {
            if (!this.spriteSets.containsKey(resourceLocation)) {
                LOGGER.debug("Redundant texture list for particle: {}", (Object)resourceLocation);
                return Optional.empty();
            }
            BufferedReader bufferedReader = resource.openAsReader();
            try {
                ParticleDescription particleDescription = ParticleDescription.fromJson(GsonHelper.parse(bufferedReader));
                optional = Optional.of(particleDescription.getTextures());
                if (bufferedReader == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            ((Reader)bufferedReader).close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    throw new IllegalStateException("Failed to load description for particle " + String.valueOf(resourceLocation), iOException);
                }
            }
            ((Reader)bufferedReader).close();
        }
        return optional;
    }

    public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions) {
        this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleOptions));
    }

    public void createTrackingEmitter(Entity entity, ParticleOptions particleOptions, int n) {
        this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleOptions, n));
    }

    @Nullable
    public Particle createParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6) {
        Particle particle = this.makeParticle(particleOptions, d, d2, d3, d4, d5, d6);
        if (particle != null) {
            this.add(particle);
            return particle;
        }
        return null;
    }

    @Nullable
    private <T extends ParticleOptions> Particle makeParticle(T t, double d, double d2, double d3, double d4, double d5, double d6) {
        ParticleProvider particleProvider = (ParticleProvider)this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getId(t.getType()));
        if (particleProvider == null) {
            return null;
        }
        return particleProvider.createParticle(t, this.level, d, d2, d3, d4, d5, d6);
    }

    public void add(Particle particle) {
        Optional<ParticleGroup> optional = particle.getParticleGroup();
        if (optional.isPresent()) {
            if (this.hasSpaceInParticleLimit(optional.get())) {
                this.particlesToAdd.add(particle);
                this.updateCount(optional.get(), 1);
            }
        } else {
            this.particlesToAdd.add(particle);
        }
    }

    public void tick() {
        Object object;
        this.particles.forEach((particleRenderType, queue) -> {
            Profiler.get().push(particleRenderType.toString());
            this.tickParticleList((Collection<Particle>)queue);
            Profiler.get().pop();
        });
        if (!this.trackingEmitters.isEmpty()) {
            object = Lists.newArrayList();
            for (TrackingEmitter trackingEmitter : this.trackingEmitters) {
                trackingEmitter.tick();
                if (trackingEmitter.isAlive()) continue;
                object.add(trackingEmitter);
            }
            this.trackingEmitters.removeAll((Collection<?>)object);
        }
        if (!this.particlesToAdd.isEmpty()) {
            while ((object = this.particlesToAdd.poll()) != null) {
                this.particles.computeIfAbsent(((Particle)object).getRenderType(), particleRenderType -> EvictingQueue.create((int)16384)).add(object);
            }
        }
    }

    private void tickParticleList(Collection<Particle> collection) {
        if (!collection.isEmpty()) {
            Iterator<Particle> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                this.tickParticle(particle);
                if (particle.isAlive()) continue;
                particle.getParticleGroup().ifPresent(particleGroup -> this.updateCount((ParticleGroup)particleGroup, -1));
                iterator.remove();
            }
        }
    }

    private void updateCount(ParticleGroup particleGroup, int n) {
        this.trackedParticleCounts.addTo((Object)particleGroup, n);
    }

    private void tickParticle(Particle particle) {
        try {
            particle.tick();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking Particle");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being ticked");
            crashReportCategory.setDetail("Particle", particle::toString);
            crashReportCategory.setDetail("Particle Type", particle.getRenderType()::toString);
            throw new ReportedException(crashReport);
        }
    }

    public void render(Camera camera, float f, MultiBufferSource.BufferSource bufferSource) {
        for (ParticleRenderType particleRenderType : RENDER_ORDER) {
            Queue<Particle> queue = this.particles.get(particleRenderType);
            if (queue == null || queue.isEmpty()) continue;
            ParticleEngine.renderParticleType(camera, f, bufferSource, particleRenderType, queue);
        }
        Queue<Particle> queue = this.particles.get(ParticleRenderType.CUSTOM);
        if (queue != null && !queue.isEmpty()) {
            ParticleEngine.renderCustomParticles(camera, f, bufferSource, queue);
        }
        bufferSource.endBatch();
    }

    private static void renderParticleType(Camera camera, float f, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleRenderType, Queue<Particle> queue) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(Objects.requireNonNull(particleRenderType.renderType()));
        for (Particle particle : queue) {
            try {
                particle.render(vertexConsumer, camera, f);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering Particle");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
                crashReportCategory.setDetail("Particle", particle::toString);
                crashReportCategory.setDetail("Particle Type", particleRenderType::toString);
                throw new ReportedException(crashReport);
            }
        }
    }

    private static void renderCustomParticles(Camera camera, float f, MultiBufferSource.BufferSource bufferSource, Queue<Particle> queue) {
        PoseStack poseStack = new PoseStack();
        for (Particle particle : queue) {
            try {
                particle.renderCustom(poseStack, bufferSource, camera, f);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering Particle");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
                crashReportCategory.setDetail("Particle", particle::toString);
                crashReportCategory.setDetail("Particle Type", "Custom");
                throw new ReportedException(crashReport);
            }
        }
    }

    public void setLevel(@Nullable ClientLevel clientLevel) {
        this.level = clientLevel;
        this.clearParticles();
        this.trackingEmitters.clear();
    }

    public void destroy(BlockPos blockPos, BlockState blockState) {
        if (blockState.isAir() || !blockState.shouldSpawnTerrainParticles()) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape(this.level, blockPos);
        double d7 = 0.25;
        voxelShape.forAllBoxes((d, d2, d3, d4, d5, d6) -> {
            double d7 = Math.min(1.0, d4 - d);
            double d8 = Math.min(1.0, d5 - d2);
            double d9 = Math.min(1.0, d6 - d3);
            int n = Math.max(2, Mth.ceil(d7 / 0.25));
            int n2 = Math.max(2, Mth.ceil(d8 / 0.25));
            int n3 = Math.max(2, Mth.ceil(d9 / 0.25));
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n2; ++j) {
                    for (int k = 0; k < n3; ++k) {
                        double d10 = ((double)i + 0.5) / (double)n;
                        double d11 = ((double)j + 0.5) / (double)n2;
                        double d12 = ((double)k + 0.5) / (double)n3;
                        double d13 = d10 * d7 + d;
                        double d14 = d11 * d8 + d2;
                        double d15 = d12 * d9 + d3;
                        this.add(new TerrainParticle(this.level, (double)blockPos.getX() + d13, (double)blockPos.getY() + d14, (double)blockPos.getZ() + d15, d10 - 0.5, d11 - 0.5, d12 - 0.5, blockState, blockPos));
                    }
                }
            }
        });
    }

    public void crack(BlockPos blockPos, Direction direction) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.getRenderShape() == RenderShape.INVISIBLE || !blockState.shouldSpawnTerrainParticles()) {
            return;
        }
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        float f = 0.1f;
        AABB aABB = blockState.getShape(this.level, blockPos).bounds();
        double d = (double)n + this.random.nextDouble() * (aABB.maxX - aABB.minX - (double)0.2f) + (double)0.1f + aABB.minX;
        double d2 = (double)n2 + this.random.nextDouble() * (aABB.maxY - aABB.minY - (double)0.2f) + (double)0.1f + aABB.minY;
        double d3 = (double)n3 + this.random.nextDouble() * (aABB.maxZ - aABB.minZ - (double)0.2f) + (double)0.1f + aABB.minZ;
        if (direction == Direction.DOWN) {
            d2 = (double)n2 + aABB.minY - (double)0.1f;
        }
        if (direction == Direction.UP) {
            d2 = (double)n2 + aABB.maxY + (double)0.1f;
        }
        if (direction == Direction.NORTH) {
            d3 = (double)n3 + aABB.minZ - (double)0.1f;
        }
        if (direction == Direction.SOUTH) {
            d3 = (double)n3 + aABB.maxZ + (double)0.1f;
        }
        if (direction == Direction.WEST) {
            d = (double)n + aABB.minX - (double)0.1f;
        }
        if (direction == Direction.EAST) {
            d = (double)n + aABB.maxX + (double)0.1f;
        }
        this.add(new TerrainParticle(this.level, d, d2, d3, 0.0, 0.0, 0.0, blockState, blockPos).setPower(0.2f).scale(0.6f));
    }

    public String countParticles() {
        return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
    }

    private boolean hasSpaceInParticleLimit(ParticleGroup particleGroup) {
        return this.trackedParticleCounts.getInt((Object)particleGroup) < particleGroup.getLimit();
    }

    private void clearParticles() {
        this.particles.clear();
        this.particlesToAdd.clear();
        this.trackingEmitters.clear();
        this.trackedParticleCounts.clear();
    }

    private /* synthetic */ void lambda$reload$7(CompletableFuture completableFuture, CompletableFuture completableFuture2, Void void_) {
        this.clearParticles();
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("upload");
        SpriteLoader.Preparations preparations = (SpriteLoader.Preparations)completableFuture.join();
        this.textureAtlas.upload(preparations);
        profilerFiller.popPush("bindSpriteSets");
        HashSet hashSet = new HashSet();
        TextureAtlasSprite textureAtlasSprite = preparations.missing();
        ((List)completableFuture2.join()).forEach(particleDefinition -> {
            Optional<List<ResourceLocation>> optional = particleDefinition.sprites();
            if (optional.isEmpty()) {
                return;
            }
            ArrayList<TextureAtlasSprite> arrayList = new ArrayList<TextureAtlasSprite>();
            for (ResourceLocation resourceLocation : optional.get()) {
                TextureAtlasSprite textureAtlasSprite2 = preparations.regions().get(resourceLocation);
                if (textureAtlasSprite2 == null) {
                    hashSet.add(resourceLocation);
                    arrayList.add(textureAtlasSprite);
                    continue;
                }
                arrayList.add(textureAtlasSprite2);
            }
            if (arrayList.isEmpty()) {
                arrayList.add(textureAtlasSprite);
            }
            this.spriteSets.get(particleDefinition.id()).rebind(arrayList);
        });
        if (!hashSet.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", (Object)hashSet.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
        }
        profilerFiller.pop();
    }

    @FunctionalInterface
    static interface SpriteParticleRegistration<T extends ParticleOptions> {
        public ParticleProvider<T> create(SpriteSet var1);
    }

    static class MutableSpriteSet
    implements SpriteSet {
        private List<TextureAtlasSprite> sprites;

        MutableSpriteSet() {
        }

        @Override
        public TextureAtlasSprite get(int n, int n2) {
            return this.sprites.get(n * (this.sprites.size() - 1) / n2);
        }

        @Override
        public TextureAtlasSprite get(RandomSource randomSource) {
            return this.sprites.get(randomSource.nextInt(this.sprites.size()));
        }

        public void rebind(List<TextureAtlasSprite> list) {
            this.sprites = ImmutableList.copyOf(list);
        }
    }
}

