/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.toasts;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AdvancementToast
implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    public static final int DISPLAY_TIME = 5000;
    private final AdvancementHolder advancement;
    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

    public AdvancementToast(AdvancementHolder advancementHolder) {
        this.advancement = advancementHolder;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager toastManager, long l) {
        DisplayInfo displayInfo = this.advancement.value().display().orElse(null);
        if (displayInfo == null) {
            this.wantedVisibility = Toast.Visibility.HIDE;
            return;
        }
        this.wantedVisibility = (double)l >= 5000.0 * toastManager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    @Nullable
    public SoundEvent getSoundEvent() {
        return this.isChallengeAdvancement() ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : null;
    }

    private boolean isChallengeAdvancement() {
        Optional<DisplayInfo> optional = this.advancement.value().display();
        return optional.isPresent() && optional.get().getType().equals(AdvancementType.CHALLENGE);
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long l) {
        int n;
        DisplayInfo displayInfo = this.advancement.value().display().orElse(null);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        if (displayInfo == null) {
            return;
        }
        List<FormattedCharSequence> list = font.split(displayInfo.getTitle(), 125);
        int n2 = n = displayInfo.getType() == AdvancementType.CHALLENGE ? -30465 : -256;
        if (list.size() == 1) {
            guiGraphics.drawString(font, displayInfo.getType().getDisplayName(), 30, 7, n, false);
            guiGraphics.drawString(font, list.get(0), 30, 18, -1, false);
        } else {
            int n3 = 1500;
            float f = 300.0f;
            if (l < 1500L) {
                int n4 = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f);
                guiGraphics.drawString(font, displayInfo.getType().getDisplayName(), 30, 11, ARGB.color(n4, n), false);
            } else {
                int n5 = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f);
                int n6 = this.height() / 2 - list.size() * font.lineHeight / 2;
                for (FormattedCharSequence formattedCharSequence : list) {
                    guiGraphics.drawString(font, formattedCharSequence, 30, n6, ARGB.color(n5, -1), false);
                    n6 += font.lineHeight;
                }
            }
        }
        guiGraphics.renderFakeItem(displayInfo.getIcon(), 8, 8);
    }
}

