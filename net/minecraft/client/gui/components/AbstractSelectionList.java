/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class AbstractSelectionList<E extends Entry<E>>
extends AbstractContainerWidget {
    private static final ResourceLocation MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final List<E> children = new TrackedList();
    protected boolean centerListVertically = true;
    private boolean renderHeader;
    protected int headerHeight;
    @Nullable
    private E selected;
    @Nullable
    private E hovered;

    public AbstractSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4) {
        super(0, n3, n, n2, CommonComponents.EMPTY);
        this.minecraft = minecraft;
        this.itemHeight = n4;
    }

    public AbstractSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4, int n5) {
        this(minecraft, n, n2, n3, n4);
        this.renderHeader = true;
        this.headerHeight = n5;
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    public void setSelectedIndex(int n) {
        if (n == -1) {
            this.setSelected(null);
        } else if (this.getItemCount() != 0) {
            this.setSelected(this.getEntry(n));
        }
    }

    public void setSelected(@Nullable E e) {
        this.selected = e;
    }

    public E getFirstElement() {
        return (E)((Entry)this.children.get(0));
    }

    @Nullable
    public E getFocused() {
        return (E)((Entry)super.getFocused());
    }

    public final List<E> children() {
        return this.children;
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    public void replaceEntries(Collection<E> collection) {
        this.clearEntries();
        this.children.addAll(collection);
    }

    protected E getEntry(int n) {
        return (E)((Entry)this.children().get(n));
    }

    protected int addEntry(E e) {
        this.children.add(e);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E e) {
        double d = (double)this.maxScrollAmount() - this.scrollAmount();
        this.children.add(0, e);
        this.setScrollAmount((double)this.maxScrollAmount() - d);
    }

    protected boolean removeEntryFromTop(E e) {
        double d = (double)this.maxScrollAmount() - this.scrollAmount();
        boolean bl = this.removeEntry(e);
        this.setScrollAmount((double)this.maxScrollAmount() - d);
        return bl;
    }

    protected int getItemCount() {
        return this.children().size();
    }

    protected boolean isSelectedItem(int n) {
        return Objects.equals(this.getSelected(), this.children().get(n));
    }

    @Nullable
    protected final E getEntryAtPosition(double d, double d2) {
        int n = this.getRowWidth() / 2;
        int n2 = this.getX() + this.width / 2;
        int n3 = n2 - n;
        int n4 = n2 + n;
        int n5 = Mth.floor(d2 - (double)this.getY()) - this.headerHeight + (int)this.scrollAmount() - 4;
        int n6 = n5 / this.itemHeight;
        if (d >= (double)n3 && d <= (double)n4 && n6 >= 0 && n5 >= 0 && n6 < this.getItemCount()) {
            return (E)((Entry)this.children().get(n6));
        }
        return null;
    }

    public void updateSize(int n, HeaderAndFooterLayout headerAndFooterLayout) {
        this.updateSizeAndPosition(n, headerAndFooterLayout.getContentHeight(), headerAndFooterLayout.getHeaderHeight());
    }

    public void updateSizeAndPosition(int n, int n2, int n3) {
        this.setSize(n, n2);
        this.setPosition(0, n3);
        this.refreshScrollAmount();
    }

    @Override
    protected int contentHeight() {
        return this.getItemCount() * this.itemHeight + this.headerHeight + 4;
    }

    protected void renderHeader(GuiGraphics guiGraphics, int n, int n2) {
    }

    protected void renderDecorations(GuiGraphics guiGraphics, int n, int n2) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.hovered = this.isMouseOver(n, n2) ? this.getEntryAtPosition(n, n2) : null;
        this.renderListBackground(guiGraphics);
        this.enableScissor(guiGraphics);
        if (this.renderHeader) {
            int n3 = this.getRowLeft();
            int n4 = this.getY() + 4 - (int)this.scrollAmount();
            this.renderHeader(guiGraphics, n3, n4);
        }
        this.renderListItems(guiGraphics, n, n2, f);
        guiGraphics.disableScissor();
        this.renderListSeparators(guiGraphics);
        this.renderScrollbar(guiGraphics);
        this.renderDecorations(guiGraphics, n, n2);
    }

    protected void renderListSeparators(GuiGraphics guiGraphics) {
        ResourceLocation resourceLocation = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        ResourceLocation resourceLocation2 = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY() - 2, 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation2, this.getX(), this.getBottom(), 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
    }

    protected void renderListBackground(GuiGraphics guiGraphics) {
        ResourceLocation resourceLocation = this.minecraft.level == null ? MENU_LIST_BACKGROUND : INWORLD_MENU_LIST_BACKGROUND;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.getRight(), this.getBottom() + (int)this.scrollAmount(), this.getWidth(), this.getHeight(), 32, 32);
    }

    protected void enableScissor(GuiGraphics guiGraphics) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
    }

    protected void centerScrollOn(E e) {
        this.setScrollAmount(this.children().indexOf(e) * this.itemHeight + this.itemHeight / 2 - this.height / 2);
    }

    protected void ensureVisible(E e) {
        int n;
        int n2 = this.getRowTop(this.children().indexOf(e));
        int n3 = n2 - this.getY() - 4 - this.itemHeight;
        if (n3 < 0) {
            this.scroll(n3);
        }
        if ((n = this.getBottom() - n2 - this.itemHeight - this.itemHeight) < 0) {
            this.scroll(-n);
        }
    }

    private void scroll(int n) {
        this.setScrollAmount(this.scrollAmount() + (double)n);
    }

    @Override
    protected double scrollRate() {
        return (double)this.itemHeight / 2.0;
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + 6 + 2;
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double d, double d2) {
        return Optional.ofNullable(this.getEntryAtPosition(d, d2));
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        GuiEventListener guiEventListener2 = this.getFocused();
        if (guiEventListener2 != guiEventListener && guiEventListener2 instanceof ContainerEventHandler) {
            ContainerEventHandler containerEventHandler = (ContainerEventHandler)guiEventListener2;
            containerEventHandler.setFocused(null);
        }
        super.setFocused(guiEventListener);
        int n = this.children.indexOf(guiEventListener);
        if (n >= 0) {
            Entry entry = (Entry)this.children.get(n);
            this.setSelected(entry);
            if (this.minecraft.getLastInputType().isKeyboard()) {
                this.ensureVisible(entry);
            }
        }
    }

    @Nullable
    protected E nextEntry(ScreenDirection screenDirection) {
        return (E)this.nextEntry(screenDirection, entry -> true);
    }

    @Nullable
    protected E nextEntry(ScreenDirection screenDirection, Predicate<E> predicate) {
        return this.nextEntry(screenDirection, predicate, this.getSelected());
    }

    @Nullable
    protected E nextEntry(ScreenDirection screenDirection, Predicate<E> predicate, @Nullable E e) {
        int n;
        switch (screenDirection) {
            default: {
                throw new MatchException(null, null);
            }
            case RIGHT: 
            case LEFT: {
                int n2 = 0;
                break;
            }
            case UP: {
                int n2 = -1;
                break;
            }
            case DOWN: {
                int n2 = n = 1;
            }
        }
        if (!this.children().isEmpty() && n != 0) {
            int n3 = e == null ? (n > 0 ? 0 : this.children().size() - 1) : this.children().indexOf(e) + n;
            for (int i = n3; i >= 0 && i < this.children.size(); i += n) {
                Entry entry = (Entry)this.children().get(i);
                if (!predicate.test(entry)) continue;
                return (E)entry;
            }
        }
        return null;
    }

    protected void renderListItems(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = this.getRowLeft();
        int n4 = this.getRowWidth();
        int n5 = this.itemHeight - 4;
        int n6 = this.getItemCount();
        for (int i = 0; i < n6; ++i) {
            int n7 = this.getRowTop(i);
            int n8 = this.getRowBottom(i);
            if (n8 < this.getY() || n7 > this.getBottom()) continue;
            this.renderItem(guiGraphics, n, n2, f, i, n3, n7, n4, n5);
        }
    }

    protected void renderItem(GuiGraphics guiGraphics, int n, int n2, float f, int n3, int n4, int n5, int n6, int n7) {
        E e = this.getEntry(n3);
        ((Entry)e).renderBack(guiGraphics, n3, n5, n4, n6, n7, n, n2, Objects.equals(this.hovered, e), f);
        if (this.isSelectedItem(n3)) {
            int n8 = this.isFocused() ? -1 : -8355712;
            this.renderSelection(guiGraphics, n5, n6, n7, n8, -16777216);
        }
        ((Entry)e).render(guiGraphics, n3, n5, n4, n6, n7, n, n2, Objects.equals(this.hovered, e), f);
    }

    protected void renderSelection(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5) {
        int n6 = this.getX() + (this.width - n2) / 2;
        int n7 = this.getX() + (this.width + n2) / 2;
        guiGraphics.fill(n6, n - 2, n7, n + n3 + 2, n4);
        guiGraphics.fill(n6 + 1, n - 1, n7 - 1, n + n3 + 1, n5);
    }

    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    public int getRowTop(int n) {
        return this.getY() + 4 - (int)this.scrollAmount() + n * this.itemHeight + this.headerHeight;
    }

    public int getRowBottom(int n) {
        return this.getRowTop(n) + this.itemHeight;
    }

    public int getRowWidth() {
        return 220;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        if (this.hovered != null) {
            return NarratableEntry.NarrationPriority.HOVERED;
        }
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Nullable
    protected E remove(int n) {
        Entry entry = (Entry)this.children.get(n);
        if (this.removeEntry((Entry)this.children.get(n))) {
            return (E)entry;
        }
        return null;
    }

    protected boolean removeEntry(E e) {
        boolean bl = this.children.remove(e);
        if (bl && e == this.getSelected()) {
            this.setSelected(null);
        }
        return bl;
    }

    @Nullable
    protected E getHovered() {
        return this.hovered;
    }

    void bindEntryToSelf(Entry<E> entry) {
        entry.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, E e) {
        int n;
        List<E> list = this.children();
        if (list.size() > 1 && (n = list.indexOf(e)) != -1) {
            narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.list", n + 1, list.size()));
        }
    }

    @Override
    @Nullable
    public /* synthetic */ GuiEventListener getFocused() {
        return this.getFocused();
    }

    class TrackedList
    extends AbstractList<E> {
        private final List<E> delegate = Lists.newArrayList();

        TrackedList() {
        }

        @Override
        public E get(int n) {
            return (Entry)this.delegate.get(n);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public E set(int n, E e) {
            Entry entry = (Entry)this.delegate.set(n, e);
            AbstractSelectionList.this.bindEntryToSelf(e);
            return entry;
        }

        @Override
        public void add(int n, E e) {
            this.delegate.add(n, e);
            AbstractSelectionList.this.bindEntryToSelf(e);
        }

        @Override
        public E remove(int n) {
            return (Entry)this.delegate.remove(n);
        }

        @Override
        public /* synthetic */ Object remove(int n) {
            return this.remove(n);
        }

        @Override
        public /* synthetic */ void add(int n, Object object) {
            this.add(n, (E)((Entry)object));
        }

        @Override
        public /* synthetic */ Object set(int n, Object object) {
            return this.set(n, (E)((Entry)object));
        }

        @Override
        public /* synthetic */ Object get(int n) {
            return this.get(n);
        }
    }

    protected static abstract class Entry<E extends Entry<E>>
    implements GuiEventListener {
        @Deprecated
        AbstractSelectionList<E> list;

        protected Entry() {
        }

        @Override
        public void setFocused(boolean bl) {
        }

        @Override
        public boolean isFocused() {
            return this.list.getFocused() == this;
        }

        public abstract void render(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10);

        public void renderBack(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
        }

        @Override
        public boolean isMouseOver(double d, double d2) {
            return Objects.equals(this.list.getEntryAtPosition(d, d2), this);
        }
    }
}

