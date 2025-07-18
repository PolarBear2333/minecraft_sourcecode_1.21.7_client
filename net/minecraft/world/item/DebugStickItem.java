/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem
extends Item {
    public DebugStickItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            this.handleInteraction(player, blockState, level, blockPos, false, itemStack);
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Player player = useOnContext.getPlayer();
        Level level = useOnContext.getLevel();
        if (!level.isClientSide && player != null && !this.handleInteraction(player, level.getBlockState(blockPos = useOnContext.getClickedPos()), level, blockPos, true, useOnContext.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    private boolean handleInteraction(Player player, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl, ItemStack itemStack) {
        if (!player.canUseGameMasterBlocks()) {
            return false;
        }
        Holder<Block> holder = blockState.getBlockHolder();
        StateDefinition<Block, BlockState> stateDefinition = holder.value().getStateDefinition();
        Collection<Property<?>> collection = stateDefinition.getProperties();
        if (collection.isEmpty()) {
            DebugStickItem.message(player, Component.translatable(this.descriptionId + ".empty", holder.getRegisteredName()));
            return false;
        }
        DebugStickState debugStickState = itemStack.get(DataComponents.DEBUG_STICK_STATE);
        if (debugStickState == null) {
            return false;
        }
        Property<?> property = debugStickState.properties().get(holder);
        if (bl) {
            if (property == null) {
                property = collection.iterator().next();
            }
            BlockState blockState2 = DebugStickItem.cycleState(blockState, property, player.isSecondaryUseActive());
            levelAccessor.setBlock(blockPos, blockState2, 18);
            DebugStickItem.message(player, Component.translatable(this.descriptionId + ".update", property.getName(), DebugStickItem.getNameHelper(blockState2, property)));
        } else {
            property = DebugStickItem.getRelative(collection, property, player.isSecondaryUseActive());
            itemStack.set(DataComponents.DEBUG_STICK_STATE, debugStickState.withProperty(holder, property));
            DebugStickItem.message(player, Component.translatable(this.descriptionId + ".select", property.getName(), DebugStickItem.getNameHelper(blockState, property)));
        }
        return true;
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState blockState, Property<T> property, boolean bl) {
        return (BlockState)blockState.setValue(property, (Comparable)DebugStickItem.getRelative(property.getPossibleValues(), blockState.getValue(property), bl));
    }

    private static <T> T getRelative(Iterable<T> iterable, @Nullable T t, boolean bl) {
        return bl ? Util.findPreviousInIterable(iterable, t) : Util.findNextInIterable(iterable, t);
    }

    private static void message(Player player, Component component) {
        ((ServerPlayer)player).sendSystemMessage(component, true);
    }

    private static <T extends Comparable<T>> String getNameHelper(BlockState blockState, Property<T> property) {
        return property.getName(blockState.getValue(property));
    }
}

