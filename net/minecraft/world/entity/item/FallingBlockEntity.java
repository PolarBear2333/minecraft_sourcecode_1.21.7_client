/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class FallingBlockEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.SAND.defaultBlockState();
    private static final int DEFAULT_TIME = 0;
    private static final float DEFAULT_FALL_DAMAGE_PER_DISTANCE = 0.0f;
    private static final int DEFAULT_MAX_FALL_DAMAGE = 40;
    private static final boolean DEFAULT_DROP_ITEM = true;
    private static final boolean DEFAULT_CANCEL_DROP = false;
    private BlockState blockState = DEFAULT_BLOCK_STATE;
    public int time = 0;
    public boolean dropItem = true;
    private boolean cancelDrop = false;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance = 0.0f;
    @Nullable
    public CompoundTag blockData;
    public boolean forceTickAfterTeleportToDuplicate;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
    }

    private FallingBlockEntity(Level level, double d, double d2, double d3, BlockState blockState) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, level);
        this.blockState = blockState;
        this.blocksBuilding = true;
        this.setPos(d, d2, d3);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
        this.setStartPos(this.blockPosition());
    }

    public static FallingBlockEntity fall(Level level, BlockPos blockPos, BlockState blockState) {
        FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(level, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, blockState.hasProperty(BlockStateProperties.WATERLOGGED) ? (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, false) : blockState);
        level.setBlock(blockPos, blockState.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(fallingBlockEntity);
        return fallingBlockEntity;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (!this.isInvulnerableToBase(damageSource)) {
            this.markHurt();
        }
        return false;
    }

    public void setStartPos(BlockPos blockPos) {
        this.entityData.set(DATA_START_POS, blockPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
            return;
        }
        Block block = this.blockState.getBlock();
        ++this.time;
        this.applyGravity();
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        this.handlePortal();
        Object object = this.level();
        if (object instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            if (this.isAlive() || this.forceTickAfterTeleportToDuplicate) {
                Object object2;
                object = this.blockPosition();
                boolean bl = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean bl2 = bl && this.level().getFluidState((BlockPos)object).is(FluidTags.WATER);
                double d = this.getDeltaMovement().lengthSqr();
                if (bl && d > 1.0 && ((BlockHitResult)(object2 = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this)))).getType() != HitResult.Type.MISS && this.level().getFluidState(((BlockHitResult)object2).getBlockPos()).is(FluidTags.WATER)) {
                    object = ((BlockHitResult)object2).getBlockPos();
                    bl2 = true;
                }
                if (this.onGround() || bl2) {
                    object2 = this.level().getBlockState((BlockPos)object);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
                    if (!((BlockBehaviour.BlockStateBase)object2).is(Blocks.MOVING_PISTON)) {
                        if (!this.cancelDrop) {
                            boolean bl3;
                            boolean bl4 = ((BlockBehaviour.BlockStateBase)object2).canBeReplaced(new DirectionalPlaceContext(this.level(), (BlockPos)object, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean bl5 = FallingBlock.isFree(this.level().getBlockState(((BlockPos)object).below())) && (!bl || !bl2);
                            boolean bl6 = bl3 = this.blockState.canSurvive(this.level(), (BlockPos)object) && !bl5;
                            if (bl4 && bl3) {
                                if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState((BlockPos)object).getType() == Fluids.WATER) {
                                    this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                                }
                                if (this.level().setBlock((BlockPos)object, this.blockState, 3)) {
                                    BlockEntity blockEntity;
                                    ((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket((BlockPos)object, this.level().getBlockState((BlockPos)object)));
                                    this.discard();
                                    if (block instanceof Fallable) {
                                        ((Fallable)((Object)block)).onLand(this.level(), (BlockPos)object, this.blockState, (BlockState)object2, this);
                                    }
                                    if (this.blockData != null && this.blockState.hasBlockEntity() && (blockEntity = this.level().getBlockEntity((BlockPos)object)) != null) {
                                        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER);){
                                            RegistryAccess registryAccess = this.level().registryAccess();
                                            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, registryAccess);
                                            blockEntity.saveWithoutMetadata(tagValueOutput);
                                            CompoundTag compoundTag = tagValueOutput.buildResult();
                                            this.blockData.forEach((string, tag) -> compoundTag.put((String)string, tag.copy()));
                                            blockEntity.loadWithComponents(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)registryAccess, compoundTag));
                                        }
                                        catch (Exception exception) {
                                            LOGGER.error("Failed to load block entity from falling block", (Throwable)exception);
                                        }
                                        blockEntity.setChanged();
                                    }
                                } else if (this.dropItem && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.discard();
                                    this.callOnBrokenAfterFall(block, (BlockPos)object);
                                    this.spawnAtLocation(serverLevel, block);
                                }
                            } else {
                                this.discard();
                                if (this.dropItem && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.callOnBrokenAfterFall(block, (BlockPos)object);
                                    this.spawnAtLocation(serverLevel, block);
                                }
                            }
                        } else {
                            this.discard();
                            this.callOnBrokenAfterFall(block, (BlockPos)object);
                        }
                    }
                } else if (this.time > 100 && (((Vec3i)object).getY() <= this.level().getMinY() || ((Vec3i)object).getY() > this.level().getMaxY()) || this.time > 600) {
                    if (this.dropItem && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.spawnAtLocation(serverLevel, block);
                    }
                    this.discard();
                }
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    public void callOnBrokenAfterFall(Block block, BlockPos blockPos) {
        if (block instanceof Fallable) {
            ((Fallable)((Object)block)).onBrokenAfterFall(this.level(), blockPos, this);
        }
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        DamageSource damageSource2;
        if (!this.hurtEntities) {
            return false;
        }
        int n = Mth.ceil(d - 1.0);
        if (n < 0) {
            return false;
        }
        Predicate<Entity> predicate = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        Block block = this.blockState.getBlock();
        if (block instanceof Fallable) {
            Fallable fallable = (Fallable)((Object)block);
            damageSource2 = fallable.getFallDamageSource(this);
        } else {
            damageSource2 = this.damageSources().fallingBlock(this);
        }
        DamageSource damageSource3 = damageSource2;
        float f2 = Math.min(Mth.floor((float)n * this.fallDamagePerDistance), this.fallDamageMax);
        this.level().getEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.hurt(damageSource3, f2));
        boolean bl = this.blockState.is(BlockTags.ANVIL);
        if (bl && f2 > 0.0f && this.random.nextFloat() < 0.05f + (float)n * 0.05f) {
            BlockState blockState = AnvilBlock.damage(this.blockState);
            if (blockState == null) {
                this.cancelDrop = true;
            } else {
                this.blockState = blockState;
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.store("BlockState", BlockState.CODEC, this.blockState);
        valueOutput.putInt("Time", this.time);
        valueOutput.putBoolean("DropItem", this.dropItem);
        valueOutput.putBoolean("HurtEntities", this.hurtEntities);
        valueOutput.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        valueOutput.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            valueOutput.store("TileEntityData", CompoundTag.CODEC, this.blockData);
        }
        valueOutput.putBoolean("CancelDrop", this.cancelDrop);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.blockState = valueInput.read("BlockState", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE);
        this.time = valueInput.getIntOr("Time", 0);
        boolean bl = this.blockState.is(BlockTags.ANVIL);
        this.hurtEntities = valueInput.getBooleanOr("HurtEntities", bl);
        this.fallDamagePerDistance = valueInput.getFloatOr("FallHurtAmount", 0.0f);
        this.fallDamageMax = valueInput.getIntOr("FallHurtMax", 40);
        this.dropItem = valueInput.getBooleanOr("DropItem", true);
        this.blockData = valueInput.read("TileEntityData", CompoundTag.CODEC).orElse(null);
        this.cancelDrop = valueInput.getBooleanOr("CancelDrop", false);
    }

    public void setHurtsEntities(float f, int n) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = f;
        this.fallDamageMax = n;
    }

    public void disableDrop() {
        this.cancelDrop = true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        super.fillCrashReportCategory(crashReportCategory);
        crashReportCategory.setDetail("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, serverEntity, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.blockState = Block.stateById(clientboundAddEntityPacket.getData());
        this.blocksBuilding = true;
        double d = clientboundAddEntityPacket.getX();
        double d2 = clientboundAddEntityPacket.getY();
        double d3 = clientboundAddEntityPacket.getZ();
        this.setPos(d, d2, d3);
        this.setStartPos(this.blockPosition());
    }

    @Override
    @Nullable
    public Entity teleport(TeleportTransition teleportTransition) {
        ResourceKey<Level> resourceKey = teleportTransition.newLevel().dimension();
        ResourceKey<Level> resourceKey2 = this.level().dimension();
        boolean bl = (resourceKey2 == Level.END || resourceKey == Level.END) && resourceKey2 != resourceKey;
        Entity entity = super.teleport(teleportTransition);
        this.forceTickAfterTeleportToDuplicate = entity != null && bl;
        return entity;
    }
}

