/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntImmutableList
 */
package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.world.level.block.state.properties.Property;

public final class IntegerProperty
extends Property<Integer> {
    private final IntImmutableList values;
    private final int min;
    private final int max;

    private IntegerProperty(String string, int n, int n2) {
        super(string, Integer.class);
        if (n < 0) {
            throw new IllegalArgumentException("Min value of " + string + " must be 0 or greater");
        }
        if (n2 <= n) {
            throw new IllegalArgumentException("Max value of " + string + " must be greater than min (" + n + ")");
        }
        this.min = n;
        this.max = n2;
        this.values = IntImmutableList.toList((IntStream)IntStream.range(n, n2 + 1));
    }

    @Override
    public List<Integer> getPossibleValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof IntegerProperty) {
            IntegerProperty integerProperty = (IntegerProperty)object;
            if (super.equals(object)) {
                return this.values.equals(integerProperty.values);
            }
        }
        return false;
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public static IntegerProperty create(String string, int n, int n2) {
        return new IntegerProperty(string, n, n2);
    }

    @Override
    public Optional<Integer> getValue(String string) {
        try {
            int n = Integer.parseInt(string);
            return n >= this.min && n <= this.max ? Optional.of(n) : Optional.empty();
        }
        catch (NumberFormatException numberFormatException) {
            return Optional.empty();
        }
    }

    @Override
    public String getName(Integer n) {
        return n.toString();
    }

    @Override
    public int getInternalIndex(Integer n) {
        if (n <= this.max) {
            return n - this.min;
        }
        return -1;
    }

    @Override
    public /* synthetic */ int getInternalIndex(Comparable comparable) {
        return this.getInternalIndex((Integer)comparable);
    }

    @Override
    public /* synthetic */ String getName(Comparable comparable) {
        return this.getName((Integer)comparable);
    }
}

