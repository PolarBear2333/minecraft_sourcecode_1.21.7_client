/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.options;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ChatOptionsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.chat.title");

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.chatVisibility(), options.chatColors(), options.chatLinks(), options.chatLinksPrompt(), options.chatOpacity(), options.textBackgroundOpacity(), options.chatScale(), options.chatLineSpacing(), options.chatDelay(), options.chatWidth(), options.chatHeightFocused(), options.chatHeightUnfocused(), options.narrator(), options.autoSuggestions(), options.hideMatchedNames(), options.reducedDebugInfo(), options.onlyShowSecureChat()};
    }

    public ChatOptionsScreen(Screen screen, Options options) {
        super(screen, options, TITLE);
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(ChatOptionsScreen.options(this.options));
    }
}

