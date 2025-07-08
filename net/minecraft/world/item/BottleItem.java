/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BottleItem
extends Item {
    public BottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        List<AreaEffectCloud> list = level.getEntitiesOfClass(AreaEffectCloud.class, player.getBoundingBox().inflate(2.0), areaEffectCloud -> areaEffectCloud != null && areaEffectCloud.isAlive() && areaEffectCloud.getOwner() instanceof EnderDragon);
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!list.isEmpty()) {
            AreaEffectCloud areaEffectCloud2 = list.get(0);
            areaEffectCloud2.setRadius(areaEffectCloud2.getRadius() - 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0f, 1.0f);
            level.gameEvent((Entity)player, GameEvent.FLUID_PICKUP, player.position());
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(serverPlayer, itemStack, areaEffectCloud2);
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(this.turnBottleIntoItem(itemStack, player, new ItemStack(Items.DRAGON_BREATH)));
        }
        BlockHitResult blockHitResult = BottleItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!level.mayInteract(player, blockPos)) {
                return InteractionResult.PASS;
            }
            if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
                level.playSound((Entity)player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                level.gameEvent((Entity)player, GameEvent.FLUID_PICKUP, blockPos);
                return InteractionResult.SUCCESS.heldItemTransformedTo(this.turnBottleIntoItem(itemStack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
            }
        }
        return InteractionResult.PASS;
    }

    protected ItemStack turnBottleIntoItem(ItemStack itemStack, Player player, ItemStack itemStack2) {
        player.awardStat(Stats.ITEM_USED.get(this));
        return ItemUtils.createFilledResult(itemStack, player, itemStack2);
    }
}

