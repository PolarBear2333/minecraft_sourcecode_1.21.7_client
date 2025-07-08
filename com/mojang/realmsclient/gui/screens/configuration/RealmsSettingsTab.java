/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.realmsclient.dto.RealmsRegion;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RegionSelectionPreference;
import com.mojang.realmsclient.dto.RegionSelectionPreferenceDto;
import com.mojang.realmsclient.dto.ServiceQuality;
import com.mojang.realmsclient.gui.screens.RealmsPopups;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigurationTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsPreferredRegionSelectionScreen;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class RealmsSettingsTab
extends GridLayoutTab
implements RealmsConfigurationTab {
    private static final int COMPONENT_WIDTH = 212;
    private static final int EXTRA_SPACING = 2;
    private static final int DEFAULT_SPACING = 6;
    static final Component TITLE = Component.translatable("mco.configure.world.settings.title");
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private static final Component REGION_PREFERENCE_LABEL = Component.translatable("mco.configure.world.region_preference");
    private final RealmsConfigureWorldScreen configurationScreen;
    private final Minecraft minecraft;
    private RealmsServer serverData;
    private final Map<RealmsRegion, ServiceQuality> regionServiceQuality;
    final Button closeOpenButton;
    private EditBox descEdit;
    private EditBox nameEdit;
    private final StringWidget selectedRegionStringWidget;
    private final ImageWidget selectedRegionImageWidget;
    private RegionSelection preferredRegionSelection;

    RealmsSettingsTab(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Minecraft minecraft, RealmsServer realmsServer, Map<RealmsRegion, ServiceQuality> map) {
        super(TITLE);
        this.configurationScreen = realmsConfigureWorldScreen;
        this.minecraft = minecraft;
        this.serverData = realmsServer;
        this.regionServiceQuality = map;
        GridLayout.RowHelper rowHelper = this.layout.rowSpacing(6).createRowHelper(1);
        rowHelper.addChild(new StringWidget(NAME_LABEL, realmsConfigureWorldScreen.getFont()));
        this.nameEdit = new EditBox(minecraft.font, 0, 0, 212, 20, Component.translatable("mco.configure.world.name"));
        this.nameEdit.setMaxLength(32);
        rowHelper.addChild(this.nameEdit);
        rowHelper.addChild(SpacerElement.height(2));
        rowHelper.addChild(new StringWidget(DESCRIPTION_LABEL, realmsConfigureWorldScreen.getFont()));
        this.descEdit = new EditBox(minecraft.font, 0, 0, 212, 20, Component.translatable("mco.configure.world.description"));
        this.descEdit.setMaxLength(32);
        rowHelper.addChild(this.descEdit);
        rowHelper.addChild(SpacerElement.height(2));
        rowHelper.addChild(new StringWidget(REGION_PREFERENCE_LABEL, realmsConfigureWorldScreen.getFont()));
        EqualSpacingLayout equalSpacingLayout = new EqualSpacingLayout(0, 0, 212, realmsConfigureWorldScreen.getFont().lineHeight, EqualSpacingLayout.Orientation.HORIZONTAL);
        this.selectedRegionStringWidget = equalSpacingLayout.addChild(new StringWidget(192, realmsConfigureWorldScreen.getFont().lineHeight, Component.empty(), realmsConfigureWorldScreen.getFont()).alignLeft());
        this.selectedRegionImageWidget = equalSpacingLayout.addChild(ImageWidget.sprite(10, 8, ServiceQuality.UNKNOWN.getIcon()));
        rowHelper.addChild(equalSpacingLayout);
        rowHelper.addChild(Button.builder(Component.translatable("mco.configure.world.buttons.region_preference"), button -> this.openPreferenceSelector()).bounds(0, 0, 212, 20).build());
        rowHelper.addChild(SpacerElement.height(2));
        this.closeOpenButton = rowHelper.addChild(Button.builder(Component.empty(), button -> {
            if (realmsServer.state == RealmsServer.State.OPEN) {
                minecraft.setScreen(RealmsPopups.customPopupScreen(realmsConfigureWorldScreen, Component.translatable("mco.configure.world.close.question.title"), Component.translatable("mco.configure.world.close.question.line1"), popupScreen -> {
                    this.save();
                    realmsConfigureWorldScreen.closeTheWorld();
                }));
            } else {
                this.save();
                realmsConfigureWorldScreen.openTheWorld(false);
            }
        }).bounds(0, 0, 212, 20).build());
        this.closeOpenButton.active = false;
        this.updateData(realmsServer);
    }

    private static MutableComponent getTranslatableFromPreference(RegionSelection regionSelection) {
        return (regionSelection.preference().equals((Object)RegionSelectionPreference.MANUAL) && regionSelection.region() != null ? Component.translatable(regionSelection.region().translationKey) : Component.translatable(regionSelection.preference().translationKey)).withStyle(ChatFormatting.GRAY);
    }

    private static ResourceLocation getServiceQualityIcon(RegionSelection regionSelection, Map<RealmsRegion, ServiceQuality> map) {
        if (regionSelection.region() != null && map.containsKey((Object)regionSelection.region())) {
            ServiceQuality serviceQuality = map.getOrDefault((Object)regionSelection.region(), ServiceQuality.UNKNOWN);
            return serviceQuality.getIcon();
        }
        return ServiceQuality.UNKNOWN.getIcon();
    }

    private void openPreferenceSelector() {
        this.minecraft.setScreen(new RealmsPreferredRegionSelectionScreen(this.configurationScreen, this::applyRegionPreferenceSelection, this.regionServiceQuality, this.preferredRegionSelection));
    }

    private void applyRegionPreferenceSelection(RegionSelectionPreference regionSelectionPreference, RealmsRegion realmsRegion) {
        this.preferredRegionSelection = new RegionSelection(regionSelectionPreference, realmsRegion);
        this.updateRegionPreferenceValues();
    }

    private void updateRegionPreferenceValues() {
        this.selectedRegionStringWidget.setMessage(RealmsSettingsTab.getTranslatableFromPreference(this.preferredRegionSelection));
        this.selectedRegionImageWidget.updateResource(RealmsSettingsTab.getServiceQualityIcon(this.preferredRegionSelection, this.regionServiceQuality));
        this.selectedRegionImageWidget.visible = this.preferredRegionSelection.preference == RegionSelectionPreference.MANUAL;
    }

    @Override
    public void onSelected(RealmsServer realmsServer) {
        this.updateData(realmsServer);
    }

    @Override
    public void updateData(RealmsServer realmsServer) {
        Object object;
        this.serverData = realmsServer;
        if (realmsServer.regionSelectionPreference == null) {
            realmsServer.regionSelectionPreference = RegionSelectionPreferenceDto.DEFAULT;
        }
        if (realmsServer.regionSelectionPreference.regionSelectionPreference == RegionSelectionPreference.MANUAL && realmsServer.regionSelectionPreference.preferredRegion == null) {
            object = this.regionServiceQuality.keySet().stream().findFirst();
            ((Optional)object).ifPresent(realmsRegion -> {
                realmsServer.regionSelectionPreference.preferredRegion = realmsRegion;
            });
        }
        object = realmsServer.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
        this.closeOpenButton.setMessage(Component.translatable((String)object));
        this.closeOpenButton.active = true;
        this.preferredRegionSelection = new RegionSelection(realmsServer.regionSelectionPreference.regionSelectionPreference, realmsServer.regionSelectionPreference.preferredRegion);
        this.nameEdit.setValue(Objects.requireNonNullElse(realmsServer.getName(), ""));
        this.descEdit.setValue(realmsServer.getDescription());
        this.updateRegionPreferenceValues();
    }

    @Override
    public void onDeselected(RealmsServer realmsServer) {
        this.save();
    }

    public void save() {
        if (this.serverData.regionSelectionPreference != null && Objects.equals(this.nameEdit.getValue(), this.serverData.name) && Objects.equals(this.descEdit.getValue(), this.serverData.motd) && this.preferredRegionSelection.preference() == this.serverData.regionSelectionPreference.regionSelectionPreference && this.preferredRegionSelection.region() == this.serverData.regionSelectionPreference.preferredRegion) {
            return;
        }
        this.configurationScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue(), this.preferredRegionSelection.preference(), this.preferredRegionSelection.region());
    }

    public static final class RegionSelection
    extends Record {
        final RegionSelectionPreference preference;
        @Nullable
        private final RealmsRegion region;

        public RegionSelection(RegionSelectionPreference regionSelectionPreference, @Nullable RealmsRegion realmsRegion) {
            this.preference = regionSelectionPreference;
            this.region = realmsRegion;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegionSelection.class, "preference;region", "preference", "region"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegionSelection.class, "preference;region", "preference", "region"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegionSelection.class, "preference;region", "preference", "region"}, this, object);
        }

        public RegionSelectionPreference preference() {
            return this.preference;
        }

        @Nullable
        public RealmsRegion region() {
            return this.region;
        }
    }
}

