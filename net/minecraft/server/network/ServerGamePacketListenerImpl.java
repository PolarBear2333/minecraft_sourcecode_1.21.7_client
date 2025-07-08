/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.primitives.Floats
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.HashedStack;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTestInstanceBlockStatus;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSampleSubscriptionPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.StringUtil;
import net.minecraft.util.TickThrottler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class ServerGamePacketListenerImpl
extends ServerCommonPacketListenerImpl
implements GameProtocols.Context,
ServerGamePacketListener,
ServerPlayerConnection,
TickablePacketListener {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
    private static final int TRACKED_MESSAGE_DISCONNECT_THRESHOLD = 4096;
    private static final int MAXIMUM_FLYING_TICKS = 80;
    private static final Component CHAT_VALIDATION_FAILED = Component.translatable("multiplayer.disconnect.chat_validation_failed");
    private static final Component INVALID_COMMAND_SIGNATURE = Component.translatable("chat.disabled.invalid_command_signature").withStyle(ChatFormatting.RED);
    private static final int MAX_COMMAND_SUGGESTIONS = 1000;
    public ServerPlayer player;
    public final PlayerChunkSender chunkSender;
    private int tickCount;
    private int ackBlockChangesUpTo = -1;
    private final TickThrottler chatSpamThrottler = new TickThrottler(20, 200);
    private final TickThrottler dropSpamThrottler = new TickThrottler(20, 1480);
    private double firstGoodX;
    private double firstGoodY;
    private double firstGoodZ;
    private double lastGoodX;
    private double lastGoodY;
    private double lastGoodZ;
    @Nullable
    private Entity lastVehicle;
    private double vehicleFirstGoodX;
    private double vehicleFirstGoodY;
    private double vehicleFirstGoodZ;
    private double vehicleLastGoodX;
    private double vehicleLastGoodY;
    private double vehicleLastGoodZ;
    @Nullable
    private Vec3 awaitingPositionFromClient;
    private int awaitingTeleport;
    private int awaitingTeleportTime;
    private boolean clientIsFloating;
    private int aboveGroundTickCount;
    private boolean clientVehicleIsFloating;
    private int aboveGroundVehicleTickCount;
    private int receivedMovePacketCount;
    private int knownMovePacketCount;
    private boolean receivedMovementThisTick;
    @Nullable
    private RemoteChatSession chatSession;
    private SignedMessageChain.Decoder signedMessageDecoder;
    private final LastSeenMessagesValidator lastSeenMessages = new LastSeenMessagesValidator(20);
    private int nextChatIndex;
    private final MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
    private final FutureChain chatMessageChain;
    private boolean waitingForSwitchToConfig;

    public ServerGamePacketListenerImpl(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
        this.chunkSender = new PlayerChunkSender(connection.isMemoryConnection());
        this.player = serverPlayer;
        serverPlayer.connection = this;
        serverPlayer.getTextFilter().join();
        this.signedMessageDecoder = SignedMessageChain.Decoder.unsigned(serverPlayer.getUUID(), minecraftServer::enforceSecureProfile);
        this.chatMessageChain = new FutureChain(minecraftServer);
    }

    @Override
    public void tick() {
        if (this.ackBlockChangesUpTo > -1) {
            this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
            this.ackBlockChangesUpTo = -1;
        }
        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        this.player.doTick();
        this.player.absSnapTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
            if (++this.aboveGroundTickCount > this.getMaximumFlyingTicks(this.player)) {
                LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getName().getString());
                this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                return;
            }
        } else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }
        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle == this.player || this.lastVehicle.getControllingPassenger() != this.player) {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        } else {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating && this.lastVehicle.getControllingPassenger() == this.player) {
                if (++this.aboveGroundVehicleTickCount > this.getMaximumFlyingTicks(this.lastVehicle)) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getName().getString());
                    this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                    return;
                }
            } else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        }
        this.keepConnectionAlive();
        this.chatSpamThrottler.tick();
        this.dropSpamThrottler.tick();
        if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMillis() - this.player.getLastActionTime() > (long)this.server.getPlayerIdleTimeout() * 1000L * 60L) {
            this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
        }
    }

    private int getMaximumFlyingTicks(Entity entity) {
        double d = entity.getGravity();
        if (d < (double)1.0E-5f) {
            return Integer.MAX_VALUE;
        }
        double d2 = 0.08 / d;
        return Mth.ceil(80.0 * Math.max(d2, 1.0));
    }

    public void resetPosition() {
        this.firstGoodX = this.player.getX();
        this.firstGoodY = this.player.getY();
        this.firstGoodZ = this.player.getZ();
        this.lastGoodX = this.player.getX();
        this.lastGoodY = this.player.getY();
        this.lastGoodZ = this.player.getZ();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected() && !this.waitingForSwitchToConfig;
    }

    @Override
    public boolean shouldHandleMessage(Packet<?> packet) {
        if (super.shouldHandleMessage(packet)) {
            return true;
        }
        return this.waitingForSwitchToConfig && this.connection.isConnected() && packet instanceof ServerboundConfigurationAcknowledgedPacket;
    }

    @Override
    protected GameProfile playerProfile() {
        return this.player.getGameProfile();
    }

    private <T, R> CompletableFuture<R> filterTextPacket(T t, BiFunction<TextFilter, T, CompletableFuture<R>> biFunction) {
        return biFunction.apply(this.player.getTextFilter(), (TextFilter)t).thenApply(object -> {
            if (!this.isAcceptingMessages()) {
                LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            }
            return object;
        });
    }

    private CompletableFuture<FilteredText> filterTextPacket(String string) {
        return this.filterTextPacket(string, TextFilter::processStreamMessage);
    }

    private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> list) {
        return this.filterTextPacket(list, TextFilter::processMessageBundle);
    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPlayerInputPacket, this, this.player.level());
        this.player.setLastClientInput(serverboundPlayerInputPacket.input());
        if (this.player.hasClientLoaded()) {
            this.player.resetLastActionTime();
            this.player.setShiftKeyDown(serverboundPlayerInputPacket.input().shift());
        }
    }

    private static boolean containsInvalidValues(double d, double d2, double d3, float f, float f2) {
        return Double.isNaN(d) || Double.isNaN(d2) || Double.isNaN(d3) || !Floats.isFinite((float)f2) || !Floats.isFinite((float)f);
    }

    private static double clampHorizontal(double d) {
        return Mth.clamp(d, -3.0E7, 3.0E7);
    }

    private static double clampVertical(double d) {
        return Mth.clamp(d, -2.0E7, 2.0E7);
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundMoveVehiclePacket, this, this.player.level());
        if (ServerGamePacketListenerImpl.containsInvalidValues(serverboundMoveVehiclePacket.position().x(), serverboundMoveVehiclePacket.position().y(), serverboundMoveVehiclePacket.position().z(), serverboundMoveVehiclePacket.yRot(), serverboundMoveVehiclePacket.xRot())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
            return;
        }
        if (this.updateAwaitingTeleport() || !this.player.hasClientLoaded()) {
            return;
        }
        Entity entity = this.player.getRootVehicle();
        if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
            LivingEntity livingEntity;
            ServerLevel serverLevel = this.player.level();
            double d = entity.getX();
            double d2 = entity.getY();
            double d3 = entity.getZ();
            double d4 = ServerGamePacketListenerImpl.clampHorizontal(serverboundMoveVehiclePacket.position().x());
            double d5 = ServerGamePacketListenerImpl.clampVertical(serverboundMoveVehiclePacket.position().y());
            double d6 = ServerGamePacketListenerImpl.clampHorizontal(serverboundMoveVehiclePacket.position().z());
            float f = Mth.wrapDegrees(serverboundMoveVehiclePacket.yRot());
            float f2 = Mth.wrapDegrees(serverboundMoveVehiclePacket.xRot());
            double d7 = d4 - this.vehicleFirstGoodX;
            double d8 = d5 - this.vehicleFirstGoodY;
            double d9 = d6 - this.vehicleFirstGoodZ;
            double d10 = d7 * d7 + d8 * d8 + d9 * d9;
            double d11 = entity.getDeltaMovement().lengthSqr();
            if (d10 - d11 > 100.0 && !this.isSingleplayerOwner()) {
                LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{entity.getName().getString(), this.player.getName().getString(), d7, d8, d9});
                this.send(ClientboundMoveVehiclePacket.fromEntity(entity));
                return;
            }
            AABB aABB = entity.getBoundingBox();
            d7 = d4 - this.vehicleLastGoodX;
            d8 = d5 - this.vehicleLastGoodY;
            d9 = d6 - this.vehicleLastGoodZ;
            boolean bl = entity.verticalCollisionBelow;
            if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).onClimbable()) {
                livingEntity.resetFallDistance();
            }
            entity.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
            double d12 = d8;
            d7 = d4 - entity.getX();
            d8 = d5 - entity.getY();
            if (d8 > -0.5 || d8 < 0.5) {
                d8 = 0.0;
            }
            d9 = d6 - entity.getZ();
            d10 = d7 * d7 + d8 * d8 + d9 * d9;
            boolean bl2 = false;
            if (d10 > 0.0625) {
                bl2 = true;
                LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", new Object[]{entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10)});
            }
            if (bl2 && serverLevel.noCollision(entity, aABB) || this.isEntityCollidingWithAnythingNew(serverLevel, entity, aABB, d4, d5, d6)) {
                entity.absSnapTo(d, d2, d3, f, f2);
                this.send(ClientboundMoveVehiclePacket.fromEntity(entity));
                entity.removeLatestMovementRecording();
                return;
            }
            entity.absSnapTo(d4, d5, d6, f, f2);
            this.player.level().getChunkSource().move(this.player);
            Vec3 vec3 = new Vec3(entity.getX() - d, entity.getY() - d2, entity.getZ() - d3);
            this.handlePlayerKnownMovement(vec3);
            entity.setOnGroundWithMovement(serverboundMoveVehiclePacket.onGround(), vec3);
            entity.doCheckFallDamage(vec3.x, vec3.y, vec3.z, serverboundMoveVehiclePacket.onGround());
            this.player.checkMovementStatistics(vec3.x, vec3.y, vec3.z);
            this.clientVehicleIsFloating = d12 >= -0.03125 && !bl && !this.server.isFlightAllowed() && !entity.isFlyingVehicle() && !entity.isNoGravity() && this.noBlocksAround(entity);
            this.vehicleLastGoodX = entity.getX();
            this.vehicleLastGoodY = entity.getY();
            this.vehicleLastGoodZ = entity.getZ();
        }
    }

    private boolean noBlocksAround(Entity entity) {
        return entity.level().getBlockStates(entity.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockBehaviour.BlockStateBase::isAir);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundAcceptTeleportationPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundAcceptTeleportationPacket, this, this.player.level());
        if (serverboundAcceptTeleportationPacket.getId() == this.awaitingTeleport) {
            if (this.awaitingPositionFromClient == null) {
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }
            this.player.absSnapTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
            this.lastGoodX = this.awaitingPositionFromClient.x;
            this.lastGoodY = this.awaitingPositionFromClient.y;
            this.lastGoodZ = this.awaitingPositionFromClient.z;
            this.player.hasChangedDimension();
            this.awaitingPositionFromClient = null;
        }
    }

    @Override
    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket serverboundPlayerLoadedPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPlayerLoadedPacket, this, this.player.level());
        this.player.setClientLoaded(true);
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundRecipeBookSeenRecipePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundRecipeBookSeenRecipePacket, this, this.player.level());
        RecipeManager.ServerDisplayInfo serverDisplayInfo = this.server.getRecipeManager().getRecipeFromDisplay(serverboundRecipeBookSeenRecipePacket.recipe());
        if (serverDisplayInfo != null) {
            this.player.getRecipeBook().removeHighlight(serverDisplayInfo.parent().id());
        }
    }

    @Override
    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket serverboundSelectBundleItemPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSelectBundleItemPacket, this, this.player.level());
        this.player.containerMenu.setSelectedBundleItemIndex(serverboundSelectBundleItemPacket.slotId(), serverboundSelectBundleItemPacket.selectedItemIndex());
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundRecipeBookChangeSettingsPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundRecipeBookChangeSettingsPacket, this, this.player.level());
        this.player.getRecipeBook().setBookSetting(serverboundRecipeBookChangeSettingsPacket.getBookType(), serverboundRecipeBookChangeSettingsPacket.isOpen(), serverboundRecipeBookChangeSettingsPacket.isFiltering());
    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundSeenAdvancementsPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSeenAdvancementsPacket, this, this.player.level());
        if (serverboundSeenAdvancementsPacket.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            ResourceLocation resourceLocation = Objects.requireNonNull(serverboundSeenAdvancementsPacket.getTab());
            AdvancementHolder advancementHolder = this.server.getAdvancements().get(resourceLocation);
            if (advancementHolder != null) {
                this.player.getAdvancements().setSelectedTab(advancementHolder);
            }
        }
    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundCommandSuggestionPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundCommandSuggestionPacket, this, this.player.level());
        StringReader stringReader = new StringReader(serverboundCommandSuggestionPacket.getCommand());
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }
        ParseResults parseResults = this.server.getCommands().getDispatcher().parse(stringReader, (Object)this.player.createCommandSourceStack());
        this.server.getCommands().getDispatcher().getCompletionSuggestions(parseResults).thenAccept(suggestions -> {
            Suggestions suggestions2 = suggestions.getList().size() <= 1000 ? suggestions : new Suggestions(suggestions.getRange(), suggestions.getList().subList(0, 1000));
            this.send(new ClientboundCommandSuggestionsPacket(serverboundCommandSuggestionPacket.getId(), suggestions2));
        });
    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundSetCommandBlockPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetCommandBlockPacket, this, this.player.level());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
            return;
        }
        if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
            return;
        }
        BaseCommandBlock baseCommandBlock = null;
        CommandBlockEntity commandBlockEntity = null;
        BlockPos blockPos = serverboundSetCommandBlockPacket.getPos();
        BlockEntity blockEntity = this.player.level().getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity) {
            commandBlockEntity = (CommandBlockEntity)blockEntity;
            baseCommandBlock = commandBlockEntity.getCommandBlock();
        }
        String string = serverboundSetCommandBlockPacket.getCommand();
        boolean bl = serverboundSetCommandBlockPacket.isTrackOutput();
        if (baseCommandBlock != null) {
            CommandBlockEntity.Mode mode = commandBlockEntity.getMode();
            BlockState blockState = this.player.level().getBlockState(blockPos);
            Direction direction = blockState.getValue(CommandBlock.FACING);
            BlockState blockState2 = switch (serverboundSetCommandBlockPacket.getMode()) {
                case CommandBlockEntity.Mode.SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                case CommandBlockEntity.Mode.AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                default -> Blocks.COMMAND_BLOCK.defaultBlockState();
            };
            BlockState blockState3 = (BlockState)((BlockState)blockState2.setValue(CommandBlock.FACING, direction)).setValue(CommandBlock.CONDITIONAL, serverboundSetCommandBlockPacket.isConditional());
            if (blockState3 != blockState) {
                this.player.level().setBlock(blockPos, blockState3, 2);
                blockEntity.setBlockState(blockState3);
                this.player.level().getChunkAt(blockPos).setBlockEntity(blockEntity);
            }
            baseCommandBlock.setCommand(string);
            baseCommandBlock.setTrackOutput(bl);
            if (!bl) {
                baseCommandBlock.setLastOutput(null);
            }
            commandBlockEntity.setAutomatic(serverboundSetCommandBlockPacket.isAutomatic());
            if (mode != serverboundSetCommandBlockPacket.getMode()) {
                commandBlockEntity.onModeSwitch();
            }
            baseCommandBlock.onUpdated();
            if (!StringUtil.isNullOrEmpty(string)) {
                this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", string));
            }
        }
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundSetCommandMinecartPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetCommandMinecartPacket, this, this.player.level());
        if (!this.server.isCommandBlockEnabled()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
            return;
        }
        if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
            return;
        }
        BaseCommandBlock baseCommandBlock = serverboundSetCommandMinecartPacket.getCommandBlock(this.player.level());
        if (baseCommandBlock != null) {
            baseCommandBlock.setCommand(serverboundSetCommandMinecartPacket.getCommand());
            baseCommandBlock.setTrackOutput(serverboundSetCommandMinecartPacket.isTrackOutput());
            if (!serverboundSetCommandMinecartPacket.isTrackOutput()) {
                baseCommandBlock.setLastOutput(null);
            }
            baseCommandBlock.onUpdated();
            this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", serverboundSetCommandMinecartPacket.getCommand()));
        }
    }

    @Override
    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket serverboundPickItemFromBlockPacket) {
        boolean bl;
        ServerLevel serverLevel = this.player.level();
        PacketUtils.ensureRunningOnSameThread(serverboundPickItemFromBlockPacket, this, serverLevel);
        BlockPos blockPos = serverboundPickItemFromBlockPacket.pos();
        if (!this.player.canInteractWithBlock(blockPos, 1.0)) {
            return;
        }
        if (!serverLevel.isLoaded(blockPos)) {
            return;
        }
        BlockState blockState = serverLevel.getBlockState(blockPos);
        ItemStack itemStack = blockState.getCloneItemStack(serverLevel, blockPos, bl = this.player.hasInfiniteMaterials() && serverboundPickItemFromBlockPacket.includeData());
        if (itemStack.isEmpty()) {
            return;
        }
        if (bl) {
            ServerGamePacketListenerImpl.addBlockDataToItem(blockState, serverLevel, blockPos, itemStack);
        }
        this.tryPickItem(itemStack);
    }

    private static void addBlockDataToItem(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        BlockEntity blockEntity;
        BlockEntity blockEntity2 = blockEntity = blockState.hasBlockEntity() ? serverLevel.getBlockEntity(blockPos) : null;
        if (blockEntity != null) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER);){
                TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, serverLevel.registryAccess());
                blockEntity.saveCustomOnly(tagValueOutput);
                blockEntity.removeComponentsFromTag(tagValueOutput);
                BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), tagValueOutput);
                itemStack.applyComponents(blockEntity.collectComponents());
            }
        }
    }

    @Override
    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket serverboundPickItemFromEntityPacket) {
        ServerLevel serverLevel = this.player.level();
        PacketUtils.ensureRunningOnSameThread(serverboundPickItemFromEntityPacket, this, serverLevel);
        Entity entity = serverLevel.getEntityOrPart(serverboundPickItemFromEntityPacket.id());
        if (entity == null || !this.player.canInteractWithEntity(entity, 3.0)) {
            return;
        }
        ItemStack itemStack = entity.getPickResult();
        if (itemStack != null && !itemStack.isEmpty()) {
            this.tryPickItem(itemStack);
        }
    }

    private void tryPickItem(ItemStack itemStack) {
        if (!itemStack.isItemEnabled(this.player.level().enabledFeatures())) {
            return;
        }
        Inventory inventory = this.player.getInventory();
        int n = inventory.findSlotMatchingItem(itemStack);
        if (n != -1) {
            if (Inventory.isHotbarSlot(n)) {
                inventory.setSelectedSlot(n);
            } else {
                inventory.pickSlot(n);
            }
        } else if (this.player.hasInfiniteMaterials()) {
            inventory.addAndPickItem(itemStack);
        }
        this.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
        this.player.inventoryMenu.broadcastChanges();
    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket serverboundRenameItemPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundRenameItemPacket, this, this.player.level());
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof AnvilMenu) {
            AnvilMenu anvilMenu = (AnvilMenu)abstractContainerMenu;
            if (!anvilMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)anvilMenu);
                return;
            }
            anvilMenu.setItemName(serverboundRenameItemPacket.getName());
        }
    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundSetBeaconPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetBeaconPacket, this, this.player.level());
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof BeaconMenu) {
            BeaconMenu beaconMenu = (BeaconMenu)abstractContainerMenu;
            if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
                return;
            }
            beaconMenu.updateEffects(serverboundSetBeaconPacket.primary(), serverboundSetBeaconPacket.secondary());
        }
    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundSetStructureBlockPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetStructureBlockPacket, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos blockPos = serverboundSetStructureBlockPacket.getPos();
        BlockState blockState = this.player.level().getBlockState(blockPos);
        BlockEntity blockEntity = this.player.level().getBlockEntity(blockPos);
        if (blockEntity instanceof StructureBlockEntity) {
            StructureBlockEntity structureBlockEntity = (StructureBlockEntity)blockEntity;
            structureBlockEntity.setMode(serverboundSetStructureBlockPacket.getMode());
            structureBlockEntity.setStructureName(serverboundSetStructureBlockPacket.getName());
            structureBlockEntity.setStructurePos(serverboundSetStructureBlockPacket.getOffset());
            structureBlockEntity.setStructureSize(serverboundSetStructureBlockPacket.getSize());
            structureBlockEntity.setMirror(serverboundSetStructureBlockPacket.getMirror());
            structureBlockEntity.setRotation(serverboundSetStructureBlockPacket.getRotation());
            structureBlockEntity.setMetaData(serverboundSetStructureBlockPacket.getData());
            structureBlockEntity.setIgnoreEntities(serverboundSetStructureBlockPacket.isIgnoreEntities());
            structureBlockEntity.setStrict(serverboundSetStructureBlockPacket.isStrict());
            structureBlockEntity.setShowAir(serverboundSetStructureBlockPacket.isShowAir());
            structureBlockEntity.setShowBoundingBox(serverboundSetStructureBlockPacket.isShowBoundingBox());
            structureBlockEntity.setIntegrity(serverboundSetStructureBlockPacket.getIntegrity());
            structureBlockEntity.setSeed(serverboundSetStructureBlockPacket.getSeed());
            if (structureBlockEntity.hasStructureName()) {
                String string = structureBlockEntity.getStructureName();
                if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
                    if (structureBlockEntity.saveStructure()) {
                        this.player.displayClientMessage(Component.translatable("structure_block.save_success", string), false);
                    } else {
                        this.player.displayClientMessage(Component.translatable("structure_block.save_failure", string), false);
                    }
                } else if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                    if (!structureBlockEntity.isStructureLoadable()) {
                        this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", string), false);
                    } else if (structureBlockEntity.placeStructureIfSameSize(this.player.level())) {
                        this.player.displayClientMessage(Component.translatable("structure_block.load_success", string), false);
                    } else {
                        this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", string), false);
                    }
                } else if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                    if (structureBlockEntity.detectSize()) {
                        this.player.displayClientMessage(Component.translatable("structure_block.size_success", string), false);
                    } else {
                        this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                    }
                }
            } else {
                this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", serverboundSetStructureBlockPacket.getName()), false);
            }
            structureBlockEntity.setChanged();
            this.player.level().sendBlockUpdated(blockPos, blockState, blockState, 3);
        }
    }

    @Override
    public void handleSetTestBlock(ServerboundSetTestBlockPacket serverboundSetTestBlockPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetTestBlockPacket, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos blockPos = serverboundSetTestBlockPacket.position();
        BlockState blockState = this.player.level().getBlockState(blockPos);
        BlockEntity blockEntity = this.player.level().getBlockEntity(blockPos);
        if (blockEntity instanceof TestBlockEntity) {
            TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
            testBlockEntity.setMode(serverboundSetTestBlockPacket.mode());
            testBlockEntity.setMessage(serverboundSetTestBlockPacket.message());
            testBlockEntity.setChanged();
            this.player.level().sendBlockUpdated(blockPos, blockState, testBlockEntity.getBlockState(), 3);
        }
    }

    @Override
    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket serverboundTestInstanceBlockActionPacket) {
        Object object;
        PacketUtils.ensureRunningOnSameThread(serverboundTestInstanceBlockActionPacket, this, this.player.level());
        BlockPos blockPos = serverboundTestInstanceBlockActionPacket.pos();
        if (!this.player.canUseGameMasterBlocks() || !((object = this.player.level().getBlockEntity(blockPos)) instanceof TestInstanceBlockEntity)) {
            return;
        }
        TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)object;
        if (serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.QUERY || serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.INIT) {
            object = this.player.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);
            Optional optional = serverboundTestInstanceBlockActionPacket.data().test().flatMap(((Registry)object)::get);
            Component component = optional.isPresent() ? ((GameTestInstance)((Holder.Reference)optional.get()).value()).describe() : Component.translatable("test_instance.description.no_test").withStyle(ChatFormatting.RED);
            Optional<Object> optional2 = serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.QUERY ? serverboundTestInstanceBlockActionPacket.data().test().flatMap(resourceKey -> TestInstanceBlockEntity.getStructureSize(this.player.level(), resourceKey)) : Optional.empty();
            this.connection.send(new ClientboundTestInstanceBlockStatus(component, optional2));
        } else {
            testInstanceBlockEntity.set(serverboundTestInstanceBlockActionPacket.data());
            if (serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.RESET) {
                testInstanceBlockEntity.resetTest(this.player::sendSystemMessage);
            } else if (serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.SAVE) {
                testInstanceBlockEntity.saveTest(this.player::sendSystemMessage);
            } else if (serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.EXPORT) {
                testInstanceBlockEntity.exportTest(this.player::sendSystemMessage);
            } else if (serverboundTestInstanceBlockActionPacket.action() == ServerboundTestInstanceBlockActionPacket.Action.RUN) {
                testInstanceBlockEntity.runTest(this.player::sendSystemMessage);
            }
            object = this.player.level().getBlockState(blockPos);
            this.player.level().sendBlockUpdated(blockPos, Blocks.AIR.defaultBlockState(), (BlockState)object, 3);
        }
    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundSetJigsawBlockPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetJigsawBlockPacket, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos blockPos = serverboundSetJigsawBlockPacket.getPos();
        BlockState blockState = this.player.level().getBlockState(blockPos);
        BlockEntity blockEntity = this.player.level().getBlockEntity(blockPos);
        if (blockEntity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
            jigsawBlockEntity.setName(serverboundSetJigsawBlockPacket.getName());
            jigsawBlockEntity.setTarget(serverboundSetJigsawBlockPacket.getTarget());
            jigsawBlockEntity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, serverboundSetJigsawBlockPacket.getPool()));
            jigsawBlockEntity.setFinalState(serverboundSetJigsawBlockPacket.getFinalState());
            jigsawBlockEntity.setJoint(serverboundSetJigsawBlockPacket.getJoint());
            jigsawBlockEntity.setPlacementPriority(serverboundSetJigsawBlockPacket.getPlacementPriority());
            jigsawBlockEntity.setSelectionPriority(serverboundSetJigsawBlockPacket.getSelectionPriority());
            jigsawBlockEntity.setChanged();
            this.player.level().sendBlockUpdated(blockPos, blockState, blockState, 3);
        }
    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundJigsawGeneratePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundJigsawGeneratePacket, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos blockPos = serverboundJigsawGeneratePacket.getPos();
        BlockEntity blockEntity = this.player.level().getBlockEntity(blockPos);
        if (blockEntity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
            jigsawBlockEntity.generate(this.player.level(), serverboundJigsawGeneratePacket.levels(), serverboundJigsawGeneratePacket.keepJigsaws());
        }
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket serverboundSelectTradePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSelectTradePacket, this, this.player.level());
        int n = serverboundSelectTradePacket.getItem();
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof MerchantMenu) {
            MerchantMenu merchantMenu = (MerchantMenu)abstractContainerMenu;
            if (!merchantMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)merchantMenu);
                return;
            }
            merchantMenu.setSelectionHint(n);
            merchantMenu.tryMoveItems(n);
        }
    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket serverboundEditBookPacket) {
        int n = serverboundEditBookPacket.slot();
        if (!Inventory.isHotbarSlot(n) && n != 40) {
            return;
        }
        ArrayList arrayList = Lists.newArrayList();
        Optional<String> optional = serverboundEditBookPacket.title();
        optional.ifPresent(arrayList::add);
        arrayList.addAll(serverboundEditBookPacket.pages());
        Consumer<List> consumer = optional.isPresent() ? list -> this.signBook((FilteredText)list.get(0), list.subList(1, list.size()), n) : list -> this.updateBookContents((List<FilteredText>)list, n);
        this.filterTextPacket(arrayList).thenAcceptAsync(consumer, (Executor)this.server);
    }

    private void updateBookContents(List<FilteredText> list, int n) {
        ItemStack itemStack = this.player.getInventory().getItem(n);
        if (!itemStack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            return;
        }
        List<Filterable<String>> list2 = list.stream().map(this::filterableFromOutgoing).toList();
        itemStack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(list2));
    }

    private void signBook(FilteredText filteredText2, List<FilteredText> list, int n) {
        ItemStack itemStack = this.player.getInventory().getItem(n);
        if (!itemStack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            return;
        }
        ItemStack itemStack2 = itemStack.transmuteCopy(Items.WRITTEN_BOOK);
        itemStack2.remove(DataComponents.WRITABLE_BOOK_CONTENT);
        List<Filterable<Component>> list2 = list.stream().map(filteredText -> this.filterableFromOutgoing((FilteredText)filteredText).map(Component::literal)).toList();
        itemStack2.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(this.filterableFromOutgoing(filteredText2), this.player.getName().getString(), 0, list2, true));
        this.player.getInventory().setItem(n, itemStack2);
    }

    private Filterable<String> filterableFromOutgoing(FilteredText filteredText) {
        if (this.player.isTextFilteringEnabled()) {
            return Filterable.passThrough(filteredText.filteredOrEmpty());
        }
        return Filterable.from(filteredText);
    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket serverboundEntityTagQueryPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundEntityTagQueryPacket, this, this.player.level());
        if (!this.player.hasPermissions(2)) {
            return;
        }
        Entity entity = this.player.level().getEntity(serverboundEntityTagQueryPacket.getEntityId());
        if (entity != null) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);){
                TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
                entity.saveWithoutId(tagValueOutput);
                CompoundTag compoundTag = tagValueOutput.buildResult();
                this.send(new ClientboundTagQueryPacket(serverboundEntityTagQueryPacket.getTransactionId(), compoundTag));
            }
        }
    }

    @Override
    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket serverboundContainerSlotStateChangedPacket) {
        CrafterMenu crafterMenu;
        PacketUtils.ensureRunningOnSameThread(serverboundContainerSlotStateChangedPacket, this, this.player.level());
        if (this.player.isSpectator() || serverboundContainerSlotStateChangedPacket.containerId() != this.player.containerMenu.containerId) {
            return;
        }
        Object object = this.player.containerMenu;
        if (object instanceof CrafterMenu && (object = (crafterMenu = (CrafterMenu)object).getContainer()) instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)object;
            crafterBlockEntity.setSlotState(serverboundContainerSlotStateChangedPacket.slotId(), serverboundContainerSlotStateChangedPacket.newState());
        }
    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket serverboundBlockEntityTagQueryPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundBlockEntityTagQueryPacket, this, this.player.level());
        if (!this.player.hasPermissions(2)) {
            return;
        }
        BlockEntity blockEntity = this.player.level().getBlockEntity(serverboundBlockEntityTagQueryPacket.getPos());
        CompoundTag compoundTag = blockEntity != null ? blockEntity.saveWithoutMetadata(this.player.registryAccess()) : null;
        this.send(new ClientboundTagQueryPacket(serverboundBlockEntityTagQueryPacket.getTransactionId(), compoundTag));
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
        boolean bl;
        PacketUtils.ensureRunningOnSameThread(serverboundMovePlayerPacket, this, this.player.level());
        if (ServerGamePacketListenerImpl.containsInvalidValues(serverboundMovePlayerPacket.getX(0.0), serverboundMovePlayerPacket.getY(0.0), serverboundMovePlayerPacket.getZ(0.0), serverboundMovePlayerPacket.getYRot(0.0f), serverboundMovePlayerPacket.getXRot(0.0f))) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
        }
        ServerLevel serverLevel = this.player.level();
        if (this.player.wonGame) {
            return;
        }
        if (this.tickCount == 0) {
            this.resetPosition();
        }
        if (!this.player.hasClientLoaded()) {
            return;
        }
        float f = Mth.wrapDegrees(serverboundMovePlayerPacket.getYRot(this.player.getYRot()));
        float f2 = Mth.wrapDegrees(serverboundMovePlayerPacket.getXRot(this.player.getXRot()));
        if (this.updateAwaitingTeleport()) {
            this.player.absSnapRotationTo(f, f2);
            return;
        }
        double d = ServerGamePacketListenerImpl.clampHorizontal(serverboundMovePlayerPacket.getX(this.player.getX()));
        double d2 = ServerGamePacketListenerImpl.clampVertical(serverboundMovePlayerPacket.getY(this.player.getY()));
        double d3 = ServerGamePacketListenerImpl.clampHorizontal(serverboundMovePlayerPacket.getZ(this.player.getZ()));
        if (this.player.isPassenger()) {
            this.player.absSnapTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f2);
            this.player.level().getChunkSource().move(this.player);
            return;
        }
        double d4 = this.player.getX();
        double d5 = this.player.getY();
        double d6 = this.player.getZ();
        double d7 = d - this.firstGoodX;
        double d8 = d2 - this.firstGoodY;
        double d9 = d3 - this.firstGoodZ;
        double d10 = this.player.getDeltaMovement().lengthSqr();
        double d11 = d7 * d7 + d8 * d8 + d9 * d9;
        if (this.player.isSleeping()) {
            if (d11 > 1.0) {
                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f2);
            }
            return;
        }
        boolean bl2 = this.player.isFallFlying();
        if (serverLevel.tickRateManager().runsNormally()) {
            ++this.receivedMovePacketCount;
            int n = this.receivedMovePacketCount - this.knownMovePacketCount;
            if (n > 5) {
                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", (Object)this.player.getName().getString(), (Object)n);
                n = 1;
            }
            if (this.shouldCheckPlayerMovement(bl2)) {
                float f3;
                float f4 = f3 = bl2 ? 300.0f : 100.0f;
                if (d11 - d10 > (double)(f3 * (float)n)) {
                    LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.player.getName().getString(), d7, d8, d9});
                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                    return;
                }
            }
        }
        AABB aABB = this.player.getBoundingBox();
        d7 = d - this.lastGoodX;
        d8 = d2 - this.lastGoodY;
        d9 = d3 - this.lastGoodZ;
        boolean bl3 = bl = d8 > 0.0;
        if (this.player.onGround() && !serverboundMovePlayerPacket.isOnGround() && bl) {
            this.player.jumpFromGround();
        }
        boolean bl4 = this.player.verticalCollisionBelow;
        this.player.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
        double d12 = d8;
        d7 = d - this.player.getX();
        d8 = d2 - this.player.getY();
        if (d8 > -0.5 || d8 < 0.5) {
            d8 = 0.0;
        }
        d9 = d3 - this.player.getZ();
        d11 = d7 * d7 + d8 * d8 + d9 * d9;
        boolean bl5 = false;
        if (!(this.player.isChangingDimension() || !(d11 > 0.0625) || this.player.isSleeping() || this.player.isCreative() || this.player.isSpectator())) {
            bl5 = true;
            LOGGER.warn("{} moved wrongly!", (Object)this.player.getName().getString());
        }
        if (!this.player.noPhysics && !this.player.isSleeping() && (bl5 && serverLevel.noCollision(this.player, aABB) || this.isEntityCollidingWithAnythingNew(serverLevel, this.player, aABB, d, d2, d3))) {
            this.teleport(d4, d5, d6, f, f2);
            this.player.doCheckFallDamage(this.player.getX() - d4, this.player.getY() - d5, this.player.getZ() - d6, serverboundMovePlayerPacket.isOnGround());
            this.player.removeLatestMovementRecording();
            return;
        }
        this.player.absSnapTo(d, d2, d3, f, f2);
        boolean bl6 = this.player.isAutoSpinAttack();
        this.clientIsFloating = d12 >= -0.03125 && !bl4 && !this.player.isSpectator() && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !bl2 && !bl6 && this.noBlocksAround(this.player);
        this.player.level().getChunkSource().move(this.player);
        Vec3 vec3 = new Vec3(this.player.getX() - d4, this.player.getY() - d5, this.player.getZ() - d6);
        this.player.setOnGroundWithMovement(serverboundMovePlayerPacket.isOnGround(), serverboundMovePlayerPacket.horizontalCollision(), vec3);
        this.player.doCheckFallDamage(vec3.x, vec3.y, vec3.z, serverboundMovePlayerPacket.isOnGround());
        this.handlePlayerKnownMovement(vec3);
        if (bl) {
            this.player.resetFallDistance();
        }
        if (serverboundMovePlayerPacket.isOnGround() || this.player.hasLandedInLiquid() || this.player.onClimbable() || this.player.isSpectator() || bl2 || bl6) {
            this.player.tryResetCurrentImpulseContext();
        }
        this.player.checkMovementStatistics(this.player.getX() - d4, this.player.getY() - d5, this.player.getZ() - d6);
        this.lastGoodX = this.player.getX();
        this.lastGoodY = this.player.getY();
        this.lastGoodZ = this.player.getZ();
    }

    private boolean shouldCheckPlayerMovement(boolean bl) {
        if (this.isSingleplayerOwner()) {
            return false;
        }
        if (this.player.isChangingDimension()) {
            return false;
        }
        GameRules gameRules = this.player.level().getGameRules();
        if (gameRules.getBoolean(GameRules.RULE_DISABLE_PLAYER_MOVEMENT_CHECK)) {
            return false;
        }
        return !bl || !gameRules.getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK);
    }

    private boolean updateAwaitingTeleport() {
        if (this.awaitingPositionFromClient != null) {
            if (this.tickCount - this.awaitingTeleportTime > 20) {
                this.awaitingTeleportTime = this.tickCount;
                this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
            }
            return true;
        }
        this.awaitingTeleportTime = this.tickCount;
        return false;
    }

    private boolean isEntityCollidingWithAnythingNew(LevelReader levelReader, Entity entity, AABB aABB, double d, double d2, double d3) {
        AABB aABB2 = entity.getBoundingBox().move(d - entity.getX(), d2 - entity.getY(), d3 - entity.getZ());
        Iterable<VoxelShape> iterable = levelReader.getPreMoveCollisions(entity, aABB2.deflate(1.0E-5f), aABB.getBottomCenter());
        VoxelShape voxelShape = Shapes.create(aABB.deflate(1.0E-5f));
        for (VoxelShape voxelShape2 : iterable) {
            if (Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND)) continue;
            return true;
        }
        return false;
    }

    public void teleport(double d, double d2, double d3, float f, float f2) {
        this.teleport(new PositionMoveRotation(new Vec3(d, d2, d3), Vec3.ZERO, f, f2), Collections.emptySet());
    }

    public void teleport(PositionMoveRotation positionMoveRotation, Set<Relative> set) {
        this.awaitingTeleportTime = this.tickCount;
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }
        this.player.teleportSetPosition(positionMoveRotation, set);
        this.awaitingPositionFromClient = this.player.position();
        this.send(ClientboundPlayerPositionPacket.of(this.awaitingTeleport, positionMoveRotation, set));
    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPlayerActionPacket, this, this.player.level());
        if (!this.player.hasClientLoaded()) {
            return;
        }
        BlockPos blockPos = serverboundPlayerActionPacket.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action action = serverboundPlayerActionPacket.getAction();
        switch (action) {
            case SWAP_ITEM_WITH_OFFHAND: {
                if (!this.player.isSpectator()) {
                    ItemStack itemStack = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                    this.player.stopUsingItem();
                }
                return;
            }
            case DROP_ITEM: {
                if (!this.player.isSpectator()) {
                    this.player.drop(false);
                }
                return;
            }
            case DROP_ALL_ITEMS: {
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }
                return;
            }
            case RELEASE_USE_ITEM: {
                this.player.releaseUsingItem();
                return;
            }
            case START_DESTROY_BLOCK: 
            case ABORT_DESTROY_BLOCK: 
            case STOP_DESTROY_BLOCK: {
                this.player.gameMode.handleBlockBreakAction(blockPos, action, serverboundPlayerActionPacket.getDirection(), this.player.level().getMaxY(), serverboundPlayerActionPacket.getSequence());
                this.ackBlockChangesUpTo(serverboundPlayerActionPacket.getSequence());
                return;
            }
        }
        throw new IllegalArgumentException("Invalid player action");
    }

    private static boolean wasBlockPlacementAttempt(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        return (item instanceof BlockItem || item instanceof BucketItem) && !serverPlayer.getCooldowns().isOnCooldown(itemStack);
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundUseItemOnPacket, this, this.player.level());
        if (!this.player.hasClientLoaded()) {
            return;
        }
        this.ackBlockChangesUpTo(serverboundUseItemOnPacket.getSequence());
        ServerLevel serverLevel = this.player.level();
        InteractionHand interactionHand = serverboundUseItemOnPacket.getHand();
        ItemStack itemStack = this.player.getItemInHand(interactionHand);
        if (!itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
            return;
        }
        BlockHitResult blockHitResult = serverboundUseItemOnPacket.getHitResult();
        Vec3 vec3 = blockHitResult.getLocation();
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!this.player.canInteractWithBlock(blockPos, 1.0)) {
            return;
        }
        Vec3 vec32 = vec3.subtract(Vec3.atCenterOf(blockPos));
        double d = 1.0000001;
        if (!(Math.abs(vec32.x()) < 1.0000001 && Math.abs(vec32.y()) < 1.0000001 && Math.abs(vec32.z()) < 1.0000001)) {
            LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", new Object[]{this.player.getGameProfile().getName(), vec3, blockPos});
            return;
        }
        Direction direction = blockHitResult.getDirection();
        this.player.resetLastActionTime();
        int n = this.player.level().getMaxY();
        if (blockPos.getY() <= n) {
            if (this.awaitingPositionFromClient == null && serverLevel.mayInteract(this.player, blockPos)) {
                InteractionResult.Success success;
                InteractionResult interactionResult = this.player.gameMode.useItemOn(this.player, serverLevel, itemStack, interactionHand, blockHitResult);
                if (interactionResult.consumesAction()) {
                    CriteriaTriggers.ANY_BLOCK_USE.trigger(this.player, blockHitResult.getBlockPos(), itemStack.copy());
                }
                if (direction == Direction.UP && !interactionResult.consumesAction() && blockPos.getY() >= n && ServerGamePacketListenerImpl.wasBlockPlacementAttempt(this.player, itemStack)) {
                    MutableComponent mutableComponent = Component.translatable("build.tooHigh", n).withStyle(ChatFormatting.RED);
                    this.player.sendSystemMessage(mutableComponent, true);
                } else if (interactionResult instanceof InteractionResult.Success && (success = (InteractionResult.Success)interactionResult).swingSource() == InteractionResult.SwingSource.SERVER) {
                    this.player.swing(interactionHand, true);
                }
            }
        } else {
            MutableComponent mutableComponent = Component.translatable("build.tooHigh", n).withStyle(ChatFormatting.RED);
            this.player.sendSystemMessage(mutableComponent, true);
        }
        this.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos));
        this.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos.relative(direction)));
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket) {
        InteractionResult.Success success;
        InteractionResult interactionResult;
        PacketUtils.ensureRunningOnSameThread(serverboundUseItemPacket, this, this.player.level());
        if (!this.player.hasClientLoaded()) {
            return;
        }
        this.ackBlockChangesUpTo(serverboundUseItemPacket.getSequence());
        ServerLevel serverLevel = this.player.level();
        InteractionHand interactionHand = serverboundUseItemPacket.getHand();
        ItemStack itemStack = this.player.getItemInHand(interactionHand);
        this.player.resetLastActionTime();
        if (itemStack.isEmpty() || !itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
            return;
        }
        float f = Mth.wrapDegrees(serverboundUseItemPacket.getYRot());
        float f2 = Mth.wrapDegrees(serverboundUseItemPacket.getXRot());
        if (f2 != this.player.getXRot() || f != this.player.getYRot()) {
            this.player.absSnapRotationTo(f, f2);
        }
        if ((interactionResult = this.player.gameMode.useItem(this.player, serverLevel, itemStack, interactionHand)) instanceof InteractionResult.Success && (success = (InteractionResult.Success)interactionResult).swingSource() == InteractionResult.SwingSource.SERVER) {
            this.player.swing(interactionHand, true);
        }
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundTeleportToEntityPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundTeleportToEntityPacket, this, this.player.level());
        if (this.player.isSpectator()) {
            for (ServerLevel serverLevel : this.server.getAllLevels()) {
                Entity entity = serverboundTeleportToEntityPacket.getEntity(serverLevel);
                if (entity == null) continue;
                this.player.teleportTo(serverLevel, entity.getX(), entity.getY(), entity.getZ(), Set.of(), entity.getYRot(), entity.getXRot(), true);
                return;
            }
        }
    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundPaddleBoatPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPaddleBoatPacket, this, this.player.level());
        Entity entity = this.player.getControlledVehicle();
        if (entity instanceof AbstractBoat) {
            AbstractBoat abstractBoat = (AbstractBoat)entity;
            abstractBoat.setPaddleState(serverboundPaddleBoatPacket.getLeft(), serverboundPaddleBoatPacket.getRight());
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        LOGGER.info("{} lost connection: {}", (Object)this.player.getName().getString(), (Object)disconnectionDetails.reason().getString());
        this.removePlayerFromWorld();
        super.onDisconnect(disconnectionDetails);
    }

    private void removePlayerFromWorld() {
        this.chatMessageChain.close();
        this.server.invalidateStatus();
        this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        this.player.disconnect();
        this.server.getPlayerList().remove(this.player);
        this.player.getTextFilter().leave();
    }

    public void ackBlockChangesUpTo(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        }
        this.ackBlockChangesUpTo = Math.max(n, this.ackBlockChangesUpTo);
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetCarriedItemPacket, this, this.player.level());
        if (serverboundSetCarriedItemPacket.getSlot() < 0 || serverboundSetCarriedItemPacket.getSlot() >= Inventory.getSelectionSize()) {
            LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getName().getString());
            return;
        }
        if (this.player.getInventory().getSelectedSlot() != serverboundSetCarriedItemPacket.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.player.stopUsingItem();
        }
        this.player.getInventory().setSelectedSlot(serverboundSetCarriedItemPacket.getSlot());
        this.player.resetLastActionTime();
    }

    @Override
    public void handleChat(ServerboundChatPacket serverboundChatPacket) {
        Optional<LastSeenMessages> optional = this.unpackAndApplyLastSeen(serverboundChatPacket.lastSeenMessages());
        if (optional.isEmpty()) {
            return;
        }
        this.tryHandleChat(serverboundChatPacket.message(), () -> {
            PlayerChatMessage playerChatMessage;
            try {
                playerChatMessage = this.getSignedMessage(serverboundChatPacket, (LastSeenMessages)optional.get());
            }
            catch (SignedMessageChain.DecodeException decodeException) {
                this.handleMessageDecodeFailure(decodeException);
                return;
            }
            CompletableFuture<FilteredText> completableFuture = this.filterTextPacket(playerChatMessage.signedContent());
            Component component = this.server.getChatDecorator().decorate(this.player, playerChatMessage.decoratedContent());
            this.chatMessageChain.append(completableFuture, filteredText -> {
                PlayerChatMessage playerChatMessage2 = playerChatMessage.withUnsignedContent(component).filter(filteredText.mask());
                this.broadcastChatMessage(playerChatMessage2);
            });
        });
    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket) {
        this.tryHandleChat(serverboundChatCommandPacket.command(), () -> {
            this.performUnsignedChatCommand(serverboundChatCommandPacket.command());
            this.detectRateSpam();
        });
    }

    private void performUnsignedChatCommand(String string) {
        ParseResults<CommandSourceStack> parseResults = this.parseCommand(string);
        if (this.server.enforceSecureProfile() && SignableCommand.hasSignableArguments(parseResults)) {
            LOGGER.error("Received unsigned command packet from {}, but the command requires signable arguments: {}", (Object)this.player.getGameProfile().getName(), (Object)string);
            this.player.sendSystemMessage(INVALID_COMMAND_SIGNATURE);
            return;
        }
        this.server.getCommands().performCommand(parseResults, string);
    }

    @Override
    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket serverboundChatCommandSignedPacket) {
        Optional<LastSeenMessages> optional = this.unpackAndApplyLastSeen(serverboundChatCommandSignedPacket.lastSeenMessages());
        if (optional.isEmpty()) {
            return;
        }
        this.tryHandleChat(serverboundChatCommandSignedPacket.command(), () -> {
            this.performSignedChatCommand(serverboundChatCommandSignedPacket, (LastSeenMessages)optional.get());
            this.detectRateSpam();
        });
    }

    private void performSignedChatCommand(ServerboundChatCommandSignedPacket serverboundChatCommandSignedPacket, LastSeenMessages lastSeenMessages) {
        Map<String, PlayerChatMessage> map;
        ParseResults<CommandSourceStack> parseResults = this.parseCommand(serverboundChatCommandSignedPacket.command());
        try {
            map = this.collectSignedArguments(serverboundChatCommandSignedPacket, SignableCommand.of(parseResults), lastSeenMessages);
        }
        catch (SignedMessageChain.DecodeException decodeException) {
            this.handleMessageDecodeFailure(decodeException);
            return;
        }
        CommandSigningContext.SignedArguments signedArguments = new CommandSigningContext.SignedArguments(map);
        parseResults = Commands.mapSource(parseResults, commandSourceStack -> commandSourceStack.withSigningContext(signedArguments, this.chatMessageChain));
        this.server.getCommands().performCommand(parseResults, serverboundChatCommandSignedPacket.command());
    }

    private void handleMessageDecodeFailure(SignedMessageChain.DecodeException decodeException) {
        LOGGER.warn("Failed to update secure chat state for {}: '{}'", (Object)this.player.getGameProfile().getName(), (Object)decodeException.getComponent().getString());
        this.player.sendSystemMessage(decodeException.getComponent().copy().withStyle(ChatFormatting.RED));
    }

    private <S> Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandSignedPacket serverboundChatCommandSignedPacket, SignableCommand<S> signableCommand, LastSeenMessages lastSeenMessages) throws SignedMessageChain.DecodeException {
        List<ArgumentSignatures.Entry> list = serverboundChatCommandSignedPacket.argumentSignatures().entries();
        List<SignableCommand.Argument<S>> list2 = signableCommand.arguments();
        if (list.isEmpty()) {
            return this.collectUnsignedArguments(list2);
        }
        Object2ObjectOpenHashMap object2ObjectOpenHashMap = new Object2ObjectOpenHashMap();
        for (ArgumentSignatures.Entry record : list) {
            SignableCommand.Argument<S> argument = signableCommand.getArgument(record.name());
            if (argument == null) {
                this.signedMessageDecoder.setChainBroken();
                throw ServerGamePacketListenerImpl.createSignedArgumentMismatchException(serverboundChatCommandSignedPacket.command(), list, list2);
            }
            SignedMessageBody signedMessageBody = new SignedMessageBody(argument.value(), serverboundChatCommandSignedPacket.timeStamp(), serverboundChatCommandSignedPacket.salt(), lastSeenMessages);
            object2ObjectOpenHashMap.put(argument.name(), this.signedMessageDecoder.unpack(record.signature(), signedMessageBody));
        }
        for (SignableCommand.Argument argument : list2) {
            if (object2ObjectOpenHashMap.containsKey(argument.name())) continue;
            throw ServerGamePacketListenerImpl.createSignedArgumentMismatchException(serverboundChatCommandSignedPacket.command(), list, list2);
        }
        return object2ObjectOpenHashMap;
    }

    private <S> Map<String, PlayerChatMessage> collectUnsignedArguments(List<SignableCommand.Argument<S>> list) throws SignedMessageChain.DecodeException {
        HashMap<String, PlayerChatMessage> hashMap = new HashMap<String, PlayerChatMessage>();
        for (SignableCommand.Argument<S> argument : list) {
            SignedMessageBody signedMessageBody = SignedMessageBody.unsigned(argument.value());
            hashMap.put(argument.name(), this.signedMessageDecoder.unpack(null, signedMessageBody));
        }
        return hashMap;
    }

    private static <S> SignedMessageChain.DecodeException createSignedArgumentMismatchException(String string, List<ArgumentSignatures.Entry> list, List<SignableCommand.Argument<S>> list2) {
        String string2 = list.stream().map(ArgumentSignatures.Entry::name).collect(Collectors.joining(", "));
        String string3 = list2.stream().map(SignableCommand.Argument::name).collect(Collectors.joining(", "));
        LOGGER.error("Signed command mismatch between server and client ('{}'): got [{}] from client, but expected [{}]", new Object[]{string, string2, string3});
        return new SignedMessageChain.DecodeException(INVALID_COMMAND_SIGNATURE);
    }

    private ParseResults<CommandSourceStack> parseCommand(String string) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = this.server.getCommands().getDispatcher();
        return commandDispatcher.parse(string, (Object)this.player.createCommandSourceStack());
    }

    private void tryHandleChat(String string, Runnable runnable) {
        if (ServerGamePacketListenerImpl.isChatMessageIllegal(string)) {
            this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
            return;
        }
        if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
            return;
        }
        this.player.resetLastActionTime();
        this.server.execute(runnable);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.Update update) {
        LastSeenMessagesValidator lastSeenMessagesValidator = this.lastSeenMessages;
        synchronized (lastSeenMessagesValidator) {
            try {
                LastSeenMessages lastSeenMessages = this.lastSeenMessages.applyUpdate(update);
                return Optional.of(lastSeenMessages);
            }
            catch (LastSeenMessagesValidator.ValidationException validationException) {
                LOGGER.error("Failed to validate message acknowledgements from {}: {}", (Object)this.player.getName().getString(), (Object)validationException.getMessage());
                this.disconnect(CHAT_VALIDATION_FAILED);
                return Optional.empty();
            }
        }
    }

    private static boolean isChatMessageIllegal(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (StringUtil.isAllowedChatCharacter(string.charAt(i))) continue;
            return true;
        }
        return false;
    }

    private PlayerChatMessage getSignedMessage(ServerboundChatPacket serverboundChatPacket, LastSeenMessages lastSeenMessages) throws SignedMessageChain.DecodeException {
        SignedMessageBody signedMessageBody = new SignedMessageBody(serverboundChatPacket.message(), serverboundChatPacket.timeStamp(), serverboundChatPacket.salt(), lastSeenMessages);
        return this.signedMessageDecoder.unpack(serverboundChatPacket.signature(), signedMessageBody);
    }

    private void broadcastChatMessage(PlayerChatMessage playerChatMessage) {
        this.server.getPlayerList().broadcastChatMessage(playerChatMessage, this.player, ChatType.bind(ChatType.CHAT, this.player));
        this.detectRateSpam();
    }

    private void detectRateSpam() {
        this.chatSpamThrottler.increment();
        if (!(this.chatSpamThrottler.isUnderThreshold() || this.server.getPlayerList().isOp(this.player.getGameProfile()) || this.server.isSingleplayerOwner(this.player.getGameProfile()))) {
            this.disconnect(Component.translatable("disconnect.spam"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleChatAck(ServerboundChatAckPacket serverboundChatAckPacket) {
        LastSeenMessagesValidator lastSeenMessagesValidator = this.lastSeenMessages;
        synchronized (lastSeenMessagesValidator) {
            try {
                this.lastSeenMessages.applyOffset(serverboundChatAckPacket.offset());
            }
            catch (LastSeenMessagesValidator.ValidationException validationException) {
                LOGGER.error("Failed to validate message acknowledgement offset from {}: {}", (Object)this.player.getName().getString(), (Object)validationException.getMessage());
                this.disconnect(CHAT_VALIDATION_FAILED);
            }
        }
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket serverboundSwingPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSwingPacket, this, this.player.level());
        this.player.resetLastActionTime();
        this.player.swing(serverboundSwingPacket.getHand());
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundPlayerCommandPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPlayerCommandPacket, this, this.player.level());
        if (!this.player.hasClientLoaded()) {
            return;
        }
        this.player.resetLastActionTime();
        switch (serverboundPlayerCommandPacket.getAction()) {
            case START_SPRINTING: {
                this.player.setSprinting(true);
                break;
            }
            case STOP_SPRINTING: {
                this.player.setSprinting(false);
                break;
            }
            case STOP_SLEEPING: {
                if (!this.player.isSleeping()) break;
                this.player.stopSleepInBed(false, true);
                this.awaitingPositionFromClient = this.player.position();
                break;
            }
            case START_RIDING_JUMP: {
                Entity entity = this.player.getControlledVehicle();
                if (!(entity instanceof PlayerRideableJumping)) break;
                PlayerRideableJumping playerRideableJumping = (PlayerRideableJumping)((Object)entity);
                int n = serverboundPlayerCommandPacket.getData();
                if (!playerRideableJumping.canJump() || n <= 0) break;
                playerRideableJumping.handleStartJump(n);
                break;
            }
            case STOP_RIDING_JUMP: {
                Entity entity = this.player.getControlledVehicle();
                if (!(entity instanceof PlayerRideableJumping)) break;
                PlayerRideableJumping playerRideableJumping = (PlayerRideableJumping)((Object)entity);
                playerRideableJumping.handleStopJump();
                break;
            }
            case OPEN_INVENTORY: {
                Entity entity = this.player.getVehicle();
                if (!(entity instanceof HasCustomInventoryScreen)) break;
                HasCustomInventoryScreen hasCustomInventoryScreen = (HasCustomInventoryScreen)((Object)entity);
                hasCustomInventoryScreen.openCustomInventoryScreen(this.player);
                break;
            }
            case START_FALL_FLYING: {
                if (this.player.tryToStartFallFlying()) break;
                this.player.stopFallFlying();
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid client command!");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendPlayerChatMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound) {
        int n;
        this.send(new ClientboundPlayerChatPacket(this.nextChatIndex++, playerChatMessage.link().sender(), playerChatMessage.link().index(), playerChatMessage.signature(), playerChatMessage.signedBody().pack(this.messageSignatureCache), playerChatMessage.unsignedContent(), playerChatMessage.filterMask(), bound));
        MessageSignature messageSignature = playerChatMessage.signature();
        if (messageSignature == null) {
            return;
        }
        this.messageSignatureCache.push(playerChatMessage.signedBody(), playerChatMessage.signature());
        LastSeenMessagesValidator lastSeenMessagesValidator = this.lastSeenMessages;
        synchronized (lastSeenMessagesValidator) {
            this.lastSeenMessages.addPending(messageSignature);
            n = this.lastSeenMessages.trackedMessagesCount();
        }
        if (n > 4096) {
            this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
        }
    }

    public void sendDisguisedChatMessage(Component component, ChatType.Bound bound) {
        this.send(new ClientboundDisguisedChatPacket(component, bound));
    }

    public SocketAddress getRemoteAddress() {
        return this.connection.getRemoteAddress();
    }

    public void switchToConfig() {
        this.waitingForSwitchToConfig = true;
        this.removePlayerFromWorld();
        this.send(ClientboundStartConfigurationPacket.INSTANCE);
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
    }

    @Override
    public void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket) {
        this.connection.send(new ClientboundPongResponsePacket(serverboundPingRequestPacket.getTime()));
    }

    @Override
    public void handleInteract(ServerboundInteractPacket serverboundInteractPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundInteractPacket, this, this.player.level());
        if (!this.player.hasClientLoaded()) {
            return;
        }
        final ServerLevel serverLevel = this.player.level();
        final Entity entity = serverboundInteractPacket.getTarget(serverLevel);
        this.player.resetLastActionTime();
        this.player.setShiftKeyDown(serverboundInteractPacket.isUsingSecondaryAction());
        if (entity != null) {
            if (!serverLevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                return;
            }
            AABB aABB = entity.getBoundingBox();
            if (this.player.canInteractWithEntity(aABB, 3.0)) {
                serverboundInteractPacket.dispatch(new ServerboundInteractPacket.Handler(){

                    private void performInteraction(InteractionHand interactionHand, EntityInteraction entityInteraction) {
                        ItemStack itemStack = ServerGamePacketListenerImpl.this.player.getItemInHand(interactionHand);
                        if (!itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
                            return;
                        }
                        ItemStack itemStack2 = itemStack.copy();
                        InteractionResult interactionResult = entityInteraction.run(ServerGamePacketListenerImpl.this.player, entity, interactionHand);
                        if (interactionResult instanceof InteractionResult.Success) {
                            InteractionResult.Success success = (InteractionResult.Success)interactionResult;
                            ItemStack itemStack3 = success.wasItemInteraction() ? itemStack2 : ItemStack.EMPTY;
                            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, itemStack3, entity);
                            if (success.swingSource() == InteractionResult.SwingSource.SERVER) {
                                ServerGamePacketListenerImpl.this.player.swing(interactionHand, true);
                            }
                        }
                    }

                    @Override
                    public void onInteraction(InteractionHand interactionHand) {
                        this.performInteraction(interactionHand, Player::interactOn);
                    }

                    @Override
                    public void onInteraction(InteractionHand interactionHand2, Vec3 vec3) {
                        this.performInteraction(interactionHand2, (serverPlayer, entity, interactionHand) -> entity.interactAt(serverPlayer, vec3, interactionHand));
                    }

                    @Override
                    public void onAttack() {
                        DataComponentGetter dataComponentGetter;
                        if (entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity == ServerGamePacketListenerImpl.this.player || entity instanceof AbstractArrow && !((AbstractArrow)(dataComponentGetter = (AbstractArrow)entity)).isAttackable()) {
                            ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            LOGGER.warn("Player {} tried to attack an invalid entity", (Object)ServerGamePacketListenerImpl.this.player.getName().getString());
                            return;
                        }
                        dataComponentGetter = ServerGamePacketListenerImpl.this.player.getItemInHand(InteractionHand.MAIN_HAND);
                        if (!((ItemStack)dataComponentGetter).isItemEnabled(serverLevel.enabledFeatures())) {
                            return;
                        }
                        ServerGamePacketListenerImpl.this.player.attack(entity);
                    }
                });
            }
        }
    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket serverboundClientCommandPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundClientCommandPacket, this, this.player.level());
        this.player.resetLastActionTime();
        ServerboundClientCommandPacket.Action action = serverboundClientCommandPacket.getAction();
        switch (action) {
            case PERFORM_RESPAWN: {
                if (this.player.wonGame) {
                    this.player.wonGame = false;
                    this.player = this.server.getPlayerList().respawn(this.player, true, Entity.RemovalReason.CHANGED_DIMENSION);
                    this.resetPosition();
                    CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
                    break;
                }
                if (this.player.getHealth() > 0.0f) {
                    return;
                }
                this.player = this.server.getPlayerList().respawn(this.player, false, Entity.RemovalReason.KILLED);
                this.resetPosition();
                if (!this.server.isHardcore()) break;
                this.player.setGameMode(GameType.SPECTATOR);
                this.player.level().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
                break;
            }
            case REQUEST_STATS: {
                this.player.getStats().sendStats(this.player);
            }
        }
    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundContainerClosePacket, this, this.player.level());
        this.player.doCloseContainer();
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket serverboundContainerClickPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundContainerClickPacket, this, this.player.level());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId != serverboundContainerClickPacket.containerId()) {
            return;
        }
        if (this.player.isSpectator()) {
            this.player.containerMenu.sendAllDataToRemote();
            return;
        }
        if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
            return;
        }
        short s = serverboundContainerClickPacket.slotNum();
        if (!this.player.containerMenu.isValidSlotIndex(s)) {
            LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", new Object[]{this.player.getName(), (int)s, this.player.containerMenu.slots.size()});
            return;
        }
        boolean bl = serverboundContainerClickPacket.stateId() != this.player.containerMenu.getStateId();
        this.player.containerMenu.suppressRemoteUpdates();
        this.player.containerMenu.clicked(s, serverboundContainerClickPacket.buttonNum(), serverboundContainerClickPacket.clickType(), this.player);
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(serverboundContainerClickPacket.changedSlots())) {
            this.player.containerMenu.setRemoteSlotUnsafe(entry.getIntKey(), (HashedStack)entry.getValue());
        }
        this.player.containerMenu.setRemoteCarried(serverboundContainerClickPacket.carriedItem());
        this.player.containerMenu.resumeRemoteUpdates();
        if (bl) {
            this.player.containerMenu.broadcastFullState();
        } else {
            this.player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundPlaceRecipePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPlaceRecipePacket, this, this.player.level());
        this.player.resetLastActionTime();
        if (this.player.isSpectator() || this.player.containerMenu.containerId != serverboundPlaceRecipePacket.containerId()) {
            return;
        }
        if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
            return;
        }
        RecipeManager.ServerDisplayInfo serverDisplayInfo = this.server.getRecipeManager().getRecipeFromDisplay(serverboundPlaceRecipePacket.recipe());
        if (serverDisplayInfo == null) {
            return;
        }
        RecipeHolder<?> recipeHolder = serverDisplayInfo.parent();
        if (!this.player.getRecipeBook().contains(recipeHolder.id())) {
            return;
        }
        Object object = this.player.containerMenu;
        if (object instanceof RecipeBookMenu) {
            RecipeBookMenu recipeBookMenu = (RecipeBookMenu)object;
            if (recipeHolder.value().placementInfo().isImpossibleToPlace()) {
                LOGGER.debug("Player {} tried to place impossible recipe {}", (Object)this.player, (Object)recipeHolder.id().location());
                return;
            }
            object = recipeBookMenu.handlePlacement(serverboundPlaceRecipePacket.useMaxItems(), this.player.isCreative(), recipeHolder, this.player.level(), this.player.getInventory());
            if (object == RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE) {
                this.send(new ClientboundPlaceGhostRecipePacket(this.player.containerMenu.containerId, serverDisplayInfo.display().display()));
            }
        }
    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundContainerButtonClickPacket, this, this.player.level());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId != serverboundContainerButtonClickPacket.containerId() || this.player.isSpectator()) {
            return;
        }
        if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
            return;
        }
        boolean bl = this.player.containerMenu.clickMenuButton(this.player, serverboundContainerButtonClickPacket.buttonId());
        if (bl) {
            this.player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundSetCreativeModeSlotPacket, this, this.player.level());
        if (this.player.hasInfiniteMaterials()) {
            boolean bl;
            boolean bl2 = serverboundSetCreativeModeSlotPacket.slotNum() < 0;
            ItemStack itemStack = serverboundSetCreativeModeSlotPacket.itemStack();
            if (!itemStack.isItemEnabled(this.player.level().enabledFeatures())) {
                return;
            }
            boolean bl3 = serverboundSetCreativeModeSlotPacket.slotNum() >= 1 && serverboundSetCreativeModeSlotPacket.slotNum() <= 45;
            boolean bl4 = bl = itemStack.isEmpty() || itemStack.getCount() <= itemStack.getMaxStackSize();
            if (bl3 && bl) {
                this.player.inventoryMenu.getSlot(serverboundSetCreativeModeSlotPacket.slotNum()).setByPlayer(itemStack);
                this.player.inventoryMenu.setRemoteSlot(serverboundSetCreativeModeSlotPacket.slotNum(), itemStack);
                this.player.inventoryMenu.broadcastChanges();
            } else if (bl2 && bl) {
                if (this.dropSpamThrottler.isUnderThreshold()) {
                    this.dropSpamThrottler.increment();
                    this.player.drop(itemStack, true);
                } else {
                    LOGGER.warn("Player {} was dropping items too fast in creative mode, ignoring.", (Object)this.player.getName().getString());
                }
            }
        }
    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket serverboundSignUpdatePacket) {
        List<String> list2 = Stream.of(serverboundSignUpdatePacket.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
        this.filterTextPacket(list2).thenAcceptAsync(list -> this.updateSignText(serverboundSignUpdatePacket, (List<FilteredText>)list), (Executor)this.server);
    }

    private void updateSignText(ServerboundSignUpdatePacket serverboundSignUpdatePacket, List<FilteredText> list) {
        this.player.resetLastActionTime();
        ServerLevel serverLevel = this.player.level();
        BlockPos blockPos = serverboundSignUpdatePacket.getPos();
        if (serverLevel.hasChunkAt(blockPos)) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            if (!(blockEntity instanceof SignBlockEntity)) {
                return;
            }
            SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
            signBlockEntity.updateSignText(this.player, serverboundSignUpdatePacket.isFrontText(), list);
        }
    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundPlayerAbilitiesPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundPlayerAbilitiesPacket, this, this.player.level());
        this.player.getAbilities().flying = serverboundPlayerAbilitiesPacket.isFlying() && this.player.getAbilities().mayfly;
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundClientInformationPacket, this, this.player.level());
        boolean bl = this.player.isModelPartShown(PlayerModelPart.HAT);
        this.player.updateOptions(serverboundClientInformationPacket.information());
        if (this.player.isModelPartShown(PlayerModelPart.HAT) != bl) {
            this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT, this.player));
        }
    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundChangeDifficultyPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundChangeDifficultyPacket, this, this.player.level());
        if (!this.player.hasPermissions(2) && !this.isSingleplayerOwner()) {
            LOGGER.warn("Player {} tried to change difficulty to {} without required permissions", (Object)this.player.getGameProfile().getName(), (Object)serverboundChangeDifficultyPacket.difficulty().getDisplayName());
            return;
        }
        this.server.setDifficulty(serverboundChangeDifficultyPacket.difficulty(), false);
    }

    @Override
    public void handleChangeGameMode(ServerboundChangeGameModePacket serverboundChangeGameModePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundChangeGameModePacket, this, this.player.level());
        if (!this.player.hasPermissions(2)) {
            LOGGER.warn("Player {} tried to change game mode to {} without required permissions", (Object)this.player.getGameProfile().getName(), (Object)serverboundChangeGameModePacket.mode().getShortDisplayName());
            return;
        }
        GameModeCommand.setGameMode(this.player, serverboundChangeGameModePacket.mode());
    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundLockDifficultyPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundLockDifficultyPacket, this, this.player.level());
        if (!this.player.hasPermissions(2) && !this.isSingleplayerOwner()) {
            return;
        }
        this.server.setDifficultyLocked(serverboundLockDifficultyPacket.isLocked());
    }

    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundChatSessionUpdatePacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundChatSessionUpdatePacket, this, this.player.level());
        RemoteChatSession.Data data = serverboundChatSessionUpdatePacket.chatSession();
        ProfilePublicKey.Data data2 = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
        ProfilePublicKey.Data data3 = data.profilePublicKey();
        if (Objects.equals(data2, data3)) {
            return;
        }
        if (data2 != null && data3.expiresAt().isBefore(data2.expiresAt())) {
            this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
            return;
        }
        try {
            SignatureValidator signatureValidator = this.server.getProfileKeySignatureValidator();
            if (signatureValidator == null) {
                LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)this.player.getGameProfile().getName());
                return;
            }
            this.resetPlayerChatState(data.validate(this.player.getGameProfile(), signatureValidator));
        }
        catch (ProfilePublicKey.ValidationException validationException) {
            LOGGER.error("Failed to validate profile key: {}", (Object)validationException.getMessage());
            this.disconnect(validationException.getComponent());
        }
    }

    @Override
    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundConfigurationAcknowledgedPacket) {
        if (!this.waitingForSwitchToConfig) {
            throw new IllegalStateException("Client acknowledged config, but none was requested");
        }
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, new ServerConfigurationPacketListenerImpl(this.server, this.connection, this.createCookie(this.player.clientInformation())));
    }

    @Override
    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket serverboundChunkBatchReceivedPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundChunkBatchReceivedPacket, this, this.player.level());
        this.chunkSender.onChunkBatchReceivedByClient(serverboundChunkBatchReceivedPacket.desiredChunksPerTick());
    }

    @Override
    public void handleDebugSampleSubscription(ServerboundDebugSampleSubscriptionPacket serverboundDebugSampleSubscriptionPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundDebugSampleSubscriptionPacket, this, this.player.level());
        this.server.subscribeToDebugSample(this.player, serverboundDebugSampleSubscriptionPacket.sampleType());
    }

    private void resetPlayerChatState(RemoteChatSession remoteChatSession) {
        this.chatSession = remoteChatSession;
        this.signedMessageDecoder = remoteChatSession.createMessageDecoder(this.player.getUUID());
        this.chatMessageChain.append(() -> {
            this.player.setChatSession(remoteChatSession);
            this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), List.of(this.player)));
        });
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
    }

    @Override
    public void handleClientTickEnd(ServerboundClientTickEndPacket serverboundClientTickEndPacket) {
        PacketUtils.ensureRunningOnSameThread(serverboundClientTickEndPacket, this, this.player.level());
        if (!this.receivedMovementThisTick) {
            this.player.setKnownMovement(Vec3.ZERO);
        }
        this.receivedMovementThisTick = false;
    }

    private void handlePlayerKnownMovement(Vec3 vec3) {
        if (vec3.lengthSqr() > (double)1.0E-5f) {
            this.player.resetLastActionTime();
        }
        this.player.setKnownMovement(vec3);
        this.receivedMovementThisTick = true;
    }

    @Override
    public boolean hasInfiniteMaterials() {
        return this.player.hasInfiniteMaterials();
    }

    @Override
    public ServerPlayer getPlayer() {
        return this.player;
    }

    @FunctionalInterface
    static interface EntityInteraction {
        public InteractionResult run(ServerPlayer var1, Entity var2, InteractionHand var3);
    }
}

