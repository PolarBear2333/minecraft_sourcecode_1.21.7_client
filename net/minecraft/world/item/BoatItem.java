/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem
extends Item {
    private final EntityType<? extends AbstractBoat> entityType;

    public BoatItem(EntityType<? extends AbstractBoat> entityType, Item.Properties properties) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        Object object;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = BoatItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (((HitResult)blockHitResult).getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        Vec3 vec3 = player.getViewVector(1.0f);
        double d = 5.0;
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), EntitySelector.CAN_BE_PICKED);
        if (!list.isEmpty()) {
            object = player.getEyePosition();
            for (Entity entity : list) {
                AABB aABB = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (!aABB.contains((Vec3)object)) continue;
                return InteractionResult.PASS;
            }
        }
        if (((HitResult)blockHitResult).getType() == HitResult.Type.BLOCK) {
            object = this.getBoat(level, blockHitResult, itemStack, player);
            if (object == null) {
                return InteractionResult.FAIL;
            }
            ((Entity)object).setYRot(player.getYRot());
            if (!level.noCollision((Entity)object, ((Entity)object).getBoundingBox())) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                level.addFreshEntity((Entity)object);
                level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, blockHitResult.getLocation());
                itemStack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private AbstractBoat getBoat(Level level, HitResult hitResult, ItemStack itemStack, Player player) {
        AbstractBoat abstractBoat = this.entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (abstractBoat != null) {
            Vec3 vec3 = hitResult.getLocation();
            abstractBoat.setInitialPos(vec3.x, vec3.y, vec3.z);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                EntityType.createDefaultStackConfig(serverLevel, itemStack, player).accept(abstractBoat);
            }
        }
        return abstractBoat;
    }
}

