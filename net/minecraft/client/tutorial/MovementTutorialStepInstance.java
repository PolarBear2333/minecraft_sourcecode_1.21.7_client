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
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialStepInstance;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;

public class MovementTutorialStepInstance
implements TutorialStepInstance {
    private static final int MINIMUM_TIME_MOVED = 40;
    private static final int MINIMUM_TIME_LOOKED = 40;
    private static final int MOVE_HINT_DELAY = 100;
    private static final int LOOK_HINT_DELAY = 20;
    private static final int INCOMPLETE = -1;
    private static final Component MOVE_TITLE = Component.translatable("tutorial.move.title", Tutorial.key("forward"), Tutorial.key("left"), Tutorial.key("back"), Tutorial.key("right"));
    private static final Component MOVE_DESCRIPTION = Component.translatable("tutorial.move.description", Tutorial.key("jump"));
    private static final Component LOOK_TITLE = Component.translatable("tutorial.look.title");
    private static final Component LOOK_DESCRIPTION = Component.translatable("tutorial.look.description");
    private final Tutorial tutorial;
    @Nullable
    private TutorialToast moveToast;
    @Nullable
    private TutorialToast lookToast;
    private int timeWaiting;
    private int timeMoved;
    private int timeLooked;
    private boolean moved;
    private boolean turned;
    private int moveCompleted = -1;
    private int lookCompleted = -1;

    public MovementTutorialStepInstance(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

    @Override
    public void tick() {
        ++this.timeWaiting;
        if (this.moved) {
            ++this.timeMoved;
            this.moved = false;
        }
        if (this.turned) {
            ++this.timeLooked;
            this.turned = false;
        }
        if (this.moveCompleted == -1 && this.timeMoved > 40) {
            if (this.moveToast != null) {
                this.moveToast.hide();
                this.moveToast = null;
            }
            this.moveCompleted = this.timeWaiting;
        }
        if (this.lookCompleted == -1 && this.timeLooked > 40) {
            if (this.lookToast != null) {
                this.lookToast.hide();
                this.lookToast = null;
            }
            this.lookCompleted = this.timeWaiting;
        }
        if (this.moveCompleted != -1 && this.lookCompleted != -1) {
            if (this.tutorial.isSurvival()) {
                this.tutorial.setStep(TutorialSteps.FIND_TREE);
            } else {
                this.tutorial.setStep(TutorialSteps.NONE);
            }
        }
        if (this.moveToast != null) {
            this.moveToast.updateProgress((float)this.timeMoved / 40.0f);
        }
        if (this.lookToast != null) {
            this.lookToast.updateProgress((float)this.timeLooked / 40.0f);
        }
        if (this.timeWaiting >= 100) {
            Minecraft minecraft = this.tutorial.getMinecraft();
            if (this.moveCompleted == -1 && this.moveToast == null) {
                this.moveToast = new TutorialToast(minecraft.font, TutorialToast.Icons.MOVEMENT_KEYS, MOVE_TITLE, MOVE_DESCRIPTION, true);
                minecraft.getToastManager().addToast(this.moveToast);
            } else if (this.moveCompleted != -1 && this.timeWaiting - this.moveCompleted >= 20 && this.lookCompleted == -1 && this.lookToast == null) {
                this.lookToast = new TutorialToast(minecraft.font, TutorialToast.Icons.MOUSE, LOOK_TITLE, LOOK_DESCRIPTION, true);
                minecraft.getToastManager().addToast(this.lookToast);
            }
        }
    }

    @Override
    public void clear() {
        if (this.moveToast != null) {
            this.moveToast.hide();
            this.moveToast = null;
        }
        if (this.lookToast != null) {
            this.lookToast.hide();
            this.lookToast = null;
        }
    }

    @Override
    public void onInput(ClientInput clientInput) {
        if (clientInput.keyPresses.forward() || clientInput.keyPresses.backward() || clientInput.keyPresses.left() || clientInput.keyPresses.right() || clientInput.keyPresses.jump()) {
            this.moved = true;
        }
    }

    @Override
    public void onMouse(double d, double d2) {
        if (Math.abs(d) > 0.01 || Math.abs(d2) > 0.01) {
            this.turned = true;
        }
    }
}

