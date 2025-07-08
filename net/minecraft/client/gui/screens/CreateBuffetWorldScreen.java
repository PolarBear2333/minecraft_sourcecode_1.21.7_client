/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.Collator
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class CreateBuffetWorldScreen
extends Screen {
    private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome").withColor(-8355712);
    private static final int SPACING = 8;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final Consumer<Holder<Biome>> applySettings;
    final Registry<Biome> biomes;
    private BiomeList list;
    Holder<Biome> biome;
    private Button doneButton;

    public CreateBuffetWorldScreen(Screen screen, WorldCreationContext worldCreationContext, Consumer<Holder<Biome>> consumer) {
        super(Component.translatable("createWorld.customize.buffet.title"));
        this.parent = screen;
        this.applySettings = consumer;
        this.biomes = worldCreationContext.worldgenLoadContext().lookupOrThrow(Registries.BIOME);
        Holder holder = (Holder)this.biomes.get(Biomes.PLAINS).or(() -> this.biomes.listElements().findAny()).orElseThrow();
        this.biome = worldCreationContext.selectedDimensions().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(holder);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        linearLayout.addChild(new StringWidget(this.getTitle(), this.font));
        linearLayout.addChild(new StringWidget(BIOME_SELECT_INFO, this.font));
        this.list = this.layout.addToContents(new BiomeList());
        LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.doneButton = linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.biome);
            this.onClose();
        }).build());
        linearLayout2.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.list.setSelected((BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(entry.biome, this.biome)).findFirst().orElse(null));
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.list.updateSize(this.width, this.layout);
    }

    void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    class BiomeList
    extends ObjectSelectionList<Entry> {
        BiomeList() {
            super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height - 77, 40, 16);
            Collator collator = Collator.getInstance((Locale)Locale.getDefault());
            CreateBuffetWorldScreen.this.biomes.listElements().map(reference -> new Entry((Holder.Reference<Biome>)reference)).sorted(Comparator.comparing(entry -> entry.name.getString(), collator)).forEach(entry -> this.addEntry(entry));
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                CreateBuffetWorldScreen.this.biome = entry.biome;
            }
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final Holder.Reference<Biome> biome;
            final Component name;

            public Entry(Holder.Reference<Biome> reference) {
                this.biome = reference;
                ResourceLocation resourceLocation = reference.key().location();
                String string = resourceLocation.toLanguageKey("biome");
                this.name = Language.getInstance().has(string) ? Component.translatable(string) : Component.literal(resourceLocation.toString());
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                guiGraphics.drawString(CreateBuffetWorldScreen.this.font, this.name, n3 + 5, n2 + 2, -1);
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                BiomeList.this.setSelected(this);
                return super.mouseClicked(d, d2, n);
            }
        }
    }
}

