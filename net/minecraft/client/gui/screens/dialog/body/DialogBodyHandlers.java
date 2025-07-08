/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.dialog.body;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.body.DialogBodyHandler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import org.slf4j.Logger;

public class DialogBodyHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MapCodec<? extends DialogBody>, DialogBodyHandler<?>> HANDLERS = new HashMap();

    private static <B extends DialogBody> void register(MapCodec<B> mapCodec, DialogBodyHandler<? super B> dialogBodyHandler) {
        HANDLERS.put(mapCodec, dialogBodyHandler);
    }

    @Nullable
    private static <B extends DialogBody> DialogBodyHandler<B> getHandler(B b) {
        return HANDLERS.get(b.mapCodec());
    }

    @Nullable
    public static <B extends DialogBody> LayoutElement createBodyElement(DialogScreen<?> dialogScreen, B b) {
        DialogBodyHandler<B> dialogBodyHandler = DialogBodyHandlers.getHandler(b);
        if (dialogBodyHandler == null) {
            LOGGER.warn("Unrecognized dialog body {}", b);
            return null;
        }
        return dialogBodyHandler.createControls(dialogScreen, b);
    }

    public static void bootstrap() {
        DialogBodyHandlers.register(PlainMessage.MAP_CODEC, new PlainMessageHandler());
        DialogBodyHandlers.register(ItemBody.MAP_CODEC, new ItemHandler());
    }

    static void runActionOnParent(DialogScreen<?> dialogScreen, @Nullable Style style) {
        ClickEvent clickEvent;
        if (style != null && (clickEvent = style.getClickEvent()) != null) {
            dialogScreen.runAction(Optional.of(clickEvent));
        }
    }

    static class PlainMessageHandler
    implements DialogBodyHandler<PlainMessage> {
        PlainMessageHandler() {
        }

        @Override
        public LayoutElement createControls(DialogScreen<?> dialogScreen, PlainMessage plainMessage) {
            return new FocusableTextWidget(plainMessage.width(), plainMessage.contents(), dialogScreen.getFont(), false, false, 4).configureStyleHandling(true, style -> DialogBodyHandlers.runActionOnParent(dialogScreen, style)).setCentered(true);
        }
    }

    static class ItemHandler
    implements DialogBodyHandler<ItemBody> {
        ItemHandler() {
        }

        @Override
        public LayoutElement createControls(DialogScreen<?> dialogScreen, ItemBody itemBody) {
            if (itemBody.description().isPresent()) {
                PlainMessage plainMessage = itemBody.description().get();
                LinearLayout linearLayout = LinearLayout.horizontal().spacing(2);
                linearLayout.defaultCellSetting().alignVerticallyMiddle();
                ItemDisplayWidget itemDisplayWidget = new ItemDisplayWidget(Minecraft.getInstance(), 0, 0, itemBody.width(), itemBody.height(), CommonComponents.EMPTY, itemBody.item(), itemBody.showDecorations(), itemBody.showTooltip());
                linearLayout.addChild(itemDisplayWidget);
                linearLayout.addChild(new FocusableTextWidget(plainMessage.width(), plainMessage.contents(), dialogScreen.getFont(), false, false, 4).configureStyleHandling(true, style -> DialogBodyHandlers.runActionOnParent(dialogScreen, style)));
                return linearLayout;
            }
            return new ItemDisplayWidget(Minecraft.getInstance(), 0, 0, itemBody.width(), itemBody.height(), itemBody.item().getHoverName(), itemBody.item(), itemBody.showDecorations(), itemBody.showTooltip());
        }
    }
}

