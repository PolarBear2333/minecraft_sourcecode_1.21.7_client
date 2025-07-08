/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.common.hash.HashCode
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.TestInstanceBlockEditScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ChunkBatchSizeCalculator;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientRecipeContainer;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.DebugSampleSubscriber;
import net.minecraft.client.multiplayer.LevelLoadStatusManager;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SnifferSoundInstance;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiAddedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiRemovedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiTicketCountDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.RedstoneWireOrientationsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.common.custom.VillageSectionsDebugPayload;
import net.minecraft.network.protocol.common.custom.WorldGenAttemptDebugPayload;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTestInstanceBlockStatus;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Crypt;
import net.minecraft.util.HashOps;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ClientPacketListener
extends ClientCommonPacketListenerImpl
implements ClientGamePacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
    private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
    private static final Component INVALID_PACKET = Component.translatable("multiplayer.disconnect.invalid_packet");
    private static final Component RECONFIGURE_SCREEN_MESSAGE = Component.translatable("connect.reconfiguring");
    private static final Component BAD_CHAT_INDEX = Component.translatable("multiplayer.disconnect.bad_chat_index");
    private static final Component COMMAND_SEND_CONFIRM_TITLE = Component.translatable("multiplayer.confirm_command.title");
    private static final int PENDING_OFFSET_THRESHOLD = 64;
    public static final int TELEPORT_INTERPOLATION_THRESHOLD = 64;
    private static final ClientboundCommandsPacket.NodeBuilder<ClientSuggestionProvider> COMMAND_NODE_BUILDER = new ClientboundCommandsPacket.NodeBuilder<ClientSuggestionProvider>(){

        @Override
        public ArgumentBuilder<ClientSuggestionProvider, ?> createLiteral(String string) {
            return LiteralArgumentBuilder.literal((String)string);
        }

        @Override
        public ArgumentBuilder<ClientSuggestionProvider, ?> createArgument(String string, ArgumentType<?> argumentType, @Nullable ResourceLocation resourceLocation) {
            RequiredArgumentBuilder requiredArgumentBuilder = RequiredArgumentBuilder.argument((String)string, argumentType);
            if (resourceLocation != null) {
                requiredArgumentBuilder.suggests(SuggestionProviders.getProvider(resourceLocation));
            }
            return requiredArgumentBuilder;
        }

        @Override
        public ArgumentBuilder<ClientSuggestionProvider, ?> configure(ArgumentBuilder<ClientSuggestionProvider, ?> argumentBuilder, boolean bl, boolean bl2) {
            if (bl) {
                argumentBuilder.executes(commandContext -> 0);
            }
            if (bl2) {
                argumentBuilder.requires(ClientSuggestionProvider::allowsRestrictedCommands);
            }
            return argumentBuilder;
        }
    };
    private final GameProfile localGameProfile;
    private ClientLevel level;
    private ClientLevel.ClientLevelData levelData;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final Set<PlayerInfo> listedPlayers = new ReferenceOpenHashSet();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private final ClientSuggestionProvider restrictedSuggestionsProvider;
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private int serverSimulationDistance = 3;
    private final RandomSource random = RandomSource.createThreadSafe();
    private CommandDispatcher<ClientSuggestionProvider> commands = new CommandDispatcher();
    private ClientRecipeContainer recipes = new ClientRecipeContainer(Map.of(), SelectableRecipe.SingleInputSet.empty());
    private final UUID id = UUID.randomUUID();
    private Set<ResourceKey<Level>> levels;
    private final RegistryAccess.Frozen registryAccess;
    private final FeatureFlagSet enabledFeatures;
    private final PotionBrewing potionBrewing;
    private FuelValues fuelValues;
    private final HashedPatchMap.HashGenerator decoratedHashOpsGenerator;
    private OptionalInt removedPlayerVehicleId = OptionalInt.empty();
    @Nullable
    private LocalChatSession chatSession;
    private SignedMessageChain.Encoder signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
    private int nextChatIndex;
    private LastSeenMessagesTracker lastSeenMessages = new LastSeenMessagesTracker(20);
    private MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
    @Nullable
    private CompletableFuture<Optional<ProfileKeyPair>> keyPairFuture;
    @Nullable
    private ClientInformation remoteClientInformation;
    private final ChunkBatchSizeCalculator chunkBatchSizeCalculator = new ChunkBatchSizeCalculator();
    private final PingDebugMonitor pingDebugMonitor;
    private final DebugSampleSubscriber debugSampleSubscriber;
    @Nullable
    private LevelLoadStatusManager levelLoadStatusManager;
    private boolean serverEnforcesSecureChat;
    private boolean seenInsecureChatWarning = false;
    private volatile boolean closed;
    private final Scoreboard scoreboard = new Scoreboard();
    private final ClientWaypointManager waypointManager = new ClientWaypointManager();
    private final SessionSearchTrees searchTrees = new SessionSearchTrees();
    private final List<WeakReference<CacheSlot<?, ?>>> cacheSlots = new ArrayList();

    public ClientPacketListener(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
        this.localGameProfile = commonListenerCookie.localGameProfile();
        this.registryAccess = commonListenerCookie.receivedRegistries();
        RegistryOps<HashCode> registryOps = this.registryAccess.createSerializationContext(HashOps.CRC32C_INSTANCE);
        this.decoratedHashOpsGenerator = typedDataComponent -> ((HashCode)typedDataComponent.encodeValue(registryOps).getOrThrow(string -> new IllegalArgumentException("Failed to hash " + String.valueOf(typedDataComponent) + ": " + string))).asInt();
        this.enabledFeatures = commonListenerCookie.enabledFeatures();
        this.advancements = new ClientAdvancements(minecraft, this.telemetryManager);
        this.suggestionsProvider = new ClientSuggestionProvider(this, minecraft, true);
        this.restrictedSuggestionsProvider = new ClientSuggestionProvider(this, minecraft, false);
        this.pingDebugMonitor = new PingDebugMonitor(this, minecraft.getDebugOverlay().getPingLogger());
        this.debugSampleSubscriber = new DebugSampleSubscriber(this, minecraft.getDebugOverlay());
        if (commonListenerCookie.chatState() != null) {
            minecraft.gui.getChat().restoreState(commonListenerCookie.chatState());
        }
        this.potionBrewing = PotionBrewing.bootstrap(this.enabledFeatures);
        this.fuelValues = FuelValues.vanillaBurnTimes(commonListenerCookie.receivedRegistries(), this.enabledFeatures);
    }

    public ClientSuggestionProvider getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    public void close() {
        this.closed = true;
        this.clearLevel();
        this.telemetryManager.onDisconnect();
    }

    public void clearLevel() {
        this.clearCacheSlots();
        this.level = null;
        this.levelLoadStatusManager = null;
    }

    private void clearCacheSlots() {
        for (WeakReference<CacheSlot<?, ?>> weakReference : this.cacheSlots) {
            CacheSlot cacheSlot = (CacheSlot)weakReference.get();
            if (cacheSlot == null) continue;
            cacheSlot.clear();
        }
        this.cacheSlots.clear();
    }

    public RecipeAccess recipes() {
        return this.recipes;
    }

    @Override
    public void handleLogin(ClientboundLoginPacket clientboundLoginPacket) {
        ClientLevel.ClientLevelData clientLevelData;
        PacketUtils.ensureRunningOnSameThread(clientboundLoginPacket, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
        CommonPlayerSpawnInfo commonPlayerSpawnInfo = clientboundLoginPacket.commonPlayerSpawnInfo();
        ArrayList arrayList = Lists.newArrayList(clientboundLoginPacket.levels());
        Collections.shuffle(arrayList);
        this.levels = Sets.newLinkedHashSet((Iterable)arrayList);
        ResourceKey<Level> resourceKey = commonPlayerSpawnInfo.dimension();
        Holder<DimensionType> holder = commonPlayerSpawnInfo.dimensionType();
        this.serverChunkRadius = clientboundLoginPacket.chunkRadius();
        this.serverSimulationDistance = clientboundLoginPacket.simulationDistance();
        boolean bl = commonPlayerSpawnInfo.isDebug();
        boolean bl2 = commonPlayerSpawnInfo.isFlat();
        int n = commonPlayerSpawnInfo.seaLevel();
        this.levelData = clientLevelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, clientboundLoginPacket.hardcore(), bl2);
        this.level = new ClientLevel(this, clientLevelData, resourceKey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft.levelRenderer, bl, commonPlayerSpawnInfo.seed(), n);
        this.minecraft.setLevel(this.level, ReceivingLevelScreen.Reason.OTHER);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0f);
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }
        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        this.minecraft.player.setId(clientboundLoginPacket.playerId());
        this.level.addEntity(this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.startWaitingForNewLevel(this.minecraft.player, this.level, ReceivingLevelScreen.Reason.OTHER);
        this.minecraft.player.setReducedDebugInfo(clientboundLoginPacket.reducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(clientboundLoginPacket.showDeathScreen());
        this.minecraft.player.setDoLimitedCrafting(clientboundLoginPacket.doLimitedCrafting());
        this.minecraft.player.setLastDeathLocation(commonPlayerSpawnInfo.lastDeathLocation());
        this.minecraft.player.setPortalCooldown(commonPlayerSpawnInfo.portalCooldown());
        this.minecraft.gameMode.setLocalMode(commonPlayerSpawnInfo.gameType(), commonPlayerSpawnInfo.previousGameType());
        this.minecraft.options.setServerRenderDistance(clientboundLoginPacket.chunkRadius());
        this.chatSession = null;
        this.signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
        this.nextChatIndex = 0;
        this.lastSeenMessages = new LastSeenMessagesTracker(20);
        this.messageSignatureCache = MessageSignatureCache.createDefault();
        if (this.connection.isEncrypted()) {
            this.prepareKeyPair();
        }
        this.telemetryManager.onPlayerInfoReceived(commonPlayerSpawnInfo.gameType(), clientboundLoginPacket.hardcore());
        this.minecraft.quickPlayLog().log(this.minecraft);
        this.serverEnforcesSecureChat = clientboundLoginPacket.enforcesSecureChat();
        if (this.serverData != null && !this.seenInsecureChatWarning && !this.enforcesSecureChat()) {
            SystemToast systemToast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastId.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST);
            this.minecraft.getToastManager().addToast(systemToast);
            this.seenInsecureChatWarning = true;
        }
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        Entity entity;
        PacketUtils.ensureRunningOnSameThread(clientboundAddEntityPacket, this, this.minecraft);
        if (this.removedPlayerVehicleId.isPresent() && this.removedPlayerVehicleId.getAsInt() == clientboundAddEntityPacket.getId()) {
            this.removedPlayerVehicleId = OptionalInt.empty();
        }
        if ((entity = this.createEntityFromPacket(clientboundAddEntityPacket)) != null) {
            entity.recreateFromPacket(clientboundAddEntityPacket);
            this.level.addEntity(entity);
            this.postAddEntitySoundInstance(entity);
        } else {
            LOGGER.warn("Skipping Entity with id {}", clientboundAddEntityPacket.getType());
        }
    }

    @Nullable
    private Entity createEntityFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        EntityType<?> entityType = clientboundAddEntityPacket.getType();
        if (entityType == EntityType.PLAYER) {
            PlayerInfo playerInfo = this.getPlayerInfo(clientboundAddEntityPacket.getUUID());
            if (playerInfo == null) {
                LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", (Object)clientboundAddEntityPacket.getUUID());
                return null;
            }
            return new RemotePlayer(this.level, playerInfo.getProfile());
        }
        return entityType.create(this.level, EntitySpawnReason.LOAD);
    }

    private void postAddEntitySoundInstance(Entity entity) {
        if (entity instanceof AbstractMinecart) {
            AbstractMinecart abstractMinecart = (AbstractMinecart)entity;
            this.minecraft.getSoundManager().play(new MinecartSoundInstance(abstractMinecart));
        } else if (entity instanceof Bee) {
            Bee bee = (Bee)entity;
            boolean bl = bee.isAngry();
            BeeSoundInstance beeSoundInstance = bl ? new BeeAggressiveSoundInstance(bee) : new BeeFlyingSoundInstance(bee);
            this.minecraft.getSoundManager().queueTickingSound(beeSoundInstance);
        }
    }

    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEntityMotionPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEntityMotionPacket.getId());
        if (entity == null) {
            return;
        }
        entity.lerpMotion(clientboundSetEntityMotionPacket.getXa(), clientboundSetEntityMotionPacket.getYa(), clientboundSetEntityMotionPacket.getZa());
    }

    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEntityDataPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEntityDataPacket.id());
        if (entity != null) {
            entity.getEntityData().assignValues(clientboundSetEntityDataPacket.packedItems());
        }
    }

    @Override
    public void handleEntityPositionSync(ClientboundEntityPositionSyncPacket clientboundEntityPositionSyncPacket) {
        boolean bl;
        PacketUtils.ensureRunningOnSameThread(clientboundEntityPositionSyncPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundEntityPositionSyncPacket.id());
        if (entity == null) {
            return;
        }
        Vec3 vec3 = clientboundEntityPositionSyncPacket.values().position();
        entity.getPositionCodec().setBase(vec3);
        if (entity.isLocalInstanceAuthoritative()) {
            return;
        }
        float f = clientboundEntityPositionSyncPacket.values().yRot();
        float f2 = clientboundEntityPositionSyncPacket.values().xRot();
        boolean bl2 = bl = entity.position().distanceToSqr(vec3) > 4096.0;
        if (this.level.isTickingEntity(entity) && !bl) {
            entity.moveOrInterpolateTo(vec3, f, f2);
        } else {
            entity.snapTo(vec3, f, f2);
        }
        if (!entity.isInterpolating() && entity.hasIndirectPassenger(this.minecraft.player)) {
            entity.positionRider(this.minecraft.player);
            this.minecraft.player.setOldPosAndRot();
        }
        entity.setOnGround(clientboundEntityPositionSyncPacket.onGround());
    }

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTeleportEntityPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundTeleportEntityPacket.id());
        if (entity == null) {
            if (this.removedPlayerVehicleId.isPresent() && this.removedPlayerVehicleId.getAsInt() == clientboundTeleportEntityPacket.id()) {
                LOGGER.debug("Trying to teleport entity with id {}, that was formerly player vehicle, applying teleport to player instead", (Object)clientboundTeleportEntityPacket.id());
                ClientPacketListener.setValuesFromPositionPacket(clientboundTeleportEntityPacket.change(), clientboundTeleportEntityPacket.relatives(), this.minecraft.player, false);
                this.connection.send(new ServerboundMovePlayerPacket.PosRot(this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), this.minecraft.player.getYRot(), this.minecraft.player.getXRot(), false, false));
            }
            return;
        }
        boolean bl = clientboundTeleportEntityPacket.relatives().contains((Object)Relative.X) || clientboundTeleportEntityPacket.relatives().contains((Object)Relative.Y) || clientboundTeleportEntityPacket.relatives().contains((Object)Relative.Z);
        boolean bl2 = this.level.isTickingEntity(entity) || !entity.isLocalInstanceAuthoritative() || bl;
        boolean bl3 = ClientPacketListener.setValuesFromPositionPacket(clientboundTeleportEntityPacket.change(), clientboundTeleportEntityPacket.relatives(), entity, bl2);
        entity.setOnGround(clientboundTeleportEntityPacket.onGround());
        if (!bl3 && entity.hasIndirectPassenger(this.minecraft.player)) {
            entity.positionRider(this.minecraft.player);
            this.minecraft.player.setOldPosAndRot();
            if (entity.isLocalInstanceAuthoritative()) {
                this.connection.send(ServerboundMoveVehiclePacket.fromEntity(entity));
            }
        }
    }

    @Override
    public void handleTickingState(ClientboundTickingStatePacket clientboundTickingStatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTickingStatePacket, this, this.minecraft);
        if (this.minecraft.level == null) {
            return;
        }
        TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
        tickRateManager.setTickRate(clientboundTickingStatePacket.tickRate());
        tickRateManager.setFrozen(clientboundTickingStatePacket.isFrozen());
    }

    @Override
    public void handleTickingStep(ClientboundTickingStepPacket clientboundTickingStepPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTickingStepPacket, this, this.minecraft);
        if (this.minecraft.level == null) {
            return;
        }
        TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
        tickRateManager.setFrozenTicksToRun(clientboundTickingStepPacket.tickSteps());
    }

    @Override
    public void handleSetHeldSlot(ClientboundSetHeldSlotPacket clientboundSetHeldSlotPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetHeldSlotPacket, this, this.minecraft);
        if (Inventory.isHotbarSlot(clientboundSetHeldSlotPacket.slot())) {
            this.minecraft.player.getInventory().setSelectedSlot(clientboundSetHeldSlotPacket.slot());
        }
    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMoveEntityPacket, this, this.minecraft);
        Entity entity = clientboundMoveEntityPacket.getEntity(this.level);
        if (entity == null) {
            return;
        }
        if (entity.isLocalInstanceAuthoritative()) {
            VecDeltaCodec vecDeltaCodec = entity.getPositionCodec();
            Vec3 vec3 = vecDeltaCodec.decode(clientboundMoveEntityPacket.getXa(), clientboundMoveEntityPacket.getYa(), clientboundMoveEntityPacket.getZa());
            vecDeltaCodec.setBase(vec3);
            return;
        }
        if (clientboundMoveEntityPacket.hasPosition()) {
            VecDeltaCodec vecDeltaCodec = entity.getPositionCodec();
            Vec3 vec3 = vecDeltaCodec.decode(clientboundMoveEntityPacket.getXa(), clientboundMoveEntityPacket.getYa(), clientboundMoveEntityPacket.getZa());
            vecDeltaCodec.setBase(vec3);
            if (clientboundMoveEntityPacket.hasRotation()) {
                entity.moveOrInterpolateTo(vec3, clientboundMoveEntityPacket.getYRot(), clientboundMoveEntityPacket.getXRot());
            } else {
                entity.moveOrInterpolateTo(vec3, entity.getYRot(), entity.getXRot());
            }
        } else if (clientboundMoveEntityPacket.hasRotation()) {
            entity.moveOrInterpolateTo(entity.position(), clientboundMoveEntityPacket.getYRot(), clientboundMoveEntityPacket.getXRot());
        }
        entity.setOnGround(clientboundMoveEntityPacket.isOnGround());
    }

    @Override
    public void handleMinecartAlongTrack(ClientboundMoveMinecartPacket clientboundMoveMinecartPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMoveMinecartPacket, this, this.minecraft);
        Entity entity = clientboundMoveMinecartPacket.getEntity(this.level);
        if (!(entity instanceof AbstractMinecart)) {
            return;
        }
        AbstractMinecart abstractMinecart = (AbstractMinecart)entity;
        MinecartBehavior minecartBehavior = abstractMinecart.getBehavior();
        if (minecartBehavior instanceof NewMinecartBehavior) {
            NewMinecartBehavior newMinecartBehavior = (NewMinecartBehavior)minecartBehavior;
            newMinecartBehavior.lerpSteps.addAll(clientboundMoveMinecartPacket.lerpSteps());
        }
    }

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket clientboundRotateHeadPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRotateHeadPacket, this, this.minecraft);
        Entity entity = clientboundRotateHeadPacket.getEntity(this.level);
        if (entity == null) {
            return;
        }
        entity.lerpHeadTo(clientboundRotateHeadPacket.getYHeadRot(), 3);
    }

    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRemoveEntitiesPacket, this, this.minecraft);
        clientboundRemoveEntitiesPacket.getEntityIds().forEach(n -> {
            Entity entity = this.level.getEntity(n);
            if (entity == null) {
                return;
            }
            if (entity.hasIndirectPassenger(this.minecraft.player)) {
                LOGGER.debug("Remove entity {}:{} that has player as passenger", entity.getType(), (Object)n);
                this.removedPlayerVehicleId = OptionalInt.of(n);
            }
            this.level.removeEntity(n, Entity.RemovalReason.DISCARDED);
        });
    }

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerPositionPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        if (!localPlayer.isPassenger()) {
            ClientPacketListener.setValuesFromPositionPacket(clientboundPlayerPositionPacket.change(), clientboundPlayerPositionPacket.relatives(), localPlayer, false);
        }
        this.connection.send(new ServerboundAcceptTeleportationPacket(clientboundPlayerPositionPacket.id()));
        this.connection.send(new ServerboundMovePlayerPacket.PosRot(localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), localPlayer.getYRot(), localPlayer.getXRot(), false, false));
    }

    private static boolean setValuesFromPositionPacket(PositionMoveRotation positionMoveRotation, Set<Relative> set, Entity entity, boolean bl) {
        boolean bl2;
        PositionMoveRotation positionMoveRotation2 = PositionMoveRotation.of(entity);
        PositionMoveRotation positionMoveRotation3 = PositionMoveRotation.calculateAbsolute(positionMoveRotation2, positionMoveRotation, set);
        boolean bl3 = bl2 = positionMoveRotation2.position().distanceToSqr(positionMoveRotation3.position()) > 4096.0;
        if (bl && !bl2) {
            entity.moveOrInterpolateTo(positionMoveRotation3.position(), positionMoveRotation3.yRot(), positionMoveRotation3.xRot());
            entity.setDeltaMovement(positionMoveRotation3.deltaMovement());
            return true;
        }
        entity.setPos(positionMoveRotation3.position());
        entity.setDeltaMovement(positionMoveRotation3.deltaMovement());
        entity.setYRot(positionMoveRotation3.yRot());
        entity.setXRot(positionMoveRotation3.xRot());
        PositionMoveRotation positionMoveRotation4 = new PositionMoveRotation(entity.oldPosition(), Vec3.ZERO, entity.yRotO, entity.xRotO);
        PositionMoveRotation positionMoveRotation5 = PositionMoveRotation.calculateAbsolute(positionMoveRotation4, positionMoveRotation, set);
        entity.setOldPosAndRot(positionMoveRotation5.position(), positionMoveRotation5.yRot(), positionMoveRotation5.xRot());
        return false;
    }

    @Override
    public void handleRotatePlayer(ClientboundPlayerRotationPacket clientboundPlayerRotationPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerRotationPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        localPlayer.setYRot(clientboundPlayerRotationPacket.yRot());
        localPlayer.setXRot(clientboundPlayerRotationPacket.xRot());
        localPlayer.setOldRot();
        this.connection.send(new ServerboundMovePlayerPacket.Rot(localPlayer.getYRot(), localPlayer.getXRot(), false, false));
    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSectionBlocksUpdatePacket, this, this.minecraft);
        clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) -> this.level.setServerVerifiedBlockState((BlockPos)blockPos, (BlockState)blockState, 19));
    }

    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLevelChunkWithLightPacket, this, this.minecraft);
        int n = clientboundLevelChunkWithLightPacket.getX();
        int n2 = clientboundLevelChunkWithLightPacket.getZ();
        this.updateLevelChunk(n, n2, clientboundLevelChunkWithLightPacket.getChunkData());
        ClientboundLightUpdatePacketData clientboundLightUpdatePacketData = clientboundLevelChunkWithLightPacket.getLightData();
        this.level.queueLightUpdate(() -> {
            this.applyLightData(n, n2, clientboundLightUpdatePacketData, false);
            LevelChunk levelChunk = this.level.getChunkSource().getChunk(n, n2, false);
            if (levelChunk != null) {
                this.enableChunkLight(levelChunk, n, n2);
                this.minecraft.levelRenderer.onChunkReadyToRender(levelChunk.getPos());
            }
        });
    }

    @Override
    public void handleChunksBiomes(ClientboundChunksBiomesPacket clientboundChunksBiomesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundChunksBiomesPacket, this, this.minecraft);
        for (ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData : clientboundChunksBiomesPacket.chunkBiomeData()) {
            this.level.getChunkSource().replaceBiomes(chunkBiomeData.pos().x, chunkBiomeData.pos().z, chunkBiomeData.getReadBuffer());
        }
        for (ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData : clientboundChunksBiomesPacket.chunkBiomeData()) {
            this.level.onChunkLoaded(new ChunkPos(chunkBiomeData.pos().x, chunkBiomeData.pos().z));
        }
        for (ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData : clientboundChunksBiomesPacket.chunkBiomeData()) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = this.level.getMinSectionY(); k <= this.level.getMaxSectionY(); ++k) {
                        this.minecraft.levelRenderer.setSectionDirty(chunkBiomeData.pos().x + i, k, chunkBiomeData.pos().z + j);
                    }
                }
            }
        }
    }

    private void updateLevelChunk(int n, int n2, ClientboundLevelChunkPacketData clientboundLevelChunkPacketData) {
        this.level.getChunkSource().replaceWithPacketData(n, n2, clientboundLevelChunkPacketData.getReadBuffer(), clientboundLevelChunkPacketData.getHeightmaps(), clientboundLevelChunkPacketData.getBlockEntitiesTagsConsumer(n, n2));
    }

    private void enableChunkLight(LevelChunk levelChunk, int n, int n2) {
        LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] levelChunkSectionArray = levelChunk.getSections();
        ChunkPos chunkPos = levelChunk.getPos();
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            int n3 = this.level.getSectionYFromSectionIndex(i);
            levelLightEngine.updateSectionStatus(SectionPos.of(chunkPos, n3), levelChunkSection.hasOnlyAir());
        }
        this.level.setSectionRangeDirty(n - 1, this.level.getMinSectionY(), n2 - 1, n + 1, this.level.getMaxSectionY(), n2 + 1);
    }

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundForgetLevelChunkPacket, this, this.minecraft);
        this.level.getChunkSource().drop(clientboundForgetLevelChunkPacket.pos());
        this.queueLightRemoval(clientboundForgetLevelChunkPacket);
    }

    private void queueLightRemoval(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
        ChunkPos chunkPos = clientboundForgetLevelChunkPacket.pos();
        this.level.queueLightUpdate(() -> {
            int n;
            LevelLightEngine levelLightEngine = this.level.getLightEngine();
            levelLightEngine.setLightEnabled(chunkPos, false);
            for (n = levelLightEngine.getMinLightSection(); n < levelLightEngine.getMaxLightSection(); ++n) {
                SectionPos sectionPos = SectionPos.of(chunkPos, n);
                levelLightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, null);
                levelLightEngine.queueSectionData(LightLayer.SKY, sectionPos, null);
            }
            for (n = this.level.getMinSectionY(); n <= this.level.getMaxSectionY(); ++n) {
                levelLightEngine.updateSectionStatus(SectionPos.of(chunkPos, n), true);
            }
        });
    }

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockUpdatePacket, this, this.minecraft);
        this.level.setServerVerifiedBlockState(clientboundBlockUpdatePacket.getPos(), clientboundBlockUpdatePacket.getBlockState(), 19);
    }

    @Override
    public void handleConfigurationStart(ClientboundStartConfigurationPacket clientboundStartConfigurationPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundStartConfigurationPacket, this, this.minecraft);
        this.minecraft.getChatListener().clearQueue();
        this.sendChatAcknowledgement();
        ChatComponent.State state = this.minecraft.gui.getChat().storeState();
        this.minecraft.clearClientLevel(new ServerReconfigScreen(RECONFIGURE_SCREEN_MESSAGE, this.connection));
        this.connection.setupInboundProtocol(ConfigurationProtocols.CLIENTBOUND, new ClientConfigurationPacketListenerImpl(this.minecraft, this.connection, new CommonListenerCookie(this.localGameProfile, this.telemetryManager, this.registryAccess, this.enabledFeatures, this.serverBrand, this.serverData, this.postDisconnectScreen, this.serverCookies, state, this.customReportDetails, this.serverLinks())));
        this.send(ServerboundConfigurationAcknowledgedPacket.INSTANCE);
        this.connection.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND);
    }

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTakeItemEntityPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundTakeItemEntityPacket.getItemId());
        LivingEntity livingEntity = (LivingEntity)this.level.getEntity(clientboundTakeItemEntityPacket.getPlayerId());
        if (livingEntity == null) {
            livingEntity = this.minecraft.player;
        }
        if (entity != null) {
            if (entity instanceof ExperienceOrb) {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f, false);
            } else {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 1.4f + 2.0f, false);
            }
            this.minecraft.particleEngine.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.level, entity, livingEntity));
            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity)entity;
                ItemStack itemStack = itemEntity.getItem();
                if (!itemStack.isEmpty()) {
                    itemStack.shrink(clientboundTakeItemEntityPacket.getAmount());
                }
                if (itemStack.isEmpty()) {
                    this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(entity instanceof ExperienceOrb)) {
                this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void handleSystemChat(ClientboundSystemChatPacket clientboundSystemChatPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSystemChatPacket, this, this.minecraft);
        this.minecraft.getChatListener().handleSystemMessage(clientboundSystemChatPacket.content(), clientboundSystemChatPacket.overlay());
    }

    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerChatPacket, this, this.minecraft);
        int n = this.nextChatIndex++;
        if (clientboundPlayerChatPacket.globalIndex() != n) {
            LOGGER.error("Missing or out-of-order chat message from server, expected index {} but got {}", (Object)n, (Object)clientboundPlayerChatPacket.globalIndex());
            this.connection.disconnect(BAD_CHAT_INDEX);
            return;
        }
        Optional<SignedMessageBody> optional = clientboundPlayerChatPacket.body().unpack(this.messageSignatureCache);
        if (optional.isEmpty()) {
            LOGGER.error("Message from player with ID {} referenced unrecognized signature id", (Object)clientboundPlayerChatPacket.sender());
            this.connection.disconnect(INVALID_PACKET);
            return;
        }
        this.messageSignatureCache.push(optional.get(), clientboundPlayerChatPacket.signature());
        UUID uUID = clientboundPlayerChatPacket.sender();
        PlayerInfo playerInfo = this.getPlayerInfo(uUID);
        if (playerInfo == null) {
            LOGGER.error("Received player chat packet for unknown player with ID: {}", (Object)uUID);
            this.minecraft.getChatListener().handleChatMessageError(uUID, clientboundPlayerChatPacket.signature(), clientboundPlayerChatPacket.chatType());
            return;
        }
        RemoteChatSession remoteChatSession = playerInfo.getChatSession();
        SignedMessageLink signedMessageLink = remoteChatSession != null ? new SignedMessageLink(clientboundPlayerChatPacket.index(), uUID, remoteChatSession.sessionId()) : SignedMessageLink.unsigned(uUID);
        PlayerChatMessage playerChatMessage = new PlayerChatMessage(signedMessageLink, clientboundPlayerChatPacket.signature(), optional.get(), clientboundPlayerChatPacket.unsignedContent(), clientboundPlayerChatPacket.filterMask());
        playerChatMessage = playerInfo.getMessageValidator().updateAndValidate(playerChatMessage);
        if (playerChatMessage != null) {
            this.minecraft.getChatListener().handlePlayerChatMessage(playerChatMessage, playerInfo.getProfile(), clientboundPlayerChatPacket.chatType());
        } else {
            this.minecraft.getChatListener().handleChatMessageError(uUID, clientboundPlayerChatPacket.signature(), clientboundPlayerChatPacket.chatType());
        }
    }

    @Override
    public void handleDisguisedChat(ClientboundDisguisedChatPacket clientboundDisguisedChatPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundDisguisedChatPacket, this, this.minecraft);
        this.minecraft.getChatListener().handleDisguisedChatMessage(clientboundDisguisedChatPacket.message(), clientboundDisguisedChatPacket.chatType());
    }

    @Override
    public void handleDeleteChat(ClientboundDeleteChatPacket clientboundDeleteChatPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundDeleteChatPacket, this, this.minecraft);
        Optional<MessageSignature> optional = clientboundDeleteChatPacket.messageSignature().unpack(this.messageSignatureCache);
        if (optional.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET);
            return;
        }
        this.lastSeenMessages.ignorePending(optional.get());
        if (!this.minecraft.getChatListener().removeFromDelayedMessageQueue(optional.get())) {
            this.minecraft.gui.getChat().deleteMessage(optional.get());
        }
    }

    @Override
    public void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundAnimatePacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundAnimatePacket.getId());
        if (entity == null) {
            return;
        }
        if (clientboundAnimatePacket.getAction() == 0) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swing(InteractionHand.MAIN_HAND);
        } else if (clientboundAnimatePacket.getAction() == 3) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swing(InteractionHand.OFF_HAND);
        } else if (clientboundAnimatePacket.getAction() == 2) {
            Player player = (Player)entity;
            player.stopSleepInBed(false, false);
        } else if (clientboundAnimatePacket.getAction() == 4) {
            this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
        } else if (clientboundAnimatePacket.getAction() == 5) {
            this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
        }
    }

    @Override
    public void handleHurtAnimation(ClientboundHurtAnimationPacket clientboundHurtAnimationPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundHurtAnimationPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundHurtAnimationPacket.id());
        if (entity == null) {
            return;
        }
        entity.animateHurt(clientboundHurtAnimationPacket.yaw());
    }

    @Override
    public void handleSetTime(ClientboundSetTimePacket clientboundSetTimePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetTimePacket, this, this.minecraft);
        this.level.setTimeFromServer(clientboundSetTimePacket.gameTime(), clientboundSetTimePacket.dayTime(), clientboundSetTimePacket.tickDayTime());
        this.telemetryManager.setTime(clientboundSetTimePacket.gameTime());
    }

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundSetDefaultSpawnPositionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetDefaultSpawnPositionPacket, this, this.minecraft);
        this.minecraft.level.setDefaultSpawnPos(clientboundSetDefaultSpawnPositionPacket.getPos(), clientboundSetDefaultSpawnPositionPacket.getAngle());
    }

    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetPassengersPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetPassengersPacket.getVehicle());
        if (entity == null) {
            LOGGER.warn("Received passengers for unknown entity");
            return;
        }
        boolean bl = entity.hasIndirectPassenger(this.minecraft.player);
        entity.ejectPassengers();
        for (int n : clientboundSetPassengersPacket.getPassengers()) {
            Entity entity2 = this.level.getEntity(n);
            if (entity2 == null) continue;
            entity2.startRiding(entity, true);
            if (entity2 != this.minecraft.player) continue;
            this.removedPlayerVehicleId = OptionalInt.empty();
            if (bl) continue;
            if (entity instanceof AbstractBoat) {
                this.minecraft.player.yRotO = entity.getYRot();
                this.minecraft.player.setYRot(entity.getYRot());
                this.minecraft.player.setYHeadRot(entity.getYRot());
            }
            MutableComponent mutableComponent = Component.translatable("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage());
            this.minecraft.gui.setOverlayMessage(mutableComponent, false);
            this.minecraft.getNarrator().saySystemNow(mutableComponent);
        }
    }

    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEntityLinkPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEntityLinkPacket.getSourceId());
        if (entity instanceof Leashable) {
            Leashable leashable = (Leashable)((Object)entity);
            leashable.setDelayedLeashHolderId(clientboundSetEntityLinkPacket.getDestId());
        }
    }

    private static ItemStack findTotem(Player player) {
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (!itemStack.has(DataComponents.DEATH_PROTECTION)) continue;
            return itemStack;
        }
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundEntityEventPacket, this, this.minecraft);
        Entity entity = clientboundEntityEventPacket.getEntity(this.level);
        if (entity != null) {
            switch (clientboundEntityEventPacket.getEventId()) {
                case 63: {
                    this.minecraft.getSoundManager().play(new SnifferSoundInstance((Sniffer)entity));
                    break;
                }
                case 21: {
                    this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
                    break;
                }
                case 35: {
                    int n = 40;
                    this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0f, 1.0f, false);
                    if (entity != this.minecraft.player) break;
                    this.minecraft.gameRenderer.displayItemActivation(ClientPacketListener.findTotem(this.minecraft.player));
                    break;
                }
                default: {
                    entity.handleEntityEvent(clientboundEntityEventPacket.getEventId());
                }
            }
        }
    }

    @Override
    public void handleDamageEvent(ClientboundDamageEventPacket clientboundDamageEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundDamageEventPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundDamageEventPacket.entityId());
        if (entity == null) {
            return;
        }
        entity.handleDamageEvent(clientboundDamageEventPacket.getSource(this.level));
    }

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket clientboundSetHealthPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetHealthPacket, this, this.minecraft);
        this.minecraft.player.hurtTo(clientboundSetHealthPacket.getHealth());
        this.minecraft.player.getFoodData().setFoodLevel(clientboundSetHealthPacket.getFood());
        this.minecraft.player.getFoodData().setSaturation(clientboundSetHealthPacket.getSaturation());
    }

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket clientboundSetExperiencePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetExperiencePacket, this, this.minecraft);
        this.minecraft.player.setExperienceValues(clientboundSetExperiencePacket.getExperienceProgress(), clientboundSetExperiencePacket.getTotalExperience(), clientboundSetExperiencePacket.getExperienceLevel());
    }

    @Override
    public void handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket) {
        Object object;
        PacketUtils.ensureRunningOnSameThread(clientboundRespawnPacket, this, this.minecraft);
        CommonPlayerSpawnInfo commonPlayerSpawnInfo = clientboundRespawnPacket.commonPlayerSpawnInfo();
        ResourceKey<Level> resourceKey = commonPlayerSpawnInfo.dimension();
        Holder<DimensionType> holder = commonPlayerSpawnInfo.dimensionType();
        LocalPlayer localPlayer = this.minecraft.player;
        ResourceKey<Level> resourceKey2 = localPlayer.level().dimension();
        boolean bl = resourceKey != resourceKey2;
        ReceivingLevelScreen.Reason reason = this.determineLevelLoadingReason(localPlayer.isDeadOrDying(), resourceKey, resourceKey2);
        if (bl) {
            ClientLevel.ClientLevelData clientLevelData;
            object = this.level.getAllMapData();
            boolean bl2 = commonPlayerSpawnInfo.isDebug();
            boolean bl3 = commonPlayerSpawnInfo.isFlat();
            int n = commonPlayerSpawnInfo.seaLevel();
            this.levelData = clientLevelData = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), bl3);
            this.level = new ClientLevel(this, clientLevelData, resourceKey, holder, this.serverChunkRadius, this.serverSimulationDistance, this.minecraft.levelRenderer, bl2, commonPlayerSpawnInfo.seed(), n);
            this.level.addMapData((Map<MapId, MapItemSavedData>)object);
            this.minecraft.setLevel(this.level, reason);
        }
        this.minecraft.cameraEntity = null;
        if (localPlayer.hasContainerOpen()) {
            localPlayer.closeContainer();
        }
        object = clientboundRespawnPacket.shouldKeep((byte)2) ? this.minecraft.gameMode.createPlayer(this.level, localPlayer.getStats(), localPlayer.getRecipeBook(), localPlayer.getLastSentInput(), localPlayer.isSprinting()) : this.minecraft.gameMode.createPlayer(this.level, localPlayer.getStats(), localPlayer.getRecipeBook());
        this.startWaitingForNewLevel((LocalPlayer)object, this.level, reason);
        ((Entity)object).setId(localPlayer.getId());
        this.minecraft.player = object;
        if (bl) {
            this.minecraft.getMusicManager().stopPlaying();
        }
        this.minecraft.cameraEntity = object;
        if (clientboundRespawnPacket.shouldKeep((byte)2)) {
            List<SynchedEntityData.DataValue<?>> list = localPlayer.getEntityData().getNonDefaultValues();
            if (list != null) {
                ((Entity)object).getEntityData().assignValues(list);
            }
            ((Entity)object).setDeltaMovement(localPlayer.getDeltaMovement());
            ((Entity)object).setYRot(localPlayer.getYRot());
            ((Entity)object).setXRot(localPlayer.getXRot());
        } else {
            ((LocalPlayer)object).resetPos();
            ((Entity)object).setYRot(-180.0f);
        }
        if (clientboundRespawnPacket.shouldKeep((byte)1)) {
            ((LivingEntity)object).getAttributes().assignAllValues(localPlayer.getAttributes());
        } else {
            ((LivingEntity)object).getAttributes().assignBaseValues(localPlayer.getAttributes());
        }
        this.level.addEntity((Entity)object);
        ((LocalPlayer)object).input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer((Player)object);
        ((Player)object).setReducedDebugInfo(localPlayer.isReducedDebugInfo());
        ((LocalPlayer)object).setShowDeathScreen(localPlayer.shouldShowDeathScreen());
        ((Player)object).setLastDeathLocation(commonPlayerSpawnInfo.lastDeathLocation());
        ((Entity)object).setPortalCooldown(commonPlayerSpawnInfo.portalCooldown());
        ((LocalPlayer)object).portalEffectIntensity = localPlayer.portalEffectIntensity;
        ((LocalPlayer)object).oPortalEffectIntensity = localPlayer.oPortalEffectIntensity;
        if (this.minecraft.screen instanceof DeathScreen || this.minecraft.screen instanceof DeathScreen.TitleConfirmScreen) {
            this.minecraft.setScreen(null);
        }
        this.minecraft.gameMode.setLocalMode(commonPlayerSpawnInfo.gameType(), commonPlayerSpawnInfo.previousGameType());
    }

    private ReceivingLevelScreen.Reason determineLevelLoadingReason(boolean bl, ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
        ReceivingLevelScreen.Reason reason = ReceivingLevelScreen.Reason.OTHER;
        if (!bl) {
            if (resourceKey == Level.NETHER || resourceKey2 == Level.NETHER) {
                reason = ReceivingLevelScreen.Reason.NETHER_PORTAL;
            } else if (resourceKey == Level.END || resourceKey2 == Level.END) {
                reason = ReceivingLevelScreen.Reason.END_PORTAL;
            }
        }
        return reason;
    }

    @Override
    public void handleExplosion(ClientboundExplodePacket clientboundExplodePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundExplodePacket, this, this.minecraft);
        Vec3 vec3 = clientboundExplodePacket.center();
        this.minecraft.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), clientboundExplodePacket.explosionSound().value(), SoundSource.BLOCKS, 4.0f, (1.0f + (this.minecraft.level.random.nextFloat() - this.minecraft.level.random.nextFloat()) * 0.2f) * 0.7f, false);
        this.minecraft.level.addParticle(clientboundExplodePacket.explosionParticle(), vec3.x(), vec3.y(), vec3.z(), 1.0, 0.0, 0.0);
        clientboundExplodePacket.playerKnockback().ifPresent(this.minecraft.player::addDeltaMovement);
    }

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundHorseScreenOpenPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundHorseScreenOpenPacket.getEntityId());
        if (entity instanceof AbstractHorse) {
            AbstractHorse abstractHorse = (AbstractHorse)entity;
            LocalPlayer localPlayer = this.minecraft.player;
            int n = clientboundHorseScreenOpenPacket.getInventoryColumns();
            SimpleContainer simpleContainer = new SimpleContainer(AbstractHorse.getInventorySize(n));
            HorseInventoryMenu horseInventoryMenu = new HorseInventoryMenu(clientboundHorseScreenOpenPacket.getContainerId(), localPlayer.getInventory(), simpleContainer, abstractHorse, n);
            localPlayer.containerMenu = horseInventoryMenu;
            this.minecraft.setScreen(new HorseInventoryScreen(horseInventoryMenu, localPlayer.getInventory(), abstractHorse, n));
        }
    }

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundOpenScreenPacket, this, this.minecraft);
        MenuScreens.create(clientboundOpenScreenPacket.getType(), this.minecraft, clientboundOpenScreenPacket.getContainerId(), clientboundOpenScreenPacket.getTitle());
    }

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket) {
        Object object;
        PacketUtils.ensureRunningOnSameThread(clientboundContainerSetSlotPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        ItemStack itemStack = clientboundContainerSetSlotPacket.getItem();
        int n = clientboundContainerSetSlotPacket.getSlot();
        this.minecraft.getTutorial().onGetItem(itemStack);
        Screen screen = this.minecraft.screen;
        boolean bl = screen instanceof CreativeModeInventoryScreen ? !((CreativeModeInventoryScreen)(object = (CreativeModeInventoryScreen)screen)).isInventoryOpen() : false;
        if (clientboundContainerSetSlotPacket.getContainerId() == 0) {
            if (InventoryMenu.isHotbarSlot(n) && !itemStack.isEmpty() && (((ItemStack)(object = localPlayer.inventoryMenu.getSlot(n).getItem())).isEmpty() || ((ItemStack)object).getCount() < itemStack.getCount())) {
                itemStack.setPopTime(5);
            }
            localPlayer.inventoryMenu.setItem(n, clientboundContainerSetSlotPacket.getStateId(), itemStack);
        } else if (!(clientboundContainerSetSlotPacket.getContainerId() != localPlayer.containerMenu.containerId || clientboundContainerSetSlotPacket.getContainerId() == 0 && bl)) {
            localPlayer.containerMenu.setItem(n, clientboundContainerSetSlotPacket.getStateId(), itemStack);
        }
        if (this.minecraft.screen instanceof CreativeModeInventoryScreen) {
            localPlayer.inventoryMenu.setRemoteSlot(n, itemStack);
            localPlayer.inventoryMenu.broadcastChanges();
        }
    }

    @Override
    public void handleSetCursorItem(ClientboundSetCursorItemPacket clientboundSetCursorItemPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetCursorItemPacket, this, this.minecraft);
        this.minecraft.getTutorial().onGetItem(clientboundSetCursorItemPacket.contents());
        if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen)) {
            this.minecraft.player.containerMenu.setCarried(clientboundSetCursorItemPacket.contents());
        }
    }

    @Override
    public void handleSetPlayerInventory(ClientboundSetPlayerInventoryPacket clientboundSetPlayerInventoryPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetPlayerInventoryPacket, this, this.minecraft);
        this.minecraft.getTutorial().onGetItem(clientboundSetPlayerInventoryPacket.contents());
        this.minecraft.player.getInventory().setItem(clientboundSetPlayerInventoryPacket.slot(), clientboundSetPlayerInventoryPacket.contents());
    }

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerSetContentPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        if (clientboundContainerSetContentPacket.containerId() == 0) {
            localPlayer.inventoryMenu.initializeContents(clientboundContainerSetContentPacket.stateId(), clientboundContainerSetContentPacket.items(), clientboundContainerSetContentPacket.carriedItem());
        } else if (clientboundContainerSetContentPacket.containerId() == localPlayer.containerMenu.containerId) {
            localPlayer.containerMenu.initializeContents(clientboundContainerSetContentPacket.stateId(), clientboundContainerSetContentPacket.items(), clientboundContainerSetContentPacket.carriedItem());
        }
    }

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundOpenSignEditorPacket, this, this.minecraft);
        BlockPos blockPos = clientboundOpenSignEditorPacket.getPos();
        BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
            this.minecraft.player.openTextEdit(signBlockEntity, clientboundOpenSignEditorPacket.isFrontText());
        } else {
            LOGGER.warn("Ignoring openTextEdit on an invalid entity: {} at pos {}", (Object)this.level.getBlockEntity(blockPos), (Object)blockPos);
        }
    }

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockEntityDataPacket, this, this.minecraft);
        BlockPos blockPos = clientboundBlockEntityDataPacket.getPos();
        this.minecraft.level.getBlockEntity(blockPos, clientboundBlockEntityDataPacket.getType()).ifPresent(blockEntity -> {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER);){
                blockEntity.loadWithComponents(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)this.registryAccess, clientboundBlockEntityDataPacket.getTag()));
            }
            if (blockEntity instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen) {
                ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
            }
        });
    }

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerSetDataPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        if (localPlayer.containerMenu != null && localPlayer.containerMenu.containerId == clientboundContainerSetDataPacket.getContainerId()) {
            localPlayer.containerMenu.setData(clientboundContainerSetDataPacket.getId(), clientboundContainerSetDataPacket.getValue());
        }
    }

    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket clientboundSetEquipmentPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetEquipmentPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSetEquipmentPacket.getEntity());
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            clientboundSetEquipmentPacket.getSlots().forEach(pair -> livingEntity.setItemSlot((EquipmentSlot)pair.getFirst(), (ItemStack)pair.getSecond()));
        }
    }

    @Override
    public void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundContainerClosePacket, this, this.minecraft);
        this.minecraft.player.clientSideCloseContainer();
    }

    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockEventPacket, this, this.minecraft);
        this.minecraft.level.blockEvent(clientboundBlockEventPacket.getPos(), clientboundBlockEventPacket.getBlock(), clientboundBlockEventPacket.getB0(), clientboundBlockEventPacket.getB1());
    }

    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockDestructionPacket, this, this.minecraft);
        this.minecraft.level.destroyBlockProgress(clientboundBlockDestructionPacket.getId(), clientboundBlockDestructionPacket.getPos(), clientboundBlockDestructionPacket.getProgress());
    }

    @Override
    public void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundGameEventPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        ClientboundGameEventPacket.Type type = clientboundGameEventPacket.getEvent();
        float f = clientboundGameEventPacket.getParam();
        int n = Mth.floor(f + 0.5f);
        if (type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
            ((Player)localPlayer).displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
        } else if (type == ClientboundGameEventPacket.START_RAINING) {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0f);
        } else if (type == ClientboundGameEventPacket.STOP_RAINING) {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0f);
        } else if (type == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
            this.minecraft.gameMode.setLocalMode(GameType.byId(n));
        } else if (type == ClientboundGameEventPacket.WIN_GAME) {
            this.minecraft.setScreen(new WinScreen(true, () -> {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(null);
            }));
        } else if (type == ClientboundGameEventPacket.DEMO_EVENT) {
            Options options = this.minecraft.options;
            MutableComponent mutableComponent = null;
            if (f == 0.0f) {
                this.minecraft.setScreen(new DemoIntroScreen());
            } else if (f == 101.0f) {
                mutableComponent = Component.translatable("demo.help.movement", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage());
            } else if (f == 102.0f) {
                mutableComponent = Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage());
            } else if (f == 103.0f) {
                mutableComponent = Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage());
            } else if (f == 104.0f) {
                mutableComponent = Component.translatable("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage());
            }
            if (mutableComponent != null) {
                this.minecraft.gui.getChat().addMessage(mutableComponent);
                this.minecraft.getNarrator().saySystemQueued(mutableComponent);
            }
        } else if (type == ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND) {
            this.level.playSound((Entity)localPlayer, localPlayer.getX(), localPlayer.getEyeY(), localPlayer.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18f, 0.45f);
        } else if (type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
            this.level.setRainLevel(f);
        } else if (type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
            this.level.setThunderLevel(f);
        } else if (type == ClientboundGameEventPacket.PUFFER_FISH_STING) {
            this.level.playSound((Entity)localPlayer, localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else if (type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), 0.0, 0.0, 0.0);
            if (n == 1) {
                this.level.playSound((Entity)localPlayer, localPlayer.getX(), localPlayer.getY(), localPlayer.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0f, 1.0f);
            }
        } else if (type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
            this.minecraft.player.setShowDeathScreen(f == 0.0f);
        } else if (type == ClientboundGameEventPacket.LIMITED_CRAFTING) {
            this.minecraft.player.setDoLimitedCrafting(f == 1.0f);
        } else if (type == ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START && this.levelLoadStatusManager != null) {
            this.levelLoadStatusManager.loadingPacketsReceived();
        }
    }

    private void startWaitingForNewLevel(LocalPlayer localPlayer, ClientLevel clientLevel, ReceivingLevelScreen.Reason reason) {
        this.levelLoadStatusManager = new LevelLoadStatusManager(localPlayer, clientLevel, this.minecraft.levelRenderer);
        this.minecraft.setScreen(new ReceivingLevelScreen(this.levelLoadStatusManager::levelReady, reason));
    }

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMapItemDataPacket, this, this.minecraft);
        MapId mapId = clientboundMapItemDataPacket.mapId();
        MapItemSavedData mapItemSavedData = this.minecraft.level.getMapData(mapId);
        if (mapItemSavedData == null) {
            mapItemSavedData = MapItemSavedData.createForClient(clientboundMapItemDataPacket.scale(), clientboundMapItemDataPacket.locked(), this.minecraft.level.dimension());
            this.minecraft.level.overrideMapData(mapId, mapItemSavedData);
        }
        clientboundMapItemDataPacket.applyToMap(mapItemSavedData);
        this.minecraft.getMapTextureManager().update(mapId, mapItemSavedData);
    }

    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLevelEventPacket, this, this.minecraft);
        if (clientboundLevelEventPacket.isGlobalEvent()) {
            this.minecraft.level.globalLevelEvent(clientboundLevelEventPacket.getType(), clientboundLevelEventPacket.getPos(), clientboundLevelEventPacket.getData());
        } else {
            this.minecraft.level.levelEvent(clientboundLevelEventPacket.getType(), clientboundLevelEventPacket.getPos(), clientboundLevelEventPacket.getData());
        }
    }

    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateAdvancementsPacket, this, this.minecraft);
        this.advancements.update(clientboundUpdateAdvancementsPacket);
    }

    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundSelectAdvancementsTabPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSelectAdvancementsTabPacket, this, this.minecraft);
        ResourceLocation resourceLocation = clientboundSelectAdvancementsTabPacket.getTab();
        if (resourceLocation == null) {
            this.advancements.setSelectedTab(null, false);
        } else {
            AdvancementHolder advancementHolder = this.advancements.get(resourceLocation);
            this.advancements.setSelectedTab(advancementHolder, false);
        }
    }

    @Override
    public void handleCommands(ClientboundCommandsPacket clientboundCommandsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCommandsPacket, this, this.minecraft);
        this.commands = new CommandDispatcher(clientboundCommandsPacket.getRoot(CommandBuildContext.simple(this.registryAccess, this.enabledFeatures), COMMAND_NODE_BUILDER));
    }

    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket clientboundStopSoundPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundStopSoundPacket, this, this.minecraft);
        this.minecraft.getSoundManager().stop(clientboundStopSoundPacket.getName(), clientboundStopSoundPacket.getSource());
    }

    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCommandSuggestionsPacket, this, this.minecraft);
        this.suggestionsProvider.completeCustomSuggestions(clientboundCommandSuggestionsPacket.id(), clientboundCommandSuggestionsPacket.toSuggestions());
    }

    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateRecipesPacket, this, this.minecraft);
        this.recipes = new ClientRecipeContainer(clientboundUpdateRecipesPacket.itemSets(), clientboundUpdateRecipesPacket.stonecutterRecipes());
    }

    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket clientboundPlayerLookAtPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerLookAtPacket, this, this.minecraft);
        Vec3 vec3 = clientboundPlayerLookAtPacket.getPosition(this.level);
        if (vec3 != null) {
            this.minecraft.player.lookAt(clientboundPlayerLookAtPacket.getFromAnchor(), vec3);
        }
    }

    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket clientboundTagQueryPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTagQueryPacket, this, this.minecraft);
        if (!this.debugQueryHandler.handleResponse(clientboundTagQueryPacket.getTransactionId(), clientboundTagQueryPacket.getTag())) {
            LOGGER.debug("Got unhandled response to tag query {}", (Object)clientboundTagQueryPacket.getTransactionId());
        }
    }

    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket) {
        Object object2;
        PacketUtils.ensureRunningOnSameThread(clientboundAwardStatsPacket, this, this.minecraft);
        for (Object object2 : clientboundAwardStatsPacket.stats().object2IntEntrySet()) {
            Stat stat = (Stat)object2.getKey();
            int n = object2.getIntValue();
            this.minecraft.player.getStats().setValue(this.minecraft.player, stat, n);
        }
        object2 = this.minecraft.screen;
        if (object2 instanceof StatsScreen) {
            StatsScreen statsScreen = (StatsScreen)object2;
            statsScreen.onStatsUpdated();
        }
    }

    @Override
    public void handleRecipeBookAdd(ClientboundRecipeBookAddPacket clientboundRecipeBookAddPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRecipeBookAddPacket, this, this.minecraft);
        ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
        if (clientboundRecipeBookAddPacket.replace()) {
            clientRecipeBook.clear();
        }
        for (ClientboundRecipeBookAddPacket.Entry entry : clientboundRecipeBookAddPacket.entries()) {
            clientRecipeBook.add(entry.contents());
            if (entry.highlight()) {
                clientRecipeBook.addHighlight(entry.contents().id());
            }
            if (!entry.notification()) continue;
            RecipeToast.addOrUpdate(this.minecraft.getToastManager(), entry.contents().display());
        }
        this.refreshRecipeBook(clientRecipeBook);
    }

    @Override
    public void handleRecipeBookRemove(ClientboundRecipeBookRemovePacket clientboundRecipeBookRemovePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRecipeBookRemovePacket, this, this.minecraft);
        ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
        for (RecipeDisplayId recipeDisplayId : clientboundRecipeBookRemovePacket.recipes()) {
            clientRecipeBook.remove(recipeDisplayId);
        }
        this.refreshRecipeBook(clientRecipeBook);
    }

    @Override
    public void handleRecipeBookSettings(ClientboundRecipeBookSettingsPacket clientboundRecipeBookSettingsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRecipeBookSettingsPacket, this, this.minecraft);
        ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
        clientRecipeBook.setBookSettings(clientboundRecipeBookSettingsPacket.bookSettings());
        this.refreshRecipeBook(clientRecipeBook);
    }

    private void refreshRecipeBook(ClientRecipeBook clientRecipeBook) {
        clientRecipeBook.rebuildCollections();
        this.searchTrees.updateRecipes(clientRecipeBook, this.level);
        Screen screen = this.minecraft.screen;
        if (screen instanceof RecipeUpdateListener) {
            RecipeUpdateListener recipeUpdateListener = (RecipeUpdateListener)((Object)screen);
            recipeUpdateListener.recipesUpdated();
        }
    }

    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateMobEffectPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundUpdateMobEffectPacket.getEntityId());
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        Holder<MobEffect> holder = clientboundUpdateMobEffectPacket.getEffect();
        MobEffectInstance mobEffectInstance = new MobEffectInstance(holder, clientboundUpdateMobEffectPacket.getEffectDurationTicks(), clientboundUpdateMobEffectPacket.getEffectAmplifier(), clientboundUpdateMobEffectPacket.isEffectAmbient(), clientboundUpdateMobEffectPacket.isEffectVisible(), clientboundUpdateMobEffectPacket.effectShowsIcon(), null);
        if (!clientboundUpdateMobEffectPacket.shouldBlend()) {
            mobEffectInstance.skipBlending();
        }
        ((LivingEntity)entity).forceAddEffect(mobEffectInstance, null);
    }

    private <T> Registry.PendingTags<T> updateTags(ResourceKey<? extends Registry<? extends T>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
        HolderLookup.RegistryLookup registryLookup = this.registryAccess.lookupOrThrow((ResourceKey)resourceKey);
        return registryLookup.prepareTagReload(networkPayload.resolve(registryLookup));
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateTagsPacket, this, this.minecraft);
        ArrayList arrayList = new ArrayList(clientboundUpdateTagsPacket.getTags().size());
        boolean bl = this.connection.isMemoryConnection();
        clientboundUpdateTagsPacket.getTags().forEach((resourceKey, networkPayload) -> {
            if (!bl || RegistrySynchronization.isNetworkable(resourceKey)) {
                arrayList.add(this.updateTags((ResourceKey)resourceKey, (TagNetworkSerialization.NetworkPayload)networkPayload));
            }
        });
        arrayList.forEach(Registry.PendingTags::apply);
        this.fuelValues = FuelValues.vanillaBurnTimes(this.registryAccess, this.enabledFeatures);
        List<ItemStack> list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
        this.searchTrees.updateCreativeTags(list);
    }

    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundPlayerCombatEndPacket) {
    }

    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundPlayerCombatEnterPacket) {
    }

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerCombatKillPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundPlayerCombatKillPacket.playerId());
        if (entity == this.minecraft.player) {
            if (this.minecraft.player.shouldShowDeathScreen()) {
                this.minecraft.setScreen(new DeathScreen(clientboundPlayerCombatKillPacket.message(), this.level.getLevelData().isHardcore()));
            } else {
                this.minecraft.player.respawn();
            }
        }
    }

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundChangeDifficultyPacket, this, this.minecraft);
        this.levelData.setDifficulty(clientboundChangeDifficultyPacket.difficulty());
        this.levelData.setDifficultyLocked(clientboundChangeDifficultyPacket.locked());
    }

    @Override
    public void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetCameraPacket, this, this.minecraft);
        Entity entity = clientboundSetCameraPacket.getEntity(this.level);
        if (entity != null) {
            this.minecraft.setCameraEntity(entity);
        }
    }

    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundInitializeBorderPacket, this, this.minecraft);
        WorldBorder worldBorder = this.level.getWorldBorder();
        worldBorder.setCenter(clientboundInitializeBorderPacket.getNewCenterX(), clientboundInitializeBorderPacket.getNewCenterZ());
        long l = clientboundInitializeBorderPacket.getLerpTime();
        if (l > 0L) {
            worldBorder.lerpSizeBetween(clientboundInitializeBorderPacket.getOldSize(), clientboundInitializeBorderPacket.getNewSize(), l);
        } else {
            worldBorder.setSize(clientboundInitializeBorderPacket.getNewSize());
        }
        worldBorder.setAbsoluteMaxSize(clientboundInitializeBorderPacket.getNewAbsoluteMaxSize());
        worldBorder.setWarningBlocks(clientboundInitializeBorderPacket.getWarningBlocks());
        worldBorder.setWarningTime(clientboundInitializeBorderPacket.getWarningTime());
    }

    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundSetBorderCenterPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderCenterPacket, this, this.minecraft);
        this.level.getWorldBorder().setCenter(clientboundSetBorderCenterPacket.getNewCenterX(), clientboundSetBorderCenterPacket.getNewCenterZ());
    }

    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundSetBorderLerpSizePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderLerpSizePacket, this, this.minecraft);
        this.level.getWorldBorder().lerpSizeBetween(clientboundSetBorderLerpSizePacket.getOldSize(), clientboundSetBorderLerpSizePacket.getNewSize(), clientboundSetBorderLerpSizePacket.getLerpTime());
    }

    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundSetBorderSizePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderSizePacket, this, this.minecraft);
        this.level.getWorldBorder().setSize(clientboundSetBorderSizePacket.getSize());
    }

    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundSetBorderWarningDistancePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderWarningDistancePacket, this, this.minecraft);
        this.level.getWorldBorder().setWarningBlocks(clientboundSetBorderWarningDistancePacket.getWarningBlocks());
    }

    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundSetBorderWarningDelayPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetBorderWarningDelayPacket, this, this.minecraft);
        this.level.getWorldBorder().setWarningTime(clientboundSetBorderWarningDelayPacket.getWarningDelay());
    }

    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket clientboundClearTitlesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundClearTitlesPacket, this, this.minecraft);
        this.minecraft.gui.clearTitles();
        if (clientboundClearTitlesPacket.shouldResetTimes()) {
            this.minecraft.gui.resetTitleTimes();
        }
    }

    @Override
    public void handleServerData(ClientboundServerDataPacket clientboundServerDataPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundServerDataPacket, this, this.minecraft);
        if (this.serverData == null) {
            return;
        }
        this.serverData.motd = clientboundServerDataPacket.motd();
        clientboundServerDataPacket.iconBytes().map(ServerData::validateIcon).ifPresent(this.serverData::setIconBytes);
        ServerList.saveSingleServer(this.serverData);
    }

    @Override
    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket clientboundCustomChatCompletionsPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCustomChatCompletionsPacket, this, this.minecraft);
        this.suggestionsProvider.modifyCustomCompletions(clientboundCustomChatCompletionsPacket.action(), clientboundCustomChatCompletionsPacket.entries());
    }

    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket clientboundSetActionBarTextPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetActionBarTextPacket, this, this.minecraft);
        this.minecraft.gui.setOverlayMessage(clientboundSetActionBarTextPacket.text(), false);
    }

    @Override
    public void setTitleText(ClientboundSetTitleTextPacket clientboundSetTitleTextPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetTitleTextPacket, this, this.minecraft);
        this.minecraft.gui.setTitle(clientboundSetTitleTextPacket.text());
    }

    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundSetSubtitleTextPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetSubtitleTextPacket, this, this.minecraft);
        this.minecraft.gui.setSubtitle(clientboundSetSubtitleTextPacket.text());
    }

    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetTitlesAnimationPacket, this, this.minecraft);
        this.minecraft.gui.setTimes(clientboundSetTitlesAnimationPacket.getFadeIn(), clientboundSetTitlesAnimationPacket.getStay(), clientboundSetTitlesAnimationPacket.getFadeOut());
    }

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTabListPacket, this, this.minecraft);
        this.minecraft.gui.getTabList().setHeader(clientboundTabListPacket.header().getString().isEmpty() ? null : clientboundTabListPacket.header());
        this.minecraft.gui.getTabList().setFooter(clientboundTabListPacket.footer().getString().isEmpty() ? null : clientboundTabListPacket.footer());
    }

    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundRemoveMobEffectPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundRemoveMobEffectPacket, this, this.minecraft);
        Entity entity = clientboundRemoveMobEffectPacket.getEntity(this.level);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.removeEffectNoUpdate(clientboundRemoveMobEffectPacket.effect());
        }
    }

    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerInfoRemovePacket, this, this.minecraft);
        for (UUID uUID : clientboundPlayerInfoRemovePacket.profileIds()) {
            this.minecraft.getPlayerSocialManager().removePlayer(uUID);
            PlayerInfo playerInfo = this.playerInfoMap.remove(uUID);
            if (playerInfo == null) continue;
            this.listedPlayers.remove(playerInfo);
        }
    }

    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket clientboundPlayerInfoUpdatePacket) {
        PlayerInfo playerInfo;
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerInfoUpdatePacket, this, this.minecraft);
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : clientboundPlayerInfoUpdatePacket.newEntries()) {
            playerInfo = new PlayerInfo(Objects.requireNonNull(entry.profile()), this.enforcesSecureChat());
            if (this.playerInfoMap.putIfAbsent(entry.profileId(), playerInfo) != null) continue;
            this.minecraft.getPlayerSocialManager().addPlayer(playerInfo);
        }
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : clientboundPlayerInfoUpdatePacket.entries()) {
            playerInfo = this.playerInfoMap.get(entry.profileId());
            if (playerInfo == null) {
                LOGGER.warn("Ignoring player info update for unknown player {} ({})", (Object)entry.profileId(), clientboundPlayerInfoUpdatePacket.actions());
                continue;
            }
            for (ClientboundPlayerInfoUpdatePacket.Action action : clientboundPlayerInfoUpdatePacket.actions()) {
                this.applyPlayerInfoUpdate(action, entry, playerInfo);
            }
        }
    }

    private void applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry entry, PlayerInfo playerInfo) {
        switch (action) {
            case INITIALIZE_CHAT: {
                this.initializeChatSession(entry, playerInfo);
                break;
            }
            case UPDATE_GAME_MODE: {
                if (playerInfo.getGameMode() != entry.gameMode() && this.minecraft.player != null && this.minecraft.player.getUUID().equals(entry.profileId())) {
                    this.minecraft.player.onGameModeChanged(entry.gameMode());
                }
                playerInfo.setGameMode(entry.gameMode());
                break;
            }
            case UPDATE_LISTED: {
                if (entry.listed()) {
                    this.listedPlayers.add(playerInfo);
                    break;
                }
                this.listedPlayers.remove(playerInfo);
                break;
            }
            case UPDATE_LATENCY: {
                playerInfo.setLatency(entry.latency());
                break;
            }
            case UPDATE_DISPLAY_NAME: {
                playerInfo.setTabListDisplayName(entry.displayName());
                break;
            }
            case UPDATE_HAT: {
                playerInfo.setShowHat(entry.showHat());
                break;
            }
            case UPDATE_LIST_ORDER: {
                playerInfo.setTabListOrder(entry.listOrder());
            }
        }
    }

    private void initializeChatSession(ClientboundPlayerInfoUpdatePacket.Entry entry, PlayerInfo playerInfo) {
        GameProfile gameProfile = playerInfo.getProfile();
        SignatureValidator signatureValidator = this.minecraft.getProfileKeySignatureValidator();
        if (signatureValidator == null) {
            LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)gameProfile.getName());
            playerInfo.clearChatSession(this.enforcesSecureChat());
            return;
        }
        RemoteChatSession.Data data = entry.chatSession();
        if (data != null) {
            try {
                RemoteChatSession remoteChatSession = data.validate(gameProfile, signatureValidator);
                playerInfo.setChatSession(remoteChatSession);
            }
            catch (ProfilePublicKey.ValidationException validationException) {
                LOGGER.error("Failed to validate profile key for player: '{}'", (Object)gameProfile.getName(), (Object)validationException);
                playerInfo.clearChatSession(this.enforcesSecureChat());
            }
        } else {
            playerInfo.clearChatSession(this.enforcesSecureChat());
        }
    }

    private boolean enforcesSecureChat() {
        return this.minecraft.canValidateProfileKeys() && this.serverEnforcesSecureChat;
    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlayerAbilitiesPacket, this, this.minecraft);
        LocalPlayer localPlayer = this.minecraft.player;
        localPlayer.getAbilities().flying = clientboundPlayerAbilitiesPacket.isFlying();
        localPlayer.getAbilities().instabuild = clientboundPlayerAbilitiesPacket.canInstabuild();
        localPlayer.getAbilities().invulnerable = clientboundPlayerAbilitiesPacket.isInvulnerable();
        localPlayer.getAbilities().mayfly = clientboundPlayerAbilitiesPacket.canFly();
        localPlayer.getAbilities().setFlyingSpeed(clientboundPlayerAbilitiesPacket.getFlyingSpeed());
        localPlayer.getAbilities().setWalkingSpeed(clientboundPlayerAbilitiesPacket.getWalkingSpeed());
    }

    @Override
    public void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSoundPacket, this, this.minecraft);
        this.minecraft.level.playSeededSound((Entity)this.minecraft.player, clientboundSoundPacket.getX(), clientboundSoundPacket.getY(), clientboundSoundPacket.getZ(), clientboundSoundPacket.getSound(), clientboundSoundPacket.getSource(), clientboundSoundPacket.getVolume(), clientboundSoundPacket.getPitch(), clientboundSoundPacket.getSeed());
    }

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSoundEntityPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundSoundEntityPacket.getId());
        if (entity == null) {
            return;
        }
        this.minecraft.level.playSeededSound(this.minecraft.player, entity, clientboundSoundEntityPacket.getSound(), clientboundSoundEntityPacket.getSource(), clientboundSoundEntityPacket.getVolume(), clientboundSoundEntityPacket.getPitch(), clientboundSoundEntityPacket.getSeed());
    }

    @Override
    public void handleBossUpdate(ClientboundBossEventPacket clientboundBossEventPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBossEventPacket, this, this.minecraft);
        this.minecraft.gui.getBossOverlay().update(clientboundBossEventPacket);
    }

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket clientboundCooldownPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundCooldownPacket, this, this.minecraft);
        if (clientboundCooldownPacket.duration() == 0) {
            this.minecraft.player.getCooldowns().removeCooldown(clientboundCooldownPacket.cooldownGroup());
        } else {
            this.minecraft.player.getCooldowns().addCooldown(clientboundCooldownPacket.cooldownGroup(), clientboundCooldownPacket.duration());
        }
    }

    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundMoveVehiclePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMoveVehiclePacket, this, this.minecraft);
        Entity entity = this.minecraft.player.getRootVehicle();
        if (entity != this.minecraft.player && entity.isLocalInstanceAuthoritative()) {
            Vec3 vec3;
            Vec3 vec32 = clientboundMoveVehiclePacket.position();
            if (vec32.distanceTo(vec3 = entity.isInterpolating() ? entity.getInterpolation().position() : entity.position()) > (double)1.0E-5f) {
                if (entity.isInterpolating()) {
                    entity.getInterpolation().cancel();
                }
                entity.absSnapTo(vec32.x(), vec32.y(), vec32.z(), clientboundMoveVehiclePacket.yRot(), clientboundMoveVehiclePacket.xRot());
            }
            this.connection.send(ServerboundMoveVehiclePacket.fromEntity(entity));
        }
    }

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundOpenBookPacket, this, this.minecraft);
        ItemStack itemStack = this.minecraft.player.getItemInHand(clientboundOpenBookPacket.getHand());
        BookViewScreen.BookAccess bookAccess = BookViewScreen.BookAccess.fromItem(itemStack);
        if (bookAccess != null) {
            this.minecraft.setScreen(new BookViewScreen(bookAccess));
        }
    }

    @Override
    public void handleCustomPayload(CustomPacketPayload customPacketPayload) {
        if (customPacketPayload instanceof PathfindingDebugPayload) {
            PathfindingDebugPayload pathfindingDebugPayload = (PathfindingDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.pathfindingRenderer.addPath(pathfindingDebugPayload.entityId(), pathfindingDebugPayload.path(), pathfindingDebugPayload.maxNodeDistance());
        } else if (customPacketPayload instanceof NeighborUpdatesDebugPayload) {
            NeighborUpdatesDebugPayload neighborUpdatesDebugPayload = (NeighborUpdatesDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.neighborsUpdateRenderer.addUpdate(neighborUpdatesDebugPayload.time(), neighborUpdatesDebugPayload.pos());
        } else if (customPacketPayload instanceof RedstoneWireOrientationsDebugPayload) {
            RedstoneWireOrientationsDebugPayload redstoneWireOrientationsDebugPayload = (RedstoneWireOrientationsDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.redstoneWireOrientationsRenderer.addWireOrientations(redstoneWireOrientationsDebugPayload);
        } else if (customPacketPayload instanceof StructuresDebugPayload) {
            StructuresDebugPayload structuresDebugPayload = (StructuresDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.structureRenderer.addBoundingBox(structuresDebugPayload.mainBB(), structuresDebugPayload.pieces(), structuresDebugPayload.dimension());
        } else if (customPacketPayload instanceof WorldGenAttemptDebugPayload) {
            WorldGenAttemptDebugPayload worldGenAttemptDebugPayload = (WorldGenAttemptDebugPayload)customPacketPayload;
            ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer).addPos(worldGenAttemptDebugPayload.pos(), worldGenAttemptDebugPayload.scale(), worldGenAttemptDebugPayload.red(), worldGenAttemptDebugPayload.green(), worldGenAttemptDebugPayload.blue(), worldGenAttemptDebugPayload.alpha());
        } else if (customPacketPayload instanceof PoiTicketCountDebugPayload) {
            PoiTicketCountDebugPayload poiTicketCountDebugPayload = (PoiTicketCountDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(poiTicketCountDebugPayload.pos(), poiTicketCountDebugPayload.freeTicketCount());
        } else if (customPacketPayload instanceof PoiAddedDebugPayload) {
            PoiAddedDebugPayload poiAddedDebugPayload = (PoiAddedDebugPayload)customPacketPayload;
            BrainDebugRenderer.PoiInfo poiInfo = new BrainDebugRenderer.PoiInfo(poiAddedDebugPayload.pos(), poiAddedDebugPayload.poiType(), poiAddedDebugPayload.freeTicketCount());
            this.minecraft.debugRenderer.brainDebugRenderer.addPoi(poiInfo);
        } else if (customPacketPayload instanceof PoiRemovedDebugPayload) {
            PoiRemovedDebugPayload poiRemovedDebugPayload = (PoiRemovedDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.brainDebugRenderer.removePoi(poiRemovedDebugPayload.pos());
        } else if (customPacketPayload instanceof VillageSectionsDebugPayload) {
            VillageSectionsDebugPayload villageSectionsDebugPayload = (VillageSectionsDebugPayload)customPacketPayload;
            VillageSectionsDebugRenderer villageSectionsDebugRenderer = this.minecraft.debugRenderer.villageSectionsDebugRenderer;
            villageSectionsDebugPayload.villageChunks().forEach(villageSectionsDebugRenderer::setVillageSection);
            villageSectionsDebugPayload.notVillageChunks().forEach(villageSectionsDebugRenderer::setNotVillageSection);
        } else if (customPacketPayload instanceof GoalDebugPayload) {
            GoalDebugPayload goalDebugPayload = (GoalDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(goalDebugPayload.entityId(), goalDebugPayload.pos(), goalDebugPayload.goals());
        } else if (customPacketPayload instanceof BrainDebugPayload) {
            BrainDebugPayload brainDebugPayload = (BrainDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(brainDebugPayload.brainDump());
        } else if (customPacketPayload instanceof BeeDebugPayload) {
            BeeDebugPayload beeDebugPayload = (BeeDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beeDebugPayload.beeInfo());
        } else if (customPacketPayload instanceof HiveDebugPayload) {
            HiveDebugPayload hiveDebugPayload = (HiveDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(hiveDebugPayload.hiveInfo(), this.level.getGameTime());
        } else if (customPacketPayload instanceof GameTestAddMarkerDebugPayload) {
            GameTestAddMarkerDebugPayload gameTestAddMarkerDebugPayload = (GameTestAddMarkerDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(gameTestAddMarkerDebugPayload.pos(), gameTestAddMarkerDebugPayload.color(), gameTestAddMarkerDebugPayload.text(), gameTestAddMarkerDebugPayload.durationMs());
        } else if (customPacketPayload instanceof GameTestClearMarkersDebugPayload) {
            this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
        } else if (customPacketPayload instanceof RaidsDebugPayload) {
            RaidsDebugPayload raidsDebugPayload = (RaidsDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(raidsDebugPayload.raidCenters());
        } else if (customPacketPayload instanceof GameEventDebugPayload) {
            GameEventDebugPayload gameEventDebugPayload = (GameEventDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameEventDebugPayload.gameEventType(), gameEventDebugPayload.pos());
        } else if (customPacketPayload instanceof GameEventListenerDebugPayload) {
            GameEventListenerDebugPayload gameEventListenerDebugPayload = (GameEventListenerDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(gameEventListenerDebugPayload.listenerPos(), gameEventListenerDebugPayload.listenerRange());
        } else if (customPacketPayload instanceof BreezeDebugPayload) {
            BreezeDebugPayload breezeDebugPayload = (BreezeDebugPayload)customPacketPayload;
            this.minecraft.debugRenderer.breezeDebugRenderer.add(breezeDebugPayload.breezeInfo());
        } else {
            this.handleUnknownCustomPayload(customPacketPayload);
        }
    }

    private void handleUnknownCustomPayload(CustomPacketPayload customPacketPayload) {
        LOGGER.warn("Unknown custom packet payload: {}", (Object)customPacketPayload.type().id());
    }

    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetObjectivePacket, this, this.minecraft);
        String string = clientboundSetObjectivePacket.getObjectiveName();
        if (clientboundSetObjectivePacket.getMethod() == 0) {
            this.scoreboard.addObjective(string, ObjectiveCriteria.DUMMY, clientboundSetObjectivePacket.getDisplayName(), clientboundSetObjectivePacket.getRenderType(), false, clientboundSetObjectivePacket.getNumberFormat().orElse(null));
        } else {
            Objective objective = this.scoreboard.getObjective(string);
            if (objective != null) {
                if (clientboundSetObjectivePacket.getMethod() == 1) {
                    this.scoreboard.removeObjective(objective);
                } else if (clientboundSetObjectivePacket.getMethod() == 2) {
                    objective.setRenderType(clientboundSetObjectivePacket.getRenderType());
                    objective.setDisplayName(clientboundSetObjectivePacket.getDisplayName());
                    objective.setNumberFormat(clientboundSetObjectivePacket.getNumberFormat().orElse(null));
                }
            }
        }
    }

    @Override
    public void handleSetScore(ClientboundSetScorePacket clientboundSetScorePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetScorePacket, this, this.minecraft);
        String string = clientboundSetScorePacket.objectiveName();
        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(clientboundSetScorePacket.owner());
        Objective objective = this.scoreboard.getObjective(string);
        if (objective != null) {
            ScoreAccess scoreAccess = this.scoreboard.getOrCreatePlayerScore(scoreHolder, objective, true);
            scoreAccess.set(clientboundSetScorePacket.score());
            scoreAccess.display(clientboundSetScorePacket.display().orElse(null));
            scoreAccess.numberFormatOverride(clientboundSetScorePacket.numberFormat().orElse(null));
        } else {
            LOGGER.warn("Received packet for unknown scoreboard objective: {}", (Object)string);
        }
    }

    @Override
    public void handleResetScore(ClientboundResetScorePacket clientboundResetScorePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundResetScorePacket, this, this.minecraft);
        String string = clientboundResetScorePacket.objectiveName();
        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(clientboundResetScorePacket.owner());
        if (string == null) {
            this.scoreboard.resetAllPlayerScores(scoreHolder);
        } else {
            Objective objective = this.scoreboard.getObjective(string);
            if (objective != null) {
                this.scoreboard.resetSinglePlayerScore(scoreHolder, objective);
            } else {
                LOGGER.warn("Received packet for unknown scoreboard objective: {}", (Object)string);
            }
        }
    }

    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundSetDisplayObjectivePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetDisplayObjectivePacket, this, this.minecraft);
        String string = clientboundSetDisplayObjectivePacket.getObjectiveName();
        Objective objective = string == null ? null : this.scoreboard.getObjective(string);
        this.scoreboard.setDisplayObjective(clientboundSetDisplayObjectivePacket.getSlot(), objective);
    }

    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket) {
        PlayerTeam playerTeam;
        PacketUtils.ensureRunningOnSameThread(clientboundSetPlayerTeamPacket, this, this.minecraft);
        ClientboundSetPlayerTeamPacket.Action action = clientboundSetPlayerTeamPacket.getTeamAction();
        if (action == ClientboundSetPlayerTeamPacket.Action.ADD) {
            playerTeam = this.scoreboard.addPlayerTeam(clientboundSetPlayerTeamPacket.getName());
        } else {
            playerTeam = this.scoreboard.getPlayerTeam(clientboundSetPlayerTeamPacket.getName());
            if (playerTeam == null) {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", new Object[]{clientboundSetPlayerTeamPacket.getName(), clientboundSetPlayerTeamPacket.getTeamAction(), clientboundSetPlayerTeamPacket.getPlayerAction()});
                return;
            }
        }
        Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = clientboundSetPlayerTeamPacket.getParameters();
        optional.ifPresent(parameters -> {
            playerTeam.setDisplayName(parameters.getDisplayName());
            playerTeam.setColor(parameters.getColor());
            playerTeam.unpackOptions(parameters.getOptions());
            playerTeam.setNameTagVisibility(parameters.getNametagVisibility());
            playerTeam.setCollisionRule(parameters.getCollisionRule());
            playerTeam.setPlayerPrefix(parameters.getPlayerPrefix());
            playerTeam.setPlayerSuffix(parameters.getPlayerSuffix());
        });
        ClientboundSetPlayerTeamPacket.Action action2 = clientboundSetPlayerTeamPacket.getPlayerAction();
        if (action2 == ClientboundSetPlayerTeamPacket.Action.ADD) {
            for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
                this.scoreboard.addPlayerToTeam(string, playerTeam);
            }
        } else if (action2 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
                this.scoreboard.removePlayerFromTeam(string, playerTeam);
            }
        }
        if (action == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            this.scoreboard.removePlayerTeam(playerTeam);
        }
    }

    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLevelParticlesPacket, this, this.minecraft);
        if (clientboundLevelParticlesPacket.getCount() == 0) {
            double d = clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getXDist();
            double d2 = clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getYDist();
            double d3 = clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getZDist();
            try {
                this.level.addParticle(clientboundLevelParticlesPacket.getParticle(), clientboundLevelParticlesPacket.isOverrideLimiter(), clientboundLevelParticlesPacket.alwaysShow(), clientboundLevelParticlesPacket.getX(), clientboundLevelParticlesPacket.getY(), clientboundLevelParticlesPacket.getZ(), d, d2, d3);
            }
            catch (Throwable throwable) {
                LOGGER.warn("Could not spawn particle effect {}", (Object)clientboundLevelParticlesPacket.getParticle());
            }
        } else {
            for (int i = 0; i < clientboundLevelParticlesPacket.getCount(); ++i) {
                double d = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getXDist();
                double d4 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getYDist();
                double d5 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getZDist();
                double d6 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
                double d7 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
                double d8 = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
                try {
                    this.level.addParticle(clientboundLevelParticlesPacket.getParticle(), clientboundLevelParticlesPacket.isOverrideLimiter(), clientboundLevelParticlesPacket.alwaysShow(), clientboundLevelParticlesPacket.getX() + d, clientboundLevelParticlesPacket.getY() + d4, clientboundLevelParticlesPacket.getZ() + d5, d6, d7, d8);
                    continue;
                }
                catch (Throwable throwable) {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)clientboundLevelParticlesPacket.getParticle());
                    return;
                }
            }
        }
    }

    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundUpdateAttributesPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundUpdateAttributesPacket.getEntityId());
        if (entity == null) {
            return;
        }
        if (!(entity instanceof LivingEntity)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + String.valueOf(entity) + ")");
        }
        AttributeMap attributeMap = ((LivingEntity)entity).getAttributes();
        for (ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot : clientboundUpdateAttributesPacket.getValues()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(attributeSnapshot.attribute());
            if (attributeInstance == null) {
                LOGGER.warn("Entity {} does not have attribute {}", (Object)entity, (Object)attributeSnapshot.attribute().getRegisteredName());
                continue;
            }
            attributeInstance.setBaseValue(attributeSnapshot.base());
            attributeInstance.removeModifiers();
            for (AttributeModifier attributeModifier : attributeSnapshot.modifiers()) {
                attributeInstance.addTransientModifier(attributeModifier);
            }
        }
    }

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundPlaceGhostRecipePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundPlaceGhostRecipePacket, this, this.minecraft);
        AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
        if (abstractContainerMenu.containerId != clientboundPlaceGhostRecipePacket.containerId()) {
            return;
        }
        Screen screen = this.minecraft.screen;
        if (screen instanceof RecipeUpdateListener) {
            RecipeUpdateListener recipeUpdateListener = (RecipeUpdateListener)((Object)screen);
            recipeUpdateListener.fillGhostRecipe(clientboundPlaceGhostRecipePacket.recipeDisplay());
        }
    }

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundLightUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundLightUpdatePacket, this, this.minecraft);
        int n = clientboundLightUpdatePacket.getX();
        int n2 = clientboundLightUpdatePacket.getZ();
        ClientboundLightUpdatePacketData clientboundLightUpdatePacketData = clientboundLightUpdatePacket.getLightData();
        this.level.queueLightUpdate(() -> this.applyLightData(n, n2, clientboundLightUpdatePacketData, true));
    }

    private void applyLightData(int n, int n2, ClientboundLightUpdatePacketData clientboundLightUpdatePacketData, boolean bl) {
        LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
        BitSet bitSet = clientboundLightUpdatePacketData.getSkyYMask();
        BitSet bitSet2 = clientboundLightUpdatePacketData.getEmptySkyYMask();
        Iterator<byte[]> iterator = clientboundLightUpdatePacketData.getSkyUpdates().iterator();
        this.readSectionList(n, n2, levelLightEngine, LightLayer.SKY, bitSet, bitSet2, iterator, bl);
        BitSet bitSet3 = clientboundLightUpdatePacketData.getBlockYMask();
        BitSet bitSet4 = clientboundLightUpdatePacketData.getEmptyBlockYMask();
        Iterator<byte[]> iterator2 = clientboundLightUpdatePacketData.getBlockUpdates().iterator();
        this.readSectionList(n, n2, levelLightEngine, LightLayer.BLOCK, bitSet3, bitSet4, iterator2, bl);
        levelLightEngine.setLightEnabled(new ChunkPos(n, n2), true);
    }

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundMerchantOffersPacket, this, this.minecraft);
        AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
        if (clientboundMerchantOffersPacket.getContainerId() == abstractContainerMenu.containerId && abstractContainerMenu instanceof MerchantMenu) {
            MerchantMenu merchantMenu = (MerchantMenu)abstractContainerMenu;
            merchantMenu.setOffers(clientboundMerchantOffersPacket.getOffers());
            merchantMenu.setXp(clientboundMerchantOffersPacket.getVillagerXp());
            merchantMenu.setMerchantLevel(clientboundMerchantOffersPacket.getVillagerLevel());
            merchantMenu.setShowProgressBar(clientboundMerchantOffersPacket.showProgress());
            merchantMenu.setCanRestock(clientboundMerchantOffersPacket.canRestock());
        }
    }

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheRadiusPacket, this, this.minecraft);
        this.serverChunkRadius = clientboundSetChunkCacheRadiusPacket.getRadius();
        this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
        this.level.getChunkSource().updateViewRadius(clientboundSetChunkCacheRadiusPacket.getRadius());
    }

    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetSimulationDistancePacket, this, this.minecraft);
        this.serverSimulationDistance = clientboundSetSimulationDistancePacket.simulationDistance();
        this.level.setServerSimulationDistance(this.serverSimulationDistance);
    }

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheCenterPacket, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(clientboundSetChunkCacheCenterPacket.getX(), clientboundSetChunkCacheCenterPacket.getZ());
    }

    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundBlockChangedAckPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBlockChangedAckPacket, this, this.minecraft);
        this.level.handleBlockChangedAck(clientboundBlockChangedAckPacket.sequence());
    }

    @Override
    public void handleBundlePacket(ClientboundBundlePacket clientboundBundlePacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundBundlePacket, this, this.minecraft);
        for (Packet<ClientPacketListener> packet : clientboundBundlePacket.subPackets()) {
            packet.handle(this);
        }
    }

    @Override
    public void handleProjectilePowerPacket(ClientboundProjectilePowerPacket clientboundProjectilePowerPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundProjectilePowerPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(clientboundProjectilePowerPacket.getId());
        if (entity instanceof AbstractHurtingProjectile) {
            AbstractHurtingProjectile abstractHurtingProjectile = (AbstractHurtingProjectile)entity;
            abstractHurtingProjectile.accelerationPower = clientboundProjectilePowerPacket.getAccelerationPower();
        }
    }

    @Override
    public void handleChunkBatchStart(ClientboundChunkBatchStartPacket clientboundChunkBatchStartPacket) {
        this.chunkBatchSizeCalculator.onBatchStart();
    }

    @Override
    public void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket clientboundChunkBatchFinishedPacket) {
        this.chunkBatchSizeCalculator.onBatchFinished(clientboundChunkBatchFinishedPacket.batchSize());
        this.send(new ServerboundChunkBatchReceivedPacket(this.chunkBatchSizeCalculator.getDesiredChunksPerTick()));
    }

    @Override
    public void handleDebugSample(ClientboundDebugSamplePacket clientboundDebugSamplePacket) {
        this.minecraft.getDebugOverlay().logRemoteSample(clientboundDebugSamplePacket.sample(), clientboundDebugSamplePacket.debugSampleType());
    }

    @Override
    public void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket) {
        this.pingDebugMonitor.onPongReceived(clientboundPongResponsePacket);
    }

    @Override
    public void handleTestInstanceBlockStatus(ClientboundTestInstanceBlockStatus clientboundTestInstanceBlockStatus) {
        PacketUtils.ensureRunningOnSameThread(clientboundTestInstanceBlockStatus, this, this.minecraft);
        Screen screen = this.minecraft.screen;
        if (screen instanceof TestInstanceBlockEditScreen) {
            TestInstanceBlockEditScreen testInstanceBlockEditScreen = (TestInstanceBlockEditScreen)screen;
            testInstanceBlockEditScreen.setStatus(clientboundTestInstanceBlockStatus.status(), clientboundTestInstanceBlockStatus.size());
        }
    }

    @Override
    public void handleWaypoint(ClientboundTrackedWaypointPacket clientboundTrackedWaypointPacket) {
        PacketUtils.ensureRunningOnSameThread(clientboundTrackedWaypointPacket, this, this.minecraft);
        clientboundTrackedWaypointPacket.apply(this.waypointManager);
    }

    private void readSectionList(int n, int n2, LevelLightEngine levelLightEngine, LightLayer lightLayer, BitSet bitSet, BitSet bitSet2, Iterator<byte[]> iterator, boolean bl) {
        for (int i = 0; i < levelLightEngine.getLightSectionCount(); ++i) {
            int n3 = levelLightEngine.getMinLightSection() + i;
            boolean bl2 = bitSet.get(i);
            boolean bl3 = bitSet2.get(i);
            if (!bl2 && !bl3) continue;
            levelLightEngine.queueSectionData(lightLayer, SectionPos.of(n, n3, n2), bl2 ? new DataLayer((byte[])iterator.next().clone()) : new DataLayer());
            if (!bl) continue;
            this.level.setSectionDirtyWithNeighbors(n, n3, n2);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected() && !this.closed;
    }

    public Collection<PlayerInfo> getListedOnlinePlayers() {
        return this.listedPlayers;
    }

    public Collection<PlayerInfo> getOnlinePlayers() {
        return this.playerInfoMap.values();
    }

    public Collection<UUID> getOnlinePlayerIds() {
        return this.playerInfoMap.keySet();
    }

    @Nullable
    public PlayerInfo getPlayerInfo(UUID uUID) {
        return this.playerInfoMap.get(uUID);
    }

    @Nullable
    public PlayerInfo getPlayerInfo(String string) {
        for (PlayerInfo playerInfo : this.playerInfoMap.values()) {
            if (!playerInfo.getProfile().getName().equals(string)) continue;
            return playerInfo;
        }
        return null;
    }

    public GameProfile getLocalGameProfile() {
        return this.localGameProfile;
    }

    public ClientAdvancements getAdvancements() {
        return this.advancements;
    }

    public CommandDispatcher<ClientSuggestionProvider> getCommands() {
        return this.commands;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public DebugQueryHandler getDebugQueryHandler() {
        return this.debugQueryHandler;
    }

    public UUID getId() {
        return this.id;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public RegistryAccess.Frozen registryAccess() {
        return this.registryAccess;
    }

    public void markMessageAsProcessed(MessageSignature messageSignature, boolean bl) {
        if (this.lastSeenMessages.addPending(messageSignature, bl) && this.lastSeenMessages.offset() > 64) {
            this.sendChatAcknowledgement();
        }
    }

    private void sendChatAcknowledgement() {
        int n = this.lastSeenMessages.getAndClearOffset();
        if (n > 0) {
            this.send(new ServerboundChatAckPacket(n));
        }
    }

    public void sendChat(String string) {
        Instant instant = Instant.now();
        long l = Crypt.SaltSupplier.getLong();
        LastSeenMessagesTracker.Update update = this.lastSeenMessages.generateAndApplyUpdate();
        MessageSignature messageSignature = this.signedMessageEncoder.pack(new SignedMessageBody(string, instant, l, update.lastSeen()));
        this.send(new ServerboundChatPacket(string, instant, l, messageSignature, update.update()));
    }

    public void sendCommand(String string2) {
        SignableCommand signableCommand = SignableCommand.of(this.commands.parse(string2, (Object)this.suggestionsProvider));
        if (signableCommand.arguments().isEmpty()) {
            this.send(new ServerboundChatCommandPacket(string2));
            return;
        }
        Instant instant = Instant.now();
        long l = Crypt.SaltSupplier.getLong();
        LastSeenMessagesTracker.Update update = this.lastSeenMessages.generateAndApplyUpdate();
        ArgumentSignatures argumentSignatures = ArgumentSignatures.signCommand(signableCommand, string -> {
            SignedMessageBody signedMessageBody = new SignedMessageBody(string, instant, l, update.lastSeen());
            return this.signedMessageEncoder.pack(signedMessageBody);
        });
        this.send(new ServerboundChatCommandSignedPacket(string2, instant, l, argumentSignatures, update.update()));
    }

    public void sendUnattendedCommand(String string, @Nullable Screen screen) {
        switch (this.verifyCommand(string).ordinal()) {
            case 0: {
                this.send(new ServerboundChatCommandPacket(string));
                this.minecraft.setScreen(screen);
                break;
            }
            case 1: {
                this.openCommandSendConfirmationWindow(string, "multiplayer.confirm_command.parse_errors", screen);
                break;
            }
            case 3: {
                this.openCommandSendConfirmationWindow(string, "multiplayer.confirm_command.permissions_required", screen);
                break;
            }
            case 2: {
                LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", (Object)string);
            }
        }
    }

    private CommandCheckResult verifyCommand(String string) {
        ParseResults parseResults = this.commands.parse(string, (Object)this.suggestionsProvider);
        if (!ClientPacketListener.isValidCommand(parseResults)) {
            return CommandCheckResult.PARSE_ERRORS;
        }
        if (SignableCommand.hasSignableArguments(parseResults)) {
            return CommandCheckResult.SIGNATURE_REQUIRED;
        }
        ParseResults parseResults2 = this.commands.parse(string, (Object)this.restrictedSuggestionsProvider);
        if (!ClientPacketListener.isValidCommand(parseResults2)) {
            return CommandCheckResult.PERMISSIONS_REQUIRED;
        }
        return CommandCheckResult.NO_ISSUES;
    }

    private static boolean isValidCommand(ParseResults<?> parseResults) {
        return !parseResults.getReader().canRead() && parseResults.getExceptions().isEmpty() && parseResults.getContext().getLastChild().getCommand() != null;
    }

    private void openCommandSendConfirmationWindow(String string, String string2, @Nullable Screen screen) {
        Screen screen2 = this.minecraft.screen;
        this.minecraft.setScreen(new ConfirmScreen(bl -> {
            if (bl) {
                this.send(new ServerboundChatCommandPacket(string));
            }
            if (bl) {
                this.minecraft.setScreen(screen);
            } else {
                this.minecraft.setScreen(screen2);
            }
        }, COMMAND_SEND_CONFIRM_TITLE, Component.translatable(string2, Component.literal(string).withStyle(ChatFormatting.YELLOW))));
    }

    public void broadcastClientInformation(ClientInformation clientInformation) {
        if (!clientInformation.equals(this.remoteClientInformation)) {
            this.send(new ServerboundClientInformationPacket(clientInformation));
            this.remoteClientInformation = clientInformation;
        }
    }

    @Override
    public void tick() {
        if (this.chatSession != null && this.minecraft.getProfileKeyPairManager().shouldRefreshKeyPair()) {
            this.prepareKeyPair();
        }
        if (this.keyPairFuture != null && this.keyPairFuture.isDone()) {
            this.keyPairFuture.join().ifPresent(this::setKeyPair);
            this.keyPairFuture = null;
        }
        this.sendDeferredPackets();
        if (this.minecraft.getDebugOverlay().showNetworkCharts()) {
            this.pingDebugMonitor.tick();
        }
        this.debugSampleSubscriber.tick();
        this.telemetryManager.tick();
        if (this.levelLoadStatusManager != null) {
            this.levelLoadStatusManager.tick();
            if (this.levelLoadStatusManager.levelReady() && !this.minecraft.player.hasClientLoaded()) {
                this.connection.send(new ServerboundPlayerLoadedPacket());
                this.minecraft.player.setClientLoaded(true);
            }
        }
    }

    public void prepareKeyPair() {
        this.keyPairFuture = this.minecraft.getProfileKeyPairManager().prepareKeyPair();
    }

    private void setKeyPair(ProfileKeyPair profileKeyPair) {
        if (!this.minecraft.isLocalPlayer(this.localGameProfile.getId())) {
            return;
        }
        if (this.chatSession != null && this.chatSession.keyPair().equals(profileKeyPair)) {
            return;
        }
        this.chatSession = LocalChatSession.create(profileKeyPair);
        this.signedMessageEncoder = this.chatSession.createMessageEncoder(this.localGameProfile.getId());
        this.send(new ServerboundChatSessionUpdatePacket(this.chatSession.asRemote().asData()));
    }

    @Override
    protected DialogConnectionAccess createDialogAccess() {
        return new DialogConnectionAccess(){

            @Override
            public void disconnect(Component component) {
                ClientPacketListener.this.getConnection().disconnect(component);
            }

            @Override
            public void runCommand(String string, @Nullable Screen screen) {
                ClientPacketListener.this.sendUnattendedCommand(string, screen);
            }

            @Override
            public void openDialog(Holder<Dialog> holder, @Nullable Screen screen) {
                ClientPacketListener.this.showDialog(holder, this, screen);
            }

            @Override
            public void sendCustomAction(ResourceLocation resourceLocation, Optional<Tag> optional) {
                ClientPacketListener.this.send(new ServerboundCustomClickActionPacket(resourceLocation, optional));
            }

            @Override
            public ServerLinks serverLinks() {
                return ClientPacketListener.this.serverLinks();
            }
        };
    }

    @Nullable
    public ServerData getServerData() {
        return this.serverData;
    }

    public FeatureFlagSet enabledFeatures() {
        return this.enabledFeatures;
    }

    public boolean isFeatureEnabled(FeatureFlagSet featureFlagSet) {
        return featureFlagSet.isSubsetOf(this.enabledFeatures());
    }

    public Scoreboard scoreboard() {
        return this.scoreboard;
    }

    public PotionBrewing potionBrewing() {
        return this.potionBrewing;
    }

    public FuelValues fuelValues() {
        return this.fuelValues;
    }

    public void updateSearchTrees() {
        this.searchTrees.rebuildAfterLanguageChange();
    }

    public SessionSearchTrees searchTrees() {
        return this.searchTrees;
    }

    public void registerForCleaning(CacheSlot<?, ?> cacheSlot) {
        this.cacheSlots.add(new WeakReference(cacheSlot));
    }

    public HashedPatchMap.HashGenerator decoratedHashOpsGenenerator() {
        return this.decoratedHashOpsGenerator;
    }

    public ClientWaypointManager getWaypointManager() {
        return this.waypointManager;
    }

    static enum CommandCheckResult {
        NO_ISSUES,
        PARSE_ERRORS,
        SIGNATURE_REQUIRED,
        PERMISSIONS_REQUIRED;

    }
}

