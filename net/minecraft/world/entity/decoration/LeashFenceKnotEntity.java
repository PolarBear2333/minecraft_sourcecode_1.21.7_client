/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.decoration;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LeashFenceKnotEntity
extends BlockAttachedEntity {
    public static final double OFFSET_Y = 0.375;

    public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> entityType, Level level) {
        super((EntityType<? extends BlockAttachedEntity>)entityType, level);
    }

    public LeashFenceKnotEntity(Level level, BlockPos blockPos) {
        super(EntityType.LEASH_KNOT, level, blockPos);
        this.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void recalculateBoundingBox() {
        this.setPosRaw((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.375, (double)this.pos.getZ() + 0.5);
        double d = (double)this.getType().getWidth() / 2.0;
        double d2 = this.getType().getHeight();
        this.setBoundingBox(new AABB(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + d2, this.getZ() + d));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 1024.0;
    }

    @Override
    public void dropItem(ServerLevel serverLevel, @Nullable Entity entity) {
        this.playSound(SoundEvents.LEAD_UNTIED, 1.0f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        Object object;
        Object object2;
        InteractionResult interactionResult;
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.getItemInHand(interactionHand).is(Items.SHEARS) && (interactionResult = super.interact(player, interactionHand)) instanceof InteractionResult.Success && ((InteractionResult.Success)(object2 = (InteractionResult.Success)interactionResult)).wasItemInteraction()) {
            return interactionResult;
        }
        boolean bl = false;
        object2 = Leashable.leashableLeashedTo(player);
        Iterator<Leashable> iterator = object2.iterator();
        while (iterator.hasNext()) {
            object = iterator.next();
            if (!object.canHaveALeashAttachedTo(this)) continue;
            object.setLeashedTo(this, true);
            bl = true;
        }
        boolean bl2 = false;
        if (!bl && !player.isSecondaryUseActive()) {
            object = Leashable.leashableLeashedTo(this);
            Iterator iterator2 = object.iterator();
            while (iterator2.hasNext()) {
                Leashable leashable = (Leashable)iterator2.next();
                if (!leashable.canHaveALeashAttachedTo(player)) continue;
                leashable.setLeashedTo(player, true);
                bl2 = true;
            }
        }
        if (bl || bl2) {
            this.gameEvent(GameEvent.BLOCK_ATTACH, player);
            this.playSound(SoundEvents.LEAD_TIED);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, interactionHand);
    }

    @Override
    public void notifyLeasheeRemoved(Leashable leashable) {
        if (Leashable.leashableLeashedTo(this).isEmpty()) {
            this.discard();
        }
    }

    @Override
    public boolean survives() {
        return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
    }

    public static LeashFenceKnotEntity getOrCreateKnot(Level level, BlockPos blockPos) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        List<LeashFenceKnotEntity> list = level.getEntitiesOfClass(LeashFenceKnotEntity.class, new AABB((double)n - 1.0, (double)n2 - 1.0, (double)n3 - 1.0, (double)n + 1.0, (double)n2 + 1.0, (double)n3 + 1.0));
        for (LeashFenceKnotEntity leashFenceKnotEntity : list) {
            if (!leashFenceKnotEntity.getPos().equals(blockPos)) continue;
            return leashFenceKnotEntity;
        }
        LeashFenceKnotEntity leashFenceKnotEntity = new LeashFenceKnotEntity(level, blockPos);
        level.addFreshEntity(leashFenceKnotEntity);
        return leashFenceKnotEntity;
    }

    public void playPlacementSound() {
        this.playSound(SoundEvents.LEAD_TIED, 1.0f, 1.0f);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, 0, this.getPos());
    }

    @Override
    public Vec3 getRopeHoldPosition(float f) {
        return this.getPosition(f).add(0.0, 0.2, 0.0);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.LEAD);
    }
}

