/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.options;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.FontOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class LanguageSelectScreen
extends OptionsSubScreen {
    private static final Component WARNING_LABEL = Component.translatable("options.languageAccuracyWarning").withColor(-4539718);
    private static final int FOOTER_HEIGHT = 53;
    private LanguageSelectionList languageSelectionList;
    final LanguageManager languageManager;

    public LanguageSelectScreen(Screen screen, Options options, LanguageManager languageManager) {
        super(screen, options, Component.translatable("options.language.title"));
        this.languageManager = languageManager;
        this.layout.setFooterHeight(53);
    }

    @Override
    protected void addContents() {
        this.languageSelectionList = this.layout.addToContents(new LanguageSelectionList(this.minecraft));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.vertical()).spacing(8);
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        linearLayout.addChild(new StringWidget(WARNING_LABEL, this.font));
        LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal().spacing(8));
        linearLayout2.addChild(Button.builder(Component.translatable("options.font"), button -> this.minecraft.setScreen(new FontOptionsScreen(this, this.options))).build());
        linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.languageSelectionList.updateSize(this.width, this.layout);
    }

    void onDone() {
        LanguageSelectionList.Entry entry = (LanguageSelectionList.Entry)this.languageSelectionList.getSelected();
        if (entry != null && !entry.code.equals(this.languageManager.getSelected())) {
            this.languageManager.setSelected(entry.code);
            this.options.languageCode = entry.code;
            this.minecraft.reloadResourcePacks();
        }
        this.minecraft.setScreen(this.lastScreen);
    }

    class LanguageSelectionList
    extends ObjectSelectionList<Entry> {
        public LanguageSelectionList(Minecraft minecraft) {
            super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height - 33 - 53, 33, 18);
            String string = LanguageSelectScreen.this.languageManager.getSelected();
            LanguageSelectScreen.this.languageManager.getLanguages().forEach((string2, languageInfo) -> {
                Entry entry = new Entry((String)string2, (LanguageInfo)languageInfo);
                this.addEntry(entry);
                if (string.equals(string2)) {
                    this.setSelected(entry);
                }
            });
            if (this.getSelected() != null) {
                this.centerScrollOn((Entry)this.getSelected());
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final String code;
            private final Component language;
            private long lastClickTime;

            public Entry(String string, LanguageInfo languageInfo) {
                this.code = string;
                this.language = languageInfo.toComponent();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                guiGraphics.drawCenteredString(LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, n2 + n5 / 2 - ((LanguageSelectScreen)LanguageSelectScreen.this).font.lineHeight / 2, -1);
            }

            @Override
            public boolean keyPressed(int n, int n2, int n3) {
                if (CommonInputs.selected(n)) {
                    this.select();
                    LanguageSelectScreen.this.onDone();
                    return true;
                }
                return super.keyPressed(n, n2, n3);
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                this.select();
                if (Util.getMillis() - this.lastClickTime < 250L) {
                    LanguageSelectScreen.this.onDone();
                }
                this.lastClickTime = Util.getMillis();
                return super.mouseClicked(d, d2, n);
            }

            private void select() {
                LanguageSelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.language);
            }
        }
    }
}

