/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

class SwitchGrid {
    private static final int DEFAULT_SWITCH_BUTTON_WIDTH = 44;
    private final List<LabeledSwitch> switches;
    private final Layout layout;

    SwitchGrid(List<LabeledSwitch> list, Layout layout) {
        this.switches = list;
        this.layout = layout;
    }

    public Layout layout() {
        return this.layout;
    }

    public void refreshStates() {
        this.switches.forEach(LabeledSwitch::refreshState);
    }

    public static Builder builder(int n) {
        return new Builder(n);
    }

    public static class Builder {
        final int width;
        private final List<SwitchBuilder> switchBuilders = new ArrayList<SwitchBuilder>();
        int paddingLeft;
        int rowSpacing = 4;
        int rowCount;
        Optional<InfoUnderneathSettings> infoUnderneath = Optional.empty();

        public Builder(int n) {
            this.width = n;
        }

        void increaseRow() {
            ++this.rowCount;
        }

        public SwitchBuilder addSwitch(Component component, BooleanSupplier booleanSupplier, Consumer<Boolean> consumer) {
            SwitchBuilder switchBuilder = new SwitchBuilder(component, booleanSupplier, consumer, 44);
            this.switchBuilders.add(switchBuilder);
            return switchBuilder;
        }

        public Builder withPaddingLeft(int n) {
            this.paddingLeft = n;
            return this;
        }

        public Builder withRowSpacing(int n) {
            this.rowSpacing = n;
            return this;
        }

        public SwitchGrid build() {
            GridLayout gridLayout = new GridLayout().rowSpacing(this.rowSpacing);
            gridLayout.addChild(SpacerElement.width(this.width - 44), 0, 0);
            gridLayout.addChild(SpacerElement.width(44), 0, 1);
            ArrayList<LabeledSwitch> arrayList = new ArrayList<LabeledSwitch>();
            this.rowCount = 0;
            for (SwitchBuilder switchBuilder : this.switchBuilders) {
                arrayList.add(switchBuilder.build(this, gridLayout, 0));
            }
            gridLayout.arrangeElements();
            SwitchGrid switchGrid = new SwitchGrid(arrayList, gridLayout);
            switchGrid.refreshStates();
            return switchGrid;
        }

        public Builder withInfoUnderneath(int n, boolean bl) {
            this.infoUnderneath = Optional.of(new InfoUnderneathSettings(n, bl));
            return this;
        }
    }

    static final class InfoUnderneathSettings
    extends Record {
        final int maxInfoRows;
        final boolean alwaysMaxHeight;

        InfoUnderneathSettings(int n, boolean bl) {
            this.maxInfoRows = n;
            this.alwaysMaxHeight = bl;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InfoUnderneathSettings.class, "maxInfoRows;alwaysMaxHeight", "maxInfoRows", "alwaysMaxHeight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InfoUnderneathSettings.class, "maxInfoRows;alwaysMaxHeight", "maxInfoRows", "alwaysMaxHeight"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InfoUnderneathSettings.class, "maxInfoRows;alwaysMaxHeight", "maxInfoRows", "alwaysMaxHeight"}, this, object);
        }

        public int maxInfoRows() {
            return this.maxInfoRows;
        }

        public boolean alwaysMaxHeight() {
            return this.alwaysMaxHeight;
        }
    }

    record LabeledSwitch(CycleButton<Boolean> button, BooleanSupplier stateSupplier, @Nullable BooleanSupplier isActiveCondition) {
        public void refreshState() {
            this.button.setValue(this.stateSupplier.getAsBoolean());
            if (this.isActiveCondition != null) {
                this.button.active = this.isActiveCondition.getAsBoolean();
            }
        }
    }

    public static class SwitchBuilder {
        private final Component label;
        private final BooleanSupplier stateSupplier;
        private final Consumer<Boolean> onClicked;
        @Nullable
        private Component info;
        @Nullable
        private BooleanSupplier isActiveCondition;
        private final int buttonWidth;

        SwitchBuilder(Component component, BooleanSupplier booleanSupplier, Consumer<Boolean> consumer, int n) {
            this.label = component;
            this.stateSupplier = booleanSupplier;
            this.onClicked = consumer;
            this.buttonWidth = n;
        }

        public SwitchBuilder withIsActiveCondition(BooleanSupplier booleanSupplier) {
            this.isActiveCondition = booleanSupplier;
            return this;
        }

        public SwitchBuilder withInfo(Component component) {
            this.info = component;
            return this;
        }

        LabeledSwitch build(Builder builder, GridLayout gridLayout, int n) {
            NarrationSupplier narrationSupplier;
            boolean bl2;
            builder.increaseRow();
            StringWidget stringWidget = new StringWidget(this.label, Minecraft.getInstance().font).alignLeft();
            gridLayout.addChild(stringWidget, builder.rowCount, n, gridLayout.newCellSettings().align(0.0f, 0.5f).paddingLeft(builder.paddingLeft));
            Optional<InfoUnderneathSettings> optional = builder.infoUnderneath;
            CycleButton.Builder<Boolean> builder2 = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
            builder2.displayOnlyValue();
            boolean bl3 = bl2 = this.info != null && optional.isEmpty();
            if (bl2) {
                narrationSupplier = Tooltip.create(this.info);
                builder2.withTooltip(arg_0 -> SwitchBuilder.lambda$build$0((Tooltip)narrationSupplier, arg_0));
            }
            if (this.info != null && !bl2) {
                builder2.withCustomNarration(cycleButton -> CommonComponents.joinForNarration(this.label, cycleButton.createDefaultNarrationMessage(), this.info));
            } else {
                builder2.withCustomNarration(cycleButton -> CommonComponents.joinForNarration(this.label, cycleButton.createDefaultNarrationMessage()));
            }
            narrationSupplier = builder2.create(0, 0, this.buttonWidth, 20, Component.empty(), (cycleButton, bl) -> this.onClicked.accept((Boolean)bl));
            if (this.isActiveCondition != null) {
                ((CycleButton)narrationSupplier).active = this.isActiveCondition.getAsBoolean();
            }
            gridLayout.addChild(narrationSupplier, builder.rowCount, n + 1, gridLayout.newCellSettings().alignHorizontallyRight());
            if (this.info != null) {
                optional.ifPresent(infoUnderneathSettings -> {
                    MutableComponent mutableComponent = this.info.copy().withStyle(ChatFormatting.GRAY);
                    Font font = Minecraft.getInstance().font;
                    MultiLineTextWidget multiLineTextWidget = new MultiLineTextWidget(mutableComponent, font);
                    multiLineTextWidget.setMaxWidth(builder.width - builder.paddingLeft - this.buttonWidth);
                    multiLineTextWidget.setMaxRows(infoUnderneathSettings.maxInfoRows());
                    builder.increaseRow();
                    int n2 = infoUnderneathSettings.alwaysMaxHeight ? font.lineHeight * infoUnderneathSettings.maxInfoRows - multiLineTextWidget.getHeight() : 0;
                    gridLayout.addChild(multiLineTextWidget, builder.rowCount, n, gridLayout.newCellSettings().paddingTop(-builder.rowSpacing).paddingBottom(n2));
                });
            }
            return new LabeledSwitch((CycleButton<Boolean>)narrationSupplier, this.stateSupplier, this.isActiveCondition);
        }

        private static /* synthetic */ Tooltip lambda$build$0(Tooltip tooltip, Boolean bl) {
            return tooltip;
        }
    }
}

