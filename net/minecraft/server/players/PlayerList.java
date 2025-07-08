/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class PlayerList {
    public static final File USERBANLIST_FILE = new File("banned-players.json");
    public static final File IPBANLIST_FILE = new File("banned-ips.json");
    public static final File OPLIST_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final Component CHAT_FILTERED_FULL = Component.translatable("chat.filtered_full");
    public static final Component DUPLICATE_LOGIN_DISCONNECT_MESSAGE = Component.translatable("multiplayer.disconnect.duplicate_login");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SEND_PLAYER_INFO_INTERVAL = 600;
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<ServerPlayer> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
    private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
    private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
    private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
    private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
    private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
    private final PlayerDataStorage playerIo;
    private boolean doWhiteList;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    protected final int maxPlayers;
    private int viewDistance;
    private int simulationDistance;
    private boolean allowCommandsForAllPlayers;
    private static final boolean ALLOW_LOGOUTIVATOR = false;
    private int sendAllPlayerInfoIn;

    public PlayerList(MinecraftServer minecraftServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage, int n) {
        this.server = minecraftServer;
        this.registries = layeredRegistryAccess;
        this.maxPlayers = n;
        this.playerIo = playerDataStorage;
    }

    public void placeNewPlayer(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie) {
        String string;
        Object object;
        GameProfile gameProfile = serverPlayer.getGameProfile();
        GameProfileCache gameProfileCache = this.server.getProfileCache();
        if (gameProfileCache != null) {
            object = gameProfileCache.get(gameProfile.getId());
            string = ((Optional)object).map(GameProfile::getName).orElse(gameProfile.getName());
            gameProfileCache.add(gameProfile);
        } else {
            string = gameProfile.getName();
        }
        object = new ProblemReporter.ScopedCollector(serverPlayer.problemPath(), LOGGER);
        try {
            ServerLevel serverLevel;
            Optional<ValueInput> optional = this.load(serverPlayer, (ProblemReporter)object);
            ResourceKey<Level> resourceKey = optional.flatMap(valueInput -> valueInput.read("Dimension", Level.RESOURCE_KEY_CODEC)).orElse(Level.OVERWORLD);
            ServerLevel serverLevel2 = this.server.getLevel(resourceKey);
            if (serverLevel2 == null) {
                LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourceKey);
                serverLevel = this.server.overworld();
            } else {
                serverLevel = serverLevel2;
            }
            serverPlayer.setServerLevel(serverLevel);
            if (optional.isEmpty()) {
                serverPlayer.snapTo(serverPlayer.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter(), serverLevel.getSharedSpawnAngle(), 0.0f);
            }
            serverLevel.waitForChunkAndEntities(serverPlayer.chunkPosition(), 1);
            String string2 = connection.getLoggableAddress(this.server.logIPs());
            LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{serverPlayer.getName().getString(), string2, serverPlayer.getId(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()});
            LevelData levelData = serverLevel.getLevelData();
            serverPlayer.loadGameTypes(optional.orElse(null));
            ServerGamePacketListenerImpl serverGamePacketListenerImpl = new ServerGamePacketListenerImpl(this.server, connection, serverPlayer, commonListenerCookie);
            connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess()), serverGamePacketListenerImpl), serverGamePacketListenerImpl);
            GameRules gameRules = serverLevel.getGameRules();
            boolean bl = gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
            boolean bl2 = gameRules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
            boolean bl3 = gameRules.getBoolean(GameRules.RULE_LIMITED_CRAFTING);
            serverGamePacketListenerImpl.send(new ClientboundLoginPacket(serverPlayer.getId(), levelData.isHardcore(), this.server.levelKeys(), this.getMaxPlayers(), this.viewDistance, this.simulationDistance, bl2, !bl, bl3, serverPlayer.createCommonSpawnInfo(serverLevel), this.server.enforceSecureProfile()));
            serverGamePacketListenerImpl.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
            serverGamePacketListenerImpl.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.getAbilities()));
            serverGamePacketListenerImpl.send(new ClientboundSetHeldSlotPacket(serverPlayer.getInventory().getSelectedSlot()));
            RecipeManager recipeManager = this.server.getRecipeManager();
            serverGamePacketListenerImpl.send(new ClientboundUpdateRecipesPacket(recipeManager.getSynchronizedItemProperties(), recipeManager.getSynchronizedStonecutterRecipes()));
            this.sendPlayerPermissionLevel(serverPlayer);
            serverPlayer.getStats().markAllDirty();
            serverPlayer.getRecipeBook().sendInitialRecipeBook(serverPlayer);
            this.updateEntireScoreboard(serverLevel.getScoreboard(), serverPlayer);
            this.server.invalidateStatus();
            MutableComponent mutableComponent = serverPlayer.getGameProfile().getName().equalsIgnoreCase(string) ? Component.translatable("multiplayer.player.joined", serverPlayer.getDisplayName()) : Component.translatable("multiplayer.player.joined.renamed", serverPlayer.getDisplayName(), string);
            this.broadcastSystemMessage(mutableComponent.withStyle(ChatFormatting.YELLOW), false);
            serverGamePacketListenerImpl.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
            ServerStatus serverStatus = this.server.getStatus();
            if (serverStatus != null && !commonListenerCookie.transferred()) {
                serverPlayer.sendServerStatus(serverStatus);
            }
            serverPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(this.players));
            this.players.add(serverPlayer);
            this.playersByUUID.put(serverPlayer.getUUID(), serverPlayer);
            this.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(serverPlayer)));
            this.sendLevelInfo(serverPlayer, serverLevel);
            serverLevel.addNewPlayer(serverPlayer);
            this.server.getCustomBossEvents().onPlayerConnect(serverPlayer);
            this.sendActivePlayerEffects(serverPlayer);
            optional.ifPresent(valueInput -> {
                serverPlayer.loadAndSpawnEnderPearls((ValueInput)valueInput);
                serverPlayer.loadAndSpawnParentVehicle((ValueInput)valueInput);
            });
            serverPlayer.initInventoryMenu();
        }
        finally {
            ((ProblemReporter.ScopedCollector)object).close();
        }
    }

    protected void updateEntireScoreboard(ServerScoreboard serverScoreboard, ServerPlayer serverPlayer) {
        HashSet hashSet = Sets.newHashSet();
        for (PlayerTeam playerTeam : serverScoreboard.getPlayerTeams()) {
            serverPlayer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true));
        }
        for (DisplaySlot displaySlot : DisplaySlot.values()) {
            Objective objective = serverScoreboard.getDisplayObjective(displaySlot);
            if (objective == null || hashSet.contains(objective)) continue;
            List<Packet<?>> list = serverScoreboard.getStartTrackingPackets(objective);
            for (Packet<?> packet : list) {
                serverPlayer.connection.send(packet);
            }
            hashSet.add(objective);
        }
    }

    public void addWorldborderListener(ServerLevel serverLevel) {
        serverLevel.getWorldBorder().addListener(new BorderChangeListener(){

            @Override
            public void onBorderSizeSet(WorldBorder worldBorder, double d) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(worldBorder));
            }

            @Override
            public void onBorderSizeLerping(WorldBorder worldBorder, double d, double d2, long l) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(worldBorder));
            }

            @Override
            public void onBorderCenterSet(WorldBorder worldBorder, double d, double d2) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(worldBorder));
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder worldBorder, int n) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldBorder));
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder worldBorder, int n) {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldBorder));
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d) {
            }

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d) {
            }
        });
    }

    public Optional<ValueInput> load(ServerPlayer serverPlayer, ProblemReporter problemReporter) {
        Optional<ValueInput> optional;
        CompoundTag compoundTag = this.server.getWorldData().getLoadedPlayerTag();
        if (this.server.isSingleplayerOwner(serverPlayer.getGameProfile()) && compoundTag != null) {
            ValueInput valueInput = TagValueInput.create(problemReporter, (HolderLookup.Provider)serverPlayer.registryAccess(), compoundTag);
            optional = Optional.of(valueInput);
            serverPlayer.load(valueInput);
            LOGGER.debug("loading single player");
        } else {
            optional = this.playerIo.load((Player)serverPlayer, problemReporter);
        }
        return optional;
    }

    protected void save(ServerPlayer serverPlayer) {
        PlayerAdvancements playerAdvancements;
        this.playerIo.save(serverPlayer);
        ServerStatsCounter serverStatsCounter = this.stats.get(serverPlayer.getUUID());
        if (serverStatsCounter != null) {
            serverStatsCounter.save();
        }
        if ((playerAdvancements = this.advancements.get(serverPlayer.getUUID())) != null) {
            playerAdvancements.save();
        }
    }

    public void remove(ServerPlayer serverPlayer) {
        Entity entity22;
        Object object;
        ServerLevel serverLevel = serverPlayer.level();
        serverPlayer.awardStat(Stats.LEAVE_GAME);
        this.save(serverPlayer);
        if (serverPlayer.isPassenger() && ((Entity)(object = serverPlayer.getRootVehicle())).hasExactlyOnePlayerPassenger()) {
            LOGGER.debug("Removing player mount");
            serverPlayer.stopRiding();
            ((Entity)object).getPassengersAndSelf().forEach(entity -> entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
        }
        serverPlayer.unRide();
        for (Entity entity22 : serverPlayer.getEnderPearls()) {
            entity22.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        }
        serverLevel.removePlayerImmediately(serverPlayer, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        serverPlayer.getAdvancements().stopListening();
        this.players.remove(serverPlayer);
        this.server.getCustomBossEvents().onPlayerDisconnect(serverPlayer);
        object = serverPlayer.getUUID();
        entity22 = this.playersByUUID.get(object);
        if (entity22 == serverPlayer) {
            this.playersByUUID.remove(object);
            this.stats.remove(object);
            this.advancements.remove(object);
        }
        this.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(serverPlayer.getUUID())));
    }

    @Nullable
    public Component canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile) {
        if (this.bans.isBanned(gameProfile)) {
            UserBanListEntry userBanListEntry = (UserBanListEntry)this.bans.get(gameProfile);
            MutableComponent mutableComponent = Component.translatable("multiplayer.disconnect.banned.reason", userBanListEntry.getReason());
            if (userBanListEntry.getExpires() != null) {
                mutableComponent.append(Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userBanListEntry.getExpires())));
            }
            return mutableComponent;
        }
        if (!this.isWhiteListed(gameProfile)) {
            return Component.translatable("multiplayer.disconnect.not_whitelisted");
        }
        if (this.ipBans.isBanned(socketAddress)) {
            IpBanListEntry ipBanListEntry = this.ipBans.get(socketAddress);
            MutableComponent mutableComponent = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipBanListEntry.getReason());
            if (ipBanListEntry.getExpires() != null) {
                mutableComponent.append(Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipBanListEntry.getExpires())));
            }
            return mutableComponent;
        }
        if (this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameProfile)) {
            return Component.translatable("multiplayer.disconnect.server_full");
        }
        return null;
    }

    public boolean disconnectAllPlayersWithProfile(GameProfile gameProfile) {
        UUID uUID = gameProfile.getId();
        Set set = Sets.newIdentityHashSet();
        for (ServerPlayer object : this.players) {
            if (!object.getUUID().equals(uUID)) continue;
            set.add(object);
        }
        ServerPlayer serverPlayer = this.playersByUUID.get(gameProfile.getId());
        if (serverPlayer != null) {
            set.add(serverPlayer);
        }
        for (ServerPlayer serverPlayer2 : set) {
            serverPlayer2.connection.disconnect(DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
        }
        return !set.isEmpty();
    }

    public ServerPlayer respawn(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason) {
        BlockPos blockPos;
        BlockState blockState;
        ServerLevel serverLevel;
        this.players.remove(serverPlayer);
        serverPlayer.level().removePlayerImmediately(serverPlayer, removalReason);
        TeleportTransition teleportTransition = serverPlayer.findRespawnPositionAndUseSpawnBlock(!bl, TeleportTransition.DO_NOTHING);
        ServerLevel serverLevel2 = teleportTransition.newLevel();
        ServerPlayer serverPlayer2 = new ServerPlayer(this.server, serverLevel2, serverPlayer.getGameProfile(), serverPlayer.clientInformation());
        serverPlayer2.connection = serverPlayer.connection;
        serverPlayer2.restoreFrom(serverPlayer, bl);
        serverPlayer2.setId(serverPlayer.getId());
        serverPlayer2.setMainArm(serverPlayer.getMainArm());
        if (!teleportTransition.missingRespawnBlock()) {
            serverPlayer2.copyRespawnPosition(serverPlayer);
        }
        for (String string : serverPlayer.getTags()) {
            serverPlayer2.addTag(string);
        }
        Vec3 vec3 = teleportTransition.position();
        serverPlayer2.snapTo(vec3.x, vec3.y, vec3.z, teleportTransition.yRot(), teleportTransition.xRot());
        if (teleportTransition.missingRespawnBlock()) {
            serverPlayer2.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0f));
        }
        byte by = bl ? (byte)1 : 0;
        ServerLevel serverLevel3 = serverPlayer2.level();
        LevelData levelData = serverLevel3.getLevelData();
        serverPlayer2.connection.send(new ClientboundRespawnPacket(serverPlayer2.createCommonSpawnInfo(serverLevel3), by));
        serverPlayer2.connection.teleport(serverPlayer2.getX(), serverPlayer2.getY(), serverPlayer2.getZ(), serverPlayer2.getYRot(), serverPlayer2.getXRot());
        serverPlayer2.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverLevel2.getSharedSpawnPos(), serverLevel2.getSharedSpawnAngle()));
        serverPlayer2.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        serverPlayer2.connection.send(new ClientboundSetExperiencePacket(serverPlayer2.experienceProgress, serverPlayer2.totalExperience, serverPlayer2.experienceLevel));
        this.sendActivePlayerEffects(serverPlayer2);
        this.sendLevelInfo(serverPlayer2, serverLevel2);
        this.sendPlayerPermissionLevel(serverPlayer2);
        serverLevel2.addRespawnedPlayer(serverPlayer2);
        this.players.add(serverPlayer2);
        this.playersByUUID.put(serverPlayer2.getUUID(), serverPlayer2);
        serverPlayer2.initInventoryMenu();
        serverPlayer2.setHealth(serverPlayer2.getHealth());
        ServerPlayer.RespawnConfig respawnConfig = serverPlayer2.getRespawnConfig();
        if (!bl && respawnConfig != null && (serverLevel = this.server.getLevel(respawnConfig.dimension())) != null && (blockState = serverLevel.getBlockState(blockPos = respawnConfig.pos())).is(Blocks.RESPAWN_ANCHOR)) {
            serverPlayer2.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0f, 1.0f, serverLevel2.getRandom().nextLong()));
        }
        return serverPlayer2;
    }

    public void sendActivePlayerEffects(ServerPlayer serverPlayer) {
        this.sendActiveEffects(serverPlayer, serverPlayer.connection);
    }

    public void sendActiveEffects(LivingEntity livingEntity, ServerGamePacketListenerImpl serverGamePacketListenerImpl) {
        for (MobEffectInstance mobEffectInstance : livingEntity.getActiveEffects()) {
            serverGamePacketListenerImpl.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), mobEffectInstance, false));
        }
    }

    public void sendPlayerPermissionLevel(ServerPlayer serverPlayer) {
        GameProfile gameProfile = serverPlayer.getGameProfile();
        int n = this.server.getProfilePermissions(gameProfile);
        this.sendPlayerPermissionLevel(serverPlayer, n);
    }

    public void tick() {
        if (++this.sendAllPlayerInfoIn > 600) {
            this.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), this.players));
            this.sendAllPlayerInfoIn = 0;
        }
    }

    public void broadcastAll(Packet<?> packet) {
        for (ServerPlayer serverPlayer : this.players) {
            serverPlayer.connection.send(packet);
        }
    }

    public void broadcastAll(Packet<?> packet, ResourceKey<Level> resourceKey) {
        for (ServerPlayer serverPlayer : this.players) {
            if (serverPlayer.level().dimension() != resourceKey) continue;
            serverPlayer.connection.send(packet);
        }
    }

    public void broadcastSystemToTeam(Player player, Component component) {
        PlayerTeam playerTeam = player.getTeam();
        if (playerTeam == null) {
            return;
        }
        Collection<String> collection = ((Team)playerTeam).getPlayers();
        for (String string : collection) {
            ServerPlayer serverPlayer = this.getPlayerByName(string);
            if (serverPlayer == null || serverPlayer == player) continue;
            serverPlayer.sendSystemMessage(component);
        }
    }

    public void broadcastSystemToAllExceptTeam(Player player, Component component) {
        PlayerTeam playerTeam = player.getTeam();
        if (playerTeam == null) {
            this.broadcastSystemMessage(component, false);
            return;
        }
        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayer serverPlayer = this.players.get(i);
            if (serverPlayer.getTeam() == playerTeam) continue;
            serverPlayer.sendSystemMessage(component);
        }
    }

    public String[] getPlayerNamesArray() {
        String[] stringArray = new String[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i) {
            stringArray[i] = this.players.get(i).getGameProfile().getName();
        }
        return stringArray;
    }

    public UserBanList getBans() {
        return this.bans;
    }

    public IpBanList getIpBans() {
        return this.ipBans;
    }

    public void op(GameProfile gameProfile) {
        this.ops.add(new ServerOpListEntry(gameProfile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(gameProfile)));
        ServerPlayer serverPlayer = this.getPlayer(gameProfile.getId());
        if (serverPlayer != null) {
            this.sendPlayerPermissionLevel(serverPlayer);
        }
    }

    public void deop(GameProfile gameProfile) {
        this.ops.remove(gameProfile);
        ServerPlayer serverPlayer = this.getPlayer(gameProfile.getId());
        if (serverPlayer != null) {
            this.sendPlayerPermissionLevel(serverPlayer);
        }
    }

    private void sendPlayerPermissionLevel(ServerPlayer serverPlayer, int n) {
        if (serverPlayer.connection != null) {
            byte by = n <= 0 ? (byte)24 : (n >= 4 ? (byte)28 : (byte)((byte)(24 + n)));
            serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, by));
        }
        this.server.getCommands().sendCommands(serverPlayer);
    }

    public boolean isWhiteListed(GameProfile gameProfile) {
        return !this.doWhiteList || this.ops.contains(gameProfile) || this.whitelist.contains(gameProfile);
    }

    public boolean isOp(GameProfile gameProfile) {
        return this.ops.contains(gameProfile) || this.server.isSingleplayerOwner(gameProfile) && this.server.getWorldData().isAllowCommands() || this.allowCommandsForAllPlayers;
    }

    @Nullable
    public ServerPlayer getPlayerByName(String string) {
        int n = this.players.size();
        for (int i = 0; i < n; ++i) {
            ServerPlayer serverPlayer = this.players.get(i);
            if (!serverPlayer.getGameProfile().getName().equalsIgnoreCase(string)) continue;
            return serverPlayer;
        }
        return null;
    }

    public void broadcast(@Nullable Player player, double d, double d2, double d3, double d4, ResourceKey<Level> resourceKey, Packet<?> packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            double d5;
            double d6;
            double d7;
            ServerPlayer serverPlayer = this.players.get(i);
            if (serverPlayer == player || serverPlayer.level().dimension() != resourceKey || !((d7 = d - serverPlayer.getX()) * d7 + (d6 = d2 - serverPlayer.getY()) * d6 + (d5 = d3 - serverPlayer.getZ()) * d5 < d4 * d4)) continue;
            serverPlayer.connection.send(packet);
        }
    }

    public void saveAll() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.save(this.players.get(i));
        }
    }

    public UserWhiteList getWhiteList() {
        return this.whitelist;
    }

    public String[] getWhiteListNames() {
        return this.whitelist.getUserList();
    }

    public ServerOpList getOps() {
        return this.ops;
    }

    public String[] getOpNames() {
        return this.ops.getUserList();
    }

    public void reloadWhiteList() {
    }

    public void sendLevelInfo(ServerPlayer serverPlayer, ServerLevel serverLevel) {
        WorldBorder worldBorder = this.server.overworld().getWorldBorder();
        serverPlayer.connection.send(new ClientboundInitializeBorderPacket(worldBorder));
        serverPlayer.connection.send(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
        serverPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverLevel.getSharedSpawnPos(), serverLevel.getSharedSpawnAngle()));
        if (serverLevel.isRaining()) {
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0f));
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, serverLevel.getRainLevel(1.0f)));
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, serverLevel.getThunderLevel(1.0f)));
        }
        serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0f));
        this.server.tickRateManager().updateJoiningPlayer(serverPlayer);
    }

    public void sendAllPlayerInfo(ServerPlayer serverPlayer) {
        serverPlayer.inventoryMenu.sendAllDataToRemote();
        serverPlayer.resetSentInfo();
        serverPlayer.connection.send(new ClientboundSetHeldSlotPacket(serverPlayer.getInventory().getSelectedSlot()));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isUsingWhitelist() {
        return this.doWhiteList;
    }

    public void setUsingWhiteList(boolean bl) {
        this.doWhiteList = bl;
    }

    public List<ServerPlayer> getPlayersWithAddress(String string) {
        ArrayList arrayList = Lists.newArrayList();
        for (ServerPlayer serverPlayer : this.players) {
            if (!serverPlayer.getIpAddress().equals(string)) continue;
            arrayList.add(serverPlayer);
        }
        return arrayList;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    @Nullable
    public CompoundTag getSingleplayerData() {
        return null;
    }

    public void setAllowCommandsForAllPlayers(boolean bl) {
        this.allowCommandsForAllPlayers = bl;
    }

    public void removeAll() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.players.get((int)i).connection.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
        }
    }

    public void broadcastSystemMessage(Component component, boolean bl) {
        this.broadcastSystemMessage(component, serverPlayer -> component, bl);
    }

    public void broadcastSystemMessage(Component component, Function<ServerPlayer, Component> function, boolean bl) {
        this.server.sendSystemMessage(component);
        for (ServerPlayer serverPlayer : this.players) {
            Component component2 = function.apply(serverPlayer);
            if (component2 == null) continue;
            serverPlayer.sendSystemMessage(component2, bl);
        }
    }

    public void broadcastChatMessage(PlayerChatMessage playerChatMessage, CommandSourceStack commandSourceStack, ChatType.Bound bound) {
        this.broadcastChatMessage(playerChatMessage, commandSourceStack::shouldFilterMessageTo, commandSourceStack.getPlayer(), bound);
    }

    public void broadcastChatMessage(PlayerChatMessage playerChatMessage, ServerPlayer serverPlayer, ChatType.Bound bound) {
        this.broadcastChatMessage(playerChatMessage, serverPlayer::shouldFilterMessageTo, serverPlayer, bound);
    }

    private void broadcastChatMessage(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, @Nullable ServerPlayer serverPlayer, ChatType.Bound bound) {
        boolean bl = this.verifyChatTrusted(playerChatMessage);
        this.server.logChatMessage(playerChatMessage.decoratedContent(), bound, bl ? null : "Not Secure");
        OutgoingChatMessage outgoingChatMessage = OutgoingChatMessage.create(playerChatMessage);
        boolean bl2 = false;
        for (ServerPlayer serverPlayer2 : this.players) {
            boolean bl3 = predicate.test(serverPlayer2);
            serverPlayer2.sendChatMessage(outgoingChatMessage, bl3, bound);
            bl2 |= bl3 && playerChatMessage.isFullyFiltered();
        }
        if (bl2 && serverPlayer != null) {
            serverPlayer.sendSystemMessage(CHAT_FILTERED_FULL);
        }
    }

    private boolean verifyChatTrusted(PlayerChatMessage playerChatMessage) {
        return playerChatMessage.hasSignature() && !playerChatMessage.hasExpiredServer(Instant.now());
    }

    public ServerStatsCounter getPlayerStats(Player player) {
        UUID uUID = player.getUUID();
        ServerStatsCounter serverStatsCounter = this.stats.get(uUID);
        if (serverStatsCounter == null) {
            File file;
            Path path;
            File file2 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File file3 = new File(file2, String.valueOf(uUID) + ".json");
            if (!file3.exists() && FileUtil.isPathNormalized(path = (file = new File(file2, player.getName().getString() + ".json")).toPath()) && FileUtil.isPathPortable(path) && path.startsWith(file2.getPath()) && file.isFile()) {
                file.renameTo(file3);
            }
            serverStatsCounter = new ServerStatsCounter(this.server, file3);
            this.stats.put(uUID, serverStatsCounter);
        }
        return serverStatsCounter;
    }

    public PlayerAdvancements getPlayerAdvancements(ServerPlayer serverPlayer) {
        UUID uUID = serverPlayer.getUUID();
        PlayerAdvancements playerAdvancements = this.advancements.get(uUID);
        if (playerAdvancements == null) {
            Path path = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(String.valueOf(uUID) + ".json");
            playerAdvancements = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), path, serverPlayer);
            this.advancements.put(uUID, playerAdvancements);
        }
        playerAdvancements.setPlayer(serverPlayer);
        return playerAdvancements;
    }

    public void setViewDistance(int n) {
        this.viewDistance = n;
        this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(n));
        for (ServerLevel serverLevel : this.server.getAllLevels()) {
            if (serverLevel == null) continue;
            serverLevel.getChunkSource().setViewDistance(n);
        }
    }

    public void setSimulationDistance(int n) {
        this.simulationDistance = n;
        this.broadcastAll(new ClientboundSetSimulationDistancePacket(n));
        for (ServerLevel serverLevel : this.server.getAllLevels()) {
            if (serverLevel == null) continue;
            serverLevel.getChunkSource().setSimulationDistance(n);
        }
    }

    public List<ServerPlayer> getPlayers() {
        return this.players;
    }

    @Nullable
    public ServerPlayer getPlayer(UUID uUID) {
        return this.playersByUUID.get(uUID);
    }

    public boolean canBypassPlayerLimit(GameProfile gameProfile) {
        return false;
    }

    public void reloadResources() {
        for (PlayerAdvancements object2 : this.advancements.values()) {
            object2.reload(this.server.getAdvancements());
        }
        this.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
        RecipeManager recipeManager = this.server.getRecipeManager();
        ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket = new ClientboundUpdateRecipesPacket(recipeManager.getSynchronizedItemProperties(), recipeManager.getSynchronizedStonecutterRecipes());
        for (ServerPlayer serverPlayer : this.players) {
            serverPlayer.connection.send(clientboundUpdateRecipesPacket);
            serverPlayer.getRecipeBook().sendInitialRecipeBook(serverPlayer);
        }
    }

    public boolean isAllowCommandsForAllPlayers() {
        return this.allowCommandsForAllPlayers;
    }
}

