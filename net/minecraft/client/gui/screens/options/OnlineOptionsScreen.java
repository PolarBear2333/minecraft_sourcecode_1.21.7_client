/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.options;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;

public class OnlineOptionsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.online.title");
    @Nullable
    private OptionInstance<Unit> difficultyDisplay;

    public OnlineOptionsScreen(Screen screen, Options options) {
        super(screen, options, TITLE);
    }

    @Override
    protected void init() {
        AbstractWidget abstractWidget;
        super.init();
        if (this.difficultyDisplay != null && (abstractWidget = this.list.findOption(this.difficultyDisplay)) != null) {
            abstractWidget.active = false;
        }
    }

    private OptionInstance<?>[] options(Options options, Minecraft minecraft) {
        ArrayList<OptionInstance> arrayList = new ArrayList<OptionInstance>();
        arrayList.add(options.realmsNotifications());
        arrayList.add(options.allowServerListing());
        OptionInstance optionInstance = Optionull.map(minecraft.level, clientLevel -> {
            Difficulty difficulty = clientLevel.getDifficulty();
            return new OptionInstance<Unit>("options.difficulty.online", OptionInstance.noTooltip(), (component, unit) -> difficulty.getDisplayName(), new OptionInstance.Enum<Unit>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()), Unit.INSTANCE, unit -> {});
        });
        if (optionInstance != null) {
            this.difficultyDisplay = optionInstance;
            arrayList.add(optionInstance);
        }
        return arrayList.toArray(new OptionInstance[0]);
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(this.options(this.options, this.minecraft));
    }
}

