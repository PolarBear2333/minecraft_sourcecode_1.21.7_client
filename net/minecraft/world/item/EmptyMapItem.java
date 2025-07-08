/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;

public class EmptyMapItem
extends Item {
    public EmptyMapItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        itemStack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        serverLevel.playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0f, 1.0f);
        ItemStack itemStack2 = MapItem.create(serverLevel, player.getBlockX(), player.getBlockZ(), (byte)0, true, false);
        if (itemStack.isEmpty()) {
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack2);
        }
        if (!player.getInventory().add(itemStack2.copy())) {
            player.drop(itemStack2, false);
        }
        return InteractionResult.SUCCESS;
    }
}

