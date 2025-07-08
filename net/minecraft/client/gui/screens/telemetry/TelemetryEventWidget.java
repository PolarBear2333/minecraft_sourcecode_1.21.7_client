/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.telemetry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.DoubleConsumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TelemetryEventWidget
extends AbstractTextAreaWidget {
    private static final int HEADER_HORIZONTAL_PADDING = 32;
    private static final String TELEMETRY_REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
    private static final String TELEMETRY_OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
    private static final String TELEMETRY_OPTIONAL_DISABLED_TRANSLATION_KEY = "telemetry.event.optional.disabled";
    private static final Component PROPERTY_TITLE = Component.translatable("telemetry_info.property_title").withStyle(ChatFormatting.UNDERLINE);
    private final Font font;
    private Content content;
    @Nullable
    private DoubleConsumer onScrolledListener;

    public TelemetryEventWidget(int n, int n2, int n3, int n4, Font font) {
        super(n, n2, n3, n4, Component.empty());
        this.font = font;
        this.content = this.buildContent(Minecraft.getInstance().telemetryOptInExtra());
    }

    public void onOptInChanged(boolean bl) {
        this.content = this.buildContent(bl);
        this.refreshScrollAmount();
    }

    public void updateLayout() {
        this.content = this.buildContent(Minecraft.getInstance().telemetryOptInExtra());
        this.refreshScrollAmount();
    }

    private Content buildContent(boolean bl) {
        ContentBuilder contentBuilder = new ContentBuilder(this.containerWidth());
        ArrayList<TelemetryEventType> arrayList = new ArrayList<TelemetryEventType>(TelemetryEventType.values());
        arrayList.sort(Comparator.comparing(TelemetryEventType::isOptIn));
        for (int i = 0; i < arrayList.size(); ++i) {
            TelemetryEventType telemetryEventType = (TelemetryEventType)arrayList.get(i);
            boolean bl2 = telemetryEventType.isOptIn() && !bl;
            this.addEventType(contentBuilder, telemetryEventType, bl2);
            if (i >= arrayList.size() - 1) continue;
            contentBuilder.addSpacer(this.font.lineHeight);
        }
        return contentBuilder.build();
    }

    public void setOnScrolledListener(@Nullable DoubleConsumer doubleConsumer) {
        this.onScrolledListener = doubleConsumer;
    }

    @Override
    public void setScrollAmount(double d) {
        super.setScrollAmount(d);
        if (this.onScrolledListener != null) {
            this.onScrolledListener.accept(this.scrollAmount());
        }
    }

    @Override
    protected int getInnerHeight() {
        return this.content.container().getHeight();
    }

    @Override
    protected double scrollRate() {
        return this.font.lineHeight;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = this.getInnerTop();
        int n4 = this.getInnerLeft();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)n4, (float)n3);
        this.content.container().visitWidgets(abstractWidget -> abstractWidget.render(guiGraphics, n, n2, f));
        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.content.narration());
    }

    private Component grayOutIfDisabled(Component component, boolean bl) {
        if (bl) {
            return component.copy().withStyle(ChatFormatting.GRAY);
        }
        return component;
    }

    private void addEventType(ContentBuilder contentBuilder, TelemetryEventType telemetryEventType, boolean bl) {
        String string = telemetryEventType.isOptIn() ? (bl ? TELEMETRY_OPTIONAL_DISABLED_TRANSLATION_KEY : TELEMETRY_OPTIONAL_TRANSLATION_KEY) : TELEMETRY_REQUIRED_TRANSLATION_KEY;
        contentBuilder.addHeader(this.font, this.grayOutIfDisabled(Component.translatable(string, telemetryEventType.title()), bl));
        contentBuilder.addHeader(this.font, telemetryEventType.description().withStyle(ChatFormatting.GRAY));
        contentBuilder.addSpacer(this.font.lineHeight / 2);
        contentBuilder.addLine(this.font, this.grayOutIfDisabled(PROPERTY_TITLE, bl), 2);
        this.addEventTypeProperties(telemetryEventType, contentBuilder, bl);
    }

    private void addEventTypeProperties(TelemetryEventType telemetryEventType, ContentBuilder contentBuilder, boolean bl) {
        for (TelemetryProperty<?> telemetryProperty : telemetryEventType.properties()) {
            contentBuilder.addLine(this.font, this.grayOutIfDisabled(telemetryProperty.title(), bl));
        }
    }

    private int containerWidth() {
        return this.width - this.totalInnerPadding();
    }

    record Content(Layout container, Component narration) {
    }

    static class ContentBuilder {
        private final int width;
        private final LinearLayout layout;
        private final MutableComponent narration = Component.empty();

        public ContentBuilder(int n) {
            this.width = n;
            this.layout = LinearLayout.vertical();
            this.layout.defaultCellSetting().alignHorizontallyLeft();
            this.layout.addChild(SpacerElement.width(n));
        }

        public void addLine(Font font, Component component) {
            this.addLine(font, component, 0);
        }

        public void addLine(Font font, Component component, int n) {
            this.layout.addChild(new MultiLineTextWidget(component, font).setMaxWidth(this.width), layoutSettings -> layoutSettings.paddingBottom(n));
            this.narration.append(component).append("\n");
        }

        public void addHeader(Font font, Component component) {
            this.layout.addChild(new MultiLineTextWidget(component, font).setMaxWidth(this.width - 64).setCentered(true), layoutSettings -> layoutSettings.alignHorizontallyCenter().paddingHorizontal(32));
            this.narration.append(component).append("\n");
        }

        public void addSpacer(int n) {
            this.layout.addChild(SpacerElement.height(n));
        }

        public Content build() {
            this.layout.arrangeElements();
            return new Content(this.layout, this.narration);
        }
    }
}

