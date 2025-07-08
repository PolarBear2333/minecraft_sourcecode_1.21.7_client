/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.realmsclient.dto.RealmsRegion;
import com.mojang.realmsclient.dto.RegionSelectionPreference;
import com.mojang.realmsclient.dto.ServiceQuality;
import com.mojang.realmsclient.gui.screens.configuration.RealmsSettingsTab;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class RealmsPreferredRegionSelectionScreen
extends Screen {
    private static final Component REGION_SELECTION_LABEL = Component.translatable("mco.configure.world.region_preference.title");
    private static final int SPACING = 8;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final BiConsumer<RegionSelectionPreference, RealmsRegion> applySettings;
    final Map<RealmsRegion, ServiceQuality> regionServiceQuality;
    @Nullable
    private RegionSelectionList list;
    RealmsSettingsTab.RegionSelection selection;
    @Nullable
    private Button doneButton;

    public RealmsPreferredRegionSelectionScreen(Screen screen, BiConsumer<RegionSelectionPreference, RealmsRegion> biConsumer, Map<RealmsRegion, ServiceQuality> map, RealmsSettingsTab.RegionSelection regionSelection) {
        super(REGION_SELECTION_LABEL);
        this.parent = screen;
        this.applySettings = biConsumer;
        this.regionServiceQuality = map;
        this.selection = regionSelection;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        linearLayout.addChild(new StringWidget(this.getTitle(), this.font));
        this.list = this.layout.addToContents(new RegionSelectionList());
        LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.doneButton = linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.selection.preference(), this.selection.region());
            this.onClose();
        }).build());
        linearLayout2.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.list.setSelected((RegionSelectionList.Entry)this.list.children().stream().filter(entry -> Objects.equals(entry.regionSelection, this.selection)).findFirst().orElse(null));
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.list.updateSize(this.width, this.layout);
    }

    void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    class RegionSelectionList
    extends ObjectSelectionList<Entry> {
        RegionSelectionList() {
            super(RealmsPreferredRegionSelectionScreen.this.minecraft, RealmsPreferredRegionSelectionScreen.this.width, RealmsPreferredRegionSelectionScreen.this.height - 77, 40, 16);
            this.addEntry(new Entry(RegionSelectionPreference.AUTOMATIC_PLAYER, null));
            this.addEntry(new Entry(RegionSelectionPreference.AUTOMATIC_OWNER, null));
            RealmsPreferredRegionSelectionScreen.this.regionServiceQuality.keySet().stream().map(realmsRegion -> new Entry(RegionSelectionPreference.MANUAL, (RealmsRegion)((Object)realmsRegion))).forEach(entry -> this.addEntry(entry));
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                RealmsPreferredRegionSelectionScreen.this.selection = entry.regionSelection;
            }
            RealmsPreferredRegionSelectionScreen.this.updateButtonValidity();
        }

        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final RealmsSettingsTab.RegionSelection regionSelection;
            private final Component name;

            public Entry(@Nullable RegionSelectionPreference regionSelectionPreference, RealmsRegion realmsRegion) {
                this(new RealmsSettingsTab.RegionSelection(regionSelectionPreference, realmsRegion));
            }

            public Entry(RealmsSettingsTab.RegionSelection regionSelection) {
                this.regionSelection = regionSelection;
                this.name = regionSelection.preference() == RegionSelectionPreference.MANUAL ? (regionSelection.region() != null ? Component.translatable(regionSelection.region().translationKey) : Component.empty()) : Component.translatable(regionSelection.preference().translationKey);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                guiGraphics.drawString(RealmsPreferredRegionSelectionScreen.this.font, this.name, n3 + 5, n2 + 2, -1);
                if (this.regionSelection.region() != null && RealmsPreferredRegionSelectionScreen.this.regionServiceQuality.containsKey((Object)this.regionSelection.region())) {
                    ServiceQuality serviceQuality = RealmsPreferredRegionSelectionScreen.this.regionServiceQuality.getOrDefault((Object)this.regionSelection.region(), ServiceQuality.UNKNOWN);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, serviceQuality.getIcon(), n3 + n4 - 18, n2 + 2, 10, 8);
                }
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                RegionSelectionList.this.setSelected(this);
                return super.mouseClicked(d, d2, n);
            }
        }
    }
}

