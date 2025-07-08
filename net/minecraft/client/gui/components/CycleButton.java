/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class CycleButton<T>
extends AbstractButton {
    public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of((Object)Boolean.TRUE, (Object)Boolean.FALSE);
    private final Component name;
    private int index;
    private T value;
    private final ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<CycleButton<T>, MutableComponent> narrationProvider;
    private final OnValueChange<T> onValueChange;
    private final boolean displayOnlyValue;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

    CycleButton(int n, int n2, int n3, int n4, Component component, Component component2, int n5, T t, ValueListSupplier<T> valueListSupplier, Function<T, Component> function, Function<CycleButton<T>, MutableComponent> function2, OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean bl) {
        super(n, n2, n3, n4, component);
        this.name = component2;
        this.index = n5;
        this.value = t;
        this.values = valueListSupplier;
        this.valueStringifier = function;
        this.narrationProvider = function2;
        this.onValueChange = onValueChange;
        this.displayOnlyValue = bl;
        this.tooltipSupplier = tooltipSupplier;
        this.updateTooltip();
    }

    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }
    }

    private void cycleValue(int n) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + n, list.size());
        T t = list.get(this.index);
        this.updateValue(t);
        this.onValueChange.onValueChange(this, t);
    }

    private T getCycledValue(int n) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + n, list.size()));
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (d4 > 0.0) {
            this.cycleValue(-1);
        } else if (d4 < 0.0) {
            this.cycleValue(1);
        }
        return true;
    }

    public void setValue(T t) {
        List<T> list = this.values.getSelectedList();
        int n = list.indexOf(t);
        if (n != -1) {
            this.index = n;
        }
        this.updateValue(t);
    }

    private void updateValue(T t) {
        Component component = this.createLabelForValue(t);
        this.setMessage(component);
        this.value = t;
        this.updateTooltip();
    }

    private Component createLabelForValue(T t) {
        return this.displayOnlyValue ? this.valueStringifier.apply(t) : this.createFullName(t);
    }

    private MutableComponent createFullName(T t) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(t));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            T t = this.getCycledValue(1);
            Component component = this.createLabelForValue(t);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return CycleButton.wrapDefaultNarrationMessage(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage());
    }

    public static <T> Builder<T> builder(Function<T, Component> function) {
        return new Builder<T>(function);
    }

    public static Builder<Boolean> booleanBuilder(Component component, Component component2) {
        return new Builder<Boolean>(bl -> bl != false ? component : component2).withValues((Collection<Boolean>)BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder() {
        return new Builder<Boolean>(bl -> bl != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).withValues((Collection<Boolean>)BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder(boolean bl) {
        return CycleButton.onOffBuilder().withInitialValue(bl);
    }

    public static interface ValueListSupplier<T> {
        public List<T> getSelectedList();

        public List<T> getDefaultList();

        public static <T> ValueListSupplier<T> create(Collection<T> collection) {
            ImmutableList immutableList = ImmutableList.copyOf(collection);
            return new ValueListSupplier<T>((List)immutableList){
                final /* synthetic */ List val$copy;
                {
                    this.val$copy = list;
                }

                @Override
                public List<T> getSelectedList() {
                    return this.val$copy;
                }

                @Override
                public List<T> getDefaultList() {
                    return this.val$copy;
                }
            };
        }

        public static <T> ValueListSupplier<T> create(final BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
            ImmutableList immutableList = ImmutableList.copyOf(list);
            ImmutableList immutableList2 = ImmutableList.copyOf(list2);
            return new ValueListSupplier<T>((List)immutableList2, (List)immutableList){
                final /* synthetic */ List val$altCopy;
                final /* synthetic */ List val$defaultCopy;
                {
                    this.val$altCopy = list;
                    this.val$defaultCopy = list2;
                }

                @Override
                public List<T> getSelectedList() {
                    return booleanSupplier.getAsBoolean() ? this.val$altCopy : this.val$defaultCopy;
                }

                @Override
                public List<T> getDefaultList() {
                    return this.val$defaultCopy;
                }
            };
        }
    }

    @FunctionalInterface
    public static interface OnValueChange<T> {
        public void onValueChange(CycleButton<T> var1, T var2);
    }

    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = object -> null;
        private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
        private ValueListSupplier<T> values = ValueListSupplier.create(ImmutableList.of());
        private boolean displayOnlyValue;

        public Builder(Function<T, Component> function) {
            this.valueStringifier = function;
        }

        public Builder<T> withValues(Collection<T> collection) {
            return this.withValues(ValueListSupplier.create(collection));
        }

        @SafeVarargs
        public final Builder<T> withValues(T ... TArray) {
            return this.withValues((Collection<T>)ImmutableList.copyOf((Object[])TArray));
        }

        public Builder<T> withValues(List<T> list, List<T> list2) {
            return this.withValues(ValueListSupplier.create(DEFAULT_ALT_LIST_SELECTOR, list, list2));
        }

        public Builder<T> withValues(BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
            return this.withValues(ValueListSupplier.create(booleanSupplier, list, list2));
        }

        public Builder<T> withValues(ValueListSupplier<T> valueListSupplier) {
            this.values = valueListSupplier;
            return this;
        }

        public Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        public Builder<T> withInitialValue(T t) {
            this.initialValue = t;
            int n = this.values.getDefaultList().indexOf(t);
            if (n != -1) {
                this.initialIndex = n;
            }
            return this;
        }

        public Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> function) {
            this.narrationProvider = function;
            return this;
        }

        public Builder<T> displayOnlyValue(boolean bl) {
            this.displayOnlyValue = bl;
            return this;
        }

        public Builder<T> displayOnlyValue() {
            return this.displayOnlyValue(true);
        }

        public CycleButton<T> create(Component component, OnValueChange<T> onValueChange) {
            return this.create(0, 0, 150, 20, component, onValueChange);
        }

        public CycleButton<T> create(int n, int n2, int n3, int n4, Component component) {
            return this.create(n, n2, n3, n4, component, (cycleButton, object) -> {});
        }

        public CycleButton<T> create(int n, int n2, int n3, int n4, Component component, OnValueChange<T> onValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            }
            T t = this.initialValue != null ? this.initialValue : list.get(this.initialIndex);
            Component component2 = this.valueStringifier.apply(t);
            Component component3 = this.displayOnlyValue ? component2 : CommonComponents.optionNameValue(component, component2);
            return new CycleButton<T>(n, n2, n3, n4, component3, component, this.initialIndex, t, this.values, this.valueStringifier, this.narrationProvider, onValueChange, this.tooltipSupplier, this.displayOnlyValue);
        }
    }
}

