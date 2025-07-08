/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer.resolver;

import com.mojang.logging.LogUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.slf4j.Logger;

@FunctionalInterface
public interface ServerAddressResolver {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ServerAddressResolver SYSTEM = serverAddress -> {
        try {
            InetAddress inetAddress = InetAddress.getByName(serverAddress.getHost());
            return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(inetAddress, serverAddress.getPort())));
        }
        catch (UnknownHostException unknownHostException) {
            LOGGER.debug("Couldn't resolve server {} address", (Object)serverAddress.getHost(), (Object)unknownHostException);
            return Optional.empty();
        }
    };

    public Optional<ResolvedServerAddress> resolve(ServerAddress var1);
}

