/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import org.slf4j.Logger;

public final class OptionInstance<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Enum<Boolean> BOOLEAN_VALUES = new Enum(ImmutableList.of((Object)Boolean.TRUE, (Object)Boolean.FALSE), Codec.BOOL);
    public static final CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (component, bl) -> bl != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    private final TooltipSupplier<T> tooltip;
    final Function<T, Component> toString;
    private final ValueSet<T> values;
    private final Codec<T> codec;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    final Component caption;
    T value;

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl2) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl2, bl -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, boolean bl2) {
        return OptionInstance.createBoolean(string, tooltipSupplier, bl2, bl -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, tooltipSupplier, BOOLEAN_TO_STRING, bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, CaptionBasedToString<Boolean> captionBasedToString, boolean bl, Consumer<Boolean> consumer) {
        return new OptionInstance<Boolean>(string, tooltipSupplier, captionBasedToString, BOOLEAN_VALUES, bl, consumer);
    }

    public OptionInstance(String string, TooltipSupplier<T> tooltipSupplier, CaptionBasedToString<T> captionBasedToString, ValueSet<T> valueSet, T t, Consumer<T> consumer) {
        this(string, tooltipSupplier, captionBasedToString, valueSet, valueSet.codec(), t, consumer);
    }

    public OptionInstance(String string, TooltipSupplier<T> tooltipSupplier, CaptionBasedToString<T> captionBasedToString, ValueSet<T> valueSet, Codec<T> codec, T t, Consumer<T> consumer) {
        this.caption = Component.translatable(string);
        this.tooltip = tooltipSupplier;
        this.toString = object -> captionBasedToString.toString(this.caption, object);
        this.values = valueSet;
        this.codec = codec;
        this.initialValue = t;
        this.onValueUpdate = consumer;
        this.value = this.initialValue;
    }

    public static <T> TooltipSupplier<T> noTooltip() {
        return object -> null;
    }

    public static <T> TooltipSupplier<T> cachedConstantTooltip(Component component) {
        return object -> Tooltip.create(component);
    }

    public static <T extends OptionEnum> CaptionBasedToString<T> forOptionEnum() {
        return (component, optionEnum) -> optionEnum.getCaption();
    }

    public AbstractWidget createButton(Options options) {
        return this.createButton(options, 0, 0, 150);
    }

    public AbstractWidget createButton(Options options, int n, int n2, int n3) {
        return this.createButton(options, n, n2, n3, object -> {});
    }

    public AbstractWidget createButton(Options options, int n, int n2, int n3, Consumer<T> consumer) {
        return this.values.createButton(this.tooltip, options, n, n2, n3, consumer).apply(this);
    }

    public T get() {
        return this.value;
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public String toString() {
        return this.caption.getString();
    }

    public void set(T t) {
        Object object = this.values.validateValue(t).orElseGet(() -> {
            LOGGER.error("Illegal option value " + String.valueOf(t) + " for " + String.valueOf(this.caption));
            return this.initialValue;
        });
        if (!Minecraft.getInstance().isRunning()) {
            this.value = object;
            return;
        }
        if (!Objects.equals(this.value, object)) {
            this.value = object;
            this.onValueUpdate.accept(this.value);
        }
    }

    public ValueSet<T> values() {
        return this.values;
    }

    @FunctionalInterface
    public static interface TooltipSupplier<T> {
        @Nullable
        public Tooltip apply(T var1);
    }

    public static interface CaptionBasedToString<T> {
        public Component toString(Component var1, T var2);
    }

    public record Enum<T>(List<T> values, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public Optional<T> validateValue(T t) {
            return this.values.contains(t) ? Optional.of(t) : Optional.empty();
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.values);
        }
    }

    static interface ValueSet<T> {
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5, Consumer<T> var6);

        public Optional<T> validateValue(T var1);

        public Codec<T> codec();
    }

    public static enum UnitDouble implements SliderableValueSet<Double>
    {
        INSTANCE;


        @Override
        public Optional<Double> validateValue(Double d) {
            return d >= 0.0 && d <= 1.0 ? Optional.of(d) : Optional.empty();
        }

        @Override
        public double toSliderValue(Double d) {
            return d;
        }

        @Override
        public Double fromSliderValue(double d) {
            return d;
        }

        public <R> SliderableValueSet<R> xmap(final DoubleFunction<? extends R> doubleFunction, final ToDoubleFunction<? super R> toDoubleFunction) {
            return new SliderableValueSet<R>(){

                @Override
                public Optional<R> validateValue(R r) {
                    return this.validateValue(toDoubleFunction.applyAsDouble(r)).map(doubleFunction::apply);
                }

                @Override
                public double toSliderValue(R r) {
                    return this.toSliderValue(toDoubleFunction.applyAsDouble(r));
                }

                @Override
                public R fromSliderValue(double d) {
                    return doubleFunction.apply(this.fromSliderValue(d));
                }

                @Override
                public Codec<R> codec() {
                    return this.codec().xmap(doubleFunction::apply, toDoubleFunction::applyAsDouble);
                }
            };
        }

        @Override
        public Codec<Double> codec() {
            return Codec.withAlternative((Codec)Codec.doubleRange((double)0.0, (double)1.0), (Codec)Codec.BOOL, bl -> bl != false ? 1.0 : 0.0);
        }

        @Override
        public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }
    }

    public record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier, int encodableMaxInclusive) implements IntRangeBase,
    SliderableOrCyclableValueSet<Integer>
    {
        @Override
        public Optional<Integer> validateValue(Integer n) {
            return Optional.of(Mth.clamp(n, this.minInclusive(), this.maxInclusive()));
        }

        @Override
        public int maxInclusive() {
            return this.maxSupplier.getAsInt();
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.INT.validate(n -> {
                int n2 = this.encodableMaxInclusive + 1;
                if (n.compareTo(this.minInclusive) >= 0 && n.compareTo(n2) <= 0) {
                    return DataResult.success((Object)n);
                }
                return DataResult.error(() -> "Value " + n + " outside of range [" + this.minInclusive + ":" + n2 + "]", (Object)n);
            });
        }

        @Override
        public boolean createCycleButton() {
            return true;
        }

        @Override
        public CycleButton.ValueListSupplier<Integer> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
        }
    }

    public record IntRange(int minInclusive, int maxInclusive, boolean applyValueImmediately) implements IntRangeBase
    {
        public IntRange(int n, int n2) {
            this(n, n2, true);
        }

        @Override
        public Optional<Integer> validateValue(Integer n) {
            return n.compareTo(this.minInclusive()) >= 0 && n.compareTo(this.maxInclusive()) <= 0 ? Optional.of(n) : Optional.empty();
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.intRange((int)this.minInclusive, (int)(this.maxInclusive + 1));
        }
    }

    static interface IntRangeBase
    extends SliderableValueSet<Integer> {
        public int minInclusive();

        public int maxInclusive();

        @Override
        default public double toSliderValue(Integer n) {
            if (n.intValue() == this.minInclusive()) {
                return 0.0;
            }
            if (n.intValue() == this.maxInclusive()) {
                return 1.0;
            }
            return Mth.map((double)n.intValue() + 0.5, (double)this.minInclusive(), (double)this.maxInclusive() + 1.0, 0.0, 1.0);
        }

        @Override
        default public Integer fromSliderValue(double d) {
            if (d >= 1.0) {
                d = 0.99999f;
            }
            return Mth.floor(Mth.map(d, 0.0, 1.0, (double)this.minInclusive(), (double)this.maxInclusive() + 1.0));
        }

        default public <R> SliderableValueSet<R> xmap(final IntFunction<? extends R> intFunction, final ToIntFunction<? super R> toIntFunction) {
            return new SliderableValueSet<R>(){

                @Override
                public Optional<R> validateValue(R r) {
                    return this.validateValue(toIntFunction.applyAsInt(r)).map(intFunction::apply);
                }

                @Override
                public double toSliderValue(R r) {
                    return this.toSliderValue(toIntFunction.applyAsInt(r));
                }

                @Override
                public R fromSliderValue(double d) {
                    return intFunction.apply(this.fromSliderValue(d));
                }

                @Override
                public Codec<R> codec() {
                    return this.codec().xmap(intFunction::apply, toIntFunction::applyAsInt);
                }
            };
        }

        @Override
        default public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }
    }

    public static final class OptionInstanceSliderButton<N>
    extends AbstractOptionSliderButton {
        private final OptionInstance<N> instance;
        private final SliderableValueSet<N> values;
        private final TooltipSupplier<N> tooltipSupplier;
        private final Consumer<N> onValueChanged;
        @Nullable
        private Long delayedApplyAt;
        private final boolean applyValueImmediately;

        OptionInstanceSliderButton(Options options, int n, int n2, int n3, int n4, OptionInstance<N> optionInstance, SliderableValueSet<N> sliderableValueSet, TooltipSupplier<N> tooltipSupplier, Consumer<N> consumer, boolean bl) {
            super(options, n, n2, n3, n4, sliderableValueSet.toSliderValue(optionInstance.get()));
            this.instance = optionInstance;
            this.values = sliderableValueSet;
            this.tooltipSupplier = tooltipSupplier;
            this.onValueChanged = consumer;
            this.applyValueImmediately = bl;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.instance.toString.apply(this.values.fromSliderValue(this.value)));
            this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
        }

        @Override
        protected void applyValue() {
            if (this.applyValueImmediately) {
                this.applyUnsavedValue();
            } else {
                this.delayedApplyAt = Util.getMillis() + 600L;
            }
        }

        public void applyUnsavedValue() {
            N n = this.values.fromSliderValue(this.value);
            if (!Objects.equals(n, this.instance.get())) {
                this.instance.set(n);
                this.onValueChanged.accept(this.instance.get());
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            super.renderWidget(guiGraphics, n, n2, f);
            if (this.delayedApplyAt != null && Util.getMillis() >= this.delayedApplyAt) {
                this.delayedApplyAt = null;
                this.applyUnsavedValue();
            }
        }
    }

    public record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public Optional<T> validateValue(T t) {
            return this.validateValue.apply(t);
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create((Collection)this.values.get());
        }
    }

    public record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
        }

        @Override
        public Optional<T> validateValue(T t) {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(t) ? Optional.of(t) : Optional.empty();
        }
    }

    static interface SliderableOrCyclableValueSet<T>
    extends CycleableValueSet<T>,
    SliderableValueSet<T> {
        public boolean createCycleButton();

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int n, int n2, int n3, Consumer<T> consumer) {
            if (this.createCycleButton()) {
                return CycleableValueSet.super.createButton(tooltipSupplier, options, n, n2, n3, consumer);
            }
            return SliderableValueSet.super.createButton(tooltipSupplier, options, n, n2, n3, consumer);
        }
    }

    static interface CycleableValueSet<T>
    extends ValueSet<T> {
        public CycleButton.ValueListSupplier<T> valueListSupplier();

        default public ValueSetter<T> valueSetter() {
            return OptionInstance::set;
        }

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int n, int n2, int n3, Consumer<T> consumer) {
            return optionInstance -> CycleButton.builder(optionInstance.toString).withValues(this.valueListSupplier()).withTooltip(tooltipSupplier).withInitialValue(optionInstance.value).create(n, n2, n3, 20, optionInstance.caption, (cycleButton, object) -> {
                this.valueSetter().set((OptionInstance<Object>)optionInstance, object);
                options.save();
                consumer.accept(object);
            });
        }

        public static interface ValueSetter<T> {
            public void set(OptionInstance<T> var1, T var2);
        }
    }

    static interface SliderableValueSet<T>
    extends ValueSet<T> {
        public double toSliderValue(T var1);

        public T fromSliderValue(double var1);

        default public boolean applyValueImmediately() {
            return true;
        }

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int n, int n2, int n3, Consumer<T> consumer) {
            return optionInstance -> new OptionInstanceSliderButton(options, n, n2, n3, 20, optionInstance, this, tooltipSupplier, consumer, this.applyValueImmediately());
        }
    }
}

