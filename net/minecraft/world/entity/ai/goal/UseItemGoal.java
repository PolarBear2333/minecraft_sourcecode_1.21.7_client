/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

public class UseItemGoal<T extends Mob>
extends Goal {
    private final T mob;
    private final ItemStack item;
    private final Predicate<? super T> canUseSelector;
    @Nullable
    private final SoundEvent finishUsingSound;

    public UseItemGoal(T t, ItemStack itemStack, @Nullable SoundEvent soundEvent, Predicate<? super T> predicate) {
        this.mob = t;
        this.item = itemStack;
        this.finishUsingSound = soundEvent;
        this.canUseSelector = predicate;
    }

    @Override
    public boolean canUse() {
        return this.canUseSelector.test(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return ((LivingEntity)this.mob).isUsingItem();
    }

    @Override
    public void start() {
        ((LivingEntity)this.mob).setItemSlot(EquipmentSlot.MAINHAND, this.item.copy());
        ((LivingEntity)this.mob).startUsingItem(InteractionHand.MAIN_HAND);
    }

    @Override
    public void stop() {
        ((LivingEntity)this.mob).setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (this.finishUsingSound != null) {
            ((Entity)this.mob).playSound(this.finishUsingSound, 1.0f, ((Entity)this.mob).getRandom().nextFloat() * 0.2f + 0.9f);
        }
    }
}

