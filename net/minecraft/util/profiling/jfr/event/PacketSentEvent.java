/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.event;

import java.net.SocketAddress;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.PacketEvent;

@Name(value="minecraft.PacketSent")
@Label(value="Network Packet Sent")
@DontObfuscate
public class PacketSentEvent
extends PacketEvent {
    public static final String NAME = "minecraft.PacketSent";
    public static final EventType TYPE = EventType.getEventType(PacketSentEvent.class);

    public PacketSentEvent(String string, String string2, String string3, SocketAddress socketAddress, int n) {
        super(string, string2, string3, socketAddress, n);
    }
}

