/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum FrontAndTop implements StringRepresentable
{
    DOWN_EAST("down_east", Direction.DOWN, Direction.EAST),
    DOWN_NORTH("down_north", Direction.DOWN, Direction.NORTH),
    DOWN_SOUTH("down_south", Direction.DOWN, Direction.SOUTH),
    DOWN_WEST("down_west", Direction.DOWN, Direction.WEST),
    UP_EAST("up_east", Direction.UP, Direction.EAST),
    UP_NORTH("up_north", Direction.UP, Direction.NORTH),
    UP_SOUTH("up_south", Direction.UP, Direction.SOUTH),
    UP_WEST("up_west", Direction.UP, Direction.WEST),
    WEST_UP("west_up", Direction.WEST, Direction.UP),
    EAST_UP("east_up", Direction.EAST, Direction.UP),
    NORTH_UP("north_up", Direction.NORTH, Direction.UP),
    SOUTH_UP("south_up", Direction.SOUTH, Direction.UP);

    private static final int NUM_DIRECTIONS;
    private static final FrontAndTop[] BY_TOP_FRONT;
    private final String name;
    private final Direction top;
    private final Direction front;

    private static int lookupKey(Direction direction, Direction direction2) {
        return direction.ordinal() * NUM_DIRECTIONS + direction2.ordinal();
    }

    private FrontAndTop(String string2, Direction direction, Direction direction2) {
        this.name = string2;
        this.front = direction;
        this.top = direction2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static FrontAndTop fromFrontAndTop(Direction direction, Direction direction2) {
        return BY_TOP_FRONT[FrontAndTop.lookupKey(direction, direction2)];
    }

    public Direction front() {
        return this.front;
    }

    public Direction top() {
        return this.top;
    }

    static {
        NUM_DIRECTIONS = Direction.values().length;
        BY_TOP_FRONT = Util.make(new FrontAndTop[NUM_DIRECTIONS * NUM_DIRECTIONS], frontAndTopArray -> {
            FrontAndTop[] frontAndTopArray2 = FrontAndTop.values();
            int n = frontAndTopArray2.length;
            for (int i = 0; i < n; ++i) {
                FrontAndTop frontAndTop;
                frontAndTopArray[FrontAndTop.lookupKey((Direction)frontAndTop.front, (Direction)frontAndTop.top)] = frontAndTop = frontAndTopArray2[i];
            }
        });
    }
}

