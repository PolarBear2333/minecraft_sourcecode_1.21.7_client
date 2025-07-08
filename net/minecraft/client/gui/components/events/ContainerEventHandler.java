/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  org.joml.Vector2i
 */
package net.minecraft.client.gui.components.events;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2i;

public interface ContainerEventHandler
extends GuiEventListener {
    public List<? extends GuiEventListener> children();

    default public Optional<GuiEventListener> getChildAt(double d, double d2) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.isMouseOver(d, d2)) continue;
            return Optional.of(guiEventListener);
        }
        return Optional.empty();
    }

    @Override
    default public boolean mouseClicked(double d, double d2, int n) {
        Optional<GuiEventListener> optional = this.getChildAt(d, d2);
        if (optional.isEmpty()) {
            return false;
        }
        GuiEventListener guiEventListener = optional.get();
        if (guiEventListener.mouseClicked(d, d2, n)) {
            this.setFocused(guiEventListener);
            if (n == 0) {
                this.setDragging(true);
            }
        }
        return true;
    }

    @Override
    default public boolean mouseReleased(double d, double d2, int n) {
        if (n == 0 && this.isDragging()) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(d, d2, n);
            }
        }
        return false;
    }

    @Override
    default public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.getFocused() != null && this.isDragging() && n == 0) {
            return this.getFocused().mouseDragged(d, d2, n, d3, d4);
        }
        return false;
    }

    public boolean isDragging();

    public void setDragging(boolean var1);

    @Override
    default public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        return this.getChildAt(d, d2).filter(guiEventListener -> guiEventListener.mouseScrolled(d, d2, d3, d4)).isPresent();
    }

    @Override
    default public boolean keyPressed(int n, int n2, int n3) {
        return this.getFocused() != null && this.getFocused().keyPressed(n, n2, n3);
    }

    @Override
    default public boolean keyReleased(int n, int n2, int n3) {
        return this.getFocused() != null && this.getFocused().keyReleased(n, n2, n3);
    }

    @Override
    default public boolean charTyped(char c, int n) {
        return this.getFocused() != null && this.getFocused().charTyped(c, n);
    }

    @Nullable
    public GuiEventListener getFocused();

    public void setFocused(@Nullable GuiEventListener var1);

    @Override
    default public void setFocused(boolean bl) {
    }

    @Override
    default public boolean isFocused() {
        return this.getFocused() != null;
    }

    @Override
    @Nullable
    default public ComponentPath getCurrentFocusPath() {
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener != null) {
            return ComponentPath.path(this, guiEventListener.getCurrentFocusPath());
        }
        return null;
    }

    @Override
    @Nullable
    default public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        Object object;
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener != null && (object = guiEventListener.nextFocusPath(focusNavigationEvent)) != null) {
            return ComponentPath.path(this, (ComponentPath)object);
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
            object = (FocusNavigationEvent.TabNavigation)focusNavigationEvent;
            return this.handleTabNavigation((FocusNavigationEvent.TabNavigation)object);
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            object = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            return this.handleArrowNavigation((FocusNavigationEvent.ArrowNavigation)object);
        }
        return null;
    }

    @Nullable
    private ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation tabNavigation) {
        Supplier<GuiEventListener> supplier;
        BooleanSupplier booleanSupplier;
        boolean bl = tabNavigation.forward();
        GuiEventListener guiEventListener2 = this.getFocused();
        ArrayList<? extends GuiEventListener> arrayList = new ArrayList<GuiEventListener>(this.children());
        Collections.sort(arrayList, Comparator.comparingInt(guiEventListener -> guiEventListener.getTabOrderGroup()));
        int n = arrayList.indexOf(guiEventListener2);
        int n2 = guiEventListener2 != null && n >= 0 ? n + (bl ? 1 : 0) : (bl ? 0 : arrayList.size());
        ListIterator listIterator = arrayList.listIterator(n2);
        BooleanSupplier booleanSupplier2 = bl ? listIterator::hasNext : (booleanSupplier = listIterator::hasPrevious);
        Supplier<GuiEventListener> supplier2 = bl ? listIterator::next : (supplier = listIterator::previous);
        while (booleanSupplier.getAsBoolean()) {
            GuiEventListener guiEventListener3 = supplier.get();
            ComponentPath componentPath = guiEventListener3.nextFocusPath(tabNavigation);
            if (componentPath == null) continue;
            return ComponentPath.path(this, componentPath);
        }
        return null;
    }

    @Nullable
    private ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation arrowNavigation) {
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener == null) {
            ScreenDirection screenDirection = arrowNavigation.direction();
            ScreenRectangle screenRectangle = this.getBorderForArrowNavigation(screenDirection.getOpposite());
            return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle, screenDirection, null, arrowNavigation));
        }
        ScreenRectangle screenRectangle = guiEventListener.getRectangle();
        return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle, arrowNavigation.direction(), guiEventListener, arrowNavigation));
    }

    @Nullable
    private ComponentPath nextFocusPathInDirection(ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener2, FocusNavigationEvent focusNavigationEvent) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        ScreenAxis screenAxis2 = screenAxis.orthogonal();
        ScreenDirection screenDirection2 = screenAxis2.getPositive();
        int n = screenRectangle.getBoundInDirection(screenDirection.getOpposite());
        ArrayList<GuiEventListener> arrayList = new ArrayList<GuiEventListener>();
        for (GuiEventListener object2 : this.children()) {
            Object object;
            if (object2 == guiEventListener2 || !((ScreenRectangle)(object = object2.getRectangle())).overlapsInAxis(screenRectangle, screenAxis2)) continue;
            int guiEventListener3 = ((ScreenRectangle)object).getBoundInDirection(screenDirection.getOpposite());
            if (screenDirection.isAfter(guiEventListener3, n)) {
                arrayList.add(object2);
                continue;
            }
            if (guiEventListener3 != n || !screenDirection.isAfter(((ScreenRectangle)object).getBoundInDirection(screenDirection), screenRectangle.getBoundInDirection(screenDirection))) continue;
            arrayList.add(object2);
        }
        Comparator<GuiEventListener> comparator = Comparator.comparing(guiEventListener -> guiEventListener.getRectangle().getBoundInDirection(screenDirection.getOpposite()), screenDirection.coordinateValueComparator());
        Comparator<GuiEventListener> comparator2 = Comparator.comparing(guiEventListener -> guiEventListener.getRectangle().getBoundInDirection(screenDirection2.getOpposite()), screenDirection2.coordinateValueComparator());
        arrayList.sort(comparator.thenComparing(comparator2));
        for (GuiEventListener guiEventListener3 : arrayList) {
            ComponentPath componentPath = guiEventListener3.nextFocusPath(focusNavigationEvent);
            if (componentPath == null) continue;
            return componentPath;
        }
        return this.nextFocusPathVaguelyInDirection(screenRectangle, screenDirection, guiEventListener2, focusNavigationEvent);
    }

    @Nullable
    private ComponentPath nextFocusPathVaguelyInDirection(ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener, FocusNavigationEvent focusNavigationEvent) {
        Object object;
        ScreenAxis screenAxis = screenDirection.getAxis();
        ScreenAxis screenAxis2 = screenAxis.orthogonal();
        ArrayList<Pair> arrayList = new ArrayList<Pair>();
        ScreenPosition screenPosition = ScreenPosition.of(screenAxis, screenRectangle.getBoundInDirection(screenDirection), screenRectangle.getCenterInAxis(screenAxis2));
        for (GuiEventListener guiEventListener2 : this.children()) {
            ScreenPosition screenPosition2;
            if (guiEventListener2 == guiEventListener || !screenDirection.isAfter((screenPosition2 = ScreenPosition.of(screenAxis, ((ScreenRectangle)(object = guiEventListener2.getRectangle())).getBoundInDirection(screenDirection.getOpposite()), ((ScreenRectangle)object).getCenterInAxis(screenAxis2))).getCoordinate(screenAxis), screenPosition.getCoordinate(screenAxis))) continue;
            long l = Vector2i.distanceSquared((int)screenPosition.x(), (int)screenPosition.y(), (int)screenPosition2.x(), (int)screenPosition2.y());
            arrayList.add(Pair.of((Object)guiEventListener2, (Object)l));
        }
        arrayList.sort(Comparator.comparingDouble(Pair::getSecond));
        for (Pair pair : arrayList) {
            object = ((GuiEventListener)pair.getFirst()).nextFocusPath(focusNavigationEvent);
            if (object == null) continue;
            return object;
        }
        return null;
    }
}

