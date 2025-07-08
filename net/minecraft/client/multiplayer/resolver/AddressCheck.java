/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Streams
 *  com.mojang.blocklist.BlockListSupplier
 */
package net.minecraft.client.multiplayer.resolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.blocklist.BlockListSupplier;
import java.util.Objects;
import java.util.ServiceLoader;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public interface AddressCheck {
    public boolean isAllowed(ResolvedServerAddress var1);

    public boolean isAllowed(ServerAddress var1);

    public static AddressCheck createFromService() {
        final ImmutableList immutableList = (ImmutableList)Streams.stream(ServiceLoader.load(BlockListSupplier.class)).map(BlockListSupplier::createBlockList).filter(Objects::nonNull).collect(ImmutableList.toImmutableList());
        return new AddressCheck(){

            @Override
            public boolean isAllowed(ResolvedServerAddress resolvedServerAddress) {
                String string = resolvedServerAddress.getHostName();
                String string2 = resolvedServerAddress.getHostIp();
                return immutableList.stream().noneMatch(predicate -> predicate.test(string) || predicate.test(string2));
            }

            @Override
            public boolean isAllowed(ServerAddress serverAddress) {
                String string = serverAddress.getHost();
                return immutableList.stream().noneMatch(predicate -> predicate.test(string));
            }
        };
    }
}

