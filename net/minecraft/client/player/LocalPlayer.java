/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.player;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.MinecartCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.TestBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.TestInstanceBlockEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.BubbleColumnAmbientSoundHandler;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.client.resources.sounds.RidingHappyGhastSoundInstance;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundHandler;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundInstances;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.TickThrottler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class LocalPlayer
extends AbstractClientPlayer {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final int POSITION_REMINDER_INTERVAL = 20;
    private static final int WATER_VISION_MAX_TIME = 600;
    private static final int WATER_VISION_QUICK_TIME = 100;
    private static final float WATER_VISION_QUICK_PERCENT = 0.6f;
    private static final double SUFFOCATING_COLLISION_CHECK_SCALE = 0.35;
    private static final double MINOR_COLLISION_ANGLE_THRESHOLD_RADIAN = 0.13962633907794952;
    public static final float USING_ITEM_SPEED_FACTOR = 0.2f;
    public final ClientPacketListener connection;
    private final StatsCounter stats;
    private final ClientRecipeBook recipeBook;
    private final TickThrottler dropSpamThrottler = new TickThrottler(20, 1280);
    private final List<AmbientSoundHandler> ambientSoundHandlers = Lists.newArrayList();
    private int permissionLevel = 0;
    private double xLast;
    private double yLast;
    private double zLast;
    private float yRotLast;
    private float xRotLast;
    private boolean lastOnGround;
    private boolean lastHorizontalCollision;
    private boolean crouching;
    private boolean wasSprinting;
    private int positionReminder;
    private boolean flashOnSetHealth;
    public ClientInput input = new ClientInput();
    private Input lastSentInput;
    protected final Minecraft minecraft;
    protected int sprintTriggerTime;
    public int experienceDisplayStartTick;
    public float yBob;
    public float xBob;
    public float yBobO;
    public float xBobO;
    private int jumpRidingTicks;
    private float jumpRidingScale;
    public float portalEffectIntensity;
    public float oPortalEffectIntensity;
    private boolean startedUsingItem;
    @Nullable
    private InteractionHand usingItemHand;
    private boolean handsBusy;
    private boolean autoJumpEnabled = true;
    private int autoJumpTime;
    private boolean wasFallFlying;
    private int waterVisionTime;
    private boolean showDeathScreen = true;
    private boolean doLimitedCrafting = false;

    public LocalPlayer(Minecraft minecraft, ClientLevel clientLevel, ClientPacketListener clientPacketListener, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, Input input, boolean bl) {
        super(clientLevel, clientPacketListener.getLocalGameProfile());
        this.minecraft = minecraft;
        this.connection = clientPacketListener;
        this.stats = statsCounter;
        this.recipeBook = clientRecipeBook;
        this.lastSentInput = input;
        this.wasSprinting = bl;
        this.ambientSoundHandlers.add(new UnderwaterAmbientSoundHandler(this, minecraft.getSoundManager()));
        this.ambientSoundHandlers.add(new BubbleColumnAmbientSoundHandler(this));
        this.ambientSoundHandlers.add(new BiomeAmbientSoundsHandler(this, minecraft.getSoundManager(), clientLevel.getBiomeManager()));
    }

    @Override
    public void heal(float f) {
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl) {
        if (!super.startRiding(entity, bl)) {
            return false;
        }
        if (entity instanceof AbstractMinecart) {
            this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)entity, true));
            this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)entity, false));
        } else if (entity instanceof HappyGhast) {
            this.minecraft.getSoundManager().play(new RidingHappyGhastSoundInstance(this, (HappyGhast)entity));
        }
        return true;
    }

    @Override
    public void removeVehicle() {
        super.removeVehicle();
        this.handsBusy = false;
    }

    @Override
    public float getViewXRot(float f) {
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float f) {
        if (this.isPassenger()) {
            return super.getViewYRot(f);
        }
        return this.getYRot();
    }

    @Override
    public void tick() {
        this.tickClientLoadTimeout();
        if (!this.hasClientLoaded()) {
            return;
        }
        this.dropSpamThrottler.tick();
        super.tick();
        if (!this.lastSentInput.equals(this.input.keyPresses)) {
            this.connection.send(new ServerboundPlayerInputPacket(this.input.keyPresses));
            this.lastSentInput = this.input.keyPresses;
        }
        if (this.isPassenger()) {
            this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround(), this.horizontalCollision));
            Entity entity = this.getRootVehicle();
            if (entity != this && entity.isLocalInstanceAuthoritative()) {
                this.connection.send(ServerboundMoveVehiclePacket.fromEntity(entity));
                this.sendIsSprintingIfNeeded();
            }
        } else {
            this.sendPosition();
        }
        for (AmbientSoundHandler ambientSoundHandler : this.ambientSoundHandlers) {
            ambientSoundHandler.tick();
        }
    }

    public float getCurrentMood() {
        for (AmbientSoundHandler ambientSoundHandler : this.ambientSoundHandlers) {
            if (!(ambientSoundHandler instanceof BiomeAmbientSoundsHandler)) continue;
            return ((BiomeAmbientSoundsHandler)ambientSoundHandler).getMoodiness();
        }
        return 0.0f;
    }

    private void sendPosition() {
        this.sendIsSprintingIfNeeded();
        if (this.isControlledCamera()) {
            boolean bl;
            double d = this.getX() - this.xLast;
            double d2 = this.getY() - this.yLast;
            double d3 = this.getZ() - this.zLast;
            double d4 = this.getYRot() - this.yRotLast;
            double d5 = this.getXRot() - this.xRotLast;
            ++this.positionReminder;
            boolean bl2 = Mth.lengthSquared(d, d2, d3) > Mth.square(2.0E-4) || this.positionReminder >= 20;
            boolean bl3 = bl = d4 != 0.0 || d5 != 0.0;
            if (bl2 && bl) {
                this.connection.send(new ServerboundMovePlayerPacket.PosRot(this.position(), this.getYRot(), this.getXRot(), this.onGround(), this.horizontalCollision));
            } else if (bl2) {
                this.connection.send(new ServerboundMovePlayerPacket.Pos(this.position(), this.onGround(), this.horizontalCollision));
            } else if (bl) {
                this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround(), this.horizontalCollision));
            } else if (this.lastOnGround != this.onGround() || this.lastHorizontalCollision != this.horizontalCollision) {
                this.connection.send(new ServerboundMovePlayerPacket.StatusOnly(this.onGround(), this.horizontalCollision));
            }
            if (bl2) {
                this.xLast = this.getX();
                this.yLast = this.getY();
                this.zLast = this.getZ();
                this.positionReminder = 0;
            }
            if (bl) {
                this.yRotLast = this.getYRot();
                this.xRotLast = this.getXRot();
            }
            this.lastOnGround = this.onGround();
            this.lastHorizontalCollision = this.horizontalCollision;
            this.autoJumpEnabled = this.minecraft.options.autoJump().get();
        }
    }

    private void sendIsSprintingIfNeeded() {
        boolean bl = this.isSprinting();
        if (bl != this.wasSprinting) {
            ServerboundPlayerCommandPacket.Action action = bl ? ServerboundPlayerCommandPacket.Action.START_SPRINTING : ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
            this.connection.send(new ServerboundPlayerCommandPacket(this, action));
            this.wasSprinting = bl;
        }
    }

    public boolean drop(boolean bl) {
        ServerboundPlayerActionPacket.Action action = bl ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS : ServerboundPlayerActionPacket.Action.DROP_ITEM;
        ItemStack itemStack = this.getInventory().removeFromSelected(bl);
        this.connection.send(new ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN));
        return !itemStack.isEmpty();
    }

    @Override
    public void swing(InteractionHand interactionHand) {
        super.swing(interactionHand);
        this.connection.send(new ServerboundSwingPacket(interactionHand));
    }

    @Override
    public void respawn() {
        this.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
        KeyMapping.resetToggleKeys();
    }

    @Override
    public void closeContainer() {
        this.connection.send(new ServerboundContainerClosePacket(this.containerMenu.containerId));
        this.clientSideCloseContainer();
    }

    public void clientSideCloseContainer() {
        super.closeContainer();
        this.minecraft.setScreen(null);
    }

    public void hurtTo(float f) {
        if (this.flashOnSetHealth) {
            float f2 = this.getHealth() - f;
            if (f2 <= 0.0f) {
                this.setHealth(f);
                if (f2 < 0.0f) {
                    this.invulnerableTime = 10;
                }
            } else {
                this.lastHurt = f2;
                this.invulnerableTime = 20;
                this.setHealth(f);
                this.hurtTime = this.hurtDuration = 10;
            }
        } else {
            this.setHealth(f);
            this.flashOnSetHealth = true;
        }
    }

    @Override
    public void onUpdateAbilities() {
        this.connection.send(new ServerboundPlayerAbilitiesPacket(this.getAbilities()));
    }

    @Override
    public boolean isLocalPlayer() {
        return true;
    }

    @Override
    public boolean isSuppressingSlidingDownLadder() {
        return !this.getAbilities().flying && super.isSuppressingSlidingDownLadder();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return !this.getAbilities().flying && super.canSpawnSprintParticle();
    }

    protected void sendRidingJump() {
        this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP, Mth.floor(this.getJumpRidingScale() * 100.0f)));
    }

    public void sendOpenInventory() {
        this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
    }

    public StatsCounter getStats() {
        return this.stats;
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void removeRecipeHighlight(RecipeDisplayId recipeDisplayId) {
        if (this.recipeBook.willHighlight(recipeDisplayId)) {
            this.recipeBook.removeHighlight(recipeDisplayId);
            this.connection.send(new ServerboundRecipeBookSeenRecipePacket(recipeDisplayId));
        }
    }

    @Override
    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    public void setPermissionLevel(int n) {
        this.permissionLevel = n;
    }

    @Override
    public void displayClientMessage(Component component, boolean bl) {
        this.minecraft.getChatListener().handleSystemMessage(component, bl);
    }

    private void moveTowardsClosestSpace(double d, double d2) {
        Direction[] directionArray;
        BlockPos blockPos = BlockPos.containing(d, this.getY(), d2);
        if (!this.suffocatesAt(blockPos)) {
            return;
        }
        double d3 = d - (double)blockPos.getX();
        double d4 = d2 - (double)blockPos.getZ();
        Direction direction = null;
        double d5 = Double.MAX_VALUE;
        for (Direction direction2 : directionArray = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
            double d6;
            double d7 = direction2.getAxis().choose(d3, 0.0, d4);
            double d8 = d6 = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - d7 : d7;
            if (!(d6 < d5) || this.suffocatesAt(blockPos.relative(direction2))) continue;
            d5 = d6;
            direction = direction2;
        }
        if (direction != null) {
            Vec3 vec3 = this.getDeltaMovement();
            if (direction.getAxis() == Direction.Axis.X) {
                this.setDeltaMovement(0.1 * (double)direction.getStepX(), vec3.y, vec3.z);
            } else {
                this.setDeltaMovement(vec3.x, vec3.y, 0.1 * (double)direction.getStepZ());
            }
        }
    }

    private boolean suffocatesAt(BlockPos blockPos) {
        AABB aABB = this.getBoundingBox();
        AABB aABB2 = new AABB(blockPos.getX(), aABB.minY, blockPos.getZ(), (double)blockPos.getX() + 1.0, aABB.maxY, (double)blockPos.getZ() + 1.0).deflate(1.0E-7);
        return this.level().collidesWithSuffocatingBlock(this, aABB2);
    }

    public void setExperienceValues(float f, int n, int n2) {
        this.experienceProgress = f;
        this.totalExperience = n;
        this.experienceLevel = n2;
        this.experienceDisplayStartTick = this.tickCount;
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by >= 24 && by <= 28) {
            this.setPermissionLevel(by - 24);
        } else {
            super.handleEntityEvent(by);
        }
    }

    public void setShowDeathScreen(boolean bl) {
        this.showDeathScreen = bl;
    }

    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    public void setDoLimitedCrafting(boolean bl) {
        this.doLimitedCrafting = bl;
    }

    public boolean getDoLimitedCrafting() {
        return this.doLimitedCrafting;
    }

    @Override
    public void playSound(SoundEvent soundEvent, float f, float f2) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, f2, false);
    }

    @Override
    public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float f2) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundEvent, soundSource, f, f2, false);
    }

    @Override
    public void startUsingItem(InteractionHand interactionHand) {
        ItemStack itemStack = this.getItemInHand(interactionHand);
        if (itemStack.isEmpty() || this.isUsingItem()) {
            return;
        }
        super.startUsingItem(interactionHand);
        this.startedUsingItem = true;
        this.usingItemHand = interactionHand;
    }

    @Override
    public boolean isUsingItem() {
        return this.startedUsingItem;
    }

    @Override
    public void stopUsingItem() {
        super.stopUsingItem();
        this.startedUsingItem = false;
    }

    @Override
    public InteractionHand getUsedItemHand() {
        return Objects.requireNonNullElse(this.usingItemHand, InteractionHand.MAIN_HAND);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_LIVING_ENTITY_FLAGS.equals(entityDataAccessor)) {
            InteractionHand interactionHand;
            boolean bl = ((Byte)this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
            InteractionHand interactionHand2 = interactionHand = ((Byte)this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            if (bl && !this.startedUsingItem) {
                this.startUsingItem(interactionHand);
            } else if (!bl && this.startedUsingItem) {
                this.stopUsingItem();
            }
        }
        if (DATA_SHARED_FLAGS_ID.equals(entityDataAccessor) && this.isFallFlying() && !this.wasFallFlying) {
            this.minecraft.getSoundManager().play(new ElytraOnPlayerSoundInstance(this));
        }
    }

    @Nullable
    public PlayerRideableJumping jumpableVehicle() {
        PlayerRideableJumping playerRideableJumping;
        Entity entity = this.getControlledVehicle();
        return entity instanceof PlayerRideableJumping && (playerRideableJumping = (PlayerRideableJumping)((Object)entity)).canJump() ? playerRideableJumping : null;
    }

    public float getJumpRidingScale() {
        return this.jumpRidingScale;
    }

    @Override
    public boolean isTextFilteringEnabled() {
        return this.minecraft.isTextFilteringEnabled();
    }

    @Override
    public void openTextEdit(SignBlockEntity signBlockEntity, boolean bl) {
        if (signBlockEntity instanceof HangingSignBlockEntity) {
            HangingSignBlockEntity hangingSignBlockEntity = (HangingSignBlockEntity)signBlockEntity;
            this.minecraft.setScreen(new HangingSignEditScreen(hangingSignBlockEntity, bl, this.minecraft.isTextFilteringEnabled()));
        } else {
            this.minecraft.setScreen(new SignEditScreen(signBlockEntity, bl, this.minecraft.isTextFilteringEnabled()));
        }
    }

    @Override
    public void openMinecartCommandBlock(BaseCommandBlock baseCommandBlock) {
        this.minecraft.setScreen(new MinecartCommandBlockEditScreen(baseCommandBlock));
    }

    @Override
    public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
        this.minecraft.setScreen(new CommandBlockEditScreen(commandBlockEntity));
    }

    @Override
    public void openStructureBlock(StructureBlockEntity structureBlockEntity) {
        this.minecraft.setScreen(new StructureBlockEditScreen(structureBlockEntity));
    }

    @Override
    public void openTestBlock(TestBlockEntity testBlockEntity) {
        this.minecraft.setScreen(new TestBlockEditScreen(testBlockEntity));
    }

    @Override
    public void openTestInstanceBlock(TestInstanceBlockEntity testInstanceBlockEntity) {
        this.minecraft.setScreen(new TestInstanceBlockEditScreen(testInstanceBlockEntity));
    }

    @Override
    public void openJigsawBlock(JigsawBlockEntity jigsawBlockEntity) {
        this.minecraft.setScreen(new JigsawBlockEditScreen(jigsawBlockEntity));
    }

    @Override
    public void openDialog(Holder<Dialog> holder) {
        this.connection.showDialog(holder, this.minecraft.screen);
    }

    @Override
    public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
        WritableBookContent writableBookContent = itemStack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (writableBookContent != null) {
            this.minecraft.setScreen(new BookEditScreen(this, itemStack, interactionHand, writableBookContent));
        }
    }

    @Override
    public void crit(Entity entity) {
        this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
    }

    @Override
    public void magicCrit(Entity entity) {
        this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isShiftKeyDown() {
        return this.input.keyPresses.shift();
    }

    @Override
    public boolean isCrouching() {
        return this.crouching;
    }

    public boolean isMovingSlowly() {
        return this.isCrouching() || this.isVisuallyCrawling();
    }

    @Override
    public void applyInput() {
        if (this.isControlledCamera()) {
            Vec2 vec2 = this.modifyInput(this.input.getMoveVector());
            this.xxa = vec2.x;
            this.zza = vec2.y;
            this.jumping = this.input.keyPresses.jump();
            this.yBobO = this.yBob;
            this.xBobO = this.xBob;
            this.xBob += (this.getXRot() - this.xBob) * 0.5f;
            this.yBob += (this.getYRot() - this.yBob) * 0.5f;
        } else {
            super.applyInput();
        }
    }

    private Vec2 modifyInput(Vec2 vec2) {
        if (vec2.lengthSquared() == 0.0f) {
            return vec2;
        }
        Vec2 vec22 = vec2.scale(0.98f);
        if (this.isUsingItem() && !this.isPassenger()) {
            vec22 = vec22.scale(0.2f);
        }
        if (this.isMovingSlowly()) {
            float f = (float)this.getAttributeValue(Attributes.SNEAKING_SPEED);
            vec22 = vec22.scale(f);
        }
        return LocalPlayer.modifyInputSpeedForSquareMovement(vec22);
    }

    private static Vec2 modifyInputSpeedForSquareMovement(Vec2 vec2) {
        float f = vec2.length();
        if (f <= 0.0f) {
            return vec2;
        }
        Vec2 vec22 = vec2.scale(1.0f / f);
        float f2 = LocalPlayer.distanceToUnitSquare(vec22);
        float f3 = Math.min(f * f2, 1.0f);
        return vec22.scale(f3);
    }

    private static float distanceToUnitSquare(Vec2 vec2) {
        float f = Math.abs(vec2.x);
        float f2 = Math.abs(vec2.y);
        float f3 = f2 > f ? f / f2 : f2 / f;
        return Mth.sqrt(1.0f + Mth.square(f3));
    }

    protected boolean isControlledCamera() {
        return this.minecraft.getCameraEntity() == this;
    }

    public void resetPos() {
        this.setPose(Pose.STANDING);
        if (this.level() != null) {
            for (double d = this.getY(); d > (double)this.level().getMinY() && d <= (double)this.level().getMaxY(); d += 1.0) {
                this.setPos(this.getX(), d, this.getZ());
                if (this.level().noCollision(this)) break;
            }
            this.setDeltaMovement(Vec3.ZERO);
            this.setXRot(0.0f);
        }
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    public void aiStep() {
        PlayerRideableJumping playerRideableJumping;
        int n;
        if (this.sprintTriggerTime > 0) {
            --this.sprintTriggerTime;
        }
        if (!(this.minecraft.screen instanceof ReceivingLevelScreen)) {
            this.handlePortalTransitionEffect(this.getActivePortalLocalTransition() == Portal.Transition.CONFUSION);
            this.processPortalCooldown();
        }
        boolean bl = this.input.keyPresses.jump();
        boolean bl2 = this.input.keyPresses.shift();
        boolean bl3 = this.input.hasForwardImpulse();
        Abilities abilities = this.getAbilities();
        this.crouching = !abilities.flying && !this.isSwimming() && !this.isPassenger() && this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.CROUCHING) && (this.isShiftKeyDown() || !this.isSleeping() && !this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.STANDING));
        this.input.tick();
        this.minecraft.getTutorial().onInput(this.input);
        boolean bl4 = false;
        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            bl4 = true;
            this.input.makeJump();
        }
        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35, this.getZ() + (double)this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35, this.getZ() - (double)this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35, this.getZ() - (double)this.getBbWidth() * 0.35);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35, this.getZ() + (double)this.getBbWidth() * 0.35);
        }
        if (bl2 || this.isUsingItem() && !this.isPassenger() || this.input.keyPresses.backward()) {
            this.sprintTriggerTime = 0;
        }
        if (this.canStartSprinting()) {
            if (!bl3) {
                if (this.sprintTriggerTime > 0) {
                    this.setSprinting(true);
                } else {
                    this.sprintTriggerTime = 7;
                }
            }
            if (this.input.keyPresses.sprint()) {
                this.setSprinting(true);
            }
        }
        if (this.isSprinting()) {
            if (this.isSwimming()) {
                if (this.shouldStopSwimSprinting()) {
                    this.setSprinting(false);
                }
            } else if (this.shouldStopRunSprinting()) {
                this.setSprinting(false);
            }
        }
        boolean bl5 = false;
        if (abilities.mayfly) {
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!abilities.flying) {
                    abilities.flying = true;
                    bl5 = true;
                    this.onUpdateAbilities();
                }
            } else if (!bl && this.input.keyPresses.jump() && !bl4) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                } else if (!this.isSwimming()) {
                    boolean bl6 = abilities.flying = !abilities.flying;
                    if (abilities.flying && this.onGround()) {
                        this.jumpFromGround();
                    }
                    bl5 = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }
        if (this.input.keyPresses.jump() && !bl5 && !bl && !this.onClimbable() && this.tryToStartFallFlying()) {
            this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
        this.wasFallFlying = this.isFallFlying();
        if (this.isInWater() && this.input.keyPresses.shift() && this.isAffectedByFluids()) {
            this.goDownInWater();
        }
        if (this.isEyeInFluid(FluidTags.WATER)) {
            n = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + n, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }
        if (abilities.flying && this.isControlledCamera()) {
            n = 0;
            if (this.input.keyPresses.shift()) {
                --n;
            }
            if (this.input.keyPresses.jump()) {
                ++n;
            }
            if (n != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, (float)n * abilities.getFlyingSpeed() * 3.0f, 0.0));
            }
        }
        if ((playerRideableJumping = this.jumpableVehicle()) != null && playerRideableJumping.getJumpCooldown() == 0) {
            if (this.jumpRidingTicks < 0) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks == 0) {
                    this.jumpRidingScale = 0.0f;
                }
            }
            if (bl && !this.input.keyPresses.jump()) {
                this.jumpRidingTicks = -10;
                playerRideableJumping.onPlayerJump(Mth.floor(this.getJumpRidingScale() * 100.0f));
                this.sendRidingJump();
            } else if (!bl && this.input.keyPresses.jump()) {
                this.jumpRidingTicks = 0;
                this.jumpRidingScale = 0.0f;
            } else if (bl) {
                ++this.jumpRidingTicks;
                this.jumpRidingScale = this.jumpRidingTicks < 10 ? (float)this.jumpRidingTicks * 0.1f : 0.8f + 2.0f / (float)(this.jumpRidingTicks - 9) * 0.1f;
            }
        } else {
            this.jumpRidingScale = 0.0f;
        }
        super.aiStep();
        if (this.onGround() && abilities.flying && !this.minecraft.gameMode.isAlwaysFlying()) {
            abilities.flying = false;
            this.onUpdateAbilities();
        }
    }

    private boolean shouldStopRunSprinting() {
        return this.hasBlindness() || this.isPassenger() && !this.vehicleCanSprint(this.getVehicle()) || !this.input.hasForwardImpulse() || !this.hasEnoughFoodToSprint() || this.horizontalCollision && !this.minorHorizontalCollision || this.isInWater() && !this.isUnderWater();
    }

    private boolean shouldStopSwimSprinting() {
        return this.hasBlindness() || this.isPassenger() && !this.vehicleCanSprint(this.getVehicle()) || !this.isInWater() || !this.input.hasForwardImpulse() && !this.onGround() && !this.input.keyPresses.shift() || !this.hasEnoughFoodToSprint();
    }

    private boolean hasBlindness() {
        return this.hasEffect(MobEffects.BLINDNESS);
    }

    public Portal.Transition getActivePortalLocalTransition() {
        return this.portalProcess == null ? Portal.Transition.NONE : this.portalProcess.getPortalLocalTransition();
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    private void handlePortalTransitionEffect(boolean bl) {
        this.oPortalEffectIntensity = this.portalEffectIntensity;
        float f = 0.0f;
        if (bl && this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
            if (!(this.minecraft.screen == null || this.minecraft.screen.isPauseScreen() || this.minecraft.screen instanceof DeathScreen || this.minecraft.screen instanceof WinScreen)) {
                if (this.minecraft.screen instanceof AbstractContainerScreen) {
                    this.closeContainer();
                }
                this.minecraft.setScreen(null);
            }
            if (this.portalEffectIntensity == 0.0f) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRIGGER, this.random.nextFloat() * 0.4f + 0.8f, 0.25f));
            }
            f = 0.0125f;
            this.portalProcess.setAsInsidePortalThisTick(false);
        } else if (this.portalEffectIntensity > 0.0f) {
            f = -0.05f;
        }
        this.portalEffectIntensity = Mth.clamp(this.portalEffectIntensity + f, 0.0f, 1.0f);
    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.handsBusy = false;
        Entity entity = this.getControlledVehicle();
        if (entity instanceof AbstractBoat) {
            AbstractBoat abstractBoat = (AbstractBoat)entity;
            abstractBoat.setInput(this.input.keyPresses.left(), this.input.keyPresses.right(), this.input.keyPresses.forward(), this.input.keyPresses.backward());
            this.handsBusy |= this.input.keyPresses.left() || this.input.keyPresses.right() || this.input.keyPresses.forward() || this.input.keyPresses.backward();
        }
    }

    public boolean isHandsBusy() {
        return this.handsBusy;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        double d = this.getX();
        double d2 = this.getZ();
        super.move(moverType, vec3);
        float f = (float)(this.getX() - d);
        float f2 = (float)(this.getZ() - d2);
        this.updateAutoJump(f, f2);
        this.walkDist += Mth.length(f, f2) * 0.6f;
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    @Override
    public boolean shouldRotateWithMinecart() {
        return this.minecraft.options.rotateWithMinecart().get();
    }

    protected void updateAutoJump(float f, float f2) {
        float f3;
        if (!this.canAutoJump()) {
            return;
        }
        Vec3 vec3 = this.position();
        Vec3 vec32 = vec3.add(f, 0.0, f2);
        Vec3 vec33 = new Vec3(f, 0.0, f2);
        float f4 = this.getSpeed();
        float f5 = (float)vec33.lengthSqr();
        if (f5 <= 0.001f) {
            Vec2 vec2 = this.input.getMoveVector();
            float f6 = f4 * vec2.x;
            float f7 = f4 * vec2.y;
            f3 = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float f8 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            vec33 = new Vec3(f6 * f8 - f7 * f3, vec33.y, f7 * f8 + f6 * f3);
            f5 = (float)vec33.lengthSqr();
            if (f5 <= 0.001f) {
                return;
            }
        }
        float f9 = Mth.invSqrt(f5);
        Vec3 vec34 = vec33.scale(f9);
        Vec3 vec35 = this.getForward();
        f3 = (float)(vec35.x * vec34.x + vec35.z * vec34.z);
        if (f3 < -0.15f) {
            return;
        }
        CollisionContext collisionContext = CollisionContext.of(this);
        BlockPos blockPos = BlockPos.containing(this.getX(), this.getBoundingBox().maxY, this.getZ());
        BlockState blockState = this.level().getBlockState(blockPos);
        if (!blockState.getCollisionShape(this.level(), blockPos, collisionContext).isEmpty()) {
            return;
        }
        blockPos = blockPos.above();
        BlockState blockState2 = this.level().getBlockState(blockPos);
        if (!blockState2.getCollisionShape(this.level(), blockPos, collisionContext).isEmpty()) {
            return;
        }
        float f10 = 7.0f;
        float f11 = 1.2f;
        if (this.hasEffect(MobEffects.JUMP_BOOST)) {
            f11 += (float)(this.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75f;
        }
        float f12 = Math.max(f4 * 7.0f, 1.0f / f9);
        Vec3 vec36 = vec3;
        Vec3 vec37 = vec32.add(vec34.scale(f12));
        float f13 = this.getBbWidth();
        float f14 = this.getBbHeight();
        AABB aABB = new AABB(vec36, vec37.add(0.0, f14, 0.0)).inflate(f13, 0.0, f13);
        vec36 = vec36.add(0.0, 0.51f, 0.0);
        vec37 = vec37.add(0.0, 0.51f, 0.0);
        Vec3 vec38 = vec34.cross(new Vec3(0.0, 1.0, 0.0));
        Vec3 vec39 = vec38.scale(f13 * 0.5f);
        Vec3 vec310 = vec36.subtract(vec39);
        Vec3 vec311 = vec37.subtract(vec39);
        Vec3 vec312 = vec36.add(vec39);
        Vec3 vec313 = vec37.add(vec39);
        Iterable<VoxelShape> iterable = this.level().getCollisions(this, aABB);
        Iterator iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap(voxelShape -> voxelShape.toAabbs().stream()).iterator();
        float f15 = Float.MIN_VALUE;
        while (iterator.hasNext()) {
            AABB aABB2 = (AABB)iterator.next();
            if (!aABB2.intersects(vec310, vec311) && !aABB2.intersects(vec312, vec313)) continue;
            f15 = (float)aABB2.maxY;
            Vec3 vec314 = aABB2.getCenter();
            BlockPos blockPos2 = BlockPos.containing(vec314);
            int n = 1;
            while ((float)n < f11) {
                BlockPos blockPos3 = blockPos2.above(n);
                BlockState blockState3 = this.level().getBlockState(blockPos3);
                VoxelShape voxelShape2 = blockState3.getCollisionShape(this.level(), blockPos3, collisionContext);
                if (!voxelShape2.isEmpty() && (double)(f15 = (float)voxelShape2.max(Direction.Axis.Y) + (float)blockPos3.getY()) - this.getY() > (double)f11) {
                    return;
                }
                if (n > 1) {
                    blockPos = blockPos.above();
                    BlockState blockState4 = this.level().getBlockState(blockPos);
                    if (!blockState4.getCollisionShape(this.level(), blockPos, collisionContext).isEmpty()) {
                        return;
                    }
                }
                ++n;
            }
            break block0;
        }
        if (f15 == Float.MIN_VALUE) {
            return;
        }
        float f16 = (float)((double)f15 - this.getY());
        if (f16 <= 0.5f || f16 > f11) {
            return;
        }
        this.autoJumpTime = 1;
    }

    @Override
    protected boolean isHorizontalCollisionMinor(Vec3 vec3) {
        float f = this.getYRot() * ((float)Math.PI / 180);
        double d = Mth.sin(f);
        double d2 = Mth.cos(f);
        double d3 = (double)this.xxa * d2 - (double)this.zza * d;
        double d4 = (double)this.zza * d2 + (double)this.xxa * d;
        double d5 = Mth.square(d3) + Mth.square(d4);
        double d6 = Mth.square(vec3.x) + Mth.square(vec3.z);
        if (d5 < (double)1.0E-5f || d6 < (double)1.0E-5f) {
            return false;
        }
        double d7 = d3 * vec3.x + d4 * vec3.z;
        double d8 = Math.acos(d7 / Math.sqrt(d5 * d6));
        return d8 < 0.13962633907794952;
    }

    private boolean canAutoJump() {
        return this.isAutoJumpEnabled() && this.autoJumpTime <= 0 && this.onGround() && !this.isStayingOnGroundSurface() && !this.isPassenger() && this.isMoving() && (double)this.getBlockJumpFactor() >= 1.0;
    }

    private boolean isMoving() {
        return this.input.getMoveVector().lengthSquared() > 0.0f;
    }

    private boolean canStartSprinting() {
        return !(this.isSprinting() || !this.input.hasForwardImpulse() || !this.hasEnoughFoodToSprint() || this.isUsingItem() || this.hasBlindness() || this.isPassenger() && !this.vehicleCanSprint(this.getVehicle()) || this.isFallFlying() && !this.isUnderWater() || this.isMovingSlowly() && !this.isUnderWater() || this.isInWater() && !this.isUnderWater());
    }

    private boolean vehicleCanSprint(Entity entity) {
        return entity.canSprint() && entity.isLocalInstanceAuthoritative();
    }

    private boolean hasEnoughFoodToSprint() {
        return this.isPassenger() || (float)this.getFoodData().getFoodLevel() > 6.0f || this.getAbilities().mayfly;
    }

    public float getWaterVision() {
        if (!this.isEyeInFluid(FluidTags.WATER)) {
            return 0.0f;
        }
        float f = 600.0f;
        float f2 = 100.0f;
        if ((float)this.waterVisionTime >= 600.0f) {
            return 1.0f;
        }
        float f3 = Mth.clamp((float)this.waterVisionTime / 100.0f, 0.0f, 1.0f);
        float f4 = (float)this.waterVisionTime < 100.0f ? 0.0f : Mth.clamp(((float)this.waterVisionTime - 100.0f) / 500.0f, 0.0f, 1.0f);
        return f3 * 0.6f + f4 * 0.39999998f;
    }

    public void onGameModeChanged(GameType gameType) {
        if (gameType == GameType.SPECTATOR) {
            this.setDeltaMovement(this.getDeltaMovement().with(Direction.Axis.Y, 0.0));
        }
    }

    @Override
    public boolean isUnderWater() {
        return this.wasUnderwater;
    }

    @Override
    protected boolean updateIsUnderwater() {
        boolean bl = this.wasUnderwater;
        boolean bl2 = super.updateIsUnderwater();
        if (this.isSpectator()) {
            return this.wasUnderwater;
        }
        if (!bl && bl2) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.AMBIENT, 1.0f, 1.0f, false);
            this.minecraft.getSoundManager().play(new UnderwaterAmbientSoundInstances.UnderwaterAmbientSoundInstance(this));
        }
        if (bl && !bl2) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.AMBIENT, 1.0f, 1.0f, false);
        }
        return this.wasUnderwater;
    }

    @Override
    public Vec3 getRopeHoldPosition(float f) {
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            float f2 = Mth.lerp(f * 0.5f, this.getYRot(), this.yRotO) * ((float)Math.PI / 180);
            float f3 = Mth.lerp(f * 0.5f, this.getXRot(), this.xRotO) * ((float)Math.PI / 180);
            double d = this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0;
            Vec3 vec3 = new Vec3(0.39 * d, -0.6, 0.3);
            return vec3.xRot(-f3).yRot(-f2).add(this.getEyePosition(f));
        }
        return super.getRopeHoldPosition(f);
    }

    @Override
    public void updateTutorialInventoryAction(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction) {
        this.minecraft.getTutorial().onInventoryAction(itemStack, itemStack2, clickAction);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.getYRot();
    }

    @Override
    public void handleCreativeModeItemDrop(ItemStack itemStack) {
        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
    }

    @Override
    public boolean canDropItems() {
        return this.dropSpamThrottler.isUnderThreshold();
    }

    public TickThrottler getDropSpamThrottler() {
        return this.dropSpamThrottler;
    }

    public Input getLastSentInput() {
        return this.lastSentInput;
    }
}

