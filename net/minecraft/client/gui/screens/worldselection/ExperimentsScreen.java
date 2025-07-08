/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SwitchGrid;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

public class ExperimentsScreen
extends Screen {
    private static final Component TITLE = Component.translatable("selectWorld.experiments");
    private static final Component INFO = Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED);
    private static final int MAIN_CONTENT_WIDTH = 310;
    private static final int SCROLL_AREA_MIN_HEIGHT = 130;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final PackRepository packRepository;
    private final Consumer<PackRepository> output;
    private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap();
    @Nullable
    private ScrollableLayout scrollArea;

    public ExperimentsScreen(Screen screen, PackRepository packRepository, Consumer<PackRepository> consumer) {
        super(TITLE);
        this.parent = screen;
        this.packRepository = packRepository;
        this.output = consumer;
        for (Pack pack : packRepository.getAvailablePacks()) {
            if (pack.getPackSource() != PackSource.FEATURE) continue;
            this.packs.put((Object)pack, packRepository.getSelectedPacks().contains(pack));
        }
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical());
        linearLayout.addChild(new MultiLineTextWidget(INFO, this.font).setMaxWidth(310), layoutSettings -> layoutSettings.paddingBottom(15));
        SwitchGrid.Builder builder = SwitchGrid.builder(299).withInfoUnderneath(2, true).withRowSpacing(4);
        this.packs.forEach((pack, bl2) -> builder.addSwitch(ExperimentsScreen.getHumanReadableTitle(pack), () -> this.packs.getBoolean(pack), bl -> this.packs.put(pack, bl.booleanValue())).withInfo(pack.getDescription()));
        Layout layout = builder.build().layout();
        this.scrollArea = new ScrollableLayout(this.minecraft, layout, 130);
        this.scrollArea.setMinWidth(310);
        linearLayout.addChild(this.scrollArea);
        LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).build());
        linearLayout2.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    private static Component getHumanReadableTitle(Pack pack) {
        String string = "dataPack." + pack.getId() + ".name";
        return I18n.exists(string) ? Component.translatable(string) : pack.getTitle();
    }

    @Override
    protected void repositionElements() {
        this.scrollArea.setMaxHeight(130);
        this.layout.arrangeElements();
        int n = this.height - this.layout.getFooterHeight() - this.scrollArea.getRectangle().bottom();
        this.scrollArea.setMaxHeight(this.scrollArea.getHeight() + n);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), INFO);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void onDone() {
        ArrayList<Pack> arrayList = new ArrayList<Pack>(this.packRepository.getSelectedPacks());
        ArrayList arrayList2 = new ArrayList();
        this.packs.forEach((pack, bl) -> {
            arrayList.remove(pack);
            if (bl.booleanValue()) {
                arrayList2.add(pack);
            }
        });
        arrayList.addAll(Lists.reverse(arrayList2));
        this.packRepository.setSelected(arrayList.stream().map(Pack::getId).toList());
        this.output.accept(this.packRepository);
    }
}

