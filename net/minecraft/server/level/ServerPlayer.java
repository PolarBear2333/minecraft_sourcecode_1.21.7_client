/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.google.common.hash.HashCode
 *  com.google.common.net.InetAddresses
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.HashOps;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ServerPlayer
extends Player {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
    private static final int FLY_STAT_RECORDING_SPEED = 25;
    public static final double BLOCK_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 1.0;
    public static final double ENTITY_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 3.0;
    public static final int ENDER_PEARL_TICKET_RADIUS = 2;
    public static final String ENDER_PEARLS_TAG = "ender_pearls";
    public static final String ENDER_PEARL_DIMENSION_TAG = "ender_pearl_dimension";
    public static final String TAG_DIMENSION = "Dimension";
    private static final AttributeModifier CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER = new AttributeModifier(ResourceLocation.withDefaultNamespace("creative_mode_block_range"), 0.5, AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER = new AttributeModifier(ResourceLocation.withDefaultNamespace("creative_mode_entity_range"), 2.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Component SPAWN_SET_MESSAGE = Component.translatable("block.minecraft.set_spawn");
    private static final AttributeModifier WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER = new AttributeModifier(ResourceLocation.withDefaultNamespace("waypoint_transmit_range_crouch"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final boolean DEFAULT_SEEN_CREDITS = false;
    private static final boolean DEFAULT_SPAWN_EXTRA_PARTICLES_ON_FALL = false;
    public ServerGamePacketListenerImpl connection;
    private final MinecraftServer server;
    public final ServerPlayerGameMode gameMode;
    private final PlayerAdvancements advancements;
    private final ServerStatsCounter stats;
    private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
    private int lastRecordedFoodLevel = Integer.MIN_VALUE;
    private int lastRecordedAirLevel = Integer.MIN_VALUE;
    private int lastRecordedArmor = Integer.MIN_VALUE;
    private int lastRecordedLevel = Integer.MIN_VALUE;
    private int lastRecordedExperience = Integer.MIN_VALUE;
    private float lastSentHealth = -1.0E8f;
    private int lastSentFood = -99999999;
    private boolean lastFoodSaturationZero = true;
    private int lastSentExp = -99999999;
    private ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    private ParticleStatus particleStatus = ParticleStatus.ALL;
    private boolean canChatColor = true;
    private long lastActionTime = Util.getMillis();
    @Nullable
    private Entity camera;
    private boolean isChangingDimension;
    public boolean seenCredits = false;
    private final ServerRecipeBook recipeBook;
    @Nullable
    private Vec3 levitationStartPos;
    private int levitationStartTime;
    private boolean disconnected;
    private int requestedViewDistance = 2;
    private String language = "en_us";
    @Nullable
    private Vec3 startingToFallPosition;
    @Nullable
    private Vec3 enteredNetherPosition;
    @Nullable
    private Vec3 enteredLavaOnVehiclePosition;
    private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
    private ChunkTrackingView chunkTrackingView = ChunkTrackingView.EMPTY;
    @Nullable
    private RespawnConfig respawnConfig;
    private final TextFilter textFilter;
    private boolean textFilteringEnabled;
    private boolean allowsListing;
    private boolean spawnExtraParticlesOnFall = false;
    private WardenSpawnTracker wardenSpawnTracker = new WardenSpawnTracker();
    @Nullable
    private BlockPos raidOmenPosition;
    private Vec3 lastKnownClientMovement = Vec3.ZERO;
    private Input lastClientInput = Input.EMPTY;
    private final Set<ThrownEnderpearl> enderPearls = new HashSet<ThrownEnderpearl>();
    private final ContainerSynchronizer containerSynchronizer = new ContainerSynchronizer(){
        private final LoadingCache<TypedDataComponent<?>, Integer> cache = CacheBuilder.newBuilder().maximumSize(256L).build(new CacheLoader<TypedDataComponent<?>, Integer>(){
            private final DynamicOps<HashCode> registryHashOps;
            {
                this.registryHashOps = ServerPlayer.this.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);
            }

            public Integer load(TypedDataComponent<?> typedDataComponent) {
                return ((HashCode)typedDataComponent.encodeValue(this.registryHashOps).getOrThrow(string -> new IllegalArgumentException("Failed to hash " + String.valueOf(typedDataComponent) + ": " + string))).asInt();
            }

            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((TypedDataComponent)object);
            }
        });

        @Override
        public void sendInitialData(AbstractContainerMenu abstractContainerMenu, List<ItemStack> list, ItemStack itemStack, int[] nArray) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetContentPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), list, itemStack));
            for (int i = 0; i < nArray.length; ++i) {
                this.broadcastDataValue(abstractContainerMenu, i, nArray[i]);
            }
        }

        @Override
        public void sendSlotChange(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), n, itemStack));
        }

        @Override
        public void sendCarriedChange(AbstractContainerMenu abstractContainerMenu, ItemStack itemStack) {
            ServerPlayer.this.connection.send(new ClientboundSetCursorItemPacket(itemStack));
        }

        @Override
        public void sendDataChange(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
            this.broadcastDataValue(abstractContainerMenu, n, n2);
        }

        private void broadcastDataValue(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket(abstractContainerMenu.containerId, n, n2));
        }

        @Override
        public RemoteSlot createSlot() {
            return new RemoteSlot.Synchronized(arg_0 -> this.cache.getUnchecked(arg_0));
        }
    };
    private final ContainerListener containerListener = new ContainerListener(){

        @Override
        public void slotChanged(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
            Slot slot = abstractContainerMenu.getSlot(n);
            if (slot instanceof ResultSlot) {
                return;
            }
            if (slot.container == ServerPlayer.this.getInventory()) {
                CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), itemStack);
            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
        }
    };
    @Nullable
    private RemoteChatSession chatSession;
    @Nullable
    public final Object object;
    private final CommandSource commandSource = new CommandSource(){

        @Override
        public boolean acceptsSuccess() {
            return ServerPlayer.this.level().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return true;
        }

        @Override
        public void sendSystemMessage(Component component) {
            ServerPlayer.this.sendSystemMessage(component);
        }
    };
    private int containerCounter;
    public boolean wonGame;

    public ServerPlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation) {
        super(serverLevel, gameProfile);
        this.textFilter = minecraftServer.createTextFilterForPlayer(this);
        this.gameMode = minecraftServer.createGameModeForPlayer(this);
        this.recipeBook = new ServerRecipeBook((resourceKey, consumer) -> minecraftServer.getRecipeManager().listDisplaysForRecipe(resourceKey, consumer));
        this.server = minecraftServer;
        this.stats = minecraftServer.getPlayerList().getPlayerStats(this);
        this.advancements = minecraftServer.getPlayerList().getPlayerAdvancements(this);
        this.updateOptions(clientInformation);
        this.object = null;
    }

    @Override
    public BlockPos adjustSpawnLocation(ServerLevel serverLevel, BlockPos blockPos) {
        AABB aABB = this.getDimensions(Pose.STANDING).makeBoundingBox(Vec3.ZERO);
        BlockPos blockPos2 = blockPos;
        if (serverLevel.dimensionType().hasSkyLight() && serverLevel.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
            long l;
            long l2;
            int n = Math.max(0, this.server.getSpawnRadius(serverLevel));
            int n2 = Mth.floor(serverLevel.getWorldBorder().getDistanceToBorder(blockPos.getX(), blockPos.getZ()));
            if (n2 < n) {
                n = n2;
            }
            if (n2 <= 1) {
                n = 1;
            }
            int n3 = (l2 = (l = (long)(n * 2 + 1)) * l) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)l2;
            int n4 = this.getCoprime(n3);
            int n5 = RandomSource.create().nextInt(n3);
            for (int i = 0; i < n3; ++i) {
                int n6 = (n5 + n4 * i) % n3;
                int n7 = n6 % (n * 2 + 1);
                int n8 = n6 / (n * 2 + 1);
                int n9 = blockPos.getX() + n7 - n;
                int n10 = blockPos.getZ() + n8 - n;
                try {
                    blockPos2 = PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, n9, n10);
                    if (blockPos2 == null || !this.noCollisionNoLiquid(serverLevel, aABB.move(blockPos2.getBottomCenter()))) continue;
                    return blockPos2;
                }
                catch (Exception exception) {
                    int n11 = i;
                    int n12 = n;
                    CrashReport crashReport = CrashReport.forThrowable(exception, "Searching for spawn");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Spawn Lookup");
                    crashReportCategory.setDetail("Origin", blockPos::toString);
                    crashReportCategory.setDetail("Radius", () -> Integer.toString(n12));
                    crashReportCategory.setDetail("Candidate", () -> "[" + n9 + "," + n10 + "]");
                    crashReportCategory.setDetail("Progress", () -> n11 + " out of " + n3);
                    throw new ReportedException(crashReport);
                }
            }
            blockPos2 = blockPos;
        }
        while (!this.noCollisionNoLiquid(serverLevel, aABB.move(blockPos2.getBottomCenter())) && blockPos2.getY() < serverLevel.getMaxY()) {
            blockPos2 = blockPos2.above();
        }
        while (this.noCollisionNoLiquid(serverLevel, aABB.move(blockPos2.below().getBottomCenter())) && blockPos2.getY() > serverLevel.getMinY() + 1) {
            blockPos2 = blockPos2.below();
        }
        return blockPos2;
    }

    private boolean noCollisionNoLiquid(ServerLevel serverLevel, AABB aABB) {
        return serverLevel.noCollision(this, aABB, true);
    }

    private int getCoprime(int n) {
        return n <= 16 ? n - 1 : 17;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.wardenSpawnTracker = valueInput.read("warden_spawn_tracker", WardenSpawnTracker.CODEC).orElseGet(WardenSpawnTracker::new);
        this.enteredNetherPosition = valueInput.read("entered_nether_pos", Vec3.CODEC).orElse(null);
        this.seenCredits = valueInput.getBooleanOr("seenCredits", false);
        valueInput.read("recipeBook", ServerRecipeBook.Packed.CODEC).ifPresent(packed -> this.recipeBook.loadUntrusted((ServerRecipeBook.Packed)packed, resourceKey -> this.server.getRecipeManager().byKey((ResourceKey<Recipe<?>>)resourceKey).isPresent()));
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        this.respawnConfig = valueInput.read("respawn", RespawnConfig.CODEC).orElse(null);
        this.spawnExtraParticlesOnFall = valueInput.getBooleanOr("spawn_extra_particles_on_fall", false);
        this.raidOmenPosition = valueInput.read("raid_omen_position", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("warden_spawn_tracker", WardenSpawnTracker.CODEC, this.wardenSpawnTracker);
        this.storeGameTypes(valueOutput);
        valueOutput.putBoolean("seenCredits", this.seenCredits);
        valueOutput.storeNullable("entered_nether_pos", Vec3.CODEC, this.enteredNetherPosition);
        this.saveParentVehicle(valueOutput);
        valueOutput.store("recipeBook", ServerRecipeBook.Packed.CODEC, this.recipeBook.pack());
        valueOutput.putString(TAG_DIMENSION, this.level().dimension().location().toString());
        valueOutput.storeNullable("respawn", RespawnConfig.CODEC, this.respawnConfig);
        valueOutput.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
        valueOutput.storeNullable("raid_omen_position", BlockPos.CODEC, this.raidOmenPosition);
        this.saveEnderPearls(valueOutput);
    }

    private void saveParentVehicle(ValueOutput valueOutput) {
        Entity entity = this.getRootVehicle();
        Entity entity2 = this.getVehicle();
        if (entity2 != null && entity != this && entity.hasExactlyOnePlayerPassenger()) {
            ValueOutput valueOutput2 = valueOutput.child("RootVehicle");
            valueOutput2.store("Attach", UUIDUtil.CODEC, entity2.getUUID());
            entity.save(valueOutput2.child("Entity"));
        }
    }

    public void loadAndSpawnParentVehicle(ValueInput valueInput) {
        Optional<ValueInput> optional = valueInput.child("RootVehicle");
        if (optional.isEmpty()) {
            return;
        }
        ServerLevel serverLevel = this.level();
        Entity entity2 = EntityType.loadEntityRecursive(optional.get().childOrEmpty("Entity"), (Level)serverLevel, EntitySpawnReason.LOAD, entity -> {
            if (!serverLevel.addWithUUID((Entity)entity)) {
                return null;
            }
            return entity;
        });
        if (entity2 == null) {
            return;
        }
        UUID uUID = optional.get().read("Attach", UUIDUtil.CODEC).orElse(null);
        if (entity2.getUUID().equals(uUID)) {
            this.startRiding(entity2, true);
        } else {
            for (Entity entity3 : entity2.getIndirectPassengers()) {
                if (!entity3.getUUID().equals(uUID)) continue;
                this.startRiding(entity3, true);
                break;
            }
        }
        if (!this.isPassenger()) {
            LOGGER.warn("Couldn't reattach entity to player");
            entity2.discard();
            for (Entity entity3 : entity2.getIndirectPassengers()) {
                entity3.discard();
            }
        }
    }

    private void saveEnderPearls(ValueOutput valueOutput) {
        if (!this.enderPearls.isEmpty()) {
            ValueOutput.ValueOutputList valueOutputList = valueOutput.childrenList(ENDER_PEARLS_TAG);
            for (ThrownEnderpearl thrownEnderpearl : this.enderPearls) {
                if (thrownEnderpearl.isRemoved()) {
                    LOGGER.warn("Trying to save removed ender pearl, skipping");
                    continue;
                }
                ValueOutput valueOutput2 = valueOutputList.addChild();
                thrownEnderpearl.save(valueOutput2);
                valueOutput2.store(ENDER_PEARL_DIMENSION_TAG, Level.RESOURCE_KEY_CODEC, thrownEnderpearl.level().dimension());
            }
        }
    }

    public void loadAndSpawnEnderPearls(ValueInput valueInput) {
        valueInput.childrenListOrEmpty(ENDER_PEARLS_TAG).forEach(this::loadAndSpawnEnderPearl);
    }

    private void loadAndSpawnEnderPearl(ValueInput valueInput) {
        Optional<ResourceKey<Level>> optional = valueInput.read(ENDER_PEARL_DIMENSION_TAG, Level.RESOURCE_KEY_CODEC);
        if (optional.isEmpty()) {
            return;
        }
        ServerLevel serverLevel = this.level().getServer().getLevel(optional.get());
        if (serverLevel != null) {
            Entity entity2 = EntityType.loadEntityRecursive(valueInput, (Level)serverLevel, EntitySpawnReason.LOAD, entity -> {
                if (!serverLevel.addWithUUID((Entity)entity)) {
                    return null;
                }
                return entity;
            });
            if (entity2 != null) {
                ServerPlayer.placeEnderPearlTicket(serverLevel, entity2.chunkPosition());
            } else {
                LOGGER.warn("Failed to spawn player ender pearl in level ({}), skipping", optional.get());
            }
        } else {
            LOGGER.warn("Trying to load ender pearl without level ({}) being loaded, skipping", optional.get());
        }
    }

    public void setExperiencePoints(int n) {
        float f = this.getXpNeededForNextLevel();
        float f2 = (f - 1.0f) / f;
        this.experienceProgress = Mth.clamp((float)n / f, 0.0f, f2);
        this.lastSentExp = -1;
    }

    public void setExperienceLevels(int n) {
        this.experienceLevel = n;
        this.lastSentExp = -1;
    }

    @Override
    public void giveExperienceLevels(int n) {
        super.giveExperienceLevels(n);
        this.lastSentExp = -1;
    }

    @Override
    public void onEnchantmentPerformed(ItemStack itemStack, int n) {
        super.onEnchantmentPerformed(itemStack, n);
        this.lastSentExp = -1;
    }

    private void initMenu(AbstractContainerMenu abstractContainerMenu) {
        abstractContainerMenu.addSlotListener(this.containerListener);
        abstractContainerMenu.setSynchronizer(this.containerSynchronizer);
    }

    public void initInventoryMenu() {
        this.initMenu(this.inventoryMenu);
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        this.connection.send(ClientboundPlayerCombatEnterPacket.INSTANCE);
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
    }

    @Override
    public void onInsideBlock(BlockState blockState) {
        CriteriaTriggers.ENTER_BLOCK.trigger(this, blockState);
    }

    @Override
    protected ItemCooldowns createItemCooldowns() {
        return new ServerItemCooldowns(this);
    }

    @Override
    public void tick() {
        Entity entity;
        this.tickClientLoadTimeout();
        this.gameMode.tick();
        this.wardenSpawnTracker.tick();
        if (this.invulnerableTime > 0) {
            --this.invulnerableTime;
        }
        this.containerMenu.broadcastChanges();
        if (!this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }
        if ((entity = this.getCamera()) != this) {
            if (entity.isAlive()) {
                this.absSnapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                this.level().getChunkSource().move(this);
                if (this.wantsToStopRiding()) {
                    this.setCamera(this);
                }
            } else {
                this.setCamera(this);
            }
        }
        CriteriaTriggers.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
        }
        this.trackStartFallingPosition();
        this.trackEnteredOrExitedLavaOnVehicle();
        this.updatePlayerAttributes();
        this.advancements.flushDirty(this, true);
    }

    private void updatePlayerAttributes() {
        AttributeInstance attributeInstance;
        AttributeInstance attributeInstance2;
        AttributeInstance attributeInstance3 = this.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (attributeInstance3 != null) {
            if (this.isCreative()) {
                attributeInstance3.addOrUpdateTransientModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            } else {
                attributeInstance3.removeModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            }
        }
        if ((attributeInstance2 = this.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)) != null) {
            if (this.isCreative()) {
                attributeInstance2.addOrUpdateTransientModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            } else {
                attributeInstance2.removeModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            }
        }
        if ((attributeInstance = this.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE)) != null) {
            if (this.isCrouching()) {
                attributeInstance.addOrUpdateTransientModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
            } else {
                attributeInstance.removeModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
            }
        }
    }

    public void doTick() {
        try {
            if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
                super.tick();
            }
            for (int i = 0; i < this.getInventory().getContainerSize(); ++i) {
                ItemStack itemStack = this.getInventory().getItem(i);
                if (itemStack.isEmpty()) continue;
                this.synchronizeSpecialItemUpdates(itemStack);
            }
            if (this.getHealth() != this.lastSentHealth || this.lastSentFood != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0f != this.lastFoodSaturationZero) {
                this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.lastSentHealth = this.getHealth();
                this.lastSentFood = this.foodData.getFoodLevel();
                boolean bl = this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0f;
            }
            if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
                this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
                this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
            }
            if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
                this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
                this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil(this.lastRecordedFoodLevel));
            }
            if (this.getAirSupply() != this.lastRecordedAirLevel) {
                this.lastRecordedAirLevel = this.getAirSupply();
                this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil(this.lastRecordedAirLevel));
            }
            if (this.getArmorValue() != this.lastRecordedArmor) {
                this.lastRecordedArmor = this.getArmorValue();
                this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil(this.lastRecordedArmor));
            }
            if (this.totalExperience != this.lastRecordedExperience) {
                this.lastRecordedExperience = this.totalExperience;
                this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil(this.lastRecordedExperience));
            }
            if (this.experienceLevel != this.lastRecordedLevel) {
                this.lastRecordedLevel = this.experienceLevel;
                this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil(this.lastRecordedLevel));
            }
            if (this.totalExperience != this.lastSentExp) {
                this.lastSentExp = this.totalExperience;
                this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }
            if (this.tickCount % 20 == 0) {
                CriteriaTriggers.LOCATION.trigger(this);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking player");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Player being ticked");
            this.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    private void synchronizeSpecialItemUpdates(ItemStack itemStack) {
        Packet<?> packet;
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, (Level)this.level());
        if (mapItemSavedData != null && (packet = mapItemSavedData.getUpdatePacket(mapId, this)) != null) {
            this.connection.send(packet);
        }
    }

    @Override
    protected void tickRegeneration() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            if (this.tickCount % 20 == 0) {
                float f;
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.0f);
                }
                if ((f = this.foodData.getSaturationLevel()) < 20.0f) {
                    this.foodData.setSaturation(f + 1.0f);
                }
            }
            if (this.tickCount % 10 == 0 && this.foodData.needsFood()) {
                this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
            }
        }
    }

    @Override
    public void resetFallDistance() {
        if (this.getHealth() > 0.0f && this.startingToFallPosition != null) {
            CriteriaTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
        }
        this.startingToFallPosition = null;
        super.resetFallDistance();
    }

    public void trackStartFallingPosition() {
        if (this.fallDistance > 0.0 && this.startingToFallPosition == null) {
            this.startingToFallPosition = this.position();
            if (this.currentImpulseImpactPos != null && this.currentImpulseImpactPos.y <= this.startingToFallPosition.y) {
                CriteriaTriggers.FALL_AFTER_EXPLOSION.trigger(this, this.currentImpulseImpactPos, this.currentExplosionCause);
            }
        }
    }

    public void trackEnteredOrExitedLavaOnVehicle() {
        if (this.getVehicle() != null && this.getVehicle().isInLava()) {
            if (this.enteredLavaOnVehiclePosition == null) {
                this.enteredLavaOnVehiclePosition = this.position();
            } else {
                CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
            }
        }
        if (!(this.enteredLavaOnVehiclePosition == null || this.getVehicle() != null && this.getVehicle().isInLava())) {
            this.enteredLavaOnVehiclePosition = null;
        }
    }

    private void updateScoreForCriteria(ObjectiveCriteria objectiveCriteria, int n) {
        this.getScoreboard().forAllObjectives(objectiveCriteria, this, scoreAccess -> scoreAccess.set(n));
    }

    @Override
    public void die(DamageSource damageSource) {
        Object object;
        this.gameEvent(GameEvent.ENTITY_DIE);
        boolean bl = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
        if (bl) {
            object = this.getCombatTracker().getDeathMessage();
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), (Component)object), PacketSendListener.exceptionallySend(() -> this.lambda$die$10((Component)object)));
            PlayerTeam playerTeam = this.getTeam();
            if (playerTeam == null || ((Team)playerTeam).getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
                this.server.getPlayerList().broadcastSystemMessage((Component)object, false);
            } else if (((Team)playerTeam).getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerList().broadcastSystemToTeam(this, (Component)object);
            } else if (((Team)playerTeam).getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, (Component)object);
            }
        } else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
        }
        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }
        if (!this.isSpectator()) {
            this.dropAllDeathLoot(this.level(), damageSource);
        }
        this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this, ScoreAccess::increment);
        object = this.getKillCredit();
        if (object != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(((Entity)object).getType()));
            ((Entity)object).awardKillScore(this, damageSource);
            this.createWitherRose((LivingEntity)object);
        }
        this.level().broadcastEntityEvent(this, (byte)3);
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
        this.setClientLoaded(false);
    }

    private void tellNeutralMobsThatIDied() {
        AABB aABB = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
        this.level().getEntitiesOfClass(Mob.class, aABB, EntitySelector.NO_SPECTATORS).stream().filter(mob -> mob instanceof NeutralMob).forEach(mob -> ((NeutralMob)((Object)mob)).playerDied(this.level(), this));
    }

    @Override
    public void awardKillScore(Entity entity, DamageSource damageSource) {
        if (entity == this) {
            return;
        }
        super.awardKillScore(entity, damageSource);
        this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, this, ScoreAccess::increment);
        if (entity instanceof Player) {
            this.awardStat(Stats.PLAYER_KILLS);
            this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, this, ScoreAccess::increment);
        } else {
            this.awardStat(Stats.MOB_KILLS);
        }
        this.handleTeamKill(this, entity, ObjectiveCriteria.TEAM_KILL);
        this.handleTeamKill(entity, this, ObjectiveCriteria.KILLED_BY_TEAM);
        CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, damageSource);
    }

    private void handleTeamKill(ScoreHolder scoreHolder, ScoreHolder scoreHolder2, ObjectiveCriteria[] objectiveCriteriaArray) {
        int n;
        PlayerTeam playerTeam = this.getScoreboard().getPlayersTeam(scoreHolder2.getScoreboardName());
        if (playerTeam != null && (n = playerTeam.getColor().getId()) >= 0 && n < objectiveCriteriaArray.length) {
            this.getScoreboard().forAllObjectives(objectiveCriteriaArray[n], scoreHolder, ScoreAccess::increment);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        Player player;
        Entity entity;
        Entity entity2;
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        Entity entity3 = damageSource.getEntity();
        if (entity3 instanceof Player && !this.canHarmPlayer((Player)(entity2 = (Player)entity3))) {
            return false;
        }
        if (entity3 instanceof AbstractArrow && (entity = ((Projectile)(entity2 = (AbstractArrow)entity3)).getOwner()) instanceof Player && !this.canHarmPlayer(player = (Player)entity)) {
            return false;
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    public boolean canHarmPlayer(Player player) {
        if (!this.isPvpAllowed()) {
            return false;
        }
        return super.canHarmPlayer(player);
    }

    private boolean isPvpAllowed() {
        return this.server.isPvpAllowed();
    }

    public TeleportTransition findRespawnPositionAndUseSpawnBlock(boolean bl, TeleportTransition.PostTeleportTransition postTeleportTransition) {
        RespawnConfig respawnConfig = this.getRespawnConfig();
        ServerLevel serverLevel = this.server.getLevel(RespawnConfig.getDimensionOrDefault(respawnConfig));
        if (serverLevel != null && respawnConfig != null) {
            Optional<RespawnPosAngle> optional = ServerPlayer.findRespawnAndUseSpawnBlock(serverLevel, respawnConfig, bl);
            if (optional.isPresent()) {
                RespawnPosAngle respawnPosAngle = optional.get();
                return new TeleportTransition(serverLevel, respawnPosAngle.position(), Vec3.ZERO, respawnPosAngle.yaw(), 0.0f, postTeleportTransition);
            }
            return TeleportTransition.missingRespawnBlock(this.server.overworld(), this, postTeleportTransition);
        }
        return new TeleportTransition(this.server.overworld(), this, postTeleportTransition);
    }

    public boolean isReceivingWaypoints() {
        return this.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE) > 0.0;
    }

    @Override
    protected void onAttributeUpdated(Holder<Attribute> holder) {
        if (holder.is(Attributes.WAYPOINT_RECEIVE_RANGE)) {
            ServerWaypointManager serverWaypointManager = this.level().getWaypointManager();
            if (this.getAttributes().getValue(holder) > 0.0) {
                serverWaypointManager.addPlayer(this);
            } else {
                serverWaypointManager.removePlayer(this);
            }
        }
        super.onAttributeUpdated(holder);
    }

    private static Optional<RespawnPosAngle> findRespawnAndUseSpawnBlock(ServerLevel serverLevel, RespawnConfig respawnConfig, boolean bl) {
        BlockPos blockPos = respawnConfig.pos;
        float f = respawnConfig.angle;
        boolean bl2 = respawnConfig.forced;
        BlockState blockState = serverLevel.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock && (bl2 || blockState.getValue(RespawnAnchorBlock.CHARGE) > 0) && RespawnAnchorBlock.canSetSpawn(serverLevel)) {
            Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos);
            if (!bl2 && bl && optional.isPresent()) {
                serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(RespawnAnchorBlock.CHARGE, blockState.getValue(RespawnAnchorBlock.CHARGE) - 1), 3);
            }
            return optional.map(vec3 -> RespawnPosAngle.of(vec3, blockPos));
        }
        if (block instanceof BedBlock && BedBlock.canSetSpawn(serverLevel)) {
            return BedBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos, (Direction)blockState.getValue(BedBlock.FACING), f).map(vec3 -> RespawnPosAngle.of(vec3, blockPos));
        }
        if (!bl2) {
            return Optional.empty();
        }
        boolean bl3 = block.isPossibleToRespawnInThis(blockState);
        BlockState blockState2 = serverLevel.getBlockState(blockPos.above());
        boolean bl4 = blockState2.getBlock().isPossibleToRespawnInThis(blockState2);
        if (bl3 && bl4) {
            return Optional.of(new RespawnPosAngle(new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.1, (double)blockPos.getZ() + 0.5), f));
        }
        return Optional.empty();
    }

    public void showEndCredits() {
        this.unRide();
        this.level().removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
        if (!this.wonGame) {
            this.wonGame = true;
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0f));
            this.seenCredits = true;
        }
    }

    @Override
    @Nullable
    public ServerPlayer teleport(TeleportTransition teleportTransition) {
        if (this.isRemoved()) {
            return null;
        }
        if (teleportTransition.missingRespawnBlock()) {
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0f));
        }
        ServerLevel serverLevel = teleportTransition.newLevel();
        ServerLevel serverLevel2 = this.level();
        ResourceKey<Level> resourceKey = serverLevel2.dimension();
        if (!teleportTransition.asPassenger()) {
            this.removeVehicle();
        }
        if (serverLevel.dimension() == resourceKey) {
            this.connection.teleport(PositionMoveRotation.of(teleportTransition), teleportTransition.relatives());
            this.connection.resetPosition();
            teleportTransition.postTeleportTransition().onTransition(this);
            return this;
        }
        this.isChangingDimension = true;
        LevelData levelData = serverLevel.getLevelData();
        this.connection.send(new ClientboundRespawnPacket(this.createCommonSpawnInfo(serverLevel), 3));
        this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        PlayerList playerList = this.server.getPlayerList();
        playerList.sendPlayerPermissionLevel(this);
        serverLevel2.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
        this.unsetRemoved();
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("moving");
        if (resourceKey == Level.OVERWORLD && serverLevel.dimension() == Level.NETHER) {
            this.enteredNetherPosition = this.position();
        }
        profilerFiller.pop();
        profilerFiller.push("placing");
        this.setServerLevel(serverLevel);
        this.connection.teleport(PositionMoveRotation.of(teleportTransition), teleportTransition.relatives());
        this.connection.resetPosition();
        serverLevel.addDuringTeleport(this);
        profilerFiller.pop();
        this.triggerDimensionChangeTriggers(serverLevel2);
        this.stopUsingItem();
        this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
        playerList.sendLevelInfo(this, serverLevel);
        playerList.sendAllPlayerInfo(this);
        playerList.sendActivePlayerEffects(this);
        teleportTransition.postTeleportTransition().onTransition(this);
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0f;
        this.lastSentFood = -1;
        this.teleportSpectators(teleportTransition, serverLevel2);
        return this;
    }

    @Override
    public void forceSetRotation(float f, float f2) {
        this.connection.send(new ClientboundPlayerRotationPacket(f, f2));
    }

    private void triggerDimensionChangeTriggers(ServerLevel serverLevel) {
        ResourceKey<Level> resourceKey = serverLevel.dimension();
        ResourceKey<Level> resourceKey2 = this.level().dimension();
        CriteriaTriggers.CHANGED_DIMENSION.trigger(this, resourceKey, resourceKey2);
        if (resourceKey == Level.NETHER && resourceKey2 == Level.OVERWORLD && this.enteredNetherPosition != null) {
            CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
        }
        if (resourceKey2 != Level.NETHER) {
            this.enteredNetherPosition = null;
        }
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
        if (serverPlayer.isSpectator()) {
            return this.getCamera() == this;
        }
        if (this.isSpectator()) {
            return false;
        }
        return super.broadcastToPlayer(serverPlayer);
    }

    @Override
    public void take(Entity entity, int n) {
        super.take(entity, n);
        this.containerMenu.broadcastChanges();
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
        Direction direction = this.level().getBlockState(blockPos).getValue(HorizontalDirectionalBlock.FACING);
        if (this.isSleeping() || !this.isAlive()) {
            return Either.left((Object)((Object)Player.BedSleepingProblem.OTHER_PROBLEM));
        }
        if (!this.level().dimensionType().natural()) {
            return Either.left((Object)((Object)Player.BedSleepingProblem.NOT_POSSIBLE_HERE));
        }
        if (!this.bedInRange(blockPos, direction)) {
            return Either.left((Object)((Object)Player.BedSleepingProblem.TOO_FAR_AWAY));
        }
        if (this.bedBlocked(blockPos, direction)) {
            return Either.left((Object)((Object)Player.BedSleepingProblem.OBSTRUCTED));
        }
        this.setRespawnPosition(new RespawnConfig(this.level().dimension(), blockPos, this.getYRot(), false), true);
        if (this.level().isBrightOutside()) {
            return Either.left((Object)((Object)Player.BedSleepingProblem.NOT_POSSIBLE_NOW));
        }
        if (!this.isCreative()) {
            double d = 8.0;
            double d2 = 5.0;
            Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
            List<Monster> list = this.level().getEntitiesOfClass(Monster.class, new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0), monster -> monster.isPreventingPlayerRest(this.level(), this));
            if (!list.isEmpty()) {
                return Either.left((Object)((Object)Player.BedSleepingProblem.NOT_SAFE));
            }
        }
        Either either = super.startSleepInBed(blockPos).ifRight(unit -> {
            this.awardStat(Stats.SLEEP_IN_BED);
            CriteriaTriggers.SLEPT_IN_BED.trigger(this);
        });
        if (!this.level().canSleepThroughNights()) {
            this.displayClientMessage(Component.translatable("sleep.not_possible"), true);
        }
        this.level().updateSleepingPlayerList();
        return either;
    }

    @Override
    public void startSleeping(BlockPos blockPos) {
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        super.startSleeping(blockPos);
    }

    private boolean bedInRange(BlockPos blockPos, Direction direction) {
        return this.isReachableBedBlock(blockPos) || this.isReachableBedBlock(blockPos.relative(direction.getOpposite()));
    }

    private boolean isReachableBedBlock(BlockPos blockPos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
        return Math.abs(this.getX() - vec3.x()) <= 3.0 && Math.abs(this.getY() - vec3.y()) <= 2.0 && Math.abs(this.getZ() - vec3.z()) <= 3.0;
    }

    private boolean bedBlocked(BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.above();
        return !this.freeAt(blockPos2) || !this.freeAt(blockPos2.relative(direction.getOpposite()));
    }

    @Override
    public void stopSleepInBed(boolean bl, boolean bl2) {
        if (this.isSleeping()) {
            this.level().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
        }
        super.stopSleepInBed(bl, bl2);
        if (this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
        return super.isInvulnerableTo(serverLevel, damageSource) || this.isChangingDimension() && !damageSource.is(DamageTypes.ENDER_PEARL) || !this.hasClientLoaded();
    }

    @Override
    protected void onChangedBlock(ServerLevel serverLevel, BlockPos blockPos) {
        if (!this.isSpectator()) {
            super.onChangedBlock(serverLevel, blockPos);
        }
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        if (this.spawnExtraParticlesOnFall && bl && this.fallDistance > 0.0) {
            Vec3 vec3 = blockPos.getCenter().add(0.0, 0.5, 0.0);
            int n = (int)Mth.clamp(50.0 * this.fallDistance, 0.0, 200.0);
            this.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec3.x, vec3.y, vec3.z, n, 0.3f, 0.3f, 0.3f, 0.15f);
            this.spawnExtraParticlesOnFall = false;
        }
        super.checkFallDamage(d, bl, blockState, blockPos);
    }

    @Override
    public void onExplosionHit(@Nullable Entity entity) {
        super.onExplosionHit(entity);
        this.currentImpulseImpactPos = this.position();
        this.currentExplosionCause = entity;
        this.setIgnoreFallDamageFromCurrentImpulse(entity != null && entity.getType() == EntityType.WIND_CHARGE);
    }

    @Override
    protected void pushEntities() {
        if (this.level().tickRateManager().runsNormally()) {
            super.pushEntities();
        }
    }

    @Override
    public void openTextEdit(SignBlockEntity signBlockEntity, boolean bl) {
        this.connection.send(new ClientboundBlockUpdatePacket(this.level(), signBlockEntity.getBlockPos()));
        this.connection.send(new ClientboundOpenSignEditorPacket(signBlockEntity.getBlockPos(), bl));
    }

    @Override
    public void openDialog(Holder<Dialog> holder) {
        this.connection.send(new ClientboundShowDialogPacket(holder));
    }

    private void nextContainerCounter() {
        this.containerCounter = this.containerCounter % 100 + 1;
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
        if (menuProvider == null) {
            return OptionalInt.empty();
        }
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }
        this.nextContainerCounter();
        AbstractContainerMenu abstractContainerMenu = menuProvider.createMenu(this.containerCounter, this.getInventory(), this);
        if (abstractContainerMenu == null) {
            if (this.isSpectator()) {
                this.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
            }
            return OptionalInt.empty();
        }
        this.connection.send(new ClientboundOpenScreenPacket(abstractContainerMenu.containerId, abstractContainerMenu.getType(), menuProvider.getDisplayName()));
        this.initMenu(abstractContainerMenu);
        this.containerMenu = abstractContainerMenu;
        return OptionalInt.of(this.containerCounter);
    }

    @Override
    public void sendMerchantOffers(int n, MerchantOffers merchantOffers, int n2, int n3, boolean bl, boolean bl2) {
        this.connection.send(new ClientboundMerchantOffersPacket(n, merchantOffers, n2, n3, bl, bl2));
    }

    @Override
    public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }
        this.nextContainerCounter();
        int n = abstractHorse.getInventoryColumns();
        this.connection.send(new ClientboundHorseScreenOpenPacket(this.containerCounter, n, abstractHorse.getId()));
        this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), container, abstractHorse, n);
        this.initMenu(this.containerMenu);
    }

    @Override
    public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
        if (itemStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            if (WrittenBookContent.resolveForItem(itemStack, this.createCommandSourceStack(), this)) {
                this.containerMenu.broadcastChanges();
            }
            this.connection.send(new ClientboundOpenBookPacket(interactionHand));
        }
    }

    @Override
    public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
        this.connection.send(ClientboundBlockEntityDataPacket.create(commandBlockEntity, BlockEntity::saveCustomOnly));
    }

    @Override
    public void closeContainer() {
        this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
        this.doCloseContainer();
    }

    @Override
    public void doCloseContainer() {
        this.containerMenu.removed(this);
        this.inventoryMenu.transferState(this.containerMenu);
        this.containerMenu = this.inventoryMenu;
    }

    @Override
    public void rideTick() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        super.rideTick();
        this.checkRidingStatistics(this.getX() - d, this.getY() - d2, this.getZ() - d3);
    }

    public void checkMovementStatistics(double d, double d2, double d3) {
        if (this.isPassenger() || ServerPlayer.didNotMove(d, d2, d3)) {
            return;
        }
        if (this.isSwimming()) {
            int n = Math.round((float)Math.sqrt(d * d + d2 * d2 + d3 * d3) * 100.0f);
            if (n > 0) {
                this.awardStat(Stats.SWIM_ONE_CM, n);
                this.causeFoodExhaustion(0.01f * (float)n * 0.01f);
            }
        } else if (this.isEyeInFluid(FluidTags.WATER)) {
            int n = Math.round((float)Math.sqrt(d * d + d2 * d2 + d3 * d3) * 100.0f);
            if (n > 0) {
                this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, n);
                this.causeFoodExhaustion(0.01f * (float)n * 0.01f);
            }
        } else if (this.isInWater()) {
            int n = Math.round((float)Math.sqrt(d * d + d3 * d3) * 100.0f);
            if (n > 0) {
                this.awardStat(Stats.WALK_ON_WATER_ONE_CM, n);
                this.causeFoodExhaustion(0.01f * (float)n * 0.01f);
            }
        } else if (this.onClimbable()) {
            if (d2 > 0.0) {
                this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(d2 * 100.0));
            }
        } else if (this.onGround()) {
            int n = Math.round((float)Math.sqrt(d * d + d3 * d3) * 100.0f);
            if (n > 0) {
                if (this.isSprinting()) {
                    this.awardStat(Stats.SPRINT_ONE_CM, n);
                    this.causeFoodExhaustion(0.1f * (float)n * 0.01f);
                } else if (this.isCrouching()) {
                    this.awardStat(Stats.CROUCH_ONE_CM, n);
                    this.causeFoodExhaustion(0.0f * (float)n * 0.01f);
                } else {
                    this.awardStat(Stats.WALK_ONE_CM, n);
                    this.causeFoodExhaustion(0.0f * (float)n * 0.01f);
                }
            }
        } else if (this.isFallFlying()) {
            int n = Math.round((float)Math.sqrt(d * d + d2 * d2 + d3 * d3) * 100.0f);
            this.awardStat(Stats.AVIATE_ONE_CM, n);
        } else {
            int n = Math.round((float)Math.sqrt(d * d + d3 * d3) * 100.0f);
            if (n > 25) {
                this.awardStat(Stats.FLY_ONE_CM, n);
            }
        }
    }

    private void checkRidingStatistics(double d, double d2, double d3) {
        if (!this.isPassenger() || ServerPlayer.didNotMove(d, d2, d3)) {
            return;
        }
        int n = Math.round((float)Math.sqrt(d * d + d2 * d2 + d3 * d3) * 100.0f);
        Entity entity = this.getVehicle();
        if (entity instanceof AbstractMinecart) {
            this.awardStat(Stats.MINECART_ONE_CM, n);
        } else if (entity instanceof AbstractBoat) {
            this.awardStat(Stats.BOAT_ONE_CM, n);
        } else if (entity instanceof Pig) {
            this.awardStat(Stats.PIG_ONE_CM, n);
        } else if (entity instanceof AbstractHorse) {
            this.awardStat(Stats.HORSE_ONE_CM, n);
        } else if (entity instanceof Strider) {
            this.awardStat(Stats.STRIDER_ONE_CM, n);
        } else if (entity instanceof HappyGhast) {
            this.awardStat(Stats.HAPPY_GHAST_ONE_CM, n);
        }
    }

    private static boolean didNotMove(double d, double d2, double d3) {
        return d == 0.0 && d2 == 0.0 && d3 == 0.0;
    }

    @Override
    public void awardStat(Stat<?> stat, int n) {
        this.stats.increment(this, stat, n);
        this.getScoreboard().forAllObjectives(stat, this, scoreAccess -> scoreAccess.add(n));
    }

    @Override
    public void resetStat(Stat<?> stat) {
        this.stats.setValue(this, stat, 0);
        this.getScoreboard().forAllObjectives(stat, this, ScoreAccess::reset);
    }

    @Override
    public int awardRecipes(Collection<RecipeHolder<?>> collection) {
        return this.recipeBook.addRecipes(collection, this);
    }

    @Override
    public void triggerRecipeCrafted(RecipeHolder<?> recipeHolder, List<ItemStack> list) {
        CriteriaTriggers.RECIPE_CRAFTED.trigger(this, recipeHolder.id(), list);
    }

    @Override
    public void awardRecipesByKey(List<ResourceKey<Recipe<?>>> list) {
        List<RecipeHolder<?>> list2 = list.stream().flatMap(resourceKey -> this.server.getRecipeManager().byKey((ResourceKey<Recipe<?>>)resourceKey).stream()).collect(Collectors.toList());
        this.awardRecipes(list2);
    }

    @Override
    public int resetRecipes(Collection<RecipeHolder<?>> collection) {
        return this.recipeBook.removeRecipes(collection, this);
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.awardStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.causeFoodExhaustion(0.2f);
        } else {
            this.causeFoodExhaustion(0.05f);
        }
    }

    @Override
    public void giveExperiencePoints(int n) {
        super.giveExperiencePoints(n);
        this.lastSentExp = -1;
    }

    public void disconnect() {
        this.disconnected = true;
        this.ejectPassengers();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, false);
        }
    }

    public boolean hasDisconnected() {
        return this.disconnected;
    }

    public void resetSentInfo() {
        this.lastSentHealth = -1.0E8f;
    }

    @Override
    public void displayClientMessage(Component component, boolean bl) {
        this.sendSystemMessage(component, bl);
    }

    @Override
    protected void completeUsingItem() {
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.connection.send(new ClientboundEntityEventPacket(this, 9));
            super.completeUsingItem();
        }
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
        super.lookAt(anchor, vec3);
        this.connection.send(new ClientboundPlayerLookAtPacket(anchor, vec3.x, vec3.y, vec3.z));
    }

    public void lookAt(EntityAnchorArgument.Anchor anchor, Entity entity, EntityAnchorArgument.Anchor anchor2) {
        Vec3 vec3 = anchor2.apply(entity);
        super.lookAt(anchor, vec3);
        this.connection.send(new ClientboundPlayerLookAtPacket(anchor, entity, anchor2));
    }

    public void restoreFrom(ServerPlayer serverPlayer, boolean bl) {
        this.wardenSpawnTracker = serverPlayer.wardenSpawnTracker;
        this.chatSession = serverPlayer.chatSession;
        this.gameMode.setGameModeForPlayer(serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.gameMode.getPreviousGameModeForPlayer());
        this.onUpdateAbilities();
        if (bl) {
            this.getAttributes().assignBaseValues(serverPlayer.getAttributes());
            this.getAttributes().assignPermanentModifiers(serverPlayer.getAttributes());
            this.setHealth(serverPlayer.getHealth());
            this.foodData = serverPlayer.foodData;
            for (MobEffectInstance mobEffectInstance : serverPlayer.getActiveEffects()) {
                this.addEffect(new MobEffectInstance(mobEffectInstance));
            }
            this.getInventory().replaceWith(serverPlayer.getInventory());
            this.experienceLevel = serverPlayer.experienceLevel;
            this.totalExperience = serverPlayer.totalExperience;
            this.experienceProgress = serverPlayer.experienceProgress;
            this.setScore(serverPlayer.getScore());
            this.portalProcess = serverPlayer.portalProcess;
        } else {
            this.getAttributes().assignBaseValues(serverPlayer.getAttributes());
            this.setHealth(this.getMaxHealth());
            if (this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || serverPlayer.isSpectator()) {
                this.getInventory().replaceWith(serverPlayer.getInventory());
                this.experienceLevel = serverPlayer.experienceLevel;
                this.totalExperience = serverPlayer.totalExperience;
                this.experienceProgress = serverPlayer.experienceProgress;
                this.setScore(serverPlayer.getScore());
            }
        }
        this.enchantmentSeed = serverPlayer.enchantmentSeed;
        this.enderChestInventory = serverPlayer.enderChestInventory;
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (Byte)serverPlayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0f;
        this.lastSentFood = -1;
        this.recipeBook.copyOverData(serverPlayer.recipeBook);
        this.seenCredits = serverPlayer.seenCredits;
        this.enteredNetherPosition = serverPlayer.enteredNetherPosition;
        this.chunkTrackingView = serverPlayer.chunkTrackingView;
        this.setShoulderEntityLeft(serverPlayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(serverPlayer.getShoulderEntityRight());
        this.setLastDeathLocation(serverPlayer.getLastDeathLocation());
    }

    @Override
    protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        super.onEffectAdded(mobEffectInstance, entity);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, true));
        if (mobEffectInstance.is(MobEffects.LEVITATION)) {
            this.levitationStartTime = this.tickCount;
            this.levitationStartPos = this.position();
        }
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
    }

    @Override
    protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl, @Nullable Entity entity) {
        super.onEffectUpdated(mobEffectInstance, bl, entity);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, false));
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
    }

    @Override
    protected void onEffectsRemoved(Collection<MobEffectInstance> collection) {
        super.onEffectsRemoved(collection);
        for (MobEffectInstance mobEffectInstance : collection) {
            this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
            if (!mobEffectInstance.is(MobEffects.LEVITATION)) continue;
            this.levitationStartPos = null;
        }
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, (Entity)null);
    }

    @Override
    public void teleportTo(double d, double d2, double d3) {
        this.connection.teleport(new PositionMoveRotation(new Vec3(d, d2, d3), Vec3.ZERO, 0.0f, 0.0f), Relative.union(Relative.DELTA, Relative.ROTATION));
    }

    @Override
    public void teleportRelative(double d, double d2, double d3) {
        this.connection.teleport(new PositionMoveRotation(new Vec3(d, d2, d3), Vec3.ZERO, 0.0f, 0.0f), Relative.ALL);
    }

    @Override
    public boolean teleportTo(ServerLevel serverLevel, double d, double d2, double d3, Set<Relative> set, float f, float f2, boolean bl) {
        boolean bl2;
        if (this.isSleeping()) {
            this.stopSleepInBed(true, true);
        }
        if (bl) {
            this.setCamera(this);
        }
        if (bl2 = super.teleportTo(serverLevel, d, d2, d3, set, f, f2, bl)) {
            this.setYHeadRot(set.contains((Object)Relative.Y_ROT) ? this.getYHeadRot() + f : f);
        }
        return bl2;
    }

    @Override
    public void snapTo(double d, double d2, double d3) {
        super.snapTo(d, d2, d3);
        this.connection.resetPosition();
    }

    @Override
    public void crit(Entity entity) {
        this.level().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 4));
    }

    @Override
    public void magicCrit(Entity entity) {
        this.level().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 5));
    }

    @Override
    public void onUpdateAbilities() {
        if (this.connection == null) {
            return;
        }
        this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
        this.updateInvisibilityStatus();
    }

    @Override
    public ServerLevel level() {
        return (ServerLevel)super.level();
    }

    public boolean setGameMode(GameType gameType) {
        boolean bl = this.isSpectator();
        if (!this.gameMode.changeGameModeForPlayer(gameType)) {
            return false;
        }
        this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, gameType.getId()));
        if (gameType == GameType.SPECTATOR) {
            this.removeEntitiesOnShoulder();
            this.stopRiding();
            EnchantmentHelper.stopLocationBasedEffects(this);
        } else {
            this.setCamera(this);
            if (bl) {
                EnchantmentHelper.runLocationChangedEffects(this.level(), this);
            }
        }
        this.onUpdateAbilities();
        this.updateEffectVisibility();
        return true;
    }

    @Override
    @Nonnull
    public GameType gameMode() {
        return this.gameMode.getGameModeForPlayer();
    }

    public CommandSource commandSource() {
        return this.commandSource;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(this.commandSource(), this.position(), this.getRotationVector(), this.level(), this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.server, this);
    }

    public void sendSystemMessage(Component component) {
        this.sendSystemMessage(component, false);
    }

    public void sendSystemMessage(Component component, boolean bl) {
        if (!this.acceptsSystemMessages(bl)) {
            return;
        }
        this.connection.send(new ClientboundSystemChatPacket(component, bl), PacketSendListener.exceptionallySend(() -> {
            if (this.acceptsSystemMessages(false)) {
                int n = 256;
                String string = component.getString(256);
                MutableComponent mutableComponent = Component.literal(string).withStyle(ChatFormatting.YELLOW);
                return new ClientboundSystemChatPacket(Component.translatable("multiplayer.message_not_delivered", mutableComponent).withStyle(ChatFormatting.RED), false);
            }
            return null;
        }));
    }

    public void sendChatMessage(OutgoingChatMessage outgoingChatMessage, boolean bl, ChatType.Bound bound) {
        if (this.acceptsChatMessages()) {
            outgoingChatMessage.sendToPlayer(this, bl, bound);
        }
    }

    public String getIpAddress() {
        SocketAddress socketAddress = this.connection.getRemoteAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            return InetAddresses.toAddrString((InetAddress)inetSocketAddress.getAddress());
        }
        return "<unknown>";
    }

    public void updateOptions(ClientInformation clientInformation) {
        this.language = clientInformation.language();
        this.requestedViewDistance = clientInformation.viewDistance();
        this.chatVisibility = clientInformation.chatVisibility();
        this.canChatColor = clientInformation.chatColors();
        this.textFilteringEnabled = clientInformation.textFilteringEnabled();
        this.allowsListing = clientInformation.allowsListing();
        this.particleStatus = clientInformation.particleStatus();
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)clientInformation.modelCustomisation());
        this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)clientInformation.mainHand().getId());
    }

    public ClientInformation clientInformation() {
        byte by = (Byte)this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
        HumanoidArm humanoidArm = HumanoidArm.BY_ID.apply(((Byte)this.getEntityData().get(DATA_PLAYER_MAIN_HAND)).byteValue());
        return new ClientInformation(this.language, this.requestedViewDistance, this.chatVisibility, this.canChatColor, by, humanoidArm, this.textFilteringEnabled, this.allowsListing, this.particleStatus);
    }

    public boolean canChatInColor() {
        return this.canChatColor;
    }

    public ChatVisiblity getChatVisibility() {
        return this.chatVisibility;
    }

    private boolean acceptsSystemMessages(boolean bl) {
        if (this.chatVisibility == ChatVisiblity.HIDDEN) {
            return bl;
        }
        return true;
    }

    private boolean acceptsChatMessages() {
        return this.chatVisibility == ChatVisiblity.FULL;
    }

    public int requestedViewDistance() {
        return this.requestedViewDistance;
    }

    public void sendServerStatus(ServerStatus serverStatus) {
        this.connection.send(new ClientboundServerDataPacket(serverStatus.description(), serverStatus.favicon().map(ServerStatus.Favicon::iconBytes)));
    }

    @Override
    public int getPermissionLevel() {
        return this.server.getProfilePermissions(this.getGameProfile());
    }

    public void resetLastActionTime() {
        this.lastActionTime = Util.getMillis();
    }

    public ServerStatsCounter getStats() {
        return this.stats;
    }

    public ServerRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected void updateInvisibilityStatus() {
        if (this.isSpectator()) {
            this.removeEffectParticles();
            this.setInvisible(true);
        } else {
            super.updateInvisibilityStatus();
        }
    }

    public Entity getCamera() {
        return this.camera == null ? this : this.camera;
    }

    public void setCamera(@Nullable Entity entity) {
        Entity entity2 = this.getCamera();
        Entity entity3 = this.camera = entity == null ? this : entity;
        if (entity2 != this.camera) {
            Level level = this.camera.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.teleportTo(serverLevel, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot(), false);
            }
            if (entity != null) {
                this.level().getChunkSource().move(this);
            }
            this.connection.send(new ClientboundSetCameraPacket(this.camera));
            this.connection.resetPosition();
        }
    }

    @Override
    protected void processPortalCooldown() {
        if (!this.isChangingDimension) {
            super.processPortalCooldown();
        }
    }

    @Override
    public void attack(Entity entity) {
        if (this.isSpectator()) {
            this.setCamera(entity);
        } else {
            super.attack(entity);
        }
    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Nullable
    public Component getTabListDisplayName() {
        return null;
    }

    public int getTabListOrder() {
        return 0;
    }

    @Override
    public void swing(InteractionHand interactionHand) {
        super.swing(interactionHand);
        this.resetAttackStrengthTicker();
    }

    public boolean isChangingDimension() {
        return this.isChangingDimension;
    }

    public void hasChangedDimension() {
        this.isChangingDimension = false;
    }

    public PlayerAdvancements getAdvancements() {
        return this.advancements;
    }

    @Nullable
    public RespawnConfig getRespawnConfig() {
        return this.respawnConfig;
    }

    public void copyRespawnPosition(ServerPlayer serverPlayer) {
        this.setRespawnPosition(serverPlayer.respawnConfig, false);
    }

    public void setRespawnPosition(@Nullable RespawnConfig respawnConfig, boolean bl) {
        if (bl && respawnConfig != null && !respawnConfig.isSamePosition(this.respawnConfig)) {
            this.sendSystemMessage(SPAWN_SET_MESSAGE);
        }
        this.respawnConfig = respawnConfig;
    }

    public SectionPos getLastSectionPos() {
        return this.lastSectionPos;
    }

    public void setLastSectionPos(SectionPos sectionPos) {
        this.lastSectionPos = sectionPos;
    }

    public ChunkTrackingView getChunkTrackingView() {
        return this.chunkTrackingView;
    }

    public void setChunkTrackingView(ChunkTrackingView chunkTrackingView) {
        this.chunkTrackingView = chunkTrackingView;
    }

    @Override
    public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), soundSource, this.getX(), this.getY(), this.getZ(), f, f2, this.random.nextLong()));
    }

    @Override
    public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
        ItemEntity itemEntity = super.drop(itemStack, bl, bl2);
        if (bl2) {
            ItemStack itemStack2;
            ItemStack itemStack3 = itemStack2 = itemEntity != null ? itemEntity.getItem() : ItemStack.EMPTY;
            if (!itemStack2.isEmpty()) {
                this.awardStat(Stats.ITEM_DROPPED.get(itemStack2.getItem()), itemStack.getCount());
                this.awardStat(Stats.DROP);
            }
        }
        return itemEntity;
    }

    public TextFilter getTextFilter() {
        return this.textFilter;
    }

    public void setServerLevel(ServerLevel serverLevel) {
        this.setLevel(serverLevel);
        this.gameMode.setLevel(serverLevel);
    }

    @Nullable
    private static GameType readPlayerMode(@Nullable ValueInput valueInput, String string) {
        return valueInput != null ? (GameType)valueInput.read(string, GameType.LEGACY_ID_CODEC).orElse(null) : null;
    }

    private GameType calculateGameModeForNewPlayer(@Nullable GameType gameType) {
        GameType gameType2 = this.server.getForcedGameType();
        if (gameType2 != null) {
            return gameType2;
        }
        return gameType != null ? gameType : this.server.getDefaultGameType();
    }

    public void loadGameTypes(@Nullable ValueInput valueInput) {
        this.gameMode.setGameModeForPlayer(this.calculateGameModeForNewPlayer(ServerPlayer.readPlayerMode(valueInput, "playerGameType")), ServerPlayer.readPlayerMode(valueInput, "previousPlayerGameType"));
    }

    private void storeGameTypes(ValueOutput valueOutput) {
        valueOutput.store("playerGameType", GameType.LEGACY_ID_CODEC, this.gameMode.getGameModeForPlayer());
        GameType gameType = this.gameMode.getPreviousGameModeForPlayer();
        valueOutput.storeNullable("previousPlayerGameType", GameType.LEGACY_ID_CODEC, gameType);
    }

    @Override
    public boolean isTextFilteringEnabled() {
        return this.textFilteringEnabled;
    }

    public boolean shouldFilterMessageTo(ServerPlayer serverPlayer) {
        if (serverPlayer == this) {
            return false;
        }
        return this.textFilteringEnabled || serverPlayer.textFilteringEnabled;
    }

    @Override
    public boolean mayInteract(ServerLevel serverLevel, BlockPos blockPos) {
        return super.mayInteract(serverLevel, blockPos) && serverLevel.mayInteract(this, blockPos);
    }

    @Override
    protected void updateUsingItem(ItemStack itemStack) {
        CriteriaTriggers.USING_ITEM.trigger(this, itemStack);
        super.updateUsingItem(itemStack);
    }

    public boolean drop(boolean bl) {
        Inventory inventory = this.getInventory();
        ItemStack itemStack = inventory.removeFromSelected(bl);
        this.containerMenu.findSlot(inventory, inventory.getSelectedSlot()).ifPresent(n -> this.containerMenu.setRemoteSlot(n, inventory.getSelectedItem()));
        return this.drop(itemStack, false, true) != null;
    }

    @Override
    public void handleExtraItemsCreatedOnUse(ItemStack itemStack) {
        if (!this.getInventory().add(itemStack)) {
            this.drop(itemStack, false);
        }
    }

    public boolean allowsListing() {
        return this.allowsListing;
    }

    @Override
    public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
        return Optional.of(this.wardenSpawnTracker);
    }

    public void setSpawnExtraParticlesOnFall(boolean bl) {
        this.spawnExtraParticlesOnFall = bl;
    }

    @Override
    public void onItemPickup(ItemEntity itemEntity) {
        super.onItemPickup(itemEntity);
        Entity entity = itemEntity.getOwner();
        if (entity != null) {
            CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, itemEntity.getItem(), entity);
        }
    }

    public void setChatSession(RemoteChatSession remoteChatSession) {
        this.chatSession = remoteChatSession;
    }

    @Nullable
    public RemoteChatSession getChatSession() {
        if (this.chatSession != null && this.chatSession.hasExpired()) {
            return null;
        }
        return this.chatSession;
    }

    @Override
    public void indicateDamage(double d, double d2) {
        this.hurtDir = (float)(Mth.atan2(d2, d) * 57.2957763671875 - (double)this.getYRot());
        this.connection.send(new ClientboundHurtAnimationPacket(this));
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl) {
        if (super.startRiding(entity, bl)) {
            entity.positionRider(this);
            this.connection.teleport(new PositionMoveRotation(this.position(), Vec3.ZERO, 0.0f, 0.0f), Relative.ROTATION);
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                this.server.getPlayerList().sendActiveEffects(livingEntity, this.connection);
            }
            this.connection.send(new ClientboundSetPassengersPacket(entity));
            return true;
        }
        return false;
    }

    @Override
    public void removeVehicle() {
        Entity entity = this.getVehicle();
        super.removeVehicle();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            for (MobEffectInstance mobEffectInstance : livingEntity.getActiveEffects()) {
                this.connection.send(new ClientboundRemoveMobEffectPacket(entity.getId(), mobEffectInstance.getEffect()));
            }
        }
        if (entity != null) {
            this.connection.send(new ClientboundSetPassengersPacket(entity));
        }
    }

    public CommonPlayerSpawnInfo createCommonSpawnInfo(ServerLevel serverLevel) {
        return new CommonPlayerSpawnInfo(serverLevel.dimensionTypeRegistration(), serverLevel.dimension(), BiomeManager.obfuscateSeed(serverLevel.getSeed()), this.gameMode.getGameModeForPlayer(), this.gameMode.getPreviousGameModeForPlayer(), serverLevel.isDebug(), serverLevel.isFlat(), this.getLastDeathLocation(), this.getPortalCooldown(), serverLevel.getSeaLevel());
    }

    public void setRaidOmenPosition(BlockPos blockPos) {
        this.raidOmenPosition = blockPos;
    }

    public void clearRaidOmenPosition() {
        this.raidOmenPosition = null;
    }

    @Nullable
    public BlockPos getRaidOmenPosition() {
        return this.raidOmenPosition;
    }

    @Override
    public Vec3 getKnownMovement() {
        Entity entity = this.getVehicle();
        if (entity != null && entity.getControllingPassenger() != this) {
            return entity.getKnownMovement();
        }
        return this.lastKnownClientMovement;
    }

    public void setKnownMovement(Vec3 vec3) {
        this.lastKnownClientMovement = vec3;
    }

    @Override
    protected float getEnchantedDamage(Entity entity, float f, DamageSource damageSource) {
        return EnchantmentHelper.modifyDamage(this.level(), this.getWeaponItem(), entity, damageSource, f);
    }

    @Override
    public void onEquippedItemBroken(Item item, EquipmentSlot equipmentSlot) {
        super.onEquippedItemBroken(item, equipmentSlot);
        this.awardStat(Stats.ITEM_BROKEN.get(item));
    }

    public Input getLastClientInput() {
        return this.lastClientInput;
    }

    public void setLastClientInput(Input input) {
        this.lastClientInput = input;
    }

    public Vec3 getLastClientMoveIntent() {
        float f;
        float f2 = this.lastClientInput.left() == this.lastClientInput.right() ? 0.0f : (f = this.lastClientInput.left() ? 1.0f : -1.0f);
        float f3 = this.lastClientInput.forward() == this.lastClientInput.backward() ? 0.0f : (this.lastClientInput.forward() ? 1.0f : -1.0f);
        return ServerPlayer.getInputVector(new Vec3(f, 0.0, f3), 1.0f, this.getYRot());
    }

    public void registerEnderPearl(ThrownEnderpearl thrownEnderpearl) {
        this.enderPearls.add(thrownEnderpearl);
    }

    public void deregisterEnderPearl(ThrownEnderpearl thrownEnderpearl) {
        this.enderPearls.remove(thrownEnderpearl);
    }

    public Set<ThrownEnderpearl> getEnderPearls() {
        return this.enderPearls;
    }

    public long registerAndUpdateEnderPearlTicket(ThrownEnderpearl thrownEnderpearl) {
        Object object = thrownEnderpearl.level();
        if (object instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            object = thrownEnderpearl.chunkPosition();
            this.registerEnderPearl(thrownEnderpearl);
            serverLevel.resetEmptyTime();
            return ServerPlayer.placeEnderPearlTicket(serverLevel, (ChunkPos)object) - 1L;
        }
        return 0L;
    }

    public static long placeEnderPearlTicket(ServerLevel serverLevel, ChunkPos chunkPos) {
        serverLevel.getChunkSource().addTicketWithRadius(TicketType.ENDER_PEARL, chunkPos, 2);
        return TicketType.ENDER_PEARL.timeout();
    }

    @Override
    public /* synthetic */ Level level() {
        return this.level();
    }

    @Override
    @Nullable
    public /* synthetic */ Entity teleport(TeleportTransition teleportTransition) {
        return this.teleport(teleportTransition);
    }

    private /* synthetic */ Packet lambda$die$10(Component component) {
        int n = 256;
        String string = component.getString(256);
        MutableComponent mutableComponent = Component.translatable("death.attack.message_too_long", Component.literal(string).withStyle(ChatFormatting.YELLOW));
        MutableComponent mutableComponent2 = Component.translatable("death.attack.even_more_magic", this.getDisplayName()).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(mutableComponent)));
        return new ClientboundPlayerCombatKillPacket(this.getId(), mutableComponent2);
    }

    public static final class RespawnConfig
    extends Record {
        private final ResourceKey<Level> dimension;
        final BlockPos pos;
        final float angle;
        final boolean forced;
        public static final Codec<RespawnConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(RespawnConfig::dimension), (App)BlockPos.CODEC.fieldOf("pos").forGetter(RespawnConfig::pos), (App)Codec.FLOAT.optionalFieldOf("angle", (Object)Float.valueOf(0.0f)).forGetter(RespawnConfig::angle), (App)Codec.BOOL.optionalFieldOf("forced", (Object)false).forGetter(RespawnConfig::forced)).apply((Applicative)instance, RespawnConfig::new));

        public RespawnConfig(ResourceKey<Level> resourceKey, BlockPos blockPos, float f, boolean bl) {
            this.dimension = resourceKey;
            this.pos = blockPos;
            this.angle = f;
            this.forced = bl;
        }

        static ResourceKey<Level> getDimensionOrDefault(@Nullable RespawnConfig respawnConfig) {
            return respawnConfig != null ? respawnConfig.dimension() : Level.OVERWORLD;
        }

        public boolean isSamePosition(@Nullable RespawnConfig respawnConfig) {
            return respawnConfig != null && this.dimension == respawnConfig.dimension && this.pos.equals(respawnConfig.pos);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RespawnConfig.class, "dimension;pos;angle;forced", "dimension", "pos", "angle", "forced"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RespawnConfig.class, "dimension;pos;angle;forced", "dimension", "pos", "angle", "forced"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RespawnConfig.class, "dimension;pos;angle;forced", "dimension", "pos", "angle", "forced"}, this, object);
        }

        public ResourceKey<Level> dimension() {
            return this.dimension;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public float angle() {
            return this.angle;
        }

        public boolean forced() {
            return this.forced;
        }
    }

    record RespawnPosAngle(Vec3 position, float yaw) {
        public static RespawnPosAngle of(Vec3 vec3, BlockPos blockPos) {
            return new RespawnPosAngle(vec3, RespawnPosAngle.calculateLookAtYaw(vec3, blockPos));
        }

        private static float calculateLookAtYaw(Vec3 vec3, BlockPos blockPos) {
            Vec3 vec32 = Vec3.atBottomCenterOf(blockPos).subtract(vec3).normalize();
            return (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 57.2957763671875 - 90.0);
        }
    }
}

