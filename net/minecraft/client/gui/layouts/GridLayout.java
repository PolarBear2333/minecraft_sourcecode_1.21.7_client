/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.util.Mth;

public class GridLayout
extends AbstractLayout {
    private final List<LayoutElement> children = new ArrayList<LayoutElement>();
    private final List<CellInhabitant> cellInhabitants = new ArrayList<CellInhabitant>();
    private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();
    private int rowSpacing = 0;
    private int columnSpacing = 0;

    public GridLayout() {
        this(0, 0);
    }

    public GridLayout(int n, int n2) {
        super(n, n2, 0, 0);
    }

    @Override
    public void arrangeElements() {
        int n;
        int n2;
        int n3;
        super.arrangeElements();
        int n4 = 0;
        int n5 = 0;
        for (CellInhabitant object22 : this.cellInhabitants) {
            n4 = Math.max(object22.getLastOccupiedRow(), n4);
            n5 = Math.max(object22.getLastOccupiedColumn(), n5);
        }
        Object object3 = new int[n5 + 1];
        int[] nArray = new int[n4 + 1];
        for (CellInhabitant cellInhabitant : this.cellInhabitants) {
            n3 = cellInhabitant.getHeight() - (cellInhabitant.occupiedRows - 1) * this.rowSpacing;
            Divisor divisor = new Divisor(n3, cellInhabitant.occupiedRows);
            for (n2 = cellInhabitant.row; n2 <= cellInhabitant.getLastOccupiedRow(); ++n2) {
                nArray[n2] = Math.max(nArray[n2], divisor.nextInt());
            }
            n2 = cellInhabitant.getWidth() - (cellInhabitant.occupiedColumns - 1) * this.columnSpacing;
            Divisor divisor2 = new Divisor(n2, cellInhabitant.occupiedColumns);
            for (n = cellInhabitant.column; n <= cellInhabitant.getLastOccupiedColumn(); ++n) {
                object3[n] = Math.max((int)object3[n], divisor2.nextInt());
            }
        }
        Object object = new int[n5 + 1];
        int[] nArray2 = new int[n4 + 1];
        object[0] = false;
        for (n3 = 1; n3 <= n5; ++n3) {
            object[n3] = object[n3 - 1] + object3[n3 - 1] + this.columnSpacing;
        }
        nArray2[0] = 0;
        for (n3 = 1; n3 <= n4; ++n3) {
            nArray2[n3] = nArray2[n3 - 1] + nArray[n3 - 1] + this.rowSpacing;
        }
        for (CellInhabitant cellInhabitant : this.cellInhabitants) {
            int n6;
            n2 = 0;
            for (n6 = cellInhabitant.column; n6 <= cellInhabitant.getLastOccupiedColumn(); ++n6) {
                n2 += object3[n6];
            }
            cellInhabitant.setX(this.getX() + object[cellInhabitant.column], n2 += this.columnSpacing * (cellInhabitant.occupiedColumns - 1));
            n6 = 0;
            for (n = cellInhabitant.row; n <= cellInhabitant.getLastOccupiedRow(); ++n) {
                n6 += nArray[n];
            }
            cellInhabitant.setY(this.getY() + nArray2[cellInhabitant.row], n6 += this.rowSpacing * (cellInhabitant.occupiedRows - 1));
        }
        this.width = (int)(object[n5] + object3[n5]);
        this.height = nArray2[n4] + nArray[n4];
    }

    public <T extends LayoutElement> T addChild(T t, int n, int n2) {
        return this.addChild(t, n, n2, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T t, int n, int n2, LayoutSettings layoutSettings) {
        return this.addChild(t, n, n2, 1, 1, layoutSettings);
    }

    public <T extends LayoutElement> T addChild(T t, int n, int n2, Consumer<LayoutSettings> consumer) {
        return this.addChild(t, n, n2, 1, 1, Util.make(this.newCellSettings(), consumer));
    }

    public <T extends LayoutElement> T addChild(T t, int n, int n2, int n3, int n4) {
        return this.addChild(t, n, n2, n3, n4, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T t, int n, int n2, int n3, int n4, LayoutSettings layoutSettings) {
        if (n3 < 1) {
            throw new IllegalArgumentException("Occupied rows must be at least 1");
        }
        if (n4 < 1) {
            throw new IllegalArgumentException("Occupied columns must be at least 1");
        }
        this.cellInhabitants.add(new CellInhabitant(t, n, n2, n3, n4, layoutSettings));
        this.children.add(t);
        return t;
    }

    public <T extends LayoutElement> T addChild(T t, int n, int n2, int n3, int n4, Consumer<LayoutSettings> consumer) {
        return this.addChild(t, n, n2, n3, n4, Util.make(this.newCellSettings(), consumer));
    }

    public GridLayout columnSpacing(int n) {
        this.columnSpacing = n;
        return this;
    }

    public GridLayout rowSpacing(int n) {
        this.rowSpacing = n;
        return this;
    }

    public GridLayout spacing(int n) {
        return this.columnSpacing(n).rowSpacing(n);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.children.forEach(consumer);
    }

    public LayoutSettings newCellSettings() {
        return this.defaultCellSettings.copy();
    }

    public LayoutSettings defaultCellSetting() {
        return this.defaultCellSettings;
    }

    public RowHelper createRowHelper(int n) {
        return new RowHelper(n);
    }

    static class CellInhabitant
    extends AbstractLayout.AbstractChildWrapper {
        final int row;
        final int column;
        final int occupiedRows;
        final int occupiedColumns;

        CellInhabitant(LayoutElement layoutElement, int n, int n2, int n3, int n4, LayoutSettings layoutSettings) {
            super(layoutElement, layoutSettings.getExposed());
            this.row = n;
            this.column = n2;
            this.occupiedRows = n3;
            this.occupiedColumns = n4;
        }

        public int getLastOccupiedRow() {
            return this.row + this.occupiedRows - 1;
        }

        public int getLastOccupiedColumn() {
            return this.column + this.occupiedColumns - 1;
        }
    }

    public final class RowHelper {
        private final int columns;
        private int index;

        RowHelper(int n) {
            this.columns = n;
        }

        public <T extends LayoutElement> T addChild(T t) {
            return this.addChild(t, 1);
        }

        public <T extends LayoutElement> T addChild(T t, int n) {
            return this.addChild(t, n, this.defaultCellSetting());
        }

        public <T extends LayoutElement> T addChild(T t, LayoutSettings layoutSettings) {
            return this.addChild(t, 1, layoutSettings);
        }

        public <T extends LayoutElement> T addChild(T t, int n, LayoutSettings layoutSettings) {
            int n2 = this.index / this.columns;
            int n3 = this.index % this.columns;
            if (n3 + n > this.columns) {
                ++n2;
                n3 = 0;
                this.index = Mth.roundToward(this.index, this.columns);
            }
            this.index += n;
            return GridLayout.this.addChild(t, n2, n3, 1, n, layoutSettings);
        }

        public GridLayout getGrid() {
            return GridLayout.this;
        }

        public LayoutSettings newCellSettings() {
            return GridLayout.this.newCellSettings();
        }

        public LayoutSettings defaultCellSetting() {
            return GridLayout.this.defaultCellSetting();
        }
    }
}

