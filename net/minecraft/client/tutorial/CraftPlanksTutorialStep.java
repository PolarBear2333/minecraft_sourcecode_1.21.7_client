/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialStepInstance;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftPlanksTutorialStep
implements TutorialStepInstance {
    private static final int HINT_DELAY = 1200;
    private static final Component CRAFT_TITLE = Component.translatable("tutorial.craft_planks.title");
    private static final Component CRAFT_DESCRIPTION = Component.translatable("tutorial.craft_planks.description");
    private final Tutorial tutorial;
    @Nullable
    private TutorialToast toast;
    private int timeWaiting;

    public CraftPlanksTutorialStep(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    @Override
    public void tick() {
        LocalPlayer localPlayer;
        ++this.timeWaiting;
        if (!this.tutorial.isSurvival()) {
            this.tutorial.setStep(TutorialSteps.NONE);
            return;
        }
        Minecraft minecraft = this.tutorial.getMinecraft();
        if (this.timeWaiting == 1 && (localPlayer = minecraft.player) != null) {
            if (localPlayer.getInventory().contains(ItemTags.PLANKS)) {
                this.tutorial.setStep(TutorialSteps.NONE);
                return;
            }
            if (CraftPlanksTutorialStep.hasCraftedPlanksPreviously(localPlayer, ItemTags.PLANKS)) {
                this.tutorial.setStep(TutorialSteps.NONE);
                return;
            }
        }
        if (this.timeWaiting >= 1200 && this.toast == null) {
            this.toast = new TutorialToast(minecraft.font, TutorialToast.Icons.WOODEN_PLANKS, CRAFT_TITLE, CRAFT_DESCRIPTION, false);
            minecraft.getToastManager().addToast(this.toast);
        }
    }

    @Override
    public void clear() {
        if (this.toast != null) {
            this.toast.hide();
            this.toast = null;
        }
    }

    @Override
    public void onGetItem(ItemStack itemStack) {
        if (itemStack.is(ItemTags.PLANKS)) {
            this.tutorial.setStep(TutorialSteps.NONE);
        }
    }

    public static boolean hasCraftedPlanksPreviously(LocalPlayer localPlayer, TagKey<Item> tagKey) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
            if (localPlayer.getStats().getValue(Stats.ITEM_CRAFTED.get(holder.value())) <= 0) continue;
            return true;
        }
        return false;
    }
}

