/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.options.controls;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeyBindsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("controls.keybinds.title");
    @Nullable
    public KeyMapping selectedKey;
    public long lastKeySelection;
    private KeyBindsList keyBindsList;
    private Button resetButton;

    public KeyBindsScreen(Screen screen, Options options) {
        super(screen, options, TITLE);
    }

    @Override
    protected void addContents() {
        this.keyBindsList = this.layout.addToContents(new KeyBindsList(this, this.minecraft));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        this.resetButton = Button.builder(Component.translatable("controls.resetAll"), button -> {
            for (KeyMapping keyMapping : this.options.keyMappings) {
                keyMapping.setKey(keyMapping.getDefaultKey());
            }
            this.keyBindsList.resetMappingAndUpdateButtons();
        }).build();
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout.addChild(this.resetButton);
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.keyBindsList.updateSize(this.width, this.layout);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.selectedKey != null) {
            this.selectedKey.setKey(InputConstants.Type.MOUSE.getOrCreate(n));
            this.selectedKey = null;
            this.keyBindsList.resetMappingAndUpdateButtons();
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.selectedKey != null) {
            if (n == 256) {
                this.selectedKey.setKey(InputConstants.UNKNOWN);
            } else {
                this.selectedKey.setKey(InputConstants.getKey(n, n2));
            }
            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            this.keyBindsList.resetMappingAndUpdateButtons();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        boolean bl = false;
        for (KeyMapping keyMapping : this.options.keyMappings) {
            if (keyMapping.isDefault()) continue;
            bl = true;
            break;
        }
        this.resetButton.active = bl;
    }
}

