/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public record TicketType(long timeout, boolean persist, TicketUse use) {
    public static final long NO_TIMEOUT = 0L;
    public static final TicketType START = TicketType.register("start", 0L, false, TicketUse.LOADING_AND_SIMULATION);
    public static final TicketType DRAGON = TicketType.register("dragon", 0L, false, TicketUse.LOADING_AND_SIMULATION);
    public static final TicketType PLAYER_LOADING = TicketType.register("player_loading", 0L, false, TicketUse.LOADING);
    public static final TicketType PLAYER_SIMULATION = TicketType.register("player_simulation", 0L, false, TicketUse.SIMULATION);
    public static final TicketType FORCED = TicketType.register("forced", 0L, true, TicketUse.LOADING_AND_SIMULATION);
    public static final TicketType PORTAL = TicketType.register("portal", 300L, true, TicketUse.LOADING_AND_SIMULATION);
    public static final TicketType ENDER_PEARL = TicketType.register("ender_pearl", 40L, false, TicketUse.LOADING_AND_SIMULATION);
    public static final TicketType UNKNOWN = TicketType.register("unknown", 1L, false, TicketUse.LOADING);

    private static TicketType register(String string, long l, boolean bl, TicketUse ticketUse) {
        return Registry.register(BuiltInRegistries.TICKET_TYPE, string, new TicketType(l, bl, ticketUse));
    }

    public boolean doesLoad() {
        return this.use == TicketUse.LOADING || this.use == TicketUse.LOADING_AND_SIMULATION;
    }

    public boolean doesSimulate() {
        return this.use == TicketUse.SIMULATION || this.use == TicketUse.LOADING_AND_SIMULATION;
    }

    public boolean hasTimeout() {
        return this.timeout != 0L;
    }

    public static enum TicketUse {
        LOADING,
        SIMULATION,
        LOADING_AND_SIMULATION;

    }
}

