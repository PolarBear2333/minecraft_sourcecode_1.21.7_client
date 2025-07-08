/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem
extends Item {
    public InstrumentItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Item item, Holder<Instrument> holder) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.set(DataComponents.INSTRUMENT, new InstrumentComponent(holder));
        return itemStack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Optional<Holder<Instrument>> optional = this.getInstrument(itemStack, player.registryAccess());
        if (optional.isPresent()) {
            Instrument instrument = optional.get().value();
            player.startUsingItem(interactionHand);
            InstrumentItem.play(level, player, instrument);
            player.getCooldowns().addCooldown(itemStack, Mth.floor(instrument.useDuration() * 20.0f));
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        Optional<Holder<Instrument>> optional = this.getInstrument(itemStack, livingEntity.registryAccess());
        return optional.map(holder -> Mth.floor(((Instrument)holder.value()).useDuration() * 20.0f)).orElse(0);
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack itemStack, HolderLookup.Provider provider) {
        InstrumentComponent instrumentComponent = itemStack.get(DataComponents.INSTRUMENT);
        return instrumentComponent != null ? instrumentComponent.unwrap(provider) : Optional.empty();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    private static void play(Level level, Player player, Instrument instrument) {
        SoundEvent soundEvent = instrument.soundEvent().value();
        float f = instrument.range() / 16.0f;
        level.playSound((Entity)player, player, soundEvent, SoundSource.RECORDS, f, 1.0f);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }
}

