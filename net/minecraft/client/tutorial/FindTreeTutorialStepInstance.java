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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialStepInstance;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FindTreeTutorialStepInstance
implements TutorialStepInstance {
    private static final int HINT_DELAY = 6000;
    private static final Component TITLE = Component.translatable("tutorial.find_tree.title");
    private static final Component DESCRIPTION = Component.translatable("tutorial.find_tree.description");
    private final Tutorial tutorial;
    @Nullable
    private TutorialToast toast;
    private int timeWaiting;

    public FindTreeTutorialStepInstance(Tutorial tutorial) {
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
        if (this.timeWaiting == 1 && (localPlayer = minecraft.player) != null && (FindTreeTutorialStepInstance.hasCollectedTreeItems(localPlayer) || FindTreeTutorialStepInstance.hasPunchedTreesPreviously(localPlayer))) {
            this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
            return;
        }
        if (this.timeWaiting >= 6000 && this.toast == null) {
            this.toast = new TutorialToast(minecraft.font, TutorialToast.Icons.TREE, TITLE, DESCRIPTION, false);
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
    public void onLookAt(ClientLevel clientLevel, HitResult hitResult) {
        BlockState blockState;
        if (hitResult.getType() == HitResult.Type.BLOCK && (blockState = clientLevel.getBlockState(((BlockHitResult)hitResult).getBlockPos())).is(BlockTags.COMPLETES_FIND_TREE_TUTORIAL)) {
            this.tutorial.setStep(TutorialSteps.PUNCH_TREE);
        }
    }

    @Override
    public void onGetItem(ItemStack itemStack) {
        if (itemStack.is(ItemTags.COMPLETES_FIND_TREE_TUTORIAL)) {
            this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
        }
    }

    private static boolean hasCollectedTreeItems(LocalPlayer localPlayer) {
        return localPlayer.getInventory().hasAnyMatching(itemStack -> itemStack.is(ItemTags.COMPLETES_FIND_TREE_TUTORIAL));
    }

    public static boolean hasPunchedTreesPreviously(LocalPlayer localPlayer) {
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.COMPLETES_FIND_TREE_TUTORIAL)) {
            Block block = holder.value();
            if (localPlayer.getStats().getValue(Stats.BLOCK_MINED.get(block)) <= 0) continue;
            return true;
        }
        return false;
    }
}

