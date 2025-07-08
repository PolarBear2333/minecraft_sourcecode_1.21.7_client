/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Ordering
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

public class EffectsInInventory {
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_small");
    private final AbstractContainerScreen<?> screen;
    private final Minecraft minecraft;
    @Nullable
    private MobEffectInstance hoveredEffect;

    public EffectsInInventory(AbstractContainerScreen<?> abstractContainerScreen) {
        this.screen = abstractContainerScreen;
        this.minecraft = Minecraft.getInstance();
    }

    public boolean canSeeEffects() {
        int n = this.screen.leftPos + this.screen.imageWidth + 2;
        int n2 = this.screen.width - n;
        return n2 >= 32;
    }

    public void renderEffects(GuiGraphics guiGraphics, int n, int n2) {
        this.hoveredEffect = null;
        int n3 = this.screen.leftPos + this.screen.imageWidth + 2;
        int n4 = this.screen.width - n3;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty() || n4 < 32) {
            return;
        }
        boolean bl = n4 >= 120;
        int n5 = 33;
        if (collection.size() > 5) {
            n5 = 132 / (collection.size() - 1);
        }
        List list = Ordering.natural().sortedCopy(collection);
        this.renderBackgrounds(guiGraphics, n3, n5, list, bl);
        this.renderIcons(guiGraphics, n3, n5, list, bl);
        if (bl) {
            this.renderLabels(guiGraphics, n3, n5, list);
        } else if (n >= n3 && n <= n3 + 33) {
            int n6 = this.screen.topPos;
            for (MobEffectInstance mobEffectInstance : list) {
                if (n2 >= n6 && n2 <= n6 + n5) {
                    this.hoveredEffect = mobEffectInstance;
                }
                n6 += n5;
            }
        }
    }

    public void renderTooltip(GuiGraphics guiGraphics, int n, int n2) {
        if (this.hoveredEffect != null) {
            List<Component> list = List.of(this.getEffectName(this.hoveredEffect), MobEffectUtil.formatDuration(this.hoveredEffect, 1.0f, this.minecraft.level.tickRateManager().tickrate()));
            guiGraphics.setTooltipForNextFrame(this.screen.getFont(), list, Optional.empty(), n, n2);
        }
    }

    private void renderBackgrounds(GuiGraphics guiGraphics, int n, int n2, Iterable<MobEffectInstance> iterable, boolean bl) {
        int n3 = this.screen.topPos;
        for (MobEffectInstance mobEffectInstance : iterable) {
            if (bl) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_LARGE_SPRITE, n, n3, 120, 32);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SMALL_SPRITE, n, n3, 32, 32);
            }
            n3 += n2;
        }
    }

    private void renderIcons(GuiGraphics guiGraphics, int n, int n2, Iterable<MobEffectInstance> iterable, boolean bl) {
        int n3 = this.screen.topPos;
        for (MobEffectInstance mobEffectInstance : iterable) {
            Holder<MobEffect> holder = mobEffectInstance.getEffect();
            ResourceLocation resourceLocation = Gui.getMobEffectSprite(holder);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n + (bl ? 6 : 7), n3 + 7, 18, 18);
            n3 += n2;
        }
    }

    private void renderLabels(GuiGraphics guiGraphics, int n, int n2, Iterable<MobEffectInstance> iterable) {
        int n3 = this.screen.topPos;
        for (MobEffectInstance mobEffectInstance : iterable) {
            Component component = this.getEffectName(mobEffectInstance);
            guiGraphics.drawString(this.screen.getFont(), component, n + 10 + 18, n3 + 6, -1);
            Component component2 = MobEffectUtil.formatDuration(mobEffectInstance, 1.0f, this.minecraft.level.tickRateManager().tickrate());
            guiGraphics.drawString(this.screen.getFont(), component2, n + 10 + 18, n3 + 6 + 10, -8421505);
            n3 += n2;
        }
    }

    private Component getEffectName(MobEffectInstance mobEffectInstance) {
        MutableComponent mutableComponent = mobEffectInstance.getEffect().value().getDisplayName().copy();
        if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
            mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
        }
        return mutableComponent;
    }
}

