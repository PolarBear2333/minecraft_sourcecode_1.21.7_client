/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.dialog.input;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.input.InputControlHandler;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.input.BooleanInput;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.server.dialog.input.NumberRangeInput;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.server.dialog.input.TextInput;
import org.slf4j.Logger;

public class InputControlHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MapCodec<? extends InputControl>, InputControlHandler<?>> HANDLERS = new HashMap();

    private static <T extends InputControl> void register(MapCodec<T> mapCodec, InputControlHandler<? super T> inputControlHandler) {
        HANDLERS.put(mapCodec, inputControlHandler);
    }

    @Nullable
    private static <T extends InputControl> InputControlHandler<T> get(T t) {
        return HANDLERS.get(t.mapCodec());
    }

    public static <T extends InputControl> void createHandler(T t, Screen screen, InputControlHandler.Output output) {
        InputControlHandler<T> inputControlHandler = InputControlHandlers.get(t);
        if (inputControlHandler == null) {
            LOGGER.warn("Unrecognized input control {}", t);
            return;
        }
        inputControlHandler.addControl(t, screen, output);
    }

    public static void bootstrap() {
        InputControlHandlers.register(TextInput.MAP_CODEC, new TextInputHandler());
        InputControlHandlers.register(SingleOptionInput.MAP_CODEC, new SingleOptionHandler());
        InputControlHandlers.register(BooleanInput.MAP_CODEC, new BooleanHandler());
        InputControlHandlers.register(NumberRangeInput.MAP_CODEC, new NumberRangeHandler());
    }

    static class TextInputHandler
    implements InputControlHandler<TextInput> {
        TextInputHandler() {
        }

        @Override
        public void addControl(TextInput textInput, Screen screen, InputControlHandler.Output output) {
            Supplier<String> supplier;
            Object object;
            Object object2;
            Font font = screen.getFont();
            if (textInput.multiline().isPresent()) {
                object2 = textInput.multiline().get();
                int n = ((TextInput.MultilineOptions)object2).height().orElseGet(() -> TextInputHandler.lambda$addControl$0((TextInput.MultilineOptions)object2, font));
                MultiLineEditBox multiLineEditBox = MultiLineEditBox.builder().build(font, textInput.width(), n, CommonComponents.EMPTY);
                multiLineEditBox.setCharacterLimit(textInput.maxLength());
                ((TextInput.MultilineOptions)object2).maxLines().ifPresent(multiLineEditBox::setLineLimit);
                multiLineEditBox.setValue(textInput.initial());
                object = multiLineEditBox;
                supplier = multiLineEditBox::getValue;
            } else {
                object2 = new EditBox(font, textInput.width(), 20, textInput.label());
                ((EditBox)object2).setMaxLength(textInput.maxLength());
                ((EditBox)object2).setValue(textInput.initial());
                object = object2;
                supplier = ((EditBox)object2)::getValue;
            }
            object2 = textInput.labelVisible() ? CommonLayouts.labeledElement(font, (LayoutElement)object, textInput.label()) : object;
            output.accept((LayoutElement)object2, new Action.ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return StringTag.escapeWithoutQuotes((String)supplier.get());
                }

                @Override
                public Tag asTag() {
                    return StringTag.valueOf((String)supplier.get());
                }
            });
        }

        @Override
        public /* synthetic */ void addControl(InputControl inputControl, Screen screen, InputControlHandler.Output output) {
            this.addControl((TextInput)inputControl, screen, output);
        }

        private static /* synthetic */ Integer lambda$addControl$0(TextInput.MultilineOptions multilineOptions, Font font) {
            int n = multilineOptions.maxLines().orElse(4);
            return Math.min(font.lineHeight * n + 8, 512);
        }
    }

    static class SingleOptionHandler
    implements InputControlHandler<SingleOptionInput> {
        SingleOptionHandler() {
        }

        @Override
        public void addControl(SingleOptionInput singleOptionInput, Screen screen, InputControlHandler.Output output) {
            CycleButton.Builder<SingleOptionInput.Entry> builder = CycleButton.builder(SingleOptionInput.Entry::displayOrDefault).withValues((Collection<SingleOptionInput.Entry>)singleOptionInput.entries()).displayOnlyValue(!singleOptionInput.labelVisible());
            Optional<SingleOptionInput.Entry> optional = singleOptionInput.initial();
            if (optional.isPresent()) {
                builder = builder.withInitialValue(optional.get());
            }
            CycleButton<SingleOptionInput.Entry> cycleButton = builder.create(0, 0, singleOptionInput.width(), 20, singleOptionInput.label());
            output.accept(cycleButton, Action.ValueGetter.of(() -> ((SingleOptionInput.Entry)cycleButton.getValue()).id()));
        }

        @Override
        public /* synthetic */ void addControl(InputControl inputControl, Screen screen, InputControlHandler.Output output) {
            this.addControl((SingleOptionInput)inputControl, screen, output);
        }
    }

    static class BooleanHandler
    implements InputControlHandler<BooleanInput> {
        BooleanHandler() {
        }

        @Override
        public void addControl(final BooleanInput booleanInput, Screen screen, InputControlHandler.Output output) {
            Font font = screen.getFont();
            final Checkbox checkbox = Checkbox.builder(booleanInput.label(), font).selected(booleanInput.initial()).build();
            output.accept(checkbox, new Action.ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return checkbox.selected() ? booleanInput.onTrue() : booleanInput.onFalse();
                }

                @Override
                public Tag asTag() {
                    return ByteTag.valueOf(checkbox.selected());
                }
            });
        }

        @Override
        public /* synthetic */ void addControl(InputControl inputControl, Screen screen, InputControlHandler.Output output) {
            this.addControl((BooleanInput)inputControl, screen, output);
        }
    }

    static class NumberRangeHandler
    implements InputControlHandler<NumberRangeInput> {
        NumberRangeHandler() {
        }

        @Override
        public void addControl(NumberRangeInput numberRangeInput, Screen screen, InputControlHandler.Output output) {
            float f = numberRangeInput.rangeInfo().initialSliderValue();
            final SliderImpl sliderImpl = new SliderImpl(numberRangeInput, f);
            output.accept(sliderImpl, new Action.ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return sliderImpl.stringValueToSend();
                }

                @Override
                public Tag asTag() {
                    return FloatTag.valueOf(sliderImpl.floatValueToSend());
                }
            });
        }

        @Override
        public /* synthetic */ void addControl(InputControl inputControl, Screen screen, InputControlHandler.Output output) {
            this.addControl((NumberRangeInput)inputControl, screen, output);
        }

        static class SliderImpl
        extends AbstractSliderButton {
            private final NumberRangeInput input;

            SliderImpl(NumberRangeInput numberRangeInput, double d) {
                super(0, 0, numberRangeInput.width(), 20, SliderImpl.computeMessage(numberRangeInput, d), d);
                this.input = numberRangeInput;
            }

            @Override
            protected void updateMessage() {
                this.setMessage(SliderImpl.computeMessage(this.input, this.value));
            }

            @Override
            protected void applyValue() {
            }

            public String stringValueToSend() {
                return SliderImpl.sliderValueToString(this.input, this.value);
            }

            public float floatValueToSend() {
                return SliderImpl.scaledValue(this.input, this.value);
            }

            private static float scaledValue(NumberRangeInput numberRangeInput, double d) {
                return numberRangeInput.rangeInfo().computeScaledValue((float)d);
            }

            private static String sliderValueToString(NumberRangeInput numberRangeInput, double d) {
                return SliderImpl.valueToString(SliderImpl.scaledValue(numberRangeInput, d));
            }

            private static Component computeMessage(NumberRangeInput numberRangeInput, double d) {
                return numberRangeInput.computeLabel(SliderImpl.sliderValueToString(numberRangeInput, d));
            }

            private static String valueToString(float f) {
                int n = (int)f;
                if ((float)n == f) {
                    return Integer.toString(n);
                }
                return Float.toString(f);
            }
        }
    }
}

